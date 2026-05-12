# Guía de Migración: De Tailscale a Cloudflare Tunnels

Esta guía detalla los pasos para migrar la infraestructura de red del proyecto SLIOR para permitir que la aplicación móvil acceda al backend de forma segura a través de Internet (HTTPS) sin necesidad de una VPN cliente como Tailscale.

## 1. Configuración en el Panel de Cloudflare

Para que este sistema funcione, necesitas un dominio gestionado en Cloudflare (puedes conseguir uno gratuito en `.pp.ua` o similares, o usar uno propio).

1.  Inicia sesión en el [Dashboard de Cloudflare](https://dash.cloudflare.com/).
2.  Ve a **Zero Trust** (en la barra lateral).
3.  Navega a **Networks** -> **Tunnels**.
4.  Haz clic en **Create a Tunnel**.
5.  Selecciona **Cloudflared** y ponle un nombre (ej. `slior-backend`).
6.  En la sección "Install and run a connector", elige **Docker**.
7.  **Copia el Token:** Verás un comando de Docker con un token largo después de `--token`. Copia solo ese token.
8.  Ve a la pestaña **Public Hostname**.
9.  Añade un hostname:
    *   **Subdomain:** `api`
    *   **Domain:** `sliorlogistics.app`
    *   **Service Type:** `HTTP`
    *   **URL:** `host.docker.internal:8080` (si usas Docker para el túnel) o `localhost:8080` (si el backend corre directamente en tu PC).

## 2. Configuración en el Proyecto

### Paso A: Variable de Entorno
Crea o edita un archivo `.env` en la raíz del proyecto (o en la carpeta `contenedores`) y añade tu token:
```env
CLOUDFLARE_TUNNEL_TOKEN=tu_token_aqui
```

### Paso B: Docker Compose
Hemos añadido el servicio `cloudflared` a `contenedores/docker-compose.yml`. Este servicio se encargará de levantar el túnel automáticamente.

### Paso C: App Móvil
Modifica el archivo `mobile-app/app/build.gradle.kts` para que la constante `BASE_URL` apunte a tu nuevo dominio:

```kotlin
buildConfigField("String", "BASE_URL", "\"https://api.sliorlogistics.app/\"")
```

## 3. Ventajas de este Cambio

1.  **HTTPS nativo:** Cloudflare gestiona los certificados SSL automáticamente.
2.  **Sin VPN:** El repartidor no necesita tener ninguna app extra instalada ni conectada.
3.  **Seguridad:** Tu IP real nunca queda expuesta a Internet.
4.  **Cero puertos abiertos:** No necesitas tocar la configuración de tu router (CGNAT friendly).
