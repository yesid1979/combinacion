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
</script>
