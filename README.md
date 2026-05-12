# SLIOR - Sistema de Optimización de Rutas Logísticas

SLIOR es una solución integral para la gestión y optimización de rutas de reparto, diseñada como Proyecto de Fin de Ciclo (TFG) para el CFGS de Desarrollo de Aplicaciones Multiplataforma (DAM).

## 🚀 Características Principales

- **App Móvil Offline-First:** Desarrollada con Kotlin y Jetpack Compose, permite trabajar sin conexión y sincroniza automáticamente las entregas al detectar red.
- **Backend Robusto:** API REST con Spring Boot 3, seguridad JWT y base de datos PostgreSQL.
- **Optimización de Rutas:** Algoritmo inteligente que reordena las paradas para minimizar la distancia recorrida.
- **Logística Profesional:** Generación automática de etiquetas PDF con códigos de barras (Code 128) y QR.
- **Acceso Seguro:** Despliegue mediante Cloudflare Tunnels para acceso público HTTPS sin necesidad de VPN.

## 🛠️ Stack Tecnológico

- **Móvil:** Kotlin, Jetpack Compose, Room, Retrofit, Hilt, WorkManager, OSMDroid.
- **Backend:** Java 17, Spring Boot, Spring Security, JPA/Hibernate, Flyway.
- **Infraestructura:** Docker, PostgreSQL, Cloudflare Tunnels.

## 📦 Estructura del Proyecto

- `/backend`: Servidor de aplicaciones Spring Boot.
- `/mobile-app`: Aplicación Android nativa.
- `/contenedores`: Configuración de Docker Compose para base de datos y túneles.
- `/docs`: Documentación detallada del desarrollo y arquitectura.

## 🛠️ Instalación y Uso

1. **Configuración de Red:** Asegúrate de tener un archivo `.env` en la carpeta `contenedores/` con tu `CLOUDFLARE_TUNNEL_TOKEN`.
2. **Levantar todo el sistema:**
   ```bash
   cd contenedores
   docker-compose up -d --build
   ```
   *Esto compilará el backend, levantará la base de datos PostgreSQL, el servicio de mapas Photon y el túnel de Cloudflare.*
3. **App Móvil:** Compila e instala la app móvil en un dispositivo o emulador. La app ya está configurada para conectar con `https://api.sliorlogistics.app/`.

---
**Autor:** José Ramos Contioso
**Año:** 2026
