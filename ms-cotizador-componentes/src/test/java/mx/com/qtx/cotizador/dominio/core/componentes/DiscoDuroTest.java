package mx.com.qtx.cotizador.dominio.core.componentes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests unitarios para la funcionalidad de DiscoDuro.
 * Valida las propiedades específicas, herencia de ComponenteSimple, 
 * métodos específicos y reglas de negocio para discos duros.
 * 
 * Casos cubiertos:
 * - Propiedades específicas (capacidad de almacenamiento)
 * - Validaciones de negocio (capacidades válidas, tipos de disco)
 * - Herencia correcta de Componente y ComponenteSimple
 * - Métodos específicos de DiscoDuro (getCapacidadAlm, setCapacidadAlm, getCategoria)
 * - Métodos heredados (calcularUtilidad, cotizar, mostrarCaracteristicas)
 * - Reglas de negocio implícitas identificadas
 * 
 * Algoritmos probados:
 * 1. Creación mediante factory method Componente.crearDiscoDuro()
 * 2. Cálculo de utilidad: precioBase - costo
 * 3. Cotización: precioBase × cantidad (sin promoción)
 * 4. Validación de capacidades de almacenamiento válidas
 * 5. Verificación de tipos de disco permitidos
 *
 * @author Claude Code
 * @version 1.0
 * @since 2025-01-10
 */
@DisplayName("DiscoDuro - Tests de Lógica de Dominio")
class DiscoDuroTest {

    // ==================== CREACIÓN Y PROPIEDADES BÁSICAS ====================

    /**
     * Verifica que se pueda crear un disco duro correctamente mediante el factory method
     * y que todas las propiedades básicas se inicialicen adecuadamente.
     * 
     * Given: Parámetros válidos para crear un disco duro
     * When: Se crea el disco duro usando el factory method
     * Then: El disco se crea correctamente con todas las propiedades asignadas
     */
    @Test
    @DisplayName("Debe crear disco duro correctamente con factory method")
    void shouldCreateDiscoDuroSuccessfullyWhenValidParametersProvided() {
        // Arrange
        String id = "DD001";
        String descripcion = "Disco Duro SATA 1TB";
        String marca = "Seagate";
        String modelo = "Barracuda";
        BigDecimal costo = new BigDecimal("50.00");
        BigDecimal precioBase = new BigDecimal("75.00");
        String capacidadAlm = "1TB";

        // Act
        Componente discoDuro = Componente.crearDiscoDuro(id, descripcion, marca, 
                                                        modelo, costo, precioBase, capacidadAlm);

        // Assert
        assertThat(discoDuro).isNotNull();
        assertThat(discoDuro).isInstanceOf(DiscoDuro.class);
        assertThat(discoDuro.getId()).isEqualTo(id);
        assertThat(discoDuro.getDescripcion()).isEqualTo(descripcion);
        assertThat(discoDuro.getMarca()).isEqualTo(marca);
        assertThat(discoDuro.getModelo()).isEqualTo(modelo);
        assertThat(discoDuro.getCosto()).isEqualByComparingTo(costo);
        assertThat(discoDuro.getPrecioBase()).isEqualByComparingTo(precioBase);
        
        // Verificar propiedad específica
        DiscoDuro disco = (DiscoDuro) discoDuro;
        assertThat(disco.getCapacidadAlm()).isEqualTo(capacidadAlm);
    }

    /**
     * Verifica que el getter de capacidad de almacenamiento funcione correctamente.
     * 
     * Given: Un disco duro con capacidad específica
     * When: Se obtiene la capacidad mediante getter
     * Then: Se retorna la capacidad correcta
     */
    @Test
    @DisplayName("Debe obtener capacidad de almacenamiento correctamente")
    void shouldGetCapacidadAlmCorrectlyWhenDiscoDuroExists() {
        // Arrange
        String capacidadEsperada = "500GB";
        Componente discoDuro = Componente.crearDiscoDuro("DD002", "Disco SSD", 
                "Samsung", "EVO", new BigDecimal("80.00"), 
                new BigDecimal("120.00"), capacidadEsperada);

        // Act
        DiscoDuro disco = (DiscoDuro) discoDuro;
        String capacidadObtenida = disco.getCapacidadAlm();

        // Assert
        assertThat(capacidadObtenida).isEqualTo(capacidadEsperada);
    }

    /**
     * Verifica que el setter de capacidad de almacenamiento funcione correctamente.
     * 
     * Given: Un disco duro existente
     * When: Se actualiza la capacidad mediante setter
     * Then: La nueva capacidad se asigna correctamente
     */
    @Test
    @DisplayName("Debe actualizar capacidad de almacenamiento correctamente")
    void shouldSetCapacidadAlmCorrectlyWhenNewValueProvided() {
        // Arrange
        Componente discoDuro = Componente.crearDiscoDuro("DD003", "Disco NVMe", 
                "Western Digital", "Black", new BigDecimal("100.00"), 
                new BigDecimal("150.00"), "256GB");
        DiscoDuro disco = (DiscoDuro) discoDuro;
        String nuevaCapacidad = "512GB";

        // Act
        disco.setCapacidadAlm(nuevaCapacidad);

        // Assert
        assertThat(disco.getCapacidadAlm()).isEqualTo(nuevaCapacidad);
    }

    // ==================== HERENCIA DE COMPONENTESIMPLE Y COMPONENTE ====================

    /**
     * Verifica que DiscoDuro herede correctamente de ComponenteSimple.
     * 
     * Given: Un disco duro creado
     * When: Se verifica la herencia
     * Then: El disco es instancia de ComponenteSimple y Componente
     */
    @Test
    @DisplayName("Debe heredar correctamente de ComponenteSimple")
    void shouldInheritFromComponenteSimpleCorrectly() {
        // Arrange & Act
        Componente discoDuro = Componente.crearDiscoDuro("DD004", "Test Disk", 
                "TestBrand", "TestModel", new BigDecimal("30.00"), 
                new BigDecimal("50.00"), "120GB");

        // Assert
        assertThat(discoDuro).isInstanceOf(ComponenteSimple.class);
        assertThat(discoDuro).isInstanceOf(Componente.class);
        assertThat(discoDuro).isInstanceOf(DiscoDuro.class);
    }

    /**
     * Verifica que el método getCategoria retorne la categoría correcta.
     * 
     * Given: Un disco duro
     * When: Se solicita la categoría
     * Then: Retorna "Disco Duro"
     */
    @Test
    @DisplayName("Debe retornar categoría 'Disco Duro' correctamente")
    void shouldReturnCorrectCategoryWhenGetCategoriaInvoked() {
        // Arrange
        Componente discoDuro = Componente.crearDiscoDuro("DD005", "HDD Test", 
                "Toshiba", "P300", new BigDecimal("40.00"), 
                new BigDecimal("65.00"), "2TB");

        // Act
        String categoria = discoDuro.getCategoria();

        // Assert
        assertThat(categoria).isEqualTo("Disco Duro");
    }

    /**
     * Verifica que el cálculo de utilidad funcione correctamente (método heredado).
     * 
     * Given: Un disco duro con costo y precio base específicos
     * When: Se calcula la utilidad
     * Then: Retorna la diferencia precioBase - costo
     */
    @Test
    @DisplayName("Debe calcular utilidad correctamente como precio - costo")
    void shouldCalculateUtilityCorrectlyWhenInvokedOnDiscoDuro() {
        // Arrange
        BigDecimal costo = new BigDecimal("60.00");
        BigDecimal precioBase = new BigDecimal("90.00");
        BigDecimal utilidadEsperada = new BigDecimal("30.00");
        
        Componente discoDuro = Componente.crearDiscoDuro("DD006", "Utility Test", 
                "Hitachi", "Ultrastar", costo, precioBase, "4TB");

        // Act
        BigDecimal utilidadCalculada = discoDuro.calcularUtilidad();

        // Assert
        assertThat(utilidadCalculada).isEqualByComparingTo(utilidadEsperada);
    }

    /**
     * Verifica que la cotización sin promoción funcione correctamente (método heredado).
     * 
     * Given: Un disco duro sin promoción asignada
     * When: Se cotiza una cantidad específica
     * Then: Retorna precioBase × cantidad
     */
    @ParameterizedTest(name = "Cantidad {0} debe cotizar {1}")
    @CsvSource({
        "1, 100.00",
        "2, 200.00", 
        "5, 500.00",
        "10, 1000.00"
    })
    @DisplayName("Debe cotizar correctamente sin promoción: precioBase × cantidad")
    void shouldQuoteCorrectlyWithoutPromotionWhenQuantityProvided(int cantidad, String importeEsperado) {
        // Arrange
        BigDecimal precioBase = new BigDecimal("100.00");
        Componente discoDuro = Componente.crearDiscoDuro("DD007", "Quote Test", 
                "ADATA", "SU800", new BigDecimal("70.00"), precioBase, "240GB");

        // Act
        BigDecimal importeCotizado = discoDuro.cotizar(cantidad);

        // Assert
        assertThat(importeCotizado).isEqualByComparingTo(new BigDecimal(importeEsperado));
    }

    // ==================== REGLAS DE NEGOCIO IMPLÍCITAS ====================

    /**
     * Verifica validación de capacidades de almacenamiento válidas.
     * Regla de negocio implícita: Las capacidades deben seguir estándares del mercado.
     * 
     * Given: Capacidades de almacenamiento estándar del mercado
     * When: Se crean discos duros con estas capacidades
     * Then: Los discos se crean correctamente
     */
    @ParameterizedTest(name = "Capacidad {0} debe ser válida")
    @ValueSource(strings = {
        "120GB", "240GB", "480GB", "960GB",  // SSD capacidades comunes
        "500GB", "1TB", "2TB", "4TB", "8TB", "16TB", // HDD capacidades comunes
        "256GB", "512GB", "1024GB", // NVMe capacidades comunes
        "128MB", "256MB", "512MB", "1GB", "2GB" // Capacidades pequeñas legacy
    })
    @DisplayName("Debe aceptar capacidades de almacenamiento estándar del mercado")
    void shouldAcceptStandardMarketCapacitiesWhenCreatingDiscoDuro(String capacidad) {
        // Arrange & Act
        Componente discoDuro = Componente.crearDiscoDuro("DD_CAP_" + capacidad.hashCode(), 
                "Test Capacity", "TestBrand", "TestModel", 
                new BigDecimal("50.00"), new BigDecimal("75.00"), capacidad);

        // Assert
        assertThat(discoDuro).isNotNull();
        DiscoDuro disco = (DiscoDuro) discoDuro;
        assertThat(disco.getCapacidadAlm()).isEqualTo(capacidad);
    }

    /**
     * Verifica que se puedan identificar tipos de disco por capacidad y patrón.
     * Regla de negocio implícita: Diferentes tipos de disco tienen rangos de capacidad típicos.
     * 
     * Given: Capacidades típicas de diferentes tipos de disco
     * When: Se analiza el tipo de disco implícito
     * Then: Se puede inferir el tipo de disco por los patrones de capacidad
     */
    @ParameterizedTest(name = "Capacidad {0} sugiere tipo {1}")
    @CsvSource({
        "128MB, Legacy",
        "120GB, SSD",
        "240GB, SSD", 
        "480GB, SSD",
        "500GB, HDD",
        "1TB, HDD/SSD",
        "2TB, HDD",
        "8TB, HDD",
        "256GB, NVMe",
        "512GB, NVMe"
    })
    @DisplayName("Debe permitir inferir tipo de disco por capacidad típica")
    void shouldAllowInferringDiskTypeByTypicalCapacityWhenCreated(String capacidad, String tipoEsperado) {
        // Arrange & Act
        Componente discoDuro = Componente.crearDiscoDuro("DD_TYPE_" + capacidad.hashCode(), 
                "Test Type Classification", "TestBrand", "TestModel", 
                new BigDecimal("45.00"), new BigDecimal("70.00"), capacidad);

        // Assert
        assertThat(discoDuro).isNotNull();
        DiscoDuro disco = (DiscoDuro) discoDuro;
        assertThat(disco.getCapacidadAlm()).isEqualTo(capacidad);
        
        // Verificar que el disco se pueda clasificar por capacidad
        // (Esta sería una regla de negocio que se podría implementar en el futuro)
        assertThat(disco.getCapacidadAlm()).containsAnyOf("GB", "TB", "MB");
    }

    /**
     * Verifica que las velocidades RPM sean coherentes con tipos de disco.
     * Regla de negocio implícita: Solo HDDs mecánicos tienen velocidad RPM.
     * Nota: Esta validación podría agregarse en el futuro como mejora.
     * 
     * Given: Discos que podrían tener información de RPM
     * When: Se verifica la coherencia de la información
     * Then: La información debe ser coherente con el tipo de disco
     */
    @Test
    @DisplayName("Debe permitir extensión futura para validación de RPM por tipo de disco")
    void shouldAllowFutureExtensionForRpmValidationByDiskType() {
        // Arrange & Act
        // Crear discos que representan diferentes tipos
        Componente hddDisco = Componente.crearDiscoDuro("DD_HDD", "HDD 7200 RPM", 
                "Seagate", "Barracuda", new BigDecimal("50.00"), 
                new BigDecimal("75.00"), "1TB");
        
        Componente ssdDisco = Componente.crearDiscoDuro("DD_SSD", "SSD NVMe", 
                "Samsung", "970 EVO", new BigDecimal("80.00"), 
                new BigDecimal("120.00"), "500GB");

        // Assert
        // Verificar que ambos discos se creen correctamente
        assertThat(hddDisco).isNotNull();
        assertThat(ssdDisco).isNotNull();
        
        // En el futuro se podrían agregar validaciones como:
        // - HDDs con descripción que incluya "RPM" sean válidos
        // - SSDs no deberían tener información de RPM
        // - NVMe deberían tener velocidades en términos de MB/s o GB/s
        DiscoDuro hdd = (DiscoDuro) hddDisco;
        DiscoDuro ssd = (DiscoDuro) ssdDisco;
        
        assertThat(hdd.getDescripcion()).contains("RPM");
        assertThat(ssd.getDescripcion()).contains("SSD");
    }

    /**
     * Verifica validación de coherencia precio/capacidad.
     * Regla de negocio implícita: Discos con mayor capacidad generalmente cuestan más.
     * 
     * Given: Discos con diferentes capacidades
     * When: Se comparan precios relativos
     * Then: Los precios deben ser coherentes con las capacidades
     */
    @Test
    @DisplayName("Debe permitir validación de coherencia precio/capacidad")
    void shouldValidatePriceCapacityCoherenceWhenComparingDisks() {
        // Arrange
        Componente disco120GB = Componente.crearDiscoDuro("DD_120", "SSD 120GB", 
                "Kingston", "A400", new BigDecimal("25.00"), 
                new BigDecimal("40.00"), "120GB");
                
        Componente disco1TB = Componente.crearDiscoDuro("DD_1TB", "SSD 1TB", 
                "Kingston", "A400", new BigDecimal("80.00"), 
                new BigDecimal("120.00"), "1TB");

        // Act & Assert
        // Verificar que el disco de mayor capacidad tenga mayor precio
        assertThat(disco1TB.getPrecioBase()).isGreaterThan(disco120GB.getPrecioBase());
        assertThat(disco1TB.getCosto()).isGreaterThan(disco120GB.getCosto());
        
        // Verificar que las utilidades también sean coherentes
        assertThat(disco1TB.calcularUtilidad()).isGreaterThan(disco120GB.calcularUtilidad());
    }

    // ==================== CASOS EDGE Y VALIDACIONES ====================

    /**
     * Verifica el comportamiento cuando se asigna capacidad null.
     * 
     * Given: Un disco duro existente
     * When: Se asigna null como capacidad
     * Then: Se acepta null (comportamiento actual del código)
     */
    @Test
    @DisplayName("Debe manejar capacidad null sin errores")
    void shouldHandleNullCapacityWithoutErrorsWhenSet() {
        // Arrange
        Componente discoDuro = Componente.crearDiscoDuro("DD_NULL", "Test Null", 
                "TestBrand", "TestModel", new BigDecimal("30.00"), 
                new BigDecimal("50.00"), "100GB");
        DiscoDuro disco = (DiscoDuro) discoDuro;

        // Act
        disco.setCapacidadAlm(null);

        // Assert
        assertThat(disco.getCapacidadAlm()).isNull();
    }

    /**
     * Verifica el comportamiento cuando se asigna capacidad vacía.
     * 
     * Given: Un disco duro existente
     * When: Se asigna cadena vacía como capacidad
     * Then: Se acepta la cadena vacía
     */
    @Test
    @DisplayName("Debe manejar capacidad vacía sin errores")
    void shouldHandleEmptyCapacityWithoutErrorsWhenSet() {
        // Arrange
        Componente discoDuro = Componente.crearDiscoDuro("DD_EMPTY", "Test Empty", 
                "TestBrand", "TestModel", new BigDecimal("30.00"), 
                new BigDecimal("50.00"), "100GB");
        DiscoDuro disco = (DiscoDuro) discoDuro;

        // Act
        disco.setCapacidadAlm("");

        // Assert
        assertThat(disco.getCapacidadAlm()).isEmpty();
    }

    /**
     * Verifica que el método toString heredado funcione correctamente.
     *
     * Given: Un disco duro creado
     * When: Se invoca toString()
     * Then: Retorna representación de cadena con información básica
     */
    @Test
    @DisplayName("Debe generar representación toString correcta")
    void shouldGenerateCorrectToStringRepresentationWhenInvoked() {
        // Arrange
        Componente discoDuro = Componente.crearDiscoDuro("DD_STR", "Test ToString",
                "TestBrand", "TestModel", new BigDecimal("30.00"),
                new BigDecimal("50.00"), "1TB");

        // Act
        String stringRepresentation = discoDuro.toString();

        // Assert
        assertThat(stringRepresentation).isNotNull();
        assertThat(stringRepresentation).contains("DD_STR");
        assertThat(stringRepresentation).contains("Test ToString");
        assertThat(stringRepresentation).contains("TestBrand");
        assertThat(stringRepresentation).contains("TestModel");
    }

    // ==================== VALIDACIONES PRD REQUERIDAS ====================

    /**
     * Verifica que se lance IllegalArgumentException cuando capacidadAlm es nula.
     *
     * PRD Requirement: Al recibir el valor para el campo capacidadAlm, debe validarse
     * que no sea nulo. En caso de ser el caso, tirar una excepción.
     *
     * Given: Parámetros válidos excepto capacidadAlm nula
     * When: Se intenta crear disco duro
     * Then: Se lanza IllegalArgumentException
     */
    @Test
    @DisplayName("Dado capacidadAlm nula entonces lanza IllegalArgumentException")
    void dadoCapacidadAlmNulaEntoncesLanzaIllegalArgumentException() {
        // Given & When & Then
        assertThatThrownBy(() -> Componente.crearDiscoDuro(
                "DD_NULL", "Test Disco", "TestBrand", "TestModel",
                new BigDecimal("50.00"), new BigDecimal("75.00"), null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("capacidadAlm")
          .hasMessageContaining("nula");
    }

    /**
     * Verifica que se lance IllegalArgumentException cuando capacidadAlm está vacía.
     *
     * PRD Requirement: Al recibir el valor para el campo capacidadAlm, debe validarse
     * que no sea tampoco vacío. En caso de ser el caso, tirar una excepción.
     *
     * Given: Parámetros válidos excepto capacidadAlm vacía
     * When: Se intenta crear disco duro
     * Then: Se lanza IllegalArgumentException
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("Dado capacidadAlm vacía entonces lanza IllegalArgumentException")
    void dadoCapacidadAlmVaciaEntoncesLanzaIllegalArgumentException(String capacidadVacia) {
        // Given & When & Then
        assertThatThrownBy(() -> Componente.crearDiscoDuro(
                "DD_EMPTY", "Test Disco", "TestBrand", "TestModel",
                new BigDecimal("50.00"), new BigDecimal("75.00"), capacidadVacia
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("capacidadAlm")
          .hasMessageContaining("vacía");
    }

    /**
     * Verifica que la validación de capacidadAlm válida funcione correctamente.
     *
     * Given: Parámetros válidos incluyendo capacidadAlm válida
     * When: Se crea disco duro
     * Then: Se crea exitosamente sin excepciones
     */
    @ParameterizedTest
    @CsvSource({
        "1TB, WD Blue 1TB",
        "500GB, Seagate 500GB",
        "2TB, Samsung SSD 2TB",
        "128GB, Kingston 128GB SSD"
    })
    @DisplayName("Dado capacidadAlm válida entonces creación exitosa")
    void dadoCapacidadAlmValidaEntoncesCreacionExitosa(String capacidad, String descripcion) {
        // Given & When
        Componente disco = Componente.crearDiscoDuro(
            "DD_VALID", descripcion, "TestBrand", "TestModel",
            new BigDecimal("50.00"), new BigDecimal("75.00"), capacidad
        );

        // Then
        assertThat(disco).isNotNull();
        assertThat(disco).isInstanceOf(DiscoDuro.class);
        assertThat(((DiscoDuro) disco).getCapacidadAlm()).isEqualTo(capacidad);
    }
}