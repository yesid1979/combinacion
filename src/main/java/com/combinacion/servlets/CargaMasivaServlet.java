package com.combinacion.servlets;

import com.combinacion.services.CargaMasivaService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "CargaMasivaServlet", urlPatterns = { "/upload", "/google-sync" })
@MultipartConfig
public class CargaMasivaServlet extends HttpServlet {

    private CargaMasivaService cargaMasivaService = new CargaMasivaService();

    @Override
    public void init() throws ServletException {
        super.init();
        com.combinacion.util.DatabasePatcher.ensureSchema();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        cargaMasivaService.procesarCargaMasiva(request, response);
    }
}
