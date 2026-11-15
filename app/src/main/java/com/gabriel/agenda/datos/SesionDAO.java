package com.gabriel.agenda.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gabriel.agenda.modelo.SesionEstudio;

import java.util.ArrayList;
import java.util.List;

// DAO (Data Access Object): guarda, lee, cambia y borra sesiones en la tabla "sesion"
public class SesionDAO {

    private static final String TABLA = "sesion";

    // Consulta base para LEER sesiones: une cada sesión (s) con su materia (m)
    // para traer también el nombre y el color de la materia.
    // INNER JOIN ... ON s.materia_id = m.id → "pega" a cada sesión la fila de su materia
    private static final String SELECT_CON_MATERIA =
            "SELECT s.id, s.materia_id, "
                    + "m.nombre AS nombre_materia, m.color AS color_materia, "
                    + "s.fecha, s.hora_inicio, s.hora_fin, s.estado, s.notas "
                    + "FROM sesion s "
                    + "INNER JOIN materia m ON s.materia_id = m.id ";

    private final BaseDatos baseDatos;

    // Constructor: prepara el acceso a la base de datos
    public SesionDAO(Context context) {
        baseDatos = new BaseDatos(context);
    }

    // Inserta una sesión nueva. Devuelve el id generado, o -1 si hubo un error
    public long insertar(SesionEstudio s) {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        return db.insert(TABLA, null, crearValores(s));
    }

    // Devuelve las sesiones de un día ("2026-11-13"), ordenadas por hora de inicio
    public List<SesionEstudio> buscarPorFecha(String fecha) {
        String sql = SELECT_CON_MATERIA
                + "WHERE s.fecha = ? "
                + "ORDER BY s.hora_inicio";
        return leerLista(sql, new String[]{fecha});
    }

    // Devuelve las sesiones de la semana que empieza en ese lunes (de lunes a domingo).
    // date(?, '+6 days') es una función de SQLite: al lunes "2026-11-09" le suma 6 días → "2026-11-15"
    public List<SesionEstudio> buscarPorSemana(String lunes) {
        String sql = SELECT_CON_MATERIA
                + "WHERE s.fecha BETWEEN ? AND date(?, '+6 days') "
                + "ORDER BY s.fecha, s.hora_inicio";
        return leerLista(sql, new String[]{lunes, lunes});
    }

    // Devuelve la próxima sesión sin completar desde ese momento ("2026-11-15", "19:30"), o null.
    // Cuenta las de días siguientes y las de hoy que todavía no terminaron (hora_fin > ahora).
    // LIMIT 1: de todas las que cumplen, solo trae la primera según el orden
    public SesionEstudio buscarProxima(String fecha, String hora) {
        String sql = SELECT_CON_MATERIA
                + "WHERE s.estado <> 'Completada' "
                + "AND (s.fecha > ? OR (s.fecha = ? AND s.hora_fin > ?)) "
                + "ORDER BY s.fecha, s.hora_inicio "
                + "LIMIT 1";
        List<SesionEstudio> lista = leerLista(sql, new String[]{fecha, fecha, hora});
        if (lista.isEmpty()) {
            return null;
        }
        return lista.get(0);
    }

    // Devuelve la sesión con ese id (con su materia), o null si no existe
    public SesionEstudio buscarPorId(int id) {
        String sql = SELECT_CON_MATERIA + "WHERE s.id = ?";
        List<SesionEstudio> lista = leerLista(sql, new String[]{String.valueOf(id)});
        if (lista.isEmpty()) {
            return null;
        }
        return lista.get(0);
    }

    // Cambia los datos de una sesión. Devuelve cuántas filas se actualizaron (1 si salió bien)
    public int actualizar(SesionEstudio s) {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        String[] parametros = {String.valueOf(s.getId())};
        return db.update(TABLA, crearValores(s), "id = ?", parametros);
    }

    // Borra una sesión. Devuelve cuántas filas se borraron (1 si salió bien)
    public int eliminar(int id) {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        String[] parametros = {String.valueOf(id)};
        return db.delete(TABLA, "id = ?", parametros);
    }

    // Arma los valores a guardar. El nombre y el color de la materia NO se guardan:
    // en la tabla sesion solo va materia_id
    private ContentValues crearValores(SesionEstudio s) {
        ContentValues valores = new ContentValues();
        valores.put("materia_id", s.getMateriaId());
        valores.put("fecha", s.getFecha());
        valores.put("hora_inicio", s.getHoraInicio());
        valores.put("hora_fin", s.getHoraFin());
        valores.put("estado", s.getEstado());
        valores.put("notas", s.getNotas());
        return valores;
    }

    // Ejecuta una consulta con parámetros "?" y convierte cada fila en una SesionEstudio
    private List<SesionEstudio> leerLista(String sql, String[] parametros) {
        List<SesionEstudio> lista = new ArrayList<>();
        SQLiteDatabase db = baseDatos.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, parametros);
        try {
            // Recorre el cursor fila por fila
            while (cursor.moveToNext()) {
                lista.add(leerSesion(cursor));
            }
        } finally {
            cursor.close(); // siempre se cierra, aunque haya un error
        }
        return lista;
    }

    // Convierte la fila actual del cursor en un objeto SesionEstudio
    private SesionEstudio leerSesion(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        int materiaId = cursor.getInt(cursor.getColumnIndexOrThrow("materia_id"));
        String nombreMateria = cursor.getString(cursor.getColumnIndexOrThrow("nombre_materia"));
        String colorMateria = cursor.getString(cursor.getColumnIndexOrThrow("color_materia"));
        String fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"));
        String horaInicio = cursor.getString(cursor.getColumnIndexOrThrow("hora_inicio"));
        String horaFin = cursor.getString(cursor.getColumnIndexOrThrow("hora_fin"));
        String estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"));
        String notas = cursor.getString(cursor.getColumnIndexOrThrow("notas"));
        return new SesionEstudio(id, materiaId, nombreMateria, colorMateria,
                fecha, horaInicio, horaFin, estado, notas);
    }
}