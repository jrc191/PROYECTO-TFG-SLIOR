# Guía de Tecnologías SLIOR — Explicación Personal

> Este documento explica de forma sencilla todas las tecnologías que estamos
> usando en el proyecto. Léelo cuando no recuerdes para qué sirve algo.

---

##  BACKEND

---

### Java 17

**¿Qué es?**  
Java es el lenguaje de programación con el que escribimos el servidor (backend). 
La versión 17 es una versión LTS (Long Term Support), lo que significa que tendrá 
soporte oficial y actualizaciones de seguridad durante muchos años.

**¿Por qué Java 17 y no otra versión?**  
- Es la versión que Spring Boot 3.x exige como mínimo
- Tiene características modernas: Records (para nuestros DTOs), switch expressions, text blocks
- Es estable y ampliamente usada en empresas
- Tenemos instalado Java 21, que es compatible hacia atrás con código Java 17

**¿Qué nos permite?**  
Escribir el servidor de forma robusta, con tipado fuerte (los errores se detectan al compilar, no al ejecutar) y con una enorme comunidad de soporte.

---

### Spring Boot 3.2.4

**¿Qué es?**  
Spring Boot es un framework que hace que crear servidores en Java sea muchísimo más fácil. Sin Spring Boot, tendrías que configurar manualmente el servidor web, la base de datos, la seguridad... Spring Boot lo hace casi todo automático.

**¿Qué hace exactamente?**  
- Arranca un servidor web (Tomcat) embebido: no necesitas instalar ningún servidor aparte
- Conecta automáticamente a la base de datos con solo poner las credenciales en un `.properties`
- Detecta y configura tus clases solo con anotaciones (`@RestController`, `@Service`, etc.)
- Genera un único fichero `.jar` ejecutable que lleva todo dentro

**¿Por qué la versión 3.2.4?**  
- Es la versión estable más reciente de la rama 3.x cuando empezamos el proyecto
- Usa Jakarta EE (la nueva versión del estándar, antes llamada Java EE)
- Tiene soporte nativo para características modernas de Java como Records

**¿Cuánto trabajo nos ahorra?**  
Sin Spring Boot, un proyecto como SLIOR necesitaría semanas solo de configuración inicial. Con Spring Boot, el servidor básico se hace en minutos.

---

### Spring Data JPA + Hibernate

**¿Qué es?**  
JPA (Java Persistence API) es el estándar de Java para trabajar con bases de datos relacionales usando objetos Java en lugar de SQL. 
Hibernate es la implementación más popular de JPA. Spring Data JPA añade una capa encima que hace que crear consultas sea trivial.

**¿Qué hace en el proyecto?**  
- Convierte nuestras clases Java (`User`, `Route`, `Stop`) en tablas de base de datos automáticamente
- Genera el SQL necesario para guardar, buscar, actualizar y borrar datos sin que nosotros lo escribamos
- Con `JpaRepository`, métodos como `findByEmail()` se generan solos a partir del nombre del método

**Ejemplo real:**  
Sin JPA escribirías: `SELECT * FROM users WHERE email = ? AND is_deleted = false`  
Con JPA simplemente declaras: `Optional<User> findByEmail(String email)` y listo.

**¿Por qué `ddl-auto=update`?**  
En desarrollo, Hibernate crea y modifica las tablas automáticamente al arrancar. Si añades un campo a `User.java`, Hibernate añade la columna en la BD sin que hagas nada. En producción esto se desactivaría.

---

### Spring Security + JWT

**¿Qué es Spring Security?**  
Es el módulo de Spring que gestiona quién puede acceder a qué en la aplicación. 
Sin él, cualquiera podría llamar a cualquier endpoint y ver o modificar datos de otros usuarios.

**¿Qué es JWT (JSON Web Token)?**  
Un JWT es un "ticket de acceso" digital. 
Cuando haces login, el servidor te genera un ticket firmado con información sobre quién eres. 
En cada petición posterior mandas ese ticket y el servidor lo verifica sin necesidad de consultar la base de datos.

**¿Cómo funciona en SLIOR?**  
1. El repartidor hace login → el servidor le da un JWT (válido 30 minutos)
2. La app guarda ese JWT
3. En cada petición (crear ruta, ver paquetes...) la app manda el JWT en el header
4. El servidor verifica que el JWT es válido y no ha expirado
5. Si es válido, procesa la petición; si no, responde 401 Unauthorized

**¿Por qué jjwt 0.12.3?**  
Es la librería Java más popular para trabajar con JWT. La versión 0.12.x tiene una API modernizada, más segura y más clara que las versiones anteriores.

**¿Por qué BCrypt para las contraseñas?**  
BCrypt es un algoritmo de hashing pensado específicamente para contraseñas. Cada vez que hashea la misma contraseña, produce un resultado diferente (gracias al "salt" aleatorio). Esto hace imposibles los ataques de diccionario. Aunque alguien robe la base de datos, no puede saber las contraseñas reales.

---

### PostgreSQL 15

**¿Qué es?**  
PostgreSQL es un sistema de gestión de bases de datos relacionales (como MySQL o SQLite, pero más potente). 
Es donde se guardan todos los datos del sistema: usuarios, rutas, paradas, paquetes.

**¿Por qué PostgreSQL y no MySQL o SQLite?**  
- Soporte nativo para UUID como tipo de dato (perfecto para nuestros IDs)
- Mejor manejo de tipos de datos complejos (JSON, arrays)
- Es el estándar en proyectos Spring Boot profesionales
- SQLite es demasiado limitado para un servidor; MySQL es bueno pero PostgreSQL tiene ventajas técnicas

**¿Por qué versión 15+?**  
Es la versión LTS más reciente y estable. Tiene mejoras de rendimiento y nuevas funciones respecto a versiones anteriores.

---

### Maven 3.9.6

**¿Qué es?**  
Maven es la herramienta que gestiona el proyecto Java: descarga las dependencias (librerías), compila el código, ejecuta los tests y empaqueta todo en un JAR ejecutable.

**¿Qué hace el `pom.xml`?**  
Es el fichero de configuración de Maven. En él declaramos qué librerías necesitamos (Spring Boot, JWT, Lombok...) y Maven las descarga automáticamente de internet la primera vez.

**¿Por qué Maven y no Gradle?**  
Para el backend Java, Maven es el estándar más extendido y con más documentación. Gradle (que usamos en Android) es mejor para proyectos Android por su flexibilidad con el ecosistema de Android.

---

### Lombok

**¿Qué es?**  
Lombok es una librería que genera código repetitivo automáticamente mediante anotaciones. 
En Java, una clase con 5 campos normalmente necesita getters, setters, constructor, equals, hashCode y toString. Lombok lo genera todo en tiempo de compilación.

**¿Qué nos ahorra?**  
Una entidad JPA típica sin Lombok tendría ~150 líneas. Con Lombok son ~30 líneas con las mismas funcionalidades.

**Anotaciones que usamos:**
- `@Data` → genera getters, setters, equals, hashCode, toString
- `@Builder` → patrón Builder para crear objetos de forma legible
- `@NoArgsConstructor` / `@AllArgsConstructor` → constructores
- `@RequiredArgsConstructor` → constructor con los campos `final` (para inyección de dependencias)

---

##  ANDROID

---

### Kotlin 2.0.21

**¿Qué es?**  
Kotlin es el lenguaje de programación oficial de Android (desde 2017). 
Es como Java pero más moderno, más conciso y con menos errores posibles (especialmente los temidos NullPointerException).

**¿Por qué Kotlin y no Java para Android?**  
- Google recomienda oficialmente Kotlin para Android
- Tiene null safety incorporado (evita crashes por NullPointerException)
- Las Coroutines de Kotlin hacen el código asíncrono muy legible
- Las data classes, extension functions y lambdas reducen el código a la mitad

**¿Por qué la versión 2.0.21?**  
Es la versión estable más reciente de Kotlin. Incluye el nuevo compilador K2, significativamente más rápido que el anterior.

---

### Jetpack Compose

**¿Qué es?**  
Jetpack Compose es la forma moderna de construir interfaces de usuario en Android. En lugar de diseñar pantallas con archivos XML (la forma "clásica"), con Compose escribes la UI directamente en Kotlin con funciones especiales llamadas `@Composable`.

**¿Cómo funciona?**  
Describes *qué* quieres mostrar, no *cómo* dibujarlo. Compose se encarga del resto:
```kotlin
@Composable
fun LoginScreen() {
    Column {
        Text("SLIOR")
        TextField(value = email, onValueChange = { email = it })
        Button(onClick = { viewModel.login(email, password) }) {
            Text("Entrar")
        }
    }
}
```

**¿Por qué Compose en vez de XML?**  
- El código XML y el Kotlin estaban en archivos separados, obligando a sincronizarlos manualmente (ViewBinding, `findViewById`...). Con Compose todo está en Kotlin.
- Con XML, un cambio de estado requería actualizar la UI manualmente. Con Compose, la UI se redibuja sola cuando cambian los datos.
- Es el estándar actual que Google recomienda para todo proyecto Android nuevo.
- Lo usaste en FotApp y ya tienes experiencia con ello.

**¿Qué relación tiene con tu FotApp?**  
En FotApp usabas Compose con `mutableStateOf` directamente en los composables (estado local). En SLIOR mantenemos esa forma de construir la UI, pero el estado lo gestiona el `ViewModel` con `StateFlow`. La UI en Compose observa ese StateFlow y se actualiza sola. Es lo mismo que hacías, pero con mejor separación de responsabilidades.

**¿Qué es el Compose BOM?**  
BOM significa *Bill of Materials* (lista de materiales). Es un fichero que define versiones compatibles entre sí de todas las librerías de Compose. En vez de buscar manualmente qué versión de `compose.ui` funciona con `compose.material3`, el BOM lo gestiona todo.

---

### Navigation Compose

**¿Qué es?**  
Es la librería oficial para navegar entre pantallas en Compose. Define rutas (como URLs) y un `NavHost` que muestra la pantalla correspondiente.

**¿Cómo lo usabas en FotApp?**
```kotlin
NavHost(navController, startDestination = "player_list") {
    composable("player_list") { PlayerListScreen() }
    composable("player_detail/{playerName}") { ... }
}
```

**En SLIOR es igual**, pero con pantallas de negocio: login, mapa de rutas, detalle de paquete, etc.

**¿Por qué esto en vez de startActivity?**  
`startActivity` lanza una `Activity` completa, que tiene más peso y overhead. Navigation Compose mantiene todo dentro de una sola Activity y cambia solo el contenido, lo cual es más eficiente y moderno.

---

### Android SDK API 24 (Android 7.0 Nougat)

**¿Qué significa minSdk = 24?**  
Que nuestra app puede instalarse en dispositivos con Android 7.0 o superior. Esto cubre aproximadamente el 95% de los dispositivos Android activos en el mercado.

**¿Por qué no apuntar a versiones más recientes?**  
Apuntar a API 24 nos da el mayor alcance posible de dispositivos. Los repartidores pueden tener terminales de empresa antiguos.

**¿Qué es targetSdk = 34?**  
Es la versión de Android para la que optimizamos la app (Android 14). Google Play exige que las apps apunten a versiones recientes.

---

### Arquitectura MVVM + Clean Architecture

**¿Qué es MVVM?**  
Model-View-ViewModel es un patrón de diseño que separa la lógica de la interfaz:
- **View** (Activity/Fragment): solo muestra datos y captura eventos del usuario
- **ViewModel**: gestiona el estado de la UI y la lógica de presentación
- **Model**: los datos (Room, Retrofit, repositorios)

**¿Qué es Clean Architecture?**  
Divide el proyecto en capas con responsabilidades claras:
- **Capa de presentación** (UI + ViewModel): lo que ve el usuario
- **Capa de dominio** (lógica de negocio)
- **Capa de datos** (Room + Retrofit + Repository)

**¿Por qué esta arquitectura?**  
- Facilita los tests (cada capa se puede testear por separado)
- Permite cambiar una capa sin afectar a las otras
- Es la arquitectura recomendada por Google para Android
- El código es más mantenible y escalable

---

### Room 2.6.1

**¿Qué es?**  
Room es la librería oficial de Android para trabajar con SQLite (la base de datos local del dispositivo). 
Es la capa de abstracción sobre SQLite que nos evita escribir SQL.

**¿Para qué lo usamos en SLIOR?**  
Para que la app funcione **sin conexión a internet** (offline-first). 
Cuando el repartidor no tiene cobertura, los datos se guardan en Room. Cuando recupera conexión, WorkManager sincroniza con el servidor.

**¿Qué componentes tiene?**  
- **@Entity**: clase Kotlin que se convierte en tabla SQLite
- **@Dao**: interfaz con las consultas a la base de datos
- **@Database**: clase que une todo y proporciona instancias de los DAOs

---

### Retrofit 2.9.0

**¿Qué es?**  
Retrofit convierte nuestra API REST en una interfaz Kotlin. 
En lugar de escribir manualmente el código HTTP, defines la interfaz con anotaciones y Retrofit genera el código real.

**Ejemplo:**
```kotlin
@POST("auth/login")
suspend fun login(@Body request: LoginRequest): AuthResponse
```
Esto es todo lo que necesitas para hacer una petición HTTP POST al endpoint `/auth/login`.

**¿Por qué Retrofit y no OkHttp directamente?**  
OkHttp es la capa de bajo nivel (la que realmente hace las peticiones HTTP). Retrofit es la capa de alto nivel que usa OkHttp internamente pero nos da una interfaz mucho más cómoda.

---

### Hilt 2.51.1

**¿Qué es la Inyección de Dependencias?**  
En lugar de que cada clase cree sus propias dependencias (`val repo = AuthRepository()`), un framework externo se las proporciona automáticamente. 
Esto hace el código más testeable y flexible.

**¿Qué es Hilt?**  
Hilt es la solución oficial de Google para inyección de dependencias en Android. Está construido sobre Dagger (que es más potente pero muy complejo de configurar).

**¿Qué nos evita hacer?**  
Sin Hilt, tendríamos que crear manualmente todas las instancias y pasarlas de clase en clase. Con Hilt, usamos `@Inject` o `@HiltViewModel` y el framework se encarga de todo.

**¿Por qué 2.51.1 y no 2.48?**  
La 2.48 tiene problemas de compatibilidad con KSP y Gradle 8.x. La 2.51.1 es la versión más reciente y estable con soporte completo para KSP.

---

### Coroutines + Flow

**¿Qué son las Coroutines?**  
Son la forma que tiene Kotlin de hacer tareas asíncronas (como peticiones de red o consultas a BD) de forma sencilla y legible. Sin coroutines, el código asíncrono en Android usa callbacks que se vuelven muy complicados ("callback hell").

**Ejemplo sin coroutines (callbacks):**
```kotlin
apiService.login(request, object: Callback<AuthResponse> {
    override fun onSuccess(response: AuthResponse) {
        runOnUiThread { updateUI(response) }
    }
    override fun onFailure(error: Throwable) { ... }
})
```

**Ejemplo con coroutines:**
```kotlin
val response = apiService.login(request)  // Parece síncrono, pero no bloquea el hilo
updateUI(response)
```

**¿Qué es Flow?**  
Flow es como una "tubería" de datos que emite valores a lo largo del tiempo. 
Lo usamos para observar la base de datos Room: cuando un dato cambia en Room, 
el Flow notifica automáticamente a la UI para que se actualice.

---

### KSP (Kotlin Symbol Processing)

**¿Qué es?**  
KSP es la herramienta que usa Kotlin para generar código en tiempo de compilación. 
Lo usan Room (genera el código SQL), Hilt (genera las inyecciones) y otras librerías.

**¿Por qué KSP en vez de kapt?**  
`kapt` era la herramienta anterior. `KSP` es hasta 2x más rápida y es compatible con las versiones modernas de Gradle (8.x). 
`kapt` tiene problemas de compatibilidad con Gradle 8.3+ (que fue exactamente el error que nos dio al principio).

---

### DataStore

**¿Qué es?**  
DataStore es la solución moderna de Android para almacenar datos simples de forma persistente (como el token JWT). Reemplaza a SharedPreferences.

**¿Por qué DataStore y no SharedPreferences?**  
SharedPreferences opera en el hilo principal (puede causar ANR — "App Not Responding"). DataStore usa Coroutines y opera en background. También es más seguro y predecible.

---

### WorkManager

**¿Qué es?**  
WorkManager es la librería de Android para ejecutar tareas en segundo plano de forma garantizada. Si el trabajo no se puede ejecutar ahora (sin red, batería baja), WorkManager lo reintenta automáticamente.

**¿Para qué lo usamos?**  
Para sincronizar datos con el servidor cuando el repartidor recupera conexión a internet. Si crea una ruta sin conexión, WorkManager la subirá al servidor en cuanto haya red disponible.

---

### OSMDroid 6.1.17

**¿Qué es?**  
OSMDroid es la librería Android para mostrar mapas de OpenStreetMap (el "Wikipedia de los mapas"). Funciona sin clave API y sin coste.

**¿Por qué OSMDroid y no Google Maps?**  
- Google Maps SDK requiere clave API con facturación activada (tarjeta de crédito)
- OpenStreetMap es completamente gratuito y de código abierto
- Para un proyecto académico es la opción más práctica
- Funciona sin conexión con tiles descargados previamente

---

### ZXing 4.3.0

**¿Qué es?**  
ZXing (Zebra Crossing) es la librería más popular para leer códigos de barras y QR desde la cámara del dispositivo. Está mantenida desde 2007 y soporta todos los formatos: EAN-13, QR, Code128, etc.

**¿Para qué lo usamos?**  
Para que el repartidor pueda escanear el código de barras de un paquete con la cámara del móvil, en lugar de introducir el código manualmente. Esto reduce errores y acelera el proceso de entrega.

---

##  DECISIONES DE ARQUITECTURA GLOBAL

---

### ¿Por qué Offline-First?

Los repartidores trabajan frecuentemente en zonas con mala cobertura (sótanos, polígonos industriales, zonas rurales). 
Si la app requiriese conexión constante, sería inútil en esas situaciones.

Con Offline-First:
1. La app siempre lee de Room (base de datos local) → siempre funciona
2. Los cambios se guardan localmente primero
3. WorkManager sincroniza con el servidor cuando hay red

### ¿Por qué UUID como identificadores?

Los IDs numéricos secuenciales (1, 2, 3...) tienen problemas en offline-first: si el dispositivo crea un registro sin conexión, no sabe qué ID le asignará el servidor. Los UUID se generan en el dispositivo y son globalmente únicos, así que no hay conflictos.

### ¿Por qué separar DTOs de entidades?

Las entidades JPA/Room son el "mapa interno" de la base de datos. Los DTOs son el "contrato público" de la API. Si mezclamos ambos:
- Un cambio en la BD rompería la API
- Podríamos exponer datos sensibles (como el password hasheado)
- Sería difícil versionar la API

Con DTOs separados, podemos evolucionar la BD y la API de forma independiente.

---

##  CI/CD — INTEGRACIÓN Y ENTREGA CONTINUA

**¿Qué es CI/CD?**
- **CI (Continuous Integration):** cada vez que subes código a GitHub, una máquina automáticamente compila tu proyecto y ejecuta los tests. Si algo falla, te avisa antes de que llegue a producción.
- **CD (Continuous Delivery):** si el CI pasa, automáticamente se genera el artefacto final (el JAR del servidor o el APK de la app) listo para desplegar.

**¿Qué herramienta usaremos?**  
**GitHub Actions** — está integrado directamente en GitHub, es gratuito para repositorios privados (hasta cierto límite) y no requiere instalar nada.

**¿Cómo funcionará en SLIOR?**

```
Tú haces push a feature/fase-X
        ↓
GitHub Actions arranca automáticamente
        ↓
  
    Backend: mvn test + mvn package
    Android: gradlew assembleDebug 
  
        ↓
   Todo OK → puedes hacer PR a develop
   Error   → tienes que corregirlo primero
```

**¿Qué necesitamos para activarlo?**
- Que existan tests escritos (si el CI no tiene tests que ejecutar, solo compila, que es útil pero limitado)
- Un fichero `.github/workflows/backend.yml` y otro `android.yml`

**¿Cuándo lo implementaremos?**  
Al finalizar las fases de desarrollo, cuando tengamos tests unitarios e integración escritos. Así el CI tiene valor real.

>  **Pendiente de implementar** al finalizar las fases de desarrollo.

---

##  REFERENCIA: PROYECTO FOTAPP

Durante la Fase 1 Android, revisé mi proyecto anterior, **FotApp (FutConnect)**, disponible en [GitHub](https://github.com/jrc191/FotApp/tree/feature/GUI), para adaptar el estilo de desarrollo Android que ya conocía al proyecto SLIOR.

### ¿Qué aprendimos de FotApp?

FotApp es una app de información sobre jugadores de fútbol construida íntegramente con **Jetpack Compose** y **Material 3**. Fue el punto de referencia para decidir usar Compose en SLIOR en lugar de XML.

**Lo que tomamos de FotApp:**
- La forma de estructurar pantallas como funciones `@Composable`
- El uso de `NavHost` con rutas en string para navegar entre pantallas
- El enfoque de una sola `Activity` principal
- El patrón de componentes reutilizables (en FotApp: `FutButtonComp`, `PlayerCard`...; en SLIOR haremos lo mismo)
- El uso de Material 3 para el diseño visual

**Lo que mejoró SLIOR respecto a FotApp:**

| Aspecto | FotApp | SLIOR |
|---------|--------|-------|
| Estado de la UI | `mutableStateOf` directo en Composables | `StateFlow` en `ViewModel` |
| Gestión de datos | Datos hardcodeados en `Datasource.kt` | Room + Retrofit (datos reales del servidor) |
| Inyección de dependencias | Sin DI (objetos creados manualmente) | Hilt (inyección automática y testeable) |
| Comunicación con backend | Sin backend (Retrofit declarado, sin usar) | Retrofit con autenticación JWT |
| Persistencia | Solo en memoria (`rememberSaveable`) | Room SQLite + DataStore |
| Trabajo en segundo plano | Ninguno | WorkManager para sincronización offline |
| Arquitectura | Sin capas definidas | MVVM + Clean Architecture |

**¿Por qué mantener el estilo Compose de FotApp en SLIOR?**  
Ya tengo experiencia con Jetpack Compose gracias a FotApp. Aprender XML hubiera supuesto dedicar tiempo a una tecnología más antigua y con menos futuro. Compose es el estándar actual de Android y me permite aplicar conocimientos previos mientras incorporo patrones de arquitectura profesionales nuevos (ViewModel, Hilt, Room, Retrofit).
