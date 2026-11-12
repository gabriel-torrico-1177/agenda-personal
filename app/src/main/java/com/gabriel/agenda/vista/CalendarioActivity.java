package com.gabriel.agenda.vista;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gabriel.agenda.R;
import com.gabriel.agenda.controlador.ControladorSesion;
import com.gabriel.agenda.modelo.SesionEstudio;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

// Pantalla P2: tira con los 7 días de la semana y las sesiones del día elegido (capa vista)
public class CalendarioActivity extends AppCompatActivity {

    private static final int DIAS_SEMANA = 7;

    // Lunes de la semana que se está viendo
    private Calendar lunes;
    // Día elegido dentro de la semana: 0 = lunes, 1 = martes ... 6 = domingo
    private int indiceElegido;

    // Las 7 celdas de la tira y sus partes (se crean en crearTiraDeDias)
    private final View[] celdas = new View[DIAS_SEMANA];
    private final TextView[] tvLetras = new TextView[DIAS_SEMANA];
    private final TextView[] tvNumeros = new TextView[DIAS_SEMANA];
    private final View[] puntos = new View[DIAS_SEMANA];
    // Color normal del texto (para volver a él cuando un día deja de estar elegido)
    private ColorStateList colorTextoNormal;

    // Nombres que se muestran (vienen de strings.xml)
    private String[] diasLargos;
    private String[] meses;

    private TextView tvMes;
    private TextView tvDiaElegido;
    private SesionAdapter adaptador;
    private ControladorSesion controlador;

    // Se ejecuta al abrir la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_calendario);
        // Deja espacio para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        // Conecta las variables con el layout
        tvMes = findViewById(R.id.tvMes);
        tvDiaElegido = findViewById(R.id.tvDiaElegido);
        Button btnAnterior = findViewById(R.id.btnAnterior);
        Button btnSiguiente = findViewById(R.id.btnSiguiente);
        ListView lvSesiones = findViewById(R.id.lvSesiones);
        TextView tvVacio = findViewById(R.id.tvVacio);
        FloatingActionButton fabAgregar = findViewById(R.id.fabAgregar);

        diasLargos = getResources().getStringArray(R.array.dias_largos);
        meses = getResources().getStringArray(R.array.meses_cortos);
        controlador = new ControladorSesion(this);

        // Al abrir: la semana de hoy, con hoy elegido
        Calendar hoy = Calendar.getInstance();
        indiceElegido = indiceDelDia(hoy);
        lunes = (Calendar) hoy.clone();                       // copia de hoy...
        lunes.add(Calendar.DAY_OF_MONTH, -indiceElegido);     // ...retrocedida hasta el lunes

        crearTiraDeDias();

        // Lista de sesiones del día elegido; si está vacía muestra "No tienes sesiones para este día"
        adaptador = new SesionAdapter(this, new ArrayList<SesionEstudio>());
        lvSesiones.setAdapter(adaptador);
        lvSesiones.setEmptyView(tvVacio);

        // ◀ y ▶: retroceden o avanzan 7 días (se queda elegido el mismo día de la semana)
        btnAnterior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lunes.add(Calendar.DAY_OF_MONTH, -DIAS_SEMANA);
                mostrarSemana();
            }
        });
        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lunes.add(Calendar.DAY_OF_MONTH, DIAS_SEMANA);
                mostrarSemana();
            }
        });

        // Botón +: abre el formulario para planificar una sesión
        fabAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalendarioActivity.this, SesionFormActivity.class);
                startActivity(intent);
            }
        });
    }

    // Se ejecuta cada vez que la pantalla vuelve a verse (también al volver del formulario)
    @Override
    protected void onResume() {
        super.onResume();
        mostrarSemana();
    }

    // Crea las 7 celdas de la tira UNA sola vez: infla item_dia.xml 7 veces y las agrega al LinearLayout
    private void crearTiraDeDias() {
        LinearLayout llDias = findViewById(R.id.llDias);
        LayoutInflater inflater = LayoutInflater.from(this);
        String[] letras = getResources().getStringArray(R.array.dias_cortos);

        for (int i = 0; i < DIAS_SEMANA; i++) {
            // inflate: convierte item_dia.xml en una View (como hace el adaptador con cada fila)
            View celda = inflater.inflate(R.layout.item_dia, llDias, false);
            celdas[i] = celda;
            tvLetras[i] = celda.findViewById(R.id.tvLetra);
            tvNumeros[i] = celda.findViewById(R.id.tvNumero);
            puntos[i] = celda.findViewById(R.id.vPunto);
            tvLetras[i].setText(letras[i]);

            // "final": la clase anónima de abajo necesita recordar el número de esta celda
            final int indice = i;
            celda.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    indiceElegido = indice;
                    mostrarSemana();
                }
            });

            llDias.addView(celda); // agrega la celda a la tira
        }
        colorTextoNormal = tvNumeros[0].getTextColors();
    }

    // Pinta la semana: números, puntos, día resaltado, mes, título del día y sus sesiones
    private void mostrarSemana() {
        // Fechas de esta semana que tienen sesiones (para los puntos)
        Set<String> conSesiones = controlador.diasConSesiones(fechaParaBuscar(lunes));

        for (int i = 0; i < DIAS_SEMANA; i++) {
            Calendar dia = diaDeLaSemana(i);
            tvNumeros[i].setText(String.valueOf(dia.get(Calendar.DAY_OF_MONTH)));

            // Punto: visible si ese día tiene sesiones (INVISIBLE ocupa su lugar, así no se mueve nada)
            if (conSesiones.contains(fechaParaBuscar(dia))) {
                puntos[i].setVisibility(View.VISIBLE);
            } else {
                puntos[i].setVisibility(View.INVISIBLE);
            }

            // Día elegido: fondo azul y texto blanco. Los demás: borde gris y texto normal
            if (i == indiceElegido) {
                celdas[i].setBackgroundResource(R.drawable.fondo_dia_elegido);
                tvLetras[i].setTextColor(Color.WHITE);
                tvNumeros[i].setTextColor(Color.WHITE);
            } else {
                celdas[i].setBackgroundResource(R.drawable.fondo_dia);
                tvLetras[i].setTextColor(colorTextoNormal);
                tvNumeros[i].setTextColor(colorTextoNormal);
            }
        }

        Calendar elegido = diaDeLaSemana(indiceElegido);
        // "Nov 2026" (mes del día elegido)
        tvMes.setText(meses[elegido.get(Calendar.MONTH)] + " " + elegido.get(Calendar.YEAR));
        // "Viernes 13/11/2026"
        tvDiaElegido.setText(diasLargos[indiceElegido] + " " + String.format(Locale.ROOT,
                "%02d/%02d/%04d", elegido.get(Calendar.DAY_OF_MONTH),
                elegido.get(Calendar.MONTH) + 1, elegido.get(Calendar.YEAR)));

        // Sesiones del día elegido, ordenadas por hora
        adaptador.actualizar(controlador.listarPorFecha(fechaParaBuscar(elegido)));
    }

    // Devuelve una copia del lunes avanzada "indice" días (0 = lunes ... 6 = domingo)
    private Calendar diaDeLaSemana(int indice) {
        Calendar dia = (Calendar) lunes.clone(); // copia: así el lunes original no cambia
        dia.add(Calendar.DAY_OF_MONTH, indice);
        return dia;
    }

    // Posición de un día en la semana que empieza en lunes: lunes = 0 ... domingo = 6.
    // Calendar numera distinto: domingo = 1, lunes = 2 ... sábado = 7
    private int indiceDelDia(Calendar fecha) {
        return (fecha.get(Calendar.DAY_OF_WEEK) + 5) % 7;
    }

    // Fecha como AAAA-MM-DD para buscar en la base de datos (mes + 1 porque Calendar empieza en 0)
    private String fechaParaBuscar(Calendar fecha) {
        return String.format(Locale.ROOT, "%04d-%02d-%02d", fecha.get(Calendar.YEAR),
                fecha.get(Calendar.MONTH) + 1, fecha.get(Calendar.DAY_OF_MONTH));
    }
}