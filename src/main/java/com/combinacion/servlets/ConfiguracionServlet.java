package com.combinacion.servlets;

import com.combinacion.dao.ConfiguracionDAO;
import com.combinacion.models.Configuracion;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ConfiguracionServlet", urlPatterns = {"/admin/configuracion"})
public class ConfiguracionServlet extends HttpServlet {

    private final ConfiguracionDAO dao = new ConfiguracionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "delete":
                eliminar(request, response);
                break;
            case "edit":
                editar(request, response);
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

        if ("save".equals(action)) {
            String idStr = request.getParameter("id");
            String clave = request.getParameter("clave");
            String valor = request.getParameter("valor");
            String descripcion = request.getParameter("descripcion");

            Configuracion c = new Configuracion();
            c.setClave(clave);
            c.setValor(valor);
            c.setDescripcion(descripcion);

            if (idStr != null && !idStr.isEmpty()) {
                c.setId(Integer.parseInt(idStr));
                dao.actualizar(c);
            } else {
                dao.insertar(c);
            }
            response.sendRedirect(request.getContextPath() + "/admin/configuracion?msg=success");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/configuracion");
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Configuracion> lista = dao.listarTodos();
        request.setAttribute("listaConfiguraciones", lista);
        request.getRequestDispatcher("/admin/configuracion.jsp").forward(request, response);
    }

    private void editar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        Configuracion c = dao.obtenerPorId(id);
        request.setAttribute("configEdit", c);
        listar(request, response); // Reuse the list page to show the form + list
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        dao.eliminar(id);
        response.sendRedirect(request.getContextPath() + "/admin/configuracion?msg=deleted");
    }
}
