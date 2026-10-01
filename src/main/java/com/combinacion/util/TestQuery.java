package com.combinacion.util;
public class TestQuery {
    public static void main(String[] args) {
        System.out.println("TESTING DAO ORDER:");
        com.combinacion.dao.InformeSupervisionDAO dao = new com.combinacion.dao.InformeSupervisionDAO();
        java.util.List<com.combinacion.models.InformeSupervision> list = dao.listarTodos();
        for (int i = 0; i < Math.min(10, list.size()); i++) {
            com.combinacion.models.InformeSupervision info = list.get(i);
            System.out.println("ID: " + info.getId() + " - Estado: " + info.getEstadoRadicacion() + " - Fecha: " + info.getFechaCreacion());
        }
    }
}
