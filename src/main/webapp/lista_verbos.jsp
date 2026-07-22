<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Diccionario de Verbos - DAGJP</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
    <link href="assets/css/styles.css" rel="stylesheet">
</head>
<body class="bg-light">

    <%@ include file="inc/navbar.jsp" %>

    <div class="container-fluid py-4">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb breadcrumb-premium">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
                <li class="breadcrumb-item active text-muted">Datos Maestros</li>
                <li class="breadcrumb-item active" aria-current="page"><i class="bi bi-list-task me-1"></i>Verbos</li>
            </ol>
        </nav>
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1 class="h3 mb-0 text-gray-800">
                <i class="bi bi-translate text-primary me-2"></i>Diccionario de Verbos (Conjugación Automática)
            </h1>
            <c:if test="${sessionScope.usuario.tienePermiso('VERBOS_CREAR') || sessionScope.usuario.tienePermiso('ADMIN_VER')}">
                <a href="${pageContext.request.contextPath}/verbos?action=new" class="btn btn-primary shadow-sm">
                    <i class="bi bi-plus-circle me-2"></i>Nuevo Verbo
                </a>
            </c:if>
        </div>

        <c:if test="${not empty sessionScope.successMsg}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${sessionScope.successMsg}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            <c:remove var="successMsg" scope="session"/>
        </c:if>

        <c:if test="${not empty sessionScope.errorMsg}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                ${sessionScope.errorMsg}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            <c:remove var="errorMsg" scope="session"/>
        </c:if>

        <div class="card shadow-sm border-0">
            <div class="card-body p-4">
                <div class="table-responsive">
                    <table id="tablaVerbos" class="table table-hover table-striped align-middle" style="width:100%">
                        <thead class="table-dark">
                            <tr>
                                <th>3ra Persona (Ej. Realizó)</th>
                                <th>1ra Persona (Ej. Realicé)</th>
                                <th>Estado</th>
                                <th class="text-center">Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.4/js/dataTables.bootstrap5.min.js"></script>
    
    <script>
        $(document).ready(function() {
            $('#tablaVerbos').DataTable({
                "processing": true,
                "serverSide": true,
                "ajax": {
                    "url": "${pageContext.request.contextPath}/verbos",
                    "type": "POST",
                    "data": function(d) {
                        d.action = "data";
                    }
                },
                "columns": [
                    { "data": "terceraPersona" },
                    { "data": "primeraPersona" },
                    { 
                        "data": "activo",
                        "className": "text-center",
                        "render": function(data) {
                            if (data) return '<span class="badge bg-success">Activo</span>';
                            return '<span class="badge bg-secondary">Inactivo</span>';
                        }
                    },
                    { 
                        "data": null,
                        "className": "text-center",
                        "orderable": false,
                        "render": function(data, type, row) {
                            let html = '<div class="d-flex justify-content-center gap-2">';
                            if (row.canEdit) {
                                html += '<a href="${pageContext.request.contextPath}/verbos?action=edit&id=' + row.id + '" class="btn btn-sm btn-outline-primary" title="Editar"><i class="bi bi-pencil-square"></i></a>';
                            }
                            if (row.canDelete) {
                                html += '<form action="${pageContext.request.contextPath}/verbos" method="post" onsubmit="return confirm(\'¿Seguro que deseas eliminar este verbo?\');" style="margin:0;">';
                                html += '<input type="hidden" name="action" value="delete">';
                                html += '<input type="hidden" name="id" value="' + row.id + '">';
                                html += '<button type="submit" class="btn btn-sm btn-outline-danger" title="Eliminar"><i class="bi bi-trash"></i></button>';
                                html += '</form>';
                            }
                            html += '</div>';
                            return html;
                        }
                    }
                ],
                "language": {
                    "decimal": "",
                    "emptyTable": "No hay datos disponibles en la tabla",
                    "info": "Mostrando _START_ a _END_ de _TOTAL_ registros",
                    "infoEmpty": "Mostrando 0 a 0 de 0 registros",
                    "infoFiltered": "(filtrado de _MAX_ registros totales)",
                    "infoPostFix": "",
                    "thousands": ",",
                    "lengthMenu": "Mostrar _MENU_ registros",
                    "loadingRecords": "Cargando...",
                    "processing": "Procesando...",
                    "search": "Buscar:",
                    "zeroRecords": "No se encontraron registros coincidentes",
                    "paginate": {
                        "first": "Primero",
                        "last": "Último",
                        "next": "Siguiente",
                        "previous": "Anterior"
                    },
                    "aria": {
                        "sortAscending": ": activar para ordenar la columna ascendente",
                        "sortDescending": ": activar para ordenar la columna descendente"
                    }
                }
            });
        });
    </script>
</body>
</html>

