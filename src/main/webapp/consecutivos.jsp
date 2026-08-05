<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Gestión de Consecutivos - DAGJP</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link href="assets/css/styles.css" rel="stylesheet">
</head>

<body class="bg-light d-flex flex-column min-vh-100">

    <jsp:include page="inc/navbar.jsp" />

    <div class="container container-main mt-4 mb-5">
        
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb breadcrumb-premium">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
                <li class="breadcrumb-item active text-muted">Datos Maestros</li>
                <li class="breadcrumb-item active" aria-current="page"><i class="bi bi-123 me-1"></i>Consecutivos</li>
            </ol>
        </nav>
        
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="fw-bold text-dark mb-0">Gestión Masiva de Consecutivos</h3>
            <div>
                <a href="${pageContext.request.contextPath}/consecutivos?action=template" class="btn btn-outline-success fw-bold px-3 me-2">
                    <i class="bi bi-file-earmark-excel-fill me-1"></i>Descargar Plantilla
                </a>
            </div>
        </div>

        <div class="card border-0 shadow-sm mb-4">
            <div class="card-body bg-white rounded">
                <form action="consecutivos" method="post" enctype="multipart/form-data" class="d-flex align-items-center gap-3">
                    <input type="hidden" name="action" value="upload">
                    <div class="flex-grow-1">
                        <label for="fileExcel" class="form-label fw-bold">Seleccionar archivo Excel</label>
                        <input class="form-control" type="file" id="fileExcel" name="fileExcel" accept=".xlsx,.xls" required>
                        <div class="form-text">Asegúrese de usar la plantilla descargada con las columnas Cédula, Contrato, Cuota y Consecutivo.</div>
                    </div>
                    <div class="mt-3">
                        <button type="submit" class="btn text-white fw-bold px-4" style="background-color: #004884;">
                            <i class="bi bi-cloud-arrow-up-fill me-2"></i>Subir Archivo
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <div class="card border-0 shadow-sm">
            <div class="card-header bg-white border-bottom-0 pt-4 pb-0 d-flex justify-content-between align-items-center">
                <h5 class="fw-bold mb-0">Últimos consecutivos cargados</h5>
                <button onclick="confirmarLimpiar()" class="btn btn-outline-danger btn-sm">
                    <i class="bi bi-trash-fill me-1"></i>Limpiar todos
                </button>
            </div>
            <div class="card-body">
                <table class="table table-striped table-hover w-100">
                    <thead class="table-dark">
                        <tr>
                            <th>Cédula</th>
                            <th>Contrato</th>
                            <th>No. Cuota</th>
                            <th>Consecutivo Asignado</th>
                            <th>Fecha de Carga</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty listaConsecutivos}">
                                <c:forEach items="${listaConsecutivos}" var="c">
                                    <tr>
                                        <td><strong>${c.cedula}</strong></td>
                                        <td>${c.contrato}</td>
                                        <td>${c.numeroCuota}</td>
                                        <td><span class="badge bg-success" style="font-size: 14px;">${c.consecutivo}</span></td>
                                        <td>${c.fechaCarga}</td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" class="text-center py-4 text-muted">No hay consecutivos cargados actualmente en el sistema.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <jsp:include page="inc/footer.jsp" />

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <script>
        $(document).ready(function () {
            const urlParams = new URLSearchParams(window.location.search);
            const status = urlParams.get('status');
            const error = urlParams.get('error');
            const count = urlParams.get('count');

            if (status === 'uploaded') {
                Swal.fire('¡Éxito!', count + ' consecutivos han sido cargados o actualizados correctamente.', 'success');
            } else if (status === 'cleaned') {
                Swal.fire('¡Limpieza completa!', 'Todos los consecutivos han sido eliminados de la base de datos.', 'success');
            }
            
            if (error === 'no_file') Swal.fire('Error', 'Debe seleccionar un archivo.', 'error');
            else if (error === 'empty_or_invalid') Swal.fire('Archivo Vacío', 'El archivo no tiene datos o el formato no es válido.', 'warning');
            else if (error === 'db_error') Swal.fire('Error', 'Hubo un error al guardar en la base de datos.', 'error');
            else if (error === 'parse_error') Swal.fire('Error', 'El formato del archivo Excel es incorrecto o está dañado.', 'error');

            if (status || error) window.history.replaceState({}, document.title, window.location.pathname);
        });

        function confirmarLimpiar() {
            Swal.fire({
                title: '¿Estás seguro?',
                text: "Esto eliminará TODOS los consecutivos de la base de datos de forma permanente.",
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#dc3545',
                cancelButtonColor: '#6c757d',
                confirmButtonText: 'Sí, limpiar todo',
                cancelButtonText: 'Cancelar'
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '${pageContext.request.contextPath}/consecutivos?action=clean';
                }
            });
        }
    </script>
</body>
</html>
