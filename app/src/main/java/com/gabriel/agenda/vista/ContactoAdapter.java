package com.gabriel.agenda.vista;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gabriel.agenda.R;
import com.gabriel.agenda.modelo.Contacto;

import java.util.List;

// Adaptador: convierte cada Contacto de la lista en una fila (item_contacto.xml) del ListView
public class ContactoAdapter extends BaseAdapter {

    // "Aviso" que el adaptador manda a la pantalla cuando se toca el botón ✕ de una fila
    // (funciona igual que View.OnClickListener, pero entrega el Contacto de esa fila)
    public interface OnEliminarListener {
        void onEliminar(Contacto contacto);
    }

    private final Context context;
    private List<Contacto> contactos;
    private final OnEliminarListener eliminarListener;

    // Constructor: recibe la pantalla, la lista a mostrar y a quién avisar al tocar ✕
    public ContactoAdapter(Context context, List<Contacto> contactos,
                           OnEliminarListener eliminarListener) {
        this.context = context;
        this.contactos = contactos;
        this.eliminarListener = eliminarListener;
    }

    // Cambia la lista (por ejemplo, al buscar) y avisa al ListView que se redibuje
    public void actualizar(List<Contacto> nuevosContactos) {
        this.contactos = nuevosContactos;
        notifyDataSetChanged();
    }

    // Cuántas filas tiene la lista
    @Override
    public int getCount() {
        return contactos.size();
    }

    // El contacto que está en la posición indicada
    @Override
    public Contacto getItem(int posicion) {
        return contactos.get(posicion);
    }

    // El id del contacto en esa posición (el de la base de datos)
    @Override
    public long getItemId(int posicion) {
        return contactos.get(posicion).getId();
    }

    // Arma la fila que se ve en pantalla para la posición indicada
    @Override
    public View getView(int posicion, View convertView, ViewGroup parent) {
        // Si no hay una fila vieja para reutilizar, se crea una desde item_contacto.xml
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_contacto, parent, false);
        }

        // Llena la fila con los datos del contacto
        final Contacto contacto = getItem(posicion);
        TextView tvNombre = convertView.findViewById(R.id.tvNombre);
        TextView tvTelefono = convertView.findViewById(R.id.tvTelefono);
        ImageButton btnEliminar = convertView.findViewById(R.id.btnEliminar);
        tvNombre.setText(contacto.getNombre());
        tvTelefono.setText(contacto.getTelefono());

        // Botón ✕: avisa a la pantalla qué contacto se quiere eliminar.
        // Se vuelve a asignar en cada getView porque la fila puede reutilizarse con otro contacto
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarListener.onEliminar(contacto);
            }
        });

        return convertView;
    }
}