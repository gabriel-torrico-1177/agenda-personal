package com.gabriel.agenda.modelo;

// Modelo: representa una materia que cursa el estudiante (una fila de la tabla "materia")
public class Materia {

    // Atributos privados: solo se cambian con los getters y setters
    private int id;           // lo genera SQLite (AUTOINCREMENT)
    private String nombre;    // obligatorio y no se puede repetir
    private String docente;   // opcional
    private String color;     // color en formato "#RRGGBB", por ejemplo "#3F51B5"

    // Constructor SIN id: para una materia nueva que aún no está en la base de datos
    public Materia(String nombre, String docente, String color) {
        this.nombre = nombre;
        this.docente = docente;
        this.color = color;
    }

    // Constructor CON id: para una materia que ya se leyó de la base de datos
    public Materia(int id, String nombre, String docente, String color) {
        this.id = id;
        this.nombre = nombre;
        this.docente = docente;
        this.color = color;
    }

    // Getters: devuelven el valor de cada atributo
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDocente() {
        return docente;
    }

    public String getColor() {
        return color;
    }

    // Setters: cambian el valor de cada atributo
    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDocente(String docente) {
        this.docente = docente;
    }

    public void setColor(String color) {
        this.color = color;
    }
}