<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Lista de Presupuestos - Gestión Integral</title>
            <!-- Bootstrap 5 CSS -->
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <!-- Bootstrap Icons CSS -->
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
            <!-- DataTables CSS -->
            <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
            <link href="https://cdn.datatables.net/responsive/2.4.1/css/responsive.bootstrap5.min.css" rel="stylesheet">
            <!-- Custom Styles -->
            <link href="assets/css/styles.css" rel="stylesheet">
        </head>

        <body class="bg-light d-flex flex-column min-vh-100">
            <jsp:include page="inc/navbar.jsp" />

            <div class="container mt-4">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2><i class="bi bi-cash-coin me-2 text-info"></i>Detalles Presupuestales</h2>
                    <!-- Optional: Add New button if manual entry is required later -->
                </div>

                <div class="card shadow-sm border-0">
                    <div class="card-body">
                        <table id="presupuestoTable" class="table table-striped dt-responsive" style="width:100%">
                            <thead class="table-dark">
                                <tr>
                                    <th>No. CDP</th>
                                    <th>Fecha CDP</th>
                                    <th>Vencimiento</th>
                                    <th>No. RP</th>
                                    <th>Fecha RP</th>
                                    <th>Rubro / Apropiación</th>
                                    <th>Ficha EBI / Proyecto</th>
                                    <th>Valor CDP</th>
                                    <th class="text-end">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <!-- Data loaded by AJAX -->
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- Footer -->
            <jsp:include page="inc/footer.jsp" />

            <!-- Scripts -->
            <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
            <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
            <script src="https://cdn.datatables.net/1.13.4/js/dataTables.bootstrap5.min.js"></script>
            <script src="https://cdn.datatables.net/responsive/2.4.1/js/dataTables.responsive.min.js"></script>
            <script src="https://cdn.datatables.net/responsive/2.4.1/js/responsive.bootstrap5.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

            <script>
                $(document).ready(function () {
                    $('#presupuestoTable').DataTable({
                        "processing": true,
                        // "serverSide": true, // We are doing client-side processing for simplicity in the servlet
                        "responsive": true,
                        "ajax": {
                            "url": "presupuesto?action=data",
                            "type": "GET"
                        },
                        "columns": [
                            { "data": 0 }, // No. CDP
                            { "data": 1 }, // Fecha CDP
                            { "data": 2 }, // Vencimiento
                            { "data": 3 }, // RP Num
                            { "data": 4 }, // RP Date
                            {
                                "data": 5, // Rubro
                                "render": function (data, type, row) {
                                    if (type === 'display' && data && data.length > 20) {
                                        var safeData = data.replace(/"/g, '&quot;').replace(/'/g, "&#39;");
                                        return data.substr(0, 20) +
                                            ' <a href="javascript:void(0)" class="text-primary" title="Más información" onclick="verTexto(\'Rubro / Apropiación\', \'' + safeData + '\')"><b>...</b></a>';
                                    }
                                    return data;
                                }
                            },
                            {
                                "data": 6, // Ficha EBI
                                "render": function (data, type, row) {
                                    if (type === 'display' && data && data.length > 30) {
                                        var safeData = data.replace(/"/g, '&quot;').replace(/'/g, "&#39;");
                                        return data.substr(0, 30) +
                                            ' <a href="javascript:void(0)" class="text-primary" title="Más información" onclick="verTexto(\'Ficha EBI\', \'' + safeData + '\')"><b>...</b></a>';
                                    }
                                    return data;
                                }
                            },
                            { "data": 7 }, // Valor
                            {
                                "data": 8, // ID
                                "orderable": false,
                                "className": "text-end",
                                "render": function (data, type, row) {
                                    return `<a href="presupuesto?action=view&id=` + data + `" class="btn btn-sm btn-outline-secondary" title="Ver Detalle Completo">
                                        <i class="bi bi-eye"></i>
                                    </a>`;
                                }
                            }
                        ],
                        language: {
                            "url": "//cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json"
                        }
                    });
                });

                function verTexto(titulo, contenido) {
                    Swal.fire({
                        title: '<strong>' + titulo + '</strong>',
                        icon: 'info',
                        html: '<div style="text-align: left; max-height: 300px; overflow-y: auto;">' + contenido + '</div>',
                        showCloseButton: true,
                        focusConfirm: false,
                        confirmButtonText: 'Cerrar'
                    });
                }
            </script>
        </body>

        </html>