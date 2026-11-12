package com.gabriel.agenda.modelo;

// Modelo: representa una sesión de estudio planificada (una fila de la tabla "sesion").
// Además trae el nombre y el color de su materia, que se leen con un JOIN
public class SesionEstudio {

    // Los 3 estados posibles (los mismos del CHECK de la tabla)
    public static final String ESTADO_PENDIENTE = "Pendiente";
    public static final String ESTADO_COMPLETADA = "Completada";
    public static final String ESTADO_REPROGRAMADA = "Reprogramada";

    // Atributos privados: solo se cambian con los getters y setters
    private int id;                // lo genera SQLite (AUTOINCREMENT)
    private int materiaId;         // id de la materia a la que pertenece
    private String nombreMateria;  // viene de la tabla materia (JOIN), no se guarda en sesion
    private String colorMateria;   // viene de la tabla materia (JOIN), no se guarda en sesion
    private String fecha;          // formato AAAA-MM-DD, por ejemplo "2026-11-13"
    private String horaInicio;     // formato HH:MM, por ejemplo "19:00"
    private String horaFin;        // formato HH:MM, por ejemplo "20:30"
    private String estado;         // Pendiente, Completada o Reprogramada
    private String notas;          // opcional

    // Constructor SIN id: para una sesión nueva que aún no está en la base de datos
    public SesionEstudio(int materiaId, String fecha, String horaInicio, String horaFin,
                         String estado, String notas) {
        this.materiaId = materiaId;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
        this.notas = notas;
    }

    // Constructor COMPLETO: para una sesión que ya se leyó de la base de datos (con su materia)
    public SesionEstudio(int id, int materiaId, String nombreMateria, String colorMateria,
                         String fecha, String horaInicio, String horaFin,
                         String estado, String notas) {
        this.id = id;
        this.materiaId = materiaId;
        this.nombreMateria = nombreMateria;
        this.colorMateria = colorMateria;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
        this.notas = notas;
    }

    // Duración de la sesión en minutos: hora de fin menos hora de inicio.
    // Ejemplo: de "19:00" a "20:30" → 1230 - 1140 = 90 minutos.
    // Las horas no se guardan en la base de datos: siempre se calculan con este método
    public int getDuracionMinutos() {
        return aMinutos(horaFin) - aMinutos(horaInicio);
    }

    // Convierte "HH:MM" en minutos desde las 00:00. Ejemplo: "19:30" → 19 * 60 + 30 = 1170
    private int aMinutos(String hora) {
        String[] partes = hora.split(":");          // "19:30" → ["19", "30"]
        int horas = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);
        return horas * 60 + minutos;
    }

    // Getters: devuelven el valor de cada atributo
    public int getId() {
        return id;
    }

    public int getMateriaId() {
        return materiaId;
    }

    public String getNombreMateria() {
        return nombreMateria;
    }

    public String getColorMateria() {
        return colorMateria;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public String getEstado() {
        return estado;
    }

    public String getNotas() {
        return notas;
    }

    // Setters: cambian el valor de cada atributo
    public void setId(int id) {
        this.id = id;
    }

    public void setMateriaId(int materiaId) {
        this.materiaId = materiaId;
    }

    public void setNombreMateria(String nombreMateria) {
        this.nombreMateria = nombreMateria;
    }

    public void setColorMateria(String colorMateria) {
        this.colorMateria = colorMateria;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}