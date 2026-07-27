package com.combinacion.servlets;

import com.combinacion.dao.ConfiguracionDAO;
import com.combinacion.models.Configuracion;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ConfiguracionServlet", urlPatterns = {"/admin/configuracion"})
public class ConfiguracionServlet extends HttpServlet {

    private final ConfiguracionDAO dao = new ConfiguracionDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "delete":
                eliminar(request, response);
                break;
            case "edit":
                editar(request, response);
                break;
            case "list":
            default:
                listar(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("save".equals(action)) {
            String idStr = request.getParameter("id");
            String clave = request.getParameter("clave");
            String valor = request.getParameter("valor");
            String descripcion = request.getParameter("descripcion");

            Configuracion c = new Configuracion();
            c.setClave(clave);
            c.setValor(valor);
            c.setDescripcion(descripcion);

            if (idStr != null && !idStr.isEmpty()) {
                c.setId(Integer.parseInt(idStr));
                dao.actualizar(c);
            } else {
                dao.insertar(c);
            }
            response.sendRedirect(request.getContextPath() + "/admin/configuracion?msg=success");
        } else if ("data".equals(action)) {
            responderDatosTabla(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/configuracion");
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Configuracion> lista = dao.listarTodos();
        request.setAttribute("listaConfiguraciones", lista);
        request.getRequestDispatcher("/admin/configuracion.jsp").forward(request, response);
    }

    private void editar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        Configuracion c = dao.obtenerPorId(id);
        request.setAttribute("configEdit", c);
        listar(request, response); // Reuse the list page to show the form + list
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        dao.eliminar(id);
        response.sendRedirect(request.getContextPath() + "/admin/configuracion?msg=deleted");
    }

    private void responderDatosTabla(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int draw = parseIntSafe(request.getParameter("draw"), 1);
            int start = parseIntSafe(request.getParameter("start"), 0);
            int length = parseIntSafe(request.getParameter("length"), 10);
            String search = request.getParameter("search[value]");
            int orderCol = parseIntSafe(request.getParameter("order[0][column]"), 0);
            String orderDir = request.getParameter("order[0][dir]");
            if (orderDir == null) orderDir = "asc";

            String sortCol = "clave";
            switch (orderCol) {
                case 1: sortCol = "valor"; break;
                case 2: sortCol = "descripcion"; break;
                default: sortCol = "clave"; break;
            }

            int total = dao.countAll();
            int filtered = dao.countFiltered(search);
            List<Configuracion> list = dao.findWithPagination(start, length, search, sortCol, orderDir);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("draw", draw);
            jsonResponse.addProperty("recordsTotal", total);
            jsonResponse.addProperty("recordsFiltered", filtered);

            JsonArray dataArray = new JsonArray();
            if (list != null) {
                for (Configuracion c : list) {
                    JsonArray row = new JsonArray();
                    row.add(c.getClave() != null ? c.getClave() : "");
                    row.add(c.getValor() != null ? c.getValor() : "");
                    row.add(c.getDescripcion() != null ? c.getDescripcion() : "");
                    row.add(c.getId());
                    dataArray.add(row);
                }
            }
            jsonResponse.add("data", dataArray);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(jsonResponse));
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al procesar la solicitud");
        }
    }

    private int parseIntSafe(String val, int defaultVal) {
        if (val == null) return defaultVal;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
