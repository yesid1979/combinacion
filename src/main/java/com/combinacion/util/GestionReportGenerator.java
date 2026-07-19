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

public class GestionReportGenerator {
    
    private static String formatearFechaEspecial(java.util.Date fecha) {
        if (fecha == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy", new Locale("es", "CO"));
        String f = sdf.format(fecha);
        String[] parts = f.split("/");
        if (parts.length == 3) {
            String month = parts[1];
            if (month.length() > 0) {
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
        } catch (Exception e) {}
        return periodo;
    }

    private static final String TEMPLATE_PATH = "plantillas/INFORME_GESTION_TEMPLATE.docx";
    private static final String OUTPUT_DIR = "generados/informes";

    private static byte[] templateCacheBytes = null;

    public static String generarDocx(InformeSupervision info, Contrato contrato, String realPath) throws IOException {
        File templateFile = null;
        if (realPath != null) {
            templateFile = new File(realPath, TEMPLATE_PATH);
        }
        
        if (templateFile == null || !templateFile.exists()) {
            templateFile = new File(TEMPLATE_PATH);
        }
        if (!templateFile.exists()) {
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
        String outputFileName = "4. INFORME GESTION No. " + info.getNumeroCuota() + " -" + contratista + ".docx";
        File outputFile = new File(outputDir, outputFileName);

        Map<String, String> reps = new HashMap<>();

        // Datos del Contrato
        reps.put("${NUMERO_CONTRATO}", contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : "");
        reps.put("${CONTRATISTA_NOMBRE}", contrato.getContratistaNombre() != null ? contrato.getContratistaNombre() : "");
        String cedula = contrato.getContratista() != null && contrato.getContratista().getCedula() != null ? contrato.getContratista().getCedula() : "";
        reps.put("${CONTRATISTA_CEDULA}", cedula.replace(",", "."));
        
        // Firma del contratista
        String firmaUrl = new com.combinacion.dao.UsuarioDAO().obtenerFirmaPorCedula(cedula);
        if (firmaUrl != null) {
            reps.put("${FIRMA_CONTRATISTA}", "__IMG__:" + firmaUrl);
        } else {
            reps.put("${FIRMA_CONTRATISTA}", ""); // Si no hay firma, se deja en blanco
        }
        
        reps.put("${NOMBRE_SUPERVISOR}", contrato.getSupervisor() != null && contrato.getSupervisor().getNombre() != null ? contrato.getSupervisor().getNombre() : "");
        reps.put("${OBJETO_CONTRACTUAL}", contrato.getObjeto() != null ? contrato.getObjeto() : "");
        
        // Datos del Informe
        String cuotaNum = info.getNumeroCuota() != null ? info.getNumeroCuota().trim() : "";
        String cuotaLetras = cuotaNum;
        try {
            int c = Integer.parseInt(cuotaNum);
            String[] letras = {"Cero", "Uno", "Dos", "Tres", "Cuatro", "Cinco", "Seis", "Siete", "Ocho", "Nueve", "Diez", 
                               "Once", "Doce", "Trece", "Catorce", "Quince", "Dieciséis", "Diecisiete", "Dieciocho", "Diecinueve", "Veinte",
                               "Veintiuno", "Veintidós", "Veintitrés", "Veinticuatro"};
            if (c >= 0 && c < letras.length) {
                cuotaLetras = letras[c] + " (" + c + ")";
            }
        } catch (Exception e) {}
        
        reps.put("${NUMERO_CUOTA}", cuotaLetras);
        reps.put("${FECHA_INFORME}", formatearFechaLarga(info.getFechaFinPeriodo()));
        
        // Planilla
        reps.put("${PLANILLA_NUMERO}", info.getPlanillaNumero() != null ? info.getPlanillaNumero() : "");
        reps.put("${PLANILLA_PIN}", info.getPlanillaPin() != null ? info.getPlanillaPin() : "");
        reps.put("${PLANILLA_OPERADOR}", info.getPlanillaOperador() != null ? info.getPlanillaOperador() : "");
        reps.put("${PLANILLA_FECHA_PAGO}", formatearFechaLarga(info.getPlanillaFechaPago()));
        reps.put("${PLANILLA_PERIODO}", formatearPeriodo(info.getPlanillaPeriodo()));
        
        String concepto = info.getConceptoSupervisor();
        if (concepto == null || concepto.trim().isEmpty()) {
            concepto = contrato.getActividadesEntregables();
        }
        final String finalConceptoSup = concepto != null ? concepto : "";

        java.util.List<com.combinacion.util.ObligacionesParser.ObligacionActividad> lista = null;
        if (finalConceptoSup != null) {
            lista = com.combinacion.util.ObligacionesParser.decodificarConcepto(finalConceptoSup, contrato.getActividadesEntregables());
        }

        // 1. Cargar plantilla en cache de memoria
        if (templateCacheBytes == null) {
            templateCacheBytes = java.nio.file.Files.readAllBytes(templateFile.toPath());
        }

        ByteArrayOutputStream docxMemoryStream = new ByteArrayOutputStream();

        try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(templateCacheBytes);
             org.apache.poi.xwpf.usermodel.XWPFDocument doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(bais)) {
            
            TemplateGenerator.replacePlaceholders(doc, reps);
            
            if (lista != null) {
                for (com.combinacion.util.ObligacionesParser.ObligacionActividad item : lista) {
                    if (item.actividad != null && !item.actividad.isEmpty()) {
                        String ac = item.actividad;
                        com.combinacion.dao.VerboConjugacionDAO verboDao = new com.combinacion.dao.VerboConjugacionDAO();
                        java.util.List<com.combinacion.models.VerboConjugacion> verbos = verboDao.obtenerActivos();
                        if (verbos != null) {
                            for (com.combinacion.models.VerboConjugacion v : verbos) {
                                String t = v.getTerceraPersona();
                                String p = v.getPrimeraPersona();
                                
                                // Minúscula
                                String tMin = t.toLowerCase();
                                String pMin = p.toLowerCase();
                                ac = ac.replaceAll("\\b" + tMin + "\\b", pMin);
                                
                                // Capitalizada (Primera letra mayúscula)
                                String tCap = t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase();
                                String pCap = p.substring(0, 1).toUpperCase() + p.substring(1).toLowerCase();
                                ac = ac.replaceAll("\\b" + tCap + "\\b", pCap);
                            }
                        }

                        item.actividad = HtmlToWordXmlConverter.convertHtmlToXml(ac, doc);
                    }
                }
            }
            
            doc.write(docxMemoryStream);
        }
        
        // 2. Procesar ZIP en memoria
        ByteArrayOutputStream finalMemoryStream = new ByteArrayOutputStream();
        try (ZipInputStream zis = new ZipInputStream(new java.io.ByteArrayInputStream(docxMemoryStream.toByteArray()));
             ZipOutputStream zos = new ZipOutputStream(finalMemoryStream)) {
             
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
                    
                    if (finalConceptoSup != null && xml.contains("${ACTIVIDADES_GESTION}")) {
                        StringBuilder actividadesXml = new StringBuilder();
                        actividadesXml.append("</w:t></w:r></w:p>"); // Close current paragraph containing ${ACTIVIDADES_GESTION}
                        
                        if (lista != null) {
                            for (com.combinacion.util.ObligacionesParser.ObligacionActividad item : lista) {
                                String ob = item.obligacion != null ? item.obligacion.trim() : "";
                                String acXml = item.actividad != null ? item.actividad : "";
                                
                                // Paragraph for the obligation (Numbered, bold)
                                actividadesXml.append("<w:p><w:pPr><w:jc w:val=\"both\"/><w:spacing w:before=\"120\" w:after=\"120\"/><w:ind w:left=\"360\" w:hanging=\"360\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:b/></w:rPr><w:t>")
                                              .append(ob.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
                                              .append("</w:t></w:r></w:p>");
    
                                // Paragraphs for the activities (already Word XML generated by converter)
                                if (!acXml.isEmpty()) {
                                    actividadesXml.append(acXml);
                                }
                            }
                        }
                        
                        // Inject Drive Link at the end
                        String urlDrive = info.getUrlDriveEvidencias();
                        if (urlDrive != null && !urlDrive.trim().isEmpty()) {
                            urlDrive = urlDrive.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                            actividadesXml.append("<w:p><w:pPr><w:jc w:val=\"both\"/><w:spacing w:before=\"240\" w:after=\"120\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:b/></w:rPr><w:t>NOTA: Las evidencias detalladas y capturas de pantalla de estas actividades se encuentran anexas en el siguiente enlace de Google Drive:</w:t></w:r></w:p>");
                            actividadesXml.append("<w:p><w:pPr><w:jc w:val=\"both\"/><w:spacing w:after=\"240\"/></w:pPr>");
                            actividadesXml.append("<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>");
                            actividadesXml.append("<w:r><w:instrText xml:space=\"preserve\"> HYPERLINK \"").append(urlDrive).append("\" </w:instrText></w:r>");
                            actividadesXml.append("<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>");
                            actividadesXml.append("<w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:color w:val=\"0000FF\"/><w:u w:val=\"single\"/></w:rPr><w:t>");
                            actividadesXml.append(urlDrive);
                            actividadesXml.append("</w:t></w:r>");
                            actividadesXml.append("<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>");
                            actividadesXml.append("</w:p>");
                        }
                        
                        actividadesXml.append("<w:p><w:r><w:t>"); // Reopen paragraph tag to balance
                        
                        xml = xml.replace("${ACTIVIDADES_GESTION}", actividadesXml.toString());
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
        
        // 3. Escribir el resultado final al archivo fisico (necesario para el conversor PDF)
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(finalMemoryStream.toByteArray());
        }

        return outputFile.getAbsolutePath();
    }
}
