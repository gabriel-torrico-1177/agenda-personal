package com.gabriel.agenda.vista;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gabriel.agenda.R;
import com.gabriel.agenda.controlador.ControladorContacto;
import com.gabriel.agenda.modelo.Contacto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

// Pantalla P2: lista de contactos ordenada A-Z con buscador (capa vista)
public class ContactosActivity extends AppCompatActivity {

    private EditText etBuscar;
    private TextView tvVacio;
    private ContactoAdapter adaptador;
    private ControladorContacto controlador;

    // Se ejecuta al abrir la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_contactos);
        // Deja espacio para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        // Conecta las variables con el layout
        etBuscar = findViewById(R.id.etBuscar);
        tvVacio = findViewById(R.id.tvVacio);
        ListView lvContactos = findViewById(R.id.lvContactos);
        FloatingActionButton fabAgregar = findViewById(R.id.fabAgregar);

        controlador = new ControladorContacto(this);

        // Menú inferior con "Más" resaltado (Contactos está dentro de "Más")
        MenuInferior.configurar(this, R.id.menuMas);

        // El adaptador empieza con una lista vacía; se llena en onResume.
        // El tercer parámetro es lo que pasa cuando el adaptador avisa que se tocó ✕
        adaptador = new ContactoAdapter(this, new ArrayList<Contacto>(),
                new ContactoAdapter.OnEliminarListener() {
                    @Override
                    public void onEliminar(Contacto contacto) {
                        confirmarEliminar(contacto);
                    }
                });
        lvContactos.setAdapter(adaptador);
        // Si el adaptador no tiene filas, el ListView muestra tvVacio en su lugar
        lvContactos.setEmptyView(tvVacio);

        // Filtra la lista cada vez que cambia el texto del buscador
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no se usa
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarLista();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no se usa
            }
        });

        // Tocar un contacto: abre el formulario en modo edición con su id
        lvContactos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int posicion, long id) {
                // id = getItemId(posicion) del adaptador = id del contacto en la base de datos
                Intent intent = new Intent(ContactosActivity.this, ContactoFormActivity.class);
                intent.putExtra(ContactoFormActivity.EXTRA_ID, (int) id);
                startActivity(intent);
            }
        });

        // Botón +: abre el formulario para un contacto nuevo (sin id)
        fabAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactosActivity.this, ContactoFormActivity.class);
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

    // Muestra el diálogo "¿Eliminar a {nombre}?" con los botones Sí y No
    private void confirmarEliminar(final Contacto contacto) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_eliminar_contacto)
                .setMessage(getString(R.string.pregunta_eliminar_contacto, contacto.getNombre()))
                // Sí: borra el contacto y recarga la lista
                .setPositiveButton(R.string.btn_si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogo, int boton) {
                        if (controlador.eliminar(contacto.getId())) {
                            Toast.makeText(ContactosActivity.this,
                                    ControladorContacto.CONTACTO_ELIMINADO,
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

    // Pide los contactos al controlador según el buscador y los muestra
    private void cargarLista() {
        String texto = etBuscar.getText().toString();
        List<Contacto> contactos = controlador.buscar(texto);
        adaptador.actualizar(contactos);

        // Mensaje para la lista vacía: sin contactos o búsqueda sin resultados
        if (texto.trim().isEmpty()) {
            tvVacio.setText(R.string.lista_contactos_vacia);
        } else {
            tvVacio.setText(R.string.busqueda_sin_resultados);
        }
    }
}