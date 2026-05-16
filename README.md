# SLIOR — Sistema Logístico Inteligente de Optimización de Rutas

SLIOR es un sistema integral de gestión y optimización de rutas de reparto, diseñado como Proyecto de Fin de Ciclo (TFG) para el CFGS de Desarrollo de Aplicaciones Multiplataforma (DAM). Proporciona una solución profesional para pequeñas empresas y autónomos del sector logístico de última milla.

## 📝 Resumen del Proyecto

SLIOR permite a los repartidores gestionar sus rutas de entrega de manera eficiente, optimizando el orden de las paradas mediante el algoritmo del vecino más cercano con cálculo real de distancias. El sistema destaca por su arquitectura **offline-first**, que garantiza la operatividad en zonas sin cobertura, y su cumplimiento íntegro del **RGPD**.

## 🚀 Características Principales

- **App Móvil Offline-First:** Desarrollada con Kotlin y Jetpack Compose, permite trabajar sin conexión y sincroniza automáticamente las entregas al recuperar red mediante WorkManager.
- **Optimización de Rutas:** Algoritmo inteligente que reordena las paradas para minimizar la distancia recorrida utilizando OSRM (Open Source Routing Machine).
- **Logística Profesional:** Generación automática de etiquetas PDF (A6) con códigos de barras (Code 128) y QR para cada parada.
- **Geocodificación Híbrida:** Sistema de búsqueda de direcciones en tres niveles (caché en memoria, base de datos local PostgreSQL con índices trigram y geocodificador externo).
- **Seguridad y Privacidad:** Autenticación JWT stateless, rate limiting por IP, auditoría de acciones sensibles y herramientas para el ejercicio de derechos ARSULIPO (RGPD).
- **Multilingüe:** Interfaz disponible en 5 idiomas (ES, EN, FR, PT, DE).
- **Estética Neo-Brutalista:** Diseño de alta visibilidad y contraste para facilitar el uso en entornos de campo.

## 🛠️ Stack Tecnológico

### Backend
- **Lenguaje:** Java 17 (LTS).
- **Framework:** Spring Boot 3.2.4.
- **Seguridad:** Spring Security 6 (JWT).
- **Persistencia:** PostgreSQL 14, Hibernate, Flyway.
- **Utilidades:** OpenPDF, ZXing, Bucket4j.

### Móvil (Android)
- **Lenguaje:** Kotlin 2.0.21.
- **UI:** Jetpack Compose (Material 3).
- **Arquitectura:** MVVM + Clean Architecture.
- **Persistencia Local:** Room.
- **Inyección de Dependencias:** Hilt.
- **Red:** Retrofit 2, OkHttp 4.
- **Mapas:** OSMDroid.

### Infraestructura
- **Contenedores:** Docker & Docker Compose.
- **Red:** Cloudflare Tunnels (para acceso público seguro).

## 📦 Estructura del Proyecto

- `/backend`: Servidor de aplicaciones Spring Boot.
- `/mobile-app`: Aplicación Android nativa.
- `/contenedores`: Configuración de Docker Compose y orquestación.
- `/docs`: Documentación técnica, manuales y diario de desarrollo.

## 🛠️ Instalación y Despliegue

### Requisitos previos
- Docker Engine 24.x+ y Docker Compose 2.x+.
- Android 8.0 (API 26) o superior para la app móvil.

### Pasos para el Backend
1. Clonar el repositorio.
2. Crear un archivo `.env` en la raíz con las variables:
   ```env
   DB_PASSWORD=tu_contraseña_segura
   JWT_SECRET=tu_clave_jwt_256_bits
   CLOUDFLARE_TUNNEL_TOKEN=tu_token_opcional
   ```
3. Levantar los servicios:
   ```bash
   cd contenedores
   docker-compose up -d --build
   ```

### Pasos para la App Móvil
1. Instalar el APK en el dispositivo Android.
2. Al iniciar por primera vez, configurar la URL del servidor (ej. `https://api.tuservidor.com` o la IP local).
3. Registrar el primer usuario con rol **ADMINISTRADOR**.

## 📄 Licencia

Este proyecto está bajo la Licencia **MIT**. Consulta el archivo `LICENSE` (si existe) o el Anexo B de la memoria para más detalles.

---
**Autor:** José Ramos Contioso  
**Centro:** I.E.S. La Marisma (Huelva)  
**Año:** 2026
