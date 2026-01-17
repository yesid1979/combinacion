<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Nuevo Ordenador del Gasto</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        </head>

        <body class="bg-light">
            <jsp:include page="inc/navbar.jsp" />

            <div class="container mt-5">
                <h2 class="mb-4">Registrar Nuevo Ordenador del Gasto</h2>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger">${error}</div>
                </c:if>

                <div class="card shadow-sm">
                    <div class="card-body">
                        <form action="ordenadores" method="POST">
                            <input type="hidden" name="action" value="insert">

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Organismo *</label>
                                    <input type="text" class="form-control" name="organismo" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Dirección Organismo</label>
                                    <input type="text" class="form-control" name="direccion_organismo">
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Nombre Ordenador *</label>
                                    <input type="text" class="form-control" name="nombre_ordenador" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Cédula Ordenador</label>
                                    <input type="text" class="form-control" name="cedula_ordenador">
                                </div>

                                <div class="col-md-4">
                                    <label class="form-label">Cargo</label>
                                    <input type="text" class="form-control" name="cargo_ordenador">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Decreto Nombramiento</label>
                                    <input type="text" class="form-control" name="decreto_nombramiento">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Acta Posesión</label>
                                    <input type="text" class="form-control" name="acta_posesion">
                                </div>
                            </div>

                            <div class="mt-4 text-end">
                                <a href="ordenadores" class="btn btn-secondary me-2">Cancelar</a>
                                <button type="submit" class="btn btn-primary">Guardar Ordenador</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        </body>

        </html>