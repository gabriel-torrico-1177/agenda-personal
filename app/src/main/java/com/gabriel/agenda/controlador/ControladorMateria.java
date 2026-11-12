package com.gabriel.agenda.controlador;

import android.content.Context;

import com.gabriel.agenda.datos.MateriaDAO;
import com.gabriel.agenda.modelo.Materia;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

// Controlador: valida los datos de una materia y le pide al DAO que los guarde o los lea
public class ControladorMateria {

    // Mensajes que la pantalla muestra (públicos para poder compararlos desde la vista)
    public static final String ERROR_NOMBRE = "El nombre es obligatorio";
    public static final String ERROR_NOMBRE_REPETIDO = "Ya existe una materia con ese nombre";
    public static final String MATERIA_GUARDADA = "Materia guardada";
    public static final String MATERIA_ACTUALIZADA = "Materia actualizada";
    public static final String MATERIA_ELIMINADA = "Materia eliminada";
    public static final String ERROR_GUARDAR = "No se pudo guardar";

    // Color que se usa si el estudiante no elige ninguno (azul)
    public static final String COLOR_POR_DEFECTO = "#3F51B5";

    private final MateriaDAO materiaDAO;

    // Constructor: crea el DAO con el que se habla con la base de datos
    public ControladorMateria(Context context) {
        materiaDAO = new MateriaDAO(context);
    }

    // Valida y guarda una materia nueva. Devuelve el mensaje para la pantalla
    public String registrar(String nombre, String docente, String color) {
        // Una materia nueva todavía no tiene id: getId() vale 0 (ningún id real es 0)
        Materia materia = new Materia(nombre, docente, color);

        String error = validar(materia);
        if (error != null) {
            return error; // no se guarda nada
        }

        long id = materiaDAO.insertar(materia);
        if (id == -1) {
            return ERROR_GUARDAR;
        }
        return MATERIA_GUARDADA;
    }

    // Devuelve todas las materias ordenadas A-Z (en español: sin importar tildes ni mayúsculas)
    public List<Materia> listar() {
        List<Materia> materias = materiaDAO.obtenerTodas();
        ordenarPorNombre(materias);
        return materias;
    }

    // Devuelve la materia con ese id, o null si no existe
    public Materia obtener(int id) {
        return materiaDAO.buscarPorId(id);
    }

    // Valida y actualiza una materia existente. Devuelve el mensaje para la pantalla
    public String actualizar(Materia materia) {
        String error = validar(materia);
        if (error != null) {
            return error; // no se cambia nada
        }

        int filas = materiaDAO.actualizar(materia);
        if (filas == 0) {
            return ERROR_GUARDAR;
        }
        return MATERIA_ACTUALIZADA;
    }

    // Borra la materia (y sus sesiones, por el ON DELETE CASCADE). Devuelve true si se borró
    public boolean eliminar(int id) {
        return materiaDAO.eliminar(id) > 0;
    }

    // Limpia los datos, pone el color por defecto y revisa las reglas.
    // Devuelve el mensaje de error, o null si todo está bien
    private String validar(Materia materia) {
        // 1. Quitar espacios de los extremos (así "  Física " se guarda como "Física")
        materia.setNombre(limpiar(materia.getNombre()));
        materia.setDocente(limpiar(materia.getDocente()));
        materia.setColor(limpiar(materia.getColor()));

        // 2. El docente es opcional; el color, si no se eligió, toma el valor por defecto
        if (materia.getColor().isEmpty()) {
            materia.setColor(COLOR_POR_DEFECTO);
        }

        // 3. El nombre es obligatorio
        if (materia.getNombre().isEmpty()) {
            return ERROR_NOMBRE;
        }

        // 4. El nombre no se puede repetir. Se excluye el id de la propia materia
        //    (0 si es nueva) para que al editarla no choque consigo misma
        if (materiaDAO.existeNombre(materia.getNombre(), materia.getId())) {
            return ERROR_NOMBRE_REPETIDO;
        }
        return null;
    }

    // Ordena la lista por nombre con las reglas del español: "Álgebra" va junto a "Algoritmos"
    private void ordenarPorNombre(List<Materia> materias) {
        final Collator collator = Collator.getInstance(Locale.forLanguageTag("es"));
        collator.setStrength(Collator.PRIMARY); // PRIMARY: ignora tildes y mayúsculas
        Collections.sort(materias, new Comparator<Materia>() {
            @Override
            public int compare(Materia a, Materia b) {
                return collator.compare(a.getNombre(), b.getNombre());
            }
        });
    }

    // Quita los espacios de los extremos y convierte null en texto vacío
    private String limpiar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim();
    }
}