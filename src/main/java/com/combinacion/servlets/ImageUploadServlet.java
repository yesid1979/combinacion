package com.combinacion.servlets;

import com.combinacion.services.GoogleDriveService;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.json.JSONObject;

@WebServlet("/ImageUploadServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 1024 * 1024 * 5,
    maxRequestSize = 1024 * 1024 * 10
)
public class ImageUploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject jsonResponse = new JSONObject();
        
        try {
            Part filePart = request.getPart("file");
            if (filePart != null && filePart.getSize() > 0) {
                String submittedFileName = filePart.getSubmittedFileName();
                if (submittedFileName == null || submittedFileName.trim().isEmpty()) {
                    submittedFileName = "imagen_editor_" + System.currentTimeMillis() + ".jpg";
                }
                
                String mimeType = filePart.getContentType() != null ? filePart.getContentType() : "image/jpeg";
                
                // Asegurarse de tener la carpeta IMAGENES_EDITOR_WEB en Drive
                String masterFolderId = GoogleDriveService.getOrCreateFolder("configuracion_SistemaContratacion", null);
                String folderId = GoogleDriveService.getOrCreateFolder("IMAGENES_EDITOR_WEB", masterFolderId);
                
                // Subir a Drive
                try (InputStream is = filePart.getInputStream()) {
                    String fileId = GoogleDriveService.uploadStreamToDrive(is, filePart.getSize(), submittedFileName, mimeType, folderId);
                    
                    if (fileId != null && !fileId.isEmpty()) {
                        // Responder con la URL de nuestro Servlet de visualizacion
                        String imageUrl = request.getContextPath() + "/ImageServlet?id=" + fileId;
                        jsonResponse.put("url", imageUrl);
                        jsonResponse.put("success", true);
                        response.getWriter().write(jsonResponse.toString());
                        return;
                    }
                }
            }
            
            jsonResponse.put("success", false);
            jsonResponse.put("error", "No se recibio ninguna imagen");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(jsonResponse.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("error", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(jsonResponse.toString());
        }
    }
}
