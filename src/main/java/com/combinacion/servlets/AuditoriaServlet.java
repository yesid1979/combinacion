package com.combinacion.servlets;
import com.combinacion.models.Usuario;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin/auditoria")
public class AuditoriaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario u = (Usuario) request.getSession().getAttribute("usuario");
        if (u == null || (!u.esAdministrador() && !u.tienePermiso("AUDITORIA_VER"))) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        // Ahora todo carga por AJAX, solo mostramos la vista
        request.getRequestDispatcher("auditoria.jsp").forward(request, response);
    }
}
