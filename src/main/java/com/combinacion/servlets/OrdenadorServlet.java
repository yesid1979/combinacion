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
            case "new":
                showNewForm(request, response);
                break;
            case "list":
            default:
                listOrdenadores(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("insert".equals(action)) {
            insertOrdenador(request, response);
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
                response.sendRedirect("ordenadores?action=list");
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
}
