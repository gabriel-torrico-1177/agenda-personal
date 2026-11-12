package com.gabriel.agenda.controlador;

import android.content.Context;

import com.gabriel.agenda.datos.SesionDAO;
import com.gabriel.agenda.modelo.SesionEstudio;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Controlador: valida los datos de una sesión de estudio y le pide al DAO que los guarde o los lea
public class ControladorSesion {

    // Mensajes que la pantalla muestra (públicos para poder compararlos desde la vista)
    public static final String ERROR_MATERIA = "Elija una materia";
    public static final String ERROR_HORAS = "La hora de fin debe ser posterior a la de inicio";
    public static final String ERROR_CRUCE = "Ya tienes una sesión en ese horario";
    public static final String ERROR_FECHA_HORA = "Elija la fecha y las horas";
    public static final String SIN_MATERIAS = "Primero registre una materia";
    public static final String SESION_GUARDADA = "Sesión guardada";
    public static final String SESION_ACTUALIZADA = "Sesión actualizada";
    public static final String SESION_ELIMINADA = "Sesión eliminada";
    public static final String ERROR_GUARDAR = "No se pudo guardar";

    // Formatos que se guardan en la base de datos (expresiones regulares)
    // \\d{4}-\\d{2}-\\d{2} = AAAA-MM-DD, por ejemplo "2026-11-13"
    private static final String FORMATO_FECHA = "\\d{4}-\\d{2}-\\d{2}";
    // ([01]\\d|2[0-3]):[0-5]\\d = HH:MM de 00:00 a 23:59, siempre con dos dígitos ("09:00", no "9:00")
    private static final String FORMATO_HORA = "([01]\\d|2[0-3]):[0-5]\\d";

    private final SesionDAO sesionDAO;

    // Constructor: crea el DAO con el que se habla con la base de datos
    public ControladorSesion(Context context) {
        sesionDAO = new SesionDAO(context);
    }

    // Valida y guarda una sesión nueva. Devuelve el mensaje para la pantalla
    public String registrar(SesionEstudio sesion) {
        String error = validar(sesion);
        if (error != null) {
            return error; // no se guarda nada
        }

        long id = sesionDAO.insertar(sesion);
        if (id == -1) {
            return ERROR_GUARDAR;
        }
        return SESION_GUARDADA;
    }

    // Devuelve las sesiones de un día ("2026-11-13"), ordenadas por hora de inicio
    public List<SesionEstudio> listarPorFecha(String fecha) {
        return sesionDAO.buscarPorFecha(fecha);
    }

    // Devuelve las fechas ("2026-11-13") de la semana que tienen al menos una sesión.
    // El calendario lo usa para poner un punto debajo de esos días
    public Set<String> diasConSesiones(String lunes) {
        // Set (conjunto): guarda cada fecha una sola vez, aunque ese día tenga varias sesiones
        Set<String> fechas = new HashSet<>();
        for (SesionEstudio sesion : sesionDAO.buscarPorSemana(lunes)) {
            fechas.add(sesion.getFecha());
        }
        return fechas;
    }

    // Devuelve la sesión con ese id, o null si no existe
    public SesionEstudio obtener(int id) {
        return sesionDAO.buscarPorId(id);
    }

    // Valida y actualiza una sesión existente. Devuelve el mensaje para la pantalla
    public String actualizar(SesionEstudio sesion) {
        String error = validar(sesion);
        if (error != null) {
            return error; // no se cambia nada
        }

        int filas = sesionDAO.actualizar(sesion);
        if (filas == 0) {
            return ERROR_GUARDAR;
        }
        return SESION_ACTUALIZADA;
    }

    // Borra la sesión. Devuelve true si se borró
    public boolean eliminar(int id) {
        return sesionDAO.eliminar(id) > 0;
    }

    // Limpia los datos, pone el estado por defecto y revisa las reglas.
    // Devuelve el mensaje de error, o null si todo está bien
    private String validar(SesionEstudio sesion) {
        // 1. Quitar espacios de los extremos (y convertir null en texto vacío)
        sesion.setFecha(limpiar(sesion.getFecha()));
        sesion.setHoraInicio(limpiar(sesion.getHoraInicio()));
        sesion.setHoraFin(limpiar(sesion.getHoraFin()));
        sesion.setEstado(limpiar(sesion.getEstado()));
        sesion.setNotas(limpiar(sesion.getNotas()));

        // 2. Estado: si no es uno de los 3 válidos (por ejemplo, vacío), queda "Pendiente"
        if (!esEstadoValido(sesion.getEstado())) {
            sesion.setEstado(SesionEstudio.ESTADO_PENDIENTE);
        }

        // 3. Hay que elegir una materia (los id de SQLite empiezan en 1)
        if (sesion.getMateriaId() <= 0) {
            return ERROR_MATERIA;
        }

        // 4. La fecha y las horas deben tener el formato correcto.
        //    Con los selectores siempre lo tienen; esto evita que la app se cierre
        //    en getDuracionMinutos() si llegara un dato raro
        if (!sesion.getFecha().matches(FORMATO_FECHA)
                || !sesion.getHoraInicio().matches(FORMATO_HORA)
                || !sesion.getHoraFin().matches(FORMATO_HORA)) {
            return ERROR_FECHA_HORA;
        }

        // 5. La hora de fin debe ser posterior a la de inicio (duración mayor que 0)
        if (sesion.getDuracionMinutos() <= 0) {
            return ERROR_HORAS;
        }

        // 6. No puede cruzarse con otra sesión del mismo día (de cualquier materia)
        if (seCruzaConOtra(sesion)) {
            return ERROR_CRUCE;
        }
        return null;
    }

    // Dice si la sesión se cruza con otra del mismo día.
    // Dos sesiones se cruzan si: inicioNueva < finOtra  Y  finNueva > inicioOtra.
    // Si una empieza justo cuando termina la otra (08:00-09:00 y 09:00-10:00) NO se cruzan
    private boolean seCruzaConOtra(SesionEstudio nueva) {
        for (SesionEstudio otra : sesionDAO.buscarPorFecha(nueva.getFecha())) {
            // Al editar, la sesión no se compara consigo misma (una nueva tiene id 0: nunca coincide)
            if (otra.getId() == nueva.getId()) {
                continue; // salta a la siguiente sesión
            }
            if (nueva.getInicioEnMinutos() < otra.getFinEnMinutos()
                    && nueva.getFinEnMinutos() > otra.getInicioEnMinutos()) {
                return true;
            }
        }
        return false;
    }

    // Dice si el estado es uno de los 3 que acepta la tabla (Pendiente, Completada, Reprogramada)
    private boolean esEstadoValido(String estado) {
        return estado.equals(SesionEstudio.ESTADO_PENDIENTE)
                || estado.equals(SesionEstudio.ESTADO_COMPLETADA)
                || estado.equals(SesionEstudio.ESTADO_REPROGRAMADA);
    }

    // Quita los espacios de los extremos y convierte null en texto vacío
    private String limpiar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim();
    }
}