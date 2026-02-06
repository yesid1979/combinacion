package com.combinacion.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

public class DocxAnalyzer {

    public static void main(String[] args) {
        String filePath = "doc/PLANTILLAS_TODAS_DAGJP_INVERSION_2026.docx";
        File file = new File(filePath);

        if (!file.exists()) {
            file = new File(
                    "c:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\doc\\PLANTILLAS_TODAS_DAGJP_INVERSION_2026.docx");
        }

        if (!file.exists()) {
            System.out.println("Error: Archivo no encontrado en: " + filePath);
            return;
        }

        try (FileInputStream fis = new FileInputStream(file);
                XWPFDocument document = new XWPFDocument(fis);
                PrintWriter writer = new PrintWriter("analysis_output.txt", "UTF-8")) {

            writer.println("=== ANALISIS DE CONTENIDO: " + file.getName() + " ===");

            // Headers
            writer.println("\n--- HEADER DEFAULT ---");
            printHeader(document.getHeaderFooterPolicy().getDefaultHeader(), writer);

            // Body
            writer.println("\n--- CUERPO DEL DOCUMENTO ---");
            for (XWPFParagraph para : document.getParagraphs()) {
                writer.println(para.getText());
            }

            // Tables
            writer.println("\n--- TABLAS ---");
            List<XWPFTable> tables = document.getTables();
            for (int i = 0; i < tables.size(); i++) {
                writer.println("Tabla #" + (i + 1));
                XWPFTable table = tables.get(i);
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        writer.print("| " + cell.getText() + " ");
                    }
                    writer.println("|");
                }
            }

            // Footers
            writer.println("\n--- FOOTER DEFAULT ---");
            printFooter(document.getHeaderFooterPolicy().getDefaultFooter(), writer);

            System.out.println("Analisis completado. Resultado en analysis_output.txt");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printHeader(XWPFHeader header, PrintWriter writer) {
        if (header != null) {
            for (XWPFParagraph p : header.getParagraphs()) {
                writer.println(p.getText());
            }
            for (XWPFTable t : header.getTables()) {
                for (XWPFTableRow r : t.getRows()) {
                    for (XWPFTableCell c : r.getTableCells()) {
                        writer.print("| " + c.getText() + " ");
                    }
                    writer.println("|");
                }
            }
        }
    }

    private static void printFooter(XWPFFooter footer, PrintWriter writer) {
        if (footer != null) {
            for (XWPFParagraph p : footer.getParagraphs()) {
                writer.println(p.getText());
            }
            for (XWPFTable t : footer.getTables()) {
                for (XWPFTableRow r : t.getRows()) {
                    for (XWPFTableCell c : r.getTableCells()) {
                        writer.print("| " + c.getText() + " ");
                    }
                    writer.println("|");
                }
            }
        }
    }
}
