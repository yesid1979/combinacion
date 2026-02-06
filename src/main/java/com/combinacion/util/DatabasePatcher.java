package com.combinacion.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabasePatcher {

    public static void ensureSchema() {
        System.out.println("Verificando esquema de base de datos...");
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Verificamos si la columna existe intentando seleccionarla (manera 'lazy' pero
            // efectiva para compatibilidad)
            // O mejor, consultamos information_schema

            ResultSet rs = stmt.executeQuery(
                    "SELECT column_name FROM information_schema.columns " +
                            "WHERE table_name='contratos' AND column_name='apoyo_supervision'");

            if (!rs.next()) {
                System.out.println("⚠️ Columna 'apoyo_supervision' no encontrada. Agregándola automáticamente...");
                stmt.executeUpdate("ALTER TABLE contratos ADD COLUMN apoyo_supervision TEXT");
                System.out.println("✅ Columna 'apoyo_supervision' agregada exitosamente.");
            } else {
                System.out.println("✅ La columna 'apoyo_supervision' ya existe.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
