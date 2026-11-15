package com.gabriel.agenda.vista;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.gabriel.agenda.R;

// Maneja el menú inferior (layout menu_inferior.xml) que comparten varias pantallas (capa vista).
// Así el código de los 4 botones y del PopupMenu está en un solo lugar y no se repite en cada Activity
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

        // Estadísticas: todavía no existe la pantalla (HU-15)
        menuEstadisticas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proximamente(activity);
            }
        });

        // Más: muestra el PopupMenu pegado al botón
        menuMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarMenuMas(activity, v);
            }
        });

        // Resalta el botón de la pantalla actual: fondo celeste, texto azul y en negrita
        TextView actual = activity.findViewById(idActual);
        actual.setBackgroundResource(R.drawable.fondo_menu_activo);
        actual.setTextColor(ContextCompat.getColor(activity, R.color.menu_activo_texto));
        actual.setTypeface(null, Typeface.BOLD);
    }

    // Crea y muestra el PopupMenu de "Más" con las opciones de res/menu/menu_mas.xml
    private static void mostrarMenuMas(final Activity activity, View boton) {
        PopupMenu popup = new PopupMenu(activity, boton);           // el menú aparece junto al botón
        popup.getMenuInflater().inflate(R.menu.menu_mas, popup.getMenu()); // XML → opciones

        // Qué hacer cuando se toca una opción
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem opcion) {
                int id = opcion.getItemId();
                if (id == R.id.opcionMaterias) {
                    abrir(activity, MateriasActivity.class);
                } else if (id == R.id.opcionContactos) {
                    abrir(activity, ContactosActivity.class);
                } else {
                    // Reporte semanal (HU-14) y Meta semanal (HU-13) todavía no existen
                    proximamente(activity);
                }
                return true; // true = "ya atendí el clic"
            }
        });
        popup.show();
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

    // Mensaje para las pantallas que se programan en los próximos pasos
    private static void proximamente(Activity activity) {
        Toast.makeText(activity, R.string.proximamente, Toast.LENGTH_SHORT).show();
    }
}
