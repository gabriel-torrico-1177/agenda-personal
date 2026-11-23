package com.gabriel.agenda.vista;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gabriel.agenda.R;
import com.gabriel.agenda.controlador.ControladorMeta;
import com.gabriel.agenda.controlador.ControladorReporte;
import com.gabriel.agenda.modelo.FilaReporte;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// Pantalla P7: reporte de la semana con una tabla Materia / Plan. / Hechas / % (capa vista).
// Los cálculos los hace ControladorReporte; aquí solo se arma la tabla
public class ReporteActivity extends PantallaBase {

    private static final int DIAS_SEMANA = 7;

    // Lunes de la semana que se está viendo
    private Calendar lunes;

    private TextView tvSemana;
    private TableLayout tlReporte;
    private View svTabla;
    private TextView tvVacio;
    private ControladorReporte controlador;
    // Solo para el texto "Semana del 16/11 al 22/11" (el mismo que usa Estadísticas)
    private ControladorMeta controladorMeta;

    // Se ejecuta al abrir la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_reporte);
        // Deja espacio para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        // Menú inferior con "Más" resaltado (Reporte semanal está dentro de "Más")
        MenuInferior.configurar(this, R.id.menuMas);

        // Conecta las variables con el layout
        tvSemana = findViewById(R.id.tvSemana);
        tlReporte = findViewById(R.id.tlReporte);
        svTabla = findViewById(R.id.svTabla);
        tvVacio = findViewById(R.id.tvVacio);
        Button btnAnterior = findViewById(R.id.btnAnterior);
        Button btnSiguiente = findViewById(R.id.btnSiguiente);

        controlador = new ControladorReporte(this);
        controladorMeta = new ControladorMeta(this);

        // Al abrir: la semana de hoy (retrocede desde hoy hasta el lunes)
        lunes = Calendar.getInstance();
        // Calendar: domingo = 1 ... sábado = 7. (día + 5) % 7 → lunes = 0 ... domingo = 6
        lunes.add(Calendar.DAY_OF_MONTH, -((lunes.get(Calendar.DAY_OF_WEEK) + 5) % 7));

        // ◀ y ▶: retroceden o avanzan una semana (7 días) y vuelven a armar el reporte
        btnAnterior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lunes.add(Calendar.DAY_OF_MONTH, -DIAS_SEMANA);
                mostrarReporte();
            }
        });
        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lunes.add(Calendar.DAY_OF_MONTH, DIAS_SEMANA);
                mostrarReporte();
            }
        });
    }

    // Se ejecuta cada vez que la pantalla vuelve a verse
    @Override
    protected void onResume() {
        super.onResume();
        mostrarReporte();
    }

    // Muestra la semana y arma la tabla (o el mensaje si la semana no tiene sesiones)
    private void mostrarReporte() {
        // "Semana del 16/11 al 22/11": el texto lo arma el controlador, igual que en Estadísticas
        tvSemana.setText(controladorMeta.textoSemana(fechaParaBuscar(lunes)));

        // Borra las filas de la semana anterior: todas menos la 0, que es la de títulos
        tlReporte.removeViews(1, tlReporte.getChildCount() - 1);

        List<FilaReporte> filas = controlador.generarReporte(fechaParaBuscar(lunes));

        // Semana sin sesiones: se oculta la tabla y se muestra "No hay sesiones en esta semana"
        if (filas.isEmpty()) {
            svTabla.setVisibility(View.GONE);
            tvVacio.setVisibility(View.VISIBLE);
            return;
        }
        svTabla.setVisibility(View.VISIBLE);
        tvVacio.setVisibility(View.GONE);

        // Una fila por materia y, al final, la fila de total en negrita con fondo gris
        for (FilaReporte fila : filas) {
            agregarFila(fila, false);
        }
        agregarFila(controlador.calcularTotal(filas), true);
    }

    // Agrega a la tabla una fila con 4 celdas: materia, planificadas, hechas y porcentaje
    private void agregarFila(FilaReporte fila, boolean esTotal) {
        TableRow tableRow = new TableRow(this);
        if (esTotal) {
            tableRow.setBackgroundResource(R.color.fila_destacada);
        }

        TextView tvMateria = crearCelda(tableRow, fila.getMateria(), esTotal);
        tvMateria.setGravity(Gravity.START); // el nombre va a la izquierda; los números, a la derecha
        crearCelda(tableRow, controlador.horasComoTexto(fila.getMinutosPlanificados()), esTotal);
        crearCelda(tableRow, controlador.horasComoTexto(fila.getMinutosCompletados()), esTotal);
        crearCelda(tableRow, getString(R.string.formato_porcentaje, fila.getPorcentaje()), esTotal);

        tlReporte.addView(tableRow);
    }

    // Crea una celda desde celda_reporte.xml, le pone el texto y la agrega a la fila
    private TextView crearCelda(TableRow tableRow, String texto, boolean negrita) {
        TextView celda = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.celda_reporte, tableRow, false);
        celda.setText(texto);
        if (negrita) {
            celda.setTypeface(null, Typeface.BOLD);
        }
        tableRow.addView(celda);
        return celda;
    }

    // Fecha como AAAA-MM-DD para buscar en la base de datos (mes + 1 porque Calendar empieza en 0)
    private String fechaParaBuscar(Calendar fecha) {
        return String.format(Locale.ROOT, "%04d-%02d-%02d", fecha.get(Calendar.YEAR),
                fecha.get(Calendar.MONTH) + 1, fecha.get(Calendar.DAY_OF_MONTH));
    }
}