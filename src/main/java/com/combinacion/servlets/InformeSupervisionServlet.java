package com.combinacion.servlets;

import com.combinacion.services.InformeSupervisionService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "InformeSupervisionServlet", urlPatterns = { "/informes" })
@javax.servlet.annotation.MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class InformeSupervisionServlet extends HttpServlet {

    private final InformeSupervisionService informeService = new InformeSupervisionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "new":
                informeService.mostrarFormularioNuevo(request, response);
                break;
            case "view":
                informeService.mostrarDetalle(request, response);
                break;
            case "download":
                informeService.descargarInforme(request, response);
                break;
            case "descargar_doc":
                informeService.descargarArchivoDirecto(request, response);
                break;
            case "edit":
                informeService.mostrarFormularioEdicion(request, response);
                break;
            case "data":
                informeService.devolverDatosDataTables(request, response);
                break;
            case "delete":
                informeService.eliminar(request, response);
                break;
            default:
                informeService.listar(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if ("insert".equals(action)) {
            informeService.insertar(request, response);
        } else if ("update".equals(action)) {
            informeService.actualizar(request, response);
        } else if ("data".equals(action)) {
            informeService.devolverDatosDataTables(request, response);
        } else {
            informeService.listar(request, response);
        }
    }
}
