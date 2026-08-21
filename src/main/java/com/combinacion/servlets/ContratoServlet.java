package com.combinacion.servlets;

import com.combinacion.models.OrdenadorGasto;
import com.combinacion.models.Supervisor;
import com.combinacion.services.ContratoService;
import com.combinacion.services.ContratoService.ContratoFormData;
import com.combinacion.util.ParseUtils;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador HTTP para Contrato.
 * Responsabilidad exclusiva: leer parámetros HTTP, delegar al Service,
 * y dirigir la respuesta a la Vista correcta.
 */
// @WebServlet(name = "ContratoServlet", urlPatterns = { "/contratos" })
public class ContratoServlet extends HttpServlet {

    private final ContratoService contratoService = new ContratoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "data":
                contratoService.responderDatosTabla(request, response);
                break;
            case "new":
                contratoService.mostrarFormularioNuevo(request, response);
                break;
            case "view":
            case "edit":
                contratoService.mostrarFormularioEdicion(request, response);
                break;
            default:
                contratoService.listar(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if ("insert".equals(action)) {
            contratoService.insertar(request, response);
        } else if ("update".equals(action)) {
            contratoService.actualizar(request, response);
        } else if ("data".equals(action)) {
            contratoService.responderDatosTabla(request, response);
        } else {
            contratoService.listar(request, response);
        }
    }

    // -------------------------------------------------------------------------
    // PRIVADOS
    // -------------------------------------------------------------------------

    
}
