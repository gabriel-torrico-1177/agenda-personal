package com.gabriel.agenda.vista;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.gabriel.agenda.R;
import com.gabriel.agenda.modelo.Materia;

import java.util.List;
import java.util.Locale;

// Adaptador: convierte cada Materia de la lista en una fila (item_materia.xml) del ListView
public class MateriaAdapter extends BaseAdapter {

    // "Aviso" que el adaptador manda a la pantalla cuando se toca el botón eliminar (papelera) de una fila
    // (funciona igual que View.OnClickListener, pero entrega la Materia de esa fila)
    public interface OnEliminarListener {
        void onEliminar(Materia materia);
    }

    private final Context context;
    private List<Materia> materias;
    private final OnEliminarListener eliminarListener;

    // Constructor: recibe la pantalla, la lista a mostrar y a quién avisar al tocar la papelera
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
        TextView vColor = convertView.findViewById(R.id.vColor);
        TextView tvNombre = convertView.findViewById(R.id.tvNombre);
        ImageView ivDocente = convertView.findViewById(R.id.ivDocente);
        TextView tvDocente = convertView.findViewById(R.id.tvDocente);
        ImageButton btnEliminar = convertView.findViewById(R.id.btnEliminar);

        // Círculo: una forma ovalada rellena con el color guardado ("#3F51B5" → color real)
        GradientDrawable circulo = new GradientDrawable();
        circulo.setShape(GradientDrawable.OVAL);
        circulo.setColor(Color.parseColor(materia.getColor()));
        vColor.setBackground(circulo);
        // Dentro del círculo, la inicial de la materia ("Cálculo II" → "C")
        vColor.setText(inicial(materia.getNombre()));

        tvNombre.setText(materia.getNombre());

        // El docente es opcional: si no hay, se oculta el ícono y se muestra "Sin docente"
        // en gris más claro y en cursiva. Si hay, el ícono y el texto normal.
        // Se ponen las dos formas cada vez porque la fila puede reutilizarse con otra materia.
        // Cursiva: se inclina el texto con setTextSkewX(-0.25f) porque algunas fuentes del sistema
        // no traen versión cursiva y setTypeface(ITALIC) las deja derechas
        String docente = materia.getDocente();
        if (docente == null || docente.isEmpty()) {
            ivDocente.setVisibility(View.GONE);
            tvDocente.getPaint().setTextSkewX(-0.25f);
            tvDocente.setTextColor(ContextCompat.getColor(context, R.color.texto_pista));
            tvDocente.setText(R.string.sin_docente);
        } else {
            ivDocente.setVisibility(View.VISIBLE);
            tvDocente.getPaint().setTextSkewX(0f);
            tvDocente.setTextColor(ContextCompat.getColor(context, R.color.texto_secundario));
            tvDocente.setText(docente);
        }

        // Botón eliminar (papelera): avisa a la pantalla qué materia se quiere eliminar.
        // Se vuelve a asignar en cada getView porque la fila puede reutilizarse con otra materia
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarListener.onEliminar(materia);
            }
        });

        return convertView;
    }

    // Primera letra del nombre en mayúscula para el círculo ("cálculo II" → "C"); "?" si no hay nombre.
    // Locale "es": las reglas de mayúsculas del español ("á" → "Á")
    private String inicial(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "?";
        }
        return nombre.trim().substring(0, 1).toUpperCase(Locale.forLanguageTag("es"));
    }
}