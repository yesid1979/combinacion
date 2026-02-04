package com.combinacion.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;

public class TemplateGenerator {

    // Main method for testing via CLI
    public static void main(String[] args) {
        String filePath = "doc/DESIGNACION_SUPERVISOR_CON APOYO.docx";
        File file = new File(filePath);

        if (!file.exists()) {
            file = new File(
                    "c:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\doc\\DESIGNACION_SUPERVISOR_CON APOYO.docx");
        }

        if (!file.exists()) {
            System.out.println("Error: Archivo no encontrado en: " + filePath);
            return;
        }

        System.out.println("Creando plantilla definitiva...");

        Map<String, String> replacements = new HashMap<>();

        // Contratista - Tabla
        replacements.put("ESTEFANY PALACIOS CÓRDOBA", "${NOMBRE_CONTRATISTA}");
        replacements.put("4121.010.26.1.001 - 2026", "${NUMERO_CONTRATO}");
        replacements.put(
                "Prestación de Servicios Profesionales Especializados brindando soporte en los asuntos jurídicos del despacho del Departamento Administrativo de Gestión Jurídica Pública.",
                "${OBJETO_CONTRACTUAL}"); // Exact match required
        replacements.put("4500395980", "${RPC_NUMERO}");

        // Fechas
        // Date within the table (RPC Date)
        replacements.put("enero 6 de 2026", "${RPC_FECHA}"); // CAUTION: This string appears in table AND body. We will
                                                             // handle context in loop if needed, but for now global
                                                             // replace.
        // If "enero 6 de 2026" is used for BOTH RPC Date and Letter Date in original,
        // replacing it with RPC_FECHA means Letter Date becomes RPC Date.
        // We probably want the Letter Date to be independent ($FECHA_DOCUMENTO).
        // Strategy: Replace "Santiago de Cali, enero 6 de 2026" specifically first.
        replacements.put("Santiago de Cali, enero 6 de 2026", "Santiago de Cali, ${FECHA_DOCUMENTO}");

        // Ordenador
        replacements.put("ANA CATALINA CASTRO LOZANO", "${NOMBRE_ORDENADOR}");
        replacements.put("Director Departamento Administrativo", "${CARGO_ORDENADOR}");
        replacements.put("Departamento Administrativo de Gestión Jurídica Pública", "${ORGANISMO_ORDENADOR}");

        // Supervisor
        replacements.put("CLAUDIA PATRICIA VARGAS OROZCO", "${NOMBRE_SUPERVISOR}");
        replacements.put("Subdirector de Defensa Judicial y Prevención del Daño Antijurídico ( E )",
                "${CARGO_SUPERVISOR}");

        // Proceso/Contrato Alterno (Link)
        replacements.put("4121.010.32.1.001- 2026", "${NUMERO_PROCESO_O_CONTRATO_ALT}");

        File templateDir = new File("plantillas");
        if (!templateDir.exists())
            templateDir.mkdir();

        try (FileInputStream fis = new FileInputStream(file);
                FileOutputStream fos = new FileOutputStream(
                        new File(templateDir, "DESIGNACION_SUPERVISOR_CON APOYO.docx"))) {

            generate(fis, replacements, fos);
            System.out.println("Plantilla generada exitosamente en plantillas/DESIGNACION_SUPERVISOR_CON APOYO.docx");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a document by replacing placeholders in the input stream and
     * writing to the output stream.
     */
    public static void generate(InputStream templateInputStream, Map<String, String> replacements,
            OutputStream outputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(templateInputStream)) {

            // Process Paragraphs
            for (XWPFParagraph para : document.getParagraphs()) {
                replaceInParagraph(para, replacements);
            }

            // Process Tables
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph para : cell.getParagraphs()) {
                            replaceInParagraph(para, replacements);
                        }
                    }
                }
            }

            // Process Headers (important if date is in header, though analysis showed body)
            XWPFHeaderFooterPolicy policy = document.getHeaderFooterPolicy();
            if (policy != null) {
                processHeader(policy.getDefaultHeader(), replacements);
                processHeader(policy.getFirstPageHeader(), replacements);
                processHeader(policy.getEvenPageHeader(), replacements);
            }

            // Process Footers
            if (policy != null) {
                processFooter(policy.getDefaultFooter(), replacements);
            }

            document.write(outputStream);
        }
    }

    private static void processHeader(XWPFHeader header, Map<String, String> replacements) {
        if (header == null)
            return;
        for (XWPFParagraph p : header.getParagraphs())
            replaceInParagraph(p, replacements);
        for (XWPFTable t : header.getTables()) {
            for (XWPFTableRow r : t.getRows()) {
                for (XWPFTableCell c : r.getTableCells()) {
                    for (XWPFParagraph p : c.getParagraphs())
                        replaceInParagraph(p, replacements);
                }
            }
        }
    }

    private static void processFooter(XWPFFooter footer, Map<String, String> replacements) {
        if (footer == null)
            return;
        for (XWPFParagraph p : footer.getParagraphs())
            replaceInParagraph(p, replacements);
        for (XWPFTable t : footer.getTables()) {
            for (XWPFTableRow r : t.getRows()) {
                for (XWPFTableCell c : r.getTableCells()) {
                    for (XWPFParagraph p : c.getParagraphs())
                        replaceInParagraph(p, replacements);
                }
            }
        }
    }

    private static void replaceInParagraph(XWPFParagraph para, Map<String, String> replacements) {
        String text = para.getText();
        if (text == null || text.isEmpty())
            return;

        boolean replaced = false;

        // Iterate through map to find matches
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            if (text.contains(entry.getKey())) {
                // Perform replacement
                // Simple strategy: replace text string, then wipe runs and set new text.
                // This destroys local formatting (bolding of specific words inside the
                // paragraph),
                // but preserves paragraph styles.

                String val = entry.getValue();
                text = text.replace(entry.getKey(), val);
                replaced = true;
            }
        }

        if (replaced) {
            // Heuristic: Start with the formatting of the first run
            String fontFamily = "Arial";
            int fontSize = 10;
            if (para.getRuns().size() > 0) {
                fontFamily = para.getRuns().get(0).getFontFamily();
                if (fontFamily == null)
                    fontFamily = "Arial";
                fontSize = para.getRuns().get(0).getFontSize();
                if (fontSize == -1)
                    fontSize = 11; // Default
            }

            while (para.getRuns().size() > 0) {
                para.removeRun(0);
            }
            XWPFRun newRun = para.createRun();
            newRun.setText(text);
            newRun.setFontFamily(fontFamily);
            newRun.setFontSize(fontSize);
            // newRun.setBold(true); // Can't guess this easily, assuming plain text for
            // replaced segments
        }
    }
}
