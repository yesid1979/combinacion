<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${action == 'view' ? 'Ver' : 'Nuevo'} Informe de Supervisión - Gestión de Prestadores</title>
    <link rel="icon" href="favicon.ico" type="image/x-icon">
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
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
    </style>
</head>
<body class="bg-white d-flex flex-column min-vh-100">

    <jsp:include page="inc/navbar.jsp" />

    <div class="container-fluid mt-4 mb-5 flex-grow-1 px-4">
        <div class="row mb-3">
            <div class="col-12">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="informes">Cuentas</a></li>
                        <li class="breadcrumb-item active">Informe de Supervisión</li>
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

                <form action="informes?action=${action}<c:if test="${action == 'update'}">&id=${informe.id}</c:if>" method="POST" id="informeForm" class="needs-validation" novalidate>
                    <input type="hidden" name="action" value="${action}">
                    <input type="hidden" name="contrato_id" value="${contrato.id}">
                    <c:if test="${action == 'update'}">
                        <input type="hidden" name="id" value="${informe.id}">
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
                                    <input type="date" class="form-control" name="fecha_inicio_periodo" value="<fmt:formatDate value='${informe.fechaInicioPeriodo}' pattern='yyyy-MM-dd'/>" required ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Fecha terminación</label>
                                    <input type="date" class="form-control" name="fecha_fin_periodo" value="<fmt:formatDate value='${informe.fechaFinPeriodo}' pattern='yyyy-MM-dd'/>" required ${readonly ? 'readonly' : ''}>
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
                                                            <c:if test="${not readonly}">
                                                                <button type="button" class="btn btn-sm btn-outline-primary mt-2" onclick="agregarActividad(${status.index})">
                                                                    <i class="bi bi-plus-circle"></i> Agregar
                                                                </button>
                                                            </c:if>
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
                                <div class="col-md-6 req-cuota-1">
                                    <label class="form-label">RUT</label>
                                    <input type="file" class="form-control" name="file_rut" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6 req-cuota-1">
                                    <label class="form-label">Cédula</label>
                                    <input type="file" class="form-control" name="file_cedula" accept="application/pdf" ${readonly ? 'disabled' : ''}>
                                </div>
                                <div class="col-md-6 req-cuota-1">
                                    <label class="form-label">Constancia SECOP</label>
                                    <input type="file" class="form-control" name="file_secop" accept="application/pdf" ${readonly ? 'disabled' : ''}>
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

                    <div class="mt-5 pt-3 border-top d-flex justify-content-end gap-2">
                        <a href="informes${not empty contrato.id ? '?contrato_id=' : ''}${contrato.id}" class="btn btn-light px-4 border">
                            <i class="bi bi-arrow-left me-2"></i>Volver
                        </a>
                        <c:if test="${not readonly}">
                            <button type="submit" class="btn btn-success px-5 fw-bold shadow-sm">
                                <i class="bi bi-save me-2"></i>${action == 'update' ? 'Actualizar Informe' : 'Guardar Informe'}
                            </button>
                        </c:if>
                        <c:if test="${readonly}">
                            <button type="button" class="btn btn-primary px-5 fw-bold shadow-sm" onclick="window.print()">
                                <i class="bi bi-printer me-2"></i>Imprimir
                            </button>
                        </c:if>
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
                var acts = rawText ? rawText.split('\n') : [''];
                if (acts.length === 0) acts = [''];
                for(var j=0; j<acts.length; j++) {
                    agregarActividadConValor(i, acts[j], isReadonly);
                }
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
        });

        function agregarActividadConValor(index, valor, isReadonly) {
            var container = document.getElementById("actividades_container_" + index);
            var actIndex = container.children.length; // Para identificar cada actividad de forma única
            
            var wrapper = document.createElement("div");
            wrapper.className = "mb-3 p-2 border rounded bg-white shadow-sm";
            
            var divText = document.createElement("div");
            divText.className = "d-flex align-items-start";
            
            var textarea = document.createElement("textarea");
            textarea.className = "form-control";
            textarea.name = "actividad_" + index;
            textarea.rows = 2;
            textarea.placeholder = "Describa la actividad realizada...";
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
                    if (container.children.length > 1) {
                        wrapper.remove();
                    } else {
                        textarea.value = '';
                    }
                };
                divText.appendChild(btn);
            }
            wrapper.appendChild(divText);

            if (!isReadonly) {
                var divFile = document.createElement("div");
                divFile.className = "mt-2";
                
                var fileLabel = document.createElement("small");
                fileLabel.className = "text-muted d-block fw-bold mb-1";
                fileLabel.innerHTML = '<i class="bi bi-paperclip"></i> Evidencias de esta actividad:';
                
                var fileInput = document.createElement("input");
                fileInput.type = "file";
                fileInput.className = "form-control form-control-sm";
                fileInput.name = "evidencia_" + index + "_" + actIndex;
                fileInput.multiple = true; // Permite seleccionar varias fotos o PDFs
                fileInput.accept = "application/pdf, image/*";
                
                divFile.appendChild(fileLabel);
                divFile.appendChild(fileInput);
                wrapper.appendChild(divFile);
            }
            
            container.appendChild(wrapper);
        }

        window.agregarActividad = function(index) {
            agregarActividadConValor(index, "", false);
        };
        
        // Formato de Dinero (Puntos para miles, sin decimales)
        function formatMoney(n) {
            if(!n) return "";
            let str = n.toString();
            // Si viene de la base de datos con .00 (ej: 33978000.00), quitamos los decimales
            if (str.includes(".")) {
                str = str.split(".")[0];
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
                    title: '¡Faltan datos!',
                    text: 'Por favor, diligencia todos los campos obligatorios antes de guardar el informe.',
                    confirmButtonColor: '#007bff'
                }).then(() => {
                    setTimeout(() => firstInvalid.focus(), 300);
                });
                
                return false;
            }
            
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
        });
    </script>
</body>
</html>
