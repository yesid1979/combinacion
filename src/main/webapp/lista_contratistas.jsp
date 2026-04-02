<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Lista de Contratistas - DAGJP</title>
            <!-- Bootstrap 5 CSS -->
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <!-- Bootstrap Icons CSS -->
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
            <!-- DataTables CSS for advanced table features -->
            <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
            <!-- DataTables Responsive CSS -->
            <link href="https://cdn.datatables.net/responsive/2.4.1/css/responsive.bootstrap5.min.css" rel="stylesheet">
            <!-- Custom Styles (The secret of the "Combinación" look) -->
            <link href="assets/css/styles.css" rel="stylesheet">
            <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
            <style>
                .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
                .table td { vertical-align: middle; }
                .flex-grow-1 { flex-grow: 1 !important; }
            </style>
        </head>

        <body class="bg-light d-flex flex-column min-vh-100">
            <jsp:include page="inc/navbar.jsp" />

            <div class="container mt-4 mb-5 flex-grow-1">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h3 class="fw-bold text-dark mb-0">Contratistas registrados</h3>
                    <c:if test="${sessionScope.usuario.tienePermiso('CONTRATISTAS_CREAR')}">
                        <a href="${pageContext.request.contextPath}/contratistas?action=new" class="btn text-white fw-bold" style="background-color: #198754;">
                            <i class="bi bi-person-plus-fill me-1"></i>Nuevo contratista
                        </a>
                    </c:if>
                </div>

                <div class="card border-0 shadow-sm">
                    <div class="card-body">
                        <table class="table table-striped w-100" id="tablaContratistas">
                            <thead class="table-dark">
                                <tr>
                                    <th>Cédula</th>
                                    <th>Nombres y Apellidos</th>
                                    <th>Correo</th>
                                    <th>Teléfono</th>
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

            <jsp:include page="inc/footer.jsp" />

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
                    $('#tablaContratistas').DataTable({
                        "processing": true,
                        "serverSide": true,
                        "responsive": true,
                        "autoWidth": false,
                        "ajax": {
                            "url": "${pageContext.request.contextPath}/contratistas?action=data",
                            "type": "POST"
                        },
                        "columns": [
                            { "data": 0 }, // Cedula
                            { 
                                "data": 1, 
                                "render": function(data, type, row) {
                                    return '<span class="text-primary fw-bold">' + data + '</span>';
                                }
                            },
                            { "data": 2 }, // Correo
                            { "data": 3 }, // Teléfono
                            {
                                "data": 4, // ID para acciones
                                "className": "text-center",
                                "orderable": false,
                                "render": function (data, type, row) {
                                    let btnView = '<a href="contratistas?action=view&id=' + data + '" class="btn btn-sm btn-outline-info" title="Ver"><i class="bi bi-eye"></i></a> ';
                                    let btnEdit = '';
                                    let btnDel = '';
                                    
                                    <c:if test="${sessionScope.usuario.tienePermiso('CONTRATISTAS_EDITAR')}">
                                        btnEdit = '<a href="contratistas?action=edit&id=' + data + '" class="btn btn-sm btn-outline-primary" title="Editar"><i class="bi bi-pencil-square"></i></a> ';
                                    </c:if>
                                    
                                    <c:if test="${sessionScope.usuario.tienePermiso('CONTRATISTAS_ELIMINAR')}">
                                        btnDel = '<button onclick="confirmarEliminar(' + data + ')" class="btn btn-sm btn-outline-danger" title="Eliminar"><i class="bi bi-trash"></i></button>';
                                    </c:if>
                                    
                                    return '<div class="d-flex justify-content-center gap-2">' + btnView + btnEdit + btnDel + '</div>';
                                }
                            }
                        ],
                        "language": {
                            "url": "https://cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json"
                        }
                    });

                    // Alertas de estado
                    const urlParams = new URLSearchParams(window.location.search);
                    const status = urlParams.get('status');
                    if (status === 'created') Swal.fire('¡Listo!', 'Contratista registrado con éxito.', 'success');
                    if (status === 'updated') Swal.fire('¡Listo!', 'Contratista actualizado.', 'success');
                    if (status === 'deleted') Swal.fire('¡Eliminado!', 'El registro ha sido removido.', 'success');
                    if (status === 'error') Swal.fire('Error', 'No se pudo procesar la solicitud.', 'error');

                    if (status) window.history.replaceState({}, document.title, window.location.pathname);
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
                            window.location.href = '${pageContext.request.contextPath}/contratistas?action=delete&id=' + id;
                        }
                    });
                }
            </script>
        </body>

        </html>