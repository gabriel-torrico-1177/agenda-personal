package com.gabriel.agenda.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gabriel.agenda.R;
import com.gabriel.agenda.controlador.ControladorMateria;
import com.gabriel.agenda.modelo.Materia;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

// Pantalla P4: lista de materias ordenada A-Z con su color y su docente (capa vista)
public class MateriasActivity extends AppCompatActivity {

    private MateriaAdapter adaptador;
    private ControladorMateria controlador;

    // Se ejecuta al abrir la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_materias);
        // Deja espacio para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        // Conecta las variables con el layout
        ListView lvMaterias = findViewById(R.id.lvMaterias);
        TextView tvVacio = findViewById(R.id.tvVacio);
        FloatingActionButton fabAgregar = findViewById(R.id.fabAgregar);

        controlador = new ControladorMateria(this);

        // El adaptador empieza con una lista vacía; se llena en onResume
        adaptador = new MateriaAdapter(this, new ArrayList<Materia>());
        lvMaterias.setAdapter(adaptador);
        // Si el adaptador no tiene filas, el ListView muestra "Aún no tienes materias"
        lvMaterias.setEmptyView(tvVacio);

        // Botón +: abre el formulario para una materia nueva.
        // Al guardar, el formulario hace finish() y se vuelve a esta lista
        fabAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MateriasActivity.this, MateriaFormActivity.class);
                startActivity(intent);
            }
        });
    }

    // Se ejecuta cada vez que la pantalla vuelve a verse (también al volver del formulario)
    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    // Pide las materias al controlador y se las pasa al adaptador
    private void cargarLista() {
        adaptador.actualizar(controlador.listar());
    }
}