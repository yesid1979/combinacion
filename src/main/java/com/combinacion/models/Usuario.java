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
    private String cedula;
    private String celular;
    private String sexo;
    private String vinculacion;
    private java.sql.Date fechaInicioContrato;
    private java.sql.Date fechaFinContrato;
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

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getVinculacion() { return vinculacion; }
    public void setVinculacion(String vinculacion) { this.vinculacion = vinculacion; }

    public java.sql.Date getFechaInicioContrato() { return fechaInicioContrato; }
    public void setFechaInicioContrato(java.sql.Date fechaInicioContrato) { this.fechaInicioContrato = fechaInicioContrato; }

    public java.sql.Date getFechaFinContrato() { return fechaFinContrato; }
    public void setFechaFinContrato(java.sql.Date fechaFinContrato) { this.fechaFinContrato = fechaFinContrato; }

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
     * Comprueba si el usuario tiene un permiso específico.
     * Lógica de Jerarquía Inteligente:
     * 1. Si el usuario tiene el permiso en su lista de permisos especiales, se concede.
     * 2. Si no está en su lista especial, se busca en los permisos heredados de su Rol.
     */
    /**
     * Comprueba si el usuario tiene un permiso específico.
     * Lógica de Respeto Absoluto a los Interruptores (FOTO-CENTRISTA):
     * 1. El Administrador (rolId == 1) siempre tiene permiso absoluto (Imagen 3).
     * 2. Si el usuario tiene permisos especiales (Manual), NO se usa la herencia del Rol. 
     *    Solo se concede lo que esté marcado con "Si" (presente en la lista).
     * 3. Si el usuario NO tiene permisos manuales, se usa el permiso por defecto de su Rol.
     */
    public boolean tienePermiso(String codigoPermiso) {
        // --- 1. LLAVE MAESTRA: Administrador ---
        if (this.rolId == 1) return true;

        // --- 2. MODO MANUAL: Si tiene excepciones, la lista es la UNICA fuente de verdad ---
        if (permisosEspeciales != null && !permisosEspeciales.isEmpty()) {
            String target = codigoPermiso.toUpperCase().replace("_ACTUALIZAR", "_EDITAR");
            
            // Búsqueda directa
            for (Permiso p : permisosEspeciales) {
                String cod = p.getCodigo();
                if (cod == null) continue;
                String normalized = cod.toUpperCase().replace("_ACTUALIZAR", "_EDITAR");
                if (normalized.equals(target)) return true;
                
                // --- MEJORA: Búsqueda por Prefijo de Módulo (Fuzzy Match) ---
                // Si buscamos un 'VER' y el permiso especial coincide con el módulo base
                if (target.endsWith("_VER")) {
                    String moduloBase = target.substring(0, target.lastIndexOf("_"));
                    if (normalized.startsWith(moduloBase)) return true;
                }
                // Caso especial para CARGA_MASIVA
                if (target.contains("CARGA") && normalized.contains("CARGA")) return true;
            }
            
            // Si tiene otros permisos especiales pero ESTE no se encontró ni por asomo, se deniega
            return false;
        }

        // --- 3. MODO DEFAULT: Sin excepciones, manda el Rol ---
        if (rol != null) {
            boolean tieneRol = rol.tienePermiso(codigoPermiso);
            if (!tieneRol && codigoPermiso.contains("_ACTUALIZAR")) {
                tieneRol = rol.tienePermiso(codigoPermiso.replace("_ACTUALIZAR", "_EDITAR"));
            }
            return tieneRol;
        }

        return false;
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
