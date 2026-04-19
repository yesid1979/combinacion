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
            <!-- DataTables CSS for advanced table features -->
            <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
            <!-- DataTables Responsive CSS -->
            <link href="https://cdn.datatables.net/responsive/2.4.1/css/responsive.bootstrap5.min.css" rel="stylesheet">
            <!-- Custom Styles (The secret of the "Combinación" look) -->
            <link href="assets/css/styles.css" rel="stylesheet">
            <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
            <style>
                table { width: 100% !important; }
                .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
                .table td { vertical-align: middle; }
            </style>
    </head>

        <body class="bg-light d-flex flex-column min-vh-100">
            <jsp:include page="inc/navbar.jsp" />

            <div class="container mt-4 mb-5">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h3 class="fw-bold text-dark mb-0">Presupuestos registrados</h3>
                    <c:if test="${sessionScope.usuario.tienePermiso('PRESUPUESTO_CREAR')}">
                        <a href="${pageContext.request.contextPath}/presupuesto?action=new" class="btn text-white fw-bold" style="background-color: #198754;">
                            <i class="bi bi-plus-circle-fill me-1"></i>Nuevo presupuesto
                        </a>
                    </c:if>
                </div>

                <div class="card shadow-sm border-0">
                    <div class="card-body">
                        <table id="presupuestoTable" class="table table-striped dt-responsive" style="width:100%">
                            <thead class="table-dark">
                                <tr>
                                    <th>No. CDP</th>
                                    <th>Fecha CDP</th>
                                    <th>Vencimiento</th>
                                    <th>No. RPC</th>
                                    <th>Fecha RPC</th>
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
                        "serverSide": false,
                        "responsive": true,
                        "ajax": {
                            "url": "${pageContext.request.contextPath}/presupuesto",
                            "type": "POST",
                            "data": function(d) {
                                d.action = "data";
                            }
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
                                    let btnView = '<a href="presupuesto?action=view&id=' + data + '" class="btn btn-sm btn-outline-info" title="Ver Detalle Completo"><i class="bi bi-eye"></i></a> ';
                                    let btnEdit = '';
                                    let btnDel = '';

                                    <c:if test="${sessionScope.usuario.tienePermiso('PRESUPUESTO_EDITAR')}">
                                        btnEdit = '<a href="presupuesto?action=edit&id=' + data + '" class="btn btn-sm btn-outline-primary" title="Editar"><i class="bi bi-pencil-square"></i></a> ';
                                    </c:if>

                                    <c:if test="${sessionScope.usuario.tienePermiso('PRESUPUESTO_ELIMINAR')}">
                                        btnDel = '<button onclick="confirmarEliminar(' + data + ')" class="btn btn-sm btn-outline-danger" title="Eliminar"><i class="bi bi-trash"></i></button>';
                                    </c:if>

                                    return '<div class="d-flex justify-content-center gap-1">' + btnView + btnEdit + btnDel + '</div>';
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
                
                function confirmarEliminar(id) {
                    Swal.fire({
                        title: '¿Estás seguro?',
                        text: "Esta acción no se puede deshacer.",
                        icon: 'warning',
                        showCancelButton: true,
                        confirmButtonColor: '#d33',
                        cancelButtonColor: '#3085d6',
                        confirmButtonText: 'Sí, eliminar',
                        cancelButtonText: 'Cancelar'
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.href = 'presupuesto?action=delete&id=' + id;
                        }
                    });
                }
            </script>
        </body>

        </html>