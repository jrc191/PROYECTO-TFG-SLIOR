# SLIOR — Sistema de Optimización de Rutas Logísticas

> Proyecto de Fin de Ciclo (TFG) · 2º CFGS Desarrollo de Aplicaciones Multiplataforma  
> Alumno: José Ramos Contioso · Curso 2025-2026

---

## Descripción

**SLIOR** es una aplicación móvil Android con backend REST que permite a repartidores y pequeñas empresas de logística:

-  **Planificar rutas** de entrega con múltiples paradas
-  **Optimizar el orden** de las paradas automáticamente (algoritmo Nearest Neighbor)
-  **Gestionar paquetes** mediante escaneo de códigos de barras
-  **Trabajar sin conexión** con sincronización automática al recuperar red
-  **Visualizar rutas** en mapa (OpenStreetMap)

---

## Tecnologías

| Capa | Tecnología |
|------|-----------|
| Backend | Java 17 + Spring Boot 3.2.4 + PostgreSQL 15 |
| API Security | Spring Security + JWT (jjwt 0.12.3) |
| Persistencia BD | Spring Data JPA + Hibernate |
| Build Backend | Maven 3.9+ |
| App Móvil | Kotlin 1.9+ · Android API 24+ (Android 7.0) |
| Arquitectura App | MVVM + Clean Architecture |
| Persistencia Local | Room 2.6.1 |
| HTTP Client | Retrofit 2.9.0 + OkHttp |
| Inyección Dep. | Hilt 2.48 |
| Asincronía | Coroutines + Flow |
| Mapas | OSMDroid 6.1.17 (OpenStreetMap) |
| Escaneo | ZXing 4.3.0 |
| Background Tasks | WorkManager |

---

## Estructura del Repositorio

```
slior-project/
 backend/                    # API REST Spring Boot
    src/
        main/java/com/slior/
           config/         # Configuración (Security, CORS)
           controller/     # REST Controllers
           service/        # Lógica de negocio
           repository/     # JPA Repositories
           model/          # Entidades JPA + Enums
           dto/            # Data Transfer Objects
           security/       # JWT + Filtros
           exception/      # Excepciones + GlobalExceptionHandler
           util/           # Utilidades
        resources/
            application.properties
 mobile-app/                 # Aplicación Android
    app/src/main/java/com/slior/
        ui/                 # Activities + Fragments
        viewmodel/          # ViewModels
        data/
           local/          # Room DAO + Database
           remote/         # Retrofit API
           repository/     # Patrón Repository
        model/              # Data classes
        di/                 # Módulos Hilt
        util/               # Helpers
 docs/                       # Documentación técnica
 scripts/                    # Scripts de setup y utilidades
 .gitignore
 LICENSE
 README.md
```

---

## Setup y Ejecución

### Requisitos Previos

- JDK 17+ instalado
- PostgreSQL 15+ corriendo localmente
- Android Studio (Android)
- Maven 3.9+

### 1. Base de Datos

```sql
CREATE DATABASE slior_db;
CREATE USER slior_user WITH PASSWORD 'slior_pass';
GRANT ALL PRIVILEGES ON DATABASE slior_db TO slior_user;
```

### 2. Backend

```bash
cd backend
# Configurar src/main/resources/application.properties si es necesario
mvn clean compile       # Verificar compilación
mvn spring-boot:run     # Arrancar servidor (puerto 8080)
```

### 3. App Android

```bash
cd mobile-app
./gradlew build                        # Compilar
./gradlew assembleDebug                # Generar APK debug
./gradlew connectedAndroidTest         # Tests instrumentados
```

> **Nota para emulador:** La base URL del backend es `http://10.0.2.2:8080/` (redirige a localhost del host).

---

## API Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | /auth/register | Registro de nuevo usuario |
| POST | /auth/login | Autenticación, retorna JWT |
| GET | /routes | Listar rutas del usuario |
| POST | /routes | Crear nueva ruta con paradas |
| GET | /routes/{id} | Obtener ruta específica |
| PUT | /routes/{id} | Actualizar ruta |
| DELETE | /routes/{id} | Eliminar ruta (lógico) |
| POST | /routes/{id}/optimize | Optimizar orden de paradas |
| GET | /routes/{id}/stops | Listar paradas de una ruta |
| POST | /packages | Registrar paquete |
| GET | /packages/barcode/{code} | Buscar por código de barras |
| PUT | /packages/{id}/deliver | Marcar como entregado |

---

## Fases de Desarrollo

| Fase | Descripción | Horas |
|------|-------------|-------|
| 0 | Inicialización y estructura | 0.5 |
| 1 | Autenticación (Backend + Android) | 15 |
| 2 | Gestión de Rutas (Backend) | 12 |
| 3 | Optimización de Rutas | 8 |
| 4 | Mapas y Navegación (Android) | 20 |
| 5 | Paquetes y Códigos de Barras | 15 |
| 6 | Sincronización Offline | 12 |
| 7 | Testing y Refinamiento | 13 |
| **Total** | | **~100 h** |

---

## Decisiones Arquitectónicas

- **Offline-First**: Room es la única fuente de verdad. La UI observa Room, no Retrofit directamente.
- **UUID como PK**: Tanto en PostgreSQL como en Room (String en Android).
- **Borrado lógico**: Campo `isDeleted = true`, nunca DELETE físico.
- **Sealed States**: Cada ViewModel expone un `StateFlow<State>` con clases selladas (Loading/Success/Error).
- **WorkManager Sync**: Los cambios offline se encolan en `SyncQueue` y se sincronizan en background.

---

## Documentación del Proyecto

| Documento | Descripción |
|-----------|-------------|
| [`docs/diario_desarrollo.md`](docs/diario_desarrollo.md) | Registro cronológico de cada paso del desarrollo (decisiones, comandos, resultados) |
| [`docs/tecnologias_explicacion_personal.md`](docs/tecnologias_explicacion_personal.md) | Explicación informal de todas las tecnologías usadas |
| [`docs/tecnologias_documento_tfg.md`](docs/tecnologias_documento_tfg.md) | Justificación técnica formal para la memoria del TFG |
| [`docs/errores_y_soluciones.md`](docs/errores_y_soluciones.md) | Registro de todos los errores encontrados y sus soluciones |

---

## Licencia

MIT © 2026 José Ramos Contioso
