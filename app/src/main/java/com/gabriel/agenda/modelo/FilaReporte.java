package com.gabriel.agenda.modelo;

// Modelo: las horas de UNA materia en una semana (una fila del reporte semanal, la fila de total,
// o una barra de las estadísticas). No es una tabla de la base de datos:
// ControladorReporte y ControladorEstadisticas la arman sumando sesiones
public class FilaReporte {

    // Atributos privados: solo se cambian con los getters y setters
    private String materia;             // nombre de la materia (o "Total")
    private String colorMateria;        // color de la materia, "#3F51B5" (para las barras)
    private int minutosPlanificados;    // todas las sesiones de la semana
    private int minutosCompletados;     // solo las sesiones "Completada"
    private int porcentaje;             // completados * 100 / planificados

    // Constructor: la fila empieza con el nombre y todo en 0; el controlador va sumando
    public FilaReporte(String materia) {
        this.materia = materia;
    }

    // Getters: devuelven el valor de cada atributo
    public String getMateria() {
        return materia;
    }

    public String getColorMateria() {
        return colorMateria;
    }

    public int getMinutosPlanificados() {
        return minutosPlanificados;
    }

    public int getMinutosCompletados() {
        return minutosCompletados;
    }

    public int getPorcentaje() {
        return porcentaje;
    }

    // Setters: cambian el valor de cada atributo
    public void setMateria(String materia) {
        this.materia = materia;
    }

    public void setColorMateria(String colorMateria) {
        this.colorMateria = colorMateria;
    }

    public void setMinutosPlanificados(int minutosPlanificados) {
        this.minutosPlanificados = minutosPlanificados;
    }

    public void setMinutosCompletados(int minutosCompletados) {
        this.minutosCompletados = minutosCompletados;
    }

    public void setPorcentaje(int porcentaje) {
        this.porcentaje = porcentaje;
    }
}