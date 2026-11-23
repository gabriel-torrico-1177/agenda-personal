package com.gabriel.agenda.vista;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.gabriel.agenda.R;
import com.gabriel.agenda.controlador.ControladorMeta;
import com.gabriel.agenda.controlador.ControladorReporte;
import com.gabriel.agenda.modelo.FilaReporte;

import java.util.List;
import java.util.Locale;

// Adaptador: convierte cada FilaReporte (una materia de la semana) en una tarjeta
// (item_reporte.xml) del ListView del reporte. Solo muestra: los números ya vienen calculados
public class ReporteAdapter extends BaseAdapter {

    private final Context context;
    private List<FilaReporte> filas;
    // El controlador decide el nivel (ALTO, MEDIO o BAJO) de cada porcentaje
    private final ControladorReporte controlador;

    // Constructor: recibe la pantalla, la lista de filas a mostrar y el controlador del reporte
    public ReporteAdapter(Context context, List<FilaReporte> filas, ControladorReporte controlador) {
        this.context = context;
        this.filas = filas;
        this.controlador = controlador;
    }

    // Cambia la lista (por ejemplo, al pasar a otra semana) y avisa al ListView que se redibuje
    public void actualizar(List<FilaReporte> nuevasFilas) {
        this.filas = nuevasFilas;
        notifyDataSetChanged();
    }

    // Cuántas tarjetas tiene la lista
    @Override
    public int getCount() {
        return filas.size();
    }

    // La fila que está en la posición indicada
    @Override
    public FilaReporte getItem(int posicion) {
        return filas.get(posicion);
    }

    // FilaReporte no tiene id en la base de datos: se usa la posición
    @Override
    public long getItemId(int posicion) {
        return posicion;
    }

    // Arma la tarjeta que se ve en pantalla para la posición indicada
    @Override
    public View getView(int posicion, View convertView, ViewGroup parent) {
        // Si no hay una tarjeta vieja para reutilizar, se crea una desde item_reporte.xml
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_reporte, parent, false);
        }

        FilaReporte fila = getItem(posicion);
        TextView vColor = convertView.findViewById(R.id.vColor);
        TextView tvNombre = convertView.findViewById(R.id.tvNombre);
        TextView tvPorcentaje = convertView.findViewById(R.id.tvPorcentaje);
        TextView tvHoras = convertView.findViewById(R.id.tvHoras);
        ProgressBar pbMateria = convertView.findViewById(R.id.pbMateria);

        // Color de la materia ("#4CAF50" → color real), para el círculo y la barra
        int color = Color.parseColor(fila.getColorMateria());

        // Círculo: una forma ovalada rellena con el color, y dentro la inicial ("Física I" → "F")
        GradientDrawable circulo = new GradientDrawable();
        circulo.setShape(GradientDrawable.OVAL);
        circulo.setColor(color);
        vColor.setBackground(circulo);
        vColor.setText(inicial(fila.getMateria()));

        tvNombre.setText(fila.getMateria());
        tvPorcentaje.setText(context.getString(R.string.formato_porcentaje, fila.getPorcentaje()));
        pintarEtiqueta(tvPorcentaje, controlador.nivelCumplimiento(fila.getPorcentaje()));

        // "3 / 3,5 h": horas hechas / planificadas (el formato de las horas lo da el controlador)
        tvHoras.setText(context.getString(R.string.formato_hechas_de_plan,
                ControladorMeta.horasComoTexto(fila.getMinutosCompletados()),
                ControladorMeta.horasComoTexto(fila.getMinutosPlanificados())));

        // Barra: el avance es el porcentaje y setProgressTintList pinta la parte llena
        // con el color de la materia (en vez del verde de barra_progreso.xml)
        pbMateria.setProgress(fila.getPorcentaje());
        pbMateria.setProgressTintList(ColorStateList.valueOf(color));

        return convertView;
    }

    // Pinta la etiqueta del % según el nivel que decidió el controlador:
    // ALTO = verde, MEDIO = ámbar, BAJO = rojo. Se pone el fondo y el color cada vez
    // porque la tarjeta puede reutilizarse (convertView) con otra materia de otro nivel
    private void pintarEtiqueta(TextView etiqueta, String nivel) {
        int fondo;
        int colorTexto;
        if (ControladorReporte.NIVEL_ALTO.equals(nivel)) {
            fondo = R.drawable.etiqueta_alta;
            colorTexto = R.color.nivel_alto;
        } else if (ControladorReporte.NIVEL_MEDIO.equals(nivel)) {
            fondo = R.drawable.etiqueta_media;
            colorTexto = R.color.nivel_medio;
        } else {
            fondo = R.drawable.etiqueta_baja;
            colorTexto = R.color.nivel_bajo;
        }
        etiqueta.setBackgroundResource(fondo);
        etiqueta.setTextColor(ContextCompat.getColor(context, colorTexto));
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
