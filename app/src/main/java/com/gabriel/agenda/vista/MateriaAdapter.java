package com.gabriel.agenda.vista;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gabriel.agenda.R;
import com.gabriel.agenda.modelo.Materia;

import java.util.List;

// Adaptador: convierte cada Materia de la lista en una fila (item_materia.xml) del ListView
public class MateriaAdapter extends BaseAdapter {

    // "Aviso" que el adaptador manda a la pantalla cuando se toca el botón ✕ de una fila
    // (funciona igual que View.OnClickListener, pero entrega la Materia de esa fila)
    public interface OnEliminarListener {
        void onEliminar(Materia materia);
    }

    private final Context context;
    private List<Materia> materias;
    private final OnEliminarListener eliminarListener;

    // Constructor: recibe la pantalla, la lista a mostrar y a quién avisar al tocar ✕
    public MateriaAdapter(Context context, List<Materia> materias,
                          OnEliminarListener eliminarListener) {
        this.context = context;
        this.materias = materias;
        this.eliminarListener = eliminarListener;
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

        final Materia materia = getItem(posicion);
        View vColor = convertView.findViewById(R.id.vColor);
        TextView tvNombre = convertView.findViewById(R.id.tvNombre);
        TextView tvDocente = convertView.findViewById(R.id.tvDocente);
        ImageButton btnEliminar = convertView.findViewById(R.id.btnEliminar);

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

        // Botón ✕: avisa a la pantalla qué materia se quiere eliminar.
        // Se vuelve a asignar en cada getView porque la fila puede reutilizarse con otra materia
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarListener.onEliminar(materia);
            }
        });

        return convertView;
    }
}