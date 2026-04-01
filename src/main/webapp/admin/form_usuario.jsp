<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${usuario_edit != null ? 'Editar' : 'Nuevo'} Usuario - Administración</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
</head>
<body class="bg-light d-flex flex-column min-vh-100">

    <jsp:include page="/inc/navbar.jsp" />

    <div class="container mt-4 mb-5">
        <!-- Breadcrumb -->
        <nav aria-label="breadcrumb" class="mb-3">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/index.jsp">Inicio</a></li>
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/usuarios">Usuarios</a></li>
                <li class="breadcrumb-item active">${usuario_edit != null ? 'Editar' : 'Nuevo'}</li>
            </ol>
        </nav>

        <h3 class="fw-bold text-primary mb-4">
            <i class="bi bi-${usuario_edit != null ? 'pencil-square' : 'person-plus'} me-2"></i>
            ${usuario_edit != null ? 'Editar usuario' : 'Nuevo usuario'}
        </h3>

        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle me-1"></i>${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <div class="card border-0 shadow-sm">
            <div class="card-body p-4">
                <form action="${pageContext.request.contextPath}/admin/usuarios" method="post" autocomplete="off">
                    <input type="hidden" name="action" value="${usuario_edit != null ? 'update' : 'insert'}">
                    <c:if test="${usuario_edit != null}">
                        <input type="hidden" name="id" value="${usuario_edit.id}">
                    </c:if>

                    <div class="row g-3">
                        <!-- Username -->
                        <div class="col-md-6">
                            <label for="username" class="form-label fw-semibold">
                                <i class="bi bi-person me-1"></i>Nombre de usuario <span class="text-danger">*</span>
                            </label>
                            <input type="text" class="form-control" id="username" name="username"
                                   value="${usuario_edit != null ? usuario_edit.username : ''}"
                                   required minlength="3" maxlength="50"
                                   placeholder="Ej: juan.perez">
                        </div>

                        <!-- Password (solo en creación) -->
                        <c:if test="${usuario_edit == null}">
                            <div class="col-md-6">
                                <label for="password" class="form-label fw-semibold">
                                    <i class="bi bi-lock me-1"></i>Contraseña <span class="text-danger">*</span>
                                </label>
                                <input type="password" class="form-control" id="password" name="password"
                                       required minlength="6" maxlength="100"
                                       placeholder="Mínimo 6 caracteres">
                            </div>
                        </c:if>

                        <!-- Nombre Completo -->
                        <div class="col-md-6">
                            <label for="nombre_completo" class="form-label fw-semibold">
                                <i class="bi bi-card-text me-1"></i>Nombre completo <span class="text-danger">*</span>
                            </label>
                            <input type="text" class="form-control" id="nombre_completo" name="nombre_completo"
                                   value="${usuario_edit != null ? usuario_edit.nombreCompleto : ''}"
                                   required maxlength="255"
                                   placeholder="Ej: Juan Carlos Pérez López">
                        </div>

                        <!-- Correo -->
                        <div class="col-md-6">
                            <label for="correo" class="form-label fw-semibold">
                                <i class="bi bi-envelope me-1"></i>Correo electrónico
                            </label>
                            <input type="email" class="form-control" id="correo" name="correo"
                                   value="${usuario_edit != null ? usuario_edit.correo : ''}"
                                   maxlength="100"
                                   placeholder="Ej: juan@correo.com">
                        </div>

                        <!-- Rol -->
                        <div class="col-md-6">
                            <label for="rol_id" class="form-label fw-semibold">
                                <i class="bi bi-shield-lock me-1"></i>Rol <span class="text-danger">*</span>
                            </label>
                            <select class="form-select" id="rol_id" name="rol_id" required>
                                <option value="">-- Seleccione un rol --</option>
                                <c:forEach var="rol" items="${listRoles}">
                                    <option value="${rol.id}"
                                        ${usuario_edit != null && usuario_edit.rolId == rol.id ? 'selected' : ''}>
                                        ${rol.nombre} - ${rol.descripcion}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- Activo (solo en edición) -->
                        <c:if test="${usuario_edit != null}">
                            <div class="col-md-6">
                                <label class="form-label fw-semibold">
                                    <i class="bi bi-toggle-on me-1"></i>Estado
                                </label>
                                <div class="form-check form-switch mt-2">
                                    <input class="form-check-input" type="checkbox" id="activo" name="activo"
                                           ${usuario_edit.activo ? 'checked' : ''} style="transform: scale(1.3);">
                                    <label class="form-check-label ms-2" for="activo">Cuenta activa</label>
                                </div>
                            </div>
                        </c:if>
                    </div>

                    <hr class="my-4">

                    <div class="d-flex justify-content-between">
                        <a href="${pageContext.request.contextPath}/admin/usuarios" class="btn btn-secondary px-4 fw-bold" style="border-radius: 8px;">
                            <i class="bi bi-arrow-left me-1"></i>Cancelar
                        </a>
                        <button type="submit" class="btn text-white px-4 fw-bold" style="background-color: #198754; border-radius: 8px;">
                            <i class="bi bi-check-lg me-1"></i>
                            ${usuario_edit != null ? 'Actualizar' : 'Crear usuario'}
                        </button>
                    </div>
                </form>

                <!-- Cambiar contraseña (solo en edición) -->
                <c:if test="${usuario_edit != null}">
                    <hr class="my-4">
                    <h5 class="fw-bold text-warning mb-3">
                        <i class="bi bi-key me-2"></i>Cambiar contraseña
                    </h5>
                    <form action="${pageContext.request.contextPath}/admin/usuarios" method="post">
                        <input type="hidden" name="action" value="changePassword">
                        <input type="hidden" name="id" value="${usuario_edit.id}">
                        <div class="row g-3 align-items-end">
                            <div class="col-md-6">
                                <label for="nueva_password" class="form-label fw-semibold">Nueva contraseña</label>
                                <input type="password" class="form-control" id="nueva_password"
                                       name="nueva_password" required minlength="6"
                                       placeholder="Mínimo 6 caracteres">
                            </div>
                            <div class="col-md-3">
                                <button type="submit" class="btn btn-warning w-100"
                                        onclick="return confirm('¿Cambiar la contraseña de este usuario?')">
                                    <i class="bi bi-key me-1"></i>Cambiar
                                </button>
                            </div>
                        </div>
                    </form>
                </c:if>
            </div>
        </div>
    </div>

    <jsp:include page="/inc/footer.jsp" />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
