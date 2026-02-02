<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="es">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Detalle del Presupuesto - Gestión Integral</title>
                <!-- Bootstrap 5 CSS -->
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                <link rel="stylesheet"
                    href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
                <link href="assets/css/styles.css" rel="stylesheet">
            </head>

            <body class="bg-light d-flex flex-column min-vh-100">
                <jsp:include page="inc/navbar.jsp" />

                <div class="container mt-4 mb-5">
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h2><i class="bi bi-file-earmark-text me-2 text-primary"></i>Detalle del Registro Presupuestal
                        </h2>
                        <a href="presupuesto" class="btn btn-secondary"><i class="bi bi-arrow-left me-1"></i> Volver</a>
                    </div>

                    <c:if test="${not empty presupuesto}">
                        <div class="card shadow-sm border-0">
                            <div class="card-header bg-white border-bottom-0 pt-4 px-4">
                                <h5 class="text-primary fw-bold mb-0">Información del CDP y RP</h5>
                            </div>
                            <div class="card-body p-4">
                                <div class="row g-3">
                                    <div class="col-md-3">
                                        <label class="form-label text-muted small text-uppercase fw-bold">No.
                                            CDP</label>
                                        <div class="form-control bg-light">${presupuesto.cdpNumero}</div>
                                    </div>
                                    <div class="col-md-3">
                                        <label class="form-label text-muted small text-uppercase fw-bold">Fecha
                                            CDP</label>
                                        <div class="form-control bg-light">
                                            <fmt:formatDate value="${presupuesto.cdpFecha}" pattern="dd/MM/yyyy" />
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <label class="form-label text-muted small text-uppercase fw-bold">Valor
                                            CDP</label>
                                        <div class="form-control bg-light">
                                            <fmt:formatNumber value="${presupuesto.cdpValor}" type="currency"
                                                currencySymbol="$" />
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <label class="form-label text-muted small text-uppercase fw-bold">Vencimiento
                                            CDP</label>
                                        <div class="form-control bg-light">
                                            <fmt:formatDate value="${presupuesto.cdpVencimiento}"
                                                pattern="dd/MM/yyyy" />
                                        </div>
                                    </div>

                                    <div class="col-12">
                                        <hr class="text-muted opacity-25">
                                    </div>

                                    <div class="col-md-6">
                                        <label class="form-label text-muted small text-uppercase fw-bold">No. RP
                                            (Registro Presupuestal)</label>
                                        <div class="form-control bg-light">${presupuesto.rpNumero != null ?
                                            presupuesto.rpNumero : 'N/A'}</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label text-muted small text-uppercase fw-bold">Fecha
                                            RP</label>
                                        <div class="form-control bg-light">
                                            <c:if test="${presupuesto.rpFecha != null}">
                                                <fmt:formatDate value="${presupuesto.rpFecha}" pattern="dd/MM/yyyy" />
                                            </c:if>
                                            <c:if test="${presupuesto.rpFecha == null}">N/A</c:if>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="card shadow-sm border-0 mt-4">
                            <div class="card-header bg-white border-bottom-0 pt-4 px-4">
                                <h5 class="text-primary fw-bold mb-0">Estructura y Clasificación</h5>
                            </div>
                            <div class="card-body p-4">
                                <div class="row g-3">
                                    <div class="col-12">
                                        <label class="form-label text-muted small text-uppercase fw-bold">Rubro /
                                            Apropiación Presupuestal</label>
                                        <div class="form-control bg-light" style="min-height: 60px;">
                                            ${presupuesto.apropiacionPresupuestal}</div>
                                    </div>

                                    <div class="col-12">
                                        <label class="form-label text-muted small text-uppercase fw-bold">Nombre Ficha
                                            EBI / Proyecto</label>
                                        <div class="form-control bg-light" style="min-height: 60px;">
                                            ${presupuesto.fichaEbiNombre != null ? presupuesto.fichaEbiNombre : 'N/A'}
                                        </div>
                                    </div>

                                    <div class="col-md-6">
                                        <label class="form-label text-muted small text-uppercase fw-bold">ID PAA</label>
                                        <div class="form-control bg-light">${presupuesto.idPaa != null ?
                                            presupuesto.idPaa : 'N/A'}</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label text-muted small text-uppercase fw-bold">Código
                                            DANE</label>
                                        <div class="form-control bg-light">${presupuesto.codigoDane != null ?
                                            presupuesto.codigoDane : 'N/A'}</div>
                                    </div>

                                    <div class="col-md-6">
                                        <label
                                            class="form-label text-muted small text-uppercase fw-bold">Inversión</label>
                                        <div class="form-control bg-light">${presupuesto.inversion != null ?
                                            presupuesto.inversion : 'N/A'}</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label
                                            class="form-label text-muted small text-uppercase fw-bold">Funcionamiento</label>
                                        <div class="form-control bg-light">${presupuesto.funcionamiento != null ?
                                            presupuesto.funcionamiento : 'N/A'}</div>
                                    </div>

                                    <div class="col-12">
                                        <label class="form-label text-muted small text-uppercase fw-bold">Objetivo Ficha
                                            EBI</label>
                                        <div class="form-control bg-light" style="min-height: 80px;">
                                            ${presupuesto.fichaEbiObjetivo != null ? presupuesto.fichaEbiObjetivo :
                                            'N/A'}</div>
                                    </div>
                                    <div class="col-12">
                                        <label class="form-label text-muted small text-uppercase fw-bold">Actividades
                                            Ficha EBI</label>
                                        <div class="form-control bg-light" style="min-height: 80px;">
                                            ${presupuesto.fichaEbiActividades != null ? presupuesto.fichaEbiActividades
                                            : 'N/A'}</div>
                                    </div>

                                    <div class="col-md-6">
                                        <label class="form-label text-muted small text-uppercase fw-bold">Certificado
                                            Insuficiencia</label>
                                        <div class="form-control bg-light">${presupuesto.certificadoInsuficiencia !=
                                            null ? presupuesto.certificadoInsuficiencia : 'N/A'}</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label text-muted small text-uppercase fw-bold">Fecha
                                            Insuficiencia</label>
                                        <div class="form-control bg-light">
                                            <c:if test="${presupuesto.fechaInsuficiencia != null}">
                                                <fmt:formatDate value="${presupuesto.fechaInsuficiencia}"
                                                    pattern="dd/MM/yyyy" />
                                            </c:if>
                                            <c:if test="${presupuesto.fechaInsuficiencia == null}">N/A</c:if>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:if>

                    <c:if test="${empty presupuesto}">
                        <div class="alert alert-warning text-center">
                            <h4><i class="bi bi-exclamation-triangle"></i> Registro no encontrado</h4>
                            <p>No se pudo cargar la información del presupuesto solicitado.</p>
                            <a href="presupuesto" class="btn btn-primary mt-2">Volver a la lista</a>
                        </div>
                    </c:if>

                </div>

                <jsp:include page="inc/footer.jsp" />
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
            </body>

            </html>