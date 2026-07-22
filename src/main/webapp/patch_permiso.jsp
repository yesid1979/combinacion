<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*, com.combinacion.util.DBConnection"%>
<!DOCTYPE html>
<html>
<body>
<%
    out.println("<h2>Ejecutando Parche de Permiso Auditoría...</h2>");
    try (Connection conn = DBConnection.getConnection()) {
        String check = "SELECT id FROM permisos WHERE codigo = 'AUDITORIA_VER'";
        try (PreparedStatement ps = conn.prepareStatement(check);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                out.println("<p style='color:orange;'>El permiso AUDITORIA_VER ya existe.</p>");
            } else {
                String insert = "INSERT INTO permisos (codigo, nombre, modulo, descripcion) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psIns = conn.prepareStatement(insert)) {
                    psIns.setString(1, "AUDITORIA_VER");
                    psIns.setString(2, "Ver Auditoría del Sistema");
                    psIns.setString(3, "ADMINISTRACION");
                    psIns.setString(4, "Permite visualizar el registro de auditoría global del sistema.");
                    psIns.executeUpdate();
                    out.println("<p style='color:green;'>Permiso AUDITORIA_VER insertado correctamente en la base de datos.</p>");
                }
            }
        }
    } catch (Exception e) {
        out.println("<p style='color:red;'>Error: " + e.getMessage() + "</p>");
        e.printStackTrace();
    }
%>
</body>
</html>
