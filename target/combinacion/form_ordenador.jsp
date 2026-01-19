<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Nuevo Ordenador del Gasto</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <!-- Bootstrap Icons -->
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
            <!-- Custom Styles -->
            <link href="assets/css/styles.css" rel="stylesheet">
        </head>

        <body class="bg-light d-flex flex-column min-vh-100">
            <jsp:include page="inc/navbar.jsp" />

            <div class="container mt-4 mb-5">
                <c:if test="${not empty error}">
                    <div class="alert alert-danger">${error}</div>
                </c:if>

                <div class="card shadow-sm border-0">
                    <div class="card-body p-4">
                        <h2 class="mb-4 fw-bold">
                            <c:choose>
                                <c:when test="${ordenador != null}">Editar Ordenador del Gasto</c:when>
                                <c:otherwise>Registrar nuevo ordenador del gasto</c:otherwise>
                            </c:choose>
                        </h2>
                        <form action="ordenadores" method="POST">
                            <c:choose>
                                <c:when test="${ordenador != null}">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="id" value="${ordenador.id}">
                                </c:when>
                                <c:otherwise>
                                    <input type="hidden" name="action" value="insert">
                                </c:otherwise>
                            </c:choose>

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Organismo *</label>
                                    <input type="text" class="form-control" name="organismo"
                                        value="${ordenador.organismo}" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Dirección Organismo</label>
                                    <input type="text" class="form-control" name="direccion_organismo"
                                        value="${ordenador.direccionOrganismo}">
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Nombre Ordenador *</label>
                                    <input type="text" class="form-control" name="nombre_ordenador"
                                        value="${ordenador.nombreOrdenador}" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Cédula Ordenador</label>
                                    <input type="text" class="form-control" name="cedula_ordenador"
                                        value="${ordenador.cedulaOrdenador}">
                                </div>

                                <div class="col-md-4">
                                    <label class="form-label">Cargo</label>
                                    <input type="text" class="form-control" name="cargo_ordenador"
                                        value="${ordenador.cargoOrdenador}">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Decreto Nombramiento</label>
                                    <input type="text" class="form-control" name="decreto_nombramiento"
                                        value="${ordenador.decretoNombramiento}">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Acta Posesión</label>
                                    <input type="text" class="form-control" name="acta_posesion"
                                        value="${ordenador.actaPosesion}">
                                </div>
                            </div>

                            <div class="mt-4 text-end">
                                <a href="ordenadores" class="btn btn-secondary me-2" style="width: 140px;"><i
                                        class="bi bi-x-circle me-2"></i> Cerrar</a>
                                <button type="submit" class="btn btn-primary" style="width: 140px;"><i
                                        class="bi bi-save me-2"></i> Guardar</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Footer -->
            <jsp:include page="inc/footer.jsp" />

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        </body>

        </html>