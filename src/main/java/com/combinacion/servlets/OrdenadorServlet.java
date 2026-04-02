package com.combinacion.servlets;

import com.combinacion.models.OrdenadorGasto;
import com.combinacion.services.OrdenadorService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador HTTP para OrdenadorGasto.
 * Responsabilidad exclusiva: leer parámetros HTTP, delegar al Service,
 * y dirigir la respuesta a la Vista correcta.
 */
@WebServlet(name = "OrdenadorServlet", urlPatterns = { "/ordenadores" })
public class OrdenadorServlet extends HttpServlet {

    private final OrdenadorService ordenadorService = new OrdenadorService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "data":
                responderDatosTabla(request, response);
                break;
            case "new":
                request.getRequestDispatcher("form_ordenador.jsp").forward(request, response);
                break;
            case "view":
            case "edit":
                mostrarFormularioEdicion(request, response);
                break;
            case "delete":
                eliminar(request, response);
                break;
            case "list":
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
            insertar(request, response);
        } else if ("update".equals(action)) {
            actualizar(request, response);
        } else if ("data".equals(action)) {
            responderDatosTabla(request, response);
        } else {
            listar(request, response);
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<OrdenadorGasto> list = ordenadorService.listarTodos();
        request.setAttribute("listOrdenadores", list);
        request.getRequestDispatcher("lista_ordenadores.jsp").forward(request, response);
    }

    private void responderDatosTabla(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String draw       = request.getParameter("draw");
        int    start      = parseIntSafe(request.getParameter("start"), 0);
        int    length     = parseIntSafe(request.getParameter("length"), 10);
        String search     = request.getParameter("search[value]");
        int    orderCol   = parseIntSafe(request.getParameter("order[0][column]"), 0);
        String orderDir   = request.getParameter("order[0][dir]");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
            ordenadorService.generarJsonDataTables(draw, start, length, search, orderCol, orderDir)
        );
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            OrdenadorGasto existing = ordenadorService.obtenerPorId(id);
            if (existing != null) {
                request.setAttribute("ordenador", existing);
                if ("view".equals(request.getParameter("action"))) {
                    request.setAttribute("readonly", true);
                }
                request.getRequestDispatcher("form_ordenador.jsp").forward(request, response);
            } else {
                response.sendRedirect("ordenadores?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("ordenadores?action=list");
        }
    }

    private void insertar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            OrdenadorGasto o = ordenadorService.construirDesdeParametros(
                request.getParameter("organismo"),
                request.getParameter("direccion_organismo"),
                request.getParameter("nombre_ordenador"),
                request.getParameter("cedula_ordenador"),
                request.getParameter("cargo_ordenador"),
                request.getParameter("decreto_nombramiento"),
                request.getParameter("acta_posesion")
            );
            String error = ordenadorService.insertar(o);
            if (error != null) {
                request.setAttribute("error", error);
                request.getRequestDispatcher("form_ordenador.jsp").forward(request, response);
            } else {
                response.sendRedirect("ordenadores?status=created");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error interno: " + e.getMessage());
            request.getRequestDispatcher("form_ordenador.jsp").forward(request, response);
        }
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            OrdenadorGasto o = ordenadorService.construirDesdeParametros(
                request.getParameter("organismo"),
                request.getParameter("direccion_organismo"),
                request.getParameter("nombre_ordenador"),
                request.getParameter("cedula_ordenador"),
                request.getParameter("cargo_ordenador"),
                request.getParameter("decreto_nombramiento"),
                request.getParameter("acta_posesion")
            );
            o.setId(id);
            String error = ordenadorService.actualizar(o);
            if (error != null) {
                request.setAttribute("error", error);
                request.setAttribute("ordenador", o);
                request.getRequestDispatcher("form_ordenador.jsp").forward(request, response);
            } else {
                response.sendRedirect("ordenadores?status=updated");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error interno: " + e.getMessage());
            request.getRequestDispatcher("form_ordenador.jsp").forward(request, response);
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            ordenadorService.eliminar(id);
            response.sendRedirect("ordenadores?status=deleted");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("ordenadores?status=error");
        }
    }

    private int parseIntSafe(String val, int defaultVal) {
        if (val == null) return defaultVal;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return defaultVal; }
    }
}
