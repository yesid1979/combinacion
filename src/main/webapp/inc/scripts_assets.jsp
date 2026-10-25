<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- jQuery 3.6.0 (Local) -->
<script src="${pageContext.request.contextPath}/assets/vendor/jquery/jquery-3.6.0.min.js"></script>

<!-- Bootstrap 5 Bundle JS (Local) -->
<script src="${pageContext.request.contextPath}/assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>

<!-- DataTables + Bootstrap 5 + Responsive JS (Local) -->
<script src="${pageContext.request.contextPath}/assets/vendor/datatables/jquery.dataTables.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/vendor/datatables/dataTables.bootstrap5.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/vendor/datatables/dataTables.responsive.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/vendor/datatables/responsive.bootstrap5.min.js"></script>

<!-- SweetAlert2 v11 (Local) -->
<script src="${pageContext.request.contextPath}/assets/vendor/sweetalert2/sweetalert2.all.min.js"></script>

<!-- Summernote Lite JS (Local) -->
<script src="${pageContext.request.contextPath}/assets/vendor/summernote/summernote-lite.min.js"></script>

<script>
    // Configuración global por defecto para DataTables en Español usando el recurso local
    if (window.jQuery && $.fn.dataTable) {
        $.extend(true, $.fn.dataTable.defaults, {
            language: {
                url: '${pageContext.request.contextPath}/assets/vendor/datatables/es-ES.json'
            }
        });
    }

    // Funciones globales para sincronización de formatos con Google Drive (con confirmación y opción de cancelar)
    var currentSyncAbortController = null;

    function sincronizarDriveConConfirmacion(id, modo, isFromLista) {
        if (!id || id <= 0) return;

        Swal.fire({
            title: '¿Sincronizar formatos con Drive?',
            html: '<p class="text-muted mb-0">Se generarán y subirán a Google Drive las versiones oficiales actualizadas en <strong>Word, Excel y PDF</strong> de esta cuenta de cobro.<br><br>Esta operación toma unos segundos. ¿Deseas iniciar la sincronización?</p>',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#198754',
            cancelButtonColor: '#6c757d',
            confirmButtonText: '<i class="bi bi-cloud-arrow-up me-1"></i> Sí, sincronizar',
            cancelButtonText: 'Cancelar',
            reverseButtons: true,
            focusCancel: true
        }).then(function(result) {
            if (result.isConfirmed) {
                ejecutarSincronizacionDrive(id, modo, isFromLista);
            }
        });
    }

    function ejecutarSincronizacionDrive(id, modo, isFromLista) {
        currentSyncAbortController = new AbortController();
        var abortedByUser = false;

        Swal.fire({
            title: 'Sincronizando con Google Drive...',
            html: '<div class="py-3 text-center">' +
                  '  <div class="spinner-border text-success mb-3" style="width: 3.5rem; height: 3.5rem;" role="status">' +
                  '    <span class="visually-hidden">Cargando...</span>' +
                  '  </div>' +
                  '  <p class="text-secondary small mb-1">Generando formatos oficiales (Word, Excel y PDF) y actualizando archivos en la carpeta de Drive.</p>' +
                  '  <p class="text-muted" style="font-size: 0.8rem;">Puedes cancelar en cualquier momento si deseas continuar más tarde.</p>' +
                  '</div>',
            allowOutsideClick: false,
            allowEscapeKey: false,
            showConfirmButton: false,
            showCancelButton: true,
            cancelButtonText: '<i class="bi bi-x-circle me-1"></i> Cancelar operación',
            cancelButtonColor: '#dc3545',
            customClass: {
                cancelButton: 'btn btn-danger px-4 shadow-sm'
            }
        }).then(function(resModal) {
            if (resModal.dismiss === Swal.DismissReason.cancel) {
                abortedByUser = true;
                if (currentSyncAbortController) {
                    currentSyncAbortController.abort();
                }
                Swal.fire({
                    icon: 'info',
                    title: 'Operación cancelada',
                    text: 'Has cancelado la sincronización en el navegador. La página permanece disponible sin cambios.',
                    timer: 3000,
                    showConfirmButton: false
                });
            }
        });

        var contextPath = '${pageContext.request.contextPath}';
        var url = (contextPath ? contextPath : '') + '/informes?action=syncDrive&id=' + id + '&ajax=true';
        if (modo) {
            url += '&modo=' + encodeURIComponent(modo);
        }
        if (isFromLista) {
            url += '&from=lista';
        }

        fetch(url, {
            method: 'GET',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            signal: currentSyncAbortController.signal
        })
        .then(function(response) {
            return response.json().then(function(data) {
                if (!response.ok) {
                    var msg = (data && data.message) ? data.message : ('Error del servidor (código ' + response.status + ')');
                    throw new Error(msg);
                }
                return data;
            }).catch(function(jsonErr) {
                if (!response.ok) {
                    throw new Error('Error del servidor (código ' + response.status + ')');
                }
                return { success: true, message: 'Formatos sincronizados exitosamente.' };
            });
        })
        .then(function(data) {
            if (abortedByUser) return;
            if (data.success) {
                Swal.fire({
                    icon: 'success',
                    title: '¡Sincronización exitosa!',
                    text: data.message || 'Los formatos oficiales fueron sincronizados correctamente en Drive.',
                    confirmButtonColor: '#198754',
                    confirmButtonText: 'Entendido'
                }).then(function() {
                    if (isFromLista && window.jQuery && $.fn.dataTable && $('#tablaInformes').length) {
                        $('#tablaInformes').DataTable().ajax.reload(null, false);
                    }
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    title: 'Error al sincronizar',
                    text: data.message || 'No fue posible sincronizar los formatos en Drive.',
                    confirmButtonColor: '#dc3545'
                });
            }
        })
        .catch(function(err) {
            if (err.name === 'AbortError' || abortedByUser) {
                return;
            }
            console.error('Error en sincronización con Drive:', err);
            Swal.fire({
                icon: 'error',
                title: 'Error de sincronización',
                text: err.message || 'Ocurrió un error al intentar comunicarse con el servidor.',
                confirmButtonColor: '#dc3545'
            });
        });
    }
</script>
