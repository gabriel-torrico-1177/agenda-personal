package com.gabriel.agenda.modelo;

// Modelo: UNA fila del reporte semanal (una materia, o la fila de total).
// No es una tabla de la base de datos: el ControladorReporte la arma sumando sesiones
public class FilaReporte {

    // Atributos privados: solo se cambian con los getters y setters
    private String materia;             // nombre de la materia (o "Total")
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