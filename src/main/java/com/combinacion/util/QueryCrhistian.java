package com.combinacion.util;

import com.combinacion.dao.InformeSupervisionDAO;
import com.combinacion.models.InformeSupervision;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QueryCrhistian {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT c.id as contrato_id, ct.nombre, i.id as informe_id, i.numero_cuota, i.valor_cuota_pagar, i.valor_acumulado_pagado, i.saldo_por_cancelar FROM informes_supervision i JOIN contratos c ON i.contrato_id = c.id JOIN contratistas ct ON c.contratista_id = ct.id WHERE ct.nombre ILIKE '%CRHISTIAN CAMILO%'")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println("ID=" + rs.getInt("informe_id") + 
                    ", Cuota=" + rs.getString("numero_cuota") + 
                    ", CuotaPagar=" + rs.getBigDecimal("valor_cuota_pagar") + 
                    ", Acum=" + rs.getBigDecimal("valor_acumulado_pagado") + 
                    ", Saldo=" + rs.getBigDecimal("saldo_por_cancelar"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
