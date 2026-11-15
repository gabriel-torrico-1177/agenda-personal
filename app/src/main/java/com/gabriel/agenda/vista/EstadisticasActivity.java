package com.gabriel.agenda.vista;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gabriel.agenda.R;
import com.gabriel.agenda.controlador.ControladorEstadisticas;
import com.gabriel.agenda.controlador.ControladorMeta;
import com.gabriel.agenda.modelo.FilaReporte;
import com.gabriel.agenda.modelo.MetaSemanal;

import java.util.List;

// Pantalla P6: estadísticas de esta semana (capa vista): horas estudiadas, comparación con la
// semana anterior, la meta y una barra por materia. Los cálculos los hacen los controladores
public class EstadisticasActivity extends AppCompatActivity {

    private TextView tvSemana;
    private TextView tvTotal;
    private TextView tvVariacion;
    private LinearLayout llBarras;
    private TextView tvVacio;

    private ControladorEstadisticas controlador;
    private ControladorMeta controladorMeta;

    // Se ejecuta al abrir la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_estadisticas);
        // Deja espacio para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        // Menú inferior con "Estadísticas" resaltado
        MenuInferior.configurar(this, R.id.menuEstadisticas);

        // Conecta las variables con el layout
        tvSemana = findViewById(R.id.tvSemana);
        tvTotal = findViewById(R.id.tvTotal);
        tvVariacion = findViewById(R.id.tvVariacion);
        llBarras = findViewById(R.id.llBarras);
        tvVacio = findViewById(R.id.tvVacio);
        TextView tvTituloMeta = findViewById(R.id.tvTituloMeta);
        View tarjetaMeta = findViewById(R.id.tarjetaMeta);

        controlador = new ControladorEstadisticas(this);
        controladorMeta = new ControladorMeta(this);

        tvTituloMeta.setText(R.string.titulo_meta_corto); // en esta pantalla dice "META SEMANAL"

        // Tocar la tarjeta de la meta: abre Meta semanal
        tarjetaMeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EstadisticasActivity.this, MetaActivity.class));
            }
        });
    }

    // Se ejecuta cada vez que la pantalla vuelve a verse (por ejemplo, al volver de Meta)
    @Override
    protected void onResume() {
        super.onResume();
        String lunes = controladorMeta.lunesDeHoy();
        tvSemana.setText(controladorMeta.textoSemana(lunes));

        // Horas estudiadas y comparación con la semana anterior
        tvTotal.setText(ControladorMeta.horasComoTexto(controlador.totalHoras(lunes)) + " h");
        tvVariacion.setText(controlador.textoVariacion(lunes));

        mostrarMeta(lunes);
        mostrarBarras(controlador.horasPorMateria(lunes));
    }

    // Pinta la tarjeta de la meta: "15 / 20 h", la barra y el mensaje
    private void mostrarMeta(String lunes) {
        TextView tvProgreso = findViewById(R.id.tvProgreso);
        ProgressBar pbMeta = findViewById(R.id.pbMeta);
        TextView tvMensajeMeta = findViewById(R.id.tvMensajeMeta);

        MetaSemanal progreso = controladorMeta.obtenerProgreso(lunes);
        tvProgreso.setText(controladorMeta.textoProgreso(progreso));
        pbMeta.setProgress(controladorMeta.porcentaje(progreso));
        tvMensajeMeta.setText(controladorMeta.mensajeProgreso(progreso));
    }

    // Dibuja una barra por materia (ya vienen ordenadas de mayor a menor).
    // La materia con más horas llena toda la pista; las demás, una parte proporcional
    private void mostrarBarras(List<FilaReporte> filas) {
        llBarras.removeAllViews(); // borra las barras anteriores

        // Sin horas completadas: "Aún no hay horas registradas esta semana"
        if (filas.isEmpty()) {
            tvVacio.setVisibility(View.VISIBLE);
            return;
        }
        tvVacio.setVisibility(View.GONE);

        // La primera fila es la de más minutos: ese número será el 100 % de la pista
        int maximo = filas.get(0).getMinutosCompletados();
        LayoutInflater inflater = LayoutInflater.from(this);
        float densidad = getResources().getDisplayMetrics().density; // para pasar dp a píxeles

        for (FilaReporte fila : filas) {
            // inflate: convierte item_barra.xml en una View (como hace el calendario con los días)
            View item = inflater.inflate(R.layout.item_barra, llBarras, false);
            TextView tvMateria = item.findViewById(R.id.tvMateria);
            LinearLayout llPista = item.findViewById(R.id.llPista);
            View vBarra = item.findViewById(R.id.vBarra);
            TextView tvHoras = item.findViewById(R.id.tvHoras);

            tvMateria.setText(fila.getMateria());
            tvHoras.setText(ControladorMeta.horasComoTexto(fila.getMinutosCompletados()) + " h");

            // Ancho proporcional con pesos: la pista "vale" maximo y la barra "vale" sus minutos.
            // Ejemplo: maximo 360 y la materia 180 → la barra ocupa 180 / 360 = la mitad
            llPista.setWeightSum(maximo);
            LinearLayout.LayoutParams parametros =
                    (LinearLayout.LayoutParams) vBarra.getLayoutParams();
            parametros.weight = fila.getMinutosCompletados();
            vBarra.setLayoutParams(parametros);

            // Color de la materia, con las esquinas redondeadas como la pista (8dp)
            GradientDrawable fondo = new GradientDrawable();
            fondo.setColor(Color.parseColor(fila.getColorMateria()));
            fondo.setCornerRadius(8 * densidad);
            vBarra.setBackground(fondo);

            llBarras.addView(item);
        }
    }
}