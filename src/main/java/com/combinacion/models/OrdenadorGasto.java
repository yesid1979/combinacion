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
}
