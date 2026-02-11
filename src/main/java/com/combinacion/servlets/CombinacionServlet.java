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

        if ("generate".equals(action)) {
            generarDocumentoIndividual(request, response);
        } else if ("downloadZip".equals(action)) {
            generarZipMasivo(request, response);
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

            // Check Inversion
            boolean esInversion = (presupuesto != null && presupuesto.getInversion() != null
                    && !presupuesto.getInversion().trim().isEmpty());

            if (supervisorBytes == null && estructuradoresBytes == null && !esInversion) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "No se pudo generar ningún documento (datos faltantes o contratos no encontrados)");
                return;
            }

            // Create ZIP
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos);

            String folderName = normalizeFileName(
                    c.getNombre() != null ? c.getNombre() : "Contratista_" + contratistaId);

            // Add Standard Docs
            if (supervisorBytes != null) {
                addToZip(zos, folderName, "Designacion_Supervisor_" + cedula + ".docx", supervisorBytes);
            }
            if (estructuradoresBytes != null) {
                addToZip(zos, folderName, "Designacion_Estructuradores_" + cedula + ".docx", estructuradoresBytes);
            }

            // Add Inversion Docs
            if (esInversion && contrato != null) {
                Map<String, String> replacements = getFullReplacements(contratistaId);
                if (replacements != null) {
                    String[] invTemplates = {
                            "INVERSION_1_ESTUDIOS_PREVIOS.docx",
                            "INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx",
                            "INVERSION_3_CERTIFICADO_IDONEIDAD.docx",
                            "INVERSION_4_COMPLEMENTO_CONTRATO.docx"
                    };

                    for (String tpl : invTemplates) {
                        byte[] fileBytes = generateBytes(tpl, replacements);
                        if (fileBytes != null) {
                            addToZip(zos, folderName,
                                    tpl.replace("INVERSION_", "").replace(".docx", "_" + cedula + (".docx")),
                                    fileBytes);
                        }
                    }
                }
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
                    String folderName = normalizeFileName(c.getNombre() != null ? c.getNombre() : "Contratista_" + id);

                    // Check Inversion
                    Contrato contrato = contratoDAO.obtenerPorContratistaId(id);
                    PresupuestoDetalle presupuesto = null;
                    if (contrato != null && contrato.getPresupuestoId() > 0) {
                        presupuesto = presupuestoDAO.obtenerPorId(contrato.getPresupuestoId());
                    }
                    boolean esInversion = (presupuesto != null && presupuesto.getInversion() != null
                            && !presupuesto.getInversion().trim().isEmpty());

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
                        Map<String, String> replacements = getFullReplacements(id);
                        if (replacements != null) {
                            String[] invTemplates = {
                                    "INVERSION_1_ESTUDIOS_PREVIOS.docx",
                                    "INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx",
                                    "INVERSION_3_CERTIFICADO_IDONEIDAD.docx",
                                    "INVERSION_4_COMPLEMENTO_CONTRATO.docx"
                            };
                            for (String tpl : invTemplates) {
                                byte[] fileBytes = generateBytes(tpl, replacements);
                                if (fileBytes != null) {
                                    addToZip(zos, folderName,
                                            tpl.replace("INVERSION_", "").replace(".docx", "_" + cedula + (".docx")),
                                            fileBytes);
                                }
                            }
                        }
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

    private Map<String, String> getFullReplacements(int contratistaId) throws Exception {
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

        return getCommonReplacements(contratista, contrato, presupuesto, supervisor, ordenador, estructurador);
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
            OrdenadorGasto ordenador, com.combinacion.models.Estructurador estructurador) {
        Map<String, String> replacements = new HashMap<>();

        // ===== PLACEHOLDERS ANTIGUOS (formato ${}) para compatibilidad =====
        replacements.put("${NOMBRE_CONTRATISTA}",
                contratista.getNombre() != null ? contratista.getNombre().toUpperCase() : "");
        replacements.put("${CEDULA}", contratista.getCedula() != null ? contratista.getCedula() : "");
        replacements.put("${NUMERO_CONTRATO}",
                contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : "");
        replacements.put("${OBJETO_CONTRACTUAL}", contrato.getObjeto() != null ? contrato.getObjeto() : "");
        replacements.put("${NUMERO_PROCESO}", contrato.getTrdProceso() != null ? contrato.getTrdProceso() : "");
        replacements.put("${NUMERO_PROCESO_O_CONTRATO_ALT}",
                contrato.getTrdProceso() != null ? contrato.getTrdProceso()
                        : (contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : ""));

        // ===== NUEVOS PLACEHOLDERS (formato {{}}) para plantillas de inversión =====

        // Información del Proceso
        replacements.put("{{NUMERO_PROCESO}}", contrato.getTrdProceso() != null ? contrato.getTrdProceso() : "");

        // Información del Proyecto (desde presupuesto o contrato)
        if (presupuesto != null) {
            replacements.put("{{CODIGO_PROYECTO}}",
                    presupuesto.getInversion() != null ? presupuesto.getInversion() : "");
            replacements.put("{{BPIN}}", presupuesto.getBpin() != null ? presupuesto.getBpin() : "");
        } else {
            replacements.put("{{CODIGO_PROYECTO}}", "");
            replacements.put("{{BPIN}}", "");
        }
        replacements.put("{{NOMBRE_PROYECTO}}", contrato.getObjeto() != null ? contrato.getObjeto() : "");

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
                    presupuesto.getCdpValor() != null ? "$ " + presupuesto.getCdpValor().toString() : "");
            replacements.put("{{COMPROMISO_CDP}}",
                    presupuesto.getCompromiso() != null ? presupuesto.getCompromiso() : "");

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
            replacements.put("{{VALOR_CONTRATO}}", "$" + contrato.getValorTotalNumeros().toString());
        } else {
            replacements.put("{{VALOR_CONTRATO}}", "");
        }

        if (contrato.getValorCuotaLetras() != null) {
            replacements.put("{{VALOR_CUOTA_LETRAS}}", contrato.getValorCuotaLetras());
        } else {
            replacements.put("{{VALOR_CUOTA_LETRAS}}", "");
        }

        // Número de cuotas (usando plazo meses como aproximación si aplica)
        replacements.put("{{NUMERO_CUOTAS}}",
                contrato.getPlazoMeses() > 0 ? String.valueOf(contrato.getPlazoMeses()) : "PENDIENTE");
        replacements.put("{{VALOR_CUOTA_LETRAS}}", "PENDIENTE CALCULO"); // Requiere lógica compleja de números a letras
        replacements.put("{{VALOR_CUOTA}}", "PENDIENTE CALCULO");

        // Fecha fin contrato
        SimpleDateFormat dateFormat = new SimpleDateFormat("d 'de' MMMM 'del' yyyy", new Locale("es", "CO"));
        if (contrato.getFechaTerminacion() != null) {
            replacements.put("{{FECHA_FIN_CONTRATO}}",
                    dateFormat.format(contrato.getFechaTerminacion()));
        } else {
            replacements.put("{{FECHA_FIN_CONTRATO}}", "FECHA PENDIENTE");
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
            replacements.put("${VALOR_TOTAL}", contrato.getValorTotalNumeros().toString());
        else
            replacements.put("${VALOR_TOTAL}", "0");

        if (contrato.getValorCuotaNumero() != null)
            replacements.put("${VALOR_MENSUAL}", contrato.getValorCuotaNumero().toString());
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

        // 2. Formación y Título (Contratista)
        String formacion = contratista.getFormacionTitulo() != null ? contratista.getFormacionTitulo() : "";
        String descFormacion = contratista.getDescripcionFormacion() != null ? contratista.getDescripcionFormacion()
                : "";
        // Combina si ambos existen, o usa el que exista
        String perfilCompleto = "";
        if (!formacion.isEmpty() && !descFormacion.isEmpty()) {
            perfilCompleto = formacion + " - " + descFormacion;
        } else {
            perfilCompleto = formacion + descFormacion;
        }
        replacements.put("{{PERFIL_FORMACION}}", perfilCompleto);

        // 2.1 Experiencia (Contratista) - Nuevo para INVERSION_2
        String exp = contratista.getExperiencia() != null ? contratista.getExperiencia() : "";
        String descExp = contratista.getDescripcionExperiencia() != null ? contratista.getDescripcionExperiencia() : "";
        String expCompleta = "";
        if (!exp.isEmpty() && !descExp.isEmpty()) {
            expCompleta = exp + " - " + descExp;
        } else {
            expCompleta = exp + descExp;
        }
        replacements.put("{{PERFIL_EXPERIENCIA}}", expCompleta);

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
            String actEbi = presupuesto.getFichaEbiActividades() != null ? presupuesto.getFichaEbiActividades() : "";
            actEbi = actEbi.replace("{{", "").replace("}}", "").trim();
            replacements.put("{{ACTIVIDADES_FICHA_EBI}}", actEbi);
        } else {
            replacements.put("{{ACTIVIDADES_FICHA_EBI}}", "");
        }

        // 6. Valores numéricos específicos
        if (contrato.getValorCuotaNumero() != null) {
            replacements.put("{{VALOR_CUOTA_NUMERO}}", "$" + contrato.getValorCuotaNumero().toString());
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
            replacements.put("{{CARGO_ORDENADOR_GASTO}}",
                    ordenador.getCargoOrdenador() != null ? ordenador.getCargoOrdenador() : "");
        } else {
            replacements.put("{{NOMBRE_ORDENADOR_GASTO}}", "SIN DESIGNAR");
            replacements.put("{{CARGO_ORDENADOR_GASTO}}", "");
        }

        // RPC
        if (presupuesto != null) {
            replacements.put("${RPC_NUMERO}", presupuesto.getRpNumero() != null ? presupuesto.getRpNumero() : "");
        }

        // Standard Date Logic
        SimpleDateFormat sdfDoc = new SimpleDateFormat("MMMM d 'de' yyyy", new Locale("es", "CO"));
        java.util.Date fechaBase = (presupuesto != null && presupuesto.getRpFecha() != null) ? presupuesto.getRpFecha()
                : new java.util.Date();
        String fechaStr = sdfDoc.format(fechaBase);
        replacements.put("${FECHA_DOCUMENTO}", fechaStr);
        replacements.put("${FECHA_RPC_SUPERVISOR}", fechaStr);
        replacements.put("${FECHA_RPC_APOYO}", fechaStr);
        replacements.put("${RPC_FECHA}", fechaStr);

        SimpleDateFormat yearOnly = new SimpleDateFormat("yyyy", new Locale("es", "CO"));
        String fullDateLetters = sdfDoc.format(fechaBase) + " (" + yearOnly.format(fechaBase) + ")";
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
        Map<String, String> replacements = getFullReplacements(contratistaId);
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
}
