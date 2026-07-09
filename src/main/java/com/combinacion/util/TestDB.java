package com.combinacion.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, numero_cuota, consecutivo_cobro FROM informes_supervision")) {
            while(rs.next()) {
                System.out.println("ID: " + rs.getInt(1) + ", Cuota: " + rs.getString(2) + ", Consecutivo: " + rs.getString(3));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
