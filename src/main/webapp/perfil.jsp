<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mi Perfil - DAGJP</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Segoe+UI:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="assets/css/styles.css" rel="stylesheet">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
    
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f6f9; }
        .card-profile { border-radius: 8px; border: 1px solid #ddd; background: #fff; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
        .section-header { font-size: 1.1rem; font-weight: 600; display: flex; align-items: center; margin-bottom: 20px; }
        .header-personal { color: #007bff; }
        .header-security { color: #ffc107; }
        .form-label { font-weight: 600; color: #333; margin-bottom: 5px; font-size: 0.95rem; }
        .form-control { padding: 10px 15px; border-radius: 5px; border: 1px solid #ccc; background-color: #fff; color: #333; }
        .form-control:read-only { background-color: #f8f9fa; color: #6c757d; border-color: #dee2e6; }
        .btn-blue { background-color: #007bff; color: white; border: none; padding: 10px 20px; border-radius: 5px; font-weight: 600; width: 100%; }
        .btn-blue:hover { background-color: #0069d9; color: white; }
        .btn-yellow { background-color: #ffc107; color: #333; border: none; padding: 10px 20px; border-radius: 5px; font-weight: 600; width: 100%; }
        .btn-yellow:hover { background-color: #e0a800; color: #212529; }
        .btn-close-custom { background-color: #6c757d; color: white; border: none; padding: 8px 25px; border-radius: 5px; font-weight: 600; }
        
        /* Avatar adjustments */
        .avatar-section { position: relative; text-align: center; margin-bottom: 30px; }
        .avatar-img { width: 150px; height: 150px; border-radius: 50%; object-fit: cover; object-position: 50% 15%; border: 4px solid #fff; box-shadow: 0 5px 15px rgba(0,0,0,0.15); }
        .avatar-overlay { position: absolute; bottom: 5px; left: 50%; transform: translateX(35px); background: #198754; color: white; width: 38px; height: 38px; border-radius: 50%; display: flex; align-items: center; justify-content: center; cursor: pointer; border: 3px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.2); transition: all 0.2s; }
        .avatar-remove { position: absolute; bottom: 5px; left: 50%; transform: translateX(-73px); background: #dc3545; color: white; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; cursor: pointer; border: 2px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.2); transition: all 0.2s; }
        .avatar-remove:hover { transform: translateX(-73px) scale(1.1); background: #bb2d3b; }
        .avatar-overlay:hover { transform: translateX(35px) scale(1.1); background: #157347; }

        .container-custom { max-width: 1000px; }
    </style>
</head>
<body class="d-flex flex-column min-vh-100">
    <jsp:include page="inc/navbar.jsp" />

    <div class="container container-custom py-4 flex-grow-1">
        <div class="card card-profile p-4">
            
            <div class="row align-items-center mb-4">
                <div class="col-auto">
                    <div class="avatar-section">
                        <img src="${usuario.fotoUrl != null ? pageContext.request.contextPath.concat('/').concat(usuario.fotoUrl) : 'https://ui-avatars.com/api/?name='.concat(nombreUsuario).concat('&background=0D8ABC&color=fff&size=150')}" 
                             alt="Foto" class="avatar-img" id="profileImagePreview">
                        <label for="profileImageInput" class="avatar-overlay" title="Cambiar foto">
                            <i class="bi bi-camera-fill"></i>
                        </label>
                        <c:if test="${not empty usuario.fotoUrl}">
                            <div class="avatar-remove" onclick="removePhoto()" title="Quitar foto">
                                <i class="bi bi-trash3-fill"></i>
                            </div>
                        </c:if>
                        <form id="formUploadPhoto" enctype="multipart/form-data" class="d-none">
                            <input type="file" id="profileImageInput" name="foto" accept="image/*" onchange="uploadPhoto()">
                        </form>
                    </div>
                </div>
                <div class="col">
                    <h4 class="fw-bold mb-0 text-dark">${sessionScope.usuario.nombreCompleto}</h4>
                    <p class="text-muted mb-0">${sessionScope.rolNombre}</p>
                </div>
            </div>

            <div class="row g-5">
                <!-- Columna Izquierda: Datos Personales -->
                <div class="col-md-6 border-end">
                    <div class="section-header header-personal">
                        <i class="bi bi-person-vcard me-2"></i> Datos personales
                    </div>
                    <form id="formUpdateProfile">
                        <div class="mb-3">
                            <label class="form-label">No. de documento</label>
                            <input type="text" class="form-control" value="${sessionScope.usuario.cedula}" readonly>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Nombres y apellidos</label>
                            <input type="text" name="nombre" class="form-control" value="${sessionScope.usuario.nombreCompleto}" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Email</label>
                            <input type="email" name="correo" class="form-control" value="${sessionScope.usuario.correo}" required>
                        </div>
                        <div class="mb-4">
                            <label class="form-label">No. de contacto</label>
                            <input type="text" name="celular" class="form-control" value="${sessionScope.usuario.celular}">
                        </div>
                        <button type="submit" class="btn btn-blue shadow-sm">
                            <i class="bi bi-save me-2"></i> Guardar datos
                        </button>
                    </form>
                </div>

                <!-- Columna Derecha: Seguridad -->
                <div class="col-md-6">
                    <div class="section-header header-security">
                        <i class="bi bi-key-fill me-2 rotate-45"></i> Seguridad
                    </div>
                    <form id="formUpdatePassword">
                        <div class="mb-3">
                            <label class="form-label">Contraseña actual</label>
                            <input type="password" name="oldPass" class="form-control" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Nueva contraseña</label>
                            <input type="password" name="newPass" id="newPass" class="form-control" required>
                        </div>
                        <div class="mb-4">
                            <label class="form-label">Confirmar nueva contraseña</label>
                            <input type="password" name="confirmPass" id="confirmPass" class="form-control" required>
                        </div>
                        <button type="submit" class="btn btn-yellow shadow-sm">
                            <i class="bi bi-key-fill me-2"></i> Cambiar contraseña
                        </button>
                    </form>
                </div>
            </div>

            <div class="text-end mt-5 pt-3 border-top">
                <a href="${pageContext.request.contextPath}/index.jsp" class="btn-close-custom text-decoration-none">
                    <i class="bi bi-x-lg me-2"></i> Cerrar
                </a>
            </div>
        </div>
    </div>

    <jsp:include page="inc/footer.jsp" />

    <!-- Scripts -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

    <script>
        // Estilo solicitado: icono de llave rotado
        $('.rotate-45').css('transform', 'rotate(-45deg)');

        // Actualizar Info Personal
        $('#formUpdateProfile').on('submit', function(e) {
            e.preventDefault();
            $.post('${pageContext.request.contextPath}/usuarios?action=updateProfile', $(this).serialize(), function(res) {
                if(res.success) {
                    Swal.fire({ icon: 'success', title: '¡Guardado!', text: 'Tus datos se actualizaron correctamente.', timer: 2000, showConfirmButton: false }).then(() => {
                        location.reload();
                    });
                } else {
                    Swal.fire('Error', res.message || 'Error al guardar.', 'error');
                }
            }, 'json');
        });

        // Actualizar Contraseña
        $('#formUpdatePassword').on('submit', function(e) {
            e.preventDefault();
            if($('#newPass').val() !== $('#confirmPass').val()) {
                Swal.fire('Atención', 'Las nuevas contraseñas no coinciden.', 'warning');
                return;
            }
            $.post('${pageContext.request.contextPath}/usuarios?action=changeMyPassword', $(this).serialize(), function(res) {
                if(res.success) {
                    Swal.fire({ icon: 'success', title: '¡Listo!', text: 'Contraseña cambiada con éxito.', timer: 2000, showConfirmButton: false });
                    $('#formUpdatePassword')[0].reset();
                } else {
                    Swal.fire('Error', res.message || 'Error al cambiar contraseña.', 'error');
                }
            }, 'json');
        });

        // Subida de Foto
        function uploadPhoto() {
            const formData = new FormData($('#formUploadPhoto')[0]);
            $.ajax({
                url: '${pageContext.request.contextPath}/usuarios?action=uploadPhoto',
                type: 'POST', data: formData, processData: false, contentType: false,
                success: function(res) {
                    if(res.success) {
                        location.reload();
                    } else {
                        Swal.fire('Error', res.message || 'Error al subir la foto.', 'error');
                    }
                },
                dataType: 'json'
            });
        }

        // Eliminar Foto
        function removePhoto() {
            Swal.fire({
                title: '¿Quitar foto?',
                text: "Se volverá a mostrar el avatar por defecto.",
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#dc3545',
                cancelButtonColor: '#6c757d',
                confirmButtonText: 'Sí, quitar',
                cancelButtonText: 'Cancelar'
            }).then((result) => {
                if (result.isConfirmed) {
                    $.post('${pageContext.request.contextPath}/usuarios?action=removePhoto', function(res) {
                        if(res.success) {
                            location.reload();
                        }
                    }, 'json');
                }
            });
        }
    </script>
</body>
</html>
