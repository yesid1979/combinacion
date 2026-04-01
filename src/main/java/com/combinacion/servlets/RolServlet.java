package com.combinacion.servlets;

import com.combinacion.models.Permiso;
import com.combinacion.models.Rol;
import com.combinacion.services.RolService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador HTTP para la gestión de Roles y Permisos.
 */
@WebServlet(name = "RolServlet", urlPatterns = {"/admin/roles"})
public class RolServlet extends HttpServlet {

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
                editarRol(request, response);
                break;
            case "delete":
                eliminarRol(request, response);
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
            insertarRol(request, response);
        } else if ("update".equals(action)) {
            actualizarRol(request, response);
        } else {
            listar(request, response);
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Rol> roles = rolService.listarTodos();
        request.setAttribute("listRoles", roles);
        request.getRequestDispatcher("/admin/lista_roles.jsp").forward(request, response);
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                    Rol rol) throws ServletException, IOException {
        List<Permiso> todosPermisos = rolService.listarPermisos();
        List<String> modulos = rolService.listarModulos();
        request.setAttribute("todosPermisos", todosPermisos);
        request.setAttribute("modulos", modulos);
        if (rol != null) {
            request.setAttribute("rol_edit", rol);
        }
        request.getRequestDispatcher("/admin/form_rol.jsp").forward(request, response);
    }

    private void editarRol(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Rol rol = rolService.obtenerPorId(id);
            if (rol != null) {
                mostrarFormulario(request, response, rol);
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/roles?status=notfound");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/roles");
        }
    }

    private void insertarRol(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nombre = request.getParameter("nombre");
        String descripcion = request.getParameter("descripcion");
        List<Integer> permisosIds = extraerPermisosIds(request);

        String error = rolService.crear(nombre, descripcion, permisosIds);
        if (error != null) {
            request.setAttribute("error", error);
            Rol r = new Rol();
            r.setNombre(nombre);
            r.setDescripcion(descripcion);
            mostrarFormulario(request, response, r);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/roles?status=created");
        }
    }

    private void actualizarRol(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String nombre = request.getParameter("nombre");
            String descripcion = request.getParameter("descripcion");
            boolean activo = "on".equals(request.getParameter("activo")) || "true".equals(request.getParameter("activo"));
            List<Integer> permisosIds = extraerPermisosIds(request);

            String error = rolService.actualizar(id, nombre, descripcion, activo, permisosIds);
            if (error != null) {
                request.setAttribute("error", error);
                Rol r = new Rol();
                r.setId(id);
                r.setNombre(nombre);
                r.setDescripcion(descripcion);
                r.setActivo(activo);
                mostrarFormulario(request, response, r);
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/roles?status=updated");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/roles?status=error");
        }
    }

    private void eliminarRol(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String error = rolService.eliminar(id);
            if (error != null) {
                response.sendRedirect(request.getContextPath() + "/admin/roles?status=error&msg=" 
                    + java.net.URLEncoder.encode(error, "UTF-8"));
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/roles?status=deleted");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/roles?status=error");
        }
    }

    /**
     * Extrae los IDs de permisos seleccionados del formulario (checkboxes).
     */
    private List<Integer> extraerPermisosIds(HttpServletRequest request) {
        List<Integer> ids = new ArrayList<>();
        String[] values = request.getParameterValues("permisos");
        if (values != null) {
            for (String val : values) {
                try {
                    ids.add(Integer.parseInt(val));
                } catch (NumberFormatException ignored) {}
            }
        }
        return ids;
    }
}
