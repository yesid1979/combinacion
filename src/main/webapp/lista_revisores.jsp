<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Revisores de Documentos - GestiÃ³n de Prestadores</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
    <link href="https://cdn.datatables.net/responsive/2.4.1/css/responsive.bootstrap5.min.css" rel="stylesheet">
    <style>
        .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
        .table td { vertical-align: middle; }
    </style>
</head>
<body class="bg-white d-flex flex-column min-vh-100">

    <jsp:include page="inc/navbar.jsp" />

    <div class="container-fluid mt-4 flex-grow-1 px-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h3 class="fw-bold text-dark mb-0">Revisores de Documentos</h3>
                <p class="text-muted small">ConfiguraciÃ³n de firmas para los documentos generados en lote.</p>
            </div>
            <a href="revisores?action=new" class="btn btn-success px-4 fw-bold shadow-sm">
                <i class="bi bi-plus-circle me-2"></i>Nuevo Revisor
            </a>
        </div>

        <c:if test="${param.status == 'created'}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                Revisor creado exitosamente.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <c:if test="${param.status == 'updated'}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                Revisor actualizado exitosamente.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="card border">
            <div class="card-body">
                <table id="revisoresTable" class="table table-hover w-100 nowrap">
                    <thead>
                        <tr>
                            <th>Tipo de Documento</th>
                            <th>Nombre del Revisor</th>
                            <th>Cargo del Revisor</th>
                            <th>Ãšltima ActualizaciÃ³n</th>
                            <th class="text-center">Acciones</th>
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
    <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.4/js/dataTables.bootstrap5.min.js"></script>
    <script src="https://cdn.datatables.net/responsive/2.4.1/js/dataTables.responsive.min.js"></script>
    <script src="https://cdn.datatables.net/responsive/2.4.1/js/responsive.bootstrap5.min.js"></script>
    <script>
        $(document).ready(function() {
            $('#revisoresTable').DataTable({
                responsive: true,
                processing: true,
                serverSide: true,
                ajax: {
                    url: 'revisores',
                    type: 'POST',
                    data: function(d) {
                        d.action = 'data';
                    }
                },
                columns: [
                    { data: 'tipoDocumento', className: 'fw-bold text-primary' },
                    { data: 'nombreCompleto' },
                    { data: 'cargo' },
                    { data: 'fechaStr' },
                    {
                        data: 'id',
                        className: 'text-center',
                        orderable: false,
                        render: function(data) {
                            return '<a href="revisores?action=edit&id=' + data + '" class="btn btn-sm btn-outline-primary" title="Editar"><i class="bi bi-pencil"></i></a> ' +
                                   '<button class="btn btn-sm btn-outline-danger ms-1 btn-delete" data-id="' + data + '" title="Eliminar"><i class="bi bi-trash"></i></button>';
                        }
                    }
                ],
                language: {
                    "decimal": "",
                    "emptyTable": "No hay datos disponibles en la tabla",
                    "info": "Mostrando _START_ a _END_ de _TOTAL_ entradas",
                    "infoEmpty": "Mostrando 0 a 0 de 0 entradas",
                    "infoFiltered": "(filtrado de _MAX_ entradas totales)",
                    "infoPostFix": "",
                    "thousands": ",",
                    "lengthMenu": "Mostrar _MENU_ entradas",
                    "loadingRecords": "Cargando...",
                    "processing": "Procesando...",
                    "search": "Buscar:",
                    "zeroRecords": "No se encontraron registros coincidentes",
                    "paginate": {
                        "first": "Primero",
                        "last": "Ãšltimo",
                        "next": "Siguiente",
                        "previous": "Anterior"
                    },
                    "aria": {
                        "sortAscending": ": activar para ordenar la columna ascendente",
                        "sortDescending": ": activar para ordenar la columna descendente"
                    }
                }
            });

            $('#revisoresTable').on('click', '.btn-delete', function() {
                if(confirm('Â¿EstÃ¡ seguro de eliminar este revisor?')) {
                    const id = $(this).data('id');
                    $.post('revisores?action=delete', { id: id }, function(response) {
                        $('#revisoresTable').DataTable().ajax.reload();
                    }).fail(function() {
                        alert('Error al eliminar el revisor.');
                    });
                }
            });
        });
    </script>
</body>
</html>

