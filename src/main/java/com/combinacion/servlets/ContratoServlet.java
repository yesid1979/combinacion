package com.combinacion.servlets;

import com.combinacion.dao.*;
import com.combinacion.models.*;
import com.combinacion.util.ParseUtils;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ContratoServlet", urlPatterns = { "/contratos" })
public class ContratoServlet extends HttpServlet {

    private ContratoDAO contratoDAO = new ContratoDAO();
    private ContratistaDAO contratistaDAO = new ContratistaDAO(); // Assuming this exists or will continue working if it
                                                                  // does
    private SupervisorDAO supervisorDAO = new SupervisorDAO(); // Assuming this exists
    private OrdenadorGastoDAO ordenadorDAO = new OrdenadorGastoDAO();
    private PresupuestoDetalleDAO presupuestoDAO = new PresupuestoDetalleDAO();
    private EstructuradorDAO estructuradorDAO = new EstructuradorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "data":
                listContratosData(request, response);
                break;
            case "new":
                showNewForm(request, response);
                break;
            case "list":
            default:
                listContratos(request, response);
                break;
        }
    }

    private void listContratosData(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // DataTables parameters
        String draw = request.getParameter("draw");
        int start = ParseUtils.parseInt(request.getParameter("start"));
        int length = ParseUtils.parseInt(request.getParameter("length"));
        String searchValue = request.getParameter("search[value]");
        int orderColumn = ParseUtils.parseInt(request.getParameter("order[0][column]"));
        String orderDir = request.getParameter("order[0][dir]");

        int total = contratoDAO.countAll();
        int filtered = contratoDAO.countFiltered(searchValue);
        List<Contrato> contratos = contratoDAO.findWithPagination(start, length, searchValue, orderColumn, orderDir);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"draw\": ").append(draw != null ? draw : 1).append(",");
        json.append("\"recordsTotal\": ").append(total).append(",");
        json.append("\"recordsFiltered\": ").append(filtered).append(",");
        json.append("\"data\": [");

        for (int i = 0; i < contratos.size(); i++) {
            Contrato c = contratos.get(i);
            json.append("[");
            json.append("\"").append(escapeJson(c.getNumeroContrato())).append("\",");
            json.append("\"").append(escapeJson(c.getContratistaNombre())).append("\",");
            json.append("\"").append(escapeJson(c.getObjeto())).append("\",");
            json.append("\"").append(c.getFechaInicio() != null ? c.getFechaInicio().toString() : "").append("\",");
            json.append("\"").append(c.getFechaTerminacion() != null ? c.getFechaTerminacion().toString() : "")
                    .append("\",");
            json.append("\"").append(c.getValorTotalNumeros()).append("\",");
            json.append("\"").append(escapeJson(c.getEstado())).append("\",");
            json.append("\"").append(c.getId()).append("\""); // For actions
            json.append("]");
            if (i < contratos.size() - 1)
                json.append(",");
        }

        json.append("]}");
        response.getWriter().write(json.toString());
    }

    private String escapeJson(String s) {
        if (s == null)
            return "";
        return s.replace("\"", "\\\"").replace("\\", "\\\\").replace("\n", " ").replace("\r", " ");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("insert".equals(action)) {
            insertContrato(request, response);
        } else if ("data".equals(action)) { // Handle DataTables AJAX request via POST
            listContratosData(request, response);
        } else {
            listContratos(request, response);
        }
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Cargar listas para los selects
        List<Supervisor> listaSupervisores = supervisorDAO.listarTodos();
        List<OrdenadorGasto> listaOrdenadores = ordenadorDAO.listarTodos();

        request.setAttribute("listaSupervisores", listaSupervisores);
        request.setAttribute("listaOrdenadores", listaOrdenadores);

        request.getRequestDispatcher("form_contrato.jsp").forward(request, response);
    }

    private void listContratos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Contrato> listContratos = contratoDAO.listarTodos();
        request.setAttribute("listContratos", listContratos);
        request.getRequestDispatcher("lista_contratos.jsp").forward(request, response);
    }

    private void insertContrato(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            // 1. Create/Find Related Entities

            // Contratista
            Contratista contratista = new Contratista();
            contratista.setCedula(request.getParameter("contratista_cedula"));
            contratista.setNombre(request.getParameter("contratista_nombre"));
            contratista.setTelefono(request.getParameter("contratista_telefono"));
            contratista.setCorreo(request.getParameter("contratista_correo"));
            contratista.setDireccion(request.getParameter("contratista_direccion"));
            contratista.setFechaNacimiento(ParseUtils.parseDate(request.getParameter("contratista_fecha_nac")));
            contratista.setEdad(ParseUtils.parseInt(request.getParameter("contratista_edad")));

            // Try insert. If fails, try to find by cedula.
            if (!contratistaDAO.insertar(contratista)) {
                Contratista existing = contratistaDAO.obtenerPorCedula(contratista.getCedula());
                if (existing != null) {
                    contratista.setId(existing.getId());
                } else {
                    // Error fatal
                    throw new Exception("No se pudo crear ni encontrar el contratista.");
                }
            }

            // Supervisor (Selected directly by ID)
            int supervisorId = ParseUtils.parseInt(request.getParameter("id_supervisor"));

            // Ordenador (Selected directly by ID)
            int ordenadorId = ParseUtils.parseInt(request.getParameter("id_ordenador"));

            // Presupuesto
            PresupuestoDetalle presupuesto = new PresupuestoDetalle();
            presupuesto.setCdpNumero(request.getParameter("presupuesto_cdp"));
            presupuestoDAO.insertar(presupuesto);

            // Estructurador
            Estructurador estructurador = new Estructurador();
            estructurador.setJuridicoNombre(request.getParameter("estructurador_juridico"));
            estructuradorDAO.insertar(estructurador);

            // 2. Create Contrato
            Contrato contrato = new Contrato();
            contrato.setNumeroContrato(request.getParameter("numero_contrato"));
            contrato.setObjeto(request.getParameter("objeto"));
            contrato.setValorTotalNumeros(ParseUtils.parseBigDecimal(request.getParameter("valor_total")));
            contrato.setValorTotalLetras(request.getParameter("valor_total_letras"));
            contrato.setValorCuotaNumero(ParseUtils.parseBigDecimal(request.getParameter("valor_cuota_numero")));
            contrato.setValorCuotaLetras(request.getParameter("valor_cuota_letras"));
            contrato.setValorMediaCuotaLetras(request.getParameter("valor_media_cuota_letras"));
            contrato.setValorMediaCuotaNumero(
                    ParseUtils.parseBigDecimal(request.getParameter("valor_media_cuota_numero")));

            contrato.setFechaInicio(ParseUtils.parseDate(request.getParameter("fecha_inicio")));
            contrato.setFechaTerminacion(ParseUtils.parseDate(request.getParameter("fecha_terminacion")));

            // Foreign Keys
            contrato.setContratistaId(contratista.getId());
            contrato.setSupervisorId(supervisorId);
            contrato.setOrdenadorId(ordenadorId);
            contrato.setPresupuestoId(presupuesto.getId());
            contrato.setEstructuradorId(estructurador.getId());

            if (contratoDAO.insertar(contrato)) {
                response.sendRedirect("contratos?action=list");
            } else {
                request.setAttribute("error", "Error al guardar el contrato. Verifique los datos.");
                listContratos(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error inesperado: " + e.getMessage());
            listContratos(request, response);
        }
    }
}
