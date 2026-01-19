package com.combinacion.dao;

import com.combinacion.models.Contrato;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContratoDAO {

    public boolean insertar(Contrato c) {
        String sql = "INSERT INTO contratos (" +
                "trd_proceso, numero_contrato, tipo_contrato, nivel, objeto, modalidad, estado, periodo, " +
                "fecha_suscripcion, fecha_inicio, fecha_terminacion, fecha_aprobacion, fecha_ejecucion, fecha_arl, " +
                "plazo_ejecucion, plazo_meses, plazo_dias, valor_total_letras, valor_total_numeros, " +
                "valor_antes_iva, valor_iva, valor_cuota_letras, valor_cuota_numero, num_cuotas_letras, " +
                "num_cuotas_numero, valor_media_cuota_letras, valor_media_cuota_numero, actividades_entregables, liquidacion_acuerdo, liquidacion_articulo, "
                +
                "liquidacion_decreto, circular_honorarios, contratista_id, supervisor_id, ordenador_id, " +
                "presupuesto_id, estructurador_id) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getTrdProceso());
            ps.setString(2, c.getNumeroContrato());
            ps.setString(3, c.getTipoContrato());
            ps.setString(4, c.getNivel());
            ps.setString(5, c.getObjeto());
            ps.setString(6, c.getModalidad());
            ps.setString(7, c.getEstado());
            ps.setString(8, c.getPeriodo());
            ps.setDate(9, c.getFechaSuscripcion());
            ps.setDate(10, c.getFechaInicio());
            ps.setDate(11, c.getFechaTerminacion());
            ps.setDate(12, c.getFechaAprobacion());
            ps.setDate(13, c.getFechaEjecucion());
            ps.setDate(14, c.getFechaArl());
            ps.setString(15, c.getPlazoEjecucion());
            ps.setInt(16, c.getPlazoMeses());
            ps.setInt(17, c.getPlazoDias());
            ps.setString(18, c.getValorTotalLetras());
            ps.setBigDecimal(19, c.getValorTotalNumeros());
            ps.setBigDecimal(20, c.getValorAntesIva());
            ps.setBigDecimal(21, c.getValorIva());
            ps.setString(22, c.getValorCuotaLetras());
            ps.setBigDecimal(23, c.getValorCuotaNumero());
            ps.setString(24, c.getNumCuotasLetras());
            ps.setInt(25, c.getNumCuotasNumero());
            ps.setString(26, c.getValorMediaCuotaLetras());
            ps.setBigDecimal(27, c.getValorMediaCuotaNumero());
            ps.setString(28, c.getActividadesEntregables());
            ps.setString(29, c.getLiquidacionAcuerdo());
            ps.setString(30, c.getLiquidacionArticulo());
            ps.setString(31, c.getLiquidacionDecreto());
            ps.setString(32, c.getCircularHonorarios());
            ps.setInt(33, c.getContratistaId());
            ps.setInt(34, c.getSupervisorId());
            ps.setInt(35, c.getOrdenadorId());
            ps.setInt(36, c.getPresupuestoId());
            ps.setInt(37, c.getEstructuradorId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Contrato> listarTodos() {
        List<Contrato> lista = new ArrayList<>();
        // Join with Contratistas to get the name
        String sql = "SELECT c.*, ct.nombre as contratista_nombre " +
                "FROM contratos c " +
                "LEFT JOIN contratistas ct ON c.contratista_id = ct.id " +
                "ORDER BY c.numero_contrato DESC";

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Contrato c = new Contrato();
                c.setId(rs.getInt("id"));
                c.setNumeroContrato(rs.getString("numero_contrato"));
                c.setObjeto(rs.getString("objeto"));
                c.setEstado(rs.getString("estado"));
                c.setFechaInicio(rs.getDate("fecha_inicio"));
                c.setFechaTerminacion(rs.getDate("fecha_terminacion"));
                c.setValorTotalNumeros(rs.getBigDecimal("valor_total_numeros"));

                // Set the display field
                c.setContratistaNombre(rs.getString("contratista_nombre"));

                lista.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM contratos";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countFiltered(String search) {
        String sql = "SELECT COUNT(*) FROM contratos c LEFT JOIN contratistas ct ON c.contratista_id = ct.id WHERE 1=1 ";
        if (search != null && !search.isEmpty()) {
            sql += " AND (c.numero_contrato LIKE ? OR ct.nombre LIKE ? OR c.objeto LIKE ?)";
        }
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (search != null && !search.isEmpty()) {
                String like = "%" + search + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Contrato> findWithPagination(int start, int length, String search, int orderCol, String orderDir) {
        List<Contrato> lista = new ArrayList<>();
        String sql = "SELECT c.*, ct.nombre as contratista_nombre FROM contratos c LEFT JOIN contratistas ct ON c.contratista_id = ct.id WHERE 1=1 ";

        if (search != null && !search.isEmpty()) {
            sql += " AND (c.numero_contrato LIKE ? OR ct.nombre LIKE ? OR c.objeto LIKE ?)";
        }

        // Mapping DataTables column index to DB column name for sorting
        String[] cols = { "c.numero_contrato", "ct.nombre", "c.objeto", "c.fecha_inicio", "c.fecha_terminacion",
                "c.valor_total_numeros", "c.estado" };
        String sortCol = (orderCol >= 0 && orderCol < cols.length) ? cols[orderCol] : "c.numero_contrato";

        // Validation of orderDir
        if (!"asc".equalsIgnoreCase(orderDir) && !"desc".equalsIgnoreCase(orderDir)) {
            orderDir = "desc";
        }

        sql += " ORDER BY " + sortCol + " " + orderDir;
        sql += " LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;
            if (search != null && !search.isEmpty()) {
                String like = "%" + search + "%";
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index++, like);
            }
            ps.setInt(index++, length);
            ps.setInt(index++, start);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Contrato c = new Contrato();
                    c.setId(rs.getInt("id"));
                    c.setNumeroContrato(rs.getString("numero_contrato"));
                    c.setObjeto(rs.getString("objeto"));
                    c.setEstado(rs.getString("estado"));
                    c.setFechaInicio(rs.getDate("fecha_inicio"));
                    c.setFechaTerminacion(rs.getDate("fecha_terminacion"));
                    c.setValorTotalNumeros(rs.getBigDecimal("valor_total_numeros"));
                    c.setContratistaNombre(rs.getString("contratista_nombre"));
                    lista.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
