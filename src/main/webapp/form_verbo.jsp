<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${verbo == null ? 'Nuevo' : 'Editar'} Verbo - DAGJP</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <link href="assets/css/styles.css" rel="stylesheet">
</head>
<body class="bg-light">

    <%@ include file="inc/navbar.jsp" %>

    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-md-8 col-lg-6">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-white border-bottom-0 pt-4 pb-0 text-center">
                        <h2 class="h4 mb-0 text-primary">
                            <i class="bi bi-translate me-2"></i>${verbo == null ? 'Registrar Nuevo' : 'Editar'} Verbo
                        </h2>
                        <p class="text-muted small mt-2 mb-0">Define cómo se conjugarán las actividades en los informes.</p>
                    </div>
                    
                    <div class="card-body p-4">
                        <form action="${pageContext.request.contextPath}/verbos" method="post">
                            <input type="hidden" name="action" value="${verbo == null ? 'insert' : 'update'}">
                            <c:if test="${verbo != null}">
                                <input type="hidden" name="id" value="${verbo.id}">
                            </c:if>

                            <div class="mb-4">
                                <label for="terceraPersona" class="form-label fw-bold">Verbo en 3ra Persona (Entrada)</label>
                                <div class="input-group">
                                    <span class="input-group-text bg-light"><i class="bi bi-chat-left-text text-muted"></i></span>
                                    <input type="text" class="form-control" id="terceraPersona" name="terceraPersona" 
                                           value="${verbo.terceraPersona}" placeholder="Ejemplo: realizó, brindó, apoyó" required>
                                </div>
                                <div class="form-text">Palabra exacta que usa el contratista en su informe. Puede escribirse en minúsculas.</div>
                            </div>

                            <div class="mb-4">
                                <label for="primeraPersona" class="form-label fw-bold">Verbo en 1ra Persona (Salida)</label>
                                <div class="input-group">
                                    <span class="input-group-text bg-light"><i class="bi bi-chat-right-text text-primary"></i></span>
                                    <input type="text" class="form-control" id="primeraPersona" name="primeraPersona" 
                                           value="${verbo.primeraPersona}" placeholder="Ejemplo: realicé, brindé, apoyé" required>
                                </div>
                                <div class="form-text">Cómo debe aparecer en el Word generado.</div>
                            </div>

                            <div class="form-check form-switch mb-4">
                                <input class="form-check-input" type="checkbox" id="activo" name="activo" 
                                       ${verbo == null || verbo.activo ? 'checked' : ''}>
                                <label class="form-check-label fw-bold" for="activo">Verbo Activo</label>
                            </div>

                            <div class="d-grid gap-2 d-md-flex justify-content-md-end mt-4">
                                <a href="${pageContext.request.contextPath}/verbos" class="btn btn-light border px-4">Cancelar</a>
                                <button type="submit" class="btn btn-primary px-4">
                                    <i class="bi bi-save me-2"></i>Guardar Verbo
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

