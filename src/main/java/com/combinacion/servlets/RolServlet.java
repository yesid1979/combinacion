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

// @WebServlet(name = "RolServlet", urlPatterns = {"/admin/roles"})
public class RolServlet extends HttpServlet {
    private final RolService rolService = new RolService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";
        switch (action) {
            case "data": responderDatosTabla(request, response); break;
            case "new": mostrarFormulario(request, response, null); break;
            case "edit": editarRol(request, response); break;
            case "delete": eliminarRol(request, response); break;
            default: listar(request, response); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if ("data".equals(action)) responderDatosTabla(request, response);
        else if ("insert".equals(action)) insertarRol(request, response);
        else if ("update".equals(action)) actualizarRol(request, response);
        else listar(request, response);
    }

    private void responderDatosTabla(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int draw = parseInt(request.getParameter("draw"), 1);
        int start = parseInt(request.getParameter("start"), 0);
        int length = parseInt(request.getParameter("length"), 10);
        String search = request.getParameter("search[value]");
        int orderCol = parseInt(request.getParameter("order[0][column]"), 0);
        String orderDir = request.getParameter("order[0][dir]");
        if (orderDir == null) orderDir = "asc";
        String sortCol = (orderCol == 1) ? "nombre" : (orderCol == 2 ? "descripcion" : "id");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(rolService.generarJsonDataTables(draw, start, length, search, sortCol, orderDir));
    }

    private int parseInt(String val, int def) {
        try { return Integer.parseInt(val); } catch (Exception e) { return def; }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("listRoles", rolService.listarTodos());
        request.getRequestDispatcher("/admin/lista_roles.jsp").forward(request, response);
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response, Rol rol) throws ServletException, IOException {
        request.setAttribute("todosPermisos", rolService.listarPermisos());
        request.setAttribute("modulos", rolService.listarModulos());
        if (rol != null) request.setAttribute("rol_edit", rol);
        request.getRequestDispatcher("/admin/form_rol.jsp").forward(request, response);
    }

    private void editarRol(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Rol rol = rolService.obtenerPorId(id);
            if (rol != null) mostrarFormulario(request, response, rol);
            else response.sendRedirect("roles?status=notfound");
        } catch (Exception e) { response.sendRedirect("roles"); }
    }

    private void insertarRol(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String error = rolService.crear(request.getParameter("nombre"), request.getParameter("descripcion"), extraerIds(request));
        if (error != null) {
            request.setAttribute("error", error);
            Rol r = new Rol(); r.setNombre(request.getParameter("nombre")); r.setDescripcion(request.getParameter("descripcion"));
            mostrarFormulario(request, response, r);
        } else response.sendRedirect("roles?status=created");
    }

    private void actualizarRol(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            boolean activo = "on".equals(request.getParameter("activo")) || "true".equals(request.getParameter("activo"));
            String error = rolService.actualizar(id, request.getParameter("nombre"), request.getParameter("descripcion"), activo, extraerIds(request));
            if (error != null) {
                request.setAttribute("error", error);
                Rol r = new Rol(); r.setId(id); r.setNombre(request.getParameter("nombre")); r.setDescripcion(request.getParameter("descripcion")); r.setActivo(activo);
                mostrarFormulario(request, response, r);
            } else response.sendRedirect("roles?status=updated");
        } catch (Exception e) { response.sendRedirect("roles?status=error"); }
    }

    private void eliminarRol(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String error = rolService.eliminar(id);
            if (error != null) response.sendRedirect("roles?status=error&msg=" + java.net.URLEncoder.encode(error, "UTF-8"));
            else response.sendRedirect("roles?status=deleted");
        } catch (Exception e) { response.sendRedirect("roles?status=error"); }
    }

    private List<Integer> extraerIds(HttpServletRequest request) {
        List<Integer> ids = new ArrayList<>();
        String[] values = request.getParameterValues("permisos");
        if (values != null) for (String v : values) try { ids.add(Integer.parseInt(v)); } catch (Exception e) {}
        return ids;
    }
}
