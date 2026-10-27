<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    // Recuperar observaciones_revision y estado_previo_devolucion directamente de la BD
    String obsRev = "";
    String estPrev = "";
    try {
        if (request.getAttribute("informe") != null) {
            com.combinacion.models.InformeSupervision inf = (com.combinacion.models.InformeSupervision)request.getAttribute("informe");
            if (inf.getId() != null && inf.getId() > 0) {
                try (java.sql.Connection conn = com.combinacion.util.DBConnection.getConnection();
                     java.sql.PreparedStatement ps = conn.prepareStatement("SELECT observaciones_revision, estado_previo_devolucion FROM informes_supervision WHERE id = ?")) {
                    ps.setInt(1, inf.getId());
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            obsRev = rs.getString(1);
                            estPrev = rs.getString(2);
                        }
                    }
                }
                if ((estPrev == null || estPrev.trim().isEmpty()) && "DEVUELTA".equals(inf.getEstadoRadicacion())) {
                    estPrev = new com.combinacion.dao.InformeSupervisionDAO().obtenerEstadoPrevioDevolucion(inf.getId());
                }
            }
        }
    } catch (Exception ignore) {}
    request.setAttribute("obsRevision", obsRev);
    request.setAttribute("estadoPrevioDevolucion", estPrev != null ? estPrev.trim() : "");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <c:set var="esAdminCuentas" value="${sessionScope.usuario.tienePermiso('ADMINISTRAR_CUENTAS') || sessionScope.usuario.tienePermiso('ADMINISTRAR_CUENTAS_EDITAR') || sessionScope.usuario.esAdministrador()}" />
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${action == 'view' ? 'Ver' : (action == 'update' ? 'Editar' : 'Crear')} Cuenta de cobro No. <fmt:formatNumber value="${not empty informe.numeroCuota ? informe.numeroCuota : (not empty siguienteCuota ? siguienteCuota : 1)}" pattern="000" /> - Gestión de Prestadores</title>
    <jsp:include page="inc/head_assets.jsp" />
    <style>
        :root {
            --primary-blue: #004884;
            --success-green: #198754;
            --light-bg: #f8f9fa;
        }
        .section-title {
            background-color: var(--light-bg);
            padding: 12px 15px;
            margin-top: 25px;
            margin-bottom: 20px;
            border-left: 5px solid var(--primary-blue);
            font-weight: 700;
            color: var(--primary-blue);
            border-radius: 0 4px 4px 0;
            text-transform: uppercase;
            font-size: 0.9rem;
        }
        .card { border-radius: 12px; }
        .nav-tabs .nav-link {
            font-weight: 600;
            color: #6c757d;
            border: none;
            padding: 12px 20px;
        }
        .nav-tabs .nav-link.active {
            color: var(--primary-blue);
            border-bottom: 3px solid var(--primary-blue);
            background: transparent;
        }
        .form-label { font-weight: 600; color: #495057; font-size: 0.9rem; }
        .form-control:focus, .form-select:focus {
            border-color: var(--primary-blue);
            box-shadow: 0 0 0 0.25rem rgba(0, 72, 132, 0.1);
        }
        .file-dragover {
            border: 2px dashed var(--primary-blue) !important;
            background-color: rgba(0, 72, 132, 0.05) !important;
            transform: scale(1.02);
            transition: all 0.2s ease;
        }
        /* Custom Monthpicker Widget */
        .month-picker-group {
            position: relative;
        }
        .month-picker-display {
            background-color: #fff !important;
            cursor: pointer;
        }
        .month-picker-display:disabled {
            background-color: #e9ecef !important;
            cursor: not-allowed;
        }
        .month-picker-popup {
            position: absolute;
            top: 100%;
            left: 0;
            z-index: 1055;
            min-width: 290px;
            background: #ffffff;
            border-radius: 10px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.18);
            border: 1px solid #ced4da;
            margin-top: 5px;
            padding: 12px;
            display: none;
        }
        .mp-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 12px;
            padding-bottom: 8px;
            border-bottom: 1px solid #e9ecef;
        }
        .mp-year {
            font-size: 1.1rem;
            font-weight: 700;
            color: var(--primary-blue);
        }
        .mp-month-btn {
            font-size: 0.85rem;
            font-weight: 500;
            padding: 8px 4px;
            border-radius: 6px;
            transition: all 0.15s ease;
        }
        .mp-month-btn:hover {
            background-color: #e7f1ff;
            color: var(--primary-blue);
            border-color: #004884;
        }
    </style>
</head>
<body class="bg-white d-flex flex-column min-vh-100">

    <jsp:include page="inc/navbar.jsp" />

    <div class="container-fluid mt-4 mb-5 flex-grow-1 px-4">
        <div class="row mb-3">
            <div class="col-12">
                <nav aria-label="breadcrumb">
    <ol class="breadcrumb breadcrumb-premium">
        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
        <li class="breadcrumb-item"><a href="informes${not empty modo ? '?modo='.concat(modo) : ''}"><i class="bi bi-wallet2 me-1"></i>Cuentas</a></li>
        <li class="breadcrumb-item active" aria-current="page">${action == 'view' ? 'Ver' : (action == 'update' ? 'Editar' : 'Crear')} Cuenta de cobro No. <fmt:formatNumber value="${not empty informe.numeroCuota ? informe.numeroCuota : (not empty siguienteCuota ? siguienteCuota : 1)}" pattern="000" /></li>
    </ol>
</nav>
            </div>
        </div>

        <div class="card border">
            <div class="card-body p-4">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2 class="fw-bold m-0 text-dark">
                        <i class="bi bi-file-earmark-check me-2 text-primary"></i>
                        ${action == 'view' ? 'Detalle de' : (action == 'update' ? 'Edición de' : 'Nueva')} Cuenta de cobro No. <fmt:formatNumber value="${not empty informe.numeroCuota ? informe.numeroCuota : (not empty siguienteCuota ? siguienteCuota : 1)}" pattern="000" />
                    </h2>
                    <span class="badge bg-primary px-3 py-2">${contrato.contratistaNombre}</span>
                </div>

                <form action="${pageContext.request.contextPath}/informes" method="POST" id="informeForm" class="needs-validation" enctype="multipart/form-data" novalidate>
                    <input type="hidden" name="action" value="${action}">
                    <input type="hidden" name="contrato_id" value="${contrato.id}">
                    <c:if test="${action == 'update'}">
                        <input type="hidden" name="id" value="${informe.id}">
                    </c:if>
                    <input type="hidden" name="soportes_json" id="soportes_json" value="${fn:escapeXml(not empty informe.soportesJson ? informe.soportesJson : (not empty soportesJsonPreCargados ? soportesJsonPreCargados : '{}'))}">
                    <input type="hidden" name="soportes_eliminados" id="soportes_eliminados" value="">
                    <input type="hidden" name="periodo_informe_fallback" value="${fn:escapeXml(informe.periodoInforme)}">
                    <input type="hidden" name="numero_cuota_fallback" value="${fn:escapeXml(informe.numeroCuota)}">

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger d-flex align-items-center mb-4" role="alert">
                            <i class="bi bi-exclamation-triangle-fill me-3 fs-4"></i>
                            <div>
                                <strong>Error:</strong> ${error}
                            </div>
                        </div>
                    </c:if>



                    <!-- Tabs responsivos: No hacen salto de linea en móviles, permiten scroll horizontal -->
                    <ul class="nav nav-tabs mb-4 flex-nowrap overflow-x-auto" id="informeTabs" role="tablist" style="white-space: nowrap; padding-bottom: 2px; -webkit-overflow-scrolling: touch;">
                        <li class="nav-item">
                            <button class="nav-link active" id="general-tab" data-bs-toggle="tab" data-bs-target="#general" type="button">General</button>
                        </li>
                        <li class="nav-item">
                            <button class="nav-link" id="juridico-tab" data-bs-toggle="tab" data-bs-target="#juridico" type="button">Jurídico</button>
                        </li>
                        <li class="nav-item">
                            <button class="nav-link" id="financiero-tab" data-bs-toggle="tab" data-bs-target="#financiero" type="button">Financiero</button>
                        </li>
                        <li class="nav-item">
                            <button class="nav-link" id="seguridad-tab" data-bs-toggle="tab" data-bs-target="#seguridad" type="button">Seguridad Social</button>
                        </li>
                        <li class="nav-item">
                            <button class="nav-link" id="tecnico-tab" data-bs-toggle="tab" data-bs-target="#tecnico" type="button">Técnico</button>
                        </li>
                        <li class="nav-item">
                            <button class="nav-link" id="soportes-tab" data-bs-toggle="tab" data-bs-target="#soportes" type="button">Documentos de soporte</button>
                        </li>
                    </ul>

                    <div class="tab-content" id="informeTabsContent">
                        <!-- Tab 1: General -->
                        <div class="tab-pane fade show active" id="general" role="tabpanel">
                            <div class="section-title">Aspectos Generales</div>
                            <div class="row g-3">
                                <div class="col-md-4">
                                    <label class="form-label">No. de Contrato</label>
                                    <input type="text" id="numero_contrato_display" class="form-control bg-light text-muted" value="${contrato.numeroContrato}${not empty contrato.anio and contrato.anio > 0 ? ' de ' : ''}${not empty contrato.anio and contrato.anio > 0 ? contrato.anio : ''}" readonly>
                                </div>
                                <div class="col-md-8">
                                    <label class="form-label">Objeto del Contrato</label>
                                    <textarea class="form-control bg-light text-muted" rows="2" readonly>${contrato.objeto}</textarea>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Periodo del Informe (Mes y Año)</label>
                                    <div class="input-group month-picker-group">
                                        <input type="text" class="form-control month-picker-display" id="display_periodo_informe" data-target="#periodo_informe" placeholder="Seleccione Mes y Año..." readonly required ${readonly ? 'disabled' : ''}>
                                        <button class="btn btn-outline-primary month-picker-btn" type="button" data-target="#periodo_informe" ${readonly ? 'disabled' : ''} title="Seleccionar Mes y Año">
                                            <i class="bi bi-calendar3"></i>
                                        </button>
                                    </div>
                                    <input type="hidden" name="periodo_informe" id="periodo_informe" value="${informe.periodoInforme}">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label d-flex justify-content-between align-items-center">
                                        <span>Supervisor asignado a este informe</span>
                                        <c:if test="${not empty informe.supervisorId && not empty contrato.supervisorId && informe.supervisorId != contrato.supervisorId}">
                                            <span class="badge bg-warning text-dark"><i class="bi bi-person-badge me-1"></i>Por Encargo</span>
                                        </c:if>
                                    </label>
                                    <c:choose>
                                        <c:when test="${not readonly}">
                                            <select class="form-select" name="supervisor_id" id="supervisor_id" required>
                                                <option value="" disabled ${empty informe.supervisorId && empty contrato.supervisorId ? 'selected' : ''}>-- Seleccione un Supervisor --</option>
                                                <c:forEach var="sup" items="${listaSupervisores}">
                                                    <c:set var="esSeleccionado" value="${(not empty informe.supervisorId && informe.supervisorId == sup.id) || (empty informe.supervisorId && contrato.supervisorId == sup.id)}" />
                                                    <option value="${sup.id}" ${esSeleccionado ? 'selected' : ''}>
                                                        ${sup.nombre}${not empty sup.cargo ? ' - '.concat(sup.cargo) : ''}${contrato.supervisorId == sup.id ? ' (Titular del Contrato)' : ''}
                                                    </option>
                                                </c:forEach>
                                            </select>
                                            <div class="form-text text-muted" style="font-size: 0.78rem;">
                                                Por defecto es el titular del contrato. Cámbialo si este periodo fue supervisado por encargo.
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <input type="text" class="form-control bg-light text-muted" value="${not empty informe.supervisor ? informe.supervisor.nombre : contrato.supervisor.nombre}" readonly>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Tipo de Informe</label>
                                    <select class="form-select" name="tipo_informe" id="tipo_informe" required ${readonly ? 'disabled' : ''}>
                                        <option value="PARCIAL" ${informe.tipoInforme == 'PARCIAL' ? 'selected' : ''}>INFORME PARCIAL</option>
                                        <option value="FINAL" ${informe.tipoInforme == 'FINAL' ? 'selected' : ''}>INFORME FINAL</option>
                                    </select>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Cuota Número</label>
                                    <c:choose>
                                        <c:when test="${not empty contrato.numCuotasNumero && contrato.numCuotasNumero > 0 && not readonly}">
                                            <c:set var="totalCuotas" value="${contrato.numCuotasNumero + contrato.numeroCuotasAdicion}" />
                                            <select class="form-select" name="numero_cuota" id="numero_cuota" required>
                                                <option value="" disabled ${empty informe.numeroCuota && empty siguienteCuota ? 'selected' : ''}>Seleccione...</option>
                                                <c:forEach var="i" begin="1" end="${totalCuotas}">
                                                    <option value="${i}" ${(not empty informe.numeroCuota && informe.numeroCuota == i) || (empty informe.id && siguienteCuota == i) ? 'selected' : ''}>Cuota ${i}</option>
                                                </c:forEach>
                                            </select>
                                        </c:when>
                                        <c:otherwise>
                                            <input type="text" class="form-control" name="numero_cuota" id="numero_cuota" value="${informe.numeroCuota}" placeholder="Ej: 1" required ${readonly ? 'readonly' : ''}>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label" title="Para la cuenta de cobro">Consecutivo</label>
                                    <input type="text" class="form-control" name="consecutivo_cobro" value="${informe.consecutivoCobro}" placeholder="Ej: 0411" <c:if test="${!contrato.facturadorElectronicoOConIva && contrato.ivaSiNo != 'SI' && contrato.ivaSiNo != 'Si' && contrato.ivaSiNo != 'si' && contrato.facturadorElectronico != 'SI' && contrato.facturadorElectronico != 'Si' && contrato.facturadorElectronico != 'si'}">required</c:if> ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Fecha de inicio</label>
                                    <input type="date" class="form-control" name="fecha_inicio_periodo" value="<fmt:formatDate value='${empty informe.id ? contrato.fechaEjecucion : informe.fechaInicioPeriodo}' pattern='yyyy-MM-dd'/>" required ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Fecha terminación</label>
                                    <input type="date" class="form-control" name="fecha_fin_periodo" value="<fmt:formatDate value='${empty informe.id ? contrato.fechaTerminacion : informe.fechaFinPeriodo}' pattern='yyyy-MM-dd'/>" required ${readonly ? 'readonly' : ''}>
                                </div>
                            </div>
                        </div>

                        <!-- Tab 2: Jurídico -->
                        <div class="tab-pane fade" id="juridico" role="tabpanel">
                            <c:set var="tieneAdicion" value="${contrato.tieneAdicion}" />
                            <c:set var="numCuotaParsed" value="${not empty informe.numeroCuota ? informe.numeroCuota : siguienteCuota}" />
                            <c:set var="mostrarTextosAuto" value="${tieneAdicion && contrato.numCuotasNumero > 0 && numCuotaParsed >= contrato.numCuotasNumero}" />
                            <!-- DEBUG: siguienteCuota=${siguienteCuota}, contrato.numCuotasNumero=${contrato.numCuotasNumero}, mostrarTextosAuto=${mostrarTextosAuto} -->
                            <div class="section-title">Informe Jurídico</div>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Modificación al contrato</label>
                                    <textarea class="form-control" name="modificaciones" rows="2" ${readonly ? 'readonly' : ''}>${informe != null && not empty informe.modificaciones && informe.modificaciones != 'N/A' ? informe.modificaciones : (mostrarTextosAuto && informeAuto != null && not empty informeAuto.modificaciones ? informeAuto.modificaciones : 'N/A')}</textarea>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Suspensión</label>
                                    <textarea class="form-control" name="suspensiones" rows="2" ${readonly ? 'readonly' : ''}>${empty informe.suspensiones ? 'N/A' : informe.suspensiones}</textarea>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Reanudación</label>
                                    <input type="text" class="form-control" name="reanudaciones" value="${empty informe.reanudaciones ? 'N/A' : informe.reanudaciones}" ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Cesión</label>
                                    <input type="text" class="form-control" name="cesiones" value="${empty informe.cesiones ? 'N/A' : informe.cesiones}" ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Terminación anticipada</label>
                                    <input type="text" class="form-control" name="terminacion_anticipada" value="${empty informe.terminacionAnticipada ? 'N/A' : informe.terminacionAnticipada}" ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Adición</label>
                                    <textarea class="form-control" name="adiciones" rows="2" ${readonly ? 'readonly' : ''}>${informe != null && not empty informe.adiciones && informe.adiciones != 'N/A' ? informe.adiciones : (mostrarTextosAuto && informeAuto != null && not empty informeAuto.adiciones ? informeAuto.adiciones : 'N/A')}</textarea>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Prórroga</label>
                                    <textarea class="form-control" name="prorrogas" rows="2" ${readonly ? 'readonly' : ''}>${informe != null && not empty informe.prorrogas && informe.prorrogas != 'N/A' ? informe.prorrogas : (mostrarTextosAuto && informeAuto != null && not empty informeAuto.prorrogas ? informeAuto.prorrogas : 'N/A')}</textarea>
                                </div>
                            </div>
                        </div>

                        <!-- Tab 3: Financiero -->
                        <div class="tab-pane fade" id="financiero" role="tabpanel">
                            <div class="section-title">Informe Contable y Financiero</div>
                            <div class="row g-3">
                                <div class="col-md-3">
                                    <label class="form-label">Valor Total del Contrato ($)</label>
                                    <input type="text" id="valor_total" class="form-control bg-light text-muted fw-bold money-mask" value="${contrato.valorVigente}" readonly>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Valor Cuota a Cancelar ($)</label>
                                    <input type="text" id="valor_cuota" class="form-control money-mask" name="valor_cuota_pagar" value="${not empty informe.valorCuotaPagar ? informe.valorCuotaPagar : contrato.valorCuotaNumero}" required ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Valor Acumulado Cancelado ($)</label>
                                    <input type="text" id="valor_acumulado" class="form-control bg-light text-muted money-mask" name="valor_acumulado_pagado" 
                                           value="${(empty informe.valorAccumuladoPagado || informe.valorAccumuladoPagado == 0) ? acumuladoPrevio : informe.valorAccumuladoPagado}" placeholder="0" readonly>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Saldo por Cancelar ($)</label>
                                    <input type="text" id="saldo_cancelar" class="form-control bg-light money-mask fw-bold" name="saldo_por_cancelar" value="${informe.saldoPorCancelar}" placeholder="0" readonly>
                                </div>
                            </div>
                            <div class="row mt-3">
                                <div class="col-md-12">
                                    <label class="form-label">Observaciones al informe financiero y contable</label>
                                    <textarea class="form-control" name="observaciones_financieras" id="observaciones_financieras" rows="2" placeholder="Observaciones adicionales..." ${readonly ? 'readonly' : ''}>${empty informe.observacionesFinancieras ? '' : informe.observacionesFinancieras}</textarea>
                                </div>
                            </div>
                        </div>

                        <!-- Tab 4: Seguridad Social -->
                        <div class="tab-pane fade" id="seguridad" role="tabpanel">
                            <div class="section-title">Información del Pago de Seguridad Social</div>

                            <!-- Alerta / Switch para Contratista Nuevo (Aplica sólo cuando cuota es 1) -->
                            <div class="row mb-3" id="container_contratista_nuevo" style="display: none;">
                                <div class="col-md-12">
                                    <div class="card border-primary border-opacity-25 bg-light p-3">
                                        <div class="form-check form-switch fs-6 mb-1">
                                            <input class="form-check-input" type="checkbox" role="switch" id="check_contratista_nuevo" name="es_contratista_nuevo" value="SI"
                                                   ${informe.esContratistaNuevo == 'SI' ? 'checked' : ''} ${readonly ? 'disabled' : ''}>
                                            <label class="form-check-label fw-bold text-primary" for="check_contratista_nuevo">
                                                <i class="bi bi-person-check-fill me-1"></i> ¿Es contratista nuevo? (Primera cuota - Acredita afiliaciones sin pago de planilla previa)
                                            </label>
                                        </div>
                                        <small class="text-muted ps-4">
                                            Aplica únicamente para la <strong>Cuota 1</strong>. Al activar esta opción, los campos de pago de planilla quedarán automáticamente en <strong>N/A</strong> y se habilitará el registro de las entidades de afiliación (EPS, Administradora de Pensión y ARL).
                                        </small>
                                    </div>
                                </div>
                            </div>

                            <!-- Bloque de Afiliaciones (EPS, AFP Pensión, ARL) para Contratista Nuevo -->
                            <div id="container_afiliaciones_nuevo" class="mb-3" style="display: none;">
                                <div class="card border-info bg-white shadow-sm p-3">
                                    <div class="d-flex align-items-center mb-2 text-info">
                                        <i class="bi bi-shield-check fs-5 me-2"></i>
                                        <span class="fw-bold fs-6">Entidades de Afiliación a Seguridad Social (Contratista Nuevo)</span>
                                    </div>
                                    <p class="text-muted small mb-3">Indique las entidades a las cuales se encuentra formalmente afiliado el contratista para la acreditación de esta primera cuota:</p>
                                    <div class="row g-3">
                                        <div class="col-md-4">
                                            <label class="form-label fw-bold">EPS a la que está afiliado</label>
                                            <input type="text" class="form-control" name="eps" id="eps" value="${informe.eps}" placeholder="Ej: Sanitas, Sura, Nueva EPS, Compensar..." ${readonly ? 'readonly' : ''}>
                                        </div>
                                        <div class="col-md-4">
                                            <label class="form-label fw-bold">Administradora de Pensión (AFP)</label>
                                            <input type="text" class="form-control" name="afp_pension" id="afp_pension" value="${informe.afpPension}" placeholder="Ej: Porvenir, Protección, Colpensiones, Skandia..." ${readonly ? 'readonly' : ''}>
                                        </div>
                                        <div class="col-md-4">
                                            <label class="form-label fw-bold">ARL a la que está afiliado</label>
                                            <input type="text" class="form-control" name="arl" id="arl" value="${informe.arl}" placeholder="Ej: Positiva, Sura, Seguros Bolívar, Axa Colpatria..." ${readonly ? 'readonly' : ''}>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Campos tradicionales de Planilla de Seguridad Social -->
                            <div id="container_campos_planilla">
                                <div class="row g-3">
                                    <div class="col-md-4">
                                        <label class="form-label">Número de Planilla</label>
                                        <input type="text" class="form-control" name="planilla_numero" id="planilla_numero" value="${informe.planillaNumero}" required ${readonly ? 'readonly' : ''}>
                                    </div>
                                    <div class="col-md-4">
                                        <label class="form-label">PIN / Autorización / Referencia</label>
                                        <input type="text" class="form-control" name="planilla_pin" id="planilla_pin" value="${informe.planillaPin}" required ${readonly ? 'readonly' : ''}>
                                    </div>
                                    <div class="col-md-4">
                                        <label class="form-label">Operador</label>
                                        <input type="text" class="form-control" name="planilla_operador" id="planilla_operador" value="${informe.planillaOperador}" placeholder="Ej: Aportes en Línea" required ${readonly ? 'readonly' : ''}>
                                    </div>
                                    <div class="col-md-6" id="col_planilla_fecha_pago">
                                        <label class="form-label">Fecha de Pago</label>
                                        <input type="date" class="form-control" name="planilla_fecha_pago" id="planilla_fecha_pago" value="<fmt:formatDate value='${informe.planillaFechaPago}' pattern='yyyy-MM-dd'/>" required ${readonly ? 'readonly' : ''}>
                                    </div>
                                    <div class="col-md-6" id="col_planilla_periodo">
                                        <label class="form-label">Periodo de Pago</label>
                                        <div class="input-group month-picker-group">
                                            <input type="text" class="form-control month-picker-display" id="display_planilla_periodo" data-target="#planilla_periodo" placeholder="Seleccione Mes y Año..." readonly required ${readonly ? 'disabled' : ''}>
                                            <button class="btn btn-outline-primary month-picker-btn" type="button" data-target="#planilla_periodo" ${readonly ? 'disabled' : ''} title="Seleccionar Mes y Año">
                                                <i class="bi bi-calendar3"></i>
                                            </button>
                                        </div>
                                        <input type="hidden" name="planilla_periodo" id="planilla_periodo" value="${informe.planillaPeriodo}">
                                    </div>
                                </div>
                            </div>
                            <div class="row mt-3" id="container_pago_seguridad" style="display: none;">
                                <div class="col-md-12">
                                    <label class="form-label">El pago de la seguridad social es: <span class="text-danger">*</span></label>
                                    <select class="form-select" name="pago_seguridad_social" id="pago_seguridad_social" ${readonly ? 'disabled' : ''}>
                                        <option value="">Seleccione...</option>
                                        <option value="Al Día" ${informe.pagoSeguridadSocial == 'Al Día' ? 'selected' : ''}>Al Día</option>
                                        <option value="Mes vencido" ${informe.pagoSeguridadSocial == 'Mes vencido' ? 'selected' : ''}>Mes vencido</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <!-- Tab 5: Técnico -->
                        <div class="tab-pane fade" id="tecnico" role="tabpanel">
                            <div class="section-title">Informe Técnico y Recomendaciones</div>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Recibo a Satisfacción de Servicios</label>
                                    <textarea class="form-control" name="recibo_satisfaccion" id="recibo_satisfaccion" rows="2" ${readonly ? 'readonly' : ''}><c:choose><c:when test="${informe.tipoInforme == 'FINAL' && (empty informe.reciboSatisfaccion || informe.reciboSatisfaccion == 'N/A')}">Con la firma del presente informe se deja constancia del recibo a satisfacción por parte del DISTRITO DE SANTIAGO DE CALI – DEPARTAMENTO ADMINISTRATIVO DE GESTIÓN JURÍDICA PÚBLICA de los servicios prestados pactados en el contrato N° ${contrato.numeroContrato}${not empty contrato.anio and contrato.anio > 0 ? ' de ' : ''}${not empty contrato.anio and contrato.anio > 0 ? contrato.anio : ''}.</c:when><c:otherwise>${empty informe.reciboSatisfaccion ? 'N/A' : informe.reciboSatisfaccion}</c:otherwise></c:choose></textarea>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Constancia de Paz y Salvo</label>
                                    <textarea class="form-control" name="constancia_paz_salvo" id="constancia_paz_salvo" rows="2" ${readonly ? 'readonly' : ''}><c:choose><c:when test="${informe.tipoInforme == 'FINAL' && (empty informe.constanciaPazSalvo || informe.constanciaPazSalvo == 'N/A')}">El contratista a la fecha del presente informe no posee a su cargo elementos devolutivos de propiedad del Distrito de Santiago de Cali, entregados por este organismo para el desempeño de sus actividades. Así mismo se encuentra a paz y salvo con el archivo de gestión documental y el sistema de gestión documental.</c:when><c:otherwise>${empty informe.constanciaPazSalvo ? 'N/A' : informe.constanciaPazSalvo}</c:otherwise></c:choose></textarea>
                                </div>
                                <div class="col-md-12">
                                    <label class="form-label">Concepto Supervisor (Obligaciones y Actividades)</label>
                                    <input type="hidden" name="obligaciones_count" value="${fn:length(listaObligaciones)}">
                                    <div class="table-responsive">
                                        <table class="table table-bordered align-middle">
                                            <thead class="table-light">
                                                <tr>
                                                    <th style="width: 50%">Obligaciones del Contratista</th>
                                                    <th style="width: 50%">Actividades (Use viñetas o guiones)</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="item" items="${listaObligaciones}" varStatus="status">
                                                    <tr>
                                                        <td class="bg-light">
                                                            <input type="hidden" name="obligacion_${status.index}" value="${item.obligacion}">
                                                            <small class="text-muted d-block" style="white-space: pre-wrap;">${item.obligacion}</small>
                                                        </td>
                                                        <td>
                                                            <textarea id="raw_actividad_${status.index}" style="display:none;">${item.actividad}</textarea>
                                                            <div id="actividades_container_${status.index}"></div>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                                <div class="col-md-12">
                                    <label class="form-label">Observaciones al Informe Técnico</label>
                                    <textarea class="form-control" name="observaciones_tecnicas" rows="2" placeholder="Observaciones adicionales..." required ${readonly ? 'readonly' : ''}>${empty informe.observacionesTecnicas ? 'N/A' : informe.observacionesTecnicas}</textarea>
                                </div>
                                <div class="col-md-12">
                                    <label class="form-label">Recomendaciones para el Contratista</label>
                                    <textarea class="form-control" name="recomendaciones" rows="2" ${readonly ? 'readonly' : ''}>${empty informe.id ? 'No se reportan recomendaciones para este periodo' : informe.recomendaciones}</textarea>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label fw-semibold">Fecha de transacción <span class="text-danger fw-bold req-asterisk">*</span></label>
                                    <c:choose>
                                        <c:when test="${not empty informe.fechaSuscripcion}">
                                            <input type="date" class="form-control" name="fecha_suscripcion" id="fecha_suscripcion" value="<fmt:formatDate value='${informe.fechaSuscripcion}' pattern='yyyy-MM-dd'/>" required ${readonly ? 'readonly' : ''}>
                                        </c:when>
                                        <c:otherwise>
                                            <input type="date" class="form-control" name="fecha_suscripcion" id="fecha_suscripcion" value="" required ${readonly ? 'readonly' : ''}>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>

                        <!-- Tab 6: Soportes -->
                        <div class="tab-pane fade" id="soportes" role="tabpanel">
                            <div class="section-title">Soportes y Evidencias (Documentos PDF)</div>
                            <div class="alert alert-info">
                                <i class="bi bi-info-circle-fill me-2"></i> Los documentos requeridos cambian dependiendo de si es la Cuota 1 o una cuota posterior.
                            </div>
                            <div class="row g-3">
                                <c:if test="${contrato.facturadorElectronicoOConIva || contrato.ivaSiNo == 'SI' || contrato.ivaSiNo == 'Si' || contrato.ivaSiNo == 'si' || contrato.facturadorElectronico == 'SI' || contrato.facturadorElectronico == 'Si' || contrato.facturadorElectronico == 'si'}">
                                    <div class="col-md-12 mb-2">
                                        <div class="alert alert-warning mb-0 border-start border-warning border-4 shadow-sm">
                                            <i class="bi bi-exclamation-triangle-fill me-2 text-warning fs-5"></i>
                                            <strong>Atención:</strong> El contratista es <strong><c:choose><c:when test="${contrato.ivaSiNo == 'SI' || contrato.ivaSiNo == 'Si' || contrato.ivaSiNo == 'si'}">Responsable de IVA</c:when><c:otherwise>Facturador Electrónico</c:otherwise></c:choose></strong>. En lugar de generar el formato de cuenta de cobro en Excel (DS), debe cargar su factura electrónica a continuación.
                                        </div>
                                    </div>
                                    <div class="col-md-6 req-cuota-todas">
                                        <label class="form-label text-primary fw-bold"><i class="bi bi-receipt"></i> Factura Electrónica (Reemplaza Cuenta de Cobro) <span class="text-danger">*</span></label>
                                        <input type="file" class="form-control border-primary" name="file_factura" accept="application/pdf, text/xml" ${readonly ? 'disabled' : ''}>
                                    </div>
                                </c:if>
                                <div class="col-md-6 req-cuota-1">
                                    <label class="form-label">RUT <span class="text-danger ast-cuota-1" style="display:none;">*</span></label>
                                    <input type="file" class="form-control" name="file_rut" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6 req-cuota-1">
                                    <label class="form-label">Cédula <span class="text-danger ast-cuota-1" style="display:none;">*</span></label>
                                    <input type="file" class="form-control" name="file_cedula" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6 req-cuota-1">
                                    <label class="form-label">Contrato Secop II <span class="text-danger ast-cuota-1" style="display:none;">*</span></label>
                                    <input type="file" class="form-control" name="file_secop" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6 req-cuota-1">
                                    <label class="form-label">Certificación Corrección Monetaria</label>
                                    <input type="file" class="form-control" name="file_correccion_monetaria" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6 req-cuota-1">
                                    <label class="form-label">Certificado Medicina Prepagada</label>
                                    <input type="file" class="form-control" name="file_medicina_prepagada" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6 req-cuota-1">
                                    <label class="form-label">Certificado Dependientes</label>
                                    <input type="file" class="form-control" name="file_certificado_dependientes" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6 req-cuota-1">
                                    <label class="form-label">Ficha Técnica <span class="text-danger ast-cuota-1" style="display:none;">*</span></label>
                                    <input type="file" class="form-control" name="file_ficha_tecnica" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6 req-cuota-todas">
                                    <label class="form-label">Seguridad Social <span class="text-danger">*</span></label>
                                    <input type="file" class="form-control" name="file_seguridad_social" id="file_seguridad_social" accept="application/pdf" required ${readonly ? 'disabled' : ''}>
                                </div>
                                 <div class="col-md-6 req-cuota-todas">
                                    <label class="form-label">RPC (Registro Presupuestal) <span class="text-danger ast-cuota-1" style="display:none;">*</span></label>
                                    <input type="file" class="form-control" name="file_rpc" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6 req-cuota-adicion-only" style="display: none;">
                                    <label class="form-label text-primary fw-bold">Modificación No 001</label>
                                    <input type="file" class="form-control border-primary" name="file_modificacion" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Paz y salvo Orfeo (Opcional)</label>
                                    <input type="file" class="form-control" name="file_paz_salvo_orfeo" accept="application/pdf, image/*" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Paz y salvo Gestión de Procesos (Opcional)</label>
                                    <input type="file" class="form-control" name="file_paz_salvo_procesos" accept="application/pdf, image/*" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Paz y salvo Creaciones (Opcional)</label>
                                    <input type="file" class="form-control" name="file_paz_salvo_creaciones" accept="application/pdf, image/*" ${readonly ? 'disabled' : ''}>
                                </div>
                            </div>
                        </div>

                    </div>

                    
                    <!-- Sección de Radicación o Asignación -->
                    <c:set var="esBorradorDevuelta" value="${empty informe.estadoRadicacion || informe.estadoRadicacion == 'BORRADOR' || informe.estadoRadicacion == 'DEVUELTA'}" />
                    <c:set var="esDevuelta" value="${informe.estadoRadicacion == 'DEVUELTA'}" />
                    
                    <c:if test="${not readonly && (esBorradorDevuelta || esAdminCuentas)}">
                        <div class="card mt-4 border-primary bg-light">
                            <div class="card-body">
                                <c:choose>
                                    <c:when test="${empty informe.id}">
                                        <h5 class="card-title text-primary"><i class="bi bi-info-circle"></i> Radicación</h5>
                                        <p class="card-text text-muted">Debes **Guardar el Informe** por primera vez antes de poder seleccionar un revisor y radicar la cuenta de cobro.</p>
                                    </c:when>
                                    <c:when test="${esDevuelta}">
                                        <c:choose>
                                            <c:when test="${estadoPrevioDevolucion == 'EN REVISION SUPERVISOR'}">
                                                <h5 class="card-title text-success"><i class="bi bi-arrow-repeat me-1"></i> Reingreso Directo a Supervisión</h5>
                                                <div class="alert alert-success d-flex align-items-center mb-0 py-3 shadow-sm border-success">
                                                    <i class="bi bi-person-check-fill fs-2 text-success me-3"></i>
                                                    <div>
                                                        <strong class="d-block mb-1">Cuenta devuelta por el Supervisor</strong>
                                                        <span class="small text-muted">Una vez guarde las correcciones solicitadas y haga clic en <strong>"Volver a Radicar (Enviar a Supervisión)"</strong>, la cuenta regresará <strong>directamente al Supervisor</strong> para su aprobación definitiva, sin requerir reasignar revisor ni repetir etapas previas.</span>
                                                    </div>
                                                </div>
                                                <input type="hidden" name="id_revisor_asignado" value="${not empty informe.idRevisorAsignado ? informe.idRevisorAsignado : 0}">
                                            </c:when>
                                            <c:when test="${estadoPrevioDevolucion == 'EN REVISION CONTRATACION'}">
                                                <h5 class="card-title text-primary"><i class="bi bi-arrow-repeat me-1"></i> Reingreso Directo a Contratación</h5>
                                                <div class="alert alert-primary d-flex align-items-center mb-0 py-3 shadow-sm border-primary">
                                                    <i class="bi bi-building-check fs-2 text-primary me-3"></i>
                                                    <div>
                                                        <strong class="d-block mb-1">Cuenta devuelta por Contratación</strong>
                                                        <span class="small text-muted">Una vez guarde las correcciones solicitadas y haga clic en <strong>"Volver a Radicar (Enviar a Contratación)"</strong>, la cuenta regresará <strong>directamente a Contratación</strong> sin reiniciar el flujo inicial.</span>
                                                    </div>
                                                </div>
                                                <input type="hidden" name="id_revisor_asignado" value="${not empty informe.idRevisorAsignado ? informe.idRevisorAsignado : 0}">
                                            </c:when>
                                            <c:otherwise>
                                                <h5 class="card-title text-primary"><i class="bi bi-send-check"></i> Radicar Cuenta de Cobro</h5>
                                                <p class="card-text text-muted small">Esta cuenta fue devuelta en etapa de revisión. Verifique el revisor encargado y haga clic en Volver a Radicar.</p>
                                                <div class="row align-items-center">
                                                    <div class="col-md-6">
                                                        <label class="form-label">Asignar a Revisor:</label>
                                                        <select class="form-select" name="id_revisor_asignado" id="revisor_select" required>
                                                            <option value="">-- Seleccione un Revisor --</option>
                                                            <option value="0" ${informe.idRevisorAsignado != null && informe.idRevisorAsignado == 0 ? 'selected' : ''}>-- Sin Revisor (Pasar directo a Contratación) --</option>
                                                            <c:forEach var="rev" items="${listaRevisores}">
                                                                <option value="${rev.id}" ${informe.idRevisorAsignado == rev.id ? 'selected' : ''}>${rev.nombreCompleto}</option>
                                                            </c:forEach>
                                                        </select>
                                                    </div>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                        
                                        <!-- Campo de aclaraciones del contratista al volver a radicar una cuenta devuelta -->
                                        <div class="mt-3 pt-3 border-top">
                                            <label for="observaciones_devolucion" class="form-label fw-bold text-dark">
                                                <i class="bi bi-chat-left-text text-primary me-1"></i>Aclaraciones / Observaciones sobre las correcciones realizadas <span class="badge bg-light text-secondary border fw-normal">Opcional</span>:
                                            </label>
                                            <textarea class="form-control" name="observaciones_devolucion" id="observaciones_devolucion" rows="3" placeholder="Detalla aquí qué subsanaste o qué aclaraciones deseas remitir a quien revise tu cuenta nuevamente (ej: se adjuntó la planilla corregida, se modificó la actividad 2, etc.)...">${fn:escapeXml(not empty informe.observacionesContratista ? informe.observacionesContratista : '')}</textarea>
                                            <div class="form-text text-muted">
                                                <i class="bi bi-info-circle me-1"></i>Esta aclaración quedará registrada en el <strong>Historial de Radicación y Revisiones</strong> para conocimiento del revisor o supervisor al reingresar.
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:when test="${esBorradorDevuelta}">
                                        <h5 class="card-title text-primary"><i class="bi bi-send-check"></i> Radicar Cuenta de Cobro</h5>
                                        <p class="card-text text-muted small">Seleccione la persona encargada de revisar su cuenta y haga clic en Radicar.</p>
                                        <div class="row align-items-center">
                                            <div class="col-md-6">
                                                <label class="form-label">Asignar a Revisor:</label>
                                                <select class="form-select" name="id_revisor_asignado" id="revisor_select" required>
                                                    <option value="">-- Seleccione un Revisor --</option>
                                                    <option value="0" ${informe.idRevisorAsignado != null && informe.idRevisorAsignado == 0 ? 'selected' : ''}>-- Sin Revisor (Pasar directo a Contratación) --</option>
                                                    <c:forEach var="rev" items="${listaRevisores}">
                                                        <option value="${rev.id}" ${informe.idRevisorAsignado == rev.id ? 'selected' : ''}>${rev.nombreCompleto}</option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <h5 class="card-title text-primary"><i class="bi bi-person-gear"></i> Asignar / Cambiar Revisor</h5>
                                        <p class="card-text text-muted small">Como administrador, puedes reasignar esta cuenta a otro revisor sin cambiar su estado.</p>
                                        <div class="row align-items-center">
                                            <div class="col-md-6">
                                                <label class="form-label">Asignar a Revisor:</label>
                                                <select class="form-select" name="id_revisor_asignado" id="revisor_select" required>
                                                    <option value="">-- Seleccione un Revisor --</option>
                                                    <option value="0" ${informe.idRevisorAsignado != null && informe.idRevisorAsignado == 0 ? 'selected' : ''}>-- Sin Revisor (Pasar directo a Contratación) --</option>
                                                    <c:forEach var="rev" items="${listaRevisores}">
                                                        <option value="${rev.id}" ${informe.idRevisorAsignado == rev.id ? 'selected' : ''}>${rev.nombreCompleto}</option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                                <input type="hidden" name="radicar" id="radicar_input" value="false">
                            </div>
                        </div>
                    </c:if>
                    
                    <c:if test="${not empty informe.estadoRadicacion}">
                        <div class="alert alert-info mt-4">
                            <strong>Estado actual de la cuenta:</strong> ${informe.estadoRadicacion}
                            <c:if test="${not empty informe.idRevisorAsignado}">
                                <c:choose>
                                    <c:when test="${informe.idRevisorAsignado == 0}">
                                        <br><small><i class="bi bi-person-badge"></i> Revisor asignado: <strong>Contratación (Revisión Directa)</strong></small>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="nombreRevisor" value="ID: ${informe.idRevisorAsignado}" />
                                        <c:forEach var="r" items="${listaRevisores}">
                                            <c:if test="${r.id == informe.idRevisorAsignado}">
                                                <c:set var="nombreRevisor" value="${r.nombreCompleto}" />
                                            </c:if>
                                        </c:forEach>
                                        <br><small><i class="bi bi-person-badge"></i> Revisor asignado: <strong>${nombreRevisor}</strong></small>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </div>
                    </c:if>
                    
                    <!-- Sección de Documentos Generados (solo si ya existe el informe) -->
                    <c:if test="${not empty informe.id}">
                        <div class="card mt-4 border-success bg-light shadow-sm">
                            <div class="card-body">
                                <h5 class="card-title text-success fw-bold"><i class="bi bi-file-earmark-check"></i> Documentos Generados por el Sistema</h5>
                                <p class="card-text text-muted small">Descargue o previsualice los documentos generados automáticamente con la información actual de esta cuenta.</p>
                                <div class="d-flex gap-2 flex-wrap">
                                    <c:if test="${!contrato.facturadorElectronicoOConIva && contrato.ivaSiNo != 'SI' && contrato.ivaSiNo != 'Si' && contrato.ivaSiNo != 'si' && contrato.facturadorElectronico != 'SI' && contrato.facturadorElectronico != 'Si' && contrato.facturadorElectronico != 'si'}">
                                        <a href="${pageContext.request.contextPath}/informes?action=descargar_doc&tipo=ds&id=${informe.id}" class="btn btn-outline-success" target="_blank">
                                            <i class="bi bi-file-earmark-excel"></i> Equivalente DS (Excel)
                                        </a>
                                    </c:if>
                                    <a href="${pageContext.request.contextPath}/informes?action=descargar_doc&tipo=supervision&id=${informe.id}" class="btn btn-outline-primary" target="_blank">
                                        <i class="bi bi-file-earmark-word"></i> Informe de Supervisión (Word)
                                    </a>
                                    <a href="${pageContext.request.contextPath}/informes?action=descargar_doc&tipo=gestion&id=${informe.id}" class="btn btn-outline-primary" target="_blank">
                                        <i class="bi bi-file-earmark-word"></i> Informe de Gestión (Word)
                                    </a>
                                </div>
                            </div>
                        </div>
                    </c:if>
                    
                    <c:if test="${not empty listaHistorial}">
                        <div class="card mt-3 shadow-sm border-0 border-start border-primary border-4">
                            <div class="card-body p-4">
                                <h5 class="card-title fw-bold mb-3 text-primary"><i class="bi bi-clock-history me-2"></i>Historial de Radicación y Revisiones</h5>
                                <div class="table-responsive">
                                    <table id="historialTable" class="table table-sm table-hover table-striped mb-0 text-dark w-100">
                                        <thead class="table-dark">
                                            <tr>
                                                <th style="width: 20%;"><i class="bi bi-calendar3"></i> Fecha</th>
                                                <th style="width: 20%;"><i class="bi bi-person"></i> Usuario</th>
                                                <th style="width: 20%;"><i class="bi bi-arrow-right-circle"></i> Acción</th>
                                                <th style="width: 40%;"><i class="bi bi-chat-text"></i> Observación</th>
                                            </tr>
                                        </thead>
                                    <tbody>
                                        <c:forEach var="hr" items="${listaHistorial}">
                                            <tr>
                                                <td class="align-middle fw-semibold" data-order="${hr.fechaCambio != null ? hr.fechaCambio.time : 0}">
                                                    <fmt:formatDate value="${hr.fechaCambio}" pattern="dd/MM/yyyy hh:mm a" />
                                                </td>
                                                <td class="align-middle">${fn:escapeXml(not empty hr.nombreUsuarioCambio ? hr.nombreUsuarioCambio : 'Sistema')}</td>
                                                <td class="align-middle">
                                                    <c:choose>
                                                        <c:when test="${hr.estadoNuevo == 'RADICADA'}"><span class="badge bg-primary">RADICADA</span></c:when>
                                                        <c:when test="${hr.estadoNuevo == 'DEVUELTA'}"><span class="badge bg-danger">DEVUELTA</span></c:when>
                                                        <c:when test="${hr.estadoNuevo == 'VISTO BUENO CONTRATACION'}"><span class="badge bg-success bg-opacity-75 text-white">VISTO BUENO (CONTRATACIÓN)</span></c:when>
                                                        <c:when test="${hr.estadoNuevo == 'EN REVISION CONTRATACION'}"><span class="badge bg-warning text-dark">EN REVISIÓN (CONTRATACIÓN)</span></c:when>
                                                        <c:when test="${hr.estadoNuevo == 'EN REVISION SUPERVISOR'}"><span class="badge bg-info text-dark">EN REVISIÓN (SUPERVISIÓN)</span></c:when>
                                                        <c:when test="${hr.estadoNuevo == 'EN REVISION FINAL'}"><span class="badge bg-info text-dark">EN REVISIÓN FINAL</span></c:when>
                                                        <c:when test="${hr.estadoNuevo == 'APROBADA PARA IMPRESION' || hr.estadoNuevo == 'APROBADA'}"><span class="badge bg-success">APROBADA</span></c:when>
                                                        <c:otherwise><span class="badge bg-secondary">${fn:escapeXml(hr.estadoNuevo)}</span></c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="align-middle" style="white-space: pre-wrap;">${fn:escapeXml(hr.observaciones)}</td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                            <c:if test="${informe.estadoRadicacion == 'DEVUELTA'}">
                                <hr class="border-primary">
                                <c:choose>
                                    <c:when test="${estadoPrevioDevolucion == 'EN REVISION SUPERVISOR'}">
                                        <small class="mb-0 fw-bold text-danger"><i class="bi bi-info-circle me-1"></i>La cuenta se encuentra DEVUELTA por el Supervisor. Corrige los detalles mencionados; al volver a radicarla, regresará directamente al Supervisor para su aprobación.</small>
                                    </c:when>
                                    <c:when test="${estadoPrevioDevolucion == 'EN REVISION CONTRATACION'}">
                                        <small class="mb-0 fw-bold text-danger"><i class="bi bi-info-circle me-1"></i>La cuenta se encuentra DEVUELTA por Contratación. Corrige los detalles mencionados; al volver a radicarla, regresará directamente a Contratación.</small>
                                    </c:when>
                                    <c:otherwise>
                                        <small class="mb-0 fw-bold text-danger"><i class="bi bi-info-circle me-1"></i>La cuenta se encuentra DEVUELTA. Por favor corrige los detalles mencionados y vuelve a radicarla.</small>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                            </div>
                        </div>
                    </c:if>
                    
                    <div class="mt-5 pt-3 border-top d-flex justify-content-end gap-2" id="contenedorBotonesAccion">
                        <a href="informes${not empty modo ? '?modo='.concat(modo) : ''}" id="btnVolverForm" class="btn btn-secondary px-4 shadow-sm" onclick="if (verificarBloqueoRevision(event, 'salir o volver a la lista', function() { window.location.href = document.getElementById('btnVolverForm').getAttribute('href'); })) return false;">
                            <i class="bi bi-arrow-left me-2"></i>Volver
                        </a>
                        <c:if test="${not empty informe.id and esAdminCuentas}">
                            <button type="button" id="btnSincronizarDriveForm" class="btn btn-outline-success px-4 shadow-sm" title="Regenerar y subir los formatos actualizados (Word, Excel y PDF) a Google Drive" onclick="if (verificarBloqueoRevision(event, 'sincronizar con Drive', function() { sincronizarDriveConConfirmacion(${informe.id}, '${not empty modo ? modo : ''}', false); })) return false; sincronizarDriveConConfirmacion(${informe.id}, '${not empty modo ? modo : ''}', false)">
                                <i class="bi bi-cloud-arrow-up me-2"></i>Sincronizar Drive
                            </button>
                        </c:if>
                        
                        <%-- Lógica del botón Revisar (Idéntica a la de la lista) --%>
                        <c:set var="esAdmin" value="${sessionScope.usuario != null and (sessionScope.usuario.esAdministrador() or sessionScope.usuario.tienePermiso('ADMINISTRAR_CUENTAS_EDITAR') or sessionScope.usuario.tienePermiso('ADMINISTRAR_CUENTAS'))}" />
                        <c:set var="esRevisorAsignado" value="${sessionScope.usuario != null and informe.idRevisorAsignado == sessionScope.usuario.id}" />
                        <c:set var="sinRevisor" value="${empty informe.idRevisorAsignado or informe.idRevisorAsignado == 0}" />
                        
                        <c:set var="userCedClean" value="${fn:replace(fn:replace(sessionScope.usuario.cedula, '.', ''), ' ', '')}" />
                        <c:set var="supCedClean" value="${fn:replace(fn:replace(not empty informe.supervisor ? informe.supervisor.cedula : contrato.supervisor.cedula, '.', ''), ' ', '')}" />
                        <c:set var="esSupervisorContrato" value="${not empty userCedClean and not empty supCedClean and userCedClean == supCedClean}" />
                        
                        <c:set var="mostrarBtnRevisar" value="false" />
                        <c:set var="tipoRolModal" value="" />
                        
                        <%-- 1. Si es el Revisor asignado --%>
                        <c:if test="${esRevisorAsignado and (informe.estadoRadicacion == 'RADICADA' or informe.estadoRadicacion == 'VISTO BUENO CONTRATACION')}">
                            <c:set var="mostrarBtnRevisar" value="true" />
                            <c:choose>
                                <c:when test="${informe.estadoRadicacion == 'VISTO BUENO CONTRATACION'}">
                                    <c:set var="tipoRolModal" value="revisor_post_contratacion" />
                                </c:when>
                                <c:otherwise>
                                    <c:set var="tipoRolModal" value="revisor" />
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <%-- 2. Si es Administrador / Contratación --%>
                        <c:if test="${esAdmin}">
                            <c:choose>
                                <c:when test="${informe.estadoRadicacion == 'EN REVISION CONTRATACION' or informe.estadoRadicacion == 'EN REVISION FINAL'}">
                                    <c:set var="mostrarBtnRevisar" value="true" />
                                    <c:set var="tipoRolModal" value="contratacion_con_revisor" />
                                </c:when>
                                <c:when test="${informe.estadoRadicacion == 'RADICADA' and sinRevisor}">
                                    <c:set var="mostrarBtnRevisar" value="true" />
                                    <c:set var="tipoRolModal" value="contratacion_sin_revisor" />
                                </c:when>
                                <c:when test="${informe.estadoRadicacion == 'EN REVISION SUPERVISOR'}">
                                    <c:set var="mostrarBtnRevisar" value="true" />
                                    <c:set var="tipoRolModal" value="supervisor" />
                                </c:when>
                                <c:when test="${informe.estadoRadicacion == 'VISTO BUENO CONTRATACION'}">
                                    <c:set var="mostrarBtnRevisar" value="true" />
                                    <c:set var="tipoRolModal" value="revisor_post_contratacion" />
                                </c:when>
                            </c:choose>
                        </c:if>
                        <%-- 3. Si es el Supervisor del contrato --%>
                        <c:if test="${!esAdmin and esSupervisorContrato and informe.estadoRadicacion == 'EN REVISION SUPERVISOR'}">
                            <c:set var="mostrarBtnRevisar" value="true" />
                            <c:set var="tipoRolModal" value="supervisor" />
                        </c:if>
                        
                        <c:if test="${mostrarBtnRevisar and not empty informe.id}">
                            <button type="button" id="btnRevisarForm" class="btn btn-outline-primary px-4 shadow-sm" onclick="manejarClickBotonRevisar(${informe.id}, '${informe.estadoRadicacion}', '${tipoRolModal}')">
                                <i class="bi bi-check2-square me-2"></i>Revisar
                            </button>
                        </c:if>
                        
                        <c:if test="${not readonly}">
                            <button type="submit" id="btnGuardarForm" class="btn btn-success px-5 fw-bold shadow-sm" onclick="if (verificarBloqueoRevision(event, 'guardar o actualizar el informe', function() { document.getElementById('radicar_input').value='false'; document.getElementById('informeForm').submit(); })) return false; document.getElementById('radicar_input').value='false'; return true;">
                                <i class="bi bi-save me-2"></i>${action == 'update' ? 'Actualizar Informe' : 'Guardar Informe'}
                            </button>
                            <c:if test="${empty informe.estadoRadicacion || informe.estadoRadicacion == 'BORRADOR' || informe.estadoRadicacion == 'DEVUELTA'}">
                                <c:choose>
                                    <c:when test="${empty informe.id}">
                                        <button type="button" id="btnRadicarForm" class="btn btn-secondary px-5 fw-bold shadow-sm" disabled title="Guarde el informe primero para poder radicarlo">
                                            <i class="bi bi-send-fill me-2"></i>Radicar Cuenta
                                        </button>
                                    </c:when>
                                    <c:when test="${esDevuelta and estadoPrevioDevolucion == 'EN REVISION SUPERVISOR'}">
                                        <button type="submit" id="btnRadicarForm" class="btn btn-success px-5 fw-bold shadow-sm" onclick="if (verificarBloqueoRevision(event, 'radicar la cuenta', function() { if (setRadicarDevuelta()) document.getElementById('informeForm').submit(); })) return false; return setRadicarDevuelta()">
                                            <i class="bi bi-send-check me-2"></i>Volver a Radicar (Enviar a Supervisión)
                                        </button>
                                    </c:when>
                                    <c:when test="${esDevuelta and estadoPrevioDevolucion == 'EN REVISION CONTRATACION'}">
                                        <button type="submit" id="btnRadicarForm" class="btn btn-primary px-5 fw-bold shadow-sm" onclick="if (verificarBloqueoRevision(event, 'radicar la cuenta', function() { if (setRadicarDevuelta()) document.getElementById('informeForm').submit(); })) return false; return setRadicarDevuelta()">
                                            <i class="bi bi-send-check me-2"></i>Volver a Radicar (Enviar a Contratación)
                                        </button>
                                    </c:when>
                                    <c:when test="${esDevuelta}">
                                        <button type="submit" id="btnRadicarForm" class="btn btn-primary px-5 fw-bold shadow-sm" onclick="if (verificarBloqueoRevision(event, 'radicar la cuenta', function() { if (setRadicar()) document.getElementById('informeForm').submit(); })) return false; return setRadicar()">
                                            <i class="bi bi-send-fill me-2"></i>Volver a Radicar Cuenta
                                        </button>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="submit" id="btnRadicarForm" class="btn btn-primary px-5 fw-bold shadow-sm" onclick="if (verificarBloqueoRevision(event, 'radicar la cuenta', function() { if (setRadicar()) document.getElementById('informeForm').submit(); })) return false; return setRadicar()">
                                            <i class="bi bi-send-fill me-2"></i>Radicar Cuenta
                                        </button>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </c:if>
                        <%-- El botón de imprimir ha sido removido a petición del usuario --%>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <jsp:include page="inc/footer.jsp" />

    <!-- Scripts -->
    <jsp:include page="inc/scripts_assets.jsp" />
    <script>
        $(document).ready(function() {
            <c:if test="${not empty sessionScope.successMessage}">
                Swal.fire({
                    icon: 'success',
                    title: '¡Guardado!',
                    text: '${fn:escapeXml(sessionScope.successMessage)}',
                    timer: 3500,
                    showConfirmButton: false,
                    timerProgressBar: true
                });
                <c:remove var="successMessage" scope="session" />
            </c:if>
            <c:if test="${not empty sessionScope.errorMessage}">
                Swal.fire({
                    icon: 'error',
                    title: 'Atención',
                    text: '${fn:escapeXml(sessionScope.errorMessage)}'
                });
                <c:remove var="errorMessage" scope="session" />
            </c:if>
            <c:if test="${not empty requestScope.errorMessage}">
                Swal.fire({
                    icon: 'error',
                    title: 'Atención',
                    text: '${fn:escapeXml(requestScope.errorMessage)}'
                });
            </c:if>

            // Agregar asterisco rojo a los labels de campos requeridos
            $('input[required], select[required], textarea[required]').each(function() {
                var label = $(this).siblings('.form-label');
                if (label.length === 0) {
                    label = $(this).closest('div').find('.form-label').first();
                }
                if (label.length && label.find('.req-asterisk').length === 0) {
                    label.append(' <span class="text-danger req-asterisk" title="Requerido para radicar">*</span>');
                }
            });
            // Activar pestañas con clic
            $('#informeTabs button').on('click', function (e) {
                e.preventDefault();
                $(this).tab('show');
            });

            // Inicializar Monthpicker visual bloqueado
            initCustomMonthPicker();

            function initCustomMonthPicker() {
                var mesesNombres = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
                var mesesCortos = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];

                $('.month-picker-group').each(function() {
                    var $group = $(this);
                    var $display = $group.find('.month-picker-display');
                    var targetSelector = $display.data('target');
                    var $hidden = $(targetSelector);

                    function updateDisplay() {
                        var val = $hidden.val();
                        if (val && val.indexOf('-') !== -1) {
                            var parts = val.split('-');
                            if (parts.length === 2) {
                                var y = parseInt(parts[0], 10);
                                var m = parseInt(parts[1], 10) - 1;
                                if (!isNaN(y) && m >= 0 && m <= 11) {
                                    $display.val(mesesNombres[m] + ' de ' + y);
                                    return;
                                }
                            }
                        }
                        $display.val(val ? val : '');
                    }
                    updateDisplay();
                    $hidden.on('change', updateDisplay);

                    if ($display.is(':disabled') || (typeof isReadonly !== 'undefined' && isReadonly)) {
                        return;
                    }

                    var $popup = $group.find('.month-picker-popup');
                    if ($popup.length === 0) {
                        $popup = $(
                            '<div class="month-picker-popup">' +
                                '<div class="mp-header">' +
                                    '<button type="button" class="btn btn-sm btn-outline-secondary mp-btn-prev py-0 px-2" title="Año anterior"><i class="bi bi-chevron-left"></i></button>' +
                                    '<span class="mp-year"></span>' +
                                    '<button type="button" class="btn btn-sm btn-outline-secondary mp-btn-next py-0 px-2" title="Año siguiente"><i class="bi bi-chevron-right"></i></button>' +
                                '</div>' +
                                '<div class="row g-2 text-center mp-grid"></div>' +
                            '</div>'
                        );
                        $group.append($popup);
                    }

                    var viewingYear = new Date().getFullYear();

                    function renderMonths(year) {
                        viewingYear = year;
                        $popup.find('.mp-year').text(year);
                        var $grid = $popup.find('.mp-grid').empty();
                        
                        var currentVal = $hidden.val() || '';
                        var selYear = -1;
                        var selMonth = -1;
                        if (currentVal.indexOf('-') !== -1) {
                            var p = currentVal.split('-');
                            selYear = parseInt(p[0], 10);
                            selMonth = parseInt(p[1], 10) - 1;
                        }

                        for (var i = 0; i < 12; i++) {
                            var isSelected = (selYear === year && selMonth === i);
                            var btnClass = isSelected ? 'btn-primary text-white shadow-sm' : 'btn-outline-light text-dark border';
                            var colHtml = 
                                '<div class="col-4">' +
                                    '<button type="button" class="btn btn-sm w-100 ' + btnClass + ' mp-month-btn" data-month="' + i + '">' +
                                        mesesCortos[i] +
                                    '</button>' +
                                '</div>';
                            $grid.append(colHtml);
                        }
                    }

                    function openPicker() {
                        if ($display.is(':disabled') || (typeof isReadonly !== 'undefined' && isReadonly)) return;
                        $('.month-picker-popup').not($popup).hide();
                        var cur = $hidden.val() || '';
                        var initY = new Date().getFullYear();
                        if (cur.indexOf('-') !== -1) {
                            var py = parseInt(cur.split('-')[0], 10);
                            if (!isNaN(py) && py > 2000) initY = py;
                        }
                        renderMonths(initY);
                        $popup.stop(true, true).fadeIn(150);
                    }

                    $display.on('click', function(e) {
                        e.stopPropagation();
                        if ($popup.is(':visible')) {
                            $popup.fadeOut(100);
                        } else {
                            openPicker();
                        }
                    });

                    $group.find('.month-picker-btn').on('click', function(e) {
                        e.stopPropagation();
                        if ($popup.is(':visible')) {
                            $popup.fadeOut(100);
                        } else {
                            openPicker();
                        }
                    });

                    $popup.on('click', '.mp-btn-prev', function(e) {
                        e.stopPropagation();
                        renderMonths(viewingYear - 1);
                    });

                    $popup.on('click', '.mp-btn-next', function(e) {
                        e.stopPropagation();
                        renderMonths(viewingYear + 1);
                    });

                    $popup.on('click', '.mp-month-btn', function(e) {
                        e.stopPropagation();
                        var m = parseInt($(this).data('month'), 10);
                        var mStr = (m + 1 < 10 ? '0' : '') + (m + 1);
                        var formatted = viewingYear + '-' + mStr;
                        $hidden.val(formatted).trigger('change');
                        $display.val(mesesNombres[m] + ' de ' + viewingYear).removeClass('is-invalid');
                        $popup.fadeOut(120);
                    });
                });

                $(document).on('click', function(e) {
                    if (!$(e.target).closest('.month-picker-group').length) {
                        $('.month-picker-popup').fadeOut(100);
                    }
                });
            }

            // Cargar actividades dinámicamente
            var isReadonly = ${readonly == true ? 'true' : 'false'};
            var obligacionesCount = ${fn:length(listaObligaciones) > 0 ? fn:length(listaObligaciones) : 0};
            for(var i=0; i<obligacionesCount; i++) {
                var rawText = document.getElementById("raw_actividad_" + i).value;
                if (rawText) {
                    var tieneHtml = /<\s*(p|div|table|ul|ol|li|span|h[1-6]|br|b|i|strong|em)\b[^>]*>/i.test(rawText);
                    if (!tieneHtml) {
                        // Si es texto plano puro, reemplazar saltos de línea por <br>
                        rawText = rawText.replace(/\n/g, '<br>');
                    } else {
                        // Limpiar saltos <br> excesivos acumulados
                        rawText = rawText.replace(/(<br\s*\/?>\s*){2,}/gi, '<br>');
                    }
                }
                agregarActividadConValor(i, rawText ? rawText : '', isReadonly);
            }

            function manejarVisibilidadReq(selector, mostrar) {
                $(selector).each(function() {
                    var $div = $(this);
                    // Consider it has a document if it has the uploaded file UI or the badge
                    var tieneDocumento = $div.find('.alert-secondary').length > 0 || $div.find('.badge.bg-success').length > 0;
                    if (mostrar || tieneDocumento) {
                        $div.show();
                        if (!mostrar) {
                            $div.find('input[type="file"]').prop('required', false);
                        }
                    } else {
                        $div.hide();
                        $div.find('input[type="file"]').prop('required', false);
                    }
                });
            }

            // Lógica para mostrar/ocultar soportes según la cuota
            function actualizarCamposSoportes() {
                var cuotaStr = $('select[name="numero_cuota"], input[name="numero_cuota"]').val();
                var cuotasNormales = parseInt("${contrato.numCuotasNumero}") || 0;
                var esAdicion = "${contrato.adicionSiNo}".toLowerCase() === "si";
                
                var esFirmaAdicion = (esAdicion && parseInt(cuotaStr) === cuotasNormales);
                var esCuotaAdicionPosterior = (esAdicion && parseInt(cuotaStr) > cuotasNormales);

                if (cuotaStr == "1") {
                    manejarVisibilidadReq('.req-cuota-1', true);
                    manejarVisibilidadReq('.req-cuota-adicion-only', false);
                    
                    // Hacer requeridos los soportes de la Cuota 1 si no están cargados
                    $('input[name="file_ficha_tecnica"]').prop('required', true);
                    $('input[name="file_secop"]').prop('required', true);
                    $('input[name="file_cedula"]').prop('required', true);
                    $('input[name="file_rut"]').prop('required', true);
                    $('input[name="file_rpc"]').prop('required', true);
                } else if (esFirmaAdicion) {
                    manejarVisibilidadReq('.req-cuota-1', false);
                    
                    manejarVisibilidadReq('.req-cuota-adicion-only', true);
                    $('input[name="file_secop"]').closest('div').show();
                    $('input[name="file_ficha_tecnica"]').closest('div').show();
                    
                    // Hacer requeridos los de adicion
                    $('input[name="file_modificacion"]').prop('required', true);
                    $('input[name="file_secop"]').prop('required', true);
                    $('input[name="file_ficha_tecnica"]').prop('required', true);
                    $('input[name="file_cedula"], input[name="file_rut"]').prop('required', false);
                } else if (esCuotaAdicionPosterior) {
                    manejarVisibilidadReq('.req-cuota-1', false);
                    manejarVisibilidadReq('.req-cuota-adicion-only', false);
                    $('input[name="file_ficha_tecnica"], input[name="file_secop"], input[name="file_cedula"], input[name="file_rut"], input[name="file_rpc"], input[name="file_modificacion"]').prop('required', false);
                } else {
                    manejarVisibilidadReq('.req-cuota-1', false);
                    manejarVisibilidadReq('.req-cuota-adicion-only', false);
                    $('input[name="file_secop"]').prop('required', false);
                    $('input[name="file_ficha_tecnica"], input[name="file_cedula"], input[name="file_rut"], input[name="file_rpc"], input[name="file_modificacion"]').prop('required', false);
                }
                actualizarAsteriscosArchivos();
            }

            function actualizarAsteriscosArchivos() {
                setTimeout(function() {
                    $('input[type="file"]').each(function() {
                        var $input = $(this);
                        var $label = $input.prevAll('.form-label, small.fw-bold').first();
                        if ($label.length === 0) {
                            $label = $input.closest('div').find('.form-label, small.fw-bold').first();
                        }
                        
                        // Si ya esta cargado, ya no es requerido
                        if ($label.find('.badge.bg-success').length > 0 || $input.prevAll('.alert-secondary').length > 0 || $input.closest('div').find('.alert-secondary').length > 0) {
                            $input.prop('required', false);
                            $label.find('.ast-cuota-1').hide();
                        }
                        
                        // Refrescar asterisco
                        if ($input.prop('required') && $input.is(':visible')) {
                            $label.find('.ast-cuota-1').show();
                            if ($label.find('.req-asterisk').length === 0 && $label.find('.ast-cuota-1').length === 0) {
                                $label.append(' <span class="text-danger req-asterisk" title="Requerido para radicar">*</span>');
                            }
                        } else {
                            $label.find('.req-asterisk').remove();
                            if (!$input.prop('required')) {
                                $label.find('.ast-cuota-1').hide();
                            }
                        }
                    });
                }, 100);
            }

            $('select[name="numero_cuota"], input[name="numero_cuota"]').on('change', actualizarCamposSoportes);
            actualizarCamposSoportes(); // Ejecutar al cargar la página
            
            // Renderizar archivos previamente subidos (la interfaz que pediste)
            var soportesJsonStr = '${fn:escapeXml(not empty informe.soportesJson ? informe.soportesJson : soportesJsonPreCargados)}';
            if (soportesJsonStr && soportesJsonStr.trim() !== '') {
                try {
                    // Reemplazamos entidades xml en caso de que fn:escapeXml las haya codificado
                    var decodedStr = $('<textarea/>').html(soportesJsonStr).text();
                    var soportesObj = JSON.parse(decodedStr);
                    // Limpiar duplicados históricos de campos de un solo archivo y evidencias
                    var baseKeyMap = {};
                    var evidenciaSeen = {};
                    var keysToDelete = [];
                    for (var k in soportesObj) {
                        var fileData = soportesObj[k];
                        var bKey = k;
                        if (k.match(/^evidencia_\d+_\d+/)) {
                            bKey = k.match(/^evidencia_\d+_\d+/)[0];
                        } else if (k.match(/_[0-9]+$/)) {
                            bKey = k.replace(/_[0-9]+$/, '');
                        }

                        if (!bKey.startsWith("evidencia_")) {
                            if (baseKeyMap[bKey]) {
                                keysToDelete.push(baseKeyMap[bKey]);
                            }
                            baseKeyMap[bKey] = k;
                        } else {
                            if (!evidenciaSeen[bKey]) {
                                evidenciaSeen[bKey] = { ids: {}, names: {} };
                            }
                            var fId = (fileData && fileData.id) ? String(fileData.id).trim() : '';
                            var fName = (fileData && fileData.name) ? String(fileData.name).trim().toLowerCase() : '';
                            if ((fId && evidenciaSeen[bKey].ids[fId]) || (fName && evidenciaSeen[bKey].names[fName])) {
                                keysToDelete.push(k);
                            } else {
                                if (fId) evidenciaSeen[bKey].ids[fId] = true;
                                if (fName) evidenciaSeen[bKey].names[fName] = true;
                            }
                        }
                    }
                    for (var d = 0; d < keysToDelete.length; d++) {
                        delete soportesObj[keysToDelete[d]];
                    }
                    $('input[name="soportes_json"]').val(JSON.stringify(soportesObj));
                    
                    for (var key in soportesObj) {
                        var fileData = soportesObj[key];
                        
                        // Extraer el nombre base del input. Ej: 'evidencia_0_0_1' -> 'evidencia_0_0', 'file_rpc_2' -> 'file_rpc'
                        var baseKey = key;
                        if (key.match(/^evidencia_\d+_\d+/)) {
                            baseKey = key.match(/^evidencia_\d+_\d+/)[0];
                        } else if (key.match(/_[0-9]+$/)) {
                            baseKey = key.replace(/_[0-9]+$/, '');
                        }
                        
                        var $input = $('input[name="' + baseKey + '"]');
                        if ($input.length) {
                            var btnEliminar = '';
                            if (!isReadonly) {
                                btnEliminar = '<button type="button" class="btn btn-sm btn-outline-danger btn-eliminar-soporte" data-key="' + key + '" title="Eliminar documento"><i class="bi bi-trash"></i></button>';
                            }
                            var ui = '<div class="alert alert-secondary py-2 mt-2 mb-2 d-flex justify-content-between align-items-center shadow-sm" style="border-left: 4px solid #0d6efd;" id="soporte-ui-' + key + '">' +
                                     '<div><i class="bi bi-file-earmark-check-fill text-success me-2 fs-5"></i>' +
                                     '<a href="' + fileData.url + '" target="_blank" class="text-decoration-none fw-bold text-dark">' + fileData.name + '</a></div>' +
                                     '<div>' + btnEliminar + '</div>' +
                                     '</div>';
                            $input.before(ui);
                            
                            // Cambiar el label del input para indicar que ya hay cargados
                            var $label = $input.prevAll('.form-label, small.fw-bold').first();
                            if ($label.length && $label.find('.badge').length === 0) {
                                $label.append(' <span class="badge bg-success ms-2">Cargado</span>');
                            }
                            
                            if (isReadonly) {
                                $input.hide();
                            } else {
                                if (!$input.next('.info-append-msg').length) {
                                    var msgText = baseKey.startsWith("evidencia_") ? 
                                        "Si selecciona nuevos archivos, se AGREGARÁN a los ya existentes." : 
                                        "Dejar en blanco para mantener el archivo actual. Si selecciona uno nuevo, se agregará (o reemplazará).";
                                    $input.after('<small class="info-append-msg text-muted d-block mt-1"><i class="bi bi-info-circle"></i> ' + msgText + '</small>');
                                }
                            }
                        }
                    }
                } catch(e) {
                    console.log('Error parseando soportesJson', e);
                }
            }
            
            // Actualizar visibilidad de divs y asteriscos después de renderizar archivos
            actualizarCamposSoportes();
            
            // Si es solo lectura, limpiar visualmente la pestaña de soportes
            if (isReadonly) {
                $('input[type="file"]').each(function() {
                    $(this).hide();
                    var $label = $(this).prevAll('.form-label, small.fw-bold').first();
                    if ($label.find('.badge.bg-success').length === 0) {
                        $(this).before('<div class="text-muted fst-italic mb-3"><i class="bi bi-x-circle me-1"></i>No se cargó documento</div>');
                    }
                });
            }

            $(document).on('click', '.btn-eliminar-soporte', function() {
                var $btn = $(this);
                Swal.fire({
                    title: '¿Está seguro de eliminar este documento?',
                    text: 'Esta acción aplicará los cambios al presionar "Guardar Borrador".',
                    icon: 'warning',
                    showCancelButton: true,
                    confirmButtonColor: '#d33',
                    cancelButtonColor: '#6c757d',
                    confirmButtonText: '<i class="bi bi-trash"></i> Sí, eliminar',
                    cancelButtonText: 'Cancelar'
                }).then((result) => {
                    if (result.isConfirmed) {
                        var key = $btn.data('key');
                        var prevElim = $('#soportes_eliminados').val();
                        $('#soportes_eliminados').val(prevElim ? prevElim + ',' + key : key);
                        if (typeof soportesObj !== 'undefined' && soportesObj[key]) {
                            delete soportesObj[key];
                            $('input[name="soportes_json"]').val(JSON.stringify(soportesObj));
                        }
                        var $ui = $('#soporte-ui-' + key);
                        var $container = $ui.parent();
                        $container.find('label .badge').remove();
                        $ui.fadeOut(function() { $(this).remove(); });
                        setTimeout(actualizarAsteriscosArchivos, 100);
                    }
                });
            });
        });

        function agregarActividadConValor(index, valor, isReadonly) {
            var container = document.getElementById("actividades_container_" + index);
            var actIndex = container.children.length; // Para identificar cada actividad de forma única
            
            var wrapper = document.createElement("div");
            wrapper.className = "mb-3 p-2 border rounded bg-white shadow-sm";
            
            var divText = document.createElement("div");
            divText.className = "d-flex align-items-start";
            
            var textarea = document.createElement("textarea");
            textarea.className = "form-control summernote-editor";
            textarea.name = "actividad_" + index;
            textarea.rows = 2;
            textarea.placeholder = "Describa la actividad realizada e inserte tablas o imágenes (copiar y pegar)...";
            textarea.value = valor;
            if (isReadonly) textarea.readOnly = true;
            else textarea.required = true;
            
            divText.appendChild(textarea);
            
            if (!isReadonly) {
                var btn = document.createElement("button");
                btn.type = "button";
                btn.className = "btn btn-outline-danger ms-2";
                btn.innerHTML = '<i class="bi bi-trash"></i>';
                btn.onclick = function() {
                    $(textarea).summernote('code', '');
                };
                divText.appendChild(btn);
            }
            wrapper.appendChild(divText);

            var divFile = document.createElement("div");
            divFile.className = "mt-3 evidencias-container";
            
            var fileLabel = document.createElement("small");
            fileLabel.className = "text-muted d-block fw-bold mb-2";
            fileLabel.innerHTML = '<i class="bi bi-paperclip"></i> Evidencias de esta actividad:';
            
            var fileInput = document.createElement("input");
            fileInput.type = "file";
            fileInput.className = "d-none file-input-evidencia";
            fileInput.name = "evidencia_" + index + "_" + actIndex;
            fileInput.multiple = true;
            
            var dropZone = document.createElement("div");
            var filesListContainer = document.createElement("div");
            filesListContainer.className = "mt-2 file-list-selected";
            
            if (isReadonly) {
                dropZone.style.display = "none";
                filesListContainer.style.display = "none";
            } else {
                dropZone.className = "border rounded p-3 text-center transition-all dropzone-evidencia";
                dropZone.style.border = "2px dashed #0d6efd";
                dropZone.style.cursor = "pointer";
                dropZone.style.backgroundColor = "#f8f9fa";
                dropZone.innerHTML = '<i class="bi bi-cloud-arrow-up fs-3 text-primary"></i><br><span class="text-primary fw-semibold">Haz clic aquí o arrastra los archivos (puedes subir varios)</span>';
                
                var tempPicker = document.createElement("input");
                tempPicker.type = "file";
                tempPicker.multiple = true;
                tempPicker.className = "d-none";
                
                dropZone.onclick = function() {
                    tempPicker.click();
                };
                
                // Acumulador persistente de archivos (DataTransfer) para soportar múltiples tandas sin sobreescribir
                var dt = new DataTransfer();
                fileInput._dt = dt;
                
                var renderSelectedFiles = function() {
                    filesListContainer.innerHTML = '';
                    if (dt.files.length === 0) {
                        dropZone.innerHTML = '<i class="bi bi-cloud-arrow-up fs-3 text-primary"></i><br><span class="text-primary fw-semibold">Haz clic aquí o arrastra los archivos (puedes subir varios)</span>';
                        dropZone.style.borderColor = "#0d6efd";
                        dropZone.style.backgroundColor = "#f8f9fa";
                        return;
                    }
                    
                    var totalBytes = 0;
                    var hasOverlimit = false;
                    for (var k = 0; k < dt.files.length; k++) {
                        totalBytes += dt.files[k].size;
                        if (dt.files[k].size > 100 * 1024 * 1024) {
                            hasOverlimit = true;
                        }
                    }
                    var totalMB = (totalBytes / (1024 * 1024)).toFixed(2);
                    
                    if (hasOverlimit) {
                        dropZone.innerHTML = '<i class="bi bi-exclamation-triangle-fill text-danger fs-3"></i><br><span class="text-danger fw-bold">' + dt.files.length + ' archivo(s) seleccionado(s) (' + totalMB + ' MB)</span><br><small class="text-danger">Uno o más archivos superan el límite de 100MB. Quítalos de la lista abajo.</small>';
                        dropZone.style.borderColor = "#dc3545";
                        dropZone.style.backgroundColor = "#f8d7da";
                    } else {
                        dropZone.innerHTML = '<i class="bi bi-check-circle-fill text-success fs-3"></i><br><span class="text-success fw-bold">' + dt.files.length + ' archivo(s) listo(s) (' + totalMB + ' MB)</span><br><small class="text-primary fw-semibold"><i class="bi bi-plus-circle"></i> Haz clic aquí o arrastra más archivos para seguir sumando</small>';
                        dropZone.style.borderColor = "#198754";
                        dropZone.style.backgroundColor = "#e8f5e9";
                    }
                    
                    // Renderizar cada archivo seleccionado con botón para removerlo individualmente
                    for (var i = 0; i < dt.files.length; i++) {
                        (function(idx) {
                            var file = dt.files[idx];
                            var sizeMB = (file.size / (1024 * 1024)).toFixed(2);
                            var isTooBig = file.size > 100 * 1024 * 1024;
                            
                            var item = document.createElement("div");
                            item.className = "alert " + (isTooBig ? "alert-danger" : "alert-light border") + " py-1 px-2 my-1 d-flex justify-content-between align-items-center shadow-sm";
                            item.style.fontSize = "0.85rem";
                            
                            var safeName = $('<div>').text(file.name).html();
                            var textSpan = document.createElement("span");
                            textSpan.className = "text-truncate me-2";
                            textSpan.style.maxWidth = "85%";
                            textSpan.innerHTML = '<i class="bi bi-file-earmark-check me-1 text-primary"></i> <b>' + safeName + '</b> <span class="text-muted">(' + (file.size > 1024 * 1024 ? sizeMB + ' MB' : (file.size / 1024).toFixed(1) + ' KB') + ')</span>' + (isTooBig ? ' <span class="badge bg-danger ms-1">Excede 100MB</span>' : '');
                            
                            var removeBtn = document.createElement("button");
                            removeBtn.type = "button";
                            removeBtn.className = "btn btn-sm btn-outline-danger py-0 px-2";
                            removeBtn.title = "Quitar este archivo";
                            removeBtn.innerHTML = '<i class="bi bi-x-lg"></i>';
                            removeBtn.onclick = function(e) {
                                e.stopPropagation();
                                var newDt = new DataTransfer();
                                for (var j = 0; j < dt.files.length; j++) {
                                    if (j !== idx) {
                                        newDt.items.add(dt.files[j]);
                                    }
                                }
                                dt = newDt;
                                fileInput._dt = dt;
                                try { fileInput.files = dt.files; } catch(err) {}
                                renderSelectedFiles();
                            };
                            
                            item.appendChild(textSpan);
                            item.appendChild(removeBtn);
                            filesListContainer.appendChild(item);
                        })(i);
                    }
                };
                
                var agregarNuevosArchivos = function(nuevosArchivos) {
                    if (!nuevosArchivos || nuevosArchivos.length === 0) return;
                    for (var k = 0; k < nuevosArchivos.length; k++) {
                        var f = nuevosArchivos[k];
                        var yaExiste = false;
                        for (var j = 0; j < dt.files.length; j++) {
                            if (dt.files[j].name === f.name && dt.files[j].size === f.size) {
                                yaExiste = true;
                                break;
                            }
                        }
                        if (!yaExiste) {
                            dt.items.add(f);
                        }
                    }
                    fileInput._dt = dt;
                    try {
                        fileInput.files = dt.files;
                    } catch(err) {
                        console.error("Error asignando files:", err);
                    }
                    renderSelectedFiles();
                };
                
                tempPicker.addEventListener('change', function() {
                    if (this.files && this.files.length > 0) {
                        agregarNuevosArchivos(this.files);
                    }
                    this.value = ''; // Limpiar el selector temporal para permitir seleccionar el mismo archivo si fue eliminado
                });
                
                dropZone.addEventListener('dragover', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    dropZone.style.backgroundColor = "#e9ecef";
                });
                
                dropZone.addEventListener('dragleave', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    if(dt.files.length === 0) {
                        dropZone.style.backgroundColor = "#f8f9fa";
                    } else {
                        dropZone.style.backgroundColor = "#e8f5e9";
                    }
                });
                
                dropZone.addEventListener('drop', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    if(e.dataTransfer && e.dataTransfer.files && e.dataTransfer.files.length > 0) {
                        agregarNuevosArchivos(e.dataTransfer.files);
                    }
                });
            }
            
            divFile.appendChild(fileLabel);
            divFile.appendChild(dropZone);
            divFile.appendChild(filesListContainer);
            divFile.appendChild(fileInput);
            if (typeof tempPicker !== 'undefined') {
                divFile.appendChild(tempPicker);
            }
            wrapper.appendChild(divFile);
            
            container.appendChild(wrapper);

            // Iniciar Summernote siempre para interpretar bien el HTML, y si es solo lectura lo deshabilitamos
            $(textarea).summernote({
                height: isReadonly ? 'auto' : 120,
                toolbar: isReadonly ? [] : [
                    ['style', ['bold', 'italic', 'underline', 'clear']],
                    ['para', ['ul', 'ol', 'paragraph']],
                    ['table', ['table']],
                    ['insert', ['picture', 'link']],
                    ['view', ['fullscreen']]
                ],
                placeholder: 'Escriba la actividad o pegue aquí una imagen/tabla...',
                disableDragAndDrop: true,
                    callbacks: {
                        onImageUpload: function(files) {
                            for (let i = 0; i < files.length; i++) {
                                let file = files[i];
                                let reader = new FileReader();
                                reader.onload = function(e) {
                                    let img = new Image();
                                    img.onload = function() {
                                        let canvas = document.createElement('canvas');
                                        let MAX_WIDTH = 800;
                                        let MAX_HEIGHT = 800;
                                        let width = img.width;
                                        let height = img.height;

                                        if (width > height) {
                                            if (width > MAX_WIDTH) {
                                                height *= MAX_WIDTH / width;
                                                width = MAX_WIDTH;
                                            }
                                        } else {
                                            if (height > MAX_HEIGHT) {
                                                width *= MAX_HEIGHT / height;
                                                height = MAX_HEIGHT;
                                            }
                                        }
                                        canvas.width = width;
                                        canvas.height = height;
                                        let ctx = canvas.getContext('2d');
                                        ctx.drawImage(img, 0, 0, width, height);
                                        
                                        canvas.toBlob(function(blob) {
                                            let formData = new FormData();
                                            let originalName = file.name ? file.name : "imagen.jpg";
                                            let dotIndex = originalName.lastIndexOf('.');
                                            let baseName = dotIndex !== -1 ? originalName.substring(0, dotIndex) : originalName;
                                            let ext = dotIndex !== -1 ? originalName.substring(dotIndex) : ".jpg";
                                            let uniqueName = baseName + "_" + new Date().getTime() + ext;
                                            formData.append('file', blob, uniqueName);
                                            
                                            $.ajax({
                                                url: 'ImageUploadServlet',
                                                method: 'POST',
                                                data: formData,
                                                processData: false,
                                                contentType: false,
                                                success: function(response) {
                                                    if (response && response.success && response.url) {
                                                        $(textarea).summernote('insertImage', response.url);
                                                    } else {
                                                        alert("Error del servidor al subir la imagen.");
                                                    }
                                                },
                                                error: function(xhr, status, error) {
                                                    console.error("Upload error:", xhr, status, error);
                                                    var msg = "Error al subir imagen (Status " + xhr.status + ").";
                                                    if (xhr.responseText) {
                                                        try {
                                                            var json = JSON.parse(xhr.responseText);
                                                            msg += " Detalle: " + (json.error || json.message || xhr.responseText);
                                                        } catch (e) {
                                                            msg += " " + xhr.responseText;
                                                        }
                                                    }
                                                    alert(msg);
                                                }
                                            });
                                        }, 'image/jpeg', 0.7);
                                    }
                                    img.src = e.target.result;
                                }
                                reader.readAsDataURL(file);
                            }
                        },
                        onPaste: function (e) {
                            var clipboardData = (e.originalEvent || e).clipboardData || window.clipboardData;
                            var html = clipboardData.getData('text/html');
                            var text = clipboardData.getData('text/plain');
                            
                            // Si están pegando una imagen directa del portapapeles (sin HTML), dejamos que Summernote la procese
                            if (!html && e.originalEvent.clipboardData && e.originalEvent.clipboardData.files && e.originalEvent.clipboardData.files.length > 0) {
                                return; // Salir de esta función para que el onImageUpload tome el control
                            }
                            
                            if (html) {
                                e.preventDefault();
                                var temp = document.createElement("div");
                                temp.innerHTML = html;
                                
                                // Eliminar atributos problemáticos de todos los nodos, pero salvando cuadros/imágenes
                                var allNodes = temp.getElementsByTagName("*");
                                for (var k = 0, max = allNodes.length; k < max; k++) {
                                    var node = allNodes[k];
                                    if (node && node.nodeType === 1) { // Asegurar que es un elemento
                                        var tagName = node.tagName.toLowerCase();
                                        
                                        // A las tablas les ponemos las clases de Bootstrap para que se vean bonitas y con bordes
                                        if (tagName === 'table') {
                                            node.className = "table table-bordered table-sm";
                                        } else {
                                            node.removeAttribute("class");
                                        }
                                        
                                        // Quitamos estilos raros que desordenan todo
                                        node.removeAttribute("style");
                                        node.removeAttribute("lang");
                                        node.removeAttribute("dir");
                                        
                                        // Protegemos el ancho/alto de imágenes, tablas y celdas
                                        if (tagName !== 'img' && tagName !== 'table' && tagName !== 'td' && tagName !== 'th') {
                                            node.removeAttribute("width");
                                            node.removeAttribute("height");
                                        }
                                        node.removeAttribute("align");
                                    }
                                }
                                
                                var cleanHtml = temp.innerHTML;
                                // Remover comentarios XML/HTML (basura de Word/Excel)
                                cleanHtml = cleanHtml.replace(/<!--[\s\S]*?-->/g, "");
                                // Si Word dejó viñetas pegadas a palabras (ej: ●Realizó o •Realizó), separarlas con espacio
                                cleanHtml = cleanHtml.replace(/([•●·▪▫○⁃])([a-zA-ZáéíóúÁÉÍÓÚñÑ])/g, "$1 $2");
                                // Remover espacios y párrafos vacíos que generan huecos gigantes
                                cleanHtml = cleanHtml.replace(/<p>\s*<\/p>/gi, "");
                                cleanHtml = cleanHtml.replace(/<p>\s*&nbsp;\s*<\/p>/gi, "");
                                cleanHtml = cleanHtml.replace(/<p>\s*<br\s*\/?>\s*<\/p>/gi, "");
                                cleanHtml = cleanHtml.replace(/<div>\s*<\/div>/gi, "");
                                cleanHtml = cleanHtml.replace(/<div>\s*<br\s*\/?>\s*<\/div>/gi, "");
                                cleanHtml = cleanHtml.replace(/<div>\s*&nbsp;\s*<\/div>/gi, "");
                                cleanHtml = cleanHtml.replace(/<span>\s*<\/span>/gi, "");
                                
                                // Eliminar múltiples saltos de línea al principio del texto
                                cleanHtml = cleanHtml.replace(/^(<br\s*\/?>|\s|&nbsp;)+/i, "");
                                
                                setTimeout(function () {
                                    var currentContent = $(textarea).summernote('code');
                                    // Si el editor está completamente vacío o solo tiene el salto de línea por defecto,
                                    // reemplazamos todo para no crear una línea vacía arriba.
                                    if (!currentContent || currentContent === '<p><br></p>' || currentContent.trim() === '') {
                                        $(textarea).summernote('code', cleanHtml);
                                    } else {
                                        $(textarea).summernote('pasteHTML', cleanHtml);
                                    }
                                }, 10);
                            } else if (text) {
                                e.preventDefault();
                                setTimeout(function () {
                                    var textClean = text.replace(/([•●·▪▫○⁃])([a-zA-ZáéíóúÁÉÍÓÚñÑ])/g, "$1 $2");
                                    var textHtml = textClean.replace(/\n/g, "<br>");
                                    var currentContent = $(textarea).summernote('code');
                                    if (!currentContent || currentContent === '<p><br></p>' || currentContent.trim() === '') {
                                        $(textarea).summernote('code', textHtml);
                                    } else {
                                        $(textarea).summernote('pasteHTML', textHtml);
                                    }
                                }, 10);
                            }
                        }
                    }
                });

            if (isReadonly) {
                $(textarea).summernote('disable');
            }
        }

        window.agregarActividad = function(index) {
            agregarActividadConValor(index, "", false);
        };
        
        // Formato de Dinero (Puntos para miles, sin decimales)
        function formatMoney(n) {
            if(!n) return "";
            let str = n.toString();
            // Si viene de la base de datos con .00 (ej: 33978000.00), quitamos los decimales exactos
            if (str.endsWith(".00")) {
                str = str.substring(0, str.length - 3);
            } else if (str.endsWith(".0")) {
                str = str.substring(0, str.length - 2);
            }
            // Removemos todo lo que no sea número
            str = str.replace(/[^0-9]/g, '');
            if(!str) return "";
            // Agregamos el punto como separador de miles
            return str.replace(/\B(?=(\d{3})+(?!\d))/g, ".");
        }
        
        $('.money-mask').each(function() {
            $(this).val(formatMoney($(this).val()));
            $(this).on('input', function() {
                var pos = this.selectionStart;
                var oldVal = $(this).val();
                var newVal = formatMoney(oldVal);
                $(this).val(newVal);
                // Adjust cursor position roughly
                var offset = newVal.length - oldVal.length;
                this.setSelectionRange(pos + offset, pos + offset);
            });
        });
        
        // Validación personalizada con SweetAlert y verificación física de archivos
        var formValidated = false;
        
        $('#informeForm').on('submit', function(e) {
            if (formValidated) {
                return true; // Si ya pasó todas las validaciones asíncronas, permitir el envío nativo
            }
            e.preventDefault();
            var form = this;
            var submitBtn = $(this).find('button[type="submit"]');

            // 1. Sincronizar todos los inputs de evidencias con su acumulador DataTransfer
            $('.file-input-evidencia').each(function() {
                if (this._dt && this._dt.files) {
                    try {
                        this.files = this._dt.files;
                    } catch(err) {
                        console.error("Error sincronizando acumulador:", err);
                    }
                }
            });

            // 2. VERIFICACIÓN FÍSICA INMEDIATA DE ARCHIVOS EN EL DISCO DEL COMPUTADOR
            // Previene el error 'ERR_FILE_NOT_FOUND' de Google Chrome si el usuario borró o movió el archivo de su computador
            var filePromises = [];
            var archivosBorrados = [];
            var brokenInputs = [];
            var totalFilesSize = 0;
            var maxFileSize = 100 * 1024 * 1024; // 100MB
            var maxTotalSize = 450 * 1024 * 1024; // 450MB
            var filesTooLarge = false;
            var largeFileNames = [];

            $(form).find('input[type="file"]').each(function() {
                if (this.files && this.files.length > 0) {
                    for (var i = 0; i < this.files.length; i++) {
                        var file = this.files[i];
                        totalFilesSize += file.size;
                        if (file.size > maxFileSize) {
                            filesTooLarge = true;
                            largeFileNames.push(file.name);
                        }
                        
                        // Lectura de prueba de 16 bytes para comprobar que el archivo no fue eliminado en Windows/Mac
                        (function(f, inputEl) {
                            var p = new Promise(function(resolve) {
                                try {
                                    if (f.slice) {
                                        var reader = new FileReader();
                                        reader.onload = function() { resolve(); };
                                        reader.onerror = function() {
                                            archivosBorrados.push(f.name);
                                            if (brokenInputs.indexOf(inputEl) === -1) {
                                                brokenInputs.push(inputEl);
                                            }
                                            resolve();
                                        };
                                        reader.readAsArrayBuffer(f.slice(0, 16));
                                    } else {
                                        resolve();
                                    }
                                } catch(err) {
                                    archivosBorrados.push(f.name);
                                    if (brokenInputs.indexOf(inputEl) === -1) {
                                        brokenInputs.push(inputEl);
                                    }
                                    resolve();
                                }
                            });
                            filePromises.push(p);
                        })(file, this);
                    }
                }
            });

            Promise.all(filePromises).then(function() {
                if (archivosBorrados.length > 0) {
                    Swal.fire({
                        icon: 'warning',
                        title: 'Archivo no encontrado en tu equipo',
                        html: 'El navegador detectó que los siguientes archivos seleccionados ya no se encuentran en tu computador (fueron borrados, movidos o renombrados):<br><br>' + 
                              '<div class="text-danger fw-bold p-2 bg-light border rounded text-start" style="max-height: 120px; overflow-y: auto;">' + 
                              archivosBorrados.map(name => '• ' + $('<div>').text(name).html()).join('<br>') + 
                              '</div><br>' + 
                              'Como las evidencias no son obligatorias, ¿deseas <b>omitir estos archivos</b> y continuar guardando, o prefieres buscarlos y volver a seleccionarlos?',
                        showCancelButton: true,
                        confirmButtonColor: '#3085d6',
                        cancelButtonColor: '#6c757d',
                        confirmButtonText: '<i class="fas fa-trash-alt me-1"></i> Omitir archivos y continuar',
                        cancelButtonText: 'Volver a seleccionarlos'
                    }).then((result) => {
                        if (result.isConfirmed) {
                            brokenInputs.forEach(function(inp) {
                                try { $(inp).val(''); } catch(e){}
                            });
                            // Re-ejecutar submit ya con los inputs limpios
                            $('#informeForm').submit();
                        }
                    });
                    return false;
                }

                if (filesTooLarge) {
                    Swal.fire({
                        icon: 'error',
                        title: 'Archivo demasiado pesado',
                        html: 'Los siguientes archivos superan el límite máximo de <b>100 MB</b>:<br><br>' + 
                              '<span class="text-danger">' + largeFileNames.join('<br>') + '</span><br><br>' + 
                              'Por favor, reduce su tamaño (comprimiendo el PDF) e intenta nuevamente.',
                        confirmButtonColor: '#007bff'
                    });
                    return false;
                }

                if (totalFilesSize > maxTotalSize) {
                    let totalMB = (totalFilesSize / (1024 * 1024)).toFixed(1);
                    Swal.fire({
                        icon: 'error',
                        title: 'Carga total demasiado grande',
                        html: 'El tamaño acumulado de todos los archivos seleccionados es de <b>' + totalMB + ' MB</b>, lo cual supera el límite permitido (máximo 450 MB combinados).<br><br>' + 
                              'Por favor, sube las evidencias en grupos o comprime los documentos PDF antes de guardar.',
                        confirmButtonColor: '#007bff'
                    });
                    return false;
                }
            
            let isValid = true;
            let firstInvalid = null;
            
            var esRadicacion = ($('#radicar_input').val() === 'true');
            if (esRadicacion) {
                // Validación estricta para informe FINAL: El pago de la seguridad social es obligatorio
                if ($('#tipo_informe').val() === 'FINAL') {
                    var $pagoSS = $('#pago_seguridad_social');
                    if (!$pagoSS.val() || $pagoSS.val().trim() === '') {
                        isValid = false;
                        if (!firstInvalid) firstInvalid = $pagoSS;
                    }
                }

                $(this).find('[required]').each(function() {
                    var $el = $(this);
                    if ($el.is('input[type="file"]')) {
                        var hasExisting = $el.prevAll('.alert-secondary').length > 0 || 
                                          $el.prevAll('.form-label, small.fw-bold').find('.badge.bg-success').length > 0 ||
                                          ($el.closest('div').find('.badge.bg-success').length > 0);
                        var hasNewFile = this.files && this.files.length > 0;
                        if (!hasExisting && !hasNewFile) {
                            isValid = false;
                            if (!firstInvalid) firstInvalid = $el;
                        }
                    } else {
                        if (!$el.val() || $el.val().trim() === '') {
                            isValid = false;
                            if (!firstInvalid) firstInvalid = $el;
                        }
                    }
                });

                // Validar obligatoriedad de la ESCRITURA de actividades al radicar
                var countObs = parseInt($('input[name="obligaciones_count"]').val()) || 0;
                for (var ob = 0; ob < countObs; ob++) {
                    var hasActivityText = false;
                    var $actTextareas = $('textarea[name="actividad_' + ob + '"]');
                    
                    $actTextareas.each(function() {
                        var code = '';
                        try { code = $(this).summernote('code'); } catch(e){}
                        if (!code) code = $(this).val() || '';
                        
                        // Verificar que tenga texto real o contenido (no solo etiquetas vacías como <p><br></p>)
                        var plainText = $('<div>').html(code).text().trim();
                        var hasVisualContent = /<img|<table|<iframe/i.test(code);
                        if (plainText.length > 0 || hasVisualContent) {
                            hasActivityText = true;
                        }
                    });

                    if (!hasActivityText) {
                        isValid = false;
                        if (!firstInvalid) {
                            var $noteEditor = $('#actividades_container_' + ob).find('.note-editable');
                            firstInvalid = $noteEditor.length ? $noteEditor : $('#actividades_container_' + ob);
                        }
                    }
                }
            } else {
                // Al guardar borrador, verificar datos mínimos de identificación y Fecha de transacción obligatoria
                var $periodo = $('input[name="periodo_informe"]');
                var $cuota = $('select[name="numero_cuota"], input[name="numero_cuota"]');
                var $fechaSuscripcion = $('input[name="fecha_suscripcion"]');
                if (!$periodo.val() || $periodo.val().trim() === '') {
                    isValid = false;
                    firstInvalid = $periodo;
                } else if (!$cuota.val() || $cuota.val().trim() === '') {
                    isValid = false;
                    firstInvalid = $cuota;
                } else if (!$fechaSuscripcion.val() || $fechaSuscripcion.val().trim() === '') {
                    isValid = false;
                    firstInvalid = $fechaSuscripcion;
                }
            }
            
            if (!isValid) {
                e.preventDefault();
                
                // Mover a la pestaña donde está el campo vacío
                let tabPane = firstInvalid.closest('.tab-pane');
                if (tabPane.length) {
                    let tabId = tabPane.attr('id');
                    let tabTrigger = new bootstrap.Tab($('button[data-bs-target="#' + tabId + '"]')[0]);
                    tabTrigger.show();
                }
                
                var alertTitle = esRadicacion ? '¡Faltan datos obligatorios para radicar!' : '¡Faltan datos para el borrador!';
                var errorMsg = esRadicacion ? 
                    'Por favor, diligencia todos los campos obligatorios y asegúrate de <b>describir las actividades desarrolladas para cada obligación</b> antes de radicar la cuenta.' : 
                    'Para guardar el borrador, es obligatorio indicar el <b>Período del informe</b>, el <b>Número de cuota</b> y la <b>Fecha de transacción</b>.';
                
                Swal.fire({
                    icon: 'warning',
                    title: alertTitle,
                    html: errorMsg,
                    confirmButtonColor: '#007bff'
                }).then(() => {
                    setTimeout(() => firstInvalid.focus(), 300);
                });
                
                return false;
            }
            
            // Recopilar obligaciones en un solo JSON para evitar el límite de 50 campos de Tomcat
            var obligacionesJson = [];
            var count = parseInt($('input[name="obligaciones_count"]').val()) || 0;
            
            // Salvaguarda frontend: si hay obligaciones pero los editores no se inicializaron en pantalla, impedir submit
            if (count > 0 && $('#actividades_container_0 textarea').length === 0) {
                Swal.fire({
                    icon: 'error',
                    title: 'Error de carga en la página',
                    text: 'Los editores de actividades no se han inicializado correctamente en el navegador. Por favor recarga la página (Ctrl + F5) antes de continuar.',
                    confirmButtonColor: '#d33'
                });
                return false;
            }
            
            for (var i = 0; i < count; i++) {
                var obj = {};
                var $obligacion = $('input[name="obligacion_' + i + '"]');
                if ($obligacion.length) {
                    obj.obligacion = $obligacion.val();
                    $obligacion.removeAttr('name'); // Evitar que se envíe como parte individual
                }
                
                var acts = [];
                $('textarea[name="actividad_' + i + '"]').each(function() {
                    var code = '';
                    try {
                        code = $(this).summernote('code');
                    } catch(err) {
                        code = $(this).val();
                    }
                    if (!code || code === '<p><br></p>' || code.trim() === '') {
                        var fb = $(this).val();
                        if (fb && fb !== '<p><br></p>' && fb.trim() !== '') code = fb;
                        else code = '';
                    }
                    acts.push(code);
                    $(this).removeAttr('name'); // Ya empacado en JSON
                });
                obj.actividad = acts.join("\n");
                
                obligacionesJson.push(obj);
            }
            $('#informeForm').find('input[name="concepto_supervisor_json"]').remove();
            var jsonStr = JSON.stringify(obligacionesJson);
            try {
                var b64Payload = 'B64:' + btoa(unescape(encodeURIComponent(jsonStr)));
                $('#informeForm').append($('<input type="hidden" name="concepto_supervisor_json">').val(b64Payload));
            } catch(b64Err) {
                $('#informeForm').append($('<input type="hidden" name="concepto_supervisor_json">').val(jsonStr));
            }
            $('input[name="obligaciones_count"]').removeAttr('name');
            
            // Eliminar los nombres de TODOS los inputs de tipo file que estén realmente vacíos
            // para no saturar las partes multipart de Tomcat
            $('input[type="file"]').each(function() {
                var hasFiles = this.files && this.files.length > 0;
                if (!$(this).val() && !hasFiles) {
                    $(this).removeAttr('name');
                }
            });
            
            // Quitar puntos antes de enviar el formulario para que Java (BigDecimal) no se rompa
            $('.money-mask').each(function() {
                var clean = $(this).val().replace(/\./g, '');
                $(this).val(clean);
            });
            
            // Si es informe final, asegurar textos oficiales de recibo y paz y salvo antes de enviar
            if ($('#tipo_informe').val() === 'FINAL') {
                var numContrato = $('#numero_contrato_display').val();
                var textoRecibo = "Con la firma del presente informe se deja constancia del recibo a satisfacción por parte del DISTRITO DE SANTIAGO DE CALI – DEPARTAMENTO ADMINISTRATIVO DE GESTIÓN JURÍDICA PÚBLICA de los servicios prestados pactados en el contrato N° " + numContrato + ".";
                var textoPazSalvo = "El contratista a la fecha del presente informe no posee a su cargo elementos devolutivos de propiedad del Distrito de Santiago de Cali, entregados por este organismo para el desempeño de sus actividades. Así mismo se encuentra a paz y salvo con el archivo de gestión documental y el sistema de gestión documental.";
                var curR = $('#recibo_satisfaccion').val();
                if (!curR || curR.trim() === '' || curR.trim() === 'N/A') {
                    $('#recibo_satisfaccion').val(textoRecibo);
                }
                var curP = $('#constancia_paz_salvo').val();
                if (!curP || curP.trim() === '' || curP.trim() === 'N/A') {
                    $('#constancia_paz_salvo').val(textoPazSalvo);
                }
            }
            
            setTimeout(function() {
                submitBtn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Guardando...');
            }, 50);
            
            Swal.fire({
                title: 'Guardando Informe...',
                text: 'Por favor espere. Esto puede tardar unos minutos mientras se suben los archivos a Google Drive.',
                allowOutsideClick: false,
                didOpen: () => {
                    Swal.showLoading();
                }
            });

            formValidated = true;
            form.submit();
        });
    });
        
        // Auto-calcular el Saldo por Cancelar
        function calcularSaldo() {
            var totalStr = $('#valor_total').val().replace(/\./g, '');
            var cuotaStr = $('#valor_cuota').val().replace(/\./g, '');
            var acumuladoStr = $('#valor_acumulado').val().replace(/\./g, '');
            
            var total = parseFloat(totalStr) || 0;
            var cuota = parseFloat(cuotaStr) || 0;
            var acumulado = parseFloat(acumuladoStr) || 0;
            
            var saldo = total - (cuota + acumulado);
            if(saldo < 0) saldo = 0; // Evitar saldos negativos si se equivocan
            
            $('#saldo_cancelar').val(formatMoney(saldo));
        }
        
        $('#valor_cuota, #valor_acumulado, #valor_total').on('input', calcularSaldo);
        // Calcular al cargar la pagina por si ya hay valores
        $(document).ready(function() {
            if(!'${readonly}') {
                calcularSaldo();
            }
            
            var cuotasValores = ${empty cuotasValoresJson ? '{}' : cuotasValoresJson};
            
            // Recalcular el valor acumulado en tiempo real al cambiar de cuota
            $('select[name="numero_cuota"]').change(function() {
                var cuota = parseInt($(this).val());
                if (cuota === 1) {
                    $('#valor_acumulado').val('0').trigger('input');
                } else if (!isNaN(cuota)) {
                    var nuevoAcumulado = 0;
                    for (var i = 1; i < cuota; i++) {
                        var key = i.toString();
                        if (cuotasValores[key]) {
                            nuevoAcumulado += parseFloat(cuotasValores[key]);
                        }
                    }
                    $('#valor_acumulado').val(formatMoney(nuevoAcumulado)).trigger('input');
                }

                // Si la cuota seleccionada es la última del contrato, auto-seleccionar INFORME FINAL
                var maxCuota = 0;
                $('select[name="numero_cuota"] option').each(function() {
                    var v = parseInt($(this).val());
                    if (!isNaN(v) && v > maxCuota) maxCuota = v;
                });
                if (maxCuota > 0 && cuota >= maxCuota) {
                    if (!$('#tipo_informe').is(':disabled') && $('#tipo_informe').val() !== 'FINAL') {
                        $('#tipo_informe').val('FINAL').trigger('change');
                    }
                }
            });
            
            // Lógica de arrastrar y soltar para inputs tipo file (delegación de eventos para inputs dinámicos)
            $(document).on('dragover dragenter', 'input[type="file"]', function(e) {
                if ($(this).prop('disabled')) return;
                e.preventDefault();
                e.stopPropagation();
                $(this).addClass('file-dragover');
            })
            .on('dragleave dragend drop', 'input[type="file"]', function(e) {
                if ($(this).prop('disabled')) return;
                e.preventDefault();
                e.stopPropagation();
                $(this).removeClass('file-dragover');
            })
            .on('drop', 'input[type="file"]', function(e) {
                if ($(this).prop('disabled')) return;
                var files = e.originalEvent.dataTransfer.files;
                if (files && files.length > 0) {
                    try {
                        this.files = files;
                    } catch(err) {
                        $(this).prop('files', files);
                    }
                    $(this).trigger('change');
                }
            });
        });
    </script>
        <script>
            $(document).ready(function() {
                $('#historialTable').DataTable({
                    "order": [[0, "desc"]], // Ordenar por fecha descendente por defecto
                    "language": {
                        "decimal": "",
                        "emptyTable": "No hay historial disponible",
                        "info": "Mostrando _START_ a _END_ de _TOTAL_ registros",
                        "infoEmpty": "Mostrando 0 a 0 de 0 registros",
                        "infoFiltered": "(filtrado de _MAX_ registros totales)",
                        "infoPostFix": "",
                        "thousands": ",",
                        "lengthMenu": "Mostrar _MENU_ registros",
                        "loadingRecords": "Cargando...",
                        "processing": "Procesando...",
                        "search": "Buscar en el historial:",
                        "zeroRecords": "No se encontraron coincidencias",
                        "paginate": {
                            "first": "Primero",
                            "last": "Último",
                            "next": "Siguiente",
                            "previous": "Anterior"
                        },
                        "aria": {
                            "sortAscending": ": activar para ordenar ascendente",
                            "sortDescending": ": activar para ordenar descendente"
                        }
                    }
                });

                function togglePagoSeguridadSocial() {
                    var tipo = $('#tipo_informe').val();
                    if (tipo === 'FINAL') {
                        $('#container_pago_seguridad').show();
                        $('#pago_seguridad_social').prop('required', true);
                    } else {
                        $('#container_pago_seguridad').hide();
                        $('#pago_seguridad_social').prop('required', false);
                        if(!$('#pago_seguridad_social').is(':disabled')) {
                            $('#pago_seguridad_social').val('');
                            $('textarea[name="observaciones_financieras"]').val('');
                        }
                    }
                }
                
                const meses = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];

                function getMesTexto(dateString) {
                    if (!dateString) return '[MES ACTUAL]';
                    const parts = dateString.split('-');
                    if (parts.length !== 2) return dateString;
                    const year = parts[0];
                    const month = parseInt(parts[1], 10) - 1;
                    if (isNaN(month) || month < 0 || month > 11) return dateString;
                    return meses[month] + ' de ' + year;
                }

                function getMesSiguienteTexto(dateString) {
                    if (!dateString) return '[MES SIGUIENTE]';
                    const parts = dateString.split('-');
                    if (parts.length !== 2) return dateString;
                    const year = parseInt(parts[0], 10);
                    const month = parseInt(parts[1], 10) - 1;
                    if (isNaN(month) || month < 0 || month > 11) return dateString;
                    
                    let nextMonth = month + 1;
                    let nextYear = year;
                    if (nextMonth > 11) {
                        nextMonth = 0;
                        nextYear++;
                    }
                    return meses[nextMonth] + ' de ' + nextYear;
                }

                function updateObservacionesFinancieras() {
                    var pagoVal = $('#pago_seguridad_social').val();
                    if ($('#pago_seguridad_social').is(':disabled')) return; // No auto-cambiar si es readonly
                    
                    var obsFinancieras = $('textarea[name="observaciones_financieras"]');
                    
                    if (pagoVal === 'Mes vencido') {
                        var periodo = $('#planilla_periodo').val();
                        var mesActual = getMesTexto(periodo);
                        var mesSiguiente = getMesSiguienteTexto(periodo);
                        
                        var textoLegal = "El contratista acreditó el pago de los aportes a la Seguridad Social Integral correspondiente al mes de " + mesActual + ", último mes legalmente exigible al contratista para el trámite de la última cuota del contrato, de conformidad con lo dispuesto en Ley 1955 de 2019.\n\nNo obstante, en cumplimiento de lo señalado en el artículo 50 de la Ley 789 de 2002 que establece que: \"Las Entidades públicas en el momento de liquidar los contratos deberán verificar y dejar constancia del cumplimiento de las obligaciones del contratista frente a los aportes mencionados durante toda su vigencia, estableciendo una correcta relación entre el monto cancelado y las sumas que debieron haber sido cotizadas\", y teniendo en cuenta que a la luz del artículo 60 de la Ley 80 de 1993 la liquidación de los contratos de prestación de servicios profesionales y de apoyo a la gestión no es obligatoria, el contratista deberá acreditar ante el Supervisor el pago de los aportes su seguridad social del mes de " + mesSiguiente + " remitiendo los correspondientes soportes al correo electrónico institucional del Supervisor con copia al correo institucional del Organismo, dentro de los cinco (5) días hábiles siguientes al vencimiento del plazo para la autoliquidación y el pago de los aportes al Sistema de Seguridad Social Integral y Aportes Parafiscales, establecido en el Decreto 1990 de 2016, o la disposición que la derogue o modifique, conforme a los dígitos de su cédula. La acreditación del pago de los aportes se anexará al expediente.\n\nEn caso de que el contratista no cumpla esta obligación, el Supervisor reportará el eventual incumplimiento en el pago de aportes a la Unidad Administrativa Especial de Gestión Pensional y Contribuciones Parafiscales de la Protección Social (UGPP), con el fin de que esta entidad adelante las acciones pertinentes a que haya lugar.";
                        
                        obsFinancieras.val(textoLegal);
                    } else if (pagoVal === 'Al Día') {
                        obsFinancieras.val('');
                    }
                }
                
                $('#pago_seguridad_social').change(updateObservacionesFinancieras);
                $('#planilla_periodo').change(function() {
                    // Si ya está seleccionado "Mes vencido", actualizar el texto si cambia el mes
                    if ($('#pago_seguridad_social').val() === 'Mes vencido') {
                        updateObservacionesFinancieras();
                    }
                });
                
                // Ejecutar al cargar para autocompletar si estaba seleccionado pero el texto está vacío
                if ($('#pago_seguridad_social').val() === 'Mes vencido' && !$('textarea[name="observaciones_financieras"]').val().trim()) {
                    updateObservacionesFinancieras();
                }

                function asegurarTextosFinal() {
                    var tipo = $('#tipo_informe').val();
                    if (tipo === 'FINAL') {
                        var numContrato = $('#numero_contrato_display').val();
                        var textoRecibo = "Con la firma del presente informe se deja constancia del recibo a satisfacción por parte del DISTRITO DE SANTIAGO DE CALI – DEPARTAMENTO ADMINISTRATIVO DE GESTIÓN JURÍDICA PÚBLICA de los servicios prestados pactados en el contrato N° " + numContrato + ".";
                        var textoPazSalvo = "El contratista a la fecha del presente informe no posee a su cargo elementos devolutivos de propiedad del Distrito de Santiago de Cali, entregados por este organismo para el desempeño de sus actividades. Así mismo se encuentra a paz y salvo con el archivo de gestión documental y el sistema de gestión documental.";
                        
                        var curR = $('#recibo_satisfaccion').val();
                        if (!curR || curR.trim() === '' || curR.trim() === 'N/A') {
                            $('#recibo_satisfaccion').val(textoRecibo);
                        }
                        var curP = $('#constancia_paz_salvo').val();
                        if (!curP || curP.trim() === '' || curP.trim() === 'N/A') {
                            $('#constancia_paz_salvo').val(textoPazSalvo);
                        }
                    }
                }

                $('#tipo_informe').change(function() {
                    var tipo = $(this).val();
                    togglePagoSeguridadSocial();
                    
                    // Solo actualizar si no estamos en modo readonly
                    if ($(this).is(':disabled')) return;
                    
                    if (tipo === 'FINAL') {
                        asegurarTextosFinal();
                    } else {
                        $('#recibo_satisfaccion').val('N/A');
                        $('#constancia_paz_salvo').val('N/A');
                    }
                });
                
                // Ejecutar al inicio para ajustar estado según valor cargado
                togglePagoSeguridadSocial();
                asegurarTextosFinal();
            });

            function setRadicar() {
                var revSelect = document.getElementById("revisor_select");
                if (revSelect) {
                    var rev = revSelect.value;
                    if (!rev && rev !== "0") {
                        Swal.fire('Atención', 'Debe seleccionar un revisor (o la opción Sin Revisor) para poder radicar la cuenta.', 'warning');
                        return false;
                    }
                }
                document.getElementById("radicar_input").value = "true";
                return true;
            }

            function setRadicarDevuelta() {
                document.getElementById("radicar_input").value = "true";
                return true;
            }
            
            // Dinamismo para textos de adición y valores
            $(document).ready(function() {
                var tieneAdicion = ${contrato.tieneAdicion ? 'true' : 'false'};
                var adicionesAuto = tieneAdicion ? `${fn:escapeXml(informeAuto.adiciones)}` : '';
                var prorrogasAuto = tieneAdicion ? `${fn:escapeXml(informeAuto.prorrogas)}` : '';
                var modificacionesAuto = tieneAdicion ? `${fn:escapeXml(informeAuto.modificaciones)}` : '';
                var cuotasNormales = parseInt("${contrato.numCuotasNumero}") || 0;
                var cuotasAdicion = parseInt("${contrato.numeroCuotasAdicion}") || 0;
                var cuotaFinal = cuotasNormales + cuotasAdicion;
                
                var valorNormal = "${contrato.valorTotalNumeros}";
                var valorAdicionado = "${not empty contrato.valorContratoMasAdicion ? contrato.valorContratoMasAdicion : contrato.valorTotalNumeros}";
                var valorVigente = tieneAdicion ? valorAdicionado : valorNormal;
                
                var baseDateStr = "<fmt:formatDate value='${contrato.fechaTerminacion}' pattern='yyyy-MM-dd'/>";
                var fechaAdicion = baseDateStr;
                if (tieneAdicion && baseDateStr && cuotasAdicion > 0) {
                    var dateParts = baseDateStr.split("-");
                    var dateObj = new Date(dateParts[0], dateParts[1] - 1, dateParts[2]);
                    dateObj.setMonth(dateObj.getMonth() + cuotasAdicion);
                    var y = dateObj.getFullYear();
                    var m = ("0" + (dateObj.getMonth() + 1)).slice(-2);
                    var d = ("0" + dateObj.getDate()).slice(-2);
                    fechaAdicion = y + "-" + m + "-" + d;
                }

                $('[name="numero_cuota"]').on('change input', function() {
                    var cuota = parseInt($(this).val());
                    if (!cuota) return;

                    var currentAd = $('textarea[name="adiciones"]').val() ? $('textarea[name="adiciones"]').val().trim() : '';
                    var currentPro = $('textarea[name="prorrogas"]').val() ? $('textarea[name="prorrogas"]').val().trim() : '';
                    var currentMod = $('textarea[name="modificaciones"]').val() ? $('textarea[name="modificaciones"]').val().trim() : '';

                    if (tieneAdicion && cuotasNormales > 0 && cuota >= cuotasNormales) {
                        if (adicionesAuto && (currentAd === 'N/A' || currentAd === '')) $('textarea[name="adiciones"]').val(adicionesAuto);
                        if (prorrogasAuto && (currentPro === 'N/A' || currentPro === '')) $('textarea[name="prorrogas"]').val(prorrogasAuto);
                        if (modificacionesAuto && (currentMod === 'N/A' || currentMod === '')) $('textarea[name="modificaciones"]').val(modificacionesAuto);
                    } else {
                        if (currentAd === adicionesAuto || currentAd === '') $('textarea[name="adiciones"]').val('N/A');
                        if (currentPro === prorrogasAuto || currentPro === '') $('textarea[name="prorrogas"]').val('N/A');
                        if (currentMod === modificacionesAuto || currentMod === '') $('textarea[name="modificaciones"]').val('N/A');
                    }
                    
                    // Actualizar Valor Total y Fecha de Terminación
                    if (tieneAdicion) {
                        if (valorVigente) $('#valor_total').val(valorVigente).trigger('input');
                        if (cuotasNormales > 0 && cuota >= cuotasNormales && fechaAdicion) {
                            $('input[name="fecha_fin_periodo"]').val(fechaAdicion);
                        } else if (baseDateStr) {
                            $('input[name="fecha_fin_periodo"]').val(baseDateStr);
                        }
                    } else {
                        if (valorNormal) $('#valor_total').val(valorNormal).trigger('input');
                        if (baseDateStr) $('input[name="fecha_fin_periodo"]').val(baseDateStr);
                    }
                });
                
                // Ejecutar al cargar la página para inicializar los valores si ya hay una cuota preseleccionada
                if ($('[name="numero_cuota"]').val()) {
                    $('[name="numero_cuota"]').trigger('change');
                }
                
                // Mostrar/Ocultar asteriscos de Cuota 1
                function actualizarAsteriscosCuota() {
                    var cuota = parseInt($('[name="numero_cuota"]').val());
                    if (cuota === 1) {
                        $('.ast-cuota-1').show();
                    } else {
                        $('.ast-cuota-1').hide();
                    }
                }

                // ========================================================
                // LOGICA: CONTRATISTA NUEVO (CUOTA 1 - SEGURIDAD SOCIAL)
                // ========================================================
                var esReadonlyGeneral = ${readonly ? 'true' : 'false'};

                function actualizarEstadoContratistaNuevo() {
                    var cuotaVal = $('[name="numero_cuota"]').val();
                    var cuota = parseInt(cuotaVal);
                    var checkNuevo = $('#check_contratista_nuevo');

                    if (cuota === 1) {
                        $('#container_contratista_nuevo').show();
                    } else {
                        $('#container_contratista_nuevo').hide();
                        if (checkNuevo.is(':checked') && !esReadonlyGeneral) {
                            checkNuevo.prop('checked', false);
                        }
                    }

                    var esNuevoActivo = (cuota === 1 && checkNuevo.is(':checked'));

                    if (esNuevoActivo) {
                        // Mostrar campos de afiliación
                        $('#container_afiliaciones_nuevo').slideDown(200);
                        $('#eps, #afp_pension, #arl').prop('required', true).each(function() {
                            var lbl = $(this).closest('div').find('.form-label');
                            if (lbl.find('.req-asterisk').length === 0) {
                                lbl.append(' <span class="text-danger req-asterisk" title="Requerido para radicar">*</span>');
                            }
                        });

                        // En campos de planilla: poner N/A, readonly y quitar required/asteriscos
                        if (!$('#planilla_numero').val() || $('#planilla_numero').val() === '') {
                            $('#planilla_numero').val('N/A');
                        }
                        if (!$('#planilla_pin').val() || $('#planilla_pin').val() === '') {
                            $('#planilla_pin').val('N/A');
                        }
                        if (!$('#planilla_operador').val() || $('#planilla_operador').val() === '') {
                            $('#planilla_operador').val('N/A');
                        }
                        
                        $('#planilla_numero, #planilla_pin, #planilla_operador').attr('readonly', true).prop('required', false);

                        // Fecha de pago y Periodo de pago: no aplican
                        $('#planilla_fecha_pago').prop('required', false).val('');
                        $('#col_planilla_fecha_pago').hide();

                        $('#planilla_periodo').prop('required', false).val('');
                        $('#display_planilla_periodo').prop('required', false).val('');
                        $('#col_planilla_periodo').hide();

                        $('#planilla_numero, #planilla_pin, #planilla_operador, #planilla_fecha_pago, #planilla_periodo, #display_planilla_periodo').each(function() {
                            var lbl = $(this).closest('div').find('.form-label');
                            lbl.find('.req-asterisk').remove();
                        });

                        $('#container_pago_seguridad').hide();
                        $('#pago_seguridad_social').prop('required', false);
                        $('#file_seguridad_social').prop('required', false);
                    } else {
                        $('#file_seguridad_social').prop('required', true);
                        // Ocultar campos de afiliación y quitar asteriscos
                        $('#container_afiliaciones_nuevo').slideUp(200);
                        $('#eps, #afp_pension, #arl').prop('required', false).each(function() {
                            var lbl = $(this).closest('div').find('.form-label');
                            lbl.find('.req-asterisk').remove();
                        });

                        // Restaurar campos de planilla
                        if (!esReadonlyGeneral) {
                            $('#planilla_numero, #planilla_pin, #planilla_operador').removeAttr('readonly');
                            if ($('#planilla_numero').val() === 'N/A') $('#planilla_numero').val('');
                            if ($('#planilla_pin').val() === 'N/A') $('#planilla_pin').val('');
                            if ($('#planilla_operador').val() === 'N/A') $('#planilla_operador').val('');
                        }

                        $('#col_planilla_fecha_pago').show();
                        $('#col_planilla_periodo').show();

                        $('#planilla_numero, #planilla_pin, #planilla_operador, #planilla_fecha_pago, #planilla_periodo, #display_planilla_periodo').prop('required', true).each(function() {
                            var lbl = $(this).closest('div').find('.form-label');
                            if (lbl.find('.req-asterisk').length === 0) {
                                lbl.append(' <span class="text-danger req-asterisk" title="Requerido para radicar">*</span>');
                            }
                        });
                        
                        if (typeof togglePagoSeguridadSocial === 'function') {
                            togglePagoSeguridadSocial();
                        }
                    }
                }

                function actualizarTextoObservacionesNuevo() {
                    if (esReadonlyGeneral) return;
                    var cuota = parseInt($('[name="numero_cuota"]').val());
                    var checkNuevo = $('#check_contratista_nuevo');
                    var esNuevoActivo = (cuota === 1 && checkNuevo.is(':checked'));
                    var $obs = $('#observaciones_financieras');

                    if (esNuevoActivo) {
                        var eps = ($('#eps').val() || '').trim();
                        var afp = ($('#afp_pension').val() || '').trim();
                        var arl = ($('#arl').val() || '').trim();

                        var epsTxt = eps ? eps : 'EPS';
                        var afpTxt = afp ? afp : 'FONDO DE PENSIÓN';
                        var arlTxt = arl ? arl : 'ARL';

                        var textoNuevo = "El contratista aporta certificado de afiliación a salud – " + epsTxt + ", Fondo de pensión – " + afpTxt + ", y ARL - " + arlTxt + ", soporte válido para la presentación de la primera cuota del presente contrato";
                        
                        var valActual = $obs.val().trim();
                        if (!valActual || valActual.startsWith("El contratista aporta certificado de afiliación a salud")) {
                            $obs.val(textoNuevo);
                        }
                    } else {
                        if ($obs.val().trim().startsWith("El contratista aporta certificado de afiliación a salud")) {
                            $obs.val('');
                        }
                    }
                }

                $('#check_contratista_nuevo').on('change', function() {
                    actualizarEstadoContratistaNuevo();
                    actualizarTextoObservacionesNuevo();
                });

                $('#eps, #afp_pension, #arl').on('input change', function() {
                    actualizarTextoObservacionesNuevo();
                });
                
                $('[name="numero_cuota"]').on('change keyup', function() {
                    actualizarAsteriscosCuota();
                    actualizarEstadoContratistaNuevo();
                    actualizarTextoObservacionesNuevo();
                });
                actualizarAsteriscosCuota();
                actualizarEstadoContratistaNuevo();
                actualizarTextoObservacionesNuevo();
            });

            // Estado global para permitir minimizar y restaurar el modal de revisión sin perder texto
            var _modalRevisionState = {
                activo: false,
                idInforme: null,
                estadoActual: null,
                tipoRolModal: null,
                accion: null,
                obs: '',
                revisorId: ''
            };
            var _isMinimizingModal = false;

            // Función para hacer arrastrable cualquier ventana o widget flotante
            function hacerDraggable(elem, handle) {
                if (!elem || !handle) return;
                var x = 0, y = 0, dx = 0, dy = 0;
                handle.style.cursor = 'grab';

                handle.onmousedown = function(e) {
                    if (e.target.tagName === 'BUTTON' || e.target.closest('button')) return;
                    e.preventDefault();
                    handle.style.cursor = 'grabbing';
                    dx = e.clientX;
                    dy = e.clientY;
                    document.onmouseup = function() {
                        handle.style.cursor = 'grab';
                        document.onmouseup = null;
                        document.onmousemove = null;
                    };
                    document.onmousemove = function(e) {
                        e.preventDefault();
                        x = dx - e.clientX;
                        y = dy - e.clientY;
                        dx = e.clientX;
                        dy = e.clientY;
                        elem.style.top = (elem.offsetTop - y) + "px";
                        elem.style.left = (elem.offsetLeft - x) + "px";
                        elem.style.bottom = 'auto';
                        elem.style.right = 'auto';
                    };
                };

                handle.ontouchstart = function(e) {
                    if (e.target.tagName === 'BUTTON' || e.target.closest('button')) return;
                    var t = e.touches[0];
                    dx = t.clientX;
                    dy = t.clientY;
                    document.ontouchend = function() {
                        document.ontouchend = null;
                        document.ontouchmove = null;
                    };
                    document.ontouchmove = function(e) {
                        var t = e.touches[0];
                        x = dx - t.clientX;
                        y = dy - t.clientY;
                        dx = t.clientX;
                        dy = t.clientY;
                        elem.style.top = (elem.offsetTop - y) + "px";
                        elem.style.left = (elem.offsetLeft - x) + "px";
                        elem.style.bottom = 'auto';
                        elem.style.right = 'auto';
                    };
                };
            }

            // Gestionar estado y apariencia de los botones de acción durante la revisión minimizada
            function actualizarBloqueoBotonesRevision(bloquear) {
                var btnRev = document.getElementById('btnRevisarForm');
                var btnVolver = document.getElementById('btnVolverForm');
                var btnSync = document.getElementById('btnSincronizarDriveForm');
                var btnGuardar = document.getElementById('btnGuardarForm');
                var btnRadicar = document.getElementById('btnRadicarForm');

                if (bloquear) {
                    if (btnRev) {
                        btnRev.className = 'btn btn-warning px-4 shadow-sm text-dark fw-bold border-2 border-warning';
                        btnRev.innerHTML = '<i class="bi bi-arrows-angle-expand me-2"></i>Continuar Revisión';
                        btnRev.title = 'Revisión en curso minimizada. Clic para restaurar y finalizar.';
                    }
                    [btnVolver, btnSync, btnGuardar, btnRadicar].forEach(function(btn) {
                        if (btn) {
                            btn.dataset.bloqueadoPorRevision = 'true';
                            btn.style.opacity = '0.45';
                            btn.style.cursor = 'not-allowed';
                        }
                    });
                } else {
                    if (btnRev) {
                        btnRev.className = 'btn btn-outline-primary px-4 shadow-sm';
                        btnRev.innerHTML = '<i class="bi bi-check2-square me-2"></i>Revisar';
                        btnRev.title = '';
                    }
                    [btnVolver, btnSync, btnGuardar, btnRadicar].forEach(function(btn) {
                        if (btn) {
                            delete btn.dataset.bloqueadoPorRevision;
                            btn.style.opacity = '';
                            btn.style.cursor = '';
                        }
                    });
                }
            }

            // Interceptar cualquier acción secundaria si hay revisión minimizada
            window.verificarBloqueoRevision = function(e, nombreAccion, accionProceder) {
                if (_modalRevisionState && _modalRevisionState.activo) {
                    if (e) {
                        e.preventDefault();
                        e.stopPropagation();
                        if (e.stopImmediatePropagation) e.stopImmediatePropagation();
                    }
                    Swal.fire({
                        title: 'Revisión en curso',
                        html: 'Tienes una revisión abierta y minimizada con observaciones en proceso.<br><br>' +
                              'No es posible <b>' + nombreAccion + '</b> mientras la revisión esté pendiente.<br><br>' +
                              '¿Deseas restaurar la ventana de revisión o descartarla para proceder?',
                        icon: 'warning',
                        showCancelButton: true,
                        confirmButtonText: '<i class="bi bi-arrows-angle-expand me-1"></i> Continuar Revisión',
                        confirmButtonColor: '#0d6efd',
                        cancelButtonText: '<i class="bi bi-trash3 me-1"></i> Descartar y proceder',
                        cancelButtonColor: '#dc3545'
                    }).then(function(res) {
                        if (res.isConfirmed) {
                            restaurarModalRevision();
                        } else if (res.dismiss === Swal.DismissReason.cancel) {
                            _modalRevisionState = { activo: false };
                            var w = document.getElementById('widgetRevisionMinimizado');
                            if (w) w.remove();
                            actualizarBloqueoBotonesRevision(false);
                            if (typeof accionProceder === 'function') {
                                accionProceder();
                            }
                        }
                    });
                    return true;
                }
                return false;
            };

            window.manejarClickBotonRevisar = function(idInforme, estadoActual, tipoRolModal) {
                if (_modalRevisionState && _modalRevisionState.activo) {
                    restaurarModalRevision();
                } else {
                    abrirModalRevision(idInforme, estadoActual, tipoRolModal);
                }
            };

            function mostrarWidgetRevisionMinimizado() {
                var viejo = document.getElementById('widgetRevisionMinimizado');
                if (viejo) viejo.remove();

                var accionBadge = 'bg-primary';
                if (_modalRevisionState.accion === 'DEVUELTA') accionBadge = 'bg-danger';
                else if (_modalRevisionState.accion === 'APROBADA PARA IMPRESION') accionBadge = 'bg-success';
                else if (_modalRevisionState.accion === 'EN REVISION SUPERVISOR') accionBadge = 'bg-info text-dark';

                var textoObs = _modalRevisionState.obs ? _modalRevisionState.obs.trim() : '';
                var preview = textoObs.length > 0 ? (textoObs.substring(0, 55) + (textoObs.length > 55 ? '...' : '')) : '(Sin observaciones escritas aún)';

                var div = document.createElement('div');
                div.id = 'widgetRevisionMinimizado';
                div.className = 'card shadow-lg border-primary';
                // Ubicado en la esquina inferior izquierda para dejar libres los botones de acción principales
                div.style.cssText = 'position: fixed; bottom: 25px; left: 25px; width: 360px; max-width: calc(100vw - 50px); z-index: 1070; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.25);';
                div.innerHTML = `
                    <div id="widgetRevisionHeader" class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-2 px-3" style="cursor: grab; user-select: none;">
                        <div class="d-flex align-items-center">
                            <i class="bi bi-grip-vertical me-1 fs-5 text-white-50"></i>
                            <i class="bi bi-pencil-square me-2 fs-6"></i>
                            <strong style="font-size: 0.88rem;">Revisión en Curso</strong>
                        </div>
                        <div>
                            <button type="button" class="btn btn-sm btn-light py-0 px-2 me-1" title="Restaurar / Continuar escribiendo" onclick="restaurarModalRevision()">
                                <i class="bi bi-arrows-angle-expand"></i>
                            </button>
                            <button type="button" class="btn btn-sm btn-outline-light py-0 px-2" title="Cancelar revisión" onclick="descartarModalRevisionMinimizado()">
                                <i class="bi bi-x-lg"></i>
                            </button>
                        </div>
                    </div>
                    <div class="card-body p-2 bg-light">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <span class="badge ` + accionBadge + `" style="font-size: 0.74rem;">` + (_modalRevisionState.accion || 'REVISIÓN') + `</span>
                            <small class="text-muted">` + textoObs.length + ` caracteres</small>
                        </div>
                        <p class="small text-truncate mb-0 text-muted" style="max-height: 40px; font-style: italic;">
                            ` + preview + `
                        </p>
                    </div>
                    <div class="card-footer bg-white p-2 d-flex justify-content-between align-items-center">
                        <small class="text-success fw-bold" style="font-size: 0.75rem;">
                            <i class="bi bi-check-circle-fill me-1"></i>Texto guardado (Arrastrable)
                        </small>
                        <button type="button" class="btn btn-primary btn-sm px-3 py-1 shadow-sm" onclick="restaurarModalRevision()">
                            <i class="bi bi-arrows-angle-expand me-1"></i>Maximizar
                        </button>
                    </div>
                `;
                document.body.appendChild(div);
                hacerDraggable(div, document.getElementById('widgetRevisionHeader'));
                actualizarBloqueoBotonesRevision(true);
            }

            window.restaurarModalRevision = function() {
                var w = document.getElementById('widgetRevisionMinimizado');
                if (w) w.remove();
                if (_modalRevisionState.activo) {
                    abrirModalRevision(_modalRevisionState.idInforme, _modalRevisionState.estadoActual, _modalRevisionState.tipoRolModal);
                }
            };

            window.descartarModalRevisionMinimizado = function() {
                Swal.fire({
                    title: '¿Descartar revisión?',
                    text: 'Si cancelas ahora, se perderán las observaciones redactadas.',
                    icon: 'warning',
                    showCancelButton: true,
                    confirmButtonColor: '#dc3545',
                    cancelButtonColor: '#6c757d',
                    confirmButtonText: 'Sí, descartar',
                    cancelButtonText: 'Continuar revisando'
                }).then((r) => {
                    if (r.isConfirmed) {
                        _modalRevisionState = { activo: false };
                        var w = document.getElementById('widgetRevisionMinimizado');
                        if (w) w.remove();
                        actualizarBloqueoBotonesRevision(false);
                    }
                });
            };

            // Advertencia al intentar salir o recargar la pestaña si hay una revisión minimizada activa
            window.addEventListener('beforeunload', function(e) {
                if (_modalRevisionState && _modalRevisionState.activo) {
                    e.preventDefault();
                    e.returnValue = 'Tienes una revisión en curso con notas pendientes.';
                    return e.returnValue;
                }
            });

            // Función para modal de Revisión con opción Minimizar
            function abrirModalRevision(idInforme, estadoActual, tipoRolModal) {
                let opcionesHTML = '';
                let opcionesRevisores = `<option value="">-- Seleccione un Revisor --</option>
                    <c:forEach var="r" items="${listaRevisores}">
                        <option value="${r.id}">${r.nombreCompleto}</option>
                    </c:forEach>`;
                
                // Compatibilidad si llega booleano legado
                if (typeof tipoRolModal === 'boolean') {
                    if (tipoRolModal) {
                        tipoRolModal = (estadoActual === 'RADICADA') ? 'contratacion_sin_revisor' : 'contratacion_con_revisor';
                    } else {
                        tipoRolModal = 'revisor';
                    }
                }

                if (tipoRolModal === 'supervisor') {
                    // El Supervisor da la Aprobación Definitiva para impresión
                    opcionesHTML = `
                        <option value="APROBADA PARA IMPRESION">Aprobación Definitiva (Lista para imprimir)</option>
                        <option value="DEVUELTA">Devolver al Contratista (Requiere corrección)</option>
                    `;
                } else if (tipoRolModal === 'contratacion_con_revisor') {
                    // Contratación revisa cuenta que viene de un Revisor y la pasa al Supervisor
                    opcionesHTML = `
                        <option value="EN REVISION SUPERVISOR">Dar Visto Bueno (Pasar a Supervisión)</option>
                        <option value="DEVUELTA">Devolver al Contratista (Requiere corrección)</option>
                    `;
                } else if (tipoRolModal === 'contratacion_sin_revisor') {
                    // Contratación revisa cuenta radicada SIN revisor (revisa primero y puede pasar a revisor, mandar a imprimir o al supervisor)
                    opcionesHTML = `
                        <option value="VISTO BUENO CONTRATACION">Dar Visto Bueno Previo (Pasar a Revisor)</option>
                        <option value="APROBADA PARA IMPRESION">Mandar a Imprimir (Aprobación Definitiva)</option>
                        <option value="EN REVISION SUPERVISOR">Mandar al Supervisor (Pasar a Supervisión)</option>
                        <option value="DEVUELTA">Devolver al Contratista (Requiere corrección)</option>
                    `;
                } else if (tipoRolModal === 'revisor_post_contratacion' || (tipoRolModal === 'revisor' && estadoActual === 'VISTO BUENO CONTRATACION')) {
                    // Revisor en flujo Sin Revisor: Contratación revisó primero y ahora el Revisor decide mandar a supervisor o visto bueno para imprimir
                    opcionesHTML = `
                        <option value="EN REVISION SUPERVISOR">Mandar al Supervisor (Pasar a Supervisión)</option>
                        <option value="APROBADA PARA IMPRESION">Dar Visto Bueno para Imprimir (Aprobación Definitiva)</option>
                        <option value="DEVUELTA">Devolver al Contratista (Requiere corrección)</option>
                    `;
                } else {
                    // Revisor inicial (flujo normal con revisor seleccionado desde el inicio):
                    opcionesHTML = `
                        <option value="EN REVISION CONTRATACION">Dar Visto Bueno (Pasar a Contratación)</option>
                        <option value="DEVUELTA">Devolver al Contratista (Requiere corrección)</option>
                    `;
                }

                _isMinimizingModal = false;

                Swal.fire({
                    title: 'Revisión de Cuenta de Cobro',
                    html: `
                        <div class="d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom">
                            <span class="text-primary fw-bold" style="font-size: 0.9rem;">
                                <i class="bi bi-shield-check me-1"></i>Acción de Revisión
                            </span>
                            <button type="button" class="btn btn-sm btn-outline-secondary py-1 px-2 shadow-sm" id="btn-minimizar-swal" title="Minimizar para consultar o editar la pantalla libremente">
                                <i class="bi bi-dash-square me-1"></i>Minimizar
                            </button>
                        </div>
                        <p class="text-muted text-start mb-2">Seleccione la acción a tomar para esta cuenta:</p>
                        <div class="mb-3">
                            <select id="swal-accion" class="form-select">
                                ` + opcionesHTML + `
                            </select>
                        </div>
                        <div class="mb-3" id="div-revisor" style="display: none;">
                            <label class="form-label text-start w-100 fw-bold text-primary">Seleccione el Revisor:</label>
                            <select id="swal-revisor" class="form-select">
                                ` + opcionesRevisores + `
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label text-start w-100">Observaciones (Obligatorio si devuelve):</label>
                            <textarea id="swal-obs" class="form-control" rows="4" placeholder="Escriba aquí el motivo o sugerencias..."></textarea>
                        </div>
                    `,
                    allowOutsideClick: false,
                    allowEscapeKey: false,
                    showCancelButton: true,
                    confirmButtonText: '<i class="bi bi-check-circle me-1"></i> Aplicar',
                    cancelButtonText: '<i class="bi bi-x-circle me-1"></i> Cancelar',
                    didOpen: () => {
                        const accionSelect = document.getElementById('swal-accion');
                        const divRevisor = document.getElementById('div-revisor');
                        const btnMinimizar = document.getElementById('btn-minimizar-swal');
                        
                        accionSelect.addEventListener('change', () => {
                            if (accionSelect.value === 'VISTO BUENO CONTRATACION') {
                                divRevisor.style.display = 'block';
                            } else {
                                divRevisor.style.display = 'none';
                            }
                        });

                        // Restaurar valores si venía de un estado minimizado
                        if (_modalRevisionState.activo && _modalRevisionState.idInforme === idInforme) {
                            if (_modalRevisionState.accion) {
                                accionSelect.value = _modalRevisionState.accion;
                            }
                            if (_modalRevisionState.obs) {
                                document.getElementById('swal-obs').value = _modalRevisionState.obs;
                            }
                            if (_modalRevisionState.revisorId && document.getElementById('swal-revisor')) {
                                document.getElementById('swal-revisor').value = _modalRevisionState.revisorId;
                            }
                        }

                        accionSelect.dispatchEvent(new Event('change'));

                        // Manejador del botón Minimizar
                        if (btnMinimizar) {
                            btnMinimizar.addEventListener('click', function() {
                                var curAccion = document.getElementById('swal-accion') ? document.getElementById('swal-accion').value : '';
                                var curObs = document.getElementById('swal-obs') ? document.getElementById('swal-obs').value : '';
                                var curRev = document.getElementById('swal-revisor') ? document.getElementById('swal-revisor').value : '';

                                _modalRevisionState = {
                                    activo: true,
                                    idInforme: idInforme,
                                    estadoActual: estadoActual,
                                    tipoRolModal: tipoRolModal,
                                    accion: curAccion,
                                    obs: curObs,
                                    revisorId: curRev
                                };

                                _isMinimizingModal = true;
                                Swal.close();
                                mostrarWidgetRevisionMinimizado();
                            });
                        }
                    },
                    preConfirm: () => {
                        const accion = document.getElementById('swal-accion').value;
                        const obs = document.getElementById('swal-obs').value;
                        const revisorId = document.getElementById('swal-revisor') ? document.getElementById('swal-revisor').value : '';
                        
                        if (accion === 'DEVUELTA' && !obs.trim()) {
                            Swal.showValidationMessage('Debe escribir una observación para poder devolverla.');
                            return false;
                        }
                        if (accion === 'VISTO BUENO CONTRATACION' && !revisorId) {
                            Swal.showValidationMessage('Debe seleccionar un revisor.');
                            return false;
                        }
                        return { accion: accion, obs: obs, revisorId: revisorId };
                    }
                }).then((result) => {
                    if (_isMinimizingModal) {
                        return;
                    }

                    if (result.isConfirmed) {
                        _modalRevisionState = { activo: false };
                        var w = document.getElementById('widgetRevisionMinimizado');
                        if (w) w.remove();
                        actualizarBloqueoBotonesRevision(false);

                        const form = document.createElement('form');
                        form.method = 'POST';
                        form.action = 'procesar_revision.jsp';
                        
                        if (result.value.revisorId) {
                            const inputRevisor = document.createElement('input');
                            inputRevisor.type = 'hidden';
                            inputRevisor.name = 'revisor_id';
                            inputRevisor.value = result.value.revisorId;
                            form.appendChild(inputRevisor);
                        }
                        
                        const inputId = document.createElement('input');
                        inputId.type = 'hidden';
                        inputId.name = 'id_informe';
                        inputId.value = idInforme;
                        
                        const inputAccion = document.createElement('input');
                        inputAccion.type = 'hidden';
                        inputAccion.name = 'accion';
                        inputAccion.value = result.value.accion;
                        
                        const inputObs = document.createElement('input');
                        inputObs.type = 'hidden';
                        inputObs.name = 'observacion';
                        inputObs.value = result.value.obs;
                        
                        const inputModo = document.createElement('input');
                        inputModo.type = 'hidden';
                        inputModo.name = 'modo';
                        inputModo.value = '${not empty param.modo ? param.modo : ''}';
                        
                        form.appendChild(inputId);
                        form.appendChild(inputAccion);
                        form.appendChild(inputObs);
                        form.appendChild(inputModo);
                        
                        document.body.appendChild(form);
                        form.submit();
                    } else if (result.dismiss === Swal.DismissReason.cancel) {
                        _modalRevisionState = { activo: false };
                        var w = document.getElementById('widgetRevisionMinimizado');
                        if (w) w.remove();
                        actualizarBloqueoBotonesRevision(false);
                    }
                });
            }

            $(document).ready(function() {
                function fetchConsecutivo() {
                    var rawCedula = '<c:out value="${contrato.contratista != null ? contrato.contratista.cedula : sessionScope.usuario.cedula}" />';
                    var cedula = rawCedula ? rawCedula.replace(/[^0-9]/g, '') : '';
                    var contrato = '${contrato.numeroContrato}';
                    var cuota = $('[name="numero_cuota"]').val();
                    var anio = '${not empty contrato.anio ? contrato.anio : ""}';
                    if (!anio) {
                        var fechaTerminacion = '${contrato.fechaTerminacion}';
                        if (fechaTerminacion && fechaTerminacion.length >= 4) {
                            anio = fechaTerminacion.substring(0, 4); // YYYY-MM-DD
                        }
                    }
                    
                    if (contrato && cuota) {
                        $.post('${pageContext.request.contextPath}/consecutivos', {
                            action: 'check',
                            cedula: cedula,
                            contrato: contrato,
                            cuota: cuota,
                            anio: anio
                        }, function(response) {
                            var inputCons = $('[name="consecutivo_cobro"]');
                            if (response && response.success && response.consecutivo) {
                                // Solo reemplazar si está vacío o si el usuario no ha forzado uno diferente
                                if (!inputCons.val() || inputCons.hasClass('auto-filled')) {
                                    inputCons.val(response.consecutivo)
                                             .addClass('auto-filled is-valid')
                                             .attr('readonly', true)
                                             .attr('title', 'Consecutivo cargado automáticamente por el administrador');
                                }
                            } else {
                                // Si no encuentra consecutivo y el campo había sido autocompletado antes, lo limpiamos y lo desbloqueamos
                                if (inputCons.hasClass('auto-filled')) {
                                    inputCons.val('')
                                             .removeClass('auto-filled is-valid')
                                             .attr('readonly', false)
                                             .removeAttr('title');
                                }
                            }
                        }, 'json');
                    }
                }

                $('[name="numero_cuota"]').on('change keyup', function() {
                    fetchConsecutivo();
                });
                
                // Ejecutar al cargar la página si la cuota ya está seleccionada y consecutivo vacío
                if ($('[name="numero_cuota"]').val() && !$('[name="consecutivo_cobro"]').val()) {
                    fetchConsecutivo();
                }

                // ==========================================
                // AUTOSAVE Y RECUPERACION DE DATOS
                // ==========================================
                if (!'${readonly}' || '${readonly}' === 'false' || '${readonly}' === '') {
                    var contratoId = '${contrato.id}';
                    var informeId = '${informe != null ? informe.id : "0"}';
                    var autoSaveKey = 'autosave_contrato_' + contratoId + '_informe_' + informeId;
                    var formHasChanges = false;
                    
                    // Detectar si el usuario escribe algo
                    $('#informeForm').on('input change keyup paste', 'input, select, textarea', function() {
                        formHasChanges = true;
                    });
                    
                    // Función para guardar datos
                    function guardarBorradorLocal() {
                        if (!formHasChanges) return; // Solo guardar si realmente modificaron algo en esta sesión
                        
                        var formData = {};
                        // Guardar inputs normales (text, number, date, checkbox, etc) y selects
                        $('#informeForm').find('input:not([type="file"]):not([type="hidden"]), select, textarea:not(.summernote-editor)').each(function() {
                            var name = $(this).attr('name');
                            if (name) {
                                formData[name] = $(this).is(':checkbox') ? $(this).is(':checked') : $(this).val();
                            }
                        });
                        // Guardar Summernote
                        $('.summernote-editor').each(function(index) {
                            formData['summernote_' + index] = $(this).summernote('code');
                        });
                        
                        localStorage.setItem(autoSaveKey, JSON.stringify({
                            time: new Date().getTime(),
                            data: formData
                        }));
                    }

                    // Autoguardado cada 10 segundos
                    setInterval(guardarBorradorLocal, 10000);
                    
                    // Al enviar el formulario, limpiar el borrador local ya que se está guardando en el servidor
                    $('#informeForm').on('submit', function(e) {
                        if (_modalRevisionState && _modalRevisionState.activo) {
                            e.preventDefault();
                            verificarBloqueoRevision(e, 'guardar o radicar el informe', function() {
                                document.getElementById('informeForm').submit();
                            });
                            return false;
                        }
                        try { localStorage.removeItem(autoSaveKey); } catch(e) {}
                        formHasChanges = false;
                    });

                    // Limpiar autosave en formularios de edición si no hay un error devuelto por el servidor
                    var serverError = '${error}';
                    var isEditing = ('${action}' === 'update');

                    // Si estamos editando un informe existente que viene de la base de datos y no hubo error del servidor,
                    // la base de datos es la única fuente de verdad; descartamos borradores viejos.
                    if (isEditing && (!serverError || serverError.trim() === '')) {
                        try { localStorage.removeItem(autoSaveKey); } catch(e) {}
                    } else if (serverError && serverError.trim() !== '') {
                        // Solo si hubo un error devuelto por el servidor intentamos avisar de recuperación
                        var guardado = localStorage.getItem(autoSaveKey);
                        if (guardado) {
                            try {
                                var parseado = JSON.parse(guardado);
                                var timeDiff = (new Date().getTime() - parseado.time) / (1000 * 60 * 60); // Horas
                                
                                if (timeDiff < 24) {
                                    Swal.fire({
                                        title: 'Recuperación de datos',
                                        text: 'El sistema detectó un inconveniente con el guardado en el servidor. ¿Deseas restaurar la información que estabas escribiendo?',
                                        icon: 'info',
                                        showCancelButton: true,
                                        confirmButtonText: 'Sí, restaurar',
                                        cancelButtonText: 'No, descartar'
                                    }).then((result) => {
                                        if (result.isConfirmed) {
                                            var d = parseado.data;
                                            // Restaurar inputs y textareas normales
                                            $('#informeForm').find('input:not([type="file"]):not([type="hidden"]), select, textarea:not(.summernote-editor)').each(function() {
                                                var name = $(this).attr('name');
                                                if (name && d[name] !== undefined && d[name] !== null) {
                                                    if ($(this).is(':checkbox')) {
                                                        $(this).prop('checked', !!d[name]).trigger('change');
                                                    } else {
                                                        $(this).val(d[name]);
                                                    }
                                                }
                                            });
                                            // Restaurar Summernotes
                                            $('.summernote-editor').each(function(index) {
                                                var html = d['summernote_' + index];
                                                if (html) {
                                                    $(this).summernote('code', html);
                                                }
                                            });
                                            
                                            // Trigger recalcular
                                            calcularSaldo();
                                            formHasChanges = true;
                                            Swal.fire('¡Restaurado!', 'La información ha sido recuperada.', 'success');
                                        } else {
                                            localStorage.removeItem(autoSaveKey);
                                        }
                                    });
                                }
                            } catch (e) {
                                console.error('Error recuperando autosave', e);
                            }
                        }
                    }
                    
                    // Asegurar que Summernote también marque cambios
                    setTimeout(function() {
                        $('.summernote-editor').on('summernote.change', function() {
                            formHasChanges = true;
                        });
                    }, 1000);
                }
            });
        </script>
    </body>
</html>

