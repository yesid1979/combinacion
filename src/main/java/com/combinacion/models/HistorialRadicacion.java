package com.combinacion.models;

import java.io.Serializable;
import java.util.Date;

public class HistorialRadicacion implements Serializable {
    private Integer id;
    private Integer idInforme;
    private Integer idUsuarioCambio;
    private String estadoAnterior;
    private String estadoNuevo;
    private String observaciones;
    private Date fechaCambio;
    
    // Nombres descriptivos para la vista
    private String nombreUsuarioCambio;
    
    public HistorialRadicacion() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getIdInforme() { return idInforme; }
    public void setIdInforme(Integer idInforme) { this.idInforme = idInforme; }

    public Integer getIdUsuarioCambio() { return idUsuarioCambio; }
    public void setIdUsuarioCambio(Integer idUsuarioCambio) { this.idUsuarioCambio = idUsuarioCambio; }

    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Date getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(Date fechaCambio) { this.fechaCambio = fechaCambio; }

    public String getNombreUsuarioCambio() { return nombreUsuarioCambio; }
    public void setNombreUsuarioCambio(String nombreUsuarioCambio) { this.nombreUsuarioCambio = nombreUsuarioCambio; }
}
