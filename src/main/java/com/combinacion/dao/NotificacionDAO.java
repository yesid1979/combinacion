package com.combinacion.dao;

import com.combinacion.models.Notificacion;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificacionDAO {

    static {
        verificarTabla();
    }

    private static void verificarTabla() {
        String ddl = "CREATE TABLE IF NOT EXISTS notificaciones_usuario (" +
                     "  id SERIAL PRIMARY KEY," +
                     "  id_usuario INT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE," +
                     "  id_informe INT," +
                     "  titulo VARCHAR(255) NOT NULL," +
                     "  mensaje TEXT NOT NULL," +
                     "  tipo VARCHAR(50) DEFAULT 'INFO'," +
                     "  url VARCHAR(500)," +
                     "  leida BOOLEAN DEFAULT FALSE," +
                     "  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                     ");" +
                     "CREATE INDEX IF NOT EXISTS idx_notif_user_leida ON notificaciones_usuario(id_usuario, leida);" +
                     "CREATE INDEX IF NOT EXISTS idx_notif_user_fecha ON notificaciones_usuario(id_usuario, fecha_creacion DESC);";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        } catch (Exception e) {
            System.err.println("Aviso al verificar tabla notificaciones_usuario: " + e.getMessage());
        }
    }

    public boolean crear(Notificacion n) {
        String sql = "INSERT INTO notificaciones_usuario (id_usuario, id_informe, titulo, mensaje, tipo, url, leida) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getIdUsuario());
            if (n.getIdInforme() != null) {
                ps.setInt(2, n.getIdInforme());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, n.getTitulo());
            ps.setString(4, n.getMensaje());
            ps.setString(5, n.getTipo() != null ? n.getTipo() : "INFO");
            ps.setString(6, n.getUrl());
            ps.setBoolean(7, n.isLeida());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        n.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar notificacion: " + e.getMessage());
        }
        return false;
    }

    public boolean crearParaUsuarioPorCedula(String cedula, Integer idInforme, String titulo, String mensaje, String tipo, String url) {
        if (cedula == null || cedula.trim().isEmpty()) return false;
        String sql = "SELECT id FROM usuarios WHERE regexp_replace(cedula, '[^0-9]', '', 'g') = regexp_replace(?, '[^0-9]', '', 'g') AND activo = true LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idUsuario = rs.getInt("id");
                    Notificacion n = new Notificacion(idUsuario, idInforme, titulo, mensaje, tipo, url);
                    return crear(n);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al crear notificacion por cedula: " + e.getMessage());
        }
        return false;
    }

    public void crearParaRol(String nombreRol, Integer idInforme, String titulo, String mensaje, String tipo, String url) {
        if (nombreRol == null || nombreRol.trim().isEmpty()) return;
        String sql = "SELECT u.id FROM usuarios u " +
                     "JOIN roles r ON u.id_rol = r.id " +
                     "WHERE LOWER(TRIM(r.nombre)) = LOWER(TRIM(?)) AND u.activo = true";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreRol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idUsuario = rs.getInt("id");
                    Notificacion n = new Notificacion(idUsuario, idInforme, titulo, mensaje, tipo, url);
                    crear(n);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al crear notificacion por rol: " + e.getMessage());
        }
    }

    public List<Notificacion> listarPorUsuario(int idUsuario, int limite) {
        List<Notificacion> lista = new ArrayList<>();
        String sql = "SELECT * FROM notificaciones_usuario WHERE id_usuario = ? " +
                     "ORDER BY leida ASC, fecha_creacion DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, limite > 0 ? limite : 15);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notificacion n = new Notificacion();
                    n.setId(rs.getInt("id"));
                    n.setIdUsuario(rs.getInt("id_usuario"));
                    int idInf = rs.getInt("id_informe");
                    n.setIdInforme(rs.wasNull() ? null : idInf);
                    n.setTitulo(rs.getString("titulo"));
                    n.setMensaje(rs.getString("mensaje"));
                    n.setTipo(rs.getString("tipo"));
                    n.setUrl(rs.getString("url"));
                    n.setLeida(rs.getBoolean("leida"));
                    n.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
                    lista.add(n);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar notificaciones: " + e.getMessage());
        }
        return lista;
    }

    public int contarNoLeidas(int idUsuario) {
        String sql = "SELECT COUNT(*) FROM notificaciones_usuario WHERE id_usuario = ? AND leida = false";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al contar no leidas: " + e.getMessage());
        }
        return 0;
    }

    public int obtenerUltimoId(int idUsuario) {
        String sql = "SELECT COALESCE(MAX(id), 0) FROM notificaciones_usuario WHERE id_usuario = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ultimo id notificacion: " + e.getMessage());
        }
        return 0;
    }

    public boolean marcarLeida(int idNotificacion, int idUsuario) {
        String sql = "UPDATE notificaciones_usuario SET leida = true WHERE id = ? AND id_usuario = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idNotificacion);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al marcar leida: " + e.getMessage());
            return false;
        }
    }

    public boolean marcarTodasLeidas(int idUsuario) {
        String sql = "UPDATE notificaciones_usuario SET leida = true WHERE id_usuario = ? AND leida = false";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al marcar todas leidas: " + e.getMessage());
            return false;
        }
    }
}
