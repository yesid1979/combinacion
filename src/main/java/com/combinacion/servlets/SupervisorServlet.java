package com.combinacion.servlets;

import com.combinacion.dao.SupervisorDAO;
import com.combinacion.models.Supervisor;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "SupervisorServlet", urlPatterns = { "/supervisores" })
public class SupervisorServlet extends HttpServlet {

    private SupervisorDAO supervisorDAO = new SupervisorDAO();

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
                listSupervisores(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("insert".equals(action)) {
            insertSupervisor(request, response);
        } else {
            listSupervisores(request, response);
        }
    }

    private void listSupervisores(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Supervisor> list = supervisorDAO.listarTodos();
        request.setAttribute("listSupervisores", list);
        request.getRequestDispatcher("lista_supervisores.jsp").forward(request, response);
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("form_supervisor.jsp").forward(request, response);
    }

    private void insertSupervisor(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Supervisor s = new Supervisor();
            s.setCedula(request.getParameter("cedula"));
            s.setNombre(request.getParameter("nombre"));
            s.setCargo(request.getParameter("cargo"));

            if (supervisorDAO.insertar(s)) {
                response.sendRedirect("supervisores?action=list");
            } else {
                request.setAttribute("error", "Error al guardar el supervisor.");
                request.getRequestDispatcher("form_supervisor.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error interno: " + e.getMessage());
            request.getRequestDispatcher("form_supervisor.jsp").forward(request, response);
        }
    }
}
