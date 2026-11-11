package com.gabriel.agenda.modelo;

// Modelo: representa un contacto de la agenda (una fila de la tabla "contacto")
public class Contacto {

    // Atributos privados: solo se cambian con los getters y setters
    private int id;           // lo genera SQLite (AUTOINCREMENT)
    private String nombre;    // obligatorio
    private String telefono;  // obligatorio, 7 u 8 dígitos
    private String correo;    // opcional

    // Constructor SIN id: para un contacto nuevo que aún no está en la base de datos
    public Contacto(String nombre, String telefono, String correo) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
    }

    // Constructor CON id: para un contacto que ya se leyó de la base de datos
    public Contacto(int id, String nombre, String telefono, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
    }

    // Getters: devuelven el valor de cada atributo
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    // Setters: cambian el valor de cada atributo
    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
