package com.gabriel.agenda.controlador;

import android.content.Context;

import com.gabriel.agenda.datos.SesionDAO;
import com.gabriel.agenda.modelo.FilaReporte;
import com.gabriel.agenda.modelo.SesionEstudio;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Controlador: calcula las estadísticas de una semana.
// Solo cuentan las sesiones "Completada" (las Pendiente y Reprogramada no son horas estudiadas)
public class ControladorEstadisticas {

    private final SesionDAO sesionDAO;

    // Constructor: crea el DAO con el que se leen las sesiones
    public ControladorEstadisticas(Context context) {
        sesionDAO = new SesionDAO(context);
    }

    // Total estudiado en la semana que empieza en "lunes", EN MINUTOS (90 = 1,5 h).
    // Se devuelve en minutos para no perder exactitud; la vista lo muestra con horasComoTexto
    public int totalHoras(String lunes) {
        int total = 0;
        for (SesionEstudio sesion : sesionDAO.buscarPorSemana(lunes)) {
            if (SesionEstudio.ESTADO_COMPLETADA.equals(sesion.getEstado())) {
                total = total + sesion.getDuracionMinutos(); // fin - inicio
            }
        }
        return total;
    }

    // Cuánto subió o bajó lo estudiado respecto a la semana anterior, en porcentaje.
    // Fórmula: (actual - anterior) * 100 / anterior. Ejemplo: 15 h ahora y 12 h antes → +25.
    // Devuelve null si la semana anterior no tiene horas (no se puede dividir entre 0)
    public Integer variacionSemanaAnterior(String lunes) {
        int actual = totalHoras(lunes);
        int anterior = totalHoras(lunesAnterior(lunes));
        if (anterior == 0) {
            return null;
        }
        return (actual - anterior) * 100 / anterior;
    }

    // Texto de la comparación para la pantalla: "↑ 25 % vs. semana anterior", "↓ 10 % ..." o
    // "Sin horas la semana anterior"
    public String textoVariacion(String lunes) {
        Integer variacion = variacionSemanaAnterior(lunes);
        if (variacion == null) {
            return "Sin horas la semana anterior";
        }
        if (variacion > 0) {
            return "↑ " + variacion + " % vs. semana anterior";
        }
        if (variacion < 0) {
            // Math.abs quita el signo: -10 → 10 (la flecha ↓ ya dice que bajó)
            return "↓ " + Math.abs(variacion) + " % vs. semana anterior";
        }
        return "= Igual que la semana anterior";
    }

    // Horas completadas por materia (una FilaReporte por materia, con su color),
    // ordenadas de MAYOR a MENOR. Las materias sin horas completadas no aparecen
    public List<FilaReporte> horasPorMateria(String lunes) {
        // Map (diccionario): materia_id → su fila, para sumar todas sus sesiones en la misma fila
        Map<Integer, FilaReporte> filasPorMateria = new HashMap<>();

        for (SesionEstudio sesion : sesionDAO.buscarPorSemana(lunes)) {
            if (!SesionEstudio.ESTADO_COMPLETADA.equals(sesion.getEstado())) {
                continue; // las que no están completadas no cuentan: salta a la siguiente
            }
            FilaReporte fila = filasPorMateria.get(sesion.getMateriaId());
            if (fila == null) {
                fila = new FilaReporte(sesion.getNombreMateria());
                fila.setColorMateria(sesion.getColorMateria());
                filasPorMateria.put(sesion.getMateriaId(), fila);
            }
            fila.setMinutosCompletados(fila.getMinutosCompletados() + sesion.getDuracionMinutos());
        }

        List<FilaReporte> filas = new ArrayList<>(filasPorMateria.values());
        ordenarDeMayorAMenor(filas);
        return filas;
    }

    // Ordena por minutos completados de mayor a menor; si empatan, por nombre (A-Z)
    private void ordenarDeMayorAMenor(List<FilaReporte> filas) {
        final Collator collator = Collator.getInstance(Locale.forLanguageTag("es"));
        collator.setStrength(Collator.PRIMARY); // PRIMARY: ignora tildes y mayúsculas
        Collections.sort(filas, new Comparator<FilaReporte>() {
            @Override
            public int compare(FilaReporte a, FilaReporte b) {
                // b antes que a: así el que tiene MÁS minutos queda primero
                int porMinutos = Integer.compare(b.getMinutosCompletados(), a.getMinutosCompletados());
                if (porMinutos != 0) {
                    return porMinutos;
                }
                return collator.compare(a.getMateria(), b.getMateria());
            }
        });
    }

    // Lunes de la semana anterior: "2026-11-09" → "2026-11-02" (7 días antes)
    private String lunesAnterior(String lunes) {
        String[] partes = lunes.split("-"); // ["2026", "11", "09"]
        Calendar dia = Calendar.getInstance();
        dia.set(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]) - 1,
                Integer.parseInt(partes[2]));
        dia.add(Calendar.DAY_OF_MONTH, -7); // Calendar se encarga de cambiar de mes o de año
        return String.format(Locale.ROOT, "%04d-%02d-%02d", dia.get(Calendar.YEAR),
                dia.get(Calendar.MONTH) + 1, dia.get(Calendar.DAY_OF_MONTH));
    }
}