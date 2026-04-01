package com.combinacion.services;

import com.combinacion.dao.PermisoDAO;
import com.combinacion.dao.RolDAO;
import com.combinacion.models.Permiso;
import com.combinacion.models.Rol;
import java.util.List;

/**
 * Servicio de lógica de negocio para Roles y Permisos.
 */
public class RolService {

    private final RolDAO rolDAO = new RolDAO();
    private final PermisoDAO permisoDAO = new PermisoDAO();

    /**
     * Lista todos los roles.
     */
    public List<Rol> listarTodos() {
        return rolDAO.listarTodos();
    }

    /**
     * Obtiene un rol por ID con permisos cargados.
     */
    public Rol obtenerPorId(int id) {
        return rolDAO.obtenerPorId(id);
    }

    /**
     * Crea un nuevo rol.
     * @return null si fue exitoso, o mensaje de error.
     */
    public String crear(String nombre, String descripcion, List<Integer> permisosIds) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre del rol es obligatorio.";
        }

        // Verificar si ya existe
        Rol existente = rolDAO.obtenerPorNombre(nombre.trim());
        if (existente != null) {
            return "Ya existe un rol con el nombre '" + nombre + "'.";
        }

        Rol rol = new Rol();
        rol.setNombre(nombre.trim());
        rol.setDescripcion(descripcion != null ? descripcion.trim() : "");
        rol.setActivo(true);

        int id = rolDAO.insertar(rol);
        if (id <= 0) {
            return "Error al crear el rol en la base de datos.";
        }

        // Asignar permisos
        if (permisosIds != null && !permisosIds.isEmpty()) {
            rolDAO.asignarPermisos(id, permisosIds);
        }

        return null; // Éxito
    }

    /**
     * Actualiza un rol existente.
     * @return null si fue exitoso, o mensaje de error.
     */
    public String actualizar(int id, String nombre, String descripcion, 
                             boolean activo, List<Integer> permisosIds) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre del rol es obligatorio.";
        }

        Rol rol = rolDAO.obtenerPorId(id);
        if (rol == null) {
            return "Rol no encontrado.";
        }

        rol.setNombre(nombre.trim());
        rol.setDescripcion(descripcion != null ? descripcion.trim() : "");
        rol.setActivo(activo);

        if (!rolDAO.actualizar(rol)) {
            return "Error al actualizar el rol.";
        }

        // Reasignar permisos
        rolDAO.asignarPermisos(id, permisosIds != null ? permisosIds : new java.util.ArrayList<>());

        return null; // Éxito
    }

    /**
     * Elimina un rol. No permite eliminar si tiene usuarios asociados.
     * @return null si fue exitoso, o mensaje de error.
     */
    public String eliminar(int id) {
        int usuarios = rolDAO.contarUsuariosPorRol(id);
        if (usuarios > 0) {
            return "No se puede eliminar el rol porque tiene " + usuarios + " usuario(s) asociado(s).";
        }
        if (!rolDAO.eliminar(id)) {
            return "Error al eliminar el rol.";
        }
        return null;
    }

    /**
     * Lista todos los permisos disponibles.
     */
    public List<Permiso> listarPermisos() {
        return permisoDAO.listarTodos();
    }

    /**
     * Lista los módulos disponibles.
     */
    public List<String> listarModulos() {
        return permisoDAO.listarModulos();
    }
}
