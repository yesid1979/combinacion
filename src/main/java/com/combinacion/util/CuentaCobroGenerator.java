package com.combinacion.util;

import com.combinacion.models.Contrato;
import com.combinacion.models.InformeSupervision;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

public class CuentaCobroGenerator {

    public static String generarExcel(InformeSupervision informe, Contrato contrato, String realPath) throws Exception {
        // Asume que hay una plantilla base llamada "CUENTA_COBRO.xlsx" en la carpeta plantillas/
        String templatePath = realPath + File.separator + "plantillas" + File.separator + "CUENTA_COBRO.xlsx";
        File templateFile = new File(templatePath);
        
        if (!templateFile.exists()) {
            // Si la plantilla no existe en webapp, buscar en la carpeta doc/ como fallback de desarrollo
            templatePath = realPath + File.separator + ".." + File.separator + ".." + File.separator + "doc" + File.separator + "2. DS-4121-0411 Cuota 6 -YESID PIEDRAHITA.xlsx";
            templateFile = new File(templatePath);
            if (!templateFile.exists()) {
                throw new Exception("No se encontró la plantilla de Excel en: " + templatePath);
            }
        }

        Workbook wb = new XSSFWorkbook(new FileInputStream(templateFile));
        Sheet sheet = wb.getSheetAt(0);

        // 1. Fecha de la transacción (D12)
        Row row12 = sheet.getRow(11); if(row12 == null) row12 = sheet.createRow(11);
        Cell cellD12 = row12.getCell(3); if(cellD12 == null) cellD12 = row12.createCell(3);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date fechaBase = informe.getFechaSuscripcion() != null ? informe.getFechaSuscripcion() : (informe.getFechaCreacion() != null ? informe.getFechaCreacion() : new Date());
        cellD12.setCellValue(sdf.format(fechaBase));
        
        // 2. Número Consecutivo (I12)
        Cell cellI12 = row12.getCell(8); if(cellI12 == null) cellI12 = row12.createCell(8);
        cellI12.setCellValue(informe.getConsecutivoCobro() != null ? informe.getConsecutivoCobro() : "");

        // Forzar NIT del organismo con puntos en H13 para evitar el formateo automático de Excel con comas
        Row row13 = sheet.getRow(12); if(row13 == null) row13 = sheet.createRow(12);
        Cell cellH13 = row13.getCell(7); if(cellH13 == null) cellH13 = row13.createCell(7);
        cellH13.setCellValue("890.399.011");

        // 3. Organismo (D14)
        if (contrato.getOrdenadorGasto() != null) {
            Row row14 = sheet.getRow(13); if(row14 == null) row14 = sheet.createRow(13);
            Cell cellD14 = row14.getCell(3); if(cellD14 == null) cellD14 = row14.createCell(3);
            cellD14.setCellValue(toTitleCase(contrato.getOrdenadorGasto().getOrganismo()));
        }
        
        // Dirección - Organismo (D15)
        if (contrato.getOrdenadorGasto() != null) {
            Row row15 = sheet.getRow(14); if(row15 == null) row15 = sheet.createRow(14);
            Cell cellD15 = row15.getCell(3); if(cellD15 == null) cellD15 = row15.createCell(3);
            String direccionOrganismo = contrato.getOrdenadorGasto().getDireccionOrganismo();
            cellD15.setCellValue(direccionOrganismo != null ? direccionOrganismo : "");
        }

        // DATOS CONTRATISTA
        if (contrato.getContratista() != null) {
            Row row17 = sheet.getRow(16); if(row17 == null) row17 = sheet.createRow(16);
            Cell cellD17 = row17.getCell(3); if(cellD17 == null) cellD17 = row17.createCell(3);
            cellD17.setCellValue(toTitleCase(contrato.getContratistaNombre()));
            
            Cell cellH17 = row17.getCell(7); if(cellH17 == null) cellH17 = row17.createCell(7);
            String cedulaStr = contrato.getContratista().getCedula() != null ? contrato.getContratista().getCedula() : "";
            // Remover cualquier texto adicional como " de Jamundí"
            cedulaStr = cedulaStr.split("(?i)\\s+de\\s+")[0].trim();
            cedulaStr = cedulaStr.replace(",", ".");
            cellH17.setCellValue(cedulaStr);
            
            Cell cellI17 = row17.getCell(8); if(cellI17 == null) cellI17 = row17.createCell(8);
            String dvStr = contrato.getContratista().getDv() != null ? contrato.getContratista().getDv() : "0";
            cellI17.setCellValue(dvStr);
            
            Row row18 = sheet.getRow(17); if(row18 == null) row18 = sheet.createRow(17);
            Cell cellD18 = row18.getCell(3); if(cellD18 == null) cellD18 = row18.createCell(3);
            cellD18.setCellValue(contrato.getContratista().getDireccion());
            try {
                org.apache.poi.ss.usermodel.CellStyle style = wb.createCellStyle();
                style.cloneStyleFrom(cellD18.getCellStyle());
                style.setWrapText(true);
                cellD18.setCellStyle(style);
            } catch (Exception e) {}
            
            Cell cellH18 = row18.getCell(7); if(cellH18 == null) cellH18 = row18.createCell(7);
            cellH18.setCellValue("Santiago de Cali");
            
            Row row19 = sheet.getRow(18); if(row19 == null) row19 = sheet.createRow(18);
            Cell cellD19 = row19.getCell(3); if(cellD19 == null) cellD19 = row19.createCell(3);
            cellD19.setCellValue(contrato.getContratista().getCorreo());
            
            Cell cellH19 = row19.getCell(7); if(cellH19 == null) cellH19 = row19.createCell(7);
            cellH19.setCellValue(contrato.getContratista().getTelefono());
        }

        // Concepto (D21)
        Row row21 = sheet.getRow(20); if(row21 == null) row21 = sheet.createRow(20);
        Cell cellD21 = row21.getCell(3); if(cellD21 == null) cellD21 = row21.createCell(3);
        String numeroCuotaStr = informe.getNumeroCuota() != null ? informe.getNumeroCuota() : "";
        String tipoContrato = (contrato.getTipoContrato() != null && !contrato.getTipoContrato().trim().isEmpty()) 
                                ? contrato.getTipoContrato().trim() 
                                : "Prestación de Servicios Profesionales";
        cellD21.setCellValue(tipoContrato + " Cuota " + convertirNumeroALetras(com.combinacion.util.ParseUtils.parseInt(numeroCuotaStr)).toLowerCase() + " (" + numeroCuotaStr + ")");

        // Valor a pagar (D23 y F23)
        Row row23 = sheet.getRow(22); if(row23 == null) row23 = sheet.createRow(22);
        Cell cellD23 = row23.getCell(3); if(cellD23 == null) cellD23 = row23.createCell(3);
        Cell cellF23 = row23.getCell(5); if(cellF23 == null) cellF23 = row23.createCell(5);
        if (informe.getValorCuotaPagar() != null) {
            cellD23.setCellValue(formatearMoneda(informe.getValorCuotaPagar()));
            cellF23.setCellValue(convertirNumeroALetras(informe.getValorCuotaPagar().longValue()).toUpperCase() + " PESOS M/CTE");
        }

        // Info Contrato
        Row row25 = sheet.getRow(24); if(row25 == null) row25 = sheet.createRow(24);
        Cell cellD25 = row25.getCell(3); if(cellD25 == null) cellD25 = row25.createCell(3);
        cellD25.setCellValue(contrato.getNumeroContrato());

        if (contrato.getPresupuestoDetalle() != null) {
            com.combinacion.models.PresupuestoDetalle pre = contrato.getPresupuestoDetalle();
            Cell cellH25 = row25.getCell(7); if(cellH25 == null) cellH25 = row25.createCell(7);
            cellH25.setCellValue(pre.getCdpNumero() != null ? pre.getCdpNumero() : "");
            
            Row row26 = sheet.getRow(25); if(row26 == null) row26 = sheet.createRow(25);
            Cell cellH26 = row26.getCell(7); if(cellH26 == null) cellH26 = row26.createCell(7);
            cellH26.setCellValue(pre.getRpNumero() != null ? pre.getRpNumero() : "");
        }

        Row row27 = sheet.getRow(26); if(row27 == null) row27 = sheet.createRow(26);
        Cell cellD27 = row27.getCell(3); if(cellD27 == null) cellD27 = row27.createCell(3);
        String objeto = contrato.getObjeto() != null ? contrato.getObjeto().trim() : "";
        // Eliminar saltos de línea justo antes de una comilla final (para evitar la comilla sola en otra línea)
        objeto = objeto.replaceAll("[\\r\\n]+(?=\"$)", "");
        cellD27.setCellValue(objeto);
        try {
            org.apache.poi.ss.usermodel.CellStyle style = wb.createCellStyle();
            style.cloneStyleFrom(cellD27.getCellStyle());
            style.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.JUSTIFY);
            style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.TOP);
            style.setWrapText(true);
            cellD27.setCellStyle(style);
        } catch (Exception e) {}

        Row row28 = sheet.getRow(27); if(row28 == null) row28 = sheet.createRow(27);
        Cell cellD28 = row28.getCell(3); if(cellD28 == null) cellD28 = row28.createCell(3);
        Cell cellF28 = row28.getCell(5); if(cellF28 == null) cellF28 = row28.createCell(5);
        if (contrato.getValorTotalNumeros() != null) {
            cellD28.setCellValue(formatearMoneda(contrato.getValorTotalNumeros()));
            cellF28.setCellValue(convertirNumeroALetras(contrato.getValorTotalNumeros().longValue()).toUpperCase() + " PESOS M/CTE");
        }

        // Crear directorio temporal para el archivo generado
        File tempDir = new File(realPath, "temp");
        if (!tempDir.exists()) tempDir.mkdirs();

        String safeName = (contrato.getContratistaNombre() != null ? contrato.getContratistaNombre().replaceAll("[^a-zA-Z0-9.-]", "_") : "CONTRATISTA");
        String finalFileName = "Cuenta_Cobro_" + informe.getNumeroCuota() + "_" + safeName + ".xlsx";
        File outFile = new File(tempDir, finalFileName);

        try (FileOutputStream out = new FileOutputStream(outFile)) {
            wb.write(out);
        }
        wb.close();

        return outFile.getAbsolutePath();
    }

    private static String formatearMoneda(Number valor) {
        if (valor == null) {
            return "";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CO"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("$ #,##0", symbols);
        return formatter.format(valor);
    }

    private static String toTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : text.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    private static final String[] UNIDADES = {"", "uno ", "dos ", "tres ", "cuatro ", "cinco ", "seis ", "siete ", "ocho ", "nueve "};
    private static final String[] DECENAS = {"diez ", "once ", "doce ", "trece ", "catorce ", "quince ", "dieciseis ",
            "diecisiete ", "dieciocho ", "diecinueve", "veinte ", "treinta ", "cuarenta ",
            "cincuenta ", "sesenta ", "setenta ", "ochenta ", "noventa "};
    private static final String[] CENTENAS = {"", "ciento ", "doscientos ", "trescientos ", "cuatrocientos ", "quinientos ", "seiscientos ",
            "setecientos ", "ochocientos ", "novecientos "};

    private static String convertirNumeroALetras(long numero) {
        if (numero == 0) return "cero";
        if (numero < 0) return "menos " + convertirNumeroALetras(Math.abs(numero));
        String letras = "";
        if (numero >= 1000000) {
            long millones = numero / 1000000;
            if (millones == 1) letras += "un millon ";
            else letras += convertirNumeroALetras(millones) + " millones ";
            numero %= 1000000;
        }
        if (numero >= 1000) {
            long miles = numero / 1000;
            if (miles == 1) letras += "mil ";
            else letras += convertirNumeroALetras(miles) + " mil ";
            numero %= 1000;
        }
        if (numero >= 100) {
            if (numero == 100) {
                letras += "cien ";
                numero = 0;
            } else {
                letras += CENTENAS[(int)(numero / 100)];
                numero %= 100;
            }
        }
        if (numero >= 10) {
            if (numero < 20) {
                letras += DECENAS[(int)(numero - 10)];
                numero = 0;
            } else {
                letras += DECENAS[(int)(numero / 10) + 8];
                numero %= 10;
                if (numero > 0) letras += "y ";
            }
        }
        if (numero > 0) {
            letras += UNIDADES[(int)numero];
        }
        return letras.trim();
    }
}
