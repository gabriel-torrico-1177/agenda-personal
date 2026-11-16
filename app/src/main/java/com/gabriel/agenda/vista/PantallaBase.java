package com.gabriel.agenda.vista;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

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

    // ===== Deslizar entre pestañas =====
    // Las 3 pestañas en orden: deslizar a la izquierda va a la siguiente, a la derecha a la anterior
    private static final Class<?>[] PESTANAS =
            {MainActivity.class, CalendarioActivity.class, EstadisticasActivity.class};
    // Zona de cada borde donde no se empieza el gesto (ahí está el gesto Atrás de Android)
    private static final float BORDE_DP = 30;
    // Recorrido mínimo del dedo y velocidad mínima (dp por segundo) para que cuente como deslizar
    private static final float DISTANCIA_MINIMA_DP = 80;
    private static final float VELOCIDAD_MINIMA_DP = 400;

    // Reconoce el gesto "deslizar rápido" (fling). Es null en formularios y en las pantallas del
    // panel Más: ahí no se desliza entre pestañas
    private GestureDetector detectorGestos;
    // true mientras se abre otra pestaña: así un mismo gesto no abre dos pantallas
    private boolean cambiandoDePestana;

    // Se ejecuta al abrir la pantalla (antes que el onCreate de cada pantalla, que llama a super.onCreate)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Si el Intent no trae el dato (por ejemplo, Inicio al abrir la app), es una pestaña: fundido
        animacion = getIntent().getIntExtra(EXTRA_ANIMACION, FUNDIDO);
        registrarAnimaciones();

        // Solo en Inicio, Calendario y Estadísticas se crea el detector del deslizamiento.
        // SimpleOnGestureListener ya trae todos los métodos vacíos: solo se cambia onFling
        if (posicionPestana() != -1) {
            detectorGestos = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent inicio, MotionEvent fin,
                                       float velocidadX, float velocidadY) {
                    return alDeslizar(inicio, fin, velocidadX);
                }
            });
        }
    }

    // Cada vez que la pantalla vuelve a verse se puede deslizar otra vez
    @Override
    protected void onResume() {
        super.onResume();
        cambiandoDePestana = false;
    }

    // Android pasa POR AQUÍ cada toque de la pantalla antes de entregarlo a la vista que está bajo
    // el dedo (una lista, un botón...). Así el detector ve el gesto aunque el dedo esté sobre una lista
    @Override
    public boolean dispatchTouchEvent(MotionEvent evento) {
        if (detectorGestos != null && detectorGestos.onTouchEvent(evento)) {
            // Se deslizó y se cambió de pestaña: a la pantalla le llega "CANCEL" en vez de
            // "levantar el dedo", así no cuenta como un toque (no abre la fila que estaba debajo)
            MotionEvent cancelar = MotionEvent.obtain(evento);
            cancelar.setAction(MotionEvent.ACTION_CANCEL);
            super.dispatchTouchEvent(cancelar);
            cancelar.recycle();
            return true;
        }
        // En los demás casos el toque sigue su camino normal (listas, botones, desplazamiento)
        return super.dispatchTouchEvent(evento);
    }

    // Decide si el deslizamiento cuenta y, si cuenta, abre la pestaña de al lado.
    // inicio = donde se apoyó el dedo; fin = donde se levantó. Devuelve true si cambió de pestaña
    private boolean alDeslizar(MotionEvent inicio, MotionEvent fin, float velocidadX) {
        if (inicio == null || cambiandoDePestana) {
            return false;
        }
        float densidad = getResources().getDisplayMetrics().density; // para pasar dp a píxeles
        float anchoPantalla = getResources().getDisplayMetrics().widthPixels;

        // 1. No empezar a menos de 30dp de los bordes (ahí está el gesto Atrás del sistema)
        float borde = BORDE_DP * densidad;
        if (inicio.getX() < borde || inicio.getX() > anchoPantalla - borde) {
            return false;
        }

        // 2. Claramente horizontal: lo que se movió de lado debe ser más del doble de lo que se
        //    movió hacia arriba o abajo. Así desplazar una lista no cambia de pestaña
        float dx = fin.getX() - inicio.getX(); // negativo = hacia la izquierda
        float dy = fin.getY() - inicio.getY();
        if (Math.abs(dx) <= 2 * Math.abs(dy)) {
            return false;
        }

        // 3. Distancia y velocidad mínimas (Math.abs: sin importar la dirección)
        if (Math.abs(dx) < DISTANCIA_MINIMA_DP * densidad
                || Math.abs(velocidadX) < VELOCIDAD_MINIMA_DP * densidad) {
            return false;
        }

        // 4. Izquierda = pestaña siguiente; derecha = anterior. Si no hay (a la derecha en Inicio
        //    o a la izquierda en Estadísticas), no pasa nada
        int destino;
        if (dx < 0) {
            destino = posicionPestana() + 1;
        } else {
            destino = posicionPestana() - 1;
        }
        if (destino < 0 || destino >= PESTANAS.length) {
            return false;
        }

        // 5. Se abre igual que con el menú inferior (mismo fundido, y la pantalla nueva
        //    resalta su propio botón del menú)
        cambiandoDePestana = true;
        MenuInferior.abrir(this, PESTANAS[destino]);
        return true;
    }

    // Posición de esta pantalla entre las pestañas (0 = Inicio, 1 = Calendario, 2 = Estadísticas),
    // o -1 si no es una de ellas (formularios y pantallas del panel Más)
    private int posicionPestana() {
        for (int i = 0; i < PESTANAS.length; i++) {
            if (getClass() == PESTANAS[i]) {
                return i;
            }
        }
        return -1;
    }

    // Android lo llama justo después de setContentView (cuando la pantalla ya tiene su diseño).
    // Si el diseño tiene la flecha ← (boton_volver.xml, id btnVolver), al tocarla se cierra la
    // pantalla con finish(), que usa la animación de cierre de abajo. Así ninguna pantalla
    // necesita su propio código para la flecha
    @Override
    public void onContentChanged() {
        super.onContentChanged();
        View btnVolver = findViewById(R.id.btnVolver);
        if (btnVolver != null) { // las pestañas principales (Inicio, Calendario...) no la tienen
            btnVolver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
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
