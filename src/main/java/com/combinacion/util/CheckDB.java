package com.combinacion.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDB {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Try to add the column just in case it's completely missing
            try {
                stmt.execute("ALTER TABLE informes_supervision ADD COLUMN IF NOT EXISTS consecutivo_cobro VARCHAR(50)");
                System.out.println("Columna verificada/creada.");
            } catch (Exception e) {
                System.out.println("Error creando columna: " + e.getMessage());
            }

            ResultSet rs = stmt.executeQuery("SELECT id, consecutivo_cobro, numero_cuota FROM informes_supervision ORDER BY id DESC LIMIT 5");
            while(rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + " | Consecutivo: " + rs.getString("consecutivo_cobro") + " | Cuota: " + rs.getString("numero_cuota"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
