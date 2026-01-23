# 🖥️ Microservicio Cotizador (Backend API)

> **API REST empresarial** para cotización de componentes de hardware con arquitectura Domain-Driven Design, desarrollado con Spring Boot 3.5.0 y Java 21.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.4.4-blue.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

## 📌 Navegación del Proyecto

- **📖 [README Principal](../README.md)** - Documentación completa del sistema
- **🚀 [Portal Web](../portal-cotizador/README.md)** - Frontend Vue.js 3
- **🔒 [Seguridad](../ms-seguridad/)** - Microservicio de autenticación
- **🌐 [API Gateway](../nginx-gateway/README.md)** - Gateway Nginx
- **📚 [Documentación](../documentacion/)** - Diagramas y arquitectura

---

## 🎯 Descripción del Microservicio

Sistema empresarial para la **cotización de componentes de hardware**, **generación de pedidos** y **gestión de inventario** con arquitectura Domain-Driven Design (DDD) y patrones de diseño avanzados.

### 🏗️ **Arquitectura Domain-Driven Design**

El microservicio implementa DDD con separación clara de responsabilidades:

```
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │   Core      │ │  Pedidos    │ │ Promociones │           │
│  │(Componentes)│ │             │ │             │           │
│  │ Cotización  │ │ Proveedor   │ │ Impuestos   │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                  Application Layer                          │
│         ┌─────────────┐ ┌─────────────┐                    │
│         │  Servicios  │ │    DTOs     │                    │
│         │ (Casos Uso) │ │  Mappers    │                    │
│         └─────────────┘ └─────────────┘                    │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                Infrastructure Layer                         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │Controllers  │ │Repositories │ │Configuration│           │
│  │  (API)      │ │   (JPA)     │ │  (Spring)   │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Inicio Rápido

### ⚡ **Con Docker (Recomendado)**

```bash
# Desde la raíz del proyecto
docker-compose up -d

# Verificar que el servicio esté funcionando
curl http://localhost:8084/api/v1/actuator/health
```

### 🛠️ **Desarrollo Local**

#### **Prerrequisitos**
- Java 21+ (OpenJDK recomendado)
- Maven 3.8+
- MySQL 8.0+

#### **Setup**
```bash
cd ms-cotizador-pedidos

# 1. Configurar base de datos MySQL
mysql -u root -p
CREATE DATABASE cotizador_pedidos_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'pedidos_user'@'localhost' IDENTIFIED BY 'pedidos_pass';
GRANT ALL PRIVILEGES ON cotizador_pedidos_db.* TO 'pedidos_user'@'localhost';

# 2. Ejecutar scripts de base de datos
mysql -u pedidos_user -p cotizador_pedidos_db < sql/ddl.sql
mysql -u pedidos_user -p cotizador_pedidos_db < sql/dml.sql

# 3. Compilar y ejecutar
mvn spring-boot:run
```

### 🌐 **Acceso a los Servicios**

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| **API REST (dev)** | http://localhost:8084/api/v1 | Basic Auth (admin/admin123) |
| **Swagger UI (dev)** | http://localhost:8084/api/v1/swagger-ui/index.html | Basic Auth |
| **Health Check (dev)** | http://localhost:8084/api/v1/actuator/health | - |
| **Base de Datos (dev)** | localhost:3306/cotizador_pedidos_db | pedidos_user / pedidos_pass |

---

## 📋 Funcionalidades del Microservicio

### 🎯 **Funcionalidades Principales**

#### **🔧 Gestión de Componentes**
- **CRUD completo** de componentes de hardware
- **Tipos soportados**: Monitores, Discos Duros, Tarjetas de Video, PCs ensambladas
- **Validaciones de negocio** automáticas
- **Integración con proveedores**

#### **🖥️ Sistema de Armado de PCs**  
- **Construcción de PCs** con patrón Builder
- **Validación de compatibilidad** de componentes
- **Reglas de negocio**: máximo 2 monitores, 1-2 tarjetas gráficas, 1-3 discos duros
- **Cálculo automático** de precios totales

#### **📋 Sistema de Cotizaciones**
- **Generación de cotizaciones** detalladas con múltiples estrategias
- **Cálculo automático** de totales e impuestos
- **Aplicación de promociones** flexible y acumulable
- **Soporte multi-país** para esquemas impositivos

#### **🎁 Sistema de Promociones**
- **Tipos flexibles**: Descuentos planos, por cantidad, ofertas N×M
- **Promociones acumulables** con patrón Decorator
- **Aplicación automática** según reglas de negocio
- **Cálculos financieros** en tiempo real

#### **📦 Gestión de Pedidos**
- **Conversión automática** de cotizaciones a pedidos
- **Asignación a proveedores** específicos
- **Gestión de fechas** de emisión y entrega
- **Seguimiento de estados** del pedido

#### **🏢 Gestión de Proveedores**
- **CRUD completo** de proveedores
- **Asignación de componentes** por proveedor
- **Gestión de precios** diferenciados
- **Estados activo/inactivo**

### 🔄 **Características Técnicas**

- **Persistencia completa** con JPA/Hibernate
- **Validación robusta** de reglas de negocio
- **Manejo de excepciones** personalizado
- **Arquitectura extensible** para nuevos tipos de componentes
- **Logs estructurados** para monitoreo
- **Health checks** integrados

---

## 🏗️ Arquitectura Técnica

### 📦 **Estructura de Paquetes**

```
mx.com.qtx.cotizador/
├── 📁 dominio/                     # Domain Layer (DDD)
│   ├── 📁 core/                    # Core Business Logic
│   │   ├── Cotizacion.java         # Aggregate Root - Cotizaciones
│   │   ├── DetalleCotizacion.java  # Value Object - Detalle
│   │   ├── ICotizador.java         # Domain Service Interface
│   │   └── 📁 componentes/         # Component Domain
│   │       ├── Componente.java     # Base Component Entity
│   │       ├── Monitor.java        # Specific Components
│   │       ├── DiscoDuro.java
│   │       ├── TarjetaVideo.java
│   │       ├── Pc.java             # Composite Component
│   │       └── PcBuilder.java      # Builder Pattern
│   ├── 📁 cotizadorA/              # Strategy A Implementation
│   ├── 📁 cotizadorB/              # Strategy B Implementation  
│   ├── 📁 promos/                  # Promotion Domain
│   │   ├── Promocion.java          # Base Promotion
│   │   ├── PromDsctoPlano.java     # Flat Discount
│   │   ├── PromDsctoXcantidad.java # Quantity Discount
│   │   └── PromocionBuilder.java   # Builder Pattern
│   ├── 📁 pedidos/                 # Order Domain
│   │   ├── Pedido.java             # Order Aggregate
│   │   ├── GestorPedidos.java      # Domain Service
│   │   └── Proveedor.java          # Supplier Entity
│   └── 📁 impuestos/               # Tax Domain
│       ├── CalculadorImpuesto.java # Tax Calculator
│       ├── CalculadorImpuestoMexico.java
│       ├── CalculadorImpuestosUsa.java
│       └── CalculadorImpuestosCanada.java
├── 📁 aplicacion/                  # Application Layer
│   ├── 📁 servicio/                # Application Services
│   │   ├── componente/
│   │   ├── cotizacion/
│   │   ├── pedido/
│   │   └── promocion/
│   └── 📁 dto/                     # Data Transfer Objects
│       ├── 📁 request/             # Request DTOs
│       ├── 📁 response/            # Response DTOs
│       └── 📁 mapper/              # Entity-DTO Mappers
├── 📁 infraestructura/             # Infrastructure Layer
│   ├── 📁 controlador/             # REST Controllers
│   │   ├── ComponenteController.java
│   │   ├── CotizacionController.java
│   │   ├── PcController.java
│   │   ├── PedidoController.java
│   │   ├── ProveedorController.java
│   │   └── PromocionControlador.java
│   ├── 📁 repositorio/             # JPA Repositories
│   │   ├── ComponenteRepositorio.java
│   │   ├── CotizacionRepositorio.java
│   │   ├── PedidoRepositorio.java
│   │   ├── ProveedorRepositorio.java
│   │   └── PromocionRepositorio.java
│   └── 📁 configuracion/           # Spring Configuration
│       ├── JpaConfig.java
│       └── SecurityConfig.java
├── 📁 entidad/                     # JPA Entities
├── 📁 excepcion/                   # Exception Handling
└── 📁 util/                        # Utilities
```

### 🔧 **Patrones de Diseño Implementados**

#### **Domain Patterns**
1. **Domain-Driven Design**: Separación clara de capas de dominio, aplicación e infraestructura
2. **Aggregate Pattern**: `Cotizacion` como Aggregate Root con `DetalleCotizacion`
3. **Value Objects**: `DetalleCotizacion` para encapsular datos sin identidad
4. **Domain Services**: `ICotizador`, `GestorPedidos` para lógica de dominio compleja

#### **Creational Patterns**
1. **Builder Pattern**: `PcBuilder` para construcción fluida de PCs con validaciones
2. **Factory Method**: Métodos estáticos en `Componente` para crear tipos específicos

#### **Structural Patterns**
1. **Adapter Pattern**: `CotizacionPresupuestoAdapter` para convertir Cotización → IPresupuesto
2. **Bridge Pattern**: Sistema de impuestos separando abstracción de implementación
3. **Composite Pattern**: `Pc` como componente compuesto conteniendo otros componentes

#### **Behavioral Patterns**
1. **Strategy Pattern**: `ICotizador` con implementaciones `CotizadorA` y `CotizadorB`
2. **Decorator Pattern**: Sistema de promociones acumulables `PromAcumulable`
3. **Repository Pattern**: Interfaces de repositorio para cada aggregate

---

## 📚 API REST Endpoints

### 🔗 **Componentes**

```http
GET    /cotizador/v1/api/componentes           # Listar componentes
POST   /cotizador/v1/api/componentes           # Crear componente  
GET    /cotizador/v1/api/componentes/{id}      # Obtener por ID
PUT    /cotizador/v1/api/componentes/{id}      # Actualizar componente
DELETE /cotizador/v1/api/componentes/{id}      # Eliminar componente
GET    /cotizador/v1/api/componentes/tipos     # Listar tipos disponibles
```

### 🖥️ **PCs**

```http
GET    /cotizador/v1/api/pcs                   # Listar PCs
POST   /cotizador/v1/api/pcs                   # Crear PC
GET    /cotizador/v1/api/pcs/{id}              # Obtener PC por ID
PUT    /cotizador/v1/api/pcs/{id}              # Actualizar PC
DELETE /cotizador/v1/api/pcs/{id}              # Eliminar PC
POST   /cotizador/v1/api/pcs/{id}/componentes  # Agregar componente a PC
DELETE /cotizador/v1/api/pcs/{id}/componentes/{componenteId} # Remover componente
GET    /cotizador/v1/api/pcs/{id}/componentes  # Listar componentes de PC
```

### 📦 **Pedidos**

```http
GET    /api/v1/pedidos                          # Listar pedidos
POST   /api/v1/pedidos/generar                  # Generar pedido desde cotización
GET    /api/v1/pedidos/{id}                     # Obtener pedido por ID
PUT    /api/v1/pedidos/{id}                     # Actualizar pedido
```

### 🏢 **Proveedores**

```http
GET    /api/v1/proveedores                      # Listar proveedores
POST   /api/v1/proveedores                      # Crear proveedor
GET    /api/v1/proveedores/{id}                 # Obtener proveedor
PUT    /api/v1/proveedores/{id}                 # Actualizar proveedor
DELETE /api/v1/proveedores/{id}                 # Eliminar proveedor
```

### 🎁 **Promociones**

```http
GET    /cotizador/v1/api/promociones           # Listar promociones
POST   /cotizador/v1/api/promociones           # Crear promoción
GET    /cotizador/v1/api/promociones/{id}      # Obtener promoción
PUT    /cotizador/v1/api/promociones/{id}      # Actualizar promoción
DELETE /cotizador/v1/api/promociones/{id}      # Eliminar promoción
```

### 📖 **Documentación API**

- **Swagger UI (dev)**: http://localhost:8084/api/v1/swagger-ui/index.html
- **OpenAPI JSON (dev)**: http://localhost:8084/api/v1/v3/api-docs
- **Health Check (dev)**: http://localhost:8084/api/v1/actuator/health

---

## 🛠️ Tecnologías Utilizadas

### ☕ **Core Technologies**
- **Java 21** - Lenguaje de programación con características modernas
- **Spring Boot 3.5.0** - Framework principal con auto-configuración
- **Maven 3.8+** - Gestión de dependencias y construcción

### 🗃️ **Persistencia y Base de Datos**
- **Spring Data JPA** - Capa de acceso a datos
- **Hibernate 6.6.14** - ORM para mapeo objeto-relacional
- **MySQL 8.4.4** - Base de datos principal
- **HikariCP** - Connection pooling optimizado

### 🌐 **Web y API**
- **Spring Web MVC** - Framework web para API REST
- **Spring Security** - Autenticación y autorización (Basic Auth)
- **Jackson** - Serialización/deserialización JSON
- **Swagger/OpenAPI 3** - Documentación automática de API

### 🔧 **Infraestructura y Monitoreo**
- **Spring Boot Actuator** - Monitoreo y métricas de salud
- **Logback** - Sistema de logging estructurado
- **Docker** - Containerización
- **Spring Boot DevTools** - Hot reload en desarrollo

### 🧪 **Testing**
- **JUnit 5** - Framework de testing principal
- **TestContainers** - Tests de integración con MySQL real
- **REST Assured** - Testing de APIs REST
- **Mockito** - Mocking para unit tests

---

## ⚙️ Configuración

### 🗂️ **Perfiles de Configuración**

| Perfil | Archivo | Uso |
|--------|---------|-----|
| `default` | `application.yml` | Desarrollo local |
| `docker` | `application-docker.yml` | Contenedores Docker |
| `test` | `application-test.properties` | Tests de integración |

### 🔧 **Variables de Entorno (dev)**

```bash
# Base de datos
DB_HOST=localhost                   # Host de MySQL
DB_USERNAME=pedidos_user            # Usuario de base de datos
DB_PASSWORD=pedidos_pass            # Contraseña de base de datos

# Seguridad
SECURITY_USERNAME=admin             # Usuario API básica
SECURITY_PASSWORD=admin123          # Contraseña API básica

# Aplicación
SERVER_PORT=8084                    # Puerto del servidor (dev)
LOGGING_LEVEL=INFO                  # Nivel de logging
```

### 📊 **Configuración de Base de Datos**

```yaml
# application.yml (perfil dev)
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:3306/cotizador_pedidos_db
    username: ${DB_USERNAME:pedidos_user}
    password: ${DB_PASSWORD:pedidos_pass}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true

server:
  port: 8084
  servlet:
    context-path: /api/v1
```

---

## 🧪 Testing

### 🔬 **Suite de Tests Completa**

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar solo tests de integración
mvn test -Dtest="*IntegrationTest"

# Ejecutar tests con reporte de cobertura
mvn test jacoco:report

# Ejecutar un test específico
mvn test -Dtest=ComponenteIntegrationTest
```

### 🐳 **Tests de Integración con TestContainers**

Los tests utilizan **TestContainers** para crear un entorno real de MySQL:

| Test Suite | Cobertura | Estado |
|------------|-----------|--------|
| `ComponenteIntegrationTest` | CRUD Componentes + Validaciones | ✅ |
| `ProveedorIntegrationTest` | CRUD Proveedores + Búsquedas | ✅ |
| `PromocionIntegrationTest` | CRUD Promociones + Cálculos | ✅ |
| `PcIntegrationTest` | Armado PCs + Validaciones | ✅ |
| `CotizacionIntegrationTest` | Cotizaciones + Impuestos | ✅ |
| `PedidoIntegrationTest` | Gestión Pedidos + Conversión | ✅ |

### 📋 **Configuración de Tests**

```properties
# application-test.properties
spring.datasource.url=jdbc:tc:mysql:8.4.4:///cotizador
spring.jpa.hibernate.ddl-auto=create-drop
logging.level.org.testcontainers=INFO
logging.level.com.github.dockerjava=WARN
```

---

## 📊 Ejemplo de Uso

### 🔧 **Crear Componente**

```java
// POST /cotizador/v1/api/componentes
{
  "id": "M001",
  "descripcion": "Monitor 27 pulgadas 4K",
  "marca": "LG",
  "modelo": "27UL550",
  "costo": 3000.00,
  "precioBase": 5000.00,
  "tipoComponente": "MONITOR"
}
```

### 🖥️ **Armar PC**

```java
// POST /cotizador/v1/api/pcs
{
  "nombre": "PC Gaming Pro",
  "descripcion": "PC para gaming de alta gama",
  "componentes": [
    {
      "componenteId": "M001", 
      "cantidad": 2
    },
    {
      "componenteId": "D001", 
      "cantidad": 1
    }
  ]
}
```

### 📋 **Generar Cotización**

```java
// POST /cotizador/v1/api/cotizaciones
{
  "cliente": "Juan Pérez",
  "detalles": [
    {
      "componenteId": "PC001",
      "cantidad": 1,
      "precio": 15000.00
    }
  ],
  "pais": "MEXICO"
}
```

### 📦 **Generar Pedido**

```java
// POST /cotizador/v1/api/pedidos/generar
{
  "cotizacionId": 1,
  "proveedorId": "PROV001",
  "fechaEntrega": "2024-12-15",
  "nivelSurtido": 1
}
```

---

## 🐳 Docker y Deployment

### 🏗️ **Construcción de Imagen**

```dockerfile
# Dockerfile (multi-stage)
FROM eclipse-temurin:21.0.7_6-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN apk add --no-cache maven curl && mvn dependency:resolve
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21.0.7_6-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring && mkdir -p /app/logs && chown -R spring:spring /app
USER spring
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/pedidos.jar
EXPOSE 8084
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar pedidos.jar"]
```

### 🚀 **Comandos Docker**

```bash
# Construir imagen
docker build -t ms-cotizador-pedidos ./ms-cotizador-pedidos

# Ejecutar contenedor (dev)
docker run -p 8084:8084 \
  -e DB_HOST=localhost \
  -e DB_USERNAME=pedidos_user \
  -e DB_PASSWORD=pedidos_pass \
  -e SPRING_PROFILES_ACTIVE=default \
  ms-cotizador-pedidos

# Con Docker Compose (recomendado)
docker-compose up -d
```

### 📊 **Health Checks**

```bash
# Verificar salud del servicio
curl http://localhost:8084/api/v1/actuator/health

# Información detallada
curl http://localhost:8080/actuator/info

# Métricas
curl http://localhost:8080/actuator/metrics
```

---

## 🔧 Scripts Disponibles

```bash
# Compilación
mvn clean compile              # Compilar código fuente
mvn clean package              # Generar JAR
mvn clean install              # Instalar en repositorio local

# Ejecución
mvn spring-boot:run            # Ejecutar en modo desarrollo
java -jar target/ms-cotizador-*.jar  # Ejecutar JAR

# Testing
mvn test                       # Ejecutar tests
mvn integration-test           # Tests de integración
mvn verify                     # Verificar calidad

# Análisis
mvn jacoco:report              # Reporte de cobertura
mvn dependency:tree            # Árbol de dependencias
```

---

## 🆘 Troubleshooting

### ❌ **Problemas Comunes**

| Problema | Causa | Solución |
|----------|-------|----------|
| **Puerto 8080 en uso** | Otro proceso usando el puerto | `lsof -i :8080` y matar proceso |
| **Error de conexión DB** | MySQL no disponible | Verificar `docker-compose ps` |
| **Tests fallan** | TestContainers no puede iniciar | Verificar Docker funcionando |
| **Error 401 en API** | Credenciales incorrectas | Verificar Basic Auth (admin/admin123) |

### 🔧 **Comandos de Diagnóstico**

```bash
# Verificar estado del servicio
curl http://localhost:8080/actuator/health

# Ver logs del contenedor
docker logs ms-cotizador-pedidos

# Verificar conexión a base de datos
mysql -h localhost -u pedidos_user -p cotizador_pedidos_db

# Verificar endpoints disponibles
curl -u admin:admin123 http://localhost:8080/cotizador/v1/api/componentes
```

---

## 🤝 Contribución

### 📝 **Convenciones de Código**

#### **Java**
- **Clases**: PascalCase (`ComponenteController`)
- **Métodos**: camelCase (`crearComponente`)
- **Constantes**: UPPER_SNAKE_CASE (`MAX_COMPONENTES_PC`)
- **Packages**: lowercase (`mx.com.qtx.cotizador.dominio`)

#### **Tests**
- **Naming**: Descriptivo con patrón Given-When-Then
- **Organization**: Un test class por clase de producción
- **Data**: Usar builders o factories para datos de test

### 🔄 **Git Workflow**

```bash
# Feature branch desde develop
git checkout -b feature/nueva-funcionalidad

# Commits descriptivos
git commit -m "feat(componentes): agregar validación de stock"

# Pull request con tests
# Incluir documentación de API si es necesario
```

---

## 📞 Soporte

### 🔗 **Enlaces Útiles**

- **📖 [Documentación Principal](../README.md)** - Guía completa del sistema
- **🚀 [Portal Web](../portal-cotizador/README.md)** - Frontend Vue.js 3
- **🌐 [API Gateway](../nginx-gateway/README.md)** - Configuración del gateway
- **📚 [Spring Boot Docs](https://spring.io/projects/spring-boot)** - Documentación oficial
- **🐳 [Docker Docs](https://docs.docker.com/)** - Guías de Docker

### 🆘 **Soporte Técnico**

- **Issues**: Crear issue en GitHub
- **API Docs**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Logs**: `docker logs ms-cotizador`

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [LICENSE](../LICENSE) para más detalles.

---

<div align="center">

**🖥️ Microservicio Cotizador**

*API REST empresarial con Domain-Driven Design*

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.4.4-blue.svg)](https://www.mysql.com/)

**[⬆️ Volver al README Principal](../README.md)**

</div>