package com.combinacion.services;

import com.combinacion.dao.PresupuestoDetalleDAO;
import com.combinacion.models.PresupuestoDetalle;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Capa de servicio para la entidad PresupuestoDetalle.
 * Contiene toda la lógica de negocio extraída del PresupuestoServlet.
 */
public class PresupuestoService {

    private final PresupuestoDetalleDAO presupuestoDAO = new PresupuestoDetalleDAO();

    /**
     * Obtiene un presupuesto por su ID.
     */
    public PresupuestoDetalle obtenerPorId(int id) {
        return presupuestoDAO.obtenerPorId(id);
    }

    /**
     * Lista todos los presupuestos.
     */
    public List<PresupuestoDetalle> listar() {
        return presupuestoDAO.listar();
    }

    /**
     * Genera la estructura de datos lista para ser serializada a JSON por DataTables.
     * Centraliza la transformación de PresupuestoDetalle → arreglo de columnas.
     */
    public Object[][] generarDataParaTabla() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        List<PresupuestoDetalle> lista = presupuestoDAO.listar();

        Object[][] data = new Object[lista.size()][9];
        for (int i = 0; i < lista.size(); i++) {
            PresupuestoDetalle p = lista.get(i);
            data[i][0] = p.getCdpNumero();
            data[i][1] = p.getCdpFecha()        != null ? sdf.format(p.getCdpFecha())        : "";
            data[i][2] = p.getCdpVencimiento()  != null ? sdf.format(p.getCdpVencimiento())  : "";
            data[i][3] = p.getRpNumero()         != null ? p.getRpNumero()                    : "-";
            data[i][4] = p.getRpFecha()          != null ? sdf.format(p.getRpFecha())         : "-";
            data[i][5] = p.getApropiacionPresupuestal();
            data[i][6] = p.getFichaEbiNombre()   != null ? p.getFichaEbiNombre()              : "N/A";
            data[i][7] = String.format("%,.2f", p.getCdpValor());
            data[i][8] = p.getId();
        }
        return data;
    }
}
