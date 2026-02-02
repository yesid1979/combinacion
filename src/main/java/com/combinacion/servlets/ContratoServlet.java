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
        request.setCharacterEncoding("UTF-8");
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
            case "view":
            case "edit":
                showEditForm(request, response);
                break;
            case "list":
            default:
                listContratos(request, response);
                break;
        }
    }

    private void listContratosData(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            // DataTables parameters
            String draw = request.getParameter("draw");
            int start = ParseUtils.parseInt(request.getParameter("start"));
            int length = ParseUtils.parseInt(request.getParameter("length"));
            String searchValue = request.getParameter("search[value]");
            int orderColumn = ParseUtils.parseInt(request.getParameter("order[0][column]"));
            String orderDir = request.getParameter("order[0][dir]");

            int total = contratoDAO.countAll();
            int filtered = contratoDAO.countFiltered(searchValue);
            List<Contrato> contratos = contratoDAO.findWithPagination(start, length, searchValue, orderColumn,
                    orderDir);

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
                json.append("\"").append(c.getValorTotalNumeros() != null ? c.getValorTotalNumeros() : "0")
                        .append("\",");
                json.append("\"").append(c.getFechaInicio() != null ? c.getFechaInicio().toString() : "").append("\",");
                json.append("\"").append(c.getFechaTerminacion() != null ? c.getFechaTerminacion().toString() : "")
                        .append("\",");
                json.append("\"").append(escapeJson(c.getEstado())).append("\",");
                json.append("\"").append(c.getId()).append("\""); // For actions
                json.append("]");
                if (i < contratos.size() - 1)
                    json.append(",");
            }

            json.append("]}");
            response.getWriter().write(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"error\": \"Error generando JSON: " + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private String escapeJson(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if ("insert".equals(action)) {
            insertContrato(request, response);
        } else if ("update".equals(action)) {
            updateContrato(request, response);
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
            presupuesto.setRpNumero(request.getParameter("presupuesto_rpc"));
            presupuestoDAO.insertar(presupuesto);

            // Estructurador
            Estructurador estructurador = new Estructurador();
            estructurador.setJuridicoNombre(request.getParameter("estructurador_juridico"));
            estructurador.setTecnicoNombre(request.getParameter("estructurador_tecnico"));
            estructurador.setFinancieroNombre(request.getParameter("estructurador_financiero"));
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
            contrato.setActividadesEntregables(request.getParameter("actividades_entregables"));

            // Foreign Keys
            contrato.setContratistaId(contratista.getId());
            contrato.setSupervisorId(supervisorId);
            contrato.setOrdenadorId(ordenadorId);
            contrato.setPresupuestoId(presupuesto.getId());
            contrato.setEstructuradorId(estructurador.getId());

            if (contratoDAO.insertar(contrato)) {
                request.getSession().setAttribute("successMessage", "El contrato ha sido creado correctamente.");
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

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = ParseUtils.parseInt(request.getParameter("id"));
            Contrato contrato = contratoDAO.obtenerPorId(id);
            if (contrato == null) {
                request.setAttribute("error", "Contrato no encontrado.");
                listContratos(request, response);
                return;
            }
            // Hidratar objetos relacionados
            // Hidratar objetos relacionados con chequeo de nulos para evitar errores en JSP
            if (contrato.getContratistaId() > 0) {
                contrato.setContratista(contratistaDAO.obtenerPorId(contrato.getContratistaId()));
            }
            if (contrato.getContratista() == null)
                contrato.setContratista(new Contratista());

            if (contrato.getPresupuestoId() > 0) {
                contrato.setPresupuestoDetalle(presupuestoDAO.obtenerPorId(contrato.getPresupuestoId()));
            }
            if (contrato.getPresupuestoDetalle() == null)
                contrato.setPresupuestoDetalle(new PresupuestoDetalle());

            if (contrato.getEstructuradorId() > 0) {
                contrato.setEstructurador(estructuradorDAO.obtenerPorId(contrato.getEstructuradorId()));
            }
            if (contrato.getEstructurador() == null)
                contrato.setEstructurador(new Estructurador());

            if (contrato.getSupervisorId() > 0) {
                contrato.setSupervisor(supervisorDAO.obtenerPorId(contrato.getSupervisorId()));
            }
            if (contrato.getSupervisor() == null)
                contrato.setSupervisor(new Supervisor());

            if (contrato.getOrdenadorId() > 0) {
                contrato.setOrdenadorGasto(ordenadorDAO.obtenerPorId(contrato.getOrdenadorId()));
            }
            if (contrato.getOrdenadorGasto() == null)
                contrato.setOrdenadorGasto(new OrdenadorGasto());

            request.setAttribute("contrato", contrato);

            String actionParam = request.getParameter("action");
            if ("view".equals(actionParam)) {
                request.setAttribute("action", "view");
                request.setAttribute("readonly", true);
            } else {
                request.setAttribute("action", "update");
            }

            List<Supervisor> listaSupervisores = supervisorDAO.listarTodos();
            List<OrdenadorGasto> listaOrdenadores = ordenadorDAO.listarTodos();
            request.setAttribute("listaSupervisores", listaSupervisores);
            request.setAttribute("listaOrdenadores", listaOrdenadores);

            request.getRequestDispatcher("form_contrato.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al cargar el contrato: " + e.getMessage());
            listContratos(request, response);
        }
    }

    private void updateContrato(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            int id = ParseUtils.parseInt(request.getParameter("id"));
            Contrato contrato = contratoDAO.obtenerPorId(id); // Load existing to update
            if (contrato == null) {
                throw new Exception("Contrato a actualizar no existe.");
            }

            // 1. Update Contratista
            if (contrato.getContratistaId() > 0) {
                Contratista contratista = contratistaDAO.obtenerPorId(contrato.getContratistaId());
                if (contratista != null) {
                    // Update fields present in form
                    contratista.setCedula(request.getParameter("contratista_cedula"));
                    contratista.setDv(request.getParameter("contratista_dv"));
                    contratista.setNombre(request.getParameter("contratista_nombre"));
                    contratista.setTelefono(request.getParameter("contratista_telefono"));
                    contratista.setCorreo(request.getParameter("contratista_correo"));
                    contratista.setDireccion(request.getParameter("contratista_direccion"));
                    contratista.setFechaNacimiento(ParseUtils.parseDate(request.getParameter("contratista_fecha_nac")));
                    contratista.setEdad(ParseUtils.parseInt(request.getParameter("contratista_edad")));
                    contratistaDAO.actualizar(contratista);
                }
            }

            // 2. Update or Create Presupuesto (Only fields in form)
            String cdp = request.getParameter("presupuesto_cdp");
            String rpc = request.getParameter("presupuesto_rpc");

            if (contrato.getPresupuestoId() > 0) {
                PresupuestoDetalle presupuesto = presupuestoDAO.obtenerPorId(contrato.getPresupuestoId());
                if (presupuesto != null) {
                    presupuesto.setCdpNumero(cdp);
                    presupuesto.setRpNumero(rpc);
                    presupuestoDAO.actualizar(presupuesto);
                }
            } else if ((cdp != null && !cdp.trim().isEmpty()) || (rpc != null && !rpc.trim().isEmpty())) {
                // Create new Presupuesto if it doesn't exist but has data
                PresupuestoDetalle novPresupuesto = new PresupuestoDetalle();
                novPresupuesto.setCdpNumero(cdp);
                novPresupuesto.setRpNumero(rpc);
                // Set other non-null required fields to safe defaults or empty if needed
                novPresupuesto.setApropiacionPresupuestal("");
                novPresupuesto.setIdPaa("");
                novPresupuesto.setCodigoDane("");
                novPresupuesto.setInversion("");
                novPresupuesto.setFuncionamiento("");
                novPresupuesto.setFichaEbiNombre("");
                novPresupuesto.setFichaEbiObjetivo("");
                novPresupuesto.setFichaEbiActividades("");
                novPresupuesto.setCertificadoInsuficiencia("");
                // Dates can remain null if table allows or handle accordingly

                if (presupuestoDAO.insertar(novPresupuesto)) {
                    contrato.setPresupuestoId(novPresupuesto.getId());
                }
            }

            // 3. Update or Create Estructurador
            String jur = request.getParameter("estructurador_juridico");
            String tec = request.getParameter("estructurador_tecnico");
            String fin = request.getParameter("estructurador_financiero");

            if (contrato.getEstructuradorId() > 0) {
                // Update existing
                Estructurador estructurador = estructuradorDAO.obtenerPorId(contrato.getEstructuradorId());
                if (estructurador != null) {
                    estructurador.setJuridicoNombre(jur);
                    estructurador.setTecnicoNombre(tec);
                    estructurador.setFinancieroNombre(fin);
                    estructuradorDAO.actualizar(estructurador);
                }
            } else if ((jur != null && !jur.trim().isEmpty()) || (tec != null && !tec.trim().isEmpty())
                    || (fin != null && !fin.trim().isEmpty())) {
                // Create new if doesn't exist but has data
                Estructurador novEstructurador = new Estructurador();
                novEstructurador.setJuridicoNombre(jur);
                novEstructurador.setTecnicoNombre(tec);
                novEstructurador.setFinancieroNombre(fin);
                // Default empty for cargos as they are not in form
                novEstructurador.setJuridicoCargo("");
                novEstructurador.setTecnicoCargo("");
                novEstructurador.setFinancieroCargo("");

                if (estructuradorDAO.insertar(novEstructurador)) {
                    contrato.setEstructuradorId(novEstructurador.getId());
                }
            }

            // 4. Update Contrato fields
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
            contrato.setActividadesEntregables(request.getParameter("actividades_entregables"));

            int supervisorId = ParseUtils.parseInt(request.getParameter("id_supervisor"));
            int ordenadorId = ParseUtils.parseInt(request.getParameter("id_ordenador"));

            if (supervisorId > 0)
                contrato.setSupervisorId(supervisorId);
            if (ordenadorId > 0)
                contrato.setOrdenadorId(ordenadorId);

            if (contratoDAO.actualizar(contrato)) {
                request.getSession().setAttribute("successMessage", "El contrato ha sido actualizado correctamente.");
                response.sendRedirect("contratos?action=list");
            } else {
                request.setAttribute("error", "Error al actualizar el contrato.");
                listContratos(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error inesperado al actualizar: " + e.getMessage());
            listContratos(request, response);
        }
    }
}
