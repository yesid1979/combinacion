package com.combinacion.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Generador de plantillas base por copia directa.
 * 
 * Esta herramienta crea 4 copias idénticas del documento maestro con
 * placeholders.
 * El objetivo es que el usuario abra cada copia y elimine manualmente las
 * secciones
 * que no corresponden, garantizando así que el 100% del formato (encabezados,
 * logos, estilos) se preserve intacto.
 * 
 * @author Yesid Piedrahita
 * @version 2.0 (Estrategia Manual Segura)
 */
public class DocumentSplitter {

    public static void main(String[] args) {
        String inputFile = "doc/PLANTILLAS_TODAS_DAGJP_INVERSION_2026_CON_PLACEHOLDERS.docx";
        String outputDir = "plantillas/";

        try {
            System.out.println(repeat("=", 80));
            System.out.println("GENERANDO PLANTILLAS BASE (COPIA EXACTA)");
            System.out.println(repeat("=", 80));
            System.out.println("Estrategia: Copia idéntica para preservar formato completo.");
            System.out.println("Nota: Deberás abrir cada archivo y borrar las secciones sobrantes.\n");

            File sourceFile = new File(inputFile);
            File outDir = new File(outputDir);

            if (!outDir.exists()) {
                outDir.mkdirs();
            }

            if (!sourceFile.exists()) {
                System.err.println("❌ Error: No se encuentra el archivo maestro: " + inputFile);
                return;
            }

            // Generar las 4 copias
            copyFile(sourceFile, new File(outputDir + "INVERSION_1_ESTUDIOS_PREVIOS.docx"));
            copyFile(sourceFile, new File(outputDir + "INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx"));
            copyFile(sourceFile, new File(outputDir + "INVERSION_3_CERTIFICADO_IDONEIDAD.docx"));
            copyFile(sourceFile, new File(outputDir + "INVERSION_4_COMPLEMENTO_CONTRATO.docx"));

            System.out.println("\n" + repeat("=", 80));
            System.out.println("✅ PROCESO COMPLETADO");
            System.out.println("Las 4 plantillas se han creado en '" + outputDir + "'.");
            System.out.println("IMPORTANTE: Abre cada una y deja solo la sección correspondiente.");
            System.out.println(repeat("=", 80));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyFile(File source, File dest) throws Exception {
        System.out.print("Generando: " + dest.getName() + "... ");

        try (FileInputStream fis = new FileInputStream(source);
                FileOutputStream fos = new FileOutputStream(dest);
                FileChannel sourceChannel = fis.getChannel();
                FileChannel destChannel = fos.getChannel()) {

            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
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
