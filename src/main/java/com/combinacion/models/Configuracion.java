package com.combinacion.models;

public class Configuracion {
    private int id;
    private String clave;
    private String valor;
    private String descripcion;

    public Configuracion() {
    }

    public Configuracion(int id, String clave, String valor, String descripcion) {
        this.id = id;
        this.clave = clave;
        this.valor = valor;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
