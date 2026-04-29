package com.combinacion.dao;

import com.combinacion.models.InformeSupervision;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InformeSupervisionDAO {

    public String insertar(InformeSupervision info) {
        crearTablaSiNoExiste();
        String sql = "INSERT INTO informes_supervision (" +
                "contrato_id, periodo_informe, tipo_informe, numero_cuota, " +
                "fecha_inicio_periodo, fecha_fin_periodo, modificaciones, suspensiones, " +
                "reanudaciones, cesiones, terminacion_anticipada, " +
                "valor_cuota_pagar, valor_acumulado_pagado, saldo_por_cancelar, " +
                "planilla_numero, planilla_pin, planilla_operador, planilla_fecha_pago, planilla_periodo, " +
                "observaciones_tecnicas, recomendaciones, fecha_suscripcion" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, info.getContratoId());
            ps.setString(2, info.getPeriodoInforme());
            ps.setString(3, info.getTipoInforme());
            ps.setString(4, info.getNumeroCuota());
            ps.setDate(5, info.getFechaInicioPeriodo() != null ? new java.sql.Date(info.getFechaInicioPeriodo().getTime()) : null);
            ps.setDate(6, info.getFechaFinPeriodo() != null ? new java.sql.Date(info.getFechaFinPeriodo().getTime()) : null);
            ps.setString(7, info.getModificaciones());
            ps.setString(8, info.getSuspensiones());
            ps.setString(9, info.getReanudaciones());
            ps.setString(10, info.getCesiones());
            ps.setString(11, info.getTerminacionAnticipada());
            ps.setBigDecimal(12, info.getValorCuotaPagar());
            ps.setBigDecimal(13, info.getValorAccumuladoPagado());
            ps.setBigDecimal(14, info.getSaldoPorCancelar());
            ps.setString(15, info.getPlanillaNumero());
            ps.setString(16, info.getPlanillaPin());
            ps.setString(17, info.getPlanillaOperador());
            ps.setDate(18, info.getPlanillaFechaPago() != null ? new java.sql.Date(info.getPlanillaFechaPago().getTime()) : null);
            ps.setString(19, info.getPlanillaPeriodo());
            ps.setString(20, info.getObservacionesTecnicas());
            ps.setString(21, info.getRecomendaciones());
            ps.setDate(22, info.getFechaSuscripcion() != null ? new java.sql.Date(info.getFechaSuscripcion().getTime()) : null);

            if (ps.executeUpdate() > 0) {
                return null;
            } else {
                return "No se insertó ninguna fila.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error SQL: " + e.getMessage();
        }
    }

    public List<InformeSupervision> listarPorContrato(int contratoId) {
        crearTablaSiNoExiste();
        List<InformeSupervision> lista = new ArrayList<>();
        String sql = "SELECT i.*, c.numero_contrato, ct.nombre as contratista_nombre " +
                "FROM informes_supervision i " +
                "JOIN contratos c ON i.contrato_id = c.id " +
                "LEFT JOIN contratistas ct ON c.contratista_id = ct.id " +
                "WHERE i.contrato_id = ? ORDER BY i.fecha_creacion DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contratoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InformeSupervision info = mapResultSetToInforme(rs);
                    lista.add(info);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<InformeSupervision> listarTodos() {
        crearTablaSiNoExiste();
        List<InformeSupervision> lista = new ArrayList<>();
        String sql = "SELECT i.*, c.numero_contrato, ct.nombre as contratista_nombre " +
                "FROM informes_supervision i " +
                "JOIN contratos c ON i.contrato_id = c.id " +
                "LEFT JOIN contratistas ct ON c.contratista_id = ct.id " +
                "ORDER BY i.fecha_creacion DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InformeSupervision info = mapResultSetToInforme(rs);
                lista.add(info);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public InformeSupervision obtenerPorId(int id) {
        crearTablaSiNoExiste();
        String sql = "SELECT * FROM informes_supervision WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInforme(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InformeSupervision mapResultSetToInforme(ResultSet rs) throws SQLException {
        InformeSupervision info = new InformeSupervision();
        info.setId(rs.getInt("id"));
        info.setContratoId(rs.getInt("contrato_id"));
        info.setPeriodoInforme(rs.getString("periodo_informe"));
        info.setTipoInforme(rs.getString("tipo_informe"));
        info.setNumeroCuota(rs.getString("numero_cuota"));
        info.setFechaInicioPeriodo(rs.getDate("fecha_inicio_periodo"));
        info.setFechaFinPeriodo(rs.getDate("fecha_fin_periodo"));
        info.setModificaciones(rs.getString("modificaciones"));
        info.setSuspensiones(rs.getString("suspensiones"));
        info.setReanudaciones(rs.getString("reanudaciones"));
        info.setCesiones(rs.getString("cesiones"));
        info.setTerminacionAnticipada(rs.getString("terminacion_anticipada"));
        info.setValorCuotaPagar(rs.getBigDecimal("valor_cuota_pagar"));
        info.setValorAccumuladoPagado(rs.getBigDecimal("valor_acumulado_pagado"));
        info.setSaldoPorCancelar(rs.getBigDecimal("saldo_por_cancelar"));
        info.setPlanillaNumero(rs.getString("planilla_numero"));
        info.setPlanillaPin(rs.getString("planilla_pin"));
        info.setPlanillaOperador(rs.getString("planilla_operador"));
        info.setPlanillaFechaPago(rs.getDate("planilla_fecha_pago"));
        info.setPlanillaPeriodo(rs.getString("planilla_periodo"));
        info.setObservacionesTecnicas(rs.getString("observaciones_tecnicas"));
        info.setRecomendaciones(rs.getString("recomendaciones"));
        info.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        info.setFechaSuscripcion(rs.getDate("fecha_suscripcion"));

        // Map contract info if available in the result set
        try {
            String numContrato = rs.getString("numero_contrato");
            if (numContrato != null) {
                com.combinacion.models.Contrato c = new com.combinacion.models.Contrato();
                c.setId(info.getContratoId());
                c.setNumeroContrato(numContrato);
                c.setContratistaNombre(rs.getString("contratista_nombre"));
                info.setContrato(c);
            }
        } catch (SQLException e) {
            // numero_contrato might not be in the result set for some queries
        }

        return info;
    }

    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS informes_supervision (" +
            "id SERIAL PRIMARY KEY, " +
            "contrato_id INT REFERENCES contratos(id), " +
            "periodo_informe VARCHAR(100), " +
            "tipo_informe VARCHAR(20), " +
            "numero_cuota VARCHAR(20), " +
            "fecha_inicio_periodo DATE, " +
            "fecha_fin_periodo DATE, " +
            "modificaciones TEXT, " +
            "suspensiones TEXT, " +
            "reanudaciones TEXT, " +
            "cesiones TEXT, " +
            "terminacion_anticipada TEXT, " +
            "valor_cuota_pagar NUMERIC(15, 2), " +
            "valor_acumulado_pagado NUMERIC(15, 2), " +
            "saldo_por_cancelar NUMERIC(15, 2), " +
            "planilla_numero VARCHAR(50), " +
            "planilla_pin VARCHAR(100), " +
            "planilla_operador VARCHAR(100), " +
            "planilla_fecha_pago DATE, " +
            "planilla_periodo VARCHAR(50), " +
            "observaciones_tecnicas TEXT, " +
            "recomendaciones TEXT, " +
            "fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "fecha_suscripcion DATE" +
        "); " +
        "CREATE INDEX IF NOT EXISTS idx_informes_contrato ON informes_supervision(contrato_id);";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
