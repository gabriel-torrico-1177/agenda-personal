package com.gabriel.agenda.vista;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gabriel.agenda.R;
import com.gabriel.agenda.controlador.ControladorMateria;
import com.gabriel.agenda.modelo.Materia;

// Pantalla P5: formulario para registrar o editar una materia (capa vista)
public class MateriaFormActivity extends AppCompatActivity {

    // Nombre del dato "id" que viaja en el Intent (lo usa también MateriasActivity)
    public static final String EXTRA_ID = "id_materia";
    // Valor que significa "no llegó ningún id" → materia nueva
    private static final int SIN_ID = -1;

    // Id de la materia que se edita, o SIN_ID si es una nueva
    private int idMateria;

    // Campos de texto del formulario
    private EditText etNombre;
    private EditText etDocente;

    // Los 6 círculos de color (cada uno guarda su color en android:tag)
    private View[] circulosColor;

    // Color elegido en formato "#RRGGBB". Empieza con el azul por defecto
    private String colorElegido = ControladorMateria.COLOR_POR_DEFECTO;

    // La vista solo habla con el controlador
    private ControladorMateria controlador;

    // Se ejecuta al abrir la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_materia_form);
        // Deja espacio para las barras del sistema y el teclado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        // Conecta las variables con los elementos del layout por su id
        TextView tvTitulo = findViewById(R.id.tvTitulo);
        etNombre = findViewById(R.id.etNombre);
        etDocente = findViewById(R.id.etDocente);
        Button btnCancelar = findViewById(R.id.btnCancelar);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        circulosColor = new View[]{
                findViewById(R.id.color1), findViewById(R.id.color2),
                findViewById(R.id.color3), findViewById(R.id.color4),
                findViewById(R.id.color5), findViewById(R.id.color6)
        };

        controlador = new ControladorMateria(this);

        // Lee el id que mandó la pantalla anterior; si no mandó nada, queda SIN_ID
        idMateria = getIntent().getIntExtra(EXTRA_ID, SIN_ID);

        // Modo edición: cambia el título y carga los datos guardados (también su color)
        if (idMateria != SIN_ID) {
            tvTitulo.setText(R.string.titulo_editar_materia);
            cargarMateria();
        }

        // Un solo listener para los 6 círculos: lee el color del círculo tocado (su tag)
        View.OnClickListener alTocarColor = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorElegido = (String) v.getTag();
                pintarColores(); // vuelve a dibujar para mover el borde al elegido
            }
        };
        for (View circulo : circulosColor) {
            circulo.setOnClickListener(alTocarColor);
        }
        pintarColores(); // dibujo inicial: azul si es nueva, o el color guardado si se edita

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

    // Pide la materia al controlador y pone sus datos en los campos y en colorElegido
    private void cargarMateria() {
        Materia materia = controlador.obtener(idMateria);
        if (materia == null) {
            finish(); // la materia ya no existe: no hay nada que editar
            return;
        }
        etNombre.setText(materia.getNombre());
        etDocente.setText(materia.getDocente());
        colorElegido = materia.getColor(); // pintarColores() marcará este círculo
    }

    // Dibuja cada círculo con su color; el elegido lleva un borde oscuro grueso
    private void pintarColores() {
        float densidad = getResources().getDisplayMetrics().density; // para pasar dp a píxeles
        for (View circulo : circulosColor) {
            String color = (String) circulo.getTag();

            GradientDrawable forma = new GradientDrawable();
            forma.setShape(GradientDrawable.OVAL);         // círculo
            forma.setColor(Color.parseColor(color));        // relleno con el color del tag
            // equalsIgnoreCase: "#3f51b5" y "#3F51B5" cuentan como el mismo color
            boolean elegido = color.equalsIgnoreCase(colorElegido);
            if (elegido) {
                forma.setStroke((int) (4 * densidad), Color.BLACK); // borde de 4dp = elegido
            }
            circulo.setBackground(forma);
            circulo.setSelected(elegido); // para TalkBack (accesibilidad)
        }
    }

    // Lee los campos, pide al controlador que registre o actualice y muestra el resultado
    private void guardar() {
        etNombre.setError(null); // quita el error anterior

        String nombre = etNombre.getText().toString();
        String docente = etDocente.getText().toString();

        String mensaje;
        if (idMateria == SIN_ID) {
            // Materia nueva
            mensaje = controlador.registrar(nombre, docente, colorElegido);
        } else {
            // Materia existente: se usa el constructor CON id
            Materia materia = new Materia(idMateria, nombre, docente, colorElegido);
            mensaje = controlador.actualizar(materia);
        }

        // Los dos errores son del nombre: se marcan en ese campo
        if (mensaje.equals(ControladorMateria.ERROR_NOMBRE)
                || mensaje.equals(ControladorMateria.ERROR_NOMBRE_REPETIDO)) {
            etNombre.setError(mensaje);
            etNombre.requestFocus();
        } else {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            if (mensaje.equals(ControladorMateria.MATERIA_GUARDADA)
                    || mensaje.equals(ControladorMateria.MATERIA_ACTUALIZADA)) {
                finish(); // vuelve a la lista de materias
            }
        }
    }
}