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
        List<String> obligacionesContrato = parsearObligaciones(actividadesContrato);
        
        if (conceptoDb != null && conceptoDb.trim().startsWith("[")) {
            try {
                JSONArray arr = new JSONArray(conceptoDb);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String obligacionDb = obj.optString("obligacion", "");
                    String actividadDb = obj.optString("actividad", "");
                    
                    // La redacción oficial de las obligaciones proviene del contrato.
                    // Si el contrato tiene la obligación para este índice, se usa para evitar que comillas u otros caracteres
                    // hayan truncado el texto de la obligación guardado en el JSON.
                    String obligacionFinal = obligacionDb;
                    if (i < obligacionesContrato.size() && !obligacionesContrato.get(i).trim().isEmpty()) {
                        String obCon = obligacionesContrato.get(i).trim();
                        if (obligacionDb == null || obligacionDb.trim().isEmpty() || obCon.startsWith(obligacionDb.trim()) || !obligacionDb.equals(obCon)) {
                            obligacionFinal = obCon;
                        }
                    }
                    
                    lista.add(new ObligacionActividad(obligacionFinal, actividadDb));
                }
                
                // Si el contrato tiene más obligaciones que las registradas previamente, agregarlas vacías
                if (lista.size() < obligacionesContrato.size()) {
                    for (int i = lista.size(); i < obligacionesContrato.size(); i++) {
                        lista.add(new ObligacionActividad(obligacionesContrato.get(i), ""));
                    }
                }
                
                return lista;
            } catch (Exception e) {
                // Si falla el parseo JSON, caer al comportamiento por defecto
            }
        }
        
        // Si no hay datos (nuevo informe), extraemos las obligaciones del contrato
        if (conceptoDb == null || conceptoDb.trim().isEmpty()) {
            for (String ob : obligacionesContrato) {
                lista.add(new ObligacionActividad(ob, ""));
            }
        } else {
            // Es un informe antiguo con texto plano
            // Lo ponemos todo en la primera obligación, o creamos una "general"
            if (!obligacionesContrato.isEmpty()) {
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

        String[] partes = texto.split("(?=\\b\\d{1,2}\\.(?:[\\sA-Za-záéíóúÁÉÍÓÚñÑ]|$))");
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

