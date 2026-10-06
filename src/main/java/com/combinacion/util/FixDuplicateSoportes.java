package com.combinacion.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.json.JSONObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Script de saneamiento masivo para eliminar duplicados acumulados en soportes_json
 * de todos los informes de supervisión existentes en la base de datos,
 * incluyendo tanto documentos únicos (paz y salvo, rut, etc.) como evidencias repetidas en actividades.
 */
public class FixDuplicateSoportes {

    private static final Pattern EVIDENCIA_PATTERN = Pattern.compile("^(evidencia_\\d+_\\d+).*");

    public static void main(String[] args) {
        System.out.println("=== INICIANDO SANEAMIENTO MASIVO DE DOCUMENTOS Y EVIDENCIAS DUPLICADAS ===");
        int totalProcesados = 0;
        int totalModificados = 0;

        String selectSql = "SELECT id, soportes_json FROM informes_supervision WHERE soportes_json IS NOT NULL AND soportes_json != ''";
        String updateSql = "UPDATE informes_supervision SET soportes_json = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psSelect = conn.prepareStatement(selectSql);
             ResultSet rs = psSelect.executeQuery();
             PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {

            while (rs.next()) {
                totalProcesados++;
                int id = rs.getInt("id");
                String soportesJsonStr = rs.getString("soportes_json");

                if (soportesJsonStr == null || soportesJsonStr.trim().isEmpty()) {
                    continue;
                }

                try {
                    JSONObject originalObj = new JSONObject(soportesJsonStr);
                    boolean teniaDuplicados = false;
                    
                    JSONObject cleanedObj = new JSONObject();

                    // Rastrear documentos únicos (no evidencias): conservar la última versión
                    Map<String, String> singleDocFinalKeys = new LinkedHashMap<>();

                    // Rastrear evidencias por actividad base:
                    // baseActivity -> Set de IDs y Set de nombres ya vistos
                    Map<String, Set<String>> evidenciaSeenIds = new LinkedHashMap<>();
                    Map<String, Set<String>> evidenciaSeenNames = new LinkedHashMap<>();
                    Map<String, JSONObject> cleanEvidenciaEntries = new LinkedHashMap<>();

                    Iterator<String> keys = originalObj.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        Object val = originalObj.get(key);
                        if (!(val instanceof JSONObject)) {
                            cleanedObj.put(key, val);
                            continue;
                        }
                        JSONObject fileObj = (JSONObject) val;
                        String fileId = fileObj.optString("id", "").trim();
                        String fileName = fileObj.optString("name", "").trim().toLowerCase();

                        Matcher matcher = EVIDENCIA_PATTERN.matcher(key);
                        if (matcher.matches()) {
                            String actBase = matcher.group(1); // ej: evidencia_0_0
                            evidenciaSeenIds.putIfAbsent(actBase, new HashSet<>());
                            evidenciaSeenNames.putIfAbsent(actBase, new HashSet<>());

                            Set<String> seenIds = evidenciaSeenIds.get(actBase);
                            Set<String> seenNames = evidenciaSeenNames.get(actBase);

                            boolean isDuplicate = false;
                            if (!fileId.isEmpty() && seenIds.contains(fileId)) {
                                isDuplicate = true;
                            } else if (!fileName.isEmpty() && seenNames.contains(fileName)) {
                                isDuplicate = true;
                            }

                            if (isDuplicate) {
                                teniaDuplicados = true;
                            } else {
                                if (!fileId.isEmpty()) seenIds.add(fileId);
                                if (!fileName.isEmpty()) seenNames.add(fileName);
                                cleanEvidenciaEntries.put(key, fileObj);
                            }
                        } else {
                            // Documento único (paz y salvo, rut, rpc, etc.)
                            String baseKey = key.matches(".*_\\d+$") ? key.replaceAll("_\\d+$", "") : key;
                            if (singleDocFinalKeys.containsKey(baseKey)) {
                                teniaDuplicados = true;
                            }
                            singleDocFinalKeys.put(baseKey, key);
                        }
                    }

                    // Reconstruir documentos únicos con su clave normalizada
                    for (Map.Entry<String, String> entry : singleDocFinalKeys.entrySet()) {
                        String baseKey = entry.getKey();
                        String finalKey = entry.getValue();
                        cleanedObj.put(baseKey, originalObj.getJSONObject(finalKey));
                    }

                    // Reconstruir evidencias limpias
                    for (Map.Entry<String, JSONObject> entry : cleanEvidenciaEntries.entrySet()) {
                        cleanedObj.put(entry.getKey(), entry.getValue());
                    }

                    if (teniaDuplicados) {
                        psUpdate.setString(1, cleanedObj.toString());
                        psUpdate.setInt(2, id);
                        psUpdate.executeUpdate();
                        totalModificados++;
                        System.out.println("-> Informe ID " + id + " saneado exitosamente (duplicados de documentos/evidencias removidos).");
                    }

                } catch (Exception parseEx) {
                    System.err.println("Error procesando JSON de informe ID " + id + ": " + parseEx.getMessage());
                }
            }

            System.out.println("=============================================================");
            System.out.println("RESUMEN: Informes analizados: " + totalProcesados + " | Informes corregidos: " + totalModificados);
            System.out.println("=============================================================");

        } catch (Exception e) {
            System.err.println("Error en la ejecución del saneamiento: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
