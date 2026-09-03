package com.combinacion.services;

import com.combinacion.dao.ContratistaDAO;
import com.combinacion.dao.ContratoDAO;
import com.combinacion.dao.OrdenadorGastoDAO;
import com.combinacion.dao.SupervisorDAO;
import com.combinacion.models.Contratista;
import com.combinacion.models.Contrato;
import com.combinacion.models.OrdenadorGasto;
import com.combinacion.models.Supervisor;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RevaluacionService {

    private ContratistaDAO contratistaDAO = new ContratistaDAO();
    private ContratoDAO contratoDAO = new ContratoDAO();
    private SupervisorDAO supervisorDAO = new SupervisorDAO();
    private OrdenadorGastoDAO ordenadorDAO = new OrdenadorGastoDAO();

    public Contrato obtenerContratoParaGeneracion(int contratistaId, HttpServletRequest request) {
        String periodo = request.getParameter("periodo");
        String anioParam = request.getParameter("anio");
        Integer anio = (anioParam != null && !anioParam.isEmpty()) ? Integer.parseInt(anioParam) : null;

        if (periodo != null && !periodo.isEmpty()) {
            return contratoDAO.obtenerPorContratistaYPeriodo(contratistaId, periodo);
        }
        if (anio != null) {
            return contratoDAO.obtenerPorContratistaIdYAnio(contratistaId, anio);
        }
        return contratoDAO.obtenerPorContratistaId(contratistaId);
    }

    public void generarDocumentoIndividual(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int contratistaId = Integer.parseInt(request.getParameter("id"));
        byte[] excelBytes = generarBytesExcel(request, contratistaId);

        if (excelBytes == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron datos para generar la revaluación.");
            return;
        }

        Contratista c = contratistaDAO.obtenerPorId(contratistaId);
        Contrato contrato = obtenerContratoParaGeneracion(contratistaId, request);
        String fullNumContrato = (contrato != null && contrato.getNumeroContrato() != null) ? contrato.getNumeroContrato().trim() : "SinContrato";
        String numCorto = fullNumContrato;
        if (!numCorto.equals("SinContrato")) {
            int lastDot = numCorto.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < numCorto.length() - 1) {
                numCorto = "4121 - " + numCorto.substring(lastDot + 1);
            } else {
                numCorto = "4121 - " + numCorto;
            }
        }
        String nombreContratista = (c != null && c.getNombre() != null) ? c.getNombre().trim() : "Contratista";
        String nombreArchivo = "Revaluacion " + numCorto + " " + nombreContratista + ".xlsx";
        nombreArchivo = nombreArchivo.replaceAll("[\\\\/:*?\"<>|]", "_");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"");
        response.setContentLength(excelBytes.length);

        try (javax.servlet.ServletOutputStream out = response.getOutputStream()) {
            out.write(excelBytes);
            out.flush();
        }
    }

    public void generarZipMasivo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idsParam = request.getParameter("ids");
        if (idsParam == null || idsParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Faltan los IDs de los contratistas.");
            return;
        }

        String[] ids = idsParam.split(",");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (String idStr : ids) {
                try {
                    int contratistaId = Integer.parseInt(idStr);
                    byte[] excelBytes = generarBytesExcel(request, contratistaId);
                    if (excelBytes != null) {
                        Contratista c = contratistaDAO.obtenerPorId(contratistaId);
                        Contrato contrato = obtenerContratoParaGeneracion(contratistaId, request);
                        
                        String fullNumContrato = (contrato != null && contrato.getNumeroContrato() != null) ? contrato.getNumeroContrato().trim() : "SinContrato";
                        String numCorto = fullNumContrato;
                        if (!numCorto.equals("SinContrato")) {
                            int lastDot = numCorto.lastIndexOf('.');
                            if (lastDot >= 0 && lastDot < numCorto.length() - 1) {
                                numCorto = "4121 - " + numCorto.substring(lastDot + 1);
                            } else {
                                numCorto = "4121 - " + numCorto;
                            }
                        }
                        String nombreContratista = (c != null && c.getNombre() != null) ? c.getNombre().trim() : "Contratista";
                        String nombreArchivo = "Revaluacion " + numCorto + " " + nombreContratista + ".xlsx";
                        nombreArchivo = nombreArchivo.replaceAll("[\\\\/:*?\"<>|]", "_");
                        
                        ZipEntry entry = new ZipEntry(nombreArchivo);
                        zos.putNextEntry(entry);
                        zos.write(excelBytes);
                        zos.closeEntry();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        byte[] zipBytes = baos.toByteArray();
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"Revaluaciones_Masivas.zip\"");
        response.setContentLength(zipBytes.length);

        try (javax.servlet.ServletOutputStream out = response.getOutputStream()) {
            out.write(zipBytes);
            out.flush();
        }
    }

    private byte[] generarBytesExcel(HttpServletRequest request, int contratistaId) {
        try {
            Contratista contratista = contratistaDAO.obtenerPorId(contratistaId);
            Contrato contrato = obtenerContratoParaGeneracion(contratistaId, request);

            if (contratista == null || contrato == null) {
                return null;
            }

            Supervisor supervisor = null;
            if (contrato.getSupervisorId() > 0) {
                supervisor = supervisorDAO.obtenerPorId(contrato.getSupervisorId());
            }

            OrdenadorGasto ordenador = null;
            if (contrato.getOrdenadorId() > 0) {
                ordenador = ordenadorDAO.obtenerPorId(contrato.getOrdenadorId());
            }

            Map<String, String> variables = new HashMap<>();
            String organismo = ordenador != null && ordenador.getOrganismo() != null ? ordenador.getOrganismo() : "DEPARTAMENTO ADMINISTRATIVO DE GESTION JURIDICA PUBLICA";
            variables.put("${ORGANISMO}", organismo.toUpperCase());
            variables.put("${TIPO_CONTRATO}", contrato.getTipoContrato() != null ? contrato.getTipoContrato() : "Prestaci\u00f3n de servicios profesionales y de apoyo a la gesti\u00f3n");
            
            String numContrato = contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : "";
            if (contrato.getAnio() != null && !numContrato.isEmpty()) {
                numContrato += " - " + contrato.getAnio();
            }
            variables.put("${NUMERO_CONTRATO}", numContrato);
            
            // Fecha de Generacion
            Date fechaGeneracion = new Date();
            Calendar calGen = Calendar.getInstance();
            calGen.setTime(fechaGeneracion);
            variables.put("${DIA}", String.valueOf(calGen.get(Calendar.DAY_OF_MONTH)));
            variables.put("${MES}", String.valueOf(calGen.get(Calendar.MONTH) + 1));
            variables.put("${ANIO}", String.valueOf(calGen.get(Calendar.YEAR)));
            String mesText = new SimpleDateFormat("MMMM", new Locale("es", "ES")).format(fechaGeneracion).toUpperCase();
            variables.put("${MES_TEXT}", mesText);

            // Fecha de Suscripcion
            Date fechaSuscripcion = contrato.getFechaSuscripcion();
            if (fechaSuscripcion != null) {
                Calendar calSusc = Calendar.getInstance();
                calSusc.setTime(fechaSuscripcion);
                variables.put("${DIA_SUSC}", String.valueOf(calSusc.get(Calendar.DAY_OF_MONTH)));
                variables.put("${MES_SUSC}", String.valueOf(calSusc.get(Calendar.MONTH) + 1));
                variables.put("${ANIO_SUSC}", String.valueOf(calSusc.get(Calendar.YEAR)));
                
                String mesTextSusc = new SimpleDateFormat("MMMM", new Locale("es", "ES")).format(fechaSuscripcion).toUpperCase();
                variables.put("${MES_TEXT_SUSC}", mesTextSusc);
            } else {
                variables.put("${DIA_SUSC}", "");
                variables.put("${MES_SUSC}", "");
                variables.put("${ANIO_SUSC}", "");
                variables.put("${MES_TEXT_SUSC}", "");
            }

            String valorTotalLetras = contrato.getValorTotalLetras() != null ? contrato.getValorTotalLetras() : "";
            java.math.BigDecimal valorTotalNumeros = contrato.getValorTotalNumeros();
            double valDouble = valorTotalNumeros != null ? valorTotalNumeros.doubleValue() : 0.0;
            String valorMoneda = "$" + String.format(Locale.US, "%,.0f", valDouble).replace(',', '.');
            variables.put("${VALOR}", valorMoneda + " " + valorTotalLetras);
            
            variables.put("${OBJETO}", contrato.getObjeto() != null ? contrato.getObjeto() : "");
            variables.put("${CONTRATISTA}", contratista.getNombre() != null ? contratista.getNombre() : "");
            
            String cedulaTxt = contratista.getCedula() != null ? contratista.getCedula() : "";
            if (contratista.getDv() != null && !contratista.getDv().isEmpty()) {
                cedulaTxt += " de Cali (Valle)";
            }
            variables.put("${CEDULA}", cedulaTxt);
            
            variables.put("${SUPERVISOR}", supervisor != null && supervisor.getNombre() != null ? supervisor.getNombre() : "");

            String templatePath = request.getServletContext().getRealPath("/") + "plantillas/REVALUACION_PROVEEDORES_TEMPLATE.xlsx";
            if (!new File(templatePath).exists()) {
                // Try parent path for development environment fallback
                templatePath = new File(request.getServletContext().getRealPath("/")).getParentFile().getParentFile().getAbsolutePath() + "/plantillas/REVALUACION_PROVEEDORES_TEMPLATE.xlsx";
            }

            try (FileInputStream fis = new FileInputStream(new File(templatePath));
                 Workbook workbook = new XSSFWorkbook(fis)) {
                 
                Sheet sheet = workbook.getSheet("EVALUACION");
                if (sheet != null) {
                    for (Row row : sheet) {
                        for (Cell cell : row) {
                            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                                String cellValue = cell.getStringCellValue();
                                boolean changed = false;
                                for (Map.Entry<String, String> entry : variables.entrySet()) {
                                    if (cellValue.contains(entry.getKey())) {
                                        cellValue = cellValue.replace(entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
                                        changed = true;
                                    }
                                }
                                if (changed) {
                                    cell.setCellValue(cellValue);
                                }
                            }
                        }
                    }
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                return out.toByteArray();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
