package com.combinacion.util;

import com.combinacion.models.Usuario;
import com.combinacion.services.AuthService;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

// @WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    private final AuthService authService = new AuthService();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String servletPath = request.getServletPath();
        String action = request.getParameter("action");
        String method = request.getMethod();

        String path = servletPath;
        if (request.getPathInfo() != null) path += request.getPathInfo();
        if (path == null) path = "/";
        path = path.replace("//", "/");

        System.out.println("[FILTER] Ruta solicitada: " + path + ", Acción: " + action + ", Método: " + method);

        // 1. RECURSOS PÚBLICOS
        if (isPublicResource(path)) {
            System.out.println("[FILTER] Recurso público. Acceso permitido.");
            chain.doFilter(request, response);
            return;
        }

        // 2. SESIÓN
        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuario") : null;

        if (usuario == null) {
            System.out.println("[FILTER] Usuario no autenticado. Redirigiendo a login.");
            String requestedWith = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sesión expirada");
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            }
            return;
        }

        // 3. ADMIN
        if (usuario.getRolId() == 1 || usuario.esAdministrador()) {
            System.out.println("[FILTER] Usuario es administrador. Acceso total permitido.");
            request.setAttribute("usuarioLogueado", usuario);
            chain.doFilter(request, response);
            return;
        }

        // 3.5 EXCEPCIÓN: Todos los usuarios autenticados pueden ver su propio perfil
        if (path.equals("/perfil.jsp") || (path.equals("/usuarios") && !path.contains("/admin/"))) {
            System.out.println("[FILTER] Acceso a Mi Perfil permitido para todos los autenticados.");
            request.setAttribute("usuarioLogueado", usuario);
            chain.doFilter(request, response);
            return;
        }

        // 4. VALIDACIÓN DE SEGURIDAD
        boolean autorizado = authService.puedeAccederExtendido(usuario, path, action, method);

        if (!autorizado) {
            System.err.println("[FILTER] 403 FORBIDDEN: Usuario '" + usuario.getUsername() + "' denegado en '" + path + "' (Accion: " + action + ")");
            String requestedWith = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: " + path);
            } else {
                response.sendRedirect(request.getContextPath() + "/index.jsp?error=sin_permiso");
            }
            return;
        }

        request.setAttribute("usuarioLogueado", usuario);
        chain.doFilter(request, response);
    }

    private boolean isPublicResource(String path) {
        String p = path.toLowerCase();
        return p.equals("/login")
                || p.equals("/login.jsp")
                || p.endsWith("login.jsp")
                || p.contains("/assets/")
                || p.contains("/css/")
                || p.contains("/js/")
                || p.contains("/images/")
                || p.contains("/img/")
                || p.endsWith("/favicon.ico")
                || p.equals("/loginservlet")
                || p.equals("/logout");
    }

    @Override
    public void destroy() {}
}