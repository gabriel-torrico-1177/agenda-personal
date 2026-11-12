package com.gabriel.agenda.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gabriel.agenda.modelo.Materia;

import java.util.ArrayList;
import java.util.List;

// DAO (Data Access Object): guarda, lee, cambia y borra materias en la tabla "materia"
public class MateriaDAO {

    private static final String TABLA = "materia";
    // Orden A-Z sin importar mayúsculas/minúsculas
    private static final String ORDEN_NOMBRE = "nombre COLLATE NOCASE ASC";

    private final BaseDatos baseDatos;

    // Constructor: prepara el acceso a la base de datos
    public MateriaDAO(Context context) {
        baseDatos = new BaseDatos(context);
    }

    // Inserta una materia nueva. Devuelve el id generado, o -1 si hubo un error
    public long insertar(Materia m) {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nombre", m.getNombre());
        valores.put("docente", m.getDocente());
        valores.put("color", m.getColor());
        return db.insert(TABLA, null, valores);
    }

    // Devuelve todas las materias ordenadas por nombre (A-Z)
    public List<Materia> obtenerTodas() {
        List<Materia> lista = new ArrayList<>();
        SQLiteDatabase db = baseDatos.getReadableDatabase();
        Cursor cursor = db.query(TABLA, null, null, null, null, null, ORDEN_NOMBRE);
        try {
            // Recorre el cursor fila por fila
            while (cursor.moveToNext()) {
                lista.add(leerMateria(cursor));
            }
        } finally {
            cursor.close(); // siempre se cierra, aunque haya un error
        }
        return lista;
    }

    // Devuelve la materia con ese id, o null si no existe
    public Materia buscarPorId(int id) {
        Materia materia = null;
        SQLiteDatabase db = baseDatos.getReadableDatabase();
        String[] parametros = {String.valueOf(id)};
        Cursor cursor = db.query(TABLA, null, "id = ?", parametros, null, null, null);
        try {
            // moveToFirst devuelve false si no hay ninguna fila
            if (cursor.moveToFirst()) {
                materia = leerMateria(cursor);
            }
        } finally {
            cursor.close();
        }
        return materia;
    }

    // Dice si OTRA materia ya usa ese nombre (sin importar mayúsculas/minúsculas).
    // idExcluir: id de la materia que se está editando, para no compararla consigo misma.
    // Para una materia nueva se pasa 0 (SQLite empieza los id en 1, así que ninguno es 0).
    public boolean existeNombre(String nombre, int idExcluir) {
        SQLiteDatabase db = baseDatos.getReadableDatabase();
        String[] parametros = {nombre, String.valueOf(idExcluir)};
        Cursor cursor = db.query(TABLA, new String[]{"id"},
                "nombre = ? COLLATE NOCASE AND id <> ?", parametros,
                null, null, null);
        try {
            // Si el cursor trae al menos una fila, el nombre ya está ocupado
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    // Cambia los datos de una materia. Devuelve cuántas filas se actualizaron (1 si salió bien)
    public int actualizar(Materia m) {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nombre", m.getNombre());
        valores.put("docente", m.getDocente());
        valores.put("color", m.getColor());
        String[] parametros = {String.valueOf(m.getId())};
        return db.update(TABLA, valores, "id = ?", parametros);
    }

    // Borra una materia. Sus sesiones se borran solas por el ON DELETE CASCADE.
    // Devuelve cuántas filas se borraron (1 si salió bien)
    public int eliminar(int id) {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        String[] parametros = {String.valueOf(id)};
        return db.delete(TABLA, "id = ?", parametros);
    }

    // Convierte la fila actual del cursor en un objeto Materia
    private Materia leerMateria(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
        String docente = cursor.getString(cursor.getColumnIndexOrThrow("docente"));
        String color = cursor.getString(cursor.getColumnIndexOrThrow("color"));
        return new Materia(id, nombre, docente, color);
    }
}