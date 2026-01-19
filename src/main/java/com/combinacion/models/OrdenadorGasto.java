package com.combinacion.models;

public class OrdenadorGasto {
    private int id;
    private String organismo;
    private String direccionOrganismo;
    private String nombreOrdenador;
    private String cedulaOrdenador;
    private String cargoOrdenador;
    private String decretoNombramiento;
    private String actaPosesion;

    // Structurers
    private String juridicoNombre;
    private String juridicoCargo;
    private String tecnicoNombre;
    private String tecnicoCargo;
    private String financieroNombre;
    private String financieroCargo;

    public OrdenadorGasto() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrganismo() {
        return organismo;
    }

    public void setOrganismo(String organismo) {
        this.organismo = organismo;
    }

    public String getDireccionOrganismo() {
        return direccionOrganismo;
    }

    public void setDireccionOrganismo(String direccionOrganismo) {
        this.direccionOrganismo = direccionOrganismo;
    }

    public String getNombreOrdenador() {
        return nombreOrdenador;
    }

    public void setNombreOrdenador(String nombreOrdenador) {
        this.nombreOrdenador = nombreOrdenador;
    }

    public String getCedulaOrdenador() {
        return cedulaOrdenador;
    }

    public void setCedulaOrdenador(String cedulaOrdenador) {
        this.cedulaOrdenador = cedulaOrdenador;
    }

    public String getCargoOrdenador() {
        return cargoOrdenador;
    }

    public void setCargoOrdenador(String cargoOrdenador) {
        this.cargoOrdenador = cargoOrdenador;
    }

    public String getDecretoNombramiento() {
        return decretoNombramiento;
    }

    public void setDecretoNombramiento(String decretoNombramiento) {
        this.decretoNombramiento = decretoNombramiento;
    }

    public String getActaPosesion() {
        return actaPosesion;
    }

    public void setActaPosesion(String actaPosesion) {
        this.actaPosesion = actaPosesion;
    }

    public String getJuridicoNombre() {
        return juridicoNombre;
    }

    public void setJuridicoNombre(String juridicoNombre) {
        this.juridicoNombre = juridicoNombre;
    }

    public String getJuridicoCargo() {
        return juridicoCargo;
    }

    public void setJuridicoCargo(String juridicoCargo) {
        this.juridicoCargo = juridicoCargo;
    }

    public String getTecnicoNombre() {
        return tecnicoNombre;
    }

    public void setTecnicoNombre(String tecnicoNombre) {
        this.tecnicoNombre = tecnicoNombre;
    }

    public String getTecnicoCargo() {
        return tecnicoCargo;
    }

    public void setTecnicoCargo(String tecnicoCargo) {
        this.tecnicoCargo = tecnicoCargo;
    }

    public String getFinancieroNombre() {
        return financieroNombre;
    }

    public void setFinancieroNombre(String financieroNombre) {
        this.financieroNombre = financieroNombre;
    }

    public String getFinancieroCargo() {
        return financieroCargo;
    }

    public void setFinancieroCargo(String financieroCargo) {
        this.financieroCargo = financieroCargo;
    }
}
