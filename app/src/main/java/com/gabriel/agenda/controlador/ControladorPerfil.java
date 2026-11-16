package com.gabriel.agenda.controlador;

import android.content.Context;

import com.gabriel.agenda.datos.PerfilDatos;

import java.util.Locale;

// Controlador: valida el nombre del estudiante y le pide a PerfilDatos que lo guarde o lo lea
public class ControladorPerfil {

    // Mensajes que la pantalla muestra (públicos para poder compararlos desde la vista)
    public static final String ERROR_NOMBRE = "Ingrese su nombre";
    public static final String NOMBRE_GUARDADO = "Nombre guardado";
    // Inicial del avatar cuando todavía no hay nombre
    private static final String SIN_INICIAL = "?";

    private final PerfilDatos perfilDatos;

    // Constructor: crea la clase de datos con la que se guarda el nombre
    public ControladorPerfil(Context context) {
        perfilDatos = new PerfilDatos(context);
    }

    // Valida y guarda el nombre. Devuelve el mensaje para la pantalla
    public String guardarNombre(String nombre) {
        // 1. Quitar los espacios de los extremos (null cuenta como vacío)
        String limpio = (nombre == null) ? "" : nombre.trim();
        // 2. No puede quedar vacío
        if (limpio.isEmpty()) {
            return ERROR_NOMBRE; // no se guarda nada
        }
        perfilDatos.guardarNombre(limpio);
        return NOMBRE_GUARDADO;
    }

    // Devuelve el nombre guardado, o "" si no hay
    public String obtenerNombre() {
        return perfilDatos.obtenerNombre();
    }

    // Dice si el estudiante ya escribió su nombre (Inicio lo usa para pedirlo la primera vez)
    public boolean tieneNombre() {
        return !obtenerNombre().isEmpty();
    }

    // Primera letra del nombre en mayúscula para el avatar ("gabriel" → "G"), o "?" si no hay nombre.
    // Locale "es": las reglas de mayúsculas del español ("á" → "Á")
    public String obtenerInicial() {
        String nombre = obtenerNombre();
        if (nombre.isEmpty()) {
            return SIN_INICIAL;
        }
        return nombre.substring(0, 1).toUpperCase(Locale.forLanguageTag("es"));
    }
}
