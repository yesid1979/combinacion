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
    private final com.combinacion.dao.ContratistaDAO contratistaDAO = new com.combinacion.dao.ContratistaDAO();
    private final com.combinacion.dao.SupervisorDAO supervisorDAO = new com.combinacion.dao.SupervisorDAO();
    private final com.combinacion.dao.OrdenadorGastoDAO ordenadorGastoDAO = new com.combinacion.dao.OrdenadorGastoDAO();

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

    public String actualizar(int id, InformeFormData form) {
        try {
            InformeSupervision info = mapFormToModel(form);
            info.setId(id);
            String daoResult = informeDAO.actualizar(info);
            if (daoResult == null) {
                return null; // Éxito
            } else {
                return "No se pudo actualizar el informe en la base de datos: " + daoResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al procesar la actualización del informe: " + e.getMessage();
        }
    }

    private InformeSupervision mapFormToModel(InformeFormData f) {
        InformeSupervision info = new InformeSupervision();
        info.setContratoId(f.contratoId);
        info.setPeriodoInforme(f.periodoInforme);
        info.setTipoInforme(f.tipoInforme);
        info.setNumeroCuota(f.numeroCuota);
        info.setConsecutivoCobro(f.consecutivoCobro);
        
        info.setFechaInicioPeriodo(ParseUtils.parseDate(f.fechaInicioPeriodo));
        info.setFechaFinPeriodo(ParseUtils.parseDate(f.fechaFinPeriodo));
        info.setModificaciones(f.modificaciones);
        info.setSuspensiones(f.suspensiones);
        info.setReanudaciones(f.reanudaciones);
        info.setCesiones(f.cesiones);
        info.setTerminacionAnticipada(f.terminacionAnticipada);
        info.setAdiciones(f.adiciones);
        info.setProrrogas(f.prorrogas);
        info.setReciboSatisfaccion(f.reciboSatisfaccion);
        info.setConstanciaPazSalvo(f.constanciaPazSalvo);
        
        info.setValorCuotaPagar(ParseUtils.parseBigDecimal(f.valorCuotaPagar));
        info.setValorAccumuladoPagado(ParseUtils.parseBigDecimal(f.valorAccumuladoPagado));
        info.setSaldoPorCancelar(ParseUtils.parseBigDecimal(f.saldoPorCancelar));
        
        info.setPlanillaNumero(f.planillaNumero);
        info.setPlanillaPin(f.planillaPin);
        info.setPlanillaOperador(f.planillaOperador);
        info.setPlanillaFechaPago(ParseUtils.parseDate(f.planillaFechaPago));
        info.setPlanillaPeriodo(f.planillaPeriodo);
        
        info.setConceptoSupervisor(f.conceptoSupervisor);
        info.setObservacionesTecnicas(f.observacionesTecnicas);
        info.setRecomendaciones(f.recomendaciones);
        info.setFechaSuscripcion(ParseUtils.parseDate(f.fechaSuscripcion));
        info.setUrlDriveEvidencias(f.urlDriveEvidencias);
        
        return info;
    }

    public static class InformeFormData {
        public int contratoId;
        public String periodoInforme;
        public String tipoInforme;
        public String numeroCuota;
        public String consecutivoCobro;
        public String fechaInicioPeriodo;
        public String fechaFinPeriodo;
        public String modificaciones;
        public String suspensiones;
        public String reanudaciones;
        public String cesiones;
        public String terminacionAnticipada;
        public String adiciones;
        public String prorrogas;
        public String reciboSatisfaccion;
        public String constanciaPazSalvo;
        public String valorCuotaPagar;
        public String valorAccumuladoPagado;
        public String saldoPorCancelar;
        public String planillaNumero;
        public String planillaPin;
        public String planillaOperador;
        public String planillaFechaPago;
        public String planillaPeriodo;
        public String conceptoSupervisor;
        public String observacionesTecnicas;
        public String recomendaciones;
        public String fechaSuscripcion;
        public String urlDriveEvidencias;
    }

    public Contrato obtenerContrato(int id) {
        Contrato c = contratoDAO.obtenerPorId(id);
        if (c != null) {
            if (c.getContratistaId() > 0) {
                c.setContratista(contratistaDAO.obtenerPorId(c.getContratistaId()));
                if (c.getContratista() != null) {
                    c.setContratistaNombre(c.getContratista().getNombre());
                }
            }
            if (c.getSupervisorId() > 0) {
                c.setSupervisor(supervisorDAO.obtenerPorId(c.getSupervisorId()));
            }
            if (c.getOrdenadorId() > 0) {
                c.setOrdenadorGasto(ordenadorGastoDAO.obtenerPorId(c.getOrdenadorId()));
            }
            if (c.getPresupuestoId() > 0) {
                com.combinacion.dao.PresupuestoDetalleDAO presupuestoDAO = new com.combinacion.dao.PresupuestoDetalleDAO();
                c.setPresupuestoDetalle(presupuestoDAO.obtenerPorId(c.getPresupuestoId()));
            }
        }
        return c;
    }
}
