<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, javax.sql.*, javax.naming.*" %>
<!DOCTYPE html>
<html>
<head><title>DIAGNOSTICO USUARIO</title></head>
<body>
    <h2>Diagnóstico de Usuario: yesid.piedrahita</h2>
    <%
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/combinacion");
            try (Connection conn = ds.getConnection()) {
                // 1. Buscar ID del usuario
                String sqlUser = "SELECT id, username, nombre_completo, rol_id FROM usuarios WHERE username LIKE '%yesid%'";
                out.println("<h3>Usuarios encontrados con 'yesid':</h3><ul>");
                int yesidId = -1;
                try (PreparedStatement ps = conn.prepareStatement(sqlUser);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        yesidId = rs.getInt("id");
                        out.println("<li>ID: " + yesidId + " | Username: " + rs.getString("username") + " | Nombre: " + rs.getString("nombre_completo") + "</li>");
                    }
                }
                out.println("</ul>");

                if (yesidId != -1) {
                    // 2. Buscar sus permisos especiales
                    String sqlPerms = "SELECT p.codigo, p.nombre FROM permisos p INNER JOIN usuario_permisos up ON p.id = up.permiso_id WHERE up.usuario_id = ?";
                    out.println("<h3>Permisos Especiales en DB para ID " + yesidId + ":</h3><ul>");
                    try (PreparedStatement ps = conn.prepareStatement(sqlPerms)) {
                        ps.setInt(1, yesidId);
                        try (ResultSet rs = ps.executeQuery()) {
                            int count = 0;
                            while (rs.next()) {
                                count++;
                                out.println("<li>Código: " + rs.getString("codigo") + " | " + rs.getString("nombre") + "</li>");
                            }
                            if (count == 0) out.println("<li style='color:red;'>¡NO TIENE PERMISOS EN LA TABLA usuario_permisos!</li>");
                        }
                    }
                    out.println("</ul>");
                }
            }
        } catch (Exception e) {
            out.println("ERROR: " + e.getMessage());
            e.printStackTrace(new java.io.PrintWriter(out));
        }
    %>
</body>
</html>
