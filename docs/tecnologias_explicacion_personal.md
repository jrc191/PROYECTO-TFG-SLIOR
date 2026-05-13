# Tecnologías - Explicación Personal

## Backend (El cerebro)
He usado **Spring Boot** porque es el estándar de la industria. Me permite centrarme en escribir la lógica del negocio sin tener que pelear con la configuración del servidor. Para la base de datos elegí **PostgreSQL** porque es sólida y tiene extensiones de gran utilidad para buscar direcciones (como el soporte de Trigramas que uso para que el buscador no tenga que ser exacto).

Para que la app sea segura, implementé **JWT (JSON Web Tokens)**. Así no necesito guardar sesiones en el servidor; el token lo lleva todo y el servidor solo tiene que verificar que la firma sea mía.

## App Móvil (La cara)
Aquí he apostado por **Kotlin** y **Jetpack Compose**. Compose es una maravilla porque me permite "dibujar" la interfaz con código. He seguido un estilo de diseño llamado **"Neo-Brutalismo"**: colores muy vivos (como el verde neón y el naranja), bordes negros gruesos y sombras muy marcadas. Quería que la app no pareciera la típica app aburrida de logística, sino algo con mucha personalidad.

Una de las partes de las que más orgulloso estoy es el sistema **Offline-First**. Uso **Room** para guardar todo en el móvil y **WorkManager** para que, si el repartidor entrega un paquete en un sótano sin cobertura, la app no se bloquee. La entrega se queda "en espera" y se envía sola al servidor en cuanto el móvil detecta internet, incluso si el repartidor ha cerrado la app.

## Infraestructura y Red
Para conectar la app con el servidor, he apostado por un despliegue basado en **Docker**. Esto me permite levantar todo el entorno (backend y base de datos) de forma idéntica en cualquier máquina. El acceso se realiza a través de la red local o mediante herramientas de red privada, asegurando que los datos sensibles no estén expuestos directamente a internet.

## Pulido Final
No quería que la app solo funcionara, quería que fuera cómoda. Por eso añadí el **Modo Oscuro**, que queda espectacular con el diseño brutalista, y soporte para **5 idiomas**. También tuve que optimizar el backend porque al principio, cuando había muchas rutas, iba un poco lento; ahora, gracias a optimizar las consultas a la base de datos, vuela.
