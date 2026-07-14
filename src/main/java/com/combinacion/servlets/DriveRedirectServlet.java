package com.combinacion.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "DriveRedirectServlet", urlPatterns = {"/redirect"})
public class DriveRedirectServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = request.getParameter("url");
        if (url == null || url.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing URL");
            return;
        }

        // Sanitize to prevent XSS in the HTML output, although it should be a Google Drive URL
        url = url.replace("\"", "&quot;").replace("'", "\\'");

        // Output an HTML page with JavaScript redirect to bypass Word/Office SafeLinks
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Redirigiendo a Google Drive...</title></head>");
            out.println("<body style='font-family: Arial, sans-serif; text-align: center; margin-top: 50px;'>");
            out.println("    <h3>Redirigiendo a Google Drive...</h3>");
            out.println("    <p>Si no eres redirigido automáticamente en unos segundos, <a id='manualLink' href=\"" + url + "\">haz clic aquí</a>.</p>");
            out.println("    <script>");
            // JavaScript execution will be blocked by MS Word's internal browser,
            // but will execute normally when MS Word passes the page to Chrome/Edge.
            out.println("        window.location.href = '" + url + "';");
            out.println("    </script>");
            out.println("</body>");
            out.println("</html>");
        }
    }
}
