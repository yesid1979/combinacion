package com.combinacion.util;

import java.io.FileInputStream;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;

public class TemplateContentInspector {
    public static void main(String[] args) {
        inspect("plantillas/INVERSION_3_CERTIFICADO_IDONEIDAD.docx");
        inspect("plantillas/INVERSION_4_COMPLEMENTO_CONTRATO.docx");
    }

    private static void inspect(String path) {
        System.out.println("\n=== CONTENIDO DE: " + path + " ===");
        try (FileInputStream fis = new FileInputStream(path);
                XWPFDocument doc = new XWPFDocument(fis)) {

            // Leer Párrafos
            System.out.println("--- PÁRRAFOS ---");
            int limit = 10;
            int count = 0;
            for (XWPFParagraph p : doc.getParagraphs()) {
                if (count++ > limit)
                    break;
                String text = p.getText();
                if (text != null && !text.trim().isEmpty()) {
                    System.out.println("P[" + count + "] " + text);
                }
            }

            // Leer Tablas
            System.out.println("\n--- TABLAS ---");
            count = 0;
            for (XWPFTable table : doc.getTables()) {
                count++;
                System.out.println("Tabla #" + count);
                for (XWPFTableRow row : table.getRows()) {
                    StringBuilder rowText = new StringBuilder("| ");
                    for (XWPFTableCell cell : row.getTableCells()) {
                        rowText.append(cell.getText()).append(" | ");
                    }
                    System.out.println(rowText.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
