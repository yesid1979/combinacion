<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${not empty revisor ? 'Editar' : 'Nuevo'} Revisor - Gestión de Prestadores</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        .form-label { font-weight: 600; color: #495057; font-size: 0.9rem; }
    </style>
    <link href="${pageContext.request.contextPath}/assets/css/styles.css" rel="stylesheet">
</head>
<body class="bg-light d-flex flex-column min-vh-100">

    <jsp:include page="inc/navbar.jsp" />

    <div class="container mt-4 mb-5 flex-grow-1" style="max-width: 800px;">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb breadcrumb-premium">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
                <li class="breadcrumb-item active text-muted">Datos Maestros</li>
                <li class="breadcrumb-item"><a href="revisores"><i class="bi bi-check-circle-fill me-1"></i>Revisores</a></li>
                <li class="breadcrumb-item active" aria-current="page">Formulario Revisor</li>
            </ol>
        </nav>
        <div class="card border shadow-sm">
            <div class="card-body p-4">
                <div class="d-flex justify-content-between align-items-center mb-4 pb-2 border-bottom">
                    <h3 class="fw-bold m-0 text-dark">
                        <i class="bi bi-person-check me-2 text-primary"></i>
                        ${not empty revisor ? 'Editar' : 'Nuevo'} Revisor
                    </h3>
                </div>

                <form action="revisores" method="POST">
                    <input type="hidden" name="action" value="${not empty revisor ? 'update' : 'insert'}">
                    <c:if test="${not empty revisor}">
                        <input type="hidden" name="id" value="${revisor.id}">
                    </c:if>

                    <div class="row g-3">
                        <div class="col-md-12">
                            <label class="form-label">Tipo de Documento</label>
                            <input type="text" class="form-control" name="tipoDocumento" value="${revisor.tipoDocumento}" placeholder="Ej: ESTUDIOS_PREVIOS, CERTIFICADO_IDONEIDAD" required ${readonly ? 'readonly' : ''}>
                            <div class="form-text">Este debe coincidir exactamente con la palabra clave que usaremos para buscar el revisor de este formato.</div>
                        </div>

                        <div class="col-md-12">
                            <label class="form-label">Nombre Completo del Revisor</label>
                            <input type="text" class="form-control" name="nombreCompleto" value="${revisor.nombreCompleto}" required ${readonly ? 'readonly' : ''}>
                        </div>

                        <div class="col-md-12">
                            <label class="form-label">Cargo del Revisor</label>
                            <input type="text" class="form-control" name="cargo" value="${revisor.cargo}" required ${readonly ? 'readonly' : ''}>
                        </div>
                    </div>

                    <div class="mt-4 pt-3 border-top d-flex justify-content-end gap-2">
                        <a href="revisores" class="btn btn-light px-4 border">Cancelar</a>
                        <c:if test="${not readonly}">
                            <button type="submit" class="btn btn-success px-4 fw-bold shadow-sm">
                                <i class="bi bi-save me-2"></i>Guardar Revisor
                            </button>
                        </c:if>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <jsp:include page="inc/footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

