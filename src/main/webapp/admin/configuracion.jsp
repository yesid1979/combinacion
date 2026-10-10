<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Configuración - Gestión Contratos</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons CSS -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <!-- DataTables CSS for advanced table features -->
    <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
    <!-- DataTables Responsive CSS -->
    <link href="https://cdn.datatables.net/responsive/2.4.1/css/responsive.bootstrap5.min.css" rel="stylesheet">
    <!-- Custom Styles -->
    <link href="${pageContext.request.contextPath}/assets/css/styles.css" rel="stylesheet">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
    <style>
        .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
        .table td { vertical-align: middle; }
        .flex-grow-1 { flex-grow: 1 !important; }
    </style>
</head>

<body class="bg-light d-flex flex-column min-vh-100">
    <jsp:include page="../inc/navbar.jsp" />

    <div class="container mt-4 mb-5 flex-grow-1">

        <nav aria-label="breadcrumb">
            <ol class="breadcrumb breadcrumb-premium">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
                <li class="breadcrumb-item active text-muted">Administración</li>
                <li class="breadcrumb-item active" aria-current="page"><i class="bi bi-gear-fill me-1"></i>Configuración del Sistema</li>
            </ol>
        </nav>

        <c:if test="${not empty param.msg}">
            <div class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
                <c:choose>
                    <c:when test="${param.msg == 'success'}"><i class="bi bi-check-circle-fill me-2"></i>Configuración guardada exitosamente.</c:when>
                    <c:when test="${param.msg == 'deleted'}"><i class="bi bi-trash-fill me-2"></i>Configuración eliminada exitosamente.</c:when>
                </c:choose>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${param.action == 'new' || not empty configEdit}">
                <div class="d-flex justify-content-between align-items-center mb-3 mt-3">
                    <div>
                        <h3 class="fw-bold text-dark mb-1">
                            <c:choose>
                                <c:when test="${not empty configEdit}"><i class="bi bi-pencil-square text-primary me-2"></i>Editar Configuración</c:when>
                                <c:otherwise><i class="bi bi-plus-circle-fill text-success me-2"></i>Nueva Configuración</c:otherwise>
                            </c:choose>
                        </h3>
                        <p class="text-muted mb-0">Parámetros clave del sistema y carpetas de Google Drive</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/admin/configuracion" class="btn btn-outline-secondary fw-bold">
                        <i class="bi bi-arrow-left me-1"></i>Volver al listado
                    </a>
                </div>

                <!-- Tarjeta de Guía Rápida para el Administrador -->
                <div class="card border-0 shadow-sm mb-4 bg-light border-start border-4 border-primary">
                    <div class="card-body p-3">
                        <h6 class="fw-bold text-primary mb-2"><i class="bi bi-info-circle-fill me-2"></i>Guía de Nombres y Parámetros del Sistema (Google Drive)</h6>
                        <div class="row g-2 small text-muted">
                            <div class="col-md-6">
                                <ul class="list-unstyled mb-0">
                                    <li class="mb-2">
                                        <strong><code>DRIVE_CARPETA_CUENTAS_COBRO</code></strong> <span class="badge bg-secondary">o DRIVE_CARPETA_PRUEBAS</span>:<br>
                                        <span class="text-dark">Carpeta oficial en Google Drive donde se guardan las <strong>Cuentas de Cobro e Informes de Supervisión</strong> de los contratistas. <em>(Se actualiza cada año con el nuevo ID de la vigencia)</em>.</span>
                                    </li>
                                    <li class="mb-2">
                                        <strong><code>DRIVE_CARPETA_FIRMAS</code></strong>:<br>
                                        <span class="text-dark">Carpeta en Google Drive donde se almacenan las firmas digitales de supervisores y contratistas.</span>
                                    </li>
                                </ul>
                            </div>
                            <div class="col-md-6">
                                <ul class="list-unstyled mb-0">
                                    <li class="mb-2">
                                        <strong><code>DRIVE_CARPETA_IMAGENES_WEB</code></strong>:<br>
                                        <span class="text-dark">Carpeta en Google Drive para las fotos o evidencias gráficas subidas desde el editor del informe.</span>
                                    </li>
                                    <li class="mb-2">
                                        <strong><code>DRIVE_CARPETA_SISTEMA</code></strong>:<br>
                                        <span class="text-dark">Carpeta raíz principal que contiene la estructura general en Google Drive.</span>
                                    </li>
                                    <li class="mb-2">
                                        <strong><code>DRIVE_CARPETA_EVIDENCIAS</code></strong>:<br>
                                        <span class="text-dark">Nombre del subdirectorio que se genera automáticamente dentro de cada cuota. <span class="badge bg-warning text-dark">Importante</span> Su valor debe ser el texto exacto <code>EVIDENCIAS</code> (no poner ID).</span>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card border-0 shadow-sm">
                    <div class="card-body p-4">
                        <form action="${pageContext.request.contextPath}/admin/configuracion" method="POST" id="formConfiguracion">
                            <input type="hidden" name="action" value="save">
                            <input type="hidden" name="id" value="${configEdit != null ? configEdit.id : ''}">

                            <!-- Campo 1: Nombre de la Variable / Parámetro -->
                            <div class="mb-4">
                                <label class="form-label fw-bold text-dark d-flex justify-content-between align-items-center">
                                    <span>1. Nombre del Parámetro / Variable del Sistema <span class="text-danger">*</span></span>
                                    <span class="badge bg-secondary font-monospace" id="badgeClaveTipo">Seleccione o escriba</span>
                                </label>
                                <input type="text" 
                                       class="form-control form-control-lg font-monospace text-uppercase" 
                                       id="inputClave" 
                                       name="clave" 
                                       list="sugerenciasClaves"
                                       placeholder="Ej: DRIVE_CARPETA_CUENTAS_COBRO"
                                       value="${configEdit != null ? configEdit.clave : ''}" 
                                       required 
                                       autocomplete="off">
                                
                                <datalist id="sugerenciasClaves">
                                    <option value="DRIVE_CARPETA_CUENTAS_COBRO">Carpeta oficial para Informes y Cuentas de Cobro (Google Drive)</option>
                                    <option value="DRIVE_CARPETA_PRUEBAS">Carpeta para Informes y Cuentas de Cobro (Nombre histórico)</option>
                                    <option value="DRIVE_CARPETA_FIRMAS">Carpeta para Firmas Digitales (Google Drive)</option>
                                    <option value="DRIVE_CARPETA_IMAGENES_WEB">Carpeta para Imágenes del Editor Web (Google Drive)</option>
                                    <option value="DRIVE_CARPETA_SISTEMA">Carpeta Raíz General del Sistema (Google Drive)</option>
                                    <option value="DRIVE_CARPETA_EVIDENCIAS">Subcarpeta de Anexos dentro de cada cuota (EVIDENCIAS)</option>
                                </datalist>

                                <!-- Botones de sugerencia rápida -->
                                <div class="mt-2 d-flex flex-wrap gap-1 align-items-center">
                                    <span class="small text-muted me-1 fw-bold">Parámetros disponibles:</span>
                                    <button type="button" class="btn btn-sm btn-outline-primary py-1 px-2 btn-sugerencia" data-clave="DRIVE_CARPETA_CUENTAS_COBRO" data-desc="Carpeta principal de Google Drive para Informes de Supervisión y Cuentas de Cobro de la vigencia actual.">📁 Cuentas de Cobro / Informes</button>
                                    <button type="button" class="btn btn-sm btn-outline-secondary py-1 px-2 btn-sugerencia" data-clave="DRIVE_CARPETA_FIRMAS" data-desc="Carpeta de Google Drive para almacenar las firmas digitalizadas de supervisores y contratistas.">✍️ Firmas Digitales</button>
                                    <button type="button" class="btn btn-sm btn-outline-secondary py-1 px-2 btn-sugerencia" data-clave="DRIVE_CARPETA_IMAGENES_WEB" data-desc="Carpeta de Google Drive para imágenes e ilustraciones de informes web.">🖼️ Imágenes Editor</button>
                                    <button type="button" class="btn btn-sm btn-outline-secondary py-1 px-2 btn-sugerencia" data-clave="DRIVE_CARPETA_SISTEMA" data-desc="Carpeta raíz principal del aplicativo en Google Drive.">🌐 Carpeta Raíz Sistema</button>
                                    <button type="button" class="btn btn-sm btn-outline-secondary py-1 px-2 btn-sugerencia" data-clave="DRIVE_CARPETA_EVIDENCIAS" data-valor="EVIDENCIAS" data-desc="Nombre exacto de la subcarpeta de evidencias creada dentro de cada cuota.">📎 Subcarpeta Evidencias</button>
                                </div>
                                <div class="form-text mt-1 text-muted">
                                    <i class="bi bi-info-circle me-1"></i><strong>Nota:</strong> No es una contraseña de acceso. Es el nombre técnico de la opción que el sistema reconoce para saber qué carpeta utilizar.
                                </div>
                            </div>

                            <!-- Campo 2: Identificador / Valor -->
                            <div class="mb-4">
                                <label class="form-label fw-bold text-dark">
                                    2. Identificador / Valor (ID de Google Drive, Enlace Web o Texto) <span class="text-danger">*</span>
                                </label>
                                <input type="text" 
                                       class="form-control form-control-lg" 
                                       id="inputValor" 
                                       name="valor" 
                                       placeholder="Pegue aquí el ID o enlace (ej: 1G4pkT8wpoTiNTf4SH4_FNudq-21UkVe9)"
                                       value="${configEdit != null ? configEdit.valor : ''}" 
                                       required>
                                
                                <!-- Mensaje contextual de ayuda del valor -->
                                <div id="ayudaValorDrive" class="mt-2 p-3 rounded bg-light border small">
                                    <div class="d-flex align-items-start">
                                        <i class="bi bi-folder-check text-primary me-2 fs-5"></i>
                                        <div>
                                            <strong class="text-dark">¿Qué ingresar en este campo según el parámetro?</strong>
                                            <ul class="mb-0 ps-3 mt-1 text-muted">
                                                <li><strong>Para carpetas de Drive:</strong> Pegue el <strong>ID alfanumérico</strong> de la carpeta (ej: <code>1G4pkT8wpoTiNTf4SH4_FNudq-21UkVe9</code>) o el <strong>enlace completo</strong> de la barra del navegador (ej: <code>https://drive.google.com/drive/folders/1G4pk...</code>). El sistema extrae el ID automáticamente.</li>
                                                <li><strong>Para DRIVE_CARPETA_EVIDENCIAS:</strong> Escriba únicamente la palabra <code>EVIDENCIAS</code> (este no lleva enlace ni ID de Drive).</li>
                                            </ul>
                                        </div>
                                    </div>
                                    <div id="vistaPreviaDrive" class="mt-2 pt-2 border-top d-none">
                                        <span class="badge bg-success me-1"><i class="bi bi-check-circle me-1"></i>ID Detectado:</span>
                                        <code id="driveIdDetectado" class="fw-bold"></code>
                                        <a href="#" id="linkPruebaDrive" target="_blank" class="btn btn-sm btn-outline-primary ms-2 py-0">
                                            <i class="bi bi-box-arrow-up-right me-1"></i>Abrir y verificar carpeta en Google Drive
                                        </a>
                                    </div>
                                </div>
                            </div>

                            <!-- Campo 3: Descripción -->
                            <div class="mb-4">
                                <label class="form-label fw-bold text-dark">
                                    3. Descripción del Parámetro (Propósito y Vigencia)
                                </label>
                                <textarea class="form-control" 
                                          id="inputDescripcion" 
                                          name="descripcion" 
                                          rows="3" 
                                          placeholder="Ej: Carpeta principal en Google Drive para guardar los informes y cuentas de cobro de los contratistas durante la vigencia 2026.">${configEdit != null ? configEdit.descripcion : ''}</textarea>
                                <div class="form-text text-muted">
                                    Explique para qué se utiliza este parámetro para que cualquier persona o administrador entienda su función a futuro.
                                </div>
                            </div>

                            <!-- Botones de Acción -->
                            <div class="d-flex justify-content-end gap-2 border-top pt-3">
                                <a href="${pageContext.request.contextPath}/admin/configuracion" class="btn btn-secondary fw-bold px-4">
                                    <i class="bi bi-x-circle me-1"></i>Cancelar
                                </a>
                                <button type="submit" class="btn btn-primary fw-bold px-4" style="background-color: #004884; border-color: #004884;">
                                    <i class="bi bi-check-circle-fill me-1"></i>Guardar Configuración
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </c:when>
            
            <c:otherwise>
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h3 class="fw-bold text-dark mb-1">Configuraciones del Sistema</h3>
                        <p class="text-muted mb-0">Parámetros generales de carpetas de Google Drive y variables de entorno</p>
                    </div>
                    <div>
                        <a href="${pageContext.request.contextPath}/admin/configuracion?action=new" class="btn text-white fw-bold shadow-sm" style="background-color: #198754;">
                            <i class="bi bi-plus-circle-fill me-1"></i>Nueva configuración
                        </a>
                    </div>
                </div>

                <!-- Tarjeta Informativa Resumida en el Listado -->
                <div class="alert alert-info border-0 shadow-sm d-flex align-items-start gap-3 mb-4">
                    <i class="bi bi-cloud-arrow-up-fill fs-3 text-info"></i>
                    <div>
                        <strong class="d-block text-dark">Gestión de Carpetas en la Nube (Google Drive)</strong>
                        <span class="small text-muted">
                            Los parámetros con prefijo <code>DRIVE_CARPETA_</code> definen las carpetas de Google Drive donde se almacenan las cuentas, firmas e imágenes. Puede hacer clic en el enlace de cada carpeta para abrirla y verificarla directamente.
                        </span>
                    </div>
                </div>

                <div class="card border-0 shadow-sm">
                    <div class="card-body">
                        <table class="table table-striped w-100" id="configTableModern">
                            <thead class="table-dark">
                                <tr>
                                    <th style="width: 30%;">Parámetro del Sistema</th>
                                    <th style="width: 32%;">ID / Carpeta en Google Drive</th>
                                    <th style="width: 26%;">Descripción y Propósito</th>
                                    <th class="text-center" style="width: 12%;">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <!-- Datos cargados por AJAX -->
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <jsp:include page="../inc/footer.jsp" />

    <!-- Scripts -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.4/js/dataTables.bootstrap5.min.js"></script>
    <!-- DataTables Responsive JS -->
    <script src="https://cdn.datatables.net/responsive/2.4.1/js/dataTables.responsive.min.js"></script>
    <script src="https://cdn.datatables.net/responsive/2.4.1/js/responsive.bootstrap5.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    
    <script>
        $(document).ready(function () {
            // Lógica de formulario interactivo
            const inputClave = $('#inputClave');
            const inputValor = $('#inputValor');
            const inputDesc = $('#inputDescripcion');
            const vistaPrevia = $('#vistaPreviaDrive');
            const spanDriveId = $('#driveIdDetectado');
            const linkDrive = $('#linkPruebaDrive');
            const badgeTipo = $('#badgeClaveTipo');

            // Función para extraer ID de Drive si viene como enlace o ID directo
            function extraerDriveId(val) {
                if (!val) return null;
                val = val.trim();
                // Patrón común de ID de Google Drive (alfanumérico con guiones de al menos 20 caracteres)
                const regexUrl = /[-\w]{25,}/;
                const match = val.match(regexUrl);
                return match ? match[0] : null;
            }

            function actualizarAyuda() {
                const clave = inputClave.val() ? inputClave.val().trim().toUpperCase() : '';
                const valor = inputValor.val() ? inputValor.val().trim() : '';

                if (clave.startsWith('DRIVE_')) {
                    badgeTipo.removeClass('bg-secondary bg-warning').addClass('bg-primary').text('Carpeta Google Drive');
                } else if (clave.length > 0) {
                    badgeTipo.removeClass('bg-primary bg-warning').addClass('bg-secondary').text('Parámetro General');
                } else {
                    badgeTipo.removeClass('bg-primary bg-warning').addClass('bg-secondary').text('Seleccione o escriba');
                }

                if (clave === 'DRIVE_CARPETA_EVIDENCIAS') {
                    badgeTipo.removeClass('bg-primary bg-secondary').addClass('bg-warning text-dark').text('Subcarpeta Local');
                    vistaPrevia.addClass('d-none');
                } else if (valor) {
                    const driveId = extraerDriveId(valor);
                    if (driveId && driveId.length >= 20 && !driveId.includes('EVIDENCIAS')) {
                        spanDriveId.text(driveId);
                        linkDrive.attr('href', 'https://drive.google.com/drive/folders/' + driveId);
                        vistaPrevia.removeClass('d-none');
                    } else {
                        vistaPrevia.addClass('d-none');
                    }
                } else {
                    vistaPrevia.addClass('d-none');
                }
            }

            // Click en botones de sugerencia rápida
            $('.btn-sugerencia').on('click', function () {
                const clave = $(this).data('clave');
                const desc = $(this).data('desc');
                const valor = $(this).data('valor');

                inputClave.val(clave);
                if (desc && !inputDesc.val()) {
                    inputDesc.val(desc);
                }
                if (valor) {
                    inputValor.val(valor);
                }
                actualizarAyuda();
                inputValor.focus();
            });

            inputClave.on('input change', actualizarAyuda);
            inputValor.on('input change', actualizarAyuda);

            if (inputClave.length) {
                actualizarAyuda();
            }

            // DataTable para el listado
            if ($('#configTableModern').length) {
                $('#configTableModern').DataTable({
                    "processing": true,
                    "serverSide": true,
                    "responsive": true,
                    "autoWidth": false,
                    "ajax": {
                        "url": "${pageContext.request.contextPath}/admin/configuracion",
                        "type": "POST",
                        "data": function(d) {
                            d.action = "data";
                        },
                        "error": function(xhr, error, thrown) {
                            console.error("Error en AJAX:", error, thrown);
                            Swal.fire('Error', 'No se pudieron cargar los datos.', 'error');
                        }
                    },
                    "columns": [
                        {
                            "data": 0,
                            "render": function(data, type, row) {
                                let badge = '';
                                if (data === 'DRIVE_CARPETA_CUENTAS_COBRO' || data === 'DRIVE_CARPETA_PRUEBAS') {
                                    badge = '<div class="small text-primary fw-bold"><i class="bi bi-folder-fill me-1"></i>Cuentas de Cobro / Informes</div>';
                                } else if (data === 'DRIVE_CARPETA_FIRMAS') {
                                    badge = '<div class="small text-secondary fw-semibold"><i class="bi bi-pen-fill me-1"></i>Firmas Digitales</div>';
                                } else if (data === 'DRIVE_CARPETA_IMAGENES_WEB') {
                                    badge = '<div class="small text-secondary fw-semibold"><i class="bi bi-image-fill me-1"></i>Imágenes Web</div>';
                                } else if (data === 'DRIVE_CARPETA_SISTEMA') {
                                    badge = '<div class="small text-secondary fw-semibold"><i class="bi bi-hdd-network-fill me-1"></i>Raíz del Sistema</div>';
                                } else if (data === 'DRIVE_CARPETA_EVIDENCIAS') {
                                    badge = '<div class="small text-warning fw-semibold"><i class="bi bi-folder2-open me-1"></i>Subcarpeta de Evidencias</div>';
                                }
                                return '<span class="font-monospace fw-bold text-dark">' + data + '</span>' + badge;
                            }
                        },
                        {
                            "data": 1,
                            "render": function(data, type, row) {
                                if (!data) return '<em class="text-muted">(Vacío)</em>';
                                
                                // Si parece un ID de Google Drive (20+ caracteres alfanuméricos)
                                const esDriveId = /^[a-zA-Z0-9_-]{20,}$/.test(data.trim());
                                if (esDriveId) {
                                    return '<div class="d-flex flex-column align-items-start gap-1">' +
                                           '<code class="text-primary fw-bold" style="word-break: break-all;">' + data + '</code>' +
                                           '<a href="https://drive.google.com/drive/folders/' + data.trim() + '" target="_blank" class="btn btn-xs btn-sm btn-outline-primary py-0 px-1 small" style="font-size: 0.75rem;">' +
                                           '<i class="bi bi-box-arrow-up-right me-1"></i>Abrir en Drive</a>' +
                                           '</div>';
                                }
                                
                                return '<span class="fw-semibold text-secondary" style="word-break: break-word;">' + data + '</span>';
                            }
                        },
                        { 
                            "data": 2,
                            "render": function(data, type, row) {
                                return data ? '<span class="text-muted small">' + data + '</span>' : '<em class="text-muted small">(Sin descripción)</em>';
                            }
                        },
                        {
                            "data": 3,
                            "className": "text-center",
                            "orderable": false,
                            "render": function(data, type, row) {
                                let btnEdit = '<a href="${pageContext.request.contextPath}/admin/configuracion?action=edit&id=' + data + '" class="btn btn-sm btn-outline-primary" title="Editar"><i class="bi bi-pencil-square"></i></a> ';
                                let btnDel = '<button onclick="confirmarEliminar(' + data + ')" class="btn btn-sm btn-outline-danger" title="Eliminar"><i class="bi bi-trash"></i></button>';
                                return '<div class="d-flex justify-content-center gap-2">' + btnEdit + btnDel + '</div>';
                            }
                        }
                    ],
                    "order": [[0, "asc"]],
                    "language": {
                        "url": "https://cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json"
                    }
                });
            }
        });

        function confirmarEliminar(id) {
            Swal.fire({
                title: '¿Estás seguro?',
                text: "Esta acción no se puede deshacer",
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#dc3545',
                cancelButtonColor: '#6c757d',
                confirmButtonText: 'Sí, eliminar',
                cancelButtonText: 'Cancelar'
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '${pageContext.request.contextPath}/admin/configuracion?action=delete&id=' + id;
                }
            });
        }
    </script>
</body>
</html>
