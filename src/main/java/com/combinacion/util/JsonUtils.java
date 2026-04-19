package com.combinacion.util;

/**
 * Utilidad centralizada para la serialización de JSON.
 * Evita la duplicación del método escapeJson() en todos los Servlets.
 */
public class JsonUtils {

    private JsonUtils() {
        // Clase de utilidad, no se instancia
    }

    /**
     * Escapa caracteres especiales en una cadena para ser usada de forma
     * segura dentro de un valor JSON.
     *
     * @param s La cadena a escapar.
     * @return La cadena escapada, o cadena vacía si el parámetro es null.
     */
    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (ch < ' ') {
                        String t = "000" + Integer.toHexString(ch);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }
}
