package com.gabriel.agenda.vista;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
public class MateriasActivity extends PantallaBase {

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

        // Menú inferior con "Más" resaltado (Materias está dentro de "Más")
        MenuInferior.configurar(this, R.id.menuMas);

        // El adaptador empieza con una lista vacía; se llena en onResume.
        // El tercer parámetro es lo que pasa cuando el adaptador avisa que se tocó ✕
        adaptador = new MateriaAdapter(this, new ArrayList<Materia>(),
                new MateriaAdapter.OnEliminarListener() {
                    @Override
                    public void onEliminar(Materia materia) {
                        confirmarEliminar(materia);
                    }
                });
        lvMaterias.setAdapter(adaptador);
        // Si el adaptador no tiene filas, el ListView muestra "Aún no tienes materias"
        lvMaterias.setEmptyView(tvVacio);

        // Tocar una materia: abre el formulario en modo edición con su id
        lvMaterias.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int posicion, long id) {
                // id = getItemId(posicion) del adaptador = id de la materia en la base de datos
                Intent intent = new Intent(MateriasActivity.this, MateriaFormActivity.class);
                intent.putExtra(MateriaFormActivity.EXTRA_ID, (int) id);
                abrirFormulario(intent);
            }
        });

        // Botón +: abre el formulario para una materia nueva (sin id).
        // Al guardar, el formulario hace finish() y se vuelve a esta lista
        fabAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MateriasActivity.this, MateriaFormActivity.class);
                abrirFormulario(intent);
            }
        });
    }

    // Se ejecuta cada vez que la pantalla vuelve a verse (también al volver del formulario)
    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    // Muestra el diálogo "¿Eliminar {materia}? También se borrarán sus sesiones" con Sí y No
    private void confirmarEliminar(final Materia materia) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_eliminar_materia)
                .setMessage(getString(R.string.pregunta_eliminar_materia, materia.getNombre()))
                // Sí: borra la materia (y sus sesiones, por el ON DELETE CASCADE) y recarga la lista
                .setPositiveButton(R.string.btn_si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogo, int boton) {
                        if (controlador.eliminar(materia.getId())) {
                            Toast.makeText(MateriasActivity.this,
                                    ControladorMateria.MATERIA_ELIMINADA,
                                    Toast.LENGTH_SHORT).show();
                        }
                        cargarLista();
                    }
                })
                // No: solo cierra el diálogo
                .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogo, int boton) {
                        dialogo.dismiss();
                    }
                })
                .show();
    }

    // Pide las materias al controlador y se las pasa al adaptador
    private void cargarLista() {
        adaptador.actualizar(controlador.listar());
    }
}