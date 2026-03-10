# Stack Tecnológico del Proyecto SLIOR

## Justificación y Análisis de Tecnologías

---

## 1. Introducción

El desarrollo del sistema SLIOR implica dos componentes principales: un servidor backend que expone una API REST, y una aplicación cliente Android. La selección de tecnologías para ambos componentes se ha realizado siguiendo criterios de madurez, compatibilidad, documentación oficial y adecuación a los requisitos funcionales del sistema, especialmente la capacidad de funcionamiento sin conexión a internet (*offline-first*).

---

## 2. Tecnologías del Backend

### 2.1 Lenguaje: Java 17 LTS

Java es un lenguaje de programación orientado a objetos de tipado estático, ampliamente adoptado en el desarrollo de aplicaciones empresariales. La versión 17 corresponde a una versión LTS (*Long Term Support*), con soporte garantizado hasta al menos 2029.

**Justificación de la elección:**
- Es la versión mínima requerida por Spring Boot 3.x, el framework empleado en el backend
- Introduce características del lenguaje modernas utilizadas en el proyecto, como los *Java Records* (empleados en los DTOs) y mejoras en el sistema de tipos
- Su carácter LTS garantiza estabilidad y soporte a largo plazo, adecuado para el ciclo de vida académico del proyecto
- El entorno de ejecución disponible (JDK 21 de Eclipse Adoptium) es compatible con código compilado para Java 17, al ser Java retrocompatible

### 2.2 Framework: Spring Boot 3.2.4

Spring Boot es un framework de código abierto basado en el ecosistema Spring que simplifica radicalmente el desarrollo de aplicaciones Java al proporcionar configuración automática (*autoconfiguration*), un servidor web embebido y convenciones sobre configuración.

**Componentes de Spring Boot utilizados:**

| Módulo | Función en SLIOR |
|--------|-----------------|
| `spring-boot-starter-web` | Servidor HTTP REST con Tomcat embebido |
| `spring-boot-starter-data-jpa` | Capa de persistencia con JPA e Hibernate |
| `spring-boot-starter-security` | Autenticación y autorización con JWT |
| `spring-boot-starter-validation` | Validación de datos de entrada (Jakarta Bean Validation) |
| `spring-boot-starter-mail` | Envío de notificaciones por correo electrónico |

**Justificación de la versión 3.2.4:**
- Es la versión estable más reciente de la rama 3.x en el momento de inicio del proyecto
- La rama 3.x utiliza Jakarta EE (sucesor de Java EE), con paquetes `jakarta.*` en lugar de los obsoletos `javax.*`
- Requiere Java 17 como mínimo, alineado con nuestra elección de lenguaje

**Ventajas frente a alternativas:**
Spring Boot fue preferido frente a otras opciones como Quarkus o Micronaut por su mayor adopción en la industria, la amplitud de su documentación y la familiaridad del alumnado con el ecosistema Java-Spring en el ámbito académico.

### 2.3 ORM: Spring Data JPA con Hibernate

JPA (*Java Persistence API*) es el estándar de Java para el mapeo objeto-relacional (ORM). Hibernate es la implementación de referencia de JPA. Spring Data JPA añade una capa de abstracción que permite definir repositorios declarativamente.

**Funcionalidades empleadas:**
- Mapeo de clases Java a tablas relacionales mediante anotaciones (`@Entity`, `@Table`, `@Column`)
- Generación automática del esquema de base de datos con `hibernate.ddl-auto=update`
- Derivación de consultas a partir del nombre del método (`findByEmail`, `existsByEmail`)
- Auditoría automática de fechas con `@CreatedDate` y `@LastModifiedDate` mediante `@EnableJpaAuditing`
- Borrado lógico mediante `@SQLDelete` y `@Where`, que sustituye el `DELETE` físico por una actualización del campo `isDeleted`

### 2.4 Base de datos: PostgreSQL 15

PostgreSQL es un sistema de gestión de bases de datos relacionales de código abierto, reconocido por su robustez, extensibilidad y cumplimiento del estándar SQL.

**Justificación:**
- Soporte nativo para el tipo de dato `UUID`, empleado como clave primaria en todas las entidades del sistema
- Mayor capacidad de concurrencia respecto a SQLite, necesaria para un entorno multi-usuario
- Amplia integración con el ecosistema Spring Boot y Hibernate
- Herramienta de referencia en proyectos Java empresariales

### 2.5 Seguridad: Spring Security con JWT

**Spring Security** es el módulo estándar de Spring para la gestión de autenticación y autorización. En SLIOR se configura en modo *stateless* (sin sesiones en servidor), adecuado para APIs REST.

**JSON Web Token (JWT)** es un estándar abierto (RFC 7519) para la transmisión segura de información entre partes como un objeto JSON firmado digitalmente.

El flujo de autenticación implementado es el siguiente:
1. El cliente envía credenciales al endpoint `POST /auth/login`
2. El servidor verifica las credenciales contra la base de datos
3. Si son correctas, genera un JWT firmado con HMAC-SHA256 con expiración de 30 minutos
4. El cliente almacena el JWT y lo incluye en el header `Authorization: Bearer {token}` en cada petición
5. El filtro `JwtAuthenticationFilter` valida el token antes de procesar cada petición

**Librería jjwt 0.12.3:**  
JJWT (*Java JWT*) es la librería Java más utilizada para la generación y validación de tokens JWT. La versión 0.12.x presenta una API modernizada respecto a versiones anteriores (pre-0.11), con métodos más seguros y claros. Se eligió esta versión por su compatibilidad con Spring Boot 3.x y su API actualizada.

**Algoritmo BCrypt para contraseñas:**  
Las contraseñas no se almacenan en texto plano ni con algoritmos de hash simples (MD5, SHA-1). Se emplea BCrypt, un algoritmo específicamente diseñado para el hashing de contraseñas que incorpora un factor de coste configurable y un *salt* aleatorio por cada hash, haciendo inviables los ataques por diccionario o tablas arcoíris.

### 2.6 Gestión de dependencias: Apache Maven 3.9.6

Maven es la herramienta de construcción estándar en el ecosistema Java. El fichero `pom.xml` (*Project Object Model*) declara las dependencias, plugins y configuración del proyecto.

**Funciones en el proyecto:**
- Gestión y descarga automática de dependencias desde repositorios Maven Central
- Compilación del proyecto: `mvn clean compile`
- Ejecución de tests: `mvn test`
- Empaquetado del artefacto final: `mvn package` (genera un JAR ejecutable con Tomcat embebido)

### 2.7 Reducción de boilerplate: Lombok

Lombok es una librería Java que, mediante procesamiento de anotaciones en tiempo de compilación, genera automáticamente código repetitivo como getters, setters, constructores, `equals`, `hashCode` y `toString`.

**Impacto en el proyecto:** Una entidad JPA típica como `User` requeriría aproximadamente 150 líneas de código sin Lombok. Con Lombok se reduce a unas 30 líneas con la misma funcionalidad.

---

## 3. Tecnologías de la Aplicación Android

### 3.1 Lenguaje: Kotlin 2.0.21

Kotlin es el lenguaje de programación oficial para el desarrollo de aplicaciones Android desde 2017, recomendado por Google. Es un lenguaje de tipado estático que compila a bytecode JVM y es 100% interoperable con Java.

**Características clave empleadas:**
- **Null safety**: el sistema de tipos distingue entre tipos nullable (`String?`) y non-nullable (`String`), eliminando en tiempo de compilación los `NullPointerException` que son la principal causa de crashes en Android
- **Data classes**: generan automáticamente `equals`, `hashCode`, `toString` y `copy`
- **Coroutines**: modelo de concurrencia que simplifica el código asíncrono
- **Extension functions**: permiten añadir métodos a clases existentes sin herencia
- **Sealed classes/interfaces**: para modelar estados con tipo seguro

**Versión 2.0.21:** Incluye el compilador K2, significativamente más rápido que su predecesor, y mejoras en la inferencia de tipos.

### 3.2 Versión mínima: Android API 24 (Android 7.0 Nougat)

La configuración `minSdk = 24` establece Android 7.0 como versión mínima soportada. Esta decisión equilibra compatibilidad y funcionalidad:

- Cubre aproximadamente el 94-96% de los dispositivos Android activos según estadísticas de Google Play
- Android 7.0 tiene soporte completo para Java 8 (streams, lambdas), necesario para las librerías del proyecto
- Excluye terminales con versiones de Android obsoletas sin soporte de seguridad

### 3.3 Arquitectura: MVVM + Clean Architecture

El proyecto Android sigue la arquitectura recomendada por Google para aplicaciones Android modernas, combinando el patrón MVVM con los principios de Clean Architecture.

**Capas de la arquitectura:**

```

  Capa de Presentación               
  Composables (Jetpack Compose)      
  Observa StateFlow del ViewModel    

  Capa de ViewModel                  
  Gestiona estado UI + lógica simple 
  Usa coroutines para llamadas async 

  Capa de Datos (Repository)         
  Única fuente de verdad             
  Decide entre Room y Retrofit       

  Room          Retrofit            
  SQLite        API REST            
  (local)       (remoto)            

```

**Ventajas:**
- La UI nunca accede directamente a la base de datos ni a la red
- Cada capa es testeable de forma independiente
- Los cambios en una capa no afectan a las demás

### 3.4 Interfaz de usuario: Jetpack Compose

Jetpack Compose es el toolkit de UI declarativo oficial de Android, introducido de forma estable en 2021 y recomendado por Google para todo nuevo desarrollo Android desde 2022.

**Fundamento del paradigma declarativo:**  
En el enfoque clásico (XML + ViewBinding), el desarrollador describe *cómo* construir la UI y *cómo* actualizarla cuando cambian los datos (manipulación imperativa del árbol de vistas). En Compose, el desarrollador describe *qué* mostrar en función del estado actual, y el framework gestiona automáticamente las actualizaciones del árbol de UI (*recomposición*).

**Componentes de Compose utilizados en SLIOR:**

| Componente | Función |
|------------|---------|
| `@Composable` | Anotación que marca una función como elemento de UI |
| `remember` / `rememberSaveable` | Estado local que sobrevive a recomposiciones |
| `collectAsState()` | Observa un `StateFlow` del ViewModel en Compose |
| `MaterialTheme` | Sistema de diseño Material 3 |
| `Scaffold` | Estructura base con TopBar, BottomBar, FAB |
| `LazyColumn` / `LazyRow` | Listas virtualizadas (equivalente a RecyclerView) |

**Compose BOM (Bill of Materials):**  
El BOM es un artefacto Maven que gestiona las versiones de todas las librerías de Compose, garantizando compatibilidad entre ellas. La versión `2024.09.03` se eligió por ser la más reciente y estable en el inicio del proyecto.

**Justificación de Compose frente a XML:**
- Elimina la sincronización manual entre XML y Kotlin (ViewBinding, `findViewById`)
- El estado de la UI fluye unidireccionalmente desde el ViewModel
- Compilación incremental más rápida en proyectos grandes
- Es el estándar oficial para nuevos proyectos Android
- El alumno tiene experiencia previa con Compose (proyecto FotApp)

**Influencia del proyecto FotApp:**  
Durante el desarrollo, se analizó el proyecto previo del alumno (*FotApp*, disponible en GitHub) para alinear el estilo de desarrollo Android. FotApp utiliza Compose, Material 3 y Navigation Compose, tecnologías adoptadas igualmente en SLIOR. Sin embargo, SLIOR incorpora patrones de arquitectura adicionales ausentes en FotApp: ViewModel con StateFlow, Hilt, Room y Retrofit. Véase la sección 6 para el análisis comparativo detallado.

### 3.5 Navegación: Navigation Compose

Navigation Compose es la extensión de Jetpack Navigation para aplicaciones Compose. Implementa la navegación mediante un `NavHost` que asocia rutas (identificadores de tipo string) con funciones Composable.

**Patrón Single-Activity:**  
SLIOR sigue la arquitectura *Single-Activity*, donde toda la navegación ocurre dentro de una única `Activity` principal (`MainActivity`). Cada pantalla es un Composable registrado como destino de navegación. Este patrón es el recomendado por Google para aplicaciones Compose.

**Ventajas frente a múltiples Activities:**
- Menor overhead de sistema (las Activities son componentes pesados del sistema Android)
- Paso de datos entre pantallas mediante argumentos de ruta, sin necesidad de `Intent` con `Bundle`
- Gestión del back stack unificada y predecible

### 3.6 Persistencia local: Room 2.6.1

Room es la librería oficial de Android para bases de datos SQLite. Proporciona una capa de abstracción sobre SQLite que verifica las consultas SQL en tiempo de compilación.

**Componentes empleados:**
- `@Entity`: define la estructura de una tabla SQLite
- `@Dao` (*Data Access Object*): interfaz con métodos de consulta que Room implementa automáticamente
- `@Database`: clase abstracta que une entidades y DAOs
- Integración nativa con Kotlin Coroutines (`suspend fun`) y Flow

**Rol en la arquitectura offline-first:**
Room es la **única fuente de verdad** de la aplicación. La UI siempre lee datos de Room. Retrofit solo se usa para sincronizar Room con el servidor, nunca para alimentar la UI directamente.

### 3.7 Cliente HTTP: Retrofit 2.9.0 + OkHttp 4.12

**Retrofit** es una librería que convierte la API REST en una interfaz Kotlin mediante anotaciones, generando automáticamente el código HTTP necesario.

**OkHttp** es el cliente HTTP de bajo nivel sobre el que se construye Retrofit. Se utiliza directamente para implementar el `AuthInterceptor`, que añade automáticamente el token JWT a cada petición.

**Justificación:** Retrofit es el cliente HTTP estándar de facto en Android, con amplia adopción, soporte de Coroutines, y conversores de JSON como Gson integrados.

### 3.8 Inyección de dependencias: Hilt 2.51.1

Hilt es la solución oficial de Google para inyección de dependencias en Android, construida sobre Dagger 2. Simplifica la configuración de Dagger mediante anotaciones predefinidas para cada componente Android.

**Anotaciones principales:**
- `@HiltAndroidApp`: en la clase Application, activa la generación de código de Hilt
- `@AndroidEntryPoint`: en Activities y Fragments, permite la inyección de dependencias
- `@HiltViewModel`: en ViewModels, permite la inyección en el constructor
- `@Module` + `@InstallIn`: declara cómo crear instancias de dependencias
- `@Provides` + `@Singleton`: define factorías de objetos con ciclo de vida controlado

**Versión 2.51.1:** Necesaria para compatibilidad con KSP y Gradle 8.x.

### 3.9 Asincronía: Kotlin Coroutines + Flow

**Coroutines** son una característica del lenguaje Kotlin que permite escribir código asíncrono de forma secuencial y legible, sin bloquear el hilo principal de Android (UI thread).

**Flow** es un tipo de Coroutine que emite múltiples valores de forma reactiva a lo largo del tiempo. Se usa para observar cambios en Room: cuando un dato cambia en la base de datos, el Flow notifica automáticamente a los observadores.

**Ventaja frente a alternativas:**
- vs. callbacks: el código es lineal y fácil de seguir
- vs. RxJava: menos verbosidad, integración nativa en Kotlin, mejor rendimiento

### 3.10 Procesamiento de anotaciones: KSP (Kotlin Symbol Processing)

KSP es el procesador de anotaciones de nueva generación para Kotlin, desarrollado por Google. Reemplaza a `kapt` (Kotlin Annotation Processing Tool).

**¿Por qué KSP en lugar de kapt?**

| Característica | kapt | KSP |
|---------------|------|-----|
| Velocidad de compilación | Lenta (genera Java stubs) | Hasta 2x más rápida |
| Compatibilidad Gradle 8.x | Problemas con 8.3+ | Completa |
| Soporte oficial | En mantenimiento | Activo y recomendado |

La migración de `kapt` a KSP fue necesaria debido a incompatibilidades con Gradle 8.13, la versión utilizada por Android Studio 2025.1.3 (Meerkat).

### 3.11 Almacenamiento de preferencias: DataStore

DataStore es la solución moderna de Android para almacenar datos clave-valor de forma persistente. Reemplaza a SharedPreferences.

**Uso en SLIOR:** Almacenamiento del token JWT entre sesiones de la aplicación.

**Ventaja frente a SharedPreferences:** DataStore opera de forma asíncrona con Coroutines, evitando bloqueos en el hilo principal que podrían causar errores de rendimiento (ANR).

### 3.12 Tareas en segundo plano: WorkManager

WorkManager es la librería recomendada por Google para tareas en background que deben ejecutarse de forma garantizada, incluso si la aplicación se cierra o el dispositivo se reinicia.

**Uso en SLIOR:** Sincronización de datos locales (creados en modo offline) con el servidor cuando el dispositivo recupera conexión a internet. WorkManager garantiza que ningún dato offline se pierda.

**Funcionamiento:**
1. El repartidor crea o modifica datos sin conexión
2. Los cambios se guardan en Room con `syncStatus = PENDING`
3. Simultáneamente, se registra una tarea en WorkManager con la restricción `NetworkType.CONNECTED`
4. Cuando hay conexión, WorkManager ejecuta `SyncWorker`, que envía los cambios pendientes al servidor

### 3.13 Mapas: OSMDroid 6.1.17

OSMDroid es la librería Android oficial para la visualización de mapas basados en OpenStreetMap (OSM), el proyecto cartográfico colaborativo de código abierto.

**Comparativa con Google Maps SDK:**

| Criterio | OSMDroid (OpenStreetMap) | Google Maps SDK |
|----------|--------------------------|-----------------|
| Coste | Gratuito, sin límites | Requiere clave API + facturación |
| Datos cartográficos | OpenStreetMap (abierto) | Google Maps |
| Uso offline | Sí (tiles descargables) | Limitado |
| Dependencia externa | Ninguna | Cuenta Google Cloud |
| Adecuación académica | Alta | Requiere configuración de pagos |

### 3.14 Escaneo de códigos: ZXing 4.3.0

ZXing (*Zebra Crossing*) es la librería de código abierto más extendida para la lectura de códigos de barras y QR mediante la cámara del dispositivo.

**Formatos soportados relevantes para SLIOR:**
- EAN-13, EAN-8: códigos de barras de productos de consumo
- Code 128, Code 39: códigos de barras logísticos (GS1)
- QR Code: código bidimensional de uso general

**Integración:** Mediante la librería `zxing-android-embedded`, que proporciona una Activity preconfigurada para el escaneo, eliminando la necesidad de gestionar manualmente la cámara.

---

## 4. Herramientas de Desarrollo

### 4.1 Control de versiones: Git y modelo de ramificación

Git es el sistema de control de versiones distribuido estándar en la industria del software. Se emplea la convención **Conventional Commits** para los mensajes de commit, que establece un formato estructurado: `tipo(ámbito): descripción`.

**Tipos de commit empleados:** `feat` (nueva funcionalidad), `fix` (corrección), `docs` (documentación), `refactor` (refactorización), `chore` (tareas de mantenimiento), `test` (tests).

**Modelo de ramificación (rama `main` como producción):**

El repositorio sigue un modelo de ramificación profesional basado en GitFlow simplificado:

| Rama | Propósito |
|------|-----------|
| `main` | Código de producción. Solo recibe merges desde `develop` |
| `develop` | Rama de integración continua. Recibe merges de ramas `feature/` |
| `feature/fase-X-descripcion` | Una rama por fase de desarrollo, creada desde `develop` |

El flujo de trabajo es el siguiente: el desarrollo ocurre en ramas `feature/`, que se integran en `develop` mediante merge al completar cada fase. Cuando `develop` es estable, se integra en `main`, constituyendo un "release". Este flujo garantiza que `main` siempre contenga código funcional y probado.

### 4.2 CI/CD: GitHub Actions

Se implementará un pipeline de **Integración Continua y Entrega Continua (CI/CD)** mediante **GitHub Actions** al finalizar las fases de desarrollo, una vez que existan tests unitarios e integración escritos.

**Pipeline previsto:**

| Evento | Acción CI/CD |
|--------|-------------|
| Push a cualquier rama | Compilación del backend (`mvn package`) y app Android (`gradlew assembleDebug`) |
| Pull Request a `develop` | Compilación + ejecución de tests unitarios |
| Merge a `main` | Compilación + tests + generación de artefactos (JAR y APK) |

**Justificación:**  
La integración de CI/CD en el flujo de desarrollo garantiza que ningún código que rompa la compilación o los tests llegue a las ramas de integración. Es una práctica estándar en entornos de desarrollo profesional que demuestra conocimiento del ciclo de vida completo del software (*DevOps*).

> **Estado:** Pendiente de implementación. Se activará al concluir las fases de desarrollo funcional del sistema.

### 4.3 IDEs

- **IntelliJ IDEA 2025.2.1**: entorno de desarrollo para el backend Java/Spring Boot
- **Android Studio 2025.1.3 (Meerkat)**: entorno oficial de desarrollo para Android

### 4.4 Control de versiones

- **Git 2.x**: sistema de control de versiones distribuido
- **GitHub**: plataforma de alojamiento del repositorio con ramas `main`, `develop` y ramas de feature

---

## 5. Resumen de Versiones

| Tecnología | Versión | Ámbito |
|------------|---------|--------|
| Java | 17 (JDK 21) | Backend |
| Spring Boot | 3.2.4 | Backend |
| Spring Security | 6.2.x (via Boot) | Backend |
| PostgreSQL | 15+ | Backend |
| jjwt | 0.12.3 | Backend |
| Lombok | (via Boot BOM) | Backend |
| Maven | 3.9.6 | Backend |
| Kotlin | 2.0.21 | Android |
| Android SDK min | 24 (Android 7.0) | Android |
| Android SDK target | 34 (Android 14) | Android |
| Room | 2.6.1 | Android |
| Retrofit | 2.9.0 | Android |
| OkHttp | 4.12.0 | Android |
| Hilt | 2.51.1 | Android |
| Coroutines | 1.7.3 | Android |
| KSP | 2.0.21-1.0.28 | Android |
| DataStore | 1.0.0 | Android |
| WorkManager | 2.9.0 | Android |
| Jetpack Compose BOM | 2024.09.03 | Android |
| Navigation Compose | 2.8.2 | Android |
| OSMDroid | 6.1.17 | Android |
| ZXing | 4.3.0 | Android |
| AGP | 8.9.0 | Android |
| Gradle | 8.13 | Android |
| Git | - | Ambos |

---

## 6. Análisis Comparativo: FotApp vs. SLIOR

Durante la fase de desarrollo Android, se analizó el proyecto previo del alumno, **FotApp (FutConnect)** (rama `feature/GUI`, disponible en GitHub), como referencia para el estilo de desarrollo Android. Este análisis motivó la adopción de Jetpack Compose en SLIOR y permitió identificar las mejoras arquitectónicas necesarias para un sistema de mayor complejidad.

### 6.1 Descripción de FotApp

FotApp es una aplicación de consulta de información sobre jugadores de fútbol, desarrollada como práctica anterior por el alumno. La aplicación presenta:

- Interfaz construida íntegramente con **Jetpack Compose** y **Material 3**
- Navegación mediante **Navigation Compose** con `NavHost`
- Diseño responsivo con variantes para pantallas compactas y expandidas (tablets)
- Gestión de datos mediante un objeto singleton (`Datasource`) con datos estáticos

### 6.2 Tabla Comparativa

| Aspecto Arquitectónico | FotApp | SLIOR |
|------------------------|--------|-------|
| **Framework UI** | Jetpack Compose | Jetpack Compose |
| **Sistema de diseño** | Material 3 | Material 3 |
| **Navegación** | Navigation Compose | Navigation Compose |
| **Patrón de pantallas** | Single-Activity | Single-Activity |
| **Gestión de estado UI** | `mutableStateOf` en Composables | `StateFlow` en ViewModel |
| **Capa de presentación** | Sin separación (estado en UI) | ViewModel con `@HiltViewModel` |
| **Origen de datos** | Datos estáticos (`Datasource.kt`) | Room + Retrofit (datos reales) |
| **Persistencia** | En memoria (`rememberSaveable`) | Room SQLite + DataStore |
| **Inyección de dependencias** | Ninguna | Hilt 2.51.1 |
| **Comunicación con backend** | Sin backend | API REST con JWT |
| **Trabajo en background** | Ninguno | WorkManager |
| **Manejo de errores** | Sin gestión formal | `sealed class Result<T>` |
| **Arquitectura formal** | Sin capas definidas | MVVM + Clean Architecture |

### 6.3 Decisión de Adopción de Compose

La decisión de emplear Jetpack Compose en SLIOR, en lugar del enfoque clásico basado en XML, se sustentó en los siguientes argumentos:

1. **Experiencia previa del alumno**: El alumno domina Compose por su uso en FotApp, lo que permite centrarse en los aspectos arquitectónicos avanzados sin la curva de aprendizaje de una nueva tecnología de UI.

2. **Alineación con las recomendaciones de Google**: Compose es el toolkit de UI recomendado oficialmente para nuevos proyectos Android desde 2022.

3. **Coherencia con el paradigma reactivo**: El flujo unidireccional de datos (`StateFlow` → `collectAsState()` → Composable) es más natural en Compose que en el sistema View clásico.

4. **Productividad**: La eliminación de XML y ViewBinding simplifica el código de la capa de presentación.

### 6.4 Mejoras Introducidas Respecto a FotApp

Las principales mejoras arquitectónicas introducidas en SLIOR respecto al modelo de FotApp son:

- **Separación de responsabilidades**: el estado deja de residir en los Composables y se traslada al `ViewModel`, que es la única clase que puede modificarlo.
- **Testabilidad**: la arquitectura MVVM con Hilt permite sustituir dependencias reales por mocks en los tests unitarios.
- **Persistencia real**: Room proporciona una base de datos SQLite local completa, frente al almacenamiento en memoria de FotApp.
- **Comunicación con servidor**: Retrofit con autenticación JWT conecta la aplicación a un backend real, frente a la ausencia de backend en FotApp.
- **Capacidad offline**: WorkManager y Room combinados permiten operar sin conexión y sincronizar datos automáticamente.
