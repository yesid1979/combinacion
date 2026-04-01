<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Carga Masiva - Gestión Integral</title>
            <!-- Bootstrap 4.x/5.x common fonts -->
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <!-- Bootstrap Icons -->
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
            <style>
                /* Custom styles if needed */
            </style>
        <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
    </head>

        <body class="bg-light d-flex flex-column min-vh-100">

            <jsp:include page="inc/navbar.jsp" />

            <div class="container mt-4 mb-5">
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

                            <div class="row row-cols-1 row-cols-md-5 g-3 mt-2">
                                <!-- Contratos -->
                                <div class="col border-end">
                                    <h6 class="fw-bold text-dark">
                                        <i class="bi bi-file-earmark-text me-1"></i>Contratos:
                                    </h6>
                                    <small class="d-block">• No. de Contrato</small>
                                    <small class="d-block">• Objeto Contractual</small>
                                    <small class="d-block">• Valor, Fechas</small>
                                    <small class="d-block">• CDP, RP</small>
                                </div>

                                <!-- Ordenadores -->
                                <div class="col border-end">
                                    <h6 class="fw-bold text-primary">
                                        <i class="bi bi-bank me-1"></i>Ordenadores:
                                    </h6>
                                    <small class="d-block">• Nombre completo</small>
                                    <small class="d-block">• Cedula, Cargo</small>
                                    <small class="d-block">• Organismo, Decreto</small>
                                </div>

                                <!-- Contratistas -->
                                <div class="col border-end">
                                    <h6 class="fw-bold text-success">
                                        <i class="bi bi-people me-1"></i>Contratistas:
                                    </h6>
                                    <small class="d-block">• Nombre, Cedula</small>
                                    <small class="d-block">• Correo, Formacion</small>
                                    <small class="d-block">• Tarjeta Profes.</small>
                                </div>

                                <!-- Supervisores -->
                                <div class="col border-end">
                                    <h6 class="fw-bold text-warning">
                                        <i class="bi bi-person-badge me-1"></i>Supervisores:
                                    </h6>
                                    <small class="d-block">• Nombre completo</small>
                                    <small class="d-block">• Cedula, Cargo</small>
                                </div>

                                <!-- Presupuesto -->
                                <div class="col">
                                    <h6 class="fw-bold text-danger">
                                        <i class="bi bi-currency-dollar me-1"></i>Presupuesto:
                                    </h6>
                                    <small class="d-block">• No. CDP, No. RP</small>
                                    <small class="d-block">• Rubro, Ficha EBI</small>
                                    <small class="d-block">• Valor, Fechas</small>
                                </div>
                            </div>
                        </div>

                        <form id="uploadForm" action="upload" method="POST" enctype="multipart/form-data" class="mt-4">
                            <div class="mb-3">
                                <label for="fileUpload" class="form-label fw-bold">Seleccionar Archivo (Excel o CSV)</label>
                                <input class="form-control form-control-lg" type="file" id="fileUpload" name="file"
                                    accept=".csv, .xlsx, .xls" required>
                            </div>

                            <div class="text-end mt-4">
                                <a href="index.jsp" class="btn btn-outline-secondary px-4 me-2" style="border-radius: 8px;"><i
                                        class="bi bi-x-circle me-2"></i> Cerrar</a>
                                <button type="submit" class="btn btn-primary px-4 fw-bold" style="border-radius: 8px;"><i
                                        class="bi bi-cloud-upload me-2"></i> Cargar archivo</button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Debug log if present -->
                <c:if test="${not empty sessionScope.debug}">
                    <div class="alert alert-secondary mt-4 p-4 border-0 shadow-sm" style="border-radius: 12px;">
                        <h6 class="alert-heading fw-bold mb-3"><i class="bi bi-terminal me-2"></i>Log de Procesamiento (Debug)</h6>
                        <pre class="mb-0 overflow-auto bg-white p-3 rounded" style="max-height: 500px; font-size: 0.85rem; font-family: 'Courier New', Courier, monospace; border: 1px solid #dee2e6;">${sessionScope.debug}</pre>
                        <% session.removeAttribute("debug"); %>
                    </div>
                </c:if>

                <!-- Status messages -->
                <c:if test="${not empty sessionScope.message}">
                    <div class="alert alert-success mt-4 alert-dismissible fade show shadow-sm" role="alert">
                        <i class="bi bi-check-circle-fill me-2"></i>${sessionScope.message}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        <% session.removeAttribute("message"); %>
                    </div>
                </c:if>

                <c:if test="${not empty sessionScope.error}">
                    <div class="alert alert-danger mt-4 alert-dismissible fade show shadow-sm" role="alert">
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>${sessionScope.error}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        <% session.removeAttribute("error"); %>
                    </div>
                </c:if>
            </div>

            <jsp:include page="inc/footer.jsp" />

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        </body>

        </html>