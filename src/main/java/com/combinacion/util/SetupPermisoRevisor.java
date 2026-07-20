package com.combinacion.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SetupPermisoRevisor {
    public static void main(String[] args) {
        String insertPermiso = "INSERT INTO permisos (nombre, descripcion) VALUES ('PUEDE_REVISAR_CUENTAS', 'Permite a un usuario ser seleccionado como revisor de cuentas de cobro') ON CONFLICT DO NOTHING";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(insertPermiso);
            System.out.println("Permiso PUEDE_REVISAR_CUENTAS verificado/creado correctamente.");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
