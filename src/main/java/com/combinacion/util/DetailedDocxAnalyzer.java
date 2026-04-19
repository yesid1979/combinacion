package com.combinacion.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.xwpf.usermodel.*;

/**
 * Analizador detallado de documentos DOCX.
 * 
 * Esta clase analiza documentos DOCX extrayendo información detallada sobre:
 * - Headers y footers
 * - Párrafos y su contenido
 * - Tablas y celdas
 * - Estilos y formato
 * - Placeholders encontrados
 * 
 * Genera un reporte completo en formato texto con toda la información extraída.
 * 
 * @author Yesid Piedrahita
 * @version 1.0
 * @since 2026-02-09
 */
public class DetailedDocxAnalyzer {

    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String filePath = args.length > 0 ? args[0] : "doc/PLANTILLAS_TODAS_DAGJP_INVERSION_2026.docx";
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("Error: Archivo no encontrado en: " + filePath);
            return;
        }

        try (FileInputStream fis = new FileInputStream(file);
                XWPFDocument document = new XWPFDocument(fis);
                PrintWriter writer = new PrintWriter("detailed_analysis.txt", "UTF-8")) {

            writer.println(repeat("=", 80));
            writer.println("ANÁLISIS DETALLADO: " + file.getName());
            writer.println(repeat("=", 80));
            writer.println();

            // Extraer todos los placeholders
            Set<String> allPlaceholders = new HashSet<>();

            // Analizar headers
            writer.println("\n" + repeat("=", 80));
            writer.println("HEADERS");
            writer.println(repeat("=", 80));
            if (document.getHeaderList() != null) {
                for (XWPFHeader header : document.getHeaderList()) {
                    analyzeHeaderFooter(header, writer, allPlaceholders, "HEADER");
                }
            }

            // Analizar body - párrafos
            writer.println("\n" + repeat("=", 80));
            writer.println("CUERPO DEL DOCUMENTO - PÁRRAFOS");
            writer.println(repeat("=", 80));

            int paraNum = 0;
            for (XWPFParagraph para : document.getParagraphs()) {
                paraNum++;
                String text = para.getText();
                if (text != null && !text.trim().isEmpty()) {
                    writer.println("\n--- Párrafo " + paraNum + " ---");
                    writer.println("Texto: " + text);

                    // Analizar estilo
                    if (para.getStyle() != null) {
                        writer.println("Estilo: " + para.getStyle());
                    }

                    // Analizar alineación
                    if (para.getAlignment() != null) {
                        writer.println("Alineación: " + para.getAlignment());
                    }

                    // Buscar placeholders
                    extractPlaceholders(text, allPlaceholders);

                    // Detectar separadores de sección
                    if (text.contains("ESTUDIOS PREVIOS") ||
                            text.contains("VERIFICACIÓN DE CUMPLIMIENTO") ||
                            text.contains("CERTIFICADO DE IDONEIDAD") ||
                            text.contains("COMPLEMENTO AL CONTRATO")) {
                        writer.println(">>> SEPARADOR DE SECCIÓN DETECTADO <<<");
                    }
                }
            }

            // Analizar tablas
            writer.println("\n" + repeat("=", 80));
            writer.println("TABLAS");
            writer.println(repeat("=", 80));

            List<XWPFTable> tables = document.getTables();
            for (int i = 0; i < tables.size(); i++) {
                writer.println("\n--- Tabla #" + (i + 1) + " ---");
                XWPFTable table = tables.get(i);

                writer.println("Número de filas: " + table.getNumberOfRows());

                for (int rowIdx = 0; rowIdx < table.getRows().size(); rowIdx++) {
                    XWPFTableRow row = table.getRows().get(rowIdx);
                    writer.println("\nFila " + (rowIdx + 1) + ":");

                    for (int cellIdx = 0; cellIdx < row.getTableCells().size(); cellIdx++) {
                        XWPFTableCell cell = row.getTableCells().get(cellIdx);
                        String cellText = cell.getText();

                        writer.println("  Celda " + (cellIdx + 1) + ": " + cellText);
                        extractPlaceholders(cellText, allPlaceholders);
                    }
                }
            }

            // Analizar footers
            writer.println("\n" + repeat("=", 80));
            writer.println("FOOTERS");
            writer.println(repeat("=", 80));
            if (document.getFooterList() != null) {
                for (XWPFFooter footer : document.getFooterList()) {
                    analyzeHeaderFooter(footer, writer, allPlaceholders, "FOOTER");
                }
            }

            // Resumen de placeholders encontrados
            writer.println("\n" + repeat("=", 80));
            writer.println("RESUMEN DE PLACEHOLDERS ENCONTRADOS");
            writer.println(repeat("=", 80));
            writer.println("Total de placeholders únicos: " + allPlaceholders.size());
            writer.println();

            List<String> sortedPlaceholders = new ArrayList<>(allPlaceholders);
            Collections.sort(sortedPlaceholders);

            for (String placeholder : sortedPlaceholders) {
                writer.println("  - " + placeholder);
            }

            writer.println("\n" + repeat("=", 80));
            writer.println("ANÁLISIS COMPLETADO");
            writer.println(repeat("=", 80));

            System.out.println("Análisis completado. Resultado en detailed_analysis.txt");
            System.out.println("Total de placeholders encontrados: " + allPlaceholders.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void analyzeHeaderFooter(IBody body, PrintWriter writer, Set<String> placeholders, String type) {
        writer.println("\n--- " + type + " ---");

        if (body instanceof XWPFHeaderFooter) {
            XWPFHeaderFooter hf = (XWPFHeaderFooter) body;

            for (XWPFParagraph p : hf.getParagraphs()) {
                String text = p.getText();
                if (text != null && !text.trim().isEmpty()) {
                    writer.println(text);
                    extractPlaceholders(text, placeholders);
                }
            }

            for (XWPFTable t : hf.getTables()) {
                for (XWPFTableRow r : t.getRows()) {
                    for (XWPFTableCell c : r.getTableCells()) {
                        String cellText = c.getText();
                        writer.println("  | " + cellText);
                        extractPlaceholders(cellText, placeholders);
                    }
                }
            }
        }
    }

    private static void extractPlaceholders(String text, Set<String> placeholders) {
        if (text == null)
            return;

        // Buscar patrones como {{PLACEHOLDER}}, {PLACEHOLDER}, [PLACEHOLDER], etc.
        Pattern[] patterns = {
                Pattern.compile("\\{\\{([^}]+)\\}\\}"), // {{PLACEHOLDER}}
                Pattern.compile("\\{([A-Z_][A-Z0-9_]*)\\}"), // {PLACEHOLDER}
                Pattern.compile("\\[([A-Z_][A-Z0-9_]*)\\]"), // [PLACEHOLDER]
                Pattern.compile("«([^»]+)»"), // «PLACEHOLDER»
                Pattern.compile("\\$\\{([^}]+)\\}") // ${PLACEHOLDER}
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                placeholders.add(matcher.group(0)); // Agregar con delimitadores
            }
        }
    }
}
