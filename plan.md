# Plan: Backend SLIOR - Geocodificación Híbrida + Seguridad RGPD

## Objetivo
Implementar backend production-ready con geocodificación híbrida (BD local + Nominatim fallback), seguridad RGPD/LOPD completa y endpoints de privacidad. Plazo: 2 días (4h máximo opción híbrida + implementación seguridad crítica).

## Estado Actual (2026-05-08 20:53 UTC)
- ✅ Spring Boot 3.3.4 configurado
- ✅ Autenticación JWT implementada (HS256 temporal)
- ✅ Endpoints versionados a `/v1/` (seg-0)
- ✅ Headers HTTP de seguridad implementados (seg-1)
- ✅ **Repositorio migrado a GitLab** (limpio, sin historial de BD)
- ✅ PostgreSQL funcionando en Docker
- ✅ Rate limiting implementado (seg-2)
- ✅ Logs sanitizados sin datos personales (seg-3)
- ✅ Correlation IDs (X-Request-ID) implementados (seg-4)
- ✅ ErrorResponse estandarizado (seg-5)
- ✅ Tabla de Auditoría (audit_log) y servicio implementados (seg-6)
- ✅ Migraciones Flyway configuradas (fijadas v10.10.0)
- ✅ Entidad User actualizada con campos RGPD (bd-user-fields)
- ✅ Reestructuración completa al paquete `com.slior`
- ✅ DTOs y Repositorios de geocodificación preparados (geo-2)
- ❌ Geocodificación vía Photon (descartado por complejidad)
- ❌ Sin endpoints RGPD (fase 2)

## Estructura de Ramas (GitLab)

```
main (principal, código limpio)
  ├─ develop (rama de desarrollo)
  │
  ├─ feature/fase-1-autenticacion
  ├─ feature/fase-2-gestion-rutas
  ├─ feature/fase-3-optimizacion
  │
  ├─ feature/fase-4-mapas (RAMA PADRE)
  │  └─ feature/fase-4.1-geocoder-autohospedado (SUB-RAMA de 4)
  │     ├─ feature/fase-4.1.1-seguridad-critica      ✅ ACTIVA
  │     ├─ feature/fase-4.1.2-geocodificacion-hibrida
  │     ├─ feature/fase-4.1.3-derechos-rgpd
  │     ├─ feature/fase-4.1.4-modelos-bd
  │     ├─ feature/fase-4.1.5-tests
  │     └─ feature/fase-4.1.6-documentacion
  │
  └─ feature/fase-5-diseno-ui
```

**Relación de ramas verificada:**
- ✅ `4.1` desciende de `4` (comprobado con `merge-base`)
- ✅ `4.1.X` desciende de `4.1`
- ✅ Estructura correcta para TFG (trazabilidad de fases)
**Marco normativo:** RGPD (UE) 2016/679 + LOPDGDD (LO 3/2018)

### 0.1 - Versionado de Endpoints a `/v1/`
Afecta a:
- `/auth/register` → `/auth/v1/register`
- `/auth/login` → `/auth/v1/login`
- `/api/geocode/search` → `/api/v1/geocode/search`
- `/api/routes/*` → `/api/v1/routes/*`

**Archivos:** AuthController, RouteController, GeocodeController, SecurityConfig

### 0.2 - Headers HTTP de Seguridad
Agregar en SecurityConfig:
- `HSTS` (Strict-Transport-Security, 1 año)
- `CSP` (Content-Security-Policy)
- `X-Frame-Options: DENY`
- `X-XSS-Protection`
- `Referrer-Policy: no-referrer`

**Archivo:** SecurityConfig.java

### 0.3 - Rate Limiting (Bucket4j)
Nuevos límites por endpoint:
- `POST /auth/v1/login`: 5 intentos/min
- `POST /auth/v1/register`: 3 registros/min
- `GET /api/v1/geocode/search`: 30 peticiones/min
- Resto API: 100 peticiones/min
- Respuesta 429 Too Many Requests

**Archivos:** pom.xml (bucket4j), RateLimitingFilter.java (nuevo)
**Estado:** completado

### 0.4 - Logs sin Datos Personales
Revisar y corregir:
- ❌ `log.info("Login para {}", user.getEmail())`
- ✅ `log.info("Login. userId={}", user.getId())`
- ❌ `log.warn("IP: {}", request.getRemoteAddr())`
- ✅ `log.warn("requestId={}", correlationId)`

**Archivos:** Todas las clases con logging (service, controller)

### 0.5 - Correlation IDs (X-Request-ID)
Filtro que genere y propague `X-Request-ID`:
- Generar UUID si no existe
- Usar en MDC para logs
- Propagar a respuesta HTTP

**Archivos:** CorrelationIdFilter.java (nuevo)

### 0.6 - ErrorResponse Estandarizado
Reemplazar respuestas genéricas con envelope:
```json
{
  "timestamp": "2025-05-05T10:30:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Búsqueda demasiado corta",
  "path": "/api/v1/geocode/search",
  "requestId": "f47ac10b-..."
}
```

**Archivos:** ErrorResponse.java (record), GlobalExceptionHandler.java (actualizar)

### 0.7 - Tabla de Auditoría (audit_log)
Crear tabla para registrar acciones sobre datos personales:
- `user_id` (quién)
- `accion` (LOGIN, EXPORT_DATA, DELETE_ACCOUNT, etc.)
- `entidad` (User, Route, Stop)
- `ip_hash` (SHA-256, nunca raw)
- `timestamp`, `request_id`

Retención: 1-3 años (script de limpieza automática)

**Archivos:** audit_log.sql (schema), AuditInterceptor.java (nuevo)

---

## FASE 1: Geocodificación Híbrida (Opción Offline-First)
**Estrategia:** Caché → BD Local → Nominatim fallback

### 1.1 - Obtener e Importar Datos de Direcciones
Fuentes libres:
- **Callejero Digital Catastro (CDAU)** o
- **OSM Spain extract** (~80MB, direcciones españolas)

Tareas:
- Descargar CDAU/OSM España
- Crear tabla `direcciones` con índice trigram
- Script SQL para COPY desde CSV
- Testear query ILIKE ~50ms

**Archivos:** schema.sql, import-direcciones.sql, README

### 1.2 - Crear Tabla de Caché Geocode
```sql
CREATE TABLE geocode_cache (
    query_normalized VARCHAR(255) PRIMARY KEY,
    results JSONB,
    source VARCHAR(10), -- 'local' | 'nominatim'
    created_at TIMESTAMP DEFAULT NOW(),
    last_accessed_at TIMESTAMP DEFAULT NOW()
);
```

**Archivos:** schema.sql

### 1.3 - DTO + Repository para Direcciones
- `AddressSuggestion.java` (DTO con id, nombre, municipio, provincia, lat, lng)
- `DireccionRepository.java` (findByNombreILike, custom JPQL)
- `GeocodeCacheRepository.java` (findByQueryNormalized, TTL)

**Archivos:** modelo, repositorio (4 archivos nuevos)

### 1.4 - Servicio Híbrido AddressSuggestionService
Lógica en cascada:
1. `searchCache(query)` → 5ms
2. `searchLocal(query)` → 50ms (BD con trigram)
3. `searchNominatim(query)` → 500ms (HTTP throttled)
4. Guardar resultado en caché

Throttle: 1 request/segundo a Nominatim (respetar TOS)

**Archivos:** AddressSuggestionService.java (120 líneas)

### 1.5 - NominatimClient (HTTP Client)
- RestTemplate con timeout 3s/5s (conexión/lectura)
- User-Agent correcto
- Manejo de errores (503 si Nominatim no responde)

**Archivos:** NominatimClient.java

### 1.6 - GeocodeController versionado a `/v1/`
- `GET /api/v1/geocode/search?q=query&limit=10`
- Llamar a `AddressSuggestionService`
- Devolver `List<AddressSuggestion>`

**Archivos:** GeocodeController.java (actualizar)

### 1.7 - Eliminar Photon
- Borrar `PhotonAutoStarter.java`
- Borrar config de Photon en `application.properties`
- Borrar dependencia Photon de `pom.xml`
- Actualizar documentación

**Archivos:** PhotonAutoStarter.java (eliminar), application.properties (actualizar), pom.xml

---

## FASE 2: Derechos RGPD (Endpoints ARSULIPO)

### 2.1 - Derecho de Acceso (A)
`GET /api/v1/users/me/data-export`
- Devolver JSON con: perfil, rutas, paradas, fechas
- Registrar en `audit_log` acción EXPORT_DATA

**Archivos:** UserController.java, UserDataExportDto.java

### 2.2 - Derecho de Rectificación (R)
`PATCH /api/v1/users/me`
- Permitir actualizar nombre, email, teléfono
- Registrar cambios en `audit_log`
- Solo el propio usuario puede modificar sus datos

**Archivos:** UserController.java

### 2.3 - Derecho de Supresión/Olvido (S)
`DELETE /api/v1/users/me`
- Marcar cuenta como `pendiente_borrado` (soft delete)
- Período de gracia: 30 días
- `@Scheduled` diario para anonimizar usuarios pendientes
- Anonimizar: email → `anon_UUID@deleted.slior`, nombre → `USUARIO_ELIMINADO`

**Archivos:** UserController.java, ScheduledTasks.java (nuevo)

### 2.4 - Derecho de Limitación del Tratamiento (U)
`POST /api/v1/users/me/limit-processing`
`DELETE /api/v1/users/me/limit-processing`
- Campo `boolean tratamientoLimitado` en User
- Si activo: datos no se procesan en batch ni exportan

**Archivos:** User.java (agregar campo), UserController.java

### 2.5 - Derecho de Portabilidad (L)
`GET /api/v1/users/me/data-export?format=json|csv`
- JSON: estructura plana, sin campos internos
- CSV: una fila por parada, cabeceras en español

**Archivos:** UserController.java, UserDataExportService.java

---

## FASE 3: Cambios en Modelos + BD

### 3.1 - Actualizar Entidad User
Nuevos campos RGPD:
- `boolean isDeleted` (soft delete)
- `LocalDateTime deletionRequestedAt` (cuándo se pidió borrar)
- `boolean tratamientoLimitado` (limitación del tratamiento)
- `boolean consentimientoNotificaciones`
- `boolean consentimientoGeolocalizacion`
- `LocalDateTime anonymizedAt` (cuándo se anonimizó)

**Archivos:** User.java, migration Flyway/Liquibase

### 3.2 - Crear Migraciones BD
- Crear tabla `audit_log`
- Crear tabla `direcciones` con índice trigram
- Crear tabla `geocode_cache`
- Agregar columnas a `users`
- Crear índices de performance

**Archivos:** db/migration/V*.sql (Flyway)

---

## FASE 4: Validación + Tests

### 4.1 - Tests de Híbrida
- Test caché vacío → fallback a BD
- Test BD vacío → fallback a Nominatim (mock)
- Test Nominatim con throttle
- Test offline (sin conexión → BD local solo)

**Archivos:** AddressSuggestionServiceTest.java

### 4.2 - Tests de Seguridad
- Rate limiting activado (429)
- Headers HTTP presentes
- Logs sin datos personales
- Correlation ID en logs

**Archivos:** SecurityTest.java, RateLimitingTest.java

### 4.3 - Tests de RGPD
- `GET /api/v1/users/me/data-export` devuelve datos correctos
- `DELETE /api/v1/users/me` marca como pendiente
- Scheduled task anonimiza correctamente
- `audit_log` registra acciones

**Archivos:** UserControllerTest.java, AnonimizationTaskTest.java

---

## FASE 5: Documentación + Deployment

### 5.1 - README.md Actualizado
- Eliminar doc sobre Photon
- Agregar instrucciones para importar datos Catastro
- Explicar arquitectura híbrida (caché → local → Nominatim)
- Cómo correr en desarrollo (Maven + BD local)

### 5.2 - Documento RAT (Registro de Actividades)
Para AEPD: responsable, finalidad, bases legales, medidas técnicas

### 5.3 - Política de Conservación de Datos
- Datos usuario activo: duración contrato
- Datos tras baja: 5 años (prescripción)
- Logs auditoría: 1 año
- Logs sistema: 90 días

### 5.4 - Canal para Derechos RGPD
- Email: `privacidad@slior.es` (o equivalente)
- Aparece en: política privacidad, app, formulario registro

---

## 🎯 Hitos + Prioridades

| Prioridad | Fase | Tareas | Tiempo Est. |
|-----------|------|--------|------------|
| 🔴 CRÍTICO | **0** | Versionado, Headers, Rate limiting, Logs, ErrorResponse | 2h |
| 🔴 CRÍTICO | **1** | Híbrida completa (datos, caché, servicio, endpoint) | 2h |
| 🟠 ALTO | **2** | Endpoints RGPD (acceso, rectificación, supresión) | 2h |
| 🟠 ALTO | **3** | Migraciones BD (audit_log, direcciones, campos User) | 1h |
| 🟡 MEDIO | **4** | Tests (30 casos: seguridad, híbrida, RGPD) | 1.5h |
| 🟡 MEDIO | **5** | Documentación + RAT + README | 1h |

**TOTAL MÁXIMO:** 9.5 horas (viable en 2 días de 4-5h c/uno)

---

## ✅ Resultado Final

**Backend production-ready:**
- ✅ Geocodificación offline-first (BD local ~5-50ms)
- ✅ Fallback a Nominatim (gratuito, ~500ms)
- ✅ Sin Photon (sin índices complejos)
- ✅ RGPD/LOPD completo (auditoría, derechos, privacidad)
- ✅ Seguridad crítica implementada (rate limiting, headers, logs limpios)
- ✅ Endpoints versionados `/v1/`
- ✅ Tests de seguridad + funcionalidad
- ✅ Documentación y RAT

**Listo para:** Integración con app Android (Kotlin + Jetpack Compose)
