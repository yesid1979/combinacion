<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Roles y Permisos - DAGJP</title>
            <!-- Bootstrap 5 CSS -->
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <!-- Bootstrap Icons CSS -->
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
            <!-- DataTables CSS for advanced table features -->
            <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
            <!-- DataTables Responsive CSS -->
            <link href="https://cdn.datatables.net/responsive/2.4.1/css/responsive.bootstrap5.min.css" rel="stylesheet">
            <!-- Custom Styles (The secret of the "Combinación" look) -->
            <link href="../assets/css/styles.css" rel="stylesheet">
            <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
            <style>
                table { width: 100% !important; }
                .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
                .table td { vertical-align: middle; }
                .rol-name { color: #0d6efd; font-weight: bold; text-transform: uppercase; }
                .dataTables_wrapper .dataTables_filter input { border: 1px solid #dee2e6; border-radius: 4px; padding: 4px 8px; }
            </style>
        </head>

        <body class="bg-light d-flex flex-column min-vh-100">
            <jsp:include page="/inc/navbar.jsp" />

            <div class="container mt-4 mb-5 flex-grow-1" data-context-path="${pageContext.request.contextPath}">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2 class="fw-bold text-dark">Roles registrados</h2>
                    <a href="${pageContext.request.contextPath}/admin/roles?action=new" class="btn btn-success fw-bold">
                        <i class="bi bi-plus-circle me-1"></i> Nuevo rol
                    </a>
                </div>

                <div class="card border-0 shadow-sm">
                    <div class="card-body">
                        <table class="table table-striped w-100" id="tablaRoles">
                            <thead class="table-dark">
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre del Rol</th>
                                    <th>Descripción</th>
                                    <th class="text-center">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="rol" items="${listRoles}">
                                    <tr>
                                        <td><span class="text-secondary fw-bold">${rol.id}</span></td>
                                        <td>
                                            <div class="rol-name">${rol.nombre}</div>
                                        </td>
                                        <td class="text-muted">
                                            ${rol.descripcion}
                                        </td>
                                        <td class="text-center">
                                            <div class="d-flex justify-content-center gap-2">
                                                <a href="${pageContext.request.contextPath}/admin/roles?action=edit&id=${rol.id}" 
                                                   class="btn btn-sm btn-outline-primary" title="Editar">
                                                    <i class="bi bi-pencil-square"></i>
                                                </a>
                                                <button type="button" onclick="confirmarEliminar(${rol.id})" 
                                                        class="btn btn-sm btn-outline-danger" title="Eliminar">
                                                    <i class="bi bi-trash"></i>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <jsp:include page="/inc/footer.jsp" />

            <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
            <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
            <script src="https://cdn.datatables.net/1.13.4/js/dataTables.bootstrap5.min.js"></script>
            <script src="https://cdn.datatables.net/responsive/2.4.1/js/dataTables.responsive.min.js"></script>
            <script src="https://cdn.datatables.net/responsive/2.4.1/js/responsive.bootstrap5.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

            <script data-context="${pageContext.request.contextPath}" id="scriptConfig">
                $(document).ready(function() {
                    $('#tablaRoles').DataTable({
                        language: { url: 'https://cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json' },
                        "processing": true,
                        "serverSide": false,
                        "responsive": true,
                        "autoWidth": false,
                        "columnDefs": [{ "orderable": false, "targets": 3 }]
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
