<style>
    .footer-simple {
        margin-top: auto; /* Empuja el footer al final en layouts flex */
        padding: 12px 25px;
        font-family: 'Inter', sans-serif;
        color: #6c757d;
        background-color: #ffffff;
        box-shadow: 0 -4px 12px rgba(0,0,0,0.06);
        z-index: 1000;
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center; /* Centrar contenido principal */
    }
    .footer-simple span { font-size: 0.88rem; }
    .text-highlight { color: #007bff; font-weight: 600; }
    .footer-version { 
        position: absolute; 
        right: 25px; 
        font-weight: 600; 
        color: #adb5bd; 
    }
</style>

<footer class="footer-simple">
    <div class="text-center">
        <span>&copy; 2026 <span class="text-highlight">DAGJP</span>. Todos los derechos reservados. | Dise&ntilde;ado por: Ing. Yesid Javier Piedrahita Correa</span>
    </div>
    <div class="footer-version">
        <span>Versi&oacute;n 1.0</span>
    </div>
</footer>

<!-- Script para mantener la sesión activa y evitar pérdida de datos -->
<script>
    document.addEventListener("DOMContentLoaded", function() {
        // Enviar un "ping" (petición vacía) al servidor cada 5 minutos (300000 ms) 
        // para decirle que el usuario sigue ahí y no caduque su sesión.
        setInterval(function() {
            fetch(window.location.href, { method: 'HEAD', cache: 'no-store' })
                .catch(err => console.debug("Error en keep-alive", err));
        }, 300000); 

        // Auto-guardado temporal en el navegador (protege si cierran la pestaña por error)
        const form = document.querySelector('form');
        if (form) {
            const storageKey = 'form_backup_' + window.location.pathname;

            // Intentar recuperar datos guardados previamente
            const savedData = sessionStorage.getItem(storageKey);
            if (savedData) {
                try {
                    const data = JSON.parse(savedData);
                    Object.keys(data).forEach(key => {
                        const el = document.getElementsByName(key)[0];
                        if (el) {
                            if (el.type === 'checkbox' || el.type === 'radio') {
                                document.querySelectorAll(`[name="${key}"]`).forEach(e => {
                                    if (e.value === data[key]) e.checked = true;
                                });
                            } else {
                                el.value = data[key];
                                // Si es un textarea de summernote, actualizar visualmente
                                if ($(el).hasClass('summernote') || $(el).next().hasClass('note-editor')) {
                                    $(el).summernote('code', data[key]);
                                }
                            }
                        }
                    });
                } catch (e) { console.debug("No se pudo cargar el backup", e); }
            }

            // Guardar a medida que escriben
            form.addEventListener('input', function() {
                const formData = new FormData(form);
                const dataObj = {};
                formData.forEach((value, key) => {
                    const el = document.getElementsByName(key)[0];
                    if (el && el.type !== 'file' && el.type !== 'password' && el.type !== 'hidden') {
                        dataObj[key] = value;
                    }
                });
                sessionStorage.setItem(storageKey, JSON.stringify(dataObj));
            });
        }

        if (!form) {
            // Borrar los backups si la página actual no tiene un formulario (ej: vista de lista)
            Object.keys(sessionStorage).forEach(key => {
                if (key.startsWith('form_backup_')) {
                    sessionStorage.removeItem(key);
                }
            });
        } else {
            // Borrar el backup si vemos un parámetro de estado o éxito (ej: status=created)
            const urlParams = new URLSearchParams(window.location.search);
            if (urlParams.has('status')) {
                sessionStorage.removeItem('form_backup_' + window.location.pathname);
            }
        }
    });
</script>