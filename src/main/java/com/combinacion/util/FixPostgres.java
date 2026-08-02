package com.combinacion.util;
import java.sql.Connection;
import java.sql.Statement;
public class FixPostgres {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            int rows = stmt.executeUpdate("UPDATE informes_supervision SET estado_radicacion = 'BORRADOR' WHERE numero_cuota = '6' AND contrato_id = 2055");
            System.out.println("Updated rows: " + rows);
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
