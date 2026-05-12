# Registro de Errores y Soluciones

## 1. Problema de Comunicación con el Emulador Android
*   **Error:** La app no conectaba con `localhost:8080`.
*   **Causa:** El emulador de Android tiene su propia red interna y ve su "localhost" como sí mismo, no como el PC anfitrión.
*   **Solución:** Cambiar la URL base a `10.0.2.2:8080` (dirección especial del emulador para acceder al host) o usar la IP de red local (ej. 192.168.x.x) si se usa un dispositivo físico.

## 2. Incompatibilidad de Librerías PDF (OpenPDF vs iText)
*   **Error:** Problemas de licencias y métodos obsoletos al intentar generar PDFs con librerías antiguas.
*   **Causa:** iText ha cambiado su modelo de licencia y muchas versiones antiguas no son compatibles con Java 17.
*   **Solución:** Migración a **OpenPDF**, que es un fork libre y actualizado. Se ajustaron las clases de color de `BaseColor` a `java.awt.Color`.

## 3. Desbordamiento de UI en Modo Horizontal
*   **Error:** Al rotar el móvil, el botón de "Confirmar Entrega" y la cabecera de perfil desaparecían o se cortaban.
*   **Causa:** Uso de alturas fijas y contenedores `Column` sin scroll.
*   **Solución:** Implementación de `LazyColumn` para permitir scroll vertical en pantallas con poca altura y rediseño de cabeceras usando `Row` para aprovechar el ancho en modo landscape.

## 4. Conflictos de Tipos en Room (Entidades Android)
*   **Error:** Error de compilación de Room al intentar guardar objetos complejos.
*   **Causa:** Room solo sabe guardar tipos primitivos por defecto.
*   **Solución:** Creación de `TypeConverters` para convertir fechas e identificadores complejos a Strings o Longs y viceversa.

## 5. Bloqueo del Hilo Principal al Generar Etiquetas
*   **Error:** El API tardaba varios segundos en responder al crear rutas con muchas paradas.
*   **Causa:** La generación de 20+ PDFs con códigos de barras es una tarea intensiva en CPU que bloqueaba la respuesta HTTP.
*   **Solución:** Habilitar `@EnableAsync` en Spring Boot y marcar el método de generación con `@Async` para que se ejecute en un hilo separado.

## 6. Problemas de Lectura de Códigos de Barras
*   **Error:** La cámara del móvil no enfocaba bien los códigos Code 128 pequeños.
*   **Causa:** El "Quiet Zone" (margen blanco) del código de barras era demasiado estrecho.
*   **Solución:** Ajustar la generación en `LabelService` para añadir un margen blanco forzoso alrededor del código, facilitando el enfoque del escáner.
