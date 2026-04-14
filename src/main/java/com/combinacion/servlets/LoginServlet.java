package com.combinacion.servlets;

import com.combinacion.models.Usuario;
import com.combinacion.services.AuthService;
import com.combinacion.services.LoginResult;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet para el proceso de inicio de sesión.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Si ya está autenticado, redirigir al inicio
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("usuario") != null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String errorParam = request.getParameter("error");
        if ("session_expired".equals(errorParam)) {
            request.setAttribute("error", "Tu sesión ha expirado o no has iniciado sesión. Por favor, ingresa de nuevo.");
        }

        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        LoginResult result = authService.autenticarDetallado(username, password);

        if (result.isExitoso()) {
            Usuario usuario = result.getUsuario();
            // Login exitoso: crear sesión
            HttpSession session = request.getSession(true);
            session.setAttribute("usuario", usuario);
            session.setAttribute("nombreUsuario", usuario.getNombreCompleto());
            session.setAttribute("rolNombre", usuario.getRol() != null ? usuario.getRol().getNombre() : "");
            session.setMaxInactiveInterval(30 * 60); // 30 minutos

            response.sendRedirect(request.getContextPath() + "/index.jsp");
        } else {
            // Login fallido con mensaje detallado
            request.setAttribute("error", result.getMensaje());
            request.setAttribute("username", username);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}
