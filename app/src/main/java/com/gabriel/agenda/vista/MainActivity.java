package com.gabriel.agenda.vista;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

// R está en el paquete base (com.gabriel.agenda), por eso hay que importarlo
import com.gabriel.agenda.R;
import com.gabriel.agenda.controlador.ControladorMeta;
import com.gabriel.agenda.controlador.ControladorPerfil;
import com.gabriel.agenda.controlador.ControladorSesion;
import com.gabriel.agenda.modelo.MetaSemanal;
import com.gabriel.agenda.modelo.SesionEstudio;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

// Pantalla P1: Inicio (capa vista). Muestra el progreso de la meta, la próxima sesión
// y las sesiones de hoy. Los cálculos los hacen ControladorMeta y ControladorSesion
public class MainActivity extends PantallaBase {

    private TextView tvSaludo;
    private TextView tvAvatar;
    private TextView tvFecha;
    private TextView tvProgreso;
    private ProgressBar pbMeta;
    private TextView tvMensajeMeta;
    private View llProxima;
    private TextView tvProximaMateria;
    private TextView tvProximaHorario;
    private SesionAdapter adaptador;

    private ControladorMeta controladorMeta;
    private ControladorSesion controladorSesion;
    private ControladorPerfil controladorPerfil;
    // Próxima sesión que se está mostrando (null si no hay); se usa al tocarla
    private SesionEstudio proxima;

    // Handler: permite programar una tarea para "dentro de un rato" en el hilo de la pantalla
    private final Handler reloj = new Handler(Looper.getMainLooper());
    // Tarea que se repite cada minuto: vuelve a pintar la pantalla (así "En curso" y su barra
    // avanzan solos) y se vuelve a programar para el siguiente cambio de minuto
    private final Runnable actualizarCadaMinuto = new Runnable() {
        @Override
        public void run() {
            mostrarTodo();
            reloj.postDelayed(this, controladorSesion.milisHastaElSiguienteMinuto());
        }
    };

    // Se ejecuta al abrir la pantalla: carga el diseño y conecta los botones
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        // Deja espacio para las barras del teléfono. El de ARRIBA (barra de estado) va dentro de la
        // franja verde, así la franja también pinta de verde detrás de la hora y la batería.
        // Los demás lados quedan en la pantalla completa
        final View franja = findViewById(R.id.franja);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            franja.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
        // Íconos oscuros en la barra de estado (hora, batería): se leen mejor sobre el verde
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);

        // Menú inferior con "Inicio" resaltado
        MenuInferior.configurar(this, R.id.menuInicio);

        // Conecta las variables con el layout (la tarjeta viene de tarjeta_meta.xml por <include>)
        tvSaludo = findViewById(R.id.tvSaludo);
        tvAvatar = findViewById(R.id.tvAvatar);
        tvFecha = findViewById(R.id.tvFecha);
        tvProgreso = findViewById(R.id.tvProgreso);
        pbMeta = findViewById(R.id.pbMeta);
        tvMensajeMeta = findViewById(R.id.tvMensajeMeta);
        llProxima = findViewById(R.id.llProxima);
        tvProximaMateria = findViewById(R.id.tvProximaMateria);
        tvProximaHorario = findViewById(R.id.tvProximaHorario);
        View tarjetaMeta = findViewById(R.id.tarjetaMeta);
        Button btnNuevaSesion = findViewById(R.id.btnNuevaSesion);
        ListView lvHoy = findViewById(R.id.lvHoy);
        TextView tvVacioHoy = findViewById(R.id.tvVacioHoy);

        controladorMeta = new ControladorMeta(this);
        controladorSesion = new ControladorSesion(this);
        controladorPerfil = new ControladorPerfil(this);

        // Sesiones de hoy: null en el tercer parámetro = sin papelera (en Inicio no se elimina)
        adaptador = new SesionAdapter(this, new ArrayList<SesionEstudio>(), null);
        lvHoy.setAdapter(adaptador);
        lvHoy.setEmptyView(tvVacioHoy);

        // Tocar una sesión de hoy: abre el formulario en modo edición (por ejemplo, para completarla)
        lvHoy.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int posicion, long id) {
                abrirSesion((int) id);
            }
        });

        // Tocar la tarjeta de la meta: abre Meta semanal
        tarjetaMeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirFormulario(new Intent(MainActivity.this, MetaActivity.class));
            }
        });

        // Tocar la próxima sesión: abre el formulario para editarla
        llProxima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (proxima != null) {
                    abrirSesion(proxima.getId());
                }
            }
        });

        // "+ Nueva sesión": abre el formulario vacío (con la fecha de hoy).
        // Si no hay materias, el formulario muestra "Primero registre una materia" y se cierra
        btnNuevaSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirFormulario(new Intent(MainActivity.this, SesionFormActivity.class));
            }
        });

        // Tocar el avatar: abre el diálogo del nombre (con "Cancelar" y el nombre actual cargado)
        tvAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogoNombre.mostrar(MainActivity.this, false);
            }
        });

        // Primera vez que se abre la app (todavía no hay nombre): se pide solo y no se puede cancelar
        if (!controladorPerfil.tieneNombre()) {
            DialogoNombre.mostrar(this, true);
        }
    }

    // DialogoNombre llama a este método al guardar el nombre: actualiza el saludo y la inicial
    @Override
    public void alCambiarNombre() {
        mostrarSaludo();
    }

    // Se ejecuta cada vez que la pantalla vuelve a verse (al volver de Meta o del formulario):
    // así el progreso y las listas siempre están al día
    @Override
    protected void onResume() {
        super.onResume();
        mostrarTodo();
        // Empieza a refrescar la pantalla cada minuto mientras está abierta
        reloj.postDelayed(actualizarCadaMinuto, controladorSesion.milisHastaElSiguienteMinuto());
    }

    // Se ejecuta cuando la pantalla deja de verse (se abre otra, o se sale de la app)
    @Override
    protected void onPause() {
        super.onPause();
        // Cancela el refresco: no hace falta repintar una pantalla que no se ve
        reloj.removeCallbacks(actualizarCadaMinuto);
    }

    // Pinta el saludo, la fecha, la meta, la próxima sesión y las sesiones de hoy
    private void mostrarTodo() {
        Calendar hoy = Calendar.getInstance();
        mostrarSaludo();
        mostrarFecha(hoy);
        mostrarMeta();
        mostrarProxima(hoy);
        adaptador.actualizar(controladorSesion.listarPorFecha(fechaParaBuscar(hoy)));
    }

    // "¡Hola, Gabriel!" y la inicial "G" en el avatar. Si todavía no hay nombre: "¡Hola!" y "?"
    private void mostrarSaludo() {
        if (controladorPerfil.tieneNombre()) {
            // %1$s del string se reemplaza por el nombre
            tvSaludo.setText(getString(R.string.saludo, controladorPerfil.obtenerNombre()));
        } else {
            tvSaludo.setText(R.string.saludo_sin_nombre);
        }
        tvAvatar.setText(controladorPerfil.obtenerInicial());
    }

    // "Domingo 15/11/2026"
    private void mostrarFecha(Calendar hoy) {
        String[] diasLargos = getResources().getStringArray(R.array.dias_largos);
        // Calendar: domingo = 1 ... sábado = 7. (día + 5) % 7 → lunes = 0 ... domingo = 6
        int indice = (hoy.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        tvFecha.setText(diasLargos[indice] + " " + fechaParaMostrar(hoy));
    }

    // Pinta la tarjeta: "1,5 / 20 h", la barra y el mensaje ("¡Meta cumplida! Sigue así" si llegó)
    private void mostrarMeta() {
        MetaSemanal progreso = controladorMeta.obtenerProgreso(controladorMeta.lunesDeHoy());
        tvProgreso.setText(controladorMeta.textoProgreso(progreso));
        pbMeta.setProgress(controladorMeta.porcentaje(progreso));
        tvMensajeMeta.setText(controladorMeta.mensajeProgreso(progreso));
    }

    // Muestra la próxima sesión sin completar, o "No tienes sesiones pendientes"
    private void mostrarProxima(Calendar hoy) {
        proxima = controladorSesion.proximaSesion();
        if (proxima == null) {
            // Mensaje en gris y sin negrita, para que no parezca el nombre de una materia
            tvProximaMateria.setText(R.string.sin_proxima);
            tvProximaMateria.setTextColor(ContextCompat.getColor(this, R.color.texto_secundario));
            tvProximaMateria.setTypeface(null, Typeface.NORMAL);
            tvProximaHorario.setVisibility(View.GONE);
            llProxima.setClickable(false);
            return;
        }

        // "Hoy" si es de hoy; si no, la fecha "16/11/2026"
        String dia;
        if (proxima.getFecha().equals(fechaParaBuscar(hoy))) {
            dia = getString(R.string.hoy);
        } else {
            dia = fechaGuardadaParaMostrar(proxima.getFecha());
        }
        String horario = getString(R.string.formato_horario,
                proxima.getHoraInicio(), proxima.getHoraFin());

        // Nombre de la materia en blanco y negrita (se vuelve a poner porque la pantalla se repinta
        // cada minuto, y antes pudo quedar en gris con el mensaje "No tienes sesiones pendientes")
        tvProximaMateria.setText(proxima.getNombreMateria());
        tvProximaMateria.setTextColor(ContextCompat.getColor(this, R.color.texto));
        tvProximaMateria.setTypeface(null, Typeface.BOLD);
        tvProximaHorario.setText(getString(R.string.formato_proxima, dia, horario));
        tvProximaHorario.setVisibility(View.VISIBLE);
        llProxima.setClickable(true);
    }

    // Abre el formulario de sesión en modo edición con ese id
    private void abrirSesion(int idSesion) {
        Intent intent = new Intent(MainActivity.this, SesionFormActivity.class);
        intent.putExtra(SesionFormActivity.EXTRA_ID, idSesion);
        abrirFormulario(intent);
    }

    // Fecha como AAAA-MM-DD para buscar en la base de datos (mes + 1 porque Calendar empieza en 0)
    private String fechaParaBuscar(Calendar fecha) {
        return String.format(Locale.ROOT, "%04d-%02d-%02d", fecha.get(Calendar.YEAR),
                fecha.get(Calendar.MONTH) + 1, fecha.get(Calendar.DAY_OF_MONTH));
    }

    // Fecha como DD/MM/AAAA para mostrar: "15/11/2026"
    private String fechaParaMostrar(Calendar fecha) {
        return String.format(Locale.ROOT, "%02d/%02d/%04d", fecha.get(Calendar.DAY_OF_MONTH),
                fecha.get(Calendar.MONTH) + 1, fecha.get(Calendar.YEAR));
    }

    // Convierte "2026-11-16" (como se guarda) en "16/11/2026" (como se muestra)
    private String fechaGuardadaParaMostrar(String fecha) {
        String[] partes = fecha.split("-"); // ["2026", "11", "16"]
        return partes[2] + "/" + partes[1] + "/" + partes[0];
    }
}