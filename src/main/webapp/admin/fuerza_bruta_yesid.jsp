<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, javax.sql.*, javax.naming.*" %>
<!DOCTYPE html>
<html>
<head><title>INYECCIÓN FINAL CARGA MASIVA</title></head>
<body style="font-family: sans-serif; padding: 30px;">
    <h2 style="color: #2c3e50;">Inyección de Permisos Especiales: IDs 22 y 631</h2>
    <%
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/combinacion");
            try (Connection conn = ds.getConnection()) {
                conn.setAutoCommit(true);
                
                int yesidId = 2; // Ya sabemos que es el ID 2
                int[] pIds = {22, 631, 1, 5, 20, 13, 17, 9}; // Agregamos los de Carga Masiva y otros Ver
                
                out.println("<ul>");
                for (int pId : pIds) {
                    String sqlIns = "INSERT INTO usuario_permisos (usuario_id, permiso_id) " +
                                    "SELECT ?, ? WHERE NOT EXISTS (" +
                                    "SELECT 1 FROM usuario_permisos WHERE usuario_id = ? AND permiso_id = ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlIns)) {
                        ps.setInt(1, yesidId);
                        ps.setInt(2, pId);
                        ps.setInt(3, yesidId);
                        ps.setInt(4, pId);
                        int rows = ps.executeUpdate();
                        if (rows > 0) out.println("<li style='color:green;'>INYECTADO ID: " + pId + "</li>");
                        else out.println("<li>ID: " + pId + " ya estaba asignado.</li>");
                    }
                }
                out.println("</ul>");
                out.println("<h2 style='color:green;'>¡INYECCIÓN COMPLETADA!</h2>");
                out.println("<p><b>ÚLTIMO PASO:</b> Refresca la página de Yesid ahora mismo.</p>");
            }
        } catch (Exception e) {
            out.println("<h3 style='color:red;'>ERROR: " + e.getMessage() + "</h3>");
        }
    %>
</body>
</html>
