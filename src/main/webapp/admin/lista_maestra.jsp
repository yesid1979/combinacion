<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, javax.sql.*, javax.naming.*" %>
<!DOCTYPE html>
<html>
<head><title>LISTA MAESTRA DE PERMISOS</title></head>
<body style="font-family: sans-serif; padding: 30px;">
    <h2>Lista Maestra de Permisos en la DB</h2>
    <table border="1" cellpadding="10" style="border-collapse: collapse; width: 100%;">
        <tr style="background-color: #34495e; color: #fff;">
            <th>ID</th><th>CÓDIGO</th><th>NOMBRE</th><th>MÓDULO</th>
        </tr>
        <%
            try {
                Context initContext = new InitialContext();
                Context envContext = (Context) initContext.lookup("java:comp/env");
                DataSource ds = (DataSource) envContext.lookup("jdbc/combinacion");
                try (Connection conn = ds.getConnection()) {
                    String sql = "SELECT id, codigo, nombre, modulo FROM permisos ORDER BY modulo, codigo";
                    try (PreparedStatement ps = conn.prepareStatement(sql);
                         ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            out.println("<tr><td>" + rs.getInt("id") + "</td><td>" + rs.getString("codigo") + "</td><td>" + rs.getString("nombre") + "</td><td>" + rs.getString("modulo") + "</td></tr>");
                        }
                    }
                }
            } catch (Exception e) {
                out.println("<h3>ERROR: " + e.getMessage() + "</h3>");
            }
        %>
    </table>
</body>
</html>
