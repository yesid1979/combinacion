package com.combinacion.servlets;

import com.combinacion.dao.SupervisorDAO;
import com.combinacion.models.Supervisor;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/supervisores")
public class SupervisorServlet extends HttpServlet {

    private SupervisorDAO supervisorDAO;

    @Override
    public void init() {
        supervisorDAO = new SupervisorDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "data":
                listSupervisoresData(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteSupervisor(request, response);
                break;
            default:
                listSupervisores(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("update".equals(action)) {
            updateSupervisor(request, response);
        } else {
            insertSupervisor(request, response);
        }
    }

    private void listSupervisores(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("lista_supervisores.jsp").forward(request, response);
    }

    private void listSupervisoresData(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // ... (Similar logic for DataTables pagination)
        String draw = request.getParameter("draw");
        int start = Integer.parseInt(request.getParameter("start") != null ? request.getParameter("start") : "0");
        int length = Integer.parseInt(request.getParameter("length") != null ? request.getParameter("length") : "10");
        String search = request.getParameter("search[value]");

        int orderCol = 0;
        String orderDir = "asc";
        if (request.getParameter("order[0][column]") != null) {
            orderCol = Integer.parseInt(request.getParameter("order[0][column]"));
            orderDir = request.getParameter("order[0][dir]");
        }

        int totalRecords = supervisorDAO.countAll();
        int filteredRecords = supervisorDAO.countFiltered(search);
        List<Supervisor> supervisores = supervisorDAO.findWithPagination(start, length, search, orderCol, orderDir);

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("draw", draw);
        jsonMap.put("recordsTotal", totalRecords);
        jsonMap.put("recordsFiltered", filteredRecords);
        jsonMap.put("data", supervisores);

        String json = new Gson().toJson(jsonMap);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    private void insertSupervisor(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // Implement insertion logic manually or redirect to form execution
        // For simplicity, assuming form posts here
        String nombre = request.getParameter("nombre");
        String cedula = request.getParameter("cedula");
        String cargo = request.getParameter("cargo");

        Supervisor s = new Supervisor();
        s.setNombre(nombre);
        s.setCedula(cedula);
        s.setCargo(cargo);

        if (supervisorDAO.insertar(s)) {
            response.sendRedirect("supervisores?status=created");
        } else {
            response.sendRedirect("supervisores?status=error");
        }
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Supervisor existingSupervisor = supervisorDAO.obtenerPorId(id);
            request.setAttribute("supervisor", existingSupervisor);
            request.getRequestDispatcher("form_supervisor.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect("supervisores");
        }
    }

    private void updateSupervisor(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String nombre = request.getParameter("nombre");
            String cedula = request.getParameter("cedula");
            String cargo = request.getParameter("cargo");

            Supervisor s = new Supervisor();
            s.setId(id);
            s.setNombre(nombre);
            s.setCedula(cedula);
            s.setCargo(cargo);

            if (supervisorDAO.actualizar(s)) {
                response.sendRedirect("supervisores?status=updated");
            } else {
                response.sendRedirect("supervisores?status=error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("supervisores?status=error");
        }
    }

    private void deleteSupervisor(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            if (supervisorDAO.eliminar(id)) {
                response.getWriter().write("success");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
