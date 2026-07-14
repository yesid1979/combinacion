package com.combinacion.util;

import java.io.File;
import java.io.FileInputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReaderTest {
    public static void main(String[] args) {
        try {
            File file = new File("plantillas/CUENTA_COBRO.xlsx");
            if (!file.exists()) {
                System.out.println("No file: " + file.getAbsolutePath());
                return;
            }
            FileInputStream fis = new FileInputStream(file);
            Workbook wb = new XSSFWorkbook(fis);
            Sheet sheet = wb.getSheetAt(0);
            Row row = sheet.getRow(16); // Row 17 is index 16
            
            System.out.println("Reading Row 17");
            if (row == null) {
                 System.out.println("Row is null");
            } else {
                 for (int i = 0; i <= 15; i++) {
                     Cell cell = row.getCell(i);
                     if (cell != null) {
                         System.out.println("Col " + i + " : " + cell.toString());
                     } else {
                         System.out.println("Col " + i + " : null");
                     }
                 }
            }
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
