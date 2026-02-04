<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Combinación de Correspondencia - Gestión de Prestadores</title>
            <!-- Bootstrap 5 CSS -->
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <!-- Bootstrap Icons CSS -->
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
            <!-- DataTables CSS for advanced table features -->
            <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
            <!-- DataTables Responsive CSS -->
            <link href="https://cdn.datatables.net/responsive/2.4.1/css/responsive.bootstrap5.min.css" rel="stylesheet">
            <!-- Custom Styles -->
            <link href="assets/css/styles.css" rel="stylesheet">
        </head>

        <body class="bg-light d-flex flex-column min-vh-100">
            <jsp:include page="inc/navbar.jsp" />

            <div class="container mt-4">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2>Generación de documentos</h2>
                    <button class="btn btn-success" onclick="descargarMasivo()">
                        <i class="bi bi-file-zip me-2"></i>Descargar Seleccionados (ZIP)
                    </button>
                </div>

                <div class="alert alert-info alert-dismissible fade show" role="alert">
                    <i class="bi bi-info-circle me-2"></i>Seleccione los contratistas para generar sus documentos.
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>

                <div class="card shadow-sm border-0">
                    <div class="card-body">
                        <table id="contratistasTable" class="table table-striped" style="width:100%">
                            <thead class="table-dark">
                                <tr>
                                    <th style="width: 40px;"><input type="checkbox" id="selectAll"
                                            class="form-check-input"></th>
                                    <th>No. Contrato</th>
                                    <th>No. de cédula</th>
                                    <th>Nombres y Apellidos</th>
                                    <th>Correo</th>
                                    <th>No. de teléfono</th>
                                    <th class="text-end">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <!-- Data loaded by Server-Side Processing -->
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
            <!-- SweetAlert2 -->
            <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

            <script>
                $(document).ready(function () {
                    $('#contratistasTable').DataTable({
                        "processing": true,
                        "serverSide": true,
                        "responsive": true,
                        "autoWidth": false,
                        "order": [[1, 'asc']], // Order by Contract No (Col 1) asc by default
                        "ajax": {
                            "url": "${pageContext.request.contextPath}/contratistas?action=data&source=combinacion&t=" + new Date().getTime(),
                            "type": "POST",
                            "error": function (xhr, error, thrown) {
                                console.error("Error AJAX Status:", xhr.status);
                                Swal.fire('Error de Conexión', 'El servidor respondió con código ' + xhr.status, 'error');
                            }
                        },
                        "columns": [
                            {
                                "data": 4, // ID (checkbox)
                                "orderable": false,
                                "className": "text-center",
                                "render": function (data, type, row) {
                                    return '<input type="checkbox" class="form-check-input row-select" value="' + data + '">';
                                }
                            },
                            { "data": 5 }, // Contrato (Visible Col 1, Source Index 5)
                            { "data": 0 }, // Cedula (Visible Col 2, Source Index 0)
                            { "data": 1 }, // Nombre (Visible Col 3, Source Index 1)
                            { "data": 2 }, // Correo (Visible Col 4, Source Index 2)
                            { "data": 3 }, // Telefono (Visible Col 5, Source Index 3)
                            {
                                "data": 4, // ID (Actions)
                                "orderable": false,
                                "className": "text-end",
                                "render": function (data, type, row) {
                                    return `
                                    <div class="d-flex justify-content-end gap-2">
                                        <a href="combinacion?action=generate&id=` + data + `" class="btn btn-sm btn-primary" title="Descargar ZIP">
                                            <i class="bi bi-download"></i>
                                        </a>
                                    </div>
                                    `;
                                }
                            }
                        ],
                        language: {
                            "decimal": "",
                            "emptyTable": "No hay datos disponibles en la tabla",
                            "info": "Mostrando _START_ a _END_ de _TOTAL_ entradas",
                            "infoEmpty": "Mostrando 0 a 0 de 0 entradas",
                            "infoFiltered": "(filtrado de _MAX_ entradas totales)",
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
                            }
                        }
                    });

                    // Handle "Select All"
                    $('#selectAll').on('click', function () {
                        var rows = $('#contratistasTable').DataTable().rows({ 'search': 'applied' }).nodes();
                        $('input[type="checkbox"]', rows).prop('checked', this.checked);
                    });
                });

                function descargarMasivo() {
                    let selected = [];
                    $('.row-select:checked').each(function () {
                        selected.push($(this).val());
                    });

                    if (selected.length === 0) {
                        Swal.fire('Atención', 'Por favor seleccione al menos un contratista.', 'warning');
                        return;
                    }

                    // Trigger extraction
                    window.location.href = 'combinacion?action=downloadZip&ids=' + selected.join(',');
                }
            </script>
        </body>

        </html>