<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Gestión de Consecutivos - DAGJP</title>
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="https://cdn.datatables.net/1.13.6/css/dataTables.bootstrap5.min.css">
    <link href="https://cdn.datatables.net/responsive/2.4.1/css/responsive.bootstrap5.min.css" rel="stylesheet">
    <style>
        .table td { vertical-align: middle; white-space: normal !important; word-break: break-word; }
    </style>
    <link href="assets/css/styles.css" rel="stylesheet">
</head>

<body class="bg-light d-flex flex-column min-vh-100">

    <jsp:include page="inc/navbar.jsp" />

    <div class="container container-main mt-4 mb-5">
        
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb breadcrumb-premium">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
                <li class="breadcrumb-item active text-muted">Datos Maestros</li>
                <li class="breadcrumb-item active" aria-current="page"><i class="bi bi-123 me-1"></i>Consecutivos</li>
            </ol>
        </nav>
        
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
            <h3 class="fw-bold text-dark mb-0">Gestión Masiva de Consecutivos</h3>
            <div class="d-flex gap-3 align-items-center flex-wrap">
                <select id="filtroAnio" class="form-select fw-bold" style="width: 120px;">
                    <c:forEach var="anioItem" items="${aniosDisponibles}">
                        <option value="${anioItem}" <c:if test="${anioItem == anioActual}">selected</c:if>>${anioItem}</option>
                    </c:forEach>
                    <c:if test="${empty aniosDisponibles}">
                        <option value="2026">2026</option>
                    </c:if>
                </select>
                <a href="#" id="btnDescargarPlantilla" class="btn btn-outline-success fw-bold px-3">
                    <i class="bi bi-file-earmark-excel-fill me-1"></i>Descargar Plantilla
                </a>
            </div>
        </div>

        <div class="card border-0 shadow-sm mb-4">
            <div class="card-body bg-white rounded">
                <form action="consecutivos" method="post" enctype="multipart/form-data" class="d-flex flex-column flex-md-row align-items-md-center gap-3 w-100">
                    <input type="hidden" name="action" value="upload">
                    <input type="hidden" name="anio_carga" id="anio_carga" value="2026">
                    <div class="flex-grow-1">
                        <label for="fileExcel" class="form-label fw-bold">Seleccionar archivo Excel</label>
                        <input class="form-control" type="file" id="fileExcel" name="fileExcel" accept=".xlsx,.xls" required>
                        <div class="form-text">Asegúrese de usar la plantilla descargada con las columnas Cédula, Contrato, Cuota y Consecutivo.</div>
                    </div>
                    <div class="mt-3">
                        <button type="submit" class="btn text-white fw-bold px-4" style="background-color: #004884;">
                            <i class="bi bi-cloud-arrow-up-fill me-2"></i>Subir Archivo
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <div class="card border-0 shadow-sm">
            <div class="card-header bg-white border-bottom-0 pt-4 pb-0 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
                <h5 class="fw-bold mb-0">Últimos consecutivos cargados</h5>
                <div>
                    <button onclick="eliminarSeleccionados()" class="btn btn-warning btn-sm fw-bold text-dark">
                        <i class="bi bi-trash me-1"></i>Eliminar Seleccionados
                    </button>
                </div>
            </div>
            <div class="card-body">
                <table id="consecutivosTable" class="table table-striped table-hover w-100 nowrap">
                    <thead class="table-dark">
                        <tr>
                            <th style="width: 40px;" class="text-center">
                                <input type="checkbox" class="form-check-input" id="checkAll">
                            </th>
                            <th>Cédula</th>
                            <th>Nombre</th>
                            <th>Contrato</th>
                            <th>No. Cuota</th>
                            <th>Consecutivo Asignado</th>
                            <th>Fecha de Carga</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <jsp:include page="inc/footer.jsp" />

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.6/js/jquery.dataTables.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.6/js/dataTables.bootstrap5.min.js"></script>
    <script src="https://cdn.datatables.net/responsive/2.4.1/js/dataTables.responsive.min.js"></script>
    <script src="https://cdn.datatables.net/responsive/2.4.1/js/responsive.bootstrap5.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    
    <script>
        $(document).ready(function () {
            const urlParams = new URLSearchParams(window.location.search);
            const status = urlParams.get('status');
            const error = urlParams.get('error');
            const count = urlParams.get('count');

            if (status === 'uploaded') Swal.fire('¡Éxito!', count + ' consecutivos han sido cargados o actualizados correctamente.', 'success');
            else if (status === 'cleaned') Swal.fire('¡Limpieza completa!', 'Todos los consecutivos han sido eliminados de la base de datos.', 'success');
            
            if (error === 'no_file') Swal.fire('Error', 'Debe seleccionar un archivo.', 'error');
            else if (error === 'empty_or_invalid') Swal.fire('Archivo Vacío', 'El archivo no tiene datos o el formato no es válido.', 'warning');
            else if (error === 'db_error') Swal.fire('Error', 'Hubo un error al guardar en la base de datos.', 'error');
            else if (error === 'parse_error') Swal.fire('Error', 'El formato del archivo Excel es incorrecto o está dañado.', 'error');

            if (status || error) window.history.replaceState({}, document.title, window.location.pathname);

            // Configuración inicial del enlace de descarga y el input de subida
            var initialAnio = $('#filtroAnio').val();
            $('#btnDescargarPlantilla').attr('href', '${pageContext.request.contextPath}/consecutivos?action=template&anio=' + initialAnio);
            $('#anio_carga').val(initialAnio);

            // Inicializar DataTables Server-Side
            var table = $('#consecutivosTable').DataTable({
                "processing": true,
                "serverSide": true,
                "responsive": true,
                "ajax": {
                    "url": "${pageContext.request.contextPath}/consecutivos?action=list_ajax",
                    "data": function ( d ) {
                        d.anio = $('#filtroAnio').val();
                    }
                },
                "columns": [
                    { 
                        "data": "id",
                        "orderable": false,
                        "searchable": false,
                        "render": function(data) {
                            return '<div class="text-center"><input type="checkbox" class="form-check-input check-item" value="' + data + '"></div>';
                        }
                    },
                    { "data": "cedula", "render": function(data) { return '<strong>' + data + '</strong>'; } },
                    { "data": "nombre" },
                    { "data": "contrato" },
                    { "data": "numeroCuota" },
                    { "data": "consecutivo", "render": function(data) { return '<span class="badge bg-success" style="font-size: 14px;">' + data + '</span>'; } },
                    { "data": "fechaCarga", "orderable": false }
                ],
                "order": [[6, 'desc']],
                "language": {
                    "url": "https://cdn.datatables.net/plug-ins/1.13.6/i18n/es-ES.json"
                }
            });

            // Evento cuando se cambia el año en el menú
            $('#filtroAnio').on('change', function() {
                var anio = $(this).val();
                $('#btnDescargarPlantilla').attr('href', '${pageContext.request.contextPath}/consecutivos?action=template&anio=' + anio);
                $('#anio_carga').val(anio);
                table.ajax.reload();
            });

            // Seleccionar/Deseleccionar todos
            $('#checkAll').on('click', function() {
                $('.check-item').prop('checked', this.checked);
            });
            
            // Actualizar el estado del checkbox general si se cambian los individuales
            $('#consecutivosTable tbody').on('change', '.check-item', function() {
                if ($('.check-item:checked').length === $('.check-item').length) {
                    $('#checkAll').prop('checked', true);
                } else {
                    $('#checkAll').prop('checked', false);
                }
            });
        });

        function eliminarSeleccionados() {
            var selected = [];
            $('.check-item:checked').each(function() {
                selected.push($(this).val());
            });

            if (selected.length === 0) {
                Swal.fire('Atención', 'Debe seleccionar al menos un consecutivo para eliminar.', 'warning');
                return;
            }

            Swal.fire({
                title: '¿Estás seguro?',
                text: "Vas a eliminar " + selected.length + " registro(s). Esta acción no se puede deshacer.",
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#ffc107',
                cancelButtonColor: '#6c757d',
                confirmButtonText: 'Sí, eliminarlos',
                cancelButtonText: 'Cancelar'
            }).then((result) => {
                if (result.isConfirmed) {
                    $.post('${pageContext.request.contextPath}/consecutivos', {
                        action: 'delete_multiple',
                        ids: selected
                    }, function(response) {
                        if (response.success) {
                            Swal.fire('¡Eliminados!', 'Los registros fueron eliminados.', 'success');
                            $('#consecutivosTable').DataTable().ajax.reload();
                            $('#checkAll').prop('checked', false);
                        } else {
                            Swal.fire('Error', 'Hubo un problema al eliminar los registros.', 'error');
                        }
                    }, 'json');
                }
            });
        }

    </script>
</body>
</html>
