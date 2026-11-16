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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gabriel.agenda.R;
import com.gabriel.agenda.controlador.ControladorSesion;
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
    // Le pregunta si una sesión está "En curso" (la vista no hace ese cálculo)
    private final ControladorSesion controlador;

    // Constructor: recibe la pantalla, la lista a mostrar y a quién avisar al tocar ✕
    // (null = la lista se muestra sin el botón ✕, como en la pantalla de Inicio)
    public SesionAdapter(Context context, List<SesionEstudio> sesiones,
                         OnEliminarListener eliminarListener) {
        this.context = context;
        this.sesiones = sesiones;
        this.eliminarListener = eliminarListener;
        this.controlador = new ControladorSesion(context);
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
        ProgressBar pbEnCurso = convertView.findViewById(R.id.pbEnCurso);
        ImageView ivMarca = convertView.findViewById(R.id.ivMarca);

        // Círculo con el color de la materia (viene del JOIN del SesionDAO)
        GradientDrawable circulo = new GradientDrawable();
        circulo.setShape(GradientDrawable.OVAL);
        circulo.setColor(Color.parseColor(sesion.getColorMateria()));
        vColor.setBackground(circulo);

        tvMateria.setText(sesion.getNombreMateria());
        // "19:00 – 20:30"
        tvHorario.setText(context.getString(R.string.formato_horario,
                sesion.getHoraInicio(), sesion.getHoraFin()));

        // Estado: el controlador dice si la sesión está en curso y cuánto avanzó (o -1 si no).
        // Se pintan las dos ramas siempre, porque la fila puede venir reutilizada de otra sesión
        int porcentaje = controlador.porcentajeEnCurso(sesion);
        if (porcentaje == ControladorSesion.NO_EN_CURSO) {
            // Normal: el estado guardado, borde gris y sin barra
            tvEstado.setText(sesion.getEstado());
            tvEstado.setBackgroundResource(R.drawable.fondo_estado);
            pbEnCurso.setVisibility(View.GONE);
        } else {
            // En curso: etiqueta verde y barra con el avance
            tvEstado.setText(R.string.estado_en_curso);
            tvEstado.setBackgroundResource(R.drawable.fondo_estado_en_curso);
            pbEnCurso.setProgress(porcentaje);
            pbEnCurso.setVisibility(View.VISIBLE);
        }

        // Sin listener (pantalla de Inicio): se oculta el ✕ porque ahí no se elimina, y en su lugar
        // va la marca: círculo con ✔ si la sesión está Completada, o círculo vacío si no
        if (eliminarListener == null) {
            btnEliminar.setVisibility(View.GONE);
            if (sesion.getEstado().equals(SesionEstudio.ESTADO_COMPLETADA)) {
                ivMarca.setImageResource(R.drawable.ic_completada);
            } else {
                ivMarca.setImageResource(R.drawable.ic_pendiente);
            }
            ivMarca.setContentDescription(sesion.getEstado()); // lo que lee TalkBack
            ivMarca.setVisibility(View.VISIBLE);
            return convertView;
        }
        ivMarca.setVisibility(View.GONE);
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