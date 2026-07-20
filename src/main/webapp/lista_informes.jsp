<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Historial de Informes - Gestión de Prestadores</title>
    <link rel="icon" href="favicon.ico" type="image/x-icon">
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
</head>
<body class="bg-white d-flex flex-column min-vh-100">

    <jsp:include page="inc/navbar.jsp" />

    <div class="container-fluid mt-4 flex-grow-1 px-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
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
                            <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0" aria-labelledby="dropdownNuevoInforme">
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
                            <div class="text-end">
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
            <% request.getSession().removeAttribute("successMessage"); %>
        </c:if>

        <div class="card border">
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
                    <thead>
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
                        
                        <c:forEach items="${listaInformes}" var="info">
                            <tr>
                                <td class="fw-bold text-primary">${info.contrato.numeroContrato}</td>
                                <td>${info.contrato.contratistaNombre}</td>
                                <td>${info.periodoInforme}</td>
                                <td>
                                    <span class="badge ${info.tipoInforme == 'FINAL' ? 'bg-info' : 'bg-secondary'}">
                                        ${info.tipoInforme}
                                    </span>
                                </td>
                                <td>${info.numeroCuota}</td>
                                <td data-sort="${info.fechaCreacion.time}"><fmt:formatDate value="${info.fechaCreacion}" pattern="dd/MM/yyyy hh:mm a"/></td>
                                <td><fmt:formatNumber value="${info.valorCuotaPagar}" type="currency" currencySymbol="$ " maxFractionDigits="0" minFractionDigits="0"/></td>
                                <td data-search="${empty info.estadoRadicacion ? 'BORRADOR' : info.estadoRadicacion}">
                                    <c:choose>
                                        <c:when test="${info.estadoRadicacion == 'BORRADOR'}">
                                            <span class="badge bg-secondary">BORRADOR</span>
                                        </c:when>
                                        <c:when test="${info.estadoRadicacion == 'RADICADA'}">
                                            <span class="badge bg-primary">RADICADA</span>
                                        </c:when>
                                        <c:when test="${info.estadoRadicacion == 'EN REVISION'}">
                                            <span class="badge bg-warning text-dark">EN REVISIÓN</span>
                                        </c:when>
                                        <c:when test="${info.estadoRadicacion == 'EN REVISION FINAL'}">
                                            <span class="badge bg-info text-dark">EN REVISIÓN FINAL</span>
                                        </c:when>
                                        <c:when test="${info.estadoRadicacion == 'APROBADA PARA IMPRESION'}">
                                            <span class="badge bg-success">APROBADA (LISTA PARA FIRMA)</span>
                                        </c:when>
                                        <c:when test="${info.estadoRadicacion == 'DEVUELTA'}">
                                            <span class="badge bg-danger">DEVUELTA</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">${empty info.estadoRadicacion ? 'BORRADOR' : info.estadoRadicacion}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">
                                    <a href="informes?action=view&id=${info.id}&modo=${param.modo}" class="btn btn-sm btn-outline-info" title="Ver Detalle">
                                        <i class="bi bi-eye"></i>
                                    </a>
                                    <c:if test="${((empty info.estadoRadicacion || info.estadoRadicacion == 'BORRADOR' || info.estadoRadicacion == 'DEVUELTA') && param.modo == 'mis_cuentas') || esAdminCuentas}">
                                        <a href="informes?action=edit&id=${info.id}&modo=${param.modo}" class="btn btn-sm btn-outline-primary ms-1" title="Editar Informe">
                                            <i class="bi bi-pencil-square"></i>
                                        </a>
                                    </c:if>
                                    <c:choose>
                                        <c:when test="${info.estadoRadicacion == 'APROBADA PARA IMPRESION'}">
                                            <a href="informes?action=download&id=${info.id}" class="btn btn-sm btn-outline-success ms-1" title="Descargar Paquete Final">
                                                <i class="bi bi-file-earmark-arrow-down"></i>
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <button type="button" class="btn btn-sm btn-outline-secondary ms-1" title="La descarga se habilitará cuando la cuenta sea aprobada" disabled>
                                                <i class="bi bi-lock"></i>
                                            </button>
                                        </c:otherwise>
                                    </c:choose>
                                    
                                    <%-- Lógica de visualización del botón Revisar según el rol y estado --%>
                                    <c:set var="mostrarBtnRevisar" value="false" />
                                    <c:set var="esModalAdmin" value="false" />
                                    
                                    <c:choose>
                                        <%-- 1. Si el usuario logueado es el revisor asignado, siempre actúa como revisor básico primero en la etapa RADICADA --%>
                                        <c:when test="${info.idRevisorAsignado == sessionScope.usuario.id && info.estadoRadicacion == 'RADICADA'}">
                                            <c:set var="mostrarBtnRevisar" value="true" />
                                            <c:set var="esModalAdmin" value="false" />
                                        </c:when>
                                        
                                        <%-- 2. Lógica para Contratación (Control Global) --%>
                                        <c:when test="${esAdminCuentas}">
                                            <%-- Contratación entra como Admin en RADICADA solo si NO hay revisor asignado (pasó directo) --%>
                                            <c:if test="${(empty info.idRevisorAsignado || info.idRevisorAsignado == 0) && info.estadoRadicacion == 'RADICADA'}">
                                                <c:set var="mostrarBtnRevisar" value="true" />
                                                <c:set var="esModalAdmin" value="true" />
                                            </c:if>
                                            <%-- Contratación entra como Admin cuando ya está en REVISION FINAL --%>
                                            <c:if test="${info.estadoRadicacion == 'EN REVISION FINAL'}">
                                                <c:set var="mostrarBtnRevisar" value="true" />
                                                <c:set var="esModalAdmin" value="true" />
                                            </c:if>
                                        </c:when>
                                    </c:choose>
                                    
                                    <c:if test="${mostrarBtnRevisar}">
                                        <button type="button" class="btn btn-sm btn-outline-primary ms-1" title="Gestionar Revisión" onclick="abrirModalRevision(${info.id}, '${info.estadoRadicacion}', ${esModalAdmin})">
                                            <i class="bi bi-check2-square"></i> Revisar
                                        </button>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
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
                responsive: true,
                order: [], // No ordenar inicialmente, respetar orden del backend
                language: {
                    "decimal": "",
                    "emptyTable": "No hay datos disponibles en la tabla",
                    "info": "Mostrando _START_ a _END_ de _TOTAL_ entradas",
                    "infoEmpty": "Mostrando 0 a 0 de 0 entradas",
                    "infoFiltered": "(filtrado de _MAX_ entradas totales)",
                    "lengthMenu": "Mostrar _MENU_ entradas",
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
                // Filtramos la columna 7 (Estado) buscando la coincidencia exacta
                let val = $.fn.dataTable.util.escapeRegex($(this).val());
                table.column(7).search(val ? '^' + val + '$' : '', true, false).draw();
            });
        });
        
        function abrirModalRevision(idInforme, estadoActual, esAdminCuentas) {
            let opcionesHTML = '';
            
            if (esAdminCuentas) {
                // Si es Contratación / Admin, tiene el poder de Aprobación Final
                opcionesHTML = `
                    <option value="APROBADA PARA IMPRESION">Aprobación Definitiva (Lista para imprimir)</option>
                    <option value="DEVUELTA">Devolver al Contratista (Requiere corrección)</option>
                `;
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
                    <div class="mb-3">
                        <label class="form-label text-start w-100">Observaciones (Obligatorio si devuelve):</label>
                        <textarea id="swal-obs" class="form-control" rows="3" placeholder="Escriba aquí el motivo o sugerencias..."></textarea>
                    </div>
                `,
                showCancelButton: true,
                confirmButtonText: 'Aplicar',
                cancelButtonText: 'Cancelar',
                preConfirm: () => {
                    const accion = document.getElementById('swal-accion').value;
                    const obs = document.getElementById('swal-obs').value;
                    if (accion === 'DEVUELTA' && !obs.trim()) {
                        Swal.showValidationMessage('Debe escribir una observación para poder devolverla.');
                        return false;
                    }
                    return { accion: accion, obs: obs };
                }
            }).then((result) => {
                if (result.isConfirmed) {
                    // Enviar por POST a procesar_revision.jsp
                    const form = document.createElement('form');
                    form.method = 'POST';
                    form.action = 'procesar_revision.jsp';
                    
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
