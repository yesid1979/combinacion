package com.combinacion.servlets;

import com.combinacion.models.Contratista;
import com.combinacion.services.ContratistaService;
import com.combinacion.services.AuthService;
import com.combinacion.models.Usuario;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Controlador HTTP para Contratista.
 */
@WebServlet(name = "ContratistaServlet", urlPatterns = { "/contratistas" })
public class ContratistaServlet extends HttpServlet {

    private final ContratistaService contratistaService = new ContratistaService();
    private final AuthService authService = new AuthService();

    private Usuario getUsuario(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (Usuario) session.getAttribute("usuario") : null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        Usuario u = getUsuario(request);
        switch (action) {
            case "search":
                buscarPorCedula(request, response);
                break;
            case "data":
                responderDatosTabla(request, response);
                break;
            case "new":
                if (authService.tienePermiso(u, "CONTRATISTAS_CREAR")) {
                    request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
                } else {
                    response.sendRedirect("contratistas?error=sin_permiso");
                }
                break;
            case "view":
            case "edit":
                if ("view".equals(action)) {
                    // response.getWriter().write("LLEGO A VIEW");
                    // response.getWriter().flush();
                    // if(true) return;
                    request.setAttribute("readonly", true);
                }
                if ("edit".equals(action) && !authService.tienePermiso(u, "CONTRATISTAS_EDITAR")) {
                    response.sendRedirect("contratistas?error=sin_permiso");
                } else {
                    mostrarFormularioEdicion(request, response);
                }
                break;
            case "delete":
                if (authService.tienePermiso(u, "CONTRATISTAS_ELIMINAR")) {
                    eliminar(request, response);
                } else {
                    response.sendRedirect("contratistas?error=sin_permiso");
                }
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
        Usuario u = getUsuario(request);

        if ("insert".equals(action)) {
            if (authService.tienePermiso(u, "CONTRATISTAS_CREAR")) {
                insertar(request, response);
            } else {
                response.sendRedirect("contratistas?error=sin_permiso");
            }
        } else if ("update".equals(action)) {
            if (authService.tienePermiso(u, "CONTRATISTAS_EDITAR")) {
                actualizar(request, response);
            } else {
                response.sendRedirect("contratistas?error=sin_permiso");
            }
        } else if ("data".equals(action)) {
            responderDatosTabla(request, response);
        } else if ("search".equals(action)) {
            buscarPorCedula(request, response);
        } else {
            listar(request, response);
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Contratista> list = contratistaService.listarTodos();
        request.setAttribute("listContratistas", list);
        request.getRequestDispatcher("lista_contratistas.jsp").forward(request, response);
    }

    private void responderDatosTabla(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String draw     = request.getParameter("draw");
        int    start    = parseIntSafe(request.getParameter("start"),            0);
        int    length   = parseIntSafe(request.getParameter("length"),          10);
        String search   = request.getParameter("search[value]");
        int    orderCol = parseIntSafe(request.getParameter("order[0][column]"), 1);
        String orderDir = request.getParameter("order[0][dir]");
        if (orderDir == null) orderDir = "asc";

        String source  = request.getParameter("source");
        String sortCol = resolverColumnaOrden(source, orderCol);
        
        boolean soloAdiciones = "true".equals(request.getParameter("filterAdicion"));

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(
            contratistaService.generarJsonDataTables(
                parseIntSafe(draw, 1), start, length, search, sortCol, orderDir, soloAdiciones)
        );
        response.getWriter().flush();
    }

    private void buscarPorCedula(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String cedula = request.getParameter("cedula");
        Contratista c = contratistaService.obtenerPorCedula(cedula);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(contratistaService.generarJsonBusqueda(c));
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Contratista existing = contratistaService.obtenerPorId(id);
            if (existing != null) {
                request.setAttribute("contratista", existing);
                if ("view".equals(request.getParameter("action"))) {
                    request.setAttribute("readonly", true);
                }
                request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
            } else {
                response.sendRedirect("contratistas?action=list&error=not_found&id=" + request.getParameter("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("contratistas?action=list&error=exception&msg=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }

    private void insertar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Contratista c = contratistaService.construirDesdeParametros(
                request.getParameter("cedula"),
                request.getParameter("dv"),
                request.getParameter("nombre"),
                request.getParameter("telefono"),
                request.getParameter("correo"),
                request.getParameter("direccion"),
                request.getParameter("fecha_nacimiento"),
                request.getParameter("edad"),
                request.getParameter("formacion_titulo"),
                request.getParameter("descripcion_formacion"),
                request.getParameter("experiencia"),
                request.getParameter("descripcion_experiencia"),
                request.getParameter("tarjeta_profesional"),
                request.getParameter("descripcion_tarjeta"),
                request.getParameter("restricciones")
            );
            String error = contratistaService.insertar(c);
            if (error != null) {
                request.setAttribute("error", error);
                request.setAttribute("contratista", c);
                request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
            } else {
                response.sendRedirect("contratistas?status=created");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error interno: " + e.getMessage());
            request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
        }
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Contratista c = contratistaService.obtenerPorId(id);
            if (c == null) {
                response.sendRedirect("contratistas?action=list");
                return;
            }
            c = contratistaService.construirDesdeParametros(
                request.getParameter("cedula"),
                request.getParameter("dv"),
                request.getParameter("nombre"),
                request.getParameter("telefono"),
                request.getParameter("correo"),
                request.getParameter("direccion"),
                request.getParameter("fecha_nacimiento"),
                request.getParameter("edad"),
                request.getParameter("formacion_titulo"),
                request.getParameter("descripcion_formacion"),
                request.getParameter("experiencia"),
                request.getParameter("descripcion_experiencia"),
                request.getParameter("tarjeta_profesional"),
                request.getParameter("descripcion_tarjeta"),
                request.getParameter("restricciones")
            );
            c.setId(id);
            String error = contratistaService.actualizar(id, c);
            if (error != null) {
                request.setAttribute("error", error);
                request.setAttribute("contratista", c);
                request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
            } else {
                response.sendRedirect("contratistas?status=updated");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error interno: " + e.getMessage());
            request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            contratistaService.eliminar(id);
            response.sendRedirect("contratistas?status=deleted");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("contratistas?status=error");
        }
    }

    private String resolverColumnaOrden(String source, int orderColumn) {
        if ("combinacion".equals(source)) {
            switch (orderColumn) {
                case 1: return "numero_contrato";
                case 2: return "cedula";
                case 3: return "nombre";
                case 4: return "correo";
                case 5: return "telefono";
                default: return "numero_contrato";
            }
        } else {
            switch (orderColumn) {
                case 0: return "cedula";
                case 1: return "nombre";
                case 2: return "correo";
                case 3: return "telefono";
                default: return "nombre";
            }
        }
    }

    private int parseIntSafe(String val, int defaultVal) {
        if (val == null) return defaultVal;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return defaultVal; }
    }
}
