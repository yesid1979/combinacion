package com.combinacion.models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo que representa un rol del sistema con sus permisos asociados.
 */
public class Rol implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private String descripcion;
    private boolean activo;
    private Timestamp fechaCreacion;
    private List<Permiso> permisos;

    public Rol() {
        this.permisos = new ArrayList<>();
        this.activo = true;
    }

    public Rol(int id, String nombre, String descripcion) {
        this();
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public List<Permiso> getPermisos() { return permisos; }
    public void setPermisos(List<Permiso> permisos) { this.permisos = permisos; }

    /**
     * Verifica si este rol tiene un permiso específico por su código.
     */
    public boolean tienePermiso(String codigoPermiso) {
        if (permisos == null) return false;
        for (Permiso p : permisos) {
            if (p.getCodigo().equals(codigoPermiso)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Rol{" + "id=" + id + ", nombre=" + nombre + '}';
    }
}
