package com.combinacion.servlets;

import com.combinacion.dao.ContratistaDAO;
import com.combinacion.models.Contratista;
import com.combinacion.util.ParseUtils;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ContratistaServlet", urlPatterns = { "/contratistas" })
public class ContratistaServlet extends HttpServlet {

    private ContratistaDAO contratistaDAO = new ContratistaDAO();

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
            case "edit":
                // showEditForm(request, response); // Implement later
                break;
            case "list":
            default:
                listContratistas(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("insert".equals(action)) {
            insertContratista(request, response);
        } else {
            listContratistas(request, response);
        }
    }

    private void listContratistas(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Contratista> list = contratistaDAO.listarTodos();
        request.setAttribute("listContratistas", list);
        request.getRequestDispatcher("lista_contratistas.jsp").forward(request, response);
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
    }

    private void insertContratista(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Contratista c = new Contratista();
            c.setCedula(request.getParameter("cedula"));
            c.setDv(request.getParameter("dv"));
            c.setNombre(request.getParameter("nombre"));
            c.setTelefono(request.getParameter("telefono"));
            c.setCorreo(request.getParameter("correo"));
            c.setDireccion(request.getParameter("direccion"));
            c.setFechaNacimiento(ParseUtils.parseDate(request.getParameter("fecha_nacimiento")));
            c.setEdad(ParseUtils.parseInt(request.getParameter("edad")));
            // Add other fields as needed based on form

            if (contratistaDAO.insertar(c)) {
                // Success
                response.sendRedirect("contratistas?action=list");
            } else {
                request.setAttribute("error", "Error al guardar. Verifique si la cédula ya existe.");
                request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error interno: " + e.getMessage());
            request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
        }
    }
}
