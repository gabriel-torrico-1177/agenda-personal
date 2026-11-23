package com.gabriel.agenda.vista;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gabriel.agenda.R;
import com.gabriel.agenda.controlador.ControladorMeta;
import com.gabriel.agenda.controlador.ControladorReporte;
import com.gabriel.agenda.modelo.FilaReporte;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// Pantalla P7: reporte de la semana (capa vista). Arriba una tarjeta de resumen con el total
// y debajo una tarjeta por materia (ListView + ReporteAdapter).
// Los cálculos los hace ControladorReporte; aquí solo se muestran
public class ReporteActivity extends PantallaBase {

    private static final int DIAS_SEMANA = 7;

    // Lunes de la semana que se está viendo
    private Calendar lunes;

    private TextView tvSemana;
    // Tarjeta de resumen: "62 %", la barra y "5,93 h hechas de 9,5 h planificadas"
    private View tarjetaResumen;
    private TextView tvPorcentajeTotal;
    private ProgressBar pbTotal;
    private TextView tvHorasTotal;
    // Lista con una tarjeta por materia
    private ListView lvReporte;
    private ReporteAdapter adapter;
    // Semana sin sesiones: ícono, "No hay sesiones en esta semana" y la pista
    private View llVacio;
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
        tarjetaResumen = findViewById(R.id.tarjetaResumen);
        tvPorcentajeTotal = findViewById(R.id.tvPorcentajeTotal);
        pbTotal = findViewById(R.id.pbTotal);
        tvHorasTotal = findViewById(R.id.tvHorasTotal);
        lvReporte = findViewById(R.id.lvReporte);
        llVacio = findViewById(R.id.llVacio);
        Button btnAnterior = findViewById(R.id.btnAnterior);
        Button btnSiguiente = findViewById(R.id.btnSiguiente);

        controlador = new ControladorReporte(this);
        controladorMeta = new ControladorMeta(this);

        // El adaptador empieza con una lista vacía; mostrarReporte() le pasa las filas de la semana
        adapter = new ReporteAdapter(this, new ArrayList<FilaReporte>(), controlador);
        lvReporte.setAdapter(adapter);

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

    // Muestra la semana, la tarjeta de resumen y una tarjeta por materia
    // (o el mensaje si la semana no tiene sesiones)
    private void mostrarReporte() {
        // "Semana del 16/11 al 22/11": el texto lo arma el controlador, igual que en Estadísticas
        tvSemana.setText(controladorMeta.textoSemana(fechaParaBuscar(lunes)));

        List<FilaReporte> filas = controlador.generarReporte(fechaParaBuscar(lunes));
        adapter.actualizar(filas); // una tarjeta por materia

        // Semana sin sesiones: se ocultan la tarjeta y la lista y se muestra "No hay sesiones en esta semana"
        if (filas.isEmpty()) {
            tarjetaResumen.setVisibility(View.GONE);
            lvReporte.setVisibility(View.GONE);
            llVacio.setVisibility(View.VISIBLE);
            return;
        }
        tarjetaResumen.setVisibility(View.VISIBLE);
        lvReporte.setVisibility(View.VISIBLE);
        llVacio.setVisibility(View.GONE);

        mostrarResumen(controlador.calcularTotal(filas));
    }

    // Llena la tarjeta de resumen con el total que calculó el controlador
    private void mostrarResumen(FilaReporte total) {
        tvPorcentajeTotal.setText(getString(R.string.formato_porcentaje, total.getPorcentaje()));
        pbTotal.setProgress(total.getPorcentaje()); // la barra va de 0 a 100
        tvHorasTotal.setText(getString(R.string.formato_horas_total,
                controlador.horasComoTexto(total.getMinutosCompletados()),
                controlador.horasComoTexto(total.getMinutosPlanificados())));
    }

    // Fecha como AAAA-MM-DD para buscar en la base de datos (mes + 1 porque Calendar empieza en 0)
    private String fechaParaBuscar(Calendar fecha) {
        return String.format(Locale.ROOT, "%04d-%02d-%02d", fecha.get(Calendar.YEAR),
                fecha.get(Calendar.MONTH) + 1, fecha.get(Calendar.DAY_OF_MONTH));
    }
}
