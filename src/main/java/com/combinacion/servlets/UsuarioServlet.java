package com.combinacion.servlets;

import com.combinacion.models.Rol;
import com.combinacion.models.Usuario;
import com.combinacion.services.RolService;
import com.combinacion.services.UsuarioService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador HTTP para la gestión de Usuarios.
 */
@WebServlet(name = "UsuarioServlet", urlPatterns = {"/admin/usuarios"})
public class UsuarioServlet extends HttpServlet {

    private final UsuarioService usuarioService = new UsuarioService();
    private final RolService rolService = new RolService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "new":
                mostrarFormulario(request, response, null);
                break;
            case "edit":
                editarUsuario(request, response);
                break;
            case "delete":
                eliminarUsuario(request, response);
                break;
            case "permissions":
                mostrarPermisosUsuario(request, response);
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

        if ("insert".equals(action)) {
            insertarUsuario(request, response);
        } else if ("update".equals(action)) {
            actualizarUsuario(request, response);
        } else if ("changePassword".equals(action)) {
            cambiarPassword(request, response);
        } else if ("updatePermissions".equals(action)) {
            guardarPermisosUsuario(request, response);
        } else {
            listar(request, response);
        }
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
        int rolId = 0;
        try { rolId = Integer.parseInt(request.getParameter("rol_id")); } catch (Exception ignored) {}

        String error = usuarioService.crear(username, password, nombreCompleto, correo, rolId);
        if (error != null) {
            request.setAttribute("error", error);
            Usuario u = new Usuario();
            u.setUsername(username);
            u.setNombreCompleto(nombreCompleto);
            u.setCorreo(correo);
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
            boolean activo = "on".equals(request.getParameter("activo")) || "true".equals(request.getParameter("activo"));
            int rolId = Integer.parseInt(request.getParameter("rol_id"));

            String error = usuarioService.actualizar(id, username, nombreCompleto, correo, activo, rolId);
            if (error != null) {
                request.setAttribute("error", error);
                Usuario u = new Usuario();
                u.setId(id);
                u.setUsername(username);
                u.setNombreCompleto(nombreCompleto);
                u.setCorreo(correo);
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
}
