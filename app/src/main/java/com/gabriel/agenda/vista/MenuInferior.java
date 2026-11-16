package com.gabriel.agenda.vista;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.gabriel.agenda.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

// Maneja el menú inferior (layout menu_inferior.xml) que comparten varias pantallas (capa vista).
// Así el código de los 4 botones y del panel "Más" está en un solo lugar y no se repite en cada Activity
public class MenuInferior {

    // Conecta los 4 botones del menú y resalta el de la pantalla actual.
    // idActual: el botón que se resalta (R.id.menuInicio, R.id.menuCalendario, R.id.menuEstadisticas o R.id.menuMas)
    public static void configurar(final Activity activity, int idActual) {
        TextView menuInicio = activity.findViewById(R.id.menuInicio);
        TextView menuCalendario = activity.findViewById(R.id.menuCalendario);
        TextView menuEstadisticas = activity.findViewById(R.id.menuEstadisticas);
        TextView menuMas = activity.findViewById(R.id.menuMas);

        // Inicio: vuelve al panel principal
        menuInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrir(activity, MainActivity.class);
            }
        });

        // Calendario: abre la semana
        menuCalendario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrir(activity, CalendarioActivity.class);
            }
        });

        // Estadísticas: abre las estadísticas de la semana
        menuEstadisticas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrir(activity, EstadisticasActivity.class);
            }
        });

        // Más: abre el panel que sube desde abajo
        menuMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarMenuMas(activity);
            }
        });

        // Resalta el botón de la pantalla actual: fondo celeste, texto azul y en negrita
        TextView actual = activity.findViewById(idActual);
        actual.setBackgroundResource(R.drawable.fondo_menu_activo);
        actual.setTextColor(ContextCompat.getColor(activity, R.color.menu_activo_texto));
        actual.setTypeface(null, Typeface.BOLD);
    }

    // Crea y muestra el panel "Más" (BottomSheetDialog): sube desde abajo con las 4 tarjetas
    private static void mostrarMenuMas(Activity activity) {
        BottomSheetDialog panel = new BottomSheetDialog(activity);
        // inflate: convierte panel_mas.xml en una View; null = todavía no tiene "padre"
        View contenido = LayoutInflater.from(activity).inflate(R.layout.panel_mas, null);
        panel.setContentView(contenido);

        // Cada tarjeta abre su pantalla (el mismo método para las 4, así no se repite el listener)
        conectarTarjeta(activity, panel, contenido, R.id.tarjetaMaterias, MateriasActivity.class);
        conectarTarjeta(activity, panel, contenido, R.id.tarjetaReporte, ReporteActivity.class);
        conectarTarjeta(activity, panel, contenido, R.id.tarjetaMeta, MetaActivity.class);
        conectarTarjeta(activity, panel, contenido, R.id.tarjetaContactos, ContactosActivity.class);

        panel.show();
    }

    // Al tocar la tarjeta "idTarjeta": cierra el panel y abre la pantalla "destino"
    private static void conectarTarjeta(final Activity activity, final BottomSheetDialog panel,
                                        View contenido, int idTarjeta, final Class<?> destino) {
        View tarjeta = contenido.findViewById(idTarjeta);
        tarjeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Primero se cierra el panel: abrir() puede cerrar la pantalla actual,
                // y un panel que sigue abierto sobre una pantalla cerrada da error
                panel.dismiss();
                abrir(activity, destino);
            }
        });
    }

    // Abre otra pantalla del menú. La pila queda siempre "Inicio → pantalla actual",
    // así el botón Atrás del teléfono vuelve a Inicio y no se acumulan pantallas
    private static void abrir(Activity activity, Class<?> destino) {
        // Si ya estoy en esa pantalla, no hago nada
        if (activity.getClass() == destino) {
            return;
        }

        Intent intent = new Intent(activity, destino);
        if (destino == MainActivity.class) {
            // CLEAR_TOP: cierra las pantallas que están encima de Inicio.
            // SINGLE_TOP: reutiliza el Inicio que ya está abierto en vez de crear otro
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
        } else {
            activity.startActivity(intent);
            // Si no estoy en Inicio, cierro la pantalla actual para no apilarla
            if (!(activity instanceof MainActivity)) {
                activity.finish();
            }
        }
    }
}
