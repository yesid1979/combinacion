<%@ page import="java.io.*, org.apache.poi.ss.usermodel.*, org.apache.poi.xssf.usermodel.XSSFWorkbook" %>
    <%@ page contentType="text/html; charset=UTF-8" %>
        <!DOCTYPE html>
        <html>

        <head>
            <title>Test Excel</title>
        </head>

        <body>
            <h2>Analizando Excel...</h2>
            <pre>
<%
    try {
        String path = "c:\\Users\\Soporte y Desarrollo\\Documents\\NetBeansProjects\\combinacion\\doc\\MATRIZ PRESTADORES DE SERVICIOS 2026.xlsx";
        FileInputStream fis = new FileInputStream(new File(path));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);
        Row header = sheet.getRow(0);
        if(header != null) {
            out.println("Total Celdas: " + header.getLastCellNum());
            for(int i=0; i<header.getLastCellNum(); i++) {
                Cell c = header.getCell(i);
                out.println(i + ": " + (c!=null ? c.toString() : "NULL"));
            }
        }
        workbook.close();
    } catch(Exception e) {
        e.printStackTrace(new java.io.PrintWriter(out));
    }
%>
</pre>
        </body>

        </html>