package com.combinacion.servlets;

import com.combinacion.models.Contratista;
import com.combinacion.services.ContratistaService;
import com.combinacion.services.AuthService;
import com.combinacion.models.Usuario;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// @WebServlet(name = "ContratistaServlet", urlPatterns = {"/contratistas"})
public class ContratistaServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ContratistaServlet.class.getName());

    // Constantes para acciones
    private static final String ACTION_LIST = "list";
    private static final String ACTION_SEARCH = "search";
    private static final String ACTION_DATA = "data";
    private static final String ACTION_NEW = "new";
    private static final String ACTION_VIEW = "view";
    private static final String ACTION_EDIT = "edit";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_INSERT = "insert";
    private static final String ACTION_UPDATE = "update";

    // Constantes para permisos
    private static final String PERMISO_CREAR = "CONTRATISTAS_CREAR";
    private static final String PERMISO_EDITAR = "CONTRATISTAS_EDITAR";
    private static final String PERMISO_ELIMINAR = "CONTRATISTAS_ELIMINAR";

    private final ContratistaService contratistaService = new ContratistaService();
    private final AuthService authService = new AuthService();

    private Usuario getUsuario(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (Usuario) session.getAttribute("usuario") : null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) {
            action = ACTION_LIST;
        }

        Usuario u = getUsuario(request);
        switch (action) {
            case ACTION_SEARCH:
                buscarPorCedula(request, response);
                break;
            case ACTION_DATA:
                responderDatosTabla(request, response);
                break;
            case ACTION_NEW:
                if (authService.tienePermiso(u, PERMISO_CREAR)) {
                    request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
                } else {
                    response.sendRedirect("contratistas?error=sin_permiso");
                }
                break;
            case ACTION_VIEW:
                request.setAttribute("readonly", true);
            case ACTION_EDIT:
                if (ACTION_EDIT.equals(action) && !authService.tienePermiso(u, PERMISO_EDITAR)) {
                    response.sendRedirect("contratistas?error=sin_permiso");
                } else {
                    mostrarFormularioEdicion(request, response);
                }
                break;
            case ACTION_DELETE:
                if (authService.tienePermiso(u, PERMISO_ELIMINAR)) {
                    eliminar(request, response);
                } else {
                    response.sendRedirect("contratistas?error=sin_permiso");
                }
                break;
            case ACTION_LIST:
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

        switch (action) {
            case ACTION_INSERT:
                if (authService.tienePermiso(u, PERMISO_CREAR)) {
                    insertar(request, response);
                } else {
                    response.sendRedirect("contratistas?error=sin_permiso");
                }
                break;
            case ACTION_UPDATE:
                if (authService.tienePermiso(u, PERMISO_EDITAR)) {
                    actualizar(request, response);
                } else {
                    response.sendRedirect("contratistas?error=sin_permiso");
                }
                break;
            case ACTION_DATA:
                responderDatosTabla(request, response);
                break;
            case ACTION_SEARCH:
                buscarPorCedula(request, response);
                break;
            default:
                listar(request, response);
                break;
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Contratista> list = contratistaService.listarTodos();
            request.setAttribute("listContratistas", list);
            request.getRequestDispatcher("lista_contratistas.jsp").forward(request, response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al listar contratistas", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al listar contratistas");
        }
    }

    private void responderDatosTabla(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            // Parámetros de DataTables
            String draw = request.getParameter("draw");
            int start = parseIntSafe(request.getParameter("start"), 0);
            int length = parseIntSafe(request.getParameter("length"), 10);
            String search = request.getParameter("search[value]");
            int orderCol = parseIntSafe(request.getParameter("order[0][column]"), 1);
            String orderDir = request.getParameter("order[0][dir]");
            if (orderDir == null) {
                orderDir = "asc";
            }

            String source = request.getParameter("source");
            boolean soloAdiciones = "true".equals(request.getParameter("filterAdicion"));

            // Validación de parámetros críticos
            if (source == null || source.isEmpty()) {
                source = "lista"; // Default source
            }

            // Resolver columna de ordenamiento
            String sortCol = resolverColumnaOrden(source, orderCol);
            if (sortCol == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Columna de ordenamiento no válida");
                return;
            }

            // Log para depuración
            logger.info(String.format(
                    "DataTables Request - Draw: %s, Start: %d, Length: %d, Search: %s, Order: %s %s, Source: %s",
                    draw, start, length, search, sortCol, orderDir, source));

            // Configurar respuesta
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-store");

            // Generar y enviar JSON
            String jsonResponse = contratistaService.generarJsonDataTables(
                    parseIntSafe(draw, 1), start, length, search, sortCol, orderDir, soloAdiciones);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al procesar la solicitud DataTables", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al procesar la solicitud");
        }
    }

    private void buscarPorCedula(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String cedula = request.getParameter("cedula");
            if (cedula == null || cedula.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parámetro 'cedula' es obligatorio");
                return;
            }

            Contratista c = contratistaService.obtenerPorCedula(cedula);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(contratistaService.generarJsonBusqueda(c));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al buscar contratista por cédula", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al buscar contratista");
        }
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                response.sendRedirect("contratistas?action=list&error=invalid_id");
                return;
            }

            int id = Integer.parseInt(idParam);
            Contratista existing = contratistaService.obtenerPorId(id);
            if (existing != null) {
                request.setAttribute("contratista", existing);
                if (ACTION_VIEW.equals(request.getParameter("action"))) {
                    request.setAttribute("readonly", true);
                }
                request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
            } else {
                response.sendRedirect("contratistas?action=list&error=not_found&id=" + idParam);
            }
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "ID inválido: " + request.getParameter("id"), e);
            response.sendRedirect("contratistas?action=list&error=invalid_id");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al mostrar formulario de edición", e);
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
            logger.log(Level.SEVERE, "Error al insertar contratista", e);
            request.setAttribute("error", "Error interno: " + e.getMessage());
            request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
        }
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                response.sendRedirect("contratistas?action=list&error=invalid_id");
                return;
            }

            int id = Integer.parseInt(idParam);
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
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "ID inválido: " + request.getParameter("id"), e);
            response.sendRedirect("contratistas?action=list&error=invalid_id");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al actualizar contratista", e);
            request.setAttribute("error", "Error interno: " + e.getMessage());
            request.getRequestDispatcher("form_contratista.jsp").forward(request, response);
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                response.sendRedirect("contratistas?status=error&msg=invalid_id");
                return;
            }

            int id = Integer.parseInt(idParam);
            contratistaService.eliminar(id);
            response.sendRedirect("contratistas?status=deleted");
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "ID inválido: " + request.getParameter("id"), e);
            response.sendRedirect("contratistas?status=error&msg=invalid_id");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al eliminar contratista", e);
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
            // Default source or "lista"
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
        if (val == null) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}