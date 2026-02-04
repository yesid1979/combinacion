package com.combinacion.servlets;

import com.combinacion.dao.ContratistaDAO;
import com.combinacion.models.Contratista;
import com.combinacion.util.ParseUtils;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ContratistaServlet", urlPatterns = { "/contratistas" })
public class ContratistaServlet extends HttpServlet {

    private ContratistaDAO contratistaDAO = new ContratistaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null)
            action = "list";

        switch (action) {
            case "search":
                searchByCedula(request, response);
                break;
            case "data":
                listContratistasData(request, response);
                break;
            case "new":
                showNewForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteContratista(request, response);
                break;
            case "list":
            default:
                listContratistas(request, response);
                break;
        }
    }

    private void listContratistasData(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String drawParam = request.getParameter("draw");
            String startParam = request.getParameter("start");
            String lengthParam = request.getParameter("length");

            int draw = (drawParam != null) ? ParseUtils.parseInt(drawParam) : 1;
            int start = (startParam != null) ? ParseUtils.parseInt(startParam) : 0;
            int length = (lengthParam != null) ? ParseUtils.parseInt(lengthParam) : 10;

            String searchValue = request.getParameter("search[value]");
            String orderColParam = request.getParameter("order[0][column]");
            int orderColumn = (orderColParam != null) ? ParseUtils.parseInt(orderColParam) : 1;
            String orderDir = request.getParameter("order[0][dir]");
            if (orderDir == null)
                orderDir = "asc";

            // Determine sort column name based on view source and index
            String source = request.getParameter("source");
            String sortCol = "nombre"; // Default

            if ("combinacion".equals(source)) {
                // Mapping for Combinacion View: Checkbox(0), Contrato(1), Cedula(2),
                // Nombre(3)...
                switch (orderColumn) {
                    case 1:
                        sortCol = "numero_contrato";
                        break;
                    case 2:
                        sortCol = "cedula";
                        break;
                    case 3:
                        sortCol = "nombre";
                        break;
                    case 4:
                        sortCol = "correo";
                        break;
                    case 5:
                        sortCol = "telefono";
                        break;
                    default:
                        sortCol = "numero_contrato";
                        break;
                }
            } else {
                // Mapping for Standard List: Cedula(0), Nombre(1), Correo(2), Telefono(3)...
                switch (orderColumn) {
                    case 0:
                        sortCol = "cedula";
                        break;
                    case 1:
                        sortCol = "nombre";
                        break;
                    case 2:
                        sortCol = "correo";
                        break;
                    case 3:
                        sortCol = "telefono";
                        break;
                    default:
                        sortCol = "nombre";
                        break;
                }
            }

            int total = contratistaDAO.countAll();
            int filtered = contratistaDAO.countFiltered(searchValue);
            List<Contratista> list = contratistaDAO.findWithPagination(start, length, searchValue, sortCol, orderDir);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-store");

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"draw\": ").append(draw).append(",");
            json.append("\"recordsTotal\": ").append(total).append(",");
            json.append("\"recordsFiltered\": ").append(filtered).append(",");
            json.append("\"data\": [");

            for (int i = 0; i < list.size(); i++) {
                Contratista c = list.get(i);
                json.append("[");
                // Index 0: Cedula
                json.append("\"").append(escapeJson(c.getCedula() != null ? c.getCedula().trim() : "")).append("\",");
                // Index 1: Nombre
                json.append("\"").append(escapeJson(c.getNombre() != null ? c.getNombre().trim() : "")).append("\",");
                // Index 2: Correo
                json.append("\"").append(escapeJson(c.getCorreo() != null ? c.getCorreo().trim() : "")).append("\",");
                // Index 3: Telefono
                json.append("\"").append(escapeJson(c.getTelefono() != null ? c.getTelefono().trim() : ""))
                        .append("\",");
                // Index 4: ID
                json.append("\"").append(c.getId()).append("\",");
                // Index 5: Contrato (Extra data for combinacion)
                json.append("\"").append(escapeJson(c.getNumeroContrato() != null ? c.getNumeroContrato().trim() : ""))
                        .append("\"");
                json.append("]");
                if (i < list.size() - 1)
                    json.append(",");
            }

            json.append("]}");

            response.getWriter().write(json.toString());
            response.getWriter().flush();

        } catch (

        Exception e) {
            e.printStackTrace();
            response.setContentType("application/json");
            response.getWriter().write("{\"draw\":1,\"recordsTotal\":0,\"recordsFiltered\":0,\"data\":[],\"error\":\""
                    + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void searchByCedula(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String cedula = request.getParameter("cedula");
        Contratista c = contratistaDAO.obtenerPorCedula(cedula);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (c != null) {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"found\": true,");
            json.append("\"cedula\": \"").append(escapeJson(c.getCedula())).append("\",");
            json.append("\"dv\": \"").append(escapeJson(c.getDv())).append("\",");
            json.append("\"nombre\": \"").append(escapeJson(c.getNombre())).append("\",");
            json.append("\"telefono\": \"").append(escapeJson(c.getTelefono())).append("\",");
            json.append("\"correo\": \"").append(escapeJson(c.getCorreo())).append("\",");
            json.append("\"direccion\": \"").append(escapeJson(c.getDireccion())).append("\",");
            json.append("\"fecha_nacimiento\": \"")
                    .append(c.getFechaNacimiento() != null ? c.getFechaNacimiento().toString() : "").append("\",");
            json.append("\"edad\": ").append(c.getEdad());
            json.append("}");
            response.getWriter().write(json.toString());
        } else {
            response.getWriter().write("{\"found\": false}");
        }
    }

    private String escapeJson(String s) {
        if (s == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch < ' ') {
                        String t = "000" + Integer.toHexString(ch);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if ("insert".equals(action)) {
            insertContratista(request, response);
        } else if ("update".equals(action)) {
            updateContratista(request, response);
        } else if ("data".equals(action)) {
            listContratistasData(request, response);
        } else if ("search".equals(action)) {
            searchByCedula(request, response);
        } else {
            listContratistas(request, response);
        }
    }

    private void listContratistas(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Contratista> list = contratistaDAO.listarTodos();
        request.setAttribute("listContratistas", list);
        request.getRequestDispatcher("lista_contratistas.jsp").forward(request, response);
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
    }

    private void insertContratista(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Contratista c = new Contratista();
            c.setCedula(request.getParameter("cedula"));
            c.setDv(request.getParameter("dv"));
            c.setNombre(request.getParameter("nombre"));
            c.setTelefono(request.getParameter("telefono"));
            c.setCorreo(request.getParameter("correo"));
            c.setDireccion(request.getParameter("direccion"));
            c.setFechaNacimiento(ParseUtils.parseDate(request.getParameter("fecha_nacimiento")));
            c.setEdad(ParseUtils.parseInt(request.getParameter("edad")));
            // Add other fields as needed based on form

            if (contratistaDAO.insertar(c)) {
                // Success
                response.sendRedirect("contratistas?status=created");
            } else {
                request.setAttribute("error", "Error al guardar. Verifique si la cédula ya existe.");
                request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error interno: " + e.getMessage());
            request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
        }
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Contratista existing = contratistaDAO.obtenerPorId(id);
            if (existing != null) {
                request.setAttribute("contratista", existing);
                request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
            } else {
                response.sendRedirect("contratistas?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("contratistas?action=list");
        }
    }

    private void updateContratista(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Contratista c = new Contratista();
            c.setId(id);
            c.setCedula(request.getParameter("cedula"));
            c.setDv(request.getParameter("dv"));
            c.setNombre(request.getParameter("nombre"));
            c.setTelefono(request.getParameter("telefono"));
            c.setCorreo(request.getParameter("correo"));
            c.setDireccion(request.getParameter("direccion"));
            c.setFechaNacimiento(ParseUtils.parseDate(request.getParameter("fecha_nacimiento")));
            c.setEdad(ParseUtils.parseInt(request.getParameter("edad")));

            if (contratistaDAO.actualizar(c)) {
                response.sendRedirect("contratistas?status=updated");
            } else {
                request.setAttribute("error", "Error al actualizar el contratista.");
                request.setAttribute("contratista", c);
                request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error interno: " + e.getMessage());
            request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
        }
    }

    private void deleteContratista(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            contratistaDAO.eliminar(id);
            response.sendRedirect("contratistas?status=deleted");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("contratistas?status=error");
        }
    }
}
