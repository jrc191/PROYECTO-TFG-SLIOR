# Tecnologías - Explicación Personal

## Backend (El cerebro)
He usado **Spring Boot** porque es como el estándar de la industria. Me permite centrarme en escribir la lógica del negocio sin pelearme con la configuración del servidor. Para la base de datos elegí **PostgreSQL** porque es sólida como una roca y tiene extensiones geniales para buscar direcciones (como el soporte de Trigramas que uso para que el buscador no tenga que ser exacto).

Para que la app sea segura, implementé **JWT (JSON Web Tokens)**. Es genial porque no necesito guardar sesiones en el servidor; el token lo lleva todo y el servidor solo tiene que verificar que la firma sea mía.

## App Móvil (La cara)
Aquí he apostado por **Kotlin** y **Jetpack Compose**. Compose es una maravilla porque me permite "dibujar" la interfaz con código de forma declarativa. He seguido un estilo de diseño llamado **"Neo-Brutalismo"**: colores muy vivos (como el verde neón y el naranja de seguridad), bordes negros gruesos y sombras muy marcadas. Quería que la app no pareciera la típica app aburrida de logística, sino algo con mucha personalidad.

Una de las partes de las que más orgulloso estoy es el sistema **Offline-First**. Uso **Room** para guardar todo en el móvil y **WorkManager** para que, si el repartidor entrega un paquete en un sótano sin cobertura, la app no se bloquee. La entrega se queda "en espera" y se envía sola al servidor en cuanto el móvil detecta internet, incluso si el repartidor ha cerrado la app.

## Herramientas Útiles
Para el tema de los mapas uso **OSMDroid**. Es una alternativa libre a Google Maps que funciona muy bien. Y para las etiquetas de los paquetes, uso una librería llamada **OpenPDF** combinada con **ZXing**. Me permite generar PDFs que parecen sacados de una empresa de transporte real, con sus códigos de barras y QRs perfectamente legibles.
