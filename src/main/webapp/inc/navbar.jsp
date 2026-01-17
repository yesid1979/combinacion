<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary sticky-top shadow-sm">
        <div class="container-fluid px-4">
            <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/index.jsp">
                <i class="bi bi-layers-fill me-2"></i>Gestión Contratos
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/index.jsp">Inicio</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/contratos">Contratos</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/contratistas">Contratistas</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/supervisores">Supervisores</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/ordenadores">Ordenadores</a>
                    </li>
                </ul>
                <div class="d-flex ms-3">
                    <a class="btn btn-outline-light btn-sm" href="#">
                        <i class="bi bi-person-circle"></i> Admin
                    </a>
                </div>
            </div>
        </div>
    </nav>