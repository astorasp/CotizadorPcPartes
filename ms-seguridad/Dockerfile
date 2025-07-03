# Dockerfile para ms-seguridad
# Utiliza multi-stage build para optimizar el tamaño de la imagen

# Etapa 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

# Información del maintainer
LABEL maintainer="QTX Development Team"
LABEL description="Microservicio de Seguridad - JWT Authentication"
LABEL version="1.0.0"

# Variables de build
ARG JAR_FILE=target/ms-seguridad-1.0.0.jar

# Crear directorio de trabajo
WORKDIR /build

# Copiar el JAR de la aplicación
COPY ${JAR_FILE} app.jar

# Extraer las capas del JAR para optimización
RUN java -Djarmode=layertools -jar app.jar extract

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

# Cambiar al usuario no-root
USER seguridad

# Establecer directorio de trabajo
WORKDIR /app

# Copiar las capas extraídas desde el builder
COPY --from=builder --chown=seguridad:seguridad /build/dependencies/ ./
COPY --from=builder --chown=seguridad:seguridad /build/spring-boot-loader/ ./
COPY --from=builder --chown=seguridad:seguridad /build/snapshot-dependencies/ ./
COPY --from=builder --chown=seguridad:seguridad /build/application/ ./

# Variables de entorno
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseStringDeduplication" \
    SPRING_PROFILES_ACTIVE=docker \
    SERVER_PORT=8081 \
    MANAGEMENT_SERVER_PORT=8091

# Exponer puertos
EXPOSE 8081 8091

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Punto de entrada
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]

# Metadata adicional
LABEL org.opencontainers.image.title="ms-seguridad"
LABEL org.opencontainers.image.description="Microservicio de autenticación JWT con Spring Boot"
LABEL org.opencontainers.image.version="1.0.0"
LABEL org.opencontainers.image.vendor="QTX"
LABEL org.opencontainers.image.authors="QTX Development Team"
LABEL org.opencontainers.image.source="https://github.com/qtx/cotizador-pc-partes"