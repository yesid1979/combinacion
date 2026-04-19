package com.combinacion.services;

import com.combinacion.dao.SupervisorDAO;
import com.combinacion.models.Supervisor;
import java.util.List;

/**
 * Capa de servicio para la entidad Supervisor.
 * Contiene toda la lógica de negocio extraída del SupervisorServlet.
 */
public class SupervisorService {

    private final SupervisorDAO supervisorDAO = new SupervisorDAO();

    /**
     * Inserta un nuevo supervisor.
     * @return true si fue exitoso, false si falló.
     */
    public boolean insertar(Supervisor s) {
        return supervisorDAO.insertar(s);
    }

    /**
     * Actualiza un supervisor existente.
     * @return true si fue exitoso, false si falló.
     */
    public boolean actualizar(Supervisor s) {
        return supervisorDAO.actualizar(s);
    }

    /**
     * Elimina un supervisor por su ID.
     * @return true si fue exitoso, false si falló.
     */
    public boolean eliminar(int id) {
        return supervisorDAO.eliminar(id);
    }

    /**
     * Obtiene un supervisor por su ID.
     */
    public Supervisor obtenerPorId(int id) {
        return supervisorDAO.obtenerPorId(id);
    }

    /**
     * Lista todos los supervisores.
     */
    public List<Supervisor> listarTodos() {
        return supervisorDAO.listarTodos();
    }

    /**
     * Conteo total de supervisores.
     */
    public int countAll() {
        return supervisorDAO.countAll();
    }

    /**
     * Conteo filtrado de supervisores.
     */
    public int countFiltered(String search) {
        return supervisorDAO.countFiltered(search);
    }

    /**
     * Paginación de supervisores.
     */
    public List<Supervisor> findWithPagination(int start, int length, String search, int orderCol, String orderDir) {
        return supervisorDAO.findWithPagination(start, length, search, orderCol, orderDir);
    }

    /**
     * Construye un objeto Supervisor a partir de parámetros de formulario.
     */
    public Supervisor construirDesdeParametros(String nombre, String cedula, String cargo) {
        Supervisor s = new Supervisor();
        s.setNombre(nombre);
        s.setCedula(cedula);
        s.setCargo(cargo);
        return s;
    }
}
