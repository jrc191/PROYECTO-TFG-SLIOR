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

1. Levantar la infraestructura:
   ```bash
   cd contenedores
   docker-compose up -d
   ```
2. Ejecutar el backend desde tu IDE favorito (Puerto 8080).
3. Compilar e instalar la app móvil en un dispositivo o emulador.

---
**Autor:** José Ramos Contioso
**Año:** 2026
