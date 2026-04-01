package com.combinacion.models;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Modelo que representa un usuario del sistema.
 */
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String passwordHash;
    private String salt;
    private String nombreCompleto;
    private String correo;
    private boolean activo;
    private Timestamp ultimoAcceso;
    private Timestamp fechaCreacion;
    private java.util.List<Permiso> permisosEspeciales = new java.util.ArrayList<>();
    private int rolId;
    private Rol rol;

    public Usuario() {
        this.activo = true;
    }

    // Getters y Setters
    public java.util.List<Permiso> getPermisosEspeciales() { return permisosEspeciales; }
    public void setPermisosEspeciales(java.util.List<Permiso> permisosEspeciales) { this.permisosEspeciales = permisosEspeciales; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Timestamp getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(Timestamp ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public int getRolId() { return rolId; }
    public void setRolId(int rolId) { this.rolId = rolId; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    /**
     * Verifica si el usuario tiene un permiso específico, considerando 
     * permisos especiales (sobrescritura) o los del rol por defecto.
     */
    public boolean tienePermiso(String codigoPermiso) {
        // 1. Si el usuario tiene el permiso asignado de forma especial
        for (Permiso p : permisosEspeciales) {
            if (p.getCodigo().equals(codigoPermiso)) {
                return true;
            }
        }

        // 2. Lógica de "Sobre-escritura" por módulo:
        // Si existen permisos especiales para el módulo del permiso solicitado,
        // pero el permiso solicitado NO está en ellos, entonces está denegado explícitamente.
        
        // Pero primero necesitamos identificar a qué módulo pertenece el permiso.
        // Si no tenemos esa info a la mano, verificamos si el rol lo tiene.
        if (rol == null) return false;
        
        // Buscamos el permiso en el rol para saber su módulo
        String moduloDelPermiso = null;
        if (rol.getPermisos() != null) {
            for (Permiso rp : rol.getPermisos()) {
                if (rp.getCodigo().equals(codigoPermiso)) {
                    moduloDelPermiso = rp.getModulo();
                    break;
                }
            }
        }
        
        // Si no encontramos el módulo del permiso, devolvemos lo que diga el rol directamente
        if (moduloDelPermiso == null) {
            return rol.tienePermiso(codigoPermiso);
        }

        // ¿Tiene el usuario ALGÚN permiso especial para ESE módulo?
        boolean moduloSobreescrito = false;
        for (Permiso p : permisosEspeciales) {
            if (moduloDelPermiso.equals(p.getModulo())) {
                moduloSobreescrito = true;
                break;
            }
        }

        // Si el módulo está sobreescrito pero no lo encontramos en el paso 1, es FALSE.
        if (moduloSobreescrito) {
            return false;
        }

        // Si el módulo NO está sobreescrito, usamos el del rol por defecto.
        return rol.tienePermiso(codigoPermiso);
    }

    /**
     * Verifica si el usuario tiene el rol Administrador.
     */
    public boolean esAdministrador() {
        return rol != null && "Administrador".equals(rol.getNombre());
    }

    @Override
    public String toString() {
        return "Usuario{" + "id=" + id + ", username=" + username + ", nombre=" + nombreCompleto + '}';
    }
}
