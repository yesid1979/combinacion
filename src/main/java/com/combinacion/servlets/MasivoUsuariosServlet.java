package com.combinacion.servlets;

import com.combinacion.dao.ContratoDAO;
import com.combinacion.dao.ContratistaDAO;
import com.combinacion.dao.RolDAO;
import com.combinacion.dao.UsuarioDAO;
import com.combinacion.models.Contrato;
import com.combinacion.models.Contratista;
import com.combinacion.models.Rol;
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

@WebServlet(name = "MasivoUsuariosServlet", urlPatterns = {"/masivo-usuarios"})
public class MasivoUsuariosServlet extends HttpServlet {

    private final ContratistaDAO contratistaDAO = new ContratistaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();
    private final ContratoDAO contratoDAO = new ContratoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<Contratista> todos = contratistaDAO.listarTodos();
        List<Contratista> sinUsuario = new ArrayList<>();
        
        // Optimización: Cargar todos los usuarios en memoria para evitar N consultas a la base de datos
        List<Usuario> todosUsuarios = usuarioDAO.listarTodos();
        java.util.Set<String> cedulasExistentes = new java.util.HashSet<>();
        java.util.Set<String> usernamesExistentes = new java.util.HashSet<>();
        
        if (todosUsuarios != null) {
            for (Usuario u : todosUsuarios) {
                if (u.getCedula() != null) cedulasExistentes.add(u.getCedula());
                if (u.getUsername() != null) usernamesExistentes.add(u.getUsername());
            }
        }
        
        // Análisis de datos: Verificamos qué contratistas NO tienen usuario
        for (Contratista c : todos) {
            String cedulaOriginal = c.getCedula();
            if (cedulaOriginal != null && !cedulaOriginal.trim().isEmpty()) {
                // Limpiar la cédula para buscarla correctamente
                String cedulaLimpia = cedulaOriginal.trim().replaceAll("[^0-9]", "");
                
                if (cedulaLimpia.isEmpty()) {
                    continue; // No procesar si no tiene números
                }
                
                boolean existeComoUsername = usernamesExistentes.contains(cedulaLimpia);
                boolean existeComoCedula = cedulasExistentes.contains(cedulaLimpia);
                
                if (!existeComoUsername && !existeComoCedula) {
                    sinUsuario.add(c);
                }
            }
        }
        
        request.setAttribute("listaSinUsuario", sinUsuario);
        request.getRequestDispatcher("/masivo_usuarios.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String[] idsSeleccionados = request.getParameterValues("contratista_ids");
        
        if (idsSeleccionados == null || idsSeleccionados.length == 0) {
            response.sendRedirect(request.getContextPath() + "/masivo-usuarios?error=Seleccione al menos un contratista");
            return;
        }

        // Obtener el rol "Contratista"
        Rol rolContratista = rolDAO.obtenerPorNombre("Contratista");
        if (rolContratista == null) {
            rolContratista = rolDAO.obtenerPorNombre("CONTRATISTA");
        }
        
        int tempRolId = 3;
        if (rolContratista != null) {
            tempRolId = rolContratista.getId();
        } else {
            Rol nuevoRol = new Rol();
            nuevoRol.setNombre("Contratista");
            nuevoRol.setDescripcion("Rol automático para contratistas");
            nuevoRol.setActivo(true);
            int newRolId = rolDAO.insertar(nuevoRol);
            if (newRolId > 0) {
                tempRolId = newRolId;
            }
        }
        final int rolId = tempRolId;

        // EJECUTAR EN SEGUNDO PLANO PARA NO BLOQUEAR LA PANTALLA
        new Thread(() -> {
            int creados = 0;
            int omitidos = 0;

            for (String idStr : idsSeleccionados) {
                try {
                    int cId = Integer.parseInt(idStr);
                    Contratista c = contratistaDAO.obtenerPorId(cId);
                    
                    if (c != null && c.getCedula() != null && !c.getCedula().trim().isEmpty()) {
                        String cedulaOriginal = c.getCedula().trim();
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
                            u.setNombreCompleto(c.getNombre());
                            u.setCorreo(c.getCorreo() != null ? c.getCorreo() : "");
                            u.setCedula(cedulaLimpia);
                            u.setCelular(c.getTelefono() != null ? c.getTelefono() : "");
                            u.setVinculacion("CONTRATISTA");
                            
                            Contrato contrato = contratoDAO.obtenerPorContratistaId(c.getId());
                            if (contrato != null) {
                                if (contrato.getFechaInicio() != null) u.setFechaInicioContrato(contrato.getFechaInicio());
                                if (contrato.getFechaTerminacion() != null) u.setFechaFinContrato(contrato.getFechaTerminacion());
                            }
                            
                            u.setActivo(true);
                            u.setRolId(rolId);
                            
                            int newId = usuarioDAO.insertar(u);
                            if (newId > 0) {
                                creados++;
                            } else {
                                omitidos++;
                            }
                        } else {
                            omitidos++;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("[MasivoUsuarios] Tarea asíncrona completada: Creados=" + creados + " | Omitidos=" + omitidos);
        }).start();
        
        String finalMsg = "Se ha iniciado la creación de usuarios en segundo plano. Esto evitará que la pantalla se congele. ¡Puedes seguir trabajando! Los perfiles estarán listos en unos momentos.";
        try { com.combinacion.models.Usuario __u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario"); if(__u!=null) com.combinacion.dao.AuditoriaDAO.registrar(__u, "Carga Masiva", "Carga masiva de usuarios procesada", request.getRemoteAddr()); } catch(Exception ex){}
            response.sendRedirect(request.getContextPath() + "/masivo-usuarios?success=" + java.net.URLEncoder.encode(finalMsg, "UTF-8"));
    }
}
