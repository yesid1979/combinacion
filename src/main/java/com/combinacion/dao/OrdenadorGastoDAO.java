package com.combinacion.dao;

import com.combinacion.models.OrdenadorGasto;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdenadorGastoDAO {

    public boolean insertar(OrdenadorGasto o) {
        String sql = "INSERT INTO ordenadores_gasto (organismo, direccion_organismo, nombre_ordenador, cedula_ordenador, cargo_ordenador, decreto_nombramiento, acta_posesion, juridico_nombre, juridico_cargo, tecnico_nombre, tecnico_cargo, financiero_nombre, financiero_cargo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, o.getOrganismo());
            ps.setString(2, o.getDireccionOrganismo());
            ps.setString(3, o.getNombreOrdenador());
            ps.setString(4, o.getCedulaOrdenador());
            ps.setString(5, o.getCargoOrdenador());
            ps.setString(6, o.getDecretoNombramiento());
            ps.setString(7, o.getActaPosesion());
            ps.setString(8, o.getJuridicoNombre());
            ps.setString(9, o.getJuridicoCargo());
            ps.setString(10, o.getTecnicoNombre());
            ps.setString(11, o.getTecnicoCargo());
            ps.setString(12, o.getFinancieroNombre());
            ps.setString(13, o.getFinancieroCargo());

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
                o.setJuridicoNombre(rs.getString("juridico_nombre"));
                o.setJuridicoCargo(rs.getString("juridico_cargo"));
                o.setTecnicoNombre(rs.getString("tecnico_nombre"));
                o.setTecnicoCargo(rs.getString("tecnico_cargo"));
                o.setFinancieroNombre(rs.getString("financiero_nombre"));
                o.setFinancieroCargo(rs.getString("financiero_cargo"));
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

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM ordenadores_gasto";
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
        String sql = "SELECT COUNT(*) FROM ordenadores_gasto WHERE 1=1 ";
        if (search != null && !search.isEmpty()) {
            sql += " AND (nombre_ordenador LIKE ? OR cargo_ordenador LIKE ? OR organismo LIKE ?)";
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

    public List<OrdenadorGasto> findWithPagination(int start, int length, String search, int orderCol,
            String orderDir) {
        List<OrdenadorGasto> lista = new ArrayList<>();
        String sql = "SELECT * FROM ordenadores_gasto WHERE 1=1 ";

        if (search != null && !search.isEmpty()) {
            sql += " AND (nombre_ordenador LIKE ? OR cargo_ordenador LIKE ? OR organismo LIKE ?)";
        }

        String[] cols = { "nombre_ordenador", "cargo_ordenador", "organismo" };
        String sortCol = (orderCol >= 0 && orderCol < cols.length) ? cols[orderCol] : "nombre_ordenador";

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
                    OrdenadorGasto o = new OrdenadorGasto();
                    o.setId(rs.getInt("id"));
                    o.setOrganismo(rs.getString("organismo"));
                    o.setDireccionOrganismo(rs.getString("direccion_organismo"));
                    o.setNombreOrdenador(rs.getString("nombre_ordenador"));
                    o.setCedulaOrdenador(rs.getString("cedula_ordenador"));
                    o.setCargoOrdenador(rs.getString("cargo_ordenador"));
                    // ... other fields if needed
                    lista.add(o);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public OrdenadorGasto obtenerPorId(int id) {
        String sql = "SELECT * FROM ordenadores_gasto WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OrdenadorGasto o = new OrdenadorGasto();
                    o.setId(rs.getInt("id"));
                    o.setOrganismo(rs.getString("organismo"));
                    o.setDireccionOrganismo(rs.getString("direccion_organismo"));
                    o.setNombreOrdenador(rs.getString("nombre_ordenador"));
                    o.setCedulaOrdenador(rs.getString("cedula_ordenador"));
                    o.setCargoOrdenador(rs.getString("cargo_ordenador"));
                    o.setDecretoNombramiento(rs.getString("decreto_nombramiento"));
                    o.setActaPosesion(rs.getString("acta_posesion"));
                    o.setJuridicoNombre(rs.getString("juridico_nombre"));
                    o.setJuridicoCargo(rs.getString("juridico_cargo"));
                    o.setTecnicoNombre(rs.getString("tecnico_nombre"));
                    o.setTecnicoCargo(rs.getString("tecnico_cargo"));
                    o.setFinancieroNombre(rs.getString("financiero_nombre"));
                    o.setFinancieroCargo(rs.getString("financiero_cargo"));
                    return o;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean actualizar(OrdenadorGasto o) {
        String sql = "UPDATE ordenadores_gasto SET organismo=?, direccion_organismo=?, nombre_ordenador=?, cedula_ordenador=?, cargo_ordenador=?, decreto_nombramiento=?, acta_posesion=?, juridico_nombre=?, juridico_cargo=?, tecnico_nombre=?, tecnico_cargo=?, financiero_nombre=?, financiero_cargo=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, o.getOrganismo());
            ps.setString(2, o.getDireccionOrganismo());
            ps.setString(3, o.getNombreOrdenador());
            ps.setString(4, o.getCedulaOrdenador());
            ps.setString(5, o.getCargoOrdenador());
            ps.setString(6, o.getDecretoNombramiento());
            ps.setString(7, o.getActaPosesion());
            ps.setString(8, o.getJuridicoNombre());
            ps.setString(9, o.getJuridicoCargo());
            ps.setString(10, o.getTecnicoNombre());
            ps.setString(11, o.getTecnicoCargo());
            ps.setString(12, o.getFinancieroNombre());
            ps.setString(13, o.getFinancieroCargo());
            ps.setInt(14, o.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM ordenadores_gasto WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public OrdenadorGasto obtenerPorCedula(String cedula) {
        String sql = "SELECT * FROM ordenadores_gasto WHERE cedula_ordenador = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OrdenadorGasto o = new OrdenadorGasto();
                    o.setId(rs.getInt("id"));
                    o.setCedulaOrdenador(rs.getString("cedula_ordenador"));
                    o.setNombreOrdenador(rs.getString("nombre_ordenador"));
                    return o;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.util.Set<String> obtenerTodasLasCedulas() {
        java.util.Set<String> cedulas = new java.util.HashSet<>();
        String sql = "SELECT cedula_ordenador FROM ordenadores_gasto WHERE cedula_ordenador IS NOT NULL";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cedulas.add(rs.getString("cedula_ordenador"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cedulas;
    }
}
