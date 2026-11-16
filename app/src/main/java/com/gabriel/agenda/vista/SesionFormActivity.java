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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
public class SesionFormActivity extends PantallaBase {

    // Nombre del dato "fecha" que puede llegar en el Intent (lo manda CalendarioActivity)
    public static final String EXTRA_FECHA = "fecha_sesion";
    // Nombre del dato "id" que viaja en el Intent al editar una sesión
    public static final String EXTRA_ID = "id_sesion";
    // Valor que significa "no llegó ningún id" → sesión nueva
    private static final int SIN_ID = -1;

    // Id de la sesión que se edita, o SIN_ID si es una nueva
    private int idSesion;

    // Elementos del formulario
    private Spinner spMateria;
    private EditText etFecha;
    private EditText etInicio;
    private EditText etFin;
    private RadioGroup rgEstado;
    private EditText etNotas;
    private Button btnTerminar;

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
        TextView tvTitulo = findViewById(R.id.tvTitulo);
        spMateria = findViewById(R.id.spMateria);
        etFecha = findViewById(R.id.etFecha);
        etInicio = findViewById(R.id.etInicio);
        etFin = findViewById(R.id.etFin);
        rgEstado = findViewById(R.id.rgEstado);
        etNotas = findViewById(R.id.etNotas);
        Button btnCancelar = findViewById(R.id.btnCancelar);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        btnTerminar = findViewById(R.id.btnTerminar);

        llenarSpinner();
        ponerValoresIniciales();

        // Lee el id que mandó la pantalla anterior; si no mandó nada, queda SIN_ID
        idSesion = getIntent().getIntExtra(EXTRA_ID, SIN_ID);

        // Modo edición: cambia el título y reemplaza los valores iniciales por los guardados
        if (idSesion != SIN_ID) {
            tvTitulo.setText(R.string.titulo_editar_sesion);
            cargarSesion();
        }

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
        btnTerminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                terminarAhora();
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
        leerFecha(getIntent().getStringExtra(EXTRA_FECHA));

        // Math.min(..., 22): así el fin nunca pasa de las 23:00
        horaInicio = Math.min(hoy.get(Calendar.HOUR_OF_DAY) + 1, 22);
        minutoInicio = 0;
        horaFin = horaInicio + 1;
        minutoFin = 0;

        mostrarFecha();
        etInicio.setText(formatoHora(horaInicio, minutoInicio));
        etFin.setText(formatoHora(horaFin, minutoFin));
    }

    // Pide la sesión al controlador y pone sus datos en el formulario
    private void cargarSesion() {
        SesionEstudio sesion = controlador.obtener(idSesion);
        if (sesion == null) {
            finish(); // la sesión ya no existe: no hay nada que editar
            return;
        }

        // Materia: busca su posición en la lista y la elige en el Spinner
        // (+ 1 porque la posición 0 del Spinner es "Elija una materia")
        for (int i = 0; i < materias.size(); i++) {
            if (materias.get(i).getId() == sesion.getMateriaId()) {
                spMateria.setSelection(i + 1);
            }
        }

        // Fecha y horas: de texto ("2026-11-13", "19:00") a números para los selectores
        leerFecha(sesion.getFecha());
        mostrarFecha();
        int[] inicio = leerHora(sesion.getHoraInicio());
        horaInicio = inicio[0];
        minutoInicio = inicio[1];
        int[] fin = leerHora(sesion.getHoraFin());
        horaFin = fin[0];
        minutoFin = fin[1];
        etInicio.setText(formatoHora(horaInicio, minutoInicio));
        etFin.setText(formatoHora(horaFin, minutoFin));

        // Estado: marca el RadioButton que corresponde
        if (sesion.getEstado().equals(SesionEstudio.ESTADO_COMPLETADA)) {
            rgEstado.check(R.id.rbCompletada);
        } else if (sesion.getEstado().equals(SesionEstudio.ESTADO_REPROGRAMADA)) {
            rgEstado.check(R.id.rbReprogramada);
        } else {
            rgEstado.check(R.id.rbPendiente);
        }

        etNotas.setText(sesion.getNotas());

        // "Terminar ahora" solo aparece si el controlador dice que la sesión está en curso
        if (controlador.porcentajeEnCurso(sesion) != ControladorSesion.NO_EN_CURSO) {
            btnTerminar.setVisibility(View.VISIBLE);
        }
    }

    // Convierte "2026-11-13" en anio, mes y dia. Si la fecha es null o no tiene ese formato, no cambia nada
    private void leerFecha(String fecha) {
        if (fecha != null && fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] partes = fecha.split("-");      // "2026-11-13" → ["2026", "11", "13"]
            anio = Integer.parseInt(partes[0]);
            mes = Integer.parseInt(partes[1]) - 1;   // - 1: para Calendar noviembre es 10
            dia = Integer.parseInt(partes[2]);
        }
    }

    // Convierte "19:30" en {19, 30} (hora y minuto)
    private int[] leerHora(String hora) {
        String[] partes = hora.split(":");
        return new int[]{Integer.parseInt(partes[0]), Integer.parseInt(partes[1])};
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

    // Botón "Terminar ahora": el controlador pone el fin en la hora actual, la marca
    // Completada y la guarda. La vista solo muestra el mensaje y, si salió bien, se cierra
    private void terminarAhora() {
        String mensaje = controlador.terminarAhora(idSesion);
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        if (mensaje.equals(ControladorSesion.SESION_TERMINADA)) {
            finish(); // vuelve al calendario o a Inicio
        }
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

        String mensaje;
        if (idSesion == SIN_ID) {
            // Sesión nueva
            mensaje = controlador.registrar(sesion);
        } else {
            // Sesión existente: se le pone su id para que el DAO sepa qué fila cambiar
            sesion.setId(idSesion);
            mensaje = controlador.actualizar(sesion);
        }

        if (mensaje.equals(ControladorSesion.ERROR_HORAS)) {
            // Error de horas: icono de error en el campo Fin y Toast con el mensaje
            // (el campo no se puede enfocar, por eso el texto se muestra en el Toast)
            etFin.setError(mensaje);
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        } else {
            // Errores del Spinner o de los selectores, y los mensajes de éxito: con Toast
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            if (mensaje.equals(ControladorSesion.SESION_GUARDADA)
                    || mensaje.equals(ControladorSesion.SESION_ACTUALIZADA)) {
                finish(); // vuelve al calendario
            }
        }
    }
}