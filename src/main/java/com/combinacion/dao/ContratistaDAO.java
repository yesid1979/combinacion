package com.combinacion.dao;

import com.combinacion.models.Contratista;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
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
                    c.setDv(rs.getString("dv"));
                    c.setNombre(rs.getString("nombre"));
                    c.setTelefono(rs.getString("telefono"));
                    c.setCorreo(rs.getString("correo"));
                    c.setDireccion(rs.getString("direccion"));
                    c.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                    c.setEdad(rs.getInt("edad"));
                    c.setFormacionTitulo(rs.getString("formacion_titulo"));
                    c.setDescripcionFormacion(rs.getString("descripcion_formacion"));
                    c.setTarjetaProfesional(rs.getString("tarjeta_profesional"));
                    c.setDescripcionTarjeta(rs.getString("descripcion_tarjeta"));
                    c.setExperiencia(rs.getString("experiencia"));
                    c.setDescripcionExperiencia(rs.getString("descripcion_experiencia"));
                    c.setRestricciones(rs.getString("restricciones"));
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
                    c.setDv(rs.getString("dv"));
                    c.setNombre(rs.getString("nombre"));
                    c.setTelefono(rs.getString("telefono"));
                    c.setCorreo(rs.getString("correo"));
                    c.setDireccion(rs.getString("direccion"));
                    c.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                    c.setEdad(rs.getInt("edad"));
                    c.setFormacionTitulo(rs.getString("formacion_titulo"));
                    c.setDescripcionFormacion(rs.getString("descripcion_formacion"));
                    c.setTarjetaProfesional(rs.getString("tarjeta_profesional"));
                    c.setDescripcionTarjeta(rs.getString("descripcion_tarjeta"));
                    c.setExperiencia(rs.getString("experiencia"));
                    c.setDescripcionExperiencia(rs.getString("descripcion_experiencia"));
                    c.setRestricciones(rs.getString("restricciones"));
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM contratistas";
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
        String sql = "SELECT COUNT(*) FROM contratistas WHERE 1=1 ";
        if (search != null && !search.isEmpty()) {
            search = removeAccents(search).toLowerCase();
            sql += " AND (translate(LOWER(cedula), 'áéíóú', 'aeiou') LIKE ? "
                    + " OR translate(LOWER(nombre), 'áéíóú', 'aeiou') LIKE ? "
                    + " OR translate(LOWER(correo), 'áéíóú', 'aeiou') LIKE ?)";
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

    public List<Contratista> findWithPagination(int start, int length, String search, String sortCol, String orderDir) {
        List<Contratista> lista = new ArrayList<>();
        // Fetch all needed columns plus latest contract number
        String sql = "SELECT id, cedula, nombre, correo, telefono, direccion, fecha_nacimiento, " +
                "(SELECT numero_contrato FROM contratos WHERE contratista_id = contratistas.id ORDER BY fecha_inicio DESC LIMIT 1) as numero_contrato "
                +
                "FROM contratistas WHERE 1=1 ";

        if (search != null && !search.isEmpty()) {
            // Normalizamos la busqueda en Java (quitamos tildes al input)
            search = removeAccents(search).toLowerCase();

            // En SQL usamos translate para quitar tildes a las columnas antes de comparar
            // PostgreSQL specific: translate(LOWER(col), 'áéíóú', 'aeiou')
            sql += " AND (translate(LOWER(cedula), 'áéíóú', 'aeiou') LIKE ? "
                    + " OR translate(LOWER(nombre), 'áéíóú', 'aeiou') LIKE ? "
                    + " OR translate(LOWER(correo), 'áéíóú', 'aeiou') LIKE ?)";
        }

        // Validate sortCol to prevent SQL injection
        List<String> allowedCols = Arrays.asList("id", "cedula", "nombre", "correo", "telefono", "numero_contrato");
        if (sortCol == null || !allowedCols.contains(sortCol)) {
            sortCol = "nombre";
        }

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
                    Contratista c = new Contratista();
                    c.setId(rs.getInt("id"));
                    c.setCedula(rs.getString("cedula"));
                    c.setNombre(rs.getString("nombre"));
                    c.setCorreo(rs.getString("correo"));
                    c.setTelefono(rs.getString("telefono"));
                    c.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                    c.setNumeroContrato(rs.getString("numero_contrato"));
                    lista.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private String removeAccents(String input) {
        if (input == null)
            return null;
        return java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    public boolean actualizar(Contratista c) {
        String sql = "UPDATE contratistas SET cedula=?, dv=?, nombre=?, telefono=?, correo=?, direccion=?, fecha_nacimiento=?, edad=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCedula());
            ps.setString(2, c.getDv());
            ps.setString(3, c.getNombre());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getCorreo());
            ps.setString(6, c.getDireccion());
            ps.setDate(7, c.getFechaNacimiento());
            ps.setInt(8, c.getEdad());
            ps.setInt(9, c.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM contratistas WHERE id = ?";
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
        String sql = "SELECT cedula FROM contratistas WHERE cedula IS NOT NULL";
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
