import os

def update_usuario_dao():
    path = r"c:\Users\Soporte y Desarrollo\Documents\NetBeansProjects\combinacion\src\main\java\com\combinacion\dao\UsuarioDAO.java"
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    new_method = """
    public List<Usuario> listarRevisores() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.password_hash, u.salt, u.nombre_completo, "
                   + "u.correo, u.cedula, u.celular, u.sexo, u.cargo, u.vinculacion, u.fecha_inicio_contrato, u.fecha_fin_contrato, "
                   + "u.activo, u.ultimo_acceso, u.fecha_creacion, u.rol_id, u.foto_url, u.firma_url, "
                   + "r.nombre as rol_nombre "
                   + "FROM usuarios u "
                   + "LEFT JOIN roles r ON u.rol_id = r.id "
                   + "JOIN usuario_permisos up ON u.id = up.usuario_id "
                   + "JOIN permisos p ON up.permiso_id = p.id "
                   + "WHERE p.nombre = 'PUEDE_REVISAR_CUENTAS' AND u.activo = true "
                   + "ORDER BY u.nombre_completo";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public int contarTotal() {"""
    
    if "public List<Usuario> listarRevisores()" not in content:
        content = content.replace("public int contarTotal() {", new_method)
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print("UsuarioDAO actualizado con listarRevisores().")

def update_informe_servlet():
    path = r"c:\Users\Soporte y Desarrollo\Documents\NetBeansProjects\combinacion\src\main\java\com\combinacion\servlets\InformeSupervisionServlet.java"
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    if "request.setAttribute(\"listaRevisores\"" not in content:
        # Add setAttribute to mostrarFormularioNuevo
        content = content.replace(
            "request.setAttribute(\"siguienteCuota\", previos != null ? previos.size() + 1 : 1);\n            }\n        }\n        request.setAttribute(\"action\", \"insert\");",
            "request.setAttribute(\"siguienteCuota\", previos != null ? previos.size() + 1 : 1);\n            }\n        }\n        request.setAttribute(\"listaRevisores\", new com.combinacion.dao.UsuarioDAO().listarRevisores());\n        request.setAttribute(\"action\", \"insert\");"
        )
        # Add setAttribute to mostrarDetalle
        content = content.replace(
            "request.setAttribute(\"listaObligaciones\", com.combinacion.util.ObligacionesParser.decodificarConcepto(informe.getConceptoSupervisor(), contrato.getActividadesEntregables()));\n            }\n        }\n        request.setAttribute(\"readonly\", true);",
            "request.setAttribute(\"listaObligaciones\", com.combinacion.util.ObligacionesParser.decodificarConcepto(informe.getConceptoSupervisor(), contrato.getActividadesEntregables()));\n            }\n        }\n        request.setAttribute(\"listaRevisores\", new com.combinacion.dao.UsuarioDAO().listarRevisores());\n        request.setAttribute(\"readonly\", true);"
        )
        # Add setAttribute to mostrarFormularioEdicion
        content = content.replace(
            "request.setAttribute(\"listaObligaciones\", com.combinacion.util.ObligacionesParser.decodificarConcepto(informe.getConceptoSupervisor(), contrato.getActividadesEntregables()));\n            }\n        }\n        request.setAttribute(\"action\", \"update\");",
            "request.setAttribute(\"listaObligaciones\", com.combinacion.util.ObligacionesParser.decodificarConcepto(informe.getConceptoSupervisor(), contrato.getActividadesEntregables()));\n            }\n        }\n        request.setAttribute(\"listaRevisores\", new com.combinacion.dao.UsuarioDAO().listarRevisores());\n        request.setAttribute(\"action\", \"update\");"
        )
        # Add Radicar logic inside insertar
        content = content.replace(
            "InformeFormData form = construirFormData(request);\n        String error = informeService.insertar(form);",
            "InformeFormData form = construirFormData(request);\n        String esRadicar = request.getParameter(\"radicar\");\n        if (\"true\".equals(esRadicar)) {\n            form.estadoRadicacion = \"RADICADA\";\n            String rv = request.getParameter(\"id_revisor_asignado\");\n            if(rv != null && !rv.isEmpty()) {\n                form.idRevisorAsignado = Integer.parseInt(rv);\n            }\n        }\n        String error = informeService.insertar(form);"
        )
        
        # Add Radicar logic inside actualizar
        content = content.replace(
            "InformeFormData form = construirFormData(request);\n        \n        String error = informeService.actualizar(id, form);",
            "InformeFormData form = construirFormData(request);\n        String esRadicar = request.getParameter(\"radicar\");\n        if (\"true\".equals(esRadicar)) {\n            form.estadoRadicacion = \"RADICADA\";\n            String rv = request.getParameter(\"id_revisor_asignado\");\n            if(rv != null && !rv.isEmpty()) {\n                form.idRevisorAsignado = Integer.parseInt(rv);\n            }\n        } else {\n            com.combinacion.models.InformeSupervision infOld = informeService.obtenerPorId(id);\n            form.estadoRadicacion = infOld.getEstadoRadicacion();\n            form.idRevisorAsignado = infOld.getIdRevisorAsignado();\n        }\n        String error = informeService.actualizar(id, form);"
        )
        
        # We need to add state/revisor to form data building
        content = content.replace(
            "public String consecutivoCobro;",
            "public String consecutivoCobro;\n        public String estadoRadicacion;\n        public Integer idRevisorAsignado;"
        )
        
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print("InformeSupervisionServlet actualizado.")

if __name__ == '__main__':
    update_usuario_dao()
    update_informe_servlet()
