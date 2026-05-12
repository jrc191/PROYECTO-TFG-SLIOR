# ============================================================
# Dockerfile para SLIOR Backend
# Multi-stage build: Maven para compilar y OpenJDK para ejecutar
# ============================================================

# --- Fase 1: Compilación ---
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiar el POM y descargar dependencias (para aprovechar la caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar el JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Fase 2: Ejecución ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear directorio para etiquetas y logs con permisos
RUN mkdir -p generated-labels logs && chmod 777 generated-labels logs

# Copiar el JAR desde la fase de compilación
COPY --from=build /app/target/slior-backend-*.jar app.jar

# Variables de entorno por defecto (se pueden sobrescribir en docker-compose)
ENV SERVER_PORT=8080
ENV SPRING_PROFILES_ACTIVE=docker
ENV JWT_SECRET=5368566D597133743677397A244226452948404D635166546A576E5A72347537

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
