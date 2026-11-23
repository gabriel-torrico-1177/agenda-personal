package com.gabriel.agenda.controlador;

import android.content.Context;

import com.gabriel.agenda.datos.SesionDAO;
import com.gabriel.agenda.modelo.FilaReporte;
import com.gabriel.agenda.modelo.SesionEstudio;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Controlador: arma el reporte semanal. Por cada materia suma las horas planificadas
// (todas las sesiones de la semana) y las completadas (solo "Completada") y saca el porcentaje
public class ControladorReporte {

    // Nombre de la última fila del reporte.
    // (El mensaje "No hay sesiones en esta semana" está en strings.xml: lo muestra la vista)
    public static final String TOTAL = "Total";

    // Niveles de cumplimiento de una materia (la vista elige el color de la etiqueta del %):
    // ALTO = 80 % o más (verde), MEDIO = de 50 % a 79 % (ámbar), BAJO = menos de 50 % (rojo)
    public static final String NIVEL_ALTO = "ALTO";
    public static final String NIVEL_MEDIO = "MEDIO";
    public static final String NIVEL_BAJO = "BAJO";
    private static final int MINIMO_ALTO = 80;
    private static final int MINIMO_MEDIO = 50;

    private final SesionDAO sesionDAO;

    // Constructor: crea el DAO con el que se leen las sesiones
    public ControladorReporte(Context context) {
        sesionDAO = new SesionDAO(context);
    }

    // Devuelve una fila por cada materia que tiene sesiones en la semana que empieza en "lunes",
    // ordenadas por nombre (A-Z). Si la semana no tiene sesiones, la lista viene vacía
    public List<FilaReporte> generarReporte(String lunes) {
        // Map (diccionario): materia_id → su fila. Así todas las sesiones de una materia
        // se suman en la MISMA fila
        Map<Integer, FilaReporte> filasPorMateria = new HashMap<>();

        for (SesionEstudio sesion : sesionDAO.buscarPorSemana(lunes)) {
            // 1. Buscar la fila de su materia; si es la primera sesión de esa materia, crearla
            FilaReporte fila = filasPorMateria.get(sesion.getMateriaId());
            if (fila == null) {
                fila = new FilaReporte(sesion.getNombreMateria());
                fila.setColorMateria(sesion.getColorMateria());
                filasPorMateria.put(sesion.getMateriaId(), fila);
            }

            // 2. Toda sesión suma a las planificadas
            int minutos = sesion.getDuracionMinutos(); // fin - inicio
            fila.setMinutosPlanificados(fila.getMinutosPlanificados() + minutos);

            // 3. Solo las completadas suman a las hechas
            if (SesionEstudio.ESTADO_COMPLETADA.equals(sesion.getEstado())) {
                fila.setMinutosCompletados(fila.getMinutosCompletados() + minutos);
            }
        }

        // 4. Pasar las filas a una lista, calcular el porcentaje de cada una y ordenarlas
        List<FilaReporte> filas = new ArrayList<>(filasPorMateria.values());
        for (FilaReporte fila : filas) {
            calcularPorcentaje(fila);
        }
        ordenarPorNombre(filas);
        return filas;
    }

    // Suma todas las filas en una fila "Total" (con su propio porcentaje)
    public FilaReporte calcularTotal(List<FilaReporte> filas) {
        FilaReporte total = new FilaReporte(TOTAL);
        for (FilaReporte fila : filas) {
            total.setMinutosPlanificados(total.getMinutosPlanificados() + fila.getMinutosPlanificados());
            total.setMinutosCompletados(total.getMinutosCompletados() + fila.getMinutosCompletados());
        }
        calcularPorcentaje(total);
        return total;
    }

    // Decide el nivel de cumplimiento según el porcentaje. Ejemplos: 85 → ALTO, 50 → MEDIO, 0 → BAJO.
    // Lo decide el controlador para que la vista no haga comparaciones: solo pinta el color
    public String nivelCumplimiento(int porcentaje) {
        if (porcentaje >= MINIMO_ALTO) {
            return NIVEL_ALTO;
        }
        if (porcentaje >= MINIMO_MEDIO) {
            return NIVEL_MEDIO;
        }
        return NIVEL_BAJO;
    }

    // Texto de horas para el reporte: 90 minutos → "1,5 h" (usa el mismo formato que la meta)
    public String horasComoTexto(int minutos) {
        return ControladorMeta.horasComoTexto(minutos) + " h";
    }

    // Porcentaje cumplido: completados * 100 / planificados. Ejemplo: 360 de 480 min → 75 %.
    // Es división entera: 2 de 3 horas da 66 (se cortan los decimales)
    private void calcularPorcentaje(FilaReporte fila) {
        if (fila.getMinutosPlanificados() == 0) {
            fila.setPorcentaje(0); // nunca se divide entre 0
            return;
        }
        fila.setPorcentaje(fila.getMinutosCompletados() * 100 / fila.getMinutosPlanificados());
    }

    // Ordena las filas por nombre de materia (A-Z), sin importar tildes ni mayúsculas
    private void ordenarPorNombre(List<FilaReporte> filas) {
        final Collator collator = Collator.getInstance(Locale.forLanguageTag("es"));
        collator.setStrength(Collator.PRIMARY); // PRIMARY: ignora tildes y mayúsculas
        Collections.sort(filas, new Comparator<FilaReporte>() {
            @Override
            public int compare(FilaReporte a, FilaReporte b) {
                return collator.compare(a.getMateria(), b.getMateria());
            }
        });
    }
}