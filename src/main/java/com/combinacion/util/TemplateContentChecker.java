package com.combinacion.util;

import java.io.File;
import java.io.FileInputStream;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

public class TemplateContentChecker {
    public static void main(String[] args) {
        String templateName = "plantillas/INVERSION_3_CERTIFICADO_IDONEIDAD.docx";
        File file = new File(templateName);
        if (!file.exists()) {
             file = new File("c:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\plantillas\\INVERSION_3_CERTIFICADO_IDONEIDAD.docx");
        }
        
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {
            
            System.out.println("--- ALL TABLES CONTENT ---");
            for (XWPFTable t : doc.getTables()) {
                System.out.println("New Table:");
                for (XWPFTableRow r : t.getRows()) {
                    for (XWPFTableCell c : r.getTableCells()) {
                         System.out.println("CELL: [" + c.getText().trim() + "]");
                    }
                    System.out.println("--- End of Row ---");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
