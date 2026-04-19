<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Lista de Supervisores - DAGJP</title>
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
            </style>
        </head>

        <body class="bg-light d-flex flex-column min-vh-100">
            <jsp:include page="/inc/navbar.jsp" />

            <div class="container mt-4 mb-5 flex-grow-1">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h3 class="fw-bold text-dark mb-0">Supervisores registrados</h3>
                    <c:if test="${sessionScope.usuario.tienePermiso('SUPERVISORES_CREAR')}">
                        <a href="${pageContext.request.contextPath}/supervisores?action=new" class="btn text-white fw-bold" style="background-color: #198754;">
                            <i class="bi bi-person-plus-fill me-1"></i>Nuevo supervisor
                        </a>
                    </c:if>
                </div>

                <script>
                    const canEdit = ${sessionScope.usuario.tienePermiso('SUPERVISORES_EDITAR')};
                    const canDelete = ${sessionScope.usuario.tienePermiso('SUPERVISORES_ELIMINAR')};
                </script>

                <div class="card border-0 shadow-sm">
                    <div class="card-body">
                        <table class="table table-striped w-100" id="tablaSupervisores">
                            <thead class="table-dark">
                                <tr>
                                    <th>Cédula</th>
                                    <th>Nombres y Apellidos</th>
                                    <th>Cargo</th>
                                    <th class="text-center">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="s" items="${listSupervisores}">
                                    <tr>
                                        <td>${s.cedula}</td>
                                        <td><span class="text-primary fw-bold">${s.nombre}</span></td>
                                        <td>${s.cargo}</td>
                                        <td class="text-center">
                                            <div class="d-flex justify-content-center gap-2">
                                                <a href="${pageContext.request.contextPath}/supervisores?action=view&id=${s.id}"
                                                   class="btn btn-sm btn-outline-info" title="Ver">
                                                    <i class="bi bi-eye"></i>
                                                </a>
                                                <a href="${pageContext.request.contextPath}/supervisores?action=edit&id=${s.id}"
                                                   class="btn btn-sm btn-outline-primary" title="Editar">
                                                    <i class="bi bi-pencil-square"></i>
                                                </a>
                                                <button onclick="confirmarEliminar(${s.id})" class="btn btn-sm btn-outline-danger" title="Eliminar">
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

            <!-- Scripts -->
            <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
            <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
            <script src="https://cdn.datatables.net/1.13.4/js/dataTables.bootstrap5.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

            <script>
                $(document).ready(function () {
                    $('#tablaSupervisores').DataTable({
                        "processing": true,
                        "serverSide": false,
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
                            "url": "https://cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json"
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