package com.combinacion.dao;

import com.combinacion.models.PresupuestoDetalle;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PresupuestoDetalleDAO {

    public boolean insertar(PresupuestoDetalle p) {
        String sql = "INSERT INTO presupuesto_detalles (cdp_numero, cdp_fecha, cdp_valor, cdp_vencimiento, rp_numero, rp_fecha, apropiacion_presupuestal, id_paa, codigo_dane, inversion, funcionamiento, ficha_ebi_nombre, ficha_ebi_objetivo, ficha_ebi_actividades, certificado_insuficiencia, fecha_insuficiencia) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getCdpNumero());
            ps.setDate(2, p.getCdpFecha());
            ps.setBigDecimal(3, p.getCdpValor());
            ps.setDate(4, p.getCdpVencimiento());
            ps.setString(5, p.getRpNumero());
            ps.setDate(6, p.getRpFecha());
            ps.setString(7, p.getApropiacionPresupuestal());
            ps.setString(8, p.getIdPaa());
            ps.setString(9, p.getCodigoDane());
            ps.setString(10, p.getInversion());
            ps.setString(11, p.getFuncionamiento());
            ps.setString(12, p.getFichaEbiNombre());
            ps.setString(13, p.getFichaEbiObjetivo());
            ps.setString(14, p.getFichaEbiActividades());
            ps.setString(15, p.getCertificadoInsuficiencia());
            ps.setDate(16, p.getFechaInsuficiencia());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        p.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<PresupuestoDetalle> listar() {
        List<PresupuestoDetalle> lista = new ArrayList<>();
        String sql = "SELECT * FROM presupuesto_detalles";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PresupuestoDetalle p = new PresupuestoDetalle();
                p.setId(rs.getInt("id"));
                p.setCdpNumero(rs.getString("cdp_numero"));
                p.setCdpFecha(rs.getDate("cdp_fecha"));
                p.setCdpValor(rs.getBigDecimal("cdp_valor"));
                p.setCdpVencimiento(rs.getDate("cdp_vencimiento"));
                p.setRpNumero(rs.getString("rp_numero"));
                p.setRpFecha(rs.getDate("rp_fecha"));
                p.setApropiacionPresupuestal(rs.getString("apropiacion_presupuestal"));
                p.setIdPaa(rs.getString("id_paa"));
                p.setCodigoDane(rs.getString("codigo_dane"));
                p.setInversion(rs.getString("inversion"));
                p.setFuncionamiento(rs.getString("funcionamiento"));
                p.setFichaEbiNombre(rs.getString("ficha_ebi_nombre"));
                p.setFichaEbiObjetivo(rs.getString("ficha_ebi_objetivo"));
                p.setFichaEbiActividades(rs.getString("ficha_ebi_actividades"));
                p.setCertificadoInsuficiencia(rs.getString("certificado_insuficiencia"));
                p.setFechaInsuficiencia(rs.getDate("fecha_insuficiencia"));
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
