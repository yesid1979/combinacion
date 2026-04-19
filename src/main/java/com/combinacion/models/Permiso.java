package com.combinacion.models;

import java.io.Serializable;

/**
 * Modelo que representa un permiso del sistema.
 */
public class Permiso implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String codigo;
    private String nombre;
    private String modulo;
    private String descripcion;

    public Permiso() {}

    public Permiso(int id, String codigo, String nombre, String modulo, String descripcion) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.modulo = modulo;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return "Permiso{" + "codigo=" + codigo + ", nombre=" + nombre + '}';
    }
}
