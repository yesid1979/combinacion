<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Nuevo Supervisor</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        </head>

        <body class="bg-light">
            <jsp:include page="inc/navbar.jsp" />

            <div class="container mt-5">
                <h2 class="mb-4">Registrar Nuevo Supervisor</h2>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger">${error}</div>
                </c:if>

                <div class="card shadow-sm">
                    <div class="card-body">
                        <form action="supervisores" method="POST">
                            <input type="hidden" name="action" value="insert">

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Cédula *</label>
                                    <input type="text" class="form-control" name="cedula" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Cargo *</label>
                                    <input type="text" class="form-control" name="cargo" required>
                                </div>
                                <div class="col-12">
                                    <label class="form-label">Nombre Completo *</label>
                                    <input type="text" class="form-control" name="nombre" required>
                                </div>
                            </div>

                            <div class="mt-4 text-end">
                                <a href="supervisores" class="btn btn-secondary me-2">Cancelar</a>
                                <button type="submit" class="btn btn-primary">Guardar Supervisor</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        </body>

        </html>