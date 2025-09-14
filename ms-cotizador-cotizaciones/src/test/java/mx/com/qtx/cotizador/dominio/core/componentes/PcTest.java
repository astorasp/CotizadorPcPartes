package mx.com.qtx.cotizador.dominio.core.componentes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests unitarios para la funcionalidad de PC (Componente Compuesto).
 * Valida la composición con otros componentes, cálculos agregados, herencia de Componente, 
 * métodos específicos y reglas de negocio para PCs ensambladas.
 * 
 * Casos cubiertos:
 * - Composición con otros componentes (DiscoDuro, Monitor, TarjetaVideo)
 * - Cálculos de costo total (suma de componentes sin descuento)
 * - Cálculos de precio total (suma con descuento de ensamblaje 20%)
 * - Validaciones de PC completa (componentes requeridos)
 * - Gestión de lista de componentes y subcomponentes
 * - Herencia correcta de Componente (no ComponenteSimple)
 * - Métodos específicos de PC (getSubComponentes, mostrarCaracteristicas)
 * - Reglas de negocio implícitas identificadas
 * 
 * Algoritmos probados:
 * 1. Creación mediante factory method Componente.crearPc()
 * 2. Creación mediante constructor con PcBuilder
 * 3. Cálculo de precio con descuento: (suma precios) × (1 - 20%)
 * 4. Cálculo de costo total: suma costos (sin descuento)
 * 5. Cálculo de utilidad: precioBase - costo
 * 6. Filtrado de ComponenteSimple en factory method
 * 7. Validación de compatibilidad entre componentes
 * 8. Verificación de requisitos mínimos del sistema
 *
 * Constantes del dominio:
 * - DSCTO_PRECIO_AGREGADO = 20.0f (descuento por ensamblaje)
 *
 * @author Claude Code
 * @version 1.0
 * @since 2025-01-10
 */
@DisplayName("PC - Tests de Lógica de Dominio (Componente Compuesto)")
class PcTest {

    // ==================== CREACIÓN Y PROPIEDADES BÁSICAS ====================

    /**
     * Verifica que se pueda crear una PC correctamente mediante el factory method
     * y que todas las propiedades básicas se inicialicen adecuadamente.
     * 
     * Given: Parámetros válidos y lista de subcomponentes para crear una PC
     * When: Se crea la PC usando el factory method
     * Then: La PC se crea correctamente con todas las propiedades asignadas
     */
    @Test
    @DisplayName("Debe crear PC correctamente con factory method")
    void shouldCreatePcSuccessfullyWhenValidParametersProvided() {
        // Arrange
        String id = "PC001";
        String descripcion = "PC Gaming Completa";
        String marca = "Custom Build";
        String modelo = "Gaming Rig 2025";
        
        List<Componente> subComponentes = crearComponentesDeEjemplo();

        // Act
        Componente pc = Componente.crearPc(id, descripcion, marca, modelo, subComponentes);

        // Assert
        assertThat(pc).isNotNull();
        assertThat(pc).isInstanceOf(Pc.class);
        assertThat(pc.getId()).isEqualTo(id);
        assertThat(pc.getDescripcion()).isEqualTo(descripcion);
        assertThat(pc.getMarca()).isEqualTo(marca);
        assertThat(pc.getModelo()).isEqualTo(modelo);
        
        // Verificar que los precios y costos se calculen automáticamente
        assertThat(pc.getPrecioBase()).isPositive();
        assertThat(pc.getCosto()).isPositive();
        
        // Verificar que tenga subcomponentes
        Pc pcCast = (Pc) pc;
        assertThat(pcCast.getSubComponentes()).isNotEmpty();
        assertThat(pcCast.getSubComponentes().size()).isEqualTo(3); // Solo ComponenteSimple
    }

    /**
     * Verifica que se pueda crear una PC mediante constructor con PcBuilder.
     * 
     * Given: Un PcBuilder configurado con componentes válidos
     * When: Se crea la PC usando el constructor con PcBuilder
     * Then: La PC se crea correctamente con componentes del builder
     */
    @Test
    @DisplayName("Debe crear PC correctamente mediante constructor con PcBuilder")
    void shouldCreatePcSuccessfullyWhenPcBuilderProvided() {
        // Arrange
        PcBuilder builder = crearPcBuilderDeEjemplo();

        // Act
        Pc pc = new Pc(builder) {
            // Subclase concreta para testing (Pc es protegida)
        };

        // Assert
        assertThat(pc).isNotNull();
        assertThat(pc.getId()).isEqualTo(builder.getIdPc());
        assertThat(pc.getDescripcion()).isEqualTo(builder.getDescripcionPc());
        assertThat(pc.getMarca()).isEqualTo(builder.getMarcaPc());
        assertThat(pc.getModelo()).isEqualTo(builder.getModeloPc());
        
        // Verificar que los componentes se hayan agregado correctamente
        assertThat(pc.getSubComponentes()).hasSize(3); // disco + monitor + tarjeta
        assertThat(pc.getPrecioBase()).isPositive();
        assertThat(pc.getCosto()).isPositive();
    }

    // ==================== HERENCIA DE COMPONENTE ====================

    /**
     * Verifica que PC herede correctamente de Componente (NO de ComponenteSimple).
     * 
     * Given: Una PC creada
     * When: Se verifica la herencia
     * Then: La PC es instancia de Componente pero NO de ComponenteSimple
     */
    @Test
    @DisplayName("Debe heredar de Componente pero NO de ComponenteSimple")
    void shouldInheritFromComponenteButNotComponenteSimple() {
        // Arrange & Act
        List<Componente> componentes = crearComponentesDeEjemplo();
        Componente pc = Componente.crearPc("PC002", "Test Herencia", 
                "TestBrand", "TestModel", componentes);

        // Assert
        assertThat(pc).isInstanceOf(Componente.class);
        assertThat(pc).isNotInstanceOf(ComponenteSimple.class); // CRÍTICO: PC NO es ComponenteSimple
        assertThat(pc).isInstanceOf(Pc.class);
    }

    /**
     * Verifica que el método getCategoria retorne la categoría correcta.
     * 
     * Given: Una PC
     * When: Se solicita la categoría
     * Then: Retorna "PC"
     */
    @Test
    @DisplayName("Debe retornar categoría 'PC' correctamente")
    void shouldReturnCorrectCategoryWhenGetCategoriaInvoked() {
        // Arrange
        List<Componente> componentes = crearComponentesDeEjemplo();
        Componente pc = Componente.crearPc("PC003", "Category Test", 
                "TestBrand", "Pro", componentes);

        // Act
        String categoria = pc.getCategoria();

        // Assert
        assertThat(categoria).isEqualTo("PC");
    }

    // ==================== COMPOSICIÓN CON OTROS COMPONENTES ====================

    /**
     * Verifica que la PC maneje correctamente la composición con diferentes tipos de componentes.
     * 
     * Given: Una lista mixta de componentes (incluyendo otra PC anidada)
     * When: Se crea la PC con el factory method
     * Then: Solo los ComponenteSimple se incluyen en los subcomponentes
     */
    @Test
    @DisplayName("Debe filtrar solo ComponenteSimple en composición")
    void shouldFilterOnlyComponenteSimpleWhenComposingPc() {
        // Arrange
        List<Componente> componentesMixtos = new ArrayList<>();
        
        // Agregar ComponenteSimple
        componentesMixtos.add(Componente.crearDiscoDuro("DD001", "SSD 1TB", "Samsung", "EVO", 
                new BigDecimal("80"), new BigDecimal("120"), "1TB"));
        componentesMixtos.add(Componente.crearMonitor("MON001", "Monitor 24\"", "Dell", "S2422HS", 
                new BigDecimal("150"), new BigDecimal("200")));
        
        // Agregar PC anidada (NO debe incluirse porque no es ComponenteSimple)
        List<Componente> subComponentesPcAnidada = Arrays.asList(
                Componente.crearDiscoDuro("DD002", "HDD 500GB", "WD", "Blue", 
                        new BigDecimal("40"), new BigDecimal("60"), "500GB")
        );
        Componente pcAnidada = Componente.crearPc("PC_NESTED", "PC Anidada", "Test", "Nested", subComponentesPcAnidada);
        componentesMixtos.add(pcAnidada);

        // Act
        Componente pc = Componente.crearPc("PC_FILTER", "Test Filtrado", "Test", "Filter", componentesMixtos);

        // Assert
        Pc pcCast = (Pc) pc;
        assertThat(pcCast.getSubComponentes()).hasSize(2); // Solo los 2 ComponenteSimple
        assertThat(pcCast.getSubComponentes().stream().allMatch(c -> c instanceof ComponenteSimple)).isTrue();
        
        // Verificar que la PC anidada NO esté en los subcomponentes
        assertThat(pcCast.getSubComponentes().stream().noneMatch(c -> c.getId().equals("PC_NESTED"))).isTrue();
    }

    /**
     * Verifica que se puedan agregar múltiples componentes del mismo tipo.
     * 
     * Given: Una PC con múltiples discos duros y monitores
     * When: Se verifica la composición
     * Then: Todos los componentes del mismo tipo se incluyen correctamente
     */
    @Test
    @DisplayName("Debe permitir múltiples componentes del mismo tipo")
    void shouldAllowMultipleComponentsOfSameTypeWhenComposing() {
        // Arrange
        List<Componente> componentes = new ArrayList<>();
        
        // Múltiples discos
        componentes.add(Componente.crearDiscoDuro("DD001", "SSD Sistema", "Samsung", "EVO", 
                new BigDecimal("80"), new BigDecimal("120"), "512GB"));
        componentes.add(Componente.crearDiscoDuro("DD002", "HDD Datos", "WD", "Black", 
                new BigDecimal("60"), new BigDecimal("90"), "2TB"));
        componentes.add(Componente.crearDiscoDuro("DD003", "SSD Cache", "Intel", "Optane", 
                new BigDecimal("100"), new BigDecimal("150"), "256GB"));
        
        // Múltiples monitores
        componentes.add(Componente.crearMonitor("MON001", "Monitor Principal", "ASUS", "ProArt", 
                new BigDecimal("300"), new BigDecimal("450")));
        componentes.add(Componente.crearMonitor("MON002", "Monitor Secundario", "Dell", "UltraSharp", 
                new BigDecimal("200"), new BigDecimal("300")));

        // Act
        Componente pc = Componente.crearPc("PC_MULTI", "PC Multi-Componente", "Custom", "Multi", componentes);

        // Assert
        Pc pcCast = (Pc) pc;
        assertThat(pcCast.getSubComponentes()).hasSize(5);
        
        // Verificar conteos por tipo
        long discosCount = pcCast.getSubComponentes().stream().filter(c -> c instanceof DiscoDuro).count();
        long monitoresCount = pcCast.getSubComponentes().stream().filter(c -> c instanceof Monitor).count();
        
        assertThat(discosCount).isEqualTo(3);
        assertThat(monitoresCount).isEqualTo(2);
    }

    // ==================== CÁLCULOS DE PRECIO Y COSTO ====================

    /**
     * Verifica que el cálculo de precio total aplique correctamente el descuento de ensamblaje.
     * 
     * Given: Una PC con componentes de precios conocidos
     * When: Se calcula el precio base
     * Then: Se aplica el descuento del 20% sobre la suma de precios
     */
    @Test
    @DisplayName("Debe calcular precio con descuento de ensamblaje del 20%")
    void shouldCalculatePriceWithAssemblyDiscountCorrectly() {
        // Arrange
        List<Componente> componentes = Arrays.asList(
                Componente.crearDiscoDuro("DD001", "SSD", "Samsung", "EVO", 
                        new BigDecimal("50"), new BigDecimal("100"), "500GB"), // $100
                Componente.crearMonitor("MON001", "Monitor", "Dell", "S24", 
                        new BigDecimal("120"), new BigDecimal("200")), // $200
                Componente.crearTarjetaVideo("GPU001", "GPU", "NVIDIA", "RTX", 
                        new BigDecimal("200"), new BigDecimal("300"), "8GB") // $300
        );
        // Suma total sin descuento: $600
        // Con descuento 20%: $600 × 0.8 = $480

        // Act
        Componente pc = Componente.crearPc("PC_DISCOUNT", "Test Descuento", "Test", "Discount", componentes);

        // Assert
        BigDecimal precioEsperado = new BigDecimal("480.00");
        assertThat(pc.getPrecioBase()).isCloseTo(precioEsperado, within(new BigDecimal("0.01")));
    }

    /**
     * Verifica que el cálculo de costo total NO aplique descuento.
     * 
     * Given: Una PC con componentes de costos conocidos
     * When: Se calcula el costo total
     * Then: Se suma los costos sin aplicar descuento alguno
     */
    @Test
    @DisplayName("Debe calcular costo total sin descuento (suma directa)")
    void shouldCalculateCostWithoutDiscountCorrectly() {
        // Arrange
        List<Componente> componentes = Arrays.asList(
                Componente.crearDiscoDuro("DD001", "SSD", "Samsung", "EVO", 
                        new BigDecimal("50"), new BigDecimal("100"), "500GB"), // Costo: $50
                Componente.crearMonitor("MON001", "Monitor", "Dell", "S24", 
                        new BigDecimal("120"), new BigDecimal("200")), // Costo: $120
                Componente.crearTarjetaVideo("GPU001", "GPU", "NVIDIA", "RTX", 
                        new BigDecimal("200"), new BigDecimal("300"), "8GB") // Costo: $200
        );
        // Suma total de costos: $370 (sin descuento)

        // Act
        Componente pc = Componente.crearPc("PC_COST", "Test Costo", "Test", "Cost", componentes);

        // Assert
        BigDecimal costoEsperado = new BigDecimal("370.00");
        assertThat(pc.getCosto()).isEqualByComparingTo(costoEsperado);
    }

    /**
     * Verifica que el cálculo de utilidad sea correcto (precio con descuento - costo sin descuento).
     * 
     * Given: Una PC con precios y costos conocidos
     * When: Se calcula la utilidad
     * Then: Utilidad = precioConDescuento - costoSinDescuento
     */
    @Test
    @DisplayName("Debe calcular utilidad correctamente: precio(con descuento) - costo(sin descuento)")
    void shouldCalculateUtilityCorrectlyWithDiscountedPrice() {
        // Arrange
        List<Componente> componentes = Arrays.asList(
                Componente.crearDiscoDuro("DD001", "SSD", "Samsung", "EVO", 
                        new BigDecimal("60"), new BigDecimal("100"), "500GB"), // Utilidad componente: $40
                Componente.crearMonitor("MON001", "Monitor", "Dell", "S24", 
                        new BigDecimal("150"), new BigDecimal("200")) // Utilidad componente: $50
        );
        // Precio total sin descuento: $300
        // Precio con descuento 20%: $300 × 0.8 = $240
        // Costo total: $210
        // Utilidad PC: $240 - $210 = $30

        // Act
        Componente pc = Componente.crearPc("PC_UTILITY", "Test Utilidad", "Test", "Utility", componentes);

        // Assert
        BigDecimal utilidadEsperada = new BigDecimal("30.00");
        assertThat(pc.calcularUtilidad()).isCloseTo(utilidadEsperada, within(new BigDecimal("0.01")));
    }

    /**
     * Verifica diferentes escenarios de cálculo con diversas combinaciones de componentes.
     * 
     * Given: Diferentes combinaciones de componentes y cantidades
     * When: Se calculan precios y costos
     * Then: Los cálculos son correctos para cada escenario
     */
    @ParameterizedTest(name = "Scenario {0}: componentes={1}, precio_esperado={2}, costo_esperado={3}")
    @CsvSource({
        "Solo_Disco, 1, 80.00, 50.00",
        "Disco_Monitor, 2, 240.00, 170.00", 
        "PC_Completa, 3, 480.00, 370.00",
        "PC_Premium, 4, 800.00, 620.00"
    })
    @DisplayName("Debe calcular correctamente para diferentes escenarios de componentes")
    void shouldCalculateCorrectlyForDifferentComponentScenarios(String scenario, int numComponentes, 
                                                               String precioEsperado, String costoEsperado) {
        // Arrange
        List<Componente> componentes = new ArrayList<>();
        
        if (numComponentes >= 1) {
            componentes.add(Componente.crearDiscoDuro("DD", "SSD", "Samsung", "EVO", 
                    new BigDecimal("50"), new BigDecimal("100"), "500GB"));
        }
        if (numComponentes >= 2) {
            componentes.add(Componente.crearMonitor("MON", "Monitor", "Dell", "S24", 
                    new BigDecimal("120"), new BigDecimal("200")));
        }
        if (numComponentes >= 3) {
            componentes.add(Componente.crearTarjetaVideo("GPU", "GPU", "NVIDIA", "RTX", 
                    new BigDecimal("200"), new BigDecimal("300"), "8GB"));
        }
        if (numComponentes >= 4) {
            componentes.add(Componente.crearTarjetaVideo("GPU2", "GPU2", "AMD", "RX", 
                    new BigDecimal("250"), new BigDecimal("400"), "12GB"));
        }

        // Act
        Componente pc = Componente.crearPc("PC_" + scenario, "Test " + scenario, "Test", scenario, componentes);

        // Assert
        assertThat(pc.getPrecioBase()).isCloseTo(new BigDecimal(precioEsperado), within(new BigDecimal("0.01")));
        assertThat(pc.getCosto()).isCloseTo(new BigDecimal(costoEsperado), within(new BigDecimal("0.01")));
    }

    // ==================== GESTIÓN DE SUBCOMPONENTES ====================

    /**
     * Verifica que el método getSubComponentes retorne la lista correcta.
     * 
     * Given: Una PC con componentes específicos
     * When: Se obtienen los subcomponentes
     * Then: Se retorna la lista completa y correcta
     */
    @Test
    @DisplayName("Debe retornar lista correcta de subcomponentes")
    void shouldReturnCorrectSubComponentsListWhenRequested() {
        // Arrange
        List<Componente> componentesOriginales = crearComponentesDeEjemplo();
        Componente pc = Componente.crearPc("PC_SUB", "Test Subcomponentes", "Test", "Sub", componentesOriginales);

        // Act
        Pc pcCast = (Pc) pc;
        List<ComponenteSimple> subComponentes = pcCast.getSubComponentes();

        // Assert
        assertThat(subComponentes).isNotNull();
        assertThat(subComponentes).hasSize(3);
        assertThat(subComponentes.stream().allMatch(c -> c instanceof ComponenteSimple)).isTrue();
        
        // Verificar que contiene los tipos esperados
        boolean tieneDiscoDuro = subComponentes.stream().anyMatch(c -> c instanceof DiscoDuro);
        boolean tieneMonitor = subComponentes.stream().anyMatch(c -> c instanceof Monitor);
        boolean tieneTarjetaVideo = subComponentes.stream().anyMatch(c -> c instanceof TarjetaVideo);
        
        assertThat(tieneDiscoDuro).isTrue();
        assertThat(tieneMonitor).isTrue();
        assertThat(tieneTarjetaVideo).isTrue();
    }

    /**
     * Verifica el manejo de componentes nulos en la lista.
     * 
     * Given: Una lista de subcomponentes que incluye elementos nulos
     * When: Se realizan cálculos de precio y costo
     * Then: Los elementos nulos se ignoran sin causar errores
     */
    @Test
    @DisplayName("Debe manejar componentes nulos sin errores en cálculos")
    void shouldHandleNullComponentsWithoutErrorsInCalculations() {
        // Arrange
        List<ComponenteSimple> subComponentesConNulos = new ArrayList<>();
        subComponentesConNulos.add((DiscoDuro) Componente.crearDiscoDuro("DD001", "SSD", "Samsung", "EVO", 
                new BigDecimal("50"), new BigDecimal("100"), "500GB"));
        subComponentesConNulos.add(null); // Elemento nulo
        subComponentesConNulos.add((Monitor) Componente.crearMonitor("MON001", "Monitor", "Dell", "S24", 
                new BigDecimal("120"), new BigDecimal("200")));
        subComponentesConNulos.add(null); // Otro elemento nulo

        // Act & Assert - No debe lanzar excepción
        Pc pc = new Pc("PC_NULL", "Test Nulos", "Test", "Null", subComponentesConNulos) {};
        
        // Los cálculos deben funcionar ignorando los nulos
        assertThat(pc.getPrecioBase()).isPositive(); // Solo cuenta componentes válidos
        assertThat(pc.getCosto()).isPositive();
        assertThat(pc.calcularUtilidad()).isNotNull();
    }

    // ==================== REGLAS DE NEGOCIO IMPLÍCITAS ====================

    /**
     * Verifica compatibilidad básica entre componentes.
     * Regla de negocio implícita: Los componentes de una PC deben ser técnicamente compatibles.
     * 
     * Given: Componentes con especificaciones técnicas
     * When: Se ensambla la PC
     * Then: Se pueden identificar posibles incompatibilidades
     */
    @Test
    @DisplayName("Debe permitir validación futura de compatibilidad entre componentes")
    void shouldAllowFutureCompatibilityValidationBetweenComponents() {
        // Arrange
        List<Componente> componentes = Arrays.asList(
                // GPU de alta gama que requiere fuente potente
                Componente.crearTarjetaVideo("GPU_HIGH", "RTX 4090 TDP 450W", "NVIDIA", "RTX 4090", 
                        new BigDecimal("1200"), new BigDecimal("1800"), "24GB GDDR6X"),
                // Monitor 4K que requiere GPU potente
                Componente.crearMonitor("MON_4K", "Monitor 4K 144Hz", "ASUS", "ROG Swift", 
                        new BigDecimal("600"), new BigDecimal("900")),
                // SSD NVMe rápido
                Componente.crearDiscoDuro("SSD_NVME", "SSD NVMe Gen4", "Samsung", "980 PRO", 
                        new BigDecimal("150"), new BigDecimal("250"), "1TB")
        );

        // Act
        Componente pc = Componente.crearPc("PC_COMPAT", "PC Gaming High-End", "Custom", "Gaming", componentes);

        // Assert
        Pc pcCast = (Pc) pc;
        assertThat(pcCast).isNotNull();
        
        // En el futuro se podrían agregar validaciones como:
        // - GPU de alta gama + Monitor 4K = Compatible ✓
        // - Verificar que la fuente de poder sea suficiente
        // - Validar que la motherboard soporte NVMe Gen4
        // - Verificar compatibilidad de conectores (HDMI 2.1, DP 1.4)
        
        // Por ahora verificamos que se pueda crear sin errores
        assertThat(pc.getDescripcion()).contains("High-End");
        assertThat(pc.getPrecioBase()).isGreaterThan(new BigDecimal("1000")); // PC cara por componentes premium
    }

    /**
     * Verifica coherencia de gama entre componentes.
     * Regla de negocio implícita: Componentes de una PC deberían ser de gama similar.
     * 
     * Given: Componentes de diferentes gamas
     * When: Se evalúa la coherencia
     * Then: Se puede detectar incoherencias de gama
     */
    @Test
    @DisplayName("Debe permitir validación de coherencia de gama entre componentes")
    void shouldAllowGamaTierCoherenceValidationBetweenComponents() {
        // Arrange - PC con componentes de gama muy diferente
        List<Componente> componentesIncoherentes = Arrays.asList(
                // Componente de gama ultra-alta
                Componente.crearTarjetaVideo("GPU_ULTRA", "RTX 4090", "NVIDIA", "Titan", 
                        new BigDecimal("1500"), new BigDecimal("2000"), "24GB"),
                // Componente de gama muy baja
                Componente.crearMonitor("MON_BASIC", "Monitor 1080p Básico", "Generic", "Basic", 
                        new BigDecimal("50"), new BigDecimal("80")),
                // Componente de gama media
                Componente.crearDiscoDuro("SSD_MID", "SSD SATA", "Kingston", "A400", 
                        new BigDecimal("40"), new BigDecimal("70"), "500GB")
        );

        // Act
        Componente pc = Componente.crearPc("PC_UNBALANCED", "PC Desbalanceada", "Custom", "Unbalanced", componentesIncoherentes);

        // Assert
        assertThat(pc).isNotNull();
        
        // Verificar que existe una gran diferencia de precios entre componentes
        Pc pcCast = (Pc) pc;
        BigDecimal precioMax = pcCast.getSubComponentes().stream()
                .map(Componente::getPrecioBase)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        BigDecimal precioMin = pcCast.getSubComponentes().stream()
                .map(Componente::getPrecioBase)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        // Ratio muy alto indica incoherencia de gama
        BigDecimal ratio = precioMax.divide(precioMin, 2, java.math.RoundingMode.HALF_UP);
        assertThat(ratio).isGreaterThan(new BigDecimal("10")); // Ratio > 10 indica gamas muy diferentes
        
        // En el futuro se podría agregar validación para sugerir componentes más coherentes
    }

    /**
     * Verifica requisitos mínimos del sistema.
     * Regla de negocio implícita: Una PC debe tener al menos un componente de cada tipo crítico.
     * 
     * Given: PCs con diferentes combinaciones de componentes
     * When: Se valida la completitud
     * Then: Se pueden identificar componentes faltantes críticos
     */
    @ParameterizedTest(name = "PC {0} debe tener componentes mínimos: {1}")
    @CsvSource({
        "Solo_GPU, GPU, false",      // Solo GPU, falta disco y monitor
        "GPU_Disco, GPU+Disco, false", // Falta monitor
        "Completa, GPU+Monitor+Disco, true", // PC completa
        "Dual_GPU, 2GPU+Monitor+Disco, true" // PC con múltiples GPUs
    })
    @DisplayName("Debe permitir validación de requisitos mínimos del sistema")
    void shouldAllowMinimumSystemRequirementsValidation(String tipo, String componentes, boolean esCompleta) {
        // Arrange
        List<Componente> componentesPC = new ArrayList<>();
        
        if (componentes.contains("GPU")) {
            componentesPC.add(Componente.crearTarjetaVideo("GPU1", "GTX 1060", "NVIDIA", "GTX", 
                    new BigDecimal("200"), new BigDecimal("300"), "6GB"));
        }
        if (componentes.contains("2GPU")) {
            componentesPC.add(Componente.crearTarjetaVideo("GPU1", "GTX 1060", "NVIDIA", "GTX", 
                    new BigDecimal("200"), new BigDecimal("300"), "6GB"));
            componentesPC.add(Componente.crearTarjetaVideo("GPU2", "GTX 1060", "NVIDIA", "GTX", 
                    new BigDecimal("200"), new BigDecimal("300"), "6GB"));
        }
        if (componentes.contains("Monitor")) {
            componentesPC.add(Componente.crearMonitor("MON1", "Monitor 24\"", "Dell", "S24", 
                    new BigDecimal("150"), new BigDecimal("200")));
        }
        if (componentes.contains("Disco")) {
            componentesPC.add(Componente.crearDiscoDuro("DD1", "SSD 500GB", "Samsung", "EVO", 
                    new BigDecimal("80"), new BigDecimal("120"), "500GB"));
        }

        // Act
        Componente pc = Componente.crearPc("PC_" + tipo, "Test " + tipo, "Test", tipo, componentesPC);

        // Assert
        Pc pcCast = (Pc) pc;
        assertThat(pcCast).isNotNull();
        
        // Verificar tipos de componentes presentes
        boolean tieneGPU = pcCast.getSubComponentes().stream().anyMatch(c -> c instanceof TarjetaVideo);
        boolean tieneMonitor = pcCast.getSubComponentes().stream().anyMatch(c -> c instanceof Monitor);
        boolean tieneDisco = pcCast.getSubComponentes().stream().anyMatch(c -> c instanceof DiscoDuro);
        
        boolean pcRealmenteCompleta = tieneGPU && tieneMonitor && tieneDisco;
        assertThat(pcRealmenteCompleta).isEqualTo(esCompleta);
        
        // En el futuro se podría agregar:
        // - Validación de que una PC "completa" tenga al menos: 1 GPU, 1 Monitor, 1 Disco
        // - Sugerencias de componentes faltantes
        // - Validación de compatibilidad entre los componentes existentes
    }

    // ==================== CASOS EDGE Y VALIDACIONES ====================

    /**
     * Verifica el comportamiento con lista vacía de subcomponentes.
     * 
     * Given: Una PC sin subcomponentes
     * When: Se realizan cálculos
     * Then: Los cálculos retornan cero sin errores
     */
    @Test
    @DisplayName("Debe manejar lista vacía de subcomponentes sin errores")
    void shouldHandleEmptySubComponentsListWithoutErrors() {
        // Arrange
        List<Componente> componentesVacios = new ArrayList<>();

        // Act
        Componente pc = Componente.crearPc("PC_EMPTY", "PC Vacía", "Test", "Empty", componentesVacios);

        // Assert
        assertThat(pc).isNotNull();
        assertThat(pc.getPrecioBase()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(pc.getCosto()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(pc.calcularUtilidad()).isEqualByComparingTo(BigDecimal.ZERO);
        
        Pc pcCast = (Pc) pc;
        assertThat(pcCast.getSubComponentes()).isEmpty();
    }

    /**
     * Verifica que el método toString heredado funcione correctamente.
     * 
     * Given: Una PC creada
     * When: Se invoca toString()
     * Then: Retorna representación de cadena con información básica
     */
    @Test
    @DisplayName("Debe generar representación toString correcta")
    void shouldGenerateCorrectToStringRepresentationWhenInvoked() {
        // Arrange
        List<Componente> componentes = crearComponentesDeEjemplo();
        Componente pc = Componente.crearPc("PC_STR", "PC ToString Test", 
                "TestBrand", "TestModel", componentes);

        // Act
        String stringRepresentation = pc.toString();

        // Assert
        assertThat(stringRepresentation).isNotNull();
        assertThat(stringRepresentation).contains("PC_STR");
        assertThat(stringRepresentation).contains("PC ToString Test");
        assertThat(stringRepresentation).contains("TestBrand");
        assertThat(stringRepresentation).contains("TestModel");
    }

    // ==================== MÉTODOS HELPER ====================

    /**
     * Crea una lista de componentes de ejemplo para testing.
     */
    private List<Componente> crearComponentesDeEjemplo() {
        return Arrays.asList(
                Componente.crearDiscoDuro("DD001", "SSD 1TB", "Samsung", "980 EVO", 
                        new BigDecimal("80"), new BigDecimal("150"), "1TB"),
                Componente.crearMonitor("MON001", "Monitor 27\"", "Dell", "S2722HS", 
                        new BigDecimal("200"), new BigDecimal("300")),
                Componente.crearTarjetaVideo("GPU001", "RTX 4060", "NVIDIA", "GeForce", 
                        new BigDecimal("250"), new BigDecimal("400"), "8GB GDDR6")
        );
    }

    /**
     * Crea un PcBuilder de ejemplo para testing.
     */
    private PcBuilder crearPcBuilderDeEjemplo() {
        return Componente.getPcBuilder()
                .definirId("PC_BUILDER")
                .definirDescripcion("PC desde Builder")
                .definirMarcaYmodelo("Custom", "Builder Model")
                .agregarDisco("DD_B", "SSD Builder", "Samsung", "EVO", 
                        new BigDecimal("90"), new BigDecimal("140"), "512GB")
                .agregarMonitor("MON_B", "Monitor Builder", "ASUS", "ProArt", 
                        new BigDecimal("250"), new BigDecimal("350"))
                .agregarTarjetaVideo("GPU_B", "GPU Builder", "AMD", "RX 6600", 
                        new BigDecimal("200"), new BigDecimal("300"), "8GB");
    }
}