package com.gabriel.agenda.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gabriel.agenda.modelo.MetaSemanal;

// DAO (Data Access Object): guarda y lee las metas semanales en la tabla "meta_semanal"
public class MetaDAO {

    private static final String TABLA = "meta_semanal";

    private final BaseDatos baseDatos;

    // Constructor: prepara el acceso a la base de datos
    public MetaDAO(Context context) {
        baseDatos = new BaseDatos(context);
    }

    // Guarda la meta de una semana: si esa semana ya tenía meta la ACTUALIZA, si no la INSERTA.
    // (semana_inicio es UNIQUE: no puede haber dos metas para el mismo lunes).
    // Devuelve true si se guardó
    public boolean guardar(MetaSemanal meta) {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("semana_inicio", meta.getSemanaInicio());
        valores.put("horas_objetivo", meta.getHorasObjetivo());

        MetaSemanal existente = buscarPorSemana(meta.getSemanaInicio());
        if (existente == null) {
            // Primera vez que se define la meta de esta semana
            return db.insert(TABLA, null, valores) != -1;
        }
        // Ya había meta: se cambian las horas de esa fila
        String[] parametros = {String.valueOf(existente.getId())};
        return db.update(TABLA, valores, "id = ?", parametros) > 0;
    }

    // Devuelve la meta de la semana que empieza en ese lunes ("2026-11-09"), o null si no tiene
    public MetaSemanal buscarPorSemana(String lunes) {
        MetaSemanal meta = null;
        SQLiteDatabase db = baseDatos.getReadableDatabase();
        String[] parametros = {lunes};
        Cursor cursor = db.query(TABLA, null, "semana_inicio = ?", parametros,
                null, null, null);
        try {
            // moveToFirst devuelve false si no hay ninguna fila
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String semana = cursor.getString(cursor.getColumnIndexOrThrow("semana_inicio"));
                int horas = cursor.getInt(cursor.getColumnIndexOrThrow("horas_objetivo"));
                meta = new MetaSemanal(id, semana, horas);
            }
        } finally {
            cursor.close(); // siempre se cierra, aunque haya un error
        }
        return meta;
    }
}