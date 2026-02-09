package com.combinacion.models;

import java.math.BigDecimal;
import java.sql.Date;

public class PresupuestoDetalle {
    private int id;
    private String cdpNumero;
    private Date cdpFecha;
    private BigDecimal cdpValor;
    private Date cdpVencimiento;
    private String rpNumero;
    private Date rpFecha;
    private String apropiacionPresupuestal;
    private String idPaa;
    private String codigoDane;
    private String inversion;
    private String funcionamiento;
    private String fichaEbiNombre;
    private String fichaEbiObjetivo;
    private String fichaEbiActividades;
    private String certificadoInsuficiencia;
    private Date fechaInsuficiencia;
    private String bpin;
    private String compromiso;

    public PresupuestoDetalle() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCdpNumero() {
        return cdpNumero;
    }

    public void setCdpNumero(String cdpNumero) {
        this.cdpNumero = cdpNumero;
    }

    public Date getCdpFecha() {
        return cdpFecha;
    }

    public void setCdpFecha(Date cdpFecha) {
        this.cdpFecha = cdpFecha;
    }

    public BigDecimal getCdpValor() {
        return cdpValor;
    }

    public void setCdpValor(BigDecimal cdpValor) {
        this.cdpValor = cdpValor;
    }

    public Date getCdpVencimiento() {
        return cdpVencimiento;
    }

    public void setCdpVencimiento(Date cdpVencimiento) {
        this.cdpVencimiento = cdpVencimiento;
    }

    public String getRpNumero() {
        return rpNumero;
    }

    public void setRpNumero(String rpNumero) {
        this.rpNumero = rpNumero;
    }

    public Date getRpFecha() {
        return rpFecha;
    }

    public void setRpFecha(Date rpFecha) {
        this.rpFecha = rpFecha;
    }

    public String getApropiacionPresupuestal() {
        return apropiacionPresupuestal;
    }

    public void setApropiacionPresupuestal(String apropiacionPresupuestal) {
        this.apropiacionPresupuestal = apropiacionPresupuestal;
    }

    public String getIdPaa() {
        return idPaa;
    }

    public void setIdPaa(String idPaa) {
        this.idPaa = idPaa;
    }

    public String getCodigoDane() {
        return codigoDane;
    }

    public void setCodigoDane(String codigoDane) {
        this.codigoDane = codigoDane;
    }

    public String getInversion() {
        return inversion;
    }

    public void setInversion(String inversion) {
        this.inversion = inversion;
    }

    public String getFuncionamiento() {
        return funcionamiento;
    }

    public void setFuncionamiento(String funcionamiento) {
        this.funcionamiento = funcionamiento;
    }

    public String getFichaEbiNombre() {
        return fichaEbiNombre;
    }

    public void setFichaEbiNombre(String fichaEbiNombre) {
        this.fichaEbiNombre = fichaEbiNombre;
    }

    public String getFichaEbiObjetivo() {
        return fichaEbiObjetivo;
    }

    public void setFichaEbiObjetivo(String fichaEbiObjetivo) {
        this.fichaEbiObjetivo = fichaEbiObjetivo;
    }

    public String getFichaEbiActividades() {
        return fichaEbiActividades;
    }

    public void setFichaEbiActividades(String fichaEbiActividades) {
        this.fichaEbiActividades = fichaEbiActividades;
    }

    public String getCertificadoInsuficiencia() {
        return certificadoInsuficiencia;
    }

    public void setCertificadoInsuficiencia(String certificadoInsuficiencia) {
        this.certificadoInsuficiencia = certificadoInsuficiencia;
    }

    public Date getFechaInsuficiencia() {
        return fechaInsuficiencia;
    }

    public void setFechaInsuficiencia(Date fechaInsuficiencia) {
        this.fechaInsuficiencia = fechaInsuficiencia;
    }

    public String getBpin() {
        return bpin;
    }

    public void setBpin(String bpin) {
        this.bpin = bpin;
    }

    public String getCompromiso() {
        return compromiso;
    }

    public void setCompromiso(String compromiso) {
        this.compromiso = compromiso;
    }
}
