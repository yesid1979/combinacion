package com.combinacion.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;

public class PdfGenerator {

    /**
     * Genera PDF detectando el sistema operativo para usar el mejor motor disponible.
     */
    public static boolean convertToPdf(File docxFile, File pdfFile) {
        if (docxFile == null || !docxFile.exists()) {
            return false;
        }

        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            return convertWithWordWindows(docxFile, pdfFile);
        } else {
            // Intentar con LibreOffice (Soffice) en Linux
            if (convertWithLibreOfficeLinux(docxFile, pdfFile)) {
                return true;
            }
            // Fallback al motor Java si LibreOffice no está instalado o falla
            return convertWithJavaLibrary(docxFile, pdfFile);
        }
    }

    /**
     * Motor para Windows: Usa PowerShell + Word para calidad perfecta.
     */
    private static boolean convertWithWordWindows(File docxFile, File pdfFile) {
        try {
            String psCommand = String.format(
                "$word = New-Object -ComObject Word.Application; " +
                "$doc = $word.Documents.Open('%s'); " +
                "$doc.SaveAs([ref]'%s', [ref]17); " +
                "$doc.Close(); $word.Quit();",
                docxFile.getAbsolutePath(), pdfFile.getAbsolutePath()
            );

            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", psCommand);
            Process p = pb.start();
            p.waitFor();
            
            return pdfFile.exists() && pdfFile.length() > 0;
        } catch (Exception e) {
            System.err.println("⚠️ Falló motor Word Windows, intentando fallback Java...");
            return convertWithJavaLibrary(docxFile, pdfFile);
        }
    }

    /**
     * Motor para Linux: Usa LibreOffice (soffice) en modo headless.
     */
    private static boolean convertWithLibreOfficeLinux(File docxFile, File pdfFile) {
        try {
            // LibreOffice guarda en el directorio de salida con el mismo nombre del archivo base
            String outDir = pdfFile.getParent();
            ProcessBuilder pb = new ProcessBuilder(
                "soffice", "--headless", "--convert-to", "pdf", 
                "--outdir", outDir, docxFile.getAbsolutePath()
            );
            
            Process p = pb.start();
            p.waitFor();
            
            // Soffice genera un archivo con el mismo nombre pero .pdf
            File generatedPdf = new File(docxFile.getAbsolutePath().replace(".docx", ".pdf"));
            if (generatedPdf.exists() && !generatedPdf.getAbsolutePath().equals(pdfFile.getAbsolutePath())) {
                generatedPdf.renameTo(pdfFile);
            }
            
            return pdfFile.exists() && pdfFile.length() > 0;
        } catch (Exception e) {
            System.err.println("⚠️ LibreOffice no encontrado o falló en Linux.");
            return false;
        }
    }

    /**
     * Motor Fallback: XDocReport (Java nativo).
     * Solo se usa si los motores superiores no están disponibles.
     */
    private static boolean convertWithJavaLibrary(File docxFile, File pdfFile) {
        try (InputStream is = new FileInputStream(docxFile);
             OutputStream os = new FileOutputStream(pdfFile);
             XWPFDocument document = new XWPFDocument(is)) {
            
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(document, os, options);
            
            return pdfFile.exists();
        } catch (Exception e) {
            System.err.println("❌ Fallo total en PdfGenerator: " + e.getMessage());
            return false;
        }
    }
}
