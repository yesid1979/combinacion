package com.combinacion.services;

import com.combinacion.dao.ContratistaDAO;
import com.combinacion.models.Contratista;
import com.combinacion.util.ParseUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;

/**
 * Capa de servicio para la entidad Contratista.
 * Contiene toda la lógica de negocio extraída del ContratistaServlet.
 */
public class ContratistaService {

    private final ContratistaDAO contratistaDAO = new ContratistaDAO();
    private final Gson gson = new Gson();

    /**
     * Inserta un nuevo contratista. Valida que la cédula no esté duplicada.
     * @return null si fue exitoso, o un mensaje de error si falló.
     */
    public String insertar(Contratista c) {
        if (c == null) {
            return "El contratista no puede ser nulo.";
        }

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
        if (c == null) {
            return "El contratista no puede ser nulo.";
        }

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
            String searchValue, String sortCol, String orderDir, boolean soloAdiciones) {

        System.out.println("[SERVICE] Iniciando generación de JSON para DataTables");

        if (start < 0 || length < 0) {
            System.err.println("[SERVICE] Parámetros de paginación inválidos: start=" + start + ", length=" + length);
            return "{\"error\": \"Parámetros de paginación inválidos\"}";
        }

        int total = contratistaDAO.countAll();
        int filtered = contratistaDAO.countFiltered(searchValue, soloAdiciones);
        List<Contratista> list = contratistaDAO.findWithPagination(start, length, searchValue, sortCol, orderDir, soloAdiciones);

        System.out.println("[SERVICE] Registros obtenidos: " + (list != null ? list.size() : "null"));

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("draw", draw);
        jsonResponse.addProperty("recordsTotal", total);
        jsonResponse.addProperty("recordsFiltered", filtered);

        JsonArray dataArray = new JsonArray();
        if (list != null) {
            for (Contratista c : list) {
                try {
                    JsonArray row = new JsonArray();
                    row.add(c.getCedula() != null ? c.getCedula().trim() : "");
                    row.add(c.getNombre() != null ? c.getNombre().trim() : "");
                    row.add(c.getCorreo() != null ? c.getCorreo().trim() : "");
                    row.add(c.getTelefono() != null ? c.getTelefono().trim() : "");
                    row.add(c.getId());
                    row.add(c.getNumeroContrato() != null ? c.getNumeroContrato().trim() : "");
                    row.add(c.getAdicionSiNo() != null ? c.getAdicionSiNo().trim() : "");
                    dataArray.add(row);
                } catch (Exception e) {
                    System.err.println("[SERVICE] Error procesando contratista ID " + c.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        jsonResponse.add("data", dataArray);
 
        return gson.toJson(jsonResponse);
    }

    /**
     * Genera el JSON de respuesta para búsqueda por cédula.
     */
    public String generarJsonBusqueda(Contratista c) {
        if (c == null) {
            return "{\"found\": false}";
        }

        JsonObject json = new JsonObject();
        json.addProperty("found", true);
        json.addProperty("cedula", c.getCedula());
        json.addProperty("dv", c.getDv());
        json.addProperty("nombre", c.getNombre());
        json.addProperty("telefono", c.getTelefono());
        json.addProperty("correo", c.getCorreo());
        json.addProperty("direccion", c.getDireccion());
        json.addProperty("fecha_nacimiento", c.getFechaNacimiento() != null ? c.getFechaNacimiento().toString() : "");
        json.addProperty("edad", c.getEdad());

        return gson.toJson(json);
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