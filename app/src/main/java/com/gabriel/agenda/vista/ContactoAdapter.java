package com.gabriel.agenda.vista;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gabriel.agenda.R;
import com.gabriel.agenda.modelo.Contacto;

import java.util.List;

// Adaptador: convierte cada Contacto de la lista en una fila (item_contacto.xml) del ListView
public class ContactoAdapter extends BaseAdapter {

    private final Context context;
    private List<Contacto> contactos;

    // Constructor: recibe la pantalla y la lista de contactos a mostrar
    public ContactoAdapter(Context context, List<Contacto> contactos) {
        this.context = context;
        this.contactos = contactos;
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
        Contacto contacto = getItem(posicion);
        TextView tvNombre = convertView.findViewById(R.id.tvNombre);
        TextView tvTelefono = convertView.findViewById(R.id.tvTelefono);
        tvNombre.setText(contacto.getNombre());
        tvTelefono.setText(contacto.getTelefono());

        return convertView;
    }
}