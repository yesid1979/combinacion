package com.combinacion.util;

import com.combinacion.dao.InformeSupervisionDAO;
import com.combinacion.dao.ContratoDAO;
import com.combinacion.models.InformeSupervision;
import com.combinacion.models.Contrato;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class FixMissingHistorical {
    public static void main(String[] args) {
        try {
            InformeSupervisionDAO informeDAO = new InformeSupervisionDAO();
            ContratoDAO contratoDAO = new ContratoDAO();
            List<InformeSupervision> todos = informeDAO.listarTodos();
            
            int fixedCount = 0;
            Connection conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("UPDATE informes_supervision SET valor_acumulado_pagado = ?, saldo_por_cancelar = ? WHERE id = ?");

            for (InformeSupervision info : todos) {
                if (info.getContratoId() == null || info.getNumeroCuota() == null) continue;
                int currentCuota = ParseUtils.parseInt(info.getNumeroCuota());
                if (currentCuota <= 1) continue;

                Contrato contrato = contratoDAO.obtenerPorId(info.getContratoId());
                if (contrato != null) {
                    BigDecimal total = contrato.getValorTotalNumeros();
                    int cuotasNormales = contrato.getNumCuotasNumero();
                    if (cuotasNormales > 0 && currentCuota >= cuotasNormales && contrato.getValorTotalAdicion() != null && contrato.getValorTotalAdicion().compareTo(BigDecimal.ZERO) > 0) {
                        total = contrato.getValorTotalAdicion();
                    }
                    
                    if (total != null) {
                        BigDecimal cuota = info.getValorCuotaPagar() != null ? info.getValorCuotaPagar() : BigDecimal.ZERO;
                        
                        // INFER MISSING ACUMULADO
                        BigDecimal acumulado = cuota.multiply(new BigDecimal(currentCuota - 1));
                        
                        BigDecimal saldo = total.subtract(acumulado).subtract(cuota);
                        if (saldo.compareTo(BigDecimal.ZERO) < 0) saldo = BigDecimal.ZERO;
                        
                        boolean changed = false;
                        if (info.getValorAccumuladoPagado() == null || info.getValorAccumuladoPagado().compareTo(acumulado) != 0) {
                            changed = true;
                        }
                        if (info.getSaldoPorCancelar() == null || info.getSaldoPorCancelar().compareTo(saldo) != 0) {
                            changed = true;
                        }
                        
                        if (changed) {
                            ps.setBigDecimal(1, acumulado);
                            ps.setBigDecimal(2, saldo);
                            ps.setInt(3, info.getId());
                            ps.addBatch();
                            fixedCount++;
                        }
                    }
                }
            }
            
            int[] results = ps.executeBatch();
            conn.commit();
            conn.close();
            System.out.println("Se corrigieron/estimaron automaticamente " + fixedCount + " informes historicos en la base de datos.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
