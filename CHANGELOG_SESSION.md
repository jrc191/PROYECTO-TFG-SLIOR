# Resumen de Cambios - Sesión 12/05/2026

## 1. Implementación de Derechos RGPD (ARSULIPO)
- **Derechos de Usuario:** Se ha creado `UserController` y `UserService` para gestionar los derechos ARSULIPO (Acceso, Rectificación, Supresión, Limitación, Portabilidad).
    - `GET /api/v1/users/me/data-export`: Exportación completa de datos de usuario en formato JSON (Acceso/Portabilidad).
    - `DELETE /api/v1/users/me`: Solicitud de borrado de cuenta (Supresión/Olvido).
    - `POST /api/v1/users/me/limit-processing`: Activación/Desactivación de limitación de tratamiento.
- **Anonimización Automática:** Implementada tarea programada (`@Scheduled`) que anonimiza usuarios que solicitaron el borrado hace más de 7 días (configurable), reemplazando datos personales por identificadores genéricos.
- **Auditoría:** Todas las acciones de derechos RGPD se registran en la tabla `audit_log`.

## 2. Geocodificación Híbrida Completa
- **Integración de BD Local:** Se ha integrado la búsqueda en la tabla local `direcciones` dentro del `GeocodeService`.
- **Estrategia Cascada:** El flujo de búsqueda ahora es:
    1. Caché en Memoria (muy rápido).
    2. Caché Persistente en BD (rápido).
    3. Búsqueda en BD Local con índices Trigram (eficiente, offline-ready).
    4. Fallback a proveedores externos (Photon/Nominatim) con throttling.

## 3. Generación Asíncrona de Etiquetas
- **Mejora de Rendimiento:** La generación de PDFs de etiquetas ahora se realiza de forma asíncrona (`@Async`). 
- **Optimización de API:** La creación de rutas con muchas paradas (ej. 20+) ya no bloquea la respuesta del API, permitiendo una experiencia de usuario más fluida tanto en web como en móvil.

## 4. Estabilidad y Pruebas de Resiliencia
- **Configuración de Spring Boot:** Se han habilitado `@EnableScheduling` y `@EnableAsync`.
- **Backend (Automatizado):** Tests de integración con JUnit 5 para Rate limiting, RGPD, Fallback de Geocoder y Generación Async.
- **App Móvil (Manual):** Entrega de `mobile_test_guide.md` para validación de estados offline y WorkManager.

## 5. Mejoras de UI Móvil y Soporte Landscape
- **Menús Duales en Lista de Rutas:** Se han implementado dos cajones de navegación (drawers) laterales:
    - **Izquierda (Menú de Sistema):** Acceso a rutas, estadísticas, mapa general y ajustes.
    - **Derecha (Perfil de Usuario):** Muestra nombre y email del repartidor, con opciones para editar perfil, cambiar contraseña y cerrar sesión.
- **Soporte Landscape:** Ambos menús utilizan `LazyColumn` para asegurar que todo el contenido sea accesible mediante scroll en orientación horizontal o en pantallas pequeñas.
- **Integración de Datos:** El `RouteViewModel` ahora observa y expone el perfil del usuario actual para personalizar la interfaz.
