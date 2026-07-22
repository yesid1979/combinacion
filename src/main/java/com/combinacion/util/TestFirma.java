package com.combinacion.util;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import java.io.File;
import java.io.FileInputStream;

public class TestFirma {
    public static void main(String[] args) {
        try {
            File file = new File("src/main/webapp/plantillas/CUENTA_COBRO.xlsx");
            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            Sheet sheet = wb.getSheetAt(0);
            
            System.out.println("Merged Regions:");
            for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                CellRangeAddress region = sheet.getMergedRegion(i);
                if (region.getFirstRow() < 12) {
                    System.out.println("Row " + region.getFirstRow() + "-" + region.getLastRow() + ", Col " + region.getFirstColumn() + "-" + region.getLastColumn() + " (" + region.formatAsString() + ")");
                }
            }
            fis.close();
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
