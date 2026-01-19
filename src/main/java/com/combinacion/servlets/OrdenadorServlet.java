package com.combinacion.servlets;

import com.combinacion.dao.OrdenadorGastoDAO;
import com.combinacion.models.OrdenadorGasto;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "OrdenadorServlet", urlPatterns = { "/ordenadores" })
public class OrdenadorServlet extends HttpServlet {

    private OrdenadorGastoDAO ordenadorDAO = new OrdenadorGastoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null)
            action = "list";

        switch (action) {
            case "data":
                listOrdenadoresData(request, response);
                break;
            case "new":
                showNewForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteOrdenador(request, response);
                break;
            case "list":
            default:
                listOrdenadores(request, response);
                break;
        }
    }

    private void listOrdenadoresData(HttpServletRequest request, HttpServletResponse response) throws IOException { // Added
                                                                                                                    // missing
                                                                                                                    // IOException
        String draw = request.getParameter("draw");
        int start = 0;
        int length = 10;
        int orderColumn = 0;
        try {
            start = Integer.parseInt(request.getParameter("start"));
            length = Integer.parseInt(request.getParameter("length"));
            orderColumn = Integer.parseInt(request.getParameter("order[0][column]"));
        } catch (NumberFormatException e) {
        }

        String searchValue = request.getParameter("search[value]");
        String orderDir = request.getParameter("order[0][dir]");

        int total = ordenadorDAO.countAll();
        int filtered = ordenadorDAO.countFiltered(searchValue);
        List<OrdenadorGasto> list = ordenadorDAO.findWithPagination(start, length, searchValue, orderColumn, orderDir);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"draw\": ").append(draw != null ? draw : 1).append(",");
        json.append("\"recordsTotal\": ").append(total).append(",");
        json.append("\"recordsFiltered\": ").append(filtered).append(",");
        json.append("\"data\": [");

        for (int i = 0; i < list.size(); i++) {
            OrdenadorGasto o = list.get(i);
            json.append("[");
            json.append("\"").append(escapeJson(o.getNombreOrdenador())).append("\",");
            json.append("\"").append(escapeJson(o.getCargoOrdenador())).append("\",");
            json.append("\"").append(escapeJson(o.getOrganismo())).append("\",");
            json.append("\"").append(o.getId()).append("\"");
            json.append("]");
            if (i < list.size() - 1)
                json.append(",");
        }
        json.append("]}");
        response.getWriter().write(json.toString());
    }

    private String escapeJson(String s) {
        if (s == null)
            return "";
        return s.replace("\"", "\\\"").replace("\\", "\\\\");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if ("insert".equals(action)) {
            insertOrdenador(request, response);
        } else if ("update".equals(action)) {
            updateOrdenador(request, response);
        } else if ("data".equals(action)) {
            listOrdenadoresData(request, response);
        } else {
            listOrdenadores(request, response);
        }
    }

    private void listOrdenadores(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<OrdenadorGasto> list = ordenadorDAO.listarTodos();
        request.setAttribute("listOrdenadores", list);
        request.getRequestDispatcher("lista_ordenadores.jsp").forward(request, response);
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("form_ordenador.jsp").forward(request, response);
    }

    private void insertOrdenador(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            OrdenadorGasto o = new OrdenadorGasto();
            o.setOrganismo(request.getParameter("organismo"));
            o.setDireccionOrganismo(request.getParameter("direccion_organismo"));
            o.setNombreOrdenador(request.getParameter("nombre_ordenador"));
            o.setCedulaOrdenador(request.getParameter("cedula_ordenador"));
            o.setCargoOrdenador(request.getParameter("cargo_ordenador"));
            o.setDecretoNombramiento(request.getParameter("decreto_nombramiento"));
            o.setActaPosesion(request.getParameter("acta_posesion"));

            if (ordenadorDAO.insertar(o)) {
                response.sendRedirect("ordenadores?status=created");
            } else {
                request.setAttribute("error", "Error al guardar el ordenador.");
                request.getRequestDispatcher("form_ordenador.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error interno: " + e.getMessage());
            request.getRequestDispatcher("form_ordenador.jsp").forward(request, response);
        }
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            OrdenadorGasto existing = ordenadorDAO.obtenerPorId(id);
            if (existing != null) {
                request.setAttribute("ordenador", existing);
                request.getRequestDispatcher("form_ordenador.jsp").forward(request, response);
            } else {
                response.sendRedirect("ordenadores?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("ordenadores?action=list");
        }
    }

    private void updateOrdenador(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            OrdenadorGasto o = new OrdenadorGasto();
            o.setId(id);
            o.setOrganismo(request.getParameter("organismo"));
            o.setDireccionOrganismo(request.getParameter("direccion_organismo"));
            o.setNombreOrdenador(request.getParameter("nombre_ordenador"));
            o.setCedulaOrdenador(request.getParameter("cedula_ordenador"));
            o.setCargoOrdenador(request.getParameter("cargo_ordenador"));
            o.setDecretoNombramiento(request.getParameter("decreto_nombramiento"));
            o.setActaPosesion(request.getParameter("acta_posesion"));

            if (ordenadorDAO.actualizar(o)) {
                response.sendRedirect("ordenadores?status=updated");
            } else {
                request.setAttribute("error", "Error al actualizar el ordenador.");
                request.setAttribute("ordenador", o);
                request.getRequestDispatcher("form_ordenador.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error interno: " + e.getMessage());
            request.getRequestDispatcher("form_ordenador.jsp").forward(request, response);
        }
    }

    private void deleteOrdenador(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            ordenadorDAO.eliminar(id);
            response.sendRedirect("ordenadores?status=deleted");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("ordenadores?status=error");
        }
    }
}
