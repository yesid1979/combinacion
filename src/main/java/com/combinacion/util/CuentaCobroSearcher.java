package com.combinacion.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;

public class CuentaCobroSearcher {
    public static void main(String[] args) throws Exception {
        String templatePath = "c:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\src\\main\\webapp\\plantillas\\CUENTA_COBRO.xlsx";
        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            templatePath = "c:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\doc\\2. DS-4121-0411 Cuota 6 -YESID PIEDRAHITA.xlsx";
            templateFile = new File(templatePath);
        }
        
        System.out.println("Reading " + templatePath);
        Workbook wb = new XSSFWorkbook(new FileInputStream(templateFile));
        Sheet sheet = wb.getSheetAt(0);
        
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String val = cell.getStringCellValue();
                    if (val.toLowerCase().contains("prestac")) {
                        System.out.println("Row " + row.getRowNum() + " Col " + cell.getColumnIndex() + " = " + val);
                    }
                }
            }
        }
        wb.close();
    }
}
