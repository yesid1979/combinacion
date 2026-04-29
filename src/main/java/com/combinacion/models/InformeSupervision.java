package com.combinacion.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class InformeSupervision implements Serializable {
    private Integer id;
    private Integer contratoId;
    private String periodoInforme;
    private String tipoInforme;
    private String numeroCuota;
    
    // Informe Jurídico
    private Date fechaInicioPeriodo;
    private Date fechaFinPeriodo;
    private String modificaciones;
    private String suspensiones;
    private String reanudaciones;
    private String cesiones;
    private String terminacionAnticipada;
    
    // Informe Contable y Financiero
    private BigDecimal valorCuotaPagar;
    private BigDecimal valorAccumuladoPagado;
    private BigDecimal saldoPorCancelar;
    
    // Seguridad Social
    private String planillaNumero;
    private String planillaPin;
    private String planillaOperador;
    private Date planillaFechaPago;
    private String planillaPeriodo;
    
    // Informe Técnico
    private String observacionesTecnicas;
    private String recomendaciones;
    
    // Metadatos
    private Date fechaCreacion;
    private Date fechaSuscripcion;
    
    // Relación con Contrato (opcional para el modelo)
    private Contrato contrato;

    public InformeSupervision() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getContratoId() { return contratoId; }
    public void setContratoId(Integer contratoId) { this.contratoId = contratoId; }

    public String getPeriodoInforme() { return periodoInforme; }
    public void setPeriodoInforme(String periodoInforme) { this.periodoInforme = periodoInforme; }

    public String getTipoInforme() { return tipoInforme; }
    public void setTipoInforme(String tipoInforme) { this.tipoInforme = tipoInforme; }

    public String getNumeroCuota() { return numeroCuota; }
    public void setNumeroCuota(String numeroCuota) { this.numeroCuota = numeroCuota; }

    public Date getFechaInicioPeriodo() { return fechaInicioPeriodo; }
    public void setFechaInicioPeriodo(Date fechaInicioPeriodo) { this.fechaInicioPeriodo = fechaInicioPeriodo; }

    public Date getFechaFinPeriodo() { return fechaFinPeriodo; }
    public void setFechaFinPeriodo(Date fechaFinPeriodo) { this.fechaFinPeriodo = fechaFinPeriodo; }

    public String getModificaciones() { return modificaciones; }
    public void setModificaciones(String modificaciones) { this.modificaciones = modificaciones; }

    public String getSuspensiones() { return suspensiones; }
    public void setSuspensiones(String suspensiones) { this.suspensiones = suspensiones; }

    public String getReanudaciones() { return reanudaciones; }
    public void setReanudaciones(String reanudaciones) { this.reanudaciones = reanudaciones; }

    public String getCesiones() { return cesiones; }
    public void setCesiones(String cesiones) { this.cesiones = cesiones; }

    public String getTerminacionAnticipada() { return terminacionAnticipada; }
    public void setTerminacionAnticipada(String terminacionAnticipada) { this.terminacionAnticipada = terminacionAnticipada; }

    public BigDecimal getValorCuotaPagar() { return valorCuotaPagar; }
    public void setValorCuotaPagar(BigDecimal valorCuotaPagar) { this.valorCuotaPagar = valorCuotaPagar; }

    public BigDecimal getValorAccumuladoPagado() { return valorAccumuladoPagado; }
    public void setValorAccumuladoPagado(BigDecimal valorAccumuladoPagado) { this.valorAccumuladoPagado = valorAccumuladoPagado; }

    public BigDecimal getSaldoPorCancelar() { return saldoPorCancelar; }
    public void setSaldoPorCancelar(BigDecimal saldoPorCancelar) { this.saldoPorCancelar = saldoPorCancelar; }

    public String getPlanillaNumero() { return planillaNumero; }
    public void setPlanillaNumero(String planillaNumero) { this.planillaNumero = planillaNumero; }

    public String getPlanillaPin() { return planillaPin; }
    public void setPlanillaPin(String planillaPin) { this.planillaPin = planillaPin; }

    public String getPlanillaOperador() { return planillaOperador; }
    public void setPlanillaOperador(String planillaOperador) { this.planillaOperador = planillaOperador; }

    public Date getPlanillaFechaPago() { return planillaFechaPago; }
    public void setPlanillaFechaPago(Date planillaFechaPago) { this.planillaFechaPago = planillaFechaPago; }

    public String getPlanillaPeriodo() { return planillaPeriodo; }
    public void setPlanillaPeriodo(String planillaPeriodo) { this.planillaPeriodo = planillaPeriodo; }

    public String getObservacionesTecnicas() { return observacionesTecnicas; }
    public void setObservacionesTecnicas(String observacionesTecnicas) { this.observacionesTecnicas = observacionesTecnicas; }

    public String getRecomendaciones() { return recomendaciones; }
    public void setRecomendaciones(String recomendaciones) { this.recomendaciones = recomendaciones; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Date getFechaSuscripcion() { return fechaSuscripcion; }
    public void setFechaSuscripcion(Date fechaSuscripcion) { this.fechaSuscripcion = fechaSuscripcion; }

    public Contrato getContrato() { return contrato; }
    public void setContrato(Contrato contrato) { this.contrato = contrato; }
}
