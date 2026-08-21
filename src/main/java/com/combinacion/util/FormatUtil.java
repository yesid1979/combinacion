package com.combinacion.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FormatUtil {

    /**
     * Formatea un número como moneda colombiana con separadores de miles.
     * Ejemplo: 19220000 -> "$ 19.220.000"
     */
    public static String formatearMoneda(Number valor) {
        if (valor == null) {
            return "";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CO"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat formatter = new DecimalFormat("$#,##0", symbols);
        return formatter.format(valor);
    }

    /**
     * Convierte un número (1-31) a su representación en letras en español.
     */
    public static String convertirNumeroALetras(int numero) {
        String[] unidades = { "", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve" };
        String[] decenas = { "", "diez", "veinte", "treinta" };
        String[] especiales = { "diez", "once", "doce", "trece", "catorce", "quince", "dieciséis",
                "diecisiete", "dieciocho", "diecinueve" };
        String[] veintitantos = { "veinte", "veintiuno", "veintidós", "veintitrés", "veinticuatro",
                "veinticinco", "veintiséis", "veintisiete", "veintiocho", "veintinueve" };

        if (numero < 1 || numero > 31) {
            return String.valueOf(numero);
        }

        if (numero < 10) {
            return unidades[numero];
        } else if (numero < 20) {
            return especiales[numero - 10];
        } else if (numero < 30) {
            return veintitantos[numero - 20];
        } else if (numero == 30) {
            return "treinta";
        } else {
            return "treinta y " + unidades[numero - 30];
        }
    }

    public static String capitalizeFirst(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        String low = input.toLowerCase().trim();
        return Character.toUpperCase(low.charAt(0)) + low.substring(1);
    }

    public static String toTitleCase(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() > 0) {
                sb.append(Character.toUpperCase(words[i].charAt(0)))
                  .append(words[i].substring(1));
            }
            if (i < words.length - 1) sb.append(" ");
        }
        return sb.toString();
    }

    public static String convertirMontoALetras(BigDecimal monto) {
        if (monto == null) return "";
        long lPart = monto.longValue();
        if (lPart == 0) return "CERO PESOS M/CTE";
        String letras = convertirNumeroALetrasGrandes(lPart);
        if (letras.endsWith("MILLON") || letras.endsWith("MILLONES")) {
            letras += " DE";
        }
        return letras + " PESOS M/CTE";
    }

    public static String convertirNumeroALetrasGrandes(long n) {
        if (n == 0) return "CERO";
        if (n == 1) return "UN";
        if (n == 100) return "CIEN";
        if (n < 1000) return getCentenas(n);
        if (n < 1000000) {
            long mil = n / 1000;
            long resto = n % 1000;
            String sMil = (mil == 1) ? "MIL" : (convertirNumeroALetrasGrandes(mil) + " MIL");
            return (resto == 0) ? sMil : (sMil + " " + getCentenas(resto));
        }
        long millon = n / 1000000;
        long restoMillon = n % 1000000;
        String sMillon = (millon == 1) ? "UN MILLON" : (convertirNumeroALetrasGrandes(millon) + " MILLONES");
        if (restoMillon == 0) return sMillon;
        return sMillon + " " + (restoMillon < 1000 ? getCentenas(restoMillon) : convertirNumeroALetrasGrandes(restoMillon));
    }

    private static String getCentenas(long n) {
        if (n > 999) return "";
        if (n == 100) return "CIEN";
        if (n < 10) return getUnidades(n);
        if (n < 20) return getEspeciales(n);
        if (n < 100) {
            int d = (int)(n / 10);
            int u = (int)(n % 10);
            String[] dec = {"", "", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"};
            if (n == 20) return "VEINTE";
            if (n < 30) return "VEINTI" + getUnidades(u);
            return dec[d] + (u == 0 ? "" : " Y " + getUnidades(u));
        }
        int c = (int)(n / 100);
        long r = n % 100;
        String[] cent = {"", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"};
        return cent[c] + (r == 0 ? "" : " " + getCentenas(r));
    }

    private static String getUnidades(long n) {
        String[] u = {"", "UN", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE"};
        return u[(int)n];
    }

    private static String getEspeciales(long n) {
        String[] esp = {"DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE"};
        return esp[(int)(n - 10)];
    }
}
