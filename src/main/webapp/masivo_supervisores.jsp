<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Generación Masiva de Usuarios Supervisores - DAGJP</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <!-- DataTables CSS -->
    <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
    <link href="https://cdn.datatables.net/responsive/2.4.1/css/responsive.bootstrap5.min.css" rel="stylesheet">
    <link href="assets/css/styles.css" rel="stylesheet">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
    <style>
        .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
    </style>
</head>
<body class="bg-light d-flex flex-column min-vh-100">
    <jsp:include page="inc/navbar.jsp" />

    <div class="container mt-4 mb-5 flex-grow-1">

        <nav aria-label="breadcrumb">
            <ol class="breadcrumb breadcrumb-premium">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/supervisores">Supervisores</a></li>
                <li class="breadcrumb-item active" aria-current="page"><i class="bi bi-person-lines-fill me-1"></i>Generar Usuarios Supervisores</li>
            </ol>
        </nav>
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="fw-bold text-dark mb-0">Generar usuarios para Supervisores</h3>
            <a href="${pageContext.request.contextPath}/supervisores" class="btn btn-secondary fw-bold">
                <i class="bi bi-arrow-left"></i> Volver
            </a>
        </div>

        <c:if test="${not empty param.success}">
            <div class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i> ${param.success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty param.error}">
            <div class="alert alert-danger alert-dismissible fade show shadow-sm" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${param.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="card border-0 shadow-sm">
            <div class="card-body">
                <p class="text-muted">
                    Se muestran únicamente los supervisores que <strong>NO</strong> tienen una cuenta de usuario en el sistema. Seleccione de 1 a N supervisores y haga clic en <strong>"Crear Usuarios"</strong>. La contraseña inicial y usuario por defecto será su número de cédula, con el rol <strong>Supervisor</strong>.
                </p>

                <form action="${pageContext.request.contextPath}/masivo-supervisores" method="POST" id="formMasivo">
                    <div class="table-responsive">
                        <table class="table table-striped w-100 nowrap" id="tablaMasivo">
                            <thead class="table-dark">
                                <tr>
                                    <th style="width: 40px;" class="text-center">
                                        <input class="form-check-input" type="checkbox" id="checkAll">
                                    </th>
                                    <th>Cédula</th>
                                    <th>Nombres y Apellidos</th>
                                    <th>Cargo</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="s" items="${listaSinUsuario}">
                                    <tr>
                                        <td class="text-center">
                                            <input class="form-check-input check-item" type="checkbox" name="supervisor_ids" value="${s.id}">
                                        </td>
                                        <td>${s.cedula}</td>
                                        <td class="text-primary fw-bold">${s.nombre}</td>
                                        <td>${s.cargo}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                    
                    <div class="mt-3 d-flex justify-content-end">
                        <button type="button" id="btnSubmit" class="btn btn-primary fw-bold" style="background-color: #004884; border: none;">
                            <i class="bi bi-person-plus-fill me-1"></i> Crear Usuarios Seleccionados
                        </button>
                    </div>
                </form>
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
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

    <script>
        $(document).ready(function() {
            var table = $('#tablaMasivo').DataTable({
                responsive: true,
                language: {
                    url: "//cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json"
                },
                pageLength: 25,
                columnDefs: [
                    { orderable: false, targets: 0 }
                ]
            });

            // Seleccionar / Deseleccionar todos (incluso a través de páginas)
            $('#checkAll').on('change', function() {
                var isChecked = this.checked;
                // Marcar en todas las filas del datatable
                var rows = table.rows().nodes();
                $('input[type="checkbox"].check-item', rows).prop('checked', isChecked);
            });

            $('#btnSubmit').on('click', function(e) {
                var checkedCount = 0;
                // Contar seleccionados en todas las páginas de DataTable
                table.$('input[type="checkbox"].check-item:checked').each(function() {
                    checkedCount++;
                });

                if (checkedCount === 0) {
                    Swal.fire({
                        icon: 'warning',
                        title: 'Ningún supervisor seleccionado',
                        text: 'Por favor seleccione al menos un supervisor para crear su usuario.'
                    });
                    return;
                }

                Swal.fire({
                    title: '¿Crear ' + checkedCount + ' usuarios?',
                    text: 'Se crearán cuentas de usuario con rol Supervisor. Su usuario y contraseña inicial serán su número de cédula.',
                    icon: 'question',
                    showCancelButton: true,
                    confirmButtonColor: '#004884',
                    cancelButtonColor: '#6c757d',
                    confirmButtonText: 'Sí, crear usuarios',
                    cancelButtonText: 'Cancelar'
                }).then((result) => {
                    if (result.isConfirmed) {
                        // Asegurar que todos los checkboxes seleccionados de todas las páginas se envíen
                        var form = $('#formMasivo');
                        // Remover campos hidden previos para evitar duplicados
                        form.find('input[name="supervisor_ids"][type="hidden"]').remove();
                        
                        table.$('input[type="checkbox"].check-item:checked').each(function() {
                            // Si el checkbox no está en el DOM actual de la página activa, creamos un hidden input
                            if (!$.contains(document, this)) {
                                form.append(
                                    $('<input>')
                                        .attr('type', 'hidden')
                                        .attr('name', 'supervisor_ids')
                                        .val($(this).val())
                                );
                            }
                        });

                        form.submit();
                    }
                });
            });
        });
    </script>
</body>
</html>
