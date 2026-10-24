<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gesti&oacute;n de Usuarios - DAGJP</title>
    
    <jsp:include page="../inc/head_assets.jsp" />

    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
    <style>
        .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
        .table td { vertical-align: middle; }
        .badge { font-weight: 600; padding: 6px 12px; border-radius: 6px; text-transform: uppercase; font-size: 0.75rem; }
        .badge-activo { background-color: #198754; color: #fff; }
        .badge-inactivo { background-color: #dc3545; color: #fff; }
    </style>
</head>
<body class="bg-light d-flex flex-column min-vh-100">
    <jsp:include page="/inc/navbar.jsp" />

    <div class="container mt-4 mb-5 flex-grow-1">

            <nav aria-label="breadcrumb">
                <ol class="breadcrumb breadcrumb-premium">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
                    <li class="breadcrumb-item active text-muted">Administración</li>
                    <li class="breadcrumb-item active" aria-current="page"><i class="bi bi-people-fill me-1"></i>Usuarios</li>
                </ol>
            </nav>
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="fw-bold text-dark mb-0">Usuarios registrados</h3>
            <a href="${pageContext.request.contextPath}/admin/usuarios?action=new" class="btn text-white fw-bold" style="background-color: #198754;">
                <i class="bi bi-person-plus-fill me-1"></i>Nuevo usuario
            </a>
        </div>

        <div class="card border-0 shadow-sm">
            <div class="card-body">
                <table class="table table-striped w-100 nowrap" id="tablaUsuarios">
                    <thead class="table-dark">
                        <tr>
                            <th>No. de Cédula</th>
                            <th>Usuario</th>
                            <th>Nombres y Apellidos</th>
                            <th>Correo electrónico</th>
                            <th>Vinculación</th>
                            <th>Estado</th>
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
    <!-- Scripts -->
    <jsp:include page="../inc/scripts_assets.jsp" />

    <script>
        $(document).ready(function() {
            $('#tablaUsuarios').DataTable({
                "processing": true,
                "serverSide": true,
                "responsive": true,
                "autoWidth": false,
                "ajax": {
                    "url": "${pageContext.request.contextPath}/admin/usuarios",
                    "type": "POST",
                    "data": function(d) {
                        d.action = "data";
                    }
                },
                "columns": [
                    { "data": 0 }, // Cedula
                    { 
                        "data": 1,
                        "render": function(data, type, row) {
                            return '<span class="text-primary fw-bold">' + data + '</span>';
                        }
                    },
                    { "data": 2 }, // Nombre
                    { "data": 3 }, // Correo
                    { 
                        "data": 4, // Vinculacion
                        "render": function(data, type, row) {
                            return '<span class="badge bg-info text-dark bg-opacity-25">' + data + '</span>';
                        }
                    },
                    { 
                        "data": 5, // Activo
                        "render": function(data, type, row) {
                            let isActive = (data === "true" || data === true);
                            return '<span class="badge ' + (isActive ? 'badge-activo' : 'badge-inactivo') + '">' + 
                                   (isActive ? 'Activo' : 'Inactivo') + '</span>';
                        }
                    },
                    {
                        "data": 6, // ID para acciones
                        "className": "text-center",
                        "orderable": false,
                        "render": function (data, type, row) {
                            let btnPerms = '<a href="${pageContext.request.contextPath}/admin/usuarios?action=permissions&id=' + data + '" class="btn btn-sm btn-outline-warning text-dark border-warning" title="Permisos" style="background-color: #ffc10722;"><i class="bi bi-shield-lock-fill"></i></a> ';
                            let btnEdit = '<a href="${pageContext.request.contextPath}/admin/usuarios?action=edit&id=' + data + '" class="btn btn-sm btn-outline-primary" title="Editar"><i class="bi bi-pencil-square"></i></a> ';
                            let btnDel = '<button onclick="confirmarEliminar(' + data + ')" class="btn btn-sm btn-outline-danger" title="Eliminar"><i class="bi bi-trash"></i></button>';
                            
                            return '<div class="d-flex justify-content-center gap-2">' + btnPerms + btnEdit + btnDel + '</div>';
                        }
                    }
                ],
                "language": {
                    "url": "${pageContext.request.contextPath}/assets/vendor/datatables/es-ES.json"
                }
            });

            // Check URL params for status
            const urlParams = new URLSearchParams(window.location.search);
            const status = urlParams.get('status');
            if (status === 'created') Swal.fire('¡Listo!', 'Usuario creado con &eacute;xito.', 'success');
            if (status === 'updated') Swal.fire('¡Listo!', 'Usuario actualizado con &eacute;xito.', 'success');
            if (status === 'deleted') Swal.fire('¡Eliminado!', 'El usuario ha sido removido.', 'success');
            
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
                            window.location.href = '${pageContext.request.contextPath}/admin/usuarios?action=delete&id=' + id;
                        }
                    });
                }
    </script>
</body>
</html>
