package com.combinacion.servlets;

import com.combinacion.dao.PresupuestoDetalleDAO;
import com.combinacion.models.PresupuestoDetalle;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "PresupuestoServlet", urlPatterns = { "/presupuesto" })
public class PresupuestoServlet extends HttpServlet {

    private PresupuestoDetalleDAO presupuestoDAO = new PresupuestoDetalleDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) {
            action = "list"; // default action
        }

        switch (action) {
            case "data":
                listarData(request, response);
                break;
            case "view":
                verDetalle(request, response);
                break;
            default:
                // Show the main list page
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
                PresupuestoDetalle p = presupuestoDAO.obtenerPorId(id);
                request.setAttribute("presupuesto", p);
                request.getRequestDispatcher("ver_presupuesto.jsp").forward(request, response);
            } catch (NumberFormatException e) {
                response.sendRedirect("presupuesto");
            }
        } else {
            response.sendRedirect("presupuesto");
        }
    }

    private void listarData(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

        // 1. Get parameters for DataTables (server-side processing if permitted,
        // but for now we might just dump all data client-side for simplicity
        // or use the simple listing logic).
        // Looking at other servlets (e.g. ContratistaServlet), let's see how they do
        // it.
        // The user's code usually does simple JSON dumps or handles full server-side
        // params.
        // Let's stick to a simple dump first compatible with the AJAX calls.

        List<PresupuestoDetalle> lista = presupuestoDAO.listar();

        // Convert to format expected by DataTables: { "data": [ ... ] }
        // Or arrays of arrays if that's what the frontend expects.
        // 'lista_contratistas.jsp' expects an array of objects/arrays.
        // Let's create a generic "data" wrapper.

        // We will return an array of arrays mostly to save bandwidth or match the JSP
        // config.
        // In lista_contratistas, user used columns: [ {data: 0}, {data: 1}... ] implies
        // array of arrays or objects with index keys?
        // Actually usually default DataTables AJAX expects { "data": [ [col1, col2...],
        // ... ] }

        Object[][] data = new Object[lista.size()][9];
        for (int i = 0; i < lista.size(); i++) {
            PresupuestoDetalle p = lista.get(i);
            // Customize columns for the view
            data[i][0] = p.getCdpNumero();
            data[i][1] = p.getCdpFecha() != null ? sdf.format(p.getCdpFecha()) : "";
            data[i][2] = p.getCdpVencimiento() != null ? sdf.format(p.getCdpVencimiento()) : ""; // Added: Vencimiento
            data[i][3] = p.getRpNumero() != null ? p.getRpNumero() : "-"; // Added: RP Num
            data[i][4] = p.getRpFecha() != null ? sdf.format(p.getRpFecha()) : "-"; // Added: RP Date
            data[i][5] = p.getApropiacionPresupuestal();
            data[i][6] = p.getFichaEbiNombre() != null ? p.getFichaEbiNombre() : "N/A";
            data[i][7] = String.format("%,.2f", p.getCdpValor());
            data[i][8] = p.getId();
        }

        ResponseWrapper wrapper = new ResponseWrapper(data);
        String json = new Gson().toJson(wrapper);

        try (PrintWriter out = response.getWriter()) {
            out.print(json);
        }
    }

    // Simple helper for JSON structure
    class ResponseWrapper {
        Object data;

        public ResponseWrapper(Object data) {
            this.data = data;
        }
    }
}
