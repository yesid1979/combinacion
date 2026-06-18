package com.combinacion.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class TestReplace {
    public static void main(String[] args) throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("${NOMBRE_PROYECTO}", "YESID PIEDRAHITA");
        replacements.put("${CARGO_PROYECTO}", "INGENIERO");
        replacements.put("${NOMBRE_REVISO}", "JUAN PEREZ");
        replacements.put("${CARGO_REVISO}", "ABOGADO");

        String in = "c:/Users/yesid.piedrahita/Documents/NetBeansProjects/combinacion/plantillas/DESIGNACION_SUPERVISOR_CON APOYO.docx";
        String out = "c:/Users/yesid.piedrahita/Documents/NetBeansProjects/combinacion/scratch/TEST_OUT.docx";

        try (FileInputStream fis = new FileInputStream(in);
             FileOutputStream fos = new FileOutputStream(out)) {
            TemplateGenerator.generate(fis, replacements, fos);
        }
        System.out.println("DONE");
    }
}
