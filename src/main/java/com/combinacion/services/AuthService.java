package com.combinacion.services;

import com.combinacion.models.Usuario;
import com.combinacion.dao.UsuarioDAO;
import com.combinacion.util.PasswordUtils;

/**
 * Servicio para la gestion de autenticacion y seguridad.
 */
public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Autentica un usuario por username y password (metodo simple compatible).
     */
    public Usuario autenticar(String username, String password) {
        LoginResult result = autenticarDetallado(username, password);
        if (result != null) {
            return result.getUsuario();
        }
        return null;
    }

    /**
     * Version detallada para distinguir entre credenciales e inactividad.
     */
    public LoginResult autenticarDetallado(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null) {
            return new LoginResult(null, "Por favor, complete todos los campos.", false);
        }

        Usuario usuario = usuarioDAO.obtenerPorUsername(username.trim());

        if (usuario == null) {
            return new LoginResult(null, "Usuario o contraseña incorrectos.", false);
        }

        // Comprobacion de estado inactivo
        if (!usuario.isActivo()) {
            return new LoginResult(null, "El usuario se encuentra inactivo por favor consulte al administrador.", false);
        }

        // --- AUTOMATIZACION: Inactivacion por fin de contrato ---
        if ("Contratista".equalsIgnoreCase(usuario.getVinculacion()) && usuario.getFechaFinContrato() != null) {
            java.util.Date hoy = new java.util.Date();
            // Si la fecha de fin es antes que hoy (ya vencio)
            if (usuario.getFechaFinContrato().before(hoy)) {
                usuario.setActivo(false);
                usuarioDAO.actualizar(usuario); // Guardar en DB el cambio automatico
                return new LoginResult(null, "El usuario se encuentra inactivo por favor consulte al administrador.", false);
            }
        }
        // ---------------------------------------------------------

        // Verificar contraseña hasheada
        if (!PasswordUtils.verifyPassword(password, usuario.getPasswordHash(), usuario.getSalt())) {
            return new LoginResult(null, "Usuario o contraseña incorrectos.", false);
        }

        // Registro de acceso exitoso
        usuarioDAO.actualizarUltimoAcceso(usuario.getId());
        return new LoginResult(usuario, null, true);
    }

    /**
     * Determina si un usuario tiene permiso para acceder a una ruta especifica.
     */
    public boolean puedeAcceder(Usuario usuario, String path) {
        if (usuario == null) return false;
        
        // Bloqueo inmediato para usuarios inactivados
        if (!usuario.isActivo()) return false;

        // El Administrador total (Rol ID: 1) tiene acceso a TODO
        if (usuario.getRolId() == 1) return true;

        // --- AUTOMATIZACION: Inactivacion en tiempo real por fin de contrato ---
        if ("Contratista".equalsIgnoreCase(usuario.getVinculacion()) && usuario.getFechaFinContrato() != null) {
            java.util.Date hoy = new java.util.Date();
            if (usuario.getFechaFinContrato().before(hoy)) {
                usuario.setActivo(false);
                usuarioDAO.actualizar(usuario); // Persistir en DB
                return false; // Denegar acceso de inmediato
            }
        }
        // -----------------------------------------------------------------------

        // Rutas publicas y basicas (Index y Logout siempre permitidos si esta activo)
        if (path.equals("/index.jsp") || path.equals("/") || path.equals("/logout")) {
            return true;
        }
        
        // --- VALIDACION DE MODULOS POR PERMISO DE 'VER' ---
        String lowerPath = path.toLowerCase();

        if (lowerPath.contains("/admin/") || lowerPath.contains("/admin/usuarios") || lowerPath.contains("/roles")) {
            return tienePermiso(usuario, "ADMINISTRACION_VER");
        }
        if (lowerPath.contains("carga_masiva") || lowerPath.contains("cargamasiva") || lowerPath.contains("cargamos")) {
            return tienePermiso(usuario, "CARGA_MASIVA_VER");
        }
        if (lowerPath.contains("/contratos")) {
            return tienePermiso(usuario, "CONTRATOS_VER");
        }
        if (lowerPath.contains("/contratistas")) {
            return tienePermiso(usuario, "CONTRATISTAS_VER");
        }
        if (lowerPath.contains("/presupuesto")) {
            return tienePermiso(usuario, "PRESUPUESTO_VER");
        }
        if (lowerPath.contains("/ordenador")) {
            return tienePermiso(usuario, "ORDENADORES_VER");
        }
        if (lowerPath.contains("/supervisor")) {
            return tienePermiso(usuario, "SUPERVISORES_VER");
        }
        if (lowerPath.contains("/combinacion")) {
             return tienePermiso(usuario, "COMBINACION_VER");
        }

        return false; 
    }

    /**
     * Determina si un usuario tiene permiso para realizar una accion especifica.
     */
    public boolean tienePermiso(Usuario usuario, String codigoPermiso) {
        if (usuario == null) return false;
        
        // Bloqueo de permisos para cuentas inactivas
        if (!usuario.isActivo()) return false;
        
        // El Administrador total (Rol ID: 1) tiene todos los poderes
        if (usuario.getRolId() == 1) return true;
        
        // Consultar la logica de permisos (especiales vs rol) definida en el modelo
        return usuario.tienePermiso(codigoPermiso);
    }
}
