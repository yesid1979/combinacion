package com.combinacion.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import org.apache.poi.xwpf.usermodel.*;

/**
 * Herramienta para agregar placeholders al documento original
 * Identifica campos que deben ser dinámicos y los reemplaza con {{PLACEHOLDER}}
 */
public class PlaceholderMapper {

    // Mapa de valores hardcodeados -> placeholders
    private static final Map<String, String> PLACEHOLDER_MAP = new LinkedHashMap<>();

    static {
        // ===== INFORMACIÓN DEL PROCESO =====
        PLACEHOLDER_MAP.put("4121.010.32.1.076-2026", "{{NUMERO_PROCESO}}");

        // ===== INFORMACIÓN DEL SUPERVISOR =====
        PLACEHOLDER_MAP.put("CLAUDIA PATRICIA VARGAS OROZCO", "{{NOMBRE_SUPERVISOR}}");
        PLACEHOLDER_MAP.put("Subdirectora de Defensa Judicial y Prevención del Daño Antijurídico",
                "{{CARGO_SUPERVISOR}}");

        // ===== INFORMACIÓN PRESUPUESTAL (CDP) =====
        PLACEHOLDER_MAP.put("3500254255", "{{NUMERO_CDP}}");
        PLACEHOLDER_MAP.put("5 de enero de 2026", "{{FECHA_EXPEDICION_CDP}}");
        PLACEHOLDER_MAP.put("31 de didciembre de 2026", "{{FECHA_VENCIMIENTO_CDP}}");
        PLACEHOLDER_MAP.put("$ 962010000", "{{VALOR_CDP}}");
        PLACEHOLDER_MAP.put("4121/1.2.1.0.00/2.3.2.02.02.008/63020010002/BP260054601010103", "{{COMPROMISO_CDP}}");

        // ===== INFORMACIÓN DEL CONTRATO =====
        PLACEHOLDER_MAP.put("Diecinueve millones doscientos veinte mil pesos m/cte ($19220000)",
                "{{VALOR_CONTRATO_LETRAS}}");
        PLACEHOLDER_MAP.put("$19220000", "{{VALOR_CONTRATO}}");
        PLACEHOLDER_MAP.put("Cuatro (4)", "{{NUMERO_CUOTAS}}");
        PLACEHOLDER_MAP.put("Cuatro millones ochocientos cinco mil pesos m/cte ($4805000)", "{{VALOR_CUOTA_LETRAS}}");
        PLACEHOLDER_MAP.put("$4805000", "{{VALOR_CUOTA}}");
        PLACEHOLDER_MAP.put("Treinta (30) de junio del Dos mil veintiséis (2026)", "{{FECHA_FIN_CONTRATO}}");

        // ===== INFORMACIÓN DEL PROYECTO =====
        PLACEHOLDER_MAP.put("BP-26005460", "{{CODIGO_PROYECTO}}");
        PLACEHOLDER_MAP.put(
                "Fortalecimiento del ciclo de defensa jurídica y de la política de mejora normativa del Distrito Especial de Santiago de Cali",
                "{{NOMBRE_PROYECTO}}");
        PLACEHOLDER_MAP.put("2024760010018", "{{BPIN}}");

        // ===== INFORMACIÓN DEL CONTRATISTA (estos estarán vacíos en la plantilla)
        // =====
        // Estos se agregarán como placeholders vacíos para ser llenados después

        // ===== CLASIFICACIÓN UNSPSC =====
        PLACEHOLDER_MAP.put("18441", "{{ID_PAA}}");
    }

    public static void main(String[] args) {
        String inputFile = "doc/PLANTILLAS_TODAS_DAGJP_INVERSION_2026.docx";
        String outputFile = "doc/PLANTILLAS_TODAS_DAGJP_INVERSION_2026_CON_PLACEHOLDERS.docx";
        String reportFile = "placeholder_replacement_report.txt";

        try {
            System.out.println("Iniciando proceso de inserción de placeholders...");
            System.out.println("Archivo de entrada: " + inputFile);
            System.out.println("Archivo de salida: " + outputFile);

            int replacements = addPlaceholders(inputFile, outputFile, reportFile);

            System.out.println("\n=== PROCESO COMPLETADO ===");
            System.out.println("Total de reemplazos realizados: " + replacements);
            System.out.println("Documento con placeholders guardado en: " + outputFile);
            System.out.println("Reporte detallado guardado en: " + reportFile);

        } catch (Exception e) {
            System.err.println("Error durante el proceso: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static int addPlaceholders(String inputPath, String outputPath, String reportPath) throws Exception {
        File inputFile = new File(inputPath);
        int totalReplacements = 0;

        Map<String, Integer> replacementCount = new LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(inputFile);
                XWPFDocument document = new XWPFDocument(fis);
                PrintWriter report = new PrintWriter(reportPath, "UTF-8")) {

            report.println(repeat("=", 80));
            report.println("REPORTE DE INSERCIÓN DE PLACEHOLDERS");
            report.println(repeat("=", 80));
            report.println("Archivo: " + inputPath);
            report.println("Fecha: " + new Date());
            report.println();

            // Procesar headers
            report.println("\n--- PROCESANDO HEADERS ---");
            if (document.getHeaderList() != null) {
                for (XWPFHeader header : document.getHeaderList()) {
                    int count = processHeaderFooter(header, replacementCount, report);
                    totalReplacements += count;
                }
            }

            // Procesar párrafos del cuerpo
            report.println("\n--- PROCESANDO PÁRRAFOS DEL CUERPO ---");
            for (XWPFParagraph para : document.getParagraphs()) {
                int count = processParagraph(para, replacementCount, report);
                totalReplacements += count;
            }

            // Procesar tablas
            report.println("\n--- PROCESANDO TABLAS ---");
            for (XWPFTable table : document.getTables()) {
                int count = processTable(table, replacementCount, report);
                totalReplacements += count;
            }

            // Procesar footers
            report.println("\n--- PROCESANDO FOOTERS ---");
            if (document.getFooterList() != null) {
                for (XWPFFooter footer : document.getFooterList()) {
                    int count = processHeaderFooter(footer, replacementCount, report);
                    totalReplacements += count;
                }
            }

            // Guardar documento modificado
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                document.write(fos);
            }

            // Escribir resumen
            report.println("\n" + repeat("=", 80));
            report.println("RESUMEN DE REEMPLAZOS");
            report.println(repeat("=", 80));

            for (Map.Entry<String, Integer> entry : replacementCount.entrySet()) {
                report.println(String.format("%-50s : %d veces", entry.getKey(), entry.getValue()));
            }

            report.println("\nTotal de reemplazos: " + totalReplacements);
        }

        return totalReplacements;
    }

    private static int processHeaderFooter(IBody body, Map<String, Integer> replacementCount, PrintWriter report) {
        int count = 0;

        if (body instanceof XWPFHeaderFooter) {
            XWPFHeaderFooter hf = (XWPFHeaderFooter) body;

            for (XWPFParagraph para : hf.getParagraphs()) {
                count += processParagraph(para, replacementCount, report);
            }

            for (XWPFTable table : hf.getTables()) {
                count += processTable(table, replacementCount, report);
            }
        }

        return count;
    }

    private static int processParagraph(XWPFParagraph para, Map<String, Integer> replacementCount, PrintWriter report) {
        int count = 0;
        String originalText = para.getText();

        if (originalText == null || originalText.trim().isEmpty()) {
            return 0;
        }

        // Intentar reemplazar en el párrafo completo
        for (Map.Entry<String, String> entry : PLACEHOLDER_MAP.entrySet()) {
            String searchText = entry.getKey();
            String placeholder = entry.getValue();

            if (originalText.contains(searchText)) {
                // Reemplazar en los runs del párrafo
                boolean replaced = replaceInParagraph(para, searchText, placeholder);

                if (replaced) {
                    count++;
                    replacementCount.put(placeholder, replacementCount.getOrDefault(placeholder, 0) + 1);
                    report.println("  ✓ Reemplazado: '" + searchText + "' -> " + placeholder);
                }
            }
        }

        return count;
    }

    private static int processTable(XWPFTable table, Map<String, Integer> replacementCount, PrintWriter report) {
        int count = 0;

        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph para : cell.getParagraphs()) {
                    count += processParagraph(para, replacementCount, report);
                }
            }
        }

        return count;
    }

    private static boolean replaceInParagraph(XWPFParagraph para, String searchText, String replacement) {
        String paraText = para.getText();

        if (!paraText.contains(searchText)) {
            return false;
        }

        // Estrategia: reconstruir el párrafo con el texto reemplazado
        // manteniendo el formato del primer run

        List<XWPFRun> runs = para.getRuns();
        if (runs.isEmpty()) {
            return false;
        }

        // Concatenar todo el texto
        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : runs) {
            String runText = run.getText(0);
            if (runText != null) {
                fullText.append(runText);
            }
        }

        String originalFullText = fullText.toString();
        String newText = originalFullText.replace(searchText, replacement);

        if (originalFullText.equals(newText)) {
            return false; // No hubo cambio
        }

        // Limpiar todos los runs excepto el primero
        XWPFRun firstRun = runs.get(0);

        // Remover runs adicionales
        for (int i = runs.size() - 1; i > 0; i--) {
            para.removeRun(i);
        }

        // Establecer el nuevo texto en el primer run
        firstRun.setText(newText, 0);

        return true;
    }

    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
