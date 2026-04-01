<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>${rol_edit != null ? 'Editar' : 'Nuevo'} Rol - DAGJP</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
            <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
            <style>
                .modulo-card { border: 1px solid #dee2e6; border-radius: 12px; padding: 18px; margin-bottom: 20px; background-color: #fff; }
                .modulo-title { font-weight: 800; font-size: 0.85rem; color: #004884; border-bottom: 2px solid #004884; padding-bottom: 8px; margin-bottom: 12px; text-transform: uppercase; }
                .form-check-label { font-size: 0.88rem; color: #333; }
                .btn-save { background-color: #198754; color: white; transition: all 0.2s; }
                .btn-save:hover { background-color: #157347; color: white; transform: translateY(-2px); }
            </style>
        </head>

        <body class="bg-light d-flex flex-column min-vh-100">
            <jsp:include page="/inc/navbar.jsp" />

            <div class="container mt-4 mb-5">
                <nav aria-label="breadcrumb" class="mb-3">
                    <ol class="breadcrumb small">
                        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp">Inicio</a></li>
                        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/roles">Roles</a></li>
                        <li class="breadcrumb-item active">${rol_edit != null ? 'Editar' : 'Nuevo'}</li>
                    </ol>
                </nav>

                <div class="card border-0 shadow-sm overflow-hidden">
                    <div class="card-body p-4">
                        <form action="${pageContext.request.contextPath}/admin/roles" method="post">
                            <input type="hidden" name="action" value="${rol_edit != null ? 'update' : 'insert'}">
                            <c:if test="${rol_edit != null}">
                                <input type="hidden" name="id" value="${rol_edit.id}">
                            </c:if>

                            <div class="d-flex justify-content-between align-items-center mb-4">
                                <h3 class="fw-bold mb-0">${rol_edit != null ? 'Editar Rol' : 'Nuevo Rol'}</h3>
                                <div class="gap-2 d-flex">
                                    <a href="${pageContext.request.contextPath}/admin/roles" class="btn btn-outline-secondary fw-bold px-4">Cancelar</a>
                                    <button type="submit" class="btn btn-save fw-bold px-4">
                                        <i class="bi bi-save-fill me-2"></i> Guardar Cambios
                                    </button>
                                </div>
                            </div>

                            <div class="row g-4 mb-4">
                                <div class="col-md-6 text-start">
                                    <label class="form-label fw-bold small">Nombre del Rol *</label>
                                    <input type="text" class="form-control" name="nombre" value="${rol_edit.nombre}" required placeholder="Ej: Administrador">
                                </div>
                                <div class="col-md-6 text-start">
                                    <label class="form-label fw-bold small">Descripción</label>
                                    <input type="text" class="form-control" name="descripcion" value="${rol_edit.descripcion}" placeholder="Breve nota sobre este rol">
                                </div>
                            </div>

                            <h5 class="fw-bold text-dark border-bottom pb-2 mb-4"><i class="bi bi-shield-check me-2"></i>Asignación de Permisos</h5>

                            <div class="row">
                                <c:forEach var="modulo" items="${modulos}">
                                    <div class="col-md-6 col-lg-4">
                                        <div class="modulo-card">
                                            <div class="d-flex justify-content-between align-items-center modulo-title">
                                                <span><i class="bi bi-folder-fill me-2"></i>${modulo}</span>
                                                <button type="button" class="btn btn-sm btn-link p-0 text-decoration-none text-muted" 
                                                        onclick="checkModulo('${modulo}', true)">Marcar todos</button>
                                            </div>
                                            <c:forEach var="permiso" items="${todosPermisos}">
                                                <c:if test="${permiso.modulo == modulo}">
                                                    <div class="form-check mb-2">
                                                        <input class="form-check-input check-${modulo}" type="checkbox" name="permisos" 
                                                               value="${permiso.id}" id="perm_${permiso.id}"
                                                               <c:forEach var="p" items="${rol_edit.permisos}"><c:if test="${p.id == permiso.id}">checked</c:if></c:forEach>
                                                        >
                                                        <label class="form-check-label" for="perm_${permiso.id}">${permiso.nombre}</label>
                                                    </div>
                                                </c:if>
                                            </c:forEach>
                                            <div class="text-end mt-2">
                                                <button type="button" class="btn btn-sm btn-link p-0 text-decoration-none text-danger small" 
                                                        onclick="checkModulo('${modulo}', false)" style="font-size: 0.75rem;">Limpiar</button>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <jsp:include page="/inc/footer.jsp" />

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
            <script>
                function checkModulo(modulo, status) {
                    var checks = document.querySelectorAll('.check-' + modulo);
                    checks.forEach(function(c) { c.checked = status; });
                }
            </script>
        </body>
        </html>
