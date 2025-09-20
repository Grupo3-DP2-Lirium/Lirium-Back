# ============================================================================
# DOCKERFILE PARA LIRIUM BACKEND - SPRING BOOT
# ============================================================================
# Dockerfile optimizado para Azure Container Apps con multi-stage build
# para minimizar el tamaño de la imagen final
# ============================================================================

# Etapa 1: Build - imagen completa para compilar
FROM eclipse-temurin:21-jdk-alpine AS build

# Instalar herramientas necesarias para build
RUN apk add --no-cache curl

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Gradle (layer caching)
COPY gradle/ gradle/
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .

# Dar permisos de ejecución a gradlew
RUN chmod +x gradlew

# Descargar dependencias (separado para aprovechar cache de Docker)
RUN ./gradlew dependencies --no-daemon

# Copiar código fuente
COPY src/ src/

# Construir la aplicación
RUN ./gradlew build -x test --no-daemon

# Etapa 2: Runtime - imagen mínima para ejecutar la aplicación
FROM eclipse-temurin:21-jre-alpine AS final

# Configuración para producción
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Crear directorio de trabajo
WORKDIR /app

# Instalar curl para health checks
RUN apk add --no-cache curl

# Crear usuario no-root para seguridad
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copiar la aplicación construida desde la etapa de build
COPY --from=build /app/build/libs/Lirium-Back-0.0.1-SNAPSHOT.jar app.jar

# Cambiar propiedad del archivo a appuser
RUN chown appuser:appgroup app.jar

# Cambiar a usuario no-root
USER appuser

# Exponer puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]