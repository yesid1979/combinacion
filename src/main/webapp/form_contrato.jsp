<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="es">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>${action == 'update' ? 'Editar' : (action == 'view' ? 'Ver' : 'Nuevo')} Contrato - Gestión de
                    Prestadores</title>
                <!-- Bootstrap 5 CSS -->
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                <link rel="stylesheet"
                    href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
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

            <body class="bg-light d-flex flex-column min-vh-100">

                <jsp:include page="inc/navbar.jsp" />

                <div class="container mt-4 mb-5">
                    <div class="row">
                        <div class="col-12">
                            <c:if test="${not empty error}">
                                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                    <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
                                    <button type="button" class="btn-close" data-bs-dismiss="alert"
                                        aria-label="Close"></button>
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <div class="card shadow-sm border-0">
                        <div class="card-body p-4">
                            <h2 class="mb-4 fw-bold">${action == 'update' ? 'Editar' : (action == 'view' ? 'Detalle de'
                                : 'Registrar nuevo')} contrato</h2>

                            <form action="contratos" method="POST" id="contratoForm">
                                <input type="hidden" name="action"
                                    value="${not empty action ? (action == 'view' ? 'list' : action) : 'insert'}">
                                <c:if test="${not empty contrato.id}">
                                    <input type="hidden" name="id" value="${contrato.id}">
                                </c:if>

                                <!-- Pestañas de Navegación -->
                                <ul class="nav nav-tabs" id="myTab" role="tablist">
                                    <li class="nav-item" role="presentation">
                                        <button class="nav-link active" id="contratista-tab" data-bs-toggle="tab"
                                            data-bs-target="#contratista" type="button" role="tab"
                                            aria-controls="contratista" aria-selected="true">Contratista</button>
                                    </li>
                                    <li class="nav-item" role="presentation">
                                        <button class="nav-link" id="contrato-tab" data-bs-toggle="tab"
                                            data-bs-target="#contrato" type="button" role="tab" aria-controls="contrato"
                                            aria-selected="false">Detalles Contrato</button>
                                    </li>
                                    <li class="nav-item" role="presentation">
                                        <button class="nav-link" id="financiero-tab" data-bs-toggle="tab"
                                            data-bs-target="#financiero" type="button" role="tab"
                                            aria-controls="financiero" aria-selected="false">Presupuesto</button>
                                    </li>
                                    <li class="nav-item" role="presentation">
                                        <button class="nav-link" id="admin-tab" data-bs-toggle="tab"
                                            data-bs-target="#admin" type="button" role="tab" aria-controls="admin"
                                            aria-selected="false">Administrativo</button>
                                    </li>
                                </ul>

                                <div class="tab-content border border-top-0 p-4 bg-white shadow-sm" id="myTabContent">

                                    <!-- SECCION CONTRATISTA -->
                                    <div class="tab-pane fade show active" id="contratista" role="tabpanel">
                                        <div class="section-title">Datos del Contratista</div>
                                        <div class="row g-3">
                                            <div class="col-md-4">
                                                <label class="form-label">Cédula</label>
                                                <input type="text" class="form-control" name="contratista_cedula"
                                                    value="${contrato.contratista.cedula}" required ${readonly
                                                    ? 'readonly' : '' }>
                                            </div>
                                            <div class="col-md-2">
                                                <label class="form-label">DV</label>
                                                <input type="text" class="form-control" name="contratista_dv"
                                                    value="${contrato.contratista.dv}" ${readonly ? 'readonly' : '' }>
                                            </div>
                                            <div class="col-md-6">
                                                <label class="form-label">Nombre Completo</label>
                                                <input type="text" class="form-control" name="contratista_nombre"
                                                    value="${contrato.contratista.nombre}" required ${readonly
                                                    ? 'readonly' : '' }>
                                            </div>
                                            <div class="col-md-4">
                                                <label class="form-label">Teléfono</label>
                                                <input type="text" class="form-control" name="contratista_telefono"
                                                    value="${contrato.contratista.telefono}" ${readonly ? 'readonly'
                                                    : '' }>
                                            </div>
                                            <div class="col-md-4">
                                                <label class="form-label">Correo</label>
                                                <input type="email" class="form-control" name="contratista_correo"
                                                    value="${contrato.contratista.correo}" ${readonly ? 'readonly' : ''
                                                    }>
                                            </div>
                                            <div class="col-md-4">
                                                <label class="form-label">Dirección</label>
                                                <input type="text" class="form-control" name="contratista_direccion"
                                                    value="${contrato.contratista.direccion}" ${readonly ? 'readonly'
                                                    : '' }>
                                            </div>
                                            <div class="col-md-4">
                                                <label class="form-label">Fecha Nacimiento</label>
                                                <input type="date" class="form-control" name="contratista_fecha_nac"
                                                    value="${contrato.contratista.fechaNacimiento}" ${readonly
                                                    ? 'readonly' : '' }>
                                            </div>
                                            <div class="col-md-2">
                                                <label class="form-label">Edad</label>
                                                <input type="number" class="form-control" name="contratista_edad"
                                                    value="${contrato.contratista.edad}" ${readonly ? 'readonly' : '' }>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- SECCION CONTRATO -->
                                    <div class="tab-pane fade" id="contrato" role="tabpanel">
                                        <div class="section-title">Información Contractual</div>
                                        <div class="row g-3">
                                            <div class="col-md-4">
                                                <label class="form-label">Número Contrato</label>
                                                <input type="text" class="form-control" name="numero_contrato"
                                                    value="${contrato.numeroContrato}" required ${readonly ? 'readonly'
                                                    : '' }>
                                            </div>
                                            <div class="col-md-8">
                                                <label class="form-label">Objeto del Contrato</label>
                                                <textarea class="form-control" name="objeto" rows="2" ${readonly
                                                    ? 'readonly' : '' }>${contrato.objeto}</textarea>
                                            </div>
                                            <div class="col-md-3">
                                                <label class="form-label">Fecha Inicio</label>
                                                <input type="date" class="form-control" name="fecha_inicio"
                                                    value="${contrato.fechaInicio}" ${readonly ? 'readonly' : '' }>
                                            </div>
                                            <div class="col-md-3">
                                                <label class="form-label">Fecha Terminación</label>
                                                <input type="date" class="form-control" name="fecha_terminacion"
                                                    value="${contrato.fechaTerminacion}" ${readonly ? 'readonly' : '' }>
                                            </div>
                                            <div class="col-md-3">
                                                <label class="form-label">Valor Total</label>
                                                <input type="number" step="0.01" class="form-control" name="valor_total"
                                                    value="${contrato.valorTotalNumeros}" ${readonly ? 'readonly' : ''
                                                    }>
                                            </div>
                                            <div class="col-md-3">
                                                <label class="form-label">Total en Letras</label>
                                                <input type="text" class="form-control" name="valor_total_letras"
                                                    value="${contrato.valorTotalLetras}" ${readonly ? 'readonly' : '' }>
                                            </div>

                                            <div class="section-title mt-4">Detalle de Pagos</div>
                                            <div class="col-md-3">
                                                <label class="form-label">Valor Cuota (Num)</label>
                                                <input type="number" step="0.01" class="form-control"
                                                    name="valor_cuota_numero" value="${contrato.valorCuotaNumero}"
                                                    ${readonly ? 'readonly' : '' }>
                                            </div>
                                            <div class="col-md-3">
                                                <label class="form-label">Valor Cuota (Letras)</label>
                                                <input type="text" class="form-control" name="valor_cuota_letras"
                                                    value="${contrato.valorCuotaLetras}" ${readonly ? 'readonly' : '' }>
                                            </div>
                                            <div class="col-md-3">
                                                <label class="form-label">Media Cuota (Num)</label>
                                                <input type="number" step="0.01" class="form-control"
                                                    name="valor_media_cuota_numero"
                                                    value="${contrato.valorMediaCuotaNumero}" ${readonly ? 'readonly'
                                                    : '' }>
                                            </div>
                                            <div class="col-md-3">
                                                <label class="form-label">Media Cuota (Letras)</label>
                                                <input type="text" class="form-control" name="valor_media_cuota_letras"
                                                    value="${contrato.valorMediaCuotaLetras}" ${readonly ? 'readonly'
                                                    : '' }>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- SECCION PRESUPUESTO -->
                                    <div class="tab-pane fade" id="financiero" role="tabpanel">
                                        <div class="section-title">Datos Presupuestales</div>
                                        <div class="row g-3">
                                            <div class="col-md-4">
                                                <label class="form-label">CDP Número</label>
                                                <input type="text" class="form-control" name="presupuesto_cdp"
                                                    value="${contrato.presupuestoDetalle.cdpNumero}" ${readonly
                                                    ? 'readonly' : '' }>
                                            </div>
                                            <div class="col-md-4">
                                                <label class="form-label">RPC Número</label>
                                                <input type="text" class="form-control" name="presupuesto_rpc"
                                                    value="${contrato.presupuestoDetalle.rpNumero}" ${readonly
                                                    ? 'readonly' : '' }>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- SECCION ADMINISTRATIVO -->
                                    <div class="tab-pane fade" id="admin" role="tabpanel">
                                        <div class="section-title">Supervisión y Ordenación</div>
                                        <div class="row g-3">
                                            <div class="col-md-6">
                                                <label class="form-label">Nombre Supervisor</label>
                                                <select class="form-select" name="id_supervisor" id="selectSupervisor"
                                                    onchange="actualizarCedula()" required ${readonly ? 'disabled' : ''
                                                    }>
                                                    <option value="" data-cedula="">Seleccione...</option>
                                                    <c:forEach items="${listaSupervisores}" var="sup">
                                                        <option value="${sup.id}" data-cedula="${sup.cedula}"
                                                            ${sup.id==contrato.supervisorId ? 'selected' : '' }>
                                                            ${sup.nombre}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="col-md-6">
                                                <label class="form-label">Cédula Supervisor</label>
                                                <input type="text" class="form-control" id="cedulaSupervisor" readonly
                                                    value="${contrato.supervisor.cedula}">
                                            </div>
                                            <div class="col-md-12">
                                                <label class="form-label">Nombre Ordenador del Gasto</label>
                                                <select class="form-select" name="id_ordenador" required ${readonly
                                                    ? 'disabled' : '' }>
                                                    <option value="">Seleccione...</option>
                                                    <c:forEach items="${listaOrdenadores}" var="ord">
                                                        <option value="${ord.id}" ${ord.id==contrato.ordenadorId
                                                            ? 'selected' : '' }>
                                                            ${ord.nombreOrdenador} - ${ord.cargoOrdenador}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                        </div>
                                        <script>
                                            function actualizarCedula() {
                                                var select = document.getElementById("selectSupervisor");
                                                var cedula = select.options[select.selectedIndex].getAttribute("data-cedula");
                                                document.getElementById("cedulaSupervisor").value = cedula;
                                            }
                                        </script>

                                        <div class="section-title mt-4">Estructuradores</div>
                                        <div class="row g-3">
                                            <div class="col-md-4">
                                                <label class="form-label">Jurídico</label>
                                                <input type="text" class="form-control" name="estructurador_juridico"
                                                    value="${contrato.estructurador.juridicoNombre}" ${readonly
                                                    ? 'readonly' : '' }>
                                            </div>
                                            <div class="col-md-4">
                                                <label class="form-label">Técnico</label>
                                                <input type="text" class="form-control" name="estructurador_tecnico"
                                                    value="${contrato.estructurador.tecnicoNombre}" ${readonly
                                                    ? 'readonly' : '' }>
                                            </div>
                                            <div class="col-md-4">
                                                <label class="form-label">Financiero</label>
                                                <input type="text" class="form-control" name="estructurador_financiero"
                                                    value="${contrato.estructurador.financieroNombre}" ${readonly
                                                    ? 'readonly' : '' }>
                                            </div>
                                        </div>

                                        <div class="mt-4 text-end">
                                            <a href="contratos" class="btn btn-secondary me-2" style="width: 140px;"><i
                                                    class="bi bi-x-circle me-2"></i> Cerrar</a>
                                            <c:if test="${not readonly}">
                                                <button type="submit" class="btn btn-primary" style="width: 140px;"><i
                                                        class="bi bi-save me-2"></i> Guardar</button>
                                            </c:if>
                                        </div>
                                    </div>

                                </div>
                            </form>
                        </div>
                    </div>
                </div>

                <!-- jQuery and Bootstrap JS -->
                <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
                <script>
                    $(document).ready(function () {
                        console.log("Initializing Tabs with Vanilla JS...");

                        // INITIALIZE TABS (BOOTSTRAP 5 VANILLA JS API)
                        // This handles cases where data-bs-toggle doesn't automatically trigger for some reason
                        var triggerTabList = [].slice.call(document.querySelectorAll('#myTab button'))
                        triggerTabList.forEach(function (triggerEl) {
                            var tabTrigger = new bootstrap.Tab(triggerEl)
                            triggerEl.addEventListener('click', function (event) {
                                event.preventDefault()
                                // Force show
                                tabTrigger.show()
                            })
                        })

                        // Variable de estado readonly segura
                        var isReadonly = '${readonly}' === 'true';

                        // Auto-calcular Edad
                        $('input[name="contratista_fecha_nac"]').on('change', function () {
                            var fechaNac = new Date($(this).val());
                            var hoy = new Date();
                            if (!isNaN(fechaNac.getTime())) {
                                var edad = hoy.getFullYear() - fechaNac.getFullYear();
                                var m = hoy.getMonth() - fechaNac.getMonth();
                                if (m < 0 || (m === 0 && hoy.getDate() < fechaNac.getDate())) {
                                    edad--;
                                }
                                $('input[name="contratista_edad"]').val(edad);
                            }
                        });

                        // AJAX: Buscar Contratista por Cédula (Solo si no es readonly)
                        if (!isReadonly) {
                            $('input[name="contratista_cedula"]').on('blur', function () {
                                var cedula = $(this).val();
                                if (cedula && cedula.length > 4) {
                                    $.ajax({
                                        url: '${pageContext.request.contextPath}/contratistas',
                                        type: 'GET',
                                        cache: false,
                                        data: { action: 'search', cedula: cedula, _: new Date().getTime() },
                                        dataType: 'json',
                                        success: function (data) {
                                            if (data.found) {
                                                $('input[name="contratista_dv"]').val(data.dv);
                                                $('input[name="contratista_nombre"]').val(data.nombre);
                                                $('input[name="contratista_telefono"]').val(data.telefono);
                                                $('input[name="contratista_correo"]').val(data.correo);
                                                $('input[name="contratista_direccion"]').val(data.direccion);
                                                $('input[name="contratista_fecha_nac"]').val(data.fecha_nacimiento);
                                                $('input[name="contratista_edad"]').val(data.edad);
                                                $('input[name="contratista_fecha_nac"]').trigger('change');
                                                $('input[name="contratista_cedula"]').addClass('is-valid').removeClass('is-invalid');
                                            } else {
                                                $('input[name="contratista_cedula"]').addClass('is-invalid').removeClass('is-valid');
                                                alert("El contratista con cédula " + cedula + " no existe en la base de datos.");
                                            }
                                        },
                                    });
                                }
                            });
                        }
                    });
                </script>

                <!-- Footer -->
                <jsp:include page="inc/footer.jsp" />
            </body>

            </html>