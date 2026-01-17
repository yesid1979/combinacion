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
}
