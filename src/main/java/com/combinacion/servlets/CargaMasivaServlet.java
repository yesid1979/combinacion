package com.combinacion.servlets;

import com.combinacion.services.CargaMasivaService;
import com.combinacion.services.CargaMasivaService.ResultadoCarga;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Controlador HTTP para la Carga Masiva de datos desde Excel/CSV.
 * Responsabilidad exclusiva: recibir el archivo HTTP, delegar el procesamiento
 * al CargaMasivaService, y redirigir con el resultado a la Vista.
 */
@WebServlet(name = "CargaMasivaServlet", urlPatterns = { "/upload" })
@MultipartConfig
public class CargaMasivaServlet extends HttpServlet {

    private final CargaMasivaService cargaMasivaService = new CargaMasivaService();

    @Override
    public void init() throws ServletException {
        super.init();
        com.combinacion.util.DatabasePatcher.ensureSchema();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        Part filePart = request.getPart("file");

        if (filePart == null) {
            request.getSession().setAttribute("error", "❌ No se seleccionó ningún archivo.");
            response.sendRedirect("carga_masiva.jsp");
            return;
        }

        String fileName = filePart.getSubmittedFileName().toLowerCase();

        try (InputStream fileContent = filePart.getInputStream()) {
            ResultadoCarga resultado = cargaMasivaService.procesarArchivo(fileContent, fileName);

            request.getSession().setAttribute("message", resultado.generarMensaje());
            request.getSession().setAttribute("debug",   resultado.log.toString());

        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("error", "❌ Error al procesar el archivo: " + e.getMessage());
        }

        response.sendRedirect("carga_masiva.jsp");
    }
}
