# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CotizadorPcPartes is a comprehensive enterprise-level microservices system for PC hardware component quotation, order management, and inventory control. The system implements Domain-Driven Design with Spring Boot 3.5.3, Java 21, MySQL 8.4.4, and JWT-based authentication across multiple services.

## Technology Stack

- **Backend**: Spring Boot 3.5.3, Java 21, Spring Data JPA, Spring Security with JWT
- **Database**: MySQL 8.4.4 with HikariCP connection pooling (separate DBs per service)
- **Frontend**: Vue.js 3, Vite, TailwindCSS, Pinia stores
- **Authentication**: JWT with RS256 signing, JWKS endpoints, session management
- **Gateway**: Nginx API Gateway for service routing
- **Testing**: JUnit 5, TestContainers, REST Assured
- **DevOps**: Docker Compose, Spring Boot Actuator, health checks
- **Build**: Maven 3.8+ (backend), npm/Vite (frontend)

## Common Commands

### Development Commands
```bash
# Build and run ms-cotizador locally
cd ms-cotizador
mvn spring-boot:run

# Build and run ms-seguridad locally
cd ms-seguridad
mvn spring-boot:run

# Build and run frontend locally
cd portal-cotizador
npm install
npm run dev

# Run all tests (specific microservice)
cd ms-cotizador && mvn test
cd ms-seguridad && mvn test

# Run integration tests only (in specific order)
mvn test -Dtest="*IntegrationTest"

# Run with coverage report
mvn test jacoco:report

# Frontend build commands
npm run build     # Production build
npm run lint      # ESLint check
npm run format    # Prettier formatting
```

### Environment Setup Commands
```bash
# Initialize environment files (Linux/macOS)
./init-env.sh

# Initialize environment files (Windows PowerShell)
./init-env.ps1

# Initialize with auto-generated passwords (PowerShell)
./init-env.ps1 -AutoGeneratePasswords

# Force overwrite existing files (PowerShell)
./init-env.ps1 -Force
```

### Docker Commands
```bash
# Start entire system
docker-compose up -d

# Use management script
./docker-scripts.sh start     # Start system
./docker-scripts.sh stop      # Stop system
./docker-scripts.sh logs      # View logs
./docker-scripts.sh status    # Check service status
./docker-scripts.sh health    # Check health endpoints
./docker-scripts.sh clean     # Clean containers and volumes
```

### Database Commands
```bash
# Access MySQL Cotizador container
docker exec -it cotizador-mysql mysql -u cotizador_user -p cotizador

# Access MySQL Seguridad container  
docker exec -it seguridad-mysql mysql -u seguridad_user -p seguridad

# Re-initialize databases
./docker-scripts.sh db-init

# Service-specific database access via script
./docker-scripts.sh shell-mysql           # Cotizador DB
./docker-scripts.sh shell-mysql-seguridad # Seguridad DB
```

## Architecture

### Microservices Architecture
The system implements a microservices architecture with the following services:

#### **ms-seguridad** (Security Microservice)
- **Purpose**: Centralized JWT authentication, RBAC authorization, session management
- **Domain**: Simple entities (`Usuario`, `Rol`, `RolAsignado`, `Acceso`)
- **Key Features**: JWT with RS256, JWKS endpoints, session tracking, rate limiting

#### **ms-cotizador** (Quotation Microservice) - Domain-Driven Design
- **Purpose**: Core business logic for PC quotation, order management, inventory
- **Architecture**: Rich domain models with embedded business logic

### Domain Layer Structure (ms-cotizador)
The cotizador service follows Domain-Driven Design with rich domain models:

- **Core Domain** (`dominio.core`): Main business entities (`Cotizacion`, `DetalleCotizacion`)
- **Components** (`dominio.core.componentes`): Hardware component hierarchy with `Componente` base class
- **PC Building** (`dominio.core.componentes.Pc`): Composite pattern for PC assembly with `PcBuilder`
- **Quotations** (`dominio.cotizadorA`, `dominio.cotizadorB`): Strategy pattern for different quotation algorithms
- **Promotions** (`dominio.promos`): Decorator pattern for stackable discounts and promotions
- **Orders** (`dominio.pedidos`): Order management with `GestorPedidos` domain service
- **Taxes** (`dominio.impuestos`): Bridge pattern for multi-country tax calculations

### Key Design Patterns

1. **Strategy Pattern**: `ICotizador` interface with `CotizadorA` and `CotizadorB` implementations
2. **Builder Pattern**: `PcBuilder` for PC assembly with business rule validation
3. **Decorator Pattern**: Stackable promotions (`PromDsctoPlano`, `PromDsctoXcantidad`)
4. **Bridge Pattern**: Tax calculation system with country-specific implementations
5. **Composite Pattern**: `Pc` containing multiple `Componente` objects
6. **Adapter Pattern**: `CotizacionPresupuestoAdapter` for domain-to-persistence conversion

### Service Layer & Authentication
**ms-cotizador Services** orchestrate domain objects rather than containing business logic:
- Transaction management with `@Transactional`
- Standardized `ApiResponse<T>` wrapper for all endpoints
- Domain-first approach with minimal service logic
- JWT token validation via JWKS from ms-seguridad

**ms-seguridad Services** handle authentication and authorization:
- JWT token generation and validation using RS256
- Session management with automatic cleanup
- Role-based access control (RBAC) enforcement
- Rate limiting and security monitoring

### Testing Architecture
**TestContainers Integration**: All integration tests use a shared MySQL 8.4.4 container managed by `BaseIntegrationTest`:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
```

**Test Execution Order**: Tests run in specific sequence for data consistency:
1. `ComponenteIntegrationTest`
2. `ProveedorIntegrationTest` 
3. `PcIntegrationTest`
4. `CotizacionIntegrationTest`
5. `PedidoIntegrationTest`
6. `PromocionIntegrationTest`

**Authentication**: Tests use either JWT tokens from ms-seguridad or fallback Basic Auth (test/test123)

## Configuration

### Profiles
- `default`: Local development (`application.yml`)
- `docker`: Container deployment (`application-docker.yml`)
- `test`: Integration testing (`application-test.properties`)

### Environment Variables
```bash
# Database (per service)
DB_HOST=mysql / mysql-seguridad  # Database hosts  
DB_USERNAME=cotizador_user       # Cotizador DB user
DB_PASSWORD=cotizador_pass       # Cotizador DB password
MYSQL_SEGURIDAD_USER=seguridad_user      # Seguridad DB user
MYSQL_SEGURIDAD_PASSWORD=seguridad_pass  # Seguridad DB password

# Security & JWT
SECURITY_USERNAME=admin          # Fallback basic auth user
SECURITY_PASSWORD=admin123       # Fallback basic auth password
JWT_ACCESS_TOKEN_DURATION=15m    # JWT access token expiry
JWT_REFRESH_TOKEN_DURATION=24h   # JWT refresh token expiry
JWT_ISSUER=cotizador-system      # JWT issuer

# Service URLs
JWT_MS_SEGURIDAD_BASE_URL=http://ms-seguridad:8081
```

### Service URLs
- **Frontend (Vue.js)**: http://localhost (via gateway)
- **Gateway (Nginx)**: http://localhost (single entry point)
- **Cotizador API**: http://localhost/cotizador/v1/api (via gateway)
- **Seguridad API**: http://localhost/seguridad/v1/api (via gateway)
- **Swagger UI**: http://localhost/swagger-ui.html (via gateway)
- **Health Checks**: 
  - Cotizador: http://localhost/actuator/health
  - Seguridad: http://localhost/seguridad/actuator/health
- **Internal Services** (Docker network only):
  - ms-cotizador: http://backend:8080
  - ms-seguridad: http://ms-seguridad:8081
  - MySQL Cotizador: mysql:3306
  - MySQL Seguridad: mysql-seguridad:3306

## Development Guidelines

### Business Logic Location
- **Domain Objects**: Core business rules and calculations
- **Services**: Orchestration and transaction management only
- **Controllers**: Request/response handling and validation

### Testing Strategy
- **Unit Tests**: Test domain logic in isolation
- **Integration Tests**: Test complete workflows with real database
- **API Tests**: Use REST Assured for endpoint validation

### Database Development
- **ms-cotizador DDL**: Schema changes in `ms-cotizador/sql/ddl.sql`
- **ms-cotizador DML**: Sample data in `ms-cotizador/sql/dml.sql`
- **ms-seguridad DDL**: Schema changes in `ms-seguridad/scripts/seguridad_ddl.sql`
- **Migration**: Currently manual via Docker init scripts (Flyway planned for future)

### Component Development Pattern
When adding new hardware components:
1. Extend `Componente` abstract class
2. Add type-specific attributes and validation
3. Update `PcBuilder` validation rules if needed
4. Add integration tests for CRUD operations

### Quotation Algorithm Development
To add new quotation algorithms:
1. Implement `ICotizador` interface in ms-cotizador
2. Add algorithm selection logic in service layer
3. Create integration tests covering business scenarios

### Authentication Development
When working with JWT authentication:
1. Token validation happens automatically via `JwtAuthenticationFilter`
2. RBAC permissions checked via `@PreAuthorize` annotations
3. Session management handled by ms-seguridad service
4. JWKS endpoint provides public keys for token validation

### Frontend Development (Vue.js 3)
When adding new frontend features:
1. Use Composition API for component logic
2. Leverage Pinia stores for state management
3. Implement loading states using centralized loading system
4. Follow TailwindCSS utility-first approach
5. Handle JWT token refresh automatically via interceptors

## Common Development Tasks

### Adding New Component Type (ms-cotizador)
1. Create new component class in `dominio.core.componentes`
2. Update `TipoComponente` enum in both domain and entity packages
3. Add validation rules in `PcBuilder`
4. Update frontend Vue component for UI support
5. Create integration tests

### Adding New Promotion Type (ms-cotizador)
1. Extend `Promocion` abstract class
2. Implement `calcularImportePromocion()` method
3. Add builder support in `PromocionBuilder`
4. Update frontend promotion management UI
5. Test promotion stacking behavior

### Adding New User Role (ms-seguridad)
1. Add role to `scripts/seguridad_ddl.sql`
2. Update RBAC permissions in controllers
3. Update frontend permission checks
4. Create test users with new role

### Debugging Integration Tests
- Check TestContainer logs: `docker logs <container_id>`
- Verify test data setup in DDL/DML scripts for each service
- Ensure proper test isolation and cleanup
- For JWT issues, check ms-seguridad service logs

## Database Schema

### ms-cotizador Database
Key entities and relationships:
- `cocomponente` → `cotipo_componente` (component types)
- `cocotizacion` → `codetalle_cotizacion` (quotation details)
- `copedido` → `cocotizacion` (orders from quotations)
- `copromocion` → component associations
- `copc_parte` → component composition (PC parts)

Business rules enforced at domain level:
- PC must have 1-2 monitors, 1-2 graphics cards, 1-3 hard drives
- Promotions can be stacked with proper precedence
- Tax calculations vary by country (Mexico, USA, Canada)

### ms-seguridad Database
Authentication and authorization entities:
- `usuarios` → user accounts with credentials
- `roles` → system roles (ADMIN, GERENTE, VENDEDOR, INVENTARIO, CONSULTOR)
- `rol_asignado` → user-role assignments (many-to-many)
- `accesos` → session tracking and audit trail

## Key API Endpoints

### ms-seguridad APIs (`/seguridad/v1/api`)
```http
POST /auth/login           # User authentication
POST /auth/refresh         # Token refresh
POST /auth/logout          # Session termination
GET  /auth/validate        # Token validation
GET  /keys/jwks           # JWKS public keys
GET  /session/{id}/info   # Session information
POST /session/{id}/close  # Close specific session
```

### ms-cotizador APIs (`/cotizador/v1/api`)
```http
# Components (requires JWT authentication)
GET    /componentes       # List components
POST   /componentes       # Create (ADMIN, GERENTE, INVENTARIO)
DELETE /componentes/{id}  # Delete (ADMIN only)

# Quotations
GET    /cotizaciones      # List quotations  
POST   /cotizaciones      # Create (ADMIN, GERENTE, VENDEDOR)

# PCs
GET    /pcs               # List PCs
POST   /pcs               # Create (ADMIN, GERENTE, INVENTARIO)
DELETE /pcs/{id}          # Delete (ADMIN only)

# Promotions
GET    /promociones       # List promotions
POST   /promociones       # Create (ADMIN, GERENTE)
DELETE /promociones/{id}  # Delete (ADMIN only)
```