# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CotizadorPcPartes is a comprehensive enterprise-level system for PC hardware component quotation, order management, and inventory control. It implements Domain-Driven Design with Spring Boot 3.5.0, Java 21, and MySQL 8.4.4.

## Technology Stack

- **Backend**: Spring Boot 3.5.0, Java 21, Spring Data JPA, Spring Security (Basic Auth)
- **Database**: MySQL 8.4.4 with HikariCP connection pooling
- **Frontend**: HTML5/CSS3/JavaScript ES6+ with TailwindCSS
- **Testing**: JUnit 5, TestContainers, REST Assured
- **DevOps**: Docker Compose, Spring Boot Actuator
- **Build**: Maven 3.8+

## Common Commands

### Development Commands
```bash
# Build and run backend locally
cd Cotizador
mvn spring-boot:run

# Run all tests
mvn test

# Run integration tests only (in specific order)
mvn test -Dtest="*IntegrationTest"

# Run with coverage report
mvn test jacoco:report
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
# Access MySQL container
docker exec -it cotizador-mysql mysql -u cotizador_user -p cotizador

# Re-initialize database
./docker-scripts.sh db-init
```

## Architecture

### Domain Layer Structure
The system follows Domain-Driven Design with rich domain models:

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

### Service Layer
Services orchestrate domain objects rather than containing business logic:
- Transaction management with `@Transactional`
- Standardized `ApiResponse<T>` wrapper for all endpoints
- Domain-first approach with minimal service logic

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

**Authentication**: Tests use automatic Basic Auth setup (test/test123)

## Configuration

### Profiles
- `default`: Local development (`application.yml`)
- `docker`: Container deployment (`application-docker.yml`)
- `test`: Integration testing (`application-test.properties`)

### Environment Variables
```bash
DB_HOST=mysql                    # Database host
DB_USERNAME=cotizador_user       # Database user
DB_PASSWORD=cotizador_pass       # Database password
SECURITY_USERNAME=admin          # API basic auth user
SECURITY_PASSWORD=admin123       # API basic auth password
```

### Service URLs
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080/cotizador/v1/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **MySQL**: localhost:3306

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
- **DDL**: Schema changes in `sql/ddl.sql`
- **DML**: Sample data in `sql/dml.sql`
- **Migration**: Currently manual (Flyway planned for future)

### Component Development Pattern
When adding new hardware components:
1. Extend `Componente` abstract class
2. Add type-specific attributes and validation
3. Update `PcBuilder` validation rules if needed
4. Add integration tests for CRUD operations

### Quotation Algorithm Development
To add new quotation algorithms:
1. Implement `ICotizador` interface
2. Add algorithm selection logic in service layer
3. Create integration tests covering business scenarios

## Common Development Tasks

### Adding New Component Type
1. Create new component class in `dominio.core.componentes`
2. Update `TipoComponente` enum
3. Add validation rules in `PcBuilder`
4. Create integration tests

### Adding New Promotion Type
1. Extend `Promocion` abstract class
2. Implement `calcularImportePromocion()` method
3. Add builder support in `PromocionBuilder`
4. Test promotion stacking behavior

### Debugging Integration Tests
- Check TestContainer logs: `docker logs <container_id>`
- Verify test data setup in DDL/DML scripts
- Ensure proper test isolation and cleanup

## Database Schema

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