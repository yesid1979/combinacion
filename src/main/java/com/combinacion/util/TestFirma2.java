package com.combinacion.util;

import com.combinacion.dao.UsuarioDAO;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestFirma2 {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, cedula, firma_url FROM usuarios WHERE firma_url IS NOT NULL LIMIT 1")) {
            if (rs.next()) {
                String ced = rs.getString("cedula");
                String url = rs.getString("firma_url");
                System.out.println("En BD: cedula='" + ced + "', url='" + url + "'");
                
                // Probar obtenerFirmaPorCedula
                UsuarioDAO dao = new UsuarioDAO();
                String res1 = dao.obtenerFirmaPorCedula(ced);
                System.out.println("obtenerFirmaPorCedula(ced) = " + res1);
                
                String withDots = ced.substring(0, 1) + "." + ced.substring(1, 4) + "." + ced.substring(4);
                System.out.println("Probando con cedula formateada: " + withDots);
                String res2 = dao.obtenerFirmaPorCedula(withDots);
                System.out.println("obtenerFirmaPorCedula(withDots) = " + res2);
            } else {
                System.out.println("No hay firmas en la BD.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
