package com.combinacion.servlets;

import com.combinacion.models.Supervisor;
import com.combinacion.services.SupervisorService;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador HTTP para Supervisor.
 * Responsabilidad exclusiva: leer parámetros HTTP, delegar al Service,
 * y dirigir la respuesta a la Vista correcta.
 */
// @WebServlet("/supervisores")
public class SupervisorServlet extends HttpServlet {

    private SupervisorService supervisorService;

    @Override
    public void init() {
        supervisorService = new SupervisorService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "data":
                responderDatosTabla(request, response);
                break;
            case "new":
                request.getRequestDispatcher("form_supervisor.jsp").forward(request, response);
                break;
            case "view":
            case "edit":
                mostrarFormularioEdicion(request, response);
                break;
            case "delete":
                eliminar(request, response);
                break;
            case "list":
            default:
                request.getRequestDispatcher("lista_supervisores.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) action = "insert";

        switch (action) {
            case "data":
                responderDatosTabla(request, response);
                break;
            case "update":
                actualizar(request, response);
                break;
            default:
                insertar(request, response);
                break;
        }
    }

    private void responderDatosTabla(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String draw     = request.getParameter("draw");
        int    start    = parseIntSafe(request.getParameter("start"),            0);
        int    length   = parseIntSafe(request.getParameter("length"),          10);
        String search   = request.getParameter("search[value]");
        int    orderCol = parseIntSafe(request.getParameter("order[0][column]"), 0);
        String orderDir = request.getParameter("order[0][dir]");
        if (orderDir == null) orderDir = "asc";

        int totalRecords    = supervisorService.countAll();
        int filteredRecords = supervisorService.countFiltered(search);
        List<Supervisor> supervisores = supervisorService.findWithPagination(start, length, search, orderCol, orderDir);

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("draw",            draw);
        jsonMap.put("recordsTotal",    totalRecords);
        jsonMap.put("recordsFiltered", filteredRecords);
        jsonMap.put("data",            supervisores);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(jsonMap));
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Supervisor existing = supervisorService.obtenerPorId(id);
            request.setAttribute("supervisor", existing);
            if ("view".equals(request.getParameter("action"))) {
                request.setAttribute("readonly", true);
            }
            request.getRequestDispatcher("form_supervisor.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect("supervisores");
        }
    }

    private void insertar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Supervisor s = supervisorService.construirDesdeParametros(
            request.getParameter("nombre"),
            request.getParameter("cedula"),
            request.getParameter("cargo")
        );
        if (supervisorService.insertar(s)) {
            response.sendRedirect("supervisores?status=created");
        } else {
            response.sendRedirect("supervisores?status=error");
        }
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Supervisor s = supervisorService.construirDesdeParametros(
                request.getParameter("nombre"),
                request.getParameter("cedula"),
                request.getParameter("cargo")
            );
            s.setId(id);
            if (supervisorService.actualizar(s)) {
                response.sendRedirect("supervisores?status=updated");
            } else {
                response.sendRedirect("supervisores?status=error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("supervisores?status=error");
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            if (supervisorService.eliminar(id)) {
                response.getWriter().write("success");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private int parseIntSafe(String val, int defaultVal) {
        if (val == null) return defaultVal;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return defaultVal; }
    }
}
