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
                        <!-- Identificación: Cédula y Nombres -->
                        <div class="col-md-4">
                            <label for="cedula" class="form-label fw-semibold">
                                <i class="bi bi-person-vcard me-1"></i>No. de Cédula <span class="text-danger">*</span>
                            </label>
                            <input type="text" class="form-control" id="cedula" name="cedula"
                                   value="${usuario_edit != null ? usuario_edit.cedula : ''}"
                                   required placeholder="Solo números"
                                   oninput="this.value = this.value.replace(/[^0-9]/g, ''); checkCedulaAjax()">
                            <div id="cedula-error" class="text-danger small mt-1" style="display: none;">
                                <i class="bi bi-exclamation-circle me-1"></i>Esta cédula ya está registrada.
                            </div>
                        </div>

                        <div class="col-md-8">
                            <label for="nombre_completo" class="form-label fw-semibold">
                                <i class="bi bi-card-text me-1"></i>Nombres y Apellidos <span class="text-danger">*</span>
                            </label>
                            <input type="text" class="form-control" id="nombre_completo" name="nombre_completo"
                                   value="${usuario_edit != null ? usuario_edit.nombreCompleto : ''}"
                                   required maxlength="255"
                                   placeholder="Ej: Juan Carlos Pérez López"
                                   oninput="generarUsername()">
                        </div>

                        <!-- Contacto: Correo y Celular -->
                        <div class="col-md-6">
                            <label for="correo" class="form-label fw-semibold">
                                <i class="bi bi-envelope me-1"></i>Correo electrónico
                            </label>
                            <input type="email" class="form-control" id="correo" name="correo"
                                   value="${usuario_edit != null ? usuario_edit.correo : ''}"
                                   maxlength="100"
                                   placeholder="Ej: juan@correo.com">
                        </div>

                        <div class="col-md-6">
                            <label for="celular" class="form-label fw-semibold">
                                <i class="bi bi-phone me-1"></i>Celular
                            </label>
                            <input type="text" class="form-control" id="celular" name="celular"
                                   value="${usuario_edit != null ? usuario_edit.celular : ''}"
                                   placeholder="Ej: 3001234567">
                        </div>

                        <!-- Cuenta: Username y Password -->
                        <div class="col-md-6">
                            <label for="username" class="form-label fw-semibold">
                                <i class="bi bi-person-badge me-1"></i>Nombre de usuario <span class="text-danger">*</span>
                            </label>
                            <input type="text" class="form-control" id="username" name="username"
                                   value="${usuario_edit != null ? usuario_edit.username : ''}"
                                   required minlength="3" maxlength="50"
                                   placeholder="Ej: juan.perez"
                                   onblur="checkUsernameAjax()">
                            <div id="username-error" class="text-danger small mt-1" style="display: none;">
                                <i class="bi bi-exclamation-circle me-1"></i>Este nombre de usuario ya está ocupado.
                            </div>
                        </div>

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

                        <!-- Perfil: Sexo y Rol -->
                        <div class="col-md-6">
                            <label for="sexo" class="form-label fw-semibold">
                                <i class="bi bi-gender-ambiguous me-1"></i>Sexo
                            </label>
                            <select class="form-select" id="sexo" name="sexo">
                                <option value="">-- Seleccione --</option>
                                <option value="Masculino" ${usuario_edit != null && usuario_edit.sexo == 'Masculino' ? 'selected' : ''}>Masculino</option>
                                <option value="Femenino" ${usuario_edit != null && usuario_edit.sexo == 'Femenino' ? 'selected' : ''}>Femenino</option>
                                <option value="Otro" ${usuario_edit != null && usuario_edit.sexo == 'Otro' ? 'selected' : ''}>Otro</option>
                            </select>
                        </div>

                        <div class="col-md-6">
                            <label for="rol_id" class="form-label fw-semibold">
                                <i class="bi bi-shield-lock me-1"></i>Rol del Sistema <span class="text-danger">*</span>
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

                        <!-- Vinculación y Cuenta Activa (Siempre visible) -->
                        <div class="col-md-6">
                            <label for="vinculacion" class="form-label fw-semibold">
                                <i class="bi bi-info-circle me-1"></i>Vinculación <span class="text-danger">*</span>
                            </label>
                            <select class="form-select" id="vinculacion" name="vinculacion" required onchange="toggleContratistaFields()">
                                <option value="">-- Seleccione vinculación --</option>
                                <option value="Planta" ${usuario_edit != null && usuario_edit.vinculacion == 'Planta' ? 'selected' : ''}>Planta</option>
                                <option value="Contratista" ${usuario_edit != null && usuario_edit.vinculacion == 'Contratista' ? 'selected' : ''}>Contratista</option>
                            </select>
                        </div>

                        <div class="col-md-6 d-flex align-items-end pb-1">
                            <div class="form-check form-switch bg-light p-2 px-4 rounded border w-100" style="height: 38px;">
                                <input class="form-check-input ms-0" type="checkbox" id="activo" name="activo"
                                       ${usuario_edit == null || usuario_edit.activo ? 'checked' : ''} style="transform: scale(1.2);">
                                <label class="form-check-label ms-3 fw-semibold" for="activo">Cuenta de Acceso Activa</label>
                            </div>
                        </div>

                        <!-- Información de Contratista (Condicional) -->
                        <div id="contratista_fields" style="display: none;" class="col-md-12">
                            <div class="row g-3">
                                <div class="col-md-12 mt-3">
                                    <h6 class="text-primary fw-bold border-bottom pb-2">
                                        <i class="bi bi-file-earmark-text me-1"></i>Información de Contratista
                                    </h6>
                                </div>

                                <div class="col-md-6">
                                    <label for="fecha_inicio" class="form-label fw-semibold">
                                        <i class="bi bi-calendar-event me-1"></i>Fecha Inicio Contrato <span class="text-danger">*</span>
                                    </label>
                                    <input type="date" class="form-control" id="fecha_inicio" name="fecha_inicio"
                                           value="${usuario_edit != null ? usuario_edit.fechaInicioContrato : ''}">
                                </div>

                                <div class="col-md-6">
                                    <label for="fecha_fin" class="form-label fw-semibold">
                                        <i class="bi bi-calendar-check me-1"></i>Fecha Fin Contrato <span class="text-danger">*</span>
                                    </label>
                                    <input type="date" class="form-control" id="fecha_fin" name="fecha_fin"
                                           value="${usuario_edit != null ? usuario_edit.fechaFinContrato : ''}">
                                </div>
                            </div>
                        </div>
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
    <script>
        function toggleContratistaFields() {
            const vinculacion = document.getElementById('vinculacion').value;
            const container = document.getElementById('contratista_fields');
            const fechaInicio = document.getElementById('fecha_inicio');
            const fechaFin = document.getElementById('fecha_fin');
            
            if (vinculacion === 'Contratista') {
                container.style.display = 'block';
                fechaInicio.required = true;
                fechaFin.required = true;
            } else {
                container.style.display = 'none';
                fechaInicio.required = false;
                fechaFin.required = false;
            }
        }

        function checkCedulaAjax() {
            const cedula = document.getElementById('cedula').value.trim();
            const idInput = document.querySelector('input[name="id"]');
            const id = idInput ? idInput.value : '0';
            const errorDiv = document.getElementById('cedula-error');
            const cedulaInput = document.getElementById('cedula');

            if (cedula.length < 3) {
                errorDiv.style.display = 'none';
                cedulaInput.classList.remove('is-invalid');
                checkFormValidity();
                return;
            }

            console.log('Validando cédula:', cedula, 'ID a excluir:', id);

            fetch('${pageContext.request.contextPath}/admin/usuarios?action=checkCedula&cedula=' + cedula + '&id=' + id)
                .then(response => {
                    if (!response.ok) throw new Error('Error en el servidor: ' + response.status);
                    return response.json();
                })
                .then(data => {
                    console.log('Respuesta servidor (Cédula):', data);
                    if (data.exists) {
                        errorDiv.style.display = 'block';
                        cedulaInput.classList.add('is-invalid');
                    } else {
                        errorDiv.style.display = 'none';
                        cedulaInput.classList.remove('is-invalid');
                    }
                    checkFormValidity();
                })
                .catch(error => {
                    console.error('Error en validación AJAX:', error);
                });
        }

        function checkUsernameAjax() {
            const username = document.getElementById('username').value.trim();
            const id = '${usuario_edit != null ? usuario_edit.id : 0}';
            const errorDiv = document.getElementById('username-error');
            const userInput = document.getElementById('username');

            if (username === '') {
                errorDiv.style.display = 'none';
                userInput.classList.remove('is-invalid');
                checkFormValidity();
                return;
            }

            return fetch('${pageContext.request.contextPath}/admin/usuarios?action=checkUsername&username=' + username + '&id=' + id)
                .then(response => response.json())
                .then(data => {
                    if (data.exists) {
                        errorDiv.style.display = 'block';
                        userInput.classList.add('is-invalid');
                    } else {
                        errorDiv.style.display = 'none';
                        userInput.classList.remove('is-invalid');
                    }
                    checkFormValidity();
                    return data.exists;
                })
                .catch(error => console.error('Error validation:', error));
        }

        function generarUsername() {
            const nombreCompleto = document.getElementById('nombre_completo').value.trim();
            const usernameInput = document.getElementById('username');
            const id = '${usuario_edit != null ? usuario_edit.id : 0}';
            
            if (id !== '0') return; // No autogenerar en edición si ya tiene uno
            if (nombreCompleto === '') {
                usernameInput.value = '';
                return;
            }

            const parts = nombreCompleto.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "").split(/\s+/);
            if (parts.length < 2) return;

            const primerNombre = parts[0];
            const primerApellido = parts[parts.length - 2] || parts[1];
            const segundoApellido = parts[parts.length - 1];

            const sugerencia1 = primerNombre + "." + (parts.length > 2 ? parts[2] : parts[1]);
            // Ajuste: si el nombre es "Nombre Apellido1 Apellido2", parts[2] es Apellido1.
            // Vamos a intentar ser más precisos. 
            // Si hay 2: N A -> n.a
            // Si hay 3: N N A -> n.a (parts[0].parts[2])
            // Si hay 4: N N A A -> n.a (parts[0].parts[2])
            
            let finalNombre = parts[0];
            let finalApellido = "";
            
            if (parts.length === 2) finalApellido = parts[1];
            else if (parts.length >= 3) finalApellido = parts[2];

            let usernameSugerido = finalNombre + "." + finalApellido;
            usernameInput.value = usernameSugerido;

            // Validar si existe para aplicar regla del segundo apellido
            fetch('${pageContext.request.contextPath}/admin/usuarios?action=checkUsername&username=' + usernameSugerido + '&id=0')
                .then(response => response.json())
                .then(data => {
                    if (data.exists && parts.length >= (parts.length <= 3 ? 3 : 4)) {
                        // Si existe juan.perez y hay un segundo apellido, usar juan.perez.rod
                        const lasPart = parts[parts.length - 1];
                        if (lasPart) {
                            usernameSugerido += "." + lasPart.substring(0, 3);
                            usernameInput.value = usernameSugerido;
                        }
                    }
                    checkUsernameAjax();
                });
        }

        function checkFormValidity() {
            const cedulaInvalid = document.getElementById('cedula').classList.contains('is-invalid');
            const userInvalid = document.getElementById('username').classList.contains('is-invalid');
            document.querySelector('button[type="submit"]').disabled = (cedulaInvalid || userInvalid);
        }

        // Ejecutar al cargar para casos de edición
        document.addEventListener('DOMContentLoaded', function() {
            toggleContratistaFields();
        });
    </script>
</body>
</html>
