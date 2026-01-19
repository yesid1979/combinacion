<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="es">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Lista de Contratos - Gestión de Prestadores</title>
                <!-- Bootstrap 5 CSS -->
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                <!-- Bootstrap Icons CSS -->
                <link rel="stylesheet"
                    href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
                <!-- DataTables CSS for advanced table features -->
                <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
                <!-- DataTables Responsive CSS -->
                <link href="https://cdn.datatables.net/responsive/2.4.1/css/responsive.bootstrap5.min.css"
                    rel="stylesheet">
                <!-- Custom Styles -->
                <link href="assets/css/styles.css" rel="stylesheet">
            </head>

            <body class="bg-light d-flex flex-column min-vh-100">

                <jsp:include page="inc/navbar.jsp" />

                <div class="container mt-4">
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h2>Contratos registrados</h2>
                        <a href="contratos?action=new" class="btn btn-success"><i class="bi bi-plus-circle me-1"></i>
                            Nuevo Contrato</a>
                    </div>

                    <div class="card shadow-sm border-0">
                        <div class="card-body">
                            <!-- Removed table-responsive class to prevent double scrollbar -->
                            <table id="contratosTable" class="table table-striped" style="width:100%">
                                <thead class="table-dark">
                                    <tr>
                                        <th># Contrato</th>
                                        <th>Contratista</th>
                                        <th>Objeto</th>
                                        <th>Valor Total</th>
                                        <th>Fecha Inicio</th>
                                        <th>Fecha Fin</th>
                                        <th>Estado</th>
                                        <th>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <!-- Data loaded by Server-Side Processing via AJAX -->
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Footer -->
                <jsp:include page="inc/footer.jsp" />

                <!-- jQuery and Bootstrap JS -->
                <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
                <!-- DataTables JS -->
                <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
                <script src="https://cdn.datatables.net/1.13.4/js/dataTables.bootstrap5.min.js"></script>
                <!-- DataTables Responsive JS -->
                <script src="https://cdn.datatables.net/responsive/2.4.1/js/dataTables.responsive.min.js"></script>
                <script src="https://cdn.datatables.net/responsive/2.4.1/js/responsive.bootstrap5.min.js"></script>

                <script>
                    $(document).ready(function () {
                        $('#contratosTable').DataTable({
                            "processing": true,
                            "serverSide": true,
                            "responsive": true,
                            "autoWidth": false,
                            "ajax": {
                                "url": "contratos?action=data",
                                "type": "POST"
                            },
                            "columns": [
                                { "data": 0 }, // Numero
                                { "data": 1 }, // Contratista (Nombre)
                                { "data": 2 }, // Objeto
                                { "data": 3 }, // Fecha Inicio
                                { "data": 4 }, // Fecha Fin
                                {
                                    "data": 5, // Valor
                                    "render": $.fn.dataTable.render.number(',', '.', 2, '$ ')
                                },
                                {
                                    "data": 6, // Estado
                                    "render": function (data, type, row) {
                                        var badgeClass = 'bg-secondary';
                                        if (!data) return '';
                                        if (data.toLowerCase() === 'activo') badgeClass = 'bg-success';
                                        else if (data.toLowerCase() === 'liquidado') badgeClass = 'bg-info';
                                        else if (data.toLowerCase() === 'suspendido') badgeClass = 'bg-warning text-dark';
                                        else if (data.toLowerCase() === 'terminado') badgeClass = 'bg-danger';
                                        return '<span class="badge ' + badgeClass + '">' + data + '</span>';
                                    }
                                },
                                {
                                    "data": 7, // Actions (ID)
                                    "orderable": false,
                                    "render": function (data, type, row) {
                                        return '<div class="btn-group" role="group">' +
                                            '<a href="#" class="btn btn-sm btn-info text-white" title="Ver Detalle"><i class="bi bi-eye"></i></a>' +
                                            '<a href="#" class="btn btn-sm btn-warning text-dark" title="Editar"><i class="bi bi-pencil-square"></i></a>' +
                                            '</div>';
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
                                    "last": "Último",
                                    "next": "Siguiente",
                                    "previous": "Anterior"
                                },
                                "aria": {
                                    "sortAscending": ": activar para ordenar columna ascendente",
                                    "sortDescending": ": activar para ordenar columna descendente"
                                }
                            }
                        });
                    });
                </script>
            </body>

            </html>