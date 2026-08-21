package com.combinacion.servlets;

import com.combinacion.models.Rol;
import com.combinacion.models.Usuario;
import com.combinacion.services.RolService;
import com.combinacion.services.UsuarioService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import com.combinacion.util.PasswordUtils;
import com.google.gson.Gson;

/**
 * Controlador HTTP para la gestión de Usuarios.
 */
// @WebServlet(name = "UsuarioServlet", urlPatterns = {"/admin/usuarios"})
@MultipartConfig(maxFileSize = 1024 * 1024 * 20, maxRequestSize = 1024 * 1024 * 25) // 20MB max file, 25MB max request
public class UsuarioServlet extends HttpServlet {

    private final UsuarioService usuarioService = new UsuarioService();
    private final RolService rolService = new RolService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String uri = request.getRequestURI();

        // --- SEGURIDAD ABSOLUTA: Si no es ruta /admin/, BLOQUEAR todo excepto el perfil propio ---
        if (!uri.contains("/admin/")) {
            if ("profile".equals(action)) {
                Usuario logged = (Usuario) request.getSession().getAttribute("usuario");
                if (logged != null && logged.getCedula() != null) {
                    request.setAttribute("contratistaInfo", new com.combinacion.dao.ContratistaDAO().obtenerPorCedula(logged.getCedula()));
                }
                request.getRequestDispatcher("/perfil.jsp").forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado. No tiene permisos para esta zona.");
            }
            return;
        }

        if (action == null) action = "list";
        switch (action) {
            case "new":
                usuarioService.mostrarFormulario(request, response, null);
                break;
            case "edit":
                usuarioService.editarUsuario(request, response);
                break;
            case "checkCedula":
                usuarioService.checkCedula(request, response);
                break;
            case "checkUsername":
                usuarioService.checkUsername(request, response);
                break;
            case "delete":
                usuarioService.eliminarUsuario(request, response);
                break;
            case "permissions":
                usuarioService.mostrarPermisosUsuario(request, response);
                break;
            case "profile":
                Usuario logged2 = (Usuario) request.getSession().getAttribute("usuario");
                if (logged2 != null && logged2.getCedula() != null) {
                    request.setAttribute("contratistaInfo", new com.combinacion.dao.ContratistaDAO().obtenerPorCedula(logged2.getCedula()));
                }
                request.getRequestDispatcher("/perfil.jsp").forward(request, response);
                break;
            case "data":
                usuarioService.responderDatosTabla(request, response);
                break;
            default:
                usuarioService.listar(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        String uri = request.getRequestURI();

        // --- SEGURIDAD ABSOLUTA: Si no es ruta /admin/, SOLO permitir acciones de perfil propio ---
        if (!uri.contains("/admin/")) {
            if ("updateProfile".equals(action)) {
                usuarioService.actualizarMiPerfil(request, response);
            } else if ("changeMyPassword".equals(action)) {
                usuarioService.cambiarMiPassword(request, response);
            } else if ("uploadPhoto".equals(action)) {
                usuarioService.subirFotoPerfil(request, response);
            } else if ("removePhoto".equals(action)) {
                usuarioService.eliminarFotoPerfil(request, response);
            } else if ("uploadFirma".equals(action)) {
                usuarioService.subirFirmaPerfil(request, response);
            } else if ("removeFirma".equals(action)) {
                usuarioService.eliminarFirmaPerfil(request, response);
            } else if ("data".equals(action)) {
                // Solo permitimos cargar datos si es por admin, pero lo movemos al doGet por uniformidad
                doGet(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso NO autorizado a la acción: " + action);
            }
            return;
        }

        if ("insert".equals(action)) {
            usuarioService.insertarUsuario(request, response);
        } else if ("update".equals(action)) {
            usuarioService.actualizarUsuario(request, response);
        } else if ("changePassword".equals(action)) {
            usuarioService.cambiarPassword(request, response);
        } else if ("updatePermissions".equals(action)) {
            usuarioService.guardarPermisosUsuario(request, response);
        } else if ("updateProfile".equals(action)) {
            usuarioService.actualizarMiPerfil(request, response);
        } else if ("changeMyPassword".equals(action)) {
            usuarioService.cambiarMiPassword(request, response);
        } else if ("uploadPhoto".equals(action)) {
            usuarioService.subirFotoPerfil(request, response);
        } else if ("removePhoto".equals(action)) {
            usuarioService.eliminarFotoPerfil(request, response);
        } else if ("uploadFirma".equals(action)) {
            usuarioService.subirFirmaPerfil(request, response);
        } else if ("removeFirma".equals(action)) {
            usuarioService.eliminarFirmaPerfil(request, response);
        } else if ("data".equals(action)) {
            doGet(request, response);
        } else {
            usuarioService.listar(request, response);
        }
    }

    
}
