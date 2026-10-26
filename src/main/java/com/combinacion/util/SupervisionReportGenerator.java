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
        return ParseUtils.formatearPeriodo(periodo);
    }

    private static final String TEMPLATE_PATH_NORMAL = "plantillas/INFORME_SUPERVISION_TEMPLATE.docx";
    private static final String TEMPLATE_PATH_BORRADOR = "plantillas/INFORME_SUPERVISION_TEMPLATE_BORRADOR.docx";
    private static final String OUTPUT_DIR = "generados/informes";

    private static final java.util.Map<String, byte[]> templateCacheMap = new java.util.concurrent.ConcurrentHashMap<>();

    public static String generarDocx(InformeSupervision info, Contrato contrato) throws IOException {
        return generarDocx(info, contrato, null);
    }

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
            // Fallback for different environments
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
        String outputFileName = "3. INFORME SUPERVISION No. " + info.getNumeroCuota() + " -" + contratista + ".docx";
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
        reps.put("${CONTRATISTA_DV}", contrato.getContratista() != null && contrato.getContratista().getDv() != null ? contrato.getContratista().getDv() : "0");
        reps.put("${TELEFONO}", contrato.getContratista() != null && contrato.getContratista().getTelefono() != null ? contrato.getContratista().getTelefono() : "");
        
        // Firma del contratista
        String firmaUrl = new com.combinacion.dao.UsuarioDAO().obtenerFirmaPorCedula(cedula);
        if (firmaUrl != null) {
            reps.put("${FIRMA_CONTRATISTA}", "__IMG__:" + firmaUrl);
        } else {
            reps.put("${FIRMA_CONTRATISTA}", ""); // Si no hay firma, se deja en blanco
        }
        
        com.combinacion.models.Supervisor supInforme = (info != null && info.getSupervisor() != null) ? info.getSupervisor() : (contrato != null ? contrato.getSupervisor() : null);
        reps.put("${NOMBRE_SUPERVISOR}", supInforme != null && supInforme.getNombre() != null ? supInforme.getNombre() : "");
        reps.put("${CARGO_SUPERVISOR}", supInforme != null && supInforme.getCargo() != null ? supInforme.getCargo() : "");
        
        boolean esEncargado = false;
        if (supInforme != null) {
            String cargo = supInforme.getCargo() != null ? supInforme.getCargo() : "";
            String nombre = supInforme.getNombre() != null ? supInforme.getNombre() : "";
            if (cargo.contains("(E)") || cargo.contains("(e)") || nombre.contains("(E)") || nombre.contains("(e)")) {
                esEncargado = true;
            }
        }
        if (info != null && info.getSupervisorId() != null && contrato != null && contrato.getSupervisorId() > 0) {
            if (!info.getSupervisorId().equals(contrato.getSupervisorId())) {
                esEncargado = true;
            }
        }
        reps.put("${TEXTO_SUPERVISOR}", esEncargado ? "Nombre y firma del Supervisor (E)" : "Nombre y firma del Supervisor");
        
        reps.put("${ORGANISMO}", contrato.getOrdenadorGasto() != null && contrato.getOrdenadorGasto().getOrganismo() != null ? contrato.getOrdenadorGasto().getOrganismo() : "");
        reps.put("${OBJETO_CONTRACTUAL}", contrato.getObjeto() != null ? contrato.getObjeto() : "");
        reps.put("${TIPO_CONTRATO}", contrato.getTipoContrato() != null ? contrato.getTipoContrato().toUpperCase() : "");

        
        NumberFormat nf = NumberFormat.getInstance(new Locale("es", "CO"));
        if (nf instanceof DecimalFormat) {
            ((DecimalFormat) nf).applyPattern("#,##0");
        }
        
        // 1. Valor inicial del contrato (siempre el valor original antes de adición)
        Number valorInicialNums = contrato.getValorTotalNumeros();
        String valorInicialLetras = contrato.getValorTotalLetras();
        String valorInicialStr = valorInicialNums != null ? "$ " + nf.format(valorInicialNums).replace(',', '.') : "$ 0";
        reps.put("${VALOR_INICIAL_LETRAS}", valorInicialLetras != null ? valorInicialLetras.toUpperCase() : "");
        reps.put("${VALOR_INICIAL_NUMEROS}", valorInicialStr);

        // 2. Valor total vigente del contrato (con adición si existe, para la columna 'Valor Total del Contrato' en el Resumen Financiero)
        Number valorTotalNums = contrato.getValorVigente();
        String valorTotalStr = valorTotalNums != null ? "$ " + nf.format(valorTotalNums).replace(',', '.') : "$ 0";
        reps.put("${VALOR_TOTAL_NUMEROS}", valorTotalStr);
        reps.put("${VALOR_TOTAL_CONTRATO}", valorTotalStr);
        reps.put("${VALOR_TOTAL}", valorTotalStr);

        // Datos del Informe
        reps.put("${PERIODO_INFORME}", info.getPeriodoInforme() != null ? formatearPeriodo(info.getPeriodoInforme()) : "");
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
        boolean esInformeFinal = (info.getTipoInforme() != null && info.getTipoInforme().toUpperCase().contains("FINAL"));
        if (!esInformeFinal && contrato != null && contrato.getNumCuotasNumero() > 0) {
            int totalCuotasContrato = contrato.getNumCuotasNumero() + contrato.getNumeroCuotasAdicion();
            int cuotaActualParsed = com.combinacion.util.ParseUtils.parseInt(info.getNumeroCuota());
            if (cuotaActualParsed > 0 && cuotaActualParsed >= totalCuotasContrato) {
                esInformeFinal = true;
            }
        }

        String numContratoConAnio = (contrato != null && contrato.getNumeroContrato() != null) ? contrato.getNumeroContrato().trim() : "";
        if (contrato != null && contrato.getAnio() != null && contrato.getAnio() > 0 && !numContratoConAnio.contains(String.valueOf(contrato.getAnio()))) {
            numContratoConAnio += " de " + contrato.getAnio();
        }

        String textoReciboLegal = "Con la firma del presente informe se deja constancia del recibo a satisfacción por parte del DISTRITO DE SANTIAGO DE CALI – DEPARTAMENTO ADMINISTRATIVO DE GESTIÓN JURÍDICA PÚBLICA de los servicios prestados pactados en el contrato N° " + numContratoConAnio + ".";
        String textoPazSalvoLegal = "El contratista a la fecha del presente informe no posee a su cargo elementos devolutivos de propiedad del Distrito de Santiago de Cali, entregados por este organismo para el desempeño de sus actividades. Así mismo se encuentra a paz y salvo con el archivo de gestión documental y el sistema de gestión documental.";

        String valRecibo = info.getReciboSatisfaccion();
        if (esInformeFinal) {
            if (valRecibo == null || valRecibo.trim().isEmpty() || "N/A".equalsIgnoreCase(valRecibo.trim())) {
                valRecibo = textoReciboLegal;
            }
        } else {
            if (valRecibo == null || valRecibo.trim().isEmpty()) {
                valRecibo = "N/A";
            }
        }

        String valPazSalvo = info.getConstanciaPazSalvo();
        if (esInformeFinal) {
            if (valPazSalvo == null || valPazSalvo.trim().isEmpty() || "N/A".equalsIgnoreCase(valPazSalvo.trim())) {
                valPazSalvo = textoPazSalvoLegal;
            }
        } else {
            if (valPazSalvo == null || valPazSalvo.trim().isEmpty()) {
                valPazSalvo = "N/A";
            }
        }

        reps.put("${RECIBO_SATISFACCION}", valRecibo);
        reps.put("${CONSTANCIA_PAZ_SALVO}", valPazSalvo);
        
        reps.put("${VALOR_CUOTA_PAGAR}", info.getValorCuotaPagar() != null ? "$ " + nf.format(info.getValorCuotaPagar()).replace(',', '.') : "$ 0");
        reps.put("${VALOR_ACUMULADO}", info.getValorAccumuladoPagado() != null ? "$ " + nf.format(info.getValorAccumuladoPagado()).replace(',', '.') : "$ 0");
        reps.put("${SALDO_CANCELAR}", info.getSaldoPorCancelar() != null ? "$ " + nf.format(info.getSaldoPorCancelar()).replace(',', '.') : "$ 0");
        
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
        // No agregamos CONCEPTO_SUPERVISOR a reps para que TemplateGenerator no lo rompa.
        // Lo reemplazaremos directamente en el XML en el post-procesamiento.
        final String finalConceptoSup = concepto != null ? concepto : "";
        reps.put("${OBSERVACIONES_FINANCIERAS}", info.getObservacionesFinancieras() != null ? info.getObservacionesFinancieras() : "");
        reps.put("${OBSERVACIONES_TECNICAS}", info.getObservacionesTecnicas() != null ? info.getObservacionesTecnicas() : "");
        reps.put("${RECOMENDACIONES}", info.getRecomendaciones() != null ? info.getRecomendaciones() : "");
        String fechaSuscripcionFormateada = info.getFechaSuscripcion() != null ? 
            "Santiago de Cali, " + formatearFechaLarga(info.getFechaSuscripcion()) : "";
        reps.put("${FECHA_SUSCRIPCION}", fechaSuscripcionFormateada);

        boolean tieneApoyo = contrato.getApoyoSupervision() != null && !contrato.getApoyoSupervision().trim().isEmpty();
        reps.put("${LINEA_APOYO}", tieneApoyo ? "______________________________________________________" : "{{REMOVE_PARAGRAPH}}");
        reps.put("${NOMBRE_APOYO}", tieneApoyo ? contrato.getApoyoSupervision().toUpperCase() : "{{REMOVE_PARAGRAPH}}");
        reps.put("${TEXTO_APOYO}", tieneApoyo ? "Nombre y firma del Apoyo a la Supervisión" : "{{REMOVE_PARAGRAPH}}");

        java.util.List<com.combinacion.util.ObligacionesParser.ObligacionActividad> lista = null;
        if (finalConceptoSup != null) {
            lista = com.combinacion.util.ObligacionesParser.decodificarConcepto(finalConceptoSup, contrato.getActividadesEntregables());
        }
        
        // 1. Cargar plantilla en cache de memoria
        String cacheKey = templateFile.getAbsolutePath() + "_" + templateFile.lastModified();
        byte[] tplBytes = templateCacheMap.get(cacheKey);
        if (tplBytes == null) {
            tplBytes = java.nio.file.Files.readAllBytes(templateFile.toPath());
            templateCacheMap.put(cacheKey, tplBytes);
        }

        ByteArrayOutputStream docxMemoryStream = new ByteArrayOutputStream();

        try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(tplBytes);
             org.apache.poi.xwpf.usermodel.XWPFDocument doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(bais)) {
            
            TemplateGenerator.replacePlaceholders(doc, reps);
            
            // Reemplazar la tabla XML (se generan las imágenes en doc)
            if (lista != null) {
                for (com.combinacion.util.ObligacionesParser.ObligacionActividad item : lista) {
                    if (item.actividad != null && !item.actividad.isEmpty()) {
                        item.actividad = HtmlToWordXmlConverter.convertHtmlToXml(item.actividad, doc, 300.0);
                    }
                }
            }
            
            doc.write(docxMemoryStream);
        }
        
        // 2. Procesar ZIP en memoria para reemplazar variables en XML directamente
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
                    
                    String xParcial = reps.get("${X_PARCIAL}");
                    String xFinal = reps.get("${X_FINAL}");
                    
                    xml = xml.replace("${X_PARCIAL}", xParcial != null ? xParcial : "  ");
                    xml = xml.replace("${X_FINAL}", xFinal != null ? xFinal : "  ");
                    
                    if (esEncargado) {
                        xml = xml.replace("Nombre y firma del Supervisor", "Nombre y firma del Supervisor (E)");
                    }
                    
                    // Asegurar consistencia en la tabla 'Resumen contable y financiero':
                    // la columna 'Valor Total del Contrato' debe reflejar el valor total vigente con adición
                    if (contrato != null && contrato.isTieneAdicion()) {
                        String vTotal = reps.get("${VALOR_TOTAL_NUMEROS}");
                        String vInicial = reps.get("${VALOR_INICIAL_NUMEROS}");
                        if (vTotal != null && vInicial != null && !vTotal.equals(vInicial)) {
                            int idxResumen = xml.indexOf("Resumen contable y financiero");
                            if (idxResumen != -1) {
                                int idxTable = xml.indexOf("<w:tbl", idxResumen);
                                int idxEndTable = (idxTable != -1) ? xml.indexOf("</w:tbl>", idxTable) : -1;
                                if (idxTable != -1 && idxEndTable != -1) {
                                    String tblSub = xml.substring(idxTable, idxEndTable);
                                    if (tblSub.contains(vInicial)) {
                                        String updatedTbl = tblSub.replace(vInicial, vTotal);
                                        xml = xml.substring(0, idxTable) + updatedTbl + xml.substring(idxEndTable);
                                    }
                                }
                            }
                        }
                    }
                    
                    // Forzar que TODAS las filas de las tablas puedan dividirse entre páginas 
                    // para evitar que LibreOffice corte el texto cuando es muy largo
                    xml = xml.replaceAll("(?i)<w:cantSplit[^>]*>", "");
                    
                    // ================================================================
                    // FIX DEFINITIVO PARA LibreOffice Linux - CAUSA RAÍZ IDENTIFICADA:
                    // Cuando la tabla abarca múltiples páginas (por Objeto largo) Y
                    // contiene formas flotantes con <wp:anchor> (<mc:AlternateContent>),
                    // LibreOffice falla al renderizar TODA la tabla (pierde bordes, rellenos, etc.)
                    // ================================================================
                    
                    // FIX 1: Eliminar los bloques <mc:AlternateContent> (formas flotantes WPS)
                    // y conservar solo el contenido VML de <mc:Fallback>.
                    // LibreOffice no puede manejar correctamente formas WPS ancladas (<wp:anchor>)
                    // dentro de tablas que saltan de página → falla el renderizado completo.
                    xml = xml.replaceAll(
                        "<mc:AlternateContent>.*?<mc:Fallback>(.*?)</mc:Fallback>\\s*</mc:AlternateContent>",
                        "$1"
                    );
                    
                    // FIX 2: Agregar <w:tblBorders> a nivel de la tabla principal del cuerpo.
                    // La plantilla SOLO tiene bordes a nivel de celda (<w:tcBorders>),
                    // NO tiene <w:tblBorders> en el <w:tblPr>. LibreOffice necesita los
                    // bordes también a nivel de tabla para respetarlos cuando las celdas
                    // saltan páginas.
                    String TBL_BORDERS =
                        "<w:tblBorders>" +
                        "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>" +
                        "<w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>" +
                        "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>" +
                        "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>" +
                        "<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>" +
                        "<w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>" +
                        "</w:tblBorders>";
                    // Usar replaceFirst para agregar solo a la PRIMERA tabla (la principal del cuerpo).
                    // La tabla principal es la única que tiene <w:tblCellMar> en su <w:tblPr>.
                    xml = xml.replaceFirst("<w:tblCellMar>", TBL_BORDERS + "<w:tblCellMar>");
                    
                    if (lista != null) {
                        StringBuilder tablaXml = new StringBuilder();
                        tablaXml.append("</w:t></w:r></w:p>"); // Cerrar el párrafo actual de ${CONCEPTO_SUPERVISOR}
                        
                        tablaXml.append("<w:tbl>");
                        tablaXml.append("<w:tblPr>");
                        tablaXml.append("<w:tblW w:w=\"5000\" w:type=\"pct\"/>");
                        tablaXml.append(TBL_BORDERS);
                        tablaXml.append("<w:tblCellMar>");
                        tablaXml.append("<w:top w:w=\"80\" w:type=\"dxa\"/>");
                        tablaXml.append("<w:left w:w=\"100\" w:type=\"dxa\"/>");
                        tablaXml.append("<w:bottom w:w=\"80\" w:type=\"dxa\"/>");
                        tablaXml.append("<w:right w:w=\"100\" w:type=\"dxa\"/>");
                        tablaXml.append("</w:tblCellMar>");
                        tablaXml.append("<w:tblLook w:val=\"0000\" w:firstRow=\"0\" w:lastRow=\"0\" w:firstColumn=\"0\" w:lastColumn=\"0\" w:noHBand=\"0\" w:noVBand=\"0\"/>");
                        tablaXml.append("</w:tblPr>");
                        
                        tablaXml.append("<w:tblGrid>");
                        tablaXml.append("<w:gridCol w:w=\"4987\"/>");
                        tablaXml.append("<w:gridCol w:w=\"4987\"/>");
                        tablaXml.append("</w:tblGrid>");
                        
                        String tcBordersXml = "<w:tcBorders>" +
                            "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>" +
                            "<w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>" +
                            "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>" +
                            "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>" +
                            "</w:tcBorders>";
                        
                        // Header Row (50% / 50% = 2500 pct cada columna)
                        tablaXml.append("<w:tr>");
                        tablaXml.append("<w:tc><w:tcPr><w:tcW w:w=\"2500\" w:type=\"pct\"/>").append(tcBordersXml).append("<w:vAlign w:val=\"center\"/></w:tcPr><w:p><w:pPr><w:jc w:val=\"center\"/><w:spacing w:before=\"60\" w:after=\"60\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:b/><w:bCs/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/></w:rPr><w:t>OBLIGACIONES DEL CONTRATISTA</w:t></w:r></w:p></w:tc>");
                        tablaXml.append("<w:tc><w:tcPr><w:tcW w:w=\"2500\" w:type=\"pct\"/>").append(tcBordersXml).append("<w:vAlign w:val=\"center\"/></w:tcPr><w:p><w:pPr><w:jc w:val=\"center\"/><w:spacing w:before=\"60\" w:after=\"60\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:b/><w:bCs/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/></w:rPr><w:t>ACTIVIDADES</w:t></w:r></w:p></w:tc>");
                        tablaXml.append("</w:tr>");
                        
                        // Rows
                        for (com.combinacion.util.ObligacionesParser.ObligacionActividad item : lista) {
                            String obRaw = item.obligacion != null ? item.obligacion.trim() : "";
                            String acXml = item.actividad != null ? item.actividad : "<w:p><w:pPr><w:jc w:val=\"both\"/><w:spacing w:before=\"0\" w:after=\"40\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/></w:rPr><w:t></w:t></w:r></w:p>";
                            
                            String ob = obRaw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                            ob = ob.replace("\n", "</w:t><w:br/><w:t>");
                            
                            tablaXml.append("<w:tr>");
                            
                            // Cell Obligacion (50% = 2500 pct)
                            tablaXml.append("<w:tc><w:tcPr><w:tcW w:w=\"2500\" w:type=\"pct\"/>").append(tcBordersXml).append("<w:vAlign w:val=\"top\"/></w:tcPr><w:p><w:pPr><w:jc w:val=\"both\"/><w:spacing w:before=\"40\" w:after=\"40\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/></w:rPr><w:t>")
                                    .append(ob)
                                    .append("</w:t></w:r></w:p></w:tc>");
                                    
                            // Cell Actividad (50% = 2500 pct)
                            tablaXml.append("<w:tc><w:tcPr><w:tcW w:w=\"2500\" w:type=\"pct\"/>").append(tcBordersXml).append("<w:vAlign w:val=\"top\"/></w:tcPr>")
                                    .append(acXml)
                                    .append("</w:tc>");
                                    
                            tablaXml.append("</w:tr>");
                        }
                        
                        tablaXml.append("</w:tbl>");
                        
                        // Inyectar enlace de Drive si existe (formato estándar compatible con Google Docs y Microsoft Word)
                        String urlDrive = info.getUrlDriveEvidencias();
                        if (urlDrive != null && !urlDrive.trim().isEmpty()) {
                            urlDrive = urlDrive.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                            
                            // Párrafo de texto introductorio
                            tablaXml.append("<w:p><w:pPr><w:spacing w:before=\"240\" w:after=\"120\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/></w:rPr><w:t>En el siguiente link se encuentra las evidencias cuota ");
                            tablaXml.append(info.getNumeroCuota() != null ? info.getNumeroCuota() : "");
                            tablaXml.append(":</w:t></w:r></w:p>");
                            
                            // Párrafo con la URL azul subrayada legible y compatible con Google Docs
                            tablaXml.append("<w:p><w:pPr><w:spacing w:after=\"240\"/></w:pPr>");
                            tablaXml.append("<w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:color w:val=\"0000FF\"/><w:u w:val=\"single\"/></w:rPr><w:t>");
                            tablaXml.append(urlDrive);
                            tablaXml.append("</w:t></w:r>");
                            tablaXml.append("</w:p>");
                        }
                        
                        tablaXml.append("<w:p><w:r><w:t>"); // Reabrir para balancear el tag original
                        
                        xml = xml.replace("${CONCEPTO_SUPERVISOR}", tablaXml.toString());
                    } else {
                        xml = xml.replace("${CONCEPTO_SUPERVISOR}", "");
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
