package com.combinacion.models;

public class VerboConjugacion {
    private int id;
    private String terceraPersona;
    private String primeraPersona;
    private boolean activo;

    public VerboConjugacion() {
    }

    public VerboConjugacion(int id, String terceraPersona, String primeraPersona, boolean activo) {
        this.id = id;
        this.terceraPersona = terceraPersona;
        this.primeraPersona = primeraPersona;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTerceraPersona() { return terceraPersona; }
    public void setTerceraPersona(String terceraPersona) { this.terceraPersona = terceraPersona; }

    public String getPrimeraPersona() { return primeraPersona; }
    public void setPrimeraPersona(String primeraPersona) { this.primeraPersona = primeraPersona; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
