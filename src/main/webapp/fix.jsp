<%@page import="com.combinacion.dao.ConsecutivoDAO"%>
<%
    ConsecutivoDAO dao = new ConsecutivoDAO();
    dao.inicializarTabla();
    out.print("OK");
%>
