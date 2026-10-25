package com.combinacion.servlets;

import com.combinacion.dao.NotificacionDAO;
import com.combinacion.models.Notificacion;
import com.combinacion.models.Usuario;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "NotificacionServlet", urlPatterns = {"/notificaciones"})
public class NotificacionServlet extends HttpServlet {

    private final NotificacionDAO notificacionDAO = new NotificacionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        procesarPeticion(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        procesarPeticion(request, response);
    }

    private void procesarPeticion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuario") : null;

        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Sesión no válida\"}");
            }
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "check";
        }

        try (PrintWriter out = response.getWriter()) {
            switch (action) {
                case "check": {
                    int noLeidas = notificacionDAO.contarNoLeidas(usuario.getId());
                    int ultimoId = notificacionDAO.obtenerUltimoId(usuario.getId());
                    out.print("{\"count\":" + noLeidas + ",\"latestId\":" + ultimoId + "}");
                    break;
                }
                case "list": {
                    List<Notificacion> lista = notificacionDAO.listarPorUsuario(usuario.getId(), 15);
                    int noLeidas = notificacionDAO.contarNoLeidas(usuario.getId());
                    StringBuilder sb = new StringBuilder();
                    sb.append("{\"count\":").append(noLeidas).append(",\"notifications\":[");
                    for (int i = 0; i < lista.size(); i++) {
                        Notificacion n = lista.get(i);
                        if (i > 0) sb.append(",");
                        sb.append("{")
                          .append("\"id\":").append(n.getId()).append(",")
                          .append("\"idInforme\":").append(n.getIdInforme() != null ? n.getIdInforme() : "null").append(",")
                          .append("\"titulo\":").append(escapeJson(n.getTitulo())).append(",")
                          .append("\"mensaje\":").append(escapeJson(n.getMensaje())).append(",")
                          .append("\"tipo\":").append(escapeJson(n.getTipo())).append(",")
                          .append("\"url\":").append(escapeJson(n.getUrl())).append(",")
                          .append("\"leida\":").append(n.isLeida()).append(",")
                          .append("\"tiempo\":").append(escapeJson(calcularTiempoRelativo(n.getFechaCreacion())))
                          .append("}");
                    }
                    sb.append("]}");
                    out.print(sb.toString());
                    break;
                }
                case "mark_read": {
                    String idStr = request.getParameter("id");
                    String allStr = request.getParameter("all");
                    boolean ok;
                    if ("true".equalsIgnoreCase(allStr) || idStr == null || idStr.trim().isEmpty()) {
                        ok = notificacionDAO.marcarTodasLeidas(usuario.getId());
                    } else {
                        try {
                            int idNotif = Integer.parseInt(idStr.trim());
                            ok = notificacionDAO.marcarLeida(idNotif, usuario.getId());
                        } catch (NumberFormatException e) {
                            ok = false;
                        }
                    }
                    int restantes = notificacionDAO.contarNoLeidas(usuario.getId());
                    out.print("{\"success\":" + ok + ",\"count\":" + restantes + "}");
                    break;
                }
                default:
                    out.print("{\"error\":\"Acción desconocida\"}");
                    break;
            }
        }
    }

    private String escapeJson(String valor) {
        if (valor == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    private String calcularTiempoRelativo(Timestamp ts) {
        if (ts == null) return "Reciente";
        long diffMs = System.currentTimeMillis() - ts.getTime();
        long diffSec = diffMs / 1000;
        if (diffSec < 60) {
            return "Hace un momento";
        }
        long diffMin = diffSec / 60;
        if (diffMin < 60) {
            return "Hace " + diffMin + (diffMin == 1 ? " minuto" : " minutos");
        }
        long diffHoras = diffMin / 60;
        if (diffHoras < 24) {
            return "Hace " + diffHoras + (diffHoras == 1 ? " hora" : " horas");
        }
        long diffDias = diffHoras / 24;
        if (diffDias < 7) {
            return "Hace " + diffDias + (diffDias == 1 ? " día" : " días");
        }
        long diffSemanas = diffDias / 7;
        return "Hace " + diffSemanas + (diffSemanas == 1 ? " semana" : " semanas");
    }
}
