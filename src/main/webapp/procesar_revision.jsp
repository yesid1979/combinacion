<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="com.combinacion.util.DBConnection" %>
<%@ page import="com.combinacion.models.Usuario" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%
    Usuario u = (Usuario) session.getAttribute("usuario");
    if (u == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    boolean esAdminCuentas = u.tienePermiso("ADMINISTRAR_CUENTAS_EDITAR") || u.tienePermiso("ADMINISTRAR_CUENTAS") || u.esAdministrador();
    boolean esRevisor = u.tienePermiso("PUEDE_REVISAR_CUENTAS") || u.tienePermiso("REVISION_CUENTAS_VER");

    if (!esAdminCuentas && !esRevisor) {
        session.setAttribute("error", "No tienes permisos para realizar esta acción.");
        String errorUrl = "informes";
        String errModo = request.getParameter("modo");
        if (errModo != null && !errModo.trim().isEmpty()) {
            errorUrl += "?modo=" + errModo;
        }
        response.sendRedirect(errorUrl);
        return;
    }

    if ("POST".equalsIgnoreCase(request.getMethod())) {
        String idStr = request.getParameter("id_informe");
        String accion = request.getParameter("accion");
        String observacion = request.getParameter("observacion");

        if (idStr != null && accion != null) {
            int idInforme = 0;
            try {
                idInforme = Integer.parseInt(idStr);
            } catch (Exception e) {}

            if (idInforme > 0) {
                try (Connection conn = DBConnection.getConnection()) {
                    // Parche: Agregar columna observaciones_revision si no existe
                    try {
                        conn.createStatement().execute("ALTER TABLE informes_supervision ADD COLUMN observaciones_revision TEXT");
                    } catch (Exception ignore) {}

                    // Obtener la observación anterior (si existe) para armar el histórico (para migracion o fallback, opcional)
                    String estadoAnterior = "RADICADA";
                    try (PreparedStatement psSel = conn.prepareStatement("SELECT estado_radicacion FROM informes_supervision WHERE id = ?")) {
                        psSel.setInt(1, idInforme);
                        try (java.sql.ResultSet rs = psSel.executeQuery()) {
                            if (rs.next()) {
                                estadoAnterior = rs.getString("estado_radicacion");
                            }
                        }
                    }

                    // Actualizar el estado
                    String sql = "UPDATE informes_supervision SET estado_radicacion = ? WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, accion);
                        ps.setInt(2, idInforme);
                        
                        int affected = ps.executeUpdate();
                        if (affected > 0) {
                            com.combinacion.models.HistorialRadicacion hr = new com.combinacion.models.HistorialRadicacion();
                            hr.setIdInforme(idInforme);
                            hr.setIdUsuarioCambio(u.getId());
                            hr.setEstadoAnterior(estadoAnterior);
                            hr.setEstadoNuevo(accion);
                            hr.setObservaciones(observacion != null && !observacion.trim().isEmpty() ? observacion.trim() : "Cuenta pasada a estado: " + accion);
                            new com.combinacion.dao.HistorialRadicacionDAO().registrarCambio(hr);
                            
                            session.setAttribute("successMessage", "La cuenta de cobro fue actualizada a estado: " + accion);
                        } else {
                            session.setAttribute("error", "No se encontró la cuenta o no se pudo actualizar.");
                        }
                    }
                } catch (Exception e) {
                    session.setAttribute("error", "Error al procesar la revisión: " + e.getMessage());
                }
            }
        }
    }
    
    String url = "informes";
    String modoParams = request.getParameter("modo");
    if (modoParams != null && !modoParams.trim().isEmpty()) {
        url += "?modo=" + modoParams;
    }
    response.sendRedirect(url);
%>
