package com.gabriel.agenda.vista;

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
import com.gabriel.agenda.controlador.ControladorContacto;
import com.gabriel.agenda.modelo.Contacto;

// Pantalla P3: formulario para registrar o editar un contacto (capa vista)
public class ContactoFormActivity extends AppCompatActivity {

    // Nombre del dato "id" que viaja en el Intent (lo usa también ContactosActivity)
    public static final String EXTRA_ID = "id_contacto";
    // Valor que significa "no llegó ningún id" → contacto nuevo
    private static final int SIN_ID = -1;

    // Campos de texto del formulario
    private EditText etNombre;
    private EditText etTelefono;
    private EditText etCorreo;

    // La vista solo habla con el controlador
    private ControladorContacto controlador;

    // Id del contacto que se edita, o SIN_ID si es uno nuevo
    private int idContacto;

    // Se ejecuta al abrir la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_contacto_form);
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
        etTelefono = findViewById(R.id.etTelefono);
        etCorreo = findViewById(R.id.etCorreo);
        Button btnCancelar = findViewById(R.id.btnCancelar);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        controlador = new ControladorContacto(this);

        // Lee el id que mandó la pantalla anterior; si no mandó nada, queda SIN_ID
        idContacto = getIntent().getIntExtra(EXTRA_ID, SIN_ID);

        // Modo edición: cambia el título y carga los datos guardados
        if (idContacto != SIN_ID) {
            tvTitulo.setText(R.string.titulo_editar_contacto);
            cargarContacto();
        }

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

    // Pide el contacto al controlador y pone sus datos en los campos
    private void cargarContacto() {
        Contacto contacto = controlador.obtener(idContacto);
        if (contacto == null) {
            finish(); // el contacto ya no existe: no hay nada que editar
            return;
        }
        etNombre.setText(contacto.getNombre());
        etTelefono.setText(contacto.getTelefono());
        etCorreo.setText(contacto.getCorreo());
    }

    // Lee los campos, pide al controlador que registre o actualice y muestra el resultado
    private void guardar() {
        // Quita los errores anteriores
        etNombre.setError(null);
        etTelefono.setError(null);
        etCorreo.setError(null);

        String nombre = etNombre.getText().toString();
        String telefono = etTelefono.getText().toString();
        String correo = etCorreo.getText().toString();

        String mensaje;
        if (idContacto == SIN_ID) {
            // Contacto nuevo
            mensaje = controlador.registrar(nombre, telefono, correo);
        } else {
            // Contacto existente: se usa el constructor CON id
            Contacto contacto = new Contacto(idContacto, nombre, telefono, correo);
            mensaje = controlador.actualizar(contacto);
        }

        // Según el mensaje, marca el campo con error o muestra el éxito
        if (mensaje.equals(ControladorContacto.ERROR_NOMBRE)) {
            etNombre.setError(mensaje);
            etNombre.requestFocus();
        } else if (mensaje.equals(ControladorContacto.ERROR_TELEFONO)) {
            etTelefono.setError(mensaje);
            etTelefono.requestFocus();
        } else if (mensaje.equals(ControladorContacto.ERROR_CORREO)) {
            etCorreo.setError(mensaje);
            etCorreo.requestFocus();
        } else {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            if (mensaje.equals(ControladorContacto.CONTACTO_GUARDADO)
                    || mensaje.equals(ControladorContacto.CONTACTO_ACTUALIZADO)) {
                finish(); // vuelve a la pantalla anterior
            }
        }
    }
}