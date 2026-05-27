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


    private static final String TEMPLATE_PATH = "plantillas/INFORME_SUPERVISION_TEMPLATE.docx";
    private static final String OUTPUT_DIR = "generados/informes";

    public static String generarDocx(InformeSupervision info, Contrato contrato) throws IOException {
        File templateFile = new File(TEMPLATE_PATH);
        if (!templateFile.exists()) {
            // Fallback for different environments
            templateFile = new File("c:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\" + TEMPLATE_PATH);
        }

        if (!templateFile.exists()) {
            throw new IOException("Plantilla no encontrada en: " + TEMPLATE_PATH);
        }

        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) outputDir.mkdirs();

        String outputFileName = "Informe_" + contrato.getNumeroContrato().replace("/", "-") + "_Cuota_" + info.getNumeroCuota() + ".docx";
        File outputFile = new File(outputDir, outputFileName);

        Map<String, String> reps = new HashMap<>();

        // Datos del Contrato
        reps.put("${NUMERO_CONTRATO}", contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : "");
        reps.put("${CONTRATISTA_NOMBRE}", contrato.getContratistaNombre() != null ? contrato.getContratistaNombre() : "");
        reps.put("${CONTRATISTA_CEDULA}", contrato.getContratista() != null && contrato.getContratista().getCedula() != null ? contrato.getContratista().getCedula() : "");
        reps.put("${TELEFONO}", contrato.getContratista() != null && contrato.getContratista().getTelefono() != null ? contrato.getContratista().getTelefono() : "");
        reps.put("${NOMBRE_SUPERVISOR}", contrato.getSupervisor() != null && contrato.getSupervisor().getNombre() != null ? contrato.getSupervisor().getNombre() : "");
        reps.put("${ORGANISMO}", contrato.getSupervisor() != null && contrato.getSupervisor().getCargo() != null ? contrato.getSupervisor().getCargo() : "");
        reps.put("${OBJETO_CONTRACTUAL}", contrato.getObjeto() != null ? contrato.getObjeto() : "");
        
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
        reps.put("${NUMERO_CUOTA}", info.getNumeroCuota() != null ? info.getNumeroCuota() : "");
        
        reps.put("${X_PARCIAL}", info.getTipoInforme() != null && info.getTipoInforme().toUpperCase().contains("PARCIAL") ? "X" : "  ");
        reps.put("${X_FINAL}", info.getTipoInforme() != null && info.getTipoInforme().toUpperCase().contains("FINAL") ? "X" : "  ");

        reps.put("${FECHA_INICIO_PERIODO}", formatearFechaEspecial(info.getFechaInicioPeriodo()));
        reps.put("${FECHA_FIN_PERIODO}", formatearFechaEspecial(info.getFechaFinPeriodo()));
        
        reps.put("${MODIFICACIONES}", info.getModificaciones() != null && !info.getModificaciones().trim().isEmpty() ? info.getModificaciones() : "N/A");
        reps.put("${SUSPENSIONES}", info.getSuspensiones() != null && !info.getSuspensiones().trim().isEmpty() ? info.getSuspensiones() : "N/A");
        reps.put("${REANUDACIONES}", info.getReanudaciones() != null && !info.getReanudaciones().trim().isEmpty() ? info.getReanudaciones() : "N/A");
        reps.put("${CESIONES}", info.getCesiones() != null && !info.getCesiones().trim().isEmpty() ? info.getCesiones() : "N/A");
        reps.put("${TERMINACION_ANTICIPADA}", info.getTerminacionAnticipada() != null && !info.getTerminacionAnticipada().trim().isEmpty() ? info.getTerminacionAnticipada() : "N/A");
        
        reps.put("${VALOR_CUOTA_PAGAR}", info.getValorCuotaPagar() != null ? "$ " + nf.format(info.getValorCuotaPagar()).replace(',', '.') : "$ 0");
        reps.put("${VALOR_ACUMULADO}", info.getValorAccumuladoPagado() != null ? "$ " + nf.format(info.getValorAccumuladoPagado()).replace(',', '.') : "$ 0");
        reps.put("${SALDO_CANCELAR}", info.getSaldoPorCancelar() != null ? "$ " + nf.format(info.getSaldoPorCancelar()).replace(',', '.') : "$ 0");
        
        reps.put("${PLANILLA_NUMERO}", info.getPlanillaNumero());
        reps.put("${PLANILLA_PIN}", info.getPlanillaPin());
        reps.put("${PLANILLA_OPERADOR}", info.getPlanillaOperador());
        reps.put("${PLANILLA_FECHA_PAGO}", formatearFechaEspecial(info.getPlanillaFechaPago()));
        reps.put("${PLANILLA_PERIODO}", info.getPlanillaPeriodo());
        
        reps.put("${OBSERVACIONES_TECNICAS}", info.getObservacionesTecnicas());
        reps.put("${RECOMENDACIONES}", info.getRecomendaciones());
        reps.put("${FECHA_SUSCRIPCION}", formatearFechaEspecial(info.getFechaSuscripcion()));

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
