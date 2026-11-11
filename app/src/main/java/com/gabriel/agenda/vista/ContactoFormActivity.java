package com.gabriel.agenda.vista;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gabriel.agenda.R;
import com.gabriel.agenda.controlador.ControladorContacto;

// Pantalla P3: formulario para registrar un contacto (capa vista)
public class ContactoFormActivity extends AppCompatActivity {

    // Campos de texto del formulario
    private EditText etNombre;
    private EditText etTelefono;
    private EditText etCorreo;

    // La vista solo habla con el controlador
    private ControladorContacto controlador;

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
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etCorreo = findViewById(R.id.etCorreo);
        Button btnCancelar = findViewById(R.id.btnCancelar);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        controlador = new ControladorContacto(this);

        // Qué hacer al tocar cada botón
        btnGuardar.setOnClickListener(v -> guardar());
        btnCancelar.setOnClickListener(v -> finish()); // cerrar sin guardar
    }

    // Lee los campos, pide al controlador que registre y muestra el resultado
    private void guardar() {
        // Quita los errores anteriores
        etNombre.setError(null);
        etTelefono.setError(null);
        etCorreo.setError(null);

        String nombre = etNombre.getText().toString();
        String telefono = etTelefono.getText().toString();
        String correo = etCorreo.getText().toString();

        String mensaje = controlador.registrar(nombre, telefono, correo);

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
            if (mensaje.equals(ControladorContacto.CONTACTO_GUARDADO)) {
                finish(); // vuelve a la pantalla anterior
            }
        }
    }
}