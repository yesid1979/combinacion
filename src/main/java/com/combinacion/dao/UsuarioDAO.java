package com.combinacion.dao;

import com.combinacion.models.Rol;
import com.combinacion.models.Usuario;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones CRUD de Usuarios.
 */
public class UsuarioDAO {

    private final RolDAO rolDAO = new RolDAO();
    private final PermisoDAO permisoDAO = new PermisoDAO();

    /**
     * Lista todos los usuarios con su rol cargado.
     */
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.password_hash, u.salt, u.nombre_completo, "
                   + "u.correo, u.activo, u.ultimo_acceso, u.fecha_creacion, u.rol_id, "
                   + "r.nombre as rol_nombre "
                   + "FROM usuarios u LEFT JOIN roles r ON u.rol_id = r.id "
                   + "ORDER BY u.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene un usuario por ID con su rol y permisos cargados.
     */
    public Usuario obtenerPorId(int id) {
        String sql = "SELECT u.id, u.username, u.password_hash, u.salt, u.nombre_completo, "
                   + "u.correo, u.activo, u.ultimo_acceso, u.fecha_creacion, u.rol_id, "
                   + "r.nombre as rol_nombre "
                   + "FROM usuarios u LEFT JOIN roles r ON u.rol_id = r.id "
                   + "WHERE u.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = mapear(rs);
                    // Cargar rol completo con permisos
                    if (u.getRolId() > 0) {
                        u.setRol(rolDAO.obtenerPorId(u.getRolId()));
                    }
                    // Cargar permisos especiales
                    u.setPermisosEspeciales(permisoDAO.listarPorUsuarioId(id));
                    return u;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Reemplaza los permisos especiales de un usuario.
     */
    public boolean actualizarPermisosEspeciales(int usuarioId, List<Integer> permisosIds) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Eliminar permisos actuales del usuario
            String deleteSql = "DELETE FROM usuario_permisos WHERE usuario_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, usuarioId);
                ps.executeUpdate();
            }

            // Insertar nuevos permisos
            if (permisosIds != null && !permisosIds.isEmpty()) {
                String insertSql = "INSERT INTO usuario_permisos (usuario_id, permiso_id) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (Integer permisoId : permisosIds) {
                        ps.setInt(1, usuarioId);
                        ps.setInt(2, permisoId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        return false;
    }

    /**
     * Obtiene un usuario por username con rol y permisos completos (para login).
     */
    public Usuario obtenerPorUsername(String username) {
        String sql = "SELECT u.id, u.username, u.password_hash, u.salt, u.nombre_completo, "
                   + "u.correo, u.activo, u.ultimo_acceso, u.fecha_creacion, u.rol_id, "
                   + "r.nombre as rol_nombre "
                   + "FROM usuarios u LEFT JOIN roles r ON u.rol_id = r.id "
                   + "WHERE u.username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = mapear(rs);
                    if (u.getRolId() > 0) {
                        u.setRol(rolDAO.obtenerPorId(u.getRolId()));
                    }
                    // Cargar permisos especiales para sesión
                    u.setPermisosEspeciales(permisoDAO.listarPorUsuarioId(u.getId()));
                    return u;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Inserta un nuevo usuario. Devuelve el ID generado o -1 si falla.
     */
    public int insertar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (username, password_hash, salt, nombre_completo, correo, activo, rol_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setString(3, usuario.getSalt());
            ps.setString(4, usuario.getNombreCompleto());
            ps.setString(5, usuario.getCorreo());
            ps.setBoolean(6, usuario.isActivo());
            ps.setInt(7, usuario.getRolId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Actualiza los datos de un usuario (sin cambiar la contraseña).
     */
    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET username = ?, nombre_completo = ?, correo = ?, activo = ?, rol_id = ? "
                   + "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getNombreCompleto());
            ps.setString(3, usuario.getCorreo());
            ps.setBoolean(4, usuario.isActivo());
            ps.setInt(5, usuario.getRolId());
            ps.setInt(6, usuario.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Actualiza la contraseña de un usuario.
     */
    public boolean actualizarPassword(int id, String passwordHash, String salt) {
        String sql = "UPDATE usuarios SET password_hash = ?, salt = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setString(2, salt);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Actualiza la fecha de último acceso.
     */
    public boolean actualizarUltimoAcceso(int id) {
        String sql = "UPDATE usuarios SET ultimo_acceso = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Elimina un usuario por ID.
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Verifica si un username ya existe (excluyendo un ID específico para edición).
     */
    public boolean existeUsername(String username, int excludeId) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cuenta el total de usuarios.
     */
    public int contarTotal() {
        String sql = "SELECT COUNT(*) FROM usuarios";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setSalt(rs.getString("salt"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setCorreo(rs.getString("correo"));
        u.setActivo(rs.getBoolean("activo"));
        u.setUltimoAcceso(rs.getTimestamp("ultimo_acceso"));
        u.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        u.setRolId(rs.getInt("rol_id"));

        // Crear un Rol básico con el nombre del JOIN
        try {
            String rolNombre = rs.getString("rol_nombre");
            if (rolNombre != null) {
                Rol rol = new Rol();
                rol.setId(u.getRolId());
                rol.setNombre(rolNombre);
                u.setRol(rol);
            }
        } catch (SQLException ignored) {
            // Column not in result set
        }
        return u;
    }
}
