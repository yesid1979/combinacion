<%@ page import="com.combinacion.models.Usuario, com.combinacion.models.Permiso, java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head><title>Inspector de Permisos</title></head>
<body>
<%
    Usuario u = (Usuario) session.getAttribute("usuario");
    if (u == null) {
        out.println("No hay usuario en sesión.");
    } else {
        out.println("<h2>Usuario: " + u.getNombreCompleto() + " (ID: " + u.getId() + ")</h2>");
        out.println("<h3>Rol: " + (u.getRol() != null ? u.getRol().getNombre() : "SIN ROL") + "</h3>");
        out.println("<h3>Permisos Especiales (Personalizados):</h3><ul>");
        if (u.getPermisosEspeciales() != null) {
            for (Permiso p : u.getPermisosEspeciales()) {
                out.println("<li><b>Modulo:</b> " + p.getModulo() + " | <b>Codigo:</b> " + p.getCodigo() + "</li>");
            }
        }
        out.println("</ul>");
    }
%>
</body>
</html>
