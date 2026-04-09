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
                "valor_antes_iva, valor_iva, valor_antes_iva_letras, valor_iva_letras, valor_cuota_letras, valor_cuota_numero, valor_cuota_antes_iva_letras, valor_cuota_antes_iva, valor_cuota_iva_letras, valor_cuota_iva, num_cuotas_letras, " +
                "num_cuotas_numero, valor_media_cuota_letras, valor_media_cuota_numero, actividades_entregables, liquidacion_acuerdo, liquidacion_articulo, "
                +
                "liquidacion_decreto, circular_honorarios, contratista_id, supervisor_id, ordenador_id, " +
                "presupuesto_id, estructurador_id, apoyo_supervision, fecha_idoneidad, fecha_estructurador, adicion_si_no, numero_cuotas_adicion, valor_total_adicion_letras, valor_total_adicion, valor_contrato_mas_adicion_letras, valor_contrato_mas_adicion, enlace_secop) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            ps.setString(22, c.getValorAntesIvaLetras());
            ps.setString(23, c.getValorIvaLetras());
            ps.setString(24, c.getValorCuotaLetras());
            ps.setBigDecimal(25, c.getValorCuotaNumero());
            ps.setString(26, c.getValorCuotaAntesIvaLetras());
            ps.setBigDecimal(27, c.getValorCuotaAntesIva());
            ps.setString(28, c.getValorCuotaIvaLetras());
            ps.setBigDecimal(29, c.getValorCuotaIva());
            ps.setString(30, c.getNumCuotasLetras());
            ps.setInt(31, c.getNumCuotasNumero());
            ps.setString(32, c.getValorMediaCuotaLetras());
            ps.setBigDecimal(33, c.getValorMediaCuotaNumero());
            ps.setString(34, c.getActividadesEntregables());
            ps.setString(35, c.getLiquidacionAcuerdo());
            ps.setString(36, c.getLiquidacionArticulo());
            ps.setString(37, c.getLiquidacionDecreto());
            ps.setString(38, c.getCircularHonorarios());
            if (c.getContratistaId() > 0)
                ps.setInt(39, c.getContratistaId());
            else
                ps.setNull(39, java.sql.Types.INTEGER);
            if (c.getSupervisorId() > 0)
                ps.setInt(40, c.getSupervisorId());
            else
                ps.setNull(40, java.sql.Types.INTEGER);
            if (c.getOrdenadorId() > 0)
                ps.setInt(41, c.getOrdenadorId());
            else
                ps.setNull(41, java.sql.Types.INTEGER);
            if (c.getPresupuestoId() > 0)
                ps.setInt(42, c.getPresupuestoId());
            else
                ps.setNull(42, java.sql.Types.INTEGER);
            if (c.getEstructuradorId() > 0)
                ps.setInt(43, c.getEstructuradorId());
            else
                ps.setNull(43, java.sql.Types.INTEGER);
            ps.setString(44, c.getApoyoSupervision());
            ps.setDate(45, c.getFechaIdoneidad());
            ps.setDate(46, c.getFechaEstructurador());
            ps.setString(47, c.getAdicionSiNo());
            ps.setInt(48, c.getNumeroCuotasAdicion());
            ps.setString(49, c.getValorTotalAdicionLetras());
            ps.setBigDecimal(50, c.getValorTotalAdicion());
            ps.setString(51, c.getValorContratoMasAdicionLetras());
            ps.setBigDecimal(52, c.getValorContratoMasAdicion());
            ps.setString(53, c.getEnlaceSecop());

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
                c.setApoyoSupervision(rs.getString("apoyo_supervision"));

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
        String searchDigits = (search != null) ? search.replaceAll("[^0-9]", "") : "";
        
        if (search != null && !search.isEmpty()) {
            sql += " AND (c.numero_contrato LIKE ? OR ct.nombre LIKE ? OR c.objeto LIKE ? OR ct.cedula LIKE ?";
            if (!searchDigits.isEmpty()) {
                sql += " OR regexp_replace(ct.cedula, '[^0-9]', '', 'g') LIKE ?";
            }
            sql += ")";
        }
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (search != null && !search.isEmpty()) {
                String like = "%" + search + "%";
                String likeDigits = "%" + searchDigits + "%";
                int idx = 1;
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                if (!searchDigits.isEmpty()) {
                    ps.setString(idx++, likeDigits);
                }
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
        String searchDigits = (search != null) ? search.replaceAll("[^0-9]", "") : "";

        if (search != null && !search.isEmpty()) {
            sql += " AND (c.numero_contrato LIKE ? OR ct.nombre LIKE ? OR c.objeto LIKE ? OR ct.cedula LIKE ?";
            if (!searchDigits.isEmpty()) {
                sql += " OR regexp_replace(ct.cedula, '[^0-9]', '', 'g') LIKE ?";
            }
            sql += ")";
        }

        // Mapping DataTables column index to DB column name for sorting
        String[] cols = { "c.numero_contrato", "ct.nombre", "c.objeto", "c.valor_total_numeros", "c.fecha_inicio",
                "c.fecha_terminacion", "c.estado" };
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
                String likeDigits = "%" + searchDigits + "%";
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index++, like);
                if (!searchDigits.isEmpty()) {
                    ps.setString(index++, likeDigits);
                }
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
                    c.setApoyoSupervision(rs.getString("apoyo_supervision"));
                    lista.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Contrato obtenerPorNumero(String numero) {
        if (numero == null) return null;
        String sql = "SELECT * FROM contratos WHERE TRIM(numero_contrato) ILIKE TRIM(?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numero.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Contrato c = new Contrato();
                    c.setId(rs.getInt("id"));
                    c.setTrdProceso(rs.getString("trd_proceso"));
                    c.setNumeroContrato(rs.getString("numero_contrato"));
                    c.setTipoContrato(rs.getString("tipo_contrato"));
                    c.setNivel(rs.getString("nivel"));
                    c.setObjeto(rs.getString("objeto"));
                    c.setModalidad(rs.getString("modalidad"));
                    c.setEstado(rs.getString("estado"));
                    c.setPeriodo(rs.getString("periodo"));
                    c.setFechaSuscripcion(rs.getDate("fecha_suscripcion"));
                    c.setFechaInicio(rs.getDate("fecha_inicio"));
                    c.setFechaTerminacion(rs.getDate("fecha_terminacion"));
                    c.setFechaAprobacion(rs.getDate("fecha_aprobacion"));
                    c.setFechaEjecucion(rs.getDate("fecha_ejecucion"));
                    c.setFechaArl(rs.getDate("fecha_arl"));
                    c.setPlazoEjecucion(rs.getString("plazo_ejecucion"));
                    c.setPlazoMeses(rs.getInt("plazo_meses"));
                    c.setPlazoDias(rs.getInt("plazo_dias"));
                    c.setValorTotalLetras(rs.getString("valor_total_letras"));
                    c.setValorTotalNumeros(rs.getBigDecimal("valor_total_numeros"));
                    c.setValorAntesIva(rs.getBigDecimal("valor_antes_iva"));
                    c.setValorIva(rs.getBigDecimal("valor_iva"));
                    c.setValorAntesIvaLetras(rs.getString("valor_antes_iva_letras"));
                    c.setValorIvaLetras(rs.getString("valor_iva_letras"));
                    c.setValorCuotaLetras(rs.getString("valor_cuota_letras"));
                    c.setValorCuotaNumero(rs.getBigDecimal("valor_cuota_numero"));
                    c.setValorCuotaAntesIvaLetras(rs.getString("valor_cuota_antes_iva_letras"));
                    c.setValorCuotaAntesIva(rs.getBigDecimal("valor_cuota_antes_iva"));
                    c.setValorCuotaIvaLetras(rs.getString("valor_cuota_iva_letras"));
                    c.setValorCuotaIva(rs.getBigDecimal("valor_cuota_iva"));
                    c.setNumCuotasLetras(rs.getString("num_cuotas_letras"));
                    c.setNumCuotasNumero(rs.getInt("num_cuotas_numero"));
                    c.setValorMediaCuotaLetras(rs.getString("valor_media_cuota_letras"));
                    c.setValorMediaCuotaNumero(rs.getBigDecimal("valor_media_cuota_numero"));
                    c.setActividadesEntregables(rs.getString("actividades_entregables"));
                    c.setLiquidacionAcuerdo(rs.getString("liquidacion_acuerdo"));
                    c.setLiquidacionArticulo(rs.getString("liquidacion_articulo"));
                    c.setLiquidacionDecreto(rs.getString("liquidacion_decreto"));
                    c.setCircularHonorarios(rs.getString("circular_honorarios"));
                    c.setContratistaId(rs.getInt("contratista_id"));
                    c.setSupervisorId(rs.getInt("supervisor_id"));
                    c.setOrdenadorId(rs.getInt("ordenador_id"));
                    c.setPresupuestoId(rs.getInt("presupuesto_id"));
                    c.setEstructuradorId(rs.getInt("estructurador_id"));
                    c.setApoyoSupervision(rs.getString("apoyo_supervision"));
                    c.setFechaIdoneidad(rs.getDate("fecha_idoneidad"));
                    c.setFechaEstructurador(rs.getDate("fecha_estructurador"));
                    c.setAdicionSiNo(rs.getString("adicion_si_no"));
                    c.setNumeroCuotasAdicion(rs.getInt("numero_cuotas_adicion"));
                    c.setValorTotalAdicionLetras(rs.getString("valor_total_adicion_letras"));
                    c.setValorTotalAdicion(rs.getBigDecimal("valor_total_adicion"));
                    c.setValorContratoMasAdicionLetras(rs.getString("valor_contrato_mas_adicion_letras"));
                    c.setValorContratoMasAdicion(rs.getBigDecimal("valor_contrato_mas_adicion"));
                    c.setEnlaceSecop(rs.getString("enlace_secop"));
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean actualizar(Contrato c) {
        String sql = "UPDATE contratos SET " +
                "trd_proceso=?, tipo_contrato=?, nivel=?, objeto=?, modalidad=?, estado=?, periodo=?, " +
                "fecha_suscripcion=?, fecha_inicio=?, fecha_terminacion=?, fecha_aprobacion=?, fecha_ejecucion=?, fecha_arl=?, "
                +
                "plazo_ejecucion=?, plazo_meses=?, plazo_dias=?, valor_total_letras=?, valor_total_numeros=?, " +
                "valor_antes_iva=?, valor_iva=?, valor_antes_iva_letras=?, valor_iva_letras=?, valor_cuota_letras=?, valor_cuota_numero=?, valor_cuota_antes_iva_letras=?, valor_cuota_antes_iva=?, valor_cuota_iva_letras=?, valor_cuota_iva=?, num_cuotas_letras=?, " +
                "num_cuotas_numero=?, valor_media_cuota_letras=?, valor_media_cuota_numero=?, actividades_entregables=?, "
                +
                "liquidacion_acuerdo=?, liquidacion_articulo=?, liquidacion_decreto=?, circular_honorarios=?, " +
                "contratista_id=?, supervisor_id=?, ordenador_id=?, presupuesto_id=?, estructurador_id=?, apoyo_supervision=?, " +
                "fecha_idoneidad=?, fecha_estructurador=?, adicion_si_no=?, numero_cuotas_adicion=?, valor_total_adicion_letras=?, " +
                "valor_total_adicion=?, valor_contrato_mas_adicion_letras=?, valor_contrato_mas_adicion=?, enlace_secop=? " +
                "WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getTrdProceso());
            ps.setString(2, c.getTipoContrato());
            ps.setString(3, c.getNivel());
            ps.setString(4, c.getObjeto());
            ps.setString(5, c.getModalidad());
            ps.setString(6, c.getEstado());
            ps.setString(7, c.getPeriodo());
            ps.setDate(8, c.getFechaSuscripcion());
            ps.setDate(9, c.getFechaInicio());
            ps.setDate(10, c.getFechaTerminacion());
            ps.setDate(11, c.getFechaAprobacion());
            ps.setDate(12, c.getFechaEjecucion());
            ps.setDate(13, c.getFechaArl());
            ps.setString(14, c.getPlazoEjecucion());
            ps.setInt(15, c.getPlazoMeses());
            ps.setInt(16, c.getPlazoDias());
            ps.setString(17, c.getValorTotalLetras());
            ps.setBigDecimal(18, c.getValorTotalNumeros());
            ps.setBigDecimal(19, c.getValorAntesIva());
            ps.setBigDecimal(20, c.getValorIva());
            ps.setString(21, c.getValorAntesIvaLetras());
            ps.setString(22, c.getValorIvaLetras());
            ps.setString(23, c.getValorCuotaLetras());
            ps.setBigDecimal(24, c.getValorCuotaNumero());
            ps.setString(25, c.getValorCuotaAntesIvaLetras());
            ps.setBigDecimal(26, c.getValorCuotaAntesIva());
            ps.setString(27, c.getValorCuotaIvaLetras());
            ps.setBigDecimal(28, c.getValorCuotaIva());
            ps.setString(29, c.getNumCuotasLetras());
            ps.setInt(30, c.getNumCuotasNumero());
            ps.setString(31, c.getValorMediaCuotaLetras());
            ps.setBigDecimal(32, c.getValorMediaCuotaNumero());
            ps.setString(33, c.getActividadesEntregables());
            ps.setString(34, c.getLiquidacionAcuerdo());
            ps.setString(35, c.getLiquidacionArticulo());
            ps.setString(36, c.getLiquidacionDecreto());
            ps.setString(37, c.getCircularHonorarios());
            if (c.getContratistaId() > 0)
                ps.setInt(38, c.getContratistaId());
            else
                ps.setNull(38, java.sql.Types.INTEGER);
            if (c.getSupervisorId() > 0)
                ps.setInt(39, c.getSupervisorId());
            else
                ps.setNull(39, java.sql.Types.INTEGER);
            if (c.getOrdenadorId() > 0)
                ps.setInt(40, c.getOrdenadorId());
            else
                ps.setNull(40, java.sql.Types.INTEGER);
            if (c.getPresupuestoId() > 0)
                ps.setInt(41, c.getPresupuestoId());
            else
                ps.setNull(41, java.sql.Types.INTEGER);
            if (c.getEstructuradorId() > 0)
                ps.setInt(42, c.getEstructuradorId());
            else
                ps.setNull(42, java.sql.Types.INTEGER);
            ps.setString(43, c.getApoyoSupervision());
            ps.setDate(44, c.getFechaIdoneidad());
            ps.setDate(45, c.getFechaEstructurador());
            ps.setString(46, c.getAdicionSiNo());
            ps.setInt(47, c.getNumeroCuotasAdicion());
            ps.setString(48, c.getValorTotalAdicionLetras());
            ps.setBigDecimal(49, c.getValorTotalAdicion());
            ps.setString(50, c.getValorContratoMasAdicionLetras());
            ps.setBigDecimal(51, c.getValorContratoMasAdicion());
            ps.setString(52, c.getEnlaceSecop());
            ps.setInt(53, c.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Contrato obtenerPorContratistaId(int contratistaId) {
        String sql = "SELECT * FROM contratos WHERE contratista_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contratistaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Contrato c = new Contrato();
                    c.setId(rs.getInt("id"));
                    c.setTrdProceso(rs.getString("trd_proceso"));
                    c.setNumeroContrato(rs.getString("numero_contrato"));
                    c.setTipoContrato(rs.getString("tipo_contrato"));
                    c.setNivel(rs.getString("nivel"));
                    c.setObjeto(rs.getString("objeto"));
                    c.setModalidad(rs.getString("modalidad"));
                    c.setEstado(rs.getString("estado"));
                    c.setPeriodo(rs.getString("periodo"));
                    c.setFechaSuscripcion(rs.getDate("fecha_suscripcion"));
                    c.setFechaInicio(rs.getDate("fecha_inicio"));
                    c.setFechaTerminacion(rs.getDate("fecha_terminacion"));
                    c.setFechaAprobacion(rs.getDate("fecha_aprobacion"));
                    c.setFechaEjecucion(rs.getDate("fecha_ejecucion"));
                    c.setFechaArl(rs.getDate("fecha_arl"));
                    c.setPlazoEjecucion(rs.getString("plazo_ejecucion"));
                    c.setPlazoMeses(rs.getInt("plazo_meses"));
                    c.setPlazoDias(rs.getInt("plazo_dias"));
                    c.setValorTotalLetras(rs.getString("valor_total_letras"));
                    c.setValorTotalNumeros(rs.getBigDecimal("valor_total_numeros"));
                    c.setValorAntesIva(rs.getBigDecimal("valor_antes_iva"));
                    c.setValorIva(rs.getBigDecimal("valor_iva"));
                    c.setValorAntesIvaLetras(rs.getString("valor_antes_iva_letras"));
                    c.setValorIvaLetras(rs.getString("valor_iva_letras"));
                    c.setValorCuotaLetras(rs.getString("valor_cuota_letras"));
                    c.setValorCuotaNumero(rs.getBigDecimal("valor_cuota_numero"));
                    c.setValorCuotaAntesIvaLetras(rs.getString("valor_cuota_antes_iva_letras"));
                    c.setValorCuotaAntesIva(rs.getBigDecimal("valor_cuota_antes_iva"));
                    c.setValorCuotaIvaLetras(rs.getString("valor_cuota_iva_letras"));
                    c.setValorCuotaIva(rs.getBigDecimal("valor_cuota_iva"));
                    c.setNumCuotasLetras(rs.getString("num_cuotas_letras"));
                    c.setNumCuotasNumero(rs.getInt("num_cuotas_numero"));
                    c.setValorMediaCuotaLetras(rs.getString("valor_media_cuota_letras"));
                    c.setValorMediaCuotaNumero(rs.getBigDecimal("valor_media_cuota_numero"));
                    c.setActividadesEntregables(rs.getString("actividades_entregables"));
                    c.setLiquidacionAcuerdo(rs.getString("liquidacion_acuerdo"));
                    c.setLiquidacionArticulo(rs.getString("liquidacion_articulo"));
                    c.setLiquidacionDecreto(rs.getString("liquidacion_decreto"));
                    c.setCircularHonorarios(rs.getString("circular_honorarios"));
                    c.setContratistaId(rs.getInt("contratista_id"));
                    c.setSupervisorId(rs.getInt("supervisor_id"));
                    c.setOrdenadorId(rs.getInt("ordenador_id"));
                    c.setPresupuestoId(rs.getInt("presupuesto_id"));
                    c.setEstructuradorId(rs.getInt("estructurador_id"));
                    c.setApoyoSupervision(rs.getString("apoyo_supervision"));
                    c.setFechaIdoneidad(rs.getDate("fecha_idoneidad"));
                    c.setFechaEstructurador(rs.getDate("fecha_estructurador"));
                    c.setAdicionSiNo(rs.getString("adicion_si_no"));
                    c.setNumeroCuotasAdicion(rs.getInt("numero_cuotas_adicion"));
                    c.setValorTotalAdicionLetras(rs.getString("valor_total_adicion_letras"));
                    c.setValorTotalAdicion(rs.getBigDecimal("valor_total_adicion"));
                    c.setValorContratoMasAdicionLetras(rs.getString("valor_contrato_mas_adicion_letras"));
                    c.setValorContratoMasAdicion(rs.getBigDecimal("valor_contrato_mas_adicion"));
                    c.setEnlaceSecop(rs.getString("enlace_secop"));
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Contrato obtenerPorId(int id) {
        String sql = "SELECT * FROM contratos WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Contrato c = new Contrato();
                    c.setId(rs.getInt("id"));
                    c.setTrdProceso(rs.getString("trd_proceso"));
                    c.setNumeroContrato(rs.getString("numero_contrato"));
                    c.setTipoContrato(rs.getString("tipo_contrato"));
                    c.setNivel(rs.getString("nivel"));
                    c.setObjeto(rs.getString("objeto"));
                    c.setModalidad(rs.getString("modalidad"));
                    c.setEstado(rs.getString("estado"));
                    c.setPeriodo(rs.getString("periodo"));
                    c.setFechaSuscripcion(rs.getDate("fecha_suscripcion"));
                    c.setFechaInicio(rs.getDate("fecha_inicio"));
                    c.setFechaTerminacion(rs.getDate("fecha_terminacion"));
                    c.setFechaAprobacion(rs.getDate("fecha_aprobacion"));
                    c.setFechaEjecucion(rs.getDate("fecha_ejecucion"));
                    c.setFechaArl(rs.getDate("fecha_arl"));
                    c.setPlazoEjecucion(rs.getString("plazo_ejecucion"));
                    c.setPlazoMeses(rs.getInt("plazo_meses"));
                    c.setPlazoDias(rs.getInt("plazo_dias"));
                    c.setValorTotalLetras(rs.getString("valor_total_letras"));
                    c.setValorTotalNumeros(rs.getBigDecimal("valor_total_numeros"));
                    c.setValorAntesIva(rs.getBigDecimal("valor_antes_iva"));
                    c.setValorIva(rs.getBigDecimal("valor_iva"));
                    c.setValorAntesIvaLetras(rs.getString("valor_antes_iva_letras"));
                    c.setValorIvaLetras(rs.getString("valor_iva_letras"));
                    c.setValorCuotaLetras(rs.getString("valor_cuota_letras"));
                    c.setValorCuotaNumero(rs.getBigDecimal("valor_cuota_numero"));
                    c.setValorCuotaAntesIvaLetras(rs.getString("valor_cuota_antes_iva_letras"));
                    c.setValorCuotaAntesIva(rs.getBigDecimal("valor_cuota_antes_iva"));
                    c.setValorCuotaIvaLetras(rs.getString("valor_cuota_iva_letras"));
                    c.setValorCuotaIva(rs.getBigDecimal("valor_cuota_iva"));
                    c.setNumCuotasLetras(rs.getString("num_cuotas_letras"));
                    c.setNumCuotasNumero(rs.getInt("num_cuotas_numero"));
                    c.setValorMediaCuotaLetras(rs.getString("valor_media_cuota_letras"));
                    c.setValorMediaCuotaNumero(rs.getBigDecimal("valor_media_cuota_numero"));
                    c.setActividadesEntregables(rs.getString("actividades_entregables"));
                    c.setLiquidacionAcuerdo(rs.getString("liquidacion_acuerdo"));
                    c.setLiquidacionArticulo(rs.getString("liquidacion_articulo"));
                    c.setLiquidacionDecreto(rs.getString("liquidacion_decreto"));
                    c.setCircularHonorarios(rs.getString("circular_honorarios"));
                    c.setContratistaId(rs.getInt("contratista_id"));
                    c.setSupervisorId(rs.getInt("supervisor_id"));
                    c.setOrdenadorId(rs.getInt("ordenador_id"));
                    c.setPresupuestoId(rs.getInt("presupuesto_id"));
                    c.setEstructuradorId(rs.getInt("estructurador_id"));
                    c.setApoyoSupervision(rs.getString("apoyo_supervision"));
                    c.setFechaIdoneidad(rs.getDate("fecha_idoneidad"));
                    c.setFechaEstructurador(rs.getDate("fecha_estructurador"));
                    c.setAdicionSiNo(rs.getString("adicion_si_no"));
                    c.setNumeroCuotasAdicion(rs.getInt("numero_cuotas_adicion"));
                    c.setValorTotalAdicionLetras(rs.getString("valor_total_adicion_letras"));
                    c.setValorTotalAdicion(rs.getBigDecimal("valor_total_adicion"));
                    c.setValorContratoMasAdicionLetras(rs.getString("valor_contrato_mas_adicion_letras"));
                    c.setValorContratoMasAdicion(rs.getBigDecimal("valor_contrato_mas_adicion"));
                    c.setEnlaceSecop(rs.getString("enlace_secop"));
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
