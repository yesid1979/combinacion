package com.combinacion.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InsertPermiso {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            String check = "SELECT count(*) FROM permisos WHERE nombre = 'PUEDE_REVISAR_CUENTAS'";
            try (PreparedStatement ps = conn.prepareStatement(check);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insert = "INSERT INTO permisos (codigo, nombre, modulo, descripcion) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insert)) {
                        psIns.setString(1, "REVISAR_CTA");
                        psIns.setString(2, "PUEDE_REVISAR_CUENTAS");
                        psIns.setString(3, "RADICACION");
                        psIns.setString(4, "Permite ser asignado como revisor de cuentas de cobro radicadas.");
                        psIns.executeUpdate();
                        System.out.println("Permiso PUEDE_REVISAR_CUENTAS insertado correctamente.");
                    }
                } else {
                    System.out.println("El permiso ya existe.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
