package com.combinacion.models;

import java.io.Serializable;
import java.util.Date;

public class ConsecutivoCobro implements Serializable {
    private Integer id;
    private String cedula;
    private String contrato;
    private String numeroCuota;
    private String consecutivo;
    private Date fechaCarga;
    private Integer cargadoPor;
    private String nombre;

    public ConsecutivoCobro() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getContrato() { return contrato; }
    public void setContrato(String contrato) { this.contrato = contrato; }

    public String getNumeroCuota() { return numeroCuota; }
    public void setNumeroCuota(String numeroCuota) { this.numeroCuota = numeroCuota; }

    public String getConsecutivo() { return consecutivo; }
    public void setConsecutivo(String consecutivo) { this.consecutivo = consecutivo; }

    public Date getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(Date fechaCarga) { this.fechaCarga = fechaCarga; }

    public Integer getCargadoPor() { return cargadoPor; }
    public void setCargadoPor(Integer cargadoPor) { this.cargadoPor = cargadoPor; }
}
