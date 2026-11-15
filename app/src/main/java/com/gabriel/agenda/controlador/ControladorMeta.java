package com.gabriel.agenda.controlador;

import android.content.Context;

import com.gabriel.agenda.datos.MetaDAO;
import com.gabriel.agenda.datos.SesionDAO;
import com.gabriel.agenda.modelo.MetaSemanal;
import com.gabriel.agenda.modelo.SesionEstudio;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

// Controlador: valida y guarda la meta semanal y calcula el progreso
// (solo cuentan las sesiones "Completada" de la semana)
public class ControladorMeta {

    // Mensajes que la pantalla muestra (públicos para poder compararlos desde la vista)
    public static final String ERROR_META = "Ingrese una meta entre 1 y 80 horas";
    public static final String META_GUARDADA = "Meta guardada";
    public static final String META_CUMPLIDA = "¡Meta cumplida! Sigue así";
    public static final String SIN_META = "Toca aquí para definir tu meta semanal";
    public static final String ERROR_GUARDAR = "No se pudo guardar";

    // Límites de la meta (los mismos del CHECK de la tabla meta_semanal)
    private static final int HORAS_MINIMAS = 1;
    private static final int HORAS_MAXIMAS = 80;
    private static final int MINUTOS_POR_HORA = 60;

    private final MetaDAO metaDAO;
    private final SesionDAO sesionDAO;

    // Constructor: crea los DAO. Usa SesionDAO para sumar las horas de las sesiones completadas
    public ControladorMeta(Context context) {
        metaDAO = new MetaDAO(context);
        sesionDAO = new SesionDAO(context);
    }

    // Valida y guarda la meta de la semana que empieza en "lunes".
    // textoHoras es lo que escribió el usuario ("20"). Devuelve el mensaje para la pantalla
    public String definirMeta(String lunes, String textoHoras) {
        int horas = convertirHoras(textoHoras);
        if (horas < HORAS_MINIMAS || horas > HORAS_MAXIMAS) {
            return ERROR_META; // no se guarda nada
        }

        // Si la semana ya tenía meta, el DAO la actualiza; si no, la inserta
        if (!metaDAO.guardar(new MetaSemanal(lunes, horas))) {
            return ERROR_GUARDAR;
        }
        return META_GUARDADA;
    }

    // Devuelve la meta de la semana con los minutos completados ya calculados.
    // Si la semana no tiene meta, devuelve una con horasObjetivo = 0
    public MetaSemanal obtenerProgreso(String lunes) {
        MetaSemanal meta = metaDAO.buscarPorSemana(lunes);
        if (meta == null) {
            meta = new MetaSemanal(lunes, 0); // todavía no se definió la meta
        }
        meta.setMinutosCompletados(minutosCompletados(lunes));
        return meta;
    }

    // Dice si la semana tiene una meta definida
    public boolean tieneMeta(MetaSemanal progreso) {
        return progreso.getHorasObjetivo() > 0;
    }

    // Dice si ya se alcanzó la meta (minutos completados >= horas de la meta en minutos)
    public boolean metaCumplida(MetaSemanal progreso) {
        return tieneMeta(progreso)
                && progreso.getMinutosCompletados() >= progreso.getHorasObjetivo() * MINUTOS_POR_HORA;
    }

    // Porcentaje de avance, de 0 a 100, para la ProgressBar.
    // Ejemplo: 90 minutos de una meta de 20 h (1200 minutos) → 90 * 100 / 1200 = 7 %
    public int porcentaje(MetaSemanal progreso) {
        if (!tieneMeta(progreso)) {
            return 0;
        }
        int minutosMeta = progreso.getHorasObjetivo() * MINUTOS_POR_HORA;
        int porcentaje = progreso.getMinutosCompletados() * 100 / minutosMeta;
        return Math.min(porcentaje, 100); // si se pasó de la meta, la barra queda llena
    }

    // Texto grande del progreso: "1,5 / 20 h". Sin meta: solo las horas hechas, "1,5 h"
    public String textoProgreso(MetaSemanal progreso) {
        String hechas = horasComoTexto(progreso.getMinutosCompletados());
        if (!tieneMeta(progreso)) {
            return hechas + " h";
        }
        return hechas + " / " + progreso.getHorasObjetivo() + " h";
    }

    // Mensaje debajo de la barra: sin meta, meta cumplida o cuántas horas faltan
    public String mensajeProgreso(MetaSemanal progreso) {
        if (!tieneMeta(progreso)) {
            return SIN_META;
        }
        if (metaCumplida(progreso)) {
            return META_CUMPLIDA;
        }
        int minutosFaltan = progreso.getHorasObjetivo() * MINUTOS_POR_HORA
                - progreso.getMinutosCompletados();
        return "Te faltan " + horasComoTexto(minutosFaltan) + " h";
    }

    // Convierte minutos en horas con coma decimal: 90 → "1,5", 120 → "2", 75 → "1,25".
    // static: no usa la base de datos, así el ControladorReporte también lo puede usar
    public static String horasComoTexto(int minutos) {
        double horas = minutos / 60.0; // 60.0 (con decimal) para que 90 / 60.0 dé 1.5 y no 1
        // "0.##": hasta 2 decimales y sin ceros de más. Símbolos en español: coma decimal
        DecimalFormat formato = new DecimalFormat("0.##",
                new DecimalFormatSymbols(Locale.forLanguageTag("es")));
        return formato.format(horas);
    }

    // Devuelve el lunes de la semana actual como AAAA-MM-DD ("2026-11-09")
    public String lunesDeHoy() {
        Calendar lunes = Calendar.getInstance();
        // Calendar numera domingo = 1, lunes = 2 ... sábado = 7.
        // (díaDeLaSemana + 5) % 7 da cuántos días hay que retroceder: lunes → 0 ... domingo → 6
        int diasDesdeLunes = (lunes.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        lunes.add(Calendar.DAY_OF_MONTH, -diasDesdeLunes);
        return String.format(Locale.ROOT, "%04d-%02d-%02d", lunes.get(Calendar.YEAR),
                lunes.get(Calendar.MONTH) + 1, lunes.get(Calendar.DAY_OF_MONTH));
    }

    // Texto de la semana para la pantalla: "2026-11-09" → "Semana del 09/11 al 15/11"
    public String textoSemana(String lunes) {
        String[] partes = lunes.split("-"); // ["2026", "11", "09"]
        Calendar dia = Calendar.getInstance();
        dia.set(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]) - 1,
                Integer.parseInt(partes[2]));
        String desde = String.format(Locale.ROOT, "%02d/%02d",
                dia.get(Calendar.DAY_OF_MONTH), dia.get(Calendar.MONTH) + 1);
        dia.add(Calendar.DAY_OF_MONTH, 6); // el domingo
        String hasta = String.format(Locale.ROOT, "%02d/%02d",
                dia.get(Calendar.DAY_OF_MONTH), dia.get(Calendar.MONTH) + 1);
        return "Semana del " + desde + " al " + hasta;
    }

    // Suma la duración de las sesiones "Completada" de la semana (de lunes a domingo).
    // Las Pendiente y Reprogramada NO cuentan
    private int minutosCompletados(String lunes) {
        int total = 0;
        for (SesionEstudio sesion : sesionDAO.buscarPorSemana(lunes)) {
            if (SesionEstudio.ESTADO_COMPLETADA.equals(sesion.getEstado())) {
                total = total + sesion.getDuracionMinutos(); // fin - inicio, en minutos
            }
        }
        return total;
    }

    // Convierte el texto escrito en un número entero. Si está vacío o no es un número, devuelve -1
    // (y así no pasa la validación). trim() quita los espacios de los extremos
    private int convertirHoras(String textoHoras) {
        if (textoHoras == null) {
            return -1;
        }
        try {
            return Integer.parseInt(textoHoras.trim());
        } catch (NumberFormatException e) {
            return -1; // "", "abc" o un número demasiado grande
        }
    }
}