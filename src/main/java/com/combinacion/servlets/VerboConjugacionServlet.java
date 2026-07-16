package com.combinacion.servlets;

import com.combinacion.dao.VerboConjugacionDAO;
import com.combinacion.models.Usuario;
import com.combinacion.models.VerboConjugacion;
import com.combinacion.services.AuthService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "VerboConjugacionServlet", urlPatterns = {"/verbos"})
public class VerboConjugacionServlet extends HttpServlet {

    private final VerboConjugacionDAO verboDao = new VerboConjugacionDAO();
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Usuario u = (Usuario) session.getAttribute("usuario");

        if (u == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        if (!authService.tienePermiso(u, "ADMIN_VER") && !authService.tienePermiso(u, "VERBOS_VER")) {
            request.setAttribute("error", "No tienes permisos para ver los verbos de conjugación.");
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        try {
            switch (action) {
                case "new":
                    if (!authService.tienePermiso(u, "ADMIN_VER") && !authService.tienePermiso(u, "VERBOS_CREAR")) {
                        throw new Exception("Sin permisos para crear.");
                    }
                    request.getRequestDispatcher("form_verbo.jsp").forward(request, response);
                    break;
                case "edit":
                    if (!authService.tienePermiso(u, "ADMIN_VER") && !authService.tienePermiso(u, "VERBOS_EDITAR")) {
                        throw new Exception("Sin permisos para editar.");
                    }
                    int id = Integer.parseInt(request.getParameter("id"));
                    VerboConjugacion verbo = verboDao.obtenerPorId(id);
                    request.setAttribute("verbo", verbo);
                    request.getRequestDispatcher("form_verbo.jsp").forward(request, response);
                    break;
                default:
                    List<VerboConjugacion> lista = verboDao.listarTodos();
                    request.setAttribute("verbos", lista);
                    request.getRequestDispatcher("lista_verbos.jsp").forward(request, response);
                    break;
            }
        } catch (Exception e) {
            request.getSession().setAttribute("errorMsg", "Error: " + e.getMessage());
            response.sendRedirect("verbos");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Usuario u = (Usuario) session.getAttribute("usuario");

        if (u == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("insert".equals(action)) {
                if (!authService.tienePermiso(u, "ADMIN_VER") && !authService.tienePermiso(u, "VERBOS_CREAR")) {
                    throw new Exception("Sin permisos para crear.");
                }
                String tercera = request.getParameter("terceraPersona");
                String primera = request.getParameter("primeraPersona");
                boolean activo = request.getParameter("activo") != null;

                VerboConjugacion verbo = new VerboConjugacion(0, tercera, primera, activo);
                if (verboDao.insertar(verbo)) {
                    session.setAttribute("successMsg", "Verbo agregado exitosamente.");
                } else {
                    session.setAttribute("errorMsg", "Error al agregar el verbo.");
                }

            } else if ("update".equals(action)) {
                if (!authService.tienePermiso(u, "ADMIN_VER") && !authService.tienePermiso(u, "VERBOS_EDITAR")) {
                    throw new Exception("Sin permisos para editar.");
                }
                int id = Integer.parseInt(request.getParameter("id"));
                String tercera = request.getParameter("terceraPersona");
                String primera = request.getParameter("primeraPersona");
                boolean activo = request.getParameter("activo") != null;

                VerboConjugacion verbo = new VerboConjugacion(id, tercera, primera, activo);
                if (verboDao.actualizar(verbo)) {
                    session.setAttribute("successMsg", "Verbo actualizado exitosamente.");
                } else {
                    session.setAttribute("errorMsg", "Error al actualizar el verbo.");
                }

            } else if ("delete".equals(action)) {
                if (!authService.tienePermiso(u, "ADMIN_VER") && !authService.tienePermiso(u, "VERBOS_ELIMINAR")) {
                    throw new Exception("Sin permisos para eliminar.");
                }
                int id = Integer.parseInt(request.getParameter("id"));
                if (verboDao.eliminar(id)) {
                    session.setAttribute("successMsg", "Verbo eliminado exitosamente.");
                } else {
                    session.setAttribute("errorMsg", "Error al eliminar el verbo.");
                }
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Error: " + e.getMessage());
        }

        response.sendRedirect("verbos");
    }
}
