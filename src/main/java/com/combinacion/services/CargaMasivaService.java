package com.combinacion.services;

import com.combinacion.dao.*;
import com.combinacion.models.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Capa de servicio para la Carga Masiva desde Excel/CSV.
 * Contiene toda la lógica de negocio extraída del CargaMasivaServlet:
 * lectura del archivo, detección de encabezados, procesamiento fila por fila
 * y persistencia de todas las entidades relacionadas.
 */
public class CargaMasivaService {

    private final OrdenadorGastoDAO    ordenadorDAO    = new OrdenadorGastoDAO();
    private final ContratistaDAO       contratistaDAO  = new ContratistaDAO();
    private final SupervisorDAO        supervisorDAO   = new SupervisorDAO();
    private final EstructuradorDAO     estructuradorDAO= new EstructuradorDAO();
    private final PresupuestoDetalleDAO presupuestoDAO = new PresupuestoDetalleDAO();
    private final ContratoDAO          contratoDAO     = new ContratoDAO();

    // -------------------------------------------------------------------------
    // RESULTADO
    // -------------------------------------------------------------------------

    /**
     * Resultado del proceso de carga masiva.
     */
    public static class ResultadoCarga {
        public int ordenadoresCount;
        public int contratistasCount;
        public int supervisoresCount;
        public int estructuradoresCount;
        public int presupuestoCount;
        public int contratosCount;
        public int errorCount;
        public StringBuilder log = new StringBuilder();

        public String generarMensaje() {
            StringBuilder msg = new StringBuilder();
            if (ordenadoresCount   > 0) msg.append("✅ Ordenadores: ").append(ordenadoresCount).append("<br>");
            if (contratistasCount  > 0) msg.append("✅ Contratistas: ").append(contratistasCount).append("<br>");
            if (supervisoresCount  > 0) msg.append("✅ Supervisores: ").append(supervisoresCount).append("<br>");
            if (estructuradoresCount>0) msg.append("✅ Estructuradores: ").append(estructuradoresCount).append("<br>");
            if (presupuestoCount   > 0) msg.append("✅ Presupuestos: ").append(presupuestoCount).append("<br>");
            if (contratosCount     > 0) msg.append("✅ Contratos: ").append(contratosCount).append("<br>");
            if (errorCount         > 0) msg.append("⚠️ Errores: ").append(errorCount).append("<br>");
            if (msg.length() == 0) msg.append("⚠️ No se cargaron datos");
            return msg.toString();
        }
    }

    // -------------------------------------------------------------------------
    // PUNTO DE ENTRADA PRINCIPAL
    // -------------------------------------------------------------------------

    /**
     * Procesa un archivo de carga masiva (Excel o CSV).
     * @param fileContent Stream del contenido del archivo.
     * @param fileName    Nombre del archivo (para detectar extensión).
     * @return ResultadoCarga con conteos y log del proceso.
     */
    public ResultadoCarga procesarArchivo(InputStream fileContent, String fileName) throws Exception {
        ResultadoCarga resultado = new ResultadoCarga();
        StringBuilder log = resultado.log;

        List<String[]> allRows = leerFilas(fileContent, fileName);
        if (allRows.isEmpty()) throw new Exception("El archivo está vacío.");

        // Detectar encabezados combinando las primeras filas (estrategia de fusión vertical)
        int maxCols = 0;
        int rowsToScan = Math.min(5, allRows.size());
        for (int i = 0; i < rowsToScan; i++) {
            if (allRows.get(i).length > maxCols) maxCols = allRows.get(i).length;
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

        Map<String, Integer> map = mapearEncabezados(header, log);
        log.append("\n═════════════════════════════════════════\n\n");

        // Cachear cédulas existentes para evitar N+1 queries
        Set<String> cedulasOrdenadores  = ordenadorDAO.obtenerTodasLasCedulas();
        Set<String> cedulasSupervisores = supervisorDAO.obtenerTodasLasCedulas();
        Set<String> cedulasContratistas = new java.util.HashSet<>();
        for (String ced : contratistaDAO.obtenerTodasLasCedulas()) {
            if (ced != null) cedulasContratistas.add(ced.replaceAll("[^0-9]", ""));
        }

        // Procesar filas de datos
        for (int i = 1; i < allRows.size(); i++) {
            String[] row = allRows.get(i);
            if (esFilaVacia(row)) continue;

            try {
                OrdenadorGasto  ordenador    = procesarOrdenador(row, map, cedulasOrdenadores);
                Contratista     contratista  = procesarContratista(row, map, cedulasContratistas);
                Supervisor      supervisor   = procesarSupervisor(row, map, cedulasSupervisores, log);
                Estructurador   estructurador= procesarEstructurador(row, map, log);
                PresupuestoDetalle presupuesto= procesarPresupuesto(row, map, log);
                Contrato        contrato     = procesarContrato(row, map, contratista, supervisor,
                                                ordenador, estructurador, presupuesto, log);

                if (ordenador    != null) resultado.ordenadoresCount++;
                if (contratista  != null) resultado.contratistasCount++;
                if (supervisor   != null) resultado.supervisoresCount++;
                if (estructurador!= null) resultado.estructuradoresCount++;
                if (presupuesto  != null) resultado.presupuestoCount++;
                if (contrato     != null) resultado.contratosCount++;

                if (ordenador == null && contratista == null && supervisor == null
                        && estructurador == null && presupuesto == null && contrato == null) {
                    resultado.errorCount++;
                }
            } catch (Exception ex) {
                resultado.errorCount++;
                log.append("⚠️ Error en fila ").append(i + 1).append(": ").append(ex.getMessage()).append("\n");
                ex.printStackTrace();
            }
        }

        System.out.println(log.toString());
        return resultado;
    }

    // -------------------------------------------------------------------------
    // LECTURA DEL ARCHIVO
    // -------------------------------------------------------------------------

    private List<String[]> leerFilas(InputStream fileContent, String fileName) throws Exception {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".xlsx")) {
            try (Workbook wb = new XSSFWorkbook(fileContent)) {
                return leerHoja(wb.getSheetAt(0));
            }
        } else if (lower.endsWith(".xls")) {
            try (Workbook wb = new HSSFWorkbook(fileContent)) {
                return leerHoja(wb.getSheetAt(0));
            }
        } else {
            // CSV
            List<String[]> rows = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileContent, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) rows.add(line.split(";", -1));
                }
            }
            return rows;
        }
    }

    private List<String[]> leerHoja(Sheet sheet) {
        List<String[]> data = new ArrayList<>();
        int maxCol = 0;
        for (Row row : sheet) {
            if (row != null && row.getLastCellNum() > maxCol) maxCol = row.getLastCellNum();
        }
        for (Row row : sheet) {
            if (row == null) continue;
            String[] rowData = new String[maxCol];
            for (int i = 0; i < maxCol; i++) {
                Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                rowData[i] = obtenerValorCelda(cell);
            }
            data.add(rowData);
        }
        return data;
    }

    private String obtenerValorCelda(Cell cell) {
        if (cell == null) return "";
        try {
            switch (cell.getCellType()) {
                case STRING:  return cell.getStringCellValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell))
                        return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                    double d = cell.getNumericCellValue();
                    if (d == (long) d) return String.format("%d", (long) d);
                    return String.valueOf(d);
                case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try { return String.valueOf(cell.getNumericCellValue()); }
                    catch (Exception ex) { return cell.getStringCellValue(); }
                default:      return "";
            }
        } catch (Exception e) { return ""; }
    }

    // -------------------------------------------------------------------------
    // MAPEADO DE ENCABEZADOS
    // -------------------------------------------------------------------------

    private Map<String, Integer> mapearEncabezados(String[] header, StringBuilder log) {
        Map<String, Integer> map = new HashMap<>();
        log.append("--- ANÁLISIS DE ENCABEZADOS ---\n");
        for (int i = 0; i < header.length; i++) {
            if (header[i] != null && !header[i].isEmpty()) {
                String norm = normalizar(header[i]);
                log.append("Col ").append(i).append(": '").append(header[i]).append("' -> Norm: '").append(norm).append("'\n");
            }
        }
        log.append("-------------------------------\n");

        for (int i = 0; i < header.length; i++) {
            if (header[i] == null) continue;
            String h = normalizar(header[i]);

            // Prioridades estrictas
            if (h.startsWith("objeto") && h.contains("contractual")) { map.put("objeto", i); continue; }
            if (h.startsWith("actividades") && h.contains("aplica") && h.contains("entregables")) { map.put("actividades_entregables", i); continue; }
            if (h.equals("nivel") || (h.contains("nivel") && !h.contains("observa") && !h.contains("central") && !h.contains("territorial"))) { map.put("nivel", i); continue; }
            if (h.contains("tipo") && h.contains("contrato") && !h.contains("laboral") && !map.containsKey("tipo_contrato")) { map.put("tipo_contrato", i); continue; }
            // Prioridad: Registro Presupuestal / RPC (antes de la cadena else-if que puede bloquearlo)
            if (h.contains("rpc") || (h.contains("registro") && h.contains("presup"))) {
                if (h.contains("fecha")) { map.put("rp_fecha", i); } else { map.put("rp_numero", i); }
                continue;
            }

            // Actividades (prioridad alta)
            if ((h.contains("actividades") && h.contains("aplica") && h.contains("entregables"))
                    || (h.contains("actividades") && !h.contains("ficha") && !h.contains("ebi") && !h.contains("cronograma") && !h.contains("obj"))) {
                if (!h.contains("ficha") && !h.contains("ebi")) { map.put("actividades_entregables", i); continue; }
            }

            // Ordenadores del gasto
            if (h.contains("organismo") && !h.contains("direccion"))              { map.put("organismo", i); }
            else if (h.contains("direccion") && h.contains("organismo"))           { map.put("direccion_organismo", i); }
            else if (h.contains("nombre") && h.contains("ordenador"))              { map.put("nombre_ordenador", i); }
            else if (h.contains("cedula") && h.contains("ordenador"))              { map.put("cedula_ordenador", i); }
            else if (h.contains("cargo") && h.contains("ordenador"))               { map.put("cargo_ordenador", i); }
            else if (h.contains("decreto") && h.contains("nombramiento"))          { map.put("decreto_nombramiento", i); }
            else if (h.contains("acta") && h.contains("posesion"))                 { map.put("acta_posesion", i); }

            // Objeto y actividades (fallback)
            if (h.contains("objeto") && (h.contains("contractual") || h.contains("contrato"))) { map.put("objeto", i); }
            else if ((h.contains("entregables") || h.contains("entregable") || (h.contains("obligaciones") && !h.contains("financiera"))) && !h.contains("objeto") && !map.containsKey("actividades_entregables")) { map.put("actividades_entregables", i); }

            // Estructuradores (con prioridad estricta)
            if (h.contains("juridico") && h.contains("estructurador") && !h.contains("tecnico")) {
                if (h.contains("cargo")) map.put("estructurador_juridico_cargo", i);
                else map.put("estructurador_juridico", i);
                continue;
            }
            if (h.contains("tecnico") && h.contains("estructurador")) {
                if (h.contains("cargo")) map.put("estructurador_tecnico_cargo", i);
                else map.put("estructurador_tecnico", i);
                continue;
            }
            if (h.contains("financiero") && h.contains("estructurador")) {
                if (h.contains("cargo")) map.put("estructurador_financiero_cargo", i);
                else map.put("estructurador_financiero", i);
                continue;
            }
            // Fallbacks estructuradores
            if (h.contains("juridico") && !h.contains("cargo") && !h.contains("contratista") && !map.containsKey("estructurador_juridico")) { map.put("estructurador_juridico", i); }
            else if (h.contains("tecnico") && !h.contains("cargo") && !h.contains("apoyo") && !h.contains("contratista") && !map.containsKey("estructurador_tecnico")) { map.put("estructurador_tecnico", i); }
            else if (h.contains("financiero") && !h.contains("cargo") && !h.contains("contratista") && !map.containsKey("estructurador_financiero")) { map.put("estructurador_financiero", i); }

            // Presupuesto
            else if (h.contains("cdp")) {
                if (h.contains("vencimiento")) map.put("cdp_vencimiento", i);
                else if (h.contains("valor"))  map.put("cdp_valor", i);
                else if (h.contains("fecha") && !h.contains("numero") && !h.contains("num")) map.put("cdp_fecha", i);
                else map.put("cdp_numero", i);
            }
            else if (h.contains("apropiacion"))  { map.put("apropiacion_presupuestal", i); }
            else if (h.contains("rubro") && h.contains("presupuestal")) { map.put("rubro_presupuestal", i); }
            else if (h.contains("paa") && (h.contains("id") || h.contains("cod"))) { map.put("id_paa", i); }
            else if (h.contains("dane") && h.contains("cod")) { map.put("codigo_dane", i); }
            else if (h.contains("inversion") && h.contains("aplica")) { map.put("inversion", i); }
            else if (h.contains("funcionamiento") && h.contains("aplica")) { map.put("funcionamiento", i); }
            else if ((h.contains("nombre") || h.contains("ficha")) && (h.contains("tebi") || h.contains("ebi"))) { map.put("ficha_ebi_nombre", i); }

            // Contratistas
            else if (h.contains("nombre") && h.contains("contratista"))    { map.put("contratista_nombre", i); }
            else if (h.contains("cedula") && h.contains("contratista"))    { map.put("contratista_cedula", i); }
            else if (h.contains("dv") && !h.contains("cdp"))               { map.put("contratista_dv", i); }
            else if (h.contains("telefono") || h.contains("telefonico"))   { map.put("contratista_telefono", i); }
            else if (h.contains("correo") || h.contains("electronico"))    { map.put("contratista_correo", i); }
            else if (h.contains("direccion") && !h.contains("organismo"))  { map.put("contratista_direccion", i); }
            else if (h.contains("dia") && h.contains("nacimiento"))        { map.put("contratista_dia_nac", i); }
            else if (h.contains("mes") && h.contains("nacimiento"))        { map.put("contratista_mes_nac", i); }
            else if ((h.contains("ano") || h.contains("año")) && h.contains("nacimiento")) { map.put("contratista_ano_nac", i); }
            else if (h.contains("edad") && !h.contains("nombre"))         { map.put("contratista_edad", i); }
            else if (h.contains("formacion") || h.contains("titulo") || h.contains("academico") || h.contains("educacion")) { map.put("contratista_formacion", i); }
            else if ((h.contains("tarjeta") || h.contains("matricula")) && !h.contains("descri")) { map.put("contratista_tarjeta", i); }
            else if (h.contains("experienci") && !h.contains("descri"))   { map.put("contratista_experiencia", i); }
            else if (h.contains("restricciones"))                          { map.put("contratista_restricciones", i); }

            // Supervisores
            else if (h.contains("nombre") && h.contains("supervisor"))    { map.put("supervisor_nombre", i); }
            else if (h.contains("cedula") && h.contains("supervisor"))    { map.put("supervisor_cedula", i); }
            else if (h.contains("cargo") && h.contains("supervisor"))     { map.put("supervisor_cargo", i); }

            // Contratos
            else if (h.contains("trd") && h.contains("proceso"))           { map.put("trd_proceso", i); }
            else if ((h.contains("numero") || h.contains("nmero")) && h.contains("contrato") && !h.contains("tipo") && !h.contains("valor")) { map.put("numero_contrato", i); }
            else if (h.equals("objeto") || (h.contains("objeto") && !h.contains("observa"))) { map.put("objeto", i); }
            else if (h.equals("modalidad") || (h.contains("modalidad") && !h.contains("observa"))) { map.put("modalidad", i); }
            else if (h.equals("estado") || (h.contains("estado") && !h.contains("tramite") && !h.contains("observa"))) { map.put("estado", i); }
            else if (h.equals("periodo") || (h.contains("periodo") && !h.contains("observa"))) { map.put("periodo", i); }
            else if (h.contains("suscripcion"))                             { map.put("fecha_suscripcion", i); }
            else if (h.contains("inicio") && (h.contains("fecha") || h.equals("inicio"))) { map.put("fecha_inicio", i); }
            else if ((h.contains("terminacion") && h.contains("fecha")) || (h.contains("plazo") && h.contains("ejecucion"))) {
                map.put("fecha_terminacion", i);
                if (h.contains("plazo") && h.contains("ejecucion")) map.put("plazo_ejecucion", i);
            }
            else if (h.contains("aprobacion"))                              { map.put("fecha_aprobacion", i); }
            else if (h.contains("ejecucion") && h.contains("fecha") && !h.contains("plazo")) { map.put("fecha_ejecucion", i); }
            else if (h.contains("arl"))                                     { map.put("fecha_arl", i); }
            else if (h.contains("meses") && !h.contains("media"))          { map.put("plazo_meses", i); }
            else if (h.contains("dias") && (h.contains("plazo") || h.equals("dias"))) { map.put("plazo_dias", i); }
            else if (h.contains("valor") && h.contains("total") && h.contains("letras") && !h.contains("adicion")) { map.put("valor_total_letras", i); }
            else if (h.contains("valor") && h.contains("total") && h.contains("numeros") && !h.contains("adicion")) { map.put("valor_total_numeros", i); }
            else if (h.contains("valor") && h.contains("cuota") && h.contains("letras") && !h.contains("media") && !h.contains("adicion")) { map.put("valor_cuota_letras", i); }
            else if (h.contains("valor") && h.contains("cuota") && h.contains("numero") && !h.contains("media") && !h.contains("adicion")) { map.put("valor_cuota_numero", i); }
            else if (h.contains("media") && h.contains("letras"))          { map.put("valor_media_cuota_letras", i); }
            else if (h.contains("media") && h.contains("numero"))          { map.put("valor_media_cuota_numero", i); }
            else if (h.contains("enlace") && h.contains("secop"))          { map.put("enlace_secop", i); }
            else if (h.contains("liquidaci") && h.contains("acuerdo") && (h.contains("articulo") || h.contains("artculo"))) { map.put("liquidacion_acuerdo", i); }
            else if (h.contains("liquidaci") && h.contains("acuerdo") && (h.contains("numero") || h.contains("fecha"))) { map.put("liquidacion_articulo", i); }
            else if (h.contains("liquidaci") && h.contains("decreto"))     { map.put("liquidacion_decreto", i); }
            else if (h.contains("circular") && h.contains("honorarios"))   { map.put("circular_honorarios", i); }
            else if (h.contains("apoyo") && h.contains("supervision"))     { map.put("apoyo_supervision", i); }
            else if (h.contains("fecha") && h.contains("idoneidad"))       { map.put("fecha_idoneidad", i); }
            else if ((h.contains("fecha") || h.contains("firm")) && h.contains("estructurador")) { map.put("fecha_estructurador", i); }
            else if (h.contains("valor") && h.contains("total") && h.contains("adicion")) {
                if (h.contains("letras")) map.put("valor_total_adicion_letras", i); else map.put("valor_total_adicion", i);
            }
            else if (h.contains("valor") && h.contains("total") && h.contains("contrato") && h.contains("mas") && h.contains("adicion")) {
                if (h.contains("letras")) map.put("valor_contrato_mas_adicion_letras", i); else map.put("valor_contrato_mas_adicion", i);
            }
            else if (h.contains("numero") && h.contains("cuotas") && h.contains("adicion")) { map.put("numero_cuotas_adicion", i); }
            else if (h.contains("adicion") && h.contains("si") && h.contains("no"))         { map.put("adicion_si_no", i); }
        }
        return map;
    }

    // -------------------------------------------------------------------------
    // PROCESADORES POR ENTIDAD
    // -------------------------------------------------------------------------

    private OrdenadorGasto procesarOrdenador(String[] row, Map<String, Integer> map, Set<String> cedulasExistentes) {
        try {
            String nombre = get(row, map, "nombre_ordenador");
            String cedula = get(row, map, "cedula_ordenador");
            if (nombre.isEmpty()) return null;

            if (!cedula.isEmpty() && cedulasExistentes.contains(cedula)) {
                OrdenadorGasto existing = ordenadorDAO.obtenerPorCedula(cedula);
                if (existing != null) {
                    existing.setOrganismo(get(row, map, "organismo"));
                    existing.setDireccionOrganismo(get(row, map, "direccion_organismo"));
                    existing.setNombreOrdenador(nombre);
                    existing.setCargoOrdenador(get(row, map, "cargo_ordenador"));
                    existing.setDecretoNombramiento(get(row, map, "decreto_nombramiento"));
                    existing.setActaPosesion(get(row, map, "acta_posesion"));
                    ordenadorDAO.actualizar(existing);
                    return existing;
                }
            }
            OrdenadorGasto o = new OrdenadorGasto();
            o.setOrganismo(get(row, map, "organismo"));
            o.setDireccionOrganismo(get(row, map, "direccion_organismo"));
            o.setNombreOrdenador(nombre);
            o.setCedulaOrdenador(cedula);
            o.setCargoOrdenador(get(row, map, "cargo_ordenador"));
            o.setDecretoNombramiento(get(row, map, "decreto_nombramiento"));
            o.setActaPosesion(get(row, map, "acta_posesion"));
            if (ordenadorDAO.insertar(o)) { if (!cedula.isEmpty()) cedulasExistentes.add(cedula); return o; }
            return null;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    private Contratista procesarContratista(String[] row, Map<String, Integer> map, Set<String> cedulasExistentes) {
        try {
            String cedula = get(row, map, "contratista_cedula");
            String nombre = get(row, map, "contratista_nombre");
            if (cedula.isEmpty() && nombre.isEmpty()) return null;

            Contratista contratista;
            boolean exists = false;
            String normalizedCedula = cedula.replaceAll("[^0-9]", "");

            if (!normalizedCedula.isEmpty() && cedulasExistentes.contains(normalizedCedula)) {
                contratista = contratistaDAO.obtenerPorCedula(cedula);
                if (contratista != null) exists = true;
                else contratista = new Contratista();
            } else {
                contratista = new Contratista();
            }

            if (!exists) contratista.setCedula(cedula.isEmpty() ? "SIN_CEDULA_" + System.currentTimeMillis() : cedula);
            contratista.setDv(get(row, map, "contratista_dv"));
            contratista.setNombre(nombre.isEmpty() ? "Sin Nombre" : nombre);
            contratista.setTelefono(get(row, map, "contratista_telefono"));
            contratista.setCorreo(get(row, map, "contratista_correo"));
            contratista.setDireccion(get(row, map, "contratista_direccion"));

            String dia = get(row, map, "contratista_dia_nac");
            String mes = get(row, map, "contratista_mes_nac");
            String ano = get(row, map, "contratista_ano_nac");
            if (!dia.isEmpty() && !mes.isEmpty() && !ano.isEmpty()) {
                try {
                    String diaFmt = dia.length() == 1 ? "0" + dia : dia;
                    String mesFmt = mes.length() == 1 ? "0" + mes : mes;
                    contratista.setFechaNacimiento(java.sql.Date.valueOf(ano + "-" + mesFmt + "-" + diaFmt));
                } catch (Exception ignored) {}
            }

            String edad = get(row, map, "contratista_edad");
            if (!edad.isEmpty()) {
                try { contratista.setEdad((int) Double.parseDouble(edad.replace(",", "."))); } catch (Exception ignored) {}
            }

            contratista.setFormacionTitulo(get(row, map, "contratista_formacion"));
            contratista.setDescripcionFormacion(get(row, map, "contratista_desc_formacion"));
            contratista.setTarjetaProfesional(get(row, map, "contratista_tarjeta"));
            contratista.setDescripcionTarjeta(get(row, map, "contratista_desc_tarjeta"));
            contratista.setExperiencia(get(row, map, "contratista_experiencia"));
            contratista.setDescripcionExperiencia(get(row, map, "contratista_desc_experiencia"));
            contratista.setRestricciones(get(row, map, "contratista_restricciones"));

            if (exists) {
                if (contratistaDAO.actualizar(contratista)) return contratista;
            } else {
                if (contratistaDAO.insertar(contratista)) {
                    if (!normalizedCedula.isEmpty()) cedulasExistentes.add(normalizedCedula);
                    return contratista;
                }
            }
            return null;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    private Supervisor procesarSupervisor(String[] row, Map<String, Integer> map, Set<String> cedulasExistentes, StringBuilder log) {
        try {
            String nombre = get(row, map, "supervisor_nombre");
            String cedula = get(row, map, "supervisor_cedula");
            if (nombre.isEmpty()) return null;

            Supervisor exactMatch = supervisorDAO.obtenerPorCedulaYNombre(cedula, nombre);
            if (exactMatch != null) {
                exactMatch.setCargo(get(row, map, "supervisor_cargo"));
                supervisorDAO.actualizar(exactMatch);
                return exactMatch;
            }

            if (!cedula.isEmpty() && cedulasExistentes.contains(cedula)) {
                cedula = cedula + "-DUP-" + System.nanoTime();
            }

            Supervisor s = new Supervisor();
            s.setNombre(nombre);
            s.setCedula(cedula.isEmpty() ? "SIN_CEDULA_" + System.nanoTime() : cedula);
            s.setCargo(get(row, map, "supervisor_cargo"));

            if (supervisorDAO.insertar(s)) {
                String base = cedula.contains("-DUP-") ? cedula.substring(0, cedula.indexOf("-DUP-")) : cedula;
                if (!base.isEmpty()) cedulasExistentes.add(base);
                return s;
            }
            return null;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    private Estructurador procesarEstructurador(String[] row, Map<String, Integer> map, StringBuilder log) {
        try {
            String jurNombre = get(row, map, "estructurador_juridico");
            String tecNombre = get(row, map, "estructurador_tecnico");
            String finNombre = get(row, map, "estructurador_financiero");
            if (jurNombre.isEmpty() && tecNombre.isEmpty() && finNombre.isEmpty()) return null;

            String jurCargo = get(row, map, "estructurador_juridico_cargo");
            String tecCargo = get(row, map, "estructurador_tecnico_cargo");
            String finCargo = get(row, map, "estructurador_financiero_cargo");

            Estructurador existente = estructuradorDAO.obtenerExistente(jurNombre, jurCargo, tecNombre, tecCargo, finNombre, finCargo);
            if (existente != null) return existente;

            Estructurador e = new Estructurador();
            e.setJuridicoNombre(jurNombre); e.setJuridicoCargo(jurCargo);
            e.setTecnicoNombre(tecNombre);  e.setTecnicoCargo(tecCargo);
            e.setFinancieroNombre(finNombre); e.setFinancieroCargo(finCargo);

            if (estructuradorDAO.insertar(e)) return e;
            return null;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    private PresupuestoDetalle procesarPresupuesto(String[] row, Map<String, Integer> map, StringBuilder log) {
        try {
            String cdpNum     = get(row, map, "cdp_numero");
            String rpNum      = get(row, map, "rp_numero");
            String idPaa      = get(row, map, "id_paa");
            String apropiacion= get(row, map, "apropiacion_presupuestal");
            String cdpFechaRaw= get(row, map, "cdp_fecha");
            String rpFechaRaw = get(row, map, "rp_fecha");
            String cdpValRaw  = get(row, map, "cdp_valor");
            // Retornar null solo si NO existe ningún dato presupuestal en la fila
            boolean sinDatos = cdpNum.isEmpty() && rpNum.isEmpty() && idPaa.isEmpty()
                    && apropiacion.isEmpty() && cdpFechaRaw.isEmpty()
                    && rpFechaRaw.isEmpty() && cdpValRaw.isEmpty();
            if (sinDatos) return null;

            PresupuestoDetalle p = new PresupuestoDetalle();
            p.setCdpNumero(cdpNum);
            p.setCdpFecha(parsearFecha(cdpFechaRaw));
            try {
                if (!cdpValRaw.isEmpty()) { String val = limpiarMoneda(cdpValRaw); if (!val.isEmpty()) p.setCdpValor(new java.math.BigDecimal(val)); }
            } catch (Exception ignored) {}
            p.setCdpVencimiento(parsearFecha(get(row, map, "cdp_vencimiento")));
            p.setRpNumero(rpNum);
            p.setRpFecha(parsearFecha(rpFechaRaw));
            p.setApropiacionPresupuestal(apropiacion);
            p.setIdPaa(idPaa);
            p.setCodigoDane(get(row, map, "codigo_dane"));
            p.setInversion(parsearBoolean(get(row, map, "inversion")));
            p.setFuncionamiento(parsearBoolean(get(row, map, "funcionamiento")));
            p.setFichaEbiNombre(get(row, map, "ficha_ebi_nombre"));
            p.setFichaEbiObjetivo(get(row, map, "ficha_ebi_objetivo"));
            p.setFichaEbiActividades(get(row, map, "ficha_ebi_actividades"));
            p.setCertificadoInsuficiencia(get(row, map, "certificado_insuficiencia"));
            p.setFechaInsuficiencia(parsearFecha(get(row, map, "fecha_insuficiencia")));

            // Solo deduplicar si hay al menos UN campo clave no vacío.
            // Si todos están vacíos, siempre insertar (evita que todos los contratos
            // apunten al mismo presupuesto_detalle).
            boolean hayClaveUnica = !cdpNum.isEmpty() || !rpNum.isEmpty()
                    || !apropiacion.isEmpty() || !idPaa.isEmpty();
            if (hayClaveUnica) {
                PresupuestoDetalle existente = presupuestoDAO.obtenerExistente(cdpNum, rpNum, apropiacion, idPaa);
                if (existente != null) {
                    p.setId(existente.getId());
                    if (presupuestoDAO.actualizar(p)) return p;
                    return existente;
                }
            }
            if (presupuestoDAO.insertar(p)) return p;
            return null;
        } catch (Exception ex) { log.append("⚠️ Error presupuesto: ").append(ex.getMessage()).append("\n"); return null; }
    }

    private Contrato procesarContrato(String[] row, Map<String, Integer> map, Contratista c, Supervisor s,
            OrdenadorGasto o, Estructurador e, PresupuestoDetalle p, StringBuilder log) {
        try {
            Contrato contrato = new Contrato();
            contrato.setTrdProceso(get(row, map, "trd_proceso"));
            contrato.setNumeroContrato(get(row, map, "numero_contrato"));

            String tipoC = get(row, map, "tipo_contrato");
            if (tipoC.isEmpty()) {
                if (parsearBoolean(get(row, map, "tipo_contrato_profesional")).equals("Si")) tipoC = "PROFESIONAL";
                else if (parsearBoolean(get(row, map, "tipo_contrato_apoyo")).equals("Si"))  tipoC = "APOYO A LA GESTION";
            }
            contrato.setTipoContrato(tipoC);
            contrato.setNivel(get(row, map, "nivel"));
            contrato.setObjeto(get(row, map, "objeto"));
            contrato.setModalidad(get(row, map, "modalidad"));
            contrato.setEstado(get(row, map, "estado"));
            contrato.setPeriodo(get(row, map, "periodo"));
            contrato.setFechaSuscripcion(parsearFecha(get(row, map, "fecha_suscripcion")));
            contrato.setFechaInicio(parsearFecha(get(row, map, "fecha_inicio")));
            contrato.setFechaTerminacion(parsearFecha(get(row, map, "fecha_terminacion")));
            contrato.setFechaAprobacion(parsearFecha(get(row, map, "fecha_aprobacion")));
            contrato.setFechaEjecucion(parsearFecha(get(row, map, "fecha_ejecucion")));
            contrato.setFechaArl(parsearFecha(get(row, map, "fecha_arl")));

            String rawPlazo = get(row, map, "plazo_ejecucion");
            contrato.setPlazoEjecucion(rawPlazo);
            if (contrato.getFechaTerminacion() == null && !rawPlazo.isEmpty()) {
                java.sql.Date fTerminacion = parsearFecha(rawPlazo);
                if (fTerminacion != null) contrato.setFechaTerminacion(fTerminacion);
            }

            if (contrato.getFechaTerminacion() != null) {
                java.time.LocalDate fin = contrato.getFechaTerminacion().toLocalDate();
                contrato.setPlazoMeses(fin.getMonthValue());
                contrato.setPlazoDias(fin.getDayOfMonth());
            }

            try {
                String pm = get(row, map, "plazo_meses");
                if (!pm.isEmpty()) contrato.setPlazoMeses((int) Double.parseDouble(pm.replace(",", ".")));
                String pd = get(row, map, "plazo_dias");
                if (!pd.isEmpty()) contrato.setPlazoDias((int) Double.parseDouble(pd.replace(",", ".")));
            } catch (Exception ignored) {}

            contrato.setValorTotalLetras(get(row, map, "valor_total_letras"));
            contrato.setValorCuotaLetras(get(row, map, "valor_cuota_letras"));
            contrato.setValorMediaCuotaLetras(get(row, map, "valor_media_cuota_letras"));
            contrato.setAdicionSiNo(parsearBoolean(get(row, map, "adicion_si_no")));
            contrato.setValorTotalAdicionLetras(get(row, map, "valor_total_adicion_letras"));
            contrato.setValorContratoMasAdicionLetras(get(row, map, "valor_contrato_mas_adicion_letras"));
            contrato.setEnlaceSecop(get(row, map, "enlace_secop"));

            try {
                String vtn = limpiarMoneda(get(row, map, "valor_total_numeros")); if (!vtn.isEmpty()) contrato.setValorTotalNumeros(new java.math.BigDecimal(vtn));
                String vcn = limpiarMoneda(get(row, map, "valor_cuota_numero")); if (!vcn.isEmpty()) contrato.setValorCuotaNumero(new java.math.BigDecimal(vcn));
                String vmcn= limpiarMoneda(get(row, map, "valor_media_cuota_numero")); if (!vmcn.isEmpty()) contrato.setValorMediaCuotaNumero(new java.math.BigDecimal(vmcn));
                String ncn = get(row, map, "num_cuotas_numero"); if (!ncn.isEmpty()) contrato.setNumCuotasNumero((int) Double.parseDouble(ncn.replace(",",".")));
                String vta = limpiarMoneda(get(row, map, "valor_total_adicion")); if (!vta.isEmpty()) contrato.setValorTotalAdicion(new java.math.BigDecimal(vta));
                String vcma= limpiarMoneda(get(row, map, "valor_contrato_mas_adicion")); if (!vcma.isEmpty()) contrato.setValorContratoMasAdicion(new java.math.BigDecimal(vcma));
                String nca = get(row, map, "numero_cuotas_adicion"); if (!nca.isEmpty()) contrato.setNumeroCuotasAdicion((int) Double.parseDouble(nca.replace(",",".")));
            } catch (Exception ignored) {}

            contrato.setActividadesEntregables(get(row, map, "actividades_entregables"));
            contrato.setLiquidacionAcuerdo(get(row, map, "liquidacion_acuerdo"));
            contrato.setLiquidacionArticulo(get(row, map, "liquidacion_articulo"));
            contrato.setLiquidacionDecreto(get(row, map, "liquidacion_decreto"));
            contrato.setCircularHonorarios(get(row, map, "circular_honorarios"));
            contrato.setApoyoSupervision(get(row, map, "apoyo_supervision"));
            contrato.setFechaIdoneidad(parsearFecha(get(row, map, "fecha_idoneidad")));
            contrato.setFechaEstructurador(parsearFecha(get(row, map, "fecha_estructurador")));

            if (c != null) contrato.setContratistaId(c.getId());
            if (s != null) contrato.setSupervisorId(s.getId());
            if (o != null) contrato.setOrdenadorId(o.getId());
            if (e != null) contrato.setEstructuradorId(e.getId());
            if (p != null) contrato.setPresupuestoId(p.getId());

            Contrato existente = contratoDAO.obtenerPorNumero(contrato.getNumeroContrato());
            if (existente != null) {
                contrato.setId(existente.getId());
                // Preservar relaciones existentes si las nuevas vienen vacías
                if (contrato.getEstructuradorId() == 0 && existente.getEstructuradorId() > 0) contrato.setEstructuradorId(existente.getEstructuradorId());
                if (contrato.getContratistaId()  == 0 && existente.getContratistaId()  > 0) contrato.setContratistaId(existente.getContratistaId());
                if (contrato.getSupervisorId()   == 0 && existente.getSupervisorId()   > 0) contrato.setSupervisorId(existente.getSupervisorId());
                if (contrato.getOrdenadorId()    == 0 && existente.getOrdenadorId()    > 0) contrato.setOrdenadorId(existente.getOrdenadorId());
                if (contrato.getPresupuestoId()  == 0 && existente.getPresupuestoId()  > 0) contrato.setPresupuestoId(existente.getPresupuestoId());
                if (contratoDAO.actualizar(contrato)) { log.append("  ↻ Actualizado: ").append(contrato.getNumeroContrato()).append("\n"); return contrato; }
            } else {
                if (contratoDAO.insertar(contrato)) { log.append("  ➜ Creado: ").append(contrato.getNumeroContrato()).append("\n"); return contrato; }
            }
            return null;
        } catch (Exception ex) { log.append("  ⚠️ Error contrato: ").append(ex.getMessage()).append("\n"); ex.printStackTrace(); return null; }
    }

    // -------------------------------------------------------------------------
    // UTILIDADES INTERNAS
    // -------------------------------------------------------------------------

    private String get(String[] row, Map<String, Integer> map, String key) {
        if (!map.containsKey(key)) return "";
        int idx = map.get(key);
        if (idx >= row.length || idx < 0) return "";
        return row[idx] != null ? row[idx].trim() : "";
    }

    private boolean esFilaVacia(String[] row) {
        if (row == null || row.length == 0) return true;
        for (String cell : row) { if (cell != null && !cell.trim().isEmpty()) return false; }
        return true;
    }

    private String normalizar(String text) {
        if (text == null) return "";
        String lower = text.toLowerCase().replace("\n", " ").replace("\r", " ").replace("\t", " ").trim();
        lower = lower.replace("á","a").replace("é","e").replace("í","i").replace("ó","o").replace("ú","u").replace("ñ","n");
        StringBuilder sb = new StringBuilder();
        for (char c : lower.toCharArray()) { if (Character.isLetterOrDigit(c) || c == ' ') sb.append(c); }
        return sb.toString().trim().replaceAll(" +", " ");
    }

    private String parsearBoolean(String val) {
        if (val == null) return "No";
        val = val.trim();
        if (val.equalsIgnoreCase("x") || val.equalsIgnoreCase("si")) return "Si";
        if (val.isEmpty()) return "No";
        return val;
    }

    private java.sql.Date parsearFecha(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        String d = dateStr.trim();
        try { return java.sql.Date.valueOf(d); } catch (Exception ignored) {}
        try {
            if (d.contains("/")) {
                String[] parts = d.split("/");
                if (parts.length == 3) {
                    int day = Integer.parseInt(parts[0]), month = Integer.parseInt(parts[1]), year = Integer.parseInt(parts[2]);
                    if (year < 100) year += 2000;
                    return java.sql.Date.valueOf(java.time.LocalDate.of(year, month, day));
                }
            }
            if (d.contains("-")) {
                String[] parts = d.split("-");
                if (parts.length == 3 && parts[0].length() <= 2) {
                    int day = Integer.parseInt(parts[0]), month = Integer.parseInt(parts[1]), year = Integer.parseInt(parts[2]);
                    if (year < 100) year += 2000;
                    return java.sql.Date.valueOf(java.time.LocalDate.of(year, month, day));
                }
            }
            if (d.matches("^[0-9]+(\\.[0-9]+)?$")) {
                java.util.Date jd = DateUtil.getJavaDate(Double.parseDouble(d));
                if (jd != null) return new java.sql.Date(jd.getTime());
            }
            if (d.toLowerCase().contains(" de ")) {
                String clean = d.toLowerCase().replace("didciembre","diciembre")
                    .replace("enero","01").replace("febrero","02").replace("marzo","03").replace("abril","04")
                    .replace("mayo","05").replace("junio","06").replace("julio","07").replace("agosto","08")
                    .replace("septiembre","09").replace("octubre","10").replace("noviembre","11").replace("diciembre","12")
                    .replace(" de ","-").replace(" ","-");
                String[] parts = clean.split("-");
                if (parts.length == 3) {
                    return java.sql.Date.valueOf(java.time.LocalDate.of(
                        Integer.parseInt(parts[2].trim()), Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[0].trim())));
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String limpiarMoneda(String val) {
        if (val == null) return "";
        String clean = val.replace("$", "").trim();
        if (clean.isEmpty()) return "";
        int dots   = clean.length() - clean.replace(".", "").length();
        int commas = clean.length() - clean.replace(",", "").length();
        if (dots > 0 && commas > 0) { clean = clean.replace(".", "").replace(",", "."); }
        else if (dots > 1)          { clean = clean.replace(".", ""); }
        else if (commas > 0)        { clean = clean.replace(",", "."); }
        return clean;
    }
}
