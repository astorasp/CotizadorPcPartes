# Dockerfile para ms-seguridad
# Multi-stage build que compila el código dentro del contenedor

# Etapa 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

# Información del maintainer
LABEL maintainer="QTX Development Team"
LABEL description="Microservicio de Seguridad - JWT Authentication"
LABEL version="1.0.0"

# Instalar Maven y herramientas necesarias
RUN apk add --no-cache maven curl

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Descargar dependencias (esto se cachea si el pom.xml no cambia)
RUN mvn dependency:resolve

# Copiar el código fuente
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

# Instalar dependencias adicionales necesarias
RUN apk add --no-cache \
    curl \
    tzdata \
    && rm -rf /var/cache/apk/*

# Configurar zona horaria
ENV TZ=America/Mexico_City
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Crear usuario no-root para seguridad
RUN addgroup -g 1001 seguridad && \
    adduser -D -s /bin/sh -u 1001 -G seguridad seguridad

# Crear directorios necesarios
RUN mkdir -p /app/logs /app/config && \
    chown -R seguridad:seguridad /app

# Copiar el JAR desde la etapa de build
COPY --from=builder /app/target/ms-seguridad-1.0.0.jar /app/seguridad.jar

# Cambiar ownership del archivo JAR
RUN chown seguridad:seguridad /app/seguridad.jar

# Cambiar al usuario no-root
USER seguridad

# Establecer directorio de trabajo
WORKDIR /app

# Variables de entorno con optimizaciones para contenedores
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom" \
    SPRING_PROFILES_ACTIVE=docker \
    SERVER_PORT=8081 \
    MANAGEMENT_SERVER_PORT=8091

# Exponer puertos
EXPOSE 8081 8091

# Health check usando Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar seguridad.jar"]

# Metadata adicional
LABEL org.opencontainers.image.title="ms-seguridad"
LABEL org.opencontainers.image.description="Microservicio de autenticación JWT con Spring Boot"
LABEL org.opencontainers.image.version="1.0.0"
LABEL org.opencontainers.image.vendor="QTX"
LABEL org.opencontainers.image.authors="QTX Development Team"
LABEL org.opencontainers.image.source="https://github.com/qtx/cotizador-pc-partes"