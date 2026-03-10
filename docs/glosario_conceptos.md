# Glosario de Conceptos — SLIOR

> Documento de referencia personal. Explica los conceptos técnicos del proyecto
> desde los más básicos hasta los más avanzados, con ejemplos del propio código.

---

## NIVEL 1 — Conceptos Fundamentales

### API (Application Programming Interface)
Un contrato que define cómo dos programas se comunican. En SLIOR, el backend
expone una API REST que la app Android consume. Es como un menú de restaurante:
define qué puedes pedir y en qué formato te lo devuelven.

### HTTP y sus métodos
Protocolo de comunicación web. Los métodos más usados en SLIOR:
| Método | Uso en SLIOR | Ejemplo |
|--------|-------------|---------|
| `GET` | Obtener datos | Listar rutas de un repartidor |
| `POST` | Crear datos | Crear una ruta nueva |
| `DELETE` | Eliminar datos | Borrar una ruta |

### JSON (JavaScript Object Notation)
Formato de texto para intercambiar datos entre el backend y Android:
```json
{
  "nombre": "Ruta Madrid Norte",
  "status": "PLANIFICADA",
  "repartidorId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Endpoint
Una URL concreta de la API que realiza una acción específica.
Ejemplos en SLIOR:
- `POST /auth/login` — iniciar sesión
- `GET /api/routes/repartidor/{id}` — obtener rutas de un repartidor
- `DELETE /api/routes/{id}` — eliminar una ruta

### Request / Response
- **Request**: lo que el cliente (Android) envía al servidor.
- **Response**: lo que el servidor devuelve.

---

## NIVEL 2 — Tecnologías del Proyecto

### Java vs Kotlin
- **Java**: lenguaje en el que está escrito el backend (Spring Boot). Verboso pero
  muy estable y con un ecosistema enorme.
- **Kotlin**: lenguaje en el que está escrita la app Android. Más moderno, conciso
  y con nul-safety integrada. Corre en la misma JVM que Java.

### Spring Boot (backend)
Framework Java que facilita crear APIs REST. Gestiona automáticamente:
- Arranque del servidor (Tomcat embebido)
- Inyección de dependencias
- Mapeo de URLs a métodos Java (`@RestController`, `@GetMapping`...)

### Android + Jetpack
Android es el sistema operativo móvil. Jetpack es el conjunto de librerías
oficiales de Google para Android moderno:
- **Room** — base de datos local SQLite
- **Hilt** — inyección de dependencias
- **ViewModel** — gestión del ciclo de vida de la UI
- **Navigation Compose** — navegación entre pantallas
- **WorkManager** — tareas en segundo plano

### Gradle / Maven
Herramientas de construcción del proyecto:
- **Maven** (`pom.xml`) — gestiona el backend Java: descarga librerías,
  compila, genera el `.jar`.
- **Gradle** (`build.gradle.kts`) — gestiona la app Android: descarga
  librerías, compila, genera el `.apk`.

### Git / GitHub
- **Git**: control de versiones local. Guarda el historial de cambios.
- **GitHub**: plataforma remota donde se aloja el repositorio. Permite
  ver el historial y colaborar.

---

## NIVEL 3 — Arquitectura del Backend

### REST (Representational State Transfer)
Estilo de diseño de APIs. Principios clave:
- Sin estado (cada petición es independiente)
- Recursos identificados por URLs (`/routes`, `/users`)
- Operaciones con métodos HTTP estándar

### JWT (JSON Web Token)
Sistema de autenticación sin sesiones. Flujo en SLIOR:
1. El usuario hace login → el backend genera un token firmado
2. Android guarda ese token en DataStore
3. Cada petición incluye `Authorization: Bearer <token>`
4. El backend valida la firma y extrae el usuario

El token tiene fecha de expiración (30 minutos en SLIOR).

### JPA / Hibernate (backend)
- **JPA**: especificación de Java para mapear clases Java a tablas SQL.
- **Hibernate**: la implementación que usa Spring Boot por defecto.
- Con `@Entity` y anotaciones, una clase Java se convierte en tabla
  sin escribir SQL manual.

### DTO (Data Transfer Object)
Objeto que solo transporta datos, sin lógica de negocio. En SLIOR se usan
**Java Records** para los DTOs del backend (inmutables, sin boilerplate):
```java
public record CreateRouteRequest(String nombre, LocalDate fechaPlanificada, ...) {}
```

### Borrado lógico (soft delete)
En lugar de `DELETE FROM routes WHERE id = ?`, SLIOR marca el registro
como eliminado: `isDeleted = true`. El dato sigue en la BD pero no aparece
en las consultas. Ventaja: se puede recuperar y hay historial de auditoría.

---

## NIVEL 4 — Arquitectura Android

### MVVM (Model - View - ViewModel)
Patrón de arquitectura usado en la app:
- **Model**: datos (Room + Retrofit + Repository)
- **View**: pantallas Compose que solo muestran lo que el ViewModel dice
- **ViewModel**: intermediario; obtiene datos y expone estado a la UI

```
Room/Retrofit → Repository → ViewModel → Compose UI
```

### Room (base de datos local)
Wrapper de SQLite para Android. En SLIOR:
- `@Entity` → tabla en SQLite (`RouteEntity`, `StopEntity`, `UserEntity`)
- `@Dao` → interfaz con consultas (`RouteDao`, `UserDao`)
- `@Database` → punto de entrada a toda la BD (`AppDatabase`)

### Hilt (inyección de dependencias)
En lugar de crear objetos manualmente (`new RouteRepository(...)`), Hilt
los crea y los inyecta automáticamente:
```kotlin
class RouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository  // Hilt lo inyecta solo
)
```

### Retrofit
Librería que convierte la interfaz `ApiService` en llamadas HTTP reales.
Define el contrato; Retrofit genera el código de red:
```kotlin
interface ApiService {
    @GET("api/routes/repartidor/{id}")
    suspend fun getRoutesByRepartidor(@Path("id") id: String): List<RouteResponseDto>
}
```

### Coroutines + Flow
- **Coroutines**: forma de escribir código asíncrono en Kotlin sin callbacks.
  `suspend fun` es una función que puede pausarse sin bloquear el hilo.
- **Flow**: stream de datos que emite valores a lo largo del tiempo.
  Room devuelve `Flow<List<RouteEntity>>`: cada vez que cambia la BD,
  la UI se actualiza automáticamente.

### StateFlow
`Flow` especial para estado de UI. El ViewModel expone un `StateFlow`
que la pantalla Compose observa:
```kotlin
// ViewModel
private val _state = MutableStateFlow<RouteListState>(RouteListState.Loading)
val state: StateFlow<RouteListState> = _state.asStateFlow()

// Compose
val state by viewModel.state.collectAsStateWithLifecycle()
```

### Sealed class
Clase con subtipos conocidos en tiempo de compilación. Ideal para
representar estados mutuamente excluyentes:
```kotlin
sealed class RouteListState {
    object Loading : RouteListState()
    data class Success(val routes: List<RouteEntity>) : RouteListState()
    data class Error(val message: String) : RouteListState()
}
```
El compilador obliga a gestionar todos los casos en un `when`.

### DataStore
Almacenamiento clave-valor asíncrono de Android (reemplaza SharedPreferences).
En SLIOR guarda el token JWT localmente de forma segura.

---

## NIVEL 5 — Conceptos Avanzados

### Offline-first
Estrategia de diseño: la app funciona aunque no haya conexión.
Flujo en SLIOR:
1. La UI lee siempre de Room (local, instantáneo)
2. Al iniciar, el ViewModel lanza `syncRoutes()` en segundo plano
3. Si hay red, descarga datos del servidor y actualiza Room
4. Room notifica a la UI via Flow (actualización automática)

### WorkManager
Librería para tareas en segundo plano garantizadas. En SLIOR está
planificado para sincronización periódica: aunque el usuario cierre
la app, WorkManager puede sincronizar datos cuando haya red.

### UUID (Universally Unique Identifier)
Identificador único de 128 bits. Formato: `550e8400-e29b-41d4-a716-446655440000`.
SLIOR usa UUIDs como claves primarias en vez de IDs numéricos (1, 2, 3...)
porque se pueden generar en el móvil sin consultar al servidor,
lo que es esencial para el enfoque offline-first.

### KSP vs KAPT
Herramientas que procesan anotaciones en tiempo de compilación
(generan código para Room, Hilt, etc.):
- **KAPT**: antiguo procesador para Java/Kotlin. Incompatible con Gradle 8.3+.
- **KSP** (Kotlin Symbol Processing): moderno, más rápido. SLIOR migró a KSP
  para resolver el error de `Configuration.fileCollection(Spec)`.

### Monorepo
Estructura de repositorio que aloja múltiples proyectos en uno solo.
SLIOR tiene backend (Java) y mobile-app (Kotlin) en el mismo repositorio
Git, facilitando la coherencia de versiones y la documentación conjunta.

### CI/CD (Continuous Integration / Continuous Deployment)
Automatización del ciclo de desarrollo. Planificado para SLIOR
tras completar las fases principales:
- **CI**: ejecutar tests automáticamente en cada push
- **CD**: desplegar el backend automáticamente si los tests pasan

---

*Este documento se actualiza a lo largo del desarrollo.*
