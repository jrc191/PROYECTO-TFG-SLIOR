# Registro de Errores y Soluciones — SLIOR

> Documento actualizado a lo largo de todo el proyecto.
> Cada error encontrado queda registrado con su causa exacta y la solución aplicada.

---

## Índice de Errores

| # | Fase | Componente | Error resumido | Estado |
|---|------|-----------|---------------|--------|
| 1 | Fase 0 | Android / Gradle | `fileCollection(Spec)` incompatibilidad kapt + Gradle 8.x |  Resuelto |
| 2 | Fase 1 Android | `MainActivity` / appcompat | `addMenuProvider` firma incompatible appcompat 1.6.1 vs activity-compose 1.9.2 |  Resuelto |
| 3 | Fase 1 Android | D8 / Gradle JVM | `OutOfMemoryError: Java heap space` durante compilación |  Resuelto |

---

## ERROR #1

**Fecha:** 09/03/2026  
**Fase:** Fase 0 — Inicialización  
**Componente:** Proyecto Android (Gradle)  
**Severidad:**  Bloqueo (el proyecto no podía sincronizar)

### Mensaje de error

```
A problem occurred configuring project ':app'.
> Failed to notify project evaluation listener.
   > 'org.gradle.api.file.FileCollection
     org.gradle.api.artifacts.Configuration.fileCollection(
     org.gradle.api.specs.Spec)'
```

### ¿Qué estaba pasando?

El proyecto Android fue configurado inicialmente con el plugin `kotlin-kapt` (Kotlin Annotation Processing Tool) para que librerías como Room y Hilt pudieran generar código en tiempo de compilación.

El problema es que `kapt` llama internamente al método `Configuration.fileCollection(Spec)` de la API de Gradle. 
Este método fue marcado como deprecated en Gradle 7.x y **eliminado definitivamente en Gradle 8.3**.

Android Studio 2025.1.3 (Meerkat) utiliza Gradle 8.13 como versión predeterminada, muy por encima del umbral donde `kapt` deja de funcionar. 
Por tanto, en el momento de sincronizar el proyecto por primera vez, Gradle intentó ejecutar código que ya no existe en su API y lanzó el error.

### Diagrama de la causa

```
build.gradle.kts
   plugin: kotlin-kapt
         usa internamente: Configuration.fileCollection(Spec)
               método ELIMINADO en Gradle 8.3
                     Android Studio usa Gradle 8.13
                            ERROR al sincronizar
```

### Solución aplicada

Se migró de `kapt` a **KSP** (Kotlin Symbol Processing), el sucesor oficial recomendado por Google y JetBrains.

**Cambios en `build.gradle.kts` (raíz):**
```kotlin
// ANTES
id("org.jetbrains.kotlin.android") version "1.9.22" apply false
id("com.google.dagger.hilt.android") version "2.48" apply false

// DESPUÉS
id("org.jetbrains.kotlin.android") version "2.0.21" apply false
id("com.google.dagger.hilt.android") version "2.51.1" apply false
id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false  // ← nuevo
```

**Cambios en `app/build.gradle.kts`:**
```kotlin
// ANTES (plugins)
id("kotlin-kapt")

// DESPUÉS (plugins)
id("com.google.devtools.ksp")

// ANTES (dependencias)
kapt("androidx.room:room-compiler:2.6.1")
kapt("com.google.dagger:hilt-compiler:2.48")
kapt("androidx.hilt:hilt-compiler:1.1.0")

// DESPUÉS (dependencias)
ksp("androidx.room:room-compiler:2.6.1")
ksp("com.google.dagger:hilt-compiler:2.51.1")
ksp("androidx.hilt:hilt-compiler:1.2.0")
```

Se añadió también `gradle/wrapper/gradle-wrapper.properties` para fijar explícitamente Gradle 8.13.

### ¿Por qué KSP es mejor que kapt?

| Característica | kapt | KSP |
|---------------|------|-----|
| Velocidad de compilación | Lenta | Hasta 2× más rápida |
| Compatibilidad Gradle 8.x |  Problemas desde 8.3 |  Completa |
| Estado oficial | En mantenimiento | Activo, recomendado |
| Soporte de Room | Sí | Sí (preferido) |
| Soporte de Hilt | Sí | Sí (preferido) |

### Lección aprendida

Al iniciar un proyecto Android nuevo con versiones recientes de Android Studio (2024+), usar siempre **KSP** en lugar de `kapt`. 
Verificar que las versiones del plugin KSP y Kotlin coincidan (misma versión base, ej. `2.0.21-1.0.28` para Kotlin `2.0.21`).

---

## ERROR #2

**Fecha:** 09/03/2026
**Fase:** Fase 1 — Android
**Componente:** `MainActivity.kt` / `appcompat`

### Mensaje de error

```
Class 'MainActivity' is not abstract and does not implement abstract member:
fun addMenuProvider(p0: MenuProvider, p1: LifecycleOwner, p2: State): Unit
Cannot access 'LifecycleOwner' which is a supertype of 'MainActivity'.
Check your module classpath for missing or conflicting dependencies.
```

### Causa

Incompatibilidad de versiones entre `appcompat:1.6.1` y `activity-compose:1.9.2`.

`activity-compose:1.9.2` usa una versión de `MenuHost` (de `androidx.core`) con una firma del método `addMenuProvider` diferente a la que espera `appcompat:1.6.1`. Ambas librerías implementan o extienden `ComponentActivity`, y la firma incompatible hace que el compilador de Kotlin no pueda satisfacer la interfaz abstracta.

### Solución

Actualizar `appcompat` a la versión `1.7.0`, que es compatible con `activity-compose:1.9.2` y usa la misma versión de `androidx.core`.

```kotlin
// build.gradle.kts (app)
// ANTES
implementation("androidx.appcompat:appcompat:1.6.1")

// DESPUÉS
implementation("androidx.appcompat:appcompat:1.7.0")
```

### Lección aprendida

Al usar `activity-compose`, mantener `appcompat` en su versión más reciente (`1.7.0+`). Las versiones de `appcompat`, `activity-compose` y `androidx.core` deben ser compatibles entre sí. Usar el [BOM de Compose](https://developer.android.com/jetpack/compose/bom) ayuda a alinear las versiones de las librerías Compose, pero las librerías AndroidX externas (como `appcompat`) deben actualizarse manualmente.

---

## ERROR #3

**Fecha:** 09/03/2026
**Fase:** Fase 1 — Android
**Componente:** Build Android (D8 / dexer)

### Mensaje de error

```
AGPBI: {"kind":"error","text":"java.lang.OutOfMemoryError: Java heap space","tool":"D8"}
Caused by: java.lang.OutOfMemoryError: Java heap space
```

### Causa

El proceso de compilación de Gradle (específicamente D8, el compilador de bytecode a DEX de Android) necesita más memoria heap de la que Java tiene asignada por defecto.

El heap predeterminado de la JVM suele ser de 512 MB. Proyectos Android con muchas dependencias (Hilt, Compose, Room, Retrofit, WorkManager, etc.) pueden superar fácilmente ese límite durante la compilación.

### Solución

Crear el archivo `gradle.properties` en la raíz del módulo `mobile-app/` con una asignación de memoria mayor:

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true
android.useAndroidX=true
kotlin.code.style=official
```

`-Xmx2048m` asigna 2 GB de heap máximo a la JVM de Gradle.

### Lección aprendida

En proyectos Android con múltiples librerías que usan procesamiento de anotaciones (KSP, Hilt, Room), crear siempre `gradle.properties` con `org.gradle.jvmargs=-Xmx2048m` desde el inicio. También es recomendable activar `org.gradle.parallel=true` y `org.gradle.caching=true` para reducir los tiempos de compilación.

---

---

*— Nuevos errores se añadirán aquí a medida que avance el proyecto —*

---

## ERROR #4

**Fecha:** 11/03/2026  
**Fase:** Fase 5 — Diseño UI  
**Componente:** `RegisterScreen.kt`  
**Severidad:**  Bloqueo (el proyecto no compilaba)

### Mensaje de error

```
e: file:///...RegisterScreen.kt:61:1
Conflicting overloads:
fun RegisterScreen(onRegisterSuccess: ..., onGoToLogin: ..., viewModel: AuthViewModel = ...): Unit
```

### ¿Qué estaba pasando?

Al editar el fichero `RegisterScreen.kt` para añadir la nueva implementación brutalista, la herramienta de edición localizó solo el bloque de imports como `old_str` y ANTEPUSO el nuevo código. El resultado fue que el fichero quedó con **dos declaraciones de `fun RegisterScreen`**: la nueva (al principio) y la anterior (al final). El compilador de Kotlin lanzó un error de "conflicting overloads" al encontrar dos funciones con la misma firma en el mismo fichero.

### Solución

Eliminar la implementación duplicada al final del fichero, dejando únicamente la nueva versión brutalista. Se truncó el fichero a las primeras 340 líneas (la implementación correcta).

### Lección aprendida

Cuando se reescribe completamente un fichero, asegurarse de que el `old_str` de la edición incluya el contenido completo del fichero original, no solo las primeras líneas. Alternativamente, verificar el contenido del fichero tras la edición antes de compilar.

---

## ERROR #5

**Fecha:** 11/03/2026  
**Fase:** Fase 5 — Diseño UI  
**Componente:** `Theme.kt` / Fuentes Google  
**Severidad:**  Bloqueo (el proyecto no compilaba)

### Mensaje de error

```
e: file:///...Theme.kt:17:45
Unresolved reference 'ExperimentalGoogleFontsApi'.

e: file:///...Theme.kt:26:8
Annotation argument must be a compile-time constant.

e: file:///...Theme.kt:35:5
None of the following candidates is applicable:
fun Font(fileDescriptor: ParcelFileDescriptor, ...): Font
fun Font(file: File, ...): Font
```

### ¿Qué estaba pasando?

Tres errores relacionados con la integración de Google Fonts en Compose:

1. `ExperimentalGoogleFontsApi` no existe en el BOM `2024.09.03`. Esta anotación solo existía en versiones antiguas de la librería y fue eliminada/renombrada.

2. Al usar `@OptIn(ExperimentalGoogleFontsApi::class)` con una referencia no resuelta, el compilador no puede evaluar el argumento en tiempo de compilación.

3. `Font(googleFont, provider, ...)` no resolvía porque se estaba importando `androidx.compose.ui.text.font.Font` en lugar de `androidx.compose.ui.text.googlefonts.Font`. Las dos funciones tienen la misma firma base pero están en paquetes distintos.

### Solución

1. Eliminar todas las anotaciones `@OptIn(ExperimentalGoogleFontsApi::class)` del fichero.
2. Cambiar el import de `Font`:

```kotlin
// INCORRECTO
import androidx.compose.ui.text.font.Font

// CORRECTO
import androidx.compose.ui.text.googlefonts.Font
```

### Lección aprendida

Con el BOM `2024.09.03` de Compose, la API de Google Fonts es estable y no requiere opt-in. Siempre verificar en la documentación oficial si una API experimental ya fue promovida a estable. El error "None of the following candidates" en una función con argumentos de tipo Google-specific suele indicar un import del paquete equivocado.

---

## ERROR #6

**Fecha:** 11/03/2026  
**Fase:** Fase 5 — Diseño UI  
**Componente:** Backend Spring Boot — `/auth/login`  
**Severidad:**  Bloqueo (login devolvía siempre HTTP 500)

### Mensaje de error

```
<-- 500 http://10.0.2.2:8080/auth/login (107ms)
{"path":"/auth/login","error":"Internal Server Error",
 "message":"Error interno del servidor","status":500}
```

### ¿Qué estaba pasando?

`application.properties` define `jwt.secret=PLACEHOLDER_SET_IN_APPLICATION_LOCAL_PROPERTIES`. El secret real (`tspk8XhnY93GmpKHEvXKXBM17l3aByXhFhhEjAuJiQI=`) está en `application-local.properties`, fichero que Spring Boot solo carga cuando el perfil `local` está activo.

Al arrancar el backend sin especificar el perfil Spring, la propiedad `jwt.secret` mantenía el valor placeholder. Cuando el login llegaba a `jwtUtil.generateToken()`, la librería jjwt llamaba a `Decoders.BASE64.decode("PLACEHOLDER_SET_IN_APPLICATION_LOCAL_PROPERTIES")`. La cadena placeholder contiene guiones bajos (`_`), que son caracteres **inválidos en Base64 estándar** (solo válidos en Base64URL). La decodificación lanzaba `IllegalArgumentException`, excepción no capturada específicamente en el `GlobalExceptionHandler`, que la convierte en HTTP 500.

### Diagrama de la causa

```
Backend arrancado sin -Dspring.profiles.active=local
   application-local.properties NO se carga
         jwt.secret = "PLACEHOLDER_SET_IN_APPLICATION_LOCAL_PROPERTIES"
               Decoders.BASE64.decode(placeholder) → IllegalArgumentException
                     GlobalExceptionHandler (catch Exception) → HTTP 500
```

### Solución

Añadir el perfil activo por defecto en `application.properties`:

```properties
# application.properties
spring.profiles.active=local
```

Y en el script de arranque:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

En producción, la variable de entorno `SPRING_PROFILES_ACTIVE=prod` sobrescribe este valor.

### Lección aprendida

Cuando se usa el patrón `application-local.properties` para separar secretos del código versionado, añadir `spring.profiles.active=local` como valor por defecto en `application.properties` para desarrollo. Así el perfil se activa automáticamente sin necesidad de configurar cada entorno de ejecución (IDE, terminal, CI/CD) por separado.

---

## ERROR #7

**Fecha:** 11/03/2026  
**Fase:** Fase 5 — Diseño UI  
**Componente:** Android — `NetworkModule.kt`  
**Severidad:** 🟡 Funcional (timeout en emulador, funciona en dispositivo físico Tailscale)

### Síntoma

```
<-- HTTP FAILED: java.net.SocketTimeoutException:
    failed to connect to /100.115.5.3 (port 8080) from /10.0.2.16 after 10000ms
```

### ¿Qué estaba pasando?

`NetworkModule.kt` tenía la URL del backend **hardcodeada** directamente en el código:

```kotlin
// INCORRECTO — ignoraba BuildConfig.BASE_URL
private val BASE_URL = "http://100.115.5.3:8080/"
```

`100.115.5.3` es la IP de Tailscale del PC de desarrollo. El emulador Android corre en una máquina virtual aislada que **no tiene acceso a la red Tailscale del host**, por lo que todos los intentos de conexión agotaban el timeout de 10 segundos.

El fichero `build.gradle.kts` ya tenía configurada la URL correcta por build type (`http://10.0.2.2:8080/` para debug), pero `NetworkModule` no la usaba.

### Solución

```kotlin
// CORRECTO — usa la URL del build type activo
import com.slior.BuildConfig

private val BASE_URL = BuildConfig.BASE_URL
```

Con esto:
- **Emulador** (debug build) → `http://10.0.2.2:8080/` (emulador → localhost del PC)
- **Dispositivo físico** con Tailscale → cambiar `BASE_URL` en `build.gradle.kts` a la IP Tailscale
- **Producción** (release build) → URL pública configurada en `build.gradle.kts`

### Lección aprendida

Nunca hardcodear URLs de red en el código de producción. Usar `BuildConfig` con variables por build type es el patrón correcto en Android para gestionar entornos (desarrollo/staging/producción) sin cambiar el código fuente.
