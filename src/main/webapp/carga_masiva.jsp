<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html lang="es">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Carga Masiva - Ordenadores y Contratistas</title>
        <!-- Bootstrap 5 CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
        <link href="assets/css/styles.css" rel="stylesheet">
    </head>

    <body class="bg-light d-flex flex-column min-vh-100">

        <jsp:include page="inc/navbar.jsp" />

        <div class="container mt-4 mb-5">
            <div class="card shadow-sm border-0">
                <div class="card-body p-4">
                    <h2 class="mb-4 fw-bold">
                        <i class="bi bi-upload text-primary me-2"></i>
                        Carga Masiva - Gestión Integral
                    </h2>

                    <div class="alert alert-info border-0 shadow-sm">
                        <h5 class="alert-heading"><i class="bi bi-info-circle-fill me-2"></i>Instrucciones del Archivo
                        </h5>
                        <p>Suba un archivo <strong>Excel (.xlsx, .xls)</strong> o <strong>CSV</strong>. El sistema
                            identificará automáticamente las columnas por nombre.</p>

                        <div class="row g-3 mt-2">
                            <!-- Ordenadores -->
                            <div class="col-md-4 border-end">
                                <h6 class="fw-bold text-primary">
                                    <i class="bi bi-bank me-1"></i>Ordenadores:
                                </h6>
                                <small class="d-block">• Nombre del Ordenador</small>
                                <small class="d-block">• Cédula, Cargo</small>
                                <small class="d-block">• Organismo, Dirección</small>
                                <small class="d-block">• Decreto, Acta</small>
                            </div>

                            <!-- Contratistas -->
                            <div class="col-md-4 border-end">
                                <h6 class="fw-bold text-success">
                                    <i class="bi bi-people me-1"></i>Contratistas:
                                </h6>
                                <small class="d-block">• Cédula, Nombre, Correo</small>
                                <small class="d-block">• Formación (Descripción y Título)</small>
                                <small class="d-block">• Tarjeta Prof., Experiencia</small>
                                <small class="d-block">• Fecha Nacim., Edad</small>
                            </div>

                            <!-- Supervisores -->
                            <div class="col-md-4">
                                <h6 class="fw-bold text-info">
                                    <i class="bi bi-person-badge-fill me-1"></i>Supervisores:
                                </h6>
                                <small class="d-block">• Nombre del Supervisor</small>
                                <small class="d-block">• Cédula del Supervisor</small>
                                <small class="d-block">• Cargo del Supervisor</small>
                            </div>
                        </div>
                        <hr>
                        <small class="text-muted">
                            <i class="bi bi-lightbulb me-1"></i>
                            <strong>Nota:</strong> Puede cargar los tres tipos de registros en el mismo archivo si
                            contiene todas las columnas.
                        </small>
                    </div>

                    <form action="upload" method="POST" enctype="multipart/form-data" class="mt-4">
                        <div class="mb-3">
                            <label for="fileUpload" class="form-label fw-bold">Seleccionar Archivo (Excel o CSV)</label>
                            <input class="form-control form-control-lg" type="file" id="fileUpload" name="file"
                                accept=".csv, .xlsx, .xls" required>
                        </div>

                        <div class="text-end mt-4">
                            <a href="index.jsp" class="btn btn-secondary me-2" style="width: 140px;"><i
                                    class="bi bi-x-circle me-2"></i> Cerrar</a>
                            <button type="submit" class="btn btn-primary" style="width: 140px;"><i
                                    class="bi bi-cloud-upload me-2"></i> Cargar</button>
                        </div>
                    </form>

                    <% if (request.getAttribute("debug") !=null) { %>
                        <div class="alert alert-secondary mt-4">
                            <h6 class="alert-heading"><i class="bi bi-terminal me-2"></i>Log de Procesamiento</h6>
                            <pre class="mb-0"
                                style="white-space: pre-wrap; font-size: 0.85rem; font-family: 'Courier New', monospace;"><%= request.getAttribute("debug") %></pre>
                        </div>
                        <% } %>
                </div>
            </div>
        </div>

        <!-- SweetAlert2 -->
        <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
        <script>
        <% if (request.getAttribute("message") != null) { %>
                Swal.fire({
                    icon: 'success',
                    title: '¡Carga Exitosa!',
                    html: '<%= request.getAttribute("message") %>',
                    confirmButtonText: 'Entendido',
                    confirmButtonColor: '#0d6efd'
                });
        <% } %>

        <% if (request.getAttribute("error") != null) { %>
                Swal.fire({
                    icon: 'error',
                    title: 'Error en la Carga',
                    text: '<%= request.getAttribute("error") %>',
                    confirmButtonColor: '#dc3545'
                });
        <% } %>

        </script>

        <!-- Footer -->
        <jsp:include page="inc/footer.jsp" />

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    </body>

    </html>