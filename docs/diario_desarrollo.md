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

## Fase 7: Dockerización y Despliegue en Red Local (Mayo 2026)
*   **Contenerización:** Creación de un `Dockerfile` multi-stage para el backend, permitiendo compilar y ejecutar el servidor sin necesidad de herramientas locales.
*   **Orquestación:** Integración del servicio `slior-backend` en `docker-compose.yml`, vinculándolo a la red interna y a la base de datos PostgreSQL.
*   **Acceso en Red Privada:** Configuración del acceso al backend mediante la IP de la red local o herramientas de red privada, manteniendo los servicios aislados de la red pública.
*   **Configuración por Perfiles:** Implementación de `application-docker.properties` para gestionar variables de entorno y conexiones dentro de la red de Docker.
*   **Persistencia de Logs:** Mapeo de volúmenes para que los logs y las etiquetas generadas sean accesibles desde el host.

## Fase 8: UI/UX Pro e Internacionalización (Mayo 2026)
*   **Modo Oscuro/Claro:** Implementación de un sistema de temas dinámico en Jetpack Compose que respeta la configuración del sistema y permite el cambio manual.
*   **Internacionalización (i18n):** Soporte multi-idioma completo para Español, Inglés, Francés, Portugués y Alemán, gestionado mediante `AppCompatDelegate`.
*   **Privacidad y Perfil:** Implementación de flujos para el cambio de contraseña segura y gestión de preferencias de privacidad (RGPD) directamente desde la app móvil.
*   **Rendimiento de Datos:** Optimización de consultas JPA con `@EntityGraph` para eliminar el problema de las consultas N+1 en el listado de rutas.
*   **Preparación de Futuras Funcionalidades:** Diseño de placeholders y estructura de navegación para el Mapa General y Estadísticas de Reparto.
