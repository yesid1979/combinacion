package com.combinacion.servlets;

import com.combinacion.models.Contrato;
import com.combinacion.models.InformeSupervision;
import com.combinacion.services.InformeSupervisionService;
import com.combinacion.services.InformeSupervisionService.InformeFormData;
import com.combinacion.util.ParseUtils;
import com.combinacion.util.SupervisionReportGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "InformeSupervisionServlet", urlPatterns = { "/informes" })
public class InformeSupervisionServlet extends HttpServlet {

    private final InformeSupervisionService informeService = new InformeSupervisionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "new":
                mostrarFormularioNuevo(request, response);
                break;
            case "view":
                mostrarDetalle(request, response);
                break;
            case "download":
                descargarInforme(request, response);
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
        } else {
            listar(request, response);
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Por ahora listamos informes de un contrato específico o general
        String contratoIdStr = request.getParameter("contrato_id");
        if (contratoIdStr != null && !contratoIdStr.isEmpty()) {
            int contratoId = ParseUtils.parseInt(contratoIdStr);
            request.setAttribute("listaInformes", informeService.listarPorContrato(contratoId));
            request.setAttribute("contrato", informeService.obtenerContrato(contratoId));
        } else {
            request.setAttribute("listaInformes", informeService.listarTodos());
        }
        request.getRequestDispatcher("lista_informes.jsp").forward(request, response);
    }

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contratoIdStr = request.getParameter("contrato_id");
        if (contratoIdStr != null) {
            int contratoId = ParseUtils.parseInt(contratoIdStr);
            Contrato contrato = informeService.obtenerContrato(contratoId);
            request.setAttribute("contrato", contrato);
        }
        request.setAttribute("action", "insert");
        request.getRequestDispatcher("form_supervision.jsp").forward(request, response);
    }

    private void mostrarDetalle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = ParseUtils.parseInt(request.getParameter("id"));
        InformeSupervision informe = informeService.obtenerPorId(id);
        request.setAttribute("informe", informe);
        if (informe != null && informe.getContratoId() != null) {
            request.setAttribute("contrato", informeService.obtenerContrato(informe.getContratoId()));
        }
        request.setAttribute("readonly", true);
        request.setAttribute("action", "view");
        request.getRequestDispatcher("form_supervision.jsp").forward(request, response);
    }

    private void descargarInforme(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = ParseUtils.parseInt(request.getParameter("id"));
            InformeSupervision informe = informeService.obtenerPorId(id);
            if (informe == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Informe no encontrado.");
                return;
            }

            Contrato contrato = informeService.obtenerContrato(informe.getContratoId());
            String filePath = SupervisionReportGenerator.generarDocx(informe, contrato);
            File downloadFile = new File(filePath);

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFile.getName() + "\"");
            response.setContentLength((int) downloadFile.length());

            try (FileInputStream inStream = new FileInputStream(downloadFile);
                 OutputStream outStream = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el documento: " + e.getMessage());
        }
    }

    private void insertar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        InformeFormData form = construirFormData(request);
        String error = informeService.insertar(form);
        if (error != null) {
            request.setAttribute("error", error);
            mostrarFormularioNuevo(request, response);
        } else {
            request.getSession().setAttribute("successMessage", "El informe de supervisión ha sido registrado correctamente.");
            response.sendRedirect("informes?contrato_id=" + form.contratoId);
        }
    }

    private InformeFormData construirFormData(HttpServletRequest r) {
        InformeFormData f = new InformeFormData();
        f.contratoId = ParseUtils.parseInt(r.getParameter("contrato_id"));
        f.periodoInforme = r.getParameter("periodo_informe");
        f.tipoInforme = r.getParameter("tipo_informe");
        f.numeroCuota = r.getParameter("numero_cuota");
        f.fechaInicioPeriodo = r.getParameter("fecha_inicio_periodo");
        f.fechaFinPeriodo = r.getParameter("fecha_fin_periodo");
        f.modificaciones = r.getParameter("modificaciones");
        f.suspensiones = r.getParameter("suspensiones");
        f.reanudaciones = r.getParameter("reanudaciones");
        f.cesiones = r.getParameter("cesiones");
        f.terminacionAnticipada = r.getParameter("terminacion_anticipada");
        f.valorCuotaPagar = r.getParameter("valor_cuota_pagar");
        f.valorAccumuladoPagado = r.getParameter("valor_acumulado_pagado");
        f.saldoPorCancelar = r.getParameter("saldo_por_cancelar");
        f.planillaNumero = r.getParameter("planilla_numero");
        f.planillaPin = r.getParameter("planilla_pin");
        f.planillaOperador = r.getParameter("planilla_operador");
        f.planillaFechaPago = r.getParameter("planilla_fecha_pago");
        f.planillaPeriodo = r.getParameter("planilla_periodo");
        f.observacionesTecnicas = r.getParameter("observaciones_tecnicas");
        f.recomendaciones = r.getParameter("recomendaciones");
        f.fechaSuscripcion = r.getParameter("fecha_suscripcion");
        return f;
    }
}
