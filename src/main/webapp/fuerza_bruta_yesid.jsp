<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, javax.sql.*, javax.naming.*" %>
<!DOCTYPE html>
<html>
<head><title>REPARACIÓN FORZADA DE YESID</title></head>
<body style="font-family: sans-serif; padding: 30px;">
    <h2 style="color: #2c3e50;">Reparación Forzada de Permisos: Yesid</h2>
    <%
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/combinacion");
            try (Connection conn = ds.getConnection()) {
                conn.setAutoCommit(true);
                
                // 1. Obtener ID de Yesid
                int yesidId = -1;
                String sqlUser = "SELECT id FROM usuarios WHERE username LIKE '%yesid%' LIMIT 1";
                try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) yesidId = rs.getInt("id");
                    }
                }
                
                if (yesidId == -1) {
                    out.println("<p style='color:red;'>ERROR: No se encontró al usuario Yesid.</p>");
                } else {
                    out.println("<p style='color:blue;'>Cargando ID de Yesid: " + yesidId + "</p>");
                    
                    // 2. Obtener IDs de permisos críticos
                    out.println("<h3>Insertando permisos faltantes...</h3><ul>");
                    // Intentamos varios códigos comunes para Carga Masiva y otros
                    String[] codes = {"CARGA_MASIVA_VER", "CARGA_MASIVA", "MASIVOS", "MASIVOS_VER", "CONTRATOS_VER", "CONTRATISTAS_VER", "COMBINACION_VER"};
                    
                    for (String code : codes) {
                        int pId = -1;
                        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM permisos WHERE codigo = ? OR nombre LIKE ? LIMIT 1")) {
                            ps.setString(1, code);
                            ps.setString(2, "%" + code + "%");
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) pId = rs.getInt("id");
                            }
                        }
                        
                        if (pId != -1) {
                            // Insertar si no existe
                            String sqlIns = "INSERT INTO usuario_permisos (usuario_id, permiso_id) " +
                                            "SELECT ?, ? WHERE NOT EXISTS (" +
                                            "SELECT 1 FROM usuario_permisos WHERE usuario_id = ? AND permiso_id = ?)";
                            try (PreparedStatement ps = conn.prepareStatement(sqlIns)) {
                                ps.setInt(1, yesidId);
                                ps.setInt(2, pId);
                                ps.setInt(3, yesidId);
                                ps.setInt(4, pId);
                                int rows = ps.executeUpdate();
                                if (rows > 0) out.println("<li style='color:green;'>INSERTADO: " + code + " (ID: "+pId+")</li>");
                                else out.println("<li style='color:orange;'>YA EXISTÍA: " + code + " (Ignorado)</li>");
                            }
                        }
                    }
                    out.println("</ul>");
                    
                    out.println("<h2 style='color:green;'>¡REPARACIÓN COMPLETADA!</h2>");
                    out.println("<p><b>PASO FINAL:</b> Por favor, <b>CERRAR SESIÓN DE YESID</b> y volver a entrar ahora mismo.</p>");
                }
            }
        } catch (Exception e) {
            out.println("<h3 style='color:red;'>ERROR DE DB: " + e.getMessage() + "</h3>");
            e.printStackTrace(new java.io.PrintWriter(out));
        }
    %>
</body>
</html>
