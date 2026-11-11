package com.gabriel.agenda.datos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Crea y abre la base de datos SQLite de la agenda (capa de datos)
public class BaseDatos extends SQLiteOpenHelper {

    // Nombre del archivo de la base de datos y su versión
    private static final String NOMBRE_BD = "agenda.db";
    private static final int VERSION_BD = 1;

    // SQL para crear la tabla de contactos
    private static final String CREAR_TABLA_CONTACTO =
            "CREATE TABLE contacto ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "nombre TEXT NOT NULL, "
                    + "telefono TEXT NOT NULL, "
                    + "correo TEXT)";

    // SQL para crear la tabla de eventos (tipo solo admite 4 valores, por defecto 'Otro')
    private static final String CREAR_TABLA_EVENTO =
            "CREATE TABLE evento ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "titulo TEXT NOT NULL, "
                    + "tipo TEXT NOT NULL DEFAULT 'Otro' "
                    + "CHECK (tipo IN ('Personal', 'Trabajo', 'Estudio', 'Otro')), "
                    + "fecha TEXT NOT NULL, "   // formato AAAA-MM-DD
                    + "hora TEXT NOT NULL, "    // formato HH:MM
                    + "lugar TEXT, "
                    + "descripcion TEXT)";

    // Constructor: recibe el Context (la pantalla o la app) que usa la base de datos
    public BaseDatos(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    // Se ejecuta UNA sola vez: cuando el archivo agenda.db todavía no existe
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREAR_TABLA_CONTACTO);
        db.execSQL(CREAR_TABLA_EVENTO);
    }

    // Se ejecuta si VERSION_BD sube (por ejemplo, de 1 a 2): borra y vuelve a crear las tablas
    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        db.execSQL("DROP TABLE IF EXISTS contacto");
        db.execSQL("DROP TABLE IF EXISTS evento");
        onCreate(db);
    }
}