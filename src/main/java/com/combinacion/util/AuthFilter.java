package com.combinacion.util;

import com.combinacion.models.Usuario;
import com.combinacion.services.AuthService;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filtro de seguridad que protege todas las rutas de la aplicación.
 * Verifica autenticación y autorización en cada request.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    private final AuthService authService = new AuthService();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nada que inicializar
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        // Rutas públicas que no requieren autenticación
        if (isPublicResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Verificar sesión activa
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(contextPath + "/login");
            return;
        }

        // Verificar autorización
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (!authService.puedeAcceder(usuario, path)) {
            response.sendRedirect(contextPath + "/index.jsp?error=sin_permiso");
            return;
        }

        // Hacer el usuario disponible como atributo del request
        request.setAttribute("usuarioLogueado", usuario);

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Nada que limpiar
    }

    /**
     * Determina si una ruta es pública (no requiere autenticación).
     */
    private boolean isPublicResource(String path) {
        return path.equals("/login")
            || path.equals("/login.jsp")
            || path.equals("/LoginServlet")
            || path.startsWith("/assets/")
            || path.equals("/favicon.ico")
            || path.endsWith(".css")
            || path.endsWith(".js")
            || path.endsWith(".png")
            || path.endsWith(".jpg")
            || path.endsWith(".jpeg")
            || path.endsWith(".gif")
            || path.endsWith(".ico")
            || path.endsWith(".woff")
            || path.endsWith(".woff2")
            || path.endsWith(".ttf");
    }
}
