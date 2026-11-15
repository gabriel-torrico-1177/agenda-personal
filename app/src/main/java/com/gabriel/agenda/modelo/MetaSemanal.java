package com.gabriel.agenda.modelo;

// Modelo: representa la meta de horas de estudio de UNA semana (una fila de la tabla "meta_semanal").
// Además trae los minutos completados de esa semana, que NO se guardan: los calcula el ControladorMeta
public class MetaSemanal {

    // Atributos privados: solo se cambian con los getters y setters
    private int id;                  // lo genera SQLite (AUTOINCREMENT)
    private String semanaInicio;     // AAAA-MM-DD del lunes de la semana, por ejemplo "2026-11-09"
    private int horasObjetivo;       // entre 1 y 80 (0 = todavía no se definió la meta)
    private int minutosCompletados;  // suma de las sesiones "Completada" de la semana (calculado)

    // Constructor SIN id: para una meta nueva que aún no está en la base de datos
    public MetaSemanal(String semanaInicio, int horasObjetivo) {
        this.semanaInicio = semanaInicio;
        this.horasObjetivo = horasObjetivo;
    }

    // Constructor CON id: para una meta que ya se leyó de la base de datos
    public MetaSemanal(int id, String semanaInicio, int horasObjetivo) {
        this.id = id;
        this.semanaInicio = semanaInicio;
        this.horasObjetivo = horasObjetivo;
    }

    // Getters: devuelven el valor de cada atributo
    public int getId() {
        return id;
    }

    public String getSemanaInicio() {
        return semanaInicio;
    }

    public int getHorasObjetivo() {
        return horasObjetivo;
    }

    public int getMinutosCompletados() {
        return minutosCompletados;
    }

    // Setters: cambian el valor de cada atributo
    public void setId(int id) {
        this.id = id;
    }

    public void setSemanaInicio(String semanaInicio) {
        this.semanaInicio = semanaInicio;
    }

    public void setHorasObjetivo(int horasObjetivo) {
        this.horasObjetivo = horasObjetivo;
    }

    public void setMinutosCompletados(int minutosCompletados) {
        this.minutosCompletados = minutosCompletados;
    }
}