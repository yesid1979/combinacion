package com.combinacion.util;

import java.io.*;
import java.nio.charset.Charset;

public class TestCSV {
    public static void main(String[] args) {
        File file = new File(
                "c:\\Users\\Soporte y Desarrollo\\Documents\\NetBeansProjects\\combinacion\\doc\\MATRIZ PRESTADORES DE SERVICIOS 2026.csv");
        // Try ISO-8859-1 wich is typical for Windows CSVs
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"))) {
            String line = br.readLine();
            if (line != null) {
                String[] cols = line.split(";");
                System.out.println("Total Cols: " + cols.length);
                for (int i = 0; i < cols.length; i++) {
                    System.out.println(i + ": " + cols[i].replace("\n", " ").replace("\r", " "));
                }
            } else {
                System.out.println("File empty or read error.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
