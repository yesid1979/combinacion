# Sistema de Combinación de Correspondencia, Supervisión Contractual y Radicación de Cuentas (DAGJP)

## 1. Descripción General del Proyecto
Sistema de información web desarrollado para el **Departamento Administrativo de Gestión Jurídica Pública (DAGJP)** de la **Alcaldía Distrital de Santiago de Cali**, orientado a la gestión integral del ciclo contractual de prestación de servicios profesionales y de apoyo a la gestión:
- **Carga Masiva y Sincronización Automática:** Importación desde archivos Excel/CSV y sincronización directa con matrices en Google Sheets / Google Drive.
- **Gestión Contractual:** Administración de Contratistas, Contratos, Supervisores, Ordenadores del Gasto, Estructuradores (Jurídico, Técnico y Financiero) y Presupuesto (CDP, RP y Adiciones).
- **Combinación de Correspondencia:** Generación automatizada de minutas, estudios previos, certificados y documentos contractuales en formato Word (`.docx`) y paquetes comprimidos (`.zip`).
- **Módulo de Supervisión y Radicación de Cuentas de Cobro:** Elaboración de Informes de Gestión (IGC), Informes de Supervisión (ISC), Cuentas de Cobro (CC), carga de soportes en Google Drive con enrutamiento inteligente por período, flujo de revisión (Contratista → Supervisor → Contratación → Aprobación para Impresión), bitácora de trazabilidad y notificaciones en tiempo real.
- **Revaluación de Proveedores y Consecutivos:** Generación individual y masiva de formatos de revaluación de contratistas y gestión de consecutivos de cuentas de cobro.
- **Seguridad, Roles, Permisos y Auditoría:** Autenticación con hash criptográfico + salt (`PBKDF2` / `SHA-256`), control de acceso basado en roles (RBAC) y permisos granulares por usuario, con registro de auditoría de operaciones.

---

## 2. Tecnologías Utilizadas
- **Lenguaje y Plataforma:** Java EE 7 / Java 8 (Servlets, JSP, JSTL)
- **Servidor de Aplicaciones:** Apache Tomcat 9.x
- **Motor de Base de Datos:** PostgreSQL 14+ (Pool de conexiones vía JNDI `jdbc/combinacion`)
- **Construcción y Dependencias:** Apache Maven (`pom.xml`, empaquetado `.war`)
- **Frontend:** HTML5, CSS3, Bootstrap 5, DataTables, SweetAlert2, Bootstrap Icons
- **Procesamiento Documental:** Apache POI 5.2.3 (`.docx`, `.xlsx`, `.xls`) y XMLBeans
- **Integraciones Externas:** Google Drive API v3, Google Docs API v1, Google Sheets API v4 y JavaMail (SMTP)

---

## 3. Estructura del Proyecto (Backend y Frontend)
Siguiendo el estándar de arquitectura Java Web con Maven y los lineamientos de despliegue de **DATIC** (`MAGT04.04.01.P004.G001`):

```text
combinacion/
├── pom.xml                                  # Configuración de Maven y dependencias
├── README.md                                # Documento Léame del proyecto
├── plantillas/                              # Plantillas base (.docx y .xlsx) para combinación y cuentas
├── src/
│   └── main/
│       ├── java/com/combinacion/            # BACKEND (Código fuente Java)
│       │   ├── dao/                         # Capa de Acceso a Datos (PostgreSQL / JDBC)
│       │   ├── models/                      # Entidades y Modelos de Dominio (POJOs)
│       │   ├── services/                    # Capa de Lógica de Negocio e Integración (Drive/Sheets/Email)
│       │   ├── servlets/                    # Controladores HTTP (Servlets)
│       │   └── util/                        # Utilidades (Conexión JNDI, Seguridad, Generadores DOCX/PDF)
│       ├── resources/                       # Recursos de configuración y credenciales de servicio
│       └── webapp/                          # FRONTEND (Vistas JSP, activos estáticos y configuración web)
│           ├── assets/                      # Hojas de estilo CSS, scripts JS e imágenes
│           ├── inc/                         # Componentes reutilizables (Navbar, Header, Footer)
│           ├── WEB-INF/                     # web.xml y descriptores de despliegue
│           └── *.jsp                        # Vistas de los módulos del sistema
└── Documentación/                           # Documentación exigida por DATIC (MAGT04.04.01.P004.G001)
    ├── Manuales/                            # Manuales de instalación, operación y mantenimiento/backups
    ├── Modelo de Datos/                     # Modelo relacional, diccionario de datos y script SQL
    ├── Pruebas/                             # Casos de prueba (F001) e informes/actas de pruebas
    └── Arquitectura/                        # Diagrama de arquitectura y entregables de arquitectura empresarial
```

---

## 4. Requisitos e Instalación Rápida
1. **Base de Datos:** Crear la base de datos `combinacion` en PostgreSQL (codificación `UTF8`) y ejecutar el script `Documentación/Modelo de Datos/Script_Creacion_Base_de_Datos.sql` (al iniciar, la clase `DatabasePatcher` también verifica y asegura automáticamente las tablas, columnas, vistas y permisos base).
2. **Recurso JNDI en Tomcat:** Configurar el recurso `jdbc/combinacion` en `META-INF/context.xml` o en `conf/context.xml` de Apache Tomcat apuntando al servidor PostgreSQL.
3. **Compilación:**
   ```bash
   mvn clean package
   ```
4. **Despliegue:** Copiar el archivo generado `target/combinacion-1.0.war` en el directorio `webapps/` de Apache Tomcat.

---

## 5. Entidad Propietaria
**Alcaldía Distrital de Santiago de Cali**  
Departamento Administrativo de Gestión Jurídica Pública (DAGJP)

