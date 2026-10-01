package com.combinacion.util;
import java.sql.Connection;
import java.sql.Statement;
public class FixPostgres {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            int rows = stmt.executeUpdate("UPDATE informes_supervision SET valor_acumulado_pagado = 0, saldo_por_cancelar = 13960000 WHERE id = 59");
            System.out.println("Updated rows: " + rows);
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
