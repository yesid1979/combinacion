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
@MultipartConfig(maxFileSize = 1024 * 1024 * 5) // 5MB max
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
                request.getRequestDispatcher("/perfil.jsp").forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado. No tiene permisos para esta zona.");
            }
            return;
        }

        if (action == null) action = "list";
        switch (action) {
            case "new":
                mostrarFormulario(request, response, null);
                break;
            case "edit":
                editarUsuario(request, response);
                break;
            case "checkCedula":
                checkCedula(request, response);
                break;
            case "checkUsername":
                checkUsername(request, response);
                break;
            case "delete":
                eliminarUsuario(request, response);
                break;
            case "permissions":
                mostrarPermisosUsuario(request, response);
                break;
            case "profile":
                request.getRequestDispatcher("/perfil.jsp").forward(request, response);
                break;
            case "data":
                responderDatosTabla(request, response);
                break;
            default:
                listar(request, response);
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
                actualizarMiPerfil(request, response);
            } else if ("changeMyPassword".equals(action)) {
                cambiarMiPassword(request, response);
            } else if ("uploadPhoto".equals(action)) {
                subirFotoPerfil(request, response);
            } else if ("removePhoto".equals(action)) {
                eliminarFotoPerfil(request, response);
            } else if ("data".equals(action)) {
                // Solo permitimos cargar datos si es por admin, pero lo movemos al doGet por uniformidad
                doGet(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso NO autorizado a la acción: " + action);
            }
            return;
        }

        if ("insert".equals(action)) {
            insertarUsuario(request, response);
        } else if ("update".equals(action)) {
            actualizarUsuario(request, response);
        } else if ("changePassword".equals(action)) {
            cambiarPassword(request, response);
        } else if ("updatePermissions".equals(action)) {
            guardarPermisosUsuario(request, response);
        } else if ("updateProfile".equals(action)) {
            actualizarMiPerfil(request, response);
        } else if ("changeMyPassword".equals(action)) {
            cambiarMiPassword(request, response);
        } else if ("uploadPhoto".equals(action)) {
            subirFotoPerfil(request, response);
        } else if ("removePhoto".equals(action)) {
            eliminarFotoPerfil(request, response);
        } else if ("data".equals(action)) {
            doGet(request, response);
        } else {
            listar(request, response);
        }
    }

    private void actualizarMiPerfil(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Usuario logged = (Usuario) session.getAttribute("usuario");
        if (logged == null) return;

        String nombre = request.getParameter("nombre");
        String correo = request.getParameter("correo");
        String celular = request.getParameter("celular");

        String error = usuarioService.actualizarPerfil(logged.getId(), nombre, correo, celular);
        
        Map<String, Object> res = new HashMap<>();
        if (error == null) {
            // Actualizar objeto en sesión
            logged.setNombreCompleto(nombre);
            logged.setCorreo(correo);
            logged.setCelular(celular);
            session.setAttribute("nombreUsuario", nombre);
            res.put("success", true);
        } else {
            res.put("success", false);
            res.put("message", error);
        }
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(res));
    }

    private void cambiarMiPassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Usuario logged = (Usuario) session.getAttribute("usuario");
        if (logged == null) return;

        String oldPass = request.getParameter("oldPass");
        String newPass = request.getParameter("newPass");

        Map<String, Object> res = new HashMap<>();
        // Verificar pass actual
        if (!PasswordUtils.verifyPassword(oldPass, logged.getPasswordHash(), logged.getSalt())) {
            res.put("success", false);
            res.put("message", "La contraseña actual es incorrecta.");
        } else {
            String error = usuarioService.cambiarPassword(logged.getId(), newPass);
            if (error == null) {
                // Actualizar hash en sesión
                Usuario nuevo = usuarioService.obtenerPorId(logged.getId());
                session.setAttribute("usuario", nuevo);
                res.put("success", true);
            } else {
                res.put("success", false);
                res.put("message", error);
            }
        }
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(res));
    }

    private void eliminarFotoPerfil(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Usuario logged = (Usuario) session.getAttribute("usuario");
        Map<String, Object> res = new HashMap<>();
        if (logged != null) {
            if (usuarioService.actualizarFoto(logged.getId(), null)) {
                // Sincronizar objeto en sesión
                logged.setFotoUrl(null);
                session.setAttribute("usuario", logged);
                res.put("success", true);
            } else {
                res.put("success", false);
                res.put("message", "Error en BD al quitar foto");
            }
        } else {
            res.put("success", false);
            res.put("message", "Sesión no válida");
        }
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(res));
    }

    private void subirFotoPerfil(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Usuario logged = (Usuario) session.getAttribute("usuario");
        Map<String, Object> res = new HashMap<>();
        if (logged == null) return;

        try {
            Part filePart = request.getPart("foto");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = "profile_" + logged.getId() + "_" + System.currentTimeMillis() + ".jpg";
                String uploadPath = getServletContext().getRealPath("/") + "uploads" + File.separator + "profile";
                
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                filePart.write(uploadPath + File.separator + fileName);
                
                String fotoUrl = "uploads/profile/" + fileName;
                if (usuarioService.actualizarFoto(logged.getId(), fotoUrl)) {
                    logged.setFotoUrl(fotoUrl);
                    res.put("success", true);
                    res.put("url", fotoUrl);
                } else {
                    res.put("success", false);
                    res.put("message", "Error al guardar URL en BD");
                }
            }
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(res));
    }

    private void mostrarPermisosUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Usuario u = usuarioService.obtenerPorId(id);
            if (u == null) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios");
                return;
            }
            request.setAttribute("usuario_perms", u);
            request.setAttribute("todosPermisos", rolService.listarPermisos());
            request.setAttribute("modulos", rolService.listarModulos());
            request.getRequestDispatcher("/admin/permisos_usuario.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios");
        }
    }

    private void guardarPermisosUsuario(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String[] permsIds = request.getParameterValues("permisos");
            java.util.List<Integer> ids = new java.util.ArrayList<>();
            if (permsIds != null) {
                for (String pid : permsIds) {
                    ids.add(Integer.parseInt(pid));
                }
            }
            usuarioService.actualizarPermisosEspeciales(id, ids);
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=updated");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=error");
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Usuario> usuarios = usuarioService.listarTodos();
        request.setAttribute("listUsuarios", usuarios);
        request.getRequestDispatcher("/admin/lista_usuarios.jsp").forward(request, response);
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                    Usuario usuario) throws ServletException, IOException {
        List<Rol> roles = rolService.listarTodos();
        request.setAttribute("listRoles", roles);
        if (usuario != null) {
            request.setAttribute("usuario_edit", usuario);
        }
        request.getRequestDispatcher("/admin/form_usuario.jsp").forward(request, response);
    }

    private void editarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Usuario usuario = usuarioService.obtenerPorId(id);
            if (usuario != null) {
                mostrarFormulario(request, response, usuario);
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=notfound");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios");
        }
    }

    private void insertarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String nombreCompleto = request.getParameter("nombre_completo");
        String correo = request.getParameter("correo");
        String cedula = request.getParameter("cedula");
        String celular = request.getParameter("celular");
        String sexo = request.getParameter("sexo");
        String vinculacion = request.getParameter("vinculacion");
        java.sql.Date fechaInicio = parseDate(request.getParameter("fecha_inicio"));
        java.sql.Date fechaFin = parseDate(request.getParameter("fecha_fin"));
        int rolId = 0;
        try { rolId = Integer.parseInt(request.getParameter("rol_id")); } catch (Exception ignored) {}

        String error = usuarioService.crear(username, password, nombreCompleto, correo, 
                                          cedula, celular, sexo, vinculacion, fechaInicio, fechaFin, rolId);
        if (error != null) {
            request.setAttribute("error", error);
            Usuario u = new Usuario();
            u.setUsername(username);
            u.setNombreCompleto(nombreCompleto);
            u.setCorreo(correo);
            u.setCedula(cedula);
            u.setCelular(celular);
            u.setSexo(sexo);
            u.setVinculacion(vinculacion);
            u.setFechaInicioContrato(fechaInicio);
            u.setFechaFinContrato(fechaFin);
            u.setRolId(rolId);
            mostrarFormulario(request, response, u);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=created");
        }
    }

    private void actualizarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String username = request.getParameter("username");
            String nombreCompleto = request.getParameter("nombre_completo");
            String correo = request.getParameter("correo");
            String cedula = request.getParameter("cedula");
            String celular = request.getParameter("celular");
            String sexo = request.getParameter("sexo");
            String vinculacion = request.getParameter("vinculacion");
            java.sql.Date fechaInicio = parseDate(request.getParameter("fecha_inicio"));
            java.sql.Date fechaFin = parseDate(request.getParameter("fecha_fin"));
            boolean activo = "on".equals(request.getParameter("activo")) || "true".equals(request.getParameter("activo"));
            int rolId = Integer.parseInt(request.getParameter("rol_id"));

            String error = usuarioService.actualizar(id, username, nombreCompleto, correo, 
                                                   cedula, celular, sexo, vinculacion, fechaInicio, fechaFin,
                                                   activo, rolId);
            if (error != null) {
                request.setAttribute("error", error);
                Usuario u = new Usuario();
                u.setId(id);
                u.setUsername(username);
                u.setNombreCompleto(nombreCompleto);
                u.setCorreo(correo);
                u.setCedula(cedula);
                u.setCelular(celular);
                u.setSexo(sexo);
                u.setVinculacion(vinculacion);
                u.setFechaInicioContrato(fechaInicio);
                u.setFechaFinContrato(fechaFin);
                u.setActivo(activo);
                u.setRolId(rolId);
                mostrarFormulario(request, response, u);
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=updated");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=error");
        }
    }

    private void cambiarPassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String nuevaPassword = request.getParameter("nueva_password");

            String error = usuarioService.cambiarPassword(id, nuevaPassword);
            if (error != null) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios?action=edit&id=" + id + "&error=" + java.net.URLEncoder.encode(error, "UTF-8"));
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=password_changed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=error");
        }
    }

    private void eliminarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            usuarioService.eliminar(id);
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=deleted");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=error");
        }
    }

    private void checkCedula(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String cedula = request.getParameter("cedula");
        int excludeId = 0;
        try { excludeId = Integer.parseInt(request.getParameter("id")); } catch (Exception ignored) {}
        
        boolean existe = usuarioService.existeCedula(cedula, excludeId);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"exists\": " + existe + "}");
    }

    private void checkUsername(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username");
        int excludeId = 0;
        try { excludeId = Integer.parseInt(request.getParameter("id")); } catch (Exception ignored) {}
        
        boolean existe = usuarioService.existeUsername(username, excludeId);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"exists\": " + existe + "}");
    }

    private void responderDatosTabla(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String draw = request.getParameter("draw");
        int start = parseIntSafe(request.getParameter("start"), 0);
        int length = parseIntSafe(request.getParameter("length"), 10);
        String search = request.getParameter("search[value]");
        String sortCol = request.getParameter("order[0][column]");
        String orderDir = request.getParameter("order[0][dir]");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
            usuarioService.generarJsonDataTables(
                parseIntSafe(draw, 1), start, length, search, sortCol, orderDir)
        );
    }

    private int parseIntSafe(String val, int defaultVal) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private java.sql.Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return java.sql.Date.valueOf(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
}
