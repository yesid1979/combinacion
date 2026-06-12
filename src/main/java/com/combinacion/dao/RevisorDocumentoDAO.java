package com.combinacion.dao;

import com.combinacion.models.RevisorDocumento;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RevisorDocumentoDAO {

    public List<RevisorDocumento> listarTodos() {
        List<RevisorDocumento> lista = new ArrayList<>();
        String sql = "SELECT * FROM revisores_documento ORDER BY tipo_documento";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                RevisorDocumento r = new RevisorDocumento();
                r.setId(rs.getInt("id"));
                r.setTipoDocumento(rs.getString("tipo_documento"));
                r.setNombreCompleto(rs.getString("nombre_completo"));
                r.setCargo(rs.getString("cargo"));
                r.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
                lista.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean insertar(RevisorDocumento r) {
        String sql = "INSERT INTO revisores_documento (tipo_documento, nombre_completo, cargo, fecha_actualizacion) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, r.getTipoDocumento());
            ps.setString(2, r.getNombreCompleto());
            ps.setString(3, r.getCargo());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        r.setId(generatedKeys.getInt(1));
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

    public RevisorDocumento obtenerPorId(int id) {
        String sql = "SELECT * FROM revisores_documento WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RevisorDocumento r = new RevisorDocumento();
                    r.setId(rs.getInt("id"));
                    r.setTipoDocumento(rs.getString("tipo_documento"));
                    r.setNombreCompleto(rs.getString("nombre_completo"));
                    r.setCargo(rs.getString("cargo"));
                    r.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
                    return r;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RevisorDocumento obtenerPorTipoDocumento(String tipoDocumento) {
        String sql = "SELECT * FROM revisores_documento WHERE tipo_documento LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + tipoDocumento + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RevisorDocumento r = new RevisorDocumento();
                    r.setId(rs.getInt("id"));
                    r.setTipoDocumento(rs.getString("tipo_documento"));
                    r.setNombreCompleto(rs.getString("nombre_completo"));
                    r.setCargo(rs.getString("cargo"));
                    r.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
                    return r;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean actualizar(RevisorDocumento r) {
        String sql = "UPDATE revisores_documento SET tipo_documento=?, nombre_completo=?, cargo=?, fecha_actualizacion=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getTipoDocumento());
            ps.setString(2, r.getNombreCompleto());
            ps.setString(3, r.getCargo());
            ps.setInt(4, r.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM revisores_documento WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
