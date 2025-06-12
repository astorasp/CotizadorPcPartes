# 🔄 Migración a Base de Datos Compartida para Tests de Integración

## 📋 Objetivo
Unificar todos los tests de integración para que usen **una misma base de datos MySQL 8.4.4** compartida que se inicia una vez y se destruye al terminar toda la suite de tests.

## ✅ Beneficios
- ⚡ **Mayor velocidad**: Un solo contenedor para todos los tests
- 💾 **Menor uso de recursos**: Reduce consumo de memoria y CPU
- 🔧 **Mantenimiento simplificado**: Configuración centralizada
- 📊 **Consistencia**: Mismos datos de prueba para todos los tests

## 🏗️ Arquitectura Implementada

### 1. `TestContainerConfig.java` (✅ Actualizada)
```java
@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {
    private static MySQLContainer<?> sharedMySQLContainer; // SINGLETON
    
    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        // Patrón Singleton Thread-Safe con inicialización lazy
        // Contenedor se reutiliza entre todos los tests
        // Se destruye automáticamente con ShutdownHook
    }
}
```

### 2. `BaseIntegrationTest.java` (✅ Creada)
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
public abstract class BaseIntegrationTest {
    protected static final String USER_ADMIN = "test";
    protected static final String PASSWORD_ADMIN = "test123";
    
    @LocalServerPort
    protected int port;
    
    @BeforeEach
    protected void setUp() {
        // Configuración automática de RestAssured
    }
}
```

## 🔧 Pasos de Migración

### Tests YA MIGRADOS ✅
- `ComponenteIntegrationTest.java` 
- `CotizacionIntegrationTest.java`

### Tests PENDIENTES de Migración 🚧
- `PcIntegrationTest.java`
- `PedidoIntegrationTest.java` 
- `PromocionIntegrationTest.java`
- `ProveedorIntegrationTest.java`

### Patrón de Migración

#### ANTES (Patrón Actual):
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@DisplayName("Integration Tests - ...")
class MiIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.4")
            .withDatabaseName("cotizador_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withInitScript("sql/ddl.sql");

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        // ... más configuraciones
    }

    @BeforeAll
    static void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.authentication = RestAssured.basic("test", "test123");
        RestAssured.basePath = "/cotizador/v1/api";
    }
}
```

#### DESPUÉS (Patrón Unificado):
```java
@DisplayName("Integration Tests - ...")
class MiIntegrationTest extends BaseIntegrationTest {
    
    // ¡Toda la configuración heredada automáticamente!
    // - Puerto aleatorio
    // - Base de datos compartida MySQL 8.4.4  
    // - Autenticación preconfigurada (test/test123)
    // - RestAssured configurado automáticamente
    
    @Test
    void miPruebaDeIntegracion() {
        given()
            .contentType(ContentType.JSON)
            .body(miRequest)
        .when()
            .post("/mi-endpoint")
        .then()
            .statusCode(200);
    }
}
```

## 🛠️ Instrucciones Detalladas

### Para cada archivo `*IntegrationTest.java`:

1. **Actualizar imports:**
   ```java
   // REMOVER:
   import org.springframework.boot.test.context.SpringBootTest;
   import org.springframework.boot.test.web.server.LocalServerPort;
   import org.springframework.test.context.ActiveProfiles;
   import org.springframework.test.context.DynamicPropertyRegistry;
   import org.springframework.test.context.DynamicPropertySource;
   import org.testcontainers.containers.MySQLContainer;
   import org.testcontainers.junit.jupiter.Container;
   import org.testcontainers.junit.jupiter.Testcontainers;
   
   // AGREGAR:
   import mx.com.qtx.cotizador.integration.BaseIntegrationTest;
   ```

2. **Simplificar declaración de clase:**
   ```java
   // REMOVER todas estas anotaciones:
   @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
   @ActiveProfiles("test")
   @Testcontainers
   
   // CAMBIAR:
   class MiIntegrationTest {
   // POR:
   class MiIntegrationTest extends BaseIntegrationTest {
   ```

3. **Eliminar configuración duplicada:**
   ```java
   // REMOVER TODO ESTO:
   @Container
   static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.4")...
   
   @LocalServerPort
   private int port;
   
   @DynamicPropertySource
   static void configureProperties(DynamicPropertyRegistry registry) {...}
   
   @BeforeAll
   static void setup() {
       RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
   }
   
   @BeforeEach
   void setUp() {
       RestAssured.port = port;
       RestAssured.authentication = RestAssured.basic("test", "test123");
       RestAssured.basePath = "/cotizador/v1/api";
   }
   
   // REMOVER también las constantes duplicadas:
   private static final String USER_ADMIN = "test";
   private static final String PASSWORD_ADMIN = "test123";
   ```

4. **Mantener solo los tests:**
   ```java
   // MANTENER todos los métodos @Test sin cambios
   @Test
   void miTest() { ... }
   ```

## 🧪 Verificación

### Ejecutar Tests:
```bash
# Tests individuales
mvn test -Dtest=ComponenteIntegrationTest

# Todos los tests de integración
mvn test -Dtest="*IntegrationTest"

# Ver logs del contenedor compartido
mvn test -Dtest="*IntegrationTest" | grep "🚀 Contenedor MySQL"
```

### Logs Esperados:
```
🧪 Iniciando suite de tests de integración
📚 Base de datos compartida MySQL 8.4.4
🔐 Autenticación: test/test123

🚀 Contenedor MySQL compartido iniciado:
📍 URL: jdbc:mysql://localhost:32768/cotizador_test
👤 Usuario: test_user
🔐 Password: test_password
🗃️ Database: cotizador_test

... tests ejecutándose ...

✅ Suite de tests de integración completada  
🗄️ Contenedor MySQL será destruido automáticamente
🗄️ Cerrando contenedor MySQL compartido...
```

## 📊 Métricas de Mejora Esperadas

| Métrica | Antes | Después | Mejora |
|---------|-------|---------|--------|
| Tiempo total | ~5 min | ~2 min | 60% más rápido |
| Contenedores | 6 | 1 | 83% menos recursos |
| Líneas código | ~300 | ~50 | 83% menos código |
| Configuración | Duplicada | Centralizada | Mantenimiento simplificado |

## 🔧 Próximos Pasos

1. **Migrar test por test** siguiendo el patrón
2. **Ejecutar suite completa** para verificar funcionamiento
3. **Ajustar datos de prueba** si hay conflictos entre tests
4. **Documentar casos especiales** que requieran configuración adicional

## 🆘 Solución de Problemas

### Error: "Port already in use"
- **Causa**: Múltiples contenedores intentando usar el mismo puerto
- **Solución**: Verificar que todos los tests heredan de `BaseIntegrationTest`

### Error: "Database not found"
- **Causa**: Scripts DDL/DML no se ejecutaron correctamente
- **Solución**: Verificar que `sql/ddl.sql` y `sql/dml.sql` existen en `test/resources`

### Tests fallan por datos inconsistentes
- **Causa**: Tests modifican datos compartidos
- **Solución**: Usar IDs únicos o implementar limpieza de datos entre tests

### Logs excesivos
- **Causa**: Múltiples configuraciones de logging
- **Solución**: La configuración base ya reduce el logging. Verificar configuraciones custom. 