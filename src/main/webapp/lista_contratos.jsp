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
            <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
    </head>

            <body class="bg-light d-flex flex-column min-vh-100">

                <jsp:include page="inc/navbar.jsp" />

                <div class="container mt-4 mb-5 flex-grow-1">
                    <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="fw-bold text-dark mb-0">Contratos registrados</h3>
            <c:if test="${sessionScope.usuario.tienePermiso('CONTRATOS_CREAR')}">
                <a href="contratos?action=new" class="btn text-white px-4 fw-bold" style="background-color: #198754; border-radius: 8px;">
                    <i class="bi bi-plus-circle-fill me-2"></i>Nuevo Contrato
                </a>
            </c:if>
        </div>

                    <div class="card shadow-sm border-0">
                        <div class="card-body">
                    <style>
                        .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
                        .table td { vertical-align: middle; }
                        .badge { font-weight: 600; padding: 6px 12px; border-radius: 6px; text-transform: uppercase; font-size: 0.75rem; }
                        .btn-view { background-color: #004884; border: none; color: #fff; }
                        .btn-edit { background-color: #ffc107; border: none; color: #000; }
                        .btn-action { padding: 6px 10px; font-size: 0.85rem; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                        
                        .footer-main {
                            background: #212529;
                            color: rgba(255, 255, 255, 0.7);
                            padding: 25px 0;
                            border-top: 1px solid rgba(255, 255, 255, 0.1);
                        }
                        .footer-main strong { color: #ffffff; }
                    </style>
                            <table id="contratosTable" class="table table-striped" style="width:100%">
                                <thead class="table-dark">
                                    <tr>
                                        <th># Contrato</th>
                                        <th>Contratista</th>
                                        <th>Objeto</th>
                                        <th class="text-end">Valor Total</th>
                                        <th class="text-center">Fecha Inicio</th>
                                        <th class="text-center">Fecha Fin</th>
                                        <th class="text-center">Estado</th>
                                        <th class="text-center" style="width: 100px;">Acciones</th>
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

                <!-- SweetAlert2 -->
                <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

                <script>
                    // Permisos de usuario cargados desde el servidor
                    var canEditContratos = ${sessionScope.usuario.tienePermiso('CONTRATOS_ACTUALIZAR')};

                    $(document).ready(function () {
                        // Check for session-based alerts (e.g. from Redirects)
                        var successMsg = "${sessionScope.successMessage}";
                        var errorMsg = "${sessionScope.errorMessage}";

                        // Also check request scope for forwards
                        if (!successMsg) successMsg = "${requestScope.successMessage}";
                        if (!errorMsg) errorMsg = "${requestScope.errorMessage}";
                        // Fallback for "error" attribute currently used in servlet
                        if (!errorMsg) errorMsg = "${error}";

                        if (successMsg && successMsg.trim() !== "") {
                            Swal.fire({ icon: 'success', title: '¡Éxito!', text: successMsg, showConfirmButton: false, timer: 2000 });
                        }

                        if (errorMsg && errorMsg.trim() !== "") {
                            Swal.fire({ icon: 'error', title: 'Error', text: errorMsg });
                        }

                        $('#contratosTable').DataTable({
                            "processing": true,
                            "serverSide": true,
                            "responsive": true,
                            "autoWidth": false,
                            "ajax": {
                                "url": "contratos",
                                "type": "POST",
                                "data": function(d) {
                                    d.action = "data";
                                }
                            },
                            "columns": [
                                { "data": 0 }, 
                                { "data": 1 }, 
                                { "data": 2 }, 
                                {
                                    "data": 3, 
                                    "render": $.fn.dataTable.render.number(',', '.', 2, '$ ')
                                },
                                { "data": 4 }, 
                                { "data": 5 }, 
                                {
                                    "data": 6, 
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
                                    "data": 7, 
                                    "orderable": false,
                                    "render": function (data, type, row) {
                                        var btns = '<div class="d-flex justify-content-center" style="white-space: nowrap;">' +
                                                   '<a href="contratos?action=view&id=' + data + '" class="btn btn-sm btn-outline-info me-1" title="Ver"><i class="bi bi-eye"></i></a>';
                                        
                                        if (canEditContratos) {
                                            btns += '<a href="contratos?action=edit&id=' + data + '" class="btn btn-sm btn-outline-primary" title="Editar"><i class="bi bi-pencil-square"></i></a>';
                                        }
                                        
                                        btns += '</div>';
                                        return btns;
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
                                    "first": "Primero", "last": "Último", "next": "Siguiente", "previous": "Anterior"
                                }
                            }
                        });
                    });
                </script>

                <!-- Remove session messages -->
                <c:if test="${not empty sessionScope.successMessage}">
                    <c:remove var="successMessage" scope="session" />
                </c:if>
                <c:if test="${not empty sessionScope.errorMessage}">
                    <c:remove var="errorMessage" scope="session" />
                </c:if>
            </body>

            </html>