package mx.com.qtx.cotizador.dominio.core.componentes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests unitarios para la funcionalidad de PcBuilder (Patrón Builder).
 * Valida el patrón Builder implementado para la construcción de PCs,
 * verificando las reglas de negocio para la composición de componentes
 * y las validaciones de rangos mínimos y máximos.
 * 
 * Casos cubiertos:
 * - Validación de rangos mínimos y máximos de componentes:
 *   * 1-2 monitores (MIN_MONITORES=1, MAX_MONITORES=2)
 *   * 1-2 tarjetas de video (MIN_TARJETAS=1, MAX_TARJETAS=2)
 *   * 1-3 discos duros (MIN_DISCOS=1, MAX_DISCOS=3)
 * - Fluent interface del builder (encadenamiento de métodos)
 * - Build validation (PC válida construida)
 * - Error scenarios (componentes insuficientes/excesivos/parámetros nulos)
 * - Reglas de negocio implícitas identificadas
 * - Métodos del Builder Pattern (definir propiedades, agregar componentes, build)
 * 
 * Algoritmos probados:
 * 1. Fluent Interface: builder.definirId().definirDescripcion().agregarDisco().build()
 * 2. Validación de límites: ignora componentes que excedan MAX_* 
 * 3. Validación de PC válida: pcEsValida() (actualmente comentada pero probada)
 * 4. Construcción de PC desde PcBuilder
 * 5. Manejo de parámetros nulos en métodos de definición
 * 6. Comportamiento cuando se exceden límites máximos
 *
 * Constantes del dominio:
 * - MIN_MONITORES = 1, MAX_MONITORES = 2
 * - MIN_TARJETAS = 1, MAX_TARJETAS = 2  
 * - MIN_DISCOS = 1, MAX_DISCOS = 3
 *
 * @author Claude Code
 * @version 1.0
 * @since 2025-01-10
 */
@DisplayName("PcBuilder - Tests de Patrón Builder con Validaciones")
class PcBuilderTest {

    private PcBuilder builder;

    @BeforeEach
    void setUp() {
        builder = Componente.getPcBuilder();
    }

    // ==================== CREACIÓN Y PROPIEDADES BÁSICAS ====================

    /**
     * Verifica que se pueda crear un PcBuilder correctamente.
     * 
     * Given: Una nueva instancia de PcBuilder
     * When: Se obtiene mediante factory method
     * Then: Se crea correctamente con listas inicializadas
     */
    @Test
    @DisplayName("Debe crear PcBuilder correctamente con factory method")
    void shouldCreatePcBuilderSuccessfullyWhenObtainedFromFactory() {
        // Arrange & Act
        PcBuilder newBuilder = Componente.getPcBuilder();

        // Assert
        assertThat(newBuilder).isNotNull();
        assertThat(newBuilder.getDiscos()).isNotNull().isEmpty();
        assertThat(newBuilder.getMonitores()).isNotNull().isEmpty();
        assertThat(newBuilder.getTarjetas()).isNotNull().isEmpty();
        assertThat(newBuilder.getIdPc()).isNull();
        assertThat(newBuilder.getDescripcionPc()).isNull();
        assertThat(newBuilder.getMarcaPc()).isNull();
        assertThat(newBuilder.getModeloPc()).isNull();
    }

    // ==================== FLUENT INTERFACE TESTING ====================

    /**
     * Verifica que la fluent interface funcione correctamente con encadenamiento de métodos.
     * 
     * Given: Un PcBuilder vacío
     * When: Se encadenan métodos de definición y agregado de componentes
     * Then: Todos los métodos retornan la misma instancia para encadenamiento
     */
    @Test
    @DisplayName("Debe soportar fluent interface con encadenamiento de métodos")
    void shouldSupportFluentInterfaceWhenMethodsChained() {
        // Arrange & Act
        PcBuilder result = builder
                .definirId("PC_FLUENT")
                .definirDescripcion("PC Fluent Interface")
                .definirMarcaYmodelo("TestBrand", "TestModel")
                .agregarDisco("DD1", "SSD", "Samsung", "EVO", new BigDecimal("80"), new BigDecimal("120"), "500GB")
                .agregarMonitor("MON1", "Monitor", "Dell", "S24", new BigDecimal("150"), new BigDecimal("200"))
                .agregarTarjetaVideo("GPU1", "GPU", "NVIDIA", "RTX", new BigDecimal("300"), new BigDecimal("450"), "8GB");

        // Assert
        assertThat(result).isSameAs(builder); // Mismo objeto
        assertThat(builder.getIdPc()).isEqualTo("PC_FLUENT");
        assertThat(builder.getDescripcionPc()).isEqualTo("PC Fluent Interface");
        assertThat(builder.getMarcaPc()).isEqualTo("TestBrand");
        assertThat(builder.getModeloPc()).isEqualTo("TestModel");
        assertThat(builder.getDiscos()).hasSize(1);
        assertThat(builder.getMonitores()).hasSize(1);
        assertThat(builder.getTarjetas()).hasSize(1);
    }

    // ==================== DEFINICIÓN DE PROPIEDADES ====================

    /**
     * Verifica que el método definirId funcione correctamente.
     * 
     * Given: Un PcBuilder
     * When: Se define un ID
     * Then: El ID se asigna correctamente y se retorna el builder
     */
    @Test
    @DisplayName("Debe definir ID correctamente")
    void shouldSetIdCorrectlyWhenDefinirIdCalled() {
        // Arrange
        String expectedId = "PC_TEST_001";

        // Act
        PcBuilder result = builder.definirId(expectedId);

        // Assert
        assertThat(result).isSameAs(builder);
        assertThat(builder.getIdPc()).isEqualTo(expectedId);
    }

    /**
     * Verifica que el método definirDescripcion funcione correctamente.
     * 
     * Given: Un PcBuilder
     * When: Se define una descripción
     * Then: La descripción se asigna correctamente y se retorna el builder
     */
    @Test
    @DisplayName("Debe definir descripción correctamente")
    void shouldSetDescriptionCorrectlyWhenDefinirDescripcionCalled() {
        // Arrange
        String expectedDesc = "PC Gaming de Alta Gama";

        // Act
        PcBuilder result = builder.definirDescripcion(expectedDesc);

        // Assert
        assertThat(result).isSameAs(builder);
        assertThat(builder.getDescripcionPc()).isEqualTo(expectedDesc);
    }

    /**
     * Verifica que el método definirMarcaYmodelo funcione correctamente.
     * 
     * Given: Un PcBuilder
     * When: Se define marca y modelo
     * Then: Ambos se asignan correctamente y se retorna el builder
     */
    @Test
    @DisplayName("Debe definir marca y modelo correctamente")
    void shouldSetBrandAndModelCorrectlyWhenDefinirMarcaYmodeloCall() {
        // Arrange
        String expectedMarca = "Custom Build";
        String expectedModelo = "Gaming Rig 2025";

        // Act
        PcBuilder result = builder.definirMarcaYmodelo(expectedMarca, expectedModelo);

        // Assert
        assertThat(result).isSameAs(builder);
        assertThat(builder.getMarcaPc()).isEqualTo(expectedMarca);
        assertThat(builder.getModeloPc()).isEqualTo(expectedModelo);
    }

    // ==================== VALIDACIÓN DE RANGOS MÍNIMOS Y MÁXIMOS ====================

    /**
     * Verifica que el builder permite agregar tantos monitores como se desee y valida límites al construir.
     * MIN_MONITORES = 1, MAX_MONITORES = 2
     *
     * Given: Un PcBuilder
     * When: Se agregan monitores excediendo el máximo permitido
     * Then: El builder permite agregar todos los monitores y build() falla con excepción
     */
    @Test
    @DisplayName("Debe permitir agregar monitores y validar límites al construir: MIN=1, MAX=2")
    void shouldRespectMonitorLimitsWhenAddingMonitors() {
        // Arrange & Act
        builder.agregarMonitor("MON1", "Monitor 1", "Dell", "S24", new BigDecimal("150"), new BigDecimal("200"))
               .agregarMonitor("MON2", "Monitor 2", "ASUS", "PA24", new BigDecimal("200"), new BigDecimal("300"))
               .agregarMonitor("MON3", "Monitor 3", "LG", "UL24", new BigDecimal("180"), new BigDecimal("250"));

        // Assert - Builder debe permitir agregar todos los componentes
        assertThat(builder.getMonitores()).hasSize(3); // Se agregaron los 3 monitores
        assertThat(builder.getMonitores().get(0).getId()).isEqualTo("MON1");
        assertThat(builder.getMonitores().get(1).getId()).isEqualTo("MON2");
        assertThat(builder.getMonitores().get(2).getId()).isEqualTo("MON3");

        // Assert - build() debe fallar por exceder el máximo de monitores (2)
        builder.definirId("PC002").definirDescripcion("Test PC").definirMarcaYmodelo("TestBrand", "TestModel")
               .agregarDisco("DD1", "Disco Test", "Samsung", "EVO", new BigDecimal("80"), new BigDecimal("120"), "500GB")
               .agregarTarjetaVideo("GPU1", "GPU Test", "NVIDIA", "RTX", new BigDecimal("400"), new BigDecimal("600"), "8GB");

        assertThatThrownBy(() -> builder.build())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Estructura Pc Invalida");
    }

    /**
     * Verifica que el builder permite agregar tantas tarjetas como se desee y valida límites al construir.
     * MIN_TARJETAS = 1, MAX_TARJETAS = 2
     *
     * Given: Un PcBuilder
     * When: Se agregan tarjetas excediendo el máximo permitido
     * Then: El builder permite agregar todas las tarjetas y build() falla con excepción
     */
    @Test
    @DisplayName("Debe permitir agregar tarjetas y validar límites al construir: MIN=1, MAX=2")
    void shouldRespectVideoCardLimitsWhenAddingVideoCards() {
        // Arrange & Act
        builder.agregarTarjetaVideo("GPU1", "RTX 4070", "NVIDIA", "GeForce", new BigDecimal("400"), new BigDecimal("600"), "12GB")
               .agregarTarjetaVideo("GPU2", "RTX 4060", "NVIDIA", "GeForce", new BigDecimal("300"), new BigDecimal("450"), "8GB")
               .agregarTarjetaVideo("GPU3", "RX 7700", "AMD", "Radeon", new BigDecimal("350"), new BigDecimal("500"), "12GB");

        // Assert - Builder debe permitir agregar todos los componentes
        assertThat(builder.getTarjetas()).hasSize(3); // Se agregaron las 3 tarjetas
        assertThat(builder.getTarjetas().get(0).getId()).isEqualTo("GPU1");
        assertThat(builder.getTarjetas().get(1).getId()).isEqualTo("GPU2");
        assertThat(builder.getTarjetas().get(2).getId()).isEqualTo("GPU3");

        // Assert - build() debe fallar por exceder el máximo de tarjetas (2)
        builder.definirId("PC003").definirDescripcion("Test PC").definirMarcaYmodelo("TestBrand", "TestModel")
               .agregarDisco("DD1", "Disco Test", "Samsung", "EVO", new BigDecimal("80"), new BigDecimal("120"), "500GB")
               .agregarMonitor("MON1", "Monitor Test", "Dell", "S24", new BigDecimal("150"), new BigDecimal("200"));

        assertThatThrownBy(() -> builder.build())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Estructura Pc Invalida");
    }

    /**
     * Verifica que el builder permite agregar tantos discos como se desee y valida límites al construir.
     * MIN_DISCOS = 1, MAX_DISCOS = 3
     *
     * Given: Un PcBuilder
     * When: Se agregan discos excediendo el máximo permitido
     * Then: El builder permite agregar todos los discos y build() falla con excepción
     */
    @Test
    @DisplayName("Debe permitir agregar discos y validar límites al construir: MIN=1, MAX=3")
    void shouldRespectHardDriveLimitsWhenAddingHardDrives() {
        // Arrange & Act
        builder.agregarDisco("DD1", "SSD Sistema", "Samsung", "980 EVO", new BigDecimal("80"), new BigDecimal("120"), "500GB")
               .agregarDisco("DD2", "HDD Datos", "WD", "Black", new BigDecimal("60"), new BigDecimal("90"), "2TB")
               .agregarDisco("DD3", "SSD Cache", "Intel", "Optane", new BigDecimal("100"), new BigDecimal("150"), "256GB")
               .agregarDisco("DD4", "HDD Backup", "Seagate", "Barracuda", new BigDecimal("50"), new BigDecimal("80"), "4TB");

        // Assert - Builder debe permitir agregar todos los componentes
        assertThat(builder.getDiscos()).hasSize(4); // Se agregaron los 4 discos
        assertThat(builder.getDiscos().get(0).getId()).isEqualTo("DD1");
        assertThat(builder.getDiscos().get(1).getId()).isEqualTo("DD2");
        assertThat(builder.getDiscos().get(2).getId()).isEqualTo("DD3");
        assertThat(builder.getDiscos().get(3).getId()).isEqualTo("DD4");

        // Assert - build() debe fallar por exceder el máximo de discos (3)
        builder.definirId("PC001").definirDescripcion("Test PC").definirMarcaYmodelo("TestBrand", "TestModel")
               .agregarMonitor("MON1", "Monitor Test", "Dell", "S24", new BigDecimal("150"), new BigDecimal("200"))
               .agregarTarjetaVideo("GPU1", "GPU Test", "NVIDIA", "RTX", new BigDecimal("400"), new BigDecimal("600"), "8GB");

        assertThatThrownBy(() -> builder.build())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Estructura Pc Invalida");
    }

    /**
     * Verifica los valores de las constantes MIN y MAX del builder.
     * 
     * Given: Las constantes de PcBuilder
     * When: Se obtienen los valores
     * Then: Los valores son los esperados según especificación
     */
    @Test
    @DisplayName("Debe tener constantes MIN/MAX correctas según especificación")
    void shouldHaveCorrectMinMaxConstantsAsPerSpecification() {
        // Assert
        assertThat(PcBuilder.getMinMonitores()).isEqualTo(1);
        assertThat(PcBuilder.getMaxMonitores()).isEqualTo(2);
        assertThat(PcBuilder.getMinTarjetas()).isEqualTo(1);
        assertThat(PcBuilder.getMaxTarjetas()).isEqualTo(2);
        assertThat(PcBuilder.getMinDiscos()).isEqualTo(1);
        assertThat(PcBuilder.getMaxDiscos()).isEqualTo(3);
    }

    // ==================== BUILD VALIDATION ====================

    /**
     * Verifica que se pueda construir una PC válida con componentes mínimos.
     * 
     * Given: Un PcBuilder con configuración mínima válida
     * When: Se invoca build()
     * Then: Se construye la PC correctamente
     */
    @Test
    @DisplayName("Debe construir PC válida con configuración mínima")
    void shouldBuildValidPcWhenMinimumValidConfigurationProvided() {
        // Arrange
        builder.definirId("PC_MIN")
               .definirDescripcion("PC Mínima")
               .definirMarcaYmodelo("TestBrand", "TestModel")
               .agregarDisco("DD1", "SSD", "Samsung", "EVO", new BigDecimal("80"), new BigDecimal("120"), "500GB")
               .agregarMonitor("MON1", "Monitor", "Dell", "S24", new BigDecimal("150"), new BigDecimal("200"))
               .agregarTarjetaVideo("GPU1", "GPU", "NVIDIA", "RTX", new BigDecimal("300"), new BigDecimal("450"), "8GB");

        // Act
        Pc pc = builder.build();

        // Assert
        assertThat(pc).isNotNull();
        assertThat(pc.getId()).isEqualTo("PC_MIN");
        assertThat(pc.getDescripcion()).isEqualTo("PC Mínima");
        assertThat(pc.getMarca()).isEqualTo("TestBrand");
        assertThat(pc.getModelo()).isEqualTo("TestModel");
        assertThat(pc.getSubComponentes()).hasSize(3); // 1 disco + 1 monitor + 1 GPU
    }

    /**
     * Verifica que se pueda construir una PC con configuración máxima.
     * 
     * Given: Un PcBuilder con configuración máxima de componentes
     * When: Se invoca build()
     * Then: Se construye la PC con todos los componentes máximos
     */
    @Test
    @DisplayName("Debe construir PC con configuración máxima de componentes")
    void shouldBuildPcWithMaximumComponentConfigurationWhenAllMaxReached() {
        // Arrange
        builder.definirId("PC_MAX")
               .definirDescripcion("PC Máxima")
               .definirMarcaYmodelo("MaxBrand", "MaxModel")
               // Máximo de discos (3)
               .agregarDisco("DD1", "SSD Sistema", "Samsung", "980", new BigDecimal("100"), new BigDecimal("150"), "512GB")
               .agregarDisco("DD2", "HDD Datos", "WD", "Black", new BigDecimal("80"), new BigDecimal("120"), "2TB")
               .agregarDisco("DD3", "SSD Cache", "Intel", "Optane", new BigDecimal("120"), new BigDecimal("180"), "256GB")
               // Máximo de monitores (2)
               .agregarMonitor("MON1", "Monitor Principal", "ASUS", "ProArt", new BigDecimal("300"), new BigDecimal("450"))
               .agregarMonitor("MON2", "Monitor Secundario", "Dell", "UltraSharp", new BigDecimal("250"), new BigDecimal("350"))
               // Máximo de tarjetas (2)
               .agregarTarjetaVideo("GPU1", "RTX 4090", "NVIDIA", "GeForce", new BigDecimal("1200"), new BigDecimal("1800"), "24GB")
               .agregarTarjetaVideo("GPU2", "RTX 4070", "NVIDIA", "GeForce", new BigDecimal("400"), new BigDecimal("600"), "12GB");

        // Act
        Pc pc = builder.build();

        // Assert
        assertThat(pc).isNotNull();
        assertThat(pc.getSubComponentes()).hasSize(7); // 3 discos + 2 monitores + 2 GPUs
        
        // Verificar conteos por tipo
        long discosCount = pc.getSubComponentes().stream().filter(c -> c instanceof DiscoDuro).count();
        long monitoresCount = pc.getSubComponentes().stream().filter(c -> c instanceof Monitor).count();
        long tarjetasCount = pc.getSubComponentes().stream().filter(c -> c instanceof TarjetaVideo).count();
        
        assertThat(discosCount).isEqualTo(3);
        assertThat(monitoresCount).isEqualTo(2);
        assertThat(tarjetasCount).isEqualTo(2);
    }

    // ==================== ERROR SCENARIOS ====================

    /**
     * Verifica el comportamiento cuando se intenta construir con parámetros nulos.
     * 
     * Given: Un PcBuilder con algunos parámetros nulos
     * When: Se definen valores nulos
     * Then: El builder acepta los valores nulos sin errores
     */
    @Test
    @DisplayName("Debe manejar parámetros nulos sin errores")
    void shouldHandleNullParametersWithoutErrorsWhenSet() {
        // Arrange & Act
        PcBuilder result = builder.definirId(null)
                                 .definirDescripcion(null)
                                 .definirMarcaYmodelo(null, null);

        // Assert
        assertThat(result).isSameAs(builder);
        assertThat(builder.getIdPc()).isNull();
        assertThat(builder.getDescripcionPc()).isNull();
        assertThat(builder.getMarcaPc()).isNull();
        assertThat(builder.getModeloPc()).isNull();
    }

    /**
     * Verifica que el builder falla correctamente cuando la configuración está incompleta.
     *
     * Given: Un PcBuilder con configuración incompleta (sin propiedades básicas y componentes mínimos)
     * When: Se invoca build()
     * Then: Se lanza excepción indicando estructura inválida
     */
    @Test
    @DisplayName("Debe fallar build con configuración incompleta")
    void shouldAllowBuildWithIncompleteConfigurationWhenValidationDisabled() {
        // Arrange - PC con solo disco, sin propiedades básicas ni componentes mínimos
        builder.agregarDisco("DD1", "SSD", "Samsung", "EVO", new BigDecimal("80"), new BigDecimal("120"), "500GB");

        // Act & Assert - Debe lanzar excepción por configuración incompleta
        assertThatThrownBy(() -> builder.build())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Estructura Pc Invalida");
    }

    // ==================== REGLAS DE NEGOCIO IMPLÍCITAS ====================

    /**
     * Verifica validaciones futuras de compatibilidad entre componentes.
     * Regla de negocio implícita: Los componentes agregados deberían ser compatibles.
     * 
     * Given: Componentes con diferentes niveles de gama
     * When: Se agregan al builder
     * Then: Se pueden identificar incoherencias para validación futura
     */
    @Test
    @DisplayName("Debe permitir validación futura de compatibilidad entre componentes")
    void shouldAllowFutureCompatibilityValidationBetweenComponents() {
        // Arrange - Componentes de gamas muy diferentes
        builder.definirId("PC_COMPAT")
               .definirDescripcion("PC para validar compatibilidad")
               .definirMarcaYmodelo("Test", "Compatibility")
               // GPU ultra alta gama
               .agregarTarjetaVideo("GPU_ULTRA", "RTX 4090 TDP 450W", "NVIDIA", "GeForce RTX 4090", 
                       new BigDecimal("1500"), new BigDecimal("2000"), "24GB GDDR6X")
               // Monitor básico (incoherencia potencial)
               .agregarMonitor("MON_BASIC", "Monitor 1080p Básico 60Hz", "Generic", "Basic", 
                       new BigDecimal("50"), new BigDecimal("80"))
               // Disco adecuado
               .agregarDisco("SSD_FAST", "SSD NVMe Gen4", "Samsung", "980 PRO", 
                       new BigDecimal("150"), new BigDecimal("250"), "1TB");

        // Act
        Pc pc = builder.build();

        // Assert
        assertThat(pc).isNotNull();
        
        // En el futuro se podrían agregar validaciones como:
        // - GPU alta gama + Monitor básico = Advertencia de cuello de botella
        // - Verificar que el monitor soporte las capacidades de la GPU
        // - Validar que el SSD sea suficientemente rápido para la configuración
        
        // Por ahora verificamos que la construcción sea exitosa
        assertThat(pc.getSubComponentes()).hasSize(3);
        
        // Verificar incoherencia de precios como indicador de gamas diferentes
        BigDecimal precioGPU = pc.getSubComponentes().stream()
                .filter(c -> c instanceof TarjetaVideo)
                .map(Componente::getPrecioBase)
                .findFirst()
                .orElse(BigDecimal.ZERO);
        
        BigDecimal precioMonitor = pc.getSubComponentes().stream()
                .filter(c -> c instanceof Monitor)
                .map(Componente::getPrecioBase)
                .findFirst()
                .orElse(BigDecimal.ZERO);
        
        // Ratio muy alto indica incoherencia de gama
        BigDecimal ratio = precioGPU.divide(precioMonitor, 2, java.math.RoundingMode.HALF_UP);
        assertThat(ratio).isGreaterThan(new BigDecimal("10")); // GPU cuesta >10x que el monitor
    }

    /**
     * Verifica coherencia de gama en la configuración construida.
     * Regla de negocio implícita: Una PC coherente tiene componentes de gama similar.
     * 
     * Given: Configuraciones de PC con diferentes niveles de coherencia
     * When: Se construyen las PCs
     * Then: Se puede evaluar la coherencia entre componentes
     */
    @ParameterizedTest(name = "PC {0} debe tener coherencia de gama: {1}")
    @CsvSource({
        "Gaming_Basica, GPU_Media+Monitor_Basico+SSD_Medio, Media",
        "Gaming_Alta, GPU_Alta+Monitor_Gaming+SSD_Rapido, Alta", 
        "Workstation, GPU_Pro+Monitor_4K+SSD_Enterprise, Profesional",
        "Budget, GPU_Basica+Monitor_1080p+HDD_Basico, Basica"
    })
    @DisplayName("Debe permitir evaluación de coherencia de gama entre componentes")
    void shouldAllowGamaTierCoherenceEvaluationBetweenComponents(String tipo, String componentes, String gamaEsperada) {
        // Arrange
        builder.definirId("PC_" + tipo)
               .definirDescripcion("PC " + tipo + " para coherencia")
               .definirMarcaYmodelo("Test", tipo);
        
        // Configurar componentes según gama
        switch (gamaEsperada) {
            case "Basica":
                builder.agregarTarjetaVideo("GPU", "GTX 1650", "NVIDIA", "GTX", new BigDecimal("100"), new BigDecimal("150"), "4GB")
                       .agregarMonitor("MON", "Monitor 1080p", "Generic", "Basic", new BigDecimal("80"), new BigDecimal("120"))
                       .agregarDisco("DD", "HDD 1TB", "WD", "Blue", new BigDecimal("40"), new BigDecimal("60"), "1TB");
                break;
            case "Media":
                builder.agregarTarjetaVideo("GPU", "RTX 4060", "NVIDIA", "RTX", new BigDecimal("250"), new BigDecimal("350"), "8GB")
                       .agregarMonitor("MON", "Monitor 1440p", "ASUS", "Gaming", new BigDecimal("200"), new BigDecimal("300"))
                       .agregarDisco("DD", "SSD 500GB", "Samsung", "EVO", new BigDecimal("80"), new BigDecimal("120"), "500GB");
                break;
            case "Alta":
                builder.agregarTarjetaVideo("GPU", "RTX 4080", "NVIDIA", "RTX", new BigDecimal("800"), new BigDecimal("1200"), "16GB")
                       .agregarMonitor("MON", "Monitor 4K Gaming", "ASUS", "ROG", new BigDecimal("600"), new BigDecimal("900"))
                       .agregarDisco("DD", "SSD NVMe 1TB", "Samsung", "980 PRO", new BigDecimal("150"), new BigDecimal("250"), "1TB");
                break;
            case "Profesional":
                builder.agregarTarjetaVideo("GPU", "Quadro RTX A6000", "NVIDIA", "Quadro", new BigDecimal("3000"), new BigDecimal("4500"), "48GB")
                       .agregarMonitor("MON", "Monitor 4K Profesional", "BenQ", "SW321C", new BigDecimal("1200"), new BigDecimal("1800"))
                       .agregarDisco("DD", "SSD Enterprise", "Intel", "Optane", new BigDecimal("300"), new BigDecimal("500"), "1TB");
                break;
        }

        // Act
        Pc pc = builder.build();

        // Assert
        assertThat(pc).isNotNull();
        assertThat(pc.getSubComponentes()).hasSize(3);
        
        // Verificar coherencia de precios como indicador de gama
        BigDecimal precioPromedio = pc.getSubComponentes().stream()
                .map(Componente::getPrecioBase)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(pc.getSubComponentes().size()), 2, java.math.RoundingMode.HALF_UP);
        
        // Verificar rangos de precio por gama
        switch (gamaEsperada) {
            case "Basica":
                assertThat(precioPromedio).isBetween(new BigDecimal("50"), new BigDecimal("200"));
                break;
            case "Media":
                assertThat(precioPromedio).isBetween(new BigDecimal("150"), new BigDecimal("400"));
                break;
            case "Alta":
                assertThat(precioPromedio).isBetween(new BigDecimal("400"), new BigDecimal("1200"));
                break;
            case "Profesional":
                assertThat(precioPromedio).isGreaterThan(new BigDecimal("1000"));
                break;
        }
    }

    /**
     * Verifica validación de requisitos de alimentación eléctrica.
     * Regla de negocio implícita: La configuración debe considerar consumo energético.
     * 
     * Given: Componentes con diferentes requerimientos de energía
     * When: Se construye la PC
     * Then: Se puede evaluar si la configuración es viable energéticamente
     */
    @Test
    @DisplayName("Debe permitir validación futura de requisitos de alimentación eléctrica")
    void shouldAllowFuturePowerRequirementsValidationWhenConfigured() {
        // Arrange - Configuración con alto consumo energético
        builder.definirId("PC_HIGH_POWER")
               .definirDescripcion("PC Alto Consumo para validación energética")
               .definirMarcaYmodelo("Test", "HighPower")
               // Dual GPU de alta gama (muy alto consumo)
               .agregarTarjetaVideo("GPU1", "RTX 4090 TDP 450W", "NVIDIA", "RTX 4090", 
                       new BigDecimal("1500"), new BigDecimal("2000"), "24GB")
               .agregarTarjetaVideo("GPU2", "RTX 4080 TDP 320W", "NVIDIA", "RTX 4080", 
                       new BigDecimal("800"), new BigDecimal("1200"), "16GB")
               // Monitor de alto consumo
               .agregarMonitor("MON", "Monitor 4K 144Hz HDR", "ASUS", "ROG Swift", 
                       new BigDecimal("800"), new BigDecimal("1200"))
               // Múltiples discos
               .agregarDisco("SSD1", "SSD NVMe Gen4", "Samsung", "980 PRO", new BigDecimal("150"), new BigDecimal("250"), "1TB")
               .agregarDisco("SSD2", "SSD NVMe Gen4", "WD", "Black SN850X", new BigDecimal("180"), new BigDecimal("280"), "2TB")
               .agregarDisco("HDD", "HDD 10TB Enterprise", "Seagate", "IronWolf Pro", new BigDecimal("200"), new BigDecimal("300"), "10TB");

        // Act
        Pc pc = builder.build();

        // Assert
        assertThat(pc).isNotNull();
        assertThat(pc.getSubComponentes()).hasSize(6); // 2 GPUs + 1 Monitor + 3 Discos
        
        // En el futuro se podrían agregar validaciones como:
        // - Calcular TDP total estimado: GPUs (450W + 320W) + Monitor (~50W) + Discos (~30W) ≈ 850W
        // - Sugerir fuente de poder mínima: 1000W+ para configuración segura
        // - Validar eficiencia energética de la configuración
        // - Advertir sobre generación de calor y requisitos de refrigeración
        
        // Por ahora verificamos que la configuración sea técnicamente posible
        long gpuCount = pc.getSubComponentes().stream().filter(c -> c instanceof TarjetaVideo).count();
        assertThat(gpuCount).isEqualTo(2); // Dual GPU configurado correctamente
        
        // Verificar que sea una configuración de alta gama (precios altos)
        BigDecimal precioTotal = pc.getPrecioBase();
        assertThat(precioTotal).isGreaterThan(new BigDecimal("3000")); // PC muy cara = alto consumo
    }

    // ==================== CASOS EDGE Y VALIDACIONES ====================

    /**
     * Verifica que el método toString genere información útil.
     * 
     * Given: Un PcBuilder con componentes agregados
     * When: Se invoca toString()
     * Then: Retorna información detallada del estado del builder
     */
    @Test
    @DisplayName("Debe generar toString informativo con estado del builder")
    void shouldGenerateInformativeToStringWithBuilderState() {
        // Arrange
        builder.definirId("PC_STRING")
               .definirDescripcion("Test ToString")
               .definirMarcaYmodelo("TestBrand", "TestModel")
               .agregarDisco("DD1", "SSD", "Samsung", "EVO", new BigDecimal("80"), new BigDecimal("120"), "500GB")
               .agregarMonitor("MON1", "Monitor", "Dell", "S24", new BigDecimal("150"), new BigDecimal("200"));

        // Act
        String stringRepresentation = builder.toString();

        // Assert
        assertThat(stringRepresentation).isNotNull();
        assertThat(stringRepresentation).contains("PcBuilder");
        assertThat(stringRepresentation).contains("monitores(1)");
        assertThat(stringRepresentation).contains("discos(1)");
        assertThat(stringRepresentation).contains("tarjetas(0)");
        assertThat(stringRepresentation).contains("PC_STRING");
        assertThat(stringRepresentation).contains("Test ToString");
        assertThat(stringRepresentation).contains("TestBrand");
        assertThat(stringRepresentation).contains("TestModel");
    }

    /**
     * Verifica que el builder sea reutilizable después de build().
     * 
     * Given: Un PcBuilder que ya fue usado para build()
     * When: Se modifica y se usa para otro build()
     * Then: Se puede reutilizar correctamente
     */
    @Test
    @DisplayName("Debe ser reutilizable después de build() para crear múltiples PCs")
    void shouldBeReusableAfterBuildWhenCreatingMultiplePcs() {
        // Arrange - Primera PC
        builder.definirId("PC_FIRST")
               .definirDescripcion("Primera PC")
               .definirMarcaYmodelo("First", "PC1")
               .agregarDisco("DD1", "SSD", "Samsung", "EVO", new BigDecimal("80"), new BigDecimal("120"), "500GB")
               .agregarMonitor("MON1", "Monitor", "Dell", "S24", new BigDecimal("150"), new BigDecimal("200"))
               .agregarTarjetaVideo("GPU1", "GPU", "NVIDIA", "RTX", new BigDecimal("300"), new BigDecimal("450"), "8GB");

        // Act - Construir primera PC
        Pc firstPc = builder.build();

        // Modificar builder para segunda PC
        builder.definirId("PC_SECOND")
               .definirDescripcion("Segunda PC")
               .definirMarcaYmodelo("Second", "PC2")
               .agregarTarjetaVideo("GPU2", "GPU2", "AMD", "RX", new BigDecimal("350"), new BigDecimal("500"), "12GB");

        // Act - Construir segunda PC
        Pc secondPc = builder.build();

        // Assert
        assertThat(firstPc).isNotNull();
        assertThat(secondPc).isNotNull();
        assertThat(firstPc.getId()).isEqualTo("PC_FIRST");
        assertThat(secondPc.getId()).isEqualTo("PC_SECOND");
        assertThat(firstPc.getSubComponentes()).hasSize(3);
        assertThat(secondPc.getSubComponentes()).hasSize(4); // Componentes anteriores + GPU2 adicional
    }
}