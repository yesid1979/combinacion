<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Panel Principal - Gestión de Contratos</title>
            <!-- Bootstrap 5 CSS -->
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
            <style>
                .card-menu {
                    transition: transform 0.2s;
                    cursor: pointer;
                }

                .card-menu:hover {
                    transform: translateY(-5px);
                    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
                }

                .icon-box {
                    width: 60px;
                    height: 60px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    border-radius: 12px;
                    margin-bottom: 15px;
                }
            </style>
        </head>

        <body class="bg-light d-flex flex-column min-vh-100">

            <jsp:include page="inc/navbar.jsp" />

            <div class="container mt-4 mb-5">
                <h2 class="mb-4 text-center fw-bold text-primary">Panel de Administración</h2>
                <div class="row g-4">

                    <!-- Carga Masiva -->
                    <div class="col-md-6 col-lg-3">
                        <div class="card h-100 border-0 shadow-sm card-menu" onclick="location.href='carga_masiva.jsp'">
                            <div class="card-body text-center">
                                <div class="icon-box bg-secondary bg-opacity-10 text-secondary mx-auto">
                                    <i class="bi bi-cloud-upload fs-2"></i>
                                </div>
                                <h5 class="card-title fw-bold">Carga masiva</h5>
                                <p class="card-text text-muted small">Importar datos desde CSV/Excel.</p>
                            </div>
                        </div>
                    </div>

                    <!-- Contratos -->
                    <div class="col-md-6 col-lg-3">
                        <div class="card h-100 border-0 shadow-sm card-menu" onclick="location.href='contratos'">
                            <div class="card-body text-center">
                                <div class="icon-box bg-primary bg-opacity-10 text-primary mx-auto">
                                    <i class="bi bi-file-earmark-text fs-2"></i>
                                </div>
                                <h5 class="card-title fw-bold">Contratos</h5>
                                <p class="card-text text-muted small">Gestionar contratos, pagos y fechas.</p>
                            </div>
                        </div>
                    </div>

                    <!-- Contratistas -->
                    <div class="col-md-6 col-lg-3">
                        <div class="card h-100 border-0 shadow-sm card-menu" onclick="location.href='contratistas'">
                            <div class="card-body text-center">
                                <div class="icon-box bg-success bg-opacity-10 text-success mx-auto">
                                    <i class="bi bi-people fs-2"></i>
                                </div>
                                <h5 class="card-title fw-bold">Contratistas</h5>
                                <p class="card-text text-muted small">Registro de profesionales y sus datos.</p>
                            </div>
                        </div>
                    </div>

                    <!-- Supervisores -->
                    <div class="col-md-6 col-lg-3">
                        <div class="card h-100 border-0 shadow-sm card-menu" onclick="location.href='supervisores'">
                            <div class="card-body text-center">
                                <div class="icon-box bg-warning bg-opacity-10 text-warning mx-auto">
                                    <i class="bi bi-person-check fs-2"></i>
                                </div>
                                <h5 class="card-title fw-bold">Supervisores</h5>
                                <p class="card-text text-muted small">Gestionar supervisores e interventores.</p>
                            </div>
                        </div>
                    </div>

                    <!-- Ordenadores -->
                    <div class="col-md-6 col-lg-3">
                        <div class="card h-100 border-0 shadow-sm card-menu" onclick="location.href='ordenadores'">
                            <div class="card-body text-center">
                                <div class="icon-box bg-danger bg-opacity-10 text-danger mx-auto">
                                    <i class="bi bi-wallet2 fs-2"></i>
                                </div>
                                <h5 class="card-title fw-bold">Ordenadores gasto</h5>
                                <p class="card-text text-muted small">Gestión de ordenadores del gasto.</p>
                            </div>
                        </div>
                    </div>

                    <!-- Presupuesto (Bonus) -->
                    <div class="col-md-6 col-lg-3">
                        <div class="card h-100 border-0 shadow-sm card-menu" onclick="location.href='presupuesto'">
                            <div class="card-body text-center">
                                <div class="icon-box bg-info bg-opacity-10 text-info mx-auto">
                                    <i class="bi bi-graph-up fs-2"></i>
                                </div>
                                <h5 class="card-title fw-bold">Presupuesto</h5>
                                <p class="card-text text-muted small">Detalles presupuestales y CDPs.</p>
                            </div>
                        </div>
                    </div>

                    <!-- Combinación -->
                    <div class="col-md-6 col-lg-3">
                        <div class="card h-100 border-0 shadow-sm card-menu" onclick="location.href='combinacion'">
                            <div class="card-body text-center">
                                <div class="icon-box bg-purple bg-opacity-10 text-purple mx-auto"
                                    style="background-color: rgba(139, 92, 246, 0.1) !important; color: #8b5cf6 !important;">
                                    <i class="bi bi-file-earmark-word fs-2"></i>
                                </div>
                                <h5 class="card-title fw-bold">Combinación</h5>
                                <p class="card-text text-muted small">Generación de documentos contractuales.</p>
                            </div>
                        </div>
                    </div>



                </div>
            </div>

            <!-- Footer if you want -->
            <!-- Footer -->
            <jsp:include page="inc/footer.jsp" />

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        </body>

        </html>