package com.combinacion.services;

import com.combinacion.dao.ContratoDAO;
import com.combinacion.dao.InformeSupervisionDAO;
import com.combinacion.models.Contrato;
import com.combinacion.models.InformeSupervision;
import com.combinacion.util.ParseUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class InformeSupervisionService {

    private final InformeSupervisionDAO informeDAO = new InformeSupervisionDAO();
    private final ContratoDAO contratoDAO = new ContratoDAO();

    public List<InformeSupervision> listarPorContrato(int contratoId) {
        return informeDAO.listarPorContrato(contratoId);
    }

    public List<InformeSupervision> listarTodos() {
        return informeDAO.listarTodos();
    }

    public InformeSupervision obtenerPorId(int id) {
        InformeSupervision info = informeDAO.obtenerPorId(id);
        if (info != null && info.getContratoId() != null) {
            info.setContrato(contratoDAO.obtenerPorId(info.getContratoId()));
        }
        return info;
    }

    public String insertar(InformeFormData form) {
        try {
            InformeSupervision info = mapFormToModel(form);
            String daoResult = informeDAO.insertar(info);
            if (daoResult == null) {
                return null; // Éxito
            } else {
                return "No se pudo guardar el informe en la base de datos: " + daoResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al procesar el informe: " + e.getMessage();
        }
    }

    private InformeSupervision mapFormToModel(InformeFormData f) {
        InformeSupervision info = new InformeSupervision();
        info.setContratoId(f.contratoId);
        info.setPeriodoInforme(f.periodoInforme);
        info.setTipoInforme(f.tipoInforme);
        info.setNumeroCuota(f.numeroCuota);
        
        info.setFechaInicioPeriodo(ParseUtils.parseDate(f.fechaInicioPeriodo));
        info.setFechaFinPeriodo(ParseUtils.parseDate(f.fechaFinPeriodo));
        info.setModificaciones(f.modificaciones);
        info.setSuspensiones(f.suspensiones);
        info.setReanudaciones(f.reanudaciones);
        info.setCesiones(f.cesiones);
        info.setTerminacionAnticipada(f.terminacionAnticipada);
        
        info.setValorCuotaPagar(ParseUtils.parseBigDecimal(f.valorCuotaPagar));
        info.setValorAccumuladoPagado(ParseUtils.parseBigDecimal(f.valorAccumuladoPagado));
        info.setSaldoPorCancelar(ParseUtils.parseBigDecimal(f.saldoPorCancelar));
        
        info.setPlanillaNumero(f.planillaNumero);
        info.setPlanillaPin(f.planillaPin);
        info.setPlanillaOperador(f.planillaOperador);
        info.setPlanillaFechaPago(ParseUtils.parseDate(f.planillaFechaPago));
        info.setPlanillaPeriodo(f.planillaPeriodo);
        
        info.setObservacionesTecnicas(f.observacionesTecnicas);
        info.setRecomendaciones(f.recomendaciones);
        info.setFechaSuscripcion(ParseUtils.parseDate(f.fechaSuscripcion));
        
        return info;
    }

    public static class InformeFormData {
        public int contratoId;
        public String periodoInforme;
        public String tipoInforme;
        public String numeroCuota;
        public String fechaInicioPeriodo;
        public String fechaFinPeriodo;
        public String modificaciones;
        public String suspensiones;
        public String reanudaciones;
        public String cesiones;
        public String terminacionAnticipada;
        public String valorCuotaPagar;
        public String valorAccumuladoPagado;
        public String saldoPorCancelar;
        public String planillaNumero;
        public String planillaPin;
        public String planillaOperador;
        public String planillaFechaPago;
        public String planillaPeriodo;
        public String observacionesTecnicas;
        public String recomendaciones;
        public String fechaSuscripcion;
    }

    public Contrato obtenerContrato(int id) {
        return contratoDAO.obtenerPorId(id);
    }
}
