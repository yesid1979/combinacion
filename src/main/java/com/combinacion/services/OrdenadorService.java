package com.combinacion.services;

import com.combinacion.dao.OrdenadorGastoDAO;
import com.combinacion.models.OrdenadorGasto;
import com.combinacion.util.JsonUtils;
import java.util.List;

/**
 * Capa de servicio para la entidad OrdenadorGasto.
 * Contiene toda la lógica de negocio extraída del OrdenadorServlet.
 */
public class OrdenadorService {

    private final OrdenadorGastoDAO ordenadorDAO = new OrdenadorGastoDAO();

    /**
     * Inserta un nuevo ordenador del gasto.
     * @return null si fue exitoso, o mensaje de error si falló.
     */
    public String insertar(OrdenadorGasto o) {
        if (!ordenadorDAO.insertar(o)) {
            return "Error al guardar el ordenador.";
        }
        return null;
    }

    /**
     * Actualiza un ordenador existente.
     * @return null si fue exitoso, o mensaje de error si falló.
     */
    public String actualizar(OrdenadorGasto o) {
        if (!ordenadorDAO.actualizar(o)) {
            return "Error al actualizar el ordenador.";
        }
        return null;
    }

    /**
     * Elimina un ordenador por su ID.
     */
    public void eliminar(int id) {
        ordenadorDAO.eliminar(id);
    }

    /**
     * Obtiene un ordenador por su ID.
     */
    public OrdenadorGasto obtenerPorId(int id) {
        return ordenadorDAO.obtenerPorId(id);
    }

    /**
     * Lista todos los ordenadores.
     */
    public List<OrdenadorGasto> listarTodos() {
        return ordenadorDAO.listarTodos();
    }

    /**
     * Genera el JSON de respuesta para DataTables.
     */
    public String generarJsonDataTables(String draw, int start, int length,
            String searchValue, int orderColumn, String orderDir) {

        int total    = ordenadorDAO.countAll();
        int filtered = ordenadorDAO.countFiltered(searchValue);
        List<OrdenadorGasto> list = ordenadorDAO.findWithPagination(start, length, searchValue, orderColumn, orderDir);

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"draw\": ").append(draw != null ? draw : 1).append(",");
        json.append("\"recordsTotal\": ").append(total).append(",");
        json.append("\"recordsFiltered\": ").append(filtered).append(",");
        json.append("\"data\": [");

        for (int i = 0; i < list.size(); i++) {
            OrdenadorGasto o = list.get(i);
            json.append("[");
            json.append("\"").append(JsonUtils.escape(o.getNombreOrdenador())).append("\",");
            json.append("\"").append(JsonUtils.escape(o.getCargoOrdenador())).append("\",");
            json.append("\"").append(JsonUtils.escape(o.getOrganismo())).append("\",");
            json.append("\"").append(o.getId()).append("\"");
            json.append("]");
            if (i < list.size() - 1) json.append(",");
        }

        json.append("]}");
        return json.toString();
    }

    /**
     * Construye un objeto OrdenadorGasto a partir de parámetros de formulario.
     */
    public OrdenadorGasto construirDesdeParametros(
            String organismo, String direccionOrganismo, String nombreOrdenador,
            String cedulaOrdenador, String cargoOrdenador,
            String decretoNombramiento, String actaPosesion) {

        OrdenadorGasto o = new OrdenadorGasto();
        o.setOrganismo(organismo);
        o.setDireccionOrganismo(direccionOrganismo);
        o.setNombreOrdenador(nombreOrdenador);
        o.setCedulaOrdenador(cedulaOrdenador);
        o.setCargoOrdenador(cargoOrdenador);
        o.setDecretoNombramiento(decretoNombramiento);
        o.setActaPosesion(actaPosesion);
        return o;
    }
}
