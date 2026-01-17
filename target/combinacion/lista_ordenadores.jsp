<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Lista de Ordenadores del Gasto</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
        </head>

        <body class="bg-light">
            <jsp:include page="inc/navbar.jsp" />

            <div class="container mt-5">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2>Gestión de Ordenadores del Gasto</h2>
                    <a href="ordenadores?action=new" class="btn btn-danger text-white"><i class="bi bi-wallet2"></i>
                        Nuevo Ordenador</a>
                </div>

                <div class="card shadow-sm border-0">
                    <div class="card-body p-0">
                        <table class="table table-hover table-striped mb-0">
                            <thead class="table-dark">
                                <tr>
                                    <th>Nombre Ordenador</th>
                                    <th class="text-end">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="o" items="${listOrdenadores}">
                                    <tr>
                                        <td>${o.nombreOrdenador}</td>
                                        <td class="text-end">
                                            <a href="#" class="btn btn-sm btn-outline-primary"><i
                                                    class="bi bi-pencil"></i></a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty listOrdenadores}">
                                    <tr>
                                        <td colspan="2" class="text-center py-4 text-muted">No hay ordenadores
                                            registrados.</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        </body>

        </html>