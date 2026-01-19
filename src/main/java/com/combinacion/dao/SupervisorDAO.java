package com.combinacion.dao;

import com.combinacion.models.Supervisor;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupervisorDAO {

    public List<Supervisor> listarTodos() {
        List<Supervisor> lista = new ArrayList<>();
        String sql = "SELECT * FROM supervisores ORDER BY nombre";

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Supervisor s = new Supervisor();
                s.setId(rs.getInt("id"));
                s.setCedula(rs.getString("cedula"));
                s.setNombre(rs.getString("nombre"));
                s.setCargo(rs.getString("cargo"));
                lista.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean insertar(Supervisor s) {
        String sql = "INSERT INTO supervisores (cedula, nombre, cargo) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getCedula());
            ps.setString(2, s.getNombre());
            ps.setString(3, s.getCargo());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        s.setId(generatedKeys.getInt(1));
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

    public Supervisor obtenerPorCedula(String cedula) {
        String sql = "SELECT * FROM supervisores WHERE cedula = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Supervisor s = new Supervisor();
                    s.setId(rs.getInt("id"));
                    s.setCedula(rs.getString("cedula"));
                    s.setNombre(rs.getString("nombre"));
                    s.setCargo(rs.getString("cargo"));
                    return s;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM supervisores";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countFiltered(String search) {
        String sql = "SELECT COUNT(*) FROM supervisores WHERE 1=1 ";
        if (search != null && !search.isEmpty()) {
            sql += " AND (cedula LIKE ? OR nombre LIKE ? OR cargo LIKE ?)";
        }
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (search != null && !search.isEmpty()) {
                String like = "%" + search + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Supervisor> findWithPagination(int start, int length, String search, int orderCol, String orderDir) {
        List<Supervisor> lista = new ArrayList<>();
        String sql = "SELECT * FROM supervisores WHERE 1=1 ";

        if (search != null && !search.isEmpty()) {
            sql += " AND (cedula LIKE ? OR nombre LIKE ? OR cargo LIKE ?)";
        }

        String[] cols = { "cedula", "nombre", "cargo" };
        String sortCol = (orderCol >= 0 && orderCol < cols.length) ? cols[orderCol] : "nombre";

        if (!"asc".equalsIgnoreCase(orderDir) && !"desc".equalsIgnoreCase(orderDir)) {
            orderDir = "asc";
        }

        sql += " ORDER BY " + sortCol + " " + orderDir;
        sql += " LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;
            if (search != null && !search.isEmpty()) {
                String like = "%" + search + "%";
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index++, like);
            }
            ps.setInt(index++, length);
            ps.setInt(index++, start);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Supervisor s = new Supervisor();
                    s.setId(rs.getInt("id"));
                    s.setCedula(rs.getString("cedula"));
                    s.setNombre(rs.getString("nombre"));
                    s.setCargo(rs.getString("cargo"));
                    lista.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;

    }

    public Supervisor obtenerPorId(int id) {
        String sql = "SELECT * FROM supervisores WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Supervisor s = new Supervisor();
                    s.setId(rs.getInt("id"));
                    s.setCedula(rs.getString("cedula"));
                    s.setNombre(rs.getString("nombre"));
                    s.setCargo(rs.getString("cargo"));
                    return s;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean actualizar(Supervisor s) {
        String sql = "UPDATE supervisores SET cedula=?, nombre=?, cargo=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getCedula());
            ps.setString(2, s.getNombre());
            ps.setString(3, s.getCargo());
            ps.setInt(4, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM supervisores WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public java.util.Set<String> obtenerTodasLasCedulas() {
        java.util.Set<String> cedulas = new java.util.HashSet<>();
        String sql = "SELECT cedula FROM supervisores WHERE cedula IS NOT NULL";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cedulas.add(rs.getString("cedula"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cedulas;
    }
}
