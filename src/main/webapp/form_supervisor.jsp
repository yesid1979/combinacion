<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Registrar Supervisor - Gestión de Prestadores</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <!-- Bootstrap Icons -->
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
            <!-- Custom Styles -->
            <link href="assets/css/styles.css" rel="stylesheet">
        </head>

        <body class="bg-light">
            <jsp:include page="inc/navbar.jsp" />

            <div class="container mt-5">

                <c:if test="${not empty error}">
                    <div class="alert alert-danger">${error}</div>
                </c:if>

                <div class="card shadow-sm border-0">
                    <div class="card-body p-4">
                        <h2 class="mb-4 fw-bold">
                            <c:choose>
                                <c:when test="${supervisor != null}">Editar Supervisor</c:when>
                                <c:otherwise>Registrar Nuevo Supervisor</c:otherwise>
                            </c:choose>
                        </h2>
                        <form action="supervisores" method="POST">
                            <c:choose>
                                <c:when test="${supervisor != null}">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="id" value="${supervisor.id}">
                                </c:when>
                                <c:otherwise>
                                    <input type="hidden" name="action" value="insert">
                                </c:otherwise>
                            </c:choose>

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Cédula *</label>
                                    <input type="text" class="form-control" name="cedula" value="${supervisor.cedula}"
                                        required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Nombre Completo *</label>
                                    <input type="text" class="form-control" name="nombre" value="${supervisor.nombre}"
                                        required>
                                </div>

                                <div class="col-12">
                                    <label class="form-label">Cargo</label>
                                    <input type="text" class="form-control" name="cargo" value="${supervisor.cargo}">
                                </div>
                            </div>

                            <div class="mt-4 text-end">
                                <a href="supervisores" class="btn btn-secondary me-2">Cancelar</a>
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-save me-1"></i> Guardar
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        </body>

        </html>