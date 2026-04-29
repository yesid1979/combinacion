<style>
    .footer-simple {
        margin-top: auto; /* Empuja el footer al final en layouts flex */
        padding: 15px 25px;
        font-family: 'Inter', sans-serif;
        color: #adb5bd;
        border-top: 1px solid #f1f1f1;
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
    @media (max-width: 768px) {
        .footer-simple { flex-direction: column; text-align: center; gap: 5px; padding: 15px; }
        .footer-version { position: static; }
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