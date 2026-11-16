package com.gabriel.agenda.controlador;

import android.content.Context;

import com.gabriel.agenda.datos.SesionDAO;
import com.gabriel.agenda.modelo.SesionEstudio;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Controlador: valida los datos de una sesión de estudio y le pide al DAO que los guarde o los lea
public class ControladorSesion {

    // Mensajes que la pantalla muestra (públicos para poder compararlos desde la vista)
    public static final String ERROR_MATERIA = "Elija una materia";
    public static final String ERROR_HORAS = "La hora de fin debe ser posterior a la de inicio";
    public static final String ERROR_CRUCE = "Ya tienes una sesión en ese horario";
    public static final String ERROR_COMPLETAR_FUTURA = "No puedes completar una sesión que todavía no terminó";
    public static final String ERROR_FECHA_HORA = "Elija la fecha y las horas";
    public static final String SIN_MATERIAS = "Primero registre una materia";
    public static final String SESION_GUARDADA = "Sesión guardada";
    public static final String SESION_ACTUALIZADA = "Sesión actualizada";
    public static final String SESION_ELIMINADA = "Sesión eliminada";
    public static final String ERROR_GUARDAR = "No se pudo guardar";

    // Lo que devuelve porcentajeEnCurso cuando la sesión NO está en curso
    public static final int NO_EN_CURSO = -1;
    // Un minuto en milisegundos (60 segundos * 1000)
    private static final long MILIS_POR_MINUTO = 60 * 1000;

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

    // Devuelve la próxima sesión sin completar (desde este momento), o null si no hay.
    // La pantalla de Inicio la muestra en "Próxima sesión"
    public SesionEstudio proximaSesion() {
        Calendar ahora = Calendar.getInstance();
        // Fecha y hora de ahora en el mismo formato que la tabla: "2026-11-15" y "19:30"
        String fecha = String.format(Locale.ROOT, "%04d-%02d-%02d", ahora.get(Calendar.YEAR),
                ahora.get(Calendar.MONTH) + 1, ahora.get(Calendar.DAY_OF_MONTH));
        String hora = String.format(Locale.ROOT, "%02d:%02d",
                ahora.get(Calendar.HOUR_OF_DAY), ahora.get(Calendar.MINUTE));
        return sesionDAO.buscarProxima(fecha, hora);
    }

    // Dice cuánto avanzó una sesión que está "En curso", de 0 a 99 (%).
    // Está en curso si: es "Pendiente", es de hoy y la hora actual está entre su inicio y su fin.
    // Si no está en curso devuelve NO_EN_CURSO (-1). No se guarda nada: se calcula cada vez,
    // así la meta, el reporte y las estadísticas siguen contando solo las "Completada"
    public int porcentajeEnCurso(SesionEstudio sesion) {
        // 1. Solo las pendientes (una completada o reprogramada nunca está en curso)
        if (!sesion.getEstado().equals(SesionEstudio.ESTADO_PENDIENTE)) {
            return NO_EN_CURSO;
        }

        // 2. Solo las de hoy: se compara la fecha de la sesión con la de hoy ("2026-11-16")
        Calendar ahora = Calendar.getInstance();
        String hoy = String.format(Locale.ROOT, "%04d-%02d-%02d", ahora.get(Calendar.YEAR),
                ahora.get(Calendar.MONTH) + 1, ahora.get(Calendar.DAY_OF_MONTH));
        if (!sesion.getFecha().equals(hoy)) {
            return NO_EN_CURSO;
        }

        // 3. La hora actual en minutos desde las 00:00 (11:30 → 690), igual que el inicio y el fin
        int minutosAhora = ahora.get(Calendar.HOUR_OF_DAY) * 60 + ahora.get(Calendar.MINUTE);
        // Todavía no empezó (antes del inicio) o ya terminó (desde el fin en adelante)
        if (minutosAhora < sesion.getInicioEnMinutos() || minutosAhora >= sesion.getFinEnMinutos()) {
            return NO_EN_CURSO;
        }

        // 4. Porcentaje = minutos que pasaron * 100 / duración (división entera).
        //    Ejemplo: de 11:00 a 12:00, a las 11:30 → 30 * 100 / 60 = 50 %.
        //    La duración nunca es 0 aquí: si inicio <= ahora < fin, el fin es mayor que el inicio
        int minutosPasados = minutosAhora - sesion.getInicioEnMinutos();
        return minutosPasados * 100 / sesion.getDuracionMinutos();
    }

    // Milisegundos que faltan para que empiece el siguiente minuto del reloj
    // (a las 11:30:45 faltan 15 segundos = 15000). Las pantallas lo usan para refrescar
    // la lista justo cuando cambia el minuto, y no hasta 59 segundos tarde
    public long milisHastaElSiguienteMinuto() {
        // % (resto): cuántos milisegundos ya pasaron dentro del minuto actual
        return MILIS_POR_MINUTO - System.currentTimeMillis() % MILIS_POR_MINUTO;
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

        // 6. Solo se puede marcar "Completada" una sesión que ya terminó
        if (sesion.getEstado().equals(SesionEstudio.ESTADO_COMPLETADA) && todaviaNoTermino(sesion)) {
            return ERROR_COMPLETAR_FUTURA;
        }

        // 7. No puede cruzarse con otra sesión del mismo día (de cualquier materia)
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

    // Dice si la fecha y la hora de fin de la sesión son posteriores a este momento.
    // Arma dos textos con el mismo formato "AAAA-MM-DD HH:MM" y los compara con compareTo:
    // como todas las partes tienen ceros a la izquierda, el orden alfabético es el orden del tiempo
    // ("2026-11-16 20:30" va después de "2026-11-16 19:05")
    private boolean todaviaNoTermino(SesionEstudio sesion) {
        Calendar ahora = Calendar.getInstance(); // fecha y hora del teléfono
        String textoAhora = String.format(Locale.ROOT, "%04d-%02d-%02d %02d:%02d",
                ahora.get(Calendar.YEAR), ahora.get(Calendar.MONTH) + 1, // los meses empiezan en 0
                ahora.get(Calendar.DAY_OF_MONTH),
                ahora.get(Calendar.HOUR_OF_DAY), ahora.get(Calendar.MINUTE));
        String finSesion = sesion.getFecha() + " " + sesion.getHoraFin();
        // compareTo > 0: el fin de la sesión va después de ahora (todavía no llega).
        // Si termina justo en este minuto (son iguales), ya cuenta como terminada
        return finSesion.compareTo(textoAhora) > 0;
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