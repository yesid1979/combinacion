<%@ page import="java.sql.*, com.combinacion.util.DBConnection" %>
<%
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement("UPDATE informes_supervision SET soportes_json = NULL WHERE numero_cuota = '5'")) {
        int rows = ps.executeUpdate();
        out.println("JSON limpiado para " + rows + " cuotas 5. Vuelve a guardar la cuota 5 en el sistema y heredará los archivos limpios sin duplicarlos más.");
    } catch (Exception e) {
        out.println("Error: " + e.getMessage());
    }
%>
