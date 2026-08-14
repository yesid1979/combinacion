<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.combinacion.dao.UsuarioDAO" %>
<%@ page import="com.combinacion.models.Usuario" %>
<%@ page import="java.util.List" %>
<%
    List<Usuario> revisoresList = new UsuarioDAO().listarRevisores();
    request.setAttribute("revisoresList", revisoresList);
%>
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
                            <button class="btn btn-success px-4 fw-bold shadow-sm dropdown-toggle w-100 w-md-auto" type="button" id="dropdownNuevoInforme" data-bs-toggle="dropdown" aria-expanded="false">
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
                        <a href="informes?action=new&contrato_id=${contrato.id}" class="btn btn-success px-4 fw-bold shadow-sm w-100 w-md-auto">
                            <i class="bi bi-plus-circle me-2"></i>Nuevo Informe
                        </a>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${esAdminGlobal}">
                            <div class="text-md-end text-start">
                                <a href="contratos" class="btn btn-outline-secondary px-4 fw-bold shadow-sm w-100 w-md-auto">
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
            <% request.getSession().removeAttribute("successMessage"); %>
        </c:if>

        <c:if test="${not empty sessionScope.errorMessage}">
            <div class="alert alert-danger shadow-sm border-0 mb-4">
                <i class="bi bi-x-circle-fill me-2"></i> ${sessionScope.errorMessage}
            </div>
            <% request.getSession().removeAttribute("errorMessage"); %>
        </c:if>

        <div class="card border-0 shadow-sm">
            <div class="card-body">
                <div class="d-flex justify-content-end mb-3">
                    <div class="d-flex align-items-center">
                        <label for="filtroEstado" class="me-2 fw-bold text-secondary mb-0"><i class="bi bi-funnel"></i> Filtrar por Estado:</label>
                        <select id="filtroEstado" class="form-select w-auto shadow-sm border-primary">
                            <option value="">Todos los estados</option>
                            <option value="RADICADA">Radicadas (Pendientes)</option>
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
                            <th>Tipo</th>
                            <th>Cuota</th>
                            <th>Fecha Registro</th>
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
                        "render": function(data) {
                            let badgeClass = (data === 'FINAL') ? 'bg-info' : 'bg-secondary';
                            return '<span class="badge ' + badgeClass + '">' + data + '</span>';
                        }
                    },
                    { "data": "cuota" },
                    { "data": "fechaRegistro" },
                    { "data": "valorCuota" },
                    { 
                        "data": "estado",
                        "render": function(data) {
                            if (data === 'BORRADOR') return '<span class="badge bg-secondary">BORRADOR</span>';
                            if (data === 'RADICADA') return '<span class="badge bg-primary">RADICADA</span>';
                            if (data === 'EN REVISION') return '<span class="badge bg-warning text-dark">EN REVISIÓN</span>';
                            if (data === 'EN REVISION FINAL') return '<span class="badge bg-info text-dark">EN REVISIÓN FINAL</span>';
                            if (data === 'APROBADA PARA IMPRESION') return '<span class="badge bg-success">APROBADA (LISTA PARA FIRMA)</span>';
                            if (data === 'DEVUELTA') return '<span class="badge bg-danger">DEVUELTA</span>';
                            return '<span class="badge bg-secondary">' + data + '</span>';
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
                            html += '<div class="d-flex flex-wrap gap-1">';
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
                            
                            // Botón Revisar
                            let mostrarBtnRevisar = false;
                            let esModalAdmin = false;
                            
                            if (row.idRevisorAsignado == row.usuarioActualId && (estado === 'RADICADA' || estado === 'VISTO BUENO CONTRATACION')) {
                                mostrarBtnRevisar = true;
                                esModalAdmin = false;
                            } else if (esAdminCuentas) {
                                // Contratación interviene si:
                                // 1. Ya pasó por el supervisor (EN REVISION FINAL)
                                // 2. O si se radicó (RADICADA) pero NO tiene un revisor asignado (idRevisorAsignado es nulo, vacío o 0)
                                let sinRevisor = (!row.idRevisorAsignado || row.idRevisorAsignado === 0 || row.idRevisorAsignado === '0');
                                if (estado === 'EN REVISION FINAL' || (estado === 'RADICADA' && sinRevisor)) {
                                    mostrarBtnRevisar = true;
                                    esModalAdmin = true;
                                }
                            }
                            
                            if (mostrarBtnRevisar) {
                                html += '<button type="button" class="btn btn-sm btn-outline-primary" title="Gestionar Revisión" onclick="abrirModalRevision(' + id + ', \'' + estado + '\', ' + esModalAdmin + ')"><i class="bi bi-check2-square"></i> Revisar</button>';
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

        function abrirModalRevision(idInforme, estadoActual, esAdminCuentas) {
            let opcionesHTML = '';
            let opcionesRevisores = `<option value="">-- Seleccione un Revisor --</option>
                <c:forEach var="r" items="${revisoresList}">
                    <option value="${r.id}">${r.nombreCompleto}</option>
                </c:forEach>`;
            
            if (esAdminCuentas) {
                if (estadoActual === 'RADICADA') {
                    opcionesHTML = `
                        <option value="VISTO BUENO CONTRATACION">Dar Visto Bueno Previo (Pasar a Revisor)</option>
                        <option value="APROBADA PARA IMPRESION">Aprobación Definitiva (Lista para imprimir)</option>
                        <option value="DEVUELTA">Devolver al Contratista (Requiere corrección)</option>
                    `;
                } else {
                    opcionesHTML = `
                        <option value="APROBADA PARA IMPRESION">Aprobación Definitiva (Lista para imprimir)</option>
                        <option value="DEVUELTA">Devolver al Contratista (Requiere corrección)</option>
                    `;
                }
            } else {
                // Si es solo Revisor Básico, solo da el Visto Bueno previo
                opcionesHTML = `
                    <option value="EN REVISION FINAL">Dar Visto Bueno (Pasar a Contratación)</option>
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

