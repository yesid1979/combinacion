<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

    <!-- Navbar Principal Compacto y Equilibrado -->
    <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm py-2">
        <div class="container-fluid px-4">
            <a class="navbar-brand d-flex align-items-center me-4" href="${pageContext.request.contextPath}/index.jsp">
                <img src="${pageContext.request.contextPath}/assets/img/logo_alcaldia.png"
                    alt="Logo" style="height: 50px; width: auto;">
            </a>

            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav gap-1">
                    <li class="nav-item">
                        <a class="nav-link fw-bold px-3 py-2 d-flex align-items-center" 
                           style="color: #004884; font-size: 0.9rem;"
                           href="${pageContext.request.contextPath}/index.jsp">
                            <i class="bi bi-house-door me-2"></i>Inicio
                        </a>
                    </li>

                    <c:if test="${sessionScope.usuario.tienePermiso('CONTRATOS_VER')}">
                    <li class="nav-item">
                        <a class="nav-link fw-bold px-3 py-2 d-flex align-items-center" 
                           style="color: #004884; font-size: 0.9rem;"
                           href="${pageContext.request.contextPath}/contratos">
                            <i class="bi bi-file-earmark-text me-2"></i>Contratos
                        </a>
                    </li>
                    </c:if>
                    
                    <c:if test="${sessionScope.usuario.tienePermiso('CONTRATISTAS_VER') || sessionScope.usuario.tienePermiso('SUPERVISORES_VER') || sessionScope.usuario.tienePermiso('ORDENADORES_VER')}">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle fw-bold px-3 py-2 d-flex align-items-center" 
                           style="color: #004884; font-size: 0.9rem;"
                           href="#" id="masterDataDrop" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-database-fill-add me-2"></i>Datos maestros
                        </a>
                        <ul class="dropdown-menu border-0 shadow-sm mt-3 p-2">
                            <c:if test="${sessionScope.usuario.tienePermiso('CONTRATISTAS_VER')}">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/contratistas"><i class="bi bi-people me-2"></i>Contratistas</a></li>
                            </c:if>
                            <c:if test="${sessionScope.usuario.tienePermiso('SUPERVISORES_VER')}">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/supervisores"><i class="bi bi-person-badge me-2"></i>Supervisores</a></li>
                            </c:if>
                            <c:if test="${sessionScope.usuario.tienePermiso('ORDENADORES_VER')}">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/ordenadores"><i class="bi bi-briefcase me-2"></i>Ordenadores</a></li>
                            </c:if>
                            <c:if test="${sessionScope.usuario.tienePermiso('PRESUPUESTO_VER')}">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/presupuesto"><i class="bi bi-graph-up me-2"></i>Presupuesto</a></li>
                            </c:if>
                        </ul>
                    </li>
                    </c:if>

                    <c:if test="${sessionScope.usuario.tienePermiso('COMBINACION_VER')}">
                    <li class="nav-item">
                        <a class="nav-link fw-bold px-3 py-2 d-flex align-items-center" 
                           style="color: #004884; font-size: 0.9rem;"
                           href="${pageContext.request.contextPath}/combinacion">
                            <i class="bi bi-files me-2"></i>Combinaci&oacute;n
                        </a>
                    </li>
                    </c:if>


                    <c:if test="${sessionScope.usuario.tienePermiso('ADMINISTRACION_VER')}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle fw-bold px-3 py-2 d-flex align-items-center" 
                               style="color: #004884; font-size: 0.9rem;"
                               href="#" id="adminDrop" role="button" data-bs-toggle="dropdown">
                                <i class="bi bi-shield-lock-fill me-2"></i>Administraci&oacute;n
                            </a>
                            <ul class="dropdown-menu border-0 shadow-sm mt-3 p-2">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/admin/usuarios"><i class="bi bi-person-gear me-2"></i>Usuarios</a></li>
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/admin/roles"><i class="bi bi-key-fill me-2"></i>Roles y permisos</a></li>
                            </ul>
                        </li>
                    </c:if>
                </ul>

                <c:if test="${not empty sessionScope.usuario}">
                    <ul class="navbar-nav ms-auto">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle d-flex align-items-center px-3" 
                               href="#" id="userDrop" role="button" data-bs-toggle="dropdown" style="min-height: 50px;">
                                <c:choose>
                                    <c:when test="${not empty sessionScope.usuario.fotoUrl}">
                                        <img src="${pageContext.request.contextPath}/${sessionScope.usuario.fotoUrl}" 
                                             alt="User" class="rounded-circle me-2 shadow-sm" style="width: 38px; height: 38px; object-fit: cover; object-position: 50% 15%; border: 2px solid #fff; display: block;">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="https://ui-avatars.com/api/?name=${sessionScope.nombreUsuario}&background=0D8ABC&color=fff&size=38" 
                                             alt="User" class="rounded-circle me-2 shadow-sm" style="width: 38px; height: 38px; border: 2px solid #fff; display: block;">
                                    </c:otherwise>
                                </c:choose>
                                <div class="d-flex flex-column justify-content-center me-1" style="height: 100%;">
                                    <div class="fw-bold text-dark" style="font-size: 0.85rem; line-height: 1.2;">${sessionScope.nombreUsuario}</div>
                                    <div class="text-muted" style="font-size: 0.7rem; line-height: 1;">${sessionScope.rolNombre}</div>
                                </div>
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end border-0 shadow-sm mt-2 p-2">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/usuarios?action=profile">
                                    <i class="bi bi-person-vcard me-2 text-primary"></i>Mi Perfil
                                </a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item text-danger py-2" href="${pageContext.request.contextPath}/logout">
                                    <i class="bi bi-box-arrow-right me-2"></i>Cerrar sesi&oacute;n
                                </a></li>
                            </ul>
                        </li>
                    </ul>
                </c:if>
            </div>
        </div>
    </nav>

    <style>
        .nav-link { min-height: 40px; display: flex !important; align-items: center; }
        .nav-link:hover { background-color: #f0f7ff; color: #004884 !important; border-radius: 8px; }
        .dropdown-item:hover { background-color: #f0f7ff; color: #004884; border-radius: 5px; }
    </style>