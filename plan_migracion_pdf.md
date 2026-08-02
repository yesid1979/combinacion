# Plan de Migración: Generación de PDF con docx4j + Apache FOP

Este documento sirve como hoja de ruta para reemplazar la dependencia externa de LibreOffice (`soffice`) en el servidor Linux por una solución 100% nativa en Java para la conversión de documentos DOCX a PDF.

## 🎯 Objetivo
Eliminar los problemas de renderizado de bordes en tablas largas con elementos anclados en Linux, utilizando `docx4j` (que renderiza usando Apache FOP) en lugar de un proceso externo de LibreOffice.

---

## 1. Dependencias de Maven (pom.xml)
Deberemos agregar las librerías oficiales de docx4j orientadas a la exportación a PDF.

```xml
<!-- Core de docx4j -->
<dependency>
    <groupId>org.docx4j</groupId>
    <artifactId>docx4j-JAXB-ReferenceImpl</artifactId>
    <version>8.3.9</version>
</dependency>
<!-- Exportador PDF usando Apache FOP -->
<dependency>
    <groupId>org.docx4j</groupId>
    <artifactId>docx4j-export-fo</artifactId>
    <version>8.3.9</version>
</dependency>
```
*(Nota: Las versiones pueden variar dependiendo de si el proyecto usa Java 8 o Java 11+. docx4j versión 8.x soporta Java 8, mientras que la 11.x es para Java 11+).*

---

## 2. Instalación de Fuentes en el Servidor Linux (¡CRÍTICO!)
A diferencia de LibreOffice (que a veces hace sustituciones mágicas), **Apache FOP requiere acceso directo a las fuentes (TTF)** utilizadas en el documento DOCX (como Arial, Times New Roman, Calibri, etc.). Si no las encuentra en el servidor Linux, los textos se verán desalineados o no se renderizarán correctamente.

**Pasos en el servidor CentOS/Ubuntu:**
1. Instalar las fuentes core de Microsoft:
   - Ubuntu: `sudo apt-get install ttf-mscorefonts-installer`
   - CentOS/RHEL: Descargar e instalar el paquete `msttcore-fonts-installer`.
2. Como alternativa más segura: Copiar manualmente la carpeta `C:\Windows\Fonts` (solo los archivos `.ttf` de Arial, Calibri, Tahoma, etc.) desde una máquina Windows hacia la ruta del servidor Linux: `/usr/share/fonts/windows/`.
3. Actualizar la caché de fuentes en Linux corriendo: `fc-cache -f -v`.

---

## 3. Modificaciones en el Código (`PdfGenerator.java`)
Se reemplazará el método que invoca comandos bash de LibreOffice por código Java puro usando docx4j.

```java
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import java.io.File;
import java.io.FileOutputStream;

public class PdfGenerator {
    
    // ... otros métodos ...

    /**
     * Motor nativo Java (Linux): docx4j + Apache FOP
     */
    private static boolean convertWithDocx4j(File docxFile, File pdfFile) {
        try (FileOutputStream os = new FileOutputStream(pdfFile)) {
            // 1. Cargar el documento original
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(docxFile);
            
            // 2. Mapeo de fuentes: Asegura que FOP encuentre la equivalente en Linux
            // (docx4j intentará hacer auto-descubrimiento en el sistema operativo)
            Mapper fontMapper = new IdentityPlusMapper();
            wordMLPackage.setFontMapper(fontMapper);
            
            // Si hay fuentes físicas específicas, se pueden registrar explícitamente:
            // PhysicalFonts.discoverPhysicalFonts();
            
            // 3. Conversión a PDF
            Docx4J.toPDF(wordMLPackage, os);
            
            return pdfFile.exists() && pdfFile.length() > 0;
            
        } catch (Exception e) {
            System.err.println("❌ Fallo en la conversión a PDF con docx4j: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
```

---

## 4. Pruebas y Consideraciones Adicionales
1. **Rendimiento:** Apache FOP consume bastante memoria RAM al generar PDFs pesados, especialmente si el documento tiene muchas páginas o imágenes embebidas grandes. Es vital asegurarse de que el contenedor Tomcat/Glassfish en Linux tenga un Heap (`-Xmx`) de al menos 1GB o 2GB.
2. **Formas Flotantes (VML/WPS):** Docx4j soporta formas y tablas muchísimo mejor que el importador de LibreOffice, sin embargo, los cuadros flotantes extremadamente exóticos a veces requieren ajustes en cómo se declaran en el XML. La limpieza que se hizo previamente conservando `<mc:Fallback>` VML seguirá siendo muy útil aquí.
3. **Imágenes:** Comprobar que las resoluciones de las imágenes del logo no sean excesivamente altas, ya que FOP las procesa tal cual vienen en el DOCX.

## Resumen del Flujo de Trabajo a Futuro:
1. Agregar dependencias en `pom.xml`.
2. Copiar fuentes `.ttf` al servidor Linux (`/usr/share/fonts/`).
3. Actualizar el método de Linux en `PdfGenerator.java`.
4. Habilitar nuevamente la descarga en los botones `.jsp` para apuntar a los PDFs.
