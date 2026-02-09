package com.combinacion.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import org.apache.poi.xwpf.usermodel.*;

/**
 * Divide el documento con placeholders en 4 plantillas individuales
 * basándose en los headers que identifican cada sección
 */
public class DocumentSplitter {

    // Códigos de los headers que identifican cada plantilla
    private static final String HEADER_ESTUDIOS_PREVIOS = "MAJA01.04.01.P002.F001";
    private static final String HEADER_VERIFICACION = "MAJA01.04.02.P007.F001";
    private static final String HEADER_CERTIFICADO = "MAJA01.04.02.P007.F002";
    private static final String HEADER_COMPLEMENTO = "MAJA01.04.03.P001.F003";

    public static void main(String[] args) {
        String inputFile = "doc/PLANTILLAS_TODAS_DAGJP_INVERSION_2026_CON_PLACEHOLDERS.docx";
        String outputDir = "plantillas/";

        try {
            System.out.println(repeat("=", 80));
            System.out.println("DIVISIÓN DE DOCUMENTO EN PLANTILLAS INDIVIDUALES");
            System.out.println(repeat("=", 80));
            System.out.println("Archivo de entrada: " + inputFile);
            System.out.println("Directorio de salida: " + outputDir);
            System.out.println();

            splitDocument(inputFile, outputDir);

            System.out.println("\n" + repeat("=", 80));
            System.out.println("PROCESO COMPLETADO EXITOSAMENTE");
            System.out.println(repeat("=", 80));

        } catch (Exception e) {
            System.err.println("Error durante el proceso: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void splitDocument(String inputPath, String outputDir) throws Exception {
        File inputFile = new File(inputPath);
        File outDir = new File(outputDir);

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try (FileInputStream fis = new FileInputStream(inputFile);
                XWPFDocument sourceDoc = new XWPFDocument(fis)) {

            // Identificar los headers y sus índices
            List<XWPFHeader> headers = sourceDoc.getHeaderList();

            System.out.println("Total de headers encontrados: " + (headers != null ? headers.size() : 0));

            if (headers == null || headers.isEmpty()) {
                System.out.println("ADVERTENCIA: No se encontraron headers en el documento");
                return;
            }

            // Mapear cada header a su código
            Map<String, XWPFHeader> headerMap = new HashMap<>();

            for (int i = 0; i < headers.size(); i++) {
                XWPFHeader header = headers.get(i);
                String headerText = getHeaderText(header);

                System.out.println("\nHeader " + (i + 1) + ":");
                System.out.println("  Texto: " + headerText.substring(0, Math.min(100, headerText.length())));

                if (headerText.contains(HEADER_ESTUDIOS_PREVIOS)) {
                    headerMap.put("ESTUDIOS_PREVIOS", header);
                    System.out.println("  → Identificado como: ESTUDIOS PREVIOS");
                } else if (headerText.contains(HEADER_VERIFICACION)) {
                    headerMap.put("VERIFICACION", header);
                    System.out.println("  → Identificado como: VERIFICACIÓN");
                } else if (headerText.contains(HEADER_CERTIFICADO)) {
                    headerMap.put("CERTIFICADO", header);
                    System.out.println("  → Identificado como: CERTIFICADO");
                } else if (headerText.contains(HEADER_COMPLEMENTO)) {
                    headerMap.put("COMPLEMENTO", header);
                    System.out.println("  → Identificado como: COMPLEMENTO");
                }
            }

            System.out.println("\n" + repeat("-", 80));
            System.out.println("Plantillas identificadas: " + headerMap.size());
            System.out.println(repeat("-", 80));

            // Por ahora, crear una plantilla de ejemplo con el primer header
            // TODO: Implementar la lógica completa de división

            if (headerMap.containsKey("ESTUDIOS_PREVIOS")) {
                System.out.println("\nCreando plantilla: ESTUDIOS PREVIOS...");
                createTemplate(sourceDoc, headerMap.get("ESTUDIOS_PREVIOS"),
                        outputDir + "INVERSION_1_ESTUDIOS_PREVIOS_V2.docx");
            }

            if (headerMap.containsKey("VERIFICACION")) {
                System.out.println("Creando plantilla: VERIFICACIÓN...");
                createTemplate(sourceDoc, headerMap.get("VERIFICACION"),
                        outputDir + "INVERSION_2_VERIFICACION_CUMPLIMIENTO_V2.docx");
            }

            if (headerMap.containsKey("CERTIFICADO")) {
                System.out.println("Creando plantilla: CERTIFICADO...");
                createTemplate(sourceDoc, headerMap.get("CERTIFICADO"),
                        outputDir + "INVERSION_3_CERTIFICADO_IDONEIDAD_V2.docx");
            }

            if (headerMap.containsKey("COMPLEMENTO")) {
                System.out.println("Creando plantilla: COMPLEMENTO...");
                createTemplate(sourceDoc, headerMap.get("COMPLEMENTO"),
                        outputDir + "INVERSION_4_COMPLEMENTO_CONTRATO_V2.docx");
            }
        }
    }

    private static String getHeaderText(XWPFHeader header) {
        StringBuilder text = new StringBuilder();

        for (XWPFParagraph para : header.getParagraphs()) {
            text.append(para.getText()).append(" ");
        }

        for (XWPFTable table : header.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    text.append(cell.getText()).append(" ");
                }
            }
        }

        return text.toString();
    }

    private static void createTemplate(XWPFDocument sourceDoc, XWPFHeader targetHeader,
            String outputPath) throws Exception {
        // Crear un nuevo documento
        XWPFDocument newDoc = new XWPFDocument();

        // Copiar el header específico
        // NOTA: Apache POI no tiene una forma directa de copiar headers
        // Por ahora, copiaremos todo el contenido del documento original
        // y luego filtraremos por sección

        // TODO: Implementar copia selectiva de contenido basado en el header

        // Por ahora, copiar todos los párrafos y tablas
        for (XWPFParagraph para : sourceDoc.getParagraphs()) {
            copyParagraph(para, newDoc);
        }

        for (XWPFTable table : sourceDoc.getTables()) {
            copyTable(table, newDoc);
        }

        // Guardar el documento
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            newDoc.write(fos);
        }

        newDoc.close();
        System.out.println("  ✓ Guardado en: " + outputPath);
    }

    private static void copyParagraph(XWPFParagraph source, XWPFDocument targetDoc) {
        XWPFParagraph newPara = targetDoc.createParagraph();

        // Copiar estilo del párrafo
        newPara.setAlignment(source.getAlignment());
        newPara.setStyle(source.getStyle());

        // Copiar runs
        for (XWPFRun run : source.getRuns()) {
            XWPFRun newRun = newPara.createRun();

            // Copiar texto
            String text = run.getText(0);
            if (text != null) {
                newRun.setText(text);
            }

            // Copiar formato
            newRun.setBold(run.isBold());
            newRun.setItalic(run.isItalic());
            newRun.setUnderline(run.getUnderline());

            if (run.getFontFamily() != null) {
                newRun.setFontFamily(run.getFontFamily());
            }

            if (run.getFontSize() > 0) {
                newRun.setFontSize(run.getFontSize());
            }
        }
    }

    private static void copyTable(XWPFTable source, XWPFDocument targetDoc) {
        XWPFTable newTable = targetDoc.createTable();

        // Copiar filas
        for (int i = 0; i < source.getRows().size(); i++) {
            XWPFTableRow sourceRow = source.getRows().get(i);
            XWPFTableRow newRow;

            if (i == 0) {
                newRow = newTable.getRow(0); // Primera fila ya existe
            } else {
                newRow = newTable.createRow();
            }

            // Copiar celdas
            for (int j = 0; j < sourceRow.getTableCells().size(); j++) {
                XWPFTableCell sourceCell = sourceRow.getTableCells().get(j);
                XWPFTableCell newCell;

                if (j < newRow.getTableCells().size()) {
                    newCell = newRow.getTableCells().get(j);
                } else {
                    newCell = newRow.addNewTableCell();
                }

                // Copiar párrafos de la celda
                for (XWPFParagraph para : sourceCell.getParagraphs()) {
                    // Limpiar párrafos existentes en la celda nueva
                    if (newCell.getParagraphs().size() > 0) {
                        newCell.removeParagraph(0);
                    }

                    XWPFParagraph newPara = newCell.addParagraph();
                    newPara.setAlignment(para.getAlignment());

                    for (XWPFRun run : para.getRuns()) {
                        XWPFRun newRun = newPara.createRun();
                        String text = run.getText(0);
                        if (text != null) {
                            newRun.setText(text);
                        }
                    }
                }
            }
        }
    }

    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
