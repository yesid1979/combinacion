<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestionar Permisos: ${usuario_perms.nombreCompleto} - DAGJP</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <!-- DataTables CSS for advanced table features -->
    <link href="https://cdn.datatables.net/1.13.4/css/dataTables.bootstrap5.min.css" rel="stylesheet">
    <!-- DataTables Responsive CSS -->
    <link href="https://cdn.datatables.net/responsive/2.4.1/css/responsive.bootstrap5.min.css" rel="stylesheet">
    <!-- Custom Styles (The secret of the "Combinación" look) -->
    <link href="../assets/css/styles.css" rel="stylesheet">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
    <style>
        .matrix-card { background: #fff; border: 1px solid #dee2e6; border-radius: 12px; box-shadow: 0 10px 30px rgba(0,0,0,0.05); padding: 35px; }
        .table-data { border-collapse: separate; border-spacing: 0; width: 100%; margin-bottom: 20px !important; }
        .table-data thead th { background: #fdfdfe; color: #444; border-bottom: 2px solid #eaedf2; padding: 16px; font-size: 0.92rem; font-weight: 700; text-transform: none; }
        .table-data td { vertical-align: middle; padding: 14px; border-bottom: 1px solid #f2f5f9; }
        .mod-info { display: flex; align-items: center; gap: 12px; color: #2c3e50; font-weight: 600; font-size: 0.95rem; }
        .mod-icon { font-size: 1.2rem; min-width: 25px; text-align: center; opacity: 0.9; }
        
        /* SWITCH STYLE MEJORADO CON TEXTO SI/NO */
        .sw-box { position: relative; width: 68px; height: 30px; display: inline-block; }
        .sw-check { display: none; }
        .sw-btn { 
            display: block; width: 100%; height: 100%; border-radius: 6px; 
            background-color: #abb7b7; cursor: pointer; position: relative; 
            transition: all 0.3s ease; box-shadow: inset 0 1px 3px rgba(0,0,0,0.1);
        }
        .sw-btn:before { 
            content: ""; position: absolute; top: 4px; left: 4px; 
            width: 22px; height: 22px; border-radius: 4px; background: #fff;
            transition: all 0.3s ease; z-index: 2; box-shadow: 0 2px 5px rgba(0,0,0,0.2);
        }
        .sw-btn:after { 
            content: "No"; position: absolute; top: 50%; right: 8px; 
            transform: translateY(-50%); font-size: 0.75rem; font-weight: 850; color: #000;
            transition: all 0.3s;
        }
        
        .sw-check:checked + .sw-btn { background-color: #5acfa9; }
        .sw-check:checked + .sw-btn:before { left: 42px; }
        .sw-check:checked + .sw-btn:after { content: "Si"; left: 10px; right: auto; }

        .btn-exit { background: #00bcd4; color: #fff; font-weight: 700; padding: 12px 30px; border-radius: 6px; border: none; transition: 0.2s; }
        .btn-exit:hover { background: #00acc1; transform: translateY(-1px); box-shadow: 0 5px 15px rgba(0,188,212,0.3); }
        .btn-save-all { background: #5c6bc0; color: #fff; font-weight: 700; padding: 12px 40px; border-radius: 6px; border: none; transition: 0.2s; }
        .btn-save-all:hover { background: #3f51b5; transform: translateY(-1px); box-shadow: 0 5px 15px rgba(92,107,192,0.3); }
        
        /* DATA TABLES - CABECERA CORPORATIVA OSCURA */
        .table-data thead th { 
            background: #212529 !important; 
            color: #ffffff !important;
            border: none !important;
            vertical-align: middle;
        }
        .dataTables_wrapper .dataTables_length { float: left; margin-bottom: 20px; font-size: 0.88rem; font-weight: 500; display: flex; align-items: center; height: 34px; }
        .dataTables_wrapper .dataTables_filter { float: right; margin-bottom: 20px; font-size: 0.88rem; font-weight: 500; display: flex; align-items: center; height: 34px; }
        .dataTables_wrapper .dataTables_filter input { width: 220px !important; height: 34px !important; border: 1px solid #ced4da !important; border-radius: 4px; padding: 0 12px; margin-left: 8px; }
        .dataTables_length select { height: 32px !important; border: 1px solid #ced4da !important; border-radius: 4px; padding: 0 8px !important; margin: 0 5px; vertical-align: middle; background-image: none !important; }
        
        .dataTables_wrapper:after { content: ""; display: block; clear: both; }
        
        .dataTables_info { float: left; font-size: 0.85rem; color: #6c757d; padding-top: 15px; }
        .dataTables_paginate { float: right; padding-top: 15px; }
        
        /* DATA TABLES - ELIMINAR FILAS DE BOOTSTRAP QUE DISTORSIONAN */
        .dataTables_wrapper .row { display: block !important; margin: 0 !important; }
        .dataTables_wrapper .col-sm-12, .dataTables_wrapper .col-md-6 { width: auto !important; padding: 0 !important; display: inline-block !important; }
    </style>
</head>

<body class="bg-light">
    <jsp:include page="/inc/navbar.jsp" />

    <div class="container mt-5 mb-5 pb-5">
        <div class="matrix-card">
            <h2 class="h3 mb-4 text-secondary d-flex align-items-center">
                <i class="bi bi-shield-lock-fill me-3 text-primary fs-2"></i> 
                Permisos para: <span class="ms-2 text-dark fw-bold">${usuario_perms.nombreCompleto}</span>
            </h2>
            
            <form action="${pageContext.request.contextPath}/admin/usuarios" method="POST">
                <input type="hidden" name="action" value="updatePermissions">
                <input type="hidden" name="id" value="${usuario_perms.id}">

                <div class="table-responsive">
                    <table class="table-data table-hover" id="tablaPermisos">
                        <thead>
                            <tr>
                                <th style="width: 50px;">No.</th>
                                <th>Módulo del Sistema</th>
                                <th class="text-center">Ver</th>
                                <th class="text-center">Crear</th>
                                <th class="text-center">Actualizar</th>
                                <th class="text-center">Eliminar</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="moduloItem" items="${modulos}" varStatus="loop">
                                <c:set var="label" value="${moduloItem}" />
                                <c:set var="icon" value="bi-app-indicator" />
                                
                                <c:choose>
                                    <c:when test="${moduloItem == 'ADMINISTRACION'}">
                                        <c:set var="label" value="Administración" />
                                        <c:set var="icon" value="bi-gear text-danger" />
                                    </c:when>
                                    <c:when test="${moduloItem == 'CARGA_MASIVA'}">
                                        <c:set var="label" value="Carga Masiva" />
                                        <c:set var="icon" value="bi-cloud-upload text-warning" />
                                    </c:when>
                                    <c:when test="${moduloItem == 'COMBINACION'}">
                                        <c:set var="label" value="Combinación" />
                                        <c:set var="icon" value="bi-file-earmark-word text-primary" />
                                    </c:when>
                                    <c:when test="${moduloItem == 'CONTRATISTAS'}">
                                        <c:set var="label" value="Contratistas" />
                                        <c:set var="icon" value="bi-people text-success" />
                                    </c:when>
                                    <c:when test="${moduloItem == 'CONTRATOS'}">
                                        <c:set var="label" value="Contratos" />
                                        <c:set var="icon" value="bi-file-earmark-text text-primary" />
                                    </c:when>
                                    <c:when test="${moduloItem == 'ORDENADORES'}">
                                        <c:set var="label" value="Ordenadores gasto" />
                                        <c:set var="icon" value="bi-briefcase text-secondary" />
                                    </c:when>
                                    <c:when test="${moduloItem == 'PRESUPUESTO'}">
                                        <c:set var="label" value="Presupuesto" />
                                        <c:set var="icon" value="bi-graph-up text-info" />
                                    </c:when>
                                    <c:when test="${moduloItem == 'SUPERVISORES'}">
                                        <c:set var="label" value="Supervisores" />
                                        <c:set var="icon" value="bi-person-vcard text-dark" />
                                    </c:when>
                                </c:choose>

                                <tr>
                                    <td class="text-secondary font-monospace">${loop.count}</td>
                                    <td>
                                        <div class="mod-info">
                                            <i class="bi ${icon} mod-icon"></i>
                                            <span>${label}</span>
                                        </div>
                                    </td>
                                    
                                    <c:forEach var="act" items="${['Ver', 'Crear', 'Editar', 'Eliminar']}">
                                        <td class="text-center">
                                            <c:set var="permisoItem" value="${null}" />
                                            <c:forEach var="p" items="${todosPermisos}">
                                                <c:if test="${p.modulo == moduloItem && permisoItem == null}">
                                                    <c:set var="c" value="${fn:toUpperCase(p.codigo)}" />
                                                    <c:set var="n" value="${fn:toUpperCase(p.nombre)}" />
                                                    <c:choose>
                                                        <c:when test="${act == 'Ver' && (fn:endsWith(c, '_VER') || fn:contains(n, 'VER ') || fn:contains(c, 'USUARIOS'))}">
                                                            <c:set var="permisoItem" value="${p}" />
                                                        </c:when>
                                                        <c:when test="${act == 'Crear' && (fn:endsWith(c, '_CREAR') || fn:contains(n, 'CREAR') || fn:contains(c, 'ROLES') || fn:contains(n, 'AGREGAR'))}">
                                                            <c:set var="permisoItem" value="${p}" />
                                                        </c:when>
                                                        <c:when test="${act == 'Editar' && (fn:endsWith(c, '_EDITAR') || fn:contains(n, 'EDITAR') || fn:contains(n, 'MODIFICAR') || fn:contains(n, 'ACTUALIZAR'))}">
                                                            <c:set var="permisoItem" value="${p}" />
                                                        </c:when>
                                                        <c:when test="${act == 'Eliminar' && (fn:endsWith(c, '_ELIMINAR') || fn:contains(n, 'ELIMINAR') || fn:contains(n, 'BORRAR'))}">
                                                            <c:set var="permisoItem" value="${p}" />
                                                        </c:when>
                                                    </c:choose>
                                                </c:if>
                                            </c:forEach>
                                            
                                            <c:if test="${permisoItem != null}">
                                                <c:set var="check" value="${false}" />
                                                <c:set var="espEx" value="${false}" />
                                                <c:forEach var="uep" items="${usuario_perms.permisosEspeciales}">
                                                    <c:if test="${uep.id == permisoItem.id}"><c:set var="check" value="${true}" /><c:set var="espEx" value="${true}" /></c:if>
                                                    <c:if test="${uep.modulo == moduloItem}"><c:set var="espEx" value="${true}" /></c:if>
                                                </c:forEach>
                                                <c:if test="${not espEx}">
                                                    <c:if test="${usuario_perms.rol.tienePermiso(permisoItem.codigo)}"><c:set var="check" value="${true}" /></c:if>
                                                </c:if>

                                                <div class="sw-box">
                                                    <input class="sw-check" type="checkbox" name="permisos" 
                                                           value="${permisoItem.id}" id="s_${permisoItem.id}"
                                                           ${check ? 'checked' : ''}
                                                           ${usuario_perms.rol.nombre == 'Administrador del Sistema' ? 'disabled' : ''}>
                                                    <label for="s_${permisoItem.id}" class="sw-btn" 
                                                           style="${usuario_perms.rol.nombre == 'Administrador del Sistema' ? 'cursor: not-allowed; opacity: 0.8;' : ''}"></label>
                                                </div>
                                            </c:if>
                                        </td>
                                    </c:forEach>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <div class="mt-5 d-flex justify-content-between align-items-center">
                    <a href="${pageContext.request.contextPath}/admin/usuarios" class="btn btn-exit">
                        <i class="bi bi-reply-all me-1"></i> Cerrar
                    </a>
                    <button type="submit" class="btn btn-save-all shadow-lg">
                        <i class="bi bi-check-circle-fill me-2"></i> ACTUALIZAR PERMISOS
                    </button>
                </div>
            </form>
        </div>
    </div>

    <jsp:include page="/inc/footer.jsp" />
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.4/js/dataTables.bootstrap5.min.js"></script>
    <script src="https://cdn.datatables.net/responsive/2.4.1/js/dataTables.responsive.min.js"></script>
    <script src="https://cdn.datatables.net/responsive/2.4.1/js/responsive.bootstrap5.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    
    <script>
        $(document).ready(function() {
            $('#tablaPermisos').DataTable({
                columnDefs: [
                    { 
                        targets: [2, 3, 4, 5], 
                        orderable: false 
                    }
                ],
                language: { url: 'https://cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json' },
                "processing": true,
                "serverSide": false,
                "responsive": true,
                "autoWidth": false,
                dom: 'lfrtip',
                pageLength: 25
            });
        });
    </script>
</body>
</html>
