package com.gabriel.agenda.datos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Crea y abre la base de datos SQLite de la agenda (capa de datos)
public class BaseDatos extends SQLiteOpenHelper {

    // Nombre del archivo de la base de datos y su versión
    // Versión 1: contacto y evento. Versión 2: quita evento y agrega materia, sesion y meta_semanal
    private static final String NOMBRE_BD = "agenda.db";
    private static final int VERSION_BD = 2;

    // SQL para crear la tabla de contactos
    private static final String CREAR_TABLA_CONTACTO =
            "CREATE TABLE contacto ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "nombre TEXT NOT NULL, "
                    + "telefono TEXT NOT NULL, "
                    + "correo TEXT)";

    // SQL para crear la tabla de materias (el nombre no se puede repetir; color por defecto azul)
    private static final String CREAR_TABLA_MATERIA =
            "CREATE TABLE materia ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "nombre TEXT NOT NULL UNIQUE, "
                    + "docente TEXT, "
                    + "color TEXT NOT NULL DEFAULT '#3F51B5')";

    // SQL para crear la tabla de sesiones de estudio
    // materia_id apunta a materia(id): si se borra la materia, se borran sus sesiones (ON DELETE CASCADE)
    private static final String CREAR_TABLA_SESION =
            "CREATE TABLE sesion ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "materia_id INTEGER NOT NULL REFERENCES materia(id) ON DELETE CASCADE, "
                    + "fecha TEXT NOT NULL, "         // formato AAAA-MM-DD
                    + "hora_inicio TEXT NOT NULL, "   // formato HH:MM
                    + "hora_fin TEXT NOT NULL, "      // formato HH:MM
                    + "estado TEXT NOT NULL DEFAULT 'Pendiente' "
                    + "CHECK (estado IN ('Pendiente', 'Completada', 'Reprogramada')), "
                    + "notas TEXT)";

    // SQL para crear la tabla de metas semanales (una meta por semana, entre 1 y 80 horas)
    private static final String CREAR_TABLA_META =
            "CREATE TABLE meta_semanal ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "semana_inicio TEXT NOT NULL UNIQUE, "   // AAAA-MM-DD del lunes
                    + "horas_objetivo INTEGER NOT NULL CHECK (horas_objetivo BETWEEN 1 AND 80))";

    // Constructor: recibe el Context (la pantalla o la app) que usa la base de datos
    public BaseDatos(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    // Se ejecuta CADA vez que se abre la base de datos, antes de onCreate y onUpgrade
    // SQLite trae las claves foráneas apagadas: las encendemos para que funcione ON DELETE CASCADE
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Se ejecuta UNA sola vez: cuando el archivo agenda.db todavía no existe (app recién instalada)
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREAR_TABLA_CONTACTO);
        crearTablasVersion2(db);
    }

    // Se ejecuta cuando el teléfono tiene una versión vieja de agenda.db (por ejemplo, la 1)
    // No borra la tabla contacto: los contactos ya guardados se conservan
    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        if (versionAnterior < 2) {
            db.execSQL("DROP TABLE IF EXISTS evento");   // la tabla evento ya no se usa
            crearTablasVersion2(db);
        }
    }

    // Crea las tablas nuevas de la versión 2 (materia primero, porque sesion apunta a ella)
    private void crearTablasVersion2(SQLiteDatabase db) {
        db.execSQL(CREAR_TABLA_MATERIA);
        db.execSQL(CREAR_TABLA_SESION);
        db.execSQL(CREAR_TABLA_META);
    }
}