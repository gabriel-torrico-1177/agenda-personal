package com.gabriel.agenda.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gabriel.agenda.modelo.Contacto;

import java.util.ArrayList;
import java.util.List;

// DAO (Data Access Object): guarda, lee, cambia y borra contactos en la tabla "contacto"
public class ContactoDAO {

    private static final String TABLA = "contacto";
    // Orden A-Z sin importar mayúsculas/minúsculas
    private static final String ORDEN_NOMBRE = "nombre COLLATE NOCASE ASC";

    private final BaseDatos baseDatos;

    // Constructor: prepara el acceso a la base de datos
    public ContactoDAO(Context context) {
        baseDatos = new BaseDatos(context);
    }

    // Inserta un contacto nuevo. Devuelve el id generado, o -1 si hubo un error
    public long insertar(Contacto c) {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nombre", c.getNombre());
        valores.put("telefono", c.getTelefono());
        valores.put("correo", c.getCorreo());
        return db.insert(TABLA, null, valores);
    }

    // Devuelve todos los contactos ordenados por nombre (A-Z)
    public List<Contacto> obtenerTodos() {
        List<Contacto> lista = new ArrayList<>();
        SQLiteDatabase db = baseDatos.getReadableDatabase();
        Cursor cursor = db.query(TABLA, null, null, null, null, null, ORDEN_NOMBRE);
        try {
            // Recorre el cursor fila por fila
            while (cursor.moveToNext()) {
                lista.add(leerContacto(cursor));
            }
        } finally {
            cursor.close(); // siempre se cierra, aunque haya un error
        }
        return lista;
    }

    // Devuelve los contactos cuyo nombre contiene el texto (LIKE no distingue mayúsculas)
    public List<Contacto> buscarPorNombre(String texto) {
        List<Contacto> lista = new ArrayList<>();
        SQLiteDatabase db = baseDatos.getReadableDatabase();
        String[] parametros = {"%" + texto + "%"}; // % = cualquier texto antes o después
        Cursor cursor = db.query(TABLA, null, "nombre LIKE ?", parametros,
                null, null, ORDEN_NOMBRE);
        try {
            while (cursor.moveToNext()) {
                lista.add(leerContacto(cursor));
            }
        } finally {
            cursor.close();
        }
        return lista;
    }

    // Devuelve el contacto con ese id, o null si no existe
    public Contacto buscarPorId(int id) {
        Contacto contacto = null;
        SQLiteDatabase db = baseDatos.getReadableDatabase();
        String[] parametros = {String.valueOf(id)};
        Cursor cursor = db.query(TABLA, null, "id = ?", parametros, null, null, null);
        try {
            // moveToFirst devuelve false si no hay ninguna fila
            if (cursor.moveToFirst()) {
                contacto = leerContacto(cursor);
            }
        } finally {
            cursor.close();
        }
        return contacto;
    }

    // Cambia los datos de un contacto. Devuelve cuántas filas se actualizaron (1 si salió bien)
    public int actualizar(Contacto c) {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nombre", c.getNombre());
        valores.put("telefono", c.getTelefono());
        valores.put("correo", c.getCorreo());
        String[] parametros = {String.valueOf(c.getId())};
        return db.update(TABLA, valores, "id = ?", parametros);
    }

    // Borra un contacto. Devuelve cuántas filas se borraron (1 si salió bien)
    public int eliminar(int id) {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        String[] parametros = {String.valueOf(id)};
        return db.delete(TABLA, "id = ?", parametros);
    }

    // Convierte la fila actual del cursor en un objeto Contacto
    private Contacto leerContacto(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
        String telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono"));
        String correo = cursor.getString(cursor.getColumnIndexOrThrow("correo"));
        return new Contacto(id, nombre, telefono, correo);
    }
}