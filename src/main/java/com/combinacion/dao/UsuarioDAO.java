package com.combinacion.dao;

import com.combinacion.models.Permiso;
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
                   + "u.correo, u.cedula, u.celular, u.sexo, u.vinculacion, u.fecha_inicio_contrato, u.fecha_fin_contrato, "
                   + "u.activo, u.ultimo_acceso, u.fecha_creacion, u.rol_id, "
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
     * Obtiene un usuario por ID.
     */
    public Usuario obtenerPorId(int id) {
        String sql = "SELECT u.id, u.username, u.password_hash, u.salt, u.nombre_completo, "
                   + "u.correo, u.cedula, u.celular, u.sexo, u.vinculacion, u.fecha_inicio_contrato, u.fecha_fin_contrato, "
                   + "u.activo, u.ultimo_acceso, u.fecha_creacion, u.rol_id, "
                   + "r.nombre as rol_nombre "
                   + "FROM usuarios u LEFT JOIN roles r ON u.rol_id = r.id "
                   + "WHERE u.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = mapear(rs);
                    if (u.getRolId() > 0) {
                        u.setRol(rolDAO.obtenerPorId(u.getRolId()));
                    }
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
     * Obtiene un usuario por username.
     */
    public Usuario obtenerPorUsername(String username) {
        String sql = "SELECT u.id, u.username, u.password_hash, u.salt, u.nombre_completo, "
                   + "u.correo, u.cedula, u.celular, u.sexo, u.vinculacion, u.fecha_inicio_contrato, u.fecha_fin_contrato, "
                   + "u.activo, u.ultimo_acceso, u.fecha_creacion, u.rol_id, "
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
     * Inserta un nuevo usuario con los campos modernos.
     */
    public int insertar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (username, password_hash, salt, nombre_completo, correo, "
                   + "cedula, celular, sexo, vinculacion, fecha_inicio_contrato, fecha_fin_contrato, activo, rol_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setString(3, usuario.getSalt());
            ps.setString(4, usuario.getNombreCompleto());
            ps.setString(5, usuario.getCorreo());
            ps.setString(6, usuario.getCedula());
            ps.setString(7, usuario.getCelular());
            ps.setString(8, usuario.getSexo());
            ps.setString(9, usuario.getVinculacion());
            ps.setDate(10, usuario.getFechaInicioContrato());
            ps.setDate(11, usuario.getFechaFinContrato());
            ps.setBoolean(12, usuario.isActivo());
            ps.setInt(13, usuario.getRolId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Actualiza un usuario sin cambiar el password.
     */
    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET username = ?, nombre_completo = ?, correo = ?, "
                   + "cedula = ?, celular = ?, sexo = ?, vinculacion = ?, "
                   + "fecha_inicio_contrato = ?, fecha_fin_contrato = ?, activo = ?, rol_id = ? "
                   + "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getNombreCompleto());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getCedula());
            ps.setString(5, usuario.getCelular());
            ps.setString(6, usuario.getSexo());
            ps.setString(7, usuario.getVinculacion()); 
            ps.setDate(8, usuario.getFechaInicioContrato());
            ps.setDate(9, usuario.getFechaFinContrato());
            ps.setBoolean(10, usuario.isActivo());
            ps.setInt(11, usuario.getRolId());
            ps.setInt(12, usuario.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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

    public boolean existeCedula(String cedula, int excludeId) {
        if (cedula == null || cedula.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM usuarios WHERE cedula = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cedula.trim());
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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

    /**
     * Actualiza los permisos especiales de un usuario (sobrescritura).
     */
    public boolean actualizarPermisosEspeciales(int id, List<Integer> permisosIds) {
        String deleteSql = "DELETE FROM usuario_permisos WHERE usuario_id = ?";
        String insertSql = "INSERT INTO usuario_permisos (usuario_id, permiso_id) VALUES (?, ?)";
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Eliminar permisos especiales previos
                try (PreparedStatement psDel = conn.prepareStatement(deleteSql)) {
                    psDel.setInt(1, id);
                    psDel.executeUpdate();
                }
                
                // 2. Insertar los nuevos permisos seleccionados
                if (permisosIds != null && !permisosIds.isEmpty()) {
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        for (Integer pid : permisosIds) {
                            psIns.setInt(1, id);
                            psIns.setInt(2, pid);
                            psIns.addBatch();
                        }
                        psIns.executeBatch();
                    }
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setSalt(rs.getString("salt"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setCorreo(rs.getString("correo"));
        u.setCedula(rs.getString("cedula"));
        u.setCelular(rs.getString("celular"));
        u.setSexo(rs.getString("sexo"));
        u.setVinculacion(rs.getString("vinculacion")); 
        u.setFechaInicioContrato(rs.getDate("fecha_inicio_contrato"));
        u.setFechaFinContrato(rs.getDate("fecha_fin_contrato"));
        u.setActivo(rs.getBoolean("activo"));
        u.setUltimoAcceso(rs.getTimestamp("ultimo_acceso"));
        u.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        u.setRolId(rs.getInt("rol_id"));

        try {
            String rolNombre = rs.getString("rol_nombre");
            if (rolNombre != null) {
                Rol rol = new Rol();
                rol.setId(u.getRolId());
                rol.setNombre(rolNombre);
                u.setRol(rol);
            }
        } catch (SQLException ignored) {}
        return u;
    }
}
