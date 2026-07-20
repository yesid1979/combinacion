<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*, com.combinacion.util.DBConnection"%>
<!DOCTYPE html>
<html>
<body>
<%
    out.println("<h2>Ejecutando Parche...</h2>");
    try (Connection conn = DBConnection.getConnection()) {
        String check = "SELECT id FROM permisos WHERE nombre = 'PUEDE_REVISAR_CUENTAS'";
        try (PreparedStatement ps = conn.prepareStatement(check);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int id = rs.getInt(1);
                String update = "UPDATE permisos SET codigo = 'REVISAR_CUENTAS_VER', modulo = 'RADICACION' WHERE id = ?";
                try (PreparedStatement psUp = conn.prepareStatement(update)) {
                    psUp.setInt(1, id);
                    psUp.executeUpdate();
                    out.println("<p style='color:green;'>Permiso actualizado correctamente para que aparezca en la tabla.</p>");
                }
            } else {
                String insert = "INSERT INTO permisos (codigo, nombre, modulo, descripcion) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psIns = conn.prepareStatement(insert)) {
                    psIns.setString(1, "REVISAR_CUENTAS_VER");
                    psIns.setString(2, "PUEDE_REVISAR_CUENTAS");
                    psIns.setString(3, "RADICACION");
                    psIns.setString(4, "Permite ser asignado como revisor de cuentas de cobro radicadas.");
                    psIns.executeUpdate();
                    out.println("<p style='color:green;'>Permiso PUEDE_REVISAR_CUENTAS insertado correctamente.</p>");
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
