package com.combinacion.servlets;

import com.combinacion.models.Contratista;
import com.combinacion.services.ContratistaService;
import com.combinacion.services.AuthService;
import com.combinacion.models.Usuario;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// @WebServlet(name = "ContratistaServlet", urlPatterns = {"/contratistas"})
public class ContratistaServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ContratistaServlet.class.getName());

    // Constantes para acciones
    private static final String ACTION_LIST = "list";
    private static final String ACTION_SEARCH = "search";
    private static final String ACTION_DATA = "data";
    private static final String ACTION_NEW = "new";
    private static final String ACTION_VIEW = "view";
    private static final String ACTION_EDIT = "edit";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_INSERT = "insert";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_EXPORT = "exportExcel";

    // Constantes para permisos
    private static final String PERMISO_CREAR = "CONTRATISTAS_CREAR";
    private static final String PERMISO_EDITAR = "CONTRATISTAS_EDITAR";
    private static final String PERMISO_ELIMINAR = "CONTRATISTAS_ELIMINAR";

    private final ContratistaService contratistaService = new ContratistaService();
    private final AuthService authService = new AuthService();

    private Usuario getUsuario(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (Usuario) session.getAttribute("usuario") : null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) {
            action = ACTION_LIST;
        }

        Usuario u = getUsuario(request);
        switch (action) {
            case ACTION_SEARCH:
                contratistaService.buscarPorCedula(request, response);
                break;
            case ACTION_DATA:
                contratistaService.responderDatosTabla(request, response);
                break;
            case ACTION_EXPORT:
                contratistaService.exportarExcel(request, response);
                break;
            case ACTION_NEW:
                if (authService.tienePermiso(u, PERMISO_CREAR)) {
                    request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
                } else {
                    response.sendRedirect("contratistas?error=sin_permiso");
                }
                break;
            case ACTION_VIEW:
                request.setAttribute("readonly", true);
            case ACTION_EDIT:
                if (ACTION_EDIT.equals(action) && !authService.tienePermiso(u, PERMISO_EDITAR)) {
                    response.sendRedirect("contratistas?error=sin_permiso");
                } else {
                    contratistaService.mostrarFormularioEdicion(request, response);
                }
                break;
            case ACTION_DELETE:
                if (authService.tienePermiso(u, PERMISO_ELIMINAR)) {
                    contratistaService.eliminar(request, response);
                } else {
                    response.sendRedirect("contratistas?error=sin_permiso");
                }
                break;
            case ACTION_LIST:
            default:
                contratistaService.listar(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        Usuario u = getUsuario(request);

        switch (action) {
            case ACTION_INSERT:
                if (authService.tienePermiso(u, PERMISO_CREAR)) {
                    contratistaService.insertar(request, response);
                } else {
                    response.sendRedirect("contratistas?error=sin_permiso");
                }
                break;
            case ACTION_UPDATE:
                if (authService.tienePermiso(u, PERMISO_EDITAR)) {
                    contratistaService.actualizar(request, response);
                } else {
                    response.sendRedirect("contratistas?error=sin_permiso");
                }
                break;
            case ACTION_DATA:
                contratistaService.responderDatosTabla(request, response);
                break;
            case ACTION_SEARCH:
                contratistaService.buscarPorCedula(request, response);
                break;
            default:
                contratistaService.listar(request, response);
                break;
        }
    }

    
}
