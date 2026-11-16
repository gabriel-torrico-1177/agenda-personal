package com.gabriel.agenda.datos;

import android.content.Context;
import android.content.SharedPreferences;

// Datos: guarda el nombre del estudiante. Es el único lugar que usa SharedPreferences.
// SharedPreferences = un archivo pequeño del teléfono para guardar pares "clave = valor"
// (aquí "nombre = Gabriel"). Para un solo dato es más simple que una tabla de SQLite,
// y también se conserva al cerrar la app
public class PerfilDatos {

    // Nombre del archivo y de la clave
    private static final String ARCHIVO = "perfil";
    private static final String CLAVE_NOMBRE = "nombre";

    private final SharedPreferences preferencias;

    // Constructor: abre (o crea) el archivo "perfil". MODE_PRIVATE = solo esta app puede leerlo
    public PerfilDatos(Context context) {
        preferencias = context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    // Guarda el nombre. edit() abre el archivo para escribir, putString cambia el valor
    // y apply() lo guarda (en segundo plano, sin hacer esperar a la pantalla)
    public void guardarNombre(String nombre) {
        preferencias.edit().putString(CLAVE_NOMBRE, nombre).apply();
    }

    // Devuelve el nombre guardado, o "" si todavía no hay ninguno (el segundo parámetro)
    public String obtenerNombre() {
        return preferencias.getString(CLAVE_NOMBRE, "");
    }
}
