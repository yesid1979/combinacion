package com.combinacion.dao;

import com.combinacion.models.Contratista;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContratistaDAO {

    public boolean insertar(Contratista c) {
        String sql = "INSERT INTO contratistas (cedula, dv, nombre, telefono, correo, direccion, fecha_nacimiento, edad, "
                +
                "formacion_titulo, descripcion_formacion, tarjeta_profesional, descripcion_tarjeta, experiencia, " +
                "descripcion_experiencia, restricciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getCedula());
            ps.setString(2, c.getDv());
            ps.setString(3, c.getNombre());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getCorreo());
            ps.setString(6, c.getDireccion());
            ps.setDate(7, c.getFechaNacimiento());
            ps.setInt(8, c.getEdad());
            ps.setString(9, c.getFormacionTitulo());
            ps.setString(10, c.getDescripcionFormacion());
            ps.setString(11, c.getTarjetaProfesional());
            ps.setString(12, c.getDescripcionTarjeta());
            ps.setString(13, c.getExperiencia());
            ps.setString(14, c.getDescripcionExperiencia());
            ps.setString(15, c.getRestricciones());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        c.setId(generatedKeys.getInt(1));
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

    public List<Contratista> listarTodos() {
        List<Contratista> lista = new ArrayList<>();
        String sql = "SELECT * FROM contratistas ORDER BY nombre";

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Contratista c = new Contratista();
                c.setId(rs.getInt("id"));
                c.setCedula(rs.getString("cedula"));
                c.setDv(rs.getString("dv"));
                c.setNombre(rs.getString("nombre"));
                c.setTelefono(rs.getString("telefono"));
                c.setCorreo(rs.getString("correo"));
                c.setDireccion(rs.getString("direccion"));
                c.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                c.setEdad(rs.getInt("edad"));
                c.setFormacionTitulo(rs.getString("formacion_titulo"));
                // ... map other fields if needed for list view, or map all for detail view
                lista.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Contratista obtenerPorId(int id) {
        String sql = "SELECT * FROM contratistas WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Contratista c = new Contratista();
                    c.setId(rs.getInt("id"));
                    c.setCedula(rs.getString("cedula"));
                    c.setNombre(rs.getString("nombre"));
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Contratista obtenerPorCedula(String cedula) {
        String sql = "SELECT * FROM contratistas WHERE cedula = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Contratista c = new Contratista();
                    c.setId(rs.getInt("id"));
                    c.setCedula(rs.getString("cedula"));
                    c.setNombre(rs.getString("nombre"));
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
