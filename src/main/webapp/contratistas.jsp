<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <title>Contratistas - Gestión Contratos</title>
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap"
                rel="stylesheet">
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
            <!-- DataTables CSS -->
            <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
            <link href="assets/css/styles.css" rel="stylesheet">
        </head>

        <body class="d-flex flex-column min-vh-100">

            <jsp:include page="inc/navbar.jsp" />

            <div class="container my-5">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h2 class="fw-bold mb-1">Contratistas</h2>
                        <p class="text-muted">Directorio de prestadores de servicios.</p>
                    </div>
                    <a href="form_contratista.jsp" class="btn btn-primary d-flex align-items-center gap-2 shadow-sm">
                        <i class="bi bi-plus-circle"></i> Nuevo Contratista
                    </a>
                </div>

                <div class="card card-modern border-0">
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table id="contratistasTableModern" class="table table-hover align-middle mb-0">
                                <thead class="bg-light text-muted small text-uppercase">
                                    <tr>
                                        <th class="ps-4 py-3">Cédula</th>
                                        <th>Nombre</th>
                                        <th>Correo</th>
                                        <th>Teléfono</th>
                                        <th>Profesión</th>
                                        <th class="pe-4 text-end">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${listaContratistas}" var="c">
                                        <tr>
                                            <td class="ps-4 font-monospace">${c.cedula}</td>
                                            <td class="fw-medium">${c.nombre}</td>
                                            <td>${c.correo}</td>
                                            <td>${c.telefono}</td>
                                            <td>${c.formacionTitulo}</td>
                                            <td class="pe-4 text-end">
                                                <a href="#" class="btn btn-sm btn-warning text-dark" title="Editar"><i
                                                        class="bi bi-pencil-square"></i></a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <jsp:include page="inc/footer.jsp" />
            <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
            <!-- DataTables JS -->
            <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
            <script src="https://cdn.datatables.net/1.13.4/js/dataTables.bootstrap5.min.js"></script>
            <script>
                $(document).ready(function () {
                    $('#contratistasTableModern').DataTable({
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
                        },
                        responsive: true
                    });
                });
            </script>
        </body>

        </html>