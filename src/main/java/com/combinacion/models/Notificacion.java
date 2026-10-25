package com.combinacion.models;

import java.sql.Timestamp;

public class Notificacion {
    private int id;
    private int idUsuario;
    private Integer idInforme;
    private String titulo;
    private String mensaje;
    private String tipo; // SUCCESS, WARNING, DANGER, INFO
    private String url;
    private boolean leida;
    private Timestamp fechaCreacion;

    public Notificacion() {
    }

    public Notificacion(int idUsuario, Integer idInforme, String titulo, String mensaje, String tipo, String url) {
        this.idUsuario = idUsuario;
        this.idInforme = idInforme;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tipo = tipo != null ? tipo : "INFO";
        this.url = url;
        this.leida = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdInforme() {
        return idInforme;
    }

    public void setIdInforme(Integer idInforme) {
        this.idInforme = idInforme;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
