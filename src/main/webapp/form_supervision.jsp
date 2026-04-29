<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${action == 'view' ? 'Ver' : 'Nuevo'} Informe de Supervisión - Gestión de Prestadores</title>
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

                <form action="informes" method="POST" id="informeForm">
                    <input type="hidden" name="action" value="insert">
                    <input type="hidden" name="contrato_id" value="${contrato.id}">

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger d-flex align-items-center mb-4" role="alert">
                            <i class="bi bi-exclamation-triangle-fill me-3 fs-4"></i>
                            <div>
                                <strong>Error:</strong> ${error}
                            </div>
                        </div>
                    </c:if>

                    <div class="alert alert-info d-flex align-items-center mb-4" role="alert">
                        <i class="bi bi-info-circle-fill me-3 fs-4"></i>
                        <div>
                            <strong>Contrato:</strong> ${contrato.numeroContrato} - ${contrato.contratistaNombre}
                        </div>
                    </div>

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
                    </ul>

                    <div class="tab-content" id="informeTabsContent">
                        <!-- Tab 1: General -->
                        <div class="tab-pane fade show active" id="general" role="tabpanel">
                            <div class="section-title">Aspectos Generales</div>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Periodo del Informe (Mes y Año)</label>
                                    <input type="text" class="form-control" name="periodo_informe" value="${informe.periodoInforme}" placeholder="Ej: Enero 2026" required ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Tipo de Informe</label>
                                    <select class="form-select" name="tipo_informe" required ${readonly ? 'disabled' : ''}>
                                        <option value="PARCIAL" ${informe.tipoInforme == 'PARCIAL' ? 'selected' : ''}>INFORME PARCIAL</option>
                                        <option value="FINAL" ${informe.tipoInforme == 'FINAL' ? 'selected' : ''}>INFORME FINAL</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Cuota Número</label>
                                    <input type="text" class="form-control" name="numero_cuota" value="${informe.numeroCuota}" placeholder="Ej: 01" required ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Fecha Inicio Periodo</label>
                                    <input type="date" class="form-control" name="fecha_inicio_periodo" value="<fmt:formatDate value='${informe.fechaInicioPeriodo}' pattern='yyyy-MM-dd'/>" required ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Fecha Fin Periodo</label>
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
                                    <textarea class="form-control" name="modificaciones" rows="2" ${readonly ? 'readonly' : ''}>${informe.modificaciones}</textarea>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Suspensión</label>
                                    <textarea class="form-control" name="suspensiones" rows="2" ${readonly ? 'readonly' : ''}>${informe.suspensiones}</textarea>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Reanudación</label>
                                    <input type="text" class="form-control" name="reanudaciones" value="${informe.reanudaciones}" ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Cesión</label>
                                    <input type="text" class="form-control" name="cesiones" value="${informe.cesiones}" ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Terminación anticipada</label>
                                    <input type="text" class="form-control" name="terminacion_anticipada" value="${informe.terminacionAnticipada}" ${readonly ? 'readonly' : ''}>
                                </div>
                            </div>
                        </div>

                        <!-- Tab 3: Financiero -->
                        <div class="tab-pane fade" id="financiero" role="tabpanel">
                            <div class="section-title">Informe Contable y Financiero</div>
                            <div class="row g-3">
                                <div class="col-md-4">
                                    <label class="form-label">Valor Cuota a Cancelar ($)</label>
                                    <input type="number" step="0.01" class="form-control" name="valor_cuota_pagar" value="${not empty informe.valorCuotaPagar ? informe.valorCuotaPagar : contrato.valorCuotaNumero}" required ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Valor Acumulado Cancelado ($)</label>
                                    <input type="number" step="0.01" class="form-control" name="valor_acumulado_pagado" value="${informe.valorAccumuladoPagado}" placeholder="0.00" ${readonly ? 'readonly' : ''}>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Saldo por Cancelar ($)</label>
                                    <input type="number" step="0.01" class="form-control" name="saldo_por_cancelar" value="${informe.saldoPorCancelar}" placeholder="0.00" ${readonly ? 'readonly' : ''}>
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
                                <div class="col-md-12">
                                    <label class="form-label">Observaciones al Informe Técnico</label>
                                    <textarea class="form-control" name="observaciones_tecnicas" rows="4" placeholder="Describa el cumplimiento de las actividades..." required ${readonly ? 'readonly' : ''}>${informe.observacionesTecnicas}</textarea>
                                </div>
                                <div class="col-md-12">
                                    <label class="form-label">Recomendaciones para el Contratista</label>
                                    <textarea class="form-control" name="recomendaciones" rows="2" ${readonly ? 'readonly' : ''}>${informe.recomendaciones}</textarea>
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
                    </div>

                    <div class="mt-5 pt-3 border-top d-flex justify-content-end gap-2">
                        <a href="informes${not empty contrato.id ? '?contrato_id=' : ''}${contrato.id}" class="btn btn-light px-4 border">
                            <i class="bi bi-arrow-left me-2"></i>Volver
                        </a>
                        <c:if test="${not readonly}">
                            <button type="submit" class="btn btn-success px-5 fw-bold shadow-sm">
                                <i class="bi bi-save me-2"></i>Guardar Informe y Generar Formato
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
    <script>
        $(document).ready(function() {
            // Activar pestañas con clic
            $('#informeTabs button').on('click', function (e) {
                e.preventDefault();
                $(this).tab('show');
            });
        });
    </script>
</body>
</html>
