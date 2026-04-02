<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${readonly ? 'Detalles del Presupuesto' : (presupuesto != null && presupuesto.id > 0 ? 'Editar Presupuesto' : 'Registrar Nuevo Presupuesto')}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link href="assets/css/styles.css" rel="stylesheet">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
</head>

<body class="bg-light d-flex flex-column min-vh-100">
    <jsp:include page="inc/navbar.jsp" />

    <div class="container mt-4 mb-5 flex-grow-1">
        <c:if test="${not empty error}">
            <div class="alert alert-danger shadow-sm">${error}</div>
        </c:if>

        <div class="card shadow-sm border-0">
            <div class="card-body p-4">
                <h2 class="mb-4 fw-bold">
                    <c:choose>
                        <c:when test="${readonly}">Detalles del Presupuesto</c:when>
                        <c:when test="${presupuesto != null && presupuesto.id > 0}">Editar Presupuesto</c:when>
                        <c:otherwise>Registrar Nuevo Presupuesto</c:otherwise>
                    </c:choose>
                </h2>

                <form action="presupuesto" method="POST">
                    <input type="hidden" name="action" value="${presupuesto != null && presupuesto.id > 0 ? 'update' : 'insert'}">
                    <c:if test="${presupuesto != null && presupuesto.id > 0}">
                        <input type="hidden" name="id" value="${presupuesto.id}">
                    </c:if>

                    <div class="row g-3">
                        <h5 class="text-primary fw-bold mb-0 mt-4">Información del CDP y RPC</h5>
                        <div class="col-md-3">
                            <label class="form-label text-muted small fw-bold">No. CDP *</label>
                            <input type="text" class="form-control" name="cdpNumero" value="${presupuesto.cdpNumero}" required ${readonly ? 'readonly' : ''}>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label text-muted small fw-bold">Fecha CDP *</label>
                            <input type="date" class="form-control" name="cdpFecha" value="${presupuesto.cdpFecha}" required ${readonly ? 'readonly' : ''}>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label text-muted small fw-bold">Valor CDP *</label>
                            <input type="number" step="0.01" class="form-control" name="cdpValor" value="${presupuesto.cdpValor}" required ${readonly ? 'readonly' : ''}>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label text-muted small fw-bold">Vencimiento CDP *</label>
                            <input type="date" class="form-control" name="cdpVencimiento" value="${presupuesto.cdpVencimiento}" required ${readonly ? 'readonly' : ''}>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-muted small fw-bold">No. RPC (Registro Presupuestal)</label>
                            <input type="text" class="form-control" name="rpNumero" value="${presupuesto.rpNumero}" ${readonly ? 'readonly' : ''}>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-muted small fw-bold">Fecha RPC</label>
                            <input type="date" class="form-control" name="rpFecha" value="${presupuesto.rpFecha}" ${readonly ? 'readonly' : ''}>
                        </div>

                        <h5 class="text-primary fw-bold mb-0 mt-4">Estructura y Clasificación</h5>
                        <div class="col-12">
                            <label class="form-label text-muted small fw-bold">Rubro / Apropiación Presupuestal *</label>
                            <textarea class="form-control" name="apropiacionPresupuestal" rows="2" required ${readonly ? 'readonly' : ''}>${presupuesto.apropiacionPresupuestal}</textarea>
                        </div>

                        <div class="col-12">
                            <label class="form-label text-muted small fw-bold">Nombre Ficha EBI / Proyecto</label>
                            <textarea class="form-control" name="fichaEbiNombre" rows="2" ${readonly ? 'readonly' : ''}>${presupuesto.fichaEbiNombre}</textarea>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label text-muted small fw-bold">ID PAA</label>
                            <input type="text" class="form-control" name="idPaa" value="${presupuesto.idPaa}" ${readonly ? 'readonly' : ''}>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-muted small fw-bold">Código DANE</label>
                            <input type="text" class="form-control" name="codigoDane" value="${presupuesto.codigoDane}" ${readonly ? 'readonly' : ''}>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label text-muted small fw-bold">Inversión</label>
                            <input type="text" class="form-control" name="inversion" value="${presupuesto.inversion}" ${readonly ? 'readonly' : ''}>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-muted small fw-bold">Funcionamiento</label>
                            <input type="text" class="form-control" name="funcionamiento" value="${presupuesto.funcionamiento}" ${readonly ? 'readonly' : ''}>
                        </div>

                        <div class="col-12">
                            <label class="form-label text-muted small fw-bold">Objetivo Ficha EBI</label>
                            <textarea class="form-control" name="fichaEbiObjetivo" rows="3" ${readonly ? 'readonly' : ''}>${presupuesto.fichaEbiObjetivo}</textarea>
                        </div>
                        <div class="col-12">
                            <label class="form-label text-muted small fw-bold">Actividades Ficha EBI</label>
                            <textarea class="form-control" name="fichaEbiActividades" rows="3" ${readonly ? 'readonly' : ''}>${presupuesto.fichaEbiActividades}</textarea>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label text-muted small fw-bold">Certificado Insuficiencia</label>
                            <input type="text" class="form-control" name="certificadoInsuficiencia" value="${presupuesto.certificadoInsuficiencia}" ${readonly ? 'readonly' : ''}>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label text-muted small fw-bold">Fecha Insuficiencia</label>
                            <input type="date" class="form-control" name="fechaInsuficiencia" value="${presupuesto.fechaInsuficiencia}" ${readonly ? 'readonly' : ''}>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label text-muted small fw-bold">BPIN</label>
                            <input type="text" class="form-control" name="bpin" value="${presupuesto.bpin}" ${readonly ? 'readonly' : ''}>
                        </div>
                    </div>

                    <div class="mt-4 text-end">
                        <c:if test="${!readonly}">
                            <button type="submit" class="btn text-white px-4 fw-bold me-2" style="background-color: #198754; border-radius: 8px;">
                                <i class="bi bi-save me-2"></i> Guardar
                            </button>
                        </c:if>
                        <a href="presupuesto" class="btn btn-secondary px-4 fw-bold" style="border-radius: 8px;">
                            <i class="bi bi-x-circle me-2"></i> Cerrar
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <jsp:include page="inc/footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
