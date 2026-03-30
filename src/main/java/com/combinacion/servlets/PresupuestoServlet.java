package com.combinacion.servlets;

import com.combinacion.services.PresupuestoService;
import com.combinacion.models.PresupuestoDetalle;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador HTTP para PresupuestoDetalle.
 * Responsabilidad exclusiva: leer parámetros HTTP, delegar al Service,
 * y dirigir la respuesta a la Vista correcta.
 */
@WebServlet(name = "PresupuestoServlet", urlPatterns = { "/presupuesto" })
public class PresupuestoServlet extends HttpServlet {

    private final PresupuestoService presupuestoService = new PresupuestoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "data":
                responderDatosTabla(response);
                break;
            case "view":
                verDetalle(request, response);
                break;
            default:
                request.getRequestDispatcher("lista_presupuesto.jsp").forward(request, response);
                break;
        }
    }

    private void verDetalle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                PresupuestoDetalle p = presupuestoService.obtenerPorId(id);
                request.setAttribute("presupuesto", p);
                request.getRequestDispatcher("ver_presupuesto.jsp").forward(request, response);
            } catch (NumberFormatException e) {
                response.sendRedirect("presupuesto");
            }
        } else {
            response.sendRedirect("presupuesto");
        }
    }

    private void responderDatosTabla(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Object[][] data = presupuestoService.generarDataParaTabla();

        try (PrintWriter out = response.getWriter()) {
            out.print(new Gson().toJson(new ResponseWrapper(data)));
        }
    }

    // Clase interna para envolver la respuesta JSON de DataTables
    class ResponseWrapper {
        Object data;
        ResponseWrapper(Object data) { this.data = data; }
    }
}
