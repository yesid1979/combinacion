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

<c:if test="${not empty sessionScope.usuario}">
<!-- Estilos y Scripts del Sistema de Notificaciones en Tiempo Real -->
<style>
    @keyframes bellSwingAnim {
        0% { transform: rotate(0); }
        15% { transform: rotate(18deg); }
        30% { transform: rotate(-18deg); }
        45% { transform: rotate(14deg); }
        60% { transform: rotate(-14deg); }
        75% { transform: rotate(8deg); }
        90% { transform: rotate(-5deg); }
        100% { transform: rotate(0); }
    }
    .bell-swinging {
        display: inline-block;
        animation: bellSwingAnim 0.75s ease-in-out infinite;
        transform-origin: top center;
        color: #d97706 !important;
    }
    .notif-bell-btn:hover {
        background-color: #e2e8f0 !important;
        transform: scale(1.05);
    }
    .notif-item {
        transition: background-color 0.15s ease, transform 0.1s ease;
        border-bottom: 1px solid #f1f5f9;
        cursor: pointer;
    }
    .notif-item:hover {
        background-color: #f8fafc;
    }
    .notif-unread {
        background-color: #f0f7ff !important;
        border-left: 3px solid #004884;
    }
    .notif-icon-box {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1rem;
        flex-shrink: 0;
    }
</style>

<script>
    (function() {
        var notifContextPath = '${pageContext.request.contextPath}';
        var notifAudioCtx = null;
        var notifSwingTimer = null;

        // Inicializar o reproducir tono armónico de notificación (Web Audio API)
        function reproducirSonidoNotificacion() {
            try {
                var AudioContext = window.AudioContext || window.webkitAudioContext;
                if (!AudioContext) return;
                if (!notifAudioCtx) {
                    notifAudioCtx = new AudioContext();
                }
                if (notifAudioCtx.state === 'suspended') {
                    notifAudioCtx.resume();
                }
                var now = notifAudioCtx.currentTime;

                // Tono 1 (Ding)
                var osc1 = notifAudioCtx.createOscillator();
                var gain1 = notifAudioCtx.createGain();
                osc1.type = 'sine';
                osc1.frequency.setValueAtTime(587.33, now); // Re (D5)
                osc1.frequency.exponentialRampToValueAtTime(880, now + 0.12); // La (A5)
                gain1.gain.setValueAtTime(0.25, now);
                gain1.gain.exponentialRampToValueAtTime(0.001, now + 0.45);
                osc1.connect(gain1);
                gain1.connect(notifAudioCtx.destination);
                osc1.start(now);
                osc1.stop(now + 0.45);

                // Tono 2 (Chime armónico cristalino)
                var osc2 = notifAudioCtx.createOscillator();
                var gain2 = notifAudioCtx.createGain();
                osc2.type = 'triangle';
                osc2.frequency.setValueAtTime(880, now + 0.10); // La (A5)
                osc2.frequency.exponentialRampToValueAtTime(1174.66, now + 0.35); // Re (D6)
                gain2.gain.setValueAtTime(0.3, now + 0.10);
                gain2.gain.exponentialRampToValueAtTime(0.001, now + 0.75);
                osc2.connect(gain2);
                gain2.connect(notifAudioCtx.destination);
                osc2.start(now + 0.10);
                osc2.stop(now + 0.75);
            } catch (e) {
                console.log("Notificación sonora no iniciada:", e);
            }
        }

        // Animación de campaneo
        function activarAnimacionCampanita(activar) {
            var icon = document.getElementById('bellIcon');
            if (!icon) return;
            if (activar) {
                icon.classList.add('bell-swinging');
                if (notifSwingTimer) clearTimeout(notifSwingTimer);
                // La animación se detiene suavemente después de 6 segundos para no incomodar
                notifSwingTimer = setTimeout(function() {
                    icon.classList.remove('bell-swinging');
                }, 6000);
            } else {
                icon.classList.remove('bell-swinging');
                if (notifSwingTimer) clearTimeout(notifSwingTimer);
            }
        }

        // Actualizar el número del badge
        function actualizarBadge(conteo) {
            var badge = document.getElementById('notifBadge');
            var headerCount = document.getElementById('notifHeaderCount');
            if (!badge) return;

            if (conteo > 0) {
                badge.textContent = conteo > 99 ? '99+' : conteo;
                badge.classList.remove('d-none');
                if (headerCount) {
                    headerCount.textContent = conteo + ' nuevas';
                    headerCount.classList.remove('d-none');
                }
            } else {
                badge.textContent = '0';
                badge.classList.add('d-none');
                if (headerCount) {
                    headerCount.classList.add('d-none');
                }
            }
        }

        // Consultar estado de notificaciones en el servidor
        function verificarNotificaciones(esInicio) {
            fetch(notifContextPath + '/notificaciones?action=check', {
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
            .then(function(res) {
                if (!res.ok) throw new Error('Error al consultar notificaciones');
                return res.json();
            })
            .then(function(data) {
                var count = data.count || 0;
                var latestId = data.latestId || 0;
                actualizarBadge(count);

                var storageKey = 'notif_alerted_id_u' + '${sessionScope.usuario.id}';
                var lastAlertedId = parseInt(localStorage.getItem(storageKey) || '0', 10);

                // Si hay un ID nuevo mayor al último alertado y hay mensajes sin leer
                if (latestId > lastAlertedId && count > 0) {
                    localStorage.setItem(storageKey, latestId.toString());
                    // No sonar estridente en la primera carga si solo son antiguas, sonar siempre que sea evento nuevo
                    if (!esInicio || lastAlertedId > 0) {
                        reproducirSonidoNotificacion();
                        activarAnimacionCampanita(true);
                    }
                } else if (lastAlertedId === 0 && latestId > 0) {
                    // Inicializar storage si es primera vez que entra
                    localStorage.setItem(storageKey, latestId.toString());
                }
            })
            .catch(function(err) {
                // Silencioso ante fallos de red esporádicos
            });
        }

        // Cargar y pintar la lista desplegable de notificaciones
        function cargarListaNotificaciones() {
            var container = document.getElementById('notifListContainer');
            if (!container) return;

            container.innerHTML = '<div class="p-3 text-center text-muted"><div class="spinner-border spinner-border-sm text-primary me-2"></div>Cargando notificaciones...</div>';

            fetch(notifContextPath + '/notificaciones?action=list', {
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
            .then(function(res) { return res.json(); })
            .then(function(data) {
                actualizarBadge(data.count || 0);
                var lista = data.notifications || [];

                if (lista.length === 0) {
                    container.innerHTML = '<div class="p-4 text-center text-muted" id="notifEmptyState">' +
                                          '<i class="bi bi-bell-slash text-secondary mb-2" style="font-size: 2rem; display: block;"></i>' +
                                          '<span class="small">No tienes notificaciones pendientes</span></div>';
                    return;
                }

                var html = '';
                for (var i = 0; i < lista.length; i++) {
                    var n = lista[i];
                    var unreadCls = n.leida ? '' : 'notif-unread';
                    var iconBg = 'bg-primary-subtle text-primary';
                    var iconBi = 'bi-info-circle-fill';

                    if (n.tipo === 'SUCCESS') {
                        iconBg = 'bg-success-subtle text-success';
                        iconBi = 'bi-check-circle-fill';
                    } else if (n.tipo === 'DANGER') {
                        iconBg = 'bg-danger-subtle text-danger';
                        iconBi = 'bi-exclamation-triangle-fill';
                    } else if (n.tipo === 'WARNING') {
                        iconBg = 'bg-warning-subtle text-warning';
                        iconBi = 'bi-exclamation-circle-fill';
                    }

                    html += '<div class="p-3 notif-item ' + unreadCls + '" onclick="abrirNotificacion(' + n.id + ', \'' + (n.url || '') + '\')">' +
                            '  <div class="d-flex align-items-start">' +
                            '    <div class="notif-icon-box ' + iconBg + ' me-2">' +
                            '      <i class="bi ' + iconBi + '"></i>' +
                            '    </div>' +
                            '    <div class="flex-grow-1 overflow-hidden">' +
                            '      <div class="d-flex justify-content-between align-items-center mb-1">' +
                            '        <strong class="text-dark text-truncate" style="font-size: 0.84rem;">' + n.titulo + '</strong>' +
                            '        <small class="text-muted text-nowrap ms-2" style="font-size: 0.70rem;">' + n.tiempo + '</small>' +
                            '      </div>' +
                            '      <p class="text-secondary mb-0 small" style="font-size: 0.78rem; line-height: 1.3;">' + n.mensaje + '</p>' +
                            '    </div>' +
                            '  </div>' +
                            '</div>';
                }
                container.innerHTML = html;
            })
            .catch(function(err) {
                container.innerHTML = '<div class="p-3 text-center text-danger small">No se pudieron cargar las notificaciones.</div>';
            });
        }

        // Marcar todas como leídas
        window.marcarTodasNotificacionesLeidas = function(e) {
            if (e) e.stopPropagation();
            fetch(notifContextPath + '/notificaciones?action=mark_read&all=true', {
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
            .then(function(res) { return res.json(); })
            .then(function(data) {
                actualizarBadge(0);
                activarAnimacionCampanita(false);
                cargarListaNotificaciones();
            });
        };

        // Abrir una notificación específica y marcarla como leída
        window.abrirNotificacion = function(id, url) {
            fetch(notifContextPath + '/notificaciones?action=mark_read&id=' + id, {
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
            .then(function() {
                if (url && url !== 'null' && url.trim() !== '') {
                    var destino = url.startsWith('/') ? (notifContextPath + url) : (notifContextPath + '/' + url);
                    window.location.href = destino;
                } else {
                    cargarListaNotificaciones();
                }
            })
            .catch(function() {
                if (url && url !== 'null') {
                    window.location.href = notifContextPath + '/' + url;
                }
            });
        };

        // Eventos al interactuar con el dropdown
        document.addEventListener('DOMContentLoaded', function() {
            var bellBtn = document.getElementById('notifBellDrop');
            if (bellBtn) {
                bellBtn.addEventListener('click', function() {
                    activarAnimacionCampanita(false);
                    cargarListaNotificaciones();
                });
            }

            // Primera comprobación al cargar página
            verificarNotificaciones(true);

            // Bucle ligero en segundo plano cada 25 segundos
            setInterval(function() {
                verificarNotificaciones(false);
            }, 25000);
        });
    })();
</script>
</c:if>
