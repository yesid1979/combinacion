package com.combinacion.servlets;
import com.combinacion.dao.AuditoriaDAO;
import com.combinacion.models.Auditoria;
import com.combinacion.models.Usuario;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/admin/AuditoriaDataServlet")
public class AuditoriaDataServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario u = (Usuario) request.getSession().getAttribute("usuario");
        if (u == null || (!u.esAdministrador() && !u.tienePermiso("ADMIN_USUARIOS"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            int draw = Integer.parseInt(request.getParameter("draw") != null ? request.getParameter("draw") : "1");
            int start = Integer.parseInt(request.getParameter("start") != null ? request.getParameter("start") : "0");
            int length = Integer.parseInt(request.getParameter("length") != null ? request.getParameter("length") : "10");
            String searchValue = request.getParameter("search[value]");
            String orderColumn = request.getParameter("order[0][column]");
            String orderDir = request.getParameter("order[0][dir]");

            AuditoriaDAO dao = new AuditoriaDAO();
            int totalRecords = dao.getTotalRegistros(null);
            int filteredRecords = (searchValue != null && !searchValue.isEmpty()) ? dao.getTotalRegistros(searchValue) : totalRecords;
            
            List<Auditoria> dataList = dao.getRegistrosPaginados(start, length, searchValue, orderColumn, orderDir);

            JSONObject jsonResult = new JSONObject();
            jsonResult.put("draw", draw);
            jsonResult.put("recordsTotal", totalRecords);
            jsonResult.put("recordsFiltered", filteredRecords);

            JSONArray dataArray = new JSONArray();
            SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat timeFmt = new SimpleDateFormat("hh:mm:ss a");

            for (Auditoria a : dataList) {
                JSONObject row = new JSONObject();
                row.put("id", a.getId());
                row.put("fecha", a.getFechaHora() != null ? dateFmt.format(a.getFechaHora()) : "");
                row.put("hora", a.getFechaHora() != null ? timeFmt.format(a.getFechaHora()) : "");
                row.put("username", a.getUsername());
                row.put("nombres_apellidos", a.getNombresApellidos());
                row.put("tipo_accion", a.getTipoAccion());
                
                // Estos campos irán en los atributos ocultos
                row.put("accion_realizada", a.getAccionRealizada());
                row.put("tipo_usuario", a.getTipoUsuario());
                row.put("ip_address", a.getIpAddress());
                
                dataArray.put(row);
            }
            jsonResult.put("data", dataArray);
            
            response.getWriter().write(jsonResult.toString());
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject err = new JSONObject();
            err.put("error", "Error interno en el servidor");
            response.getWriter().write(err.toString());
        }
    }
}
