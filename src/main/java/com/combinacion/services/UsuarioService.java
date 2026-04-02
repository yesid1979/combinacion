package com.combinacion.services;

import com.combinacion.dao.UsuarioDAO;
import com.combinacion.models.Usuario;
import com.combinacion.util.PasswordUtils;
import java.util.List;

/**
 * Servicio para la lógica de negocio de Usuarios.
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

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
}
