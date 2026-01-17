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
            case "new":
                showNewForm(request, response);
                break;
            case "list":
            default:
                listContratos(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("insert".equals(action)) {
            insertContrato(request, response);
        } else {
            listContratos(request, response);
        }
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

            // Supervisor
            Supervisor supervisor = new Supervisor();
            supervisor.setCedula(request.getParameter("supervisor_cedula"));
            supervisor.setNombre(request.getParameter("supervisor_nombre"));
            supervisor.setCargo(request.getParameter("supervisor_cargo"));
            if (!supervisorDAO.insertar(supervisor)) {
                Supervisor existingSup = supervisorDAO.obtenerPorCedula(supervisor.getCedula());
                if (existingSup != null) {
                    supervisor.setId(existingSup.getId());
                }
            }

            // Ordenador
            OrdenadorGasto ordenador = new OrdenadorGasto();
            ordenador.setNombreOrdenador(request.getParameter("ordenador_nombre"));
            if (!ordenadorDAO.insertar(ordenador)) {
                OrdenadorGasto existingOrd = ordenadorDAO.obtenerPorNombre(ordenador.getNombreOrdenador());
                if (existingOrd != null) {
                    ordenador.setId(existingOrd.getId());
                }
            }

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
            contrato.setSupervisorId(supervisor.getId());
            contrato.setOrdenadorId(ordenador.getId());
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
