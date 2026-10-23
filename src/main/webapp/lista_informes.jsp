<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Historial de Informes - Gestión de Prestadores</title>
    <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon">
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
    <link href="https://cdn.datatables.net/responsive/2.4.1/css/responsive.bootstrap5.min.css" rel="stylesheet">
    <style>
        .table thead th { background-color: #212529 !important; color: #ffffff !important; border: none; }
        .table td { vertical-align: middle; }
        .badge { font-weight: 600; padding: 6px 12px; border-radius: 6px; text-transform: uppercase; font-size: 0.75rem; }
    </style>
    <link href="assets/css/styles.css" rel="stylesheet">
</head>
<body class="bg-light d-flex flex-column min-vh-100">

    <jsp:include page="inc/navbar.jsp" />

    <div class="container mt-4 mb-5 flex-grow-1">

            <nav aria-label="breadcrumb">
                <ol class="breadcrumb breadcrumb-premium">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house-door-fill me-1"></i>Inicio</a></li>
                    <li class="breadcrumb-item active" aria-current="page"><i class="bi bi-wallet2 me-1"></i>Cuentas</li>
                </ol>
            </nav>
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
            <div>
                <h3 class="fw-bold text-dark mb-0">Informes de Supervisión</h3>
            </div>
            <c:if test="${param.modo != 'revision'}">
                <c:choose>
                    <c:when test="${not empty misContratos && misContratos.size() > 1}">
                        <div class="dropdown">
                            <button class="btn btn-success px-4 fw-bold shadow-sm dropdown-toggle" type="button" id="dropdownNuevoInforme" data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-plus-circle me-2"></i>Nuevo Informe
                            </button>
                            <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 w-100" aria-labelledby="dropdownNuevoInforme">
                                <li><h6 class="dropdown-header">Selecciona el contrato:</h6></li>
                                <c:forEach items="${misContratos}" var="c">
                                    <li><a class="dropdown-item py-2" href="informes?action=new&contrato_id=${c.id}">
                                        <strong>${c.numeroContrato}</strong> - Periodo: ${c.periodo != null ? c.periodo : ''}
                                    </a></li>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:when>
                    <c:when test="${not empty contrato}">
                        <a href="informes?action=new&contrato_id=${contrato.id}" class="btn btn-success px-4 fw-bold shadow-sm">
                            <i class="bi bi-plus-circle me-2"></i>Nuevo Informe
                        </a>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${esAdminGlobal}">
                            <div class="text-md-end text-start">
                                <a href="contratos" class="btn btn-outline-secondary px-4 fw-bold shadow-sm">
                                    <i class="bi bi-search me-2"></i>Seleccionar contrato para registrar la cuenta de cobro
                                </a>
                            </div>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </c:if>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-warning shadow-sm border-0 mb-4">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
            </div>
        </c:if>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success shadow-sm border-0 mb-4">
                <i class="bi bi-check-circle-fill me-2"></i> ${successMessage}
            </div>
            <script>
                // Limpiar cualquier borrador guardado en localStorage porque la operación fue exitosa
                Object.keys(localStorage).forEach(function(key){
                   if (key.startsWith('autosave_')) {
                       localStorage.removeItem(key);
                   }
                });
            </script>
            <c:remove var="successMessage" scope="session" />
        </c:if>

        <c:if test="${not empty sessionScope.errorMessage}">
            <div class="alert alert-danger shadow-sm border-0 mb-4">
                <i class="bi bi-x-circle-fill me-2"></i> ${sessionScope.errorMessage}
            </div>
            <c:remove var="errorMessage" scope="session" />
        </c:if>

        <div class="card border-0 shadow-sm">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                    <div>
                        <button type="button" class="btn btn-outline-primary shadow-sm" data-bs-toggle="modal" data-bs-target="#modalLeyendaColores">
                            <i class="bi bi-palette me-1"></i> Significado de Colores
                        </button>
                    </div>
                    <div class="d-flex align-items-center">
                        <label for="filtroEstado" class="me-2 fw-bold text-secondary mb-0"><i class="bi bi-funnel"></i> Filtrar por Estado:</label>
                        <select id="filtroEstado" class="form-select w-auto shadow-sm border-primary">
                            <option value="">Todos los estados</option>
                            <option value="RADICADA">Radicadas (Revisor)</option>
                            <option value="VISTO BUENO CONTRATACION">Visto Bueno (Contratación)</option>
                            <option value="EN REVISION CONTRATACION">En Revisión (Contratación)</option>
                            <option value="EN REVISION SUPERVISOR">En Revisión (Supervisión)</option>
                            <option value="EN REVISION FINAL">En Revisión Final</option>
                            <option value="APROBADA PARA IMPRESION">Aprobadas</option>
                            <option value="DEVUELTA">Devueltas</option>
                            <option value="BORRADOR">Borradores</option>
                        </select>
                    </div>
                </div>
                <table id="informesTable" class="table table-hover w-100 nowrap">
                    <thead class="table-dark">
                        <tr>
                            <th>Contrato</th>
                            <th>Contratista</th>
                            <th>Periodo</th>
                            <th>Tipo <i class="bi bi-question-circle text-info ms-1" role="button" data-bs-toggle="modal" data-bs-target="#modalLeyendaColores" title="Ver qué significa cada color" onclick="event.stopPropagation();"></i></th>
                            <th>Cuota</th>
                            <th>Turno/Fecha</th>
                            <th>Valor Cuota</th>
                            <th>Estado</th>
                            <th class="text-center">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:set var="esAdminCuentas" value="${esAdminGlobal}" />
                        <c:set var="esRevisor" value="${esRevisorGlobal}" />
                        <c:set var="esContratista" value="${!esAdminGlobal && !esRevisorGlobal}" />
                        
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal Significado de Colores -->
    <div class="modal fade" id="modalLeyendaColores" tabindex="-1" aria-labelledby="modalLeyendaColoresLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content border-0 shadow">
                <div class="modal-header bg-dark text-white">
                    <h5 class="modal-title d-flex align-items-center gap-2" id="modalLeyendaColoresLabel">
                        <i class="bi bi-palette-fill text-warning"></i>
                        <span>Significado de Colores según el Estado</span>
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body p-4">
                    <p class="text-muted mb-3">
                        El distintivo de <strong>Tipo</strong> (Parcial / Final) y la columna de <strong>Estado</strong> adoptan el color correspondiente a la fase actual en que se encuentra la cuenta:
                    </p>
                    <div class="table-responsive">
                        <table class="table table-bordered align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th style="width: 28%;" class="text-center">Color / Distintivo</th>
                                    <th>Fase y Significado en el Flujo</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td class="text-center"><span class="badge bg-secondary w-100 py-2">BORRADOR</span></td>
                                    <td>Cuenta en elaboración por parte del contratista. Aún no ha sido radicada.</td>
                                </tr>
                                <tr>
                                    <td class="text-center"><span class="badge bg-primary w-100 py-2">RADICADA</span></td>
                                    <td>Radicada por el contratista. En espera de revisión preliminar o asignación de revisor.</td>
                                </tr>
                                <tr>
                                    <td class="text-center"><span class="badge bg-success bg-opacity-75 text-white w-100 py-2">VISTO BUENO (CONTRATACIÓN)</span></td>
                                    <td>Contratación emitió visto bueno previo y la cuenta fue asignada a un revisor.</td>
                                </tr>
                                <tr>
                                    <td class="text-center"><span class="badge bg-warning text-dark w-100 py-2">EN REVISIÓN (CONTRATACIÓN)</span></td>
                                    <td>El revisor emitió su visto bueno y la cuenta regresó a Contratación para validación final.</td>
                                </tr>
                                <tr>
                                    <td class="text-center"><span class="badge bg-info text-dark w-100 py-2">EN REVISIÓN (SUPERVISIÓN)</span></td>
                                    <td>Contratación dio el visto bueno y la cuenta está en manos del Supervisor del contrato para su revisión y aprobación.</td>
                                </tr>
                                <tr>
                                    <td class="text-center"><span class="badge bg-info text-dark w-100 py-2">EN REVISIÓN FINAL</span></td>
                                    <td>En etapa de revisión y verificación final de los soportes y documentos.</td>
                                </tr>
                                <tr>
                                    <td class="text-center"><span class="badge bg-success w-100 py-2">APROBADA (LISTA PARA FIRMA)</span></td>
                                    <td>Aprobada definitivamente por el supervisor. Habilitada para descargar e imprimir el paquete final para firma y cobro.</td>
                                </tr>
                                <tr>
                                    <td class="text-center"><span class="badge bg-danger w-100 py-2">DEVUELTA</span></td>
                                    <td>Presenta observaciones o inconsistencias y fue devuelta al contratista para su respectiva corrección.</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="modal-footer bg-light">
                    <button type="button" class="btn btn-primary px-4" data-bs-dismiss="modal">
                        <i class="bi bi-check2 me-1"></i> Entendido
                    </button>
                </div>
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
        function getEstadoBadgeClass(estado) {
            if (!estado) return 'bg-secondary';
            let est = estado.trim().toUpperCase();
            if (est === 'BORRADOR') return 'bg-secondary';
            if (est === 'RADICADA') return 'bg-primary';
            if (est === 'VISTO BUENO CONTRATACION') return 'bg-success bg-opacity-75 text-white';
            if (est === 'EN REVISION' || est === 'EN REVISION CONTRATACION') return 'bg-warning text-dark';
            if (est === 'EN REVISION FINAL' || est === 'EN REVISION SUPERVISOR') return 'bg-info text-dark';
            if (est === 'APROBADA PARA IMPRESION' || est === 'APROBADA') return 'bg-success';
            if (est === 'DEVUELTA') return 'bg-danger';
            return 'bg-secondary';
        }

        $(document).ready(function() {
            let table = $('#informesTable').DataTable({
                "processing": true,
                "serverSide": true,
                "responsive": true,
                "ajax": {
                    "url": "informes",
                    "type": "POST",
                    "data": function(d) {
                        d.action = "data";
                        d.modo = "${param.modo}";
                        d.contrato_id = "${param.contrato_id}";
                    }
                },
                "columns": [
                    { "data": "contrato", "render": function(data) { return '<span class="fw-bold text-primary">' + data + '</span>'; } },
                    { "data": "contratista" },
                    { "data": "periodo" },
                    { 
                        "data": "tipo",
                        "render": function(data, type, row) {
                            if (type === 'filter' || type === 'sort') {
                                return data;
                            }
                            let badgeClass = getEstadoBadgeClass(row ? row.estado : null);
                            return '<span class="badge ' + badgeClass + '">' + (data || '') + '</span>';
                        }
                    },
                    { "data": "cuota" },
                    { "data": "fechaRegistro" },
                    { "data": "valorCuota" },
                    { 
                        "data": "estado",
                        "render": function(data, type, row) {
                            if (type === 'filter' || type === 'sort') {
                                return data;
                            }
                            let badgeClass = getEstadoBadgeClass(data);
                            let est = data ? data.trim().toUpperCase() : '';
                            if (est === 'BORRADOR') return '<span class="badge ' + badgeClass + '">BORRADOR</span>';
                            if (est === 'RADICADA') return '<span class="badge ' + badgeClass + '">RADICADA</span>';
                            if (est === 'VISTO BUENO CONTRATACION') return '<span class="badge ' + badgeClass + '">VISTO BUENO (CONTRATACIÓN)</span>';
                            if (est === 'EN REVISION') return '<span class="badge ' + badgeClass + '">EN REVISIÓN</span>';
                            if (est === 'EN REVISION CONTRATACION') return '<span class="badge ' + badgeClass + '">EN REVISIÓN (CONTRATACIÓN)</span>';
                            if (est === 'EN REVISION FINAL') return '<span class="badge ' + badgeClass + '">EN REVISIÓN FINAL</span>';
                            if (est === 'EN REVISION SUPERVISOR') return '<span class="badge ' + badgeClass + '">EN REVISIÓN (SUPERVISIÓN)</span>';
                            if (est === 'APROBADA PARA IMPRESION' || est === 'APROBADA') return '<span class="badge ' + badgeClass + '">APROBADA (LISTA PARA FIRMA)</span>';
                            if (est === 'DEVUELTA') return '<span class="badge ' + badgeClass + '">DEVUELTA</span>';
                            return '<span class="badge ' + badgeClass + '">' + (data || '') + '</span>';
                        }
                    },
                    { 
                        "data": null,
                        "className": "text-center",
                        "orderable": false,
                        "render": function(data, type, row) {
                            let html = '';
                            let id = row.id;
                            let modo = row.modo;
                            let estado = row.estado;
                            let esAdminCuentas = row.esAdminCuentas;
                            
                            // Botón Ver
                            html += '<div class="d-flex flex-nowrap gap-1">';
                            html += '<a href="informes?action=view&id=' + id + '&modo=' + modo + '" class="btn btn-sm btn-outline-info" title="Ver Detalle"><i class="bi bi-eye"></i></a>';
                            
                            // Botón Editar
                            if (((estado === 'BORRADOR' || estado === 'DEVUELTA') && modo === 'mis_cuentas') || esAdminCuentas) {
                                html += '<a href="informes?action=edit&id=' + id + '&modo=' + modo + '" class="btn btn-sm btn-outline-primary" title="Editar Informe"><i class="bi bi-pencil-square"></i></a>';
                            }
                            
                            // Botón Descargar
                            if (estado === 'APROBADA PARA IMPRESION') {
                                html += '<a href="informes?action=download&id=' + id + '" class="btn btn-sm btn-outline-success" title="Descargar Paquete Final"><i class="bi bi-file-earmark-arrow-down"></i></a>';
                            } else {
                                html += '<button type="button" class="btn btn-sm btn-outline-secondary" title="La descarga se habilitará cuando la cuenta sea aprobada" disabled><i class="bi bi-lock"></i></button>';
                            }

                            // Botón Sincronizar Formatos con Drive (Solo Administrador y Contratación)
                            if (esAdminCuentas) {
                                html += '<a href="informes?action=syncDrive&id=' + id + '&modo=' + (modo || '') + '" class="btn btn-sm btn-outline-secondary" title="Sincronizar formatos en Drive"><i class="bi bi-cloud-arrow-up"></i></a>';
                            }
                            
                            // Botón Revisar
                            let mostrarBtnRevisar = false;
                            let tipoRolModal = '';
                            let sinRevisor = (!row.idRevisorAsignado || row.idRevisorAsignado === 0 || row.idRevisorAsignado === '0');
                            
                            // 1. Si es el Revisor asignado
                            if (row.idRevisorAsignado == row.usuarioActualId && (estado === 'RADICADA' || estado === 'VISTO BUENO CONTRATACION')) {
                                mostrarBtnRevisar = true;
                                tipoRolModal = (estado === 'VISTO BUENO CONTRATACION') ? 'revisor_post_contratacion' : 'revisor';
                            } 
                            // 2. Si es Administrador / Contratación
                            else if (esAdminCuentas) {
                                if (estado === 'EN REVISION CONTRATACION' || estado === 'EN REVISION FINAL') {
                                    mostrarBtnRevisar = true;
                                    tipoRolModal = 'contratacion_con_revisor';
                                } else if (estado === 'RADICADA' && sinRevisor) {
                                    mostrarBtnRevisar = true;
                                    tipoRolModal = 'contratacion_sin_revisor';
                                } else if (estado === 'EN REVISION SUPERVISOR') {
                                    mostrarBtnRevisar = true;
                                    tipoRolModal = 'supervisor';
                                } else if (estado === 'VISTO BUENO CONTRATACION') {
                                    mostrarBtnRevisar = true;
                                    tipoRolModal = 'revisor_post_contratacion';
                                }
                            }
                            // 3. Si es el Supervisor del contrato
                            else if (row.esSupervisorContrato && estado === 'EN REVISION SUPERVISOR') {
                                mostrarBtnRevisar = true;
                                tipoRolModal = 'supervisor';
                            }
                            
                            if (mostrarBtnRevisar) {
                                html += '<button type="button" class="btn btn-sm btn-outline-primary" title="Gestionar Revisión" onclick="abrirModalRevision(' + id + ', \'' + estado + '\', \'' + tipoRolModal + '\')"><i class="bi bi-check2-square"></i> Revisar</button>';
                            }

                            // Botón Borrar (solo en estado BORRADOR y si es el dueño o admin)
                            if (estado === 'BORRADOR' && (modo === 'mis_cuentas' || esAdminCuentas)) {
                                html += '<button type="button" class="btn btn-sm btn-outline-danger" title="Eliminar Borrador" onclick="confirmarBorrar(' + id + ')"><i class="bi bi-trash3"></i></button>';
                            }
                            
                            html += '</div>';
                            return html;
                        }
                    }
                ],
                order: [], // No ordenar inicialmente, respetar orden del backend
                language: {
                    "decimal": "",
                    "emptyTable": "No hay datos disponibles en la tabla",
                    "info": "Mostrando _START_ a _END_ de _TOTAL_ registros",
                    "infoEmpty": "Mostrando 0 a 0 de 0 registros",
                    "infoFiltered": "(filtrado de _MAX_ registros totales)",
                    "lengthMenu": "Mostrar _MENU_ registros",
                    "loadingRecords": "Cargando...",
                    "processing": "Procesando...",
                    "search": "Buscar:",
                    "zeroRecords": "No se encontraron registros coincidentes",
                    "paginate": {
                        "first": "Primero",
                        "last": "Último",
                        "next": "Siguiente",
                        "previous": "Anterior"
                    }
                }
            });

            // Lógica para el filtro personalizado de estados
            $('#filtroEstado').on('change', function() {
                let val = $.fn.dataTable.util.escapeRegex($(this).val());
                table.column(7).search(val ? '^' + val + '$' : '', true, false).draw();
            });
        });
        
        function confirmarBorrar(id) {
            Swal.fire({
                title: '¿Eliminar borrador?',
                html: '<p class="text-muted mb-0">Esta acción <strong>no se puede deshacer</strong>.<br>¿Está seguro de que desea eliminar esta cuenta de cobro?</p>',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#dc3545',
                cancelButtonColor: '#6c757d',
                confirmButtonText: '<i class="bi bi-trash3"></i> Sí, eliminar',
                cancelButtonText: 'Cancelar',
                reverseButtons: true
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = 'informes?action=delete&id=' + id;
                }
            });
        }

        function abrirModalRevision(idInforme, estadoActual, tipoRolModal) {
            let opcionesHTML = '';
            let opcionesRevisores = `<option value="">-- Seleccione un Revisor --</option>
                <c:forEach var="r" items="${revisoresList}">
                    <option value="${r.id}">${r.nombreCompleto}</option>
                </c:forEach>`;
            
            // Compatibilidad si llega booleano legado
            if (typeof tipoRolModal === 'boolean') {
                if (tipoRolModal) {
                    tipoRolModal = (estadoActual === 'RADICADA') ? 'contratacion_sin_revisor' : 'contratacion_con_revisor';
                } else {
                    tipoRolModal = 'revisor';
                }
            }

            if (tipoRolModal === 'supervisor') {
                // El Supervisor da la Aprobación Definitiva para impresión
                opcionesHTML = `
                    <option value="APROBADA PARA IMPRESION">Aprobación Definitiva (Lista para imprimir)</option>
                    <option value="DEVUELTA">Devolver al Contratista (Requiere corrección)</option>
                `;
            } else if (tipoRolModal === 'contratacion_con_revisor') {
                // Contratación revisa cuenta que viene de un Revisor y la pasa al Supervisor
                opcionesHTML = `
                    <option value="EN REVISION SUPERVISOR">Dar Visto Bueno (Pasar a Supervisión)</option>
                    <option value="DEVUELTA">Devolver al Contratista (Requiere corrección)</option>
                `;
            } else if (tipoRolModal === 'contratacion_sin_revisor') {
                // Contratación revisa cuenta radicada SIN revisor (revisa primero y puede pasar a revisor, mandar a imprimir o al supervisor)
                opcionesHTML = `
                    <option value="VISTO BUENO CONTRATACION">Dar Visto Bueno Previo (Pasar a Revisor)</option>
                    <option value="APROBADA PARA IMPRESION">Mandar a Imprimir (Aprobación Definitiva)</option>
                    <option value="EN REVISION SUPERVISOR">Mandar al Supervisor (Pasar a Supervisión)</option>
                    <option value="DEVUELTA">Devolver al Contratista (Requiere corrección)</option>
                `;
            } else if (tipoRolModal === 'revisor_post_contratacion' || (tipoRolModal === 'revisor' && estadoActual === 'VISTO BUENO CONTRATACION')) {
                // Revisor en flujo Sin Revisor: Contratación revisó primero y ahora el Revisor decide mandar a supervisor o visto bueno para imprimir
                opcionesHTML = `
                    <option value="EN REVISION SUPERVISOR">Mandar al Supervisor (Pasar a Supervisión)</option>
                    <option value="APROBADA PARA IMPRESION">Dar Visto Bueno para Imprimir (Aprobación Definitiva)</option>
                    <option value="DEVUELTA">Devolver al Contratista (Requiere corrección)</option>
                `;
            } else {
                // Revisor inicial (flujo normal con revisor seleccionado desde el inicio):
                opcionesHTML = `
                    <option value="EN REVISION CONTRATACION">Dar Visto Bueno (Pasar a Contratación)</option>
                    <option value="DEVUELTA">Devolver al Contratista (Requiere corrección)</option>
                `;
            }

            Swal.fire({
                title: 'Revisión de Cuenta de Cobro',
                html: `
                    <p class="text-muted">Seleccione la acción a tomar para esta cuenta:</p>
                    <div class="mb-3">
                        <select id="swal-accion" class="form-select">
                            ` + opcionesHTML + `
                        </select>
                    </div>
                    <div class="mb-3" id="div-revisor" style="display: none;">
                        <label class="form-label text-start w-100 fw-bold text-primary">Seleccione el Revisor:</label>
                        <select id="swal-revisor" class="form-select">
                            ` + opcionesRevisores + `
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label text-start w-100">Observaciones (Obligatorio si devuelve):</label>
                        <textarea id="swal-obs" class="form-control" rows="3" placeholder="Escriba aquí el motivo o sugerencias..."></textarea>
                    </div>
                `,
                showCancelButton: true,
                confirmButtonText: 'Aplicar',
                cancelButtonText: 'Cancelar',
                didOpen: () => {
                    const accionSelect = document.getElementById('swal-accion');
                    const divRevisor = document.getElementById('div-revisor');
                    
                    accionSelect.addEventListener('change', () => {
                        if (accionSelect.value === 'VISTO BUENO CONTRATACION') {
                            divRevisor.style.display = 'block';
                        } else {
                            divRevisor.style.display = 'none';
                        }
                    });
                    accionSelect.dispatchEvent(new Event('change'));
                },
                preConfirm: () => {
                    const accion = document.getElementById('swal-accion').value;
                    const obs = document.getElementById('swal-obs').value;
                    const revisorId = document.getElementById('swal-revisor') ? document.getElementById('swal-revisor').value : '';
                    
                    if (accion === 'DEVUELTA' && !obs.trim()) {
                        Swal.showValidationMessage('Debe escribir una observación para poder devolverla.');
                        return false;
                    }
                    if (accion === 'VISTO BUENO CONTRATACION' && !revisorId) {
                        Swal.showValidationMessage('Debe seleccionar un revisor.');
                        return false;
                    }
                    return { accion: accion, obs: obs, revisorId: revisorId };
                }
            }).then((result) => {
                if (result.isConfirmed) {
                    // Enviar por POST a procesar_revision.jsp
                    const form = document.createElement('form');
                    form.method = 'POST';
                    form.action = 'procesar_revision.jsp';
                    
                    if (result.value.revisorId) {
                        const inputRevisor = document.createElement('input');
                        inputRevisor.type = 'hidden';
                        inputRevisor.name = 'revisor_id';
                        inputRevisor.value = result.value.revisorId;
                        form.appendChild(inputRevisor);
                    }
                    
                    const inputId = document.createElement('input');
                    inputId.type = 'hidden';
                    inputId.name = 'id_informe';
                    inputId.value = idInforme;
                    
                    const inputAccion = document.createElement('input');
                    inputAccion.type = 'hidden';
                    inputAccion.name = 'accion';
                    inputAccion.value = result.value.accion;
                    
                    const inputObs = document.createElement('input');
                    inputObs.type = 'hidden';
                    inputObs.name = 'observacion';
                    inputObs.value = result.value.obs;
                    
                    const inputModo = document.createElement('input');
                    inputModo.type = 'hidden';
                    inputModo.name = 'modo';
                    inputModo.value = '${param.modo}';
                    
                    form.appendChild(inputId);
                    form.appendChild(inputAccion);
                    form.appendChild(inputObs);
                    form.appendChild(inputModo);
                    document.body.appendChild(form);
                    form.submit();
                }
            });
        }
    </script>
</body>
</html>

