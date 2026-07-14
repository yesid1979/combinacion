package com.combinacion.util;

import com.combinacion.models.InformeSupervision;
import com.combinacion.models.Contrato;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import java.util.Locale;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class SupervisionReportGenerator {
    
    private static String formatearFechaEspecial(java.util.Date fecha) {
        if (fecha == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy", new Locale("es", "CO"));
        String f = sdf.format(fecha);
        String[] parts = f.split("/");
        if (parts.length == 3) {
            String month = parts[1];
            if (month.length() > 0) {
                // Remove the dot that some locales add to the abbreviation (e.g. "ene." -> "Ene")
                month = month.replace(".", "");
                month = month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase();
            }
            return parts[0] + "/" + month + "/" + parts[2];
        }
        return f;
    }

    private static String formatearFechaLarga(java.util.Date fecha) {
        if (fecha == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        return sdf.format(fecha);
    }
    
    private static String formatearPeriodo(String periodo) {
        if (periodo == null || periodo.trim().isEmpty()) return "";
        try {
            String[] parts = periodo.split("-");
            if (parts.length == 2) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.YEAR, year);
                cal.set(java.util.Calendar.MONTH, month - 1);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("es", "CO"));
                String formatted = sdf.format(cal.getTime());
                return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
            }
        } catch (Exception e) {
            // Ignore parse errors and return original
        }
        return periodo;
    }

    private static final String TEMPLATE_PATH = "plantillas/INFORME_SUPERVISION_TEMPLATE.docx";
    private static final String OUTPUT_DIR = "generados/informes";

    public static String generarDocx(InformeSupervision info, Contrato contrato) throws IOException {
        return generarDocx(info, contrato, null);
    }

    public static String generarDocx(InformeSupervision info, Contrato contrato, String realPath) throws IOException {
        File templateFile = null;
        if (realPath != null) {
            templateFile = new File(realPath, TEMPLATE_PATH);
        }
        
        if (templateFile == null || !templateFile.exists()) {
            templateFile = new File(TEMPLATE_PATH);
        }

        if (!templateFile.exists()) {
            // Fallback for different environments
            templateFile = new File("c:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\" + TEMPLATE_PATH);
        }

        if (!templateFile.exists()) {
            throw new IOException("Plantilla no encontrada en: " + (realPath != null ? new File(realPath, TEMPLATE_PATH).getPath() : TEMPLATE_PATH));
        }

        File outputDir = null;
        if (realPath != null) {
            outputDir = new File(realPath, OUTPUT_DIR);
        } else {
            outputDir = new File(OUTPUT_DIR);
        }
        if (!outputDir.exists()) outputDir.mkdirs();

        String contratista = contrato.getContratistaNombre() != null ? contrato.getContratistaNombre().toUpperCase() : "CONTRATISTA";
        String outputFileName = "3. INFORME SUPERVISION No. " + info.getNumeroCuota() + " -" + contratista + ".docx";
        File outputFile = new File(outputDir, outputFileName);

        Map<String, String> reps = new HashMap<>();

        // Datos del Contrato
        reps.put("${NUMERO_CONTRATO}", contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : "");
        reps.put("${CONTRATISTA_NOMBRE}", contrato.getContratistaNombre() != null ? contrato.getContratistaNombre() : "");
        String cedula = contrato.getContratista() != null && contrato.getContratista().getCedula() != null ? contrato.getContratista().getCedula() : "";
        reps.put("${CONTRATISTA_CEDULA}", cedula.replace(",", "."));
        reps.put("${CONTRATISTA_DV}", contrato.getContratista() != null && contrato.getContratista().getDv() != null ? contrato.getContratista().getDv() : "0");
        reps.put("${TELEFONO}", contrato.getContratista() != null && contrato.getContratista().getTelefono() != null ? contrato.getContratista().getTelefono() : "");
        reps.put("${NOMBRE_SUPERVISOR}", contrato.getSupervisor() != null && contrato.getSupervisor().getNombre() != null ? contrato.getSupervisor().getNombre() : "");
        reps.put("${ORGANISMO}", contrato.getOrdenadorGasto() != null && contrato.getOrdenadorGasto().getOrganismo() != null ? contrato.getOrdenadorGasto().getOrganismo() : "");
        reps.put("${OBJETO_CONTRACTUAL}", contrato.getObjeto() != null ? contrato.getObjeto() : "");
        reps.put("${TIPO_CONTRATO}", contrato.getTipoContrato() != null ? contrato.getTipoContrato().toUpperCase() : "");

        
        NumberFormat nf = NumberFormat.getInstance(new Locale("es", "CO"));
        if (nf instanceof DecimalFormat) {
            ((DecimalFormat) nf).applyPattern("#,##0");
        }
        String valorNumeros = contrato.getValorTotalNumeros() != null ? nf.format(contrato.getValorTotalNumeros()).replace(',', '.') : "0";
        reps.put("${VALOR_INICIAL_LETRAS}", contrato.getValorTotalLetras() != null ? contrato.getValorTotalLetras().toUpperCase() : "");
        reps.put("${VALOR_INICIAL_NUMEROS}", valorNumeros);

        // Datos del Informe
        reps.put("${PERIODO_INFORME}", info.getPeriodoInforme() != null ? info.getPeriodoInforme() : "");
        reps.put("${TIPO_INFORME}", info.getTipoInforme() != null ? info.getTipoInforme() : "");
        
        String cuotaOriginal = info.getNumeroCuota() != null ? info.getNumeroCuota().trim() : "";
        String cuotaPadded = cuotaOriginal;
        try {
            int c = Integer.parseInt(cuotaOriginal);
            cuotaPadded = String.format("%02d", c);
        } catch(Exception e) {}
        reps.put("${NUMERO_CUOTA}", cuotaPadded);
        
        reps.put("${X_PARCIAL}", info.getTipoInforme() != null && info.getTipoInforme().toUpperCase().contains("PARCIAL") ? "X" : "  ");
        reps.put("${X_FINAL}", info.getTipoInforme() != null && info.getTipoInforme().toUpperCase().contains("FINAL") ? "X" : "  ");

        reps.put("${FECHA_INICIO_PERIODO}", formatearFechaEspecial(info.getFechaInicioPeriodo()));
        reps.put("${FECHA_FIN_PERIODO}", formatearFechaEspecial(info.getFechaFinPeriodo()));
        
        reps.put("${MODIFICACIONES}", info.getModificaciones() != null && !info.getModificaciones().trim().isEmpty() ? info.getModificaciones() : "N/A");
        reps.put("${SUSPENSIONES}", info.getSuspensiones() != null && !info.getSuspensiones().trim().isEmpty() ? info.getSuspensiones() : "N/A");
        reps.put("${REANUDACIONES}", info.getReanudaciones() != null && !info.getReanudaciones().trim().isEmpty() ? info.getReanudaciones() : "N/A");
        reps.put("${CESIONES}", info.getCesiones() != null && !info.getCesiones().trim().isEmpty() ? info.getCesiones() : "N/A");
        reps.put("${TERMINACION_ANTICIPADA}", info.getTerminacionAnticipada() != null && !info.getTerminacionAnticipada().trim().isEmpty() ? info.getTerminacionAnticipada() : "N/A");
        reps.put("${ADICIONES}", info.getAdiciones() != null && !info.getAdiciones().trim().isEmpty() ? info.getAdiciones() : "N/A");
        reps.put("${PRORROGAS}", info.getProrrogas() != null && !info.getProrrogas().trim().isEmpty() ? info.getProrrogas() : "N/A");
        reps.put("${RECIBO_SATISFACCION}", info.getReciboSatisfaccion() != null && !info.getReciboSatisfaccion().trim().isEmpty() ? info.getReciboSatisfaccion() : "N/A");
        reps.put("${CONSTANCIA_PAZ_SALVO}", info.getConstanciaPazSalvo() != null && !info.getConstanciaPazSalvo().trim().isEmpty() ? info.getConstanciaPazSalvo() : "N/A");
        
        reps.put("${VALOR_CUOTA_PAGAR}", info.getValorCuotaPagar() != null ? "$ " + nf.format(info.getValorCuotaPagar()).replace(',', '.') : "$ 0");
        reps.put("${VALOR_ACUMULADO}", info.getValorAccumuladoPagado() != null ? "$ " + nf.format(info.getValorAccumuladoPagado()).replace(',', '.') : "$ 0");
        reps.put("${SALDO_CANCELAR}", info.getSaldoPorCancelar() != null ? "$ " + nf.format(info.getSaldoPorCancelar()).replace(',', '.') : "$ 0");
        
        reps.put("${PLANILLA_NUMERO}", info.getPlanillaNumero());
        reps.put("${PLANILLA_PIN}", info.getPlanillaPin());
        reps.put("${PLANILLA_OPERADOR}", info.getPlanillaOperador());
        reps.put("${PLANILLA_FECHA_PAGO}", formatearFechaLarga(info.getPlanillaFechaPago()));
        reps.put("${PLANILLA_PERIODO}", formatearPeriodo(info.getPlanillaPeriodo()));
        
        String concepto = info.getConceptoSupervisor();
        if (concepto == null || concepto.trim().isEmpty()) {
            concepto = contrato.getActividadesEntregables();
        }
        // No agregamos CONCEPTO_SUPERVISOR a reps para que TemplateGenerator no lo rompa.
        // Lo reemplazaremos directamente en el XML en el post-procesamiento.
        final String finalConceptoSup = concepto != null ? concepto : "";
        reps.put("${OBSERVACIONES_TECNICAS}", info.getObservacionesTecnicas() != null ? info.getObservacionesTecnicas() : "");
        reps.put("${RECOMENDACIONES}", info.getRecomendaciones() != null ? info.getRecomendaciones() : "");
        String fechaSuscripcionFormateada = info.getFechaSuscripcion() != null ? 
            "Santiago de Cali, " + formatearFechaLarga(info.getFechaSuscripcion()) : "";
        reps.put("${FECHA_SUSCRIPCION}", fechaSuscripcionFormateada);

        boolean tieneApoyo = contrato.getApoyoSupervision() != null && !contrato.getApoyoSupervision().trim().isEmpty();
        reps.put("${LINEA_APOYO}", tieneApoyo ? "______________________________________________________" : "{{REMOVE_PARAGRAPH}}");
        reps.put("${NOMBRE_APOYO}", tieneApoyo ? contrato.getApoyoSupervision().toUpperCase() : "{{REMOVE_PARAGRAPH}}");
        reps.put("${TEXTO_APOYO}", tieneApoyo ? "Nombre y firma del Apoyo a la Supervisión (Incluir cuando aplique)" : "{{REMOVE_PARAGRAPH}}");

        try (FileInputStream fis = new FileInputStream(templateFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            TemplateGenerator.generate(fis, reps, fos);
        }
        
        // Post-processing to replace textbox variables directly in XML
        File tempFile = new File(outputFile.getAbsolutePath() + ".tmp");
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(outputFile));
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempFile))) {
             
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                zos.putNextEntry(new ZipEntry(entry.getName()));
                if (entry.getName().equals("word/document.xml")) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[1024];
                    int count;
                    while ((count = zis.read(data, 0, 1024)) != -1) {
                        buffer.write(data, 0, count);
                    }
                    String xml = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
                    
                    String xParcial = reps.get("${X_PARCIAL}");
                    String xFinal = reps.get("${X_FINAL}");
                    
                    xml = xml.replace("${X_PARCIAL}", xParcial != null ? xParcial : "  ");
                    xml = xml.replace("${X_FINAL}", xFinal != null ? xFinal : "  ");
                    
                    if (finalConceptoSup != null) {
                        java.util.List<com.combinacion.util.ObligacionesParser.ObligacionActividad> lista = 
                            com.combinacion.util.ObligacionesParser.decodificarConcepto(finalConceptoSup, contrato.getActividadesEntregables());
                            
                        StringBuilder tablaXml = new StringBuilder();
                        tablaXml.append("</w:t></w:r></w:p>"); // Cerrar el párrafo actual de ${CONCEPTO_SUPERVISOR}
                        
                        tablaXml.append("<w:tbl>");
                        tablaXml.append("<w:tblPr>");
                        tablaXml.append("<w:tblBorders>");
                        tablaXml.append("<w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>");
                        tablaXml.append("<w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>");
                        tablaXml.append("<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>");
                        tablaXml.append("<w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>");
                        tablaXml.append("<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>");
                        tablaXml.append("<w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>");
                        tablaXml.append("</w:tblBorders>");
                        tablaXml.append("<w:tblW w:w=\"5000\" w:type=\"pct\"/>"); // 100% width
                        tablaXml.append("</w:tblPr>");
                        
                        // Header
                        tablaXml.append("<w:tr><w:tc><w:tcPr><w:tcW w:w=\"2000\" w:type=\"pct\"/><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"F2F2F2\"/></w:tcPr><w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:b/></w:rPr><w:t>OBLIGACIONES DEL CONTRATISTA</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:w=\"3000\" w:type=\"pct\"/><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"F2F2F2\"/></w:tcPr><w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:b/></w:rPr><w:t>ACTIVIDADES</w:t></w:r></w:p></w:tc></w:tr>");
                        
                        // Rows
                        for (com.combinacion.util.ObligacionesParser.ObligacionActividad item : lista) {
                            String obRaw = item.obligacion != null ? item.obligacion.trim() : "";
                            String acRaw = item.actividad != null ? item.actividad.trim() : "";
                            
                            if (!acRaw.isEmpty()) {
                                String[] lineas = acRaw.split("\n");
                                StringBuilder acModificado = new StringBuilder();
                                for (int i = 0; i < lineas.length; i++) {
                                    String linea = lineas[i].trim();
                                    if (!linea.isEmpty()) {
                                        java.util.regex.Matcher mLinea = java.util.regex.Pattern.compile("^(\\d+[.-]?|[\\-\\*\\•\\●\\○\\▪])\\s*").matcher(linea);
                                        if (!mLinea.find()) {
                                            linea = "● " + linea;
                                        }
                                    }
                                    acModificado.append(linea);
                                    if (i < lineas.length - 1) {
                                        acModificado.append("\n");
                                    }
                                }
                                acRaw = acModificado.toString().trim();
                            }

                            String ob = obRaw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                            String ac = acRaw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                            
                            // Reemplazar saltos de línea por <w:br/>
                            ob = ob.replace("\n", "</w:t><w:br/><w:t>");
                            ac = ac.replace("\n", "</w:t><w:br/><w:t>");
                            
                            tablaXml.append("<w:tr>");
                            
                            // Cell Obligacion
                            tablaXml.append("<w:tc><w:tcPr><w:tcW w:w=\"2000\" w:type=\"pct\"/></w:tcPr><w:p><w:pPr><w:jc w:val=\"both\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/></w:rPr><w:t>")
                                    .append(ob)
                                    .append("</w:t></w:r></w:p></w:tc>");
                                    
                            // Cell Actividad
                            tablaXml.append("<w:tc><w:tcPr><w:tcW w:w=\"3000\" w:type=\"pct\"/></w:tcPr><w:p><w:pPr><w:jc w:val=\"both\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/></w:rPr><w:t>")
                                    .append(ac)
                                    .append("</w:t></w:r></w:p></w:tc>");
                                    
                            tablaXml.append("</w:tr>");
                        }
                        
                        tablaXml.append("</w:tbl>");
                        
                        // Inyectar enlace de Drive si existe
                        String urlDrive = info.getUrlDriveEvidencias();
                        if (urlDrive != null && !urlDrive.trim().isEmpty()) {
                            urlDrive = urlDrive.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                            
                            // Párrafo de texto introductorio
                            tablaXml.append("<w:p><w:pPr><w:spacing w:before=\"240\" w:after=\"120\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/></w:rPr><w:t>En el siguiente link se encuentra las evidencias cuota ");
                            tablaXml.append(info.getNumeroCuota() != null ? info.getNumeroCuota() : "");
                            tablaXml.append(":</w:t></w:r></w:p>");
                            
                            // Párrafo con la URL como un hipervínculo real de Word
                            tablaXml.append("<w:p><w:pPr><w:spacing w:after=\"240\"/></w:pPr>");
                            tablaXml.append("<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>");
                            tablaXml.append("<w:r><w:instrText xml:space=\"preserve\"> HYPERLINK \"").append(urlDrive).append("\" </w:instrText></w:r>");
                            tablaXml.append("<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>");
                            tablaXml.append("<w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:color w:val=\"0000FF\"/><w:u w:val=\"single\"/></w:rPr><w:t>");
                            tablaXml.append(urlDrive);
                            tablaXml.append("</w:t></w:r>");
                            tablaXml.append("<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>");
                            tablaXml.append("</w:p>");
                        }
                        
                        tablaXml.append("<w:p><w:r><w:t>"); // Reabrir para balancear el tag original
                        
                        xml = xml.replace("${CONCEPTO_SUPERVISOR}", tablaXml.toString());
                    }
                    
                    byte[] newXmlData = xml.getBytes(StandardCharsets.UTF_8);
                    zos.write(newXmlData, 0, newXmlData.length);
                } else {
                    byte[] data = new byte[1024];
                    int count;
                    while ((count = zis.read(data, 0, 1024)) != -1) {
                        zos.write(data, 0, count);
                    }
                }
                zos.closeEntry();
                entry = zis.getNextEntry();
            }
        }
        
        if (outputFile.delete()) {
            tempFile.renameTo(outputFile);
        }

        return outputFile.getAbsolutePath();
    }
}
