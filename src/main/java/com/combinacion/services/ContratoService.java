package com.combinacion.services;

import com.combinacion.dao.*;
import com.combinacion.models.*;
import com.combinacion.util.ParseUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Capa de servicio para la entidad Contrato y sus relaciones.
 * Contiene toda la lógica de negocio extraída del ContratoServlet.
 */
public class ContratoService {

    private final ContratoDAO          contratoDAO      = new ContratoDAO();
    private final ContratistaDAO       contratistaDAO   = new ContratistaDAO();
    private final SupervisorDAO        supervisorDAO    = new SupervisorDAO();
    private final OrdenadorGastoDAO    ordenadorDAO     = new OrdenadorGastoDAO();
    private final PresupuestoDetalleDAO presupuestoDAO  = new PresupuestoDetalleDAO();
    private final EstructuradorDAO     estructuradorDAO = new EstructuradorDAO();

    // -------------------------------------------------------------------------
    // CONSULTAS
    // -------------------------------------------------------------------------

    public List<Contrato> listarTodos() {
        return contratoDAO.listarTodos();
    }

    public List<Supervisor>     listarSupervisores() { return supervisorDAO.listarTodos();  }
    public List<OrdenadorGasto> listarOrdenadores()  { return ordenadorDAO.listarTodos();   }

    /**
     * Obtiene un contrato con todos sus objetos relacionados hidratados.
     */
    public Contrato obtenerConRelaciones(int id) {
        Contrato contrato = contratoDAO.obtenerPorId(id);
        if (contrato == null) return null;

        if (contrato.getContratistaId() > 0)
            contrato.setContratista(contratistaDAO.obtenerPorId(contrato.getContratistaId()));
        if (contrato.getContratista() == null)
            contrato.setContratista(new Contratista());

        if (contrato.getPresupuestoId() > 0)
            contrato.setPresupuestoDetalle(presupuestoDAO.obtenerPorId(contrato.getPresupuestoId()));
        if (contrato.getPresupuestoDetalle() == null)
            contrato.setPresupuestoDetalle(new PresupuestoDetalle());

        if (contrato.getEstructuradorId() > 0)
            contrato.setEstructurador(estructuradorDAO.obtenerPorId(contrato.getEstructuradorId()));
        if (contrato.getEstructurador() == null)
            contrato.setEstructurador(new Estructurador());

        if (contrato.getSupervisorId() > 0)
            contrato.setSupervisor(supervisorDAO.obtenerPorId(contrato.getSupervisorId()));
        if (contrato.getSupervisor() == null)
            contrato.setSupervisor(new Supervisor());

        if (contrato.getOrdenadorId() > 0)
            contrato.setOrdenadorGasto(ordenadorDAO.obtenerPorId(contrato.getOrdenadorId()));
        if (contrato.getOrdenadorGasto() == null)
            contrato.setOrdenadorGasto(new OrdenadorGasto());

        return contrato;
    }

    // -------------------------------------------------------------------------
    // INSERCIÓN
    // -------------------------------------------------------------------------

    /**
     * Inserta un contrato completo junto con sus entidades relacionadas.
     * @return null si fue exitoso, o mensaje de error si falló.
     */
    public String insertar(ContratoFormData form) throws Exception {
        // 1. Contratista
        Contratista contratista = new Contratista();
        contratista.setCedula(form.contratistaCedula);
        contratista.setNombre(form.contratistaNombre);
        contratista.setTelefono(form.contratistaTelefono);
        contratista.setCorreo(form.contratistaCorreo);
        contratista.setDireccion(form.contratistaDireccion);
        contratista.setFechaNacimiento(ParseUtils.parseDate(form.contratistaFechaNac));
        contratista.setEdad(ParseUtils.parseInt(form.contratistaEdad));

        if (!contratistaDAO.insertar(contratista)) {
            Contratista existing = contratistaDAO.obtenerPorCedula(contratista.getCedula());
            if (existing != null) {
                contratista.setId(existing.getId());
            } else {
                throw new Exception("No se pudo crear ni encontrar el contratista.");
            }
        }

        // 2. Presupuesto
        PresupuestoDetalle presupuesto = new PresupuestoDetalle();
        presupuesto.setCdpNumero(form.presupuestoCdp);
        presupuesto.setRpNumero(form.presupuestoRpc);
        presupuestoDAO.insertar(presupuesto);

        // 3. Estructurador
        Estructurador estructurador = new Estructurador();
        estructurador.setJuridicoNombre(form.estructuradorJuridico);
        estructurador.setTecnicoNombre(form.estructuradorTecnico);
        estructurador.setFinancieroNombre(form.estructuradorFinanciero);
        estructuradorDAO.insertar(estructurador);

        // 4. Contrato
        Contrato contrato = poblarContrato(new Contrato(), form);
        contrato.setContratistaId(contratista.getId());
        contrato.setSupervisorId(form.supervisorId);
        contrato.setOrdenadorId(form.ordenadorId);
        contrato.setPresupuestoId(presupuesto.getId());
        contrato.setEstructuradorId(estructurador.getId());

        if (!contratoDAO.insertar(contrato)) {
            return "Error al guardar el contrato. Verifique los datos.";
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // ACTUALIZACIÓN
    // -------------------------------------------------------------------------

    /**
     * Actualiza un contrato y sus entidades relacionadas.
     * @return null si fue exitoso, o mensaje de error si falló.
     */
    public String actualizar(int id, ContratoFormData form) throws Exception {
        Contrato contrato = contratoDAO.obtenerPorId(id);
        if (contrato == null) throw new Exception("Contrato a actualizar no existe.");

        // 1. Actualizar Contratista
        if (contrato.getContratistaId() > 0) {
            Contratista contratista = contratistaDAO.obtenerPorId(contrato.getContratistaId());
            if (contratista != null) {
                contratista.setCedula(form.contratistaCedula);
                contratista.setDv(form.contratistaDv);
                contratista.setNombre(form.contratistaNombre);
                contratista.setTelefono(form.contratistaTelefono);
                contratista.setCorreo(form.contratistaCorreo);
                contratista.setDireccion(form.contratistaDireccion);
                contratista.setFechaNacimiento(ParseUtils.parseDate(form.contratistaFechaNac));
                contratista.setEdad(ParseUtils.parseInt(form.contratistaEdad));
                contratistaDAO.actualizar(contratista);
            }
        }

        // 2. Actualizar o crear Presupuesto
        if (contrato.getPresupuestoId() > 0) {
            PresupuestoDetalle p = presupuestoDAO.obtenerPorId(contrato.getPresupuestoId());
            if (p != null) {
                p.setCdpNumero(form.presupuestoCdp);
                p.setRpNumero(form.presupuestoRpc);
                presupuestoDAO.actualizar(p);
            }
        } else if (tieneValor(form.presupuestoCdp) || tieneValor(form.presupuestoRpc)) {
            PresupuestoDetalle novP = new PresupuestoDetalle();
            novP.setCdpNumero(form.presupuestoCdp);
            novP.setRpNumero(form.presupuestoRpc);
            novP.setApropiacionPresupuestal(""); novP.setIdPaa(""); novP.setCodigoDane("");
            novP.setInversion(""); novP.setFuncionamiento(""); novP.setFichaEbiNombre("");
            novP.setFichaEbiObjetivo(""); novP.setFichaEbiActividades(""); novP.setCertificadoInsuficiencia("");
            if (presupuestoDAO.insertar(novP)) contrato.setPresupuestoId(novP.getId());
        }

        // 3. Actualizar o crear Estructurador
        if (contrato.getEstructuradorId() > 0) {
            Estructurador e = estructuradorDAO.obtenerPorId(contrato.getEstructuradorId());
            if (e != null) {
                e.setJuridicoNombre(form.estructuradorJuridico);
                e.setTecnicoNombre(form.estructuradorTecnico);
                e.setFinancieroNombre(form.estructuradorFinanciero);
                estructuradorDAO.actualizar(e);
            }
        } else if (tieneValor(form.estructuradorJuridico) || tieneValor(form.estructuradorTecnico) || tieneValor(form.estructuradorFinanciero)) {
            Estructurador novE = new Estructurador();
            novE.setJuridicoNombre(form.estructuradorJuridico);
            novE.setTecnicoNombre(form.estructuradorTecnico);
            novE.setFinancieroNombre(form.estructuradorFinanciero);
            novE.setJuridicoCargo(""); novE.setTecnicoCargo(""); novE.setFinancieroCargo("");
            if (estructuradorDAO.insertar(novE)) contrato.setEstructuradorId(novE.getId());
        }

        // 4. Actualizar Contrato
        contrato = poblarContrato(contrato, form);
        if (form.supervisorId > 0) contrato.setSupervisorId(form.supervisorId);
        if (form.ordenadorId  > 0) contrato.setOrdenadorId(form.ordenadorId);

        if (!contratoDAO.actualizar(contrato)) {
            return "Error al actualizar el contrato.";
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // JSON para DataTables
    // -------------------------------------------------------------------------

    public String generarJsonDataTables(String draw, int start, int length,
            String searchValue, int orderColumn, String orderDir) {

        int total    = contratoDAO.countAll();
        int filtered = contratoDAO.countFiltered(searchValue);
        List<Contrato> contratos = contratoDAO.findWithPagination(start, length, searchValue, orderColumn, orderDir);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("draw", draw != null ? Integer.parseInt(draw) : 1);
        jsonResponse.addProperty("recordsTotal", total);
        jsonResponse.addProperty("recordsFiltered", filtered);
        JsonArray dataArray = new JsonArray();

        for (Contrato c : contratos) {
            JsonArray row = new JsonArray();
            row.add(c.getNumeroContrato() != null ? c.getNumeroContrato().trim() : "");
            row.add(c.getContratistaNombre() != null ? c.getContratistaNombre().trim() : "");
            row.add(c.getObjeto() != null ? c.getObjeto().trim() : "");
            row.add(c.getValorTotalNumeros() != null ? c.getValorTotalNumeros().toString() : "0");
            row.add(c.getFechaInicio()      != null ? sdf.format(c.getFechaInicio())      : "");
            row.add(c.getFechaTerminacion() != null ? sdf.format(c.getFechaTerminacion()) : "");
            row.add(c.getEstado() != null ? c.getEstado().trim() : "");
            row.add(String.valueOf(c.getId()));
            dataArray.add(row);
        }

        jsonResponse.add("data", dataArray);
        return gson.toJson(jsonResponse);
    }

    // -------------------------------------------------------------------------
    // PRIVADOS
    // -------------------------------------------------------------------------

    private Contrato poblarContrato(Contrato contrato, ContratoFormData f) {
        contrato.setNumeroContrato(f.numeroContrato);
        contrato.setTipoContrato(f.tipoContrato);
        contrato.setNivel(f.nivel);
        contrato.setObjeto(f.objeto);
        contrato.setValorTotalNumeros(ParseUtils.parseBigDecimal(f.valorTotal));
        contrato.setValorTotalLetras(f.valorTotalLetras);
        contrato.setValorCuotaNumero(ParseUtils.parseBigDecimal(f.valorCuotaNumero));
        contrato.setValorCuotaLetras(f.valorCuotaLetras);
        contrato.setValorMediaCuotaLetras(f.valorMediaCuotaLetras);
        contrato.setValorMediaCuotaNumero(ParseUtils.parseBigDecimal(f.valorMediaCuotaNumero));
        contrato.setFechaInicio(ParseUtils.parseDate(f.fechaInicio));
        contrato.setFechaTerminacion(ParseUtils.parseDate(f.fechaTerminacion));
        contrato.setFechaIdoneidad(ParseUtils.parseDate(f.fechaIdoneidad));
        contrato.setFechaEstructurador(ParseUtils.parseDate(f.fechaEstructurador));
        contrato.setActividadesEntregables(f.actividadesEntregables);
        contrato.setAdicionSiNo(f.adicionSiNo);
        contrato.setNumeroCuotasAdicion(ParseUtils.parseInt(f.numeroCuotasAdicion));
        contrato.setValorTotalAdicion(ParseUtils.parseBigDecimal(f.valorTotalAdicion));
        contrato.setValorTotalAdicionLetras(f.valorTotalAdicionLetras);
        contrato.setValorContratoMasAdicion(ParseUtils.parseBigDecimal(f.valorContratoMasAdicion));
        contrato.setValorContratoMasAdicionLetras(f.valorContratoMasAdicionLetras);
        contrato.setEnlaceSecop(f.enlaceSecop);
        return contrato;
    }

    private boolean tieneValor(String s) {
        return s != null && !s.trim().isEmpty();
    }

    // -------------------------------------------------------------------------
    // DTO interno para los datos del formulario
    // -------------------------------------------------------------------------

    /**
     * Objeto de transferencia de datos del formulario de Contrato.
     * Evita pasar decenas de parámetros individuales al Service.
     */
    public static class ContratoFormData {
        // Contratista
        public String contratistaCedula;
        public String contratistaDv;
        public String contratistaNombre;
        public String contratistaTelefono;
        public String contratistaCorreo;
        public String contratistaDireccion;
        public String contratistaFechaNac;
        public String contratistaEdad;
        // Presupuesto
        public String presupuestoCdp;
        public String presupuestoRpc;
        // Estructurador
        public String estructuradorJuridico;
        public String estructuradorTecnico;
        public String estructuradorFinanciero;
        // FK por ID
        public int supervisorId;
        public int ordenadorId;
        // Contrato principal
        public String numeroContrato;
        public String tipoContrato;
        public String nivel;
        public String objeto;
        public String valorTotal;
        public String valorTotalLetras;
        public String valorCuotaNumero;
        public String valorCuotaLetras;
        public String valorMediaCuotaLetras;
        public String valorMediaCuotaNumero;
        public String fechaInicio;
        public String fechaTerminacion;
        public String fechaIdoneidad;
        public String fechaEstructurador;
        public String actividadesEntregables;
        public String adicionSiNo;
        public String numeroCuotasAdicion;
        public String valorTotalAdicion;
        public String valorTotalAdicionLetras;
        public String valorContratoMasAdicion;
        public String valorContratoMasAdicionLetras;
        public String enlaceSecop;
    }
}
