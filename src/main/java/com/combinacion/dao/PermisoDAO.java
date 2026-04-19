package com.combinacion.dao;

import com.combinacion.models.Permiso;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones CRUD de Permisos.
 */
public class PermisoDAO {

    /**
     * Lista todos los permisos del sistema.
     */
    public List<Permiso> listarTodos() {
        List<Permiso> lista = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, modulo, descripcion FROM permisos ORDER BY modulo, codigo";
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
     * Lista permisos filtrados por módulo.
     */
    public List<Permiso> listarPorModulo(String modulo) {
        List<Permiso> lista = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, modulo, descripcion FROM permisos WHERE modulo = ? ORDER BY codigo";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, modulo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene un permiso por su ID.
     */
    public Permiso obtenerPorId(int id) {
        String sql = "SELECT id, codigo, nombre, modulo, descripcion FROM permisos WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtiene los permisos asignados a un rol.
     */
    public List<Permiso> listarPorRolId(int rolId) {
        List<Permiso> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.codigo, p.nombre, p.modulo, p.descripcion "
                   + "FROM permisos p INNER JOIN rol_permisos rp ON p.id = rp.permiso_id "
                   + "WHERE rp.rol_id = ? ORDER BY p.modulo, p.codigo";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rolId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene los permisos especiales asignados directamente a un usuario.
     */
    public List<Permiso> listarPorUsuarioId(int usuarioId) {
        List<Permiso> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.codigo, p.nombre, p.modulo, p.descripcion "
                   + "FROM permisos p INNER JOIN usuario_permisos up ON p.id = up.permiso_id "
                   + "WHERE up.usuario_id = ? ORDER BY p.modulo, p.codigo";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Lista los módulos distintos disponibles.
     */
    public List<String> listarModulos() {
        List<String> modulos = new ArrayList<>();
        String sql = "SELECT DISTINCT modulo FROM permisos ORDER BY modulo";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modulos.add(rs.getString("modulo"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modulos;
    }

    private Permiso mapear(ResultSet rs) throws SQLException {
        Permiso p = new Permiso();
        p.setId(rs.getInt("id"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setModulo(rs.getString("modulo"));
        p.setDescripcion(rs.getString("descripcion"));
        return p;
    }
}
