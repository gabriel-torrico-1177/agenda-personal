package com.gabriel.agenda.vista;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

// R está en el paquete base (com.gabriel.agenda), por eso hay que importarlo
import com.gabriel.agenda.R;

// Pantalla P1: Inicio (capa vista). El panel completo se arma en HU-13
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

        // Menú inferior con "Inicio" resaltado
        MenuInferior.configurar(this, R.id.menuInicio);
    }
}
