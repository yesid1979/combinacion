package com.combinacion.models;

import java.sql.Date;

public class Contratista {
    private int id;
    private String cedula;
    private String dv;
    private String nombre;
    private String telefono;
    private String correo;
    private String direccion;
    private Date fechaNacimiento;
    private int edad;
    private String formacionTitulo;
    private String descripcionFormacion;
    private String tarjetaProfesional;
    private String descripcionTarjeta;
    private String experiencia;
    private String descripcionExperiencia;
    private String restricciones;

    public Contratista() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getDv() {
        return dv;
    }

    public void setDv(String dv) {
        this.dv = dv;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getFormacionTitulo() {
        return formacionTitulo;
    }

    public void setFormacionTitulo(String formacionTitulo) {
        this.formacionTitulo = formacionTitulo;
    }

    public String getDescripcionFormacion() {
        return descripcionFormacion;
    }

    public void setDescripcionFormacion(String descripcionFormacion) {
        this.descripcionFormacion = descripcionFormacion;
    }

    public String getTarjetaProfesional() {
        return tarjetaProfesional;
    }

    public void setTarjetaProfesional(String tarjetaProfesional) {
        this.tarjetaProfesional = tarjetaProfesional;
    }

    public String getDescripcionTarjeta() {
        return descripcionTarjeta;
    }

    public void setDescripcionTarjeta(String descripcionTarjeta) {
        this.descripcionTarjeta = descripcionTarjeta;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }

    public String getDescripcionExperiencia() {
        return descripcionExperiencia;
    }

    public void setDescripcionExperiencia(String descripcionExperiencia) {
        this.descripcionExperiencia = descripcionExperiencia;
    }

    public String getRestricciones() {
        return restricciones;
    }

    public void setRestricciones(String restricciones) {
        this.restricciones = restricciones;
    }

    // Campo transitorio para mostrar el contrato asociado
    private String numeroContrato;

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public void setNumeroContrato(String numeroContrato) {
        this.numeroContrato = numeroContrato;
    }
}
