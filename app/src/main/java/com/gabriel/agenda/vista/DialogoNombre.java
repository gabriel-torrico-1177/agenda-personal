package com.gabriel.agenda.vista;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.gabriel.agenda.R;
import com.gabriel.agenda.controlador.ControladorPerfil;

// Diálogo "¿Cómo te llamas?" (capa vista). Se usa en tres lugares, por eso está en su propia clase:
// la primera vez que se abre la app, al tocar el avatar de Inicio y desde "Mi perfil" del panel Más
public class DialogoNombre {

    // Muestra el diálogo sobre la pantalla indicada.
    // primeraVez = true: no tiene "Cancelar" y no se puede cerrar sin escribir un nombre
    public static void mostrar(final PantallaBase pantalla, boolean primeraVez) {
        final ControladorPerfil controlador = new ControladorPerfil(pantalla);

        // Contenido del diálogo: dialogo_nombre.xml (el campo), con el nombre actual ya cargado
        View contenido = LayoutInflater.from(pantalla).inflate(R.layout.dialogo_nombre, null);
        final EditText etNombre = contenido.findViewById(R.id.etNombrePerfil);
        etNombre.setText(controlador.obtenerNombre());
        etNombre.setSelection(etNombre.getText().length()); // cursor al final del texto

        // Guardar con null: si se le pusiera aquí el clic, el diálogo se cerraría siempre,
        // aunque el nombre esté vacío. Su clic se pone más abajo, después de show()
        AlertDialog.Builder constructor = new AlertDialog.Builder(pantalla)
                .setTitle(R.string.titulo_perfil)
                .setView(contenido)
                .setPositiveButton(R.string.btn_guardar, null);
        if (primeraVez) {
            // Sin "Cancelar", y tampoco se cierra con Atrás ni tocando afuera
            constructor.setCancelable(false);
        } else {
            // Cancelar con null: solo cierra el diálogo (no cambia nada)
            constructor.setNegativeButton(R.string.btn_cancelar, null);
        }
        final AlertDialog dialogo = constructor.create();
        dialogo.show();

        // Ahora que el diálogo ya se ve, existe su botón Guardar y se le cambia el clic:
        // el diálogo solo se cierra (dismiss) si el controlador dice "Nombre guardado"
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mensaje = controlador.guardarNombre(etNombre.getText().toString());
                if (mensaje.equals(ControladorPerfil.NOMBRE_GUARDADO)) {
                    Toast.makeText(pantalla, mensaje, Toast.LENGTH_SHORT).show();
                    dialogo.dismiss();
                    pantalla.alCambiarNombre(); // Inicio actualiza el saludo y la inicial
                } else {
                    // requestFocus: el texto del error solo se ve si el campo tiene el foco
                    // (sin foco, setError muestra solo el ícono rojo)
                    etNombre.requestFocus();
                    etNombre.setError(mensaje); // "Ingrese su nombre" junto al campo
                }
            }
        });
    }
}
