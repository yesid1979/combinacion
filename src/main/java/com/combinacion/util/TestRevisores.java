package com.combinacion.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestRevisores {
    public static void main(String[] args) {
        String sql = "SELECT DISTINCT u.id, u.username, u.nombre_completo "
                   + "FROM usuarios u "
                   + "LEFT JOIN roles r ON u.rol_id = r.id "
                   + "LEFT JOIN usuario_permisos up ON u.id = up.usuario_id "
                   + "LEFT JOIN rol_permisos rp ON r.id = rp.rol_id "
                   + "LEFT JOIN permisos p1 ON up.permiso_id = p1.id "
                   + "LEFT JOIN permisos p2 ON rp.permiso_id = p2.id "
                   + "WHERE (p1.nombre = 'PUEDE_REVISAR_CUENTAS' OR p2.nombre = 'PUEDE_REVISAR_CUENTAS') AND u.activo = true";
                   
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            System.out.println("---- INICIO RESULTADOS ----");
            int count = 0;
            while (rs.next()) {
                System.out.println("Revisor encontrado: " + rs.getInt("id") + " - " + rs.getString("username") + " - " + rs.getString("nombre_completo"));
                count++;
            }
            System.out.println("Total revisores encontrados: " + count);
            System.out.println("---- FIN RESULTADOS ----");
            
            // Also list all permissions to see if PUEDE_REVISAR_CUENTAS exists
            System.out.println("Verificando existencia del permiso...");
            String sqlPerm = "SELECT id, codigo, nombre FROM permisos WHERE nombre = 'PUEDE_REVISAR_CUENTAS'";
            try (PreparedStatement ps2 = conn.prepareStatement(sqlPerm); ResultSet rs2 = ps2.executeQuery()) {
                if (rs2.next()) {
                    System.out.println("Permiso EXISTE con ID: " + rs2.getInt("id") + " y codigo: " + rs2.getString("codigo"));
                } else {
                    System.out.println("El permiso PUEDE_REVISAR_CUENTAS NO EXISTE en la base de datos.");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
