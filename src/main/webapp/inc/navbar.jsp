<%@page contentType="text/html" pageEncoding="UTF-8" %>


    <!-- Navbar Principal -->
    <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
        <div class="container">
            <a class="navbar-brand d-flex align-items-center me-4" href="${pageContext.request.contextPath}/index.jsp">
                <img src="${pageContext.request.contextPath}/assets/img/logo_alcaldia.png"
                    alt="Alcaldía de Santiago de Cali" style="height: 60px; width: auto;">
            </a>

            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse" id="navbarNav">
                <!-- Changed ms-auto to ms-3 to stick menu close to the image -->
                <ul class="navbar-nav ms-3 align-items-center">
                    <li class="nav-item mx-2">
                        <a class="nav-link small fw-bold" style="color: #004884;"
                            href="${pageContext.request.contextPath}/index.jsp">
                            <i class="bi bi-house-door me-1"></i>Inicio
                        </a>
                    </li>
                    <li class="nav-item mx-2">
                        <a class="nav-link small fw-bold" style="color: #004884;"
                            href="${pageContext.request.contextPath}/contratos">
                            <i class="bi bi-file-text me-1"></i>Contratos
                        </a>
                    </li>
                    <!-- Dropdown for Master Data -->
                    <li class="nav-item dropdown mx-2">
                        <a class="nav-link dropdown-toggle small fw-bold" href="#" id="navbarDropdownMaster"
                            role="button" data-bs-toggle="dropdown" aria-expanded="false" style="color: #004884;">
                            <i class="bi bi-database me-1"></i>Datos maestros
                        </a>
                        <ul class="dropdown-menu border-0 shadow" aria-labelledby="navbarDropdownMaster">
                            <li><a class="dropdown-item small" href="${pageContext.request.contextPath}/contratistas"><i
                                        class="bi bi-people me-2"></i>Contratistas</a></li>
                            <li><a class="dropdown-item small" href="${pageContext.request.contextPath}/supervisores"><i
                                        class="bi bi-person-badge me-2"></i>Supervisores</a></li>
                            <li><a class="dropdown-item small" href="${pageContext.request.contextPath}/ordenadores"><i
                                        class="bi bi-briefcase me-2"></i>Ordenadores</a></li>
                        </ul>
                    </li>
                    <li class="nav-item mx-2">
                        <a class="nav-link small fw-bold" style="color: #004884;"
                            href="${pageContext.request.contextPath}/combinacion">
                            <i class="bi bi-files me-1"></i>Combinación
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>