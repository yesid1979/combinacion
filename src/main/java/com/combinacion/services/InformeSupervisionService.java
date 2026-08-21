package com.combinacion.services;
import com.combinacion.util.SupervisionReportGenerator;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;


import com.combinacion.dao.ContratoDAO;
import com.combinacion.dao.InformeSupervisionDAO;
import com.combinacion.models.Contrato;
import com.combinacion.models.InformeSupervision;
import com.combinacion.util.ParseUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class InformeSupervisionService {

    private final InformeSupervisionDAO informeDAO = new InformeSupervisionDAO();
    private final ContratoDAO contratoDAO = new ContratoDAO();
    private final com.combinacion.dao.ContratistaDAO contratistaDAO = new com.combinacion.dao.ContratistaDAO();
    private final com.combinacion.dao.SupervisorDAO supervisorDAO = new com.combinacion.dao.SupervisorDAO();
    private final com.combinacion.dao.OrdenadorGastoDAO ordenadorGastoDAO = new com.combinacion.dao.OrdenadorGastoDAO();

    public List<InformeSupervision> listarPorContrato(int contratoId) {
        return informeDAO.listarPorContrato(contratoId);
    }

    public List<InformeSupervision> listarTodos() {
        return informeDAO.listarTodos();
    }

    public InformeSupervision obtenerPorId(int id) {
        InformeSupervision info = informeDAO.obtenerPorId(id);
        if (info != null && info.getContratoId() != null) {
            info.setContrato(contratoDAO.obtenerPorId(info.getContratoId()));
        }
        return info;
    }

    public String insertar(InformeFormData form) {
        try {
            InformeSupervision info = mapFormToModel(form);
            if (info.getEstadoRadicacion() == null || info.getEstadoRadicacion().isEmpty()) {
                info.setEstadoRadicacion("BORRADOR");
            }
            // Asignar el año desde el contrato
            Contrato contrato = contratoDAO.obtenerPorId(info.getContratoId());
            if (contrato != null) {
                if (contrato.getAnio() != null && contrato.getAnio() > 0) {
                    info.setAnio(contrato.getAnio());
                } else if (contrato.getFechaTerminacion() != null) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(contrato.getFechaTerminacion());
                    info.setAnio(cal.get(java.util.Calendar.YEAR));
                }
            }

            // Validar duplicado: misma combinación de contrato, período, tipo e número de cuota
            if (informeDAO.existeDuplicado(info.getContratoId(), info.getPeriodoInforme(), info.getTipoInforme(), info.getNumeroCuota())) {
                return "Ya existe una cuenta de cobro registrada para este contrato con el mismo período, tipo e número de cuota. No se permite duplicar.";
            }
            String daoResult = informeDAO.insertar(info);
            if (daoResult == null) {
                return null; // Éxito
            } else {
                return "No se pudo guardar el informe en la base de datos: " + daoResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al procesar el informe: " + e.getMessage();
        }
    }

    public String actualizar(int id, InformeFormData form) {
        try {
            InformeSupervision info = mapFormToModel(form);
            info.setId(id);
            
            // Asignar el año desde el contrato
            Contrato contrato = contratoDAO.obtenerPorId(info.getContratoId());
            if (contrato != null) {
                if (contrato.getAnio() != null && contrato.getAnio() > 0) {
                    info.setAnio(contrato.getAnio());
                } else if (contrato.getFechaTerminacion() != null) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(contrato.getFechaTerminacion());
                    info.setAnio(cal.get(java.util.Calendar.YEAR));
                }
            }

            // Preserve fields that might not come in the form
            InformeSupervision existente = informeDAO.obtenerPorId(id);
            if (existente != null) {
                if (info.getUrlDriveEvidencias() == null || info.getUrlDriveEvidencias().isEmpty()) {
                    info.setUrlDriveEvidencias(existente.getUrlDriveEvidencias());
                }
                if (info.getSoportesJson() == null || info.getSoportesJson().isEmpty()) {
                    info.setSoportesJson(existente.getSoportesJson());
                }
                if (info.getEstadoRadicacion() == null || info.getEstadoRadicacion().isEmpty()) {
                    info.setEstadoRadicacion(existente.getEstadoRadicacion());
                }
            }
            
            String daoResult = informeDAO.actualizar(info);
            if (daoResult == null) {
                return null; // Éxito
            } else {
                return "No se pudo actualizar el informe en la base de datos: " + daoResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al procesar la actualización del informe: " + e.getMessage();
        }
    }

    private InformeSupervision mapFormToModel(InformeFormData f) {
        InformeSupervision info = new InformeSupervision();
        info.setContratoId(f.contratoId);
        info.setPeriodoInforme(f.periodoInforme);
        info.setTipoInforme(f.tipoInforme);
        info.setNumeroCuota(f.numeroCuota);
        info.setConsecutivoCobro(f.consecutivoCobro);
        
        info.setFechaInicioPeriodo(ParseUtils.parseDate(f.fechaInicioPeriodo));
        info.setFechaFinPeriodo(ParseUtils.parseDate(f.fechaFinPeriodo));
        info.setModificaciones(f.modificaciones);
        info.setSuspensiones(f.suspensiones);
        info.setReanudaciones(f.reanudaciones);
        info.setCesiones(f.cesiones);
        info.setTerminacionAnticipada(f.terminacionAnticipada);
        info.setAdiciones(f.adiciones);
        info.setProrrogas(f.prorrogas);
        info.setReciboSatisfaccion(f.reciboSatisfaccion);
        info.setConstanciaPazSalvo(f.constanciaPazSalvo);
        
        info.setValorCuotaPagar(ParseUtils.parseBigDecimal(f.valorCuotaPagar));
        info.setValorAccumuladoPagado(ParseUtils.parseBigDecimal(f.valorAccumuladoPagado));
        info.setSaldoPorCancelar(ParseUtils.parseBigDecimal(f.saldoPorCancelar));
        
        info.setPlanillaNumero(f.planillaNumero);
        info.setPlanillaPin(f.planillaPin);
        info.setPlanillaOperador(f.planillaOperador);
        info.setPlanillaFechaPago(ParseUtils.parseDate(f.planillaFechaPago));
        info.setPlanillaPeriodo(f.planillaPeriodo);
        info.setPagoSeguridadSocial(f.pagoSeguridadSocial);
        
        info.setConceptoSupervisor(f.conceptoSupervisor);
        info.setObservacionesFinancieras(f.observacionesFinancieras);
        info.setObservacionesTecnicas(f.observacionesTecnicas);
        info.setRecomendaciones(f.recomendaciones);
        info.setFechaSuscripcion(ParseUtils.parseDate(f.fechaSuscripcion));
        info.setUrlDriveEvidencias(f.urlDriveEvidencias);
        info.setSoportesJson(f.soportesJson);
        info.setEstadoRadicacion(f.estadoRadicacion);
        info.setIdRevisorAsignado(f.idRevisorAsignado);
        
        return info;
    }

    public static class InformeFormData {
        public int contratoId;
        public String periodoInforme;
        public String tipoInforme;
        public String numeroCuota;
        public String consecutivoCobro;
        public String fechaInicioPeriodo;
        public String fechaFinPeriodo;
        public String modificaciones;
        public String suspensiones;
        public String reanudaciones;
        public String cesiones;
        public String terminacionAnticipada;
        public String adiciones;
        public String prorrogas;
        public String reciboSatisfaccion;
        public String constanciaPazSalvo;
        public String valorCuotaPagar;
        public String valorAccumuladoPagado;
        public String saldoPorCancelar;
        public String planillaNumero;
        public String planillaPin;
        public String planillaOperador;
        public String planillaFechaPago;
        public String planillaPeriodo;
        public String pagoSeguridadSocial;
        public String conceptoSupervisor;
        public String observacionesFinancieras;
        public String observacionesTecnicas;
        public String recomendaciones;
        public String fechaSuscripcion;
        public String urlDriveEvidencias;
        public String soportesJson;
        public String estadoRadicacion;
        public Integer idRevisorAsignado;
    }

    public Contrato obtenerContrato(int id) {
        Contrato c = contratoDAO.obtenerPorId(id);
        if (c != null) {
            if (c.getContratistaId() > 0) {
                c.setContratista(contratistaDAO.obtenerPorId(c.getContratistaId()));
                if (c.getContratista() != null) {
                    c.setContratistaNombre(c.getContratista().getNombre());
                }
            }
            if (c.getSupervisorId() > 0) {
                c.setSupervisor(supervisorDAO.obtenerPorId(c.getSupervisorId()));
            }
            if (c.getOrdenadorId() > 0) {
                c.setOrdenadorGasto(ordenadorGastoDAO.obtenerPorId(c.getOrdenadorId()));
            }
            if (c.getPresupuestoId() > 0) {
                com.combinacion.dao.PresupuestoDetalleDAO presupuestoDAO = new com.combinacion.dao.PresupuestoDetalleDAO();
                c.setPresupuestoDetalle(presupuestoDAO.obtenerPorId(c.getPresupuestoId()));
            }
        }
        return c;
    }

public void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        java.util.List<InformeSupervision> listaFinal = obtenerListaInformes(request);
        String modo = (String) request.getAttribute("modo");
        
        request.setAttribute("listaInformes", listaFinal);
        request.setAttribute("revisoresList", new com.combinacion.dao.UsuarioDAO().listarRevisores());
        request.getRequestDispatcher("lista_informes.jsp").forward(request, response);
    }

    public java.util.List<InformeSupervision> obtenerListaInformes(HttpServletRequest request) {
        com.combinacion.models.Usuario u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario");
        
        // Refrescar usuario desde BD para tener los permisos actualizados en caliente
        if (u != null) {
            com.combinacion.models.Usuario freshUser = new com.combinacion.dao.UsuarioDAO().obtenerPorId(u.getId());
            if (freshUser != null) {
                request.getSession().setAttribute("usuario", freshUser);
                u = freshUser;
            }
        }
        boolean esAdmin = u != null && (u.esAdministrador() || u.tienePermiso("ADMINISTRAR_CUENTAS_EDITAR") || u.tienePermiso("ADMINISTRAR_CUENTAS"));
        boolean esRevisor = u != null && (u.tienePermiso("PUEDE_REVISAR_CUENTAS") || u.tienePermiso("REVISION_CUENTAS_VER"));
        boolean esContratistaBase = (u != null && (u.getRolId() == 3 || (u.getRol() != null && "Contratista".equalsIgnoreCase(u.getRol().getNombre()))));
        
        String modo = request.getParameter("modo");
        
        // Asignar modo por defecto si entran sin parámetro (ej. desde un enlace manual o error)
        if (modo == null || modo.trim().isEmpty()) {
            if (esContratistaBase) {
                modo = "mis_cuentas";
            } else if (esAdmin || esRevisor) {
                modo = "revision";
            }
        }
        
        String contratoIdStr = request.getParameter("contrato_id");
        java.util.List<InformeSupervision> listaFinal = new java.util.ArrayList<>();
        
        // 1. Lógica para Contratistas: Buscar sus contratos y sus propios informes
        java.util.List<Integer> misContratosIds = new java.util.ArrayList<>();
        if (esContratistaBase && !"revision".equals(modo)) {
            com.combinacion.dao.ContratistaDAO cdao = new com.combinacion.dao.ContratistaDAO();
            java.util.List<com.combinacion.models.Contratista> todos = cdao.listarTodos();
            com.combinacion.models.Contratista c = null;
            for (com.combinacion.models.Contratista cont : todos) {
                if (cont.getCedula() != null) {
                    String limpiaDB = cont.getCedula().replaceAll("[^0-9]", "");
                    if (limpiaDB.equals(u.getCedula())) {
                        c = cont;
                        break;
                    }
                }
            }
            if (c != null) {
                com.combinacion.dao.ContratoDAO codao = new com.combinacion.dao.ContratoDAO();
                java.util.List<Contrato> misContratos = codao.listarPorContratistaId(c.getId());
                if (!misContratos.isEmpty()) {
                    request.setAttribute("misContratos", misContratos);
                    request.setAttribute("contrato", misContratos.get(0)); // Default para botón "Nuevo"
                    for(Contrato con : misContratos) {
                        misContratosIds.add(con.getId());
                        if (contratoIdStr == null || contratoIdStr.isEmpty() || com.combinacion.util.ParseUtils.parseInt(contratoIdStr) == con.getId()) {
                            listaFinal.addAll(this.listarPorContrato(con.getId()));
                        }
                    }
                } else if (!esAdmin && !esRevisor) {
                    request.setAttribute("error", "No tienes ningún contrato activo asignado en el sistema.");
                }
            } else if (!esAdmin && !esRevisor) {
                request.setAttribute("error", "No se encontraron tus datos como contratista.");
            }
        }
        
        // 2. Lógica para Admin / Revisor: Agregar informes que deben revisar
        if ((esAdmin || esRevisor) && !"mis_cuentas".equals(modo)) {
            java.util.List<InformeSupervision> todos = new java.util.ArrayList<>();
            if (contratoIdStr != null && !contratoIdStr.isEmpty()) {
                int contratoIdParam = com.combinacion.util.ParseUtils.parseInt(contratoIdStr);
                todos = this.listarPorContrato(contratoIdParam);
                // Cargar el contrato para que la JSP muestre el botón "Nuevo Informe"
                if (esAdmin) {
                    Contrato contratoSeleccionado = this.obtenerContrato(contratoIdParam);
                    if (contratoSeleccionado != null) {
                        request.setAttribute("contrato", contratoSeleccionado);
                        modo = "contrato_admin"; // Permitir que la JSP muestre el botón Nuevo Informe
                    }
                }
            } else {
                todos = this.listarTodos();
            }
            
            for (InformeSupervision info : todos) {
                // Evitar duplicados si ya los cargó por ser su propio contrato
                if (misContratosIds.contains(info.getContratoId())) {
                    continue; 
                }
                
                if (esAdmin) {
                    listaFinal.add(info);
                } else if (esRevisor) {
                    // Un revisor básico solo ve las cuentas asignadas a él que no sean borradores ni devueltas
                    if (info.getIdRevisorAsignado() != null && info.getIdRevisorAsignado() == u.getId() 
                            && !"BORRADOR".equals(info.getEstadoRadicacion()) 
                            && !"DEVUELTA".equals(info.getEstadoRadicacion())) {
                        listaFinal.add(info);
                    }
                }
            }
        }
        
        // Ordenar por Fecha de Registro (antiguas primero)
        listaFinal.sort((a, b) -> {
            if (a.getFechaCreacion() == null && b.getFechaCreacion() == null) return 0;
            if (a.getFechaCreacion() == null) return 1;
            if (b.getFechaCreacion() == null) return -1;
            return b.getFechaCreacion().compareTo(a.getFechaCreacion());
        });
        
        request.setAttribute("modo", modo); // Pasar el modo a la vista para cambiar el título si se desea
        request.setAttribute("esAdminGlobal", esAdmin);
        request.setAttribute("esRevisorGlobal", esRevisor);
        
        return listaFinal;
    }
    
    public void devolverDatosDataTables(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String drawStr = request.getParameter("draw");
        int draw = (drawStr != null && !drawStr.isEmpty()) ? Integer.parseInt(drawStr) : 1;
        String startStr = request.getParameter("start");
        int start = (startStr != null && !startStr.isEmpty()) ? Integer.parseInt(startStr) : 0;
        String lengthStr = request.getParameter("length");
        int length = (lengthStr != null && !lengthStr.isEmpty()) ? Integer.parseInt(lengthStr) : 10;
        String searchValue = request.getParameter("search[value]");
        if (searchValue != null) searchValue = searchValue.toLowerCase();

        java.util.List<InformeSupervision> listaTotal = obtenerListaInformes(request);
        int recordsTotal = listaTotal.size();

        // 1. Filtrado en memoria
        String column7Search = request.getParameter("columns[7][search][value]");
        String estadoFilter = null;
        if (column7Search != null && !column7Search.isEmpty()) {
            estadoFilter = column7Search.replaceAll("\\^|\\$|\\\\", "").toUpperCase();
        }

        java.util.List<InformeSupervision> listaFiltrada = new java.util.ArrayList<>();
        for (InformeSupervision info : listaTotal) {
            boolean matchGlobal = true;
            if (searchValue != null && !searchValue.isEmpty()) {
                matchGlobal = false;
                if (info.getContrato() != null && info.getContrato().getNumeroContrato() != null 
                        && info.getContrato().getNumeroContrato().toLowerCase().contains(searchValue)) {
                    matchGlobal = true;
                }
                if (info.getContrato() != null && info.getContrato().getContratistaNombre() != null 
                        && info.getContrato().getContratistaNombre().toLowerCase().contains(searchValue)) {
                    matchGlobal = true;
                }
                if (info.getPeriodoInforme() != null && info.getPeriodoInforme().toLowerCase().contains(searchValue)) {
                    matchGlobal = true;
                }
                if (info.getEstadoRadicacion() != null && info.getEstadoRadicacion().toLowerCase().contains(searchValue)) {
                    matchGlobal = true;
                }
                // Si el filtro coincide con BORRADOR al estar vacío
                if ((info.getEstadoRadicacion() == null || info.getEstadoRadicacion().isEmpty()) && "borrador".contains(searchValue)) {
                    matchGlobal = true;
                }
            }

            boolean matchEstado = true;
            if (estadoFilter != null && !estadoFilter.isEmpty()) {
                String currentEstado = (info.getEstadoRadicacion() == null || info.getEstadoRadicacion().isEmpty()) ? "BORRADOR" : info.getEstadoRadicacion().toUpperCase();
                if (!currentEstado.equals(estadoFilter)) {
                    matchEstado = false;
                }
            }

            if (matchGlobal && matchEstado) {
                listaFiltrada.add(info);
            }
        }
        int recordsFiltered = listaFiltrada.size();

        // 2. Paginación en memoria
        int toIndex = Math.min(start + length, listaFiltrada.size());
        java.util.List<InformeSupervision> page = new java.util.ArrayList<>();
        if (start < listaFiltrada.size()) {
            page = listaFiltrada.subList(start, toIndex);
        }

        // 3. Serializar
        com.google.gson.JsonObject jsonResponse = new com.google.gson.JsonObject();
        jsonResponse.addProperty("draw", draw);
        jsonResponse.addProperty("recordsTotal", recordsTotal);
        jsonResponse.addProperty("recordsFiltered", recordsFiltered);

        com.google.gson.JsonArray dataArray = new com.google.gson.JsonArray();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("America/Bogota"));
        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "CO"));
        nf.setMaximumFractionDigits(0);
        
        boolean esAdminGlobal = false;
        boolean esRevisorGlobal = false;
        if (request.getAttribute("esAdminGlobal") != null) esAdminGlobal = (Boolean) request.getAttribute("esAdminGlobal");
        if (request.getAttribute("esRevisorGlobal") != null) esRevisorGlobal = (Boolean) request.getAttribute("esRevisorGlobal");
        String modo = (String) request.getAttribute("modo");

        for (InformeSupervision info : page) {
            com.google.gson.JsonObject row = new com.google.gson.JsonObject();
            
            // 0: Contrato
            row.addProperty("contrato", info.getContrato() != null ? info.getContrato().getNumeroContrato() : "");
            
            // 1: Contratista
            row.addProperty("contratista", info.getContrato() != null ? info.getContrato().getContratistaNombre() : "");
            
            // 2: Periodo
            row.addProperty("periodo", info.getPeriodoInforme() != null ? ParseUtils.formatearPeriodo(info.getPeriodoInforme()) : "");
            
            // 3: Tipo
            row.addProperty("tipo", info.getTipoInforme());
            
            // 4: Cuota
            row.addProperty("cuota", info.getNumeroCuota());
            
            // 5: Fecha Registro
            row.addProperty("fechaRegistro", info.getFechaCreacion() != null ? sdf.format(info.getFechaCreacion()) : "");
            row.addProperty("fechaRegistroTime", info.getFechaCreacion() != null ? info.getFechaCreacion().getTime() : 0);
            
            // 6: Valor Cuota
            row.addProperty("valorCuota", info.getValorCuotaPagar() != null ? nf.format(info.getValorCuotaPagar()) : "$ 0");
            
            // 7: Estado
            String estado = (info.getEstadoRadicacion() == null || info.getEstadoRadicacion().isEmpty()) ? "BORRADOR" : info.getEstadoRadicacion();
            row.addProperty("estado", estado);
            
            // Info adicional para construir las acciones en JS
            row.addProperty("id", info.getId());
            row.addProperty("modo", modo != null ? modo : "");
            row.addProperty("esAdminCuentas", esAdminGlobal);
            row.addProperty("esRevisorCuentas", esRevisorGlobal);
            row.addProperty("idRevisorAsignado", info.getIdRevisorAsignado() != null ? info.getIdRevisorAsignado() : 0);
            
            com.combinacion.models.Usuario u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario");
            row.addProperty("usuarioActualId", u != null ? u.getId() : 0);

            dataArray.add(row);
        }
        jsonResponse.add("data", dataArray);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    public void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contratoIdStr = request.getParameter("contrato_id");
        if (contratoIdStr != null) {
            int contratoId = ParseUtils.parseInt(contratoIdStr);
            Contrato contrato = this.obtenerContrato(contratoId);
            request.setAttribute("contrato", contrato);
            if (contrato != null) {
                // Calcular acumulado previo y número de cuota sugerido
                java.util.List<com.combinacion.models.InformeSupervision> previos = this.listarPorContrato(contratoId);
                java.math.BigDecimal acumulado = java.math.BigDecimal.ZERO;
                if(previos != null && !previos.isEmpty()){
                    for(com.combinacion.models.InformeSupervision prev : previos){
                        if(prev.getValorCuotaPagar() != null){
                            acumulado = acumulado.add(prev.getValorCuotaPagar());
                        }
                    }
                }
                request.setAttribute("acumuladoPrevio", acumulado);
                int siguienteCuota = previos != null ? previos.size() + 1 : 1;
                request.setAttribute("siguienteCuota", siguienteCuota);

                boolean esPrimeraCuotaAdicion = false;
                if ("Si".equalsIgnoreCase(contrato.getAdicionSiNo()) && contrato.getNumCuotasNumero() > 0) {
                    int totalCuotas = contrato.getNumCuotasNumero();
                int ultimaNormal = totalCuotas;
                if (siguienteCuota == ultimaNormal) {
                        esPrimeraCuotaAdicion = true;
                    }
                }

                // Auto-cargar RPC, Modificacion y Secop de la cuota anterior si existen y NO es la primera cuota de adicion
                if(previos != null && !previos.isEmpty() && !esPrimeraCuotaAdicion) {
                    org.json.JSONObject newSoportes = new org.json.JSONObject();
                    boolean foundRpc = false, foundMod = false, foundSecop = false;
                    System.out.println("Auto-cargando soportes para cuota: " + siguienteCuota);
                    for (int i = 0; i < previos.size(); i++) {
                        String sJson = previos.get(i).getSoportesJson();
                        System.out.println("Revisando previo cuota " + previos.get(i).getNumeroCuota() + " -> JSON: " + sJson);
                        if (sJson != null && !sJson.isEmpty()) {
                            try {
                                org.json.JSONObject sAnteriores = new org.json.JSONObject(sJson);
                                if (!foundRpc) {
                                    org.json.JSONObject rpcData = getLatestFile(sAnteriores, "file_rpc");
                                    if (rpcData != null) {
                                        rpcData.put("needs_copy", true);
                                        newSoportes.put("file_rpc", rpcData);
                                        foundRpc = true;
                                    }
                                }
                                boolean esAdicionAvanzada = "Si".equalsIgnoreCase(contrato.getAdicionSiNo()) && contrato.getNumCuotasNumero() > 0 && siguienteCuota > contrato.getNumCuotasNumero();
                                if (esAdicionAvanzada) {
                                    if (!foundMod) {
                                        org.json.JSONObject modData = getLatestFile(sAnteriores, "file_modificacion");
                                        if (modData != null) {
                                            modData.put("needs_copy", true);
                                            newSoportes.put("file_modificacion", modData);
                                            foundMod = true;
                                        }
                                    }
                                    if (!foundSecop) {
                                        org.json.JSONObject secopData = getLatestFile(sAnteriores, "file_secop");
                                        if (secopData != null) {
                                            secopData.put("needs_copy", true);
                                            newSoportes.put("file_secop", secopData);
                                            foundSecop = true;
                                        }
                                    }
                                }
                                System.out.println("newSoportes actuales: " + newSoportes.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (newSoportes.length() > 0) {
                        System.out.println("FINAL PRECARGADOS: " + newSoportes.toString());
                        request.setAttribute("soportesJsonPreCargados", newSoportes.toString());
                    }
                }

                // ---- GENERACION AUTOMATICA DE TEXTOS DE MODIFICACION ----
                com.combinacion.models.InformeSupervision informeAuto = new com.combinacion.models.InformeSupervision();
                if ("Si".equalsIgnoreCase(contrato.getAdicionSiNo())) {
                    poblarTextosModificacion(contrato, informeAuto);
                }
                
                request.setAttribute("informeAuto", informeAuto);
                // --------------------------------------------------------------------------------
                
                request.setAttribute("listaObligaciones", com.combinacion.util.ObligacionesParser.decodificarConcepto(null, contrato.getActividadesEntregables()));
            }
        }
        
        request.setAttribute("listaRevisores", new com.combinacion.dao.UsuarioDAO().listarRevisores());
        request.setAttribute("action", "insert");
        request.getRequestDispatcher("form_supervision.jsp").forward(request, response);
    }

    public void mostrarDetalle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = ParseUtils.parseInt(request.getParameter("id"));
        InformeSupervision informe = this.obtenerPorId(id);
        request.setAttribute("informe", informe);
        if (informe != null && informe.getContratoId() != null) {
            Contrato contrato = this.obtenerContrato(informe.getContratoId());
            request.setAttribute("contrato", contrato);
            if (contrato != null) {
                request.setAttribute("listaObligaciones", com.combinacion.util.ObligacionesParser.decodificarConcepto(informe.getConceptoSupervisor(), contrato.getActividadesEntregables()));
            }
        }
        request.setAttribute("readonly", true);
        request.setAttribute("action", "view");
        request.setAttribute("modo", request.getParameter("modo"));
        request.setAttribute("listaRevisores", new com.combinacion.dao.UsuarioDAO().listarRevisores());
        request.setAttribute("listaHistorial", new com.combinacion.dao.HistorialRadicacionDAO().listarPorInforme(id));
        request.getRequestDispatcher("form_supervision.jsp").forward(request, response);
    }

    public void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = ParseUtils.parseInt(request.getParameter("id"));
        InformeSupervision informe = this.obtenerPorId(id);
        if (informe == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Informe no encontrado");
            return;
        }
        request.setAttribute("informe", informe);
        if (informe.getContratoId() != null) {
            Contrato contrato = this.obtenerContrato(informe.getContratoId());
            request.setAttribute("contrato", contrato);
            if (contrato != null) {
                if ("Si".equalsIgnoreCase(contrato.getAdicionSiNo())) {
                    com.combinacion.models.InformeSupervision informeAuto = new com.combinacion.models.InformeSupervision();
                    poblarTextosModificacion(contrato, informeAuto);
                    request.setAttribute("informeAuto", informeAuto);
                }
                
                int cuotaActualParsed = com.combinacion.util.ParseUtils.parseInt(informe.getNumeroCuota());
                request.setAttribute("siguienteCuota", cuotaActualParsed);
                
                request.setAttribute("listaObligaciones", com.combinacion.util.ObligacionesParser.decodificarConcepto(informe.getConceptoSupervisor(), contrato.getActividadesEntregables()));
                
                // Determinar si es la PRIMERA cuota de adición para NO autocargar el RPC de las cuotas normales
                boolean esPrimeraCuotaAdicion = false;
                if ("Si".equalsIgnoreCase(contrato.getAdicionSiNo()) && contrato.getNumCuotasNumero() > 0) {
                    int totalCuotas = contrato.getNumCuotasNumero();
                int ultimaNormal = totalCuotas;
                int cuotaActual = com.combinacion.util.ParseUtils.parseInt(informe.getNumeroCuota());
                if (cuotaActual == ultimaNormal) {
                        esPrimeraCuotaAdicion = true;
                    }
                }
                
                // Auto-cargar RPC, Modificacion y Secop si no lo tiene (excepto en la primera cuota de adición)
                String currentJson = informe.getSoportesJson();
                if (!esPrimeraCuotaAdicion) {
                    java.util.List<com.combinacion.models.InformeSupervision> previos = this.listarPorContrato(informe.getContratoId());
                    if (previos != null && !previos.isEmpty()) {
                        org.json.JSONObject currentObj = (currentJson != null && !currentJson.isEmpty()) ? new org.json.JSONObject(currentJson) : new org.json.JSONObject();
                        boolean foundRpc = currentObj.has("file_rpc");
                        boolean foundMod = currentObj.has("file_modificacion");
                        boolean foundSecop = currentObj.has("file_secop");
                        
                        for (int i = 0; i < previos.size(); i++) {
                            if (previos.get(i).getId() == informe.getId()) continue; // Skip itself
                            String sJson = previos.get(i).getSoportesJson();
                            if (sJson != null && !sJson.isEmpty()) {
                                try {
                                    org.json.JSONObject sAnteriores = new org.json.JSONObject(sJson);
                                    if (!foundRpc && !esPrimeraCuotaAdicion) {
                                        org.json.JSONObject rpcData = getLatestFile(sAnteriores, "file_rpc");
                                        if (rpcData != null) {
                                            rpcData.put("needs_copy", true);
                                            currentObj.put("file_rpc", rpcData);
                                            foundRpc = true;
                                        }
                                    }
                                    int cuotaActualStr = com.combinacion.util.ParseUtils.parseInt(informe.getNumeroCuota());
                                    boolean esAdicionAvanzada = "Si".equalsIgnoreCase(contrato.getAdicionSiNo()) && contrato.getNumCuotasNumero() > 0 && cuotaActualStr > contrato.getNumCuotasNumero();
                                    if (esAdicionAvanzada) {
                                        if (!foundMod) {
                                            org.json.JSONObject modData = getLatestFile(sAnteriores, "file_modificacion");
                                            if (modData != null) {
                                                modData.put("needs_copy", true);
                                                currentObj.put("file_modificacion", modData);
                                                foundMod = true;
                                            }
                                        }
                                        if (!foundSecop && !esPrimeraCuotaAdicion) {
                                            org.json.JSONObject secopData = getLatestFile(sAnteriores, "file_secop");
                                            if (secopData != null) {
                                                secopData.put("needs_copy", true);
                                                currentObj.put("file_secop", secopData);
                                                foundSecop = true;
                                            }
                                        }
                                    }
                                } catch (Exception e) {}
                            }
                        }
                        if (currentObj.length() > 0) {
                            informe.setSoportesJson(currentObj.toString());
                        }
                    }
                }
            }
        }
        
        request.setAttribute("listaRevisores", new com.combinacion.dao.UsuarioDAO().listarRevisores());
        request.setAttribute("action", "update");
        request.setAttribute("modo", request.getParameter("modo"));
        request.setAttribute("listaHistorial", new com.combinacion.dao.HistorialRadicacionDAO().listarPorInforme(id));
        request.getRequestDispatcher("form_supervision.jsp").forward(request, response);
    }

    public void descargarArchivoDirecto(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String tipo = request.getParameter("tipo");
        if (idStr == null || idStr.isEmpty() || tipo == null) return;
        
        InformeSupervision informe = this.obtenerPorId(Integer.parseInt(idStr));
        if (informe == null) return;
        Contrato contrato = this.obtenerContrato(informe.getContratoId());
        
        java.io.File file = null;
        String fileName = "";
        String mime = "";
        boolean tieneIva = "SI".equalsIgnoreCase(contrato.getIvaSiNo());
        
        try {
            if ("ds".equals(tipo)) {
                if (tieneIva) return;
                String xlsxPath = com.combinacion.util.CuentaCobroGenerator.generarExcel(informe, contrato, request.getServletContext().getRealPath("/"));
                file = new java.io.File(xlsxPath);
                fileName = "DS_Cuota_" + informe.getNumeroCuota() + ".xlsx";
                mime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            } else if ("supervision".equals(tipo)) {
                String path = com.combinacion.util.SupervisionReportGenerator.generarDocx(informe, contrato, request.getServletContext().getRealPath("/"));
                java.io.File docx = new java.io.File(path);
                file = docx;
                fileName = "Informe_Supervision_Cuota_" + informe.getNumeroCuota() + ".docx";
                mime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            } else if ("gestion".equals(tipo)) {
                String path = com.combinacion.util.GestionReportGenerator.generarDocx(informe, contrato, request.getServletContext().getRealPath("/"));
                java.io.File docx = new java.io.File(path);
                file = docx;
                fileName = "Informe_Gestion_Cuota_" + informe.getNumeroCuota() + ".docx";
                mime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
            
            if (file != null && file.exists()) {
                response.setContentType(mime);
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                try (java.io.FileInputStream fis = new java.io.FileInputStream(file); java.io.OutputStream os = response.getOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se pudo generar el documento solicitado.");
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generando documento: " + e.getMessage());
        }
    }

    public void descargarInforme(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de informe no proporcionado.");
            return;
        }

        try {
            int informeId = com.combinacion.util.ParseUtils.parseInt(idStr);
            InformeSupervision informe = this.obtenerPorId(informeId);
            if (informe == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Informe no encontrado.");
                return;
            }

            Contrato contrato = this.obtenerContrato(informe.getContratoId());
            
            String nombreCompleto = contrato.getContratistaNombre() != null ? contrato.getContratistaNombre().trim() : "";
            String nombreCorto = nombreCompleto;
            String[] parts = nombreCompleto.split("\\s+");
            if (parts.length >= 3) {
                nombreCorto = parts[0] + " " + parts[2];
            } else if (parts.length == 2) {
                nombreCorto = parts[0] + " " + parts[1];
            }

            boolean esCuota1 = "1".equals(informe.getNumeroCuota());
            boolean tieneIva = "SI".equalsIgnoreCase(contrato.getIvaSiNo());

            boolean esCuotaAdicion = false;
            if ("Si".equalsIgnoreCase(contrato.getAdicionSiNo())) {
                int cuotasNormales = contrato.getNumCuotasNumero();
                int cuotaActual = com.combinacion.util.ParseUtils.parseInt(informe.getNumeroCuota());
                if (cuotasNormales > 0 && cuotaActual >= cuotasNormales) {
                    esCuotaAdicion = true;
                }
            }

            String consecutivoStr = (informe.getConsecutivoCobro() != null && !informe.getConsecutivoCobro().trim().isEmpty()) 
                                        ? informe.getConsecutivoCobro().trim() 
                                        : (tieneIva ? "FACTURA" : "XXXX");
            String shortContrato = contrato.getNumeroContrato() != null ? contrato.getNumeroContrato().split("\\.")[0] : "";
            
            String docxName;
            String xlsxName;
            String gestionName;
            
            if (esCuota1) {
                docxName = "5. INFORME DE SUPERVISIÓN CUOTA 1 - " + nombreCorto + ".docx";
                xlsxName = "3. DS-" + shortContrato + "-" + consecutivoStr + " CUOTA 1 " + nombreCorto + ".xlsx";
                gestionName = "12. INFORME DE GESTIÓN CUOTA 1 - " + nombreCorto + ".docx";
            } else if (esCuotaAdicion) {
                docxName = "4. INFORME DE SUPERVISIÓN CUOTA " + informe.getNumeroCuota() + " - " + nombreCorto + ".docx";
                xlsxName = "3. DS-" + shortContrato + "-" + consecutivoStr + " CUOTA " + informe.getNumeroCuota() + " " + nombreCorto + ".xlsx";
                gestionName = "12. INFORME DE GESTIÓN CUOTA " + informe.getNumeroCuota() + " - " + nombreCorto + ".docx";
            } else {
                docxName = "3. INFORME DE SUPERVISIÓN CUOTA " + informe.getNumeroCuota() + " - " + nombreCorto + ".docx";
                xlsxName = "2. DS-" + shortContrato + "-" + consecutivoStr + " CUOTA " + informe.getNumeroCuota() + " " + nombreCorto + ".xlsx";
                gestionName = "5. INFORME DE GESTIÓN CUOTA " + informe.getNumeroCuota() + " - " + nombreCorto + ".docx";
            }
            
            // Archivo DOCX temporal Archivo DOCX temporal
            String filePathDocx = SupervisionReportGenerator.generarDocx(informe, contrato, request.getServletContext().getRealPath("/"));
            File docxFile = new File(filePathDocx);
            
            // Archivo XLSX temporal (solo si no tiene IVA)
            File xlsxFile = null;
            File xlsxPdfFile = null;
            String xlsxPdfName = null;
            if (!tieneIva) {
                try {
                    String xlsxPath = com.combinacion.util.CuentaCobroGenerator.generarExcel(informe, contrato, request.getServletContext().getRealPath("/"));
                    xlsxFile = new File(xlsxPath);
                    if (xlsxFile.exists()) {
                        String pdfPath = xlsxPath.replaceAll("(?i)\\.xlsx$", ".pdf");
                        xlsxPdfFile = new File(pdfPath);
                        com.combinacion.util.PdfGenerator.convertExcelToPdf(xlsxFile, xlsxPdfFile);
                        xlsxPdfName = xlsxName.replaceAll("(?i)\\.xlsx$", ".pdf");
                    }
                } catch (Exception e) {
                    System.out.println("No se pudo generar el Excel o PDF: " + e.getMessage());
                }
            }
            
            // Archivo de Gestion temporal
            File gestionFile = null;
            File gestionPdfFile = null;
            String gestionPdfName = null;
            try {
                String gestionPath = com.combinacion.util.GestionReportGenerator.generarDocx(informe, contrato, request.getServletContext().getRealPath("/"));
                gestionFile = new File(gestionPath);
                if (gestionFile.exists()) {
                    String pdfPath = gestionPath.replaceAll("(?i)\\.docx$", ".pdf");
                    gestionPdfFile = new File(pdfPath);
                    com.combinacion.util.PdfGenerator.convertToPdf(gestionFile, gestionPdfFile);
                    gestionPdfName = gestionName.replaceAll("(?i)\\.docx$", ".pdf");
                }
            } catch (Exception e) {
                System.out.println("No se pudo generar el Informe de Gestion: " + e.getMessage());
            }

            // Si existen, generar ZIP
            if (docxFile != null && docxFile.exists()) {
                String numContrato = contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : "";
                String primerBloque = "";
                String ultimoBloque = "";
                if (numContrato.contains(".")) {
                    String[] numParts = numContrato.split("\\.");
                    primerBloque = numParts[0];
                    ultimoBloque = numParts[numParts.length - 1];
                } else {
                    primerBloque = numContrato;
                }
                
                String safeName = nombreCorto.replaceAll("[^a-zA-Z0-9.\\- ]", "");
                String zipFileName = primerBloque + (!ultimoBloque.isEmpty() ? " - " + ultimoBloque : "") + " " + safeName + ".zip";
                
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");
                
                try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(response.getOutputStream())) {
                    
                    // Las evidencias ahora se descargan desde el JSON de soportes para mantener las subcarpetas.
                    
                    // Agregar DOCX (Supervision)
                    zos.putNextEntry(new java.util.zip.ZipEntry(docxName));
                    try (FileInputStream fis = new FileInputStream(docxFile)) {
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = fis.read(buffer)) >= 0) {
                            zos.write(buffer, 0, length);
                        }
                    }
                    zos.closeEntry();
                    
                    // Agregar DOCX (Gestion)
                    if (gestionFile != null && gestionFile.exists()) {
                        zos.putNextEntry(new java.util.zip.ZipEntry(gestionName));
                        try (FileInputStream fis = new FileInputStream(gestionFile)) {
                            byte[] buffer = new byte[4096];
                            int length;
                            while ((length = fis.read(buffer)) >= 0) {
                                zos.write(buffer, 0, length);
                            }
                        }
                        zos.closeEntry();
                    }
                    
                    // Agregar PDF (Gestion)
                    if (gestionPdfFile != null && gestionPdfFile.exists()) {
                        zos.putNextEntry(new java.util.zip.ZipEntry(gestionPdfName));
                        try (FileInputStream fis = new FileInputStream(gestionPdfFile)) {
                            byte[] buffer = new byte[4096];
                            int length;
                            while ((length = fis.read(buffer)) >= 0) {
                                zos.write(buffer, 0, length);
                            }
                        }
                        zos.closeEntry();
                    }
                    
                    // Agregar XLSX (Cuenta Cobro)
                    if (xlsxFile != null && xlsxFile.exists()) {
                        zos.putNextEntry(new java.util.zip.ZipEntry(xlsxName));
                        try (FileInputStream fis = new FileInputStream(xlsxFile)) {
                            byte[] buffer = new byte[4096];
                            int length;
                            while ((length = fis.read(buffer)) >= 0) {
                                zos.write(buffer, 0, length);
                            }
                        }
                        zos.closeEntry();
                    }
                    
                    // Agregar XLSX PDF (Cuenta Cobro en PDF)
                    if (xlsxPdfFile != null && xlsxPdfFile.exists()) {
                        zos.putNextEntry(new java.util.zip.ZipEntry(xlsxPdfName));
                        try (FileInputStream fis = new FileInputStream(xlsxPdfFile)) {
                            byte[] buffer = new byte[4096];
                            int length;
                            while ((length = fis.read(buffer)) >= 0) {
                                zos.write(buffer, 0, length);
                            }
                        }
                        zos.closeEntry();
                    }
                    
                    // Agregar anexos
                    java.io.File tempSegSoc = null;
                    if (informe.getSoportesJson() != null && !informe.getSoportesJson().isEmpty()) {
                        try {
                            org.json.JSONObject soportes = new org.json.JSONObject(informe.getSoportesJson());
                            java.util.Set<String> addedEntries = new java.util.HashSet<>();
                            for (String key : soportes.keySet()) {
                                String zipPath = "";
                                if (key.startsWith("evidencia_")) {
                                    try {
                                        int actIndex = Integer.parseInt(key.split("_")[1]) + 1;
                                        zipPath = "Evidencias/Actividad " + actIndex + "/";
                                    } catch (Exception e) {
                                        zipPath = "Evidencias/";
                                    }
                                }
                                org.json.JSONObject fileData = soportes.getJSONObject(key);
                                String fileId = fileData.optString("id");
                                String fileName = fileData.optString("name");
                                if (fileId != null && !fileId.isEmpty() && fileName != null && !fileName.isEmpty()) {
                                    try {
                                        String entryName = zipPath + fileName;
                                        int counter = 1;
                                        while (addedEntries.contains(entryName)) {
                                            int dotIndex = fileName.lastIndexOf('.');
                                            if (dotIndex > 0) {
                                                entryName = zipPath + fileName.substring(0, dotIndex) + " (" + counter + ")" + fileName.substring(dotIndex);
                                            } else {
                                                entryName = zipPath + fileName + " (" + counter + ")";
                                            }
                                            counter++;
                                        }
                                        addedEntries.add(entryName);
                                        
                                        zos.putNextEntry(new java.util.zip.ZipEntry(entryName));
                                        try (java.io.InputStream in = com.combinacion.services.GoogleDriveService.downloadFile(fileId)) {
                                            byte[] buffer = new byte[4096];
                                            int length;
                                            if ("file_seguridad_social".equals(key)) {
                                                tempSegSoc = java.io.File.createTempFile("seg_soc", ".pdf");
                                                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempSegSoc)) {
                                                    while ((length = in.read(buffer)) >= 0) {
                                                        zos.write(buffer, 0, length);
                                                        fos.write(buffer, 0, length);
                                                    }
                                                }
                                            } else {
                                                while ((length = in.read(buffer)) >= 0) {
                                                    zos.write(buffer, 0, length);
                                                }
                                            }
                                        }
                                        zos.closeEntry();
                                    } catch (Exception ex) {
                                        System.err.println("No se pudo descargar o agregar anexo " + fileName + " al ZIP: " + ex.getMessage());
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            System.err.println("Error procesando anexos: " + ex.getMessage());
                        }
                    }
                    
                    if (gestionPdfFile != null && gestionPdfFile.exists() && tempSegSoc != null && tempSegSoc.exists()) {
                        String mergedName = esCuota1 ? "13. INFORME GESTIÓN No.1.pdf" : "INFORME GESTIÓN No." + informe.getNumeroCuota() + ".pdf";
                        java.io.File mergedFile = java.io.File.createTempFile("merged", ".pdf");
                        if (com.combinacion.util.PdfGenerator.mergePdfs(gestionPdfFile, tempSegSoc, mergedFile)) {
                            zos.putNextEntry(new java.util.zip.ZipEntry(mergedName));
                            try (java.io.FileInputStream fis = new java.io.FileInputStream(mergedFile)) {
                                byte[] buffer = new byte[4096];
                                int length;
                                while ((length = fis.read(buffer)) >= 0) {
                                    zos.write(buffer, 0, length);
                                }
                            }
                            zos.closeEntry();
                        }
                        mergedFile.delete();
                        tempSegSoc.delete();
                    }
                }
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No se pudo generar el informe de supervision principal.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el documento: " + e.getMessage());
        }
    }
    
    private org.json.JSONObject getLatestFile(org.json.JSONObject soportes, String baseKey) {
        org.json.JSONObject latest = null;
        int maxVersion = -1;
        
        java.util.Iterator<String> keys = soportes.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            if (k.equals(baseKey) && maxVersion < 0) {
                latest = soportes.optJSONObject(k);
                maxVersion = 0;
            } else if (k.startsWith(baseKey + "_")) {
                try {
                    int v = Integer.parseInt(k.substring(baseKey.length() + 1));
                    if (v > maxVersion) {
                        maxVersion = v;
                        latest = soportes.optJSONObject(k);
                    }
                } catch (NumberFormatException e) {}
            }
        }
        return latest;
    }

    public void exportarPdf(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {  }

    public void procesarArchivosDrive(int informeId, HttpServletRequest request) {
        try {
            System.out.println("Iniciando subida automatica a Drive para informe ID: " + informeId);
            InformeSupervision informe = this.obtenerPorId(informeId);
            if (informe == null) return;
            Contrato contrato = this.obtenerContrato(informe.getContratoId());
            if (contrato == null) return;
            
            String nombreCompleto = contrato.getContratistaNombre() != null ? contrato.getContratistaNombre().trim() : "";
            String nombreCorto = nombreCompleto;
            String[] parts = nombreCompleto.split("\\s+");
            if (parts.length >= 3) {
                nombreCorto = parts[0] + " " + parts[2];
            } else if (parts.length == 2) {
                nombreCorto = parts[0] + " " + parts[1];
            }

            boolean esCuota1 = "1".equals(informe.getNumeroCuota());
            boolean tieneIva = "SI".equalsIgnoreCase(contrato.getIvaSiNo());

            boolean esCuotaAdicion = false;
            if ("Si".equalsIgnoreCase(contrato.getAdicionSiNo())) {
                int cuotasNormales = contrato.getNumCuotasNumero();
                int cuotaActual = com.combinacion.util.ParseUtils.parseInt(informe.getNumeroCuota());
                if (cuotasNormales > 0 && cuotaActual >= cuotasNormales) {
                    esCuotaAdicion = true;
                }
            }

            String consecutivoStr = (informe.getConsecutivoCobro() != null && !informe.getConsecutivoCobro().trim().isEmpty()) 
                                        ? informe.getConsecutivoCobro().trim() 
                                        : (tieneIva ? "FACTURA" : "XXXX");

            String shortContrato = contrato.getNumeroContrato() != null ? contrato.getNumeroContrato().split("\\.")[0] : "";
            String ultimoBloque = "";
            if (contrato.getNumeroContrato() != null && contrato.getNumeroContrato().contains(".")) {
                String[] numParts = contrato.getNumeroContrato().split("\\.");
                ultimoBloque = numParts[numParts.length - 1];
            }
            
            String folderNamePrincipal = shortContrato + (!ultimoBloque.isEmpty() ? " - " + ultimoBloque : "") + " " + nombreCorto;
            String folderNameCuota = "Cuota " + informe.getNumeroCuota();
            
            // 1. Obtener/crear "pruebas cuenta de cobro"
            String pruebasFolderId = com.combinacion.services.GoogleDriveService.getOrCreateFolder(com.combinacion.dao.ConfiguracionDAO.getValor("DRIVE_CARPETA_PRUEBAS", "pruebas cuenta de cobro"), null);
            // 2. Obtener/crear carpeta principal
            String principalFolderId = com.combinacion.services.GoogleDriveService.getOrCreateFolder(folderNamePrincipal, pruebasFolderId);
            // 3. Obtener/crear cuota
            String cuotaFolderId = com.combinacion.services.GoogleDriveService.getOrCreateFolder(folderNameCuota, principalFolderId);
            // 4. Obtener/crear evidencias
            String evidenciasFolderId = com.combinacion.services.GoogleDriveService.getOrCreateFolder(com.combinacion.dao.ConfiguracionDAO.getValor("DRIVE_CARPETA_EVIDENCIAS", "EVIDENCIAS"), cuotaFolderId);
            
            // 4.1. Dar permisos públicos de lectura a TODA la carpeta de la cuota
            try {
                com.combinacion.services.GoogleDriveService.setPublicViewPermission(cuotaFolderId);
            } catch (Exception ignore) {
                System.err.println("Aviso: No se pudo asignar permisos publicos a la carpeta CUOTA: " + ignore.getMessage());
            }
            
            // 4.5. Guardar la URL en la base de datos (apuntando a la carpeta EVIDENCIAS)
            String driveUrl = "https://drive.google.com/drive/folders/" + evidenciasFolderId + "?usp=sharing";
            informe.setUrlDriveEvidencias(driveUrl);
            new com.combinacion.dao.InformeSupervisionDAO().actualizarUrlDrive(informe.getId(), driveUrl);
            
            // 5. Generar archivos localmente
            String docxName;
            String xlsxName;
            String gestionName;
            
            if (esCuota1) {
                docxName = "5. INFORME DE SUPERVISIÓN CUOTA 1 - " + nombreCorto + ".docx";
                xlsxName = "3. DS-" + shortContrato + "-" + consecutivoStr + " CUOTA 1 " + nombreCorto + ".xlsx";
                gestionName = "12. INFORME DE GESTIÓN CUOTA 1 - " + nombreCorto + ".docx";
            } else if (esCuotaAdicion) {
                docxName = "6. INFORME DE SUPERVISIÓN CUOTA " + informe.getNumeroCuota() + " - " + nombreCorto + ".docx";
                xlsxName = "4. DS-" + shortContrato + "-" + consecutivoStr + " CUOTA " + informe.getNumeroCuota() + " " + nombreCorto + ".xlsx";
                gestionName = "13. INFORME DE GESTIÓN CUOTA " + informe.getNumeroCuota() + " - " + nombreCorto + ".docx";
            } else {
                docxName = "3. INFORME DE SUPERVISIÓN CUOTA " + informe.getNumeroCuota() + " - " + nombreCorto + ".docx";
                xlsxName = "2. DS-" + shortContrato + "-" + consecutivoStr + " CUOTA " + informe.getNumeroCuota() + " " + nombreCorto + ".xlsx";
                gestionName = "5. INFORME DE GESTIÓN CUOTA " + informe.getNumeroCuota() + " - " + nombreCorto + ".docx";
            }

            String docxPath = com.combinacion.util.SupervisionReportGenerator.generarDocx(informe, contrato, request.getServletContext().getRealPath("/"));
            File docxFile = new File(docxPath);
            
            File xlsxFile = null;
            File xlsxPdfFile = null;
            String xlsxPdfName = null;
            if (!tieneIva) {
                String xlsxPath = com.combinacion.util.CuentaCobroGenerator.generarExcel(informe, contrato, request.getServletContext().getRealPath("/"));
                xlsxFile = new File(xlsxPath);
                if (xlsxFile.exists()) {
                    String pdfPath = xlsxPath.replaceAll("(?i)\\.xlsx$", ".pdf");
                    xlsxPdfFile = new File(pdfPath);
                    com.combinacion.util.PdfGenerator.convertExcelToPdf(xlsxFile, xlsxPdfFile);
                    xlsxPdfName = xlsxName.replaceAll("(?i)\\.xlsx$", ".pdf");
                }
            }
            
            String gestionPath = com.combinacion.util.GestionReportGenerator.generarDocx(informe, contrato, request.getServletContext().getRealPath("/"));
            File gestionFile = new File(gestionPath);
            File gestionPdfFile = null;
            String gestionPdfName = null;
            if (gestionFile.exists()) {
                String gestionPdfPath = gestionPath.replaceAll("(?i)\\.docx$", ".pdf");
                gestionPdfFile = new File(gestionPdfPath);
                com.combinacion.util.PdfGenerator.convertToPdf(gestionFile, gestionPdfFile);
                gestionPdfName = gestionName.replaceAll("(?i)\\.docx$", ".pdf");
            }
            
            // 6. Subir archivos a Drive (Docs y Excel)
            if (docxFile != null && docxFile.exists()) {
                com.combinacion.services.GoogleDriveService.uploadOrUpdateFile(docxFile, docxName, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", cuotaFolderId);
            }
            if (xlsxFile != null && xlsxFile.exists()) {
                // Usar prefijo para detectar y eliminar versiones anteriores con diferente consecutivo (ej: XXXX -> 0233)
                String xlsxPrefix = (esCuota1 ? "3. DS-" : (esCuotaAdicion ? "4. DS-" : "2. DS-")) + shortContrato + "-";
                com.combinacion.services.GoogleDriveService.uploadOrReplaceByPattern(xlsxFile, xlsxName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", cuotaFolderId, xlsxPrefix);
            }
            if (xlsxPdfFile != null && xlsxPdfFile.exists()) {
                // Mismo prefijo para la versión PDF de la cuenta cobro
                String xlsxPdfPrefix = (esCuota1 ? "3. DS-" : (esCuotaAdicion ? "4. DS-" : "2. DS-")) + shortContrato + "-";
                com.combinacion.services.GoogleDriveService.uploadOrReplaceByPattern(xlsxPdfFile, xlsxPdfName, "application/pdf", cuotaFolderId, xlsxPdfPrefix);
            }
            if (gestionFile != null && gestionFile.exists()) {
                com.combinacion.services.GoogleDriveService.uploadOrUpdateFile(gestionFile, gestionName, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", cuotaFolderId);
            }
            if (gestionPdfFile != null && gestionPdfFile.exists()) {
                com.combinacion.services.GoogleDriveService.uploadOrUpdateFile(gestionPdfFile, gestionPdfName, "application/pdf", cuotaFolderId);
            }
            
            // 7. Subir todos los documentos soporte
            org.json.JSONObject soportes = new org.json.JSONObject();
            if (informe.getSoportesJson() != null && !informe.getSoportesJson().isEmpty()) {
                try { 
                    soportes = new org.json.JSONObject(informe.getSoportesJson());
                } catch (Exception ignore) {}
            }
            
            // LOGICA AUTOMATICA DE HERENCIA PARA ADICIONES
            boolean esAdicion = "Si".equalsIgnoreCase(contrato.getAdicionSiNo());
            boolean esFirmaAdicion = (esAdicion && informe.getNumeroCuota() != null && informe.getNumeroCuota().equals(String.valueOf(contrato.getNumCuotasNumero())));
            boolean esCuotaAdicionPosterior = (esAdicion && informe.getNumeroCuota() != null && com.combinacion.util.ParseUtils.parseInt(informe.getNumeroCuota()) > contrato.getNumCuotasNumero());

            if (esFirmaAdicion) {
                java.util.List<com.combinacion.models.InformeSupervision> informes = new com.combinacion.dao.InformeSupervisionDAO().listarPorContrato(contrato.getId());
                for (com.combinacion.models.InformeSupervision inf : informes) {
                    if ("1".equals(inf.getNumeroCuota()) && inf.getSoportesJson() != null && !inf.getSoportesJson().isEmpty()) {
                        try {
                            org.json.JSONObject sop = new org.json.JSONObject(inf.getSoportesJson());
                            String[] keys = {"file_cedula", "file_rut", "file_correccion_monetaria", "file_medicina_prepagada", "file_certificado_dependientes"};
                            for (String k : keys) {
                                if (sop.has(k) && !soportes.has(k)) {
                                    org.json.JSONObject fileObj = sop.getJSONObject(k);
                                    fileObj.put("needs_copy", true);
                                    soportes.put(k, fileObj);
                                }
                            }
                        } catch (Exception e) {}
                        break;
                    }
                }
            } else if (esCuotaAdicionPosterior) {
                java.util.List<com.combinacion.models.InformeSupervision> informes = new com.combinacion.dao.InformeSupervisionDAO().listarPorContrato(contrato.getId());
                for (com.combinacion.models.InformeSupervision inf : informes) {
                    if (String.valueOf(contrato.getNumCuotasNumero()).equals(inf.getNumeroCuota()) && inf.getSoportesJson() != null && !inf.getSoportesJson().isEmpty()) {
                        try {
                            org.json.JSONObject sop = new org.json.JSONObject(inf.getSoportesJson());
                            String[] keys = {"file_rpc", "file_modificacion", "file_secop", "file_ficha_tecnica", "file_cedula", "file_rut", "file_correccion_monetaria", "file_medicina_prepagada", "file_certificado_dependientes"};
                            for (String k : keys) {
                                if (sop.has(k) && !soportes.has(k)) {
                                    org.json.JSONObject fileObj = sop.getJSONObject(k);
                                    fileObj.put("needs_copy", true);
                                    soportes.put(k, fileObj);
                                }
                            }
                        } catch (Exception e) {}
                        break;
                    }
                }
            }
            
            // Procesar archivos marcados para copia física (tanto los que venían en JSON como los heredados)
            java.util.Iterator<String> iterKeys = soportes.keys();
            while (iterKeys.hasNext()) {
                String key = iterKeys.next();
                org.json.JSONObject fileObj = soportes.getJSONObject(key);
                if (fileObj.optBoolean("needs_copy", false)) {
                    try {
                        String oldId = fileObj.getString("id");
                        String newName = fileObj.getString("name");
                        
                        // Renombrar los archivos heredados si estamos en la cuota de adición (Cuota 4)
                        if (esFirmaAdicion) {
                            if ("file_cedula".equals(key)) {
                                newName = "7. CEDULA - " + nombreCorto + ".pdf";
                            } else if ("file_rut".equals(key)) {
                                newName = "8. RUT - " + nombreCorto + ".pdf";
                            } else if ("file_correccion_monetaria".equals(key)) {
                                newName = "10. CERTIFICACION CORRECCION MONETARIA - " + nombreCorto + ".pdf";
                            } else if ("file_medicina_prepagada".equals(key)) {
                                newName = "11. CERTIFICADO MEDICINA PREPAGADA - " + nombreCorto + ".pdf";
                            } else if ("file_certificado_dependientes".equals(key)) {
                                newName = "12. CERTIFICADO DEPENDIENTES - " + nombreCorto + ".pdf";
                            }
                        }
                        
                        String newId = com.combinacion.services.GoogleDriveService.copyFile(oldId, newName, cuotaFolderId);
                        fileObj.put("name", newName);
                        fileObj.put("id", newId);
                        fileObj.put("url", "https://drive.google.com/file/d/" + newId + "/view");
                        fileObj.remove("needs_copy");
                    } catch (Exception e) {
                        System.err.println("Error copying file in drive: " + e.getMessage());
                    }
                }
            }
            
            java.io.File tempSegSoc = null;
            
            System.out.println("Iniciando escaneo de partes (archivos adjuntos)...");
            for (Part part : request.getParts()) {
                String submittedFileName = getFileName(part);
                if (submittedFileName != null && !submittedFileName.trim().isEmpty() && part.getSize() > 0) {
                    String partName = part.getName();
                    
                    String targetFolderId = cuotaFolderId;
                    if (partName != null && partName.startsWith("evidencia_")) {
                        try {
                            int actIndex = Integer.parseInt(partName.split("_")[1]) + 1;
                            String subFolderName = "Actividad " + actIndex;
                            targetFolderId = com.combinacion.services.GoogleDriveService.getOrCreateFolder(subFolderName, evidenciasFolderId);
                        } catch (Exception e) {
                            targetFolderId = evidenciasFolderId;
                        }
                    } else if (partName != null && (partName.equals("file_paz_salvo_orfeo") || partName.equals("file_paz_salvo_procesos") || partName.equals("file_paz_salvo_creaciones"))) {
                        targetFolderId = evidenciasFolderId;
                    }
                    
                    // Renombrar los archivos obligatorios segun la nomenclatura
                    if (partName != null && !partName.startsWith("evidencia_")) {
                        String ext = submittedFileName.contains(".") ? submittedFileName.substring(submittedFileName.lastIndexOf(".")) : ".pdf";
                        String baseName = submittedFileName;
                        String cuotaNum = informe.getNumeroCuota() != null ? informe.getNumeroCuota() : "";
                        
                        if ("file_rpc".equals(partName)) {
                            baseName = "1. RPC - " + nombreCorto;
                        } else if ("file_modificacion".equals(partName)) {
                            baseName = "2. OTROSI - " + nombreCorto;
                        } else if ("file_factura".equals(partName)) {
                            baseName = (esCuotaAdicion ? "4." : (esCuota1 ? "2." : "2.")) + " FACTURA ELECTRONICA CUOTA " + cuotaNum + " - " + nombreCorto;
                        } else if ("file_secop".equals(partName)) {
                            baseName = (esCuotaAdicion ? "3. CONTRATO Y PANTALLAZO SECOP - " : "2. CONTRATO SECOP II - ") + nombreCorto;
                        } else if ("file_ficha_tecnica".equals(partName)) {
                            baseName = (esCuotaAdicion ? "5." : "4.") + " FICHA TECNICA - " + nombreCorto;
                        } else if ("file_cedula".equals(partName)) {
                            baseName = (esCuotaAdicion ? "7." : "6.") + " CEDULA - " + nombreCorto;
                        } else if ("file_rut".equals(partName)) {
                            baseName = (esCuotaAdicion ? "8." : "7.") + " RUT - " + nombreCorto;
                        } else if ("file_seguridad_social".equals(partName)) {
                            baseName = (esCuota1 ? "8." : (esCuotaAdicion ? "9." : "4.")) + " SEGURIDAD SOCIAL CUOTA " + cuotaNum + " - " + nombreCorto;
                        } else if ("file_correccion_monetaria".equals(partName)) {
                            baseName = (esCuotaAdicion ? "10." : "9.") + " CERTIFICACION CORRECCION MONETARIA - " + nombreCorto;
                        } else if ("file_medicina_prepagada".equals(partName)) {
                            baseName = (esCuotaAdicion ? "11." : "10.") + " CERTIFICADO MEDICINA PREPAGADA - " + nombreCorto;
                        } else if ("file_certificado_dependientes".equals(partName)) {
                            baseName = (esCuotaAdicion ? "12." : "11.") + " CERTIFICADO DEPENDIENTES - " + nombreCorto;
                        } else if ("file_paz_salvo_orfeo".equals(partName)) {
                            baseName = "Paz y salvo orfeo Cuota No. " + cuotaNum;
                        } else if ("file_paz_salvo_procesos".equals(partName)) {
                            baseName = "Paz y salvo procesos Cuota No. " + cuotaNum;
                        } else if ("file_paz_salvo_creaciones".equals(partName)) {
                            baseName = "Paz y salvo creaciones Cuota No. " + cuotaNum;
                        }
                        
                        submittedFileName = baseName + ext;
                    }
                    
                    System.out.println("Subiendo " + partName + ": " + submittedFileName + " (" + part.getSize() + " bytes)");
                    String mimeType = part.getContentType() != null ? part.getContentType() : "application/octet-stream";
                    try (java.io.InputStream is = part.getInputStream()) {
                        String fileId;
                        if ("file_seguridad_social".equals(partName) && submittedFileName.toLowerCase().endsWith(".pdf")) {
                            tempSegSoc = java.io.File.createTempFile("seg_soc", ".pdf");
                            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempSegSoc)) {
                                byte[] buf = new byte[8192];
                                int bytesRead;
                                while ((bytesRead = is.read(buf)) != -1) {
                                    fos.write(buf, 0, bytesRead);
                                }
                            }
                            fileId = com.combinacion.services.GoogleDriveService.uploadOrUpdateFile(tempSegSoc, submittedFileName, mimeType, targetFolderId);
                        } else {
                            fileId = com.combinacion.services.GoogleDriveService.uploadStreamToDrive(is, part.getSize(), submittedFileName, mimeType, targetFolderId);
                        }
                        
                        org.json.JSONObject fileData = new org.json.JSONObject();
                        fileData.put("name", submittedFileName);
                        fileData.put("id", fileId);
                        fileData.put("url", "https://drive.google.com/file/d/" + fileId + "/view");
                        
                        String soportesKey = partName;
                        if (soportes.has(soportesKey)) {
                            int k = 1;
                            while (soportes.has(partName + "_" + k)) k++;
                            soportesKey = partName + "_" + k;
                        }
                        soportes.put(soportesKey, fileData);
                    } catch (Exception ex) {
                        System.err.println("Error subiendo archivo " + submittedFileName + ": " + ex.getMessage());
                    }
                }
            }
            informe.setSoportesJson(soportes.toString());
            new com.combinacion.dao.InformeSupervisionDAO().actualizarSoportesJson(informe.getId(), soportes.toString());
            
            // Logica para merge si no subieron una nueva SS pero existe en JSON
            if (tempSegSoc == null && soportes.has("file_seguridad_social")) {
                org.json.JSONObject ssObj = soportes.getJSONObject("file_seguridad_social");
                String fileId = ssObj.optString("id");
                if (fileId != null && !fileId.isEmpty()) {
                    try {
                        java.io.InputStream is = com.combinacion.services.GoogleDriveService.downloadFile(fileId);
                        tempSegSoc = java.io.File.createTempFile("seg_soc", ".pdf");
                        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempSegSoc)) {
                            byte[] buf = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = is.read(buf)) != -1) {
                                fos.write(buf, 0, bytesRead);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tempSegSoc = null;
                    }
                }
            }
            
            if (tempSegSoc != null && tempSegSoc.exists() && gestionPdfFile != null && gestionPdfFile.exists()) {
                String mergedName = esCuota1 ? "13. INFORME GESTIÓN No.1.pdf" : "INFORME GESTIÓN No." + informe.getNumeroCuota() + ".pdf";
                java.io.File mergedFile = java.io.File.createTempFile("merged", ".pdf");
                if (com.combinacion.util.PdfGenerator.mergePdfs(gestionPdfFile, tempSegSoc, mergedFile)) {
                    com.combinacion.services.GoogleDriveService.uploadOrUpdateFile(mergedFile, mergedName, "application/pdf", cuotaFolderId);
                }
                mergedFile.delete();
            }
            if (tempSegSoc != null) {
                tempSegSoc.delete();
            }
            
            System.out.println("Subida a Drive completada con exito.");
        } catch (Exception e) {
            System.err.println("Error subiendo archivos a Drive:");
            e.printStackTrace();
        }
    }

    public String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp != null) {
            for (String cd : contentDisp.split(";")) {
                if (cd.trim().startsWith("filename")) {
                    return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return null;
    }

    public void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        com.combinacion.models.Usuario u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario");
        int id = com.combinacion.util.ParseUtils.parseInt(request.getParameter("id"));
        if (id <= 0) {
            request.getSession().setAttribute("errorMessage", "ID de cuenta inválido.");
            response.sendRedirect("informes");
            return;
        }
        com.combinacion.models.InformeSupervision informe = this.obtenerPorId(id);
        if (informe == null) {
            request.getSession().setAttribute("errorMessage", "No se encontró la cuenta de cobro.");
            response.sendRedirect("informes");
            return;
        }
        if (!"BORRADOR".equals(informe.getEstadoRadicacion())) {
            request.getSession().setAttribute("errorMessage", "Solo se pueden eliminar cuentas en estado BORRADOR.");
            response.sendRedirect("informes");
            return;
        }
        new com.combinacion.dao.InformeSupervisionDAO().eliminar(id);
        com.combinacion.dao.AuditoriaDAO.registrar(u, "Eliminación de Cuenta", "Se eliminó la cuenta de cobro ID " + id + " (estaba en BORRADOR)", request.getRemoteAddr());
        request.getSession().setAttribute("successMessage", "La cuenta de cobro ha sido eliminada correctamente.");
        response.sendRedirect("informes");
    }

    public void insertar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        InformeFormData form = construirFormData(request);
        String error = this.insertar(form);
        if (error != null) {
            request.setAttribute("error", error);
            mostrarFormularioNuevo(request, response);
        } else {
            // Procesar Drive después de guardar exitosamente
            java.util.List<InformeSupervision> lista = this.listarPorContrato(form.contratoId);
            if (lista != null && !lista.isEmpty()) {
                InformeSupervision guardado = lista.get(0);
                com.combinacion.models.Usuario u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario");
                
                // Auditoría
                com.combinacion.dao.AuditoriaDAO.registrar(u, "Creación de Cuenta", "Se creó la cuenta de cobro ID " + guardado.getId() + " para el contrato " + form.contratoId, request.getRemoteAddr());
                
                if ("RADICADA".equals(guardado.getEstadoRadicacion())) {
                    com.combinacion.models.HistorialRadicacion hr = new com.combinacion.models.HistorialRadicacion();
                    hr.setIdInforme(guardado.getId());
                    hr.setIdUsuarioCambio(u != null ? u.getId() : 0);
                    hr.setEstadoAnterior("BORRADOR");
                    hr.setEstadoNuevo("RADICADA");
                    hr.setObservaciones("Cuenta radicada por primera vez.");
                    new com.combinacion.dao.HistorialRadicacionDAO().registrarCambio(hr);
                }
                procesarArchivosDrive(guardado.getId(), request);
            }
            request.getSession().setAttribute("successMessage", "El informe de supervisión ha sido registrado correctamente.");
            response.sendRedirect("informes");
        }
    }

    public void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int id = ParseUtils.parseInt(request.getParameter("id"));
        InformeFormData form = construirFormData(request);
        
        com.combinacion.models.InformeSupervision existente = this.obtenerPorId(id);
        String estadoAnterior = (existente != null) ? existente.getEstadoRadicacion() : "";
        String obsAnterior = (existente != null && existente.getObservacionesRevision() != null) ? existente.getObservacionesRevision() : "";
        
        String error = this.actualizar(id, form);
        if (error != null) {
            request.setAttribute("error", error);
            mostrarFormularioEdicion(request, response);
        } else {
            com.combinacion.models.Usuario u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario");
            
            // Auditoría
            com.combinacion.dao.AuditoriaDAO.registrar(u, "Actualización de Cuenta", "Se actualizó la cuenta de cobro ID " + id, request.getRemoteAddr());
            
            if ("RADICADA".equals(form.estadoRadicacion) && !"RADICADA".equals(estadoAnterior)) {
                com.combinacion.models.HistorialRadicacion hr = new com.combinacion.models.HistorialRadicacion();
                hr.setIdInforme(id);
                hr.setIdUsuarioCambio(u != null ? u.getId() : 0);
                hr.setEstadoAnterior(estadoAnterior);
                hr.setEstadoNuevo("RADICADA");
                hr.setObservaciones("DEVUELTA".equals(estadoAnterior) ? "Cuenta vuelta a radicar tras correcciones." : "Cuenta radicada para revisión.");
                new com.combinacion.dao.HistorialRadicacionDAO().registrarCambio(hr);
            }
            // Procesar Drive después de actualizar exitosamente
            procesarArchivosDrive(id, request);
            request.getSession().setAttribute("successMessage", "El informe de supervisión ha sido actualizado correctamente.");
            response.sendRedirect("informes");
        }
    }

    private InformeFormData construirFormData(HttpServletRequest r) {
        InformeFormData f = new InformeFormData();
        f.contratoId = ParseUtils.parseInt(r.getParameter("contrato_id"));
        f.periodoInforme = r.getParameter("periodo_informe");
        f.tipoInforme = r.getParameter("tipo_informe");
        f.numeroCuota = r.getParameter("numero_cuota");
        f.consecutivoCobro = r.getParameter("consecutivo_cobro");
        f.fechaInicioPeriodo = r.getParameter("fecha_inicio_periodo");
        f.fechaFinPeriodo = r.getParameter("fecha_fin_periodo");
        f.modificaciones = r.getParameter("modificaciones");
        f.suspensiones = r.getParameter("suspensiones");
        f.reanudaciones = r.getParameter("reanudaciones");
        f.cesiones = r.getParameter("cesiones");
        f.terminacionAnticipada = r.getParameter("terminacion_anticipada");
        f.adiciones = r.getParameter("adiciones");
        f.prorrogas = r.getParameter("prorrogas");
        f.reciboSatisfaccion = r.getParameter("recibo_satisfaccion");
        f.constanciaPazSalvo = r.getParameter("constancia_paz_salvo");
        f.valorCuotaPagar = r.getParameter("valor_cuota_pagar");
        f.valorAccumuladoPagado = r.getParameter("valor_acumulado_pagado");
        f.saldoPorCancelar = r.getParameter("saldo_por_cancelar");
        f.planillaNumero = r.getParameter("planilla_numero");
        f.planillaPin = r.getParameter("planilla_pin");
        f.planillaOperador = r.getParameter("planilla_operador");
        f.planillaFechaPago = r.getParameter("planilla_fecha_pago");
        f.planillaPeriodo = r.getParameter("planilla_periodo");
        f.pagoSeguridadSocial = r.getParameter("pago_seguridad_social");
        
        String conceptoJson = r.getParameter("concepto_supervisor_json");
        if (conceptoJson != null && !conceptoJson.isEmpty()) {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(conceptoJson);
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject obj = arr.getJSONObject(i);
                    if (obj.has("actividad")) {
                        String joinedAct = obj.getString("actividad");
                        joinedAct = cleanWordHtml(joinedAct);
                        obj.put("actividad", joinedAct);
                    }
                }
                f.conceptoSupervisor = arr.toString();
            } catch (Exception ex) {
                f.conceptoSupervisor = conceptoJson;
            }
        } else {
            int count = ParseUtils.parseInt(r.getParameter("obligaciones_count"));
            if (count > 0) {
                org.json.JSONArray arr = new org.json.JSONArray();
                for (int i = 0; i < count; i++) {
                    org.json.JSONObject obj = new org.json.JSONObject();
                    obj.put("obligacion", r.getParameter("obligacion_" + i));
                    String[] acts = r.getParameterValues("actividad_" + i);
                    String joinedAct = "";
                    if (acts != null) {
                        joinedAct = String.join("\n", acts);
                        joinedAct = cleanWordHtml(joinedAct);
                    }
                    obj.put("actividad", joinedAct);
                    arr.put(obj);
                }
                f.conceptoSupervisor = arr.toString();
            } else {
                f.conceptoSupervisor = cleanWordHtml(r.getParameter("concepto_supervisor"));
            }
        }
        
        f.observacionesFinancieras = cleanWordHtml(r.getParameter("observaciones_financieras"));
        f.observacionesTecnicas = cleanWordHtml(r.getParameter("observaciones_tecnicas"));
        f.recomendaciones = cleanWordHtml(r.getParameter("recomendaciones"));
        f.fechaSuscripcion = r.getParameter("fecha_suscripcion");
        f.soportesJson = r.getParameter("soportes_json");
        
        // Manejo de radicacion
        String radicar = r.getParameter("radicar");
        if ("true".equals(radicar)) {
            f.estadoRadicacion = "RADICADA";
        } else {
            f.estadoRadicacion = null;
        }
        String idRevisor = r.getParameter("id_revisor_asignado");
        if (idRevisor != null && !idRevisor.trim().isEmpty()) {
            int parsedRevisorId = ParseUtils.parseInt(idRevisor);
            f.idRevisorAsignado = (parsedRevisorId > 0) ? parsedRevisorId : null;
        }
        
        return f;
    }

    public String cleanWordHtml(String html) {
        if (html == null || html.isEmpty()) return html;
        try {
            // Limpieza cruda antes de Jsoup para atributos mal formados sin comillas
            html = html.replaceAll("(?i)mso-[a-zA-Z0-9\\-]+:[^;\"'>]+;?", "");
            html = html.replaceAll("(?i)font-family:[^;\"'>]+;?", "");
            html = html.replaceAll("(?i)o:p", "span"); // Reemplazar tags <o:p> de word
            
            org.jsoup.nodes.Document docHtml = org.jsoup.Jsoup.parseBodyFragment(html);
            for (org.jsoup.nodes.Element e : docHtml.getAllElements()) {
                String style = e.attr("style");
                if (style != null && !style.isEmpty()) {
                    // Limpiar basura de Word de manera segura dentro del atributo
                    style = style.replaceAll("(?i)mso-[a-zA-Z0-9\\-]+:[^;]+;?", "");
                    style = style.replaceAll("(?i)font-family:[^;]+;?", "");
                    style = style.replaceAll("(?i)font-size:[^;]+;?", "");
                    style = style.replaceAll("(?i)line-height:[^;]+;?", "");
                    if (style.trim().isEmpty()) e.removeAttr("style");
                    else e.attr("style", style.trim());
                }
                e.removeAttr("class");
                e.removeAttr("lang");
            }
            // Remover comentarios HTML (basura de Word)
            String finalHtml = docHtml.body().html().replaceAll("(?s)<!--.*?-->", "");
            
            // Limpieza final cruda por si quedó basura textual
            finalHtml = finalHtml.replaceAll("(?i)mso-[a-zA-Z0-9\\-]+:[^;\"'>]+;?", "");
            finalHtml = finalHtml.replaceAll("(?i)font-family:[^;\"'>]+;?", "");
            finalHtml = finalHtml.replaceAll("(?i)font-size:[^;\"'>]+;?", "");
            finalHtml = finalHtml.replaceAll("(?i)line-height:[^;\"'>]+;?", "");
            
            // Eliminar tags span vacíos o basura que quedó
            finalHtml = finalHtml.replaceAll("(?i)<span[^>]*>\\s*</span>", "");
            
            return finalHtml;
        } catch (Exception ex) {
            return html;
        }
    }

    public void poblarTextosModificacion(Contrato contrato, InformeSupervision informe) {
        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "CO"));
        nf.setMaximumFractionDigits(0);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es", "CO"));
        
        String valTotal = (contrato.getValorTotalAdicion() != null) ? nf.format(contrato.getValorTotalAdicion()) : "$0";
        String valCuota = (contrato.getValorCuotaNumero() != null) ? nf.format(contrato.getValorCuotaNumero()) : "$0";
        String valTotalLetras = (contrato.getValorTotalAdicionLetras() != null) ? contrato.getValorTotalAdicionLetras().toUpperCase() : "";
        String valCuotasLetras = (contrato.getValorCuotaLetras() != null) ? contrato.getValorCuotaLetras().toLowerCase() : "";
        
        int numCuotas = contrato.getNumeroCuotasAdicion();
        String[] cuotasStr = {"cero (0)", "una (1)", "dos (2)", "tres (3)", "cuatro (4)", "cinco (5)", "seis (6)", "siete (7)", "ocho (8)", "nueve (9)", "diez (10)", "once (11)", "doce (12)"};
        String cuotasTxt = (numCuotas >= 0 && numCuotas <= 12) ? cuotasStr[numCuotas] : numCuotas + " (" + numCuotas + ")";
        
        String fechaMod = contrato.getFechaModificacion() != null ? sdf.format(contrato.getFechaModificacion()) : "XX de XXX de 202X";
        
        String fechaPror = "XX de XXX de 202X";
        if (contrato.getFechaTerminacion() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(contrato.getFechaTerminacion());
            cal.add(java.util.Calendar.MONTH, numCuotas);
            fechaPror = sdf.format(cal.getTime());
        }
        
        String numMod = contrato.getNumeroModificacion() != null && !contrato.getNumeroModificacion().trim().isEmpty() ? contrato.getNumeroModificacion() : "00X";
        String numContrato = contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : "XXX";
        if (contrato.getAnio() != null && contrato.getAnio() > 0) {
            numContrato += "-" + contrato.getAnio();
        }
        
        String valTotalMod = (contrato.getValorContratoMasAdicion() != null) ? nf.format(contrato.getValorContratoMasAdicion()) : "$0";
        String valTotalModLetras = (contrato.getValorContratoMasAdicionLetras() != null) ? contrato.getValorContratoMasAdicionLetras().toUpperCase() : "";
                
        String adicionTxt = String.format("Mediante Modificación No. %s del %s se adiciona la suma %s (%s).", 
                numMod, fechaMod, valTotalLetras, valTotal);
                
        String tipoContratoStr = contrato.getTipoContrato() != null && !contrato.getTipoContrato().trim().isEmpty() ? contrato.getTipoContrato() : "Prestación de servicios";
        
        String prorrogaTxt = String.format("Mediante Modificación No. %s del %s se prorroga el Contrato de %s %s hasta el %s.",
                numMod, fechaMod, tipoContratoStr, numContrato, fechaPror);
                
        String modificacionTxt = String.format("Modificación No. %s del %s mediante la cual las partes acordaron: PRORROGAR el Contrato de %s No. %s hasta el %s; y ADICIONAR el Contrato de %s No. %s por la suma de %s (%s). Dicha adición se pagará en %s cuotas iguales, cada una de ellas por valor de %s (%s).\n\nPor lo tanto, el valor total del contrato queda en la suma de %s (%s).",
                numMod,
                fechaMod,
                tipoContratoStr,
                numContrato, 
                fechaPror,
                tipoContratoStr,
                numContrato,
                valTotalLetras, valTotal, cuotasTxt,
                valCuotasLetras, valCuota,
                valTotalModLetras, valTotalMod);
                
        boolean adVacio = (informe.getAdiciones() == null || informe.getAdiciones().trim().isEmpty() || "N/A".equalsIgnoreCase(informe.getAdiciones().trim()));
        boolean proVacio = (informe.getProrrogas() == null || informe.getProrrogas().trim().isEmpty() || "N/A".equalsIgnoreCase(informe.getProrrogas().trim()));
        boolean modVacio = (informe.getModificaciones() == null || informe.getModificaciones().trim().isEmpty() || "N/A".equalsIgnoreCase(informe.getModificaciones().trim()));
        
        if (adVacio) informe.setAdiciones(adicionTxt);
        if (proVacio) informe.setProrrogas(prorrogaTxt);
        if (modVacio) informe.setModificaciones(modificacionTxt);
    }

}
