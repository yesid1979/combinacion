package com.combinacion.servlets;

import com.combinacion.models.Contrato;
import com.combinacion.models.InformeSupervision;
import com.combinacion.services.InformeSupervisionService;
import com.combinacion.services.InformeSupervisionService.InformeFormData;
import com.combinacion.util.ParseUtils;
import com.combinacion.util.SupervisionReportGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet(name = "InformeSupervisionServlet", urlPatterns = { "/informes" })
@javax.servlet.annotation.MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class InformeSupervisionServlet extends HttpServlet {

    private final InformeSupervisionService informeService = new InformeSupervisionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "new":
                mostrarFormularioNuevo(request, response);
                break;
            case "view":
                mostrarDetalle(request, response);
                break;
            case "download":
                descargarInforme(request, response);
                break;
            case "edit":
                mostrarFormularioEdicion(request, response);
                break;
            default:
                listar(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if ("insert".equals(action)) {
            insertar(request, response);
        } else if ("update".equals(action)) {
            actualizar(request, response);
        } else {
            listar(request, response);
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        com.combinacion.models.Usuario u = (com.combinacion.models.Usuario) request.getSession().getAttribute("usuario");
        boolean esContratista = (u != null && (u.getRolId() == 3 || (u.getRol() != null && "Contratista".equalsIgnoreCase(u.getRol().getNombre()))));

        String contratoIdStr = request.getParameter("contrato_id");
        
        if (esContratista) {
            // Lógica para el Contratista: Buscar su contrato automáticamente
            com.combinacion.dao.ContratistaDAO cdao = new com.combinacion.dao.ContratistaDAO();
            
            // Buscar contratista ignorando puntos y letras en la base de datos
            java.util.List<com.combinacion.models.Contratista> todos = cdao.listarTodos();
            com.combinacion.models.Contratista c = null;
            for (com.combinacion.models.Contratista cont : todos) {
                if (cont.getCedula() != null) {
                    String limpiaDB = cont.getCedula().replaceAll("[^0-9]", "");
                    if (limpiaDB.equals(u.getCedula())) {
                        c = cont;
                        break;
                    }
                }
            }

            if (c != null) {
                com.combinacion.dao.ContratoDAO codao = new com.combinacion.dao.ContratoDAO();
                java.util.List<Contrato> misContratos = codao.listarPorContratistaId(c.getId());
                
                if (!misContratos.isEmpty()) {
                    request.setAttribute("misContratos", misContratos);
                    
                    if (contratoIdStr != null && !contratoIdStr.isEmpty()) {
                        int contratoId = ParseUtils.parseInt(contratoIdStr);
                        request.setAttribute("listaInformes", informeService.listarPorContrato(contratoId));
                        request.setAttribute("contrato", informeService.obtenerContrato(contratoId));
                    } else {
                        // Si tiene varios contratos pero no seleccionó uno, le mostramos todos sus informes y le asignamos su contrato más reciente por defecto para "Nuevo Informe" si solo tiene 1
                        request.setAttribute("contrato", misContratos.get(0));
                        
                        java.util.List<InformeSupervision> todosMisInformes = new java.util.ArrayList<>();
                        for(Contrato con : misContratos) {
                            todosMisInformes.addAll(informeService.listarPorContrato(con.getId()));
                        }
                        request.setAttribute("listaInformes", todosMisInformes);
                    }
                } else {
                    request.setAttribute("listaInformes", new java.util.ArrayList<>());
                    request.setAttribute("error", "No tienes ningún contrato activo asignado en el sistema.");
                }
            } else {
                request.setAttribute("listaInformes", new java.util.ArrayList<>());
                request.setAttribute("error", "No se encontraron tus datos como contratista.");
            }
        } else {
            // Lógica para Admin/Supervisor: Buscar por ID de contrato específico, o listar todos
            if (contratoIdStr != null && !contratoIdStr.isEmpty()) {
                int contratoId = ParseUtils.parseInt(contratoIdStr);
                request.setAttribute("listaInformes", informeService.listarPorContrato(contratoId));
                request.setAttribute("contrato", informeService.obtenerContrato(contratoId));
            } else {
                request.setAttribute("listaInformes", informeService.listarTodos());
            }
        }
        
        request.getRequestDispatcher("lista_informes.jsp").forward(request, response);
    }

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contratoIdStr = request.getParameter("contrato_id");
        if (contratoIdStr != null) {
            int contratoId = ParseUtils.parseInt(contratoIdStr);
            Contrato contrato = informeService.obtenerContrato(contratoId);
            request.setAttribute("contrato", contrato);
            if (contrato != null) {
                request.setAttribute("listaObligaciones", com.combinacion.util.ObligacionesParser.decodificarConcepto(null, contrato.getActividadesEntregables()));
                
                // Calcular acumulado previo y número de cuota sugerido
                java.util.List<com.combinacion.models.InformeSupervision> previos = informeService.listarPorContrato(contratoId);
                java.math.BigDecimal acumulado = java.math.BigDecimal.ZERO;
                if(previos != null && !previos.isEmpty()){
                    for(com.combinacion.models.InformeSupervision prev : previos){
                        if(prev.getValorCuotaPagar() != null){
                            acumulado = acumulado.add(prev.getValorCuotaPagar());
                        }
                    }
                }
                request.setAttribute("acumuladoPrevio", acumulado);
                request.setAttribute("siguienteCuota", previos != null ? previos.size() + 1 : 1);
            }
        }
        request.setAttribute("action", "insert");
        request.getRequestDispatcher("form_supervision.jsp").forward(request, response);
    }

    private void mostrarDetalle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = ParseUtils.parseInt(request.getParameter("id"));
        InformeSupervision informe = informeService.obtenerPorId(id);
        request.setAttribute("informe", informe);
        if (informe != null && informe.getContratoId() != null) {
            Contrato contrato = informeService.obtenerContrato(informe.getContratoId());
            request.setAttribute("contrato", contrato);
            if (contrato != null) {
                request.setAttribute("listaObligaciones", com.combinacion.util.ObligacionesParser.decodificarConcepto(informe.getConceptoSupervisor(), contrato.getActividadesEntregables()));
            }
        }
        request.setAttribute("readonly", true);
        request.setAttribute("action", "view");
        request.getRequestDispatcher("form_supervision.jsp").forward(request, response);
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = ParseUtils.parseInt(request.getParameter("id"));
        InformeSupervision informe = informeService.obtenerPorId(id);
        if (informe == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Informe no encontrado");
            return;
        }
        request.setAttribute("informe", informe);
        if (informe.getContratoId() != null) {
            Contrato contrato = informeService.obtenerContrato(informe.getContratoId());
            request.setAttribute("contrato", contrato);
            if (contrato != null) {
                request.setAttribute("listaObligaciones", com.combinacion.util.ObligacionesParser.decodificarConcepto(informe.getConceptoSupervisor(), contrato.getActividadesEntregables()));
            }
        }
        request.setAttribute("action", "update");
        request.getRequestDispatcher("form_supervision.jsp").forward(request, response);
    }

    private void descargarInforme(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = ParseUtils.parseInt(request.getParameter("id"));
            InformeSupervision informe = informeService.obtenerPorId(id);
            if (informe == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Informe no encontrado.");
                return;
            }

            Contrato contrato = informeService.obtenerContrato(informe.getContratoId());
            
            String nombreCompleto = contrato.getContratistaNombre() != null ? contrato.getContratistaNombre().trim() : "";
            String nombreCorto = nombreCompleto;
            String[] parts = nombreCompleto.split("\\s+");
            if (parts.length >= 3) {
                nombreCorto = parts[0] + " " + parts[2];
            } else if (parts.length == 2) {
                nombreCorto = parts[0] + " " + parts[1];
            }

            String consecutivoStr = (informe.getConsecutivoCobro() != null && !informe.getConsecutivoCobro().trim().isEmpty()) ? informe.getConsecutivoCobro().trim() : "XXXX";
            String docxName = "5. INFORME SUPERVISION No. " + informe.getNumeroCuota() + " -" + nombreCorto + ".docx";
            
            String shortContrato = contrato.getNumeroContrato() != null ? contrato.getNumeroContrato().split("\\.")[0] : "";
            String xlsxName = "3. DS-" + shortContrato + "-" + consecutivoStr + " Cuota " + informe.getNumeroCuota() + " -" + nombreCorto + ".xlsx";
            
            // Archivo DOCX temporal
            String filePathDocx = SupervisionReportGenerator.generarDocx(informe, contrato, getServletContext().getRealPath("/"));
            File docxFile = new File(filePathDocx);
            
            // Archivo XLSX temporal
            File xlsxFile = null;
            try {
                String xlsxPath = com.combinacion.util.CuentaCobroGenerator.generarExcel(informe, contrato, getServletContext().getRealPath("/"));
                xlsxFile = new File(xlsxPath);
            } catch (Exception e) {
                System.out.println("No se pudo generar el Excel: " + e.getMessage());
            }

            // Archivo de Gestion temporal
            File gestionFile = null;
            String gestionName = "4. INFORME GESTION No. " + informe.getNumeroCuota() + " -" + nombreCorto + ".docx";
            try {
                String gestionPath = com.combinacion.util.GestionReportGenerator.generarDocx(informe, contrato, getServletContext().getRealPath("/"));
                gestionFile = new File(gestionPath);
            } catch (Exception e) {
                System.out.println("No se pudo generar el Informe de Gestion: " + e.getMessage());
            }

            // Si existen, generar ZIP
            if (xlsxFile != null && xlsxFile.exists()) {
                String numContrato = contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : "";
                String primerBloque = "";
                String ultimoBloque = "";
                if (numContrato.contains(".")) {
                    String[] numParts = numContrato.split("\\.");
                    primerBloque = numParts[0];
                    ultimoBloque = numParts[numParts.length - 1];
                } else {
                    primerBloque = numContrato;
                }
                
                String safeName = nombreCorto.replaceAll("[^a-zA-Z0-9.\\- ]", "");
                String zipFileName = primerBloque + (!ultimoBloque.isEmpty() ? " - " + ultimoBloque : "") + " " + safeName + ".zip";
                
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");
                
                try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(response.getOutputStream())) {
                    
                    // Agregar Evidencias desde la carpeta de Drive
                    boolean hasEvidencias = false;
                    if (informe.getUrlDriveEvidencias() != null && !informe.getUrlDriveEvidencias().trim().isEmpty()) {
                        String folderId = com.combinacion.services.GoogleDriveService.extractIdFromUrl(informe.getUrlDriveEvidencias());
                        if (folderId != null) {
                            try {
                                com.google.api.services.drive.model.FileList files = com.combinacion.services.GoogleDriveService.getFilesInFolder(folderId);
                                if (files != null && files.getFiles() != null && !files.getFiles().isEmpty()) {
                                    for (com.google.api.services.drive.model.File gFile : files.getFiles()) {
                                        if (!"application/vnd.google-apps.folder".equals(gFile.getMimeType())) {
                                            hasEvidencias = true;
                                            zos.putNextEntry(new java.util.zip.ZipEntry("Evidencias/" + gFile.getName()));
                                            try (java.io.InputStream in = com.combinacion.services.GoogleDriveService.downloadFile(gFile.getId())) {
                                                byte[] buffer = new byte[4096];
                                                int length;
                                                while ((length = in.read(buffer)) >= 0) {
                                                    zos.write(buffer, 0, length);
                                                }
                                            } catch (Exception ex) {
                                                System.err.println("No se pudo descargar evidencia " + gFile.getName() + ": " + ex.getMessage());
                                            }
                                            zos.closeEntry();
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                System.err.println("Error al listar archivos de evidencias: " + ex.getMessage());
                            }
                        }
                    }
                    if (!hasEvidencias) {
                        // Agregar carpeta Evidencias vacía si no hay archivos
                        zos.putNextEntry(new java.util.zip.ZipEntry("Evidencias/"));
                        zos.closeEntry();
                    }
                    
                    // Agregar DOCX (Supervision)
                    zos.putNextEntry(new java.util.zip.ZipEntry(docxName));
                    try (FileInputStream fis = new FileInputStream(docxFile)) {
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = fis.read(buffer)) >= 0) {
                            zos.write(buffer, 0, length);
                        }
                    }
                    zos.closeEntry();
                    
                    // Agregar DOCX (Gestion)
                    if (gestionFile != null && gestionFile.exists()) {
                        zos.putNextEntry(new java.util.zip.ZipEntry(gestionName));
                        try (FileInputStream fis = new FileInputStream(gestionFile)) {
                            byte[] buffer = new byte[4096];
                            int length;
                            while ((length = fis.read(buffer)) >= 0) {
                                zos.write(buffer, 0, length);
                            }
                        }
                        zos.closeEntry();
                    }
                    
                    // Agregar XLSX (Cuenta Cobro)
                    zos.putNextEntry(new java.util.zip.ZipEntry(xlsxName));
                    try (FileInputStream fis = new FileInputStream(xlsxFile)) {
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = fis.read(buffer)) >= 0) {
                            zos.write(buffer, 0, length);
                        }
                    }
                    zos.closeEntry();
                    
                    // Agregar anexos
                    if (informe.getSoportesJson() != null && !informe.getSoportesJson().isEmpty()) {
                        try {
                            org.json.JSONObject soportes = new org.json.JSONObject(informe.getSoportesJson());
                            for (String key : soportes.keySet()) {
                                if (key.startsWith("evidencia_")) {
                                    continue; // Ya se descarga en la carpeta Evidencias desde Drive
                                }
                                org.json.JSONObject fileData = soportes.getJSONObject(key);
                                String fileId = fileData.optString("id");
                                String fileName = fileData.optString("name");
                                if (fileId != null && !fileId.isEmpty() && fileName != null && !fileName.isEmpty()) {
                                    zos.putNextEntry(new java.util.zip.ZipEntry(fileName));
                                    try (java.io.InputStream in = com.combinacion.services.GoogleDriveService.downloadFile(fileId)) {
                                        byte[] buffer = new byte[4096];
                                        int length;
                                        while ((length = in.read(buffer)) >= 0) {
                                            zos.write(buffer, 0, length);
                                        }
                                    } catch (Exception ex) {
                                        System.err.println("No se pudo descargar anexo " + fileName + " de Drive: " + ex.getMessage());
                                    }
                                    zos.closeEntry();
                                }
                            }
                        } catch (Exception ex) {
                            System.err.println("Error procesando anexos: " + ex.getMessage());
                        }
                    }
                }
            } else {
                // Si no hay Excel, descargar solo el DOCX
                response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + docxFile.getName() + "\"");
                response.setContentLength((int) docxFile.length());

                try (FileInputStream inStream = new FileInputStream(docxFile);
                     OutputStream outStream = response.getOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el documento: " + e.getMessage());
        }
    }

    private void procesarArchivosDrive(int informeId, HttpServletRequest request) {
        try {
            System.out.println("Iniciando subida automatica a Drive para informe ID: " + informeId);
            InformeSupervision informe = informeService.obtenerPorId(informeId);
            if (informe == null) return;
            Contrato contrato = informeService.obtenerContrato(informe.getContratoId());
            if (contrato == null) return;
            
            String nombreCompleto = contrato.getContratistaNombre() != null ? contrato.getContratistaNombre().trim() : "";
            String nombreCorto = nombreCompleto;
            String[] parts = nombreCompleto.split("\\s+");
            if (parts.length >= 3) {
                nombreCorto = parts[0] + " " + parts[2];
            } else if (parts.length == 2) {
                nombreCorto = parts[0] + " " + parts[1];
            }

            String consecutivoStr = (informe.getConsecutivoCobro() != null && !informe.getConsecutivoCobro().trim().isEmpty()) ? informe.getConsecutivoCobro().trim() : "XXXX";
            String shortContrato = contrato.getNumeroContrato() != null ? contrato.getNumeroContrato().split("\\.")[0] : "";
            
            String folderNamePrincipal = shortContrato + " - " + consecutivoStr + " " + nombreCorto;
            String folderNameCuota = "Cuota " + informe.getNumeroCuota();
            
            // 1. Obtener/crear "pruebas cuenta de cobro"
            String pruebasFolderId = com.combinacion.services.GoogleDriveService.getOrCreateFolder("pruebas cuenta de cobro", null);
            // 2. Obtener/crear carpeta principal
            String principalFolderId = com.combinacion.services.GoogleDriveService.getOrCreateFolder(folderNamePrincipal, pruebasFolderId);
            // 3. Obtener/crear cuota
            String cuotaFolderId = com.combinacion.services.GoogleDriveService.getOrCreateFolder(folderNameCuota, principalFolderId);
            // 4. Obtener/crear evidencias
            String evidenciasFolderId = com.combinacion.services.GoogleDriveService.getOrCreateFolder("EVIDENCIAS", cuotaFolderId);
            
            // 4.1. Dar permisos públicos de lectura a la carpeta EVIDENCIAS
            try {
                com.combinacion.services.GoogleDriveService.setPublicViewPermission(evidenciasFolderId);
            } catch (Exception ignore) {
                System.err.println("Aviso: No se pudo asignar permisos publicos a la carpeta EVIDENCIAS: " + ignore.getMessage());
            }
            
            // 4.5. Guardar la URL en la base de datos (apuntando a la carpeta EVIDENCIAS)
            String driveUrl = "https://drive.google.com/drive/folders/" + evidenciasFolderId + "?usp=sharing";
            informe.setUrlDriveEvidencias(driveUrl);
            new com.combinacion.dao.InformeSupervisionDAO().actualizarUrlDrive(informe.getId(), driveUrl);
            
            // 5. Generar archivos localmente
            // BYPASS WORD'S UNSUPPORTED BROWSER BUG:
            // We pass a local redirect URL to the generators, which outputs a JS-based redirect page.
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
            String redirectUrl = baseUrl + "/redirect?url=" + java.net.URLEncoder.encode(driveUrl, "UTF-8");
            
            // Temporarily set it so the generators pick up the redirect URL instead of direct Drive URL
            informe.setUrlDriveEvidencias(redirectUrl);
            String docxPath = com.combinacion.util.SupervisionReportGenerator.generarDocx(informe, contrato, request.getServletContext().getRealPath("/"));
            String xlsxPath = com.combinacion.util.CuentaCobroGenerator.generarExcel(informe, contrato, request.getServletContext().getRealPath("/"));
            String gestionPath = com.combinacion.util.GestionReportGenerator.generarDocx(informe, contrato, request.getServletContext().getRealPath("/"));
            
            // Restore the original Drive URL for the rest of the application
            informe.setUrlDriveEvidencias(driveUrl);
            
            File docxFile = new File(docxPath);
            File xlsxFile = new File(xlsxPath);
            File gestionFile = new File(gestionPath);
            
            String docxName = "5. INFORME SUPERVISION No. " + informe.getNumeroCuota() + " -" + nombreCorto + ".docx";
            String xlsxName = "3. DS-" + shortContrato + "-" + consecutivoStr + " Cuota " + informe.getNumeroCuota() + " -" + nombreCorto + ".xlsx";
            String gestionName = "4. INFORME GESTION No. " + informe.getNumeroCuota() + " -" + nombreCorto + ".docx";
            
            // 6. Subir archivos a Drive (Docs y Excel)
            if (docxFile.exists()) {
                com.combinacion.services.GoogleDriveService.uploadOrUpdateFile(docxFile, docxName, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", cuotaFolderId);
            }
            if (xlsxFile.exists()) {
                com.combinacion.services.GoogleDriveService.uploadOrUpdateFile(xlsxFile, xlsxName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", cuotaFolderId);
            }
            if (gestionFile.exists()) {
                com.combinacion.services.GoogleDriveService.uploadOrUpdateFile(gestionFile, gestionName, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", cuotaFolderId);
            }
            
            // 7. Subir todos los documentos soporte
            org.json.JSONObject soportes = new org.json.JSONObject();
            if (informe.getSoportesJson() != null && !informe.getSoportesJson().isEmpty()) {
                try { soportes = new org.json.JSONObject(informe.getSoportesJson()); } catch (Exception ignore) {}
            }
            
            System.out.println("Iniciando escaneo de partes (archivos adjuntos)...");
            for (Part part : request.getParts()) {
                String submittedFileName = getFileName(part);
                if (submittedFileName != null && !submittedFileName.trim().isEmpty() && part.getSize() > 0) {
                    String partName = part.getName();
                    
                    // Si el nombre del campo empieza con "evidencia_", va en la subcarpeta EVIDENCIAS
                    // Si es file_rut, file_cedula, file_secop, etc., va en la carpeta Cuota X
                    String targetFolderId = (partName != null && partName.startsWith("evidencia_")) ? evidenciasFolderId : cuotaFolderId;
                    
                    System.out.println("Subiendo " + partName + ": " + submittedFileName + " (" + part.getSize() + " bytes)");
                    String mimeType = part.getContentType() != null ? part.getContentType() : "application/octet-stream";
                    try (java.io.InputStream is = part.getInputStream()) {
                        String fileId = com.combinacion.services.GoogleDriveService.uploadStreamToDrive(is, part.getSize(), submittedFileName, mimeType, targetFolderId);
                        
                        org.json.JSONObject fileData = new org.json.JSONObject();
                        fileData.put("name", submittedFileName);
                        fileData.put("id", fileId);
                        fileData.put("url", "https://drive.google.com/file/d/" + fileId + "/view");
                        soportes.put(partName, fileData);
                    } catch (Exception ex) {
                        System.err.println("Error subiendo archivo " + submittedFileName + ": " + ex.getMessage());
                    }
                }
            }
            informe.setSoportesJson(soportes.toString());
            new com.combinacion.dao.InformeSupervisionDAO().actualizarSoportesJson(informe.getId(), soportes.toString());
            
            System.out.println("Subida a Drive completada con exito.");
        } catch (Exception e) {
            System.err.println("Error subiendo archivos a Drive:");
            e.printStackTrace();
        }
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp != null) {
            for (String cd : contentDisp.split(";")) {
                if (cd.trim().startsWith("filename")) {
                    return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return null;
    }

    private void insertar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        InformeFormData form = construirFormData(request);
        String error = informeService.insertar(form);
        if (error != null) {
            request.setAttribute("error", error);
            mostrarFormularioNuevo(request, response);
        } else {
            // Procesar Drive después de guardar exitosamente
            java.util.List<InformeSupervision> lista = informeService.listarPorContrato(form.contratoId);
            if (lista != null && !lista.isEmpty()) {
                procesarArchivosDrive(lista.get(0).getId(), request);
            }
            request.getSession().setAttribute("successMessage", "El informe de supervisión ha sido registrado correctamente.");
            response.sendRedirect("informes");
        }
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int id = ParseUtils.parseInt(request.getParameter("id"));
        InformeFormData form = construirFormData(request);
        
        String error = informeService.actualizar(id, form);
        if (error != null) {
            request.setAttribute("error", error);
            mostrarFormularioEdicion(request, response);
        } else {
            // Procesar Drive después de actualizar exitosamente
            procesarArchivosDrive(id, request);
            request.getSession().setAttribute("successMessage", "El informe de supervisión ha sido actualizado correctamente.");
            response.sendRedirect("informes");
        }
    }

    private InformeFormData construirFormData(HttpServletRequest r) {
        InformeFormData f = new InformeFormData();
        f.contratoId = ParseUtils.parseInt(r.getParameter("contrato_id"));
        f.periodoInforme = r.getParameter("periodo_informe");
        f.tipoInforme = r.getParameter("tipo_informe");
        f.numeroCuota = r.getParameter("numero_cuota");
        f.consecutivoCobro = r.getParameter("consecutivo_cobro");
        f.fechaInicioPeriodo = r.getParameter("fecha_inicio_periodo");
        f.fechaFinPeriodo = r.getParameter("fecha_fin_periodo");
        f.modificaciones = r.getParameter("modificaciones");
        f.suspensiones = r.getParameter("suspensiones");
        f.reanudaciones = r.getParameter("reanudaciones");
        f.cesiones = r.getParameter("cesiones");
        f.terminacionAnticipada = r.getParameter("terminacion_anticipada");
        f.adiciones = r.getParameter("adiciones");
        f.prorrogas = r.getParameter("prorrogas");
        f.reciboSatisfaccion = r.getParameter("recibo_satisfaccion");
        f.constanciaPazSalvo = r.getParameter("constancia_paz_salvo");
        f.valorCuotaPagar = r.getParameter("valor_cuota_pagar");
        f.valorAccumuladoPagado = r.getParameter("valor_acumulado_pagado");
        f.saldoPorCancelar = r.getParameter("saldo_por_cancelar");
        f.planillaNumero = r.getParameter("planilla_numero");
        f.planillaPin = r.getParameter("planilla_pin");
        f.planillaOperador = r.getParameter("planilla_operador");
        f.planillaFechaPago = r.getParameter("planilla_fecha_pago");
        f.planillaPeriodo = r.getParameter("planilla_periodo");
        
        String conceptoJson = r.getParameter("concepto_supervisor_json");
        if (conceptoJson != null && !conceptoJson.isEmpty()) {
            f.conceptoSupervisor = conceptoJson;
        } else {
            int count = ParseUtils.parseInt(r.getParameter("obligaciones_count"));
            if (count > 0) {
                org.json.JSONArray arr = new org.json.JSONArray();
                for (int i = 0; i < count; i++) {
                    org.json.JSONObject obj = new org.json.JSONObject();
                    obj.put("obligacion", r.getParameter("obligacion_" + i));
                    String[] acts = r.getParameterValues("actividad_" + i);
                    String joinedAct = "";
                    if (acts != null) {
                        joinedAct = String.join("\n", acts);
                    }
                    obj.put("actividad", joinedAct);
                    arr.put(obj);
                }
                f.conceptoSupervisor = arr.toString();
            } else {
                f.conceptoSupervisor = r.getParameter("concepto_supervisor");
            }
        }
        
        f.observacionesTecnicas = r.getParameter("observaciones_tecnicas");
        f.recomendaciones = r.getParameter("recomendaciones");
        f.fechaSuscripcion = r.getParameter("fecha_suscripcion");
        f.soportesJson = r.getParameter("soportes_json");
        return f;
    }
}
