package com.combinacion.dao;

import com.combinacion.models.Estructurador;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstructuradorDAO {

    // Insercion de estructurador
    public boolean insertar(Estructurador e) {
        String sql = "INSERT INTO estructuradores (juridico_nombre, juridico_cargo, tecnico_nombre, tecnico_cargo, financiero_nombre, financiero_cargo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getJuridicoNombre());
            ps.setString(2, e.getJuridicoCargo());
            ps.setString(3, e.getTecnicoNombre());
            ps.setString(4, e.getTecnicoCargo());
            ps.setString(5, e.getFinancieroNombre());
            ps.setString(6, e.getFinancieroCargo());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        e.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public List<Estructurador> listar() {
        List<Estructurador> lista = new ArrayList<>();
        String sql = "SELECT * FROM estructuradores";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Estructurador e = new Estructurador();
                e.setId(rs.getInt("id"));
                e.setJuridicoNombre(rs.getString("juridico_nombre"));
                e.setJuridicoCargo(rs.getString("juridico_cargo"));
                e.setTecnicoNombre(rs.getString("tecnico_nombre"));
                e.setTecnicoCargo(rs.getString("tecnico_cargo"));
                e.setFinancieroNombre(rs.getString("financiero_nombre"));
                e.setFinancieroCargo(rs.getString("financiero_cargo"));
                lista.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lista;
    }

    public Estructurador obtenerPorId(int id) {
        String sql = "SELECT * FROM estructuradores WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Estructurador e = new Estructurador();
                    e.setId(rs.getInt("id"));
                    e.setJuridicoNombre(rs.getString("juridico_nombre"));
                    e.setJuridicoCargo(rs.getString("juridico_cargo"));
                    e.setTecnicoNombre(rs.getString("tecnico_nombre"));
                    e.setTecnicoCargo(rs.getString("tecnico_cargo"));
                    e.setFinancieroNombre(rs.getString("financiero_nombre"));
                    e.setFinancieroCargo(rs.getString("financiero_cargo"));
                    return e;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean actualizar(Estructurador e) {
        String sql = "UPDATE estructuradores SET juridico_nombre=?, juridico_cargo=?, tecnico_nombre=?, tecnico_cargo=?, financiero_nombre=?, financiero_cargo=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getJuridicoNombre());
            ps.setString(2, e.getJuridicoCargo());
            ps.setString(3, e.getTecnicoNombre());
            ps.setString(4, e.getTecnicoCargo());
            ps.setString(5, e.getFinancieroNombre());
            ps.setString(6, e.getFinancieroCargo());
            ps.setInt(7, e.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public Estructurador obtenerExistente(String jNom, String jCar, String tNom, String tCar, String fNom,
            String fCar) {
        String sql = "SELECT * FROM estructuradores WHERE "
                + "juridico_nombre = ? AND juridico_cargo = ? AND "
                + "tecnico_nombre = ? AND tecnico_cargo = ? AND "
                + "financiero_nombre = ? AND financiero_cargo = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, jNom != null ? jNom : "");
            ps.setString(2, jCar != null ? jCar : "");
            ps.setString(3, tNom != null ? tNom : "");
            ps.setString(4, tCar != null ? tCar : "");
            ps.setString(5, fNom != null ? fNom : "");
            ps.setString(6, fCar != null ? fCar : "");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Estructurador e = new Estructurador();
                    e.setId(rs.getInt("id"));
                    e.setJuridicoNombre(rs.getString("juridico_nombre"));
                    e.setJuridicoCargo(rs.getString("juridico_cargo"));
                    e.setTecnicoNombre(rs.getString("tecnico_nombre"));
                    e.setTecnicoCargo(rs.getString("tecnico_cargo"));
                    e.setFinancieroNombre(rs.getString("financiero_nombre"));
                    e.setFinancieroCargo(rs.getString("financiero_cargo"));
                    return e;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
