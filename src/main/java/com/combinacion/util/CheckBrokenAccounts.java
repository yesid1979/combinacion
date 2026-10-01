package com.combinacion.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckBrokenAccounts {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT c.id as contrato_id, ct.nombre, i.id as informe_id, i.numero_cuota, i.valor_cuota_pagar, i.valor_acumulado_pagado, i.saldo_por_cancelar FROM informes_supervision i JOIN contratos c ON i.contrato_id = c.id JOIN contratistas ct ON c.contratista_id = ct.id ORDER BY i.id")) {
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                int cuota = ParseUtils.parseInt(rs.getString("numero_cuota"));
                java.math.BigDecimal valorCuota = rs.getBigDecimal("valor_cuota_pagar");
                java.math.BigDecimal acum = rs.getBigDecimal("valor_acumulado_pagado");
                if (cuota > 1 && valorCuota != null && acum != null && acum.compareTo(java.math.BigDecimal.ZERO) == 0) {
                    System.out.println("BROKEN: " + rs.getString("nombre") + " Cuota " + cuota + ", Valor=" + valorCuota + ", Acum=" + acum);
                    count++;
                }
            }
            System.out.println("Total broken: " + count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
