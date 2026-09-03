package com.combinacion.servlets;

import com.combinacion.services.RevaluacionService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "RevaluacionServlet", urlPatterns = { "/revaluacion" })
public class RevaluacionServlet extends HttpServlet {

    private RevaluacionService revaluacionService = new RevaluacionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        com.combinacion.util.DatabasePatcher.ensureSchema();
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("generate".equals(action)) {
            revaluacionService.generarDocumentoIndividual(request, response);
        } else if ("downloadZip".equals(action)) {
            revaluacionService.generarZipMasivo(request, response);
        } else {
            com.combinacion.dao.ContratoDAO contratoDAO = new com.combinacion.dao.ContratoDAO();
            java.util.List<String> periodos = contratoDAO.obtenerPeriodosDisponibles("revaluacion");
            request.setAttribute("periodos", periodos);
            java.util.List<Integer> anios = contratoDAO.obtenerAniosDisponibles("revaluacion");
            request.setAttribute("anios", anios);
            java.util.Map<Integer, java.util.List<String>> periodosPorAnio = contratoDAO.obtenerPeriodosPorAnio("revaluacion");
            request.setAttribute("periodosPorAnioJson", new com.google.gson.Gson().toJson(periodosPorAnio));
            
            // Forward to the JSP page
            request.getRequestDispatcher("/revaluacion_proveedores.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
