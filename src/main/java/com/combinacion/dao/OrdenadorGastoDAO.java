package com.combinacion.dao;

import com.combinacion.models.OrdenadorGasto;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdenadorGastoDAO {

    public boolean insertar(OrdenadorGasto o) {
        String sql = "INSERT INTO ordenadores_gasto (organismo, direccion_organismo, nombre_ordenador, cedula_ordenador, cargo_ordenador, decreto_nombramiento, acta_posesion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, o.getOrganismo());
            ps.setString(2, o.getDireccionOrganismo());
            ps.setString(3, o.getNombreOrdenador());
            ps.setString(4, o.getCedulaOrdenador());
            ps.setString(5, o.getCargoOrdenador());
            ps.setString(6, o.getDecretoNombramiento());
            ps.setString(7, o.getActaPosesion());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        o.setId(generatedKeys.getInt(1));
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

    public List<OrdenadorGasto> listarTodos() {
        List<OrdenadorGasto> lista = new ArrayList<>();
        String sql = "SELECT * FROM ordenadores_gasto";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OrdenadorGasto o = new OrdenadorGasto();
                o.setId(rs.getInt("id"));
                o.setOrganismo(rs.getString("organismo"));
                o.setDireccionOrganismo(rs.getString("direccion_organismo"));
                o.setNombreOrdenador(rs.getString("nombre_ordenador"));
                o.setCedulaOrdenador(rs.getString("cedula_ordenador"));
                o.setCargoOrdenador(rs.getString("cargo_ordenador"));
                o.setDecretoNombramiento(rs.getString("decreto_nombramiento"));
                o.setActaPosesion(rs.getString("acta_posesion"));
                lista.add(o);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public OrdenadorGasto obtenerPorNombre(String nombre) {
        String sql = "SELECT * FROM ordenadores_gasto WHERE nombre_ordenador = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OrdenadorGasto o = new OrdenadorGasto();
                    o.setId(rs.getInt("id"));
                    o.setNombreOrdenador(rs.getString("nombre_ordenador"));
                    return o;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
