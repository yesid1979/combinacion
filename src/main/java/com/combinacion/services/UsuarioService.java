package com.combinacion.services;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import com.combinacion.models.Rol;


import com.combinacion.dao.UsuarioDAO;
import com.combinacion.models.Usuario;
import com.combinacion.util.PasswordUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;

/**
 * Servicio para la lógica de negocio de Usuarios.
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final Gson gson = new Gson();
    private final RolService rolService = new RolService();


    /**
     * Lista todos los usuarios.
     */
    public List<Usuario> listarTodos() {
        return usuarioDAO.listarTodos();
    }

    /**
     * Obtiene un usuario por ID.
     */
    public Usuario obtenerPorId(int id) {
        return usuarioDAO.obtenerPorId(id);
    }

    /**
     * Autentica un usuario por username y password.
     */
    public Usuario autenticar(String username, String password) {
        if (username == null || password == null) return null;
        Usuario usuario = usuarioDAO.obtenerPorUsername(username.trim());
        if (usuario != null && PasswordUtils.verifyPassword(password, usuario.getPasswordHash(), usuario.getSalt())) {
            return usuario;
        }
        return null;
    }

    /**
     * Crea un nuevo usuario.
     * @return null si fue exitoso, o mensaje de error.
     */
    public String crear(String username, String password, String nombreCompleto,
                        String correo, String cedula, String celular, String sexo, 
                        String vinculacion, java.sql.Date fechaInicio, java.sql.Date fechaFin, int rolId) {
        // Validaciones
        if (username == null || username.trim().isEmpty()) {
            return "El nombre de usuario es obligatorio.";
        }
        if (password == null || password.trim().isEmpty()) {
            return "La contraseña es obligatoria.";
        }
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return "El nombre completo es obligatorio.";
        }
        if (cedula == null || cedula.trim().isEmpty()) {
            return "El número de cédula es obligatorio.";
        }
        if (rolId <= 0) {
            return "Debe seleccionar un rol.";
        }

        // Verificar duplicados
        if (usuarioDAO.existeUsername(username.trim(), 0)) {
            return "El nombre de usuario '" + username + "' ya está en uso.";
        }
        if (usuarioDAO.existeCedula(cedula.trim(), 0)) {
            return "Ya existe un usuario registrado con la cédula '" + cedula + "'.";
        }

        // Crear usuario con password hasheado
        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hashPassword(password, salt);

        Usuario usuario = new Usuario();
        usuario.setUsername(username.trim());
        usuario.setPasswordHash(hash);
        usuario.setSalt(salt);
        usuario.setNombreCompleto(nombreCompleto.trim());
        usuario.setCorreo(correo != null ? correo.trim() : null);
        usuario.setCedula(cedula != null ? cedula.trim() : null);
        usuario.setCelular(celular != null ? celular.trim() : null);
        usuario.setSexo(sexo);
        usuario.setVinculacion(vinculacion);
        usuario.setFechaInicioContrato(fechaInicio);
        usuario.setFechaFinContrato(fechaFin);
        usuario.setActivo(true);
        usuario.setRolId(rolId);

        int id = usuarioDAO.insertar(usuario);
        if (id <= 0) {
            return "Error al crear el usuario en la base de datos.";
        }
        return null; // Éxito
    }

    /**
     * Actualiza los datos de un usuario (sin cambiar la contraseña).
     * @return null si fue exitoso, o mensaje de error.
     */
    public String actualizar(int id, String username, String nombreCompleto,
                             String correo, String cedula, String celular, String sexo, 
                             String vinculacion, java.sql.Date fechaInicio, java.sql.Date fechaFin, 
                             boolean activo, int rolId) {
        if (username == null || username.trim().isEmpty()) {
            return "El nombre de usuario es obligatorio.";
        }
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return "El nombre completo es obligatorio.";
        }
        if (cedula == null || cedula.trim().isEmpty()) {
            return "El número de cédula es obligatorio.";
        }
        if (rolId <= 0) {
            return "Debe seleccionar un rol.";
        }

        // Verificar username duplicado excluyendo el usuario actual
        if (usuarioDAO.existeUsername(username.trim(), id)) {
            return "El nombre de usuario '" + username + "' ya está en uso.";
        }
        // Verificar cédula duplicada
        if (usuarioDAO.existeCedula(cedula.trim(), id)) {
            return "Ya existe otro usuario registrado con la cédula '" + cedula + "'.";
        }

        Usuario usuario = usuarioDAO.obtenerPorId(id);
        if (usuario == null) {
            return "Usuario no encontrado.";
        }

        usuario.setUsername(username.trim());
        usuario.setNombreCompleto(nombreCompleto.trim());
        usuario.setCorreo(correo != null ? correo.trim() : null);
        usuario.setCedula(cedula != null ? cedula.trim() : null);
        usuario.setCelular(celular != null ? celular.trim() : null);
        usuario.setSexo(sexo);
        usuario.setVinculacion(vinculacion);
        usuario.setFechaInicioContrato(fechaInicio);
        usuario.setFechaFinContrato(fechaFin);
        usuario.setActivo(activo);
        usuario.setRolId(rolId);

        if (!usuarioDAO.actualizar(usuario)) {
            return "Error al actualizar el usuario en la base de datos.";
        }
        return null; // Éxito
    }

    /**
     * Cambia la contraseña de un usuario.
     */
    public String cambiarPassword(int id, String nuevaPassword) {
        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }

        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hashPassword(nuevaPassword, salt);

        if (!usuarioDAO.actualizarPassword(id, hash, salt)) {
            return "Error al cambiar la contraseña.";
        }
        return null; // Éxito
    }

    /**
     * Elimina un usuario por ID.
     */
    public boolean eliminar(int id) {
        return usuarioDAO.eliminar(id);
    }

    public String actualizarPerfil(int id, String nombre, String correo, String celular) {
        if (nombre == null || nombre.trim().isEmpty()) return "El nombre es obligatorio.";
        if (correo == null || correo.trim().isEmpty()) return "El correo es obligatorio.";
        
        if (usuarioDAO.actualizarPerfil(id, nombre.trim(), correo.trim(), celular != null ? celular.trim() : "")) {
            return null;
        }
        return "Error al actualizar los datos en la base de datos.";
    }

    public boolean actualizarFoto(int id, String fotoUrl) {
        return usuarioDAO.actualizarFoto(id, fotoUrl);
    }

    /**
     * Actualiza los permisos especiales (dinámicos) de un usuario.
     */
    public boolean actualizarPermisosEspeciales(int id, List<Integer> permisosIds) {
        return usuarioDAO.actualizarPermisosEspeciales(id, permisosIds);
    }

    /**
     * Cuenta el total de usuarios registrados.
     */
    public int contarTotal() {
        return usuarioDAO.contarTotal();
    }

    /**
     * Verifica si una cédula ya existe.
     */
    public boolean existeCedula(String cedula, int excludeId) {
        if (cedula == null || cedula.trim().isEmpty()) return false;
        return usuarioDAO.existeCedula(cedula.trim(), excludeId);
    }

    /**
     * Verifica si un username ya existe.
     */
    public boolean existeUsername(String username, int excludeId) {
        if (username == null || username.trim().isEmpty()) return false;
        return usuarioDAO.existeUsername(username.trim(), excludeId);
    }

    /**
     * Genera el JSON de respuesta para DataTables de Usuarios.
     */
    public String generarJsonDataTables(int draw, int start, int length,
            String searchValue, String sortCol, String orderDir) {

        int total    = usuarioDAO.contarTotal();
        int filtered = usuarioDAO.countFiltered(searchValue);
        List<Usuario> list = usuarioDAO.findWithPagination(start, length, searchValue, sortCol, orderDir);

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("draw", draw);
        jsonResponse.addProperty("recordsTotal", total);
        jsonResponse.addProperty("recordsFiltered", filtered);
 
        JsonArray dataArray = new JsonArray();

        for (Usuario u : list) {
            JsonArray rowArr = new JsonArray();
            rowArr.add(u.getCedula() != null ? u.getCedula().trim() : "");
            rowArr.add(u.getUsername() != null ? u.getUsername().trim() : "");
            rowArr.add(u.getNombreCompleto() != null ? u.getNombreCompleto().trim() : "");
            rowArr.add(u.getCorreo() != null ? u.getCorreo().trim() : "");
            rowArr.add(u.getVinculacion() != null ? u.getVinculacion().trim() : "");
            rowArr.add(u.isActivo());
            rowArr.add(String.valueOf(u.getId()));
            dataArray.add(rowArr);
        }
 
        jsonResponse.add("data", dataArray);
        return gson.toJson(jsonResponse);
    }

public void actualizarMiPerfil(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Usuario logged = (Usuario) session.getAttribute("usuario");
        if (logged == null) return;

        String nombre = request.getParameter("nombre");
        String correo = request.getParameter("correo");
        String celular = request.getParameter("celular");
        String direccion = request.getParameter("direccion");
        String fechaNacimientoStr = request.getParameter("fecha_nacimiento");
        String edadStr = request.getParameter("edad");

        String error = this.actualizarPerfil(logged.getId(), nombre, correo, celular);
        
        if (logged.getCedula() != null && !logged.getCedula().trim().isEmpty()) {
            com.combinacion.dao.ContratistaDAO cDao = new com.combinacion.dao.ContratistaDAO();
            com.combinacion.models.Contratista c = cDao.obtenerPorCedula(logged.getCedula());
            if (c != null) {
                if (direccion != null) c.setDireccion(direccion);
                if (fechaNacimientoStr != null && !fechaNacimientoStr.isEmpty()) {
                    try { c.setFechaNacimiento(java.sql.Date.valueOf(fechaNacimientoStr)); } catch (Exception e) {}
                }
                if (edadStr != null && !edadStr.isEmpty()) {
                    try { c.setEdad(Integer.parseInt(edadStr)); } catch (Exception e) {}
                }
                cDao.actualizar(c);
            }
        }

        Map<String, Object> res = new HashMap<>();
        if (error == null) {
            // Actualizar objeto en sesión
            logged.setNombreCompleto(nombre);
            logged.setCorreo(correo);
            logged.setCelular(celular);
            session.setAttribute("nombreUsuario", nombre);
            res.put("success", true);
        } else {
            res.put("success", false);
            res.put("message", error);
        }
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(res));
    }

    public void cambiarMiPassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Usuario logged = (Usuario) session.getAttribute("usuario");
        if (logged == null) return;

        String oldPass = request.getParameter("oldPass");
        String newPass = request.getParameter("newPass");

        Map<String, Object> res = new HashMap<>();
        // Verificar pass actual
        if (!PasswordUtils.verifyPassword(oldPass, logged.getPasswordHash(), logged.getSalt())) {
            res.put("success", false);
            res.put("message", "La contraseña actual es incorrecta.");
        } else {
            String error = this.cambiarPassword(logged.getId(), newPass);
            if (error == null) {
                // Actualizar hash en sesión
                Usuario nuevo = this.obtenerPorId(logged.getId());
                session.setAttribute("usuario", nuevo);
                res.put("success", true);
            } else {
                res.put("success", false);
                res.put("message", error);
            }
        }
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(res));
    }

    public void eliminarFotoPerfil(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Usuario logged = (Usuario) session.getAttribute("usuario");
        Map<String, Object> res = new HashMap<>();
        if (logged != null) {
            if (this.actualizarFoto(logged.getId(), null)) {
                // Sincronizar objeto en sesión
                logged.setFotoUrl(null);
                session.setAttribute("usuario", logged);
                res.put("success", true);
            } else {
                res.put("success", false);
                res.put("message", "Error en BD al quitar foto");
            }
        } else {
            res.put("success", false);
            res.put("message", "Sesión no válida");
        }
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(res));
    }

    public void subirFotoPerfil(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Usuario logged = (Usuario) session.getAttribute("usuario");
        Map<String, Object> res = new HashMap<>();
        if (logged == null) return;

        try {
            Part filePart = request.getPart("foto");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = "profile_" + logged.getId() + "_" + System.currentTimeMillis() + ".jpg";
                String uploadPath = request.getServletContext().getRealPath("/") + "uploads" + File.separator + "profile";
                
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                filePart.write(uploadPath + File.separator + fileName);
                
                String fotoUrl = "uploads/profile/" + fileName;
                if (this.actualizarFoto(logged.getId(), fotoUrl)) {
                    logged.setFotoUrl(fotoUrl);
                    res.put("success", true);
                    res.put("url", fotoUrl);
                } else {
                    res.put("success", false);
                    res.put("message", "Error al guardar URL en BD");
                }
            }
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(res));
    }

    public void eliminarFirmaPerfil(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Usuario logged = (Usuario) session.getAttribute("usuario");
        Map<String, Object> res = new HashMap<>();
        if (logged != null) {
            // Se asume que UsuarioDAO tiene un método para actualizar la firma, por ejemplo, en base al id
            if (new com.combinacion.dao.UsuarioDAO().actualizarFirma(logged.getId(), null)) {
                logged.setFirmaUrl(null);
                session.setAttribute("usuario", logged);
                res.put("success", true);
            } else {
                res.put("success", false);
                res.put("message", "Error en BD al quitar firma");
            }
        } else {
            res.put("success", false);
        }
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(res));
    }

    public void subirFirmaPerfil(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Usuario logged = (Usuario) session.getAttribute("usuario");
        Map<String, Object> res = new HashMap<>();
        if (logged == null) return;

        try {
            Part filePart = request.getPart("firma");
            if (filePart != null && filePart.getSize() > 0) {
                String submittedFileName = filePart.getSubmittedFileName();
                if (submittedFileName == null || submittedFileName.trim().isEmpty()) {
                    submittedFileName = "firma_" + logged.getId() + ".jpg";
                }
                
                String mimeType = filePart.getContentType() != null ? filePart.getContentType() : "image/jpeg";
                
                String masterFolderId = com.combinacion.services.GoogleDriveService.getOrCreateFolder(com.combinacion.dao.ConfiguracionDAO.getValor("DRIVE_CARPETA_SISTEMA", "configuracion_SistemaContratacion"), null);
                String folderId = com.combinacion.services.GoogleDriveService.getOrCreateFolder(com.combinacion.dao.ConfiguracionDAO.getValor("DRIVE_CARPETA_FIRMAS", "FIRMAS_CONTRATISTAS"), masterFolderId);
                
                try (java.io.InputStream is = filePart.getInputStream()) {
                    String fileId = com.combinacion.services.GoogleDriveService.uploadStreamToDrive(is, filePart.getSize(), submittedFileName, mimeType, folderId);
                    
                    if (fileId != null && !fileId.isEmpty()) {
                        String firmaUrl = "ImageServlet?id=" + fileId;
                        if (new com.combinacion.dao.UsuarioDAO().actualizarFirma(logged.getId(), firmaUrl)) {
                            logged.setFirmaUrl(firmaUrl);
                            session.setAttribute("usuario", logged);
                            res.put("success", true);
                            res.put("url", firmaUrl);
                        } else {
                            res.put("success", false);
                            res.put("message", "Error al guardar URL de la firma en BD");
                        }
                    }
                }
            }
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(res));
    }

    public void mostrarPermisosUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Usuario u = this.obtenerPorId(id);
            if (u == null) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios");
                return;
            }
            request.setAttribute("usuario_perms", u);
            request.setAttribute("todosPermisos", rolService.listarPermisos());
            request.setAttribute("modulos", rolService.listarModulos());
            request.getRequestDispatcher("/admin/permisos_usuario.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios");
        }
    }

    public void guardarPermisosUsuario(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String[] permsIds = request.getParameterValues("permisos");
            java.util.List<Integer> ids = new java.util.ArrayList<>();
            if (permsIds != null) {
                for (String pid : permsIds) {
                    ids.add(Integer.parseInt(pid));
                }
            }
            this.actualizarPermisosEspeciales(id, ids);
            
            // Sincronizar sesión si se están actualizando los propios permisos
            HttpSession session = request.getSession(false);
            if (session != null) {
                Usuario logged = (Usuario) session.getAttribute("usuario");
                if (logged != null && logged.getId() == id) {
                    Usuario actualizado = this.obtenerPorId(id);
                    session.setAttribute("usuario", actualizado);
                }
            }
            
            try { com.combinacion.models.Usuario __u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario"); if(__u!=null) com.combinacion.dao.AuditoriaDAO.registrar(__u, "Actualización", "Registro actualizado en " + this.getClass().getSimpleName(), request.getRemoteAddr()); } catch(Exception ex){}
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=updated");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=error");
        }
    }

    public void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Usuario> usuarios = this.listarTodos();
        request.setAttribute("listUsuarios", usuarios);
        request.getRequestDispatcher("/admin/lista_usuarios.jsp").forward(request, response);
    }

    public void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                    Usuario usuario) throws ServletException, IOException {
        List<Rol> roles = rolService.listarTodos();
        request.setAttribute("listRoles", roles);
        if (usuario != null) {
            request.setAttribute("usuario_edit", usuario);
        }
        request.getRequestDispatcher("/admin/form_usuario.jsp").forward(request, response);
    }

    public void editarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Usuario usuario = this.obtenerPorId(id);
            if (usuario != null) {
                mostrarFormulario(request, response, usuario);
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=notfound");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios");
        }
    }

    public void insertarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String nombreCompleto = request.getParameter("nombre_completo");
        String correo = request.getParameter("correo");
        String cedula = request.getParameter("cedula");
        String celular = request.getParameter("celular");
        String sexo = request.getParameter("sexo");
        String vinculacion = request.getParameter("vinculacion");
        java.sql.Date fechaInicio = parseDate(request.getParameter("fecha_inicio"));
        java.sql.Date fechaFin = parseDate(request.getParameter("fecha_fin"));
        int rolId = 0;
        try { rolId = Integer.parseInt(request.getParameter("rol_id")); } catch (Exception ignored) {}

        String error = this.crear(username, password, nombreCompleto, correo, 
                                          cedula, celular, sexo, vinculacion, fechaInicio, fechaFin, rolId);
        if (error != null) {
            request.setAttribute("error", error);
            Usuario u = new Usuario();
            u.setUsername(username);
            u.setNombreCompleto(nombreCompleto);
            u.setCorreo(correo);
            u.setCedula(cedula);
            u.setCelular(celular);
            u.setSexo(sexo);
            u.setVinculacion(vinculacion);
            u.setFechaInicioContrato(fechaInicio);
            u.setFechaFinContrato(fechaFin);
            u.setRolId(rolId);
            mostrarFormulario(request, response, u);
        } else {
            try { com.combinacion.models.Usuario __u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario"); if(__u!=null) com.combinacion.dao.AuditoriaDAO.registrar(__u, "Creación", "Registro creado en " + this.getClass().getSimpleName(), request.getRemoteAddr()); } catch(Exception ex){}
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=created");
        }
    }

    public void actualizarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String username = request.getParameter("username");
            String nombreCompleto = request.getParameter("nombre_completo");
            String correo = request.getParameter("correo");
            String cedula = request.getParameter("cedula");
            String celular = request.getParameter("celular");
            String sexo = request.getParameter("sexo");
            String vinculacion = request.getParameter("vinculacion");
            java.sql.Date fechaInicio = parseDate(request.getParameter("fecha_inicio"));
            java.sql.Date fechaFin = parseDate(request.getParameter("fecha_fin"));
            boolean activo = "on".equals(request.getParameter("activo")) || "true".equals(request.getParameter("activo"));
            int rolId = Integer.parseInt(request.getParameter("rol_id"));

            String error = this.actualizar(id, username, nombreCompleto, correo, 
                                                   cedula, celular, sexo, vinculacion, fechaInicio, fechaFin,
                                                   activo, rolId);
            if (error != null) {
                request.setAttribute("error", error);
                Usuario u = new Usuario();
                u.setId(id);
                u.setUsername(username);
                u.setNombreCompleto(nombreCompleto);
                u.setCorreo(correo);
                u.setCedula(cedula);
                u.setCelular(celular);
                u.setSexo(sexo);
                u.setVinculacion(vinculacion);
                u.setFechaInicioContrato(fechaInicio);
                u.setFechaFinContrato(fechaFin);
                u.setActivo(activo);
                u.setRolId(rolId);
                mostrarFormulario(request, response, u);
            } else {
                try { com.combinacion.models.Usuario __u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario"); if(__u!=null) com.combinacion.dao.AuditoriaDAO.registrar(__u, "Actualización", "Registro actualizado en " + this.getClass().getSimpleName(), request.getRemoteAddr()); } catch(Exception ex){}
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=updated");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=error");
        }
    }

    public void cambiarPassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String nuevaPassword = request.getParameter("nueva_password");

            String error = this.cambiarPassword(id, nuevaPassword);
            if (error != null) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios?action=edit&id=" + id + "&error=" + java.net.URLEncoder.encode(error, "UTF-8"));
            } else {
                try { com.combinacion.models.Usuario __u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario"); if(__u!=null) com.combinacion.dao.AuditoriaDAO.registrar(__u, "Cambio de Clave", "Contraseña cambiada en " + this.getClass().getSimpleName(), request.getRemoteAddr()); } catch(Exception ex){}
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=password_changed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=error");
        }
    }

    public void eliminarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            this.eliminar(id);
            try { com.combinacion.models.Usuario __u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario"); if(__u!=null) com.combinacion.dao.AuditoriaDAO.registrar(__u, "Eliminación", "Registro eliminado en " + this.getClass().getSimpleName(), request.getRemoteAddr()); } catch(Exception ex){}
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=deleted");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios?status=error");
        }
    }

    public void checkCedula(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String cedula = request.getParameter("cedula");
        int excludeId = 0;
        try { excludeId = Integer.parseInt(request.getParameter("id")); } catch (Exception ignored) {}
        
        boolean existe = this.existeCedula(cedula, excludeId);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"exists\": " + existe + "}");
    }

    public void checkUsername(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username");
        int excludeId = 0;
        try { excludeId = Integer.parseInt(request.getParameter("id")); } catch (Exception ignored) {}
        
        boolean existe = this.existeUsername(username, excludeId);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"exists\": " + existe + "}");
    }

    public void responderDatosTabla(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String draw = request.getParameter("draw");
        int start = parseIntSafe(request.getParameter("start"), 0);
        int length = parseIntSafe(request.getParameter("length"), 10);
        String search = request.getParameter("search[value]");
        String sortCol = request.getParameter("order[0][column]");
        String orderDir = request.getParameter("order[0][dir]");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
            this.generarJsonDataTables(
                parseIntSafe(draw, 1), start, length, search, sortCol, orderDir)
        );
    }

    private int parseIntSafe(String val, int defaultVal) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private java.sql.Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return java.sql.Date.valueOf(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

}
