package com.combinacion.servlets;

import com.combinacion.dao.SupervisorDAO;
import com.combinacion.dao.OrdenadorGastoDAO;
import com.combinacion.dao.ContratistaDAO;
import com.combinacion.dao.EstructuradorDAO;
import com.combinacion.models.OrdenadorGasto;
import com.combinacion.models.Contratista;
import com.combinacion.models.Supervisor;
import com.combinacion.models.Estructurador;

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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        Part filePart = request.getPart("file");

        int ordenadoresCount = 0;
        int contratistasCount = 0;
        int supervisoresCount = 0;
        int duplicadosOrdenadores = 0;
        int duplicadosContratistas = 0;
        int duplicadosSupervisores = 0;
        int estructuradoresCount = 0;
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
                    // CSV
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileContent, "UTF-8"))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.trim().isEmpty())
                                continue;
                            allRows.add(line.split(";"));
                        }
                    }
                }

                if (allRows.isEmpty()) {
                    throw new Exception("El archivo está vacío.");
                }

                // 2. MAP COLUMNS BASED ON HEADER (First Row)
                String[] header = allRows.get(0);
                Map<String, Integer> map = mapHeaders(header);

                log.append("═══ CARGA MASIVA - ORDENADORES Y CONTRATISTAS ═══\n\n");
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
                logMapping(log, map, "juridico_nombre", "Jurídico Nombre");
                logMapping(log, map, "juridico_cargo", "Jurídico Cargo");
                logMapping(log, map, "tecnico_nombre", "Técnico Nombre");
                logMapping(log, map, "tecnico_cargo", "Técnico Cargo");
                logMapping(log, map, "financiero_nombre", "Financiero Nombre");
                logMapping(log, map, "financiero_cargo", "Financiero Cargo");

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

                log.append("\n═════════════════════════════════════════\n\n");
                System.out.println(log.toString());

                // 3. CACHE EXISTING DATA (Optimización para evitar N+1 queries)
                java.util.Set<String> cedulasOrdenadoresExistentes = ordenadorDAO.obtenerTodasLasCedulas();
                java.util.Set<String> cedulasContratistasExistentes = contratistaDAO.obtenerTodasLasCedulas();
                java.util.Set<String> cedulasSupervisoresExistentes = supervisorDAO.obtenerTodasLasCedulas();

                // 4. PROCESS DATA ROWS
                for (int i = 1; i < allRows.size(); i++) {
                    String[] row = allRows.get(i);

                    // Skip completely empty rows
                    if (isRowEmpty(row))
                        continue;

                    try {
                        // Intentar procesar como Ordenador
                        // Retorna: 1=insertado, 0=omitido, -1=duplicado
                        int resultOrdenador = processOrdenador(row, map, cedulasOrdenadoresExistentes);
                        if (resultOrdenador == 1) {
                            ordenadoresCount++;
                        } else if (resultOrdenador == -1) {
                            duplicadosOrdenadores++;
                        }

                        // Intentar procesar como Contratista
                        int resultContratista = processContratista(row, map, cedulasContratistasExistentes);
                        if (resultContratista == 1) {
                            contratistasCount++;
                        } else if (resultContratista == -1) {
                            duplicadosContratistas++;
                        }

                        // Intentar procesar como Supervisor
                        int resultSupervisor = processSupervisor(row, map, cedulasSupervisoresExistentes);
                        if (resultSupervisor == 1) {
                            supervisoresCount++;
                        } else if (resultSupervisor == -1) {
                            duplicadosSupervisores++;
                        }

                        // Intentar procesar como Estructurador
                        int resultEstructurador = processEstructurador(row, map, log);
                        if (resultEstructurador == 1) {
                            estructuradoresCount++;
                        }

                        // Si no se procesó ninguno, contar como error
                        if (resultOrdenador == 0 && resultContratista == 0 && resultSupervisor == 0
                                && resultEstructurador == 0) {
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
                if (duplicadosOrdenadores > 0) {
                    if (msg.length() > 0)
                        msg.append(" | ");
                    msg.append("⚪ Ordenadores duplicados: ").append(duplicadosOrdenadores);
                }
                if (contratistasCount > 0) {
                    if (msg.length() > 0)
                        msg.append("<br>");
                    msg.append("✅ Contratistas: ").append(contratistasCount);
                }
                if (duplicadosContratistas > 0) {
                    if (msg.length() > 0)
                        msg.append(" | ");
                    msg.append("⚪ Contratistas duplicados: ").append(duplicadosContratistas);
                }

                if (supervisoresCount > 0) {
                    if (msg.length() > 0)
                        msg.append("<br>");
                    msg.append("✅ Supervisores: ").append(supervisoresCount);
                }
                if (duplicadosSupervisores > 0) {
                    if (msg.length() > 0)
                        msg.append(" | ");
                    msg.append("⚪ Supervisores duplicados: ").append(duplicadosSupervisores);
                }

                if (estructuradoresCount > 0) {
                    if (msg.length() > 0)
                        msg.append("<br>");
                    msg.append("✅ Estructuradores: ").append(estructuradoresCount);
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
            } else if (h.contains("decreto") && h.contains("nombramiento")) {
                map.put("decreto_nombramiento", i);
            } else if (h.contains("acta") && h.contains("posesion")) {
                map.put("acta_posesion", i);
            }

            // ===== ESTRUCTURADORES =====
            // ===== ESTRUCTURADORES =====
            // ===== ESTRUCTURADORES =====
            else if (h.contains("juridico") && !h.contains("cargo")) {
                map.put("juridico_nombre", i);
            } else if (h.contains("juridico") && h.contains("cargo")) {
                map.put("juridico_cargo", i);
            } else if (h.contains("tecnico") && !h.contains("cargo") && !h.contains("apoyo")) {
                // !apoyo para evitar confusión con otros roles si los hubiera
                map.put("tecnico_nombre", i);
            } else if (h.contains("tecnico") && h.contains("cargo")) {
                map.put("tecnico_cargo", i);
            } else if (h.contains("financiero") && !h.contains("cargo")) {
                map.put("financiero_nombre", i);
            } else if (h.contains("financiero") && h.contains("cargo")) {
                map.put("financiero_cargo", i);
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
                System.out.println(
                        "DEBUG: Encontrada columna Descripcion Formacion en índice: " + i + " Nombre: " + header[i]);
                map.put("contratista_desc_formacion", i);
            } else if ((h.contains("formacion") || h.contains("titulo")) && !h.contains("descri")) {
                map.put("contratista_formacion", i);

            } else if ((h.contains("tarjeta") || h.contains("matricula")) && !h.contains("descripcion")) {
                map.put("contratista_tarjeta", i);
            } else if (h.contains("descri") && (h.contains("tarjeta") || h.contains("matricula"))) {
                System.out.println(
                        "DEBUG: Encontrada columna Descripcion Tarjeta en índice: " + i + " Nombre: " + header[i]);
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
            }
        }

        return map;
    }

    private String normalizeText(String text) {
        if (text == null)
            return "";
        return text.toLowerCase()
                .replace("ó", "o")
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ú", "u")
                .replace("ñ", "n")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();
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
    private int processOrdenador(String[] row, Map<String, Integer> map, java.util.Set<String> cedulasExistentes) {
        try {
            String nombre = get(row, map, "nombre_ordenador");
            String cedula = get(row, map, "cedula_ordenador");

            if (nombre.isEmpty()) {
                return 0; // No tiene datos de ordenador
            }

            // VALIDAR DUPLICADO EN MEMORIA (Rápido y fiable)
            if (!cedula.isEmpty() && cedulasExistentes.contains(cedula)) {
                return -1; // Ya existe en la BD
            }

            OrdenadorGasto ordenador = new OrdenadorGasto();
            ordenador.setOrganismo(get(row, map, "organismo"));
            ordenador.setDireccionOrganismo(get(row, map, "direccion_organismo"));
            ordenador.setNombreOrdenador(nombre);
            ordenador.setCedulaOrdenador(cedula);
            ordenador.setCargoOrdenador(get(row, map, "cargo_ordenador"));
            ordenador.setDecretoNombramiento(get(row, map, "decreto_nombramiento"));
            ordenador.setActaPosesion(get(row, map, "acta_posesion"));

            boolean insertado = ordenadorDAO.insertar(ordenador);
            if (insertado && !cedula.isEmpty()) {
                cedulasExistentes.add(cedula); // Actualizar caché para siguientes filas
                return 1;
            }
            return insertado ? 1 : 0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
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
    private int processContratista(String[] row, Map<String, Integer> map, java.util.Set<String> cedulasExistentes) {
        try {
            String cedula = get(row, map, "contratista_cedula");
            String nombre = get(row, map, "contratista_nombre");

            if (cedula.isEmpty() && nombre.isEmpty()) {
                return 0; // No tiene datos de contratista
            }

            // VALIDAR DUPLICADO EN MEMORIA
            if (!cedula.isEmpty() && cedulasExistentes.contains(cedula)) {
                return -1; // Ya existe en la BD
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
                    System.err.println("Error al parsear fecha: " + dia + "/" + mes + "/" + ano);
                }
            }

            // Edad
            String edad = get(row, map, "contratista_edad");
            if (!edad.isEmpty()) {
                try {
                    // Parsear como double primero para manejar "25.0" o "25" correctamente
                    double edadDouble = Double.parseDouble(edad.replace(",", "."));
                    contratista.setEdad((int) edadDouble);
                } catch (Exception e) {
                    // Ignorar error de parseo
                }
            }

            contratista.setFormacionTitulo(get(row, map, "contratista_formacion"));
            contratista.setDescripcionFormacion(get(row, map, "contratista_desc_formacion"));
            contratista.setTarjetaProfesional(get(row, map, "contratista_tarjeta"));
            contratista.setDescripcionTarjeta(get(row, map, "contratista_desc_tarjeta"));
            contratista.setExperiencia(get(row, map, "contratista_experiencia"));
            contratista.setDescripcionExperiencia(get(row, map, "contratista_desc_experiencia"));
            contratista.setRestricciones(get(row, map, "contratista_restricciones"));

            boolean insertado = contratistaDAO.insertar(contratista);
            if (insertado && !cedula.isEmpty()) {
                cedulasExistentes.add(cedula); // Actualizar caché
                return 1;
            }
            return insertado ? 1 : 0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Procesa un supervisor
     * 
     * @return 1=insertado, 0=omitido (datos vacíos), -1=duplicado
     */
    private int processSupervisor(String[] row, Map<String, Integer> map, java.util.Set<String> cedulasExistentes) {
        try {
            String nombre = get(row, map, "supervisor_nombre");
            String cedula = get(row, map, "supervisor_cedula");

            if (nombre.isEmpty()) {
                return 0; // No tiene datos de supervisor
            }

            // VALIDAR DUPLICADO EN MEMORIA
            if (!cedula.isEmpty() && cedulasExistentes.contains(cedula)) {
                return -1; // Ya existe en la BD
            }

            Supervisor supervisor = new Supervisor();
            supervisor.setNombre(nombre);
            supervisor.setCedula(cedula);
            supervisor.setCargo(get(row, map, "supervisor_cargo"));

            boolean insertado = supervisorDAO.insertar(supervisor);
            if (insertado && !cedula.isEmpty()) {
                cedulasExistentes.add(cedula); // Actualizar caché
                return 1;
            }
            return insertado ? 1 : 0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
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
    private int processEstructurador(String[] row, Map<String, Integer> map, StringBuilder log) {
        try {
            String jurNombre = get(row, map, "juridico_nombre");
            String tecNombre = get(row, map, "tecnico_nombre");
            String finNombre = get(row, map, "financiero_nombre");

            // Si no hay ninguno de los nombres, asumimos que no es fila de estructuradores
            if (jurNombre.isEmpty() && tecNombre.isEmpty() && finNombre.isEmpty()) {
                return 0;
            }

            // Crear objeto Estructurador
            Estructurador e = new Estructurador();
            e.setJuridicoNombre(jurNombre);
            e.setJuridicoCargo(get(row, map, "juridico_cargo"));
            e.setTecnicoNombre(tecNombre);
            e.setTecnicoCargo(get(row, map, "tecnico_cargo"));
            e.setFinancieroNombre(finNombre);
            e.setFinancieroCargo(get(row, map, "financiero_cargo"));

            boolean insertado = estructuradorDAO.insertar(e);
            return insertado ? 1 : 0;

        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }
}
