package com.gabriel.agenda.vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

// R está en el paquete base (com.gabriel.agenda), por eso hay que importarlo
import com.gabriel.agenda.R;

// Pantalla P1: panel principal de la agenda (capa vista)
public class MainActivity extends AppCompatActivity {

    // Se ejecuta al abrir la pantalla: carga el diseño activity_main.xml
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        // Deja espacio para la barra de estado y la de navegación del teléfono
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Botón "+ Nuevo contacto": abre el formulario con un Intent
        Button btnNuevoContacto = findViewById(R.id.btnNuevoContacto);
        btnNuevoContacto.setOnClickListener(v -> {
            Intent intent = new Intent(this, ContactoFormActivity.class);
            startActivity(intent);
        });
    }
}