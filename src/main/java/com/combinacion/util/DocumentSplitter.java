package com.combinacion.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import org.apache.poi.xwpf.usermodel.*;

/**
 * Generador de plantillas con borrado quirúrgico secuencial.
 * Asume que el documento tiene las secciones en orden:
 * 1. Estudios Previos
 * 2. Verificación
 * 3. Certificado
 * 4. Complemento
 */
public class DocumentSplitter {

    public static void main(String[] args) {
        String inputFile = "doc/PLANTILLAS_TODAS_DAGJP_INVERSION_2026_CON_PLACEHOLDERS.docx";
        String outputDir = "plantillas/";

        try {
            System.out.println(repeat("=", 80));
            System.out.println("GENERANDO PLANTILLAS (BORRADO SECUENCIAL)");
            System.out.println(repeat("=", 80));

            // Análisis de estructura secuencial
            List<SectionRange> sections = analyzeSequentialSections(inputFile);

            if (sections.isEmpty()) {
                System.out.println("❌ Error: No se encontraron secciones.");
                return;
            }

            System.out.println("Secciones encontradas: " + sections.size());

            // Asumimos el orden estándar
            if (sections.size() >= 1)
                generateTrimmedFile(inputFile, outputDir + "INVERSION_1_ESTUDIOS_PREVIOS.docx", sections.get(0));
            if (sections.size() >= 2)
                generateTrimmedFile(inputFile, outputDir + "INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx",
                        sections.get(1));
            if (sections.size() >= 3)
                generateTrimmedFile(inputFile, outputDir + "INVERSION_3_CERTIFICADO_IDONEIDAD.docx", sections.get(2));
            if (sections.size() >= 4)
                generateTrimmedFile(inputFile, outputDir + "INVERSION_4_COMPLEMENTO_CONTRATO.docx", sections.get(3));

            System.out.println("\n" + repeat("=", 80));
            System.out.println("✅ PROCESO COMPLETADO");
            System.out.println(repeat("=", 80));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class SectionRange {
        int start;
        int end;

        public SectionRange(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private static List<SectionRange> analyzeSequentialSections(String inputPath) throws Exception {
        List<SectionRange> sections = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(inputPath);
                XWPFDocument doc = new XWPFDocument(fis)) {

            List<IBodyElement> elements = doc.getBodyElements();
            int currentStart = 0;

            for (int i = 0; i < elements.size(); i++) {
                IBodyElement elem = elements.get(i);
                boolean isSectionBreak = false;

                if (elem instanceof XWPFParagraph) {
                    XWPFParagraph p = (XWPFParagraph) elem;
                    // Detectar salto de sección
                    if (p.getCTP() != null && p.getCTP().getPPr() != null && p.getCTP().getPPr().getSectPr() != null) {
                        isSectionBreak = true;
                    }
                }

                // El último elemento siempre cierra la última sección
                if (i == elements.size() - 1) {
                    isSectionBreak = true;
                }

                if (isSectionBreak) {
                    sections.add(new SectionRange(currentStart, i));
                    currentStart = i + 1;
                }
            }
        }
        return sections;
    }

    private static void generateTrimmedFile(String inputPath, String outputPath, SectionRange target) throws Exception {
        System.out.print("Generando " + new File(outputPath).getName() + "... ");

        try (FileInputStream fis = new FileInputStream(inputPath);
                XWPFDocument doc = new XWPFDocument(fis)) {

            // Borrar después (reverse loop)
            int total = doc.getBodyElements().size();
            for (int i = total - 1; i > target.end; i--) {
                doc.removeBodyElement(i);
            }

            // Borrar antes (reverse loop)
            for (int i = target.start - 1; i >= 0; i--) {
                doc.removeBodyElement(i);
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                doc.write(fos);
            }
            System.out.println("OK");
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
