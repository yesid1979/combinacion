package com.combinacion.util;
import java.sql.*;
public class CheckMariaCamila {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT i.numero_cuota, i.valor_cuota_pagar, i.valor_acumulado_pagado, i.saldo_por_cancelar " +
                "FROM informes_supervision i " +
                "JOIN contratos c ON i.contrato_id = c.id " +
                "JOIN contratistas ct ON c.contratista_id = ct.id " +
                "WHERE ct.nombre LIKE '%MARIA CAMILA PAYAN%'"
            );
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println("Cuota: " + rs.getString("numero_cuota") + 
                    " | Pagar: " + rs.getBigDecimal("valor_cuota_pagar") +
                    " | Acumulado: " + rs.getBigDecimal("valor_acumulado_pagado") + 
                    " | Saldo: " + rs.getBigDecimal("saldo_por_cancelar"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
