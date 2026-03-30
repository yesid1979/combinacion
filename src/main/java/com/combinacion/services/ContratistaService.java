package com.combinacion.services;

import com.combinacion.dao.ContratistaDAO;
import com.combinacion.models.Contratista;
import com.combinacion.util.JsonUtils;
import com.combinacion.util.ParseUtils;
import java.util.List;

/**
 * Capa de servicio para la entidad Contratista.
 * Contiene toda la lógica de negocio extraída del ContratistaServlet.
 */
public class ContratistaService {

    private final ContratistaDAO contratistaDAO = new ContratistaDAO();

    /**
     * Inserta un nuevo contratista. Valida que la cédula no esté duplicada.
     * @return null si fue exitoso, o un mensaje de error si falló.
     */
    public String insertar(Contratista c) {
        Contratista existing = contratistaDAO.obtenerPorCedula(c.getCedula());
        if (existing != null) {
            return "El contratista con cédula " + c.getCedula() + " ya existe (" + existing.getNombre() + ").";
        }
        if (!contratistaDAO.insertar(c)) {
            return "Error al guardar el contratista. Verifique los datos e intente nuevamente.";
        }
        return null;
    }

    /**
     * Actualiza un contratista existente. Valida que la cédula no esté en uso por otro.
     * @return null si fue exitoso, o un mensaje de error si falló.
     */
    public String actualizar(int id, Contratista c) {
        Contratista other = contratistaDAO.obtenerPorCedula(c.getCedula());
        if (other != null && other.getId() != id) {
            return "La cédula " + c.getCedula() + " ya se encuentra asignada a otro contratista (" + other.getNombre() + ").";
        }
        if (!contratistaDAO.actualizar(c)) {
            return "Error al actualizar el contratista.";
        }
        return null;
    }

    /**
     * Elimina un contratista por ID.
     */
    public void eliminar(int id) {
        contratistaDAO.eliminar(id);
    }

    /**
     * Obtiene un contratista por su ID.
     */
    public Contratista obtenerPorId(int id) {
        return contratistaDAO.obtenerPorId(id);
    }

    /**
     * Obtiene un contratista por su cédula.
     */
    public Contratista obtenerPorCedula(String cedula) {
        return contratistaDAO.obtenerPorCedula(cedula);
    }

    /**
     * Lista todos los contratistas.
     */
    public List<Contratista> listarTodos() {
        return contratistaDAO.listarTodos();
    }

    /**
     * Genera el JSON de respuesta para DataTables.
     */
    public String generarJsonDataTables(int draw, int start, int length,
            String searchValue, String sortCol, String orderDir) {

        int total    = contratistaDAO.countAll();
        int filtered = contratistaDAO.countFiltered(searchValue);
        List<Contratista> list = contratistaDAO.findWithPagination(start, length, searchValue, sortCol, orderDir);

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"draw\": ").append(draw).append(",");
        json.append("\"recordsTotal\": ").append(total).append(",");
        json.append("\"recordsFiltered\": ").append(filtered).append(",");
        json.append("\"data\": [");

        for (int i = 0; i < list.size(); i++) {
            Contratista c = list.get(i);
            json.append("[");
            json.append("\"").append(JsonUtils.escape(c.getCedula()        != null ? c.getCedula().trim()        : "")).append("\",");
            json.append("\"").append(JsonUtils.escape(c.getNombre()        != null ? c.getNombre().trim()        : "")).append("\",");
            json.append("\"").append(JsonUtils.escape(c.getCorreo()        != null ? c.getCorreo().trim()        : "")).append("\",");
            json.append("\"").append(JsonUtils.escape(c.getTelefono()      != null ? c.getTelefono().trim()      : "")).append("\",");
            json.append("\"").append(c.getId()).append("\",");
            json.append("\"").append(JsonUtils.escape(c.getNumeroContrato()!= null ? c.getNumeroContrato().trim(): "")).append("\",");
            json.append("\"").append(JsonUtils.escape(c.getAdicionSiNo()   != null ? c.getAdicionSiNo().trim()   : "")).append("\"");
            json.append("]");
            if (i < list.size() - 1) json.append(",");
        }

        json.append("]}");
        return json.toString();
    }

    /**
     * Genera el JSON de respuesta para búsqueda por cédula.
     */
    public String generarJsonBusqueda(Contratista c) {
        if (c == null) return "{\"found\": false}";

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"found\": true,");
        json.append("\"cedula\": \"").append(JsonUtils.escape(c.getCedula())).append("\",");
        json.append("\"dv\": \"").append(JsonUtils.escape(c.getDv())).append("\",");
        json.append("\"nombre\": \"").append(JsonUtils.escape(c.getNombre())).append("\",");
        json.append("\"telefono\": \"").append(JsonUtils.escape(c.getTelefono())).append("\",");
        json.append("\"correo\": \"").append(JsonUtils.escape(c.getCorreo())).append("\",");
        json.append("\"direccion\": \"").append(JsonUtils.escape(c.getDireccion())).append("\",");
        json.append("\"fecha_nacimiento\": \"")
                .append(c.getFechaNacimiento() != null ? c.getFechaNacimiento().toString() : "").append("\",");
        json.append("\"edad\": ").append(c.getEdad());
        json.append("}");
        return json.toString();
    }

    /**
     * Construye un objeto Contratista a partir de parámetros de formulario.
     */
    public Contratista construirDesdeParametros(
            String cedula, String dv, String nombre, String telefono, String correo,
            String direccion, String fechaNac, String edadStr,
            String formTitulo, String descFormacion, String experiencia,
            String descExperiencia, String tarjeta, String descTarjeta, String restricciones) {

        Contratista c = new Contratista();
        c.setCedula(cedula);
        c.setDv(dv);
        c.setNombre(nombre);
        c.setTelefono(telefono);
        c.setCorreo(correo);
        c.setDireccion(direccion);
        c.setFechaNacimiento(ParseUtils.parseDate(fechaNac));
        c.setEdad(ParseUtils.parseInt(edadStr));
        c.setFormacionTitulo(formTitulo);
        c.setDescripcionFormacion(descFormacion);
        c.setExperiencia(experiencia);
        c.setDescripcionExperiencia(descExperiencia);
        c.setTarjetaProfesional(tarjeta);
        c.setDescripcionTarjeta(descTarjeta);
        c.setRestricciones(restricciones);
        return c;
    }
}
