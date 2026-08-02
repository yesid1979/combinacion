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
            return new BigDecimal(numStr);
        } catch (NumberFormatException e) {
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

    public static String formatearPeriodo(String periodo) {
        if (periodo == null || periodo.trim().isEmpty()) return "";
        try {
            String[] parts = periodo.split("-");
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
