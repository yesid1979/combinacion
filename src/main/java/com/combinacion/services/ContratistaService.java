package com.combinacion.services;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.OutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.combinacion.util.ParseUtils;



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
    private static final Logger logger = Logger.getLogger(ContratistaService.class.getName());


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
            String searchValue, String sortCol, String orderDir, boolean soloAdiciones, String periodo, Integer anio) {

        System.out.println("[SERVICE] Iniciando generación de JSON para DataTables");

        if (start < 0 || length < 0) {
            System.err.println("[SERVICE] Parámetros de paginación inválidos: start=" + start + ", length=" + length);
            return "{\"error\": \"Parámetros de paginación inválidos\"}";
        }

        int total = contratistaDAO.countAll();
        int filtered = contratistaDAO.countFiltered(searchValue, soloAdiciones, periodo, anio);
        List<Contratista> list = contratistaDAO.findWithPagination(start, length, searchValue, sortCol, orderDir, soloAdiciones, periodo, anio);

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

public void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Contratista> list = this.listarTodos();
            request.setAttribute("listContratistas", list);
            request.getRequestDispatcher("lista_contratistas.jsp").forward(request, response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al listar contratistas", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al listar contratistas");
        }
    }

    public void exportarExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"Listado_Contratistas.xlsx\"");

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Contratistas");

            // Header
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"Cédula", "Nombres y Apellidos", "Correo", "Teléfono", "Dirección", "Fecha Nacimiento", "Edad", "Título", "T. Profesional"};
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            List<Contratista> list = this.listarTodos();
            int rowNum = 1;
            for (Contratista c : list) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(c.getCedula() != null ? c.getCedula() : "");
                row.createCell(1).setCellValue(c.getNombre() != null ? c.getNombre() : "");
                row.createCell(2).setCellValue(c.getCorreo() != null ? c.getCorreo() : "");
                row.createCell(3).setCellValue(c.getTelefono() != null ? c.getTelefono() : "");
                row.createCell(4).setCellValue(c.getDireccion() != null ? c.getDireccion() : "");
                if (c.getFechaNacimiento() != null) {
                    row.createCell(5).setCellValue(c.getFechaNacimiento().toString());
                } else {
                    row.createCell(5).setCellValue("");
                }
                row.createCell(6).setCellValue(c.getEdad());
                row.createCell(7).setCellValue(c.getFormacionTitulo() != null ? c.getFormacionTitulo() : "");
                row.createCell(8).setCellValue(c.getTarjetaProfesional() != null ? c.getTarjetaProfesional() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al exportar contratistas a Excel", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al exportar contratistas");
        }
    }

    public void responderDatosTabla(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            // Parámetros de DataTables
            String draw = request.getParameter("draw");
            int start = parseIntSafe(request.getParameter("start"), 0);
            int length = parseIntSafe(request.getParameter("length"), 10);
            String search = request.getParameter("search[value]");
            int orderCol = parseIntSafe(request.getParameter("order[0][column]"), 1);
            String orderDir = request.getParameter("order[0][dir]");
            if (orderDir == null) {
                orderDir = "asc";
            }

            String source = request.getParameter("source");
            boolean soloAdiciones = "true".equals(request.getParameter("filterAdicion"));
            String periodo = request.getParameter("periodo");
            String anioParam = request.getParameter("anio");
            Integer anio = (anioParam != null && !anioParam.isEmpty()) ? Integer.parseInt(anioParam) : null;

            // Validación de parámetros críticos
            if (source == null || source.isEmpty()) {
                source = "lista"; // Default source
            }

            // Resolver columna de ordenamiento
            String sortCol = resolverColumnaOrden(source, orderCol);
            if (sortCol == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Columna de ordenamiento no válida");
                return;
            }

            // Log para depuración
            logger.info(String.format(
                    "DataTables Request - Draw: %s, Start: %d, Length: %d, Search: %s, Order: %s %s, Source: %s, Periodo: %s, Anio: %s",
                    draw, start, length, search, sortCol, orderDir, source, periodo, anioParam));

            // Configurar respuesta
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-store");

            // Generar y enviar JSON
            String jsonResponse = this.generarJsonDataTables(
                    parseIntSafe(draw, 1), start, length, search, sortCol, orderDir, soloAdiciones, periodo, anio);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al procesar la solicitud DataTables", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al procesar la solicitud");
        }
    }

    public void buscarPorCedula(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String cedula = request.getParameter("cedula");
            if (cedula == null || cedula.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parámetro 'cedula' es obligatorio");
                return;
            }

            Contratista c = this.obtenerPorCedula(cedula);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(this.generarJsonBusqueda(c));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al buscar contratista por cédula", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al buscar contratista");
        }
    }

    public void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                response.sendRedirect("contratistas?action=list&error=invalid_id");
                return;
            }

            int id = Integer.parseInt(idParam);
            Contratista existing = this.obtenerPorId(id);
            if (existing != null) {
                request.setAttribute("contratista", existing);
                if ("view".equals(request.getParameter("action"))) {
                    request.setAttribute("readonly", true);
                }
                request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
            } else {
                response.sendRedirect("contratistas?action=list&error=not_found&id=" + idParam);
            }
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "ID inválido: " + request.getParameter("id"), e);
            response.sendRedirect("contratistas?action=list&error=invalid_id");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al mostrar formulario de edición", e);
            response.sendRedirect("contratistas?action=list&error=exception&msg=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }

    public void insertar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Contratista c = this.construirDesdeParametros(
                    request.getParameter("cedula"),
                    request.getParameter("dv"),
                    request.getParameter("nombre"),
                    request.getParameter("telefono"),
                    request.getParameter("correo"),
                    request.getParameter("direccion"),
                    request.getParameter("fecha_nacimiento"),
                    request.getParameter("edad"),
                    request.getParameter("formacion_titulo"),
                    request.getParameter("descripcion_formacion"),
                    request.getParameter("experiencia"),
                    request.getParameter("descripcion_experiencia"),
                    request.getParameter("tarjeta_profesional"),
                    request.getParameter("descripcion_tarjeta"),
                    request.getParameter("restricciones")
            );
            String error = this.insertar(c);
            if (error != null) {
                request.setAttribute("error", error);
                request.setAttribute("contratista", c);
                request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
            } else {
                try { com.combinacion.models.Usuario __u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario"); if(__u!=null) com.combinacion.dao.AuditoriaDAO.registrar(__u, "Creación", "Registro creado en " + this.getClass().getSimpleName(), request.getRemoteAddr()); } catch(Exception ex){}
            response.sendRedirect("contratistas?status=created");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al insertar contratista", e);
            request.setAttribute("error", "Error interno: " + e.getMessage());
            request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
        }
    }

    public void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                response.sendRedirect("contratistas?action=list&error=invalid_id");
                return;
            }

            int id = Integer.parseInt(idParam);
            Contratista c = this.obtenerPorId(id);
            if (c == null) {
                response.sendRedirect("contratistas?action=list");
                return;
            }

            c = this.construirDesdeParametros(
                    request.getParameter("cedula"),
                    request.getParameter("dv"),
                    request.getParameter("nombre"),
                    request.getParameter("telefono"),
                    request.getParameter("correo"),
                    request.getParameter("direccion"),
                    request.getParameter("fecha_nacimiento"),
                    request.getParameter("edad"),
                    request.getParameter("formacion_titulo"),
                    request.getParameter("descripcion_formacion"),
                    request.getParameter("experiencia"),
                    request.getParameter("descripcion_experiencia"),
                    request.getParameter("tarjeta_profesional"),
                    request.getParameter("descripcion_tarjeta"),
                    request.getParameter("restricciones")
            );
            c.setId(id);
            String error = this.actualizar(id, c);
            if (error != null) {
                request.setAttribute("error", error);
                request.setAttribute("contratista", c);
                request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
            } else {
                try { com.combinacion.models.Usuario __u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario"); if(__u!=null) com.combinacion.dao.AuditoriaDAO.registrar(__u, "Actualización", "Registro actualizado en " + this.getClass().getSimpleName(), request.getRemoteAddr()); } catch(Exception ex){}
            response.sendRedirect("contratistas?status=updated");
            }
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "ID inválido: " + request.getParameter("id"), e);
            response.sendRedirect("contratistas?action=list&error=invalid_id");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al actualizar contratista", e);
            request.setAttribute("error", "Error interno: " + e.getMessage());
            request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
        }
    }

    public void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                response.sendRedirect("contratistas?status=error&msg=invalid_id");
                return;
            }

            int id = Integer.parseInt(idParam);
            this.eliminar(id);
            try { com.combinacion.models.Usuario __u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario"); if(__u!=null) com.combinacion.dao.AuditoriaDAO.registrar(__u, "Eliminación", "Registro eliminado en " + this.getClass().getSimpleName(), request.getRemoteAddr()); } catch(Exception ex){}
            response.sendRedirect("contratistas?status=deleted");
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "ID inválido: " + request.getParameter("id"), e);
            response.sendRedirect("contratistas?status=error&msg=invalid_id");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al eliminar contratista", e);
            response.sendRedirect("contratistas?status=error");
        }
    }

    public String resolverColumnaOrden(String source, int orderColumn) {
        if ("combinacion".equals(source)) {
            switch (orderColumn) {
                case 1: return "numero_contrato";
                case 2: return "cedula";
                case 3: return "nombre";
                case 4: return "correo";
                case 5: return "telefono";
                default: return "numero_contrato";
            }
        } else {
            // Default source or "lista"
            switch (orderColumn) {
                case 0: return "cedula";
                case 1: return "nombre";
                case 2: return "correo";
                case 3: return "telefono";
                default: return "nombre";
            }
        }
    }

    private int parseIntSafe(String val, int defaultVal) {
        if (val == null) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

}