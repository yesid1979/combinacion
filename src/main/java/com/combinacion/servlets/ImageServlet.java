package com.combinacion.servlets;

import com.combinacion.services.GoogleDriveService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ImageServlet")
public class ImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String fileId = request.getParameter("id");
        if (fileId == null || fileId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Falta el parametro 'id'");
            return;
        }

        try {
            // Se puede optimizar con cache headers para evitar descargas multiples en el navegador
            response.setHeader("Cache-Control", "public, max-age=31536000"); // 1 ao
            response.setContentType("image/jpeg"); // Drive deduce el tipo internamente, forzamos imagen

            try (InputStream in = GoogleDriveService.downloadFile(fileId);
                 OutputStream out = response.getOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int length;
                while ((length = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, length);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // A veces, si no se encuentra o no hay permisos, falla.
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Imagen no encontrada o error en Drive");
        }
    }
}
