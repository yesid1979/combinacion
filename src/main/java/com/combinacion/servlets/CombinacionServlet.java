package com.combinacion.servlets;

import com.combinacion.dao.ContratoDAO;
import com.combinacion.services.CombinacionService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "CombinacionServlet", urlPatterns = { "/combinacion" })
public class CombinacionServlet extends HttpServlet {

    private CombinacionService combinacionService = new CombinacionService();
    private ContratoDAO contratoDAO = new ContratoDAO(); // Para listados iniciales

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        com.combinacion.util.DatabasePatcher.ensureSchema();
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        System.out.println("CombinacionServlet Action: " + action);

        if ("generate".equals(action)) {
            combinacionService.generarDocumentoIndividual(request, response);
        } else if ("downloadZip".equals(action)) {
            combinacionService.generarZipMasivo(request, response);
        } else if ("generateModificacion".equals(action)) {
            combinacionService.generarModificacionIndividual(request, response);
        } else if ("downloadZipModificacion".equals(action)) {
            combinacionService.generarModificacionMasivoZip(request, response);
        } else if ("downloadZipEstructuradores".equals(action)) {
            combinacionService.generarEstructuradoresMasivoZip(request, response);
        } else if ("downloadZipDesignacion".equals(action)) {
            combinacionService.generarDesignacionMasivoZip(request, response);
        } else {
            java.util.List<String> periodos = contratoDAO.obtenerPeriodosDisponibles();
            request.setAttribute("periodos", periodos);
            java.util.List<Integer> anios = contratoDAO.obtenerAniosDisponibles();
            request.setAttribute("anios", anios);
            java.util.Map<Integer, java.util.List<String>> periodosPorAnio = contratoDAO.obtenerPeriodosPorAnio();
            request.setAttribute("periodosPorAnioJson", new com.google.gson.Gson().toJson(periodosPorAnio));
            
            request.getRequestDispatcher("combinacion_contratistas.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
