package com.combinacion.servlets;

import com.combinacion.dao.RolDAO;
import com.combinacion.dao.SupervisorDAO;
import com.combinacion.dao.UsuarioDAO;
import com.combinacion.models.Rol;
import com.combinacion.models.Supervisor;
import com.combinacion.models.Usuario;
import com.combinacion.util.PasswordUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "MasivoSupervisoresServlet", urlPatterns = {"/masivo-supervisores"})
public class MasivoSupervisoresServlet extends HttpServlet {

    private final SupervisorDAO supervisorDAO = new SupervisorDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<Supervisor> todos = supervisorDAO.listarTodos();
        List<Supervisor> sinUsuario = new ArrayList<>();
        
        List<Usuario> todosUsuarios = usuarioDAO.listarTodos();
        java.util.Set<String> cedulasExistentes = new java.util.HashSet<>();
        java.util.Set<String> usernamesExistentes = new java.util.HashSet<>();
        
        if (todosUsuarios != null) {
            for (Usuario u : todosUsuarios) {
                if (u.getCedula() != null) {
                    cedulasExistentes.add(u.getCedula().replaceAll("[^0-9]", ""));
                }
                if (u.getUsername() != null) {
                    usernamesExistentes.add(u.getUsername().replaceAll("[^0-9]", ""));
                }
            }
        }
        
        for (Supervisor s : todos) {
            String cedulaOriginal = s.getCedula();
            if (cedulaOriginal != null && !cedulaOriginal.trim().isEmpty()) {
                String cedulaLimpia = cedulaOriginal.trim().replaceAll("[^0-9]", "");
                
                if (cedulaLimpia.isEmpty()) {
                    continue;
                }
                
                boolean existeComoUsername = usernamesExistentes.contains(cedulaLimpia);
                boolean existeComoCedula = cedulasExistentes.contains(cedulaLimpia);
                
                if (!existeComoUsername && !existeComoCedula) {
                    sinUsuario.add(s);
                }
            }
        }
        
        request.setAttribute("listaSinUsuario", sinUsuario);
        request.getRequestDispatcher("/masivo_supervisores.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String[] idsSeleccionados = request.getParameterValues("supervisor_ids");
        
        if (idsSeleccionados == null || idsSeleccionados.length == 0) {
            response.sendRedirect(request.getContextPath() + "/masivo-supervisores?error=Seleccione al menos un supervisor");
            return;
        }

        Rol rolSupervisor = rolDAO.obtenerPorNombre("Supervisor");
        if (rolSupervisor == null) {
            rolSupervisor = rolDAO.obtenerPorNombre("SUPERVISOR");
        }
        
        int tempRolId = 0;
        if (rolSupervisor != null) {
            tempRolId = rolSupervisor.getId();
        } else {
            Rol nuevoRol = new Rol();
            nuevoRol.setNombre("Supervisor");
            nuevoRol.setDescripcion("Rol para supervisores de contratos");
            nuevoRol.setActivo(true);
            tempRolId = rolDAO.insertar(nuevoRol);
        }
        final int rolId = tempRolId;

        int creados = 0;
        int omitidos = 0;

        for (String idStr : idsSeleccionados) {
            try {
                int sId = Integer.parseInt(idStr);
                Supervisor s = supervisorDAO.obtenerPorId(sId);
                
                if (s != null && s.getCedula() != null && !s.getCedula().trim().isEmpty()) {
                    String cedulaOriginal = s.getCedula().trim();
                    String cedulaLimpia = cedulaOriginal.replaceAll("[^0-9]", "");
                    
                    if (cedulaLimpia.isEmpty()) {
                        omitidos++;
                        continue;
                    }
                    
                    boolean existe = usuarioDAO.existeCedula(cedulaLimpia, 0) || (usuarioDAO.obtenerPorUsername(cedulaLimpia) != null);
                    
                    if (!existe) {
                        String password = cedulaLimpia;
                        String salt = PasswordUtils.generateSalt();
                        String hash = PasswordUtils.hashPassword(password, salt);

                        Usuario u = new Usuario();
                        u.setUsername(cedulaLimpia);
                        u.setPasswordHash(hash);
                        u.setSalt(salt);
                        u.setNombreCompleto(s.getNombre());
                        u.setCorreo("");
                        u.setCedula(cedulaLimpia);
                        u.setCelular("");
                        u.setCargo(s.getCargo() != null ? s.getCargo() : "Supervisor");
                        u.setVinculacion("SUPERVISOR");
                        u.setActivo(true);
                        u.setRolId(rolId);
                        
                        usuarioDAO.insertar(u);
                        creados++;
                    } else {
                        omitidos++;
                    }
                } else {
                    omitidos++;
                }
            } catch (Exception e) {
                System.err.println("Error procesando supervisor ID " + idStr + ": " + e.getMessage());
                omitidos++;
            }
        }

        String finalMsg = "Proceso completado: " + creados + " usuario(s) de supervisores creado(s) con éxito.";
        if (omitidos > 0) {
            finalMsg += " (" + omitidos + " omitido(s) por cédula duplicada o inválida).";
        }
        
        response.sendRedirect(request.getContextPath() + "/masivo-supervisores?success=" + java.net.URLEncoder.encode(finalMsg, "UTF-8"));
    }
}
