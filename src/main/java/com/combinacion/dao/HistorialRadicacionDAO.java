package com.combinacion.dao;

import com.combinacion.models.HistorialRadicacion;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistorialRadicacionDAO {

    public boolean registrarCambio(HistorialRadicacion historial) {
        String sql = "INSERT INTO historial_radicacion (id_informe, id_usuario_cambio, estado_anterior, estado_nuevo, observaciones) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, historial.getIdInforme());
            ps.setInt(2, historial.getIdUsuarioCambio());
            ps.setString(3, historial.getEstadoAnterior());
            ps.setString(4, historial.getEstadoNuevo());
            ps.setString(5, historial.getObservaciones());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<HistorialRadicacion> listarPorInforme(int idInforme) {
        List<HistorialRadicacion> lista = new ArrayList<>();
        String sql = "SELECT h.*, u.nombre_completo as nombre_usuario " +
                     "FROM historial_radicacion h " +
                     "LEFT JOIN usuarios u ON h.id_usuario_cambio = u.id " +
                     "WHERE h.id_informe = ? " +
                     "ORDER BY h.fecha_cambio DESC";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, idInforme);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialRadicacion h = new HistorialRadicacion();
                    h.setId(rs.getInt("id"));
                    h.setIdInforme(rs.getInt("id_informe"));
                    h.setIdUsuarioCambio(rs.getInt("id_usuario_cambio"));
                    h.setEstadoAnterior(rs.getString("estado_anterior"));
                    h.setEstadoNuevo(rs.getString("estado_nuevo"));
                    h.setObservaciones(rs.getString("observaciones"));
                    h.setFechaCambio(rs.getTimestamp("fecha_cambio"));
                    h.setNombreUsuarioCambio(rs.getString("nombre_usuario"));
                    lista.add(h);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
