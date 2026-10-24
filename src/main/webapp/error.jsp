<%@page contentType="text/html" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Estado de Solicitud - Sistema de Cuentas</title>
    <jsp:include page="inc/head_assets.jsp" />
    <style>
        .error-card {
            max-width: 650px;
            margin: 60px auto;
            border-radius: 16px;
            border: none;
            box-shadow: 0 10px 30px rgba(0,0,0,0.08);
            overflow: hidden;
        }
        .error-header {
            background: linear-gradient(135deg, #004884 0%, #0d6efd 100%);
            color: #fff;
            padding: 35px 25px;
            text-align: center;
        }
        .error-icon {
            font-size: 4rem;
            line-height: 1;
            margin-bottom: 12px;
        }
    </style>
</head>
<body class="bg-light d-flex flex-column min-vh-100">

    <jsp:include page="inc/navbar.jsp" />

    <div class="container flex-grow-1 d-flex align-items-center justify-content-center py-4">
        <div class="card error-card w-100">
            <div class="error-header">
                <c:choose>
                    <c:when test="${pageContext.errorData.statusCode == 404}">
                        <div class="error-icon"><i class="bi bi-compass"></i></div>
                        <h2 class="fw-bold mb-1">Página no encontrada (404)</h2>
                        <p class="mb-0 opacity-75">El recurso o dirección que buscas no existe o ha sido movido.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="error-icon"><i class="bi bi-shield-exclamation"></i></div>
                        <h2 class="fw-bold mb-1">Ha ocurrido una eventualidad</h2>
                        <p class="mb-0 opacity-75">El sistema ha capturado la solicitud de forma segura.</p>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="card-body p-4 p-md-5 text-center">
                <p class="text-secondary mb-4">
                    Puedes regresar a la página principal de inicio o consultar las opciones del menú de navegación.
                </p>
                <div class="d-flex justify-content-center gap-2 flex-wrap">
                    <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-primary px-4 py-2 fw-semibold">
                        <i class="bi bi-house-door me-2"></i>Ir al Inicio
                    </a>
                    <button onclick="window.history.back();" class="btn btn-outline-secondary px-4 py-2 fw-semibold">
                        <i class="bi bi-arrow-left me-2"></i>Regresar
                    </button>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="inc/footer.jsp" />
    <jsp:include page="inc/scripts_assets.jsp" />
</body>
</html>
