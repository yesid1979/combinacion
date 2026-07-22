package com.combinacion.dao;
import com.combinacion.models.Auditoria;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaDAO {
    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS auditoria_sistema (" +
                     "id SERIAL PRIMARY KEY, " +
                     "fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                     "username VARCHAR(100) NOT NULL, " +
                     "nombres_apellidos VARCHAR(255) NOT NULL, " +
                     "tipo_accion VARCHAR(100) NOT NULL, " +
                     "accion_realizada TEXT NOT NULL, " +
                     "tipo_usuario VARCHAR(100), " +
                     "ip_address VARCHAR(45)" +
                     ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public AuditoriaDAO() { crearTablaSiNoExiste(); }

    public void registrarAccion(Auditoria a) {
        String sql = "INSERT INTO auditoria_sistema (username, nombres_apellidos, tipo_accion, accion_realizada, tipo_usuario, ip_address) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getUsername());
            ps.setString(2, a.getNombresApellidos());
            ps.setString(3, a.getTipoAccion());
            ps.setString(4, a.getAccionRealizada());
            ps.setString(5, a.getTipoUsuario());
            ps.setString(6, a.getIpAddress());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    public static void registrar(com.combinacion.models.Usuario u, String accion, String descripcion, String ip) {
        if (u == null) return;
        Auditoria a = new Auditoria();
        a.setUsername(u.getUsername());
        a.setNombresApellidos(u.getNombreCompleto());
        a.setTipoAccion(accion);
        a.setAccionRealizada(descripcion);
        a.setTipoUsuario(u.getRol() != null ? u.getRol().getNombre() : "Usuario");
        a.setIpAddress(ip != null ? ip : "Desconocida");
        
        new AuditoriaDAO().registrarAccion(a);
    }

    // SERVER SIDE DATATABLES METHODS
    public int getTotalRegistros(String search) {
        String sql = "SELECT COUNT(*) FROM auditoria_sistema";
        boolean hasSearch = (search != null && !search.trim().isEmpty());
        if (hasSearch) {
            sql += " WHERE username ILIKE ? OR nombres_apellidos ILIKE ? OR tipo_accion ILIKE ? OR accion_realizada ILIKE ?";
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasSearch) {
                String term = "%" + search + "%";
                ps.setString(1, term);
                ps.setString(2, term);
                ps.setString(3, term);
                ps.setString(4, term);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<Auditoria> getRegistrosPaginados(int start, int length, String search, String orderBy, String orderDir) {
        List<Auditoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM auditoria_sistema";
        boolean hasSearch = (search != null && !search.trim().isEmpty());
        if (hasSearch) {
            sql += " WHERE username ILIKE ? OR nombres_apellidos ILIKE ? OR tipo_accion ILIKE ? OR accion_realizada ILIKE ?";
        }
        
        String safeOrderBy = "fecha_hora";
        if (orderBy != null) {
            switch (orderBy) {
                case "1": safeOrderBy = "id"; break;
                case "2": safeOrderBy = "fecha_hora"; break;
                case "4": safeOrderBy = "username"; break;
                case "5": safeOrderBy = "nombres_apellidos"; break;
                case "6": safeOrderBy = "tipo_accion"; break;
            }
        }
        String safeOrderDir = "asc".equalsIgnoreCase(orderDir) ? "ASC" : "DESC";
        
        sql += " ORDER BY " + safeOrderBy + " " + safeOrderDir + " LIMIT ? OFFSET ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            int paramIdx = 1;
            if (hasSearch) {
                String term = "%" + search + "%";
                ps.setString(paramIdx++, term);
                ps.setString(paramIdx++, term);
                ps.setString(paramIdx++, term);
                ps.setString(paramIdx++, term);
            }
            ps.setInt(paramIdx++, length);
            ps.setInt(paramIdx, start);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Auditoria a = new Auditoria();
                    a.setId(rs.getInt("id"));
                    a.setFechaHora(rs.getTimestamp("fecha_hora"));
                    a.setUsername(rs.getString("username"));
                    a.setNombresApellidos(rs.getString("nombres_apellidos"));
                    a.setTipoAccion(rs.getString("tipo_accion"));
                    a.setAccionRealizada(rs.getString("accion_realizada"));
                    a.setTipoUsuario(rs.getString("tipo_usuario"));
                    a.setIpAddress(rs.getString("ip_address"));
                    lista.add(a);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}
