package com.combinacion.util;
import com.combinacion.dao.PermisoDAO;
import com.combinacion.models.Permiso;
import java.util.List;
public class CheckPerms {
    public static void main(String[] args) {
        List<Permiso> perms = new PermisoDAO().listarPorUsuarioId(9);
        for(Permiso p : perms) {
            System.out.println(p.getId() + " - " + p.getCodigo() + " - " + p.getNombre() + " - " + p.getModulo());
        }
    }
}
