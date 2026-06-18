package com.combinacion.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBPatch {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://10.30.80.53:5432/combinacion?options=-c%20client_encoding=UTF8";
        String user = "adminjuridica";
        String pass = "Produccion2023*";

        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("ALTER TABLE contratos ADD COLUMN iva_si_no VARCHAR(10)");
            System.out.println("Columna iva_si_no agregada con éxito.");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
