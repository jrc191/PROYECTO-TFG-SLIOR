#!/bin/bash
# ============================================================
# start_backend.sh
# Script de arranque del backend SLIOR en desarrollo
# ============================================================

echo "========================================"
echo "  SLIOR Backend - Iniciando servidor..."
echo "========================================"

cd "$(dirname "$0")/../backend" || exit 1

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

# Compilar y arrancar con perfil local (carga application-local.properties)
echo "Compilando y arrancando Spring Boot en puerto 8080 (perfil: local)..."
mvn spring-boot:run -Dspring-boot.run.profiles=local

echo "Backend detenido."
