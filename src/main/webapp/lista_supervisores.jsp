<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Lista de Supervisores - DAGJP</title>
            
    <jsp:include page="inc/head_assets.jsp" />

            <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
            <style>
                .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
                .table td { vertical-align: middle; white-space: normal !important; word-break: break-word; }
            </style>
        </head>

        <body class="bg-light d-flex flex-column min-vh-100">
            <jsp:include page="/inc/navbar.jsp" />

            <div class="container mt-4 mb-5 flex-grow-1">

            <nav aria-label="breadcrumb">
                <ol class="breadcrumb breadcrumb-premium">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
                    <li class="breadcrumb-item active text-muted">Datos Maestros</li>
                    <li class="breadcrumb-item active" aria-current="page"><i class="bi bi-person-lines-fill me-1"></i>Supervisores</li>
                </ol>
            </nav>
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
                    <h3 class="fw-bold text-dark mb-0">Supervisores registrados</h3>
                    <div class="d-flex flex-wrap gap-2">
                        <c:if test="${sessionScope.usuario.tienePermiso('SUPERVISORES_CREAR')}">
                            <a href="${pageContext.request.contextPath}/supervisores?action=new" class="btn text-white fw-bold" style="background-color: #198754;">
                                <i class="bi bi-person-plus-fill me-1"></i>Nuevo supervisor
                            </a>
                        </c:if>
                        <c:if test="${sessionScope.usuario.tienePermiso('SUPERVISORES_CREAR') || sessionScope.usuario.tienePermiso('ADMIN_USUARIOS') || sessionScope.usuario.esAdministrador()}">
                            <a href="${pageContext.request.contextPath}/masivo-supervisores" class="btn text-white fw-bold" style="background-color: #004884;">
                                <i class="bi bi-person-lines-fill me-1"></i>Crear Usuarios Supervisores
                            </a>
                        </c:if>
                    </div>
                </div>

                <script>
                    const canEdit = ${sessionScope.usuario.tienePermiso('SUPERVISORES_EDITAR')};
                    const canDelete = ${sessionScope.usuario.tienePermiso('SUPERVISORES_ELIMINAR')};
                </script>

                <div class="card border-0 shadow-sm">
                    <div class="card-body">
                        <table class="table table-striped w-100 nowrap" id="tablaSupervisores">
                            <thead class="table-dark">
                                <tr>
                                    <th>Cédula</th>
                                    <th>Nombres y Apellidos</th>
                                    <th>Cargo</th>
                                    <th class="text-center">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <jsp:include page="/inc/footer.jsp" />

            <!-- Scripts -->
    <jsp:include page="inc/scripts_assets.jsp" />

            <script>
                $(document).ready(function () {
                    $('#tablaSupervisores').DataTable({
                        "processing": true,
                        "serverSide": true,
                        "responsive": true,
                        "ajax": {
                            "url": "${pageContext.request.contextPath}/supervisores",
                            "type": "POST",
                            "data": function(d) {
                                d.action = "data";
                            }
                        },
                        "columns": [
                            { "data": "cedula" },
                            { 
                                "data": "nombre",
                                "render": function(data, type, row) {
                                    return '<span class="text-primary fw-bold">' + data + '</span>';
                                }
                            },
                            { "data": "cargo" },
                            {
                                "data": "id",
                                "className": "text-center",
                                "orderable": false,
                                "render": function (data, type, row) {
                                    let html = '<div class="d-flex justify-content-center gap-2">';
                                    html += '<a href="supervisores?action=view&id=' + data + '" class="btn btn-sm btn-outline-info" title="Ver">' +
                                            '<i class="bi bi-eye"></i></a>';
                                    if (canEdit) {
                                        html += '<a href="supervisores?action=edit&id=' + data + '" class="btn btn-sm btn-outline-primary" title="Editar">' +
                                                '<i class="bi bi-pencil-square"></i></a>';
                                    }
                                    if (canDelete) {
                                        html += '<button onclick="confirmarEliminar(' + data + ')" class="btn btn-sm btn-outline-danger" title="Eliminar">' +
                                                '<i class="bi bi-trash"></i></button>';
                                    }
                                    html += '</div>';
                                    return html;
                                }
                            }
                        ],
                        "language": {
                            "url": "${pageContext.request.contextPath}/assets/vendor/datatables/es-ES.json"
                        }
                    });

                    // Alertas de estado
                    const urlParams = new URLSearchParams(window.location.search);
                    const status = urlParams.get('status');
                    if (status === 'created') Swal.fire('¡Listo!', 'Supervisor creado con éxito.', 'success');
                    if (status === 'updated') Swal.fire('¡Listo!', 'Supervisor actualizado con éxito.', 'success');
                    if (status === 'deleted') Swal.fire('¡Eliminado!', 'El supervisor ha sido removido.', 'success');
                    if (status === 'error') Swal.fire('Error', 'Ha ocurrido un error en la operación.', 'error');

                    if (status) window.history.replaceState({}, document.title, window.location.pathname);
                });

                function confirmarEliminar(id) {
                    Swal.fire({
                        title: '¿Estás seguro?',
                        text: "Esta acción no se puede deshacer.",
                        icon: 'warning',
                        showCancelButton: true,
                        confirmButtonColor: '#dc3545',
                        cancelButtonColor: '#6c757d',
                        confirmButtonText: 'Sí, eliminar',
                        cancelButtonText: 'Cancelar'
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.href = '${pageContext.request.contextPath}/supervisores?action=delete&id=' + id;
                        }
                    });
                }
            </script>
        </body>

        </html>