<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Registrar Contratista - Gestión de Prestadores</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <!-- Bootstrap Icons -->
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
            <!-- Custom Styles -->
            <link href="assets/css/styles.css" rel="stylesheet">
        </head>

        <body class="bg-light d-flex flex-column min-vh-100">
            <jsp:include page="inc/navbar.jsp" />

            <div class="container mt-5">

                <c:if test="${not empty error}">
                    <div class="alert alert-danger">${error}</div>
                </c:if>

                <div class="card shadow-sm border-0">
                    <div class="card-body p-4">
                        <h2 class="mb-4 fw-bold">
                            <c:choose>
                                <c:when test="${contratista != null}">Editar Contratista</c:when>
                                <c:otherwise>Registrar Nuevo Contratista</c:otherwise>
                            </c:choose>
                        </h2>
                        <form action="contratistas" method="POST">
                            <c:choose>
                                <c:when test="${contratista != null}">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="id" value="${contratista.id}">
                                </c:when>
                                <c:otherwise>
                                    <input type="hidden" name="action" value="insert">
                                </c:otherwise>
                            </c:choose>

                            <div class="row g-3">
                                <div class="col-md-4">
                                    <label class="form-label">Cédula *</label>
                                    <input type="text" class="form-control" name="cedula" value="${contratista.cedula}"
                                        required>
                                </div>
                                <div class="col-md-2">
                                    <label class="form-label">DV</label>
                                    <input type="text" class="form-control" name="dv" maxlength="2"
                                        value="${contratista.dv}">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Nombre Completo *</label>
                                    <input type="text" class="form-control" name="nombre" value="${contratista.nombre}"
                                        required>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Correo Electrónico</label>
                                    <input type="email" class="form-control" name="correo"
                                        value="${contratista.correo}">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Teléfono</label>
                                    <input type="text" class="form-control" name="telefono"
                                        value="${contratista.telefono}">
                                </div>

                                <div class="col-12">
                                    <label class="form-label">Dirección</label>
                                    <input type="text" class="form-control" name="direccion"
                                        value="${contratista.direccion}">
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Fecha Nacimiento</label>
                                    <input type="date" class="form-control" name="fecha_nacimiento"
                                        value="${contratista.fechaNacimiento}">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Edad</label>
                                    <input type="number" class="form-control" name="edad" value="${contratista.edad}">
                                </div>
                            </div>

                            <div class="mt-4 text-end">
                                <a href="contratistas" class="btn btn-secondary me-2" style="width: 140px;"><i
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

            <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
            <script>
                $(document).ready(function () {
                    $('input[name="fecha_nacimiento"]').on('change', function () {
                        var fechaNac = new Date($(this).val());
                        var hoy = new Date();
                        if (!isNaN(fechaNac.getTime())) {
                            var edad = hoy.getFullYear() - fechaNac.getFullYear();
                            var m = hoy.getMonth() - fechaNac.getMonth();
                            if (m < 0 || (m === 0 && hoy.getDate() < fechaNac.getDate())) {
                                edad--;
                            }
                            $('input[name="edad"]').val(edad);
                        }
                    });
                });
            </script>
        </body>

        </html>