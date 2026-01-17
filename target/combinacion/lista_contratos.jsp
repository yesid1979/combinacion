<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="es">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Lista de Contratos - Gestión de Prestadores</title>
                <!-- Bootstrap 5 CSS -->
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                <!-- DataTables CSS for advanced table features -->
                <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
            </head>

            <body class="bg-light">

                <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
                    <div class="container-fluid">
                        <a class="navbar-brand" href="#">Gestión Contratos</a>
                        <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                            data-bs-target="#navbarNav">
                            <span class="navbar-toggler-icon"></span>
                        </button>
                        <div class="collapse navbar-collapse" id="navbarNav">
                            <ul class="navbar-nav">
                                <li class="nav-item">
                                    <a class="nav-link active" href="contratos">Listado</a>
                                </li>
                                <li class="nav-item">
                                    <a class="nav-link" href="contratos?action=new">Nuevo Contrato</a>
                                </li>
                            </ul>
                        </div>
                    </div>
                </nav>

                <div class="container mt-4">
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h2>Contratos Registrados</h2>
                        <a href="contratos?action=new" class="btn btn-success"><i class="bi bi-plus-lg"></i> Nuevo
                            Contrato</a>
                    </div>

                    <div class="card shadow-sm">
                        <div class="card-body">
                            <div class="table-responsive">
                                <table id="contratosTable" class="table table-striped table-hover">
                                    <thead class="table-dark">
                                        <tr>
                                            <th># Contrato</th>
                                            <th>Contratista</th>
                                            <th>Objeto</th>
                                            <th>Valor Total</th>
                                            <th>Fecha Inicio</th>
                                            <th>Fecha Fin</th>
                                            <th>Estado</th>
                                            <th>Acciones</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="c" items="${listContratos}">
                                            <tr>
                                                <td>${c.numeroContrato}</td>
                                                <td>${c.contratistaNombre}</td>
                                                <td>
                                                    <span class="d-inline-block text-truncate"
                                                        style="max-width: 250px;">
                                                        ${c.objeto}
                                                    </span>
                                                </td>
                                                <td>
                                                    <fmt:formatNumber value="${c.valorTotalNumeros}" type="currency"
                                                        currencySymbol="$" />
                                                </td>
                                                <td>
                                                    <fmt:formatDate value="${c.fechaInicio}" pattern="dd/MM/yyyy" />
                                                </td>
                                                <td>
                                                    <fmt:formatDate value="${c.fechaTerminacion}"
                                                        pattern="dd/MM/yyyy" />
                                                </td>
                                                <td>
                                                    <span
                                                        class="badge ${c.estado == 'Activo' ? 'bg-success' : 'bg-secondary'}">
                                                        ${c.estado}
                                                    </span>
                                                </td>
                                                <td>
                                                    <div class="btn-group" role="group">
                                                        <a href="#" class="btn btn-sm btn-primary">Ver</a>
                                                        <a href="#" class="btn btn-sm btn-outline-primary">Editar</a>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty listContratos}">
                                            <tr>
                                                <td colspan="8" class="text-center py-4">
                                                    <div class="text-muted">No se encontraron contratos registrados.
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- jQuery and Bootstrap JS -->
                <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
                <!-- DataTables JS -->
                <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
                <script src="https://cdn.datatables.net/1.13.4/js/dataTables.bootstrap5.min.js"></script>

                <script>
                    $(document).ready(function () {
                        $('#contratosTable').DataTable({
                            language: {
                                url: "//cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json"
                            },
                            responsive: true
                        });
                    });
                </script>
            </body>

            </html>