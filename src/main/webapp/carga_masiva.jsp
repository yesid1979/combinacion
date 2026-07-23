<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html lang="es">

    <head>
    <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon">
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

        <div class="container mt-4 mb-5 flex-grow-1">

            <nav aria-label="breadcrumb">
                <ol class="breadcrumb breadcrumb-premium">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
                    <li class="breadcrumb-item active text-muted">Combinación</li>
                    <li class="breadcrumb-item active" aria-current="page"><i class="bi bi-upload me-1"></i>Carga Masiva Contratos</li>
                </ol>
            </nav>
            <div class="card shadow-sm border-0">
                <div class="card-body p-4">
                    <h2 class="mb-4 fw-bold">
                        <i class="bi bi-upload text-primary me-2"></i>
                        Carga masiva - gestión integral
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
                                <small class="d-block">&bull; Nombre del Ordenador</small>
                                <small class="d-block">&bull; Cédula, Cargo</small>
                                <small class="d-block">&bull; Organismo, Dirección</small>
                                <small class="d-block">&bull; Decreto, Acta</small>
                            </div>

                            <!-- Contratistas -->
                            <div class="col-md-4 border-end">
                                <h6 class="fw-bold text-success">
                                    <i class="bi bi-people me-1"></i>Contratistas:
                                </h6>
                                <small class="d-block">&bull; Cédula, Nombre, Correo</small>
                                <small class="d-block">&bull; Formación, Tarjeta Prof.</small>
                                <small class="d-block">&bull; Experiencia, Nacimiento</small>
                            </div>

                            <!-- Supervisores -->
                            <div class="col-md-4">
                                <h6 class="fw-bold text-info">
                                    <i class="bi bi-person-badge-fill me-1"></i>Supervisores:
                                </h6>
                                <small class="d-block">&bull; Nombre del Supervisor</small>
                                <small class="d-block">&bull; Cédula del Supervisor</small>
                                <small class="d-block">&bull; Cargo del Supervisor</small>
                            </div>

                            <!-- Estructuradores -->
                            <div class="col-md-4 border-end mt-3">
                                <h6 class="fw-bold text-warning">
                                    <i class="bi bi-briefcase-fill me-1"></i>Estructuradores:
                                </h6>
                                <small class="d-block">&bull; Profesional Jurídico y Cargo</small>
                                <small class="d-block">&bull; Profesional Técnico y Cargo</small>
                                <small class="d-block">&bull; Profesional Financiero y Cargo</small>
                            </div>

                            <!-- Presupuesto -->
                            <div class="col-md-4 border-end mt-3">
                                <h6 class="fw-bold text-danger">
                                    <i class="bi bi-cash-coin me-1"></i>Presupuesto:
                                </h6>
                                <small class="d-block">&bull; CDP (Número, Fecha, Valor)</small>
                                <small class="d-block">&bull; RP (Número, Fecha)</small>
                                <small class="d-block">&bull; Ficha EBI, Rubro</small>
                            </div>

                            <!-- Contratos -->
                            <div class="col-md-4 mt-3">
                                <h6 class="fw-bold text-dark">
                                    <i class="bi bi-file-earmark-text me-1"></i>Contratos:
                                </h6>
                                <small class="d-block">&bull; No. Contrato, Objeto</small>
                                <small class="d-block">&bull; Valores, Fechas (Inicio/Term.)</small>
                                <small class="d-block">&bull; Dependencia, Proceso</small>
                            </div>
                        </div>
                        <hr>
                        <small class="text-muted">
                            <i class="bi bi-lightbulb me-1"></i>
                            <strong>Nota:</strong> Puede cargar los tres tipos de registros en el mismo archivo si
                            contiene todas las columnas.
                        </small>
                    </div>

                    <div class="row g-4 mt-2">
                        <!-- Manual Upload Form -->
                        <div class="col-md-6">
                            <form id="uploadForm" action="upload" method="POST" enctype="multipart/form-data" class="border p-4 rounded bg-white h-100 shadow-sm">
                                <div class="mb-3">
                                    <label for="fileUpload" class="form-label fw-bold text-dark">Opción 1: Subida Manual</label>
                                    <p class="text-muted small">Seleccione un archivo de su computadora (.xlsx o .csv)</p>
                                    <input class="form-control" type="file" id="fileUpload" name="file" accept=".csv, .xlsx, .xls" required>
                                </div>
                                <div class="text-end mt-4">
                                    <button type="submit" class="btn btn-primary w-100"><i class="bi bi-cloud-upload me-2"></i> Cargar Archivo</button>
                                </div>
                            </form>
                        </div>

                        <!-- Google Drive Auto-Sync Form -->
                        <div class="col-md-6">
                            <form id="syncForm" action="google-sync" method="POST" class="border border-success p-4 rounded h-100 shadow-sm" style="background-color: #f2fcf5;">
                                <div class="mb-3">
                                    <label class="form-label fw-bold text-success">Opción 2: Sincronización Automática</label>
                                    <p class="text-muted small">Conecta directamente con la matriz alojada en Google Drive (Requiere configuración de credenciales).</p>
                                </div>
                                <div class="text-end mt-4 pt-3">
                                    <button type="submit" class="btn btn-success fw-bold w-100" id="btnSync" onclick="document.getElementById('btnSync').innerHTML='<span class=\'spinner-border spinner-border-sm me-2\'></span>Conectando...';">
                                        <i class="bi bi-google me-2"></i>Sincronizar desde Google Drive
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                    
                    <div class="text-center mt-4">
                        <a href="index.jsp" class="btn btn-secondary px-5"><i class="bi bi-x-circle me-2"></i> Cerrar / Volver</a>
                    </div>

                    <% if (session.getAttribute("debug") != null) { %>
                        <div class="alert alert-secondary mt-4">
                            <h6 class="alert-heading"><i class="bi bi-terminal me-2"></i>Log de Procesamiento (Debug)</h6>
                            <pre class="mb-0" style="white-space: pre-wrap; font-size: 0.80rem; font-family: 'Courier New', monospace; max-height: 500px; overflow-y: auto;"><%= session.getAttribute("debug") %></pre>
                        </div>
                        <% session.removeAttribute("debug"); } %>
                </div>
            </div>
        </div>

        <!-- SweetAlert2 -->
        <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
        <!-- Flash Message Handling -->
        <% String msg=(String) session.getAttribute("message"); String err=(String) session.getAttribute("error");
            if(msg !=null) session.removeAttribute("message"); if(err !=null) session.removeAttribute("error"); %>

            <% if (msg !=null) { %>
                <script>
                    Swal.fire({
                        icon: 'success',
                        title: 'Â¡Carga Exitosa!',
                        html: '<%= msg %>',
                        confirmButtonText: 'Entendido',
                        confirmButtonColor: '#0d6efd'
                    });
                </script>
                <% } %>

                    <% if (err !=null) { %>
                        <script>
                            Swal.fire({
                                icon: 'error',
                                title: 'Error en la Carga',
                                text: '<%= err %>',
                                confirmButtonColor: '#dc3545'
                            });
                        </script>
                        <% } %>

                            <script>
                                // Loading Overlay script
                                document.getElementById('uploadForm').addEventListener('submit', function (e) {
                                    // Optional: Validate file input is not empty before showing loader
                                    // But 'required' attribute on input handles that mostly.

                                    Swal.fire({
                                        title: 'Procesando solicitud',
                                        html: 'Esto puede tardar unos segundos...<br>Por favor no cierre la página.',
                                        allowOutsideClick: false,
                                        allowEscapeKey: false,
                                        didOpen: () => {
                                            Swal.showLoading();
                                        }
                                    });
                                });
                            </script>

                            <!-- Footer -->
                            <jsp:include page="inc/footer.jsp" />

                            <script
                                src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    </body>

    </html>
