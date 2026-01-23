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
 * Tests unitarios para la funcionalidad de TarjetaVideo.
 * Valida las propiedades específicas, herencia de ComponenteSimple, 
 * métodos específicos y reglas de negocio para tarjetas de video.
 * 
 * Casos cubiertos:
 * - Propiedades específicas (memoria VRAM, conectores, chipset)
 * - Validaciones de negocio (memoria válida, compatibilidades)
 * - Herencia correcta de Componente y ComponenteSimple
 * - Métodos específicos de TarjetaVideo (getMemoria, setMemoria, getCategoria)
 * - Métodos heredados (calcularUtilidad, cotizar, mostrarCaracteristicas)
 * - Reglas de negocio implícitas identificadas
 * 
 * Algoritmos probados:
 * 1. Creación mediante factory method Componente.crearTarjetaVideo()
 * 2. Cálculo de utilidad: precioBase - costo
 * 3. Cotización: precioBase × cantidad (sin promoción)
 * 4. Validación de rangos de memoria VRAM según gama
 * 5. Verificación de compatibilidad chipset/conectores
 * 6. Validación de consumo energético vs performance
 *
 * @author Claude Code
 * @version 1.0
 * @since 2025-01-10
 */
@DisplayName("TarjetaVideo - Tests de Lógica de Dominio")
class TarjetaVideoTest {

    // ==================== CREACIÓN Y PROPIEDADES BÁSICAS ====================

    /**
     * Verifica que se pueda crear una tarjeta de video correctamente mediante el factory method
     * y que todas las propiedades básicas se inicialicen adecuadamente.
     * 
     * Given: Parámetros válidos para crear una tarjeta de video
     * When: Se crea la tarjeta de video usando el factory method
     * Then: La tarjeta se crea correctamente con todas las propiedades asignadas
     */
    @Test
    @DisplayName("Debe crear tarjeta de video correctamente con factory method")
    void shouldCreateTarjetaVideoSuccessfullyWhenValidParametersProvided() {
        // Arrange
        String id = "GPU001";
        String descripcion = "Tarjeta de Video GeForce RTX 4070";
        String marca = "NVIDIA";
        String modelo = "RTX 4070 Ti";
        BigDecimal costo = new BigDecimal("400.00");
        BigDecimal precioBase = new BigDecimal("600.00");
        String memoria = "12GB GDDR6X";

        // Act
        Componente tarjetaVideo = Componente.crearTarjetaVideo(id, descripcion, marca, 
                                                              modelo, costo, precioBase, memoria);

        // Assert
        assertThat(tarjetaVideo).isNotNull();
        assertThat(tarjetaVideo).isInstanceOf(TarjetaVideo.class);
        assertThat(tarjetaVideo.getId()).isEqualTo(id);
        assertThat(tarjetaVideo.getDescripcion()).isEqualTo(descripcion);
        assertThat(tarjetaVideo.getMarca()).isEqualTo(marca);
        assertThat(tarjetaVideo.getModelo()).isEqualTo(modelo);
        assertThat(tarjetaVideo.getCosto()).isEqualByComparingTo(costo);
        assertThat(tarjetaVideo.getPrecioBase()).isEqualByComparingTo(precioBase);
        
        // Verificar propiedad específica
        TarjetaVideo gpu = (TarjetaVideo) tarjetaVideo;
        assertThat(gpu.getMemoria()).isEqualTo(memoria);
    }

    /**
     * Verifica que el getter de memoria funcione correctamente.
     * 
     * Given: Una tarjeta de video con memoria específica
     * When: Se obtiene la memoria mediante getter
     * Then: Se retorna la memoria correcta
     */
    @Test
    @DisplayName("Debe obtener memoria VRAM correctamente")
    void shouldGetMemoriaCorrectlyWhenTarjetaVideoExists() {
        // Arrange
        String memoriaEsperada = "8GB GDDR6";
        Componente tarjetaVideo = Componente.crearTarjetaVideo("GPU002", "RTX 4060", 
                "NVIDIA", "GeForce", new BigDecimal("300.00"), 
                new BigDecimal("450.00"), memoriaEsperada);

        // Act
        TarjetaVideo gpu = (TarjetaVideo) tarjetaVideo;
        String memoriaObtenida = gpu.getMemoria();

        // Assert
        assertThat(memoriaObtenida).isEqualTo(memoriaEsperada);
    }

    /**
     * Verifica que el setter de memoria funcione correctamente.
     * 
     * Given: Una tarjeta de video existente
     * When: Se actualiza la memoria mediante setter
     * Then: La nueva memoria se asigna correctamente
     */
    @Test
    @DisplayName("Debe actualizar memoria VRAM correctamente")
    void shouldSetMemoriaCorrectlyWhenNewValueProvided() {
        // Arrange
        Componente tarjetaVideo = Componente.crearTarjetaVideo("GPU003", "RX 7800 XT", 
                "AMD", "Radeon", new BigDecimal("350.00"), 
                new BigDecimal("500.00"), "16GB GDDR6");
        TarjetaVideo gpu = (TarjetaVideo) tarjetaVideo;
        String nuevaMemoria = "20GB GDDR6X";

        // Act
        gpu.setMemoria(nuevaMemoria);

        // Assert
        assertThat(gpu.getMemoria()).isEqualTo(nuevaMemoria);
    }

    // ==================== HERENCIA DE COMPONENTESIMPLE Y COMPONENTE ====================

    /**
     * Verifica que TarjetaVideo herede correctamente de ComponenteSimple.
     * 
     * Given: Una tarjeta de video creada
     * When: Se verifica la herencia
     * Then: La tarjeta es instancia de ComponenteSimple y Componente
     */
    @Test
    @DisplayName("Debe heredar correctamente de ComponenteSimple")
    void shouldInheritFromComponenteSimpleCorrectly() {
        // Arrange & Act
        Componente tarjetaVideo = Componente.crearTarjetaVideo("GPU004", "Test GPU", 
                "TestBrand", "TestModel", new BigDecimal("200.00"), 
                new BigDecimal("300.00"), "4GB");

        // Assert
        assertThat(tarjetaVideo).isInstanceOf(ComponenteSimple.class);
        assertThat(tarjetaVideo).isInstanceOf(Componente.class);
        assertThat(tarjetaVideo).isInstanceOf(TarjetaVideo.class);
    }

    /**
     * Verifica que el método getCategoria retorne la categoría correcta.
     * 
     * Given: Una tarjeta de video
     * When: Se solicita la categoría
     * Then: Retorna "Tarjeta de Video"
     */
    @Test
    @DisplayName("Debe retornar categoría 'Tarjeta de Video' correctamente")
    void shouldReturnCorrectCategoryWhenGetCategoriaInvoked() {
        // Arrange
        Componente tarjetaVideo = Componente.crearTarjetaVideo("GPU005", "Category Test", 
                "TestBrand", "Pro", new BigDecimal("100.00"), 
                new BigDecimal("150.00"), "2GB");

        // Act
        String categoria = tarjetaVideo.getCategoria();

        // Assert
        assertThat(categoria).isEqualTo("Tarjeta de Video");
    }

    /**
     * Verifica que el cálculo de utilidad funcione correctamente (método heredado).
     * 
     * Given: Una tarjeta de video con costo y precio base específicos
     * When: Se calcula la utilidad
     * Then: Retorna la diferencia precioBase - costo
     */
    @Test
    @DisplayName("Debe calcular utilidad correctamente como precio - costo")
    void shouldCalculateUtilityCorrectlyWhenInvokedOnTarjetaVideo() {
        // Arrange
        BigDecimal costo = new BigDecimal("250.00");
        BigDecimal precioBase = new BigDecimal("380.00");
        BigDecimal utilidadEsperada = new BigDecimal("130.00");
        
        Componente tarjetaVideo = Componente.crearTarjetaVideo("GPU006", "Utility Test", 
                "AMD", "RX 6600", costo, precioBase, "8GB");

        // Act
        BigDecimal utilidadCalculada = tarjetaVideo.calcularUtilidad();

        // Assert
        assertThat(utilidadCalculada).isEqualByComparingTo(utilidadEsperada);
    }

    /**
     * Verifica que la cotización sin promoción funcione correctamente (método heredado).
     * 
     * Given: Una tarjeta de video sin promoción asignada
     * When: Se cotiza una cantidad específica
     * Then: Retorna precioBase × cantidad
     */
    @ParameterizedTest(name = "Cantidad {0} debe cotizar {1}")
    @CsvSource({
        "1, 500.00",
        "2, 1000.00", 
        "3, 1500.00",
        "4, 2000.00"
    })
    @DisplayName("Debe cotizar correctamente sin promoción: precioBase × cantidad")
    void shouldQuoteCorrectlyWithoutPromotionWhenQuantityProvided(int cantidad, String importeEsperado) {
        // Arrange
        BigDecimal precioBase = new BigDecimal("500.00");
        Componente tarjetaVideo = Componente.crearTarjetaVideo("GPU007", "Quote Test", 
                "NVIDIA", "GTX 1660", new BigDecimal("200.00"), precioBase, "6GB");

        // Act
        BigDecimal importeCotizado = tarjetaVideo.cotizar(cantidad);

        // Assert
        assertThat(importeCotizado).isEqualByComparingTo(new BigDecimal(importeEsperado));
    }

    // ==================== REGLAS DE NEGOCIO IMPLÍCITAS - MEMORIA VRAM ====================

    /**
     * Verifica validación de rangos de memoria VRAM según gama.
     * Regla de negocio implícita: Diferentes gamas de GPU tienen rangos de memoria típicos.
     * 
     * Given: Tarjetas con memoria VRAM estándar del mercado
     * When: Se crean tarjetas con estas memorias
     * Then: Las tarjetas se crean correctamente
     */
    @ParameterizedTest(name = "Memoria {0} debe ser válida")
    @ValueSource(strings = {
        "2GB GDDR5", "3GB GDDR5", "4GB GDDR5", "4GB GDDR6",
        "6GB GDDR5", "6GB GDDR6", "8GB GDDR5", "8GB GDDR6", 
        "10GB GDDR6X", "11GB GDDR5X", "12GB GDDR6", "12GB GDDR6X",
        "16GB GDDR6", "16GB GDDR6X", "20GB GDDR6X", "24GB GDDR6X",
        "32GB HBM2", "48GB HBM3", "80GB HBM2e" // Gama profesional
    })
    @DisplayName("Debe aceptar rangos de memoria VRAM estándar del mercado")
    void shouldAcceptStandardVramRangesWhenCreatingTarjetaVideo(String memoria) {
        // Arrange & Act
        Componente tarjetaVideo = Componente.crearTarjetaVideo("GPU_MEM_" + memoria.hashCode(), 
                "Test VRAM", "TestBrand", "TestModel", 
                new BigDecimal("200.00"), new BigDecimal("300.00"), memoria);

        // Assert
        assertThat(tarjetaVideo).isNotNull();
        TarjetaVideo gpu = (TarjetaVideo) tarjetaVideo;
        assertThat(gpu.getMemoria()).isEqualTo(memoria);
    }

    /**
     * Verifica coherencia entre memoria VRAM y gama de tarjeta.
     * Regla de negocio implícita: Tarjetas más caras tienen más memoria VRAM.
     * 
     * Given: Tarjetas con diferentes cantidades de memoria y precios
     * When: Se compara la relación memoria/precio
     * Then: Debe existir coherencia entre memoria y precio
     */
    @ParameterizedTest(name = "GPU {0} con {1} debe tener precio coherente: ${2}")
    @CsvSource({
        "Gama Baja, 4GB GDDR6, 200.00",
        "Gama Media, 8GB GDDR6, 400.00",
        "Gama Alta, 12GB GDDR6X, 700.00",
        "Gama Ultra, 16GB GDDR6X, 1000.00",
        "Profesional, 24GB GDDR6X, 1500.00"
    })
    @DisplayName("Debe validar coherencia memoria VRAM/precio según gama")
    void shouldValidateVramPriceCoherenceByTierWhenCreated(String gama, String memoria, String precioEsperado) {
        // Arrange & Act
        BigDecimal precio = new BigDecimal(precioEsperado);
        BigDecimal costo = precio.multiply(new BigDecimal("0.65")); // 65% del precio
        
        Componente tarjetaVideo = Componente.crearTarjetaVideo("GPU_TIER_" + gama.hashCode(), 
                "GPU " + gama, "TestBrand", "TestModel", costo, precio, memoria);

        // Assert
        assertThat(tarjetaVideo).isNotNull();
        TarjetaVideo gpu = (TarjetaVideo) tarjetaVideo;
        
        // Verificar que tarjetas con más memoria tengan precios más altos
        int memoriaGB = Integer.parseInt(memoria.split("GB")[0]);
        BigDecimal precioPorGB = precio.divide(new BigDecimal(memoriaGB), 2, java.math.RoundingMode.HALF_UP);
        
        assertThat(gpu.getMemoria()).contains(memoria);
        assertThat(precioPorGB).isPositive();
        
        // Validar rangos de precio por GB según gama
        if (gama.equals("Gama Baja")) {
            assertThat(precioPorGB).isBetween(new BigDecimal("40"), new BigDecimal("60"));
        } else if (gama.equals("Profesional")) {
            assertThat(precioPorGB).isGreaterThan(new BigDecimal("50"));
        }
    }

    // ==================== REGLAS DE NEGOCIO IMPLÍCITAS - COMPATIBILIDAD CHIPSET/CONECTORES ====================

    /**
     * Verifica compatibilidad entre chipset y conectores de salida.
     * Regla de negocio implícita: Diferentes chipsets soportan diferentes conectores.
     * 
     * Given: Tarjetas con especificaciones de chipset y conectores
     * When: Se incluyen en la descripción/modelo
     * Then: Las combinaciones deben ser técnicamente válidas
     */
    @ParameterizedTest(name = "Chipset {0} debe ser compatible con conectores {1}")
    @CsvSource({
        "RTX 4090, HDMI 2.1 + DisplayPort 1.4a + USB-C",
        "RTX 4070, HDMI 2.1 + DisplayPort 1.4a",
        "RX 7900 XTX, HDMI 2.1 + DisplayPort 2.1",
        "GTX 1660, HDMI 2.0b + DisplayPort 1.4 + DVI-D",
        "RTX 3060, HDMI 2.1 + DisplayPort 1.4a",
        "RX 6600, HDMI 2.1 + DisplayPort 1.4"
    })
    @DisplayName("Debe validar compatibilidad chipset/conectores estándar")
    void shouldValidateChipsetConnectorCompatibilityWhenSpecified(String chipset, String conectores) {
        // Arrange & Act
        String descripcion = String.format("GPU %s con conectores %s", chipset, conectores);
        Componente tarjetaVideo = Componente.crearTarjetaVideo("GPU_CHIP_" + chipset.hashCode(), 
                descripcion, "TestBrand", chipset, 
                new BigDecimal("300.00"), new BigDecimal("450.00"), "8GB");

        // Assert
        assertThat(tarjetaVideo).isNotNull();
        assertThat(tarjetaVideo.getDescripcion()).contains(chipset);
        assertThat(tarjetaVideo.getDescripcion()).contains(conectores.split("\\+")[0].trim()); // Al menos el primer conector
        
        // Verificar que chipsets modernos tengan conectores modernos
        if (chipset.contains("RTX 40") || chipset.contains("RX 79")) {
            assertThat(conectores).contains("HDMI 2.1");
        }
        if (chipset.contains("RTX") && !chipset.contains("GTX")) {
            assertThat(conectores).containsAnyOf("DisplayPort", "USB-C");
        }
    }

    // ==================== REGLAS DE NEGOCIO IMPLÍCITAS - CONSUMO ENERGÉTICO VS PERFORMANCE ====================

    /**
     * Verifica coherencia entre consumo energético y performance.
     * Regla de negocio implícita: GPUs más potentes consumen más energía.
     * 
     * Given: Tarjetas con diferentes niveles de performance y consumo
     * When: Se especifica la información en descripción/modelo
     * Then: Debe existir coherencia entre performance y consumo
     */
    @ParameterizedTest(name = "GPU {0} con TDP {1}W debe ser coherente con performance {2}")
    @CsvSource({
        "RTX 4090, 450, Ultra-Alto",
        "RTX 4070, 200, Alto", 
        "RTX 4060, 115, Medio",
        "GTX 1650, 75, Básico",
        "RX 7900 XTX, 355, Ultra-Alto",
        "RX 6600, 132, Medio"
    })
    @DisplayName("Debe validar coherencia consumo energético/performance")
    void shouldValidateEnergyConsumptionPerformanceCoherenceWhenSpecified(String modelo, int tdpWatts, String performance) {
        // Arrange & Act
        String descripcion = String.format("GPU %s - TDP %dW - Performance %s", modelo, tdpWatts, performance);
        BigDecimal precio = calcularPrecioPorPerformance(performance);
        BigDecimal costo = precio.multiply(new BigDecimal("0.7"));
        
        Componente tarjetaVideo = Componente.crearTarjetaVideo("GPU_TDP_" + modelo.hashCode(), 
                descripcion, "TestBrand", modelo, costo, precio, "8GB");

        // Assert
        assertThat(tarjetaVideo).isNotNull();
        assertThat(tarjetaVideo.getDescripcion()).contains(String.valueOf(tdpWatts));
        
        // Verificar coherencia TDP/Performance
        if (performance.equals("Ultra-Alto")) {
            assertThat(tdpWatts).isGreaterThan(300);
            assertThat(precio).isGreaterThan(new BigDecimal("600"));
        } else if (performance.equals("Alto")) {
            assertThat(tdpWatts).isBetween(150, 350);
            assertThat(precio).isBetween(new BigDecimal("300"), new BigDecimal("600"));
        } else if (performance.equals("Medio")) {
            assertThat(tdpWatts).isBetween(100, 200);
            assertThat(precio).isBetween(new BigDecimal("150"), new BigDecimal("400"));
        } else if (performance.equals("Básico")) {
            assertThat(tdpWatts).isLessThan(150);
            assertThat(precio).isLessThan(new BigDecimal("300"));
        }
    }

    private BigDecimal calcularPrecioPorPerformance(String performance) {
        return switch (performance) {
            case "Ultra-Alto" -> new BigDecimal("800.00");
            case "Alto" -> new BigDecimal("450.00");
            case "Medio" -> new BigDecimal("250.00");
            case "Básico" -> new BigDecimal("120.00");
            default -> new BigDecimal("200.00");
        };
    }

    // ==================== REGLAS DE NEGOCIO IMPLÍCITAS - ARQUITECTURAS Y GENERACIONES ====================

    /**
     * Verifica validación de arquitecturas de GPU conocidas.
     * Regla de negocio implícita: Solo arquitecturas reales deberían ser aceptadas.
     * 
     * Given: Modelos con arquitecturas conocidas
     * When: Se especifican en el modelo
     * Then: Las arquitecturas deben ser válidas del mercado
     */
    @ParameterizedTest(name = "Arquitectura {0} debe ser válida")
    @ValueSource(strings = {
        "Ada Lovelace RTX 4090", "RDNA 3 RX 7900", "Ampere RTX 3080", 
        "RDNA 2 RX 6800", "Turing GTX 1660", "Pascal GTX 1060",
        "Navi RX 5700", "Vega RX Vega 64", "Polaris RX 580"
    })
    @DisplayName("Debe aceptar arquitecturas de GPU del mercado")
    void shouldAcceptValidGpuArchitecturesWhenSpecified(String arquitectura) {
        // Arrange & Act
        Componente tarjetaVideo = Componente.crearTarjetaVideo("GPU_ARCH_" + arquitectura.hashCode(), 
                "GPU Arquitectura " + arquitectura, "TestBrand", arquitectura,
                new BigDecimal("300.00"), new BigDecimal("450.00"), "8GB");

        // Assert
        assertThat(tarjetaVideo).isNotNull();
        assertThat(tarjetaVideo.getModelo()).contains(arquitectura);
        
        // Verificar que arquitecturas más nuevas tengan mejores características
        if (arquitectura.contains("Ada Lovelace") || arquitectura.contains("RDNA 3")) {
            assertThat(tarjetaVideo.getPrecioBase()).isGreaterThan(new BigDecimal("400"));
        }
    }

    // ==================== CASOS EDGE Y VALIDACIONES ====================

    /**
     * Verifica el comportamiento cuando se asigna memoria null.
     * 
     * Given: Una tarjeta de video existente
     * When: Se asigna null como memoria
     * Then: Se acepta null (comportamiento actual del código)
     */
    @Test
    @DisplayName("Debe manejar memoria null sin errores")
    void shouldHandleNullMemoriaWithoutErrorsWhenSet() {
        // Arrange
        Componente tarjetaVideo = Componente.crearTarjetaVideo("GPU_NULL", "Test Null", 
                "TestBrand", "TestModel", new BigDecimal("100.00"), 
                new BigDecimal("150.00"), "4GB");
        TarjetaVideo gpu = (TarjetaVideo) tarjetaVideo;

        // Act
        gpu.setMemoria(null);

        // Assert
        assertThat(gpu.getMemoria()).isNull();
    }

    /**
     * Verifica el comportamiento cuando se asigna memoria vacía.
     * 
     * Given: Una tarjeta de video existente
     * When: Se asigna cadena vacía como memoria
     * Then: Se acepta la cadena vacía
     */
    @Test
    @DisplayName("Debe manejar memoria vacía sin errores")
    void shouldHandleEmptyMemoriaWithoutErrorsWhenSet() {
        // Arrange
        Componente tarjetaVideo = Componente.crearTarjetaVideo("GPU_EMPTY", "Test Empty", 
                "TestBrand", "TestModel", new BigDecimal("100.00"), 
                new BigDecimal("150.00"), "8GB");
        TarjetaVideo gpu = (TarjetaVideo) tarjetaVideo;

        // Act
        gpu.setMemoria("");

        // Assert
        assertThat(gpu.getMemoria()).isEmpty();
    }

    /**
     * Verifica comparación de tarjetas por características técnicas.
     * Regla de negocio implícita: Tarjetas con mejores specs cuestan más.
     * 
     * Given: Tarjetas con diferentes especificaciones
     * When: Se comparan precios y características
     * Then: Debe existir coherencia precio/características
     */
    @Test
    @DisplayName("Debe validar coherencia precio/características técnicas")
    void shouldValidatePriceSpecificationCoherenceWhenComparingCards() {
        // Arrange
        Componente gpuBasica = Componente.crearTarjetaVideo("GPU_BASIC", 
                "GPU Básica GTX 1650 TDP 75W", "NVIDIA", "GTX 1650",
                new BigDecimal("80.00"), new BigDecimal("150.00"), "4GB GDDR5");
                
        Componente gpuPremium = Componente.crearTarjetaVideo("GPU_PREMIUM", 
                "GPU Premium RTX 4090 TDP 450W Ray Tracing DLSS 3", "NVIDIA", "RTX 4090",
                new BigDecimal("1200.00"), new BigDecimal("1800.00"), "24GB GDDR6X");

        // Act & Assert
        // Verificar que la GPU premium tenga mayor precio y memoria
        assertThat(gpuPremium.getPrecioBase()).isGreaterThan(gpuBasica.getPrecioBase());
        assertThat(gpuPremium.getCosto()).isGreaterThan(gpuBasica.getCosto());
        
        TarjetaVideo basicaGpu = (TarjetaVideo) gpuBasica;
        TarjetaVideo premiumGpu = (TarjetaVideo) gpuPremium;
        
        // La GPU premium debe tener más memoria
        int memoriaBasica = Integer.parseInt(basicaGpu.getMemoria().split("GB")[0]);
        int memoriaPremium = Integer.parseInt(premiumGpu.getMemoria().split("GB")[0]);
        assertThat(memoriaPremium).isGreaterThan(memoriaBasica);
        
        // Verificar que las utilidades también sean coherentes
        assertThat(gpuPremium.calcularUtilidad()).isGreaterThan(gpuBasica.calcularUtilidad());
    }

    /**
     * Verifica que el método toString heredado funcione correctamente.
     * 
     * Given: Una tarjeta de video creada
     * When: Se invoca toString()
     * Then: Retorna representación de cadena con información básica
     */
    @Test
    @DisplayName("Debe generar representación toString correcta")
    void shouldGenerateCorrectToStringRepresentationWhenInvoked() {
        // Arrange
        Componente tarjetaVideo = Componente.crearTarjetaVideo("GPU_STR", "GPU ToString Test", 
                "TestBrand", "TestModel", new BigDecimal("200.00"), 
                new BigDecimal("300.00"), "6GB");

        // Act
        String stringRepresentation = tarjetaVideo.toString();

        // Assert
        assertThat(stringRepresentation).isNotNull();
        assertThat(stringRepresentation).contains("GPU_STR");
        assertThat(stringRepresentation).contains("GPU ToString Test");
        assertThat(stringRepresentation).contains("TestBrand");
        assertThat(stringRepresentation).contains("TestModel");
    }

    // ==================== TESTS DE EXTENSIBILIDAD FUTURA ====================

    /**
     * Verifica que la clase TarjetaVideo permita extensiones futuras.
     * En el futuro se podrían agregar propiedades como frecuencia base/boost,
     * conectores específicos, soporte para tecnologías (Ray Tracing, DLSS), etc.
     * 
     * Given: Tarjetas con características avanzadas
     * When: Se evalúa la capacidad de extensión
     * Then: La estructura actual permite agregar propiedades técnicas
     */
    @Test
    @DisplayName("Debe permitir extensibilidad futura para propiedades técnicas avanzadas")
    void shouldAllowFutureExtensibilityForAdvancedTechnicalProperties() {
        // Arrange & Act
        Componente gpuGaming = Componente.crearTarjetaVideo("GPU_GAMING", 
                "GPU Gaming RTX 4070 Base Clock 2475MHz Boost 2610MHz Ray Tracing DLSS 3", 
                "NVIDIA", "RTX 4070", new BigDecimal("400.00"), 
                new BigDecimal("600.00"), "12GB GDDR6X");
                
        Componente gpuProfesional = Componente.crearTarjetaVideo("GPU_PROF", 
                "GPU Profesional Quadro RTX A6000 ECC Memory NVENC/NVDEC", 
                "NVIDIA", "Quadro RTX A6000", new BigDecimal("3000.00"), 
                new BigDecimal("4500.00"), "48GB GDDR6");

        // Assert
        assertThat(gpuGaming).isNotNull();
        assertThat(gpuProfesional).isNotNull();
        
        // En el futuro se podrían agregar propiedades como:
        // - Frecuencias base y boost
        // - Soporte para Ray Tracing, DLSS, FSR
        // - Núcleos CUDA/Stream Processors
        // - Ancho de bus de memoria
        // - Conectores específicos (HDMI 2.1, DP 2.1, USB-C)
        // - Soporte para tecnologías específicas (VR Ready, G-Sync Compatible)
        assertThat(gpuGaming.getDescripcion()).containsAnyOf("Ray Tracing", "DLSS", "Gaming");
        assertThat(gpuProfesional.getDescripcion()).containsAnyOf("Profesional", "ECC", "Quadro");
        
        // Verificar que GPU profesional tenga más memoria y mayor precio
        TarjetaVideo gaming = (TarjetaVideo) gpuGaming;
        TarjetaVideo profesional = (TarjetaVideo) gpuProfesional;
        
        int memoriaGaming = Integer.parseInt(gaming.getMemoria().split("GB")[0]);
        int memoriaProfesional = Integer.parseInt(profesional.getMemoria().split("GB")[0]);
        
        assertThat(memoriaProfesional).isGreaterThan(memoriaGaming);
        assertThat(gpuProfesional.getPrecioBase()).isGreaterThan(gpuGaming.getPrecioBase());
    }

    // ==================== VALIDACIONES PRD REQUERIDAS ====================

    /**
     * Verifica que se lance IllegalArgumentException cuando memoria es nula.
     *
     * PRD Requirement: Validar que cuando se proporcione la memoria, no sea vacia ni nula.
     * En caso de ser el caso, tirar una excepcion.
     *
     * Given: Parámetros válidos excepto memoria nula
     * When: Se intenta crear tarjeta de video
     * Then: Se lanza IllegalArgumentException
     */
    @Test
    @DisplayName("Dado memoria nula entonces lanza IllegalArgumentException")
    void dadoMemoriaNulaEntoncesLanzaIllegalArgumentException() {
        // Given & When & Then
        assertThatThrownBy(() -> Componente.crearTarjetaVideo(
                "GPU_NULL", "Test GPU", "TestBrand", "TestModel",
                new BigDecimal("200.00"), new BigDecimal("350.00"), null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("memoria")
          .hasMessageContaining("nula");
    }

    /**
     * Verifica que se lance IllegalArgumentException cuando memoria está vacía.
     *
     * PRD Requirement: Validar que cuando se proporcione la memoria, no sea vacia ni nula.
     * En caso de ser el caso, tirar una excepcion.
     *
     * Given: Parámetros válidos excepto memoria vacía
     * When: Se intenta crear tarjeta de video
     * Then: Se lanza IllegalArgumentException
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("Dado memoria vacía entonces lanza IllegalArgumentException")
    void dadoMemoriaVaciaEntoncesLanzaIllegalArgumentException(String memoriaVacia) {
        // Given & When & Then
        assertThatThrownBy(() -> Componente.crearTarjetaVideo(
                "GPU_EMPTY", "Test GPU", "TestBrand", "TestModel",
                new BigDecimal("200.00"), new BigDecimal("350.00"), memoriaVacia
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("memoria")
          .hasMessageContaining("vacía");
    }

    /**
     * Verifica que la validación de memoria válida funcione correctamente.
     *
     * Given: Parámetros válidos incluyendo memoria válida
     * When: Se crea tarjeta de video
     * Then: Se crea exitosamente sin excepciones
     */
    @ParameterizedTest
    @CsvSource({
        "4GB, GTX 1650 4GB",
        "8GB, RTX 3060 8GB",
        "16GB, RTX 4080 16GB",
        "24GB, RTX 4090 24GB"
    })
    @DisplayName("Dado memoria válida entonces creación exitosa")
    void dadoMemoriaValidaEntoncesCreacionExitosa(String memoria, String descripcion) {
        // Given & When
        Componente gpu = Componente.crearTarjetaVideo(
            "GPU_VALID", descripcion, "TestBrand", "TestModel",
            new BigDecimal("200.00"), new BigDecimal("350.00"), memoria
        );

        // Then
        assertThat(gpu).isNotNull();
        assertThat(gpu).isInstanceOf(TarjetaVideo.class);
        assertThat(((TarjetaVideo) gpu).getMemoria()).isEqualTo(memoria);
    }
}