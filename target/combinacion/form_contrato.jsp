<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html lang="es">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Nuevo Contrato - Gestión de Prestadores</title>
        <!-- Bootstrap 5 CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <style>
            .section-title {
                background-color: #f8f9fa;
                padding: 10px;
                margin-top: 20px;
                margin-bottom: 20px;
                border-left: 5px solid #0d6efd;
                font-weight: bold;
            }
        </style>
    </head>

    <body class="bg-light">

        <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
            <div class="container-fluid">
                <a class="navbar-brand" href="#">Gestión Contratos</a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarNav">
                    <ul class="navbar-nav">
                        <li class="nav-item">
                            <a class="nav-link" href="contratos">Listado</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link active" href="contratos?action=new">Nuevo Contrato</a>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>

        <div class="container mt-4 mb-5">
            <div class="row">
                <div class="col-12">
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            ${error}
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>
                </div>
            </div>

            <form action="contratos" method="POST" id="contratoForm">
                <input type="hidden" name="action" value="insert">

                <!-- Pestañas de Navegación -->
                <ul class="nav nav-tabs" id="myTab" role="tablist">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link active" id="contratista-tab" data-bs-toggle="tab"
                            data-bs-target="#contratista" type="button" role="tab">Contratista</button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="contrato-tab" data-bs-toggle="tab" data-bs-target="#contrato"
                            type="button" role="tab">Detalles Contrato</button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="financiero-tab" data-bs-toggle="tab" data-bs-target="#financiero"
                            type="button" role="tab">Presupuesto</button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="admin-tab" data-bs-toggle="tab" data-bs-target="#admin"
                            type="button" role="tab">Administrativo</button>
                    </li>
                </ul>

                <div class="tab-content border border-top-0 p-4 bg-white shadow-sm" id="myTabContent">

                    <!-- SECCION CONTRATISTA -->
                    <div class="tab-pane fade show active" id="contratista" role="tabpanel">
                        <div class="section-title">Datos del Contratista</div>
                        <div class="row g-3">
                            <div class="col-md-4">
                                <label class="form-label">Cédula</label>
                                <input type="text" class="form-control" name="contratista_cedula" required>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label">DV</label>
                                <input type="text" class="form-control" name="contratista_dv">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Nombre Completo</label>
                                <input type="text" class="form-control" name="contratista_nombre" required>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Teléfono</label>
                                <input type="text" class="form-control" name="contratista_telefono">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Correo</label>
                                <input type="email" class="form-control" name="contratista_correo">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Dirección</label>
                                <input type="text" class="form-control" name="contratista_direccion">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Fecha Nacimiento</label>
                                <input type="date" class="form-control" name="contratista_fecha_nac">
                            </div>
                            <div class="col-md-2">
                                <label class="form-label">Edad</label>
                                <input type="number" class="form-control" name="contratista_edad">
                            </div>
                            <!-- Agregado mas campos Formacion, Experiencia -->
                        </div>
                    </div>

                    <!-- SECCION CONTRATO -->
                    <div class="tab-pane fade" id="contrato" role="tabpanel">
                        <div class="section-title">Información Contractual</div>
                        <div class="row g-3">
                            <div class="col-md-4">
                                <label class="form-label">Número Contrato</label>
                                <input type="text" class="form-control" name="numero_contrato" required>
                            </div>
                            <div class="col-md-8">
                                <label class="form-label">Objeto del Contrato</label>
                                <textarea class="form-control" name="objeto" rows="2"></textarea>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">Fecha Inicio</label>
                                <input type="date" class="form-control" name="fecha_inicio">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">Fecha Terminación</label>
                                <input type="date" class="form-control" name="fecha_terminacion">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">Valor Total</label>
                                <input type="number" step="0.01" class="form-control" name="valor_total">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">Total en Letras</label>
                                <input type="text" class="form-control" name="valor_total_letras">
                            </div>

                            <div class="section-title mt-4">Detalle de Pagos</div>
                            <div class="col-md-3">
                                <label class="form-label">Valor Cuota (Num)</label>
                                <input type="number" step="0.01" class="form-control" name="valor_cuota_numero">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">Valor Cuota (Letras)</label>
                                <input type="text" class="form-control" name="valor_cuota_letras">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">Media Cuota (Num)</label> <!-- New Field -->
                                <input type="number" step="0.01" class="form-control" name="valor_media_cuota_numero">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">Media Cuota (Letras)</label> <!-- New Field -->
                                <input type="text" class="form-control" name="valor_media_cuota_letras">
                            </div>
                        </div>
                    </div>

                    <!-- SECCION PRESUPUESTO -->
                    <div class="tab-pane fade" id="financiero" role="tabpanel">
                        <div class="section-title">Datos Presupuestales</div>
                        <div class="row g-3">
                            <div class="col-md-4">
                                <label class="form-label">CDP Número</label>
                                <input type="text" class="form-control" name="presupuesto_cdp">
                            </div>
                            <!-- Add more budget fields -->
                        </div>
                    </div>

                    <!-- SECCION ADMINISTRATIVO -->
                    <div class="tab-pane fade" id="admin" role="tabpanel">
                        <div class="section-title">Supervisión y Ordenación</div>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">Nombre Supervisor</label>
                                <input type="text" class="form-control" name="supervisor_nombre">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Cédula Supervisor</label>
                                <input type="text" class="form-control" name="supervisor_cedula">
                            </div>
                            <div class="col-md-12">
                                <label class="form-label">Nombre Ordenador del Gasto</label>
                                <input type="text" class="form-control" name="ordenador_nombre">
                            </div>
                        </div>

                        <div class="section-title mt-4">Estructuradores</div>
                        <div class="row g-3">
                            <div class="col-md-4">
                                <label class="form-label">Jurídico</label>
                                <input type="text" class="form-control" name="estructurador_juridico">
                            </div>
                        </div>

                        <div class="mt-4 text-end">
                            <button type="submit" class="btn btn-success btn-lg">Guardar Contrato</button>
                        </div>
                    </div>

                </div>
            </form>
        </div>

        <!-- jQuery and Bootstrap JS -->
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            $(document).ready(function () {
                // Simple jQuery test
                console.log("jQuery is working!");

                // Example: Auto-calculate end date or validate fields
                $('input[name="contratista_cedula"]').on('blur', function () {
                    var cedula = $(this).val();
                    if (cedula) {
                        // Here we could make an AJAX call to check if contratista exists
                        // and pre-fill the form.
                        console.log("Checking cedula: " + cedula);
                    }
                });
            });
        </script>
    </body>

    </html>