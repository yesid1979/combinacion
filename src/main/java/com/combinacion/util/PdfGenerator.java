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
     * Genera PDF por lotes en una carpeta específica.
     */
    public static boolean convertBatchToPdf(File inputDir, File outputDir) {
        if (inputDir == null || !inputDir.exists() || !inputDir.isDirectory()) {
            return false;
        }
        
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            return convertBatchWithWordWindows(inputDir, outputDir != null ? outputDir : inputDir);
        } else {
            if (convertBatchWithLibreOfficeLinux(inputDir, outputDir != null ? outputDir : inputDir)) {
                return true;
            }
            return convertBatchWithJavaLibrary(inputDir, outputDir != null ? outputDir : inputDir);
        }
    }

    private static boolean runProcessSafely(ProcessBuilder pb, long timeoutSeconds) {
        try {
            pb.redirectErrorStream(true);
            Process p = pb.start();
            
            // Hilo demonio para consumir la salida estándar y evitar el bloqueo por búfer lleno del SO
            Thread gobbler = new Thread(() -> {
                try (InputStream is = p.getInputStream()) {
                    byte[] buffer = new byte[4096];
                    while (is.read(buffer) != -1) {
                        // Descartar la salida
                    }
                } catch (Exception e) {
                    // Ignorar error al cerrar o leer el stream
                }
            });
            gobbler.setDaemon(true);
            gobbler.start();
            
            boolean finished = p.waitFor(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                System.err.println("⚠️ El proceso excedió el límite de " + timeoutSeconds + " segundos. Se elimina forzadamente...");
                p.destroyForcibly();
                return false;
            }
            return p.exitValue() == 0;
        } catch (Exception e) {
            System.err.println("❌ Error al ejecutar el proceso del sistema de forma segura: " + e.getMessage());
            return false;
        }
    }

    private static boolean convertBatchWithWordWindows(File inputDir, File outputDir) {
        try {
            String psCommand = String.format(
                "$word = New-Object -ComObject Word.Application; " +
                "$word.Visible = $false; " +
                "$word.DisplayAlerts = 'wdAlertsNone'; " +
                "$files = Get-ChildItem -Path '%s' -Filter *.docx; " +
                "foreach ($file in $files) { " +
                "  try { " +
                "    $doc = $word.Documents.Open($file.FullName, $false, $true); " +
                "    $pdfName = Join-Path -Path '%s' -ChildPath ($file.BaseName + '.pdf'); " +
                "    $doc.SaveAs([ref]$pdfName, [ref]17); " +
                "    $doc.Close([ref]0); " +
                "  } catch { Write-Host $_.Exception.Message } " +
                "} " +
                "$word.Quit(); [System.Runtime.Interopservices.Marshal]::ReleaseComObject($word);",
                inputDir.getAbsolutePath(), outputDir.getAbsolutePath()
            );

            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", psCommand);
            runProcessSafely(pb, 180); // 3 minutos de límite
            
            // Check if any PDF was generated
            File[] pdfs = outputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
            if (pdfs != null && pdfs.length > 0) {
                return true;
            }
            System.err.println("⚠️ Word Windows por lotes no generó PDFs, intentando fallback Java...");
            return convertBatchWithJavaLibrary(inputDir, outputDir);
        } catch (Exception e) {
            System.err.println("⚠️ Falló motor Word Windows por lotes, intentando fallback Java...");
            return convertBatchWithJavaLibrary(inputDir, outputDir);
        }
    }

    private static boolean convertBatchWithLibreOfficeLinux(File inputDir, File outputDir) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "sh", "-c", "soffice --headless --convert-to pdf --outdir \"" + outputDir.getAbsolutePath() + "\" \"" + inputDir.getAbsolutePath() + "\"/*.docx"
            );
            
            return runProcessSafely(pb, 180);
        } catch (Exception e) {
            System.err.println("⚠️ Falló LibreOffice por lotes en Linux.");
            return false;
        }
    }

    private static boolean convertBatchWithJavaLibrary(File inputDir, File outputDir) {
        try {
            File[] files = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".docx"));
            if (files != null) {
                for (File docxFile : files) {
                    File pdfFile = new File(outputDir, docxFile.getName().replaceAll("(?i)\\.docx$", ".pdf"));
                    convertWithJavaLibrary(docxFile, pdfFile);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

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
            runProcessSafely(pb, 60); // 1 minuto de límite para archivo individual
            
            if (pdfFile.exists() && pdfFile.length() > 0) {
                return true;
            }
            System.err.println("⚠️ Motor Word Windows no generó el PDF, intentando fallback Java...");
            return convertWithJavaLibrary(docxFile, pdfFile);
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
            
            runProcessSafely(pb, 60);
            
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

    /**
     * Convierte un archivo Excel a PDF.
     */
    public static boolean convertExcelToPdf(File excelFile, File pdfFile) {
        if (excelFile == null || !excelFile.exists()) {
            return false;
        }

        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            return convertExcelWithWindows(excelFile, pdfFile);
        } else {
            return convertExcelWithLibreOfficeLinux(excelFile, pdfFile);
        }
    }

    private static boolean convertExcelWithWindows(File excelFile, File pdfFile) {
        try {
            String psCommand = String.format(
                "$excel = New-Object -ComObject Excel.Application; " +
                "$excel.Visible = $false; " +
                "$excel.DisplayAlerts = $false; " +
                "$wb = $excel.Workbooks.Open('%s'); " +
                "$wb.ExportAsFixedFormat(0, '%s'); " + 
                "$wb.Close($false); " +
                "$excel.Quit(); " +
                "[System.Runtime.Interopservices.Marshal]::ReleaseComObject($excel);",
                excelFile.getAbsolutePath(), pdfFile.getAbsolutePath()
            );

            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", psCommand);
            runProcessSafely(pb, 60);
            
            return pdfFile.exists() && pdfFile.length() > 0;
        } catch (Exception e) {
            System.err.println("⚠️ Falló motor Excel Windows: " + e.getMessage());
            return false;
        }
    }

    private static boolean convertExcelWithLibreOfficeLinux(File excelFile, File pdfFile) {
        try {
            String outDir = pdfFile.getParent();
            
            // Se usa un directorio de perfil temporal y limpio para cada conversión.
            // Esto evita el bug de BiDi (paréntesis invertidos) que ocurre cuando
            // LibreOffice reutiliza un perfil de usuario con configuraciones de idioma
            // o dirección de texto conflictivas.
            File tempProfile = new File(System.getProperty("java.io.tmpdir"), "lo_profile_" + Thread.currentThread().getId());
            tempProfile.mkdirs();
            
            ProcessBuilder pb = new ProcessBuilder(
                "soffice",
                "-env:UserInstallation=file://" + tempProfile.getAbsolutePath(),
                "--headless",
                "--norestore",
                "--nofirststartwizard",
                "--convert-to", "pdf",
                "--outdir", outDir,
                excelFile.getAbsolutePath()
            );
            
            // Forzar localización en español de Colombia para que el motor
            // de texto no aplique reglas BiDi incorrectas en el encabezado.
            pb.environment().put("LC_ALL", "es_CO.UTF-8");
            pb.environment().put("LANG", "es_CO.UTF-8");
            
            runProcessSafely(pb, 60);
            
            // Limpiar el perfil temporal después de la conversión
            try {
                deleteDirectory(tempProfile);
            } catch (Exception ignore) {}
            
            // LibreOffice genera el PDF en outDir con el mismo nombre base del xlsx
            String baseName = excelFile.getName().replaceAll("(?i)\\.xlsx?$", ".pdf");
            File generatedPdf = new File(outDir, baseName);
            if (generatedPdf.exists() && !generatedPdf.getAbsolutePath().equals(pdfFile.getAbsolutePath())) {
                generatedPdf.renameTo(pdfFile);
            }
            
            return pdfFile.exists() && pdfFile.length() > 0;
        } catch (Exception e) {
            System.err.println("⚠️ LibreOffice no encontrado o falló en Linux para Excel: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un directorio de forma recursiva.
     */
    private static void deleteDirectory(File dir) {
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) deleteDirectory(f);
                    else f.delete();
                }
            }
            dir.delete();
        }
    }

    /**
     * Une dos archivos PDF en un único archivo PDF.
     */
    public static boolean mergePdfs(File pdf1, File pdf2, File output) {
        if (pdf1 == null || !pdf1.exists() || pdf2 == null || !pdf2.exists()) {
            return false;
        }
        try {
            com.lowagie.text.Document document = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfCopy copy = new com.lowagie.text.pdf.PdfCopy(document, new java.io.FileOutputStream(output));
            document.open();
            
            com.lowagie.text.pdf.PdfReader reader1 = new com.lowagie.text.pdf.PdfReader(new java.io.FileInputStream(pdf1));
            for (int i = 1; i <= reader1.getNumberOfPages(); i++) {
                copy.addPage(copy.getImportedPage(reader1, i));
            }
            reader1.close();
            
            com.lowagie.text.pdf.PdfReader reader2 = new com.lowagie.text.pdf.PdfReader(new java.io.FileInputStream(pdf2));
            for (int i = 1; i <= reader2.getNumberOfPages(); i++) {
                copy.addPage(copy.getImportedPage(reader2, i));
            }
            reader2.close();
            
            document.close();
            return true;
        } catch (Exception e) {
            System.err.println("Error al unir PDFs: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


}
