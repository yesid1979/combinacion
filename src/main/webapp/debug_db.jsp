<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.combinacion.services.InformeSupervisionService" %>
<%@ page import="com.combinacion.models.InformeSupervision" %>
<%@ page import="com.combinacion.models.Contrato" %>
<%@ page import="com.combinacion.util.ObligacionesParser" %>
<%@ page import="java.util.List" %>
<html>
<body>
<%
    InformeSupervisionService srv = new InformeSupervisionService();
    InformeSupervision info = srv.obtenerPorId(1);
    if (info != null) {
        out.println("<b>Contrato ID:</b> " + info.getContratoId() + "<br>");
        out.println("<b>Concepto Supervisor:</b> " + info.getConceptoSupervisor() + "<br>");
        if (info.getContratoId() != null) {
            Contrato c = srv.obtenerContrato(info.getContratoId());
            if (c != null) {
                out.println("<b>Actividades Contrato:</b> " + c.getActividadesEntregables() + "<br>");
                List<ObligacionesParser.ObligacionActividad> lista = ObligacionesParser.decodificarConcepto(info.getConceptoSupervisor(), c.getActividadesEntregables());
                out.println("<b>Lista Size:</b> " + lista.size() + "<br>");
                for(ObligacionesParser.ObligacionActividad oa : lista) {
                    out.println("<li>" + oa.obligacion + " | " + oa.actividad + "</li>");
                }
            } else {
                out.println("Contrato no encontrado.<br>");
            }
        }
    } else {
        out.println("Informe 1 no encontrado.<br>");
    }
%>
</body>
</html>
