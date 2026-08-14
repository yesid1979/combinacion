package com.combinacion.servlets;

import com.combinacion.dao.ConsecutivoDAO;
import com.combinacion.models.ConsecutivoCobro;
import com.combinacion.models.Usuario;
import com.combinacion.services.AuthService;
import org.apache.poi.ss.usermodel.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ConsecutivoServlet", urlPatterns = {"/consecutivos"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
    maxFileSize = 1024 * 1024 * 10,      // 10 MB
    maxRequestSize = 1024 * 1024 * 15    // 15 MB
)
public class ConsecutivoServlet extends HttpServlet {

    private final ConsecutivoDAO consecutivoDAO = new ConsecutivoDAO();
    private final AuthService authService = new AuthService();
    private static final String PERMISO = "CONSECUTIVOS_VER";

    @Override
    public void init() throws ServletException {
        // Inicializa la tabla y el permiso si no existen
        consecutivoDAO.inicializarTabla();
    }

    private Usuario getUsuario(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (Usuario) session.getAttribute("usuario") : null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario u = getUsuario(request);
        if (u == null || !authService.tienePermiso(u, PERMISO)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tiene permiso para gestionar consecutivos.");
            return;
        }

        String action = request.getParameter("action");
        
        if ("template".equals(action)) {
            descargarPlantilla(request, response);
            return;
        }

        if ("list_ajax".equals(action)) {
            procesarListAjax(request, response);
            return;
        }

        if ("list".equals(action)) {
            request.setAttribute("aniosDisponibles", consecutivoDAO.obtenerAniosDisponibles());
            request.setAttribute("anioActual", java.time.Year.now().getValue());
            request.getRequestDispatcher("consecutivos.jsp").forward(request, response);
            return;
        }
        
        request.setAttribute("aniosDisponibles", consecutivoDAO.obtenerAniosDisponibles());
        request.setAttribute("anioActual", java.time.Year.now().getValue());
        request.getRequestDispatcher("consecutivos.jsp").forward(request, response);
    }

    private void procesarListAjax(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int draw = 1;
        int start = 0;
        int length = 10;
        String search = "";
        
        try { draw = Integer.parseInt(request.getParameter("draw")); } catch (Exception e) {}
        try { start = Integer.parseInt(request.getParameter("start")); } catch (Exception e) {}
        try { length = Integer.parseInt(request.getParameter("length")); } catch (Exception e) {}
        
        search = request.getParameter("search[value]");
        
        Integer anio = null;
        String anioStr = request.getParameter("anio");
        if (anioStr != null && !anioStr.trim().isEmpty()) {
            try { anio = Integer.parseInt(anioStr); } catch (Exception e) {}
        }
        
        int recordsTotal = consecutivoDAO.contarTodos(null, anio);
        int recordsFiltered = consecutivoDAO.contarTodos(search, anio);
        List<ConsecutivoCobro> data = consecutivoDAO.obtenerTodosPaginados(start, length, search, anio);
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"draw\": ").append(draw).append(", ");
        json.append("\"recordsTotal\": ").append(recordsTotal).append(", ");
        json.append("\"recordsFiltered\": ").append(recordsFiltered).append(", ");
        json.append("\"data\": [");
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("America/Bogota"));
        
        for (int i = 0; i < data.size(); i++) {
            ConsecutivoCobro c = data.get(i);
            json.append("{");
            json.append("\"id\": ").append(c.getId()).append(", ");
            json.append("\"cedula\": \"").append(escapeJson(c.getCedula())).append("\", ");
            json.append("\"nombre\": \"").append(escapeJson(c.getNombre() != null ? c.getNombre() : "")).append("\", ");
            json.append("\"contrato\": \"").append(escapeJson(c.getContrato())).append("\", ");
            json.append("\"numeroCuota\": \"").append(escapeJson(c.getNumeroCuota())).append("\", ");
            json.append("\"consecutivo\": \"").append(escapeJson(c.getConsecutivo())).append("\", ");
            json.append("\"fechaCarga\": \"").append(c.getFechaCarga() != null ? sdf.format(c.getFechaCarga()) : "").append("\"");
            json.append("}");
            if (i < data.size() - 1) json.append(",");
        }
        
        json.append("]}");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json.toString());
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario u = getUsuario(request);
        if (u == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesión.");
            return;
        }

        String action = request.getParameter("action");
        
        if ("check".equals(action)) {
            System.out.println("[ConsecutivoServlet] DEBUG CHECK -> Cedula: " + request.getParameter("cedula") + ", Contrato: " + request.getParameter("contrato") + ", Cuota: " + request.getParameter("cuota"));
            consultarConsecutivo(request, response);
            return;
        }

        if (!authService.tienePermiso(u, PERMISO)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tiene permiso para gestionar consecutivos.");
            return;
        }

        if ("upload".equals(action)) {
            procesarCarga(request, response, u.getId());
        } else if ("check".equals(action)) {
            consultarConsecutivo(request, response);
        } else if ("delete_multiple".equals(action)) {
            eliminarMultiples(request, response);
        } else {
            response.sendRedirect("consecutivos?action=list");
        }
    }

    private void eliminarMultiples(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] idsStr = request.getParameterValues("ids[]");
        if (idsStr == null) {
            idsStr = request.getParameterValues("ids");
        }
        if (idsStr == null || idsStr.length == 0) {
            response.getWriter().write("{\"success\": false}");
            return;
        }
        
        List<Integer> ids = new ArrayList<>();
        for (String id : idsStr) {
            try { ids.add(Integer.parseInt(id)); } catch (Exception e) {}
        }
        
        boolean ok = consecutivoDAO.eliminarVarios(ids);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\": " + ok + "}");
    }

    private void procesarCarga(HttpServletRequest request, HttpServletResponse response, int cargadoPor) 
            throws ServletException, IOException {
        Part filePart = request.getPart("fileExcel");
        if (filePart == null || filePart.getSize() == 0) {
            response.sendRedirect("consecutivos?action=list&error=no_file");
            return;
        }

        Integer anio = null;
        String anioStr = request.getParameter("anio_carga");
        if (anioStr != null && !anioStr.trim().isEmpty()) {
            try { anio = Integer.parseInt(anioStr); } catch (Exception e) {}
        }

        List<ConsecutivoCobro> list = new ArrayList<>();
        try (InputStream fileContent = filePart.getInputStream();
             Workbook workbook = WorkbookFactory.create(fileContent)) {
             
            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;
            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false;
                    continue; // Saltar encabezados
                }
                
                String cedula = getCellValue(row.getCell(0));
                if (cedula != null) {
                    cedula = cedula.replaceAll("[^0-9]", "");
                }
                // String nombre = getCellValue(row.getCell(1)); // Ignorado
                String contrato = getCellValue(row.getCell(2));
                String cuota = getCellValue(row.getCell(3));
                String consecutivo = getCellValue(row.getCell(4));
                
                if (cedula != null && !cedula.trim().isEmpty() &&
                    contrato != null && !contrato.trim().isEmpty() &&
                    cuota != null && !cuota.trim().isEmpty() &&
                    consecutivo != null && !consecutivo.trim().isEmpty()) {
                    
                    ConsecutivoCobro c = new ConsecutivoCobro();
                    c.setCedula(cedula.trim());
                    c.setContrato(contrato.trim());
                    c.setNumeroCuota(cuota.trim());
                    c.setConsecutivo(consecutivo.trim());
                    c.setAnio(anio);
                    list.add(c);
                }
            }
            
            if (list.isEmpty()) {
                response.sendRedirect("consecutivos?action=list&error=empty_or_invalid");
                return;
            }
            
            boolean success = consecutivoDAO.guardarMasivo(list, cargadoPor);
            if (success) {
                response.sendRedirect("consecutivos?action=list&status=uploaded&count=" + list.size());
            } else {
                response.sendRedirect("consecutivos?action=list&error=db_error");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("consecutivos?action=list&error=parse_error");
        }
    }

    private void consultarConsecutivo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String cedula = request.getParameter("cedula");
        String contrato = request.getParameter("contrato");
        String cuota = request.getParameter("cuota");
        
        String consecutivo = consecutivoDAO.obtenerConsecutivo(cedula, contrato, cuota);
        System.out.println("[ConsecutivoServlet] DEBUG RESULTADO -> Consecutivo encontrado: " + consecutivo);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (consecutivo != null) {
            response.getWriter().write("{\"success\":true, \"consecutivo\":\"" + consecutivo + "\"}");
        } else {
            response.getWriter().write("{\"success\":false}");
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        org.apache.poi.ss.usermodel.DataFormatter formatter = new org.apache.poi.ss.usermodel.DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private void descargarPlantilla(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"Plantilla_Consecutivos.xlsx\"");

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Consecutivos");
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            
            String[] headers = {"Cédula", "Nombre", "Contrato", "Cuota", "Consecutivo"};
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Integer anio = null;
            String anioStr = request.getParameter("anio");
            if (anioStr != null && !anioStr.trim().isEmpty()) {
                try { anio = Integer.parseInt(anioStr); } catch (Exception e) {}
            }

            String sql = "SELECT ct.cedula, ct.nombre, c.numero_contrato " +
                         "FROM contratos c " +
                         "JOIN contratistas ct ON c.contratista_id = ct.id " +
                         "WHERE c.fecha_terminacion >= CURRENT_DATE ";
            
            if (anio != null) {
                sql += "AND c.anio = ? ";
            }
            
            sql += "ORDER BY c.numero_contrato ASC";
            
            int rowNum = 1;
            try (java.sql.Connection conn = com.combinacion.util.DBConnection.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                 
                 if (anio != null) {
                     ps.setInt(1, anio);
                 }
                 
                 try (java.sql.ResultSet rs = ps.executeQuery()) {
                     while (rs.next()) {
                     org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                     row.createCell(0).setCellValue(rs.getString("cedula"));
                     row.createCell(1).setCellValue(rs.getString("nombre"));
                     row.createCell(2).setCellValue(rs.getString("numero_contrato"));
                     row.createCell(3).setCellValue(""); // Cuota
                     row.createCell(4).setCellValue(""); // Consecutivo
                     }
                 }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Si no hay contratos vigentes, poner un ejemplo
            if (rowNum == 1) {
                org.apache.poi.ss.usermodel.Row example = sheet.createRow(1);
                example.createCell(0).setCellValue("12345678");
                example.createCell(1).setCellValue("Juan Perez");
                example.createCell(2).setCellValue("4143.010.27.1.100-2026");
                example.createCell(3).setCellValue("8");
                example.createCell(4).setCellValue("150");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(response.getOutputStream());
        }
    }
}
