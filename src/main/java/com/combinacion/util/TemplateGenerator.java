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

    // Main method for testing via CLI and creating templates
    public static void main(String[] args) {
        createConApoyoTemplate();
        createSinApoyoTemplate();
        createDesignacionEstructuradoresTemplate();
    }

    public static void createDesignacionEstructuradoresTemplate() {
        String filePath = "doc/DESIGNACION_RESPONSABLES_PARA_ESTRUCTURAR.docx";
        File file = new File(filePath);

        if (!file.exists()) {
            file = new File(
                    "c:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\doc\\DESIGNACION_RESPONSABLES_PARA_ESTRUCTURAR.docx");
        }

        if (!file.exists()) {
            System.out.println("Error: Archivo ESTRUCTURADORES no encontrado en: " + filePath);
            return;
        }

        System.out.println("Creando plantilla ESTRUCTURADORES...");

        Map<String, String> replacements = new HashMap<>();

        // Proceso
        replacements.put("4121.010.32.1.038-2026", "${NUMERO_PROCESO}");

        // Objeto (Long string match)
        String objeto = "Prestación de servicios profesionales como Abogado llevando a cabo actividades de representación judicial y de seguimiento de incidentes de desacatos en el Departamento Administrativo de Gestión Jurídica Pública en el marco del proyecto de inversión denominado \"Fortalecimiento del Ciclo de Defensa Jurídica y de la Política de Mejora Normativa del Distrito Especial de Santiago de Cali BP-26005460\"";
        replacements.put(objeto, "${OBJETO_CONTRACTUAL}");

        // Ordenador
        replacements.put("ANA CATALINA CASTRO LOZANO", "${NOMBRE_ORDENADOR}");
        replacements.put("Director Departamento Administrativo", "${CARGO_ORDENADOR}");

        // Estructurador JURIDICO (Estefany)
        replacements.put("ESTEFANY PALACIOS CÓRDOBA", "${NOMBRE_ESTRUCTURADOR_JURIDICO}");
        replacements.put("Abogada contratista", "${CARGO_ESTRUCTURADOR_JURIDICO}");
        replacements.put("Abogada Contratista", "${CARGO_ESTRUCTURADOR_JURIDICO}"); // Case variant

        // Estructurador TECNICO (Claudia)
        replacements.put("CLAUDIA PATRICIA VARGAS OROZCO", "${NOMBRE_ESTRUCTURADOR_TECNICO}");
        replacements.put("Subdirector de Defensa Judicial y Prevención del Daño Antijurídico",
                "${CARGO_ESTRUCTURADOR_TECNICO}");

        // Estructurador FINANCIERO (Maria Eugenia)
        replacements.put("MARÍA EUGENIA GONZÁLEZ ESPINOSA", "${NOMBRE_ESTRUCTURADOR_FINANCIERO}");
        replacements.put("Profesional Universitario", "${CARGO_ESTRUCTURADOR_FINANCIERO}");

        // Dates
        // "06 de enero de 2026" -> Generic Date
        replacements.put("06 de enero de 2026", "${FECHA_DOCUMENTO}");
        // Context specific long date
        replacements.put("seis (06) días del mes de enero de (2026)", "${FECHA_DOCUMENTO_LETRAS}");

        File templateDir = new File("plantillas");
        if (!templateDir.exists())
            templateDir.mkdir();

        try (FileInputStream fis = new FileInputStream(file);
                FileOutputStream fos = new FileOutputStream(
                        new File(templateDir, "DESIGNACION_RESPONSABLES_PARA_ESTRUCTURAR.docx"))) {

            generate(fis, replacements, fos);
            System.out.println(
                    "Plantilla ESTRUCTURADORES generada exitosamente en plantillas/DESIGNACION_RESPONSABLES_PARA_ESTRUCTURAR.docx");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createConApoyoTemplate() {
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

        System.out.println("Creando plantilla CON APOYO...");

        Map<String, String> replacements = new HashMap<>();

        // Contratista - Tabla
        replacements.put("ESTEFANY PALACIOS CÓRDOBA", "${NOMBRE_CONTRATISTA}");
        replacements.put("4121.010.26.1.001 - 2026", "${NUMERO_CONTRATO}");
        replacements.put(
                "Prestación de Servicios Profesionales Especializados brindando soporte en los asuntos jurídicos del despacho del Departamento Administrativo de Gestión Jurídica Pública.",
                "${OBJETO_CONTRACTUAL}");
        replacements.put("4500395980", "${RPC_NUMERO}");

        // Fechas
        replacements.put("Santiago de Cali, enero 6 de 2026", "Santiago de Cali, ${FECHA_DOCUMENTO}");

        // This specific string in the table
        replacements.put("Fecha Registro presupuestal de compromiso RPC", "TEMPORAL_KEY_RPC");
        // We do this to avoid replacing "enero 6..." inside this line if it exists,
        // though looking at analysis:
        // | Fecha Registro presupuestal de compromiso RPC | enero 6 de 2026 |
        // It's in a separate cell.

        replacements.put("enero 6 de 2026", "${RPC_FECHA}"); // Table

        // Ordenador
        replacements.put("ANA CATALINA CASTRO LOZANO", "${NOMBRE_ORDENADOR}");
        replacements.put("Director Departamento Administrativo", "${CARGO_ORDENADOR}");
        replacements.put("Departamento Administrativo de Gestión Jurídica Pública", "${ORGANISMO_ORDENADOR}");

        // Supervisor
        replacements.put("CLAUDIA PATRICIA VARGAS OROZCO", "${NOMBRE_SUPERVISOR}");
        replacements.put("Subdirector de Defensa Judicial y Prevención del Daño Antijurídico ( E )",
                "${CARGO_SUPERVISOR}");

        // Restore table key if needed or just handled by cell separation.

        // Proceso/Contrato Alterno (Link)
        replacements.put("4121.010.32.1.001- 2026", "${NUMERO_PROCESO_O_CONTRATO_ALT}");

        // Fechas Firma (Supervisor y Apoyo)
        replacements.put("Fecha: 7 noviembre de 2026", "Fecha: ${FECHA_RPC_SUPERVISOR}");

        // Apoyo a la Supervision
        replacements.put("SINDY MARIEL MENA GRUESSO", "${NOMBRE_APOYO}");

        File templateDir = new File("plantillas");
        if (!templateDir.exists())
            templateDir.mkdir();

        try (FileInputStream fis = new FileInputStream(file);
                FileOutputStream fos = new FileOutputStream(
                        new File(templateDir, "DESIGNACION_SUPERVISOR_CON APOYO.docx"))) {

            generate(fis, replacements, fos);
            System.out.println("Plantilla CON APOYO generada exitosamente.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createSinApoyoTemplate() {
        // Typo in original filename
        String filePath = "doc/DESIGANCION_SUPERVISOR_SIN_APOYO.docx";
        File file = new File(filePath);

        if (!file.exists()) {
            file = new File(
                    "c:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\doc\\DESIGANCION_SUPERVISOR_SIN_APOYO.docx");
        }

        if (!file.exists()) {
            System.out.println("Error: Archivo SIN APOYO no encontrado en: " + filePath);
            return;
        }

        System.out.println("Creando plantilla SIN APOYO...");

        try (FileInputStream fis = new FileInputStream(file);
                XWPFDocument document = new XWPFDocument(fis)) {

            // Custom Logic for Context-Aware Replacement
            boolean foundNotificado = false;

            // 1. Process Body Paragraphs
            for (XWPFParagraph para : document.getParagraphs()) {
                String text = para.getText();
                if (text.contains("Notificado:")) {
                    foundNotificado = true;
                }

                // General replacements
                replaceInParagraphCustom(para, false); // General

                // Context aware
                if (foundNotificado) {
                    replaceText(para, "ANA CATALINA CASTRO LOZANO", "${NOMBRE_SUPERVISOR}");
                    // Only replace Cargo if it matches the supervisor's context, but here it is
                    // same string.
                    replaceText(para, "Director Departamento Administrativo", "${CARGO_SUPERVISOR}");

                    // Date in signature
                    if (text.contains("Fecha:") && text.contains("enero 6 de 2026")) {
                        replaceText(para, "enero 6 de 2026", "${FECHA_RPC_SUPERVISOR}");
                    }
                } else {
                    replaceText(para, "ANA CATALINA CASTRO LOZANO", "${NOMBRE_ORDENADOR}");
                    replaceText(para, "Director Departamento Administrativo", "${CARGO_ORDENADOR}");
                    replaceText(para, "Departamento Administrativo de Gestión Jurídica Pública",
                            "${ORGANISMO_ORDENADOR}");
                }
            }

            // 2. Process Tables (Assume NO context conflict in tables for this specific
            // doc)
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph para : cell.getParagraphs()) {
                            replaceInParagraphCustom(para, false);
                        }
                    }
                }
            }

            // 3. Headers/Footers (Assume Ordenador context)
            XWPFHeaderFooterPolicy policy = document.getHeaderFooterPolicy();
            if (policy != null) {
                processHeaderCustom(policy.getDefaultHeader());
                // Footer
                processFooterCustom(policy.getDefaultFooter());
            }

            File templateDir = new File("plantillas");
            if (!templateDir.exists())
                templateDir.mkdir();

            // Save correct filename
            try (FileOutputStream fos = new FileOutputStream(
                    new File(templateDir, "DESIGNACION_SUPERVISOR_SIN_APOYO.docx"))) {
                document.write(fos);
                System.out.println(
                        "Plantilla SIN APOYO generada exitosamente en plantillas/DESIGNACION_SUPERVISOR_SIN_APOYO.docx");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void replaceInParagraphCustom(XWPFParagraph para, boolean isContextAware) {
        // Map of global replacements for SIN APOYO
        Map<String, String> globalReplacements = new HashMap<>();
        globalReplacements.put("ESTEFANY PALACIOS CÓRDOBA", "${NOMBRE_CONTRATISTA}");
        globalReplacements.put("4121.010.26.1.001 - 2026", "${NUMERO_CONTRATO}");
        globalReplacements.put(
                "Prestación de Servicios Profesionales Especializados brindando soporte en los asuntos jurídicos del despacho del Departamento Administrativo de Gestión Jurídica Pública.",
                "${OBJETO_CONTRACTUAL}");
        globalReplacements.put("4500395980", "${RPC_NUMERO}");
        globalReplacements.put("4121.010.32.1.001- 2026", "${NUMERO_PROCESO_O_CONTRATO_ALT}");
        globalReplacements.put("Santiago de Cali, enero 6 de 2026", "Santiago de Cali, ${FECHA_DOCUMENTO}");

        // RPC Date in table: "enero 6 de 2026".
        // Note: Signature date handled in main loop.
        // We need to be careful not to replace Signature date if we didn't handle it
        // yet.
        // But main loop handles signature date via "Fecha: ...".
        // Table date is just "enero 6 de 2026".
        if (!para.getText().contains("Santiago de Cali") && !para.getText().contains("Fecha:")) {
            globalReplacements.put("enero 6 de 2026", "${RPC_FECHA}");
        }

        replaceInParagraph(para, globalReplacements);
    }

    // Helper to replace specific text in paragraph
    private static void replaceText(XWPFParagraph para, String target, String replacement) {
        Map<String, String> map = new HashMap<>();
        map.put(target, replacement);
        replaceInParagraph(para, map);
    }

    private static void processHeaderCustom(XWPFHeader header) {
        if (header == null)
            return;
        for (XWPFParagraph p : header.getParagraphs()) {
            // Assume Header is always Ordenador in this doc
            replaceText(p, "ANA CATALINA CASTRO LOZANO", "${NOMBRE_ORDENADOR}");
            replaceText(p, "Director Departamento Administrativo", "${CARGO_ORDENADOR}");
            replaceText(p, "Departamento Administrativo de Gestión Jurídica Pública", "${ORGANISMO_ORDENADOR}");
        }
    }

    private static void processFooterCustom(XWPFFooter footer) {
        if (footer == null)
            return;
        // Logic if footer has text to replace
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
