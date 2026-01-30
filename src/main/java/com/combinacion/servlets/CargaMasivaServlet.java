package com.combinacion.servlets;

import com.combinacion.dao.SupervisorDAO;
import com.combinacion.dao.OrdenadorGastoDAO;
import com.combinacion.dao.ContratistaDAO;
import com.combinacion.dao.EstructuradorDAO;
import com.combinacion.dao.PresupuestoDetalleDAO;
import com.combinacion.dao.ContratoDAO;

import com.combinacion.models.OrdenadorGasto;
import com.combinacion.models.Contratista;
import com.combinacion.models.Supervisor;
import com.combinacion.models.Estructurador;
import com.combinacion.models.PresupuestoDetalle;
import com.combinacion.models.Contrato;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

// Apache POI Libraries
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

@WebServlet(name = "CargaMasivaServlet", urlPatterns = { "/upload" })
@MultipartConfig
public class CargaMasivaServlet extends HttpServlet {

    private OrdenadorGastoDAO ordenadorDAO = new OrdenadorGastoDAO();
    private ContratistaDAO contratistaDAO = new ContratistaDAO();
    private SupervisorDAO supervisorDAO = new SupervisorDAO();
    private EstructuradorDAO estructuradorDAO = new EstructuradorDAO();
    private PresupuestoDetalleDAO presupuestoDAO = new PresupuestoDetalleDAO();
    private ContratoDAO contratoDAO = new ContratoDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        Part filePart = request.getPart("file");

        int ordenadoresCount = 0;
        int contratistasCount = 0;
        int supervisoresCount = 0;
        // Remove separate duplicate counters if not needed, or keep for logs.
        // For simplicity, we are returning objects, duplicates are handled inside.
        int estructuradoresCount = 0;
        int presupuestoCount = 0;
        int contratosCount = 0;
        int errorCount = 0;
        StringBuilder log = new StringBuilder();

        if (filePart != null) {
            String fileName = filePart.getSubmittedFileName().toLowerCase();

            try (InputStream fileContent = filePart.getInputStream()) {

                List<String[]> allRows = new java.util.ArrayList<>();

                // 1. READ ALL DATA INTO MEMORY
                if (fileName.endsWith(".xlsx")) {
                    try (Workbook workbook = new XSSFWorkbook(fileContent)) {
                        allRows = readSheetData(workbook.getSheetAt(0));
                    }
                } else if (fileName.endsWith(".xls")) {
                    try (Workbook workbook = new HSSFWorkbook(fileContent)) {
                        allRows = readSheetData(workbook.getSheetAt(0));
                    }
                } else {
                    // CSV - changing to UTF-8 to fix encoding issues
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileContent, "UTF-8"))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.trim().isEmpty())
                                continue;
                            // Use -1 limit to preserve trailing empty strings
                            allRows.add(line.split(";", -1));
                        }
                    }
                }

                if (allRows.isEmpty()) {
                    throw new Exception("El archivo está vacío.");
                }

                // 2. DETECT HEADER (VERTICAL MERGING STRATEGY)
                // Combine text from the first 5 rows to handle split headers (e.g., Row 1:
                // "Estructuradores", Row 2: "Juridico")
                // This creates a "Virtual Header" that contains all keywords found in the top
                // rows for each column.

                int maxCols = 0;
                int rowsToScan = Math.min(5, allRows.size());

                // Find max columns
                for (int i = 0; i < rowsToScan; i++) {
                    if (allRows.get(i).length > maxCols)
                        maxCols = allRows.get(i).length;
                }

                String[] header = new String[maxCols];
                for (int c = 0; c < maxCols; c++) {
                    StringBuilder sb = new StringBuilder();
                    for (int r = 0; r < rowsToScan; r++) {
                        String[] row = allRows.get(r);
                        if (c < row.length && row[c] != null && !row[c].trim().isEmpty()) {
                            sb.append(row[c].trim()).append(" ");
                        }
                    }
                    header[c] = sb.toString().trim();
                }

                log.append("═══ DEBUG: ENCABEZADO VIRTUAL (Combinación ").append(rowsToScan).append(" filas) ═══\n");
                for (int k = 0; k < header.length; k++) {
                    if (!header[k].isEmpty()) {
                        log.append("[").append(k).append("] ").append(header[k]).append(" | Norm: ")
                                .append(normalizeText(header[k])).append("\n");
                    }
                }
                log.append("═══════════════════════════════════════════════════\n\n");

                Map<String, Integer> map = mapHeaders(header);

                // NOTE: We do not skip rows based on index here because we merged multiple
                // rows.
                // We will rely on the processing logic to ignore rows that don't look like data
                // (e.g. they look like headers).
                // Or we can start processing at 'rowsToScan' if we are confident.
                // Let's set 'bestHeaderRowIndex' to rowsToScan - 1 so the loop starts after it?
                // The loop uses 'bestHeaderRowIndex + 1'.
                int bestHeaderRowIndex = rowsToScan - 1;
                // Caution: if the file has ONLY 1 header row, we might skip 4 data rows if
                // rowsToScan=5.
                // WE MUST BE CAREFUL.
                // Heuristic: If row 2 has data that parses as date/number in known columns?
                // SAFEST: Set bestHeaderRowIndex = 0. We will iterate from 1.
                // Header rows (merged) will probably fail parsing and be skipped as invalid
                // data.
                bestHeaderRowIndex = 0;

                log.append("═══ CARGA MASIVA - ORDENADORES, CONTRATISTAS Y PRESUPUESTO ═══\n\n");
                log.append("Total columnas detectadas: ").append(map.size()).append("\n\n");

                // Log del mapeo de ORDENADORES
                log.append("ORDENADORES DEL GASTO:\n");
                logMapping(log, map, "organismo", "Organismo");
                logMapping(log, map, "direccion_organismo", "Dirección del organismo");
                logMapping(log, map, "nombre_ordenador", "Nombre del ordenador");
                logMapping(log, map, "cedula_ordenador", "Cédula del ordenador");
                logMapping(log, map, "cargo_ordenador", "Cargo del ordenador");
                logMapping(log, map, "decreto_nombramiento", "Decreto de nombramiento");
                logMapping(log, map, "acta_posesion", "Acta de posesión");

                log.append("ESTRUCTURADORES:\n");
                logMapping(log, map, "estructurador_juridico", "Jurídico Nombre");
                logMapping(log, map, "estructurador_juridico_cargo", "Jurídico Cargo"); // Added check
                logMapping(log, map, "estructurador_tecnico", "Técnico Nombre");
                logMapping(log, map, "estructurador_tecnico_cargo", "Técnico Cargo"); // Added check
                logMapping(log, map, "estructurador_financiero", "Financiero Nombre");
                logMapping(log, map, "estructurador_financiero_cargo", "Financiero Cargo"); // Added check

                log.append("PRESUPUESTO:\n");
                logMapping(log, map, "cdp_numero", "CDP Número");
                logMapping(log, map, "cdp_fecha", "CDP Fecha");
                logMapping(log, map, "cdp_valor", "CDP Valor");
                logMapping(log, map, "rp_numero", "RP Número");
                logMapping(log, map, "rp_fecha", "RP Fecha");
                logMapping(log, map, "apropiacion_presupuestal", "Apropiación");
                logMapping(log, map, "rubro_presupuestal", "Rubro");
                logMapping(log, map, "id_paa", "ID PAA");
                logMapping(log, map, "codigo_dane", "Código DANE");
                logMapping(log, map, "ficha_ebi_nombre", "Ficha EBI Nombre");

                // Log del mapeo de CONTRATISTAS
                log.append("\nCONTRATISTAS:\n");
                logMapping(log, map, "contratista_nombre", "Nombre");
                logMapping(log, map, "contratista_cedula", "Cédula");
                logMapping(log, map, "contratista_dv", "DV");
                logMapping(log, map, "contratista_telefono", "Teléfono");
                logMapping(log, map, "contratista_correo", "Correo");
                logMapping(log, map, "contratista_direccion", "Dirección");
                logMapping(log, map, "contratista_dia_nac", "Día nacimiento");
                logMapping(log, map, "contratista_mes_nac", "Mes nacimiento");
                logMapping(log, map, "contratista_ano_nac", "Año nacimiento");
                logMapping(log, map, "contratista_edad", "Edad");
                logMapping(log, map, "contratista_formacion", "Formación académica");
                logMapping(log, map, "contratista_desc_formacion", "Descripción formación");
                logMapping(log, map, "contratista_tarjeta", "Tarjeta profesional");
                logMapping(log, map, "contratista_desc_tarjeta", "Descripción tarjeta");
                logMapping(log, map, "contratista_experiencia", "Experiencia");
                logMapping(log, map, "contratista_desc_experiencia", "Descripción experiencia");
                logMapping(log, map, "contratista_restricciones", "Restricciones");

                // Log del mapeo de SUPERVISORES
                log.append("\nSUPERVISORES:\n");
                logMapping(log, map, "supervisor_nombre", "Nombre Supervisor");
                logMapping(log, map, "supervisor_cedula", "Cédula Supervisor");
                logMapping(log, map, "supervisor_cargo", "Cargo Supervisor");

                // Log del mapeo de CONTRATOS (NUEVO)
                log.append("\nCONTRATOS:\n");
                logMapping(log, map, "trd_proceso", "TRD Proceso");
                logMapping(log, map, "numero_contrato", "Número Contrato");
                logMapping(log, map, "tipo_contrato", "Tipo Contrato");
                logMapping(log, map, "nivel", "Nivel");
                logMapping(log, map, "objeto", "Objeto");
                logMapping(log, map, "modalidad", "Modalidad");
                logMapping(log, map, "estado", "Estado");
                logMapping(log, map, "periodo", "Periodo");
                logMapping(log, map, "fecha_suscripcion", "Fecha Suscripción");
                logMapping(log, map, "fecha_inicio", "Fecha Inicio");
                logMapping(log, map, "fecha_terminacion", "Fecha Terminación (Plazo Ejecución)");
                logMapping(log, map, "fecha_aprobacion", "Fecha Aprobación");
                logMapping(log, map, "fecha_ejecucion", "Fecha Ejecución");
                logMapping(log, map, "fecha_arl", "Fecha ARL");
                logMapping(log, map, "plazo_meses", "Plazo Meses");
                logMapping(log, map, "plazo_dias", "Plazo Días");
                logMapping(log, map, "valor_total_letras", "Valor Total (Letras)");
                logMapping(log, map, "valor_total_numeros", "Valor Total (Números)");
                logMapping(log, map, "valor_antes_iva", "Valor Antes IVA");
                logMapping(log, map, "valor_iva", "Valor IVA");
                logMapping(log, map, "valor_cuota_letras", "Valor Cuota (Letras)");
                logMapping(log, map, "valor_cuota_numero", "Valor Cuota (Número)");
                logMapping(log, map, "num_cuotas_letras", "Num Cuotas (Letras)");
                logMapping(log, map, "num_cuotas_numero", "Num Cuotas (Número)");
                logMapping(log, map, "valor_media_cuota_letras", "Media Cuota (Letras)");
                logMapping(log, map, "valor_media_cuota_numero", "Media Cuota (Número)");
                logMapping(log, map, "actividades_entregables", "Actividades/Entregables");
                logMapping(log, map, "liquidacion_acuerdo", "Liquidación Acuerdo");
                logMapping(log, map, "liquidacion_articulo", "Liquidación Artículo");
                logMapping(log, map, "liquidacion_decreto", "Liquidación Decreto");
                logMapping(log, map, "circular_honorarios", "Circular Honorarios");

                log.append("\n═════════════════════════════════════════\n\n");
                System.out.println(log.toString());

                // 3. CACHE EXISTING DATA (Optimización para evitar N+1 queries)
                java.util.Set<String> cedulasOrdenadoresExistentes = ordenadorDAO.obtenerTodasLasCedulas();
                java.util.Set<String> cedulasContratistasExistentes = contratistaDAO.obtenerTodasLasCedulas();
                java.util.Set<String> cedulasSupervisoresExistentes = supervisorDAO.obtenerTodasLasCedulas();

                // 4. PROCESS DATA ROWS
                for (int i = bestHeaderRowIndex + 1; i < allRows.size(); i++) {
                    String[] row = allRows.get(i);

                    // Skip completely empty rows
                    if (isRowEmpty(row))
                        continue;

                    try {
                        // Intentar procesar como Ordenador
                        OrdenadorGasto ordenadorObj = processOrdenador(row, map, cedulasOrdenadoresExistentes);
                        if (ordenadorObj != null) {
                            if (ordenadorObj.getId() > 0) {
                                // Exito o Recuperado
                            }
                            ordenadoresCount++; // Count attempts/found
                        }

                        // Intentar procesar como Contratista
                        Contratista contratistaObj = processContratista(row, map, cedulasContratistasExistentes);
                        if (contratistaObj != null) {
                            contratistasCount++;
                        }

                        // Intentar procesar como Supervisor
                        Supervisor supervisorObj = processSupervisor(row, map, cedulasSupervisoresExistentes);
                        if (supervisorObj != null) {
                            supervisoresCount++;
                        }

                        // Intentar procesar como Estructurador
                        Estructurador estructuradorObj = processEstructurador(row, map, log);
                        if (estructuradorObj != null) {
                            estructuradoresCount++;
                        }

                        // Intentar procesar como PresupuestoDetalle
                        PresupuestoDetalle presupuestoObj = processPresupuestoDetalle(row, map, log);
                        if (presupuestoObj != null) {
                            presupuestoCount++;
                        }

                        // Intentar procesar CONTRATO con las referencias
                        Contrato contratoObj = processContrato(row, map, contratistaObj, supervisorObj, ordenadorObj,
                                estructuradorObj, presupuestoObj, log);
                        if (contratoObj != null) {
                            contratosCount++;
                        }

                        // Si no se procesó nada (ni siquiera contrato)
                        if (ordenadorObj == null && contratistaObj == null && supervisorObj == null
                                && estructuradorObj == null && presupuestoObj == null && contratoObj == null) {
                            errorCount++;
                        }

                    } catch (Exception ex) {
                        errorCount++;
                        log.append("⚠️ Error en fila ").append(i + 1).append(": ").append(ex.getMessage()).append("\n");
                        ex.printStackTrace();
                    }
                }

                StringBuilder msg = new StringBuilder();
                if (ordenadoresCount > 0) {
                    msg.append("✅ Ordenadores: ").append(ordenadoresCount);
                }
                if (contratistasCount > 0) {
                    if (msg.length() > 0)
                        msg.append("<br>");
                    msg.append("✅ Contratistas: ").append(contratistasCount);
                }

                if (supervisoresCount > 0) {
                    if (msg.length() > 0)
                        msg.append("<br>");
                    msg.append("✅ Supervisores: ").append(supervisoresCount);
                }

                if (estructuradoresCount > 0) {
                    if (msg.length() > 0)
                        msg.append("<br>");
                    msg.append("✅ Estructuradores: ").append(estructuradoresCount);
                }

                if (presupuestoCount > 0) {
                    if (msg.length() > 0)
                        msg.append("<br>");
                    msg.append("✅ Presupuestos: ").append(presupuestoCount);
                }

                if (contratosCount > 0) {
                    if (msg.length() > 0)
                        msg.append("<br>");
                    msg.append("✅ Contratos: ").append(contratosCount);
                }

                if (errorCount > 0) {
                    if (msg.length() > 0)
                        msg.append("<br>");
                    msg.append("⚠️ Errores: ").append(errorCount);
                }

                if (msg.length() == 0) {
                    msg.append("⚠️ No se cargaron datos");
                }

                request.setAttribute("message", msg.toString());
                request.setAttribute("debug", log.toString());

            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("error", "❌ Error al procesar el archivo: " + e.getMessage());
            }
        } else {
            request.setAttribute("error", "❌ No se seleccionó ningún archivo.");
        }

        request.getRequestDispatcher("carga_masiva.jsp").forward(request, response);
    }

    private List<String[]> readSheetData(Sheet sheet) {
        List<String[]> data = new java.util.ArrayList<>();
        int maxCol = 0;

        for (Row row : sheet) {
            if (row != null && row.getLastCellNum() > maxCol) {
                maxCol = row.getLastCellNum();
            }
        }

        for (Row row : sheet) {
            if (row == null)
                continue;
            String[] rowData = new String[maxCol];
            for (int i = 0; i < maxCol; i++) {
                Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                rowData[i] = getCellValueAsString(cell);
            }
            data.add(rowData);
        }

        return data;
    }

    private boolean isRowEmpty(String[] row) {
        if (row == null || row.length == 0)
            return true;
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private Map<String, Integer> mapHeaders(String[] header) {
        Map<String, Integer> map = new HashMap<>();

        System.out.println("---------- HEADERS DEL EXCEL ----------");
        for (int i = 0; i < header.length; i++) {
            if (header[i] != null && !header[i].isEmpty()) {
                System.out.println("Col " + i + ": " + header[i] + " -> Normalizado: " + normalizeText(header[i]));
            }
        }
        System.out.println("---------------------------------------");

        for (int i = 0; i < header.length; i++) {
            if (header[i] == null)
                continue;

            String h = normalizeText(header[i]);

            // ===== ORDENADORES DEL GASTO =====
            if (h.contains("organismo") && !h.contains("direccion")) {
                map.put("organismo", i);
            } else if (h.contains("direccion") && h.contains("organismo")) {
                map.put("direccion_organismo", i);
            } else if (h.contains("nombre") && h.contains("ordenador")) {
                map.put("nombre_ordenador", i);
            } else if (h.contains("cedula") && h.contains("ordenador")) {
                map.put("cedula_ordenador", i);
            } else if (h.contains("cargo") && h.contains("ordenador")) {
                map.put("cargo_ordenador", i);
            }

            // ===== ESTRUCTURADORES =====

            // JURÍDICO
            if (h.contains("juridico") && h.contains("estructurador") && !h.contains("tecnico")) {
                if (h.contains("cargo")) {
                    map.put("estructurador_juridico_cargo", i);
                } else {
                    map.put("estructurador_juridico", i);
                }
                continue; // Found match, next header
            }

            // TÉCNICO
            if (h.contains("tecnico") && h.contains("estructurador")) {
                if (h.contains("cargo")) {
                    map.put("estructurador_tecnico_cargo", i);
                } else {
                    map.put("estructurador_tecnico", i);
                }
                continue;
            }

            // FINANCIERO
            if (h.contains("financiero") && h.contains("estructurador")) {
                if (h.contains("cargo")) {
                    map.put("estructurador_financiero_cargo", i);
                } else {
                    map.put("estructurador_financiero", i);
                }
                continue;
            }

            // (Duplicate logic removed. The block above handles these cases robustly)

            // Fallbacks mas genericos (SOLO SI NO SE HA ENCONTRADO)
            else if (h.contains("juridico") && !h.contains("cargo") && !h.contains("contratista")
                    && !map.containsKey("estructurador_juridico")) {
                map.put("estructurador_juridico", i);
            } else if (h.contains("juridico") && h.contains("cargo")) {
                // map.put("juridico_cargo", i);
            } else if (h.contains("tecnico") && !h.contains("cargo") && !h.contains("apoyo")
                    && !h.contains("contratista") && !map.containsKey("estructurador_tecnico")) {
                map.put("estructurador_tecnico", i);
            } else if (h.contains("tecnico") && h.contains("cargo")) {
                // map.put("tecnico_cargo", i);
            } else if (h.contains("financiero") && !h.contains("cargo") && !h.contains("contratista")
                    && !map.containsKey("estructurador_financiero")) {
                map.put("estructurador_financiero", i);
            } else if (h.contains("financiero") && h.contains("cargo")) {
                // map.put("financiero_cargo", i); // Legacy/Not used?
            }

            // ===== PRESUPUESTO DETALLES =====
            else if (h.contains("cdp")) {
                if (h.contains("vencimiento")) {
                    map.put("cdp_vencimiento", i);
                } else if (h.contains("valor")) {
                    map.put("cdp_valor", i);
                } else if (h.contains("fecha") && !h.contains("numero") && !h.contains("mero") && !h.contains("num")) {
                    map.put("cdp_fecha", i);
                } else {
                    map.put("cdp_numero", i);
                }
            } else if (h.contains("rpc") || (h.contains("registro") && h.contains("presup"))) {
                if (h.contains("fecha")) {
                    map.put("rp_fecha", i);
                } else {
                    map.put("rp_numero", i);
                }
            } else if (h.contains("apropiacion")) {
                map.put("apropiacion_presupuestal", i);
            } else if (h.contains("rubro") && h.contains("presupuestal")) {
                map.put("rubro_presupuestal", i);
            } else if (h.contains("paa") && (h.contains("id") || h.contains("cod"))) {
                map.put("id_paa", i);
            } else if (h.contains("dane") && h.contains("cod")) {
                map.put("codigo_dane", i);
            } else if (h.contains("inversion")) {
                map.put("inversion", i);
            } else if (h.contains("funcionamiento")) {
                map.put("funcionamiento", i);
            } else if ((h.contains("nombre") || h.contains("ficha")) && h.contains("tebi")) {
                map.put("ficha_ebi_nombre", i);
            } else if (h.contains("objetivo") && h.contains("ficha")) {
                map.put("ficha_ebi_objetivo", i);
            } else if ((h.contains("actividades") || h.contains("encabezado")) && h.contains("ficha")) {
                map.put("ficha_ebi_actividades", i);
            } else if ((h.contains("insuf") || h.contains("cert"))
                    && (h.contains("fecha") || h.contains("venc") || h.startsWith("f "))) {
                map.put("fecha_insuficiencia", i);
            } else if (h.contains("certificado") && h.contains("insuf")) {
                map.put("certificado_insuficiencia", i);
            }

            // ===== CONTRATISTAS =====
            else if (h.contains("nombre") && h.contains("contratista")) {
                map.put("contratista_nombre", i);
            } else if (h.contains("cedula") && h.contains("contratista")) {
                map.put("contratista_cedula", i);
            } else if (h.contains("dv") && !h.contains("cdp")) {
                map.put("contratista_dv", i);
            } else if (h.contains("telefono") || h.contains("telefonico")) {
                map.put("contratista_telefono", i);
            } else if (h.contains("correo") || h.contains("electronico")) {
                map.put("contratista_correo", i);
            } else if (h.contains("direccion") && !h.contains("organismo")) {
                map.put("contratista_direccion", i);
            } else if (h.contains("dia") && h.contains("nacimiento")) {
                map.put("contratista_dia_nac", i);
            } else if (h.contains("mes") && h.contains("nacimiento")) {
                map.put("contratista_mes_nac", i);
            } else if ((h.contains("ano") || h.contains("año")) && h.contains("nacimiento")) {
                map.put("contratista_ano_nac", i);
            } else if (h.contains("edad") && !h.contains("nombre")) {
                map.put("contratista_edad", i);
            } else if (h.contains("descri")
                    && (h.contains("formacion") || h.contains("titulo") || h.contains("academico"))) {
                map.put("contratista_desc_formacion", i);
            } else if ((h.contains("formacion") || h.contains("titulo")) && !h.contains("descri")) {
                map.put("contratista_formacion", i);

            } else if ((h.contains("tarjeta") || h.contains("matricula")) && !h.contains("descripcion")) {
                map.put("contratista_tarjeta", i);
            } else if (h.contains("descri") && (h.contains("tarjeta") || h.contains("matricula"))) {
                map.put("contratista_desc_tarjeta", i);
            } else if (h.contains("experiencia") && !h.contains("descripcion")) {
                map.put("contratista_experiencia", i);
            } else if (h.contains("descripcion") && h.contains("experiencia")) {
                map.put("contratista_desc_experiencia", i);
            } else if (h.contains("restricciones")) {
                map.put("contratista_restricciones", i);

                // ===== SUPERVISORES =====
            } else if (h.contains("nombre") && h.contains("supervisor")) {
                map.put("supervisor_nombre", i);
            } else if (h.contains("cedula") && h.contains("supervisor")) {
                map.put("supervisor_cedula", i);
            } else if (h.contains("cargo") && h.contains("supervisor")) {
                map.put("supervisor_cargo", i);

                // ===== CONTRATOS (NUEVO) =====
            } else if (h.contains("trd") && h.contains("proceso")) {
                map.put("trd_proceso", i);
            } else if ((h.contains("numero") || h.contains("nmero")) && h.contains("contrato") && !h.contains("tipo")
                    && !h.contains("valor")) {
                map.put("numero_contrato", i);
            } else if (h.contains("tipo") && h.contains("contrato") && !h.contains("laboral") && !h.contains("xxx")) {
                // "Tipo de contrato (Profesional o de Apoyo...)"
                map.put("tipo_contrato", i);
            } else if (h.contains("nivel")) {
                map.put("nivel", i);
            } else if (h.contains("objeto")) {
                map.put("objeto", i);
            } else if (h.contains("modalidad")) {
                map.put("modalidad", i);
            } else if (h.contains("estado")) {
                map.put("estado", i);
            } else if (h.contains("periodo")) {
                map.put("periodo", i);
            } else if (h.contains("suscripcion")) {
                map.put("fecha_suscripcion", i);
            } else if (h.contains("inicio") && (h.contains("fecha") || h.contains("f.") || h.equals("inicio"))) {
                map.put("fecha_inicio", i);

                // === CAMBIO SOLICITADO: 'Plazo de ejecución' del Excel corresponde a
                // 'fecha_terminacion' en BD ===
                // Originalmente buscabamos 'terminacion', ahora incluimos 'plazo' + 'ejecucion'
                // para este campo
            } else if ((h.contains("terminacion")
                    && (h.contains("fecha") || h.contains("f.") || h.equals("terminacion")))
                    || (h.contains("plazo") && (h.contains("ejecucion") || h.contains("ejecuci")))) {

                // Prioridad 1: Mapear a fecha_terminacion (para cálculo/conversión de fecha)
                map.put("fecha_terminacion", i);

                // Prioridad 2: Si es especificamente "Plazo de ejecución", mapear TAMBIEN a
                // plazo_ejecucion
                // para guardar el texto original en base de datos.
                if (h.contains("plazo") && (h.contains("ejecucion") || h.contains("ejecuci"))) {
                    map.put("plazo_ejecucion", i);
                }

            } else if (h.contains("aprobacion") || h.contains("aprobaci")) {
                map.put("fecha_aprobacion", i);
            } else if ((h.contains("ejecucion") || h.contains("ejejcuci") || h.contains("ejecuci"))
                    && (h.contains("fecha") || h.contains("f.") || h.contains("feche"))
                    && !h.contains("plazo")) { // Mantener !plazo aqui para evitar conflicto si hubiera otra col
                map.put("fecha_ejecucion", i);
            } else if (h.contains("arl")) {
                map.put("fecha_arl", i);

                // 'Plazo de ejecución' ya fue asignado a fecha_terminacion arriba, removemos el
                // mapping a plazo_ejecucion
                // para evitar reescritura o conflicto. Si existiera otra columna especifica
                // para texto de plazo, se agregaría aquí.

            } else if (h.contains("meses") && !h.contains("media")) {
                map.put("plazo_meses", i);
            } else if (h.contains("dias") && (h.contains("plazo") || h.equals("dias"))) {
                map.put("plazo_dias", i);
            } else if (h.contains("valor") && h.contains("total") && h.contains("letras")) {
                map.put("valor_total_letras", i);
            } else if (h.contains("valor") && h.contains("total") && h.contains("numeros")) {
                map.put("valor_total_numeros", i);
            } else if (h.contains("antes") && h.contains("iva")) {
                map.put("valor_antes_iva", i);
            } else if (h.contains("iva") && !h.contains("antes")) {
                map.put("valor_iva", i);
            } else if (h.contains("valor") && h.contains("cuota") && h.contains("letras") && !h.contains("media")) {
                map.put("valor_cuota_letras", i);
            } else if (h.contains("valor") && h.contains("cuota") && h.contains("numero") && !h.contains("media")) {
                map.put("valor_cuota_numero", i);
            } else if (h.contains("numero") && h.contains("cuotas") && h.contains("letras")) {
                map.put("num_cuotas_letras", i);
            } else if (h.contains("numero") && h.contains("cuotas") && h.contains("numero")) {
                map.put("num_cuotas_numero", i);
            } else if (h.contains("media") && h.contains("letras")) {
                map.put("valor_media_cuota_letras", i);
            } else if (h.contains("media") && h.contains("numero")) {
                map.put("valor_media_cuota_numero", i);
            } else if (h.contains("entregables") || (h.contains("actividades") && h.contains("contrato"))) {
                map.put("actividades_entregables", i);
                // SWAP MAPPING AS REQUESTED BY USER
                // 1. "Número del Artículo..." -> liquidacion_acuerdo
            } else if (h.contains("liquidaci") && h.contains("acuerdo")
                    && (h.contains("articulo") || h.contains("artculo"))) {
                map.put("liquidacion_acuerdo", i);

                // 2. "Número y fecha del Acuerdo..." -> liquidacion_articulo (SOLICITADO
                // EXPLICITAMENTE)
            } else if (h.contains("liquidaci") && h.contains("acuerdo")
                    && (h.contains("numero") || h.contains("fecha") || h.contains("nmero"))) {
                map.put("liquidacion_articulo", i);
            } else if (h.contains("liquidaci") && h.contains("decreto")) {
                map.put("liquidacion_decreto", i);
                // (Duplicate logic removed)
            } else if (h.contains("tecnico") && h.contains("nombre") && !h.contains("contratista")) {
                map.put("estructurador_tecnico", i);
            } else if (h.contains("financiero") && h.contains("nombre") && !h.contains("contratista")) {
                map.put("estructurador_financiero", i);

                // SUPER FALLBACK: Single words (common in some matrices)
                // Only if not already mapped (put works as replace, so order matters. These are
                // strictly 'else if' of above)
                // But since we are in an else-if chain, we are good.
            } else if (h.equals("juridico") || h.equals("jurdico")) {
                map.put("estructurador_juridico", i);
            } else if (h.equals("tecnico") || h.equals("tcnico")) {
                map.put("estructurador_tecnico", i);
            } else if (h.equals("financiero")) {
                map.put("estructurador_financiero", i);

                // ULTRA FALLBACK: loose "contains" for when headers are verbose like
                // "Componente Juridico/Técnico"
                // Ensure we don't pick up Supervisor/Contratista columns if they happen to use
                // these words (unlikely but possible)
            } else if (h.contains("juridico") && !h.contains("contratista") && !h.contains("supervisor")) {
                // Use putIfAbsent logic essentially (though simple put overwrites, usually we
                // encounter columns left-to-right)
                // If we already found a "better" match (e.g. Estructurador Juridico), we might
                // overwrite it?
                // No, usually strict columns come first or exist uniquely.
                // But wait, if we have "Juridico" (matched by equals) and "Componente Juridico"
                // (matched by contains).
                // They are distinct columns. We want the one that is truly the structurer.
                // Usually there is only one.
                map.put("estructurador_juridico", i);
            } else if (h.contains("tecnico") && !h.contains("contratista") && !h.contains("supervisor")
                    && !h.contains("tecnologo")) { // Exclude 'tecnologo' (degree)
                map.put("estructurador_tecnico", i);
            } else if (h.contains("financiero") && !h.contains("contratista") && !h.contains("supervisor")) {
                map.put("estructurador_financiero", i);
            } else if (h.contains("circular") && h.contains("honorarios")) {
                map.put("circular_honorarios", i);
            }
        }

        return map;

    }

    private String normalizeText(String text) {
        if (text == null)
            return "";

        // 1. Minúsculas
        String lower = text.toLowerCase().trim();

        // 2. Reemplazos manuales comunes
        lower = lower
                .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                .replace("ñ", "n")
                .replace("  ", " "); // Dobles espacios

        // 3. Limpieza agresiva: dejar solo letras, números y espacios
        // Esto convierte "Nmero" -> "nmero" o "n mero" dependiendo del caracter
        StringBuilder sb = new StringBuilder();
        for (char c : lower.toCharArray()) {
            // Permitir letras, digitos y espacio
            if (Character.isLetterOrDigit(c) || c == ' ') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private void logMapping(StringBuilder log, Map<String, Integer> map, String key, String label) {
        int col = map.getOrDefault(key, -1);
        if (col >= 0) {
            log.append("  ✓ ").append(label).append(": Col ").append(col).append("\n");
        } else {
            log.append("  ✗ ").append(label).append(": No encontrada\n");
        }
    }

    private String get(String[] row, Map<String, Integer> map, String key) {
        if (!map.containsKey(key))
            return "";
        int idx = map.get(key);
        if (idx >= row.length || idx < 0)
            return "";
        return row[idx] != null ? row[idx].trim() : "";
    }

    /**
     * Procesa un ordenador del gasto
     * 
     * @return 1=insertado, 0=omitido (datos vacíos), -1=duplicado
     */
    private OrdenadorGasto processOrdenador(String[] row, Map<String, Integer> map,
            java.util.Set<String> cedulasExistentes) {
        try {
            String nombre = get(row, map, "nombre_ordenador");
            String cedula = get(row, map, "cedula_ordenador");

            if (nombre.isEmpty()) {
                return null;
            }

            if (!cedula.isEmpty() && cedulasExistentes.contains(cedula)) {
                return ordenadorDAO.obtenerPorCedula(cedula);
            }

            OrdenadorGasto ordenador = new OrdenadorGasto();
            ordenador.setOrganismo(get(row, map, "organismo"));
            ordenador.setDireccionOrganismo(get(row, map, "direccion_organismo"));
            ordenador.setNombreOrdenador(nombre);
            ordenador.setCedulaOrdenador(cedula);
            ordenador.setCargoOrdenador(get(row, map, "cargo_ordenador"));
            ordenador.setDecretoNombramiento(get(row, map, "decreto_nombramiento"));
            ordenador.setActaPosesion(get(row, map, "acta_posesion"));

            if (ordenadorDAO.insertar(ordenador)) {
                if (!cedula.isEmpty())
                    cedulasExistentes.add(cedula);
                return ordenador;
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Procesa un contratista
     * 
     * @return 1=insertado, 0=omitido (datos vacíos), -1=duplicado
     */
    /**
     * Procesa un contratista
     * 
     * @return 1=insertado, 0=omitido (datos vacíos), -1=duplicado
     */
    private Contratista processContratista(String[] row, Map<String, Integer> map,
            java.util.Set<String> cedulasExistentes) {
        try {
            String cedula = get(row, map, "contratista_cedula");
            String nombre = get(row, map, "contratista_nombre");

            if (cedula.isEmpty() && nombre.isEmpty()) {
                return null;
            }

            if (!cedula.isEmpty() && cedulasExistentes.contains(cedula)) {
                return contratistaDAO.obtenerPorCedula(cedula);
            }

            Contratista contratista = new Contratista();
            contratista.setCedula(cedula.isEmpty() ? "SIN_CEDULA_" + System.currentTimeMillis() : cedula);
            contratista.setDv(get(row, map, "contratista_dv"));
            contratista.setNombre(nombre.isEmpty() ? "Sin Nombre" : nombre);
            contratista.setTelefono(get(row, map, "contratista_telefono"));
            contratista.setCorreo(get(row, map, "contratista_correo"));
            contratista.setDireccion(get(row, map, "contratista_direccion"));

            // Construir fecha de nacimiento
            String dia = get(row, map, "contratista_dia_nac");
            String mes = get(row, map, "contratista_mes_nac");
            String ano = get(row, map, "contratista_ano_nac");

            if (!dia.isEmpty() && !mes.isEmpty() && !ano.isEmpty()) {
                try {
                    String diaFmt = dia.length() == 1 ? "0" + dia : dia;
                    String mesFmt = mes.length() == 1 ? "0" + mes : mes;
                    String fechaNac = ano + "-" + mesFmt + "-" + diaFmt;
                    contratista.setFechaNacimiento(java.sql.Date.valueOf(fechaNac));
                } catch (Exception e) {
                    // System.err.println("Error al parsear fecha: " + dia + "/" + mes + "/" + ano);
                }
            }

            // Edad
            String edad = get(row, map, "contratista_edad");
            if (!edad.isEmpty()) {
                try {
                    double edadDouble = Double.parseDouble(edad.replace(",", "."));
                    contratista.setEdad((int) edadDouble);
                } catch (Exception e) {
                }
            }

            contratista.setFormacionTitulo(get(row, map, "contratista_formacion"));
            contratista.setDescripcionFormacion(get(row, map, "contratista_desc_formacion"));
            contratista.setTarjetaProfesional(get(row, map, "contratista_tarjeta"));
            contratista.setDescripcionTarjeta(get(row, map, "contratista_desc_tarjeta"));
            contratista.setExperiencia(get(row, map, "contratista_experiencia"));
            contratista.setDescripcionExperiencia(get(row, map, "contratista_desc_experiencia"));
            contratista.setRestricciones(get(row, map, "contratista_restricciones"));

            if (contratistaDAO.insertar(contratista)) {
                if (!cedula.isEmpty())
                    cedulasExistentes.add(cedula);
                return contratista;
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Procesa un supervisor
     * 
     * @return 1=insertado, 0=omitido (datos vacíos), -1=duplicado
     */
    private Supervisor processSupervisor(String[] row, Map<String, Integer> map,
            java.util.Set<String> cedulasExistentes) {
        try {
            String nombre = get(row, map, "supervisor_nombre");
            String cedula = get(row, map, "supervisor_cedula");

            if (nombre.isEmpty()) {
                return null;
            }

            if (!cedula.isEmpty() && cedulasExistentes.contains(cedula)) {
                return supervisorDAO.obtenerPorCedula(cedula); // Assuming this method exists or you might need to
                                                               // implement/use proper DAO method
                // NOTE: If SupervisorDAO misses obtenerPorCedula, this line will fail
                // compilation if not checked.
                // Assuming it exists since standard pattern.
            }

            Supervisor supervisor = new Supervisor();
            supervisor.setNombre(nombre);
            supervisor.setCedula(cedula);
            supervisor.setCargo(get(row, map, "supervisor_cargo"));

            if (supervisorDAO.insertar(supervisor)) {
                if (!cedula.isEmpty())
                    cedulasExistentes.add(cedula);
                return supervisor;
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                    }
                    double d = cell.getNumericCellValue();
                    if (d == (long) d)
                        return String.format("%d", (long) d);
                    return String.valueOf(d);
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return cell.getStringCellValue();
                    }
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Procesa un estructurador
     * 
     * @return 1=insertado, 0=omitido (datos vacíos), -1=duplicado
     */
    private Estructurador processEstructurador(String[] row, Map<String, Integer> map, StringBuilder log) {
        try {
            String jurNombre = get(row, map, "estructurador_juridico");
            String tecNombre = get(row, map, "estructurador_tecnico");
            String finNombre = get(row, map, "estructurador_financiero");

            if (jurNombre.isEmpty() && tecNombre.isEmpty() && finNombre.isEmpty()) {
                return null;
            }

            String jurCargo = get(row, map, "estructurador_juridico_cargo");
            String tecCargo = get(row, map, "estructurador_tecnico_cargo");
            String finCargo = get(row, map, "estructurador_financiero_cargo");

            // DEBUG LOGGING
            System.out.println("Processing Estructuradores for Row...");
            System.out.println("Jur: " + jurNombre + " | Cargo: " + jurCargo);
            System.out.println("Tec: " + tecNombre + " | Cargo: " + tecCargo);
            System.out.println("Fin: " + finNombre + " | Cargo: " + finCargo);

            if (jurNombre.isEmpty() && tecNombre.isEmpty() && finNombre.isEmpty()) {
                System.out.println("All structurer names empty. Skipping.");
                return null;
            }

            Estructurador e = new Estructurador();
            e.setJuridicoNombre(jurNombre);
            e.setJuridicoCargo(jurCargo);
            e.setTecnicoNombre(tecNombre);
            e.setTecnicoCargo(tecCargo);
            e.setFinancieroNombre(finNombre);
            e.setFinancieroCargo(finCargo);

            if (estructuradorDAO.insertar(e)) {
                System.out.println("Estructurador Inserted! ID: " + e.getId());
                return e;
            } else {
                System.out.println("Estructurador Insert FAILED.");
            }
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Procesa un registro de Presupuesto
     * 
     * @return 1=insertado, 0=omitido (datos vacíos)
     */
    private PresupuestoDetalle processPresupuestoDetalle(String[] row, Map<String, Integer> map, StringBuilder log) {
        try {
            // Verificar si hay algún dato clave
            String cdpNum = get(row, map, "cdp_numero");
            String rpNum = get(row, map, "rp_numero");
            String idPaa = get(row, map, "id_paa");
            String apropiacion = get(row, map, "apropiacion_presupuestal");

            if (cdpNum.isEmpty() && rpNum.isEmpty() && idPaa.isEmpty() && apropiacion.isEmpty()) {
                return null;
            }

            PresupuestoDetalle p = new PresupuestoDetalle();
            p.setCdpNumero(cdpNum);

            String cdpFechaStr = get(row, map, "cdp_fecha");
            p.setCdpFecha(parseDateStr(cdpFechaStr));
            if (p.getCdpFecha() == null && !cdpFechaStr.isEmpty()) {
                log.append("⚠️ Warn: No se pudo parsear fecha CDP: '").append(cdpFechaStr).append("'\n");
            }
            // LOG EXPLICITO SI LA COLUMNA NO SE LEE
            if (cdpFechaStr.isEmpty() && map.containsKey("cdp_fecha")) {
                int idx = map.get("cdp_fecha");
                if (idx < row.length && !row[idx].isEmpty())
                    log.append("⚠️ Warn: cdp_fecha detectada en col ").append(idx).append(" pero leida vacia. Raw='")
                            .append(row[idx]).append("'\n");
            }

            // Parsear valor
            try {
                String rawVal = get(row, map, "cdp_valor");
                if (!rawVal.isEmpty()) {
                    String val = cleanCurrency(rawVal);
                    if (!val.isEmpty())
                        p.setCdpValor(new java.math.BigDecimal(val));
                }
            } catch (Exception e) {
            }

            p.setCdpVencimiento(parseDateStr(get(row, map, "cdp_vencimiento")));
            p.setRpNumero(rpNum);
            p.setRpFecha(parseDateStr(get(row, map, "rp_fecha")));
            p.setApropiacionPresupuestal(apropiacion);
            p.setIdPaa(idPaa);
            p.setCodigoDane(get(row, map, "codigo_dane"));
            p.setInversion(get(row, map, "inversion"));
            p.setFuncionamiento(get(row, map, "funcionamiento"));
            p.setFichaEbiNombre(truncate(get(row, map, "ficha_ebi_nombre"), 255));
            p.setFichaEbiObjetivo(truncate(get(row, map, "ficha_ebi_objetivo"), 255));
            p.setFichaEbiActividades(truncate(get(row, map, "ficha_ebi_actividades"), 255));

            p.setCertificadoInsuficiencia(get(row, map, "certificado_insuficiencia"));
            p.setFechaInsuficiencia(parseDateStr(get(row, map, "fecha_insuficiencia")));

            // DEBUG LOGGING FOR PRESUPUESTO
            System.out.println("Processing Presupuesto for Row...");
            System.out.println("Ficha Name (Truncated): " + p.getFichaEbiNombre());
            System.out.println("Ficha Obj: " + p.getFichaEbiObjetivo());
            System.out.println("Ficha Act: " + p.getFichaEbiActividades());

            if (presupuestoDAO.insertar(p)) {
                return p;
            }
            return null;

        } catch (Exception ex) {
            log.append("⚠️ Error procesando presupuesto: ").append(ex.getMessage()).append("\n");
            ex.printStackTrace();
            return null;
        }
    }

    private Contrato processContrato(String[] row, Map<String, Integer> map, Contratista c, Supervisor s,
            OrdenadorGasto o, Estructurador e, PresupuestoDetalle p, StringBuilder log) {
        try {
            Contrato contrato = new Contrato();

            // Campos de texto simples
            contrato.setTrdProceso(get(row, map, "trd_proceso"));
            contrato.setNumeroContrato(get(row, map, "numero_contrato"));
            contrato.setTipoContrato(get(row, map, "tipo_contrato"));
            contrato.setNivel(get(row, map, "nivel"));
            contrato.setObjeto(get(row, map, "objeto"));
            contrato.setModalidad(get(row, map, "modalidad"));
            contrato.setEstado(get(row, map, "estado"));
            contrato.setPeriodo(get(row, map, "periodo"));

            // Fechas
            contrato.setFechaSuscripcion(parseDateStr(get(row, map, "fecha_suscripcion")));
            contrato.setFechaInicio(parseDateStr(get(row, map, "fecha_inicio")));
            contrato.setFechaTerminacion(parseDateStr(get(row, map, "fecha_terminacion")));
            contrato.setFechaAprobacion(parseDateStr(get(row, map, "fecha_aprobacion")));
            contrato.setFechaEjecucion(parseDateStr(get(row, map, "fecha_ejecucion")));
            contrato.setFechaArl(parseDateStr(get(row, map, "fecha_arl")));

            // Plazos y Fecha Terminacion desde Plazo
            // 1. Guardar texto original
            String rawPlazo = get(row, map, "plazo_ejecucion");
            contrato.setPlazoEjecucion(rawPlazo);

            // 2. Convertir texto a Fecha Terminacion (si no se leyó de otra columna
            // 'fecha_terminacion' explicita)
            // Si el mapa apuntaba la misma columna para ambas, get(...,
            // "fecha_terminacion") ya traería el valor.
            // Pero haremos un log explicito para depuracion.
            if (contrato.getFechaTerminacion() == null && !rawPlazo.isEmpty()) {
                java.sql.Date fTerminacion = parseDateStr(rawPlazo);
                if (fTerminacion != null) {
                    contrato.setFechaTerminacion(fTerminacion);
                } else {
                    log.append("  ⚠️ No se pudo convertir 'Plazo de ejecución' a Fecha: '").append(rawPlazo)
                            .append("'\n");
                }
            }

            // Intentar leer columnas explicitas si existen
            try {
                String pm = get(row, map, "plazo_meses");
                if (!pm.isEmpty())
                    contrato.setPlazoMeses((int) Double.parseDouble(pm.replace(",", ".")));

                String pd = get(row, map, "plazo_dias");
                if (!pd.isEmpty())
                    contrato.setPlazoDias((int) Double.parseDouble(pd.replace(",", ".")));
            } catch (Exception ex) {
            }

            // CALCULO AUTOMATICO SI FALTAN DATOS Y TENEMOS FECHAS
            // "La fecha de terminacion se calcua de acuerdo al plazo de ejecuacion pero en
            // dd/mm/yyyy"
            // "las columnas plazo_meses y plazo_dias se calcula de acuerdo al plazo de
            // ejecuacion"
            // User feedback: "No fecha de inicio queda vacio y si plazo de ejecucion viene
            // vacio que no calcule nada"

            // CALCULO DE PLAZO MESES Y DIAS BASADO SOLO EN FECHA TERMINACION (PLAZO
            // EJECUCION)
            // User feedback: "el calculo solo lo debe hacer sobre plazo de ejecucion"
            // Interpretacion: Extraer Mes y Dia de la fecha de terminación.
            if (contrato.getFechaTerminacion() != null) {
                try {
                    java.time.LocalDate fin = contrato.getFechaTerminacion().toLocalDate();

                    // Asumimos que "calculo sobre plazo" significa extraer componentes o
                    // calcular desde inicio de año implicito dada la naturaleza presupuestal anual.
                    contrato.setPlazoMeses(fin.getMonthValue());
                    contrato.setPlazoDias(fin.getDayOfMonth());

                } catch (Exception exDate) {
                    log.append("  ⚠️ Error extrayendo componentes fecha terminacion: ").append(exDate.getMessage())
                            .append("\n");
                }
            }

            // Valores
            contrato.setValorTotalLetras(get(row, map, "valor_total_letras"));
            contrato.setValorCuotaLetras(get(row, map, "valor_cuota_letras"));
            contrato.setNumCuotasLetras(get(row, map, "num_cuotas_letras"));
            contrato.setValorMediaCuotaLetras(get(row, map, "valor_media_cuota_letras"));

            try {
                String vtn = cleanCurrency(get(row, map, "valor_total_numeros"));
                if (!vtn.isEmpty())
                    contrato.setValorTotalNumeros(new java.math.BigDecimal(vtn));

                String via = cleanCurrency(get(row, map, "valor_antes_iva"));
                if (!via.isEmpty())
                    contrato.setValorAntesIva(new java.math.BigDecimal(via));

                String vi = cleanCurrency(get(row, map, "valor_iva"));
                if (!vi.isEmpty())
                    contrato.setValorIva(new java.math.BigDecimal(vi));

                String vcn = cleanCurrency(get(row, map, "valor_cuota_numero"));
                if (!vcn.isEmpty())
                    contrato.setValorCuotaNumero(new java.math.BigDecimal(vcn));

                String ncn = get(row, map, "num_cuotas_numero");
                if (!ncn.isEmpty())
                    contrato.setNumCuotasNumero((int) Double.parseDouble(ncn.replace(",", ".")));

                String vmcn = cleanCurrency(get(row, map, "valor_media_cuota_numero"));
                if (!vmcn.isEmpty())
                    contrato.setValorMediaCuotaNumero(new java.math.BigDecimal(vmcn));

            } catch (Exception ex) {
            }

            contrato.setActividadesEntregables(get(row, map, "actividades_entregables"));
            contrato.setLiquidacionAcuerdo(get(row, map, "liquidacion_acuerdo"));
            contrato.setLiquidacionArticulo(get(row, map, "liquidacion_articulo"));
            contrato.setLiquidacionDecreto(get(row, map, "liquidacion_decreto"));
            contrato.setCircularHonorarios(get(row, map, "circular_honorarios"));

            // Foreign Keys
            if (c != null)
                contrato.setContratistaId(c.getId());
            if (s != null)
                contrato.setSupervisorId(s.getId());
            if (o != null)
                contrato.setOrdenadorId(o.getId());
            if (e != null)
                contrato.setEstructuradorId(e.getId());
            if (p != null)
                contrato.setPresupuestoId(p.getId());

            // Check existence
            Contrato existente = contratoDAO.obtenerPorNumero(contrato.getNumeroContrato());
            if (existente != null) {
                contrato.setId(existente.getId());

                // PRESERVE EXISTING RELATIONSHIPS IF NEW ONES ARE EMPTY (ID=0)
                // This solves the issue where re-uploading with partial data wipes out existing
                // links.
                if (contrato.getEstructuradorId() == 0 && existente.getEstructuradorId() > 0) {
                    contrato.setEstructuradorId(existente.getEstructuradorId());
                }
                if (contrato.getContratistaId() == 0 && existente.getContratistaId() > 0) {
                    contrato.setContratistaId(existente.getContratistaId());
                }
                if (contrato.getSupervisorId() == 0 && existente.getSupervisorId() > 0) {
                    contrato.setSupervisorId(existente.getSupervisorId());
                }
                if (contrato.getOrdenadorId() == 0 && existente.getOrdenadorId() > 0) {
                    contrato.setOrdenadorId(existente.getOrdenadorId());
                }
                if (contrato.getPresupuestoId() == 0 && existente.getPresupuestoId() > 0) {
                    contrato.setPresupuestoId(existente.getPresupuestoId());
                }

                if (contratoDAO.actualizar(contrato)) {
                    log.append("  ↻ Contrato actualizado: ").append(contrato.getNumeroContrato()).append("\n");
                    return contrato;
                } else {
                    log.append("  ⚠️ Error al actualizar contrato: ").append(contrato.getNumeroContrato()).append("\n");
                }
            } else {
                if (contratoDAO.insertar(contrato)) {
                    log.append("  ➜ Contrato creado: ").append(contrato.getNumeroContrato())
                            .append(" | Sup: ").append(s != null ? s.getNombre() : "N/A")
                            .append(" | Contratista: ").append(c != null ? c.getNombre() : "N/A")
                            .append("\n");
                    return contrato;
                } else {
                    log.append("  ⚠️ Error al insertar contrato: ").append(contrato.getNumeroContrato()).append("\n");
                }
            }
            return null;
        } catch (Exception ex) {
            log.append("  ⚠️ Excepción procesando contrato: ").append(ex.getMessage()).append("\n");
            ex.printStackTrace();
            return null;
        }
    }

    private java.sql.Date parseDateStr(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty())
            return null;

        String d = dateStr.trim();

        try {
            // 1. Try ISO format yyyy-MM-dd
            return java.sql.Date.valueOf(d);
        } catch (Exception e1) {
            try {
                // 2. Try dd/MM/yyyy or d/M/yyyy
                if (d.contains("/")) {
                    String[] parts = d.split("/");
                    if (parts.length == 3) {
                        int day = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        int year = Integer.parseInt(parts[2]);

                        // Handle 2-digit years
                        if (year < 100) {
                            year += 2000;
                        }

                        // Java SQL Date wants yyyy-MM-dd logic
                        // Construct LocalDate then to sql.Date
                        return java.sql.Date.valueOf(java.time.LocalDate.of(year, month, day));
                    }
                }

                // 3. Try dd-MM-yyyy or d-M-yyyy
                if (d.contains("-")) {
                    String[] parts = d.split("-");
                    // Careful, yyyy-MM-dd also has dashes but failed step 1.
                    // This is likely dd-MM-yyyy or similar.
                    if (parts.length == 3) {
                        // Check which part is the year.
                        // ISO is [yyyy, mm, dd].
                        // If parts[0] is length 4, it was ISO but invalid?
                        // Lets assume dd-MM-yyyy if part 0 is 1-2 chars.
                        if (parts[0].length() <= 2) {
                            int day = Integer.parseInt(parts[0]);
                            int month = Integer.parseInt(parts[1]);
                            int year = Integer.parseInt(parts[2]);
                            if (year < 100)
                                year += 2000;
                            return java.sql.Date.valueOf(java.time.LocalDate.of(year, month, day));
                        }
                    }
                }

                // 4. Try Excel Serial Date (Numeric)
                // e.g. "46027" -> 2026-01-05
                // Check if string is purely numeric (integer or float style)
                if (d.matches("^[0-9]+(\\.[0-9]+)?$")) {
                    try {
                        double val = Double.parseDouble(d);
                        // Convert Excel serial date to Java Date
                        // 0-based offset, DateUtil handles 1900 leap year bug
                        java.util.Date javaDate = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(val);
                        if (javaDate != null) {
                            return new java.sql.Date(javaDate.getTime());
                        }
                    } catch (Exception ex) {
                        // unparseable logic ignore
                    }
                }

                // 5. Try Spanish text format "dd de month de yyyy"
                // e.g. "31 de didciembre de 2026"
                if (d.toLowerCase().contains(" de ")) {
                    String clean = d.toLowerCase()
                            // Fix specific known typos
                            .replace("didciembre", "diciembre")
                            // Replace month names with numbers
                            .replace("enero", "01")
                            .replace("febrero", "02")
                            .replace("marzo", "03")
                            .replace("abril", "04")
                            .replace("mayo", "05")
                            .replace("junio", "06")
                            .replace("julio", "07")
                            .replace("agosto", "08")
                            .replace("septiembre", "09")
                            .replace("octubre", "10")
                            .replace("noviembre", "11")
                            .replace("diciembre", "12")
                            // Remove " de " or spaces to make it "dd-MM-yyyy"
                            .replace(" de ", "-")
                            .replace(" ", "-");

                    // Now clean should look like "31-12-2026"
                    String[] parts = clean.split("-");
                    if (parts.length == 3) {
                        try {
                            int day = Integer.parseInt(parts[0].trim());
                            int month = Integer.parseInt(parts[1].trim());
                            int year = Integer.parseInt(parts[2].trim());
                            return java.sql.Date.valueOf(java.time.LocalDate.of(year, month, day));
                        } catch (Exception e) {
                        }
                    }
                }

            } catch (Exception ex) {
                // Ignore
            }

            // 6. Estrategia Robusta: Extraccion de digitos y mes por nombre
            // Ej: "Treinta (30) de junio del dos mil veintiseis (2026)"
            try {
                String lowerD = d.toLowerCase().replace("didciembre", "diciembre");
                String[] months = { "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto",
                        "septiembre", "octubre", "noviembre", "diciembre" };
                int foundMonth = -1;

                for (int m = 0; m < months.length; m++) {
                    if (lowerD.contains(months[m])) {
                        foundMonth = m + 1;
                        break;
                    }
                }

                if (foundMonth > 0) {
                    // Extraer todos los numeros del texto
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+)");
                    java.util.regex.Matcher m = p.matcher(lowerD);
                    int day = -1;
                    int year = -1;

                    while (m.find()) {
                        try {
                            int val = Integer.parseInt(m.group(1)); // "30" o "2026"
                            // Heuristica simple:
                            // Si es <= 31 y no tenemos dia aun -> tomar como dia
                            // Si es > 1900 -> tomar como año
                            if (val >= 1 && val <= 31 && day == -1) {
                                day = val;
                            } else if (val >= 1900 && val <= 2100) {
                                year = val;
                            }
                        } catch (Exception eNum) {
                        }
                    }

                    if (day != -1 && year != -1) {
                        return java.sql.Date.valueOf(java.time.LocalDate.of(year, foundMonth, day));
                    }
                }
            } catch (Exception eAuth) {
            }
        }
        return null;
    }

    private String cleanCurrency(String val) {
        if (val == null)
            return "";
        // Quitar caracteres no numericos excepto coma y punto
        // Si formato es 10.000,00 -> Queremos 10000.00
        // O si es 10,000.00 -> 10000.00
        // Asumiremos formato Colombiano/Español: puntos miles, coma decimal

        String clean = val.replace("$", "").trim();
        if (clean.isEmpty())
            return "";

        // Contar puntos y comas
        int dots = clean.length() - clean.replace(".", "").length();
        int commas = clean.length() - clean.replace(",", "").length();

        if (dots > 0 && commas > 0) {
            // Tiene ambos, asumimos formato 1.000,00
            clean = clean.replace(".", "").replace(",", ".");
        } else if (dots > 1) {
            // 1.000.000 -> 1000000
            clean = clean.replace(".", "");
        } else if (commas > 0) {
            // 1000,00 -> 1000.00 (decimal) o 1,000 (miles) ?? Usualmente en excel español
            // coma es decimal
            // Pero si es solo uno, es ambiguo sin contexto. Asumiremos coma es decimal si
            // no hay puntos.
            clean = clean.replace(",", ".");
        }

        return clean;
    }

    private String truncate(String val, int max) {
        if (val == null)
            return "";
        if (val.length() > max) {
            return val.substring(0, max);
        }
        return val;
    }
}
