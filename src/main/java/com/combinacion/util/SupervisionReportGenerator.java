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

public class SupervisionReportGenerator {

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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Datos del Contrato
        reps.put("${NUMERO_CONTRATO}", contrato.getNumeroContrato());
        reps.put("${CONTRATISTA_NOMBRE}", contrato.getContratistaNombre());
        // reps.put("${CONTRATISTA_CEDULA}", contrato.getContratistaCedula()); // Assuming it's in the model
        reps.put("${OBJETO_CONTRACTUAL}", contrato.getObjeto());

        // Datos del Informe
        reps.put("${PERIODO_INFORME}", info.getPeriodoInforme());
        reps.put("${TIPO_INFORME}", info.getTipoInforme());
        reps.put("${NUMERO_CUOTA}", info.getNumeroCuota());
        reps.put("${FECHA_INICIO_PERIODO}", info.getFechaInicioPeriodo() != null ? sdf.format(info.getFechaInicioPeriodo()) : "");
        reps.put("${FECHA_FIN_PERIODO}", info.getFechaFinPeriodo() != null ? sdf.format(info.getFechaFinPeriodo()) : "");
        
        reps.put("${MODIFICACIONES}", info.getModificaciones() != null ? info.getModificaciones() : "N/A");
        reps.put("${SUSPENSIONES}", info.getSuspensiones() != null ? info.getSuspensiones() : "N/A");
        
        reps.put("${VALOR_CUOTA_PAGAR}", info.getValorCuotaPagar() != null ? info.getValorCuotaPagar().toString() : "0");
        reps.put("${VALOR_ACUMULADO}", info.getValorAccumuladoPagado() != null ? info.getValorAccumuladoPagado().toString() : "0");
        reps.put("${SALDO_CANCELAR}", info.getSaldoPorCancelar() != null ? info.getSaldoPorCancelar().toString() : "0");
        
        reps.put("${PLANILLA_NUMERO}", info.getPlanillaNumero());
        reps.put("${PLANILLA_PIN}", info.getPlanillaPin());
        reps.put("${PLANILLA_OPERADOR}", info.getPlanillaOperador());
        reps.put("${PLANILLA_FECHA_PAGO}", info.getPlanillaFechaPago() != null ? sdf.format(info.getPlanillaFechaPago()) : "");
        reps.put("${PLANILLA_PERIODO}", info.getPlanillaPeriodo());
        
        reps.put("${OBSERVACIONES_TECNICAS}", info.getObservacionesTecnicas());
        reps.put("${RECOMENDACIONES}", info.getRecomendaciones());
        reps.put("${FECHA_SUSCRIPCION}", info.getFechaSuscripcion() != null ? sdf.format(info.getFechaSuscripcion()) : "");

        try (FileInputStream fis = new FileInputStream(templateFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            TemplateGenerator.generate(fis, reps, fos);
        }

        return outputFile.getAbsolutePath();
    }
}
