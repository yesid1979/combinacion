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
                    <div class="text-end">
                        <a href="contratos" class="btn btn-outline-secondary px-4 fw-bold shadow-sm">
                            <i class="bi bi-search me-2"></i>Seleccionar contrato para registrar la cuenta de cobro
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
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
                            <th class="text-center">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
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
                                <td><fmt:formatDate value="${info.fechaCreacion}" pattern="dd/MM/yyyy hh:mm a"/></td>
                                <td><fmt:formatNumber value="${info.valorCuotaPagar}" type="currency" currencySymbol="$ " maxFractionDigits="0" minFractionDigits="0"/></td>
                                <td class="text-center">
                                    <a href="informes?action=view&id=${info.id}" class="btn btn-sm btn-outline-info" title="Ver Detalle">
                                        <i class="bi bi-eye"></i>
                                    </a>
                                    <a href="informes?action=edit&id=${info.id}" class="btn btn-sm btn-outline-primary ms-1" title="Editar Informe">
                                        <i class="bi bi-pencil-square"></i>
                                    </a>
                                    <a href="informes?action=download&id=${info.id}" class="btn btn-sm btn-outline-success ms-1" title="Descargar DOCX">
                                        <i class="bi bi-file-earmark-word"></i>
                                    </a>
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
    <script>
        $(document).ready(function() {
            $('#informesTable').DataTable({
                responsive: true,
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
        });
    </script>
</body>
</html>
