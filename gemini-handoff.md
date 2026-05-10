# Handoff de Sesión - SLIOR Project

## Fecha: 9 de Mayo de 2026
## Rama Actual: `feature/i18n-support`

## Resumen de Trabajo Realizado
En esta sesión se han corregido errores críticos de estabilidad y se ha implementado la infraestructura de internacionalización.

### 1. Estabilidad y Bugfixes
- **AuthViewModel NPE**: Corregido el error de inicialización de `MutableStateFlow` que causaba cierres al inicio.
- **Mapa y Rotación**: Se ha implementado persistencia de estado (`rememberSaveable`) para el centro y el zoom del mapa, evitando que se reinicie a (0,0) al girar el dispositivo.
- **Detección de Red**: Mejorado el `ConnectivityMonitor` para ser reactivo y eliminar automáticamente el banner de "Sin Conexión" al recuperar internet.

### 2. Mejoras de UI/UX en Mapas
- **Diseño Dividido**: Implementado un layout de "Pantalla Dividida" real en Detalle y Edición. El mapa es fijo (280dp) con `clipToBounds()` y `zIndex` para evitar solapamientos con la barra superior o los datos.
- **Botón de Centrado**: Añadido un botón flotante (`MyLocation`) que centra el mapa en la última parada o en el usuario.
- **Marcadores Interactivos**:
    - Todos los marcadores usan el icono de "mano".
    - El usuario es **Naranja** ("USTED ESTÁ AQUÍ"), las paradas son **Azules**.
    - Los globos de información muestran: Nombre, Dirección completa y Teléfono.
    - El teléfono es clickable y abre un **Popup de Confirmación Brutalista** antes de llamar.

### 3. Internacionalización (i18n)
- **Soporte de Idiomas**: Implementado Español (default), Inglés, Francés, Portugués y Alemán.
- **Recursos**: Todos los textos hardcoded se han movido a `strings.xml` localizados.
- **Pantalla de Ajustes**: Nueva vista accesible desde el menú lateral para cambiar el idioma en tiempo real usando `AppCompatDelegate`.

## Estado de la Rama Git
Los cambios están confirmados en la rama `feature/i18n-support`. Se realizó un commit de respaldo (`Backup`) antes de los cambios mayores y un commit final con la i18n completa.

## Tareas Pendientes / Siguiente Sesión
- [ ] Revisar posibles textos remanentes en los `ViewModels` que aún no usen recursos (algunos mensajes de error específicos).
- [ ] Validar la visualización del Popup Brutalista en diferentes tamaños de pantalla.
- [ ] Continuar con la lógica de "Estadísticas" o "Paquetes" que aparecen como "Próximamente" en el menú.

---
*Sesión guardada y lista para continuar.*
