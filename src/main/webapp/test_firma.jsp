<%@ page import="java.sql.*, com.combinacion.util.DBConnection" %>
<%
    out.println("--- RESULTADOS FIRMAS ---");
    try (Connection conn = DBConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT id, cedula, firma_url FROM usuarios WHERE firma_url IS NOT NULL")) {
        while (rs.next()) {
            out.println("ID: " + rs.getInt("id") + " | Ced: " + rs.getString("cedula") + " | Firma: " + rs.getString("firma_url"));
        }
    } catch (Exception e) {
        out.println("Error: " + e.getMessage());
    }
%>
