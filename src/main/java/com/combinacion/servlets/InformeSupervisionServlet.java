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
@javax.servlet.annotation.MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
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
        } else {
            listar(request, response);
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        com.combinacion.models.Usuario u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario");
        boolean esContratista = (u != null && (u.getRolId() == 3 || (u.getRol() != null && "Contratista".equalsIgnoreCase(u.getRol().getNombre()))));

        String contratoIdStr = request.getParameter("contrato_id");
        
        if (esContratista) {
            // Lógica para el Contratista: Buscar su contrato automáticamente
            com.combinacion.dao.ContratistaDAO cdao = new com.combinacion.dao.ContratistaDAO();
            
            // Buscar contratista ignorando puntos y letras en la base de datos
            java.util.List<com.combinacion.models.Contratista> todos = cdao.listarTodos();
            com.combinacion.models.Contratista c = null;
            for (com.combinacion.models.Contratista cont : todos) {
                if (cont.getCedula() != null) {
                    String limpiaDB = cont.getCedula().replaceAll("[^0-9]", "");
                    if (limpiaDB.equals(u.getCedula())) {
                        c = cont;
                        break;
                    }
                }
            }

            if (c != null) {
                com.combinacion.dao.ContratoDAO codao = new com.combinacion.dao.ContratoDAO();
                java.util.List<Contrato> misContratos = codao.listarPorContratistaId(c.getId());
                
                if (!misContratos.isEmpty()) {
                    request.setAttribute("misContratos", misContratos);
                    
                    if (contratoIdStr != null && !contratoIdStr.isEmpty()) {
                        int contratoId = ParseUtils.parseInt(contratoIdStr);
                        request.setAttribute("listaInformes", informeService.listarPorContrato(contratoId));
                        request.setAttribute("contrato", informeService.obtenerContrato(contratoId));
                    } else {
                        // Si tiene varios contratos pero no seleccionó uno, le mostramos todos sus informes y le asignamos su contrato más reciente por defecto para "Nuevo Informe" si solo tiene 1
                        request.setAttribute("contrato", misContratos.get(0));
                        
                        java.util.List<InformeSupervision> todosMisInformes = new java.util.ArrayList<>();
                        for(Contrato con : misContratos) {
                            todosMisInformes.addAll(informeService.listarPorContrato(con.getId()));
                        }
                        request.setAttribute("listaInformes", todosMisInformes);
                    }
                } else {
                    request.setAttribute("listaInformes", new java.util.ArrayList<>());
                    request.setAttribute("error", "No tienes ningún contrato activo asignado en el sistema.");
                }
            } else {
                request.setAttribute("listaInformes", new java.util.ArrayList<>());
                request.setAttribute("error", "No se encontraron tus datos como contratista.");
            }
        } else {
            // Lógica para Admin/Supervisor: Buscar por ID de contrato específico, o listar todos
            if (contratoIdStr != null && !contratoIdStr.isEmpty()) {
                int contratoId = ParseUtils.parseInt(contratoIdStr);
                request.setAttribute("listaInformes", informeService.listarPorContrato(contratoId));
                request.setAttribute("contrato", informeService.obtenerContrato(contratoId));
            } else {
                request.setAttribute("listaInformes", informeService.listarTodos());
            }
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
            if (contrato != null) {
                request.setAttribute("listaObligaciones", com.combinacion.util.ObligacionesParser.decodificarConcepto(null, contrato.getActividadesEntregables()));
                
                // Calcular acumulado previo y número de cuota sugerido
                java.util.List<com.combinacion.models.InformeSupervision> previos = informeService.listarPorContrato(contratoId);
                java.math.BigDecimal acumulado = java.math.BigDecimal.ZERO;
                if(previos != null && !previos.isEmpty()){
                    for(com.combinacion.models.InformeSupervision prev : previos){
                        if(prev.getValorCuotaPagar() != null){
                            acumulado = acumulado.add(prev.getValorCuotaPagar());
                        }
                    }
                }
                request.setAttribute("acumuladoPrevio", acumulado);
                request.setAttribute("siguienteCuota", previos != null ? previos.size() + 1 : 1);
            }
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
            Contrato contrato = informeService.obtenerContrato(informe.getContratoId());
            request.setAttribute("contrato", contrato);
            if (contrato != null) {
                request.setAttribute("listaObligaciones", com.combinacion.util.ObligacionesParser.decodificarConcepto(informe.getConceptoSupervisor(), contrato.getActividadesEntregables()));
            }
        }
        request.setAttribute("readonly", true);
        request.setAttribute("action", "view");
        request.getRequestDispatcher("form_supervision.jsp").forward(request, response);
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = ParseUtils.parseInt(request.getParameter("id"));
        InformeSupervision informe = informeService.obtenerPorId(id);
        if (informe == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Informe no encontrado");
            return;
        }
        request.setAttribute("informe", informe);
        if (informe.getContratoId() != null) {
            Contrato contrato = informeService.obtenerContrato(informe.getContratoId());
            request.setAttribute("contrato", contrato);
            if (contrato != null) {
                request.setAttribute("listaObligaciones", com.combinacion.util.ObligacionesParser.decodificarConcepto(informe.getConceptoSupervisor(), contrato.getActividadesEntregables()));
            }
        }
        request.setAttribute("action", "update");
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
            
            String nombreCompleto = contrato.getContratistaNombre() != null ? contrato.getContratistaNombre().trim() : "";
            String nombreCorto = nombreCompleto;
            String[] parts = nombreCompleto.split("\\s+");
            if (parts.length >= 3) {
                nombreCorto = parts[0] + " " + parts[2];
            } else if (parts.length == 2) {
                nombreCorto = parts[0] + " " + parts[1];
            }

            String consecutivoStr = (informe.getConsecutivoCobro() != null && !informe.getConsecutivoCobro().trim().isEmpty()) ? informe.getConsecutivoCobro().trim() : "XXXX";
            String docxName = "5. INFORME SUPERVISION No. " + informe.getNumeroCuota() + " -" + nombreCorto + ".docx";
            
            String shortContrato = contrato.getNumeroContrato() != null ? contrato.getNumeroContrato().split("\\.")[0] : "";
            String xlsxName = "3. DS-" + shortContrato + "-" + consecutivoStr + " Cuota " + informe.getNumeroCuota() + " -" + nombreCorto + ".xlsx";
            
            // Archivo DOCX temporal
            String filePathDocx = SupervisionReportGenerator.generarDocx(informe, contrato, getServletContext().getRealPath("/"));
            File docxFile = new File(filePathDocx);
            
            // Archivo XLSX temporal
            File xlsxFile = null;
            try {
                String xlsxPath = com.combinacion.util.CuentaCobroGenerator.generarExcel(informe, contrato, getServletContext().getRealPath("/"));
                xlsxFile = new File(xlsxPath);
            } catch (Exception e) {
                System.out.println("No se pudo generar el Excel: " + e.getMessage());
            }

            // Si ambos existen, generar ZIP
            if (xlsxFile != null && xlsxFile.exists()) {
                String numContrato = contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : "";
                String primerBloque = "";
                String ultimoBloque = "";
                if (numContrato.contains(".")) {
                    String[] numParts = numContrato.split("\\.");
                    primerBloque = numParts[0];
                    ultimoBloque = numParts[numParts.length - 1];
                } else {
                    primerBloque = numContrato;
                }
                
                String safeName = nombreCorto.replaceAll("[^a-zA-Z0-9.\\- ]", "");
                String zipFileName = primerBloque + (!ultimoBloque.isEmpty() ? " - " + ultimoBloque : "") + " " + safeName + ".zip";
                
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");
                
                try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(response.getOutputStream())) {
                    // Agregar DOCX
                    zos.putNextEntry(new java.util.zip.ZipEntry(docxName));
                    try (FileInputStream fis = new FileInputStream(docxFile)) {
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = fis.read(buffer)) >= 0) {
                            zos.write(buffer, 0, length);
                        }
                    }
                    zos.closeEntry();
                    
                    // Agregar XLSX
                    zos.putNextEntry(new java.util.zip.ZipEntry(xlsxName));
                    try (FileInputStream fis = new FileInputStream(xlsxFile)) {
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = fis.read(buffer)) >= 0) {
                            zos.write(buffer, 0, length);
                        }
                    }
                    zos.closeEntry();
                }
            } else {
                // Si no hay Excel, descargar solo el DOCX
                response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + docxFile.getName() + "\"");
                response.setContentLength((int) docxFile.length());

                try (FileInputStream inStream = new FileInputStream(docxFile);
                     OutputStream outStream = response.getOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
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
            response.sendRedirect("informes");
        }
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int id = ParseUtils.parseInt(request.getParameter("id"));
        InformeFormData form = construirFormData(request);
        
        System.out.println("====== DEBUG ACTUALIZAR ======");
        System.out.println("ID: " + id);
        System.out.println("Consecutivo Formulario: " + form.consecutivoCobro);
        System.out.println("==============================");
        
        String error = informeService.actualizar(id, form);
        if (error != null) {
            request.setAttribute("error", error);
            mostrarFormularioEdicion(request, response);
        } else {
            request.getSession().setAttribute("successMessage", "El informe de supervisión ha sido actualizado correctamente.");
            response.sendRedirect("informes");
        }
    }

    private InformeFormData construirFormData(HttpServletRequest r) {
        InformeFormData f = new InformeFormData();
        f.contratoId = ParseUtils.parseInt(r.getParameter("contrato_id"));
        f.periodoInforme = r.getParameter("periodo_informe");
        f.tipoInforme = r.getParameter("tipo_informe");
        f.numeroCuota = r.getParameter("numero_cuota");
        f.consecutivoCobro = r.getParameter("consecutivo_cobro");
        f.fechaInicioPeriodo = r.getParameter("fecha_inicio_periodo");
        f.fechaFinPeriodo = r.getParameter("fecha_fin_periodo");
        f.modificaciones = r.getParameter("modificaciones");
        f.suspensiones = r.getParameter("suspensiones");
        f.reanudaciones = r.getParameter("reanudaciones");
        f.cesiones = r.getParameter("cesiones");
        f.terminacionAnticipada = r.getParameter("terminacion_anticipada");
        f.adiciones = r.getParameter("adiciones");
        f.prorrogas = r.getParameter("prorrogas");
        f.reciboSatisfaccion = r.getParameter("recibo_satisfaccion");
        f.constanciaPazSalvo = r.getParameter("constancia_paz_salvo");
        f.valorCuotaPagar = r.getParameter("valor_cuota_pagar");
        f.valorAccumuladoPagado = r.getParameter("valor_acumulado_pagado");
        f.saldoPorCancelar = r.getParameter("saldo_por_cancelar");
        f.planillaNumero = r.getParameter("planilla_numero");
        f.planillaPin = r.getParameter("planilla_pin");
        f.planillaOperador = r.getParameter("planilla_operador");
        f.planillaFechaPago = r.getParameter("planilla_fecha_pago");
        f.planillaPeriodo = r.getParameter("planilla_periodo");
        
        int count = ParseUtils.parseInt(r.getParameter("obligaciones_count"));
        if (count > 0) {
            org.json.JSONArray arr = new org.json.JSONArray();
            for (int i = 0; i < count; i++) {
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("obligacion", r.getParameter("obligacion_" + i));
                String[] acts = r.getParameterValues("actividad_" + i);
                String joinedAct = "";
                if (acts != null) {
                    joinedAct = String.join("\n", acts);
                }
                obj.put("actividad", joinedAct);
                arr.put(obj);
            }
            f.conceptoSupervisor = arr.toString();
        } else {
            f.conceptoSupervisor = r.getParameter("concepto_supervisor");
        }
        
        f.observacionesTecnicas = r.getParameter("observaciones_tecnicas");
        f.recomendaciones = r.getParameter("recomendaciones");
        f.fechaSuscripcion = r.getParameter("fecha_suscripcion");
        return f;
    }
}
