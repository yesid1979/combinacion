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
        
        // Análisis de datos: Verificamos qué contratistas NO tienen usuario
        for (Contratista c : todos) {
            String cedulaOriginal = c.getCedula();
            if (cedulaOriginal != null && !cedulaOriginal.trim().isEmpty()) {
                // Limpiar la cédula para buscarla correctamente
                String cedulaLimpia = cedulaOriginal.trim().replaceAll("[^0-9]", "");
                
                if (cedulaLimpia.isEmpty()) {
                    continue; // No procesar si no tiene números
                }
                
                boolean existeComoUsername = (usuarioDAO.obtenerPorUsername(cedulaLimpia) != null);
                boolean existeComoCedula = usuarioDAO.existeCedula(cedulaLimpia, 0);
                
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
        
        int rolId = 3; // Fallback por defecto según diseño previo
        if (rolContratista != null) {
            rolId = rolContratista.getId();
        } else {
            // Intentar crearlo, si falla la BD (ej. restricción única por mayúsculas) usaremos 3
            Rol nuevoRol = new Rol();
            nuevoRol.setNombre("Contratista");
            nuevoRol.setDescripcion("Rol automático para contratistas");
            nuevoRol.setActivo(true);
            int newRolId = rolDAO.insertar(nuevoRol);
            if (newRolId > 0) {
                rolId = newRolId;
            }
        }

        int creados = 0;
        int omitidos = 0;
        StringBuilder errorMsg = new StringBuilder();

        for (String idStr : idsSeleccionados) {
            try {
                int cId = Integer.parseInt(idStr);
                Contratista c = contratistaDAO.obtenerPorId(cId);
                
                if (c != null && c.getCedula() != null && !c.getCedula().trim().isEmpty()) {
                    String cedulaOriginal = c.getCedula().trim();
                    // Limpiar cédula: quitar puntos, letras, espacios (ej: "31.449.649 de Jamundí" -> "31449649")
                    String cedulaLimpia = cedulaOriginal.replaceAll("[^0-9]", "");
                    
                    if (cedulaLimpia.isEmpty()) {
                        omitidos++;
                        errorMsg.append("Cédula inválida (sin números): ").append(cedulaOriginal).append(". ");
                        continue;
                    }
                    
                    // Doble validación por seguridad usando la cédula limpia
                    boolean existe = usuarioDAO.existeCedula(cedulaLimpia, 0) || (usuarioDAO.obtenerPorUsername(cedulaLimpia) != null);
                    
                    if (!existe) {
                        String password = cedulaLimpia; // Contraseña por defecto
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
                        
                        // Intentar obtener las fechas de su contrato si tiene uno asignado
                        Contrato contrato = contratoDAO.obtenerPorContratistaId(c.getId());
                        if (contrato != null) {
                            if (contrato.getFechaInicio() != null) {
                                u.setFechaInicioContrato(contrato.getFechaInicio());
                            }
                            if (contrato.getFechaTerminacion() != null) {
                                u.setFechaFinContrato(contrato.getFechaTerminacion());
                            }
                        }
                        
                        u.setActivo(true);
                        u.setRolId(rolId);
                        
                        int newId = usuarioDAO.insertar(u);
                        if (newId > 0) {
                            creados++;
                        } else {
                            omitidos++;
                            errorMsg.append("Error DB en cédula ").append(cedulaLimpia).append(". ");
                        }
                    } else {
                        omitidos++; // Ya existía, se omitió para proteger el perfil actual
                        errorMsg.append("Ya existía la cédula ").append(cedulaLimpia).append(". ");
                    }
                }
            } catch (Exception e) {
                omitidos++;
                errorMsg.append("Excepción en ID ").append(idStr).append(": ").append(e.getMessage()).append(". ");
            }
        }
        
        String finalMsg = "Se crearon " + creados + " usuarios. Omitidos/Existentes/Fallos: " + omitidos;
        if (errorMsg.length() > 0) {
            finalMsg += " | Detalle: " + errorMsg.toString();
        }
        
        response.sendRedirect(request.getContextPath() + "/masivo-usuarios?success=" + java.net.URLEncoder.encode(finalMsg, "UTF-8"));
    }
}
