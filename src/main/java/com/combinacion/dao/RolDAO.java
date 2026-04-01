package com.combinacion.dao;

import com.combinacion.models.Rol;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones CRUD de Roles y asignación de permisos.
 */
public class RolDAO {

    private final PermisoDAO permisoDAO = new PermisoDAO();

    /**
     * Lista todos los roles.
     */
    public List<Rol> listarTodos() {
        List<Rol> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, activo, fecha_creacion FROM roles ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Rol r = mapear(rs);
                r.setPermisos(permisoDAO.listarPorRolId(r.getId()));
                lista.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene un rol por ID con sus permisos cargados.
     */
    public Rol obtenerPorId(int id) {
        String sql = "SELECT id, nombre, descripcion, activo, fecha_creacion FROM roles WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Rol r = mapear(rs);
                    r.setPermisos(permisoDAO.listarPorRolId(r.getId()));
                    return r;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtiene un rol por nombre.
     */
    public Rol obtenerPorNombre(String nombre) {
        String sql = "SELECT id, nombre, descripcion, activo, fecha_creacion FROM roles WHERE nombre = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Rol r = mapear(rs);
                    r.setPermisos(permisoDAO.listarPorRolId(r.getId()));
                    return r;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Inserta un nuevo rol y devuelve el ID generado.
     */
    public int insertar(Rol rol) {
        String sql = "INSERT INTO roles (nombre, descripcion, activo) VALUES (?, ?, ?) RETURNING id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rol.getNombre());
            ps.setString(2, rol.getDescripcion());
            ps.setBoolean(3, rol.isActivo());
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
     * Actualiza un rol existente.
     */
    public boolean actualizar(Rol rol) {
        String sql = "UPDATE roles SET nombre = ?, descripcion = ?, activo = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rol.getNombre());
            ps.setString(2, rol.getDescripcion());
            ps.setBoolean(3, rol.isActivo());
            ps.setInt(4, rol.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Elimina un rol por ID (CASCADE eliminará rol_permisos).
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM roles WHERE id = ?";
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
     * Reemplaza los permisos de un rol.
     * Elimina todos los existentes y asigna los nuevos.
     */
    public boolean asignarPermisos(int rolId, List<Integer> permisosIds) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Eliminar permisos actuales
            String deleteSql = "DELETE FROM rol_permisos WHERE rol_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, rolId);
                ps.executeUpdate();
            }

            // Insertar nuevos permisos
            if (permisosIds != null && !permisosIds.isEmpty()) {
                String insertSql = "INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (Integer permisoId : permisosIds) {
                        ps.setInt(1, rolId);
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
     * Cuenta cuántos usuarios están asociados a un rol.
     */
    public int contarUsuariosPorRol(int rolId) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE rol_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rolId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Rol mapear(ResultSet rs) throws SQLException {
        Rol r = new Rol();
        r.setId(rs.getInt("id"));
        r.setNombre(rs.getString("nombre"));
        r.setDescripcion(rs.getString("descripcion"));
        r.setActivo(rs.getBoolean("activo"));
        r.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        return r;
    }
}
