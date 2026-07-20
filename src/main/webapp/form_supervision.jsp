<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    // Recuperar observaciones_revision directamente de la BD para no tocar el backend Java
    String obsRev = "";
    try {
        if (request.getAttribute("informe") != null) {
            com.combinacion.models.InformeSupervision inf = (com.combinacion.models.InformeSupervision)request.getAttribute("informe");
            if (inf.getId() > 0) {
                try (java.sql.Connection conn = com.combinacion.util.DBConnection.getConnection();
                     java.sql.PreparedStatement ps = conn.prepareStatement("SELECT observaciones_revision FROM informes_supervision WHERE id = ?")) {
                    ps.setInt(1, inf.getId());
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            obsRev = rs.getString(1);
                        }
                    }
                }
            }
        }
    } catch (Exception ignore) {}
    request.setAttribute("obsRevision", obsRev);
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <c:set var="esAdminCuentas" value="${sessionScope.usuario.tienePermiso('ADMINISTRAR_CUENTAS') || sessionScope.usuario.tienePermiso('ADMINISTRAR_CUENTAS_EDITAR') || sessionScope.usuario.esAdministrador()}" />
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${action == 'view' ? 'Ver' : 'Nuevo'} Informe de Supervisión - Gestión de Prestadores</title>
    <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon">
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
    
    <!-- Summernote Lite CSS -->
    <link href="https://cdn.jsdelivr.net/npm/summernote@0.8.18/dist/summernote-lite.min.css" rel="stylesheet">
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
        <li class="breadcrumb-item active" aria-current="page">${action == 'view' ? 'Ver' : 'Editar'} Informe de Supervisión</li>
    </ol>
</nav>
            </div>
        </div>

        <div class="card border">
            <div class="card-body p-4">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2 class="fw-bold m-0 text-dark">
                        <i class="bi bi-file-earmark-check me-2 text-primary"></i>
                        ${action == 'view' ? 'Detalle de' : 'Nuevo'} Informe de Supervisión
                    </h2>
                    <span class="badge bg-primary px-3 py-2">MAJA01.04.03.P002.F003</span>
                </div>

                <form action="informes?action=${action}<c:if test="${action == 'update'}">&id=${informe.id}</c:if>" method="POST" id="informeForm" class="needs-validation" enctype="multipart/form-data" novalidate>
                    <input type="hidden" name="action" value="${action}">
                    <input type="hidden" name="contrato_id" value="${contrato.id}">
                    <c:if test="${action == 'update'}">
                        <input type="hidden" name="id" value="${informe.id}">
                        <input type="hidden" name="soportes_json" value="${fn:escapeXml(informe.soportesJson)}">
                    </c:if>

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger d-flex align-items-center mb-4" role="alert">
                            <i class="bi bi-exclamation-triangle-fill me-3 fs-4"></i>
                            <div>
                                <strong>Error:</strong> ${error}
                            </div>
                        </div>
                    </c:if>



                    <!-- Tabs -->
                    <ul class="nav nav-tabs mb-4" id="informeTabs" role="tablist">
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
                            <button class="nav-link" id="soportes-tab" data-bs-toggle="tab" data-bs-target="#soportes" type="button">Evidencias / Soportes</button>
                        </li>
                    </ul>

                    <div class="tab-content" id="informeTabsContent">
                        <!-- Tab 1: General -->
                        <div class="tab-pane fade show active" id="general" role="tabpanel">
                            <div class="section-title">Aspectos Generales</div>
                            <div class="row g-3">
                                <div class="col-md-4">
                                    <label class="form-label">No. de Contrato</label>
                                    <input type="text" class="form-control bg-light text-muted" value="${contrato.numeroContrato}" readonly>
                                </div>
                                <div class="col-md-8">
                                    <label class="form-label">Objeto del Contrato</label>
                                    <textarea class="form-control bg-light text-muted" rows="2" readonly>${contrato.objeto}</textarea>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Periodo del Informe (Mes y Año)</label>
                                    <input type="text" class="form-control" name="periodo_informe" value="${informe.periodoInforme}" placeholder="Ej: Enero 2026" required ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-2">
                                    <label class="form-label">Tipo de Informe</label>
                                    <select class="form-select" name="tipo_informe" required ${readonly ? 'disabled' : ''}>
                                        <option value="PARCIAL" ${informe.tipoInforme == 'PARCIAL' ? 'selected' : ''}>INFORME PARCIAL</option>
                                        <option value="FINAL" ${informe.tipoInforme == 'FINAL' ? 'selected' : ''}>INFORME FINAL</option>
                                    </select>
                                </div>
                                <div class="col-md-2">
                                    <label class="form-label">Cuota Número</label>
                                    <c:choose>
                                        <c:when test="${not empty contrato.numCuotasNumero && contrato.numCuotasNumero > 0 && not readonly}">
                                            <select class="form-select" name="numero_cuota" required>
                                                <option value="" disabled ${empty informe.numeroCuota && empty siguienteCuota ? 'selected' : ''}>Seleccione...</option>
                                                <c:forEach var="i" begin="1" end="${contrato.numCuotasNumero}">
                                                    <option value="${i}" ${(not empty informe.numeroCuota && informe.numeroCuota == i) || (empty informe.id && siguienteCuota == i) ? 'selected' : ''}>Cuota ${i}</option>
                                                </c:forEach>
                                            </select>
                                        </c:when>
                                        <c:otherwise>
                                            <input type="text" class="form-control" name="numero_cuota" value="${informe.numeroCuota}" placeholder="Ej: 1" required ${readonly ? 'readonly' : ''}>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="col-md-2">
                                    <label class="form-label" title="Para la cuenta de cobro">Consecutivo</label>
                                    <input type="text" class="form-control" name="consecutivo_cobro" value="${informe.consecutivoCobro}" placeholder="Ej: 0411" ${readonly ? 'readonly' : ''}>
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
                            <div class="section-title">Informe Jurídico</div>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Modificación al contrato</label>
                                    <textarea class="form-control" name="modificaciones" rows="2" ${readonly ? 'readonly' : ''}>${empty informe.modificaciones ? 'N/A' : informe.modificaciones}</textarea>
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
                                    <input type="text" class="form-control" name="adiciones" value="${empty informe.adiciones ? 'N/A' : informe.adiciones}" ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Prórroga</label>
                                    <input type="text" class="form-control" name="prorrogas" value="${empty informe.prorrogas ? 'N/A' : informe.prorrogas}" ${readonly ? 'readonly' : ''}>
                                </div>
                            </div>
                        </div>

                        <!-- Tab 3: Financiero -->
                        <div class="tab-pane fade" id="financiero" role="tabpanel">
                            <div class="section-title">Informe Contable y Financiero</div>
                            <div class="row g-3">
                                <div class="col-md-3">
                                    <label class="form-label">Valor Total del Contrato ($)</label>
                                    <input type="text" id="valor_total" class="form-control bg-light text-muted fw-bold money-mask" value="${contrato.valorTotalNumeros}" readonly>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Valor Cuota a Cancelar ($)</label>
                                    <input type="text" id="valor_cuota" class="form-control money-mask" name="valor_cuota_pagar" value="${not empty informe.valorCuotaPagar ? informe.valorCuotaPagar : contrato.valorCuotaNumero}" required ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Valor Acumulado Cancelado ($)</label>
                                    <input type="text" id="valor_acumulado" class="form-control bg-light text-muted money-mask" name="valor_acumulado_pagado" 
                                           value="${empty informe.id ? acumuladoPrevio : informe.valorAccumuladoPagado}" placeholder="0" readonly>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Saldo por Cancelar ($)</label>
                                    <input type="text" id="saldo_cancelar" class="form-control bg-light money-mask fw-bold" name="saldo_por_cancelar" value="${informe.saldoPorCancelar}" placeholder="0" readonly>
                                </div>
                            </div>
                        </div>

                        <!-- Tab 4: Seguridad Social -->
                        <div class="tab-pane fade" id="seguridad" role="tabpanel">
                            <div class="section-title">Información del Pago de Seguridad Social</div>
                            <div class="row g-3">
                                <div class="col-md-4">
                                    <label class="form-label">Número de Planilla</label>
                                    <input type="text" class="form-control" name="planilla_numero" value="${informe.planillaNumero}" required ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">PIN / Autorización / Referencia</label>
                                    <input type="text" class="form-control" name="planilla_pin" value="${informe.planillaPin}" required ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Operador</label>
                                    <input type="text" class="form-control" name="planilla_operador" value="${informe.planillaOperador}" placeholder="Ej: Aportes en Línea" required ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Fecha de Pago</label>
                                    <input type="date" class="form-control" name="planilla_fecha_pago" value="<fmt:formatDate value='${informe.planillaFechaPago}' pattern='yyyy-MM-dd'/>" required ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Periodo de Pago</label>
                                    <input type="text" class="form-control" name="planilla_periodo" value="${informe.planillaPeriodo}" placeholder="Ej: 2026-01" required ${readonly ? 'readonly' : ''}>
                                </div>
                            </div>
                        </div>

                        <!-- Tab 5: Técnico -->
                        <div class="tab-pane fade" id="tecnico" role="tabpanel">
                            <div class="section-title">Informe Técnico y Recomendaciones</div>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Recibo a Satisfacción de Servicios</label>
                                    <textarea class="form-control" name="recibo_satisfaccion" rows="2" ${readonly ? 'readonly' : ''}>${empty informe.reciboSatisfaccion ? 'N/A' : informe.reciboSatisfaccion}</textarea>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Constancia de Paz y Salvo</label>
                                    <textarea class="form-control" name="constancia_paz_salvo" rows="2" ${readonly ? 'readonly' : ''}>${empty informe.constanciaPazSalvo ? 'N/A' : informe.constanciaPazSalvo}</textarea>
                                </div>
                                <div class="col-md-12">
                                    <label class="form-label">Concepto Supervisor (Obligaciones y Actividades)</label>
                                    <input type="hidden" name="obligaciones_count" value="${fn:length(listaObligaciones)}">
                                    <div class="table-responsive">
                                        <table class="table table-bordered align-middle">
                                            <thead class="table-light">
                                                <tr>
                                                    <th style="width: 40%">Obligaciones del Contratista</th>
                                                    <th style="width: 60%">Actividades (Use viñetas o guiones)</th>
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
                                    <textarea class="form-control" name="observaciones_tecnicas" rows="2" placeholder="Observaciones adicionales..." required ${readonly ? 'readonly' : ''}>${informe.observacionesTecnicas}</textarea>
                                </div>
                                <div class="col-md-12">
                                    <label class="form-label">Recomendaciones para el Contratista</label>
                                    <textarea class="form-control" name="recomendaciones" rows="2" ${readonly ? 'readonly' : ''}>${empty informe.id ? 'No se reportan recomendaciones para este periodo' : informe.recomendaciones}</textarea>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Fecha de Suscripción del Informe</label>
                                    <c:choose>
                                        <c:when test="${not empty informe.fechaSuscripcion}">
                                            <input type="date" class="form-control" name="fecha_suscripcion" value="<fmt:formatDate value='${informe.fechaSuscripcion}' pattern='yyyy-MM-dd'/>" required ${readonly ? 'readonly' : ''}>
                                        </c:when>
                                        <c:otherwise>
                                            <input type="date" class="form-control" name="fecha_suscripcion" value="<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) %>" required ${readonly ? 'readonly' : ''}>
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
                                <c:if test="${contrato.ivaSiNo == 'SI' || contrato.ivaSiNo == 'Si' || contrato.ivaSiNo == 'si'}">
                                    <div class="col-md-12 mb-2">
                                        <div class="alert alert-warning mb-0 border-start border-warning border-4 shadow-sm">
                                            <i class="bi bi-exclamation-triangle-fill me-2 text-warning fs-5"></i>
                                            <strong>Atención:</strong> El contratista es <strong>Responsable de IVA</strong>. En lugar de generar el formato de cuenta de cobro en Excel (DS), debe cargar su factura electrónica a continuación.
                                        </div>
                                    </div>
                                    <div class="col-md-6 req-cuota-todas">
                                        <label class="form-label text-primary fw-bold"><i class="bi bi-receipt"></i> Factura Electrónica (Reemplaza Cuenta de Cobro)</label>
                                        <input type="file" class="form-control border-primary" name="file_factura" accept="application/pdf, text/xml" ${readonly ? 'disabled' : ''}>
                                    </div>
                                </c:if>
                                <div class="col-md-6 req-cuota-1">
                                    <label class="form-label">RUT</label>
                                    <input type="file" class="form-control" name="file_rut" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6 req-cuota-1">
                                    <label class="form-label">Cédula</label>
                                    <input type="file" class="form-control" name="file_cedula" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6 req-cuota-1">
                                    <label class="form-label">Contrato Secop II</label>
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
                                    <label class="form-label">Ficha Técnica</label>
                                    <input type="file" class="form-control" name="file_ficha_tecnica" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6 req-cuota-todas">
                                    <label class="form-label">Seguridad Social</label>
                                    <input type="file" class="form-control" name="file_seguridad_social" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                 <div class="col-md-6 req-cuota-todas">
                                    <label class="form-label">RPC (Registro Presupuestal)</label>
                                    <input type="file" class="form-control" name="file_rpc" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                            </div>
                        </div>

                    </div>

                    
                    <!-- Sección de Radicación o Asignación -->
                    <c:set var="esBorradorDevuelta" value="${empty informe.estadoRadicacion || informe.estadoRadicacion == 'BORRADOR' || informe.estadoRadicacion == 'DEVUELTA'}" />
                    
                    <c:if test="${not readonly && (esBorradorDevuelta || esAdminCuentas)}">
                        <div class="card mt-4 border-primary bg-light">
                            <div class="card-body">
                                <c:choose>
                                    <c:when test="${esBorradorDevuelta}">
                                        <h5 class="card-title text-primary"><i class="bi bi-send-check"></i> Radicar Cuenta de Cobro</h5>
                                        <p class="card-text text-muted small">Seleccione la persona encargada de revisar su cuenta y haga clic en Radicar.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <h5 class="card-title text-primary"><i class="bi bi-person-gear"></i> Asignar / Cambiar Revisor</h5>
                                        <p class="card-text text-muted small">Como administrador, puedes reasignar esta cuenta a otro revisor sin cambiar su estado.</p>
                                    </c:otherwise>
                                </c:choose>
                                <div class="row align-items-center">
                                    <div class="col-md-6">
                                        <label class="form-label">Asignar a Revisor:</label>
                                        <select class="form-select" name="id_revisor_asignado" id="revisor_select">
                                            <option value="">-- Seleccione un Revisor --</option>
                                            <option value="0">-- Sin Revisor (Pasar directo a Contratación) --</option>
                                            <c:forEach var="rev" items="${listaRevisores}">
                                                <option value="${rev.id}" ${informe.idRevisorAsignado == rev.id ? 'selected' : ''}>${rev.nombreCompleto}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
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
                                                <td class="align-middle fw-semibold">
                                                    <fmt:formatDate value="${hr.fechaCambio}" pattern="dd/MM/yyyy hh:mm a" />
                                                </td>
                                                <td class="align-middle">${fn:escapeXml(not empty hr.nombreUsuarioCambio ? hr.nombreUsuarioCambio : 'Sistema')}</td>
                                                <td class="align-middle">
                                                    <c:choose>
                                                        <c:when test="${hr.estadoNuevo == 'RADICADA'}"><span class="badge bg-primary">RADICADA</span></c:when>
                                                        <c:when test="${hr.estadoNuevo == 'DEVUELTA'}"><span class="badge bg-danger">DEVUELTA</span></c:when>
                                                        <c:when test="${hr.estadoNuevo == 'APROBADA'}"><span class="badge bg-success">APROBADA</span></c:when>
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
                                <small class="mb-0 fw-bold text-danger"><i class="bi bi-info-circle me-1"></i>La cuenta se encuentra DEVUELTA. Por favor corrige los detalles mencionados y vuelve a radicarla.</small>
                            </c:if>
                            </div>
                        </div>
                    </c:if>
                    
                    <div class="mt-5 pt-3 border-top d-flex justify-content-end gap-2">
                        <a href="informes${not empty modo ? '?modo='.concat(modo) : ''}" class="btn btn-secondary px-4 shadow-sm">
                            <i class="bi bi-arrow-left me-2"></i>Volver
                        </a>
                        <c:if test="${not readonly}">
                            <button type="submit" class="btn btn-success px-5 fw-bold shadow-sm">
                                <i class="bi bi-save me-2"></i>${action == 'update' ? 'Actualizar Informe' : 'Guardar Informe'}
                            </button>
                            <c:if test="${empty informe.estadoRadicacion || informe.estadoRadicacion == 'BORRADOR' || informe.estadoRadicacion == 'DEVUELTA'}">
                                <button type="submit" class="btn btn-primary px-5 fw-bold shadow-sm" onclick="return setRadicar()">
                                    <i class="bi bi-send-fill me-2"></i>Radicar Cuenta
                                </button>
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
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/summernote@0.8.18/dist/summernote-lite.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <script>
        $(document).ready(function() {
            // Activar pestañas con clic
            $('#informeTabs button').on('click', function (e) {
                e.preventDefault();
                $(this).tab('show');
            });

            // Cargar actividades dinámicamente
            var isReadonly = ${readonly == true ? 'true' : 'false'};
            var obligacionesCount = ${fn:length(listaObligaciones) > 0 ? fn:length(listaObligaciones) : 0};
            for(var i=0; i<obligacionesCount; i++) {
                var rawText = document.getElementById("raw_actividad_" + i).value;
                if (rawText && !rawText.includes('<p>') && !rawText.includes('<table') && !rawText.includes('<ul')) {
                    // Si es texto plano antiguo, reemplazar saltos de línea por <br> para que Summernote lo entienda
                    rawText = rawText.replace(/\n/g, '<br>');
                }
                agregarActividadConValor(i, rawText ? rawText : '', isReadonly);
            }

            // Lógica para mostrar/ocultar soportes según la cuota
            function actualizarCamposSoportes() {
                var cuotaStr = $('select[name="numero_cuota"], input[name="numero_cuota"]').val();
                if (cuotaStr == "1") {
                    $('.req-cuota-1').show();
                } else {
                    $('.req-cuota-1').hide();
                    // Limpiar el input si se oculta para no enviar cosas innecesarias
                    $('.req-cuota-1 input[type="file"]').val('');
                }
            }

            $('select[name="numero_cuota"], input[name="numero_cuota"]').on('change', actualizarCamposSoportes);
            actualizarCamposSoportes(); // Ejecutar al cargar la página
            
            // Renderizar archivos previamente subidos (la interfaz que pediste)
            var soportesJsonStr = '${fn:escapeXml(informe.soportesJson)}';
            if (soportesJsonStr && soportesJsonStr.trim() !== '') {
                try {
                    // Reemplazamos entidades xml en caso de que fn:escapeXml las haya codificado
                    var decodedStr = $('<textarea/>').html(soportesJsonStr).text();
                    var soportesObj = JSON.parse(decodedStr);
                    
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
                            var ui = '<div class="alert alert-secondary py-2 mt-2 mb-2 d-flex justify-content-between align-items-center shadow-sm" style="border-left: 4px solid #0d6efd;">' +
                                     '<div><i class="bi bi-file-earmark-check-fill text-success me-2 fs-5"></i>' +
                                     '<a href="' + fileData.url + '" target="_blank" class="text-decoration-none fw-bold text-dark">' + fileData.name + '</a></div>' +
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
            
            if (isReadonly) {
                dropZone.style.display = "none";
            } else {
                dropZone.className = "border rounded p-3 text-center transition-all dropzone-evidencia";
                dropZone.style.border = "2px dashed #0d6efd";
                dropZone.style.cursor = "pointer";
                dropZone.style.backgroundColor = "#f8f9fa";
                dropZone.innerHTML = '<i class="bi bi-cloud-arrow-up fs-3 text-primary"></i><br><span class="text-primary fw-semibold">Haz clic aquí o arrastra los archivos (puedes subir varios)</span>';
                
                dropZone.onclick = function() {
                    fileInput.click();
                };
                
                var actualizarTexto = function() {
                    if(fileInput.files.length > 0) {
                        var names = [];
                        for(var k=0; k<fileInput.files.length; k++) {
                            names.push(fileInput.files[k].name);
                        }
                        dropZone.innerHTML = '<i class="bi bi-check-circle-fill text-success fs-3"></i><br><span class="text-success fw-bold">' + fileInput.files.length + ' archivo(s) seleccionado(s)</span><br><small class="text-muted d-block mt-1" style="word-break: break-all;">' + names.join(', ') + '</small>';
                        dropZone.style.borderColor = "#198754";
                        dropZone.style.backgroundColor = "#e8f5e9";
                    } else {
                        dropZone.innerHTML = '<i class="bi bi-cloud-arrow-up fs-3 text-primary"></i><br><span class="text-primary fw-semibold">Haz clic aquí o arrastra los archivos (puedes subir varios)</span>';
                        dropZone.style.borderColor = "#0d6efd";
                        dropZone.style.backgroundColor = "#f8f9fa";
                    }
                };
                
                fileInput.addEventListener('change', actualizarTexto);
                
                dropZone.addEventListener('dragover', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    dropZone.style.backgroundColor = "#e9ecef";
                });
                
                dropZone.addEventListener('dragleave', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    if(fileInput.files.length === 0) {
                        dropZone.style.backgroundColor = "#f8f9fa";
                    } else {
                        dropZone.style.backgroundColor = "#e8f5e9";
                    }
                });
                
                dropZone.addEventListener('drop', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    if(e.dataTransfer.files && e.dataTransfer.files.length > 0) {
                        fileInput.files = e.dataTransfer.files;
                        actualizarTexto();
                    }
                });
            }
            
            divFile.appendChild(fileLabel);
            divFile.appendChild(dropZone);
            divFile.appendChild(fileInput);
            wrapper.appendChild(divFile);
            
            container.appendChild(wrapper);

            // Iniciar Summernote si no es de solo lectura
            if (!isReadonly) {
                $(textarea).summernote({
                    height: 120,
                    toolbar: [
                        ['style', ['bold', 'italic', 'underline', 'clear']],
                        ['para', ['ul', 'ol', 'paragraph']],
                        ['table', ['table']],
                        ['insert', ['picture', 'link']],
                        ['view', ['fullscreen']]
                    ],
                    placeholder: 'Escriba la actividad o pegue aquí una imagen/tabla...',
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
                                            formData.append('file', blob, file.name || "imagen.jpg");
                                            
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
                        }
                    }
                });
            } else {
                // En modo solo lectura, ocultar el textarea y mostrar el HTML renderizado
                $(textarea).hide();
                var viewer = document.createElement("div");
                viewer.className = "p-3 border rounded bg-light w-100";
                viewer.innerHTML = valor;
                divText.insertBefore(viewer, textarea);
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
        
        // Validación personalizada con SweetAlert
        $('#informeForm').on('submit', function(e) {
            let isValid = true;
            let firstInvalid = null;
            
            $(this).find('[required]').each(function() {
                if (!$(this).val() || $(this).val().trim() === '') {
                    isValid = false;
                    if (!firstInvalid) firstInvalid = $(this);
                }
            });
            
            if (!isValid) {
                e.preventDefault();
                
                // Mover a la pestaña donde está el campo vacío
                let tabPane = firstInvalid.closest('.tab-pane');
                if (tabPane.length) {
                    let tabId = tabPane.attr('id');
                    let tabTrigger = new bootstrap.Tab($('button[data-bs-target="#' + tabId + '"]')[0]);
                    tabTrigger.show();
                }
                
                Swal.fire({
                    icon: 'warning',
                    title: 'Â¡Faltan datos!',
                    text: 'Por favor, diligencia todos los campos obligatorios antes de guardar el informe.',
                    confirmButtonColor: '#007bff'
                }).then(() => {
                    setTimeout(() => firstInvalid.focus(), 300);
                });
                
                return false;
            }
            
            // Recopilar obligaciones en un solo JSON para evitar el límite de 50 campos de Tomcat
            var obligacionesJson = [];
            var count = parseInt($('input[name="obligaciones_count"]').val()) || 0;
            
            for (var i = 0; i < count; i++) {
                var obj = {};
                var $obligacion = $('input[name="obligacion_' + i + '"]');
                if ($obligacion.length) {
                    obj.obligacion = $obligacion.val();
                    $obligacion.removeAttr('name'); // Evitar que se envíe como parte individual
                }
                
                var acts = [];
                $('textarea[name="actividad_' + i + '"]').each(function() {
                    acts.push($(this).val());
                    $(this).removeAttr('name'); // Evitar que se envíe como parte individual
                });
                obj.actividad = acts.join("\n");
                
                obligacionesJson.push(obj);
            }
            $('#informeForm').append($('<input type="hidden" name="concepto_supervisor_json">').val(JSON.stringify(obligacionesJson)));
            $('input[name="obligaciones_count"]').removeAttr('name'); // Tampoco enviar este
            
            // Eliminar los nombres de TODOS los inputs de tipo file que estén vacíos
            $('input[type="file"]').each(function() {
                if (!$(this).val()) {
                    $(this).removeAttr('name');
                }
            });
            
            // Quitar puntos antes de enviar el formulario para que Java (BigDecimal) no se rompa
            $('.money-mask').each(function() {
                var clean = $(this).val().replace(/\./g, '');
                $(this).val(clean);
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
        
        $('#valor_cuota, #valor_acumulado').on('input', calcularSaldo);
        // Calcular al cargar la pagina por si ya hay valores
        $(document).ready(function() {
            if(!'${readonly}') {
                calcularSaldo();
            }
            
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
                if (files.length > 0) {
                    $(this).prop('files', files);
                }
            });
        });
    </script>

        <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
        <script src="https://cdn.datatables.net/1.13.4/js/dataTables.bootstrap5.min.js"></script>
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
            });
            function setRadicar() {
                var rev = document.getElementById("revisor_select").value;
                if (!rev && rev !== "0") {
                    Swal.fire('Atención', 'Debe seleccionar un revisor (o la opción Sin Revisor) para poder radicar la cuenta.', 'warning');
                    return false;
                }
                if (rev === "0") {
                    document.getElementById("revisor_select").name = ""; // Remove name so it sends null to the backend
                }
                document.getElementById("radicar_input").value = "true";
                return true;
            }
        </script>
    </body>
</html>

