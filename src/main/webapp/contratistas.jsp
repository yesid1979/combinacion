<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <title>Contratistas - Gestión Contratos</title>
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap"
                rel="stylesheet">
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
            <link href="assets/css/styles.css" rel="stylesheet">
        </head>

        <body class="d-flex flex-column min-vh-100">

            <jsp:include page="inc/navbar.jsp" />

            <div class="container my-5">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h2 class="fw-bold mb-1">Contratistas</h2>
                        <p class="text-muted">Directorio de prestadores de servicios.</p>
                    </div>
                    <a href="form_contratista.jsp" class="btn btn-primary d-flex align-items-center gap-2 shadow-sm">
                        <i class="bi bi-person-plus"></i> Nuevo Contratista
                    </a>
                </div>

                <div class="card card-modern border-0">
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="bg-light text-muted small text-uppercase">
                                    <tr>
                                        <th class="ps-4 py-3">Cédula</th>
                                        <th>Nombre</th>
                                        <th>Correo</th>
                                        <th>Teléfono</th>
                                        <th>Profesión</th>
                                        <th class="pe-4 text-end">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${empty listaContratistas}">
                                            <tr>
                                                <td colspan="6" class="text-center py-5 text-muted">
                                                    No hay contratistas registrados.
                                                </td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach items="${listaContratistas}" var="c">
                                                <tr>
                                                    <td class="ps-4 font-monospace">${c.cedula}</td>
                                                    <td class="fw-medium">${c.nombre}</td>
                                                    <td>${c.correo}</td>
                                                    <td>${c.telefono}</td>
                                                    <td>${c.formacionTitulo}</td>
                                                    <td class="pe-4 text-end">
                                                        <a href="#" class="btn btn-sm btn-light text-muted"><i
                                                                class="bi bi-pencil"></i></a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <jsp:include page="inc/footer.jsp" />
            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        </body>

        </html>