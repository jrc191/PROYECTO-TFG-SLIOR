# Diario de Desarrollo - Proyecto SLIOR

## Fase 1: Cimientos y Arquitectura (Enero - Febrero 2026)
*   **Definición del Stack:** Elección de Java 17 con Spring Boot para el backend por su robustez, y Kotlin con Jetpack Compose para la app móvil para asegurar una UI moderna.
*   **Diseño de Base de Datos:** Creación del esquema relacional en PostgreSQL para gestionar Usuarios, Rutas, Paradas y Auditoría.
*   **Estructura del Proyecto:** Configuración del monorepositorio con módulos separados para backend, móvil y contenedores Docker.

## Fase 2: Autenticación y Seguridad (Febrero 2026)
*   **Seguridad con JWT:** Implementación de Spring Security y generación de tokens JWT para comunicación stateless.
*   **Gestión de Usuarios:** Creación de endpoints de Login y Registro con encriptación de contraseñas mediante BCrypt.
*   **Módulo Móvil:** Implementación de pantallas de Auth con diseño brutalista y persistencia de sesión local con DataStore.

## Fase 3: Motor de Optimización y Mapas (Marzo 2026)
*   **Integración de OSMDroid:** Configuración de mapas basados en OpenStreetMap en la app Android.
*   **Algoritmo de Optimización:** Desarrollo de un servicio en el backend para reordenar paradas basándose en la distancia y el tipo de vehículo.
*   **Geocodificación:** Integración inicial con APIs externas para convertir direcciones en coordenadas.

## Fase 4: Geocodificación Híbrida y RGPD (Abril - Mayo 2026)
*   **Capa Híbrida:** Implementación de una estrategia de geocodificación en tres niveles: Caché en memoria, base de datos local (PostgreSQL con índices Trigram) y fallback externo.
*   **Cumplimiento RGPD:** Desarrollo de endpoints para el ejercicio de derechos ARSULIPO. Implementación de exportación de datos en JSON y sistema de anonimización automática (Derecho al Olvido) mediante tareas programadas.
*   **Auditoría:** Registro automático de acciones sensibles en la tabla `audit_log`.

## Fase 5: Modo Offline y Sincronización (Mayo 2026)
*   **Offline-First:** Implementación de Room en Android para permitir el funcionamiento de la app sin conexión a internet.
*   **Background Sync:** Uso de Android WorkManager para encolar entregas realizadas offline y sincronizarlas automáticamente al recuperar la red.
*   **Resiliencia:** Sistema de reintentos y monitor de conectividad en tiempo real con banners informativos.

## Fase 6: Logística Profesional y Cierre (Mayo 2026)
*   **Etiquetas PDF:** Creación de un servicio de generación de etiquetas logísticas profesionales con códigos de barras (Code 128) y QR.
*   **Rendimiento:** Optimización de la generación de etiquetas mediante procesos asíncronos (`@Async`) para no bloquear el API.
*   **Testing Extensivo:** Creación de una suite de tests JUnit 5 para validar seguridad, RGPD y casos límite de geocodificación.
*   **UI Adaptativa:** Ajustes finales para soporte completo de modo horizontal (Landscape) en la aplicación móvil.
