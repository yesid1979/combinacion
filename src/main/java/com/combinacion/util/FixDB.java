package com.combinacion.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class FixDB {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/combinacion?useSSL=false&serverTimezone=America/Bogota", "root", "");
            Statement stmt = conn.createStatement();
            int rows = stmt.executeUpdate("UPDATE informes_supervision SET estado_radicacion = 'BORRADOR' WHERE numero_cuota = '6' AND contrato_id = 2055");
            System.out.println("Updated rows: " + rows);
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
