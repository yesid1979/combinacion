package com.combinacion.test;
import java.sql.*;
public class CheckDB {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://10.30.80.53:5432/combinacion?options=-c%20client_encoding=UTF8";
        String user = "adminjuridica";
        String pass = "Produccion2023*";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT column_name, character_maximum_length FROM information_schema.columns WHERE table_name = 'usuarios'");
            while (rs.next()) {
                System.out.println("Col: " + rs.getString("column_name") + " - " + rs.getInt("character_maximum_length"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
