<%@ page import="java.sql.*" %>
<%@ page import="javax.naming.*" %>
<%@ page import="javax.sql.*" %>
<%
    try {
        Context initContext = new InitialContext();
        Context envContext = (Context) initContext.lookup("java:comp/env");
        DataSource ds = (DataSource) envContext.lookup("jdbc/combinacion");
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("ALTER TABLE contratos ADD COLUMN iva_si_no VARCHAR(10)");
        out.println("OK: Columna iva_si_no agregada.");
        conn.close();
    } catch (Exception e) {
        out.println("ERROR: " + e.getMessage());
    }
%>
