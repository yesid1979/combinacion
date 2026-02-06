package com.combinacion.models;

import java.math.BigDecimal;
import java.sql.Date;

public class Contrato {
    private int id;
    private String trdProceso;
    private String numeroContrato;
    private String tipoContrato;
    private String nivel;
    private String objeto;
    private String modalidad;
    private String estado;
    private String periodo;
    private Date fechaSuscripcion;
    private Date fechaInicio;
    private Date fechaTerminacion;
    private Date fechaAprobacion;
    private Date fechaEjecucion;
    private Date fechaArl;
    private String plazoEjecucion;
    private int plazoMeses;
    private int plazoDias;

    private String valorTotalLetras;
    private BigDecimal valorTotalNumeros;
    private BigDecimal valorAntesIva;
    private BigDecimal valorIva;

    private String valorCuotaLetras;
    private BigDecimal valorCuotaNumero;
    private String numCuotasLetras;
    private int numCuotasNumero;

    private String valorMediaCuotaLetras;
    private BigDecimal valorMediaCuotaNumero;

    private String actividadesEntregables;

    private String liquidacionAcuerdo;
    private String liquidacionArticulo;
    private String liquidacionDecreto;
    private String circularHonorarios;

    // Foreign Keys
    private int contratistaId;
    private int supervisorId;
    private int ordenadorId;
    private int presupuestoId;
    private int estructuradorId;

    // Object References (Optional implementation for future use)
    private Contratista contratista;
    private Supervisor supervisor;
    private OrdenadorGasto ordenadorGasto;
    private PresupuestoDetalle presupuestoDetalle;
    private Estructurador estructurador;

    public Contrato() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTrdProceso() {
        return trdProceso;
    }

    public void setTrdProceso(String trdProceso) {
        this.trdProceso = trdProceso;
    }

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public void setNumeroContrato(String numeroContrato) {
        this.numeroContrato = numeroContrato;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getObjeto() {
        return objeto;
    }

    public void setObjeto(String objeto) {
        this.objeto = objeto;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public Date getFechaSuscripcion() {
        return fechaSuscripcion;
    }

    public void setFechaSuscripcion(Date fechaSuscripcion) {
        this.fechaSuscripcion = fechaSuscripcion;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaTerminacion() {
        return fechaTerminacion;
    }

    public void setFechaTerminacion(Date fechaTerminacion) {
        this.fechaTerminacion = fechaTerminacion;
    }

    public Date getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(Date fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    public Date getFechaEjecucion() {
        return fechaEjecucion;
    }

    public void setFechaEjecucion(Date fechaEjecucion) {
        this.fechaEjecucion = fechaEjecucion;
    }

    public Date getFechaArl() {
        return fechaArl;
    }

    public void setFechaArl(Date fechaArl) {
        this.fechaArl = fechaArl;
    }

    public String getPlazoEjecucion() {
        return plazoEjecucion;
    }

    public void setPlazoEjecucion(String plazoEjecucion) {
        this.plazoEjecucion = plazoEjecucion;
    }

    public int getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(int plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public int getPlazoDias() {
        return plazoDias;
    }

    public void setPlazoDias(int plazoDias) {
        this.plazoDias = plazoDias;
    }

    public String getValorTotalLetras() {
        return valorTotalLetras;
    }

    public void setValorTotalLetras(String valorTotalLetras) {
        this.valorTotalLetras = valorTotalLetras;
    }

    public BigDecimal getValorTotalNumeros() {
        return valorTotalNumeros;
    }

    public void setValorTotalNumeros(BigDecimal valorTotalNumeros) {
        this.valorTotalNumeros = valorTotalNumeros;
    }

    public BigDecimal getValorAntesIva() {
        return valorAntesIva;
    }

    public void setValorAntesIva(BigDecimal valorAntesIva) {
        this.valorAntesIva = valorAntesIva;
    }

    public BigDecimal getValorIva() {
        return valorIva;
    }

    public void setValorIva(BigDecimal valorIva) {
        this.valorIva = valorIva;
    }

    public String getValorCuotaLetras() {
        return valorCuotaLetras;
    }

    public void setValorCuotaLetras(String valorCuotaLetras) {
        this.valorCuotaLetras = valorCuotaLetras;
    }

    public BigDecimal getValorCuotaNumero() {
        return valorCuotaNumero;
    }

    public void setValorCuotaNumero(BigDecimal valorCuotaNumero) {
        this.valorCuotaNumero = valorCuotaNumero;
    }

    public String getNumCuotasLetras() {
        return numCuotasLetras;
    }

    public void setNumCuotasLetras(String numCuotasLetras) {
        this.numCuotasLetras = numCuotasLetras;
    }

    public int getNumCuotasNumero() {
        return numCuotasNumero;
    }

    public void setNumCuotasNumero(int numCuotasNumero) {
        this.numCuotasNumero = numCuotasNumero;
    }

    public String getValorMediaCuotaLetras() {
        return valorMediaCuotaLetras;
    }

    public void setValorMediaCuotaLetras(String valorMediaCuotaLetras) {
        this.valorMediaCuotaLetras = valorMediaCuotaLetras;
    }

    public BigDecimal getValorMediaCuotaNumero() {
        return valorMediaCuotaNumero;
    }

    public void setValorMediaCuotaNumero(BigDecimal valorMediaCuotaNumero) {
        this.valorMediaCuotaNumero = valorMediaCuotaNumero;
    }

    public String getActividadesEntregables() {
        return actividadesEntregables;
    }

    public void setActividadesEntregables(String actividadesEntregables) {
        this.actividadesEntregables = actividadesEntregables;
    }

    public String getLiquidacionAcuerdo() {
        return liquidacionAcuerdo;
    }

    public void setLiquidacionAcuerdo(String liquidacionAcuerdo) {
        this.liquidacionAcuerdo = liquidacionAcuerdo;
    }

    public String getLiquidacionArticulo() {
        return liquidacionArticulo;
    }

    public void setLiquidacionArticulo(String liquidacionArticulo) {
        this.liquidacionArticulo = liquidacionArticulo;
    }

    public String getLiquidacionDecreto() {
        return liquidacionDecreto;
    }

    public void setLiquidacionDecreto(String liquidacionDecreto) {
        this.liquidacionDecreto = liquidacionDecreto;
    }

    public String getCircularHonorarios() {
        return circularHonorarios;
    }

    public void setCircularHonorarios(String circularHonorarios) {
        this.circularHonorarios = circularHonorarios;
    }

    public int getContratistaId() {
        return contratistaId;
    }

    public void setContratistaId(int contratistaId) {
        this.contratistaId = contratistaId;
    }

    public int getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(int supervisorId) {
        this.supervisorId = supervisorId;
    }

    public int getOrdenadorId() {
        return ordenadorId;
    }

    public void setOrdenadorId(int ordenadorId) {
        this.ordenadorId = ordenadorId;
    }

    public int getPresupuestoId() {
        return presupuestoId;
    }

    public void setPresupuestoId(int presupuestoId) {
        this.presupuestoId = presupuestoId;
    }

    public int getEstructuradorId() {
        return estructuradorId;
    }

    public void setEstructuradorId(int estructuradorId) {
        this.estructuradorId = estructuradorId;
    }

    public Contratista getContratista() {
        return contratista;
    }

    public void setContratista(Contratista contratista) {
        this.contratista = contratista;
    }

    public Supervisor getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    public OrdenadorGasto getOrdenadorGasto() {
        return ordenadorGasto;
    }

    public void setOrdenadorGasto(OrdenadorGasto ordenadorGasto) {
        this.ordenadorGasto = ordenadorGasto;
    }

    public PresupuestoDetalle getPresupuestoDetalle() {
        return presupuestoDetalle;
    }

    public void setPresupuestoDetalle(PresupuestoDetalle presupuestoDetalle) {
        this.presupuestoDetalle = presupuestoDetalle;
    }

    public Estructurador getEstructurador() {
        return estructurador;
    }

    public void setEstructurador(Estructurador estructurador) {
        this.estructurador = estructurador;
    }

    // Transient field for display
    private String contratistaNombre;

    public String getContratistaNombre() {
        return contratistaNombre;
    }

    public void setContratistaNombre(String contratistaNombre) {
        this.contratistaNombre = contratistaNombre;
    }

    // Campo para Apoyo a la Supervisión
    private String apoyoSupervision;

    public String getApoyoSupervision() {
        return apoyoSupervision;
    }

    public void setApoyoSupervision(String apoyoSupervision) {
        this.apoyoSupervision = apoyoSupervision;
    }
}
