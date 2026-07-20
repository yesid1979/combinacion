import os

def update_informe_model():
    path = r"c:\Users\Soporte y Desarrollo\Documents\NetBeansProjects\combinacion\src\main\java\com\combinacion\models\InformeSupervision.java"
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if "private String estadoRadicacion;" not in content:
        content = content.replace(
            "// Relación con Contrato",
            "// Radicación\n    private String estadoRadicacion;\n    private Integer idRevisorAsignado;\n    \n    // Relación con Contrato"
        )
        content = content.replace(
            "public Contrato getContrato() { return contrato; }",
            "public String getEstadoRadicacion() { return estadoRadicacion; }\n    public void setEstadoRadicacion(String estadoRadicacion) { this.estadoRadicacion = estadoRadicacion; }\n\n    public Integer getIdRevisorAsignado() { return idRevisorAsignado; }\n    public void setIdRevisorAsignado(Integer idRevisorAsignado) { this.idRevisorAsignado = idRevisorAsignado; }\n\n    public Contrato getContrato() { return contrato; }"
        )
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print("Updated InformeSupervision.java")

def update_informe_dao():
    path = r"c:\Users\Soporte y Desarrollo\Documents\NetBeansProjects\combinacion\src\main\java\com\combinacion\dao\InformeSupervisionDAO.java"
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    if "estado_radicacion" not in content:
        # Update Insert
        content = content.replace(
            "fecha_suscripcion, url_drive_evidencias, consecutivo_cobro\"",
            "fecha_suscripcion, url_drive_evidencias, consecutivo_cobro, estado_radicacion, id_revisor_asignado\""
        )
        content = content.replace(
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\";",
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\";"
        )
        content = content.replace(
            "ps.setString(29, info.getConsecutivoCobro());\n\n            if (ps.executeUpdate() > 0) {",
            "ps.setString(29, info.getConsecutivoCobro());\n            ps.setString(30, info.getEstadoRadicacion() != null ? info.getEstadoRadicacion() : \"BORRADOR\");\n            if (info.getIdRevisorAsignado() != null) {\n                ps.setInt(31, info.getIdRevisorAsignado());\n            } else {\n                ps.setNull(31, Types.INTEGER);\n            }\n\n            if (ps.executeUpdate() > 0) {"
        )
        
        # Update mapResultSet
        content = content.replace(
            "try { info.setSoportesJson(rs.getString(\"soportes_json\")); } catch (SQLException e) {}\n\n        // Map contract info",
            "try { info.setSoportesJson(rs.getString(\"soportes_json\")); } catch (SQLException e) {}\n        try { info.setEstadoRadicacion(rs.getString(\"estado_radicacion\")); } catch (SQLException e) {}\n        try {\n            int revId = rs.getInt(\"id_revisor_asignado\");\n            if (!rs.wasNull()) {\n                info.setIdRevisorAsignado(revId);\n            }\n        } catch (SQLException e) {}\n\n        // Map contract info"
        )
        
        # Update actualizar
        content = content.replace(
            "consecutivo_cobro = ?, soportes_json = ? \"",
            "consecutivo_cobro = ?, soportes_json = ?, estado_radicacion = ?, id_revisor_asignado = ? \""
        )
        content = content.replace(
            "ps.setString(29, info.getSoportesJson());\n            ps.setInt(30, info.getId());\n\n            if (ps.executeUpdate() > 0) {",
            "ps.setString(29, info.getSoportesJson());\n            ps.setString(30, info.getEstadoRadicacion());\n            if (info.getIdRevisorAsignado() != null) {\n                ps.setInt(31, info.getIdRevisorAsignado());\n            } else {\n                ps.setNull(31, Types.INTEGER);\n            }\n            ps.setInt(32, info.getId());\n\n            if (ps.executeUpdate() > 0) {"
        )
        
        # Update crearTablaSiNoExiste
        content = content.replace(
            "try { stmt.execute(\"ALTER TABLE informes_supervision ADD COLUMN soportes_json TEXT\"); } catch (Exception ignore) {}\n        } catch (SQLException e) {",
            "try { stmt.execute(\"ALTER TABLE informes_supervision ADD COLUMN soportes_json TEXT\"); } catch (Exception ignore) {}\n            try { stmt.execute(\"ALTER TABLE informes_supervision ADD COLUMN estado_radicacion VARCHAR(50) DEFAULT 'BORRADOR'\"); } catch (Exception ignore) {}\n            try { stmt.execute(\"ALTER TABLE informes_supervision ADD COLUMN id_revisor_asignado INTEGER REFERENCES usuarios(id)\"); } catch (Exception ignore) {}\n        } catch (SQLException e) {"
        )
        
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print("Updated InformeSupervisionDAO.java")

if __name__ == '__main__':
    update_informe_model()
    update_informe_dao()
