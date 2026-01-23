# Usa Eclipse Temurin 21 con Alpine para build
FROM eclipse-temurin:21.0.7_6-jdk-alpine AS builder

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


# ================================================
# Etapa final - imagen más ligera para producción
FROM eclipse-temurin:21.0.7_6-jre-alpine

# Instalar herramientas necesarias para el contenedor
RUN apk add --no-cache curl tzdata

# Crear usuario no-root para seguridad (sintaxis Alpine)
RUN addgroup -S spring && adduser -S spring -G spring

# Crear directorio para logs
RUN mkdir -p /app/logs && \
    chown -R spring:spring /app

# Configurar zona horaria
ENV TZ=America/Mexico_City
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Copiar el JAR desde la etapa de build
COPY --from=builder /app/target/*.jar /app/componentes.jar

# Cambiar ownership del archivo JAR
RUN chown spring:spring /app/componentes.jar

# Cambiar al usuario no-root
USER spring

# Establecer directorio de trabajo
WORKDIR /app

# Exponer puerto
EXPOSE 8082

# Variables de entorno por defecto con optimizaciones para contenedores
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom" \
    DB_HOST=mysql-componentes \
    DB_PORT=3306 \
    DB_USERNAME=componentes_user \
    DB_PASSWORD=componentes_pass \
    SECURITY_USERNAME=admin \
    SECURITY_PASSWORD=admin123 \
    SPRING_PROFILES_ACTIVE=docker

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar componentes.jar"]

# Health check usando Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=15s --start-period=120s --retries=5 \
    CMD curl -f http://localhost:8082/actuator/health || exit 1 