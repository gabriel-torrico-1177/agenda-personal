package com.gabriel.agenda.vista;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gabriel.agenda.R;
import com.gabriel.agenda.controlador.ControladorMateria;
import com.gabriel.agenda.controlador.ControladorSesion;
import com.gabriel.agenda.modelo.Materia;
import com.gabriel.agenda.modelo.SesionEstudio;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// Pantalla P3: formulario para registrar una sesión de estudio (capa vista)
public class SesionFormActivity extends AppCompatActivity {

    // Nombre del dato "fecha" que puede llegar en el Intent (lo manda CalendarioActivity)
    public static final String EXTRA_FECHA = "fecha_sesion";

    // Elementos del formulario
    private Spinner spMateria;
    private EditText etFecha;
    private EditText etInicio;
    private EditText etFin;
    private RadioGroup rgEstado;
    private EditText etNotas;

    // Materias que se muestran en el Spinner (en el mismo orden, después de "Elija una materia")
    private List<Materia> materias;

    // Fecha y horas elegidas con los selectores (se guardan como números)
    private int anio;
    private int mes;          // OJO: Calendar y DatePicker cuentan los meses de 0 (enero) a 11 (diciembre)
    private int dia;
    private int horaInicio;
    private int minutoInicio;
    private int horaFin;
    private int minutoFin;

    // La vista solo habla con los controladores
    private ControladorSesion controlador;

    // Se ejecuta al abrir la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_sesion_form);
        // Deja espacio para las barras del sistema y el teclado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        controlador = new ControladorSesion(this);

        // Sin materias no se puede planificar: avisa y cierra el formulario
        materias = new ControladorMateria(this).listar();
        if (materias.isEmpty()) {
            Toast.makeText(this, ControladorSesion.SIN_MATERIAS, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Conecta las variables con los elementos del layout por su id
        spMateria = findViewById(R.id.spMateria);
        etFecha = findViewById(R.id.etFecha);
        etInicio = findViewById(R.id.etInicio);
        etFin = findViewById(R.id.etFin);
        rgEstado = findViewById(R.id.rgEstado);
        etNotas = findViewById(R.id.etNotas);
        Button btnCancelar = findViewById(R.id.btnCancelar);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        llenarSpinner();
        ponerValoresIniciales();

        // Tocar la fecha abre el calendario; tocar una hora abre el reloj
        etFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elegirFecha();
            }
        });
        etInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elegirHoraInicio();
            }
        });
        etFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elegirHoraFin();
            }
        });

        // Qué hacer al tocar cada botón
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardar();
            }
        });
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // cerrar sin guardar
            }
        });
    }

    // Llena el Spinner: primero "Elija una materia" y después el nombre de cada materia
    private void llenarSpinner() {
        List<String> nombres = new ArrayList<>();
        nombres.add(getString(R.string.opcion_elija_materia)); // posición 0 = ninguna
        for (Materia materia : materias) {
            nombres.add(materia.getNombre());
        }
        // ArrayAdapter: adaptador sencillo que muestra una lista de textos
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nombres);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMateria.setAdapter(adaptador);
    }

    // Fecha: la que mandó el calendario, o la de hoy si no llegó ninguna.
    // Inicio a la próxima hora en punto y fin una hora después
    private void ponerValoresIniciales() {
        Calendar hoy = Calendar.getInstance();
        anio = hoy.get(Calendar.YEAR);
        mes = hoy.get(Calendar.MONTH);
        dia = hoy.get(Calendar.DAY_OF_MONTH);

        // getStringExtra devuelve null si la pantalla anterior no mandó la fecha
        String fechaRecibida = getIntent().getStringExtra(EXTRA_FECHA);
        if (fechaRecibida != null && fechaRecibida.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] partes = fechaRecibida.split("-");  // "2026-11-13" → ["2026", "11", "13"]
            anio = Integer.parseInt(partes[0]);
            mes = Integer.parseInt(partes[1]) - 1;        // - 1: para Calendar noviembre es 10
            dia = Integer.parseInt(partes[2]);
        }

        // Math.min(..., 22): así el fin nunca pasa de las 23:00
        horaInicio = Math.min(hoy.get(Calendar.HOUR_OF_DAY) + 1, 22);
        minutoInicio = 0;
        horaFin = horaInicio + 1;
        minutoFin = 0;

        mostrarFecha();
        etInicio.setText(formatoHora(horaInicio, minutoInicio));
        etFin.setText(formatoHora(horaFin, minutoFin));
    }

    // Abre el calendario con la fecha elegida; al aceptar, guarda la nueva fecha
    private void elegirFecha() {
        DatePickerDialog dialogo = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker vista, int anioElegido, int mesElegido,
                                          int diaElegido) {
                        anio = anioElegido;
                        mes = mesElegido;   // de 0 a 11
                        dia = diaElegido;
                        mostrarFecha();
                    }
                }, anio, mes, dia);       // fecha con la que se abre el calendario
        dialogo.show();
    }

    // Abre el reloj con la hora de inicio elegida; al aceptar, guarda la nueva hora
    private void elegirHoraInicio() {
        TimePickerDialog dialogo = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker vista, int hora, int minuto) {
                        horaInicio = hora;
                        minutoInicio = minuto;
                        etInicio.setText(formatoHora(hora, minuto));
                        etFin.setError(null); // quita el error de horas, si había
                    }
                }, horaInicio, minutoInicio, true); // true = reloj de 24 horas
        dialogo.show();
    }

    // Abre el reloj con la hora de fin elegida; al aceptar, guarda la nueva hora
    private void elegirHoraFin() {
        TimePickerDialog dialogo = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker vista, int hora, int minuto) {
                        horaFin = hora;
                        minutoFin = minuto;
                        etFin.setText(formatoHora(hora, minuto));
                        etFin.setError(null); // quita el error de horas, si había
                    }
                }, horaFin, minutoFin, true);
        dialogo.show();
    }

    // Muestra la fecha al usuario como DD/MM/AAAA (mes + 1 porque Calendar empieza en 0)
    private void mostrarFecha() {
        etFecha.setText(String.format(Locale.ROOT, "%02d/%02d/%04d", dia, mes + 1, anio));
    }

    // Fecha para la base de datos: AAAA-MM-DD (por ejemplo "2026-11-13")
    private String fechaParaGuardar() {
        return String.format(Locale.ROOT, "%04d-%02d-%02d", anio, mes + 1, dia);
    }

    // Hora como HH:MM, siempre con dos dígitos: 9 y 5 → "09:05"
    private String formatoHora(int hora, int minuto) {
        return String.format(Locale.ROOT, "%02d:%02d", hora, minuto);
    }

    // Estado según el RadioButton marcado (se usan las constantes, no el texto de la pantalla)
    private String estadoElegido() {
        int idMarcado = rgEstado.getCheckedRadioButtonId();
        if (idMarcado == R.id.rbCompletada) {
            return SesionEstudio.ESTADO_COMPLETADA;
        } else if (idMarcado == R.id.rbReprogramada) {
            return SesionEstudio.ESTADO_REPROGRAMADA;
        }
        return SesionEstudio.ESTADO_PENDIENTE;
    }

    // Id de la materia elegida en el Spinner, o 0 si sigue en "Elija una materia"
    private int materiaElegida() {
        int posicion = spMateria.getSelectedItemPosition();
        if (posicion <= 0) {
            return 0;
        }
        // posición 1 del Spinner = materia 0 de la lista (por el "Elija una materia" del inicio)
        return materias.get(posicion - 1).getId();
    }

    // Arma la sesión con lo elegido, pide al controlador que la registre y muestra el resultado
    private void guardar() {
        etFin.setError(null); // quita el error anterior

        SesionEstudio sesion = new SesionEstudio(
                materiaElegida(),
                fechaParaGuardar(),
                formatoHora(horaInicio, minutoInicio),
                formatoHora(horaFin, minutoFin),
                estadoElegido(),
                etNotas.getText().toString());

        String mensaje = controlador.registrar(sesion);

        if (mensaje.equals(ControladorSesion.ERROR_HORAS)) {
            // Error de horas: icono de error en el campo Fin y Toast con el mensaje
            // (el campo no se puede enfocar, por eso el texto se muestra en el Toast)
            etFin.setError(mensaje);
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        } else {
            // Errores del Spinner o de los selectores, y los mensajes de éxito: con Toast
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            if (mensaje.equals(ControladorSesion.SESION_GUARDADA)) {
                finish(); // vuelve a la pantalla anterior
            }
        }
    }
}