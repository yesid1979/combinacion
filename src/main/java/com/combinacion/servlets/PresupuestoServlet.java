package com.combinacion.servlets;

import com.combinacion.services.PresupuestoService;
import com.combinacion.models.PresupuestoDetalle;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador HTTP para PresupuestoDetalle.
 * Responsabilidad exclusiva: leer parámetros HTTP, delegar al Service,
 * y dirigir la respuesta a la Vista correcta.
 */
@WebServlet(name = "PresupuestoServlet", urlPatterns = { "/presupuesto" })
public class PresupuestoServlet extends HttpServlet {

    private final PresupuestoService presupuestoService = new PresupuestoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "data":
                responderDatosTabla(response);
                break;
            case "new":
                mostrarFormularioNuevo(request, response);
                break;
            case "view":
            case "edit":
                mostrarFormularioEdicion(request, response);
                break;
            case "delete":
                eliminar(request, response);
                break;
            case "list":
            default:
                request.getRequestDispatcher("lista_presupuesto.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        switch (action) {
            case "insert":
                insertar(request, response);
                break;
            case "update":
                actualizar(request, response);
                break;
            case "data":
                doGet(request, response);
                break;
            default:
                response.sendRedirect("presupuesto");
                break;
        }
    }

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("form_presupuesto.jsp").forward(request, response);
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                PresupuestoDetalle p = presupuestoService.obtenerPorId(id);
                if (p != null) {
                    request.setAttribute("presupuesto", p);
                    if ("view".equals(request.getParameter("action"))) {
                        request.setAttribute("readonly", true);
                    }
                    request.getRequestDispatcher("form_presupuesto.jsp").forward(request, response);
                } else {
                    response.sendRedirect("presupuesto?error=no_encontrado");
                }
            } catch (NumberFormatException e) {
                response.sendRedirect("presupuesto");
            }
        } else {
            response.sendRedirect("presupuesto");
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                presupuestoService.eliminar(id);
            } catch (NumberFormatException e) {}
        }
        response.sendRedirect("presupuesto");
    }

    private void insertar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            PresupuestoDetalle p = mapearRequest(request);
            if (presupuestoService.insertar(p)) {
                response.sendRedirect("presupuesto?success=insertado");
            } else {
                request.setAttribute("error", "No se pudo insertar el presupuesto.");
                request.setAttribute("presupuesto", p);
                mostrarFormularioNuevo(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("presupuesto?error=excepcion");
        }
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            PresupuestoDetalle p = mapearRequest(request);
            p.setId(Integer.parseInt(request.getParameter("id")));
            if (presupuestoService.actualizar(p)) {
                response.sendRedirect("presupuesto?success=actualizado");
            } else {
                request.setAttribute("error", "No se pudo actualizar el presupuesto.");
                request.setAttribute("presupuesto", p);
                mostrarFormularioEdicion(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("presupuesto?error=excepcion");
        }
    }

    private PresupuestoDetalle mapearRequest(HttpServletRequest request) {
        PresupuestoDetalle p = new PresupuestoDetalle();
        p.setCdpNumero(request.getParameter("cdpNumero"));
        p.setCdpFecha(java.sql.Date.valueOf(request.getParameter("cdpFecha")));
        p.setCdpValor(new java.math.BigDecimal(request.getParameter("cdpValor")));
        p.setCdpVencimiento(java.sql.Date.valueOf(request.getParameter("cdpVencimiento")));
        p.setRpNumero(request.getParameter("rpNumero"));
        String rpFecha = request.getParameter("rpFecha");
        if (rpFecha != null && !rpFecha.isEmpty()) {
            p.setRpFecha(java.sql.Date.valueOf(rpFecha));
        }
        p.setApropiacionPresupuestal(request.getParameter("apropiacionPresupuestal"));
        p.setFichaEbiNombre(request.getParameter("fichaEbiNombre"));
        p.setIdPaa(request.getParameter("idPaa"));
        p.setCodigoDane(request.getParameter("codigoDane"));
        p.setInversion(request.getParameter("inversion"));
        p.setFuncionamiento(request.getParameter("funcionamiento"));
        p.setFichaEbiObjetivo(request.getParameter("fichaEbiObjetivo"));
        p.setFichaEbiActividades(request.getParameter("fichaEbiActividades"));
        p.setCertificadoInsuficiencia(request.getParameter("certificadoInsuficiencia"));
        String fechaIns = request.getParameter("fechaInsuficiencia");
        if (fechaIns != null && !fechaIns.isEmpty()) {
            p.setFechaInsuficiencia(java.sql.Date.valueOf(fechaIns));
        }
        p.setBpin(request.getParameter("bpin"));
        return p;
    }

    private void responderDatosTabla(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Object[][] data = presupuestoService.generarDataParaTabla();

        try (PrintWriter out = response.getWriter()) {
            out.print(new Gson().toJson(new ResponseWrapper(data)));
        }
    }

    // Clase interna para envolver la respuesta JSON de DataTables
    class ResponseWrapper {
        Object data;
        ResponseWrapper(Object data) { this.data = data; }
    }
}
