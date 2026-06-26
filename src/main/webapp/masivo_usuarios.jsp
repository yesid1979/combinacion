<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Generación Masiva de Usuarios - DAGJP</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <!-- DataTables CSS -->
    <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
    <link href="assets/css/styles.css" rel="stylesheet">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
    <style>
        .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
    </style>
</head>
<body class="bg-light d-flex flex-column min-vh-100">
    <jsp:include page="inc/navbar.jsp" />

    <div class="container mt-4 mb-5 flex-grow-1">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="fw-bold text-dark mb-0">Generar Usuarios para Contratistas</h3>
            <a href="${pageContext.request.contextPath}/contratistas" class="btn btn-secondary fw-bold">
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
                    Se muestran únicamente los contratistas que <strong>NO</strong> tienen una cuenta de usuario asignada en el sistema. Selecciona de 1 a N contratistas y haz clic en "Crear Usuarios". La contraseña por defecto será su número de cédula.
                </p>

                <form action="${pageContext.request.contextPath}/masivo-usuarios" method="POST" id="formMasivo">
                    <div class="table-responsive">
                        <table class="table table-striped w-100" id="tablaMasivo">
                            <thead class="table-dark">
                                <tr>
                                    <th style="width: 40px;" class="text-center">
                                        <input class="form-check-input" type="checkbox" id="checkAll">
                                    </th>
                                    <th>Cédula</th>
                                    <th>Nombres y Apellidos</th>
                                    <th>Correo</th>
                                    <th>Teléfono</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="c" items="${listaSinUsuario}">
                                    <tr>
                                        <td class="text-center">
                                            <input class="form-check-input check-item" type="checkbox" name="contratista_ids" value="${c.id}">
                                        </td>
                                        <td>${c.cedula}</td>
                                        <td class="text-primary fw-bold">${c.nombre}</td>
                                        <td>${c.correo}</td>
                                        <td>${c.telefono}</td>
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
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

    <script>
        $(document).ready(function() {
            var table = $('#tablaMasivo').DataTable({
                "language": {
                    "url": "https://cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json"
                },
                "columnDefs": [
                    { "orderable": false, "targets": 0 }
                ]
            });

            // Toggle ALL checkboxes on current page
            $('#checkAll').on('click', function(){
                var rows = table.rows({ 'search': 'applied' }).nodes();
                $('input[type="checkbox"]', rows).prop('checked', this.checked);
            });

            // If a checkbox is unchecked, uncheck the "checkAll" checkbox
            $('#tablaMasivo tbody').on('change', 'input[type="checkbox"]', function(){
                if(!this.checked){
                    var el = $('#checkAll').get(0);
                    if(el && el.checked && ('indeterminate' in el)){
                        el.indeterminate = true;
                    }
                }
            });

            $('#btnSubmit').on('click', function(e) {
                // To allow submitting all pages selected rows in datatables
                var form = $('#formMasivo');
                var checkboxes = table.$('input[type="checkbox"]:checked');
                
                if (checkboxes.length === 0) {
                    Swal.fire('Atención', 'Debes seleccionar al menos un contratista.', 'warning');
                    return;
                }

                Swal.fire({
                    title: '¿Confirmar Creación?',
                    text: "Se crearán " + checkboxes.length + " usuarios con la cédula como contraseña.",
                    icon: 'question',
                    showCancelButton: true,
                    confirmButtonColor: '#004884',
                    cancelButtonColor: '#6c757d',
                    confirmButtonText: 'Sí, crear usuarios',
                    cancelButtonText: 'Cancelar'
                }).then((result) => {
                    if (result.isConfirmed) {
                        // Append hidden inputs for all checked boxes (including those not visible)
                        checkboxes.each(function(){
                            if(!$.contains(document, this)){
                                form.append($('<input>').attr('type', 'hidden').attr('name', this.name).val(this.value));
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
