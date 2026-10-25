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
    boolean esSupervisor = u.tienePermiso("SUPERVISOR_CUENTAS") || (u.getRol() != null && "Supervisor".equalsIgnoreCase(u.getRol().getNombre()));

    if (!esAdminCuentas && !esRevisor && !esSupervisor) {
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
        String revisorIdStr = request.getParameter("revisor_id");

        if (idStr != null && accion != null) {
            int idInforme = 0;
            try {
                idInforme = Integer.parseInt(idStr);
            } catch (Exception e) {}

            if (idInforme > 0) {
                try (Connection conn = DBConnection.getConnection()) {
                    // Parche: Agregar columnas observaciones_revision y estado_previo_devolucion si no existen
                    try {
                        conn.createStatement().execute("ALTER TABLE informes_supervision ADD COLUMN observaciones_revision TEXT");
                    } catch (Exception ignore) {}
                    try {
                        conn.createStatement().execute("ALTER TABLE informes_supervision ADD COLUMN estado_previo_devolucion VARCHAR(50)");
                    } catch (Exception ignore) {}

                    // Obtener el estado anterior
                    String estadoAnterior = "RADICADA";
                    try (PreparedStatement psSel = conn.prepareStatement("SELECT estado_radicacion FROM informes_supervision WHERE id = ?")) {
                        psSel.setInt(1, idInforme);
                        try (java.sql.ResultSet rs = psSel.executeQuery()) {
                            if (rs.next()) {
                                estadoAnterior = rs.getString("estado_radicacion");
                            }
                        }
                    }

                    // Actualizar el estado y el revisor si aplica
                    String sql = "UPDATE informes_supervision SET estado_radicacion = ?";
                    if ("DEVUELTA".equals(accion)) {
                        sql += ", estado_previo_devolucion = ?";
                    } else {
                        sql += ", estado_previo_devolucion = NULL";
                    }

                    boolean updateRevisor = false;
                    int idRevisorNuevo = 0;
                    if ("VISTO BUENO CONTRATACION".equals(accion) && revisorIdStr != null && !revisorIdStr.trim().isEmpty()) {
                        try {
                            idRevisorNuevo = Integer.parseInt(revisorIdStr);
                            if (idRevisorNuevo > 0) {
                                sql += ", id_revisor_asignado = ?";
                                updateRevisor = true;
                            }
                        } catch (Exception e) {}
                    }
                    sql += " WHERE id = ?";
                    
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        int paramIdx = 1;
                        ps.setString(paramIdx++, accion);
                        if ("DEVUELTA".equals(accion)) {
                            ps.setString(paramIdx++, estadoAnterior);
                        }
                        if (updateRevisor) {
                            ps.setInt(paramIdx++, idRevisorNuevo);
                        }
                        ps.setInt(paramIdx++, idInforme);
                        
                        int affected = ps.executeUpdate();
                        if (affected > 0) {
                            com.combinacion.models.HistorialRadicacion hr = new com.combinacion.models.HistorialRadicacion();
                            hr.setIdInforme(idInforme);
                            hr.setIdUsuarioCambio(u.getId());
                            hr.setEstadoAnterior(estadoAnterior);
                            hr.setEstadoNuevo(accion);
                            hr.setObservaciones(observacion != null && !observacion.trim().isEmpty() ? observacion.trim() : "Cuenta pasada a estado: " + accion);
                            new com.combinacion.dao.HistorialRadicacionDAO().registrarCambio(hr);
                            
                            // Auditoría del Sistema
                            com.combinacion.dao.AuditoriaDAO.registrar(u, "Cambio de Estado", "La cuenta de cobro ID " + idInforme + " pasó a estado: " + accion, request.getRemoteAddr());
                            
                            // Notificación en el sistema y por correo al contratista si fue devuelta o aprobada
                            if ("DEVUELTA".equals(accion) || "APROBADA PARA IMPRESION".equals(accion)) {
                                try {
                                    String emailContratista = null;
                                    String nombreContratista = "";
                                    String cedContratista = "";
                                    String numCuota = "";
                                    String numContrato = "";
                                    String sqlEmail = "SELECT COALESCE(NULLIF(TRIM(u.correo), ''), NULLIF(TRIM(c.correo), '')) as correo, c.nombre, c.cedula, i.numero_cuota, con.numero_contrato FROM informes_supervision i " +
                                                      "JOIN contratos con ON i.contrato_id = con.id " +
                                                      "JOIN contratistas c ON con.contratista_id = c.id " +
                                                      "LEFT JOIN usuarios u ON regexp_replace(u.cedula, '[^0-9]', '', 'g') = regexp_replace(c.cedula, '[^0-9]', '', 'g') AND u.activo = true " +
                                                      "WHERE i.id = ?";
                                    try (PreparedStatement psEmail = conn.prepareStatement(sqlEmail)) {
                                        psEmail.setInt(1, idInforme);
                                        try (java.sql.ResultSet rs = psEmail.executeQuery()) {
                                            if (rs.next()) {
                                                emailContratista = rs.getString("correo");
                                                nombreContratista = rs.getString("nombre");
                                                cedContratista = rs.getString("cedula");
                                                numCuota = rs.getString("numero_cuota");
                                                numContrato = rs.getString("numero_contrato");
                                            }
                                        }
                                    }

                                    // Notificación en campanita para el contratista
                                    com.combinacion.dao.NotificacionDAO notifDAO = new com.combinacion.dao.NotificacionDAO();
                                    if ("DEVUELTA".equals(accion)) {
                                        notifDAO.crearParaUsuarioPorCedula(cedContratista, idInforme, "⚠️ Cuenta Devuelta", "Su cuenta del Contrato " + numContrato + " (Cuota " + numCuota + ") fue devuelta con observaciones.", "DANGER", "informes?action=edit&id=" + idInforme);
                                    } else {
                                        notifDAO.crearParaUsuarioPorCedula(cedContratista, idInforme, "✅ Cuenta Aprobada", "Su cuenta del Contrato " + numContrato + " (Cuota " + numCuota + ") ha sido aprobada para impresión.", "SUCCESS", "informes?action=edit&id=" + idInforme);
                                    }
                                    
                                    if (emailContratista != null && !emailContratista.trim().isEmpty()) {
                                        String subject;
                                        String bodyHtml;
                                        String systemLink = "https://juridica.cali.gov.co/combinacion/";
                                        
                                        if ("DEVUELTA".equals(accion)) {
                                            subject = "⚠️ Su Cuenta de Cobro fue Devuelta - Contrato " + numContrato;
                                            bodyHtml = "<html><body style='font-family: Arial, sans-serif; color: #333;'>"
                                                    + "<div style='max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>"
                                                    + "<div style='background-color: #D32F2F; color: #fff; padding: 20px; text-align: center;'>"
                                                    + "<h2 style='margin: 0;'>Cuenta de Cobro Devuelta</h2>"
                                                    + "</div>"
                                                    + "<div style='padding: 20px;'>"
                                                    + "<p>Hola <strong>" + nombreContratista + "</strong>,</p>"
                                                    + "<p>Le informamos que su cuenta de cobro del <strong>Contrato " + numContrato + "</strong> correspondiente a la <strong>Cuota " + numCuota + "</strong> ha sido revisada y <strong>devuelta con observaciones</strong>.</p>"
                                                    + "<div style='background-color: #ffebee; border-left: 4px solid #D32F2F; padding: 15px; margin-top: 20px;'>"
                                                    + "<h4 style='margin-top: 0; color: #D32F2F;'>Motivo de devolución:</h4>"
                                                    + "<p style='margin-bottom: 0;'><em>\"" + observacion + "\"</em></p>"
                                                    + "</div>"
                                                    + "<p style='margin-top: 20px;'>Por favor ingrese al sistema para verificar las correcciones solicitadas y vuelva a radicarla una vez ajustada.</p>"
                                                    + "<div style='text-align: center; margin-top: 30px;'><a href='" + systemLink + "' style='background-color: #D32F2F; color: #fff; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Ver Detalles e Ingresar</a></div>"
                                                    + "</div>"
                                                    + "<div style='background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #777;'>"
                                                    + "<p>Sistema de Gestión Contractual del DAGJP<br>Este es un mensaje automático, por favor no responda.</p>"
                                                    + "</div></div></body></html>";
                                        } else {
                                            subject = "✅ Cuenta de Cobro Aprobada - Contrato " + numContrato;
                                            bodyHtml = "<html><body style='font-family: Arial, sans-serif; color: #333;'>"
                                                    + "<div style='max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>"
                                                    + "<div style='background-color: #388E3C; color: #fff; padding: 20px; text-align: center;'>"
                                                    + "<h2 style='margin: 0;'>Cuenta de Cobro Aprobada</h2>"
                                                    + "</div>"
                                                    + "<div style='padding: 20px;'>"
                                                    + "<p>Hola <strong>" + nombreContratista + "</strong>,</p>"
                                                    + "<p>Nos complace informarle que su cuenta de cobro del <strong>Contrato " + numContrato + "</strong> correspondiente a la <strong>Cuota " + numCuota + "</strong> ha superado la revisión exitosamente y ha sido APROBADA PARA IMPRESIÓN.</p>"
                                                    + "<p style='margin-top: 20px;'>Ya puede ingresar a la plataforma, descargar los formatos (Informe de Supervisión y de Gestión), imprimirlos, firmarlos y continuar con el trámite correspondiente.</p>"
                                                    + "<div style='text-align: center; margin-top: 30px;'><a href='" + systemLink + "' style='background-color: #388E3C; color: #fff; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Ingresar al Sistema</a></div>"
                                                    + "</div>"
                                                    + "<div style='background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #777;'>"
                                                    + "<p>Sistema de Gestión Contractual del DAGJP<br>Este es un mensaje automático, por favor no responda.</p>"
                                                    + "</div></div></body></html>";
                                        }
                                        
                                        final String finalEmail = emailContratista;
                                        final String finalSubject = subject;
                                        final String finalBody = bodyHtml;
                                        new Thread(() -> {
                                            com.combinacion.services.EmailService.sendEmailHtml(finalEmail, finalSubject, finalBody);
                                        }).start();
                                    }
                                } catch (Exception ex) {
                                    System.err.println("Error enviando email contratista: " + ex.getMessage());
                                }
                            } else if ("EN REVISION SUPERVISOR".equals(accion)) {
                                try {
                                    String emailSup = null;
                                    String nombreSup = "";
                                    String numContrato = "";
                                    String numCuota = "";
                                    String cedSup = null;
                                    String sqlSup = "SELECT s.cedula, s.nombre, i.numero_cuota, con.numero_contrato FROM informes_supervision i " +
                                                    "JOIN contratos con ON i.contrato_id = con.id " +
                                                    "JOIN supervisores s ON s.id = COALESCE(i.supervisor_id, con.supervisor_id) " +
                                                    "WHERE i.id = ?";
                                    try (PreparedStatement psSup = conn.prepareStatement(sqlSup)) {
                                        psSup.setInt(1, idInforme);
                                        try (java.sql.ResultSet rs = psSup.executeQuery()) {
                                            if (rs.next()) {
                                                cedSup = rs.getString("cedula");
                                                nombreSup = rs.getString("nombre");
                                                numCuota = rs.getString("numero_cuota");
                                                numContrato = rs.getString("numero_contrato");
                                                if (cedSup != null) {
                                                    String cedLimpia = cedSup.replaceAll("[^0-9]", "");
                                                    try (PreparedStatement psUsr = conn.prepareStatement("SELECT correo FROM usuarios WHERE regexp_replace(cedula, '[^0-9]', '', 'g') = ? AND activo = true LIMIT 1")) {
                                                        psUsr.setString(1, cedLimpia);
                                                        try (java.sql.ResultSet rsUsr = psUsr.executeQuery()) {
                                                            if (rsUsr.next()) {
                                                                emailSup = rsUsr.getString("correo");
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Notificación en campanita al supervisor
                                    if (cedSup != null) {
                                        new com.combinacion.dao.NotificacionDAO().crearParaUsuarioPorCedula(cedSup, idInforme, "📋 Cuenta para Supervisión", "La cuenta del Contrato " + numContrato + " (Cuota " + numCuota + ") está en su bandeja de supervisión.", "INFO", "informes?action=edit&id=" + idInforme);
                                    }

                                    if (emailSup != null && !emailSup.trim().isEmpty()) {
                                        String systemLink = "https://juridica.cali.gov.co/combinacion/";
                                        String subject = "📋 Cuenta de Cobro Pendiente de Aprobación - Contrato " + numContrato;
                                        String bodyHtml = "<html><body style='font-family: Arial, sans-serif; color: #333;'>"
                                                + "<div style='max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>"
                                                + "<div style='background-color: #004884; color: #fff; padding: 20px; text-align: center;'>"
                                                + "<h2 style='margin: 0;'>Cuenta Pendiente de Aprobación</h2>"
                                                + "</div>"
                                                + "<div style='padding: 20px;'>"
                                                + "<p>Hola <strong>" + nombreSup + "</strong>,</p>"
                                                + "<p>La cuenta de cobro del <strong>Contrato " + numContrato + "</strong> correspondiente a la <strong>Cuota " + numCuota + "</strong> ha superado la revisión de Contratación y se encuentra lista para su <strong>aprobación definitiva para impresión</strong>.</p>"
                                                + "<p style='margin-top: 20px;'>Por favor ingrese al sistema para verificar y dar la aprobación definitiva.</p>"
                                                + "<div style='text-align: center; margin-top: 30px;'><a href='" + systemLink + "' style='background-color: #004884; color: #fff; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Ingresar al Sistema</a></div>"
                                                + "</div>"
                                                + "<div style='background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #777;'>"
                                                + "<p>Sistema de Gestión Contractual del DAGJP<br>Este es un mensaje automático, por favor no responda.</p>"
                                                + "</div></div></body></html>";
                                        
                                        final String finalEmailSup = emailSup;
                                        final String finalSubjectSup = subject;
                                        final String finalBodySup = bodyHtml;
                                        new Thread(() -> {
                                            com.combinacion.services.EmailService.sendEmailHtml(finalEmailSup, finalSubjectSup, finalBodySup);
                                        }).start();
                                    }
                                } catch (Exception ex) {
                                    System.err.println("Error notificando al supervisor: " + ex.getMessage());
                                }
                            } else if ("EN REVISION CONTRATACION".equals(accion)) {
                                try {
                                    new com.combinacion.dao.NotificacionDAO().crearParaRol("Contratacion", idInforme, "🏛️ Cuenta en Contratación", "Una cuenta de cobro pasó a revisión de Contratación.", "INFO", "informes?action=edit&id=" + idInforme);
                                } catch (Exception ex) {
                                    System.err.println("Error notificando a contratacion: " + ex.getMessage());
                                }
                            }

                            // Si se asignó un revisor en Visto Bueno de Contratación
                            if (updateRevisor && idRevisorNuevo > 0) {
                                try {
                                    new com.combinacion.dao.NotificacionDAO().crear(new com.combinacion.models.Notificacion(idRevisorNuevo, idInforme, "📥 Cuenta Asignada", "Se le ha asignado una cuenta de cobro para revisión.", "INFO", "informes?action=edit&id=" + idInforme));
                                } catch (Exception ex) {
                                    System.err.println("Error notificando a revisor asignado: " + ex.getMessage());
                                }
                            }
                            
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
