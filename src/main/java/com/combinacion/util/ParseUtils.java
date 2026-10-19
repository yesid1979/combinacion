package com.combinacion.util;

import java.math.BigDecimal;
import java.sql.Date;

public class ParseUtils {

    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null; // Or handle as needed, e.g., default date or throw controlled exception
        }
        try {
            return Date.valueOf(dateStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static BigDecimal parseBigDecimal(String numStr) {
        if (numStr == null || numStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            String clean = numStr.replaceAll("[^0-9,.-]", "").trim();
            if (clean.isEmpty()) return BigDecimal.ZERO;
            if (clean.contains(".") && clean.contains(",")) {
                clean = clean.replace(".", "").replace(",", ".");
            } else if (clean.contains(".") && !clean.contains(",")) {
                int lastDot = clean.lastIndexOf(".");
                if (clean.indexOf(".") != lastDot || clean.length() - lastDot - 1 == 3) {
                    clean = clean.replace(".", "");
                }
            } else if (clean.contains(",")) {
                clean = clean.replace(",", ".");
            }
            return new BigDecimal(clean);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public static int parseInt(String intStr) {
        if (intStr == null || intStr.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String normalizarPeriodo(String raw, Integer anioReferencia) {
        if (raw == null || raw.trim().isEmpty()) {
            return raw;
        }
        String clean = raw.trim();
        // If already YYYY-MM
        if (clean.matches("^\\d{4}-(0[1-9]|1[0-2])$")) {
            return clean;
        }
        // If MM/YYYY or MM-YYYY
        if (clean.matches("^(0?[1-9]|1[0-2])[/-](\\d{4})$")) {
            String[] parts = clean.split("[/-]");
            int m = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            return String.format("%04d-%02d", y, m);
        }
        // If YYYY/MM
        if (clean.matches("^(\\d{4})/(0?[1-9]|1[0-2])$")) {
            String[] parts = clean.split("/");
            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            return String.format("%04d-%02d", y, m);
        }

        // Try extracting year (4 digits starting with 20)
        int year = (anioReferencia != null && anioReferencia > 2000) ? anioReferencia : java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        java.util.regex.Matcher ym = java.util.regex.Pattern.compile("\\b(20\\d{2})\\b").matcher(clean);
        if (ym.find()) {
            try {
                year = Integer.parseInt(ym.group(1));
            } catch (Exception ignore) {}
        }

        // Detect month from text (Spanish)
        String lower = clean.toLowerCase();
        int month = 0;
        if (lower.contains("ene")) month = 1;
        else if (lower.contains("feb")) month = 2;
        else if (lower.contains("mar")) month = 3;
        else if (lower.contains("abr")) month = 4;
        else if (lower.contains("may")) month = 5;
        else if (lower.contains("jun")) month = 6;
        else if (lower.contains("jul")) month = 7;
        else if (lower.contains("ago")) month = 8;
        else if (lower.contains("sep") || lower.contains("set")) month = 9;
        else if (lower.contains("oct")) month = 10;
        else if (lower.contains("nov")) month = 11;
        else if (lower.contains("dic")) month = 12;

        if (month > 0) {
            return String.format("%04d-%02d", year, month);
        }

        return raw;
    }

    public static String formatearPeriodo(String periodo) {
        if (periodo == null || periodo.trim().isEmpty()) return "";
        try {
            String norm = normalizarPeriodo(periodo, null);
            String[] parts = norm.split("-");
            if (parts.length == 2) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.YEAR, year);
                cal.set(java.util.Calendar.MONTH, month - 1);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM yyyy", new java.util.Locale("es", "CO"));
                String formatted = sdf.format(cal.getTime());
                return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
            }
        } catch (Exception e) {}
        return periodo;
    }
}
