# Tecnologías - Documento Técnico TFG

## 1. Arquitectura del Sistema
El sistema SLIOR se basa en una arquitectura de microservicios simplificada, utilizando un backend centralizado y una aplicación cliente móvil. Se ha priorizado la desacoplación de componentes para facilitar el mantenimiento y la escalabilidad.

## 2. Backend (Servidor de Aplicaciones)
*   **Framework:** Spring Boot 3.2.x. Proporciona una infraestructura de inversión de control y contenedores de beans para una gestión eficiente de dependencias.
*   **Seguridad:** Spring Security 6 con autenticación basada en JWT (JSON Web Token). Se utiliza un filtro personalizado para interceptar peticiones y validar la integridad del token en cada solicitud.
*   **Persistencia:** Spring Data JPA con Hibernate como proveedor de ORM (Object-Relational Mapping).
*   **Base de Datos:** PostgreSQL 15. Se aprovecha el soporte nativo para tipos de datos complejos y extensiones de búsqueda de texto (pg_trgm) para la geocodificación híbrida.

## 3. Mobile App (Cliente Android)
*   **Lenguaje:** Kotlin 1.9.x.
*   **UI Framework:** Jetpack Compose. Implementación de una interfaz reactiva y declarativa.
*   **Inyección de Dependencias:** Hilt (Dagger). Centralización de la creación de objetos y gestión de ciclos de vida.
*   **Persistencia Local:** Room Database. Implementación de un sistema de caché local para soporte offline-first.
*   **Tareas en Segundo Plano:** Android WorkManager. Garantiza la ejecución de tareas críticas (sincronización de entregas) mediante políticas de reintento exponenciales y restricciones de red.

## 4. Servicios Complementarios e Integraciones
*   **Mapas y Ubicación:** OSMDroid (OpenStreetMap) para la visualización geoespacial.
*   **Geocodificación:** Sistema híbrido que integra caché persistente (Redis/Local DB) y fallback a servicios REST externos (Photon/Nominatim).
*   **Generación de Documentos:** OpenPDF para la maquetación de etiquetas logísticas y ZXing para la generación de simbologías de códigos de barras (Code 128) y códigos bidimensionales (QR).
*   **Despliegue:** Docker y Docker Compose para la containerización de servicios de infraestructura (Base de datos y servicios de mapas).
