<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

    <!-- Navbar Principal Compacto y Equilibrado -->
    <nav class="navbar navbar-expand-xl navbar-light bg-white shadow-sm py-2">
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
                    
                    <c:if test="${sessionScope.usuario.tienePermiso('CONTRATISTAS_VER') || sessionScope.usuario.tienePermiso('SUPERVISORES_VER') || sessionScope.usuario.tienePermiso('ORDENADORES_VER') || sessionScope.usuario.tienePermiso('REVISORES_VER') || sessionScope.usuario.tienePermiso('CONTRATOS_VER') || sessionScope.usuario.tienePermiso('PUEDE_REVISAR_CUENTAS') || sessionScope.usuario.tienePermiso('REVISION_CUENTAS_VER') || sessionScope.usuario.tienePermiso('ADMINISTRAR_CUENTAS') || sessionScope.usuario.tienePermiso('ADMINISTRAR_CUENTAS_EDITAR') || sessionScope.usuario.tienePermiso('VERBOS_VER') || sessionScope.usuario.tienePermiso('CONSECUTIVOS_VER')}">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle fw-bold px-3 py-2 d-flex align-items-center" 
                           style="color: #004884; font-size: 0.9rem;"
                           href="#" id="masterDataDrop" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-database-fill-add me-2"></i>Datos maestros
                        </a>
                        <ul class="dropdown-menu border-0 shadow-sm mt-3 p-2">
                            <c:if test="${sessionScope.usuario.tienePermiso('CONSECUTIVOS_VER')}">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/consecutivos"><i class="bi bi-123 me-2"></i>Consecutivos</a></li>
                            </c:if>
                            <c:if test="${sessionScope.usuario.tienePermiso('CONTRATISTAS_VER')}">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/contratistas"><i class="bi bi-people me-2"></i>Contratistas</a></li>
                            </c:if>
                            <c:if test="${sessionScope.usuario.tienePermiso('SUPERVISORES_VER')}">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/supervisores"><i class="bi bi-person-badge me-2"></i>Supervisores</a></li>
                            </c:if>
                            <c:if test="${sessionScope.usuario.tienePermiso('ORDENADORES_VER')}">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/ordenadores"><i class="bi bi-briefcase me-2"></i>Ordenadores</a></li>
                            </c:if>
                            <c:if test="${sessionScope.usuario.tienePermiso('REVISORES_VER')}">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/revisores"><i class="bi bi-person-check me-2"></i>Revisores de Documentos</a></li>
                            </c:if>
                            <c:if test="${sessionScope.usuario.tienePermiso('PRESUPUESTO_VER')}">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/presupuesto"><i class="bi bi-graph-up me-2"></i>Presupuesto</a></li>
                            </c:if>
                            <c:if test="${sessionScope.usuario.tienePermiso('CONTRATOS_VER') || sessionScope.usuario.tienePermiso('PUEDE_REVISAR_CUENTAS') || sessionScope.usuario.tienePermiso('REVISION_CUENTAS_VER') || sessionScope.usuario.tienePermiso('SUPERVISOR_CUENTAS') || sessionScope.usuario.tienePermiso('ADMINISTRAR_CUENTAS') || sessionScope.usuario.tienePermiso('ADMINISTRAR_CUENTAS_EDITAR') || (sessionScope.usuario.rol != null && (sessionScope.usuario.rol.nombre == 'Supervisor' || sessionScope.usuario.rol.nombre == 'SUPERVISOR'))}">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/informes"><i class="bi bi-wallet2 me-2"></i>Cuentas</a></li>
                            </c:if>
                            <c:if test="${sessionScope.usuario.tienePermiso('VERBOS_VER')}">
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/verbos"><i class="bi bi-translate me-2"></i>Diccionario de Verbos</a></li>
                            </c:if>
                            <c:if test="${sessionScope.usuario.tienePermiso('COMBINACION_VER')}">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/revaluacion"><i class="bi bi-star-fill me-2"></i>Revaluaci&oacute;n</a></li>
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


                    <c:if test="${sessionScope.usuario.tienePermiso('ADMIN_VER') || sessionScope.usuario.tienePermiso('ADMIN_USUARIOS')}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle fw-bold px-3 py-2 d-flex align-items-center" 
                               style="color: #004884; font-size: 0.9rem;"
                               href="#" id="adminDrop" role="button" data-bs-toggle="dropdown">
                                <i class="bi bi-shield-lock-fill me-2"></i>Administraci&oacute;n
                            </a>
                            <ul class="dropdown-menu border-0 shadow-sm mt-3 p-2">
                                <c:if test="${sessionScope.usuario.tienePermiso('ADMIN_USUARIOS') || sessionScope.usuario.tienePermiso('ADMIN_CREAR') || sessionScope.usuario.tienePermiso('ADMIN_VER')}">
                                    <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/admin/usuarios"><i class="bi bi-person-gear me-2"></i>Usuarios</a></li>
                                </c:if>
                                <c:if test="${sessionScope.usuario.tienePermiso('ADMIN_VER') || sessionScope.usuario.tienePermiso('ADMIN_ROLES')}">
                                    <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/admin/roles"><i class="bi bi-key-fill me-2"></i>Roles y permisos</a></li>
                                </c:if>
                                <c:if test="${sessionScope.usuario.esAdministrador()}">
                                    <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/admin/configuracion"><i class="bi bi-gear-fill me-2"></i>Configuración del Sistema</a></li>
                                </c:if>
                                <c:if test="${sessionScope.usuario.esAdministrador() || sessionScope.usuario.tienePermiso('AUDITORIA_VER')}">
                                    <li><hr class="dropdown-divider"></li>
                                    <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/admin/auditoria"><i class="bi bi-display me-2 text-primary"></i>Auditoría del sistema</a></li>
                                </c:if>
                            </ul>
                        </li>
                    </c:if>
                </ul>

                <c:if test="${not empty sessionScope.usuario}">
                    <ul class="navbar-nav ms-auto align-items-center">
                        <!-- Campanita de Notificaciones -->
                        <li class="nav-item dropdown me-2" id="notifDropdownItem">
                            <a class="nav-link position-relative p-2 d-flex align-items-center justify-content-center rounded-circle notif-bell-btn" 
                               href="#" id="notifBellDrop" role="button" data-bs-toggle="dropdown" aria-expanded="false" 
                               title="Notificaciones de Cuentas" style="width: 40px; height: 40px; color: #004884; background-color: #f1f5f9; transition: all 0.2s ease;">
                                <i class="bi bi-bell-fill fs-5" id="bellIcon"></i>
                                <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger shadow-sm d-none" 
                                      id="notifBadge" style="font-size: 0.68rem; padding: 0.35em 0.55em;">
                                    0
                                </span>
                            </a>
                            <div class="dropdown-menu dropdown-menu-end border-0 shadow-lg p-0 notif-dropdown-menu" 
                                 aria-labelledby="notifBellDrop" style="width: 360px; max-width: 92vw; border-radius: 12px; overflow: hidden; z-index: 1060;">
                                <div class="p-3 bg-light border-bottom d-flex align-items-center justify-content-between">
                                    <div class="d-flex align-items-center">
                                        <i class="bi bi-bell-fill me-2 text-primary"></i>
                                        <span class="fw-bold text-dark" style="font-size: 0.92rem;">Notificaciones</span>
                                        <span class="badge bg-primary ms-2 rounded-pill d-none" id="notifHeaderCount">0</span>
                                    </div>
                                    <button type="button" class="btn btn-link btn-sm p-0 text-decoration-none text-muted" 
                                            id="btnMarcarTodasLeidas" style="font-size: 0.78rem;" onclick="marcarTodasNotificacionesLeidas(event)">
                                        <i class="bi bi-check2-all me-1"></i>Marcar todas leídas
                                    </button>
                                </div>
                                <div class="notif-scroll-container" id="notifListContainer" style="max-height: 380px; overflow-y: auto;">
                                    <div class="p-4 text-center text-muted" id="notifEmptyState">
                                        <i class="bi bi-bell-slash text-secondary mb-2" style="font-size: 2rem; display: block;"></i>
                                        <span class="small">No tienes notificaciones pendientes</span>
                                    </div>
                                </div>
                                <div class="p-2 bg-light border-top text-center">
                                    <small class="text-muted" style="font-size: 0.72rem;">
                                        <i class="bi bi-broadcast me-1 text-success"></i>Alertas de trámite de cuentas en tiempo real
                                    </small>
                                </div>
                            </div>
                        </li>

                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle d-flex align-items-center px-3" 
                               href="#" id="userDrop" role="button" data-bs-toggle="dropdown" style="min-height: 50px;">
                                <c:choose>
                                    <c:when test="${not empty sessionScope.usuario.fotoUrl}">
                                        <img src="${pageContext.request.contextPath}/${sessionScope.usuario.fotoUrl}" 
                                             onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';"
                                             alt="User" class="rounded-circle me-2 shadow-sm" style="width: 38px; height: 38px; object-fit: cover; object-position: 50% 15%; border: 2px solid #fff; display: block;">
                                        <div class="rounded-circle me-2 shadow-sm bg-primary text-white align-items-center justify-content-center fw-bold" 
                                             style="width: 38px; height: 38px; font-size: 0.95rem; border: 2px solid #fff; display: none;">
                                            ${not empty sessionScope.nombreUsuario ? sessionScope.nombreUsuario.substring(0, 1).toUpperCase() : 'U'}
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="rounded-circle me-2 shadow-sm bg-primary text-white d-flex align-items-center justify-content-center fw-bold" 
                                             style="width: 38px; height: 38px; font-size: 0.95rem; border: 2px solid #fff;">
                                            ${not empty sessionScope.nombreUsuario ? sessionScope.nombreUsuario.substring(0, 1).toUpperCase() : 'U'}
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                                <div class="d-flex flex-column justify-content-center me-1" style="height: 100%; max-width: 150px;">
                                    <div class="fw-bold text-dark text-truncate" style="font-size: 0.85rem; line-height: 1.2;" title="${sessionScope.nombreUsuario}">${sessionScope.nombreUsuario}</div>
                                    <div class="text-muted text-truncate" style="font-size: 0.7rem; line-height: 1;" title="${sessionScope.rolNombre}">${sessionScope.rolNombre}</div>
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