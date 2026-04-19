package com.combinacion.services;

import com.combinacion.models.Usuario;
import com.combinacion.dao.UsuarioDAO;
import com.combinacion.util.PasswordUtils;

public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // ==========================================
    // SECCIÓN 1: AUTENTICACIÓN (LOGIN)
    // ==========================================
    public Usuario autenticar(String username, String password) {
        LoginResult result = autenticarDetallado(username, password);
        return (result != null) ? result.getUsuario() : null;
    }

    public LoginResult autenticarDetallado(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null) {
            return new LoginResult(null, "Complete todos los campos.", false);
        }
        Usuario usuario = usuarioDAO.obtenerPorUsername(username.trim());
        if (usuario == null) {
            return new LoginResult(null, "Usuario o contraseña incorrectos.", false);
        }
        if (!usuario.isActivo()) {
            return new LoginResult(null, "El usuario está inactivo.", false);
        }
        if (!PasswordUtils.verifyPassword(password, usuario.getPasswordHash(), usuario.getSalt())) {
            return new LoginResult(null, "Usuario o contraseña incorrectos.", false);
        }
        usuarioDAO.actualizarUltimoAcceso(usuario.getId());
        return new LoginResult(usuario, null, true);
    }

    // ==========================================
    // SECCIÓN 2: AUTORIZACIÓN (PERMISOS)
    // ==========================================
    public boolean puedeAccederExtendido(Usuario usuario, String path, String action, String method) {
        if (usuario == null) {
            System.err.println("[AUTH] Usuario no autenticado.");
            return false;
        }

        // --- 1. LLAVE MAESTRA: Administrador (Sincronizado con Usuario.java) ---
        if (usuario.esAdministrador()) {
            return true;
        }

        if (!usuario.isActivo()) {
            System.out.println("[AUTH] Usuario inactivo. Acceso denegado.");
            return false;
        }

        if (path == null) {
            path = "/";
        }
        String lowerPath = path.toLowerCase();

        // Normalizar acción y método
        if (action == null || action.isEmpty()) {
            action = "list";
        }
        action = action.toLowerCase();
        if (method == null) {
            method = "GET";
        }

        // Resolver permiso necesario
        String permiso = resolverPermiso(lowerPath, action);
        System.out.println("[AUTH] Permiso resuelto para " + lowerPath + " (" + action + "): " + permiso);

        // Si no hay permiso definido, permitir acceso a rutas base
        if (permiso == null) {
            boolean esRutaBase = lowerPath.equals("/index.jsp") || lowerPath.equals("/") || lowerPath.equals("/logout") || lowerPath.contains("servlet");
            System.out.println("[AUTH] Permiso no definido. Acceso a ruta base: " + esRutaBase);
            return esRutaBase;
        }

        // Validación de método para acciones críticas
        if (action.equals("delete") && !"POST".equalsIgnoreCase(method)) {
            System.out.println("[AUTH] Método no permitido para acción 'delete'. Debe ser POST.");
            return false;
        }

        // 3. Verificar permiso en base de datos (Lógica robusta en Usuario.java)
        boolean tienePermiso = usuario.tienePermiso(permiso);
        
        if (!tienePermiso) {
            System.err.println("[AUTH] ACCESO DENEGADO: Usuario '" + usuario.getUsername() + 
                "' no tiene permiso '" + permiso + "' para " + lowerPath + " (Accion: " + action + ")");
        }

        return tienePermiso;
    }

    private String resolverPermiso(String path, String action) {
        String modulo = obtenerModulo(path);
        if (modulo == null) {
            return null;
        }

        // Casos especiales
        if (modulo.equals("CARGA_MASIVA")) {
            return "CARGA_MASIVA_EJECUTAR";
        }

        // Mapear acción a permiso
        switch (action) {
            case "list":
            case "view":
            case "data":
            case "search":
                return modulo + "_VER";
            case "new":
            case "insert":
            case "create":
                return modulo + "_CREAR";
            case "edit":
            case "update":
            case "save":
                return modulo + "_EDITAR";
            case "delete":
                return modulo + "_ELIMINAR";
            default:
                return null;
        }
    }

    private String obtenerModulo(String path) {
        if (path.contains("contratistas")) return "CONTRATISTAS";
        if (path.contains("contratos")) return "CONTRATOS";
        if (path.contains("usuarios")) return "ADMIN";
        if (path.contains("roles")) return "ADMIN";
        if (path.contains("presupuesto")) return "PRESUPUESTO";
        if (path.contains("supervisor")) return "SUPERVISORES";
        if (path.contains("ordenador")) return "ORDENADORES";
        if (path.contains("combinacion")) return "COMBINACION";
        if (path.contains("carga_masiva")) return "CARGA_MASIVA";
        return null;
    }

    public boolean tienePermiso(Usuario usuario, String codigoPermiso) {
        return usuario != null && usuario.tienePermiso(codigoPermiso);
    }
}