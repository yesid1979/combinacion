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
        String sql = "INSERT INTO consecutivos_cobro (cedula, contrato, numero_cuota, consecutivo, cargado_por, anio) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (cedula, contrato, numero_cuota) " +
                     "DO UPDATE SET consecutivo = EXCLUDED.consecutivo, fecha_carga = CURRENT_TIMESTAMP, cargado_por = EXCLUDED.cargado_por, anio = EXCLUDED.anio";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            for (ConsecutivoCobro c : lista) {
                ps.setString(1, c.getCedula());
                ps.setString(2, c.getContrato());
                ps.setString(3, c.getNumeroCuota());
                ps.setString(4, c.getConsecutivo());
                ps.setInt(5, cargadoPor);
                if (c.getAnio() != null) {
                    ps.setInt(6, c.getAnio());
                } else {
                    ps.setNull(6, java.sql.Types.INTEGER);
                }
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

    public String obtenerConsecutivo(String cedula, String contrato, String numeroCuota, Integer anio) {
        if (contrato == null || numeroCuota == null) return null;
        
        String sql = "SELECT consecutivo FROM consecutivos_cobro " +
                     "WHERE contrato LIKE ? AND numero_cuota = ? ";
        if (anio != null) {
            sql += " AND anio = ? ";
        }
        sql += "ORDER BY id DESC LIMIT 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + contrato.trim() + "%");
            ps.setString(2, numeroCuota.trim());
            if (anio != null) {
                ps.setInt(3, anio);
            }
            
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

    public boolean eliminarVarios(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return false;
        
        StringBuilder sb = new StringBuilder("DELETE FROM consecutivos_cobro WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sb.append("?");
            if (i < ids.size() - 1) sb.append(",");
        }
        sb.append(")");
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ConsecutivoCobro> obtenerTodosPaginados(int start, int length, String search, Integer anio) {
        List<ConsecutivoCobro> lista = new ArrayList<>();
        String sql = "SELECT cc.*, c.nombre as nombre_contratista " +
                     "FROM consecutivos_cobro cc " +
                     "LEFT JOIN contratistas c ON regexp_replace(cc.cedula, '[^0-9]', '', 'g') = regexp_replace(c.cedula, '[^0-9]', '', 'g') " +
                     "WHERE 1=1 ";
        
        if (anio != null) {
            sql += "AND cc.anio = ? ";
        }
        
        if (search != null && !search.trim().isEmpty()) {
            sql += "AND (cc.cedula ILIKE ? OR cc.contrato ILIKE ? OR cc.numero_cuota ILIKE ? OR cc.consecutivo ILIKE ? OR c.nombre ILIKE ?) ";
        }
        
        sql += "ORDER BY cc.fecha_carga DESC LIMIT ? OFFSET ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            int paramIndex = 1;
            if (anio != null) {
                ps.setInt(paramIndex++, anio);
            }
            
            if (search != null && !search.trim().isEmpty()) {
                String likeSearch = "%" + search + "%";
                ps.setString(paramIndex++, likeSearch);
                ps.setString(paramIndex++, likeSearch);
                ps.setString(paramIndex++, likeSearch);
                ps.setString(paramIndex++, likeSearch);
                ps.setString(paramIndex++, likeSearch);
            }
            
            ps.setInt(paramIndex++, length);
            ps.setInt(paramIndex++, start);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ConsecutivoCobro c = new ConsecutivoCobro();
                    c.setId(rs.getInt("id"));
                    c.setCedula(rs.getString("cedula"));
                    c.setContrato(rs.getString("contrato"));
                    c.setNumeroCuota(rs.getString("numero_cuota"));
                    c.setConsecutivo(rs.getString("consecutivo"));
                    c.setFechaCarga(rs.getTimestamp("fecha_carga"));
                    c.setCargadoPor(rs.getInt("cargado_por"));
                    c.setNombre(rs.getString("nombre_contratista"));
                    
                    try {
                        int anioDb = rs.getInt("anio");
                        if (!rs.wasNull()) c.setAnio(anioDb);
                    } catch (java.sql.SQLException e) {}
                    
                    lista.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public int contarTodos(String search, Integer anio) {
        String sql = "SELECT COUNT(*) FROM consecutivos_cobro cc " +
                     "LEFT JOIN contratistas c ON regexp_replace(cc.cedula, '[^0-9]', '', 'g') = regexp_replace(c.cedula, '[^0-9]', '', 'g') " +
                     "WHERE 1=1 ";
                     
        if (anio != null) {
            sql += "AND cc.anio = ? ";
        }
        
        if (search != null && !search.trim().isEmpty()) {
            sql += "AND (cc.cedula ILIKE ? OR cc.contrato ILIKE ? OR cc.numero_cuota ILIKE ? OR cc.consecutivo ILIKE ? OR c.nombre ILIKE ?) ";
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            int paramIndex = 1;
            if (anio != null) {
                ps.setInt(paramIndex++, anio);
            }
            
            if (search != null && !search.trim().isEmpty()) {
                String likeSearch = "%" + search + "%";
                ps.setString(paramIndex++, likeSearch);
                ps.setString(paramIndex++, likeSearch);
                ps.setString(paramIndex++, likeSearch);
                ps.setString(paramIndex++, likeSearch);
                ps.setString(paramIndex++, likeSearch);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Integer> obtenerAniosDisponibles() {
        List<Integer> anios = new ArrayList<>();
        String sql = "SELECT anio FROM contratos WHERE anio IS NOT NULL " +
                     "UNION " +
                     "SELECT anio FROM consecutivos_cobro WHERE anio IS NOT NULL " +
                     "ORDER BY anio DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                anios.add(rs.getInt("anio"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (anios.isEmpty()) {
            anios.add(java.time.Year.now().getValue());
        }
        return anios;
    }
}
