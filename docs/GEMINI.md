busca TFGs de CFGS DAM por internet españoles, centrados en el desarrollo de una app con kotlin, y analiza qué es lo que deben de incluir, para saber cómo hacer el mío

¡Hola! Preparar el proyecto final del CFGS de Desarrollo de Aplicaciones Multiplataforma (formalmente llamado Proyecto Final de Ciclo o TFC, aunque todos le digamos TFG) es un reto genial.

Basándome en los requisitos de los institutos españoles y en proyectos reales de DAM que sacan notaza, he analizado qué es lo que sí o sí debe incluir tu memoria y tu aplicación Android en Kotlin.

Aquí tienes la estructura estándar y los elementos técnicos que los tribunales esperan ver para que sepas exactamente cómo enfocar el tuyo:

1. La Memoria del Proyecto (Documentación)
La documentación es casi tan importante como el código. El tribunal evaluará tu capacidad para aplicar la ingeniería de software. Tu memoria debe seguir esta estructura:

Introducción y Justificación: ¿De qué trata tu app? ¿Qué problema resuelve o qué necesidad cubre en el mercado actual?

Objetivos:

Generales: "Crear una app móvil para la gestión de rutinas de gimnasio".

Específicos: "Implementar login seguro", "Integrar una base de datos en tiempo real", "Diseñar una interfaz accesible".

Estudio de Mercado / Estado del Arte: Un análisis rápido de 2 o 3 apps de la competencia y por qué la tuya aporta algo distinto o mejor.

Metodología y Planificación: Los profesores quieren ver que no te pusiste a picar código a lo loco. Habla de metodologías ágiles (Scrum o Kanban), usa capturas de tu tablero de Trello/Jira y añade un diagrama de Gantt con los tiempos que le has dedicado a cada fase.

Análisis de Requisitos:

Requisitos Funcionales: Lo que la app hace (ej. "El usuario podrá subir una foto de perfil").

Requisitos No Funcionales: Rendimiento, seguridad, usabilidad (ej. "La app debe cargar en menos de 2 segundos").

Casos de Uso: Diagramas de cómo interactúan los distintos perfiles de usuario (Admin, Usuario estándar) con la app.

Diseño:

Diseño de la Base de Datos: Modelo Entidad-Relación (si usas SQL) o estructura de colecciones (si usas NoSQL como Firebase).

Diseño de Interfaz (UX/UI): Pantallas diseñadas en Figma o Adobe XD (Wireframes y Mockups), paleta de colores y tipografía. Material Design es clave aquí.

Desarrollo (El núcleo técnico): Explicación de la arquitectura usada, librerías, y fragmentos de código de las partes más complejas o de las que te sientas más orgulloso.

Pruebas (Testing): Qué pruebas has hecho para asegurar que no crashea (unitarias, de integración o pruebas manuales de usuario).

Presupuesto: ¿Cuánto costaría hacer esta app en el mundo real? (Calcula tus horas a un precio por hora de programador junior, suma licencias, coste de servidores/nube, etc.).

Conclusiones y Trabajo Futuro: Qué has aprendido, dificultades superadas y qué funcionalidades le añadirías a la app en una versión 2.0.

Manual de Usuario: Una guía breve con capturas de pantalla enseñando cómo usar la app.

2. Requisitos Técnicos de la App (Stack de Kotlin)
Para que tu proyecto destaque en 2026, no basta con hacer una app que funcione; el código debe estar actualizado a los estándares de la industria Android. Si incluyes esto, el sobresaliente está casi asegurado:

Lenguaje 100% Kotlin: Evita mezclar con Java.

Arquitectura MVVM (Model-View-ViewModel): Es el estándar absoluto hoy en día en Android. Separa la lógica de la interfaz. Si además aplicas principios de Clean Architecture, el tribunal te hará la ola.

Interfaz de Usuario (UI):

Opción Pro: Jetpack Compose. Es el presente y futuro de Android. Si lo usas en lugar del clásico XML, demostrarás que estás a la última.

Opción Clásica: XML usando ConstraintLayout y ViewBinding.

Asincronía: Usa Kotlin Coroutines (Corrutinas) y Flows para llamadas a la base de datos o red. Nada de AsyncTask o callbacks antiguos.

Base de Datos / Backend:

Lo más común y rápido: Firebase. Usa Firebase Authentication (login con Google/Email), Firestore (base de datos en la nube) y Firebase Storage (para guardar fotos de los usuarios).

Alternativa: Consumir una API REST externa usando la librería Retrofit. Si te ves con fuerzas, puedes hacer tú mismo esa API (en Node.js o Spring Boot) y conectarla a tu app.

Base de datos local: Si tu app necesita funcionar sin internet, usa Room (una capa de abstracción sobre SQLite).

Inyección de Dependencias: Usar Hilt o Koin demostrará un nivel muy profesional, aunque para un TFC a veces se considera un "extra" de nivel avanzado.

💡 3 Consejos extra para tu defensa:
Haz que sea funcional, no perfecta: Es mejor presentar una app con 4 funcionalidades que van a la perfección, que una con 15 a medias y llena de bugs.

Usa GitHub: Aloja tu proyecto en un repositorio de GitHub, haz commits descriptivos a lo largo de los meses. En tu memoria, pon el enlace al repo. A los profesores les encanta ver el historial de trabajo real.

Cuida la presentación final: El día que lo expongas, graba un pequeño vídeo de 1 minuto mostrando la app funcionando en un móvil físico por si te falla el emulador o el internet del instituto en pleno directo (la famosa "maldición de la demo").

¿Tienes ya alguna idea en mente para la temática de tu aplicación o estás buscando inspiración?

esos son los documentos actuales con la info, aunque hay cosas aún sujetas a cambios

¡Oye, qué pintaza tiene el proyecto SLIOR! Viendo los documentos que me has pasado, te digo con total sinceridad que tienes un TFG de matrícula de honor entre manos. Estás aplicando conceptos y tecnologías que muchas veces se ven en perfiles Junior/Mid en empresas reales.

Vamos a analizar cómo encaja lo que tienes en la estructura ideal de un TFG de DAM y qué te faltaría añadir.

1. Lo que ya tienes (y va a enamorar al tribunal)
El núcleo técnico de tu proyecto está espectacularmente bien definido y supera con creces lo que se suele pedir en un ciclo superior:

Stack Frontend a la última: Has elegido Kotlin 1.9.x y Jetpack Compose para crear una UI reactiva y declarativa. Añadir Hilt para la inyección de dependencias demuestra madurez como desarrollador.

Identidad Visual: Tu decisión de aplicar un diseño "Neo-Brutalismo" (con verde neón, naranja de seguridad, bordes gruesos y sombras) para diferenciarte de las típicas apps aburridas de logística es un puntazo para la memoria (sección de Diseño UX/UI).

Modo Offline-First (La joya de la corona): Usar Room como caché local y Android WorkManager para encolar tareas en segundo plano (y que se sincronicen solas cuando vuelve internet) es una característica técnica compleja y brillante.

Backend Robusto: Tienes una arquitectura basada en Spring Boot 3.2.x con seguridad por JWT. El uso de PostgreSQL 15 aprovechando los índices Trigram (pg_trgm) para la geocodificación demuestra que no solo has hecho un simple CRUD.

Funcionalidades Pro: Integrar OSMDroid, generar etiquetas en PDF con códigos de barras (Code 128) y QR usando OpenPDF y ZXing, y encima implementar un sistema automático de anonimización para cumplir con el RGPD (derechos ARSULIPO) es un trabajo de 10.

2. Cómo aprovechar tus documentos para la Memoria del TFG
Gran parte de la memoria ya la tienes escrita en bruto, solo tienes que reubicarla:

Sección de Metodología y Fases: Tu diario_desarrollo.md es literalmente el esquema para el diagrama de Gantt de tu memoria. Tienes las 6 fases perfectamente acotadas por meses.

Sección de Arquitectura y Tecnologías: Toda la información del tecnologias_documento_tfg.md y el tono cercano del tecnologias_explicacion_personal.md te sirven para el "Desarrollo del Proyecto". Puedes explicar por qué elegiste PostgreSQL (por su solidez y los trigramas) o OSMDroid (como alternativa libre a Google Maps).

Sección de Dificultades y Soluciones: Tu documento errores_y_soluciones.md vale su peso en oro. A los tribunales les encanta leer los problemas reales y cómo los solucionaste. Mencionar el problema de la IP 10.0.2.2 del emulador, cómo cambiaste iText por OpenPDF por incompatibilidades, el rediseño a LazyColumn para evitar desbordamientos en modo horizontal, o el uso de la anotación @Async en Spring Boot para no bloquear el hilo al generar PDFs demuestra que has peleado con el código y entiendes lo que haces.

3. Lo que te falta por hacer (Siguientes pasos)
Para tener el proyecto completo y listo para entregar, te recomendaría enfocarte en esto:

Actualizar el README: Ahora mismo, tu archivo README.md contiene la plantilla genérica por defecto de GitLab. Es fundamental que lo personalices con las instrucciones reales para levantar el proyecto (comandos de Docker, configuración de la base de datos, etc.).

Estudio de Mercado / Justificación: Te falta redactar un apartado formal explicando qué apps existen (ej. apps de Seur, GLS, Amazon Flex) y por qué SLIOR es diferente o necesaria.

Diagramas: Necesitarás incluir en la memoria tu modelo Entidad-Relación de PostgreSQL y los diagramas de Casos de Uso (Administrador, Repartidor, etc.).

Presupuesto: Tienes que simular cuánto costaría el proyecto (tu sueldo por horas en esas 6 fases, licencias de servidor, coste de la base de datos en producción, etc.).

Tienes una base increíble y el lado técnico está prácticamente blindado.

¿Te gustaría que empezáramos a estructurar la parte de la justificación comercial o prefieres que calculemos una estimación de presupuesto ficticio para la memoria?

antes de eso, quiero poder añadir modo oscuro/claro a la app, editar perfil y contraseñas, mapa con todos los paquetes, listado de todos los paquetes, y buscar alguna alternativa a tailscale (no se si clouflared serviría o algo similar, intento siempre mantener approachs gratuitos y open source)

¡Me parece una decisión súper inteligente! Esas funcionalidades son justo lo que transforma un "trabajo de clase" en un producto comercial real. Además, te van a engordar muchísimo (para bien) la memoria y la presentación.

Vamos a desglosar cómo implementar esto a nivel técnico para que encaje con tu stack actual (Compose + Spring Boot) y analizamos el tema de la infraestructura:

1. Funcionalidades de la App (Jetpack Compose + Spring Boot)
🌓 Modo Oscuro/Claro:

Cómo hacerlo: En Jetpack Compose esto es un regalo. Solo tienes que definir tu paleta de colores en Theme.kt creando un darkColorScheme y un lightColorScheme. Usando la función isSystemInDarkTheme(), la app cambiará automáticamente según el sistema del móvil.

Toque Pro: Añade un botón en "Ajustes" que permita al usuario forzar el modo claro, oscuro o dejarlo en "Automático" (guardando esta preferencia en el DataStore que ya usas para la sesión).

👤 Editar Perfil y Contraseñas:

Frontend: Una nueva pantalla en Compose con campos de texto (OutlinedTextField).

Backend: Un endpoint PUT /api/users/profile y otro PUT /api/users/password.

Seguridad (Importante): Para cambiar la contraseña, obliga siempre a enviar la contraseña actual. En Spring Boot, comprueba que la actual coincide antes de usar BCrypt para encriptar la nueva. Así evitas que alguien que pille el móvil desbloqueado pueda secuestrar la cuenta.

📦 Listado de Todos los Paquetes:

Cómo hacerlo: Usa un LazyColumn en Compose.

El detalle que da el sobresaliente: Añade una barra de búsqueda (SearchBar) arriba para buscar por destinatario o código de seguimiento, y un filtro (chips) para alternar entre "Pendientes", "Entregados" y "Fallidos".

🗺️ Mapa con Todos los Paquetes:

Cómo hacerlo: Ya tienes OSMDroid configurado. Solo tienes que pedirle al backend la lista de paquetes con sus coordenadas y hacer un bucle en el mapa para añadir un Marker por cada uno.

UX: Usa iconos de colores diferentes. Por ejemplo: marcador verde neón para "entregado", naranja para "pendiente".

2. Alternativas a Tailscale (Infraestructura de Red)
Entiendo perfectamente lo que buscas. Tailscale es genial (basado en WireGuard), pero obliga a tener la app de Tailscale instalada y conectada en el móvil del repartidor, lo cual no es realista para una app en producción.

Cloudflare Tunnels (Cloudflared) es, sin duda, la mejor opción para tu caso de uso.

¿Por qué es perfecto para ti? A diferencia de una VPN como Tailscale, Cloudflare Tunnels expone tu servidor local de forma segura a Internet con un dominio público y certificado SSL (HTTPS) sin tener que abrir puertos en tu router.

Ventaja para la App: La aplicación móvil de Android no necesita saber nada de redes raras ni tener VPNs. Simplemente hace llamadas HTTPs a https://api.tu-dominio.com, y Cloudflare se encarga de rutear ese tráfico de forma segura hasta el localhost:8080 de tu ordenador donde corre Spring Boot.

Costo y Filosofía: Es 100% gratuito. Aunque Cloudflare no es totalmente Open Source, el daemon que instalas en tu servidor (cloudflared) sí lo es.

Otras alternativas por si quieres investigar:

Ngrok: Hace lo mismo que Cloudflare Tunnels, pero la versión gratuita te cambia la URL cada vez que lo reinicias. En desarrollo da igual, pero si tienes la URL de la API hardcodeada en la app compilada de Android, es un dolor de cabeza.

ZeroTier: Es exactamente el mismo concepto que Tailscale (P2P VPN). Tendrías el mismo problema de requerir una app de terceros en el móvil.

WireGuard "Puro": Es 100% libre y Open Source, pero tendrías que abrir un puerto en el router de tu casa/instituto, y sigues necesitando la app cliente en el móvil.

Veredicto: Quédate con Cloudflare Tunnels. Te va a dar una URL profesional en HTTPS (Spring Security te lo agradecerá, porque mandar JWT por HTTP sin cifrar es un riesgo) y cero dolores de cabeza en la app cliente.

¿Por qué parte del código te gustaría empezar a meter mano: preparamos primero la lógica de base de datos en Spring Boot para editar el perfil, o le damos caña al diseño del listado de paquetes en Android?