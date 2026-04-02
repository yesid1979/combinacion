<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Acceso al Sistema - DAGJP</title>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
    <style>
        html, body {
            height: 100%;
            margin: 0;
            overflow: hidden; /* CERO SCROLL */
        }
        body {
            background-color: #f8f9fa;
            font-family: 'Inter', sans-serif;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }
        .main-container {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 10px;
        }
        .login-card {
            background: #ffffff;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
            width: 100%;
            max-width: 440px;
            padding: 30px;
            border: 1px solid #eef0f2;
        }
        .login-title {
            color: #212529;
            font-weight: 700;
            text-align: center;
            margin-bottom: 20px;
        }
        .input-group {
            border: 1px solid #ced4da;
            border-radius: 10px;
            height: 50px;
            display: flex;
            align-items: center;
            margin-bottom: 15px;
            transition: all 0.2s;
        }
        .input-group-text {
            background: transparent !important;
            border: none !important;
            color: #004884;
            font-size: 1.2rem;
            width: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 0;
        }
        .form-control {
            border: none !important;
            box-shadow: none !important;
            height: 100%;
            font-size: 1.05rem;
            flex: 1;
            padding: 0 15px 0 5px;
        }
        .input-group:focus-within {
            border-color: #004884;
            box-shadow: 0 0 0 0.2rem rgba(0, 72, 132, 0.1);
        }
        .btn-login {
            background-color: #004884;
            border: none;
            border-radius: 10px;
            padding: 12px;
            font-weight: 600;
            width: 100%;
            color: white;
            transition: all 0.3s;
            height: 50px;
            font-size: 1.1rem;
        }
        .btn-login:hover {
            background-color: #003366;
            transform: translateY(-1px);
        }
        .footer-simple {
            margin-top: 0 !important;
            padding: 10px 0 !important;
            border-top: none !important;
        }
    </style>
</head>
<body>
    <div class="main-container">
        <div class="login-card">
            <div class="text-center mb-4">
                <img src="${pageContext.request.contextPath}/assets/img/logo_alcaldia.png" alt="Logo" style="height: 60px;">
                <h2 class="login-title mt-3">Sistema de Gestión Contractual del DAGJP</h2>
            </div>
            
            <form action="login" method="POST">
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-person"></i></span>
                    <input type="text" name="username" class="form-control" placeholder="Usuario" required>
                </div>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-lock"></i></span>
                    <input type="password" name="password" class="form-control" placeholder="Contraseña" required>
                </div>
                <button type="submit" class="btn-login">Iniciar Sesión</button>
            </form>

            <c:if test="${not empty error}">
                <div class="alert alert-danger mt-3 py-2 small text-center" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
                </div>
            </c:if>
        </div>
    </div>

    <!-- Footer Corporativo Minimalista -->
    <jsp:include page="inc/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
