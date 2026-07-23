# Plan de Integración: Sincronización Automática con Google Sheets

## Objetivo
Implementar una funcionalidad que permita sincronizar automáticamente la base de datos del sistema con un documento alojado en Google Sheets, conviviendo en paralelo con la funcionalidad existente de carga masiva manual de archivos Excel/CSV.

## Requisitos Previos (Por confirmar con el usuario encargado)
1. **Verificar estructura de la matriz:** Asegurarse de que el documento en Google Sheets tiene exactamente las mismas columnas y estructura que el archivo Excel de carga manual.
2. **ID del Documento:** Obtener el ID único del Google Sheet (se extrae de la URL del navegador).
3. **Permisos de Google Drive:** Compartir el archivo de Google Sheets dándole permisos de "Lector" al correo electrónico de la cuenta de servicio generada en Google Cloud Console (la que termina en `@tu-proyecto.iam.gserviceaccount.com`).

## Arquitectura y Diseño (Lo que vamos a programar)
1. **Mantener la resiliencia:** Conservar la opción actual de carga manual como un plan de respaldo (Fallback).
2. **Interfaz Gráfica (UI):** Agregar un botón en el módulo `carga_masiva.jsp` con el texto: *"Sincronizar automáticamente con Google Drive"*.
3. **Backend en Java (Servlet):**
   - Incorporar las librerías/dependencias oficiales de Google API al proyecto.
   - Programar un nuevo controlador (ej. `GoogleSyncServlet.java`) que procese el archivo de credenciales (`.json`) para autenticarse con los servidores de Google.
   - Escribir la lógica para leer las filas del archivo en la nube, procesarlas y reutilizar la lógica de persistencia a base de datos del módulo de carga masiva actual.

## Siguientes Pasos (Para mañana)
- Recibir confirmación de la estructura del archivo por parte del usuario encargado.
- Definir si el proyecto utiliza Maven (`pom.xml`) para inyectar las dependencias de Google automáticamente, o si se deben descargar los `.jar` de forma manual.
- Escribir el código en el frontend y en el backend, y realizar las pruebas de conexión.
