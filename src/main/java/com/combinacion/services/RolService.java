package com.combinacion.services;

import com.combinacion.dao.PermisoDAO;
import com.combinacion.dao.RolDAO;
import com.combinacion.models.Permiso;
import com.combinacion.models.Rol;
import java.util.List;
import java.util.ArrayList;

public class RolService {
    private final RolDAO rolDAO = new RolDAO();
    private final PermisoDAO permisoDAO = new PermisoDAO();

    public List<Rol> listarTodos() {
        return rolDAO.listarTodos();
    }

    public Rol obtenerPorId(int id) {
        return rolDAO.obtenerPorId(id);
    }

    public String crear(String nombre, String descripcion, List<Integer> permisosIds) {
        if (nombre == null || nombre.trim().isEmpty()) return "Nombre obligatorio.";
        Rol rol = new Rol();
        rol.setNombre(nombre.trim());
        rol.setDescripcion(descripcion != null ? descripcion.trim() : "");
        rol.setActivo(true);
        int id = rolDAO.insertar(rol);
        if (id <= 0) return "Error db.";
        rolDAO.asignarPermisos(id, permisosIds);
        return null;
    }

    public String actualizar(int id, String nombre, String descripcion, boolean activo, List<Integer> permisosIds) {
        Rol rol = rolDAO.obtenerPorId(id);
        if (rol == null) return "No encontrado.";
        rol.setNombre(nombre.trim());
        rol.setDescripcion(descripcion != null ? descripcion.trim() : "");
        rol.setActivo(activo);
        if (!rolDAO.actualizar(rol)) return "Error db.";
        rolDAO.asignarPermisos(id, permisosIds != null ? permisosIds : new ArrayList<Integer>());
        return null;
    }

    public String eliminar(int id) {
        int usuarios = rolDAO.contarUsuariosPorRol(id);
        if (usuarios > 0) return "Tiene usuarios asociados.";
        if (!rolDAO.eliminar(id)) return "Error db.";
        return null;
    }

    public List<Permiso> listarPermisos() {
        return permisoDAO.listarTodos();
    }

    public List<String> listarModulos() {
        return permisoDAO.listarModulos();
    }

    public String generarJsonDataTables(int draw, int start, int length, String search, String sortCol, String sortDir) {
        int total = rolDAO.countAll();
        int filtered = rolDAO.countFiltered(search);
        List<Rol> list = rolDAO.findWithPagination(start, length, search, sortCol, sortDir);
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"draw\":").append(draw).append(",");
        sb.append("\"recordsTotal\":").append(total).append(",");
        sb.append("\"recordsFiltered\":").append(filtered).append(",");
        sb.append("\"data\":[");
        for (int i = 0; i < list.size(); i++) {
            Rol r = list.get(i);
            sb.append("[").append(r.getId()).append(",");
            sb.append("\"").append(r.getNombre()).append("\",");
            sb.append("\"").append(r.getDescripcion()).append("\",");
            sb.append(r.getId()).append("]");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }
}
