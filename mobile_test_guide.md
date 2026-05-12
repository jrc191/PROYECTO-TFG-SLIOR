# Guía de Pruebas Manuales - SLIOR Mobile App

Esta guía detalla los pasos para validar la robustez de la aplicación móvil SLIOR, enfocándose en el manejo de errores, estados offline y casos límite de la interfaz.

## 1. Escenario Offline-First (WorkManager)

### 1.1 Sincronización Exitosa
1. Abre la app y entra en una ruta activa.
2. Desactiva el WiFi y los Datos Móviles. Debería aparecer el banner naranja de **SIN CONEXIÓN**.
3. Realiza la entrega de una parada (Escanear o marcar como entregado).
4. Verifica que la parada muestra un icono de **Sync (flechas girando)**.
5. Activa el WiFi/Datos.
6. Verifica que en unos segundos el icono cambia a un **Check verde** (confirmado por el servidor).

### 1.2 Reversión por Error del Servidor (Edge Case)
1. Desactiva la conexión.
2. Realiza una entrega en la app.
3. *Simulación de error:* Desde el panel de administración o BD del backend, borra esa parada o cambia su ID.
4. Activa la conexión.
5. Verifica que la app muestra un icono de **Nube tachada (Error)** y, tras el fallo definitivo del Worker, el estado de la parada vuelve a **PENDIENTE**.

## 2. Robustez del Ciclo de Vida

### 2.1 Persistencia tras "Kill Process"
1. Desactiva la conexión.
2. Realiza 3 entregas. Verifica que están "Pendientes de sincronizar".
3. Cierra la app completamente (Swipe out) y ve a Ajustes -> Aplicaciones -> SLIOR -> **Forzar Detención**.
4. Activa la conexión.
5. Abre la app.
6. Verifica que las 3 entregas se han sincronizado automáticamente (WorkManager persiste las tareas incluso si la app se cierra).

### 2.2 Rotación y Estado
1. Entra en el mapa de una ruta.
2. Rotar el dispositivo a Landscape.
3. Verifica que la posición de las paradas y tu ubicación se mantienen correctamente.
4. Inicia la optimización de la ruta y rota la pantalla mientras carga. No debe haber crasheos.

## 3. Interfaz y Casos Límite (UI)

### 3.1 Textos Largos
1. Crea una ruta en el backend con un destinatario de nombre extremadamente largo (ej: "SANTIAGO RAMÓN Y CAJAL DE TODOS LOS SANTOS Y DE LA VIRGEN DEL PILAR").
2. Abre la ruta en el móvil.
3. Verifica que el nombre no desborda la tarjeta y se corta con puntos suspensivos o ajusta su tamaño correctamente.

### 3.2 Permisos Denegados
1. Desinstala la app o borra sus datos.
2. Ábrela y **Deniega** el permiso de ubicación.
3. Entra en el mapa. Verifica que se muestra un mensaje informativo y el mapa se centra en una vista general (España) sin cerrarse.
4. Intenta optimizar la ruta. Debería aparecer un Snackbar indicando que se necesita el permiso.

## 4. Escaneo Profesional
1. Imprime una etiqueta o ábrela en el monitor desde `backend/generated-labels/`.
2. Escanea el código de barras 128 (el alargado).
3. Verifica que la app lo reconoce al instante incluso con poca luz o inclinación.
4. Repite con el código QR. Ambos deben decodificar el mismo UUID.
