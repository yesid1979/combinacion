package com.combinacion.services;

import com.combinacion.dao.UsuarioDAO;
import com.combinacion.models.Usuario;
import com.combinacion.util.PasswordUtils;

/**
 * Servicio de autenticación: login y verificación de permisos.
 */
public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Intenta autenticar un usuario con username y password.
     * @return El Usuario autenticado (con rol y permisos cargados) o null si falla.
     */
    public Usuario autenticar(String username, String password) {
        if (username == null || password == null 
                || username.trim().isEmpty() || password.trim().isEmpty()) {
            return null;
        }

        Usuario usuario = usuarioDAO.obtenerPorUsername(username.trim());
        if (usuario == null) {
            return null;
        }

        // Comprobación de activo (temporalmente permitimos acceso para diagnóstico)
        /* if (!usuario.isActivo()) {
            return null;
        } */

        // Verificar contraseña
        if (!PasswordUtils.verifyPassword(password, usuario.getPasswordHash(), usuario.getSalt())) {
            return null;
        }

        // Actualizar último acceso
        usuarioDAO.actualizarUltimoAcceso(usuario.getId());

        return usuario;
    }

    /**
     * Verifica si un usuario tiene un permiso específico.
     */
    public boolean tienePermiso(Usuario usuario, String codigoPermiso) {
        if (usuario == null) return false;
        // Administrador tiene acceso total
        if (usuario.esAdministrador()) return true;
        return usuario.tienePermiso(codigoPermiso);
    }

    /**
     * Verifica si un usuario puede acceder a una URL determinada.
     * Mapea las URLs a los permisos requeridos.
     */
    public boolean puedeAcceder(Usuario usuario, String uri) {
        if (usuario == null) return false;
        if (usuario.esAdministrador()) return true;

        // Mapeo de URLs a permisos
        if (uri.contains("/admin/")) {
            return tienePermiso(usuario, "ADMIN_USUARIOS") || tienePermiso(usuario, "ADMIN_ROLES");
        }
        if (uri.contains("/contratos")) {
            return tienePermiso(usuario, "CONTRATOS_VER");
        }
        if (uri.contains("/contratistas")) {
            return tienePermiso(usuario, "CONTRATISTAS_VER");
        }
        if (uri.contains("/supervisores")) {
            return tienePermiso(usuario, "SUPERVISORES_VER");
        }
        if (uri.contains("/ordenadores")) {
            return tienePermiso(usuario, "ORDENADORES_VER");
        }
        if (uri.contains("/presupuesto")) {
            return tienePermiso(usuario, "PRESUPUESTO_VER");
        }
        if (uri.contains("/combinacion")) {
            return tienePermiso(usuario, "COMBINACION_VER");
        }
        if (uri.contains("/carga_masiva") || uri.contains("/cargamasiva")) {
            return tienePermiso(usuario, "CARGA_MASIVA_EJECUTAR");
        }

        // Páginas genéricas (index, resources) → acceso libre si está autenticado
        return true;
    }
}
