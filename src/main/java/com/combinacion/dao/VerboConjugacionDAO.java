package com.combinacion.dao;

import com.combinacion.models.VerboConjugacion;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VerboConjugacionDAO {

    public List<VerboConjugacion> listarTodos() {
        List<VerboConjugacion> lista = new ArrayList<>();
        String sql = "SELECT id, tercera_persona, primera_persona, activo FROM verbos_conjugacion ORDER BY tercera_persona ASC";
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

    public List<VerboConjugacion> obtenerActivos() {
        List<VerboConjugacion> lista = new ArrayList<>();
        String sql = "SELECT id, tercera_persona, primera_persona, activo FROM verbos_conjugacion WHERE activo = true ORDER BY tercera_persona ASC";
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

    public VerboConjugacion obtenerPorId(int id) {
        String sql = "SELECT id, tercera_persona, primera_persona, activo FROM verbos_conjugacion WHERE id = ?";
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

    public boolean insertar(VerboConjugacion verbo) {
        String sql = "INSERT INTO verbos_conjugacion (tercera_persona, primera_persona, activo) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, verbo.getTerceraPersona().trim());
            ps.setString(2, verbo.getPrimeraPersona().trim());
            ps.setBoolean(3, verbo.isActivo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean actualizar(VerboConjugacion verbo) {
        String sql = "UPDATE verbos_conjugacion SET tercera_persona = ?, primera_persona = ?, activo = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, verbo.getTerceraPersona().trim());
            ps.setString(2, verbo.getPrimeraPersona().trim());
            ps.setBoolean(3, verbo.isActivo());
            ps.setInt(4, verbo.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM verbos_conjugacion WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private VerboConjugacion mapear(ResultSet rs) throws SQLException {
        return new VerboConjugacion(
            rs.getInt("id"),
            rs.getString("tercera_persona"),
            rs.getString("primera_persona"),
            rs.getBoolean("activo")
        );
    }
}
