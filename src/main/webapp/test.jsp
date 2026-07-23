<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="com.combinacion.util.DBConnection"%>
<!DOCTYPE html>
<html>
<head>
    <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon">
    <title>Test DB Schema</title>
    <link href="${pageContext.request.contextPath}/assets/css/styles.css" rel="stylesheet">
</head>
<body>
    <h1>Database Test</h1>
    <%
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
             
             out.println("<h3>Trying to run ALTER TABLE...</h3>");
             try {
                 stmt.execute("ALTER TABLE informes_supervision ADD COLUMN consecutivo_cobro VARCHAR(50)");
                 out.println("<p style='color:green'>Columna creada exitosamente.</p>");
             } catch (Exception e) {
                 out.println("<p style='color:orange'>ALTER TABLE exception: " + e.getMessage() + "</p>");
             }

             out.println("<h3>Columns in informes_supervision:</h3>");
             DatabaseMetaData meta = conn.getMetaData();
             ResultSet rsMeta = meta.getColumns(null, null, "informes_supervision", null);
             boolean foundConsecutivo = false;
             while(rsMeta.next()) {
                 String colName = rsMeta.getString("COLUMN_NAME");
                 out.println(colName + "<br>");
                 if (colName.equalsIgnoreCase("consecutivo_cobro")) {
                     foundConsecutivo = true;
                 }
             }
             
             if (!foundConsecutivo) {
                 out.println("<p style='color:red;font-weight:bold'>Â¡LA COLUMNA consecutivo_cobro NO EXISTE!</p>");
             } else {
                 out.println("<p style='color:green;font-weight:bold'>LA COLUMNA consecutivo_cobro SÍ EXISTE.</p>");
             }
             
        } catch (Exception e) {
            out.println("<pre>Error: " + e.toString() + "</pre>");
        }
    %>
</body>
</html>

