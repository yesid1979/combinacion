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
}
