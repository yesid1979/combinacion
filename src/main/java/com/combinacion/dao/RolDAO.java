package com.combinacion.dao;

import com.combinacion.models.Rol;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RolDAO {
    private final PermisoDAO permisoDAO = new PermisoDAO();

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
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

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
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

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
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM roles";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countFiltered(String search) {
        String sql = "SELECT COUNT(*) FROM roles WHERE nombre ILIKE ? OR descripcion ILIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String s = "%" + (search != null ? search : "") + "%";
            ps.setString(1, s);
            ps.setString(2, s);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<Rol> findWithPagination(int start, int length, String search, String sortCol, String sortDir) {
        List<Rol> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, activo, fecha_creacion FROM roles " +
                     "WHERE nombre ILIKE ? OR descripcion ILIKE ? " +
                     "ORDER BY " + sortCol + " " + sortDir + " LIMIT ? OFFSET ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String s = "%" + (search != null ? search : "") + "%";
            ps.setString(1, s);
            ps.setString(2, s);
            ps.setInt(3, length);
            ps.setInt(4, start);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { lista.add(mapear(rs)); }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public int insertar(Rol rol) {
        String sql = "INSERT INTO roles (nombre, descripcion, activo) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, rol.getNombre());
            ps.setString(2, rol.getDescripcion());
            ps.setBoolean(3, rol.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public boolean actualizar(Rol rol) {
        String sql = "UPDATE roles SET nombre = ?, descripcion = ?, activo = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rol.getNombre());
            ps.setString(2, rol.getDescripcion());
            ps.setBoolean(3, rol.isActivo());
            ps.setInt(4, rol.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM roles WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean asignarPermisos(int rolId, List<Integer> permisosIds) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM rol_permisos WHERE rol_id = ?")) {
                psDel.setInt(1, rolId);
                psDel.executeUpdate();
            }
            if (permisosIds != null && !permisosIds.isEmpty()) {
                try (PreparedStatement psIns = conn.prepareStatement("INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (?, ?)")) {
                    for (Integer pId : permisosIds) {
                        psIns.setInt(1, rolId);
                        psIns.setInt(2, pId);
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public int contarUsuariosPorRol(int rolId) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE rol_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rolId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
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
