package com.combinacion.servlets;

import com.combinacion.dao.RevisorDocumentoDAO;
import com.combinacion.models.RevisorDocumento;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

@WebServlet(name = "RevisorDocumentoServlet", urlPatterns = { "/revisores" })
public class RevisorDocumentoServlet extends HttpServlet {

    private RevisorDocumentoDAO revisorDAO = new RevisorDocumentoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "data":
                responderDatosTabla(request, response);
                break;
            case "new":
                request.getRequestDispatcher("form_revisor.jsp").forward(request, response);
                break;
            case "view":
            case "edit":
                mostrarFormularioEdicion(request, response);
                break;
            case "delete":
                eliminar(request, response);
                break;
            case "list":
            default:
                List<RevisorDocumento> lista = revisorDAO.listarTodos();
                request.setAttribute("listaRevisores", lista);
                request.getRequestDispatcher("lista_revisores.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) action = "insert";

        switch (action) {
            case "data":
                responderDatosTabla(request, response);
                break;
            case "update":
                actualizar(request, response);
                break;
            default:
                insertar(request, response);
                break;
        }
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            RevisorDocumento existing = revisorDAO.obtenerPorId(id);
            request.setAttribute("revisor", existing);
            if ("view".equals(request.getParameter("action"))) {
                request.setAttribute("readonly", true);
            }
            request.getRequestDispatcher("form_revisor.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect("revisores");
        }
    }

    private void insertar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        RevisorDocumento r = new RevisorDocumento();
        r.setTipoDocumento(decode(request.getParameter("tipoDocumento")));
        r.setNombreCompleto(decode(request.getParameter("nombreCompleto")));
        r.setCargo(decode(request.getParameter("cargo")));

        if (revisorDAO.insertar(r)) {
            response.sendRedirect("revisores?status=created");
        } else {
            response.sendRedirect("revisores?status=error");
        }
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            RevisorDocumento r = new RevisorDocumento();
            r.setId(id);
            r.setTipoDocumento(decode(request.getParameter("tipoDocumento")));
            r.setNombreCompleto(decode(request.getParameter("nombreCompleto")));
            r.setCargo(decode(request.getParameter("cargo")));

            if (revisorDAO.actualizar(r)) {
                response.sendRedirect("revisores?status=updated");
            } else {
                response.sendRedirect("revisores?status=error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("revisores?status=error");
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            if (revisorDAO.eliminar(id)) {
                response.getWriter().write("success");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private String decode(String val) {
        if (val == null) return null;
        try {
            // Convierte de ISO-8859-1 a UTF-8 por si el servidor no respeta el setCharacterEncoding
            return new String(val.getBytes("ISO-8859-1"), "UTF-8");
        } catch (Exception e) {
            return val;
        }
    }

    private void responderDatosTabla(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String drawStr = request.getParameter("draw");
        int draw = (drawStr != null && !drawStr.isEmpty()) ? Integer.parseInt(drawStr) : 1;
        String startStr = request.getParameter("start");
        int start = (startStr != null && !startStr.isEmpty()) ? Integer.parseInt(startStr) : 0;
        String lengthStr = request.getParameter("length");
        int length = (lengthStr != null && !lengthStr.isEmpty()) ? Integer.parseInt(lengthStr) : 10;
        String searchValue = request.getParameter("search[value]");
        if (searchValue != null) searchValue = searchValue.toLowerCase();

        List<RevisorDocumento> listaTotal = revisorDAO.listarTodos();
        int recordsTotal = listaTotal.size();

        List<RevisorDocumento> listaFiltrada = new ArrayList<>();
        if (searchValue != null && !searchValue.isEmpty()) {
            for (RevisorDocumento r : listaTotal) {
                if ((r.getNombreCompleto() != null && r.getNombreCompleto().toLowerCase().contains(searchValue)) ||
                    (r.getCargo() != null && r.getCargo().toLowerCase().contains(searchValue)) ||
                    (r.getTipoDocumento() != null && r.getTipoDocumento().toLowerCase().contains(searchValue))) {
                    listaFiltrada.add(r);
                }
            }
        } else {
            listaFiltrada = listaTotal;
        }
        int recordsFiltered = listaFiltrada.size();

        int toIndex = Math.min(start + length, listaFiltrada.size());
        List<RevisorDocumento> page = new ArrayList<>();
        if (start < listaFiltrada.size()) {
            page = listaFiltrada.subList(start, toIndex);
        }

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("draw", draw);
        jsonMap.put("recordsTotal", recordsTotal);
        jsonMap.put("recordsFiltered", recordsFiltered);
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        List<Map<String, Object>> dataArray = new ArrayList<>();
        for (RevisorDocumento r : page) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", r.getId());
            row.put("tipoDocumento", r.getTipoDocumento());
            row.put("nombreCompleto", r.getNombreCompleto());
            row.put("cargo", r.getCargo());
            row.put("fechaStr", r.getFechaActualizacion() != null ? sdf.format(r.getFechaActualizacion()) : "");
            dataArray.add(row);
        }
        jsonMap.put("data", dataArray);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(jsonMap));
    }
}
