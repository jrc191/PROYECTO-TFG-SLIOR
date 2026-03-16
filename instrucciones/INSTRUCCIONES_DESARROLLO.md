# 📋 INSTRUCCIONES DE DESARROLLO - SLIOR PROJECT

## 🎯 CONTEXTO GENERAL

Este proyecto simula el desarrollo de un alumno de **2º CFGS DAM (Ciclo Formativo Grado Superior - Desarrollo de Aplicaciones Multiplataforma)**.

### Principios Fundamentales:
- ✅ Programación realista a nivel de alumno de 2º CFGS DAM
- ✅ Incluir "errores" y mejoras que se corregirán en commits posteriores
- ✅ Desarrollo coherente en el tiempo (no hacer todo en un día)
- ✅ Sin rastro de uso de IA (sin coautores, sin menciones de IA)
- ✅ Separación clara de responsabilidades (Clean Architecture)
- ✅ Métodos/funciones no excesivamente complejos

---

## 📱 STACK TECNOLÓGICO

### Backend
- **Lenguaje:** Java
- **Framework:** Spring Boot
- **Base de Datos:** PostgreSQL
- **Build:** Maven
- **Arquitectura:** Clean Architecture (Entities, Use Cases, Controllers, Repositories)

### Mobile (Android)
- **Lenguaje:** Kotlin
- **Framework:** Jetpack Compose (OBLIGATORIO - siempre usar para UI)
- **Estructura:** MVVM + Clean Architecture
- **Patrón UI:** Screens (cada pantalla = composable separado)
- **Build:** Gradle

---

## 🏗️ ARQUITECTURA - CLEAN ARCHITECTURE

### Backend (Java/Spring Boot)

```
src/main/java/com/slior/
├── domain/              (Entidades y lógica de negocio pura)
│   ├── entities/        (Modelos de dominio)
│   └── usecases/        (Casos de uso)
├── application/         (Lógica de aplicación)
│   ├── dto/            (Data Transfer Objects)
│   └── services/       (Servicios de aplicación)
├── infrastructure/      (Implementación - BD, APIs externas)
│   ├── repository/     (Implementación de repositorios)
│   ├── controller/     (REST Controllers)
│   └── config/         (Configuración)
└── presentation/        (REST endpoints)
    └── controller/     (Controllers REST)
```

### Mobile (Kotlin/Compose)

```
app/src/main/kotlin/com/slior/
├── presentation/        (UI - Screens y Composables)
│   ├── screens/        (Cada pantalla = archivo separado)
│   ├── viewmodels/     (ViewModels MVVM)
│   └── components/     (Componentes reutilizables)
├── domain/             (Lógica de negocio - Use Cases)
│   ├── usecases/
│   └── entities/
├── data/               (Acceso a datos)
│   ├── repository/
│   ├── datasource/
│   ├── local/         (Base de datos local)
│   └── remote/        (APIs)
└── di/                 (Dependency Injection - Hilt)
    └── modules/
```

---

## 💻 CONVENCIONES DE CÓDIGO

### Java (Backend)

#### Nomenclatura
- **Clases:** PascalCase (ej: `UserController`, `CreateUserUseCase`)
- **Métodos:** camelCase (ej: `getUserById()`, `saveUser()`)
- **Variables:** camelCase (ej: `userId`, `userName`)
- **Constantes:** UPPER_SNAKE_CASE (ej: `MAX_USERS = 1000`)
- **Packages:** lowercase.con.puntos (ej: `com.slior.domain.entities`)

#### Estructura de Métodos
```java
// Métodos simples y claros, sin lógica compleja
public User getUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
}

// Si la lógica es compleja, dividir en métodos privados pequeños
private void validateUserData(UserDTO dto) {
    if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
        throw new InvalidUserDataException("Email required");
    }
}
```

#### Excepciones
- Crear excepciones personalizadas para cada caso
- No usar excepciones genéricas
```java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User not found: " + id);
    }
}
```

#### Logging
- Usar SLF4J + Logback (incluido en Spring Boot)
- Niveles: DEBUG (desarrollo), INFO (información importante), WARN (advertencias), ERROR (errores)
```java
private static final Logger log = LoggerFactory.getLogger(UserService.class);

public void deleteUser(Long id) {
    log.info("Deleting user with id: {}", id);
    userRepository.deleteById(id);
    log.info("User deleted successfully");
}
```

---

### Kotlin (Mobile)

#### Nomenclatura
- **Clases/Data Classes:** PascalCase (ej: `User`, `UserViewModel`)
- **Funciones:** camelCase (ej: `getUserData()`, `navigateToHome()`)
- **Variables:** camelCase (ej: `userId`, `isLoading`)
- **Constantes:** UPPER_SNAKE_CASE (ej: `MAX_RETRY_ATTEMPTS = 3`)
- **Composables (Screens):** PascalCase (ej: `HomeScreen()`, `UserDetailsScreen()`)

#### Estructura de Screens (Obligatorio Jetpack Compose)

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    when (state) {
        is HomeState.Loading -> LoadingScreen()
        is HomeState.Success -> SuccessContent((state as HomeState.Success).data)
        is HomeState.Error -> ErrorContent((state as HomeState.Error).message)
    }
}

@Composable
private fun SuccessContent(data: HomeData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Contenido
    }
}
```

#### ViewModels
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDataUseCase: GetDataUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                val data = getDataUseCase()
                _state.value = HomeState.Success(data)
            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val data: HomeData) : HomeState()
    data class Error(val message: String) : HomeState()
}
```

#### Data Classes
```kotlin
data class User(
    val id: Long,
    val name: String,
    val email: String
)

// Con valores por defecto cuando tiene sentido
data class UserFilter(
    val status: String = "active",
    val limit: Int = 10
)
```

---

## 📅 ESTRATEGIA DE COMMITS

### Fechas Coherentes
- **No hacer más de 1-2 funcionalidades por día** de desarrollo
- **Distribuir los commits a lo largo de varias semanas/meses** de calendario
- **Simular pauses realistas:** fines de semana, días sin commits
- Ejemplo:
  - 15/03/2026: Primer commit (setup inicial)
  - 16/03/2026: Segundo commit (entidades base)
  - 20/03/2026: Tercer commit (repositorios) - pausa de 4 días
  - 22/03/2026: Cuarto commit (servicios)

### Formato de Commits
```
<tipo>: <descripción corta>

<descripción larga opcional>

Cambios:
- Cambio 1
- Cambio 2
```

**Tipos válidos:**
- `feat`: Nueva funcionalidad
- `fix`: Corrección de bug
- `refactor`: Refactorización de código
- `docs`: Cambios en documentación
- `style`: Cambios de formato (sin lógica)
- `test`: Añadir o modificar tests

**Ejemplo:**
```
feat: Add user authentication

- Implement login endpoint
- Add JWT token generation
- Create user authentication middleware
```

### ⚠️ REGLA CRÍTICA DE FECHAS DE PUSH

**FECHA ACTUAL: 16/03/2026**

- Si un commit tiene fecha **16/03/2026 o anterior** → ✅ Hacer push
- Si un commit tiene fecha **17/03/2026 o posterior** → ❌ NO hacer push hasta esa fecha

Ejemplo:
```bash
# Crear commit con fecha antigua (OK, se puede hacer push)
git commit --date="2026-03-15 10:00:00" -m "feat: initial setup"

# Crear commit con fecha futura (NO push hasta esa fecha)
git commit --date="2026-03-20 14:30:00" -m "feat: add user service"
# Esperar hasta 20/03/2026 para hacer push
```

---

## 📚 DOCUMENTACIÓN

### Ubicación: `/docs`

#### Archivos a mantener coherentes:

**1. `docs/ARQUITECTURA.md`**
- Diagrama general del proyecto
- Explicación de capas
- Flujos de datos principales
- Actualizar con cambios mayores

**2. `docs/API.md`**
- Endpoints disponibles
- Modelos de request/response
- Ejemplos de uso
- Mantener actualizado con nuevos endpoints

**3. `docs/MOBILE.md`**
- Estructura de screens
- Navegación
- Estados principales
- Use cases del móvil

**4. `docs/SETUP.md`**
- Instrucciones para clonar y levantar el proyecto
- Requisitos del sistema
- Variables de entorno
- Comandos de inicio

**5. `docs/CHANGELOG.md`**
- Registro de cambios principales
- Versiones
- Fechas coherentes con commits

#### Convenciones de Documentación:
- Usar Markdown
- Mantener fechas coherentes con commits
- No hacer cambios en documentos "futuros" antes de su fecha
- Añadir ejemplos claros
- Diagramas ASCII o referencias a imágenes

---

## 🔍 PATRONES A SEGUIR

### Backend - Use Cases

```java
// UseCase simple y claro
public interface GetUserByIdUseCase {
    User execute(Long userId);
}

@Component
public class GetUserByIdUseCaseImpl implements GetUserByIdUseCase {
    private final UserRepository repository;
    
    public GetUserByIdUseCaseImpl(UserRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public User execute(Long userId) {
        return repository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
```

### Mobile - Repository Pattern

```kotlin
interface UserRepository {
    suspend fun getUser(id: Long): Result<User>
    suspend fun saveUser(user: User): Result<Unit>
}

class UserRepositoryImpl(
    private val localDataSource: UserLocalDataSource,
    private val remoteDataSource: UserRemoteDataSource
) : UserRepository {
    
    override suspend fun getUser(id: Long): Result<User> = try {
        val user = remoteDataSource.getUser(id)
        localDataSource.saveUser(user)
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## ⚙️ HERRAMIENTAS Y CONFIGURACIÓN

### Backend (Java/Maven)
- Maven Compiler Plugin: Java 17+
- Spring Boot 3.x
- JUnit 5 para tests
- Mockito para mocks

### Mobile (Kotlin/Gradle)
- Kotlin 1.9+
- Jetpack Compose BOM
- Hilt para DI
- Coroutines para async
- Room para BD local

---

## 🚫 LO QUE NO DEBES HACER

- ❌ No añadir Co-authored-by en commits
- ❌ No mencionar "IA" o "Claude" en commits o comentarios
- ❌ No crear métodos/funciones con 100+ líneas
- ❌ No mezclar arquitectura (limpia vs monolítica)
- ❌ No usar UI XML en Android (solo Compose)
- ❌ No hacer commits enormes en un solo día
- ❌ No documentar features que no existen aún
- ❌ No hacer push de commits con fecha futura

---

## ✅ CHECKLIST ANTES DE CADA COMMIT

- [ ] Código compila/ejecuta sin errores
- [ ] Funcionalidad nueva está completa (aunque tenga "bugs" didácticos)
- [ ] Tests básicos pasan (si existen)
- [ ] Clean Architecture respetada
- [ ] Métodos no super complejos
- [ ] Documentación actualizada (si aplica)
- [ ] Commit message es descriptivo
- [ ] Fecha del commit es coherente
- [ ] Sin rastro de IA en el código
- [ ] .gitignore no incluye archivos accidentales

---

## 📝 NOTAS FINALES

- Este proyecto es **educativo y realista**
- El código debe verse como escrito por un alumno, **no perfecto**
- Los "bugs" y mejoras son **parte del proceso de aprendizaje**
- Cada commit debe mostrar **progreso incremental**
- La documentación debe evolucionar con el proyecto

