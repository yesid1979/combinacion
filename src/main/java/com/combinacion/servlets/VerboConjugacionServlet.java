package com.combinacion.servlets;

import com.combinacion.dao.VerboConjugacionDAO;
import com.combinacion.models.Usuario;
import com.combinacion.models.VerboConjugacion;
import com.combinacion.services.AuthService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "VerboConjugacionServlet", urlPatterns = {"/verbos"})
public class VerboConjugacionServlet extends HttpServlet {

    private final VerboConjugacionDAO verboDao = new VerboConjugacionDAO();
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Usuario u = (Usuario) session.getAttribute("usuario");

        if (u == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        if (!authService.tienePermiso(u, "ADMIN_VER") && !authService.tienePermiso(u, "VERBOS_VER")) {
            request.setAttribute("error", "No tienes permisos para ver los verbos de conjugación.");
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        try {
            switch (action) {
                case "new":
                    if (!authService.tienePermiso(u, "ADMIN_VER") && !authService.tienePermiso(u, "VERBOS_CREAR")) {
                        throw new Exception("Sin permisos para crear.");
                    }
                    request.getRequestDispatcher("form_verbo.jsp").forward(request, response);
                    break;
                case "edit":
                    if (!authService.tienePermiso(u, "ADMIN_VER") && !authService.tienePermiso(u, "VERBOS_EDITAR")) {
                        throw new Exception("Sin permisos para editar.");
                    }
                    int id = Integer.parseInt(request.getParameter("id"));
                    VerboConjugacion verbo = verboDao.obtenerPorId(id);
                    request.setAttribute("verbo", verbo);
                    request.getRequestDispatcher("form_verbo.jsp").forward(request, response);
                    break;
                case "data":
                    devolverDatosDataTables(request, response, u);
                    break;
                default:
                    List<VerboConjugacion> lista = verboDao.listarTodos();
                    request.setAttribute("verbos", lista);
                    request.getRequestDispatcher("lista_verbos.jsp").forward(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if ("data".equals(action)) {
                com.google.gson.JsonObject errorJson = new com.google.gson.JsonObject();
                errorJson.addProperty("error", "Error interno: " + e.getMessage());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(errorJson.toString());
                return;
            }
            request.getSession().setAttribute("errorMsg", "Error: " + e.getMessage());
            response.sendRedirect("verbos");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Usuario u = (Usuario) session.getAttribute("usuario");

        if (u == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("data".equals(action)) {
                devolverDatosDataTables(request, response, u);
                return;
            } else if ("insert".equals(action)) {
                if (!authService.tienePermiso(u, "ADMIN_VER") && !authService.tienePermiso(u, "VERBOS_CREAR")) {
                    throw new Exception("Sin permisos para crear.");
                }
                String tercera = request.getParameter("terceraPersona");
                String primera = request.getParameter("primeraPersona");
                boolean activo = request.getParameter("activo") != null;

                VerboConjugacion verbo = new VerboConjugacion(0, tercera, primera, activo);
                if (verboDao.insertar(verbo)) {
                    session.setAttribute("successMsg", "Verbo agregado exitosamente.");
                } else {
                    session.setAttribute("errorMsg", "Error al agregar el verbo.");
                }

            } else if ("update".equals(action)) {
                if (!authService.tienePermiso(u, "ADMIN_VER") && !authService.tienePermiso(u, "VERBOS_EDITAR")) {
                    throw new Exception("Sin permisos para editar.");
                }
                int id = Integer.parseInt(request.getParameter("id"));
                String tercera = request.getParameter("terceraPersona");
                String primera = request.getParameter("primeraPersona");
                boolean activo = request.getParameter("activo") != null;

                VerboConjugacion verbo = new VerboConjugacion(id, tercera, primera, activo);
                if (verboDao.actualizar(verbo)) {
                    session.setAttribute("successMsg", "Verbo actualizado exitosamente.");
                } else {
                    session.setAttribute("errorMsg", "Error al actualizar el verbo.");
                }

            } else if ("delete".equals(action)) {
                if (!authService.tienePermiso(u, "ADMIN_VER") && !authService.tienePermiso(u, "VERBOS_ELIMINAR")) {
                    throw new Exception("Sin permisos para eliminar.");
                }
                int id = Integer.parseInt(request.getParameter("id"));
                if (verboDao.eliminar(id)) {
                    session.setAttribute("successMsg", "Verbo eliminado exitosamente.");
                } else {
                    session.setAttribute("errorMsg", "Error al eliminar el verbo.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if ("data".equals(action)) {
                com.google.gson.JsonObject errorJson = new com.google.gson.JsonObject();
                errorJson.addProperty("error", "Error interno: " + e.getMessage());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(errorJson.toString());
                return;
            }
            session.setAttribute("errorMsg", "Error: " + e.getMessage());
        }

        response.sendRedirect("verbos");
    }

    private void devolverDatosDataTables(HttpServletRequest request, HttpServletResponse response, Usuario u) throws IOException {
        String drawStr = request.getParameter("draw");
        int draw = (drawStr != null && !drawStr.isEmpty()) ? Integer.parseInt(drawStr) : 1;
        String startStr = request.getParameter("start");
        int start = (startStr != null && !startStr.isEmpty()) ? Integer.parseInt(startStr) : 0;
        String lengthStr = request.getParameter("length");
        int length = (lengthStr != null && !lengthStr.isEmpty()) ? Integer.parseInt(lengthStr) : 10;
        String searchValue = request.getParameter("search[value]");
        if (searchValue != null) searchValue = searchValue.toLowerCase();

        List<VerboConjugacion> listaTotal = verboDao.listarTodos();
        int recordsTotal = listaTotal.size();

        // Filtrado en memoria
        List<VerboConjugacion> listaFiltrada = new java.util.ArrayList<>();
        if (searchValue != null && !searchValue.isEmpty()) {
            for (VerboConjugacion verbo : listaTotal) {
                if (verbo.getTerceraPersona() != null && verbo.getTerceraPersona().toLowerCase().contains(searchValue) ||
                    verbo.getPrimeraPersona() != null && verbo.getPrimeraPersona().toLowerCase().contains(searchValue)) {
                    listaFiltrada.add(verbo);
                }
            }
        } else {
            listaFiltrada = listaTotal;
        }
        int recordsFiltered = listaFiltrada.size();

        // Paginación en memoria
        int toIndex = Math.min(start + length, listaFiltrada.size());
        List<VerboConjugacion> page = new java.util.ArrayList<>();
        if (start < listaFiltrada.size()) {
            page = listaFiltrada.subList(start, toIndex);
        }

        // Permisos para UI
        boolean canEdit = authService.tienePermiso(u, "ADMIN_VER") || authService.tienePermiso(u, "VERBOS_EDITAR");
        boolean canDelete = authService.tienePermiso(u, "ADMIN_VER") || authService.tienePermiso(u, "VERBOS_ELIMINAR");

        com.google.gson.JsonObject jsonResponse = new com.google.gson.JsonObject();
        jsonResponse.addProperty("draw", draw);
        jsonResponse.addProperty("recordsTotal", recordsTotal);
        jsonResponse.addProperty("recordsFiltered", recordsFiltered);

        com.google.gson.JsonArray dataArray = new com.google.gson.JsonArray();
        for (VerboConjugacion v : page) {
            com.google.gson.JsonObject row = new com.google.gson.JsonObject();
            row.addProperty("id", v.getId());
            row.addProperty("terceraPersona", v.getTerceraPersona());
            row.addProperty("primeraPersona", v.getPrimeraPersona());
            row.addProperty("activo", v.isActivo());
            row.addProperty("canEdit", canEdit);
            row.addProperty("canDelete", canDelete);
            dataArray.add(row);
        }
        jsonResponse.add("data", dataArray);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }
}
