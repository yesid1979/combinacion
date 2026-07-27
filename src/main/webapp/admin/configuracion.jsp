<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Configuración - Gestión Contratos</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons CSS -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <!-- DataTables CSS for advanced table features -->
    <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
    <!-- DataTables Responsive CSS -->
    <link href="https://cdn.datatables.net/responsive/2.4.1/css/responsive.bootstrap5.min.css" rel="stylesheet">
    <!-- Custom Styles -->
    <link href="${pageContext.request.contextPath}/assets/css/styles.css" rel="stylesheet">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
    <style>
        .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
        .table td { vertical-align: middle; }
        .flex-grow-1 { flex-grow: 1 !important; }
    </style>
</head>

<body class="bg-light d-flex flex-column min-vh-100">
    <jsp:include page="../inc/navbar.jsp" />

    <div class="container mt-4 mb-5 flex-grow-1">

        <nav aria-label="breadcrumb">
            <ol class="breadcrumb breadcrumb-premium">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
                <li class="breadcrumb-item active text-muted">Administración</li>
                <li class="breadcrumb-item active" aria-current="page"><i class="bi bi-gear-fill me-1"></i>Configuración del Sistema</li>
            </ol>
        </nav>

        <c:if test="${not empty param.msg}">
            <div class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
                <c:choose>
                    <c:when test="${param.msg == 'success'}"><i class="bi bi-check-circle-fill me-2"></i>Configuración guardada exitosamente.</c:when>
                    <c:when test="${param.msg == 'deleted'}"><i class="bi bi-trash-fill me-2"></i>Configuración eliminada exitosamente.</c:when>
                </c:choose>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${param.action == 'new' || not empty configEdit}">
                <div class="d-flex justify-content-between align-items-center mb-4 mt-3">
                    <h3 class="fw-bold text-dark mb-0">
                        <c:choose>
                            <c:when test="${not empty configEdit}">Editar Configuración</c:when>
                            <c:otherwise>Nueva Configuración</c:otherwise>
                        </c:choose>
                    </h3>
                </div>

                <div class="card border-0 shadow-sm">
                    <div class="card-body p-4">
                        <form action="${pageContext.request.contextPath}/admin/configuracion" method="POST">
                            <input type="hidden" name="action" value="save">
                            <input type="hidden" name="id" value="${configEdit != null ? configEdit.id : ''}">

                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">Clave (Única, ej. DRIVE_CARPETA)</label>
                                    <input type="text" class="form-control" name="clave" value="${configEdit != null ? configEdit.clave : ''}" required>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">Valor</label>
                                    <input type="text" class="form-control" name="valor" value="${configEdit != null ? configEdit.valor : ''}" required>
                                </div>
                            </div>

                            <div class="mb-4">
                                <label class="form-label fw-bold">Descripción</label>
                                <textarea class="form-control" name="descripcion" rows="3">${configEdit != null ? configEdit.descripcion : ''}</textarea>
                            </div>

                            <div class="text-end">
                                <a href="${pageContext.request.contextPath}/admin/configuracion" class="btn btn-secondary fw-bold px-4 me-2">Cancelar</a>
                                <button type="submit" class="btn btn-primary fw-bold px-4" style="background-color: #004884; border-color: #004884;">Guardar</button>
                            </div>
                        </form>
                    </div>
                </div>
            </c:when>
            
            <c:otherwise>
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h3 class="fw-bold text-dark mb-0">Configuraciones registradas</h3>
                    <div>
                        <a href="${pageContext.request.contextPath}/admin/configuracion?action=new" class="btn text-white fw-bold" style="background-color: #198754;">
                            <i class="bi bi-plus-circle-fill me-1"></i>Nueva configuración
                        </a>
                    </div>
                </div>

                <div class="card border-0 shadow-sm">
                    <div class="card-body">
                        <table class="table table-striped w-100" id="configTableModern">
                            <thead class="table-dark">
                                <tr>
                                    <th>Clave</th>
                                    <th>Valor</th>
                                    <th>Descripción</th>
                                    <th class="text-center">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <!-- Datos cargados por AJAX -->
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <jsp:include page="../inc/footer.jsp" />

    <!-- Scripts -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.4/js/dataTables.bootstrap5.min.js"></script>
    <!-- DataTables Responsive JS -->
    <script src="https://cdn.datatables.net/responsive/2.4.1/js/dataTables.responsive.min.js"></script>
    <script src="https://cdn.datatables.net/responsive/2.4.1/js/responsive.bootstrap5.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    
    <script>
        $(document).ready(function () {
            if ($('#configTableModern').length) {
                $('#configTableModern').DataTable({
                    "processing": true,
                    "serverSide": true,
                    "responsive": true,
                    "autoWidth": false,
                    "ajax": {
                        "url": "${pageContext.request.contextPath}/admin/configuracion",
                        "type": "POST",
                        "data": function(d) {
                            d.action = "data";
                        },
                        "error": function(xhr, error, thrown) {
                            console.error("Error en AJAX:", error, thrown);
                            Swal.fire('Error', 'No se pudieron cargar los datos.', 'error');
                        }
                    },
                    "columns": [
                        {
                            "data": 0,
                            "render": function(data, type, row) {
                                return '<strong>' + data + '</strong>';
                            }
                        },
                        {
                            "data": 1,
                            "render": function(data, type, row) {
                                return '<span class="text-primary fw-bold" style="word-break: break-word;">' + data + '</span>';
                            }
                        },
                        { "data": 2 },
                        {
                            "data": 3,
                            "className": "text-center",
                            "orderable": false,
                            "render": function(data, type, row) {
                                let btnEdit = '<a href="${pageContext.request.contextPath}/admin/configuracion?action=edit&id=' + data + '" class="btn btn-sm btn-outline-primary" title="Editar"><i class="bi bi-pencil-square"></i></a> ';
                                let btnDel = '<button onclick="confirmarEliminar(' + data + ')" class="btn btn-sm btn-outline-danger" title="Eliminar"><i class="bi bi-trash"></i></button>';
                                return '<div class="d-flex justify-content-center gap-2">' + btnEdit + btnDel + '</div>';
                            }
                        }
                    ],
                    "order": [[0, "asc"]],
                    "language": {
                        "url": "https://cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json"
                    }
                });
            }
        });

        function confirmarEliminar(id) {
            Swal.fire({
                title: '¿Estás seguro?',
                text: "Esta acción no se puede deshacer",
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#dc3545',
                cancelButtonColor: '#6c757d',
                confirmButtonText: 'Sí, eliminar',
                cancelButtonText: 'Cancelar'
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '${pageContext.request.contextPath}/admin/configuracion?action=delete&id=' + id;
                }
            });
        }
    </script>
</body>
</html>
