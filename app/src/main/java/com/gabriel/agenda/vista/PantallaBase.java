package com.gabriel.agenda.vista;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.gabriel.agenda.R;

// Pantalla base (capa vista): todas las pantallas heredan de esta clase en vez de AppCompatActivity.
// Solo se encarga de las animaciones al abrir y cerrar pantallas (res/anim); no tiene lógica de la app.
//   - Pestañas (Inicio, Calendario, Estadísticas y las del panel Más): fundido de 150 ms.
//   - Formularios (sesión, materia, contacto y meta desde su tarjeta): suben al abrir y bajan al cerrar
public class PantallaBase extends AppCompatActivity {

    // Nombre del dato del Intent que dice con qué animación se abrió la pantalla
    private static final String EXTRA_ANIMACION = "animacion";
    // Las dos animaciones posibles
    private static final int FUNDIDO = 0;
    private static final int DESDE_ABAJO = 1;

    // Animación con la que se abrió ESTA pantalla: decide cómo se cierra
    private int animacion;

    // Se ejecuta al abrir la pantalla (antes que el onCreate de cada pantalla, que llama a super.onCreate)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Si el Intent no trae el dato (por ejemplo, Inicio al abrir la app), es una pestaña: fundido
        animacion = getIntent().getIntExtra(EXTRA_ANIMACION, FUNDIDO);
        registrarAnimaciones();
    }

    // Abre un formulario: sube desde abajo (y al cerrarse bajará)
    public void abrirFormulario(Intent intent) {
        intent.putExtra(EXTRA_ANIMACION, DESDE_ABAJO);
        startActivity(intent);
        animarCambio(R.anim.subir, R.anim.quedarse);
    }

    // Abre otra pestaña encima de esta (desde Inicio, o para volver a Inicio): fundido
    public void abrirPestana(Intent intent) {
        intent.putExtra(EXTRA_ANIMACION, FUNDIDO);
        startActivity(intent);
        animarCambio(R.anim.fundido_entrar, R.anim.quedarse);
    }

    // Abre otra pestaña y cierra esta (así no se apilan), con fundido aunque esta pantalla
    // se haya abierto como formulario (por ejemplo, Meta desde su tarjeta)
    public void cambiarAPestana(Intent intent) {
        animacion = FUNDIDO;
        registrarAnimaciones();
        intent.putExtra(EXTRA_ANIMACION, FUNDIDO);
        startActivity(intent);
        super.finish(); // el finish() de AppCompatActivity: sin la animación de cierre de abajo
        animarCambio(R.anim.fundido_entrar, R.anim.quedarse);
    }

    // Cierra la pantalla con su animación. Guardar, Cancelar y el botón Atrás terminan aquí
    @Override
    public void finish() {
        super.finish();
        if (animacion == DESDE_ABAJO) {
            animarCambio(R.anim.quedarse, R.anim.bajar);           // el formulario baja
        } else {
            animarCambio(R.anim.quedarse, R.anim.fundido_salir);   // la pestaña se desvanece
        }
    }

    // overridePendingTransition: cambia la animación del cambio de pantalla que se acaba de pedir
    // (startActivity o finish). entrada = la pantalla que aparece, salida = la que se va.
    // Desde Android 14 está "deprecated" (hay una forma nueva), pero sigue funcionando
    @SuppressWarnings("deprecation")
    private void animarCambio(int entrada, int salida) {
        overridePendingTransition(entrada, salida);
    }

    // Android 14 (API 34) o más: registra en la pantalla misma cómo se abre y cómo se cierra.
    // El sistema lo usa cuando cierra la pantalla por su cuenta, por ejemplo con el gesto Atrás
    // "predictivo" (que se ve mientras deslizas el dedo); en esos casos no pasa por finish()
    private void registrarAnimaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (animacion == DESDE_ABAJO) {
                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.subir, R.anim.quedarse);
                overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.quedarse, R.anim.bajar);
            } else {
                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                        R.anim.fundido_entrar, R.anim.quedarse);
                overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE,
                        R.anim.quedarse, R.anim.fundido_salir);
            }
        }
    }
}
