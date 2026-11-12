package com.gabriel.agenda.vista;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gabriel.agenda.R;
import com.gabriel.agenda.modelo.Materia;

import java.util.List;

// Adaptador: convierte cada Materia de la lista en una fila (item_materia.xml) del ListView
public class MateriaAdapter extends BaseAdapter {

    private final Context context;
    private List<Materia> materias;

    // Constructor: recibe la pantalla y la lista a mostrar
    public MateriaAdapter(Context context, List<Materia> materias) {
        this.context = context;
        this.materias = materias;
    }

    // Cambia la lista y avisa al ListView que se redibuje
    public void actualizar(List<Materia> nuevasMaterias) {
        this.materias = nuevasMaterias;
        notifyDataSetChanged();
    }

    // Cuántas filas tiene la lista
    @Override
    public int getCount() {
        return materias.size();
    }

    // La materia que está en la posición indicada
    @Override
    public Materia getItem(int posicion) {
        return materias.get(posicion);
    }

    // El id de la materia en esa posición (el de la base de datos)
    @Override
    public long getItemId(int posicion) {
        return materias.get(posicion).getId();
    }

    // Arma la fila que se ve en pantalla para la posición indicada
    @Override
    public View getView(int posicion, View convertView, ViewGroup parent) {
        // Si no hay una fila vieja para reutilizar, se crea una desde item_materia.xml
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_materia, parent, false);
        }

        Materia materia = getItem(posicion);
        View vColor = convertView.findViewById(R.id.vColor);
        TextView tvNombre = convertView.findViewById(R.id.tvNombre);
        TextView tvDocente = convertView.findViewById(R.id.tvDocente);

        // Círculo: una forma ovalada rellena con el color guardado ("#3F51B5" → color real)
        GradientDrawable circulo = new GradientDrawable();
        circulo.setShape(GradientDrawable.OVAL);
        circulo.setColor(Color.parseColor(materia.getColor()));
        vColor.setBackground(circulo);

        tvNombre.setText(materia.getNombre());

        // El docente es opcional: si no hay, se muestra un guion
        String docente = materia.getDocente();
        if (docente == null || docente.isEmpty()) {
            tvDocente.setText(R.string.sin_docente);
        } else {
            tvDocente.setText(docente);
        }

        return convertView;
    }
}