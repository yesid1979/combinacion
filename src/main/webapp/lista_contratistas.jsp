<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lista de Contratistas - DAGJP</title>
    
    <jsp:include page="inc/head_assets.jsp" />

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

            <nav aria-label="breadcrumb">
                <ol class="breadcrumb breadcrumb-premium">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
                    <li class="breadcrumb-item active text-muted">Datos Maestros</li>
                    <li class="breadcrumb-item active" aria-current="page"><i class="bi bi-briefcase-fill me-1"></i>Contratistas</li>
                </ol>
            </nav>
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
            <h3 class="fw-bold text-dark mb-0">Contratistas registrados</h3>
            <div class="d-flex flex-wrap gap-2">
                <a href="${pageContext.request.contextPath}/contratistas?action=exportExcel" class="btn btn-outline-success fw-bold">
                    <i class="bi bi-file-earmark-excel-fill me-1"></i>Exportar Excel
                </a>
                <a href="${pageContext.request.contextPath}/masivo-usuarios" class="btn text-white fw-bold" style="background-color: #004884;">
                    <i class="bi bi-people-fill me-1"></i>Generar Usuarios
                </a>
                <c:if test="${sessionScope.usuario.tienePermiso('CONTRATISTAS_CREAR')}">
                    <a href="${pageContext.request.contextPath}/contratistas?action=new" class="btn text-white fw-bold" style="background-color: #198754;">
                        <i class="bi bi-person-plus-fill me-1"></i>Nuevo contratista
                    </a>
                </c:if>
            </div>
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

    
    <jsp:include page="inc/scripts_assets.jsp" />

    <script>
        var tienePermisoEditar = ${sessionScope.usuario.tienePermiso('CONTRATISTAS_EDITAR') ? 'true' : 'false'};
        var tienePermisoEliminar = ${sessionScope.usuario.tienePermiso('CONTRATISTAS_ELIMINAR') ? 'true' : 'false'};
    </script>

    <script>
        $(document).ready(function () {
            $('#tablaContratistas').DataTable({
                "processing": true,
                "serverSide": true,
                "responsive": true,
                "autoWidth": false,
                "ajax": {
                    "url": "${pageContext.request.contextPath}/contratistas",
                    "type": "POST",
                    "data": function(d) {
                        d.action = "data";
                        d.source = "lista";
                    },
                    "error": function(xhr, error, thrown) {
                        console.error("Error en la solicitud AJAX:", error, thrown);
                        Swal.fire('Error', 'No se pudieron cargar los datos. Por favor, inténtelo de nuevo.', 'error');
                    }
                },
                "columns": [
                    { "data": 0 }, // Cédula
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
                            let btnView = '<a href="${pageContext.request.contextPath}/contratistas?action=view&id=' + data + '" class="btn btn-sm btn-outline-info" title="Ver"><i class="bi bi-eye"></i></a> ';
                            let btnEdit = '';
                            let btnDel = '';

                            if (tienePermisoEditar) {
                                btnEdit = '<a href="${pageContext.request.contextPath}/contratistas?action=edit&id=' + data + '" class="btn btn-sm btn-outline-primary" title="Editar"><i class="bi bi-pencil-square"></i></a> ';
                            }

                            if (tienePermisoEliminar) {
                                btnDel = '<button onclick="confirmarEliminar(' + data + ')" class="btn btn-sm btn-outline-danger" title="Eliminar"><i class="bi bi-trash"></i></button>';
                            }

                            return '<div class="d-flex justify-content-center flex-nowrap gap-2">' + btnView + btnEdit + btnDel + '</div>';
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