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
import com.combinacion.util.TemplateGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
        } else {
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
            Contrato contrato = contratoDAO.obtenerPorContratistaId(contratistaId);
            PresupuestoDetalle presupuesto = null;
            if (contrato != null && contrato.getPresupuestoId() > 0) {
                presupuesto = presupuestoDAO.obtenerPorId(contrato.getPresupuestoId());
            }

            // Generate Standard Docs
            byte[] supervisorBytes = generarBytesDocumento(contratistaId, "supervisor");
            byte[] estructuradoresBytes = generarBytesDocumento(contratistaId, "estructuradores");

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
            java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos);

            // Nombre de carpeta: 4121-014-NombreContratista
            // Se extrae solo el consecutivo del numero de contrato (ej: 4121.010.26.1.014 -> 014)
            String consecutivo = extraerConsecutivo(contrato != null ? contrato.getNumeroContrato() : null);
            String nombreFolder = c.getNombre() != null ? c.getNombre() : "Contratista_" + contratistaId;
            String folderName = normalizeFileName("4121-" + consecutivo + "-" + nombreFolder);

            // Add Standard Docs
            if (supervisorBytes != null) {
                addToZip(zos, folderName, "Designacion_Supervisor_" + cedula + ".docx", supervisorBytes);
            }
            if (estructuradoresBytes != null) {
                addToZip(zos, folderName, "Designacion_Estructuradores_" + cedula + ".docx", estructuradoresBytes);
            }

            // Add Inversion Docs
            if (esInversion && contrato != null) {
                // Para documentos de inversion, el 3 (Idoneidad) usa fecha_idoneidad
                for (String tpl : new String[]{"INVERSION_1_ESTUDIOS_PREVIOS.docx", "INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx", "INVERSION_3_CERTIFICADO_IDONEIDAD.docx", "INVERSION_4_COMPLEMENTO_CONTRATO.docx"}) {
                    String docContext = tpl.contains("IDONEIDAD") ? "idoneidad" : "inversion";
                    Map<String, String> replacements = getFullReplacements(contratistaId, docContext);
                    if (replacements != null) {
                        byte[] fileBytes = generateBytes(tpl, replacements);
                        if (fileBytes != null) {
                            addToZip(zos, folderName,
                                    tpl.replace("INVERSION_", "").replace(".docx", "_" + cedula + (".docx")),
                                    fileBytes);
                        }
                    }
                }
            }

            // Add Funcionamiento Alert
            if (!esInversion && esFuncionamiento && contrato != null) {
                // Crear archivo de texto con alerta
                String alertaTexto = "═══════════════════════════════════════════════════════════════\n" +
                        "           DOCUMENTOS DE FUNCIONAMIENTO NO DISPONIBLES\n" +
                        "═══════════════════════════════════════════════════════════════\n\n" +
                        "Estimado usuario,\n\n" +
                        "Los documentos para contratos de FUNCIONAMIENTO aún no están disponibles\n" +
                        "en el sistema.\n\n" +
                        "Información del contrato:\n" +
                        "  • Contratista: " + c.getNombre() + "\n" +
                        "  • Cédula: " + cedula + "\n" +
                        "  • Tipo: FUNCIONAMIENTO\n\n" +
                        "Por favor, contacte al administrador del sistema para más información.\n\n" +
                        "Fecha de generación: "
                        + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()) + "\n" +
                        "═══════════════════════════════════════════════════════════════\n";

                addToZip(zos, folderName, "ALERTA_FUNCIONAMIENTO_NO_DISPONIBLE.txt",
                        alertaTexto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            zos.close();

            byte[] zipBytes = baos.toByteArray();
            String zipFilename = "Documentos_" + cedula + ".zip";

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
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos);

        try {
            for (String idStr : ids) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    Contratista c = contratistaDAO.obtenerPorId(id);
                    if (c == null)
                        continue;

                    String cedula = (c.getCedula() != null ? c.getCedula() : "Doc");

                    // Determinar tipo de contrato: Inversión o Funcionamiento
                    Contrato contrato = contratoDAO.obtenerPorContratistaId(id);

                    // Nombre de carpeta: 4121-014-NombreContratista
                    // Se extrae solo el consecutivo del numero de contrato (ej: 4121.010.26.1.014 -> 014)
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

                        // Verificar si es Inversión (columna inversion = "Si" o "Sí")
                        esInversion = (inversionFlag != null &&
                                (inversionFlag.trim().equalsIgnoreCase("Si") ||
                                        inversionFlag.trim().equalsIgnoreCase("Sí")));

                        // Verificar si es Funcionamiento (columna funcionamiento = "Si" o "Sí")
                        esFuncionamiento = (funcionamientoFlag != null &&
                                (funcionamientoFlag.trim().equalsIgnoreCase("Si") ||
                                        funcionamientoFlag.trim().equalsIgnoreCase("Sí")));
                    }

                    // Standard Docs
                    byte[] supervisorBytes = generarBytesDocumento(id, "supervisor");
                    if (supervisorBytes != null) {
                        addToZip(zos, folderName, "Designacion_Supervisor_" + cedula + ".docx", supervisorBytes);
                    }

                    byte[] estructuradoresBytes = generarBytesDocumento(id, "estructuradores");
                    if (estructuradoresBytes != null) {
                        addToZip(zos, folderName, "Designacion_Estructuradores_" + cedula + ".docx",
                                estructuradoresBytes);
                    }

                    // Inversion Docs
                    if (esInversion) {
                        for (String tpl : new String[]{"INVERSION_1_ESTUDIOS_PREVIOS.docx", "INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx", "INVERSION_3_CERTIFICADO_IDONEIDAD.docx", "INVERSION_4_COMPLEMENTO_CONTRATO.docx"}) {
                            String docContext = tpl.contains("IDONEIDAD") ? "idoneidad" : "inversion";
                            Map<String, String> replacements = getFullReplacements(id, docContext);
                            if (replacements != null) {
                                byte[] fileBytes = generateBytes(tpl, replacements);
                                if (fileBytes != null) {
                                    addToZip(zos, folderName,
                                            tpl.replace("INVERSION_", "").replace(".docx", "_" + cedula + (".docx")),
                                            fileBytes);
                                }
                            }
                        }
                    }

                    // Funcionamiento Alert
                    if (!esInversion && esFuncionamiento && contrato != null) {
                        String alertaTexto = "═══════════════════════════════════════════════════════════════\n" +
                                "           DOCUMENTOS DE FUNCIONAMIENTO NO DISPONIBLES\n" +
                                "═══════════════════════════════════════════════════════════════\n\n" +
                                "Estimado usuario,\n\n" +
                                "Los documentos para contratos de FUNCIONAMIENTO aún no están disponibles\n" +
                                "en el sistema.\n\n" +
                                "Información del contrato:\n" +
                                "  • Contratista: " + c.getNombre() + "\n" +
                                "  • Cédula: " + cedula + "\n" +
                                "  • Tipo: FUNCIONAMIENTO\n\n" +
                                "Por favor, contacte al administrador del sistema para más información.\n\n" +
                                "Fecha de generación: "
                                + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date())
                                + "\n" +
                                "═══════════════════════════════════════════════════════════════\n";

                        addToZip(zos, folderName, "ALERTA_FUNCIONAMIENTO_NO_DISPONIBLE.txt",
                                alertaTexto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
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

    // Inject DAO
    private com.combinacion.dao.EstructuradorDAO estructuradorDAO = new com.combinacion.dao.EstructuradorDAO();

    private Map<String, String> getFullReplacements(int contratistaId, String docType) throws Exception {
        Contratista contratista = contratistaDAO.obtenerPorId(contratistaId);
        if (contratista == null)
            return null;
        Contrato contrato = contratoDAO.obtenerPorContratistaId(contratistaId);
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

        return getCommonReplacements(contratista, contrato, presupuesto, supervisor, ordenador, estructurador, docType);
    }

    private byte[] generateBytes(String templateName, Map<String, String> replacements) throws IOException {
        File templateFile = new File("plantillas/" + templateName);
        if (!templateFile.exists()) {
            templateFile = new File(
                    "c:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\plantillas\\"
                            + templateName);
        }
        if (!templateFile.exists())
            return null;

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (FileInputStream fis = new FileInputStream(templateFile)) {
            TemplateGenerator.generate(fis, replacements, baos);
        }
        return baos.toByteArray();
    }

    private Map<String, String> getCommonReplacements(Contratista contratista, Contrato contrato,
            PresupuestoDetalle presupuesto, Supervisor supervisor,
            OrdenadorGasto ordenador, com.combinacion.models.Estructurador estructurador, String docType) {
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
            replacements.put("{{VALOR_CDP}}",
                    presupuesto.getCdpValor() != null ? formatearMoneda(presupuesto.getCdpValor()) : "");
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
        replacements.put("{{ADICION_SI_NO}}", contrato.getAdicionSiNo() != null ? contrato.getAdicionSiNo() : "");
        replacements.put("{{NUMERO_CUOTAS_ADICION}}", contrato.getNumeroCuotasAdicion() > 0 ? String.valueOf(contrato.getNumeroCuotasAdicion()) : "");
        if (contrato.getNumeroCuotasAdicion() > 0) {
            replacements.put("{{NUMERO_CUOTAS_ADICION_LETRAS}}", convertirNumeroALetras(contrato.getNumeroCuotasAdicion()));
        } else {
            replacements.put("{{NUMERO_CUOTAS_ADICION_LETRAS}}", "");
        }

        replacements.put("{{NUMERO_CUOTAS_NUMERO}}", contrato.getNumCuotasNumero() > 0 ? String.valueOf(contrato.getNumCuotasNumero()) : "");
        replacements.put("{{NUMERO_CUOTAS_LETRAS}}", contrato.getNumCuotasLetras() != null ? contrato.getNumCuotasLetras() : "");

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
        
        // Poner N/A en campos sin datos en el sistema
        replacements.put("{{PRORROGAS}}", "N/A");
        replacements.put("{{ACLARACION}}", "N/A");
        replacements.put("{{SUSPENSION}}", "N/A");
        replacements.put("{{REANUDACION}}", "N/A");
        // Cesión: N/A fijo directo en la plantilla Word (no requiere variable)
        
        // Ciudad y Fecha de generación actual
        SimpleDateFormat formatHoy = new SimpleDateFormat("'Santiago de Cali,' d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        java.util.Date fechaActual = new java.util.Date();
        replacements.put("{{CIUDAD_Y_FECHA_HOY}}", formatHoy.format(fechaActual));
        
        java.util.Calendar calHoy = java.util.Calendar.getInstance();
        calHoy.setTime(fechaActual);
        int diaHoy = calHoy.get(java.util.Calendar.DAY_OF_MONTH);
        int anioHoy = calHoy.get(java.util.Calendar.YEAR);
        String mesHoyStr = new SimpleDateFormat("MMMM", new Locale("es", "CO")).format(fechaActual);
        String constanciaFecha = "a los " + convertirNumeroALetras(diaHoy).toLowerCase() + " (" + diaHoy + ") días del mes de " + mesHoyStr + " del año " + anioHoy;
        replacements.put("{{FECHA_CONSTANCIA_HOY}}", constanciaFecha);
        
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("d 'de' MMMM 'del' yyyy", new Locale("es", "CO"));
        if (contrato.getFechaTerminacion() != null) {
            // Formatear como "Treinta (30) de junio del dos mil veintiseis (2026)"
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(contrato.getFechaTerminacion());
            int dia = cal.get(java.util.Calendar.DAY_OF_MONTH);
            int anio = cal.get(java.util.Calendar.YEAR);
            String mesText = new SimpleDateFormat("MMMM", new Locale("es", "CO")).format(contrato.getFechaTerminacion());
            String diaLetras = convertirNumeroALetras(dia);
            
            // Convertir años conocidos a letras o hardcodearlos dinamicamente si es 2024, 2025, 2026, 2027
            String anioLetras;
            if (anio == 2024) anioLetras = "dos mil veinticuatro";
            else if (anio == 2025) anioLetras = "dos mil veinticinco";
            else if (anio == 2026) anioLetras = "dos mil veintiséis";
            else if (anio == 2027) anioLetras = "dos mil veintisiete";
            else if (anio == 2028) anioLetras = "dos mil veintiocho";
            else anioLetras = "dos mil veintialgo"; // simple fallback
            
            // "treinta (30) de junio del dos mil veintiséis (2026)"
            // Capitalizar primera letra del dia manual
            diaLetras = diaLetras.substring(0, 1).toUpperCase() + diaLetras.substring(1);
            
            String fechaFinEspecial = diaLetras + " (" + dia + ") de " + mesText + " del " + anioLetras + " (" + anio + ")";
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
        
        // Formato para el documento (Tipo frase: Prestación de servicios...)
        String tipoC_Frase = tipoC;
        if (!tipoC_Frase.isEmpty()) {
            tipoC_Frase = tipoC_Frase.toLowerCase();
            tipoC_Frase = Character.toUpperCase(tipoC_Frase.charAt(0)) + tipoC_Frase.substring(1);
        }
        replacements.put("{{TIPO_CONTRATO}}", tipoC_Frase);

        
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
            // Solo usar como MES_ANIO_ACTUAL en el contexto de idoneidad o si no hay RP
            if ("idoneidad".equals(docType) || (presupuesto == null || presupuesto.getRpFecha() == null)) {
                replacements.put("{{MES_ANIO_ACTUAL}}", mesAnioIdoneidad);
            } else {
                replacements.put("{{MES_ANIO_ACTUAL}}", sdfMesAnio.format(fechaBase));
            }
        } else {
            replacements.put("{{FECHA_IDONEIDAD}}", "");
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
        replacements.put("${FECHA_DOCUMENTO}", fechaStr);
        replacements.put("${FECHA_RPC_SUPERVISOR}", fechaStr);
        replacements.put("${FECHA_RPC_APOYO}", fechaStr);
        replacements.put("${RPC_FECHA}", fechaStr);

        SimpleDateFormat yearOnly = new SimpleDateFormat("yyyy", new Locale("es", "CO"));

        // Formato especial para FECHA_DOCUMENTO_LETRAS: "seis (06) días del mes de
        // enero de (2026)"
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(fechaBase);
        int dia = cal.get(java.util.Calendar.DAY_OF_MONTH);
        String mes = new SimpleDateFormat("MMMM", new Locale("es", "CO")).format(fechaBase);
        String anio = yearOnly.format(fechaBase);

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

        return replacements;
    }

    private byte[] generarBytesDocumento(int contratistaId, String docType) throws Exception {
        Map<String, String> replacements = getFullReplacements(contratistaId, docType);
        if (replacements == null)
            return null;

        Contratista contratista = contratistaDAO.obtenerPorId(contratistaId);
        Contrato contrato = contratoDAO.obtenerPorContratistaId(contratistaId);

        // Template Logic (Old logic mostly preserved in purpose, delegated to
        // getCommonReplacements)
        String templateName;
        if ("estructuradores".equals(docType)) {
            templateName = "DESIGNACION_RESPONSABLES_PARA_ESTRUCTURAR.docx";
        } else {
            boolean conApoyo = contrato.getApoyoSupervision() != null
                    && !contrato.getApoyoSupervision().trim().isEmpty();
            templateName = conApoyo ? "DESIGNACION_SUPERVISOR_CON APOYO.docx" : "DESIGNACION_SUPERVISOR_SIN_APOYO.docx";
        }

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

        DecimalFormat formatter = new DecimalFormat("$ #,##0", symbols);
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

            Contrato contrato = contratoDAO.obtenerPorContratistaId(contratistaId);

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos);

            String consecutivo = extraerConsecutivo(contrato != null ? contrato.getNumeroContrato() : null);
            String nombreFolder = c.getNombre() != null ? c.getNombre() : "Contratista_" + contratistaId;
            String folderName = normalizeFileName("4121-" + consecutivo + "-" + nombreFolder);

            String adicionFlag = contrato != null ? contrato.getAdicionSiNo() : null;
            boolean esAdicion = false;
            if (adicionFlag != null) {
                String adNorm = java.text.Normalizer.normalize(adicionFlag.trim().toLowerCase(), java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
                esAdicion = adNorm.equals("si") || adNorm.equals("x");
            }

            if (esAdicion) {
                // Generar Documentos de Modificacion
                for (String tpl : new String[]{"MODIFICACION_1_JUSTIFICACION.docx", "MODIFICACION_2_ACEPTACION.docx"}) {
                    Map<String, String> replacements = getFullReplacements(contratistaId, "modificacion");
                    if (replacements != null) {
                        byte[] fileBytes = generateBytes(tpl, replacements);
                        if (fileBytes != null) {
                            addToZip(zos, folderName, tpl.replace(".docx", "_" + cedula + ".docx"), fileBytes);
                        }
                    }
                }
            } else {
                String alertaTexto = "El contrato para " + (c != null ? c.getNombre() : "el contratista seleccionado") + 
                                     " no tiene registrada una ADICIÓN en el sistema.\n" +
                                     "Para generar estos documentos, el campo 'Adición' debe ser 'Sí' o 'X'.";
                addToZip(zos, folderName, "ALERTA_SIN_ADICION_" + cedula + ".txt", alertaTexto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            zos.close();

            byte[] zipBytes = baos.toByteArray();
            String zipFilename = "Modificaciones_" + cedula + ".zip";

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
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos);

        try {
            for (String idStr : ids) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    Contratista c = contratistaDAO.obtenerPorId(id);
                    if (c == null) continue;

                    String cedula = (c.getCedula() != null ? c.getCedula() : "Doc");
                    Contrato contrato = contratoDAO.obtenerPorContratistaId(id);

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
                        // Generar Documentos de Modificacion
                        for (String tpl : new String[]{"MODIFICACION_1_JUSTIFICACION.docx", "MODIFICACION_2_ACEPTACION.docx"}) {
                            Map<String, String> replacements = getFullReplacements(id, "modificacion");
                            if (replacements != null) {
                                byte[] fileBytes = generateBytes(tpl, replacements);
                                if (fileBytes != null) {
                                    addToZip(zos, folderName, tpl.replace(".docx", "_" + cedula + ".docx"), fileBytes);
                                }
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
}
