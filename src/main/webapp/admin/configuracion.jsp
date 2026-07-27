<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Configuración - Gestión Contratos</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <!-- DataTables CSS -->
    <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
    <link href="https://cdn.datatables.net/responsive/2.4.1/css/responsive.bootstrap5.min.css" rel="stylesheet">
    <!-- Custom Styles -->
    <link href="${pageContext.request.contextPath}/assets/css/styles.css" rel="stylesheet">
    <style>
        table { width: 100% !important; }
        .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
        .table td { vertical-align: middle; }
    </style>
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
</head>

<body class="bg-light d-flex flex-column min-vh-100">

    <jsp:include page="../inc/navbar.jsp" />

    <div class="container-fluid container-main mt-4 mb-5 px-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="fw-bold text-dark mb-0">Gestión de Configuración</h3>
        </div>

        <c:if test="${not empty param.msg}">
            <div class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
                <c:choose>
                    <c:when test="${param.msg == 'success'}"><i class="bi bi-check-circle-fill me-2"></i>Configuración guardada exitosamente.</c:when>
                    <c:when test="${param.msg == 'deleted'}"><i class="bi bi-trash-fill me-2"></i>Configuración eliminada exitosamente.</c:when>
                </c:choose>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="row">
            <!-- Formulario (Para Crear y Editar) -->
            <div class="col-lg-4 mb-4">
                <div class="card border-0 shadow-sm">
                    <div class="card-header bg-white border-bottom py-3">
                        <h5 class="card-title fw-bold text-dark mb-0">
                            <c:choose>
                                <c:when test="${not empty configEdit}"><i class="bi bi-pencil-square me-2 text-primary"></i>Editar Configuración</c:when>
                                <c:otherwise><i class="bi bi-plus-circle-fill me-2 text-success"></i>Nueva Configuración</c:otherwise>
                            </c:choose>
                        </h5>
                    </div>
                    <div class="card-body bg-white">
                        <form action="${pageContext.request.contextPath}/admin/configuracion" method="POST">
                            <input type="hidden" name="action" value="save">
                            <input type="hidden" name="id" value="${configEdit != null ? configEdit.id : ''}">

                            <div class="mb-3">
                                <label class="form-label fw-bold">Clave (Única, ej. DRIVE_CARPETA)</label>
                                <input type="text" class="form-control" name="clave" value="${configEdit != null ? configEdit.clave : ''}" required>
                            </div>

                            <div class="mb-3">
                                <label class="form-label fw-bold">Valor</label>
                                <input type="text" class="form-control" name="valor" value="${configEdit != null ? configEdit.valor : ''}" required>
                            </div>

                            <div class="mb-4">
                                <label class="form-label fw-bold">Descripción</label>
                                <textarea class="form-control" name="descripcion" rows="3">${configEdit != null ? configEdit.descripcion : ''}</textarea>
                            </div>

                            <div class="d-grid gap-2">
                                <button type="submit" class="btn fw-bold text-white" style="background-color: #004884;">Guardar Configuración</button>
                                <c:if test="${not empty configEdit}">
                                    <a href="${pageContext.request.contextPath}/admin/configuracion" class="btn btn-outline-secondary fw-bold">Cancelar Editar</a>
                                </c:if>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Tabla de Lista -->
            <div class="col-lg-8">
                <div class="card border-0 shadow-sm">
                    <div class="card-body">
                        <table id="configTable" class="table table-striped w-100">
                            <thead class="table-dark">
                                <tr>
                                    <th>Clave</th>
                                    <th>Valor</th>
                                    <th>Descripción</th>
                                    <th class="text-center">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="c" items="${listaConfiguraciones}">
                                    <tr>
                                        <td><strong>${c.clave}</strong></td>
                                        <td style="word-break: break-word;"><span class="text-primary fw-bold">${c.valor}</span></td>
                                        <td>${c.descripcion}</td>
                                        <td class="text-center">
                                            <a href="${pageContext.request.contextPath}/admin/configuracion?action=edit&id=${c.id}" class="btn btn-sm btn-outline-primary mb-1" title="Editar">
                                                <i class="bi bi-pencil-square"></i>
                                            </a>
                                            <a href="${pageContext.request.contextPath}/admin/configuracion?action=delete&id=${c.id}" class="btn btn-sm btn-outline-danger mb-1" title="Borrar" onclick="return confirm('¿Estás seguro de borrar esta configuración?');">
                                                <i class="bi bi-trash"></i>
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="../inc/footer.jsp" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <!-- DataTables JS -->
    <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.4/js/dataTables.bootstrap5.min.js"></script>
    <script src="https://cdn.datatables.net/responsive/2.4.1/js/dataTables.responsive.min.js"></script>
    <script src="https://cdn.datatables.net/responsive/2.4.1/js/responsive.bootstrap5.min.js"></script>
    
    <script>
        $(document).ready(function () {
            $('#configTable').DataTable({
                language: {
                    "decimal": "",
                    "emptyTable": "No hay datos disponibles en la tabla",
                    "info": "Mostrando _START_ a _END_ de _TOTAL_ entradas",
                    "infoEmpty": "Mostrando 0 a 0 de 0 entradas",
                    "infoFiltered": "(filtrado de _MAX_ entradas totales)",
                    "infoPostFix": "",
                    "thousands": ",",
                    "lengthMenu": "Mostrar _MENU_ entradas",
                    "loadingRecords": "Cargando...",
                    "processing": "Procesando...",
                    "search": "Buscar:",
                    "zeroRecords": "No se encontraron registros coincidentes",
                    "paginate": {
                        "first": "Primero",
                        "last": "Último",
                        "next": "Siguiente",
                        "previous": "Anterior"
                    }
                },
                "responsive": true,
                "autoWidth": false,
                "order": [[0, "asc"]]
            });
        });
    </script>
</body>

</html>
