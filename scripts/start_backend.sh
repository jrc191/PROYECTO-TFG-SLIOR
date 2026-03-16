#!/bin/bash
# ============================================================
# start_backend.sh
# Script de arranque del backend SLIOR en desarrollo
# ============================================================

echo "========================================"
echo "  SLIOR Backend - Iniciando servidor..."
echo "========================================"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"
PHOTON_DIR="$PROJECT_ROOT/photon"
PHOTON_JAR="$PHOTON_DIR/photon-1.0.1.jar"
PHOTON_PORT=2322

cd "$BACKEND_DIR" || exit 1

# Verificar que Maven está disponible
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven no encontrado. Instala Maven 3.9+ y añádelo al PATH."
    exit 1
fi

# Verificar que PostgreSQL está corriendo
echo "Verificando conexión a PostgreSQL..."
pg_isready -h localhost -p 5432 -U slior_user
if [ $? -ne 0 ]; then
    echo "AVISO: PostgreSQL no parece estar disponible en localhost:5432"
    echo "Asegúrate de que PostgreSQL está corriendo y ejecuta scripts/setup_database.sql"
fi

# Arrancar Photon si no está activo
if [ -f "$PHOTON_JAR" ] && [ -d "$PHOTON_DIR/photon_data" ]; then
    echo "Verificando servicio Photon en puerto $PHOTON_PORT..."
    if command -v curl >/dev/null 2>&1 && curl --silent --fail "http://127.0.0.1:${PHOTON_PORT}/api?q=ping" > /dev/null; then
        echo "Photon ya está escuchando en ${PHOTON_PORT}."
    else
        echo "Iniciando Photon (dump en $PHOTON_DIR/photon_data)..."
        nohup java -Xmx4G -jar "$PHOTON_JAR" serve -data-dir "$PHOTON_DIR" -listen-ip 127.0.0.1 -listen-port "$PHOTON_PORT" > "$PHOTON_DIR/photon.log" 2>&1 &
        PHOTON_PID=$!
        echo "$PHOTON_PID" > "$PHOTON_DIR/photon.pid"
        echo "Photon iniciado en segundo plano (PID $PHOTON_PID). Log: $PHOTON_DIR/photon.log"
    fi
else
    echo "AVISO: No se encontró $PHOTON_JAR o la carpeta photon_data. Skipping auto-start."
fi

# Compilar y arrancar
echo "Compilando y arrancando Spring Boot en puerto 8080..."
mvn spring-boot:run

echo "Backend detenido."
