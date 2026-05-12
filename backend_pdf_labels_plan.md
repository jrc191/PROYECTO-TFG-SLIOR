# Plan: Generación de Etiquetas PDF (Fase 6)

## Objetivo
Implementar en el backend la generación automática de etiquetas de envío en formato PDF. Cada etiqueta incluirá la información de la parada (destinatario, dirección) y dos códigos (QR y Barcode 128) que codifican el UUID de la parada (36 caracteres).

## Tareas

### 1. Preparación del Entorno (Completado)
- [x] Añadir dependencias en `pom.xml`: `zxing` (core y javase) y `openpdf`.

### 2. Desarrollo del Servicio de Etiquetas
- **Clase**: `com.slior.service.LabelService`
- **Funcionalidad**:
    - Generar imagen de código QR a partir de un texto (UUID).
    - Generar imagen de código de barras 128 a partir de un texto (UUID).
    - Componer el PDF usando OpenPDF con:
        - Título "ETIQUETA DE ENVÍO - SLIOR".
        - Datos del destinatario y dirección.
        - Imagen del QR.
        - Imagen del Barcode 128 (optimizado para cadenas largas de 36 caracteres).
    - Retornar el PDF como un array de bytes (`byte[]`).

### 3. Exposición de API
- **Controlador**: `com.slior.controller.RouteController`
- **Endpoint**: `GET /api/v1/routes/stops/{stopId}/label`
- **Respuesta**: Stream del PDF con cabeceras `Content-Type: application/pdf`.

### 4. Verificación
- Generar una etiqueta para una parada existente.
- Validar visualmente el PDF generado.
- Escanear ambos códigos con la app móvil para confirmar que decodifican el UUID completo de 36 caracteres.

## Detalles Técnicos
- El UUID se obtendrá directamente del campo `id` de la entidad `Stop`.
- Se usará `java.awt.image.BufferedImage` para manipular las imágenes de los códigos antes de insertarlas en el PDF.
- El código de barras 128 se configurará con `TRY_HARDER` si es necesario, aunque en generación esto no aplica, sino en lectura. Lo importante es que las barras sean nítidas.
