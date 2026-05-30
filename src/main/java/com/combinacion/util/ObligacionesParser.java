package com.combinacion.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

public class ObligacionesParser {
    
    public static class ObligacionActividad {
        public String obligacion;
        public String actividad;
        public ObligacionActividad(String o, String a) {
            this.obligacion = o;
            this.actividad = a;
        }
        public String getObligacion() { return obligacion; }
        public String getActividad() { return actividad; }
    }
    
    public static List<ObligacionActividad> decodificarConcepto(String conceptoDb, String actividadesContrato) {
        List<ObligacionActividad> lista = new ArrayList<>();
        
        if (conceptoDb != null && conceptoDb.trim().startsWith("[")) {
            try {
                JSONArray arr = new JSONArray(conceptoDb);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    lista.add(new ObligacionActividad(
                        obj.optString("obligacion", ""),
                        obj.optString("actividad", "")
                    ));
                }
                return lista;
            } catch (Exception e) {
                // Si falla el parseo JSON, caer al comportamiento por defecto
            }
        }
        
        // Si no hay datos (nuevo informe), extraemos las obligaciones del contrato
        List<String> obligaciones = parsearObligaciones(actividadesContrato);
        if (conceptoDb == null || conceptoDb.trim().isEmpty()) {
            for (String ob : obligaciones) {
                lista.add(new ObligacionActividad(ob, ""));
            }
        } else {
            // Es un informe antiguo con texto plano
            // Lo ponemos todo en la primera obligación, o creamos una "general"
            if (!obligaciones.isEmpty()) {
                lista.add(new ObligacionActividad("1. Obligaciones Generales", conceptoDb));
            } else {
                lista.add(new ObligacionActividad("Obligaciones", conceptoDb));
            }
        }
        
        return lista;
    }

    public static List<String> parsearObligaciones(String texto) {
        List<String> obligaciones = new ArrayList<>();
        if (texto == null || texto.trim().isEmpty()) {
            return obligaciones;
        }

        String[] partes = texto.split("(?=\\b\\d+\\.\\s)");
        for (String p : partes) {
            String clean = p.trim();
            if (!clean.isEmpty()) {
                obligaciones.add(clean);
            }
        }
        
        if (obligaciones.isEmpty() && !texto.trim().isEmpty()) {
            obligaciones.add(texto.trim());
        }
        
        return obligaciones;
    }
}

