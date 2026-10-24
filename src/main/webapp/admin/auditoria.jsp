<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Auditoría del Sistema - SGC</title>
    
    <jsp:include page="../inc/head_assets.jsp" />

    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
    <style>
        .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
        .table td { vertical-align: middle; }
    </style>
</head>
<body class="bg-light d-flex flex-column min-vh-100">
    <jsp:include page="../inc/navbar.jsp" />
    
    <div class="container mt-4 mb-5 flex-grow-1">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb breadcrumb-premium">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
                <li class="breadcrumb-item active text-muted">Administración</li>
                <li class="breadcrumb-item active" aria-current="page"><i class="bi bi-display me-1"></i>Auditoría</li>
            </ol>
        </nav>
        
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="fw-bold text-dark mb-0">Auditoría del Sistema</h3>
        </div>
        
        <div class="card border-0 shadow-sm">
            <div class="card-body">
                <table class="table table-striped w-100" id="tablaAuditoria">
                    <thead class="table-dark">
                        <tr>
                            <th>ID</th>
                            <th>Fecha</th>
                            <th>Hora</th>
                            <th>Usuario</th>
                            <th>Nombres y Apellidos</th>
                            <th>Tipo de Acción</th>
                            <th class="none">Acción realizada</th>
                            <th class="none">Tipo de usuario</th>
                            <th class="none">No. de IP</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    
    <jsp:include page="../inc/footer.jsp" />
    <jsp:include page="../inc/scripts_assets.jsp" />
    
    <script>
        $(document).ready(function() {
            $('#tablaAuditoria').DataTable({
                "processing": true,
                "serverSide": true,
                "responsive": true,
                "autoWidth": false,
                "ajax": {
                    "url": "AuditoriaDataServlet",
                    "type": "POST"
                },
                "order": [[ 0, "desc" ]],
                "pageLength": 10,
                "columns": [
                    { "data": "id" },
                    { "data": "fecha" },
                    { "data": "hora", "orderable": false },
                    { 
                        "data": "username",
                        "render": function(data, type, row) {
                            return '<span class="text-primary fw-bold">' + data + '</span>';
                        }
                    },
                    { "data": "nombres_apellidos" },
                    { "data": "tipo_accion" },
                    { "data": "accion_realizada", "orderable": false },
                    { "data": "tipo_usuario", "orderable": false },
                    { "data": "ip_address", "orderable": false }
                ],
                "language": {
                    "url": "${pageContext.request.contextPath}/assets/vendor/datatables/es-ES.json"
                }
            });
        });
    </script>
</body>
</html>
