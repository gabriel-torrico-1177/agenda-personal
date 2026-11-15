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
import com.gabriel.agenda.modelo.SesionEstudio;

import java.util.List;

// Adaptador: convierte cada SesionEstudio de la lista en una fila (item_sesion.xml) del ListView
public class SesionAdapter extends BaseAdapter {

    // "Aviso" que el adaptador manda a la pantalla cuando se toca el botón ✕ de una fila
    // (funciona igual que View.OnClickListener, pero entrega la SesionEstudio de esa fila)
    public interface OnEliminarListener {
        void onEliminar(SesionEstudio sesion);
    }

    private final Context context;
    private List<SesionEstudio> sesiones;
    private final OnEliminarListener eliminarListener;

    // Constructor: recibe la pantalla, la lista a mostrar y a quién avisar al tocar ✕
    // (null = la lista se muestra sin el botón ✕, como en la pantalla de Inicio)
    public SesionAdapter(Context context, List<SesionEstudio> sesiones,
                         OnEliminarListener eliminarListener) {
        this.context = context;
        this.sesiones = sesiones;
        this.eliminarListener = eliminarListener;
    }

    // Cambia la lista (por ejemplo, al elegir otro día) y avisa al ListView que se redibuje
    public void actualizar(List<SesionEstudio> nuevasSesiones) {
        this.sesiones = nuevasSesiones;
        notifyDataSetChanged();
    }

    // Cuántas filas tiene la lista
    @Override
    public int getCount() {
        return sesiones.size();
    }

    // La sesión que está en la posición indicada
    @Override
    public SesionEstudio getItem(int posicion) {
        return sesiones.get(posicion);
    }

    // El id de la sesión en esa posición (el de la base de datos)
    @Override
    public long getItemId(int posicion) {
        return sesiones.get(posicion).getId();
    }

    // Arma la fila que se ve en pantalla para la posición indicada
    @Override
    public View getView(int posicion, View convertView, ViewGroup parent) {
        // Si no hay una fila vieja para reutilizar, se crea una desde item_sesion.xml
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_sesion, parent, false);
        }

        final SesionEstudio sesion = getItem(posicion);
        View vColor = convertView.findViewById(R.id.vColor);
        TextView tvMateria = convertView.findViewById(R.id.tvMateria);
        TextView tvHorario = convertView.findViewById(R.id.tvHorario);
        TextView tvEstado = convertView.findViewById(R.id.tvEstado);
        ImageButton btnEliminar = convertView.findViewById(R.id.btnEliminar);

        // Círculo con el color de la materia (viene del JOIN del SesionDAO)
        GradientDrawable circulo = new GradientDrawable();
        circulo.setShape(GradientDrawable.OVAL);
        circulo.setColor(Color.parseColor(sesion.getColorMateria()));
        vColor.setBackground(circulo);

        tvMateria.setText(sesion.getNombreMateria());
        // "19:00 – 20:30"
        tvHorario.setText(context.getString(R.string.formato_horario,
                sesion.getHoraInicio(), sesion.getHoraFin()));
        tvEstado.setText(sesion.getEstado());

        // Sin listener (pantalla de Inicio): se oculta el ✕ porque ahí no se elimina
        if (eliminarListener == null) {
            btnEliminar.setVisibility(View.GONE);
            return convertView;
        }
        btnEliminar.setVisibility(View.VISIBLE);

        // Botón ✕: avisa a la pantalla qué sesión se quiere eliminar.
        // Se vuelve a asignar en cada getView porque la fila puede reutilizarse con otra sesión
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarListener.onEliminar(sesion);
            }
        });

        return convertView;
    }
}