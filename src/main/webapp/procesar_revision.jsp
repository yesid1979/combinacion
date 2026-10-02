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
        String revisorIdStr = request.getParameter("revisor_id");

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

                    // Actualizar el estado y el revisor si aplica
                    String sql = "UPDATE informes_supervision SET estado_radicacion = ?";
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
                        ps.setString(1, accion);
                        if (updateRevisor) {
                            ps.setInt(2, idRevisorNuevo);
                            ps.setInt(3, idInforme);
                        } else {
                            ps.setInt(2, idInforme);
                        }
                        
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
                            
                            // Notificación por correo al contratista si fue devuelta o aprobada
                            if ("DEVUELTA".equals(accion) || "APROBADA PARA IMPRESION".equals(accion)) {
                                try {
                                    String emailContratista = null;
                                    String nombreContratista = "";
                                    String numCuota = "";
                                    String numContrato = "";
                                    String sqlEmail = "SELECT c.correo, c.nombre, i.numero_cuota, con.numero_contrato FROM informes_supervision i " +
                                                      "JOIN contratos con ON i.contrato_id = con.id " +
                                                      "JOIN contratistas c ON con.contratista_id = c.id " +
                                                      "WHERE i.id = ?";
                                    try (PreparedStatement psEmail = conn.prepareStatement(sqlEmail)) {
                                        psEmail.setInt(1, idInforme);
                                        try (java.sql.ResultSet rs = psEmail.executeQuery()) {
                                            if (rs.next()) {
                                                emailContratista = rs.getString("correo");
                                                nombreContratista = rs.getString("nombre");
                                                numCuota = rs.getString("numero_cuota");
                                                numContrato = rs.getString("numero_contrato");
                                            }
                                        }
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
                                    System.err.println("Error enviando email: " + ex.getMessage());
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
