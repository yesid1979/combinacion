package com.combinacion.dao;

import com.combinacion.models.ConsecutivoCobro;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsecutivoDAO {

    public void inicializarTabla() {
        String sqlTable = "CREATE TABLE IF NOT EXISTS consecutivos_cobro (" +
                          "id SERIAL PRIMARY KEY, " +
                          "cedula VARCHAR(50) NOT NULL, " +
                          "contrato VARCHAR(50) NOT NULL, " +
                          "numero_cuota VARCHAR(20) NOT NULL, " +
                          "consecutivo VARCHAR(50) NOT NULL, " +
                          "fecha_carga TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                          "cargado_por INTEGER, " +
                          "UNIQUE (cedula, contrato, numero_cuota)" +
                          ")";
                          
        String sqlPermiso = "INSERT INTO permisos (codigo, nombre, descripcion, modulo) " +
                            "SELECT 'CONSECUTIVOS_VER', 'Ver y gestionar consecutivos', 'Permite cargar masivamente y gestionar los consecutivos de cobro', 'CONSECUTIVOS' " +
                            "WHERE NOT EXISTS (SELECT 1 FROM permisos WHERE codigo = 'CONSECUTIVOS_VER')";
                            
        String sqlUpdatePermiso = "UPDATE permisos SET modulo = 'CONSECUTIVOS', codigo = 'CONSECUTIVOS_VER', nombre = 'Ver y gestionar consecutivos' WHERE codigo = 'CONSECUTIVOS_VER' OR codigo = 'CONSECUTIVOS_GESTIONAR'";
                            
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlTable);
            stmt.executeUpdate(sqlPermiso);
            stmt.executeUpdate(sqlUpdatePermiso);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean guardarMasivo(List<ConsecutivoCobro> lista, int cargadoPor) {
        String sql = "INSERT INTO consecutivos_cobro (cedula, contrato, numero_cuota, consecutivo, cargado_por) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON CONFLICT (cedula, contrato, numero_cuota) " +
                     "DO UPDATE SET consecutivo = EXCLUDED.consecutivo, fecha_carga = CURRENT_TIMESTAMP, cargado_por = EXCLUDED.cargado_por";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            for (ConsecutivoCobro c : lista) {
                ps.setString(1, c.getCedula());
                ps.setString(2, c.getContrato());
                ps.setString(3, c.getNumeroCuota());
                ps.setString(4, c.getConsecutivo());
                ps.setInt(5, cargadoPor);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<ConsecutivoCobro> listarTodos() {
        List<ConsecutivoCobro> lista = new ArrayList<>();
        String sql = "SELECT * FROM consecutivos_cobro ORDER BY fecha_carga DESC LIMIT 1000";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                ConsecutivoCobro c = new ConsecutivoCobro();
                c.setId(rs.getInt("id"));
                c.setCedula(rs.getString("cedula"));
                c.setContrato(rs.getString("contrato"));
                c.setNumeroCuota(rs.getString("numero_cuota"));
                c.setConsecutivo(rs.getString("consecutivo"));
                c.setFechaCarga(rs.getTimestamp("fecha_carga"));
                c.setCargadoPor(rs.getInt("cargado_por"));
                lista.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public String obtenerConsecutivo(String cedula, String contrato, String numeroCuota) {
        if (cedula == null || contrato == null || numeroCuota == null) return null;
        
        String sql = "SELECT consecutivo FROM consecutivos_cobro " +
                     "WHERE cedula = ? AND contrato = ? AND numero_cuota = ? " +
                     "ORDER BY id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cedula.trim());
            ps.setString(2, contrato.trim());
            ps.setString(3, numeroCuota.trim());
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("consecutivo");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean eliminarTodos() {
        String sql = "DELETE FROM consecutivos_cobro";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql) >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
