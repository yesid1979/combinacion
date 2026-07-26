package com.combinacion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MainDatabaseViewer {
    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://10.30.80.53:5432/combinacion?options=-c%20client_encoding=UTF8";
            try (Connection conn = DriverManager.getConnection(url, "adminjuridica", "Produccion2023*")) {
                System.out.println("--- BUSCANDO CONTRATISTA ID 2146 ---");
                try (PreparedStatement stmt = conn.prepareStatement("SELECT id, nombre, cedula FROM contratistas WHERE id = 2146");
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("ID: " + rs.getInt("id") + " | Nombre: " + rs.getString("nombre") + " | Cédula: " + rs.getString("cedula"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
