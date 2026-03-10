# Diario de Desarrollo — SLIOR

> Registro detallado de cada decisión técnica, comando ejecutado y problema
> encontrado durante el desarrollo. Fuente primaria para la Memoria del TFG.

---

## FASE 0: Inicialización del Proyecto

**Fecha:** 09/03/2026  
**Duración estimada:** 30 minutos  
**Estado:**  Completada

---

### 0.1 Entorno de Trabajo

| Elemento | Valor |
|----------|-------|
| Sistema Operativo | Windows 11 |
| IDE Backend | IntelliJ IDEA 2025.2.1 |
| IDE Android | Android Studio 2025.1.3 Meerkat |
| Ruta del proyecto | `C:\Users\User\Documents\PROYECTO-TFG\slior-project\` |
| Control de versiones | Git |

---

### 0.2 Creación de la Estructura de Directorios

Creé la estructura de carpetas del monorepo con el siguiente comando PowerShell:

```powershell
New-Item -ItemType Directory -Path "slior-project"
New-Item -ItemType Directory -Path "slior-project\backend"
New-Item -ItemType Directory -Path "slior-project\mobile-app"
New-Item -ItemType Directory -Path "slior-project\docs"
New-Item -ItemType Directory -Path "slior-project\scripts"
```

**Estructura resultante:**
```
slior-project/
 backend/      → API REST Spring Boot
 mobile-app/   → Aplicación Android
 docs/         → Documentación técnica
 scripts/      → Scripts de utilidad (BD, arranque)
```

**Decisión:** Opté por un **monorepo** (un único repositorio Git para backend y app móvil) en lugar de repositorios separados. Ventajas para un proyecto académico:
- Un solo `git clone` para tener todo el código
- Commits coordinados entre backend y frontend
- Más sencillo para el tribunal evaluador

---

### 0.3 Inicialización de Git

```bash
cd slior-project
git init
git branch -M main   # Rama principal llamada 'main' (convención moderna)
```

**Resultado:** Repositorio Git vacío inicializado en `.git/`.

---

### 0.4 Archivo `.gitignore`

Creé `.gitignore` con exclusiones para:
- **Java/Maven:** carpeta `target/`, archivos `.class`, `.jar`
- **Android/Gradle:** carpeta `build/`, `.gradle/`, `.apk`, `.aab`
- **IDEs:** `.idea/`, `.vscode/`, `.cursor/`
- **Seguridad:** `*.env`, `application-prod.properties`, `keystore.properties`, `google-services.json`

**Decisión importante:** Incluí `application-prod.properties` en el gitignore para evitar exponer credenciales de producción en el repositorio público.

---

### 0.5 Licencia MIT

Elegí la **licencia MIT** por ser la más permisiva y adecuada para un proyecto académico que puede servir de referencia a otros estudiantes.

---

### 0.6 Configuración Backend (Spring Boot)

#### Dependencias Maven (`pom.xml`)

| Dependencia | Versión | Motivo |
|-------------|---------|--------|
| spring-boot-starter-web | 3.2.4 | API REST con Tomcat embebido |
| spring-boot-starter-data-jpa | 3.2.4 | ORM con Hibernate y PostgreSQL |
| spring-boot-starter-security | 3.2.4 | Autenticación y autorización |
| spring-boot-starter-validation | 3.2.4 | Validación de DTOs (Jakarta) |
| spring-boot-starter-mail | 3.2.4 | Envío de emails (confirmaciones) |
| postgresql | runtime | Driver JDBC para PostgreSQL 15 |
| lombok | opcional | Reducir código boilerplate |
| jjwt-api/impl/jackson | 0.12.3 | Generación y validación de JWT |
| spring-boot-starter-test | test | JUnit 5 + Mockito |

**¿Por qué jjwt 0.12.3?** Es la versión más reciente de la librería JJWT con API modernizada. Las versiones anteriores (<0.11) tienen una API diferente y menos segura.

**¿Por qué UUID como ID?** Los UUIDs son independientes de la base de datos (no necesitan autoincremento), son globalmente únicos (útil para sincronización offline) y no exponen información sobre el volumen de datos (a diferencia de IDs numéricos secuenciales).

#### Estructura de paquetes backend

```
com.slior
 config/          → SecurityConfig, CorsConfig
 controller/      → AuthController, RouteController, PackageController
 service/         → AuthService, RouteService, RouteOptimizationService, PackageService
 repository/      → UserRepository, RouteRepository, StopRepository, PackageRepository
 model/           → Entidades JPA (User, Route, Stop, Package)
    enums/       → UserRole, RouteStatus, StopStatus, PackageStatus
 dto/             → Data Transfer Objects (entrada/salida de API)
    auth/        → LoginRequest, RegisterRequest, AuthResponse
    route/       → CreateRouteRequest, RouteResponse, etc.
    mapper/      → RouteMapper
 security/        → JwtUtil, JwtAuthenticationFilter, CustomUserDetailsService
 exception/       → GlobalExceptionHandler + excepciones custom
 util/            → Haversine, helpers
```

**¿Por qué DTOs separados de las entidades?** Las entidades JPA representan la estructura interna de la BD. Exponer directamente puede filtrar datos sensibles, crear dependencias del schema en el contrato de la API, y dificultar la evolución independiente. Los DTOs actúan como contrato explícito de la API.

#### `application.properties`

Se configuraron:
- Conexión a PostgreSQL en `localhost:5432` (base de datos `slior_db`)
- `ddl-auto=update` para que Hibernate cree/actualice tablas automáticamente en desarrollo
- JWT secret placeholder (se debe cambiar en producción por una clave de mínimo 256 bits)
- Access token con expiración de **30 minutos** (valor `1800000` ms)

---

### 0.7 Configuración Mobile App (Android)

#### Dependencias Android (`build.gradle.kts`)

| Dependencia | Versión | Motivo |
|-------------|---------|--------|
| Room | 2.6.1 | Base de datos SQLite local (offline-first) |
| Retrofit | 2.9.0 | Cliente HTTP type-safe para la API REST |
| OkHttp logging-interceptor | 4.12.0 | Log de peticiones HTTP en desarrollo |
| Hilt | 2.48 | Inyección de dependencias (simplifica DI vs Dagger) |
| Coroutines | 1.7.3 | Asincronía sin callbacks (más legible) |
| DataStore | 1.0.0 | Almacenamiento del JWT (reemplaza SharedPreferences) |
| WorkManager | 2.9.0 | Tareas en background (sincronización offline) |
| OSMDroid | 6.1.17 | Mapas offline con OpenStreetMap (sin clave API) |
| Play Services Location | 21.1.0 | GPS/red del dispositivo (FusedLocationProvider) |
| ZXing Android Embedded | 4.3.0 | Escáner de códigos de barras con cámara |

**¿Por qué Hilt en vez de Koin?** Hilt está integrado con el ciclo de vida de Android, tiene mejor soporte para ViewModel y WorkManager, y es la solución oficial recomendada por Google. Koin usa reflexión en runtime (más lento).

**¿Por qué OSMDroid en vez de Google Maps?** OpenStreetMap es completamente gratuito sin límites de uso. Google Maps SDK requiere clave API con facturación activada y tiene un límite de peticiones gratuitas que puede ser problemático en un entorno académico.

**¿Por qué minSdk=24 (Android 7.0)?** Cubre aproximadamente el 95% de los dispositivos Android activos, excluyendo solo terminales muy antiguos. Android 7.0 tiene soporte nativo para Java 8 (streams, lambdas).

#### Estructura de paquetes Android

```
com.slior
 ui/
    auth/        → LoginActivity, RegisterActivity
    routes/      → RoutesListActivity, RouteDetailActivity, CreateRouteActivity
    packages/    → PackageDetailActivity, BarcodeScannerActivity, ScanPackageFragment
    map/         → MapFragment
    components/  → NetworkStatusBar
 viewmodel/       → AuthViewModel, RouteViewModel, PackageViewModel
 data/
    local/
       entity/  → UserEntity, RouteEntity, StopEntity, PackageEntity, SyncQueue
       dao/     → UserDao, RouteDao, StopDao, PackageDao, SyncQueueDao
    remote/
       dto/     → DTOs de red (LoginRequest, AuthResponse, etc.)
    repository/  → AuthRepository, RouteRepository, PackageRepository, SyncRepository
 model/           → Data classes del dominio
 di/              → DatabaseModule, NetworkModule, WorkerModule, AppModule
 util/            → LocationHelper, NetworkMonitor, Result
```

#### `AndroidManifest.xml` — Permisos declarados

| Permiso | Necesario para |
|---------|---------------|
| INTERNET | Comunicación con backend REST |
| ACCESS_NETWORK_STATE | Detectar si hay conexión (NetworkMonitor) |
| ACCESS_FINE_LOCATION | GPS preciso para optimización de ruta |
| ACCESS_COARSE_LOCATION | Ubicación aproximada (fallback) |
| CAMERA | Escáner de códigos de barras ZXing |
| WRITE_EXTERNAL_STORAGE | Caché de tiles OSMDroid (solo hasta API 28) |

**Nota:** `android:usesCleartextTraffic="true"` está activado solo para desarrollo (peticiones HTTP a localhost). En producción se debe desactivar y usar HTTPS.

---

### 0.8 Clase Application (`SliorApplication.kt`)

Creé la clase Application con:
- **`@HiltAndroidApp`**: dispara la generación de código de Hilt al compilar
- **`Configuration.Provider`**: permite a WorkManager usar `HiltWorkerFactory` para inyectar dependencias en los Workers

---

### 0.9 Scripts de Utilidad

| Archivo | Propósito |
|---------|-----------|
| `scripts/setup_database.sql` | Crea usuario y BD en PostgreSQL |
| `scripts/start_backend.sh` | Arranca el servidor Spring Boot |

---

### 0.10 Primer Commit

```bash
git add .
git commit -m "chore: inicializar proyecto SLIOR

- Estructura monorepo: backend/, mobile-app/, docs/, scripts/
- .gitignore completo para Java/Maven + Android/Gradle + IDEs
- README.md con descripción, tecnologías y setup
- pom.xml Spring Boot 3.2.4 con dependencias de la fase 0
- build.gradle.kts Android con Hilt, Room, Retrofit, OSMDroid, ZXing
- AndroidManifest.xml con permisos declarados
- SliorBackendApplication.java (clase main)
- SliorApplication.kt (Application class con Hilt + WorkManager)
- Recursos base: strings.xml, colors.xml, themes.xml
- Scripts: setup_database.sql, start_backend.sh"
```

---

### 0.11 Resumen de Archivos Creados en Fase 0

| Archivo | Descripción |
|---------|-------------|
| `.gitignore` | Exclusiones Git |
| `LICENSE` | Licencia MIT |
| `README.md` | Documentación principal |
| `backend/pom.xml` | Dependencias Maven |
| `backend/src/main/resources/application.properties` | Configuración Spring Boot |
| `backend/src/main/java/com/slior/SliorBackendApplication.java` | Main class |
| `mobile-app/settings.gradle.kts` | Módulos del proyecto Android |
| `mobile-app/build.gradle.kts` | Plugins raíz |
| `mobile-app/app/build.gradle.kts` | Dependencias Android |
| `mobile-app/app/src/main/AndroidManifest.xml` | Manifest con permisos |
| `mobile-app/app/src/main/java/com/slior/SliorApplication.kt` | Application class |
| `mobile-app/app/src/main/res/values/strings.xml` | Textos de la UI |
| `mobile-app/app/src/main/res/values/colors.xml` | Paleta de colores |
| `mobile-app/app/src/main/res/values/themes.xml` | Tema Material Design |
| `scripts/setup_database.sql` | Script PostgreSQL |
| `scripts/start_backend.sh` | Script arranque backend |
| `docs/diario_desarrollo.md` | Este archivo |

---

### 0.12 Instalación de Apache Maven

Maven no estaba disponible en el sistema. Se instaló manualmente:

```powershell
# Descarga desde el archivo oficial de Apache
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
Invoke-WebRequest -Uri $mavenUrl -OutFile "apache-maven-3.9.6-bin.zip"
Expand-Archive -Path "apache-maven-3.9.6-bin.zip" -DestinationPath "C:\Users\User\AppData\Local\Programs"
# Añadir al PATH
$env:PATH = "C:\Users\User\AppData\Local\Programs\apache-maven-3.9.6\bin;$env:PATH"
```

**Versión instalada:** Apache Maven 3.9.6  
**JDK usado:** Eclipse Adoptium JDK 21.0.8 (Java 21 es compatible con proyectos Java 17)

> **Nota para el TFG:** Si Maven no está instalado en el entorno de evaluación, ejecutar el comando anterior antes de compilar el proyecto.

---

### 0.13 Verificación de Compilación Backend

```bash
cd backend
mvn clean compile
```

**Resultado:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 18.413 s
```

 El backend Spring Boot compila correctamente en la Fase 0 (solo la clase main, sin dependencias de negocio aún).

---

## FASE 1: Autenticación y Fundamentos — Backend

**Fecha:** 09/03/2026  
**Duración real:** ~15 minutos (guiado por IA)  
**Estado:**  Completada (backend)

### Archivos creados (16 en total)

| Archivo | Paquete | Descripción |
|---------|---------|-------------|
| `UserRole.java` | `model/enums` | Enum REPARTIDOR / ADMINISTRADOR |
| `User.java` | `model` | Entidad JPA con UUID, BCrypt, borrado lógico, auditoría |
| `UserRepository.java` | `repository` | JPA Repository con findByEmail, existsByEmail |
| `LoginRequest.java` | `dto/auth` | Record con validaciones Jakarta |
| `RegisterRequest.java` | `dto/auth` | Record con validaciones Jakarta |
| `AuthResponse.java` | `dto/auth` | Record de respuesta (sin password) |
| `JwtUtil.java` | `security` | Generar/validar/extraer claims JWT (jjwt 0.12.x) |
| `CustomUserDetailsService.java` | `security` | Carga usuario de BD para Spring Security |
| `JwtAuthenticationFilter.java` | `security` | Filtro HTTP que valida JWT por petición |
| `SecurityConfig.java` | `config` | /auth/** público, resto requiere JWT. Stateless. |
| `AuthService.java` | `service` | register (BCrypt) + login |
| `AuthController.java` | `controller` | POST /auth/register (201), POST /auth/login (200) |
| `EmailAlreadyExistsException.java` | `exception` | 400 Bad Request |
| `InvalidCredentialsException.java` | `exception` | 401 Unauthorized |
| `GlobalExceptionHandler.java` | `exception` | JSON estructurado para todos los errores |
| `SliorBackendApplication.java` | raíz | Añadido @EnableJpaAuditing |

### Decisiones técnicas

**¿Por qué Java Records para los DTOs?**  
Los Records (Java 16+) son clases inmutables con constructor, getters, equals, hashCode y toString generados automáticamente. Son ideales para DTOs porque no pueden mutar después de crearse, lo que previene bugs sutiles.

**¿Por qué BCrypt para passwords?**  
BCrypt incluye un "salt" aleatorio en cada hash, lo que hace imposible los ataques de rainbow table. El factor de coste es configurable (por defecto 10 rondas). Es el estándar recomendado por Spring Security.

**¿Por qué SessionCreationPolicy.STATELESS?**  
Al ser una API REST consumida por una app móvil, no tiene sentido mantener sesiones en servidor. Cada petición lleva el JWT en el header y el servidor no guarda estado. Esto facilita el escalado horizontal.

**¿Por qué @ControllerAdvice en GlobalExceptionHandler?**  
Centraliza el manejo de errores en un único lugar. Sin esto, cada controlador tendría que capturar sus propias excepciones, generando duplicación de código.

### Verificación de compilación

```bash
mvn clean compile
# BUILD SUCCESS — 16 archivos compilados en 3.884 s
```

> **Warning inofensivo:** `User.java uses or overrides a deprecated API`  
> Causado por `@Where` de Hibernate (borrado lógico). Funciona correctamente,  
> será migrado a `@SoftDelete` en versiones futuras de Hibernate 6.x.

### Commit

```
feat(backend): implementar modelo de usuario y autenticacion JWT
Hash: 3b9f887
```

---

*Próxima fase: [FASE 2 — Gestión de Rutas Backend](../README.md#fases-de-desarrollo)*

---

## FASE 1: Autenticación y Fundamentos — Android

**Fecha:** 09/03/2026
**Estado:**  En progreso

---

### 1.A Migración a Jetpack Compose

Antes de crear los archivos de UI, revisé mi proyecto anterior (**FotApp**, disponible en mi GitHub, rama `feature/GUI`) para alinear el estilo de desarrollo Android con lo que ya conocía.

**Hallazgo principal:** FotApp está construida íntegramente con Jetpack Compose y Material 3, no con XML. Decidí adoptar el mismo enfoque en SLIOR, sustituyendo los layouts XML por funciones `@Composable`.

**Cambios en `build.gradle.kts` (raíz):**
- Añadido plugin: `id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false`
  - Este plugin es necesario desde Kotlin 2.0 para habilitar el compilador de Compose (ya no se necesita `kotlinCompilerExtensionVersion` separado)

**Cambios en `app/build.gradle.kts`:**
- Añadido plugin: `id("org.jetbrains.kotlin.plugin.compose")`
- `buildFeatures { viewBinding = true }` → `buildFeatures { compose = true }`
- Eliminadas: `constraintlayout`, `fragment-ktx`, `livedata-ktx`, `activity-ktx` (no necesarias en Compose)
- Añadidas:
  - `androidx.activity:activity-compose:1.9.2` — integración Activity/Compose
  - `platform("androidx.compose:compose-bom:2024.09.03")` — gestión centralizada de versiones Compose
  - `androidx.compose.ui:ui` — núcleo de Compose
  - `androidx.compose.material3:material3` — componentes Material 3
  - `androidx.compose.material:material-icons-extended` — iconos adicionales
  - `androidx.navigation:navigation-compose:2.8.2` — navegación entre pantallas
  - `androidx.hilt:hilt-navigation-compose:1.2.0` — integración Hilt + Navigation
  - `androidx.lifecycle:lifecycle-runtime-compose:2.8.6` — `collectAsStateWithLifecycle`

**¿Por qué Compose BOM 2024.09.03?**  
Es la versión estable más reciente compatible con Kotlin 2.0.21 y AGP 8.9.0 en el momento del inicio del proyecto.

---

### 1.B Archivos Android creados (Fase 1)

#### Capa de datos local (Room)

| Archivo | Paquete | Descripción |
|---------|---------|-------------|
| `UserEntity.kt` | `data/local/entity` | Entidad Room: usuario con UUID, syncStatus, timestamps |
| `UserDao.kt` | `data/local/dao` | Consultas Room: insert, getUserById (Flow), deleteAll |
| `AppDatabase.kt` | `data/local` | Clase @Database: une entidades y DAOs, singleton con `getInstance` |
| `DatabaseModule.kt` | `di` | Módulo Hilt que provee AppDatabase y UserDao |

**Decisión: `syncStatus` en las entidades Room**  
Cada entidad tiene un campo `syncStatus` (PENDING / SYNCED) que indica si el dato ha sido enviado al servidor. WorkManager lee los registros PENDING y los sincroniza cuando hay red.

#### Capa de datos remota (Retrofit)

| Archivo | Paquete | Descripción |
|---------|---------|-------------|
| `LoginRequest.kt` | `data/remote/dto` | DTO de petición login |
| `RegisterRequest.kt` | `data/remote/dto` | DTO de petición registro |
| `AuthResponse.kt` | `data/remote/dto` | DTO de respuesta (token JWT + datos usuario) |
| `ApiService.kt` | `data/remote` | Interfaz Retrofit con endpoints `/auth/login` y `/auth/register` |
| `AuthInterceptor.kt` | `data/remote` | OkHttp Interceptor: añade `Authorization: Bearer {token}` a cada petición. Define `Context.dataStore` y `TOKEN_KEY` |

**Decisión: `AuthInterceptor` define `Context.dataStore`**  
La extension property `Context.dataStore` debe declararse una sola vez en todo el proyecto. Se colocó en `AuthInterceptor.kt` porque es la primera clase que necesita acceder al DataStore. El resto de clases la importan desde aquí.

#### Módulos Hilt

| Archivo | Paquete | Descripción |
|---------|---------|-------------|
| `NetworkModule.kt` | `di` | Provee OkHttpClient (con AuthInterceptor + logging), Retrofit, ApiService |
| `DatabaseModule.kt` | `di` | Provee AppDatabase y UserDao |

**URL base del emulador Android:**  
`http://10.0.2.2:8080/` — La IP `10.0.2.2` es la redirección especial del emulador Android para acceder al `localhost` del PC de desarrollo.

#### Utilidades y lógica de dominio

| Archivo | Paquete | Descripción |
|---------|---------|-------------|
| `Result.kt` | `util` | Sealed class: `Success<T>`, `Error(Exception)`, `Loading` |
| `AuthRepository.kt` | `data/repository` | login/register → llama a Retrofit, guarda en Room y DataStore |
| `LoginState.kt` | `ui/auth` | Sealed interface: `Idle`, `Loading`, `Success`, `Error(String)` |
| `AuthViewModel.kt` | `viewmodel` | @HiltViewModel. Expone `StateFlow<LoginState>`. Métodos: login, register, resetState |

**Decisión: `Result<T>` vs `LoginState`**  
Se usan dos tipos diferentes con propósitos distintos:
- `Result<T>`: lo usa el repositorio para indicar éxito/error de una operación de datos (capa de datos)
- `LoginState`: lo usa el ViewModel para comunicar el estado actual de la pantalla a la UI (capa de presentación)

Esta separación evita que la UI tenga que entender detalles de la capa de datos.

---

### 1.C Análisis FotApp → SLIOR

Comparativa entre la arquitectura de mi proyecto anterior FotApp y las mejoras introducidas en SLIOR:

| Aspecto | FotApp | SLIOR (mejora) |
|---------|--------|----------------|
| UI | Compose  | Compose  (mantenido) |
| Estado | `mutableStateOf` en Composables | `StateFlow` en ViewModel |
| Datos | Hardcodeados en `Datasource` | Room + Retrofit |
| DI | Sin DI | Hilt |
| Backend | Sin backend | API REST JWT |
| Offline | Sin soporte | Room + WorkManager |
| Errores | Sin gestión | `sealed class Result<T>` |

---

## FASE 2: Gestión de Rutas — Backend

**Fecha:** 02/03/2026 – 03/03/2026
**Estado:**  Completada (CRUD básico)
**Rama:** `feature/fase-2-gestion-rutas`

---

### 2.1 Motivación y objetivo

Con la autenticación completada en Fase 1, el siguiente bloque es el núcleo del sistema: la gestión de rutas de reparto. Un administrador debe poder crear rutas, asignarlas a repartidores y definir las paradas en orden. El repartidor puede consultar sus rutas.

---

### 2.2 Modelo de datos

Se crearon dos entidades JPA nuevas y sus enumeraciones asociadas:

#### Enumeraciones

| Enum | Valores |
|------|---------|
| `RouteStatus` | `PLANIFICADA`, `EN_CURSO`, `COMPLETADA`, `CANCELADA` |
| `StopStatus` | `PENDIENTE`, `EN_CAMINO`, `ENTREGADO`, `FALLIDO`, `REPROGRAMADO` |

#### Entidad `Route`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | `UUID` | PK generada automáticamente |
| `nombre` | `String` | Nombre descriptivo de la ruta |
| `fechaPlanificada` | `LocalDate` | Fecha para la que está planificada |
| `status` | `RouteStatus` | Estado actual de la ruta |
| `repartidor` | `User` (ManyToOne) | Repartidor asignado |
| `stops` | `List<Stop>` (OneToMany) | Paradas ordenadas por `ordenVisita` |
| `distanciaTotal` | `Double` | Distancia total calculada (km) |
| `notas` | `String` | Notas adicionales |
| `isDeleted` | `boolean` | Borrado lógico |

#### Entidad `Stop`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | `UUID` | PK generada automáticamente |
| `route` | `Route` (ManyToOne) | Ruta a la que pertenece |
| `direccion` | `String` | Dirección de entrega |
| `destinatario` | `String` | Nombre del destinatario |
| `telefonoDestinatario` | `String` | Teléfono de contacto |
| `latitud` / `longitud` | `Double` | Coordenadas GPS |
| `ordenVisita` | `Integer` | Posición en la ruta (1, 2, 3…) |
| `status` | `StopStatus` | Estado de la parada |
| `notas` | `String` | Notas de la parada |
| `entregadoEn` | `LocalDateTime` | Timestamp real de entrega |
| `isDeleted` | `boolean` | Borrado lógico |

**Decisión de diseño:** `@OrderBy("ordenVisita ASC")` en la relación `OneToMany` garantiza que las paradas siempre se devuelven ordenadas sin necesidad de ordenar en memoria.

---

### 2.3 DTOs creados

| DTO | Tipo | Descripción |
|-----|------|-------------|
| `StopRequest` | Record | Datos para crear una parada |
| `CreateRouteRequest` | Record | Datos para crear una ruta (con lista de paradas) |
| `StopResponse` | Record | Respuesta de una parada (con factory `from(Stop)`) |
| `RouteResponse` | Record | Respuesta de una ruta (con factory `from(Route)`) |

Se usaron **Java Records** como en Fase 1, ya que los DTOs son objetos inmutables de transferencia de datos.

---

### 2.4 Repositorios

Se añadieron métodos de consulta por naming convention de Spring Data JPA:

```java
// RouteRepository
List<Route> findByRepartidorIdAndIsDeletedFalse(UUID userId);
Optional<Route> findByIdAndIsDeletedFalse(UUID id);

// StopRepository
List<Stop> findByRouteIdAndIsDeletedFalse(UUID routeId);
```

---

### 2.5 Servicio: `RouteService`

Implementación inicial del servicio. En esta primera versión opté por la solución más directa:
- Sin `@Transactional` (lo añadiré en refactors posteriores)
- `RuntimeException` genérica para errores (crearé excepciones personalizadas más adelante)
- Lógica de mapeo inline (sin mapper dedicado)

Operaciones implementadas:
- `createRoute(request)` — crea ruta con sus paradas, asigna repartidor por ID
- `getRoutesForRepartidor(repartidorId)` — lista rutas de un repartidor
- `getRouteById(id)` — obtiene ruta por ID
- `deleteRoute(id)` — borrado lógico

---

### 2.6 Controlador: `RouteController`

```
POST   /api/routes                         → createRoute
GET    /api/routes/repartidor/{id}         → getRoutesByRepartidor
GET    /api/routes/{id}                    → getRouteById
DELETE /api/routes/{id}                    → deleteRoute
```

Todos los endpoints requieren JWT válido (protegidos por `.anyRequest().authenticated()` en `SecurityConfig`).

---

### 2.7 Compilación

```
mvn clean compile → BUILD SUCCESS
```

Sin errores. Los nuevos archivos se integran correctamente con el contexto existente.

---

## FASE 3: Optimización de Rutas

**Fecha:** 10/03/2026
**Estado:**  Completada

---

### 3.1 Objetivo

Implementar un algoritmo de optimización de rutas que, dado un punto de inicio (coordenadas GPS), reordene las paradas de una ruta de forma eficiente usando el algoritmo **Nearest Neighbor** (vecino más cercano).

---

### 3.2 Archivos creados

| Archivo | Descripción |
|---------|-------------|
| `util/HaversineUtil.java` | Calcula distancia en km entre dos coordenadas geográficas |
| `exception/RouteNotFoundException.java` | Excepción 404 cuando no existe una ruta |
| `exception/UnauthorizedAccessException.java` | Excepción 403 para accesos no autorizados |
| `dto/route/OptimizeRouteRequest.java` | DTO con `puntoInicioLat` y `puntoInicioLon` |
| `service/RouteOptimizationService.java` | Servicio con el algoritmo Nearest Neighbor |

### 3.3 Archivos modificados

| Archivo | Cambio |
|---------|--------|
| `model/Route.java` | Añadido campo `tiempoEstimado` (Integer, minutos) |
| `dto/route/RouteResponse.java` | Añadido `tiempoEstimado` al record y al `from()` |
| `controller/RouteController.java` | Nuevo endpoint `POST /api/routes/{id}/optimize` |
| `exception/GlobalExceptionHandler.java` | Handlers para 404 (RouteNotFound) y 403 (UnauthorizedAccess) |
| `service/RouteService.java` | Reemplazado `RuntimeException` por `RouteNotFoundException` |

---

### 3.4 Algoritmo Nearest Neighbor

El algoritmo funciona de la siguiente manera:

1. Se parte del punto de inicio (lat/lon recibidos en el request)
2. De todas las paradas pendientes, se elige la más cercana usando Haversine
3. Se añade esa parada al resultado y se elimina de pendientes
4. Se repite desde la parada recién añadida hasta visitar todas
5. Se actualiza `ordenVisita` (1, 2, 3…) en cada parada
6. Se calcula `distanciaTotal` (km, redondeado a 2 decimales)
7. Se calcula `tiempoEstimado` (minutos) asumiendo velocidad media urbana de **30 km/h**

---

### 3.5 Fórmula Haversine

```
a = sin²(Δlat/2) + cos(lat1) · cos(lat2) · sin²(Δlon/2)
c = 2 · atan2(√a, √(1−a))
d = R · c       (R = 6371 km)
```

---

### 3.6 Endpoint

```
POST /api/routes/{id}/optimize
Authorization: Bearer <token>
Content-Type: application/json

{
  "puntoInicioLat": 40.4168,
  "puntoInicioLon": -3.7038
}
```

Respuesta: `RouteResponse` con paradas reordenadas, `distanciaTotal` y `tiempoEstimado` actualizados.

---

### 3.7 Migración de BD

Hibernate ejecutó automáticamente al arrancar:
```sql
ALTER TABLE routes ADD COLUMN tiempo_estimado INTEGER;
```

---

### 3.8 Verificación

```
Spring Boot arrancó sin errores en 4.6 segundos.
Columna tiempo_estimado creada automáticamente en PostgreSQL.
Commit: feat(optimization): implementar algoritmo Nearest Neighbor para optimizacion de rutas
```
