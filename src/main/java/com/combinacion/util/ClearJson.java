package com.combinacion.util;
import java.sql.*;

public class ClearJson {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE informes_supervision SET soportes_json = NULL WHERE numero_cuota = '4' AND id_contrato IN (SELECT id FROM contratos WHERE short_contrato LIKE '%035%')")) {
             int updated = ps.executeUpdate();
             System.out.println("OK: " + updated + " records updated.");
        } catch(Exception e) {
             e.printStackTrace();
        }
    }
}
