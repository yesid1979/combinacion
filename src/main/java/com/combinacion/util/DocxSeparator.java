package com.combinacion.util;

import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.util.List;

public class DocxSeparator {

    public static void main(String[] args) {
        String inputPath = "doc/PLANTILLAS_TODAS_DAGJP_INVERSION_2026.docx";
        File file = new File(inputPath);
        if (!file.exists()) {
            inputPath = "c:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\doc\\PLANTILLAS_TODAS_DAGJP_INVERSION_2026.docx";
        }

        try {
            splitAndCleanDocument(inputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void splitAndCleanDocument(String inputPath) throws IOException {
        int doc2StartIndex = -1;
        int doc3StartIndex = -1;
        int doc4StartIndex = -1;

        FileInputStream fis = new FileInputStream(inputPath);
        XWPFDocument docScan = new XWPFDocument(fis);
        List<IBodyElement> elements = docScan.getBodyElements();

        for (int i = 0; i < elements.size(); i++) {
            IBodyElement element = elements.get(i);
            String text = "";
            if (element instanceof XWPFParagraph) {
                text = ((XWPFParagraph) element).getText();
            } else if (element instanceof XWPFTable) {
                text = ((XWPFTable) element).getText();
            }
            String upperText = text.toUpperCase();

            // Find Doc 2 Start
            if (doc2StartIndex == -1 && upperText.contains("VERIFICACIÓN DE CUMPLIMIENTO DE REQUISITOS")) {
                doc2StartIndex = i;
            }

            // Find Doc 3 Start
            if (doc3StartIndex == -1 && text.contains("De conformidad con la verificación de requisitos realizada")) {
                doc3StartIndex = i;
                // Backtrack for Date "Santiago de Cali"
                if (i > 5) {
                    for (int j = 1; j <= 5; j++) {
                        IBodyElement prev = elements.get(i - j);
                        String prevText = (prev instanceof XWPFParagraph) ? ((XWPFParagraph) prev).getText() : "";
                        if (prevText.contains("Santiago de Cali")) {
                            doc3StartIndex = i - j;
                            break;
                        }
                    }
                }
            }

            // Find Doc 4 Start
            // Must be a header, so it likely starts with the phrase
            if (doc4StartIndex == -1 && i > 300 && upperText.trim().startsWith("CONTRATO DE PRESTACIÓN DE SERVICIOS")) {
                doc4StartIndex = i;
            }
        }
        docScan.close();
        fis.close();

        System.out.println(
                "Split Points: Doc2=" + doc2StartIndex + ", Doc3=" + doc3StartIndex + ", Doc4=" + doc4StartIndex);

        if (doc2StartIndex == -1 || doc3StartIndex == -1 || doc4StartIndex == -1) {
            System.err.println("Could not find all split points. Aborting.");
            return;
        }

        // Generate the 4 Docs
        createTemplate(inputPath, 0, doc2StartIndex, "plantillas/1_ESTUDIOS_PREVIOS.docx");
        createTemplate(inputPath, doc2StartIndex, doc3StartIndex, "plantillas/2_VERIFICACION_CUMPLIMIENTO.docx");
        createTemplate(inputPath, doc3StartIndex, doc4StartIndex, "plantillas/3_CERTIFICADO_IDONEIDAD.docx");
        createTemplate(inputPath, doc4StartIndex, elements.size(), "plantillas/4_COMPLEMENTO_CONTRATO.docx");
    }

    private static void createTemplate(String inputPath, int startIdx, int endIdx, String outputPath)
            throws IOException {
        System.out.println("Creating " + outputPath + " (Range: " + startIdx + " to " + endIdx + ")");
        FileInputStream fis = new FileInputStream(inputPath);
        XWPFDocument doc = new XWPFDocument(fis);

        int size = doc.getBodyElements().size();

        // Remove After
        for (int i = size - 1; i >= endIdx; i--) {
            doc.removeBodyElement(i);
        }
        // Remove Before
        for (int i = startIdx - 1; i >= 0; i--) {
            doc.removeBodyElement(i);
        }

        // Replace Hardcoded Values with Placeholders
        replaceInDoc(doc, "NINA JHOANA SOTO BUSTAMANTE", "${NOMBRE_CONTRATISTA}");
        replaceInDoc(doc, "1.130.648.239", "${CEDULA}");
        replaceInDoc(doc, "CLAUDIA PATRICIA VARGAS OROZCO", "${NOMBRE_SUPERVISOR}");
        replaceInDoc(doc, "Subdirectora de Defensa Judicial y Prevención del Daño Antijurídico", "${CARGO_SUPERVISOR}");
        replaceInDoc(doc, "4121.010.32.1.076-2026", "${NUMERO_PROCESO}"); // Proceso logic might need check
        replaceInDoc(doc, "4121.010.26.1.076-2026", "${NUMERO_CONTRATO}");
        replaceInDoc(doc, "$19220000", "${VALOR_TOTAL}");
        replaceInDoc(doc, "$ 19220000", "${VALOR_TOTAL}"); // space var
        replaceInDoc(doc, "$4805000", "${VALOR_MENSUAL}");
        replaceInDoc(doc, "$ 4805000", "${VALOR_MENSUAL}");

        FileOutputStream fos = new FileOutputStream(outputPath);
        doc.write(fos);
        fos.close();
        doc.close();
        fis.close();
        System.out.println("Saved " + outputPath);
    }

    private static void replaceInDoc(XWPFDocument doc, String target, String replacement) {
        // Paragraphs
        for (XWPFParagraph p : doc.getParagraphs()) {
            replaceInParagraph(p, target, replacement);
        }
        // Tables
        for (XWPFTable tbl : doc.getTables()) {
            for (XWPFTableRow row : tbl.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        replaceInParagraph(p, target, replacement);
                    }
                }
            }
        }
    }

    private static void replaceInParagraph(XWPFParagraph p, String target, String replacement) {
        String text = p.getText();
        if (text != null && text.contains(target)) {
            // Simple replace for Runs (imperfect but functional for simple docs)
            List<XWPFRun> runs = p.getRuns();
            if (runs != null) {
                // Determine if target is split across runs.
                // Creating a new single run to replace all content is safer for "template"
                // generation.
                StringBuilder sb = new StringBuilder();
                for (XWPFRun r : runs) {
                    sb.append(r.getText(0));
                }
                String paragraphText = sb.toString();
                if (paragraphText.contains(target)) {
                    String newText = paragraphText.replace(target, replacement);
                    // Clear existing runs
                    for (int i = runs.size() - 1; i >= 0; i--) {
                        p.removeRun(i);
                    }
                    // Add new run
                    XWPFRun newRun = p.createRun();
                    newRun.setText(newText);
                    // Copy style from first run? (Simplified: no, assume default or inherit)
                    // If style preservation is critical, this primitive replace is risky.
                    // But standard for quick placeholder injection.
                }
            }
        }
    }
}
