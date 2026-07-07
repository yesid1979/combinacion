package com.combinacion.servlets;

import com.combinacion.dao.ContratistaDAO;
import com.combinacion.dao.ContratoDAO;
import com.combinacion.dao.OrdenadorGastoDAO;
import com.combinacion.dao.PresupuestoDetalleDAO;
import com.combinacion.dao.SupervisorDAO;
import com.combinacion.models.Contratista;
import com.combinacion.models.Contrato;
import com.combinacion.models.OrdenadorGasto;
import com.combinacion.models.PresupuestoDetalle;
import com.combinacion.models.Supervisor;
import com.combinacion.models.Usuario;
import com.combinacion.models.RevisorDocumento;
import com.combinacion.dao.RevisorDocumentoDAO;
import com.combinacion.dao.EstructuradorDAO;
import com.combinacion.util.TemplateGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "CombinacionServlet", urlPatterns = { "/combinacion" })
public class CombinacionServlet extends HttpServlet {

    private ContratistaDAO contratistaDAO = new ContratistaDAO();
    private ContratoDAO contratoDAO = new ContratoDAO();
    private PresupuestoDetalleDAO presupuestoDAO = new PresupuestoDetalleDAO();
    private SupervisorDAO supervisorDAO = new SupervisorDAO();
    private OrdenadorGastoDAO ordenadorDAO = new OrdenadorGastoDAO();
    private EstructuradorDAO estructuradorDAO = new EstructuradorDAO();
    private RevisorDocumentoDAO revisorDAO = new RevisorDocumentoDAO();

    // Caché de plantillas en memoria
    private java.util.Map<String, byte[]> templateCache = new java.util.concurrent.ConcurrentHashMap<>();

    private Contrato obtenerContratoParaGeneracion(int contratistaId, HttpServletRequest request) {
        String periodo = request.getParameter("periodo");
        if (periodo != null && !periodo.isEmpty()) {
            return contratoDAO.obtenerPorContratistaYPeriodo(contratistaId, periodo);
        }
        return contratoDAO.obtenerPorContratistaId(contratistaId);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        com.combinacion.util.DatabasePatcher.ensureSchema();
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        System.out.println("CombinacionServlet Action: " + action);

        if ("generate".equals(action)) {
            generarDocumentoIndividual(request, response);
        } else if ("downloadZip".equals(action)) {
            generarZipMasivo(request, response);
        } else if ("generateModificacion".equals(action)) {
            generarModificacionIndividual(request, response);
        } else if ("downloadZipModificacion".equals(action)) {
            generarModificacionMasivoZip(request, response);
        } else if ("downloadZipEstructuradores".equals(action)) {
            generarEstructuradoresMasivoZip(request, response);
        } else if ("downloadZipDesignacion".equals(action)) {
            generarDesignacionMasivoZip(request, response);
        } else {
            java.util.List<String> periodos = contratoDAO.obtenerPeriodosDisponibles();
            request.setAttribute("periodos", periodos);
            // Default view: Show list of contractors for merge
            request.getRequestDispatcher("combinacion_contratistas.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private void generarDocumentoIndividual(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int contratistaId = Integer.parseInt(request.getParameter("id"));
            Contratista c = contratistaDAO.obtenerPorId(contratistaId);
            String cedula = (c.getCedula() != null ? c.getCedula() : "Doc");

            // Load all data to check Inversion status
            Contrato contrato = obtenerContratoParaGeneracion(contratistaId, request);
            PresupuestoDetalle presupuesto = null;
            if (contrato != null && contrato.getPresupuestoId() > 0) {
                presupuesto = presupuestoDAO.obtenerPorId(contrato.getPresupuestoId());
            }

            // Generate Standard Docs
            byte[] supervisorBytes = generarBytesDocumento(request, contratistaId, "supervisor");
            byte[] estructuradoresBytes = generarBytesDocumento(request, contratistaId, "estructuradores");

            // Determinar tipo de contrato: Inversión o Funcionamiento
            boolean esInversion = false;
            boolean esFuncionamiento = false;

            if (presupuesto != null) {
                String inversionFlag = presupuesto.getInversion();
                String funcionamientoFlag = presupuesto.getFuncionamiento();

                // Verificar si es Inversión (columna inversion = "Si" o "Sí")
                esInversion = (inversionFlag != null &&
                        (inversionFlag.trim().equalsIgnoreCase("Si") ||
                                inversionFlag.trim().equalsIgnoreCase("Sí")));

                // Verificar si es Funcionamiento (columna funcionamiento = "Si" o "Sí")
                esFuncionamiento = (funcionamientoFlag != null &&
                        (funcionamientoFlag.trim().equalsIgnoreCase("Si") ||
                                funcionamientoFlag.trim().equalsIgnoreCase("Sí")));
            }

            if (supervisorBytes == null && estructuradoresBytes == null && !esInversion && !esFuncionamiento) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "No se pudo generar ningún documento (datos faltantes o contratos no encontrados)");
                return;
            }

            // Create ZIP
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos, java.nio.charset.StandardCharsets.UTF_8);
            java.util.List<DocxEntry> pendingDocs = new java.util.ArrayList<>();

            // Nombre de carpeta: 4121-014-NombreContratista
            // Se extrae solo el consecutivo del numero de contrato (ej: 4121.010.26.1.014 -> 014)
            String consecutivo = extraerConsecutivo(contrato != null ? contrato.getNumeroContrato() : null);
            String nombreFolder = c.getNombre() != null ? c.getNombre() : "Contratista_" + contratistaId;
            String folderName = normalizeFileName("4121-" + consecutivo + "-" + nombreFolder);

            // Add Standard Docs
            if (supervisorBytes != null) {
                pendingDocs.add(new DocxEntry(folderName, "DESIGNACI\u00D3N SUPERVISOR", supervisorBytes));
            }
            if (estructuradoresBytes != null) {
                pendingDocs.add(new DocxEntry(folderName, "DESIGNACI\u00D3N ESTRUCTURADOR PS", estructuradoresBytes));
            }

            // Add Inversion Docs
            if (esInversion && contrato != null) {
                String ivaStr = contrato.getIvaSiNo() != null ? contrato.getIvaSiNo().trim().toLowerCase() : "";
                boolean tieneIva = ivaStr.equals("si") || ivaStr.equals("sí");
                String complementoDoc = tieneIva ? "INVERSION_5_COMPLEMENTO_CONTRATO_IVA.docx" : "INVERSION_4_COMPLEMENTO_CONTRATO.docx";

                for (String tpl : new String[]{"INVERSION_1_ESTUDIOS_PREVIOS.docx", "INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx", "INVERSION_3_CERTIFICADO_IDONEIDAD.docx", complementoDoc}) {
                    String docContext = tpl.contains("IDONEIDAD") ? "idoneidad" : "inversion";
                    Map<String, String> replacements = getFullReplacements(request, contratistaId, docContext, tpl);
                    if (replacements != null) {
                        byte[] fileBytes = generateBytes(tpl, replacements);
                        if (fileBytes != null) {
                            String baseName = "";
                            if (tpl.equals("INVERSION_1_ESTUDIOS_PREVIOS.docx")) {
                                baseName = "ESTUDIOS PREVIOS";
                            } else if (tpl.equals("INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx")) {
                                baseName = "VERIFICACI\u00D3N CUMPLIMIENTO REQUISITOS";
                            } else if (tpl.equals("INVERSION_3_CERTIFICADO_IDONEIDAD.docx")) {
                                baseName = "CERTIFICADO DE IDONEIDAD";
                            } else if (tpl.equals(complementoDoc)) {
                                baseName = "COMPLEMENTO AL CONTRATO ELECTR\u00D3NICO";
                            }
                            pendingDocs.add(new DocxEntry(folderName, baseName, fileBytes));
                        }
                    }
                }
            }

            // Add Funcionamiento Docs
            if (!esInversion && esFuncionamiento && contrato != null) {
                String ivaStr = contrato.getIvaSiNo() != null ? contrato.getIvaSiNo().trim().toLowerCase() : "";
                boolean tieneIva = ivaStr.equals("si") || ivaStr.equals("sí");
                String complementoDoc = tieneIva ? "INVERSION_5_COMPLEMENTO_CONTRATO_IVA.docx" : "INVERSION_4_COMPLEMENTO_CONTRATO.docx";

                for (String tpl : new String[]{"FUNCIONAMIENTO_1_ESTUDIOS_PREVIOS.docx", "INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx", "INVERSION_3_CERTIFICADO_IDONEIDAD.docx", complementoDoc}) {
                    String docContext = tpl.contains("IDONEIDAD") ? "idoneidad" : "funcionamiento";
                    Map<String, String> replacements = getFullReplacements(request, contratistaId, docContext, tpl);
                    if (replacements != null) {
                        byte[] fileBytes = generateBytes(tpl, replacements);
                        if (fileBytes != null) {
                            String baseName = "";
                            if (tpl.equals("FUNCIONAMIENTO_1_ESTUDIOS_PREVIOS.docx")) {
                                baseName = "ESTUDIOS PREVIOS FUNCIONAMIENTO";
                            } else if (tpl.equals("INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx")) {
                                baseName = "VERIFICACI\u00D3N CUMPLIMIENTO REQUISITOS";
                            } else if (tpl.equals("INVERSION_3_CERTIFICADO_IDONEIDAD.docx")) {
                                baseName = "CERTIFICADO DE IDONEIDAD";
                            } else if (tpl.equals(complementoDoc)) {
                                baseName = "COMPLEMENTO AL CONTRATO ELECTR\u00D3NICO";
                            }
                            pendingDocs.add(new DocxEntry(folderName, baseName, fileBytes));
                        }
                    }
                }
            }

            empaquetarYConvertirZip(pendingDocs, zos);
            zos.close();

            byte[] zipBytes = baos.toByteArray();
            String zipFilename = folderName + ".zip";

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFilename + "\"");
            response.getOutputStream().write(zipBytes);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error generando el documento", e);
        }
    }

    private void addToZip(java.util.zip.ZipOutputStream zos, String folder, String filename, byte[] data)
            throws IOException {
        String entryPath = folder + "/" + filename;
        java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(entryPath);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
    }

    private void generarZipMasivo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idsParam = request.getParameter("ids");

        if (idsParam == null || idsParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se seleccionaron contratistas");
            return;
        }

        String[] ids = idsParam.split(",");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos, java.nio.charset.StandardCharsets.UTF_8);
        java.util.List<DocxEntry> pendingDocs = new java.util.ArrayList<>();

        try {
            for (String idStr : ids) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    Contratista c = contratistaDAO.obtenerPorId(id);
                    if (c == null)
                        continue;

                    String cedula = (c.getCedula() != null ? c.getCedula() : "Doc");
                    Contrato contrato = obtenerContratoParaGeneracion(id, request);
                    String consecutivo = extraerConsecutivo(contrato != null ? contrato.getNumeroContrato() : null);
                    String nombreFolder = c.getNombre() != null ? c.getNombre() : "Contratista_" + id;
                    String folderName = normalizeFileName("4121-" + consecutivo + "-" + nombreFolder);

                    PresupuestoDetalle presupuesto = null;
                    if (contrato != null && contrato.getPresupuestoId() > 0) {
                        presupuesto = presupuestoDAO.obtenerPorId(contrato.getPresupuestoId());
                    }

                    boolean esInversion = false;
                    boolean esFuncionamiento = false;

                    if (presupuesto != null) {
                        String inversionFlag = presupuesto.getInversion();
                        String funcionamientoFlag = presupuesto.getFuncionamiento();
                        esInversion = (inversionFlag != null && (inversionFlag.trim().equalsIgnoreCase("Si") || inversionFlag.trim().equalsIgnoreCase("Sí")));
                        esFuncionamiento = (funcionamientoFlag != null && (funcionamientoFlag.trim().equalsIgnoreCase("Si") || funcionamientoFlag.trim().equalsIgnoreCase("Sí")));
                    }

                    byte[] supervisorBytes = generarBytesDocumento(request, id, "supervisor");
                    if (supervisorBytes != null) {
                        pendingDocs.add(new DocxEntry(folderName, "DESIGNACI\u00D3N SUPERVISOR", supervisorBytes));
                    }

                    byte[] estructuradoresBytes = generarBytesDocumento(request, id, "estructuradores");
                    if (estructuradoresBytes != null) {
                        pendingDocs.add(new DocxEntry(folderName, "DESIGNACI\u00D3N ESTRUCTURADOR PS", estructuradoresBytes));
                    }

                    if (esInversion) {
                        String ivaStr = contrato.getIvaSiNo() != null ? contrato.getIvaSiNo().trim().toLowerCase() : "";
                        boolean tieneIva = ivaStr.equals("si") || ivaStr.equals("sí");
                        String complementoDoc = tieneIva ? "INVERSION_5_COMPLEMENTO_CONTRATO_IVA.docx" : "INVERSION_4_COMPLEMENTO_CONTRATO.docx";

                        for (String tpl : new String[]{"INVERSION_1_ESTUDIOS_PREVIOS.docx", "INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx", "INVERSION_3_CERTIFICADO_IDONEIDAD.docx", complementoDoc}) {
                            String docContext = tpl.contains("IDONEIDAD") ? "idoneidad" : "inversion";
                            Map<String, String> replacements = getFullReplacements(request, id, docContext, tpl);
                            if (replacements != null) {
                                byte[] fileBytes = generateBytes(tpl, replacements);
                                if (fileBytes != null) {
                                    String baseName = "";
                                    if (tpl.equals("INVERSION_1_ESTUDIOS_PREVIOS.docx")) {
                                        baseName = "ESTUDIOS PREVIOS";
                                    } else if (tpl.equals("INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx")) {
                                        baseName = "VERIFICACI\u00D3N CUMPLIMIENTO REQUISITOS";
                                    } else if (tpl.equals("INVERSION_3_CERTIFICADO_IDONEIDAD.docx")) {
                                        baseName = "CERTIFICADO DE IDONEIDAD";
                                    } else if (tpl.equals(complementoDoc)) {
                                        baseName = "COMPLEMENTO AL CONTRATO ELECTR\u00D3NICO";
                                    }
                                    pendingDocs.add(new DocxEntry(folderName, baseName, fileBytes));
                                }
                            }
                        }
                    }

                    if (!esInversion && esFuncionamiento && contrato != null) {
                        String ivaStr = contrato.getIvaSiNo() != null ? contrato.getIvaSiNo().trim().toLowerCase() : "";
                        boolean tieneIva = ivaStr.equals("si") || ivaStr.equals("sí");
                        String complementoDoc = tieneIva ? "INVERSION_5_COMPLEMENTO_CONTRATO_IVA.docx" : "INVERSION_4_COMPLEMENTO_CONTRATO.docx";

                        for (String tpl : new String[]{"FUNCIONAMIENTO_1_ESTUDIOS_PREVIOS.docx", "INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx", "INVERSION_3_CERTIFICADO_IDONEIDAD.docx", complementoDoc}) {
                            String docContext = tpl.contains("IDONEIDAD") ? "idoneidad" : "funcionamiento";
                            Map<String, String> replacements = getFullReplacements(request, id, docContext, tpl);
                            if (replacements != null) {
                                byte[] fileBytes = generateBytes(tpl, replacements);
                                if (fileBytes != null) {
                                    String baseName = "";
                                    if (tpl.equals("FUNCIONAMIENTO_1_ESTUDIOS_PREVIOS.docx")) {
                                        baseName = "ESTUDIOS PREVIOS FUNCIONAMIENTO";
                                    } else if (tpl.equals("INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx")) {
                                        baseName = "VERIFICACI\u00D3N CUMPLIMIENTO REQUISITOS";
                                    } else if (tpl.equals("INVERSION_3_CERTIFICADO_IDONEIDAD.docx")) {
                                        baseName = "CERTIFICADO DE IDONEIDAD";
                                    } else if (tpl.equals(complementoDoc)) {
                                        baseName = "COMPLEMENTO AL CONTRATO ELECTR\u00D3NICO";
                                    }
                                    pendingDocs.add(new DocxEntry(folderName, baseName, fileBytes));
                                }
                            }
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            empaquetarYConvertirZip(pendingDocs, zos);
            zos.close();

            byte[] zipBytes = baos.toByteArray();
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"Documentos_" + System.currentTimeMillis() + ".zip\"");
            response.getOutputStream().write(zipBytes);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generando ZIP");
        }
    }

    private void generarEstructuradoresMasivoZip(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idsParam = request.getParameter("ids");

        if (idsParam == null || idsParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se seleccionaron contratistas");
            return;
        }

        String[] ids = idsParam.split(",");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos, java.nio.charset.StandardCharsets.UTF_8);

        try {
            java.util.Set<String> usedNames = new java.util.HashSet<>();
            for (String idStr : ids) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    Contrato contrato = obtenerContratoParaGeneracion(id, request);
                    if (contrato == null) continue;

                    String anioContrato = "";
                    if (contrato.getFechaTerminacion() != null) {
                        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", new Locale("es", "CO"));
                        anioContrato = yearFormat.format(contrato.getFechaTerminacion());
                    }
                    String trdProceso = contrato.getTrdProceso() != null ? contrato.getTrdProceso().trim() : "";
                    if (trdProceso.isEmpty()) {
                        trdProceso = "SIN_PROCESO_" + id;
                    } else if (!anioContrato.isEmpty() && !trdProceso.endsWith(anioContrato)) {
                        trdProceso = trdProceso + "-" + anioContrato;
                    }

                    String safeName = normalizeFileName(trdProceso);

                    // Deduplicate
                    String originalName = safeName;
                    int counter = 1;
                    while (usedNames.contains(safeName)) {
                        safeName = originalName + "_" + counter;
                        counter++;
                    }
                    usedNames.add(safeName);

                    byte[] estructuradoresBytes = generarBytesDocumento(request, id, "estructuradores");
                    if (estructuradoresBytes != null) {
                        String entryPath = safeName + ".docx";
                        java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(entryPath);
                        zos.putNextEntry(entry);
                        zos.write(estructuradoresBytes);
                        zos.closeEntry();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            zos.close();

            byte[] zipBytes = baos.toByteArray();
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"Estructuradores_" + System.currentTimeMillis() + ".zip\"");
            response.getOutputStream().write(zipBytes);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generando ZIP");
        }
    }

    private void generarDesignacionMasivoZip(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idsParam = request.getParameter("ids");

        if (idsParam == null || idsParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se seleccionaron contratistas");
            return;
        }

        String[] ids = idsParam.split(",");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos, java.nio.charset.StandardCharsets.UTF_8);

        try {
            java.util.Set<String> usedNames = new java.util.HashSet<>();
            for (String idStr : ids) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    Contrato contrato = obtenerContratoParaGeneracion(id, request);
                    if (contrato == null) continue;

                    String anioContrato = "";
                    if (contrato.getFechaTerminacion() != null) {
                        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", new Locale("es", "CO"));
                        anioContrato = yearFormat.format(contrato.getFechaTerminacion());
                    }
                    String trdProceso = contrato.getTrdProceso() != null ? contrato.getTrdProceso().trim() : "";
                    if (trdProceso.isEmpty()) {
                        trdProceso = "SIN_PROCESO_" + id;
                    } else if (!anioContrato.isEmpty() && !trdProceso.endsWith(anioContrato)) {
                        trdProceso = trdProceso + "-" + anioContrato;
                    }

                    String safeName = normalizeFileName(trdProceso);

                    // Deduplicate
                    String originalName = safeName;
                    int counter = 1;
                    while (usedNames.contains(safeName)) {
                        safeName = originalName + "_" + counter;
                        counter++;
                    }
                    usedNames.add(safeName);

                    byte[] designacionBytes = generarBytesDocumento(request, id, "supervisor");
                    if (designacionBytes != null) {
                        String entryPath = safeName + ".docx";
                        java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(entryPath);
                        zos.putNextEntry(entry);
                        zos.write(designacionBytes);
                        zos.closeEntry();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            zos.close();

            byte[] zipBytes = baos.toByteArray();
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"DesignacionSupervisor_" + System.currentTimeMillis() + ".zip\"");
            response.getOutputStream().write(zipBytes);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generando ZIP");
        }
    }

    private Map<String, String> getFullReplacements(HttpServletRequest request, int contratistaId, String docType, String realTemplateName) throws Exception {
        Contratista contratista = contratistaDAO.obtenerPorId(contratistaId);
        if (contratista == null)
            return null;
        Contrato contrato = obtenerContratoParaGeneracion(contratistaId, request);
        if (contrato == null)
            return null;

        PresupuestoDetalle presupuesto = null;
        if (contrato.getPresupuestoId() > 0)
            presupuesto = presupuestoDAO.obtenerPorId(contrato.getPresupuestoId());

        Supervisor supervisor = null;
        if (contrato.getSupervisorId() > 0)
            supervisor = supervisorDAO.obtenerPorId(contrato.getSupervisorId());

        OrdenadorGasto ordenador = null;
        if (contrato.getOrdenadorId() > 0)
            ordenador = ordenadorDAO.obtenerPorId(contrato.getOrdenadorId());

        com.combinacion.models.Estructurador estructurador = null;
        if (contrato.getEstructuradorId() > 0)
            estructurador = estructuradorDAO.obtenerPorId(contrato.getEstructuradorId());

        return getCommonReplacements(contratista, contrato, presupuesto, supervisor, ordenador, estructurador, docType, request, realTemplateName);
    }

    private byte[] generateBytes(String templateName, Map<String, String> replacements) throws IOException {
        byte[] templateBytes = templateCache.get(templateName);

        if (templateBytes == null) {
            String realPath = getServletContext().getRealPath("/plantillas/" + templateName);
            File templateFile = (realPath != null) ? new File(realPath) : null;

            if (templateFile == null || !templateFile.exists()) {
                templateFile = new File("c:\\Users\\Soporte y Desarrollo\\Documents\\NetBeansProjects\\combinacion\\plantillas\\" + templateName);
            }
            if (!templateFile.exists()) {
                templateFile = new File("c:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\plantillas\\" + templateName);
            }

            if (!templateFile.exists()) {
                System.err.println("⚠️ Archivo de plantilla no encontrado: " + templateName);
                return null;
            }

            templateBytes = java.nio.file.Files.readAllBytes(templateFile.toPath());
            templateCache.put(templateName, templateBytes);
        }

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(templateBytes)) {
            TemplateGenerator.generate(bais, replacements, baos);
        }
        return baos.toByteArray();
    }

    private Map<String, String> getCommonReplacements(Contratista contratista, Contrato contrato,
            PresupuestoDetalle presupuesto, Supervisor supervisor,
            OrdenadorGasto ordenador, com.combinacion.models.Estructurador estructurador, String docType, HttpServletRequest request, String realTemplateName) {
        Map<String, String> replacements = new HashMap<>();

        // Extraer año de fecha de terminación para agregarlo a números de contrato y
        // proceso
        String anioContrato = "";
        if (contrato.getFechaTerminacion() != null) {
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", new Locale("es", "CO"));
            anioContrato = yearFormat.format(contrato.getFechaTerminacion());
        }

        // ===== PLACEHOLDERS ANTIGUOS (formato ${}) para compatibilidad =====
        replacements.put("${NOMBRE_CONTRATISTA}",
                contratista.getNombre() != null ? contratista.getNombre().toUpperCase() : "");
        replacements.put("${CEDULA}", contratista.getCedula() != null ? contratista.getCedula() : "");

        // Número de contrato con año
        String numeroContrato = contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : "";
        if (!numeroContrato.isEmpty() && !anioContrato.isEmpty()) {
            numeroContrato = numeroContrato + "-" + anioContrato;
        }
        replacements.put("${NUMERO_CONTRATO}", numeroContrato);

        replacements.put("${OBJETO_CONTRACTUAL}", contrato.getObjeto() != null ? contrato.getObjeto() : "");

        // TRD Proceso con año
        String trdProceso = contrato.getTrdProceso() != null ? contrato.getTrdProceso() : "";
        if (!trdProceso.isEmpty() && !anioContrato.isEmpty()) {
            trdProceso = trdProceso + "-" + anioContrato;
        }
        replacements.put("${NUMERO_PROCESO}", trdProceso);

        replacements.put("${NUMERO_PROCESO_O_CONTRATO_ALT}",
                !trdProceso.isEmpty() ? trdProceso : numeroContrato);

        // ===== NUEVOS PLACEHOLDERS (formato {{}}) para plantillas de inversión =====

        // Información del Proceso y Contrato (con año)
        replacements.put("{{NUMERO_PROCESO}}", trdProceso);
        replacements.put("{{NUMERO_CONTRATO}}", numeroContrato);

        // Información del Proyecto (desde presupuesto o contrato)
        if (presupuesto != null) {
            replacements.put("{{CODIGO_PROYECTO}}",
                    presupuesto.getInversion() != null ? presupuesto.getInversion() : "");
            replacements.put("{{BPIN}}", presupuesto.getBpin() != null ? presupuesto.getBpin() : "");

            // Nombre del proyecto desde Ficha EBI
            String nombreProyecto = presupuesto.getFichaEbiNombre() != null
                    && !presupuesto.getFichaEbiNombre().trim().isEmpty()
                            ? presupuesto.getFichaEbiNombre()
                            : (contrato.getObjeto() != null ? contrato.getObjeto() : "");
            replacements.put("{{NOMBRE_PROYECTO}}", nombreProyecto);
            replacements.put("{{FICHA_EBI_NOMBRE}}",
                    presupuesto.getFichaEbiNombre() != null ? presupuesto.getFichaEbiNombre() : "");
        } else {
            replacements.put("{{CODIGO_PROYECTO}}", "");
            replacements.put("{{BPIN}}", "");
            replacements.put("{{NOMBRE_PROYECTO}}", contrato.getObjeto() != null ? contrato.getObjeto() : "");
            replacements.put("{{FICHA_EBI_NOMBRE}}", "");
        }

        // Información del Supervisor
        if (supervisor != null) {
            replacements.put("{{NOMBRE_SUPERVISOR}}",
                    supervisor.getNombre() != null ? supervisor.getNombre().toUpperCase() : "");
            replacements.put("{{CARGO_SUPERVISOR}}", supervisor.getCargo() != null ? supervisor.getCargo() : "");
        } else {
            replacements.put("{{NOMBRE_SUPERVISOR}}", "SIN DESIGNAR");
            replacements.put("{{CARGO_SUPERVISOR}}", "");
        }

        // Información Presupuestal (CDP)
        if (presupuesto != null) {
            replacements.put("{{NUMERO_CDP}}", presupuesto.getCdpNumero() != null ? presupuesto.getCdpNumero() : "");
            
            BigDecimal cdpVal = presupuesto.getCdpValor();
            replacements.put("{{VALOR_CDP}}", cdpVal != null ? formatearMoneda(cdpVal) : "");
            replacements.put("{{CDP_VALOR}}", cdpVal != null ? formatearMoneda(cdpVal) : "");
            
            replacements.put("{{CERTIFICADO_INSUFICIENCIA}}", presupuesto.getCertificadoInsuficiencia() != null ? presupuesto.getCertificadoInsuficiencia() : "");
            
            String cdpLetras = cdpVal != null ? convertirMontoALetras(cdpVal) : "";
            replacements.put("{{VALOR_CDP_LETRAS}}", cdpLetras);
            replacements.put("{{CDP_VALOR_LETRAS}}", cdpLetras);
            
            // Versión en minúsculas (formato frase)
            String cdpLetrasMin = !cdpLetras.isEmpty() ? capitalizeFirst(cdpLetras) : "";
            replacements.put("{{VALOR_CDP_LETRAS_MIN}}", cdpLetrasMin);
            replacements.put("{{CDP_VALOR_LETRAS_MIN}}", cdpLetrasMin);
            
            replacements.put("{{COMPROMISO_CDP}}",
                    presupuesto.getApropiacionPresupuestal() != null ? presupuesto.getApropiacionPresupuestal() : "");

            // Fechas del CDP
            SimpleDateFormat sdfDoc = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
            if (presupuesto.getCdpFecha() != null) {
                replacements.put("{{FECHA_EXPEDICION_CDP}}", sdfDoc.format(presupuesto.getCdpFecha()));
            } else {
                replacements.put("{{FECHA_EXPEDICION_CDP}}", "");
            }

            if (presupuesto.getCdpVencimiento() != null) {
                replacements.put("{{FECHA_VENCIMIENTO_CDP}}", sdfDoc.format(presupuesto.getCdpVencimiento()));
            } else {
                replacements.put("{{FECHA_VENCIMIENTO_CDP}}", "");
            }

            // --- NUEVOS CAMPOS DE ADICIÓN ---
            replacements.put("{{CDP_ADICION}}", presupuesto.getCdpAdicion() != null ? presupuesto.getCdpAdicion() : "");
            
            BigDecimal valAdicion = presupuesto.getCdpValorAdicion();
            if (valAdicion != null) {
                replacements.put("{{VALOR_CDP_ADICION}}", formatearMoneda(valAdicion));
                replacements.put("{{VALOR_CDP_ADICION_LETRAS}}", convertirMontoALetras(valAdicion));
            } else {
                replacements.put("{{VALOR_CDP_ADICION}}", "");
                replacements.put("{{VALOR_CDP_ADICION_LETRAS}}", "");
            }

            replacements.put("{{RP_ADICION}}", presupuesto.getRpAdicion() != null ? presupuesto.getRpAdicion() : "");
            if (presupuesto.getRpFechaAdicion() != null) {
                replacements.put("{{FECHA_RP_ADICION}}", sdfDoc.format(presupuesto.getRpFechaAdicion()));
            } else {
                replacements.put("{{FECHA_RP_ADICION}}", "");
            }
            // --------------------------------
        } else {
            replacements.put("{{NUMERO_CDP}}", "");
            replacements.put("{{FECHA_EXPEDICION_CDP}}", "");
            replacements.put("{{FECHA_VENCIMIENTO_CDP}}", "");
            replacements.put("{{VALOR_CDP}}", "");
            replacements.put("{{COMPROMISO_CDP}}", "");
        }

        // Información del Contrato
        if (contrato.getValorTotalLetras() != null) {
            replacements.put("{{VALOR_CONTRATO_LETRAS}}", contrato.getValorTotalLetras());
        } else {
            replacements.put("{{VALOR_CONTRATO_LETRAS}}", "");
        }

        if (contrato.getValorTotalNumeros() != null) {
            replacements.put("{{VALOR_CONTRATO}}", formatearMoneda(contrato.getValorTotalNumeros()));
        } else {
            replacements.put("{{VALOR_CONTRATO}}", "");
        }
        
        // Antes de IVA
        if (contrato.getValorAntesIvaLetras() != null) {
            replacements.put("{{VALOR_ANTES_IVA_LETRAS}}", contrato.getValorAntesIvaLetras());
        } else {
            replacements.put("{{VALOR_ANTES_IVA_LETRAS}}", "");
        }
        if (contrato.getValorAntesIva() != null) {
            replacements.put("{{VALOR_ANTES_IVA}}", formatearMoneda(contrato.getValorAntesIva()));
        } else {
            replacements.put("{{VALOR_ANTES_IVA}}", "");
        }
        
        // IVA
        if (contrato.getValorIvaLetras() != null) {
            replacements.put("{{VALOR_IVA_LETRAS}}", contrato.getValorIvaLetras());
        } else {
            replacements.put("{{VALOR_IVA_LETRAS}}", "");
        }
        if (contrato.getValorIva() != null) {
            replacements.put("{{VALOR_IVA}}", formatearMoneda(contrato.getValorIva()));
        } else {
            replacements.put("{{VALOR_IVA}}", "");
        }

        if (contrato.getValorCuotaLetras() != null) {
            replacements.put("{{VALOR_CUOTA_LETRAS}}", contrato.getValorCuotaLetras());
        } else {
            replacements.put("{{VALOR_CUOTA_LETRAS}}", "");
        }
        
        // Cuota antes IVA
        if (contrato.getValorCuotaAntesIvaLetras() != null) {
            replacements.put("{{VALOR_CUOTA_ANTES_IVA_LETRAS}}", contrato.getValorCuotaAntesIvaLetras());
        } else {
            replacements.put("{{VALOR_CUOTA_ANTES_IVA_LETRAS}}", "");
        }
        if (contrato.getValorCuotaAntesIva() != null) {
            replacements.put("{{VALOR_CUOTA_ANTES_IVA}}", formatearMoneda(contrato.getValorCuotaAntesIva()));
        } else {
            replacements.put("{{VALOR_CUOTA_ANTES_IVA}}", "");
        }
        
        // Cuota IVA
        if (contrato.getValorCuotaIvaLetras() != null) {
            replacements.put("{{VALOR_CUOTA_IVA_LETRAS}}", contrato.getValorCuotaIvaLetras());
        } else {
            replacements.put("{{VALOR_CUOTA_IVA_LETRAS}}", "");
        }
        if (contrato.getValorCuotaIva() != null) {
            replacements.put("{{VALOR_CUOTA_IVA}}", formatearMoneda(contrato.getValorCuotaIva()));
        } else {
            replacements.put("{{VALOR_CUOTA_IVA}}", "");
        }

        // Número de cuotas (usando plazo meses como aproximación si aplica)
        replacements.put("{{NUMERO_CUOTAS}}",
                contrato.getPlazoMeses() > 0 ? String.valueOf(contrato.getPlazoMeses()) : "PENDIENTE");

        // Nuevos campos: Adición y SECOP
        replacements.put("{{IVA_SI_NO}}", contrato.getIvaSiNo() != null ? contrato.getIvaSiNo() : "");
        replacements.put("{{ADICION_SI_NO}}", contrato.getAdicionSiNo() != null ? contrato.getAdicionSiNo() : "");
        replacements.put("{{NUMERO_CUOTAS_ADICION}}", contrato.getNumeroCuotasAdicion() > 0 ? String.valueOf(contrato.getNumeroCuotasAdicion()) : "");
        if (contrato.getNumeroCuotasAdicion() > 0) {
            replacements.put("{{NUMERO_CUOTAS_ADICION_LETRAS}}", convertirNumeroALetras(contrato.getNumeroCuotasAdicion()));
        } else {
            replacements.put("{{NUMERO_CUOTAS_ADICION_LETRAS}}", "");
        }

        replacements.put("{{NUMERO_CUOTAS_NUMERO}}", contrato.getNumCuotasNumero() > 0 ? String.valueOf(contrato.getNumCuotasNumero()) : "");
        replacements.put("{{NUM_CUOTAS_NUMERO}}", contrato.getNumCuotasNumero() > 0 ? String.valueOf(contrato.getNumCuotasNumero()) : "");
        replacements.put("{{NUMERO_CUOTAS_LETRAS}}", contrato.getNumCuotasLetras() != null ? contrato.getNumCuotasLetras() : "");
        replacements.put("{{NUM_CUOTAS_LETRAS}}", contrato.getNumCuotasLetras() != null ? contrato.getNumCuotasLetras() : "");

        replacements.put("{{VALOR_TOTAL_ADICION_LETRAS}}", contrato.getValorTotalAdicionLetras() != null ? contrato.getValorTotalAdicionLetras() : "");
        replacements.put("{{VALOR_TOTAL_ADICION}}", contrato.getValorTotalAdicion() != null ? formatearMoneda(contrato.getValorTotalAdicion()) : "");
        replacements.put("{{VALOR_CONTRATO_MAS_ADICION_LETRAS}}", contrato.getValorContratoMasAdicionLetras() != null ? contrato.getValorContratoMasAdicionLetras() : "");
        replacements.put("{{VALOR_CONTRATO_MAS_ADICION}}", contrato.getValorContratoMasAdicion() != null ? formatearMoneda(contrato.getValorContratoMasAdicion()) : "");
        replacements.put("{{CIRCULAR_HONORARIOS}}", contrato.getCircularHonorarios() != null ? contrato.getCircularHonorarios() : "");
        replacements.put("{{CIRCULAR_CONTRATACION}}", contrato.getCircularHonorarios() != null ? contrato.getCircularHonorarios() : "");
        replacements.put("${CIRCULAR_HONORARIOS}", contrato.getCircularHonorarios() != null ? contrato.getCircularHonorarios() : "");
        replacements.put("{{ENLACE_SECOP}}", contrato.getEnlaceSecop() != null ? contrato.getEnlaceSecop() : "");

        // Configurar X para la Adición si está lleno y es "Sí/Si" o "X"
        String adicionFlag = contrato.getAdicionSiNo();
        boolean esAdicion = false;
        if (adicionFlag != null) {
            String adNorm = java.text.Normalizer.normalize(adicionFlag.trim().toLowerCase(), java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
            esAdicion = adNorm.equals("si") || adNorm.equals("x");
        }
        replacements.put("{{ADICION_X}}", esAdicion ? "X" : " ");
        
        // Ciudad y Fecha de generación actual (Formato corto: Santiago de Cali, Abril 10 de 2026)
        java.util.Date fechaActual = new java.util.Date();
        SimpleDateFormat sdfCorta = new SimpleDateFormat("'Santiago de Cali,' MMMM d 'de' yyyy", new Locale("es", "CO"));
        String fechaHoyCorta = sdfCorta.format(fechaActual);
        // Capitalizar el mes
        if (fechaHoyCorta.contains(",")) {
            String[] parts = fechaHoyCorta.split(", ");
            if (parts.length > 1) {
                String mesDiaAnio = parts[1];
                mesDiaAnio = mesDiaAnio.substring(0, 1).toUpperCase() + mesDiaAnio.substring(1);
                fechaHoyCorta = parts[0] + ", " + mesDiaAnio;
            }
        }
        replacements.put("{{CIUDAD_Y_FECHA_HOY}}", fechaHoyCorta);
        
        // Nueva FECHA_HOY_SIMPLE para partes como "5. Que el día..." (Ej: 10 de abril de 2026)
        SimpleDateFormat sdfSimple = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        replacements.put("{{FECHA_HOY_SIMPLE}}", sdfSimple.format(fechaActual));
        
        java.util.Calendar calHoy = java.util.Calendar.getInstance();
        calHoy.setTime(fechaActual);
        int diaHoy = calHoy.get(java.util.Calendar.DAY_OF_MONTH);
        int anioHoy = calHoy.get(java.util.Calendar.YEAR);
        String mesHoyStr = new SimpleDateFormat("MMMM", new Locale("es", "CO")).format(fechaActual);
        String constanciaFecha = "a los " + convertirNumeroALetras(diaHoy).toLowerCase() + " (" + diaHoy + ") días del mes de " + mesHoyStr + " del año " + anioHoy;
        replacements.put("{{FECHA_CONSTANCIA_HOY}}", constanciaFecha);
        
        // Poner N/A en campos sin datos en el sistema (especialmente para Modificación #1)
        replacements.put("{{ADICIONES}}", "N/A");
        replacements.put("{{ADICIONES_PREVIAS}}", "N/A");
        replacements.put("{{VALOR_TOTAL_A_LA_FECHA}}", "N/A");
        replacements.put("{{VALOR_TOTAL_AL_FECHA}}", "N/A");
        replacements.put("{{PRORROGAS}}", "N/A");
        replacements.put("{{ACLARACION}}", "N/A");
        replacements.put("{{SUSPENSION}}", "N/A");
        replacements.put("{{REANUDACION}}", "N/A");
        // Cesión: N/A fijo directo en la plantilla Word (no requiere variable)
        
        // Nombre y cargo del supervisor
        if (supervisor != null && supervisor.getNombre() != null) {
            replacements.put("{{SUPERVISOR_NOMBRE_COMPLETO}}", supervisor.getNombre().toUpperCase());
        } else {
            replacements.put("{{SUPERVISOR_NOMBRE_COMPLETO}}", "NOMBRE DEL SUPERVISOR NO ENCONTRADO");
        }
        if (supervisor != null && supervisor.getCargo() != null) {
            replacements.put("{{SUPERVISOR_CARGO}}", supervisor.getCargo().toUpperCase());
        } else {
            replacements.put("{{SUPERVISOR_CARGO}}", "CARGO NO ENCONTRADO");
        }

        // Fechas de inicio, terminación y suscripción
        SimpleDateFormat dateFormat = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        if (contrato.getFechaTerminacion() != null) {
            String fechaFinEspecial = formatearFechaLargaLegal(contrato.getFechaTerminacion());
            // Capitalizar la primera letra si es necesario (ej: Treinta...)
            if (!fechaFinEspecial.isEmpty()) {
                fechaFinEspecial = fechaFinEspecial.substring(0, 1).toUpperCase() + fechaFinEspecial.substring(1);
            }
            replacements.put("{{FECHA_FIN_CONTRATO}}", fechaFinEspecial);
        } else {
            replacements.put("{{FECHA_FIN_CONTRATO}}", "FECHA PENDIENTE");
        }
        
        // Nueva variable FECHA_FINAL_ADICIONAL
        if (contrato.getFechaTerminacion() != null && contrato.getNumeroCuotasAdicion() > 0) {
            java.util.Calendar calAdicional = java.util.Calendar.getInstance();
            calAdicional.setTime(contrato.getFechaTerminacion());
            calAdicional.add(java.util.Calendar.MONTH, contrato.getNumeroCuotasAdicion());
            
            SimpleDateFormat sdfFinal = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
            replacements.put("{{FECHA_FINAL_ADICIONAL}}", sdfFinal.format(calAdicional.getTime()));
        } else {
            replacements.put("{{FECHA_FINAL_ADICIONAL}}", "");
        }
        
        // Fecha de inicio mapeada a Fecha de Ejecucion
        if (contrato.getFechaEjecucion() != null) {
            replacements.put("{{FECHA_ACTA_INICIO}}", dateFormat.format(contrato.getFechaEjecucion()));
        } else {
            replacements.put("{{FECHA_ACTA_INICIO}}", "FECHA PENDIENTE");
        }
        
        // Fecha de suscripción mapeada a Fecha de Aprobacion
        if (contrato.getFechaAprobacion() != null) {
            replacements.put("{{FECHA_SUSCRIPCION}}", dateFormat.format(contrato.getFechaAprobacion()));
        } else {
            replacements.put("{{FECHA_SUSCRIPCION}}", "FECHA PENDIENTE");
        }

        // ID del PAA (Plan Anual de Adquisiciones)
        if (presupuesto != null && presupuesto.getIdPaa() != null) {
            replacements.put("{{ID_PAA}}", presupuesto.getIdPaa());
        } else {
            replacements.put("{{ID_PAA}}", "");
        }

        if (presupuesto != null && presupuesto.getIdPaaSiNo() != null) {
            replacements.put("{{ID_PAA_SI_NO}}", presupuesto.getIdPaaSiNo());
            
            // Lógica para ocultar el párrafo si NO es PAA
            String paaNorm = java.text.Normalizer.normalize(presupuesto.getIdPaaSiNo().trim().toLowerCase(), java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
            if (paaNorm.equals("si") || paaNorm.equals("x")) {
                replacements.put("{{OCULTAR_SI_NO_ES_PAA}}", ""); // Dejar el texto intacto
            } else {
                replacements.put("{{OCULTAR_SI_NO_ES_PAA}}", "{{REMOVE_PARAGRAPH}}"); // Eliminar párrafo entero
            }
        } else {
            replacements.put("{{ID_PAA_SI_NO}}", "");
            replacements.put("{{OCULTAR_SI_NO_ES_PAA}}", "{{REMOVE_PARAGRAPH}}"); // Eliminar si no hay datos
        }

        // ===== PLACEHOLDERS ANTIGUOS CONTINUACIÓN =====

        // Values
        if (contrato.getValorTotalNumeros() != null)
            replacements.put("${VALOR_TOTAL}", formatearMoneda(contrato.getValorTotalNumeros()));
        else
            replacements.put("${VALOR_TOTAL}", "0");

        if (contrato.getValorCuotaNumero() != null)
            replacements.put("${VALOR_MENSUAL}", formatearMoneda(contrato.getValorCuotaNumero()));
        else
            replacements.put("${VALOR_MENSUAL}", "0");

        // Nuevos placeholders para valores de IVA
        if (contrato.getValorAntesIva() != null)
            replacements.put("{{VALOR_ANTES_IVA}}", formatearMoneda(contrato.getValorAntesIva()));
        else
            replacements.put("{{VALOR_ANTES_IVA}}", "0");

        if (contrato.getValorIva() != null)
            replacements.put("{{VALOR_IVA}}", formatearMoneda(contrato.getValorIva()));
        else
            replacements.put("{{VALOR_IVA}}", "0");

        if (contrato.getValorCuotaAntesIva() != null)
            replacements.put("{{VALOR_CUOTA_ANTES_IVA}}", formatearMoneda(contrato.getValorCuotaAntesIva()));
        else
            replacements.put("{{VALOR_CUOTA_ANTES_IVA}}", "0");

        if (contrato.getValorCuotaIva() != null)
            replacements.put("{{VALOR_CUOTA_IVA}}", formatearMoneda(contrato.getValorCuotaIva()));
        else
            replacements.put("{{VALOR_CUOTA_IVA}}", "0");

        replacements.put("{{VALOR_ANTES_IVA_LETRAS}}", contrato.getValorAntesIvaLetras() != null ? contrato.getValorAntesIvaLetras() : "");
        replacements.put("{{VALOR_IVA_LETRAS}}", contrato.getValorIvaLetras() != null ? contrato.getValorIvaLetras() : "");
        replacements.put("{{VALOR_CUOTA_ANTES_IVA_LETRAS}}", contrato.getValorCuotaAntesIvaLetras() != null ? contrato.getValorCuotaAntesIvaLetras() : "");
        replacements.put("{{VALOR_CUOTA_IVA_LETRAS}}", contrato.getValorCuotaIvaLetras() != null ? contrato.getValorCuotaIvaLetras() : "");


        // Ordenador
        if (ordenador != null) {
            replacements.put("${NOMBRE_ORDENADOR}",
                    ordenador.getNombreOrdenador() != null ? ordenador.getNombreOrdenador().toUpperCase() : "");
            replacements.put("${CARGO_ORDENADOR}",
                    ordenador.getCargoOrdenador() != null ? ordenador.getCargoOrdenador() : "");
            replacements.put("${ORGANISMO_ORDENADOR}",
                    ordenador.getOrganismo() != null ? ordenador.getOrganismo() : "");
        } else {
            replacements.put("${NOMBRE_ORDENADOR}", "SIN DESIGNAR");
            replacements.put("${CARGO_ORDENADOR}", "");
            replacements.put("${ORGANISMO_ORDENADOR}", "");
        }

        // Estructurador
        if (estructurador != null) {
            replacements.put("${NOMBRE_ESTRUCTURADOR_JURIDICO}",
                    estructurador.getJuridicoNombre() != null ? estructurador.getJuridicoNombre().toUpperCase() : "");
            replacements.put("${CARGO_ESTRUCTURADOR_JURIDICO}",
                    estructurador.getJuridicoCargo() != null ? estructurador.getJuridicoCargo() : "");
            replacements.put("${NOMBRE_ESTRUCTURADOR_TECNICO}",
                    estructurador.getTecnicoNombre() != null ? estructurador.getTecnicoNombre().toUpperCase() : "");
            replacements.put("${CARGO_ESTRUCTURADOR_TECNICO}",
                    estructurador.getTecnicoCargo() != null ? estructurador.getTecnicoCargo() : "");
            replacements.put("${NOMBRE_ESTRUCTURADOR_FINANCIERO}",
                    estructurador.getFinancieroNombre() != null ? estructurador.getFinancieroNombre().toUpperCase()
                            : "");
            replacements.put("${CARGO_ESTRUCTURADOR_FINANCIERO}",
                    estructurador.getFinancieroCargo() != null ? estructurador.getFinancieroCargo() : "");

            // Nuevos Placeholders {{}} para Estructuradores
            replacements.put("{{NOMBRE_ESTRUCTURADOR_JURIDICO}}",
                    estructurador.getJuridicoNombre() != null ? estructurador.getJuridicoNombre().toUpperCase() : "");
            replacements.put("{{NOMBRE_ESTRUCTURADOR_FINANCIERO}}",
                    estructurador.getFinancieroNombre() != null ? estructurador.getFinancieroNombre().toUpperCase()
                            : "");
        } else {
            replacements.put("${NOMBRE_ESTRUCTURADOR_JURIDICO}", "SIN ASIGNAR");
            replacements.put("${CARGO_ESTRUCTURADOR_JURIDICO}", "");
            replacements.put("${NOMBRE_ESTRUCTURADOR_TECNICO}", "SIN ASIGNAR");
            replacements.put("${CARGO_ESTRUCTURADOR_TECNICO}", "");
            replacements.put("${NOMBRE_ESTRUCTURADOR_FINANCIERO}", "SIN ASIGNAR");
            replacements.put("${CARGO_ESTRUCTURADOR_FINANCIERO}", "");

            replacements.put("{{NOMBRE_ESTRUCTURADOR_JURIDICO}}", "SIN ASIGNAR");
            replacements.put("{{NOMBRE_ESTRUCTURADOR_FINANCIERO}}", "SIN ASIGNAR");
        }

        // ===== NUEVOS CAMPOS SOLICITADOS (INVERSION) =====

        // 1. Nivel / Perfil
        replacements.put("{{NIVEL_CONTRATO}}", contrato.getNivel() != null ? contrato.getNivel().toUpperCase() : "");
        // Marca visual "X" para campos de nivel (usado en formularios/checkboxes)
        replacements.put("{{NIVEL_CONTRATO_MARCA}}", "X");

        // Marcas para el tipo de contrato (Profesional / Apoyo)
        String tipoC = contrato.getTipoContrato() != null ? contrato.getTipoContrato().toUpperCase() : "";
        String nivelC = contrato.getNivel() != null ? contrato.getNivel().toUpperCase() : "";
        
        // Formato para el documento (Uppercase como solicitó el usuario)
        String tipoC_Frase = tipoC;
        
        replacements.put("{{TIPO_CONTRATO}}", tipoC_Frase);
        replacements.put("${TIPO_CONTRATO}", tipoC_Frase); // Soporte para formato con $
        
        boolean esProfesional = tipoC.contains("PROFESIONAL") || nivelC.contains("PROFESIONAL");
        boolean esApoyo = tipoC.contains("APOYO") || nivelC.contains("APOYO");
        
        replacements.put("{{MARCA_PROFESIONAL}}", esProfesional ? "X" : "");
        replacements.put("{{MARCA_APOYO}}", esApoyo ? "X" : "");
        
        // Manejo de errores tipográficos en las plantillas del usuario (ej: espacio extra)
        replacements.put("{{ MARCA_PROFESIONAL}}", esProfesional ? "X" : "");
        replacements.put("{{ MARCA_APOYO}}", esApoyo ? "X" : "");

        // 2. Formación y Título (Contratista)
        String formacion = contratista.getFormacionTitulo() != null ? contratista.getFormacionTitulo() : "";
        String descFormacion = contratista.getDescripcionFormacion() != null ? contratista.getDescripcionFormacion() : "";
        
        replacements.put("{{FORMACION}}", formacion);
        replacements.put("{{PERFIL_FORMACION}}", descFormacion);
        
        // Idoneidad (Específicos para el formato de Verificacion de Cumplimiento)
        replacements.put("{{IDONEIDAD_FORMACION}}", formacion);
        replacements.put("{{IDONEIDAD_DESCRIPCION}}", descFormacion);

        // 2.1 Experiencia (Contratista)
        String expShort = contratista.getExperiencia() != null ? contratista.getExperiencia() : "";
        String descExp = contratista.getDescripcionExperiencia() != null ? contratista.getDescripcionExperiencia() : "";
        
        replacements.put("{{EXPERIENCIA}}", expShort);
        replacements.put("{{PERFIL_EXPERIENCIA}}", descExp);
        
        // Específicos para el formato de Verificacion de Cumplimiento
        replacements.put("{{EXPERIENCIA_BASICA}}", expShort);
        replacements.put("{{EXPERIENCIA_DESCRIPCION}}", descExp);

        // 3. Objeto Contractual (Redundancia segura)
        replacements.put("{{OBJETO_CONTRACTUAL}}", contrato.getObjeto() != null ? contrato.getObjeto() : "");

        // 4. Actividades Contractuales
        String actividades = contrato.getActividadesEntregables() != null ? contrato.getActividadesEntregables()
                : "SIN ACTIVIDADES REGISTRADAS";
        // Limpieza de llaves si vienen en la base de datos
        actividades = actividades.replace("{{", "").replace("}}", "").trim();
        replacements.put("{{ACTIVIDADES_CONTRACTUALES}}", actividades);

        // 5. Actividades Ficha EBI (Presupuesto)
        if (presupuesto != null) {
            String fullActEbi = presupuesto.getFichaEbiActividades() != null ? presupuesto.getFichaEbiActividades() : "";
            // Limpiar posibles llaves y espacios
            String actEbi = fullActEbi.replace("{{", "").replace("}}", "").trim();
            
            // EBI No. - Se extrae el código base quitando lo que sigue al primer "/" 
            // ej: BP-26005460/1/02/01/04 -> BP-26005460
            String ebiNumero = actEbi;
            if (ebiNumero.contains("/")) {
                ebiNumero = ebiNumero.substring(0, ebiNumero.indexOf("/")).trim();
            }
            
            // Se usa el valor recortado en ambos placeholders para facilitar su uso en la plantilla
            replacements.put("{{ACTIVIDADES_FICHA_EBI}}", ebiNumero);
            replacements.put("{{EBI_NUMERO}}", ebiNumero);
            
            // Nueva variable con la actividad completa, sin recortar
            replacements.put("{{ACTIVIDAD_ESPECIFICA_EBI}}", actEbi);

            String objEbi = presupuesto.getFichaEbiObjetivo() != null ? presupuesto.getFichaEbiObjetivo() : "";
            objEbi = objEbi.replace("{{", "").replace("}}", "").trim();
            replacements.put("{{FICHA_EBI_OBJETIVO}}", objEbi);
        } else {
            replacements.put("{{ACTIVIDADES_FICHA_EBI}}", "");
            replacements.put("{{FICHA_EBI_OBJETIVO}}", "");
            replacements.put("{{ACTIVIDAD_ESPECIFICA_EBI}}", "");
        }

        // 6. Valores numéricos específicos
        if (contrato.getValorCuotaNumero() != null) {
            replacements.put("{{VALOR_CUOTA_NUMERO}}", formatearMoneda(contrato.getValorCuotaNumero()));
        } else {
            replacements.put("{{VALOR_CUOTA_NUMERO}}", "0");
        }

        // 7. Información del Contratista (para plantillas INVERSION_2, 3, 4)
        replacements.put("{{CONTRATISTA_NOMBRE}}",
                contratista.getNombre() != null ? contratista.getNombre().toUpperCase() : "");
        replacements.put("{{CONTRATISTA_CEDULA}}",
                contratista.getCedula() != null ? contratista.getCedula() : "");

        // 8. Ordenador del Gasto (para plantillas INVERSION)
        if (ordenador != null) {
            replacements.put("{{NOMBRE_ORDENADOR_GASTO}}",
                    ordenador.getNombreOrdenador() != null ? ordenador.getNombreOrdenador().toUpperCase() : "");
            replacements.put("{{CEDULA_ORDENADOR}}",
                    ordenador.getCedulaOrdenador() != null ? ordenador.getCedulaOrdenador() : "");
            replacements.put("{{CARGO_ORDENADOR_GASTO}}",
                    ordenador.getCargoOrdenador() != null ? ordenador.getCargoOrdenador() : "");
            replacements.put("{{ORGANISMO}}",
                    ordenador.getOrganismo() != null ? ordenador.getOrganismo() : "");
            replacements.put("{{DECRETO_NOMBRAMIENTO}}",
                    ordenador.getDecretoNombramiento() != null ? ordenador.getDecretoNombramiento() : "");
            replacements.put("{{ACTA_POSESION}}",
                    ordenador.getActaPosesion() != null ? ordenador.getActaPosesion() : "");
        } else {
            replacements.put("{{NOMBRE_ORDENADOR_GASTO}}", "SIN DESIGNAR");
            replacements.put("{{CEDULA_ORDENADOR}}", "");
            replacements.put("{{CARGO_ORDENADOR_GASTO}}", "");
            replacements.put("{{ORGANISMO}}", "");
            replacements.put("{{DECRETO_NOMBRAMIENTO}}", "");
            replacements.put("{{ACTA_POSESION}}", "");
        }

        // Lógica de fechas base (Priorizar según el tipo de documento)
        java.util.Date fechaBase = (presupuesto != null && presupuesto.getRpFecha() != null) ? presupuesto.getRpFecha()
                : new java.util.Date();
        
        // Priorizar fecha_estructurador para ese documento
        if ("estructuradores".equals(docType) && contrato.getFechaEstructurador() != null) {
            fechaBase = contrato.getFechaEstructurador();
        } 
        // Priorizar fecha_idoneidad para el certificado
        else if ("idoneidad".equals(docType) && contrato.getFechaIdoneidad() != null) {
            fechaBase = contrato.getFechaIdoneidad();
        }

        // Mes y Año para idoneidad (Formato: "marzo de 2026")
        SimpleDateFormat sdfMesAnio = new SimpleDateFormat("MMMM 'de' yyyy", new Locale("es", "CO"));
        
        // 9. Nuevos Campos de Fecha (Idoneidad y Estructurador)
        if (contrato.getFechaIdoneidad() != null) {
            String mesAnioIdoneidad = sdfMesAnio.format(contrato.getFechaIdoneidad());
            replacements.put("{{FECHA_IDONEIDAD}}", mesAnioIdoneidad);
        } else {
            replacements.put("{{FECHA_IDONEIDAD}}", "");
        }
        
        if ("idoneidad".equals(docType)) {
            // Requerimiento: MES_ANIO_ACTUAL debe ser la fecha del sistema (generación)
            replacements.put("{{MES_ANIO_ACTUAL}}", sdfMesAnio.format(new java.util.Date()));
        } else if (contrato.getFechaIdoneidad() != null && (presupuesto == null || presupuesto.getRpFecha() == null)) {
            replacements.put("{{MES_ANIO_ACTUAL}}", sdfMesAnio.format(contrato.getFechaIdoneidad()));
        } else {
            replacements.put("{{MES_ANIO_ACTUAL}}", sdfMesAnio.format(fechaBase));
        }

        if (contrato.getFechaEstructurador() != null) {
            replacements.put("{{FECHA_ESTRUCTURADOR}}", dateFormat.format(contrato.getFechaEstructurador()));
        } else {
            replacements.put("{{FECHA_ESTRUCTURADOR}}", "");
        }

        // RPC
        if (presupuesto != null) {
            replacements.put("${RPC_NUMERO}", presupuesto.getRpNumero() != null ? presupuesto.getRpNumero() : "");
            replacements.put("{{RPC_NUMERO}}", presupuesto.getRpNumero() != null ? presupuesto.getRpNumero() : "");
            replacements.put("{{RPC_NO}}", presupuesto.getRpNumero() != null ? presupuesto.getRpNumero() : "");
            replacements.put("{{CDP_VALOR}}", presupuesto.getCdpValor() != null ? formatearMoneda(presupuesto.getCdpValor()) : "");
        } else {
            replacements.put("{{RPC_NO}}", "");
            replacements.put("{{CDP_VALOR}}", "");
        }
        
        // Certificado Insuficiencia
        if (presupuesto != null && presupuesto.getCertificadoInsuficiencia() != null) {
            replacements.put("{{CERTIFICADO_INSUFICIENCIA}}", presupuesto.getCertificadoInsuficiencia());
        } else {
            replacements.put("{{CERTIFICADO_INSUFICIENCIA}}", "");
        }

        // Fechas adicionales
        if (contrato.getFechaArl() != null) {
            replacements.put("{{FECHA_ARL}}", dateFormat.format(contrato.getFechaArl()));
        } else {
            replacements.put("{{FECHA_ARL}}", "");
        }



        // Standard Date Logic - Formato: "06 de enero de 2026"
        SimpleDateFormat sdfDoc = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        String fechaStr = sdfDoc.format(fechaBase);
        
        if ("supervisor".equals(docType) || "estructuradores".equals(docType)) {
            String fechaGeneracion = sdfDoc.format(new java.util.Date());
            replacements.put("${FECHA_DOCUMENTO}", fechaGeneracion);
            replacements.put("${FECHA_RPC_SUPERVISOR}", fechaGeneracion);
            replacements.put("${FECHA_RPC_APOYO}", fechaGeneracion);
            replacements.put("${RPC_FECHA}", fechaStr); // Esta se mantiene como la fecha del RP
        } else {
            replacements.put("${FECHA_DOCUMENTO}", fechaStr);
            replacements.put("${FECHA_RPC_SUPERVISOR}", fechaStr);
            replacements.put("${FECHA_RPC_APOYO}", fechaStr);
            replacements.put("${RPC_FECHA}", fechaStr);
        }

        SimpleDateFormat yearOnly = new SimpleDateFormat("yyyy", new Locale("es", "CO"));

        // Formato especial para FECHA_DOCUMENTO_LETRAS: "seis (06) días del mes de
        // enero de (2026)"
        java.util.Date fechaLetrasBase = ("supervisor".equals(docType) || "estructuradores".equals(docType)) ? new java.util.Date() : fechaBase;
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(fechaLetrasBase);
        int dia = cal.get(java.util.Calendar.DAY_OF_MONTH);
        String mes = new SimpleDateFormat("MMMM", new Locale("es", "CO")).format(fechaLetrasBase);
        String anio = yearOnly.format(fechaLetrasBase);

        // Convertir día a letras
        String diaLetras = convertirNumeroALetras(dia);
        String diaNumeros = String.format("%02d", dia);

        String fullDateLetters = diaLetras + " (" + diaNumeros + ") días del mes de " + mes + " de (" + anio + ")";
        replacements.put("${FECHA_DOCUMENTO_LETRAS}", fullDateLetters);

        if (supervisor != null) {
            replacements.put("${NOMBRE_SUPERVISOR}",
                    supervisor.getNombre() != null ? supervisor.getNombre().toUpperCase() : "");
            replacements.put("${CARGO_SUPERVISOR}", supervisor.getCargo() != null ? supervisor.getCargo() : "");
            String cedClean = supervisor.getCedula();
            if (cedClean != null && cedClean.contains("-DUP-")) {
                cedClean = cedClean.substring(0, cedClean.indexOf("-DUP-"));
            }
            replacements.put("${CEDULA_SUPERVISOR}", cedClean != null ? cedClean : "");
        } else {
            replacements.put("${NOMBRE_SUPERVISOR}", "SIN DESIGNAR");
            replacements.put("${CARGO_SUPERVISOR}", "");
            replacements.put("${CEDULA_SUPERVISOR}", "");
        }

        boolean conApoyo = contrato.getApoyoSupervision() != null && !contrato.getApoyoSupervision().trim().isEmpty();
        if (conApoyo) {
            replacements.put("${NOMBRE_APOYO}", contrato.getApoyoSupervision().toUpperCase());
        } else {
            replacements.put("${NOMBRE_APOYO}", "");
        }

        // ===== ELABORÓ / PROYECTÓ Y REVISÓ =====
        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("usuario");
        if (usuarioLogueado != null) {
            String nombreTitleCase = toTitleCase(usuarioLogueado.getNombreCompleto());
            String vinculacionTitleCase = toTitleCase(usuarioLogueado.getCargo());
            
            replacements.put("${NOMBRE_PROYECTO}", nombreTitleCase);
            replacements.put("${CARGO_PROYECTO}", vinculacionTitleCase);
            replacements.put("{{NOMBRE_ELABORO}}", nombreTitleCase);
            replacements.put("{{CARGO_ELABORO}}", vinculacionTitleCase);
        } else {
            replacements.put("${NOMBRE_PROYECTO}", "Sin Designar");
            replacements.put("${CARGO_PROYECTO}", "");
            replacements.put("{{NOMBRE_ELABORO}}", "Sin Designar");
            replacements.put("{{CARGO_ELABORO}}", "");
        }

        if (realTemplateName != null) {
            String searchTpl = realTemplateName.replace(".docx", "").trim();
            RevisorDocumento revisor = revisorDAO.obtenerPorTipoDocumento(searchTpl);
            if (revisor != null) {
                String revisorNombre = toTitleCase(revisor.getNombreCompleto());
                String revisorCargo = toTitleCase(revisor.getCargo());
                replacements.put("${NOMBRE_REVISO}", revisorNombre);
                replacements.put("${CARGO_REVISO}", revisorCargo);
                replacements.put("{{NOMBRE_REVISO}}", revisorNombre);
                replacements.put("{{CARGO_REVISO}}", revisorCargo);
            } else {
                replacements.put("${NOMBRE_REVISO}", "Sin Designar");
                replacements.put("${CARGO_REVISO}", "");
                replacements.put("{{NOMBRE_REVISO}}", "Sin Designar");
                replacements.put("{{CARGO_REVISO}}", "");
            }
        } else {
            replacements.put("${NOMBRE_REVISO}", "Sin Designar");
            replacements.put("${CARGO_REVISO}", "");
            replacements.put("{{NOMBRE_REVISO}}", "Sin Designar");
            replacements.put("{{CARGO_REVISO}}", "");
        }

        return replacements;
    }

    private byte[] generarBytesDocumento(HttpServletRequest request, int contratistaId, String docType) throws Exception {
        Contrato contratoCheck = obtenerContratoParaGeneracion(contratistaId, request);
        String templateName;
        if ("estructuradores".equals(docType)) {
            templateName = "DESIGNACION_RESPONSABLES_PARA_ESTRUCTURAR.docx";
        } else {
            boolean conApoyo = contratoCheck != null && contratoCheck.getApoyoSupervision() != null && !contratoCheck.getApoyoSupervision().trim().isEmpty();
            templateName = conApoyo ? "DESIGNACION_SUPERVISOR_CON APOYO.docx" : "DESIGNACION_SUPERVISOR_SIN_APOYO.docx";
        }

        Map<String, String> replacements = getFullReplacements(request, contratistaId, docType, templateName);
        if (replacements == null)
            return null;

        return generateBytes(templateName, replacements);
    }

    /**
     * Extrae el número consecutivo del número de contrato.
     * Ejemplo: "4121.010.26.1.014" -> "014"
     * Si no tiene puntos, devuelve el valor tal cual.
     */
    private String extraerConsecutivo(String numeroContrato) {
        if (numeroContrato == null || numeroContrato.trim().isEmpty()) {
            return "";
        }
        String nc = numeroContrato.trim();
        int lastDot = nc.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < nc.length() - 1) {
            return nc.substring(lastDot + 1); // ej: "014"
        }
        return nc; // Si no tiene punto, usar todo
    }

    private String normalizeFileName(String input) {
        if (input == null)
            return "Unknown";
        // Normalize Unicode (NFD splits accents from letters)
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        // Remove accents (non-spacing marks)
        normalized = normalized.replaceAll("\\p{M}", "");
        // Keep only alphanumeric, spaces, dots, dashes
        normalized = normalized.replaceAll("[^a-zA-Z0-9 ._-]", "");
        return normalized.trim();
    }

    /**
     * Formatea un número como moneda colombiana con separadores de miles.
     * Ejemplo: 19220000 -> "$ 19.220.000"
     * 
     * @param valor El valor numérico a formatear
     * @return String formateado con símbolo $ y separadores de miles
     */
    private String formatearMoneda(Number valor) {
        if (valor == null) {
            return "";
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CO"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat formatter = new DecimalFormat("$#,##0", symbols);
        return formatter.format(valor);
    }

    /**
     * Convierte un número (1-31) a su representación en letras en español.
     * Usado para formatear días del mes.
     * 
     * @param numero El día del mes (1-31)
     * @return String con el número en letras
     */
    private String convertirNumeroALetras(int numero) {
        String[] unidades = { "", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve" };
        String[] decenas = { "", "diez", "veinte", "treinta" };
        String[] especiales = { "diez", "once", "doce", "trece", "catorce", "quince", "dieciséis",
                "diecisiete", "dieciocho", "diecinueve" };
        String[] veintitantos = { "veinte", "veintiuno", "veintidós", "veintitrés", "veinticuatro",
                "veinticinco", "veintiséis", "veintisiete", "veintiocho", "veintinueve" };

        if (numero < 1 || numero > 31) {
            return String.valueOf(numero);
        }

        if (numero < 10) {
            return unidades[numero];
        } else if (numero < 20) {
            return especiales[numero - 10];
        } else if (numero < 30) {
            return veintitantos[numero - 20];
        } else if (numero == 30) {
            return "treinta";
        } else {
            return "treinta y " + unidades[numero - 30];
        }
    }

    private void generarModificacionIndividual(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int contratistaId = Integer.parseInt(request.getParameter("id"));
            Contratista c = contratistaDAO.obtenerPorId(contratistaId);
            String cedula = (c.getCedula() != null ? c.getCedula() : "Doc");

            Contrato contrato = obtenerContratoParaGeneracion(contratistaId, request);

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos, java.nio.charset.StandardCharsets.UTF_8);

            String consecutivo = extraerConsecutivo(contrato != null ? contrato.getNumeroContrato() : null);
            String nombreFolder = c.getNombre() != null ? c.getNombre() : "Contratista_" + contratistaId;
            String folderName = normalizeFileName("4121-" + consecutivo + "-" + nombreFolder);

            String adicionFlag = contrato != null ? contrato.getAdicionSiNo() : null;
            boolean esAdicion = false;
            if (adicionFlag != null) {
                String adNorm = java.text.Normalizer.normalize(adicionFlag.trim().toLowerCase(), java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
                esAdicion = adNorm.equals("si") || adNorm.equals("x");
            }

            java.util.List<DocxEntry> pendingDocs = new java.util.ArrayList<>();

            if (esAdicion) {
                String[][] plantillas = {
                    {"MODIFICACION_1_JUSTIFICACION.docx", "JUSTIFICACI\u00D3N No. 001"},
                    {"MODIFICACION_2_ACEPTACION.docx", "MODIFICACI\u00D3N No. 001"}
                };
                for (String[] par : plantillas) {
                    Map<String, String> replacements = getFullReplacements(request, contratistaId, "modificacion", par[0]);
                    if (replacements == null) continue;
                    byte[] docxBytes = generateBytes(par[0], replacements);
                    if (docxBytes != null) {
                        pendingDocs.add(new DocxEntry(folderName, par[1], docxBytes));
                    }
                }
                empaquetarYConvertirZip(pendingDocs, zos);
            } else {
                String alertaTexto = "El contrato para " + (c != null ? c.getNombre() : "el contratista seleccionado") + 
                                     " no tiene registrada una ADICIÓN en el sistema.\n" +
                                     "Para generar estos documentos, el campo 'Adición' debe ser 'Sí' o 'X'.";
                addToZip(zos, folderName, "ALERTA_SIN_ADICION_" + cedula + ".txt", alertaTexto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            zos.close();

            byte[] zipBytes = baos.toByteArray();
            String zipFilename = folderName + ".zip";

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFilename + "\"");
            response.getOutputStream().write(zipBytes);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generando documentos de modificacion");
        }
    }

    private void generarModificacionMasivoZip(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idsParam = request.getParameter("ids");
        if (idsParam == null || idsParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se seleccionaron contratistas");
            return;
        }

        String[] ids = idsParam.split(",");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos, java.nio.charset.StandardCharsets.UTF_8);
        java.util.List<DocxEntry> pendingDocs = new java.util.ArrayList<>();

        try {
            for (String idStr : ids) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    Contratista c = contratistaDAO.obtenerPorId(id);
                    if (c == null) continue;

                    String cedula = (c.getCedula() != null ? c.getCedula() : "Doc");
                    Contrato contrato = obtenerContratoParaGeneracion(id, request);

                    String consecutivo = extraerConsecutivo(contrato != null ? contrato.getNumeroContrato() : null);
                    String nombreFolder = c.getNombre() != null ? c.getNombre() : "Contratista_" + id;
                    String folderName = normalizeFileName("4121-" + consecutivo + "-" + nombreFolder);

                    String adicionFlag = contrato != null ? contrato.getAdicionSiNo() : null;
                    boolean esAdicion = false;
                    if (adicionFlag != null) {
                        String adNorm = java.text.Normalizer.normalize(adicionFlag.trim().toLowerCase(), java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
                        esAdicion = adNorm.equals("si") || adNorm.equals("x");
                    }

                    if (esAdicion) {
                        String[][] plantillas = {
                            {"MODIFICACION_1_JUSTIFICACION.docx", "JUSTIFICACI\u00D3N No. 001"},
                            {"MODIFICACION_2_ACEPTACION.docx", "MODIFICACI\u00D3N No. 001"}
                        };
                        for (String[] par : plantillas) {
                            Map<String, String> replacements = getFullReplacements(request, id, "modificacion", par[0]);
                            if (replacements == null) continue;
                            byte[] docxBytes = generateBytes(par[0], replacements);
                            if (docxBytes != null) {
                                pendingDocs.add(new DocxEntry(folderName, par[1], docxBytes));
                            }
                        }
                    } else {
                        String alertaTexto = "El contrato para " + (c != null ? c.getNombre() : "este contratista") + 
                                             " no tiene registrada una ADICIÓN en el sistema.\n" +
                                             "Para generar estos documentos, el campo 'Adición' debe ser 'Sí' o 'X'.";
                        addToZip(zos, folderName, "ALERTA_SIN_ADICION_" + cedula + ".txt", alertaTexto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            empaquetarYConvertirZip(pendingDocs, zos);
            zos.close();

            byte[] zipBytes = baos.toByteArray();
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"Modificaciones_Masivas_" + System.currentTimeMillis() + ".zip\"");
            response.getOutputStream().write(zipBytes);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generando ZIP");
        }
    }

    /**
     * Convierte un texto a minúsculas y capitaliza solo la primera letra.
     */
    private String capitalizeFirst(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        String low = input.toLowerCase().trim();
        return Character.toUpperCase(low.charAt(0)) + low.substring(1);
    }

    /**
     * Convierte un texto a Title Case (Primera letra de cada palabra en mayúscula).
     */
    private String toTitleCase(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() > 0) {
                sb.append(Character.toUpperCase(words[i].charAt(0)))
                  .append(words[i].substring(1));
            }
            if (i < words.length - 1) sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Convierte un monto BigDecimal a su representación en letras (Castellano).
     * Ejemplo: 1500000 -> "UN MILLÓN QUINIENTOS MIL PESOS M/CTE"
     */
    private String convertirMontoALetras(java.math.BigDecimal monto) {
        if (monto == null) return "";
        
        long lPart = monto.longValue();
        if (lPart == 0) return "CERO PESOS M/CTE";
        
        String letras = convertirNumeroALetrasGrandes(lPart);
        
        // Regla: Millón/Millones + DE + PESOS
        if (letras.endsWith("MILLON") || letras.endsWith("MILLONES")) {
            letras += " DE";
        }
        
        return letras + " PESOS M/CTE";
    }

    private String convertirNumeroALetrasGrandes(long n) {
        if (n == 0) return "CERO";
        if (n == 1) return "UN";
        if (n == 100) return "CIEN";
        
        if (n < 1000) return getCentenas(n);
        if (n < 1000000) {
            long mil = n / 1000;
            long resto = n % 1000;
            String sMil = (mil == 1) ? "MIL" : (convertirNumeroALetrasGrandes(mil) + " MIL");
            return (resto == 0) ? sMil : (sMil + " " + getCentenas(resto));
        }
        
        long millon = n / 1000000;
        long restoMillon = n % 1000000;
        String sMillon = (millon == 1) ? "UN MILLON" : (convertirNumeroALetrasGrandes(millon) + " MILLONES");
        
        if (restoMillon == 0) return sMillon;
        return sMillon + " " + (restoMillon < 1000 ? getCentenas(restoMillon) : convertirNumeroALetrasGrandes(restoMillon));
    }

    private String getCentenas(long n) {
        if (n > 999) return "";
        if (n == 100) return "CIEN";
        if (n < 10) return getUnidades(n);
        if (n < 20) return getEspeciales(n);
        if (n < 100) {
            int d = (int)(n / 10);
            int u = (int)(n % 10);
            String[] dec = {"", "", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"};
            if (n == 20) return "VEINTE";
            if (n < 30) return "VEINTI" + getUnidades(u);
            return dec[d] + (u == 0 ? "" : " Y " + getUnidades(u));
        }
        
        int c = (int)(n / 100);
        long r = n % 100;
        String[] cent = {"", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"};
        return cent[c] + (r == 0 ? "" : " " + getCentenas(r));
    }

    private String getUnidades(long n) {
        String[] u = {"", "UN", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE"};
        return u[(int)n];
    }

    private String getEspeciales(long n) {
        String[] esp = {"DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE"};
        return esp[(int)(n - 10)];
    }

    private void addDocxAndPdfToZip(java.util.zip.ZipOutputStream zos, String folderName, String baseNameWithoutExt, byte[] docxBytes) throws IOException {
        addToZip(zos, folderName, baseNameWithoutExt + ".docx", docxBytes);
        try {
            File tempDocx = File.createTempFile("pdfgen_", ".docx");
            File tempPdf = new File(tempDocx.getAbsolutePath().replace(".docx", ".pdf"));
            java.nio.file.Files.write(tempDocx.toPath(), docxBytes);
            
            if (com.combinacion.util.PdfGenerator.convertToPdf(tempDocx, tempPdf)) {
                byte[] pdfBytes = java.nio.file.Files.readAllBytes(tempPdf.toPath());
                addToZip(zos, folderName, baseNameWithoutExt + ".pdf", pdfBytes);
            }
            tempDocx.delete();
            tempPdf.delete();
        } catch (Exception ex) {
            System.err.println("⚠️ No se pudo generar el PDF para " + baseNameWithoutExt + ": " + ex.getMessage());
        }
    }

    private static class DocxEntry {
        String folderName;
        String baseNameWithoutExt;
        byte[] docxBytes;
        public DocxEntry(String folderName, String baseNameWithoutExt, byte[] docxBytes) {
            this.folderName = folderName;
            this.baseNameWithoutExt = baseNameWithoutExt;
            this.docxBytes = docxBytes;
        }
    }

    private void empaquetarYConvertirZip(java.util.List<DocxEntry> pendingDocs, java.util.zip.ZipOutputStream zos) throws IOException {
        if (pendingDocs == null || pendingDocs.isEmpty()) return;

        File tempDir = java.nio.file.Files.createTempDirectory("batch_pdf_").toFile();
        try {
            // Escribir DOCX con nombres numerados seguros
            for (int i = 0; i < pendingDocs.size(); i++) {
                DocxEntry doc = pendingDocs.get(i);
                File tempDocx = new File(tempDir, "doc_" + i + ".docx");
                java.nio.file.Files.write(tempDocx.toPath(), doc.docxBytes);
            }

            // Convertir por lotes de una sola vez
            com.combinacion.util.PdfGenerator.convertBatchToPdf(tempDir, tempDir);

            // Leer de vuelta y empacar
            for (int i = 0; i < pendingDocs.size(); i++) {
                DocxEntry doc = pendingDocs.get(i);
                addToZip(zos, doc.folderName, doc.baseNameWithoutExt + ".docx", doc.docxBytes);
                
                File expectedPdf = new File(tempDir, "doc_" + i + ".pdf");
                if (expectedPdf.exists()) {
                    byte[] pdfBytes = java.nio.file.Files.readAllBytes(expectedPdf.toPath());
                    addToZip(zos, doc.folderName, doc.baseNameWithoutExt + ".pdf", pdfBytes);
                }
            }
        } finally {
            deleteDirectory(tempDir);
        }
    }

    private void deleteDirectory(File dir) {
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) deleteDirectory(f);
                    else f.delete();
                }
            }
            dir.delete();
        }
    }

    /**
     * Formatea una fecha en formato formal/legal: "Treinta (30) de abril del dos mil veintiséis (2026)"
     */
    private String formatearFechaLargaLegal(java.util.Date fecha) {
        if (fecha == null) return "";
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(fecha);
        int dia = cal.get(java.util.Calendar.DAY_OF_MONTH);
        int anio = cal.get(java.util.Calendar.YEAR);
        String mesText = new SimpleDateFormat("MMMM", new Locale("es", "CO")).format(fecha);
        String diaLetras = convertirNumeroALetras(dia).toLowerCase();
        
        return diaLetras + " (" + dia + ") de " + mesText + " de " + anio;
    }
}
