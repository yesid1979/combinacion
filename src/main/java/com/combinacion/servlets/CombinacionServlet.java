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
            byte[] docBytes = generarBytesDocumento(contratistaId);

            if (docBytes == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "No se pudo generar el documento (datos faltantes)");
                return;
            }

            Contratista c = contratistaDAO.obtenerPorId(contratistaId);

            // Create ZIP Structure for Individual Download
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos);

            String folderName = normalizeFileName(
                    c.getNombre() != null ? c.getNombre() : "Contratista_" + contratistaId);
            String entryPath = folderName + "/Designacion_" + (c.getCedula() != null ? c.getCedula() : "Doc") + ".docx";

            java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(entryPath);
            zos.putNextEntry(entry);
            zos.write(docBytes);
            zos.closeEntry();
            zos.close();

            byte[] zipBytes = baos.toByteArray();
            String zipFilename = "Documentos_" + (c.getCedula() != null ? c.getCedula() : "Doc") + ".zip";

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

    private void generarZipMasivo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idsParam = request.getParameter("ids"); // Expecting "1,2,3,4"
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

                    byte[] docBytes = generarBytesDocumento(id);
                    if (docBytes != null) {
                        // Folder structure: NOMBRE_CONTRATISTA/Designacion.docx
                        String folderName = normalizeFileName(
                                c.getNombre() != null ? c.getNombre() : "Contratista_" + id);
                        String entryPath = folderName + "/Designacion_" + c.getCedula() + ".docx";

                        java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(entryPath);
                        zos.putNextEntry(entry);
                        zos.write(docBytes);
                        zos.closeEntry();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(); // Log and continue with others
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

    private byte[] generarBytesDocumento(int contratistaId) throws Exception {
        // Fetch Data
        Contratista contratista = contratistaDAO.obtenerPorId(contratistaId);
        if (contratista == null)
            return null;

        Contrato contrato = contratoDAO.obtenerPorContratistaId(contratistaId);
        if (contrato == null)
            return null;

        PresupuestoDetalle presupuesto = null;
        if (contrato.getPresupuestoId() > 0) {
            presupuesto = presupuestoDAO.obtenerPorId(contrato.getPresupuestoId());
        }

        Supervisor supervisor = null;
        if (contrato.getSupervisorId() > 0) {
            supervisor = supervisorDAO.obtenerPorId(contrato.getSupervisorId());
        }

        OrdenadorGasto ordenador = null;
        if (contrato.getOrdenadorId() > 0) {
            ordenador = ordenadorDAO.obtenerPorId(contrato.getOrdenadorId());
        }

        // Prepare Replacements
        Map<String, String> replacements = new HashMap<>();
        replacements.put("${NOMBRE_CONTRATISTA}",
                contratista.getNombre() != null ? contratista.getNombre().toUpperCase() : "");
        replacements.put("${NUMERO_CONTRATO}",
                contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : "");
        // Handle potential newlines in Objeto by replacing them or leaving as is (POI
        // handles some basic text)
        replacements.put("${OBJETO_CONTRACTUAL}", contrato.getObjeto() != null ? contrato.getObjeto() : "");

        // Mapping TRD Proceso or similar as the "Proceso/Contrato Alt" link found in
        // the doc
        replacements.put("${NUMERO_PROCESO_O_CONTRATO_ALT}",
                contrato.getTrdProceso() != null ? contrato.getTrdProceso()
                        : (contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : ""));

        if (presupuesto != null) {
            replacements.put("${RPC_NUMERO}", presupuesto.getRpNumero() != null ? presupuesto.getRpNumero() : "");

            if (presupuesto.getRpFecha() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d 'de' yyyy", new Locale("es", "CO"));
                replacements.put("${RPC_FECHA}", sdf.format(presupuesto.getRpFecha()));
            } else {
                replacements.put("${RPC_FECHA}", "");
            }
        } else {
            replacements.put("${RPC_NUMERO}", "N/A");
            replacements.put("${RPC_FECHA}", "N/A");
        }

        // Supervisor
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

        // Fecha Documento (Same as RPC Date per user request)
        SimpleDateFormat sdfDoc = new SimpleDateFormat("MMMM d 'de' yyyy", new Locale("es", "CO"));
        if (presupuesto != null && presupuesto.getRpFecha() != null) {
            String fechaRpc = sdfDoc.format(presupuesto.getRpFecha());
            replacements.put("${FECHA_DOCUMENTO}", fechaRpc);
            replacements.put("${FECHA_RPC_SUPERVISOR}", fechaRpc); // Explicit for supervisor block
            replacements.put("${FECHA_RPC_APOYO}", fechaRpc); // Explicit for apoyo block
        } else {
            String fechaHoy = sdfDoc.format(new java.util.Date());
            replacements.put("${FECHA_DOCUMENTO}", fechaHoy);
            replacements.put("${FECHA_RPC_SUPERVISOR}", fechaHoy);
            replacements.put("${FECHA_RPC_APOYO}", fechaHoy);
        }

        // Load Template
        File templateFile = new File("plantillas/DESIGNACION_SUPERVISOR_CON APOYO.docx");
        if (!templateFile.exists()) {
            // Fallback
            templateFile = new File(
                    "c:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\plantillas\\DESIGNACION_SUPERVISOR_CON APOYO.docx");
        }

        if (!templateFile.exists()) {
            return null;
        }

        // Generate to ByteArray
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (FileInputStream fis = new FileInputStream(templateFile)) {
            TemplateGenerator.generate(fis, replacements, baos);
        }
        return baos.toByteArray();
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
