package com.combinacion.servlets;

import com.combinacion.models.OrdenadorGasto;
import com.combinacion.models.Supervisor;
import com.combinacion.services.ContratoService;
import com.combinacion.services.ContratoService.ContratoFormData;
import com.combinacion.util.ParseUtils;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador HTTP para Contrato.
 * Responsabilidad exclusiva: leer parámetros HTTP, delegar al Service,
 * y dirigir la respuesta a la Vista correcta.
 */
// @WebServlet(name = "ContratoServlet", urlPatterns = { "/contratos" })
public class ContratoServlet extends HttpServlet {

    private final ContratoService contratoService = new ContratoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "data":
                responderDatosTabla(request, response);
                break;
            case "new":
                mostrarFormularioNuevo(request, response);
                break;
            case "view":
            case "edit":
                mostrarFormularioEdicion(request, response);
                break;
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
        if ("insert".equals(action)) {
            insertar(request, response);
        } else if ("update".equals(action)) {
            actualizar(request, response);
        } else if ("data".equals(action)) {
            responderDatosTabla(request, response);
        } else {
            listar(request, response);
        }
    }

    // -------------------------------------------------------------------------
    // PRIVADOS
    // -------------------------------------------------------------------------

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("listContratos", contratoService.listarTodos());
        request.getRequestDispatcher("lista_contratos.jsp").forward(request, response);
    }

    private void responderDatosTabla(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            String draw     = request.getParameter("draw");
            int    start    = ParseUtils.parseInt(request.getParameter("start"));
            int    length   = ParseUtils.parseInt(request.getParameter("length"));
            String search   = request.getParameter("search[value]");
            int    orderCol = ParseUtils.parseInt(request.getParameter("order[0][column]"));
            String orderDir = request.getParameter("order[0][dir]");

            response.getWriter().write(
                contratoService.generarJsonDataTables(draw, start, length, search, orderCol, orderDir)
            );
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"error\": \"Error generando datos: " + e.getMessage() + "\"}");
        }
    }

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("listaSupervisores", contratoService.listarSupervisores());
        request.setAttribute("listaOrdenadores",  contratoService.listarOrdenadores());
        request.getRequestDispatcher("form_contrato.jsp").forward(request, response);
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = ParseUtils.parseInt(request.getParameter("id"));
            com.combinacion.models.Contrato contrato = contratoService.obtenerConRelaciones(id);
            if (contrato == null) {
                request.setAttribute("error", "Contrato no encontrado.");
                listar(request, response);
                return;
            }
            request.setAttribute("contrato", contrato);

            List<Supervisor>     listaSupervisores = contratoService.listarSupervisores();
            List<OrdenadorGasto> listaOrdenadores  = contratoService.listarOrdenadores();
            request.setAttribute("listaSupervisores", listaSupervisores);
            request.setAttribute("listaOrdenadores",  listaOrdenadores);

            if ("view".equals(request.getParameter("action"))) {
                request.setAttribute("action",   "view");
                request.setAttribute("readonly", true);
            } else {
                request.setAttribute("action", "update");
            }
            request.getRequestDispatcher("form_contrato.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al cargar el contrato: " + e.getMessage());
            listar(request, response);
        }
    }

    private void insertar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            ContratoFormData form = construirFormData(request);
            String error = contratoService.insertar(form);
            if (error != null) {
                request.setAttribute("error", error);
                listar(request, response);
            } else {
                request.getSession().setAttribute("successMessage", "El contrato ha sido creado correctamente.");
                response.sendRedirect("contratos?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error inesperado: " + e.getMessage());
            listar(request, response);
        }
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            int id = ParseUtils.parseInt(request.getParameter("id"));
            ContratoFormData form = construirFormData(request);
            String error = contratoService.actualizar(id, form);
            if (error != null) {
                request.setAttribute("error", error);
                listar(request, response);
            } else {
                request.getSession().setAttribute("successMessage", "El contrato ha sido actualizado correctamente.");
                response.sendRedirect("contratos?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error inesperado al actualizar: " + e.getMessage());
            listar(request, response);
        }
    }

    /**
     * Construye el DTO de datos de formulario a partir del HttpServletRequest.
     */
    private ContratoFormData construirFormData(HttpServletRequest r) {
        ContratoFormData f = new ContratoFormData();
        f.contratistaCedula            = r.getParameter("contratista_cedula");
        f.contratistaDv                = r.getParameter("contratista_dv");
        f.contratistaNombre            = r.getParameter("contratista_nombre");
        f.contratistaTelefono          = r.getParameter("contratista_telefono");
        f.contratistaCorreo            = r.getParameter("contratista_correo");
        f.contratistaDireccion         = r.getParameter("contratista_direccion");
        f.contratistaFechaNac          = r.getParameter("contratista_fecha_nac");
        f.contratistaEdad              = r.getParameter("contratista_edad");
        f.presupuestoCdp               = r.getParameter("presupuesto_cdp");
        f.presupuestoRpc               = r.getParameter("presupuesto_rpc");
        f.estructuradorJuridico        = r.getParameter("estructurador_juridico");
        f.estructuradorTecnico         = r.getParameter("estructurador_tecnico");
        f.estructuradorFinanciero      = r.getParameter("estructurador_financiero");
        f.supervisorId                 = ParseUtils.parseInt(r.getParameter("id_supervisor"));
        f.ordenadorId                  = ParseUtils.parseInt(r.getParameter("id_ordenador"));
        f.numeroContrato               = r.getParameter("numero_contrato");
        f.tipoContrato                 = r.getParameter("tipo_contrato");
        f.nivel                        = r.getParameter("nivel");
        f.objeto                       = r.getParameter("objeto");
        f.valorTotal                   = r.getParameter("valor_total");
        f.valorTotalLetras             = r.getParameter("valor_total_letras");
        f.valorCuotaNumero             = r.getParameter("valor_cuota_numero");
        f.valorCuotaLetras             = r.getParameter("valor_cuota_letras");
        f.valorMediaCuotaLetras        = r.getParameter("valor_media_cuota_letras");
        f.valorMediaCuotaNumero        = r.getParameter("valor_media_cuota_numero");
        f.fechaInicio                  = r.getParameter("fecha_inicio");
        f.fechaTerminacion             = r.getParameter("fecha_terminacion");
        f.fechaIdoneidad               = r.getParameter("fecha_idoneidad");
        f.fechaEstructurador           = r.getParameter("fecha_estructurador");
        f.actividadesEntregables       = r.getParameter("actividades_entregables");
        f.adicionSiNo                  = r.getParameter("adicion_si_no");
        f.numeroCuotasAdicion          = r.getParameter("numero_cuotas_adicion");
        f.valorTotalAdicion            = r.getParameter("valor_total_adicion");
        f.valorTotalAdicionLetras      = r.getParameter("valor_total_adicion_letras");
        f.valorContratoMasAdicion      = r.getParameter("valor_contrato_mas_adicion");
        f.valorContratoMasAdicionLetras= r.getParameter("valor_contrato_mas_adicion_letras");
        f.enlaceSecop                  = r.getParameter("enlace_secop");
        return f;
    }
}
