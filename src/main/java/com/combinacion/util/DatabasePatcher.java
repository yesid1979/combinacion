package com.combinacion.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabasePatcher {

    public static void ensureSchema() {
        System.out.println("Verificando esquema de base de datos...");
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Verificamos si la columna existe intentando seleccionarla (manera 'lazy' pero
            // efectiva para compatibilidad)
            // O mejor, consultamos information_schema

            ResultSet rs = stmt.executeQuery(
                    "SELECT column_name FROM information_schema.columns " +
                            "WHERE table_name='contratos' AND column_name='apoyo_supervision'");

            if (!rs.next()) {
                System.out.println("⚠️ Columna 'apoyo_supervision' no encontrada. Agregándola automáticamente...");
                stmt.executeUpdate("ALTER TABLE contratos ADD COLUMN apoyo_supervision TEXT");
                System.out.println("✅ Columna 'apoyo_supervision' agregada exitosamente.");
            } else {
                System.out.println("✅ La columna 'apoyo_supervision' ya existe.");
            }

            ResultSet rsIva = stmt.executeQuery(
                    "SELECT column_name FROM information_schema.columns " +
                            "WHERE table_name='contratos' AND column_name='iva_si_no'");
            if (!rsIva.next()) {
                System.out.println("⚠️ Columna 'iva_si_no' no encontrada. Agregándola automáticamente...");
                stmt.executeUpdate("ALTER TABLE contratos ADD COLUMN iva_si_no VARCHAR(10)");
                System.out.println("✅ Columna 'iva_si_no' agregada exitosamente.");
            }

            ResultSet rsNumMod = stmt.executeQuery(
                    "SELECT column_name FROM information_schema.columns " +
                            "WHERE table_name='contratos' AND column_name='numero_modificacion'");
            if (!rsNumMod.next()) {
                System.out.println("⚠️ Columna 'numero_modificacion' no encontrada. Agregándola automáticamente...");
                stmt.executeUpdate("ALTER TABLE contratos ADD COLUMN numero_modificacion VARCHAR(50)");
                System.out.println("✅ Columna 'numero_modificacion' agregada exitosamente.");
            }

            ResultSet rsFecMod = stmt.executeQuery(
                    "SELECT column_name FROM information_schema.columns " +
                            "WHERE table_name='contratos' AND column_name='fecha_modificacion'");
            if (!rsFecMod.next()) {
                System.out.println("⚠️ Columna 'fecha_modificacion' no encontrada. Agregándola automáticamente...");
                stmt.executeUpdate("ALTER TABLE contratos ADD COLUMN fecha_modificacion DATE");
                System.out.println("✅ Columna 'fecha_modificacion' agregada exitosamente.");
            }

            ResultSet rsFact = stmt.executeQuery(
                    "SELECT column_name FROM information_schema.columns " +
                            "WHERE table_name='contratos' AND column_name='facturador_electronico'");
            if (!rsFact.next()) {
                System.out.println("⚠️ Columna 'facturador_electronico' no encontrada. Agregándola automáticamente...");
                stmt.executeUpdate("ALTER TABLE contratos ADD COLUMN facturador_electronico VARCHAR(50)");
                System.out.println("✅ Columna 'facturador_electronico' agregada exitosamente.");
            } else {
                System.out.println("✅ La columna 'facturador_electronico' ya existe.");
            }

            // --- NUEVOS CAMPOS PARA ADICION EN PRESUPUESTO_DETALLES ---
            String[] columnasPresupuesto = {
                "cdp_adicion TEXT", 
                "cdp_valor_adicion NUMERIC", 
                "rp_adicion TEXT", 
                "rp_fecha_adicion DATE",
                "id_paa_si_no VARCHAR(10)"
            };

            // Verificar tabla usuarios
            ResultSet rsFirma = stmt.executeQuery(
                    "SELECT column_name FROM information_schema.columns " +
                            "WHERE table_name='usuarios' AND column_name='firma_url'");
            if (!rsFirma.next()) {
                System.out.println("⚠️ Columna 'firma_url' no encontrada en usuarios. Agregándola automáticamente...");
                stmt.executeUpdate("ALTER TABLE usuarios ADD COLUMN firma_url VARCHAR(500)");
                System.out.println("✅ Columna 'firma_url' agregada exitosamente.");
            }

            for (String colInfo : columnasPresupuesto) {
                String colName = colInfo.split(" ")[0];
                ResultSet rsP = stmt.executeQuery(
                        "SELECT column_name FROM information_schema.columns " +
                        "WHERE table_name='presupuesto_detalles' AND column_name='" + colName + "'");
                
                if (!rsP.next()) {
                    System.out.println("⚠️ Columna '" + colName + "' no encontrada en presupuesto_detalles. Agregándola...");
                    stmt.executeUpdate("ALTER TABLE presupuesto_detalles ADD COLUMN " + colInfo);
                    System.out.println("✅ Columna '" + colName + "' agregada.");
                }
            }

            // --- NUEVOS CAMPOS PARA CONTRATISTA NUEVO EN INFORMES_SUPERVISION ---
            String[] columnasSeguridadNuevo = {
                "eps VARCHAR(150)",
                "afp_pension VARCHAR(150)",
                "arl VARCHAR(150)",
                "es_contratista_nuevo VARCHAR(10) DEFAULT 'NO'"
            };
            for (String colInfo : columnasSeguridadNuevo) {
                String colName = colInfo.split(" ")[0];
                ResultSet rsS = stmt.executeQuery(
                        "SELECT column_name FROM information_schema.columns " +
                        "WHERE table_name='informes_supervision' AND column_name='" + colName + "'");
                if (!rsS.next()) {
                    System.out.println("⚠️ Columna '" + colName + "' no encontrada en informes_supervision. Agregándola...");
                    stmt.executeUpdate("ALTER TABLE informes_supervision ADD COLUMN " + colInfo);
                    System.out.println("✅ Columna '" + colName + "' agregada.");
                }
            }

            // Verificar columna supervisor_id en informes_supervision
            ResultSet rsSup = stmt.executeQuery(
                    "SELECT column_name FROM information_schema.columns " +
                    "WHERE table_name='informes_supervision' AND column_name='supervisor_id'");
            if (!rsSup.next()) {
                System.out.println("⚠️ Columna 'supervisor_id' no encontrada en informes_supervision. Agregándola...");
                stmt.executeUpdate("ALTER TABLE informes_supervision ADD COLUMN supervisor_id INT REFERENCES supervisores(id)");
                System.out.println("✅ Columna 'supervisor_id' agregada en informes_supervision.");
            }

            // Verificar tabla usuario_permisos
            ResultSet rs2 = stmt.executeQuery(
                    "SELECT 1 FROM information_schema.tables WHERE table_name='usuario_permisos'");
            if (!rs2.next()) {
                System.out.println("⚠️ Tabla 'usuario_permisos' no encontrada. Creándola...");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS usuario_permisos (" +
                                 "usuario_id INT REFERENCES usuarios(id) ON DELETE CASCADE, " +
                                 "permiso_id INT REFERENCES permisos(id) ON DELETE CASCADE, " +
                                 "PRIMARY KEY (usuario_id, permiso_id))");
                System.out.println("✅ Tabla 'usuario_permisos' creada exitosamente.");
            }

            // Asegurar usuario id = 0 para opción 'Sin Revisor (Contratación)'
            ResultSet rsU0 = stmt.executeQuery("SELECT id FROM usuarios WHERE id = 0");
            if (!rsU0.next()) {
                stmt.executeUpdate("INSERT INTO usuarios (id, username, password_hash, salt, nombre_completo, activo, rol_id) " +
                                 "VALUES (0, 'sin_revisor', 'N/A', 'N/A', 'Contratación (Revisión Directa)', true, 1)");
                System.out.println("✅ Usuario 0 (Sin Revisor) asegurado en tabla usuarios.");
            }

            // Asegurar permisos VER TODO para la mallas dinámica
            String[] modulosPerms = {"ADMINISTRACION", "CARGA_MASIVA", "COMBINACION", "CONTRATISTAS", "CONTRATOS", "ORDENADORES", "PRESUPUESTO", "SUPERVISORES", "REVALUACION"};
            for (String mod : modulosPerms) {
                String cod = mod + "_VER_TODO";
                if (mod.equals("ADMINISTRACION")) cod = "ADMIN_VER_TODO";
                stmt.executeUpdate("INSERT INTO permisos (codigo, nombre, modulo, descripcion) " +
                                 "VALUES ('" + cod + "', 'Ver todo', '" + mod + "', 'Ver todo del módulo " + mod + "') " +
                                 "ON CONFLICT (codigo) DO NOTHING");
            }

            // Asegurar permisos para el módulo ADMINISTRACION (CRUD completo)
            String[] adminPerms = {"ADMIN_EDITAR", "ADMIN_ELIMINAR", "ADMIN_VER"};
            for (String ap : adminPerms) {
                stmt.executeUpdate("INSERT INTO permisos (codigo, nombre, modulo, descripcion) " +
                                 "VALUES ('" + ap + "', 'Gestionar admin', 'ADMINISTRACION', 'Permisos de administración') " +
                                 "ON CONFLICT (codigo) DO NOTHING");
            }
            
            // Asegurar permisos para REVALUACION
            String[][] revaPerms = {
                {"REVALUACION_VER", "Ver revaluación"}, 
                {"REVALUACION_CREAR", "Crear revaluación"}, 
                {"REVALUACION_EDITAR", "Editar revaluación"}, 
                {"REVALUACION_ELIMINAR", "Eliminar revaluación"}
            };
            for (String[] p : revaPerms) {
                stmt.executeUpdate("INSERT INTO permisos (codigo, nombre, modulo, descripcion) " +
                                 "VALUES ('" + p[0] + "', '" + p[1] + "', 'REVALUACION', 'Permiso generado para REVALUACION') " +
                                 "ON CONFLICT (codigo) DO UPDATE SET nombre = EXCLUDED.nombre");
            }

            // Asegurar que el rol Administrador tenga TODOS los permisos incluyendo los nuevos
            stmt.executeUpdate("INSERT INTO rol_permisos (rol_id, permiso_id) " +
                             "SELECT r.id, p.id FROM roles r, permisos p " +
                             "WHERE r.nombre = 'Administrador' " +
                             "ON CONFLICT DO NOTHING");

            // Asegurar creación de las vistas de informes y contratos
            stmt.executeUpdate("DROP VIEW IF EXISTS vista_resumen_contratos_cuotas CASCADE");
            stmt.executeUpdate(
                "CREATE OR REPLACE VIEW vista_resumen_contratos_cuotas AS\n" +
                "SELECT \n" +
                "    c.id AS contrato_id,\n" +
                "    c.numero_contrato,\n" +
                "    c.anio,\n" +
                "    ct.nombre AS nombres_apellidos,\n" +
                "    ct.cedula AS cedula_contratista,\n" +
                "    COALESCE(s_inf.nombre, s_con.nombre, 'Sin supervisor') AS supervisor,\n" +
                "    STRING_AGG(DISTINCT COALESCE(u.nombre_completo, 'Sin revisor'), ', ') AS revisor,\n" +
                "    STRING_AGG(inf.id::text, ', ' ORDER BY (CASE WHEN inf.numero_cuota ~ '^[0-9]+$' THEN inf.numero_cuota::int ELSE 9999 END), inf.id) AS ids_informes,\n" +
                "    MAX(CASE WHEN inf.numero_cuota = '1' THEN COALESCE(inf.estado_radicacion, 'BORRADOR') END) AS cuota_1,\n" +
                "    MAX(CASE WHEN inf.numero_cuota = '2' THEN COALESCE(inf.estado_radicacion, 'BORRADOR') END) AS cuota_2,\n" +
                "    MAX(CASE WHEN inf.numero_cuota = '3' THEN COALESCE(inf.estado_radicacion, 'BORRADOR') END) AS cuota_3,\n" +
                "    MAX(CASE WHEN inf.numero_cuota = '4' THEN COALESCE(inf.estado_radicacion, 'BORRADOR') END) AS cuota_4,\n" +
                "    MAX(CASE WHEN inf.numero_cuota = '5' THEN COALESCE(inf.estado_radicacion, 'BORRADOR') END) AS cuota_5,\n" +
                "    MAX(CASE WHEN inf.numero_cuota = '6' THEN COALESCE(inf.estado_radicacion, 'BORRADOR') END) AS cuota_6,\n" +
                "    MAX(CASE WHEN inf.numero_cuota = '7' THEN COALESCE(inf.estado_radicacion, 'BORRADOR') END) AS cuota_7,\n" +
                "    MAX(CASE WHEN inf.numero_cuota = '8' THEN COALESCE(inf.estado_radicacion, 'BORRADOR') END) AS cuota_8,\n" +
                "    MAX(CASE WHEN inf.numero_cuota = '9' THEN COALESCE(inf.estado_radicacion, 'BORRADOR') END) AS cuota_9,\n" +
                "    MAX(CASE WHEN inf.numero_cuota = '10' THEN COALESCE(inf.estado_radicacion, 'BORRADOR') END) AS cuota_10,\n" +
                "    MAX(CASE WHEN inf.numero_cuota = '11' THEN COALESCE(inf.estado_radicacion, 'BORRADOR') END) AS cuota_11,\n" +
                "    MAX(CASE WHEN inf.numero_cuota = '12' THEN COALESCE(inf.estado_radicacion, 'BORRADOR') END) AS cuota_12,\n" +
                "    COUNT(inf.id) AS total_cuotas_registradas\n" +
                "FROM contratos c\n" +
                "LEFT JOIN contratistas ct ON c.contratista_id = ct.id\n" +
                "LEFT JOIN supervisores s_con ON c.supervisor_id = s_con.id\n" +
                "INNER JOIN informes_supervision inf ON inf.contrato_id = c.id\n" +
                "LEFT JOIN supervisores s_inf ON inf.supervisor_id = s_inf.id\n" +
                "LEFT JOIN usuarios u ON inf.id_revisor_asignado = u.id\n" +
                "GROUP BY \n" +
                "    c.id, \n" +
                "    c.numero_contrato, \n" +
                "    c.anio, \n" +
                "    ct.nombre, \n" +
                "    ct.cedula,\n" +
                "    COALESCE(s_inf.nombre, s_con.nombre, 'Sin supervisor')\n" +
                "ORDER BY c.id DESC"
            );

            stmt.executeUpdate(
                "CREATE OR REPLACE VIEW vista_informes_cuotas_detalle AS\n" +
                "SELECT \n" +
                "    inf.id AS id_informe,\n" +
                "    c.id AS contrato_id,\n" +
                "    c.numero_contrato,\n" +
                "    c.anio,\n" +
                "    ct.nombre AS nombres_apellidos,\n" +
                "    ct.cedula AS cedula_contratista,\n" +
                "    COALESCE(s_inf.nombre, s_con.nombre, 'Sin supervisor') AS supervisor,\n" +
                "    COALESCE(u.nombre_completo, 'Sin revisor') AS revisor,\n" +
                "    inf.numero_cuota,\n" +
                "    COALESCE(inf.estado_radicacion, 'BORRADOR') AS estado_radicacion,\n" +
                "    inf.periodo_informe,\n" +
                "    inf.fecha_creacion\n" +
                "FROM informes_supervision inf\n" +
                "INNER JOIN contratos c ON inf.contrato_id = c.id\n" +
                "LEFT JOIN contratistas ct ON c.contratista_id = ct.id\n" +
                "LEFT JOIN supervisores s_con ON c.supervisor_id = s_con.id\n" +
                "LEFT JOIN supervisores s_inf ON inf.supervisor_id = s_inf.id\n" +
                "LEFT JOIN usuarios u ON inf.id_revisor_asignado = u.id\n" +
                "ORDER BY inf.id DESC"
            );

            stmt.executeUpdate("DROP VIEW IF EXISTS vista_personas_sin_cuentas_radicadas CASCADE");
            stmt.executeUpdate(
                "CREATE OR REPLACE VIEW vista_personas_sin_cuentas_radicadas AS\n" +
                "SELECT \n" +
                "    c.id AS contrato_id,\n" +
                "    c.numero_contrato,\n" +
                "    c.anio,\n" +
                "    ct.cedula,\n" +
                "    ct.nombre AS nombres_apellidos,\n" +
                "    ct.telefono,\n" +
                "    ct.correo,\n" +
                "    COALESCE(s.nombre, 'Sin supervisor') AS supervisor,\n" +
                "    c.estado AS estado_contrato,\n" +
                "    COUNT(inf.id) AS total_cuentas_creadas,\n" +
                "    CASE \n" +
                "        WHEN COUNT(inf.id) = 0 THEN 'Sin ninguna cuenta creada'\n" +
                "        ELSE 'Tiene cuentas solo en BORRADOR (sin radicar)'\n" +
                "    END AS situacion,\n" +
                "    c.fecha_inicio,\n" +
                "    c.fecha_terminacion,\n" +
                "    c.plazo_ejecucion AS plazo,\n" +
                "    c.valor_total_numeros AS valor_contrato\n" +
                "FROM contratos c\n" +
                "LEFT JOIN contratistas ct ON c.contratista_id = ct.id\n" +
                "LEFT JOIN supervisores s ON c.supervisor_id = s.id\n" +
                "LEFT JOIN informes_supervision inf ON inf.contrato_id = c.id\n" +
                "WHERE NOT EXISTS (\n" +
                "    SELECT 1 \n" +
                "    FROM informes_supervision inf_rad \n" +
                "    WHERE inf_rad.contrato_id = c.id \n" +
                "      AND inf_rad.estado_radicacion IS NOT NULL \n" +
                "      AND inf_rad.estado_radicacion != 'BORRADOR'\n" +
                ")\n" +
                "GROUP BY \n" +
                "    c.id, c.numero_contrato, c.anio, ct.cedula, ct.nombre, ct.telefono, ct.correo, s.nombre, c.estado, c.fecha_inicio, c.fecha_terminacion, c.plazo_ejecucion, c.valor_total_numeros\n" +
                "ORDER BY c.anio DESC, c.numero_contrato ASC"
            );

            System.out.println("✅ Vistas 'vista_resumen_contratos_cuotas', 'vista_informes_cuotas_detalle' y 'vista_personas_sin_cuentas_radicadas' aseguradas.");
            System.out.println("✅ Todos los permisos de ADMINISTRACION asegurados.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ensureSchema();
    }
}
