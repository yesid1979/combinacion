<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Lista de Contratistas - Gestión de Prestadores</title>
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
                    <h2>Contratistas registrados</h2>
                    <a href="contratistas?action=new" class="btn btn-success"><i class="bi bi-plus-circle me-1"></i>
                        Nuevo contratista</a>
                </div>

                <div class="card shadow-sm border-0">
                    <div class="card-body">
                        <!-- Removed table-responsive -->
                        <table id="contratistasTable" class="table table-striped" style="width:100%">
                            <thead class="table-dark">
                                <tr>
                                    <th>Cédula</th>
                                    <th>Nombre</th>
                                    <th>Correo</th>
                                    <th>Teléfono</th>
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
                        "ajax": {
                            "url": "contratistas?action=data",
                            "type": "POST"
                        },
                        "columns": [
                            { "data": 0 }, // Cedula
                            { "data": 1 }, // Nombre
                            { "data": 2 }, // Correo
                            { "data": 3 }, // Telefono
                            {
                                "data": 4, // ID (Actions)
                                "orderable": false,
                                "className": "text-end",
                                "render": function (data, type, row) {
                                    return `
                                    <div class="d-flex justify-content-end gap-2">
                                        <a href="contratistas?action=edit&id=` + data + `" class="btn btn-sm btn-outline-primary" title="Editar">
                                            <i class="bi bi-pencil-square"></i>
                                        </a>
                                        <button onclick="confirmarEliminar(` + data + `)" class="btn btn-sm btn-outline-danger" title="Eliminar">
                                            <i class="bi bi-trash"></i>
                                        </button>
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

                // Check for URL parameters for SweetAlert
                const urlParams = new URLSearchParams(window.location.search);
                const status = urlParams.get('status');

                if (status === 'created') {
                    Swal.fire('¡Creado!', 'El registro ha sido creado exitosamente.', 'success');
                } else if (status === 'updated') {
                    Swal.fire('¡Actualizado!', 'El registro ha sido actualizado correctamente.', 'success');
                } else if (status === 'deleted') {
                    Swal.fire('¡Eliminado!', 'El registro ha sido eliminado.', 'success');
                } else if (status === 'error') {
                    Swal.fire('Error', 'Ha ocurrido un error al procesar la solicitud.', 'error');
                }

                // Clean URL
                if (status) {
                    window.history.replaceState({}, document.title, window.location.pathname);
                }

                function confirmarEliminar(id) {
                    Swal.fire({
                        title: '¿Estás seguro?',
                        text: "Esta acción no se puede deshacer",
                        icon: 'warning',
                        showCancelButton: true,
                        confirmButtonColor: '#d33',
                        cancelButtonColor: '#6c757d',
                        confirmButtonText: 'Sí, eliminar',
                        cancelButtonText: 'Cancelar'
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.href = 'contratistas?action=delete&id=' + id;
                        }
                    });
                }
            </script>
        </body>

        </html>