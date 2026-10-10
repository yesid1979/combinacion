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

    private static final String TEMPLATE_PATH_NORMAL = "plantillas/INFORME_GESTION_TEMPLATE.docx";
    private static final String TEMPLATE_PATH_BORRADOR = "plantillas/INFORME_GESTION_TEMPLATE_BORRADOR.docx";
    private static final String OUTPUT_DIR = "generados/informes";

    private static byte[] templateCacheBytes = null;

    public static String generarDocx(InformeSupervision info, Contrato contrato, String realPath) throws IOException {
        String currentTemplatePath = ("BORRADOR".equalsIgnoreCase(info.getEstadoRadicacion())) 
            ? TEMPLATE_PATH_BORRADOR 
            : TEMPLATE_PATH_NORMAL;

        File templateFile = null;
        if (realPath != null) {
            templateFile = new File(realPath, currentTemplatePath);
        }
        
        if (templateFile == null || !templateFile.exists()) {
            templateFile = new File(currentTemplatePath);
        }
        if (!templateFile.exists()) {
            templateFile = new File("c:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\" + currentTemplatePath);
        }
        if (!templateFile.exists()) {
            throw new IOException("Plantilla no encontrada en: " + (realPath != null ? new File(realPath, currentTemplatePath).getPath() : currentTemplatePath));
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
        String numContrato = contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : "";
        if (contrato.getAnio() != null && contrato.getAnio() > 0 && !numContrato.isEmpty()) {
            numContrato += " de " + contrato.getAnio();
        }
        reps.put("${NUMERO_CONTRATO}", numContrato);
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
        
        com.combinacion.models.Supervisor supInforme = (info != null && info.getSupervisor() != null) ? info.getSupervisor() : (contrato != null ? contrato.getSupervisor() : null);
        reps.put("${NOMBRE_SUPERVISOR}", supInforme != null && supInforme.getNombre() != null ? supInforme.getNombre() : "");
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
        java.util.Date fechaInforme = info.getFechaSuscripcion() != null ? info.getFechaSuscripcion() : info.getFechaFinPeriodo();
        reps.put("${FECHA_INFORME}", formatearFechaLarga(fechaInforme));
        
        // Planilla
        boolean esNuevo = "SI".equalsIgnoreCase(info.getEsContratistaNuevo());
        reps.put("${PLANILLA_NUMERO}", info.getPlanillaNumero() != null && !info.getPlanillaNumero().trim().isEmpty() ? info.getPlanillaNumero() : (esNuevo ? "N/A" : ""));
        reps.put("${PLANILLA_PIN}", info.getPlanillaPin() != null && !info.getPlanillaPin().trim().isEmpty() ? info.getPlanillaPin() : (esNuevo ? "N/A" : ""));
        reps.put("${PLANILLA_OPERADOR}", info.getPlanillaOperador() != null && !info.getPlanillaOperador().trim().isEmpty() ? info.getPlanillaOperador() : (esNuevo ? "N/A" : ""));
        reps.put("${PLANILLA_FECHA_PAGO}", (info.getPlanillaFechaPago() != null && !esNuevo) ? formatearFechaLarga(info.getPlanillaFechaPago()) : "N/A");
        reps.put("${PLANILLA_PERIODO}", (esNuevo || info.getPlanillaPeriodo() == null || info.getPlanillaPeriodo().trim().isEmpty() || "N/A".equalsIgnoreCase(info.getPlanillaPeriodo())) ? "N/A" : formatearPeriodo(info.getPlanillaPeriodo()));
        
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
                com.combinacion.dao.VerboConjugacionDAO verboDao = new com.combinacion.dao.VerboConjugacionDAO();
                java.util.List<com.combinacion.models.VerboConjugacion> verbos = verboDao.obtenerActivos();
                if (verbos != null) {
                    // Ordenar por longitud descendente para que frases compuestas se reemplacen antes que verbos simples
                    verbos.sort((v1, v2) -> {
                        int len1 = v1.getTerceraPersona() != null ? v1.getTerceraPersona().length() : 0;
                        int len2 = v2.getTerceraPersona() != null ? v2.getTerceraPersona().length() : 0;
                        return Integer.compare(len2, len1);
                    });
                }

                for (com.combinacion.util.ObligacionesParser.ObligacionActividad item : lista) {
                    if (item.actividad != null && !item.actividad.isEmpty()) {
                        String ac = item.actividad;
                        if (verbos != null) {
                            for (com.combinacion.models.VerboConjugacion v : verbos) {
                                ac = aplicarConjugacion(ac, v.getTerceraPersona(), v.getPrimeraPersona());
                            }
                        }

                        item.actividad = HtmlToWordXmlConverter.convertHtmlToXml(ac, doc, 460.0);
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
                                actividadesXml.append("<w:p><w:pPr><w:jc w:val=\"both\"/><w:spacing w:before=\"120\" w:after=\"60\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:b/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/></w:rPr><w:t>")
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

    private static String aplicarConjugacion(String text, String targetVerb, String replacementVerb) {
        if (text == null || targetVerb == null || targetVerb.trim().isEmpty() || replacementVerb == null) {
            return text;
        }
        String regex = buildRegexForPhrase(targetVerb);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String matched = matcher.group();
            String adapted = adaptCasing(matched, replacementVerb);
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(adapted));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String buildRegexForPhrase(String phrase) {
        String trimmed = phrase.trim();
        String[] words = trimmed.split("\\s+");
        StringBuilder sb = new StringBuilder();
        sb.append("(?<!\\p{L})");

        // Protección especial para sustantivos comunes que coinciden con verbos (ej: "apoyo", "soporte", "trámite", "control", "pago", "cobro")
        // Si va precedido de un verbo de acción (brindó/brindé, prestó/presté, dio/di...) o preposición/artículo (de, el, un...),
        // funciona como sustantivo y NO debe conjugarse (evita "brindé apoyé").
        if (words.length == 1) {
            String wLower = trimmed.toLowerCase();
            if (wLower.equals("apoyó") || wLower.equals("apoyo") || 
                wLower.equals("soporte") || wLower.equals("soportó") ||
                wLower.equals("trámite") || wLower.equals("tramitó") ||
                wLower.equals("control") || wLower.equals("controló") ||
                wLower.equals("pago") || wLower.equals("pagó") ||
                wLower.equals("cobro") || wLower.equals("cobró") ||
                wLower.equals("registro") || wLower.equals("registró") ||
                wLower.equals("cargo") || wLower.equals("cargó")) {
                sb.append("(?<!(?i)(?:brind[oóé]|prest[oóé]|di[oó]?|dar|brindar|prestar|realiz[oóé]|ejerci[oó]|ejerc[ií]|llev[oóé]|efectu[oóé]|de|del|el|la|los|las|al|un|una|unos|unas|en|con|como|sin|para|por|su|sus)\\s+)");
            }
        }

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                sb.append("\\s+");
            }
            sb.append(buildRegexForWord(words[i]));
        }
        sb.append("(?!\\p{L})");
        return sb.toString();
    }

    private static String buildRegexForWord(String word) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            char lower = Character.toLowerCase(c);
            switch (lower) {
                case 'a':
                case '\u00e1':
                case '\u00e0':
                    sb.append("[a\u00e1\u00e0A\u00c1\u00c0]");
                    break;
                case 'e':
                case '\u00e9':
                case '\u00e8':
                    sb.append("[e\u00e9\u00e8E\u00c9\u00c8]");
                    break;
                case 'i':
                case '\u00ed':
                case '\u00ec':
                    sb.append("[i\u00ed\u00ecI\u00cd\u00cc]");
                    break;
                case 'o':
                case '\u00f3':
                case '\u00f2':
                    sb.append("[o\u00f3\u00f2O\u00d3\u00d2]");
                    break;
                case 'u':
                case '\u00fa':
                case '\u00f9':
                case '\u00fc':
                    sb.append("[u\u00fa\u00f9\u00fcU\u00da\u00d9\u00dc]");
                    break;
                case 'n':
                case '\u00f1':
                    sb.append("[n\u00f1N\u00d1]");
                    break;
                default:
                    if (Character.isLetterOrDigit(c)) {
                        sb.append(c);
                    } else {
                        sb.append(java.util.regex.Pattern.quote(String.valueOf(c)));
                    }
                    break;
            }
        }
        return sb.toString();
    }

    private static String adaptCasing(String matched, String replacement) {
        if (matched == null || matched.isEmpty() || replacement == null || replacement.isEmpty()) {
            return replacement;
        }
        boolean isAllUpper = true;
        for (int i = 0; i < matched.length(); i++) {
            char c = matched.charAt(i);
            if (Character.isLetter(c) && !Character.isUpperCase(c)) {
                isAllUpper = false;
                break;
            }
        }
        if (isAllUpper) {
            return replacement.toUpperCase();
        }
        if (Character.isUpperCase(matched.charAt(0))) {
            if (replacement.length() == 1) {
                return replacement.toUpperCase();
            }
            return Character.toUpperCase(replacement.charAt(0)) + replacement.substring(1);
        }
        return replacement.toLowerCase();
    }
}
