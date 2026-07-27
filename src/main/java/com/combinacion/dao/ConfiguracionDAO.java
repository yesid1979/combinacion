package com.combinacion.dao;

import com.combinacion.models.Configuracion;
import com.combinacion.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfiguracionDAO {
    
    // Caché en memoria
    private static final Map<String, String> cache = new ConcurrentHashMap<>();
    private static boolean cacheCargado = false;

    // Obtener un valor rápido usando el caché
    public static String getValor(String clave, String valorPorDefecto) {
        if (!cacheCargado) {
            recargarCache();
        }
        return cache.getOrDefault(clave, valorPorDefecto);
    }

    // Recargar todas las configuraciones en memoria
    public static synchronized void recargarCache() {
        cache.clear();
        String sql = "SELECT clave, valor FROM configuracion";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cache.put(rs.getString("clave"), rs.getString("valor"));
            }
            cacheCargado = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Configuracion> listarTodos() {
        List<Configuracion> lista = new ArrayList<>();
        String sql = "SELECT * FROM configuracion ORDER BY clave";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Configuracion(
                    rs.getInt("id"),
                    rs.getString("clave"),
                    rs.getString("valor"),
                    rs.getString("descripcion")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Configuracion obtenerPorId(int id) {
        String sql = "SELECT * FROM configuracion WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Configuracion(
                        rs.getInt("id"),
                        rs.getString("clave"),
                        rs.getString("valor"),
                        rs.getString("descripcion")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertar(Configuracion c) {
        String sql = "INSERT INTO configuracion (clave, valor, descripcion) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getClave());
            ps.setString(2, c.getValor());
            ps.setString(3, c.getDescripcion());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                recargarCache();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean actualizar(Configuracion c) {
        String sql = "UPDATE configuracion SET clave=?, valor=?, descripcion=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getClave());
            ps.setString(2, c.getValor());
            ps.setString(3, c.getDescripcion());
            ps.setInt(4, c.getId());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                recargarCache();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM configuracion WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                recargarCache();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
