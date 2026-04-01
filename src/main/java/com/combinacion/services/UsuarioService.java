package com.combinacion.services;

import com.combinacion.dao.UsuarioDAO;
import com.combinacion.models.Usuario;
import com.combinacion.util.PasswordUtils;
import java.util.List;

/**
 * Servicio de lógica de negocio para Usuarios.
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
     * Crea un nuevo usuario con la contraseña hasheada.
     * @return null si fue exitoso, o un mensaje de error.
     */
    public String crear(String username, String password, String nombreCompleto,
                        String correo, int rolId) {
        // Validaciones
        if (username == null || username.trim().isEmpty()) {
            return "El nombre de usuario es obligatorio.";
        }
        if (password == null || password.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return "El nombre completo es obligatorio.";
        }
        if (rolId <= 0) {
            return "Debe seleccionar un rol.";
        }

        // Verificar username duplicado
        if (usuarioDAO.existeUsername(username.trim(), 0)) {
            return "El nombre de usuario '" + username + "' ya está en uso.";
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
                             String correo, boolean activo, int rolId) {
        if (username == null || username.trim().isEmpty()) {
            return "El nombre de usuario es obligatorio.";
        }
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return "El nombre completo es obligatorio.";
        }
        if (rolId <= 0) {
            return "Debe seleccionar un rol.";
        }

        // Verificar username duplicado excluyendo el usuario actual
        if (usuarioDAO.existeUsername(username.trim(), id)) {
            return "El nombre de usuario '" + username + "' ya está en uso.";
        }

        Usuario usuario = usuarioDAO.obtenerPorId(id);
        if (usuario == null) {
            return "Usuario no encontrado.";
        }

        usuario.setUsername(username.trim());
        usuario.setNombreCompleto(nombreCompleto.trim());
        usuario.setCorreo(correo != null ? correo.trim() : null);
        usuario.setActivo(activo);
        usuario.setRolId(rolId);

        if (!usuarioDAO.actualizar(usuario)) {
            return "Error al actualizar el usuario.";
        }
        return null; // Éxito
    }

    /**
     * Cambia la contraseña de un usuario.
     * @return null si fue exitoso, o mensaje de error.
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
}
