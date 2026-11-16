package com.gabriel.agenda.vista;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gabriel.agenda.R;
import com.gabriel.agenda.controlador.ControladorMeta;
import com.gabriel.agenda.modelo.MetaSemanal;

// Pantalla P8: definir las horas de estudio de la semana actual y ver el progreso (capa vista)
public class MetaActivity extends PantallaBase {

    private EditText etHoras;
    private ControladorMeta controlador;
    private String lunes; // lunes de la semana actual, "2026-11-09"

    // Se ejecuta al abrir la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_meta);
        // Deja espacio para las barras del sistema y el teclado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        // Menú inferior con "Más" resaltado (Meta semanal está dentro de "Más")
        MenuInferior.configurar(this, R.id.menuMas);

        // Conecta las variables con el layout
        etHoras = findViewById(R.id.etHoras);
        TextView tvSemana = findViewById(R.id.tvSemana);
        TextView tvTituloMeta = findViewById(R.id.tvTituloMeta);
        Button btnCancelar = findViewById(R.id.btnCancelar);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        controlador = new ControladorMeta(this);
        lunes = controlador.lunesDeHoy();

        tvSemana.setText(controlador.textoSemana(lunes));
        tvTituloMeta.setText(R.string.titulo_progreso); // en esta pantalla la tarjeta dice "PROGRESO"

        // Pide el progreso al controlador, pinta la tarjeta y, si ya hay meta, la pone en el campo
        MetaSemanal progreso = controlador.obtenerProgreso(lunes);
        mostrarProgreso(progreso);
        if (controlador.tieneMeta(progreso)) {
            etHoras.setText(String.valueOf(progreso.getHorasObjetivo()));
        }

        // Cancelar: cierra sin guardar
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Guardar: el controlador valida y guarda
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardar();
            }
        });
    }

    // Manda lo escrito al controlador y muestra el resultado
    private void guardar() {
        etHoras.setError(null); // quita el error anterior
        String mensaje = controlador.definirMeta(lunes, etHoras.getText().toString());

        if (mensaje.equals(ControladorMeta.META_GUARDADA)) {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            finish(); // vuelve a la pantalla anterior (Inicio muestra el progreso nuevo)
        } else if (mensaje.equals(ControladorMeta.ERROR_META)) {
            etHoras.setError(mensaje); // error junto al campo
        } else {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        }
    }

    // Pinta la tarjeta: "1,5 / 20 h", la barra y el mensaje (los textos los arma el controlador)
    private void mostrarProgreso(MetaSemanal progreso) {
        TextView tvProgreso = findViewById(R.id.tvProgreso);
        ProgressBar pbMeta = findViewById(R.id.pbMeta);
        TextView tvMensajeMeta = findViewById(R.id.tvMensajeMeta);

        tvProgreso.setText(controlador.textoProgreso(progreso));
        pbMeta.setProgress(controlador.porcentaje(progreso));
        // Sin meta, aquí no hace falta "Toca aquí para definir...": el campo está justo arriba
        if (controlador.tieneMeta(progreso)) {
            tvMensajeMeta.setText(controlador.mensajeProgreso(progreso));
        } else {
            tvMensajeMeta.setText("");
        }
    }
}