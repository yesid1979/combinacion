<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Roles y Permisos - DAGJP</title>
            
    <jsp:include page="../inc/head_assets.jsp" />

            <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
            <style>
                table { width: 100% !important; }
                .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
                .table td { vertical-align: middle; white-space: normal !important; word-break: break-word; }
                .rol-name { color: #0d6efd; font-weight: bold; text-transform: uppercase; }
                .dataTables_wrapper .dataTables_filter input { border: 1px solid #dee2e6; border-radius: 4px; padding: 4px 8px; }
            </style>
        </head>

        <body class="bg-light d-flex flex-column min-vh-100">
            <jsp:include page="/inc/navbar.jsp" />

            <div class="container mt-4 mb-5 flex-grow-1" data-context-path="${pageContext.request.contextPath}">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb breadcrumb-premium">
                        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
                        <li class="breadcrumb-item active text-muted">Administración</li>
                        <li class="breadcrumb-item active" aria-current="page"><i class="bi bi-shield-lock-fill me-1"></i>Roles</li>
                    </ol>
                </nav>
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
                    <h2 class="fw-bold text-dark mb-0">Roles registrados</h2>
                    <div class="d-flex flex-wrap gap-2">
                        <a href="${pageContext.request.contextPath}/admin/roles?action=new" class="btn btn-success fw-bold">
                            <i class="bi bi-plus-circle me-1"></i> Nuevo rol
                        </a>
                    </div>
                </div>

                <div class="card border-0 shadow-sm">
                    <div class="card-body">
                        <table class="table table-striped w-100 nowrap" id="tablaRoles">
                            <thead class="table-dark">
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre del Rol</th>
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
            </div>

            <jsp:include page="/inc/footer.jsp" />
    <jsp:include page="../inc/scripts_assets.jsp" />

            <script data-context="${pageContext.request.contextPath}" id="scriptConfig">
                $(document).ready(function() {
                    $('#tablaRoles').DataTable({
                        "processing": true,
                        "serverSide": true,
                        "responsive": true,
                        "autoWidth": false,
                        "ajax": {
                            "url": "${pageContext.request.contextPath}/admin/roles",
                            "type": "POST",
                            "data": function(d) {
                                d.action = "data";
                            }
                        },
                        "columns": [
                            { "data": 0 }, // ID
                            { 
                                "data": 1,
                                "render": function(data, type, row) {
                                    return '<span class="text-primary fw-bold">' + data + '</span>';
                                }
                            },
                            { "data": 2 }, // Descripcion
                            {
                                "data": 3, // ID para acciones
                                "className": "text-center",
                                "orderable": false,
                                "render": function (data, type, row) {
                                    let btnEdit = '<a href="${pageContext.request.contextPath}/admin/roles?action=edit&id=' + data + '" class="btn btn-sm btn-outline-primary" title="Editar"><i class="bi bi-pencil-square"></i></a> ';
                                    let btnDel = '<button onclick="confirmarEliminar(' + data + ')" class="btn btn-sm btn-outline-danger" title="Eliminar"><i class="bi bi-trash"></i></button>';
                                    
                                    return '<div class="d-flex justify-content-center gap-2">' + btnEdit + btnDel + '</div>';
                                }
                            }
                        ],
                        "language": {
                            "url": "${pageContext.request.contextPath}/assets/vendor/datatables/es-ES.json"
                        }
                    });
                });

                function confirmarEliminar(id) {
                    const ctx = document.querySelector('.container[data-context-path]').getAttribute('data-context-path');
                    Swal.fire({
                        title: '¿Eliminar Rol?',
                        text: "Se borrará el rol de forma permanente.",
                        icon: 'warning',
                        showCancelButton: true,
                        confirmButtonColor: '#dc3545',
                        confirmButtonText: 'Sí, eliminar',
                        cancelButtonText: 'Cancelar'
                    }).then((result) => {
                        if (result.isConfirmed) {
                             window.location.href = ctx + '/admin/roles?action=delete&id=' + id;
                        }
                    });
                }
            </script>
        </body>
        </html>
