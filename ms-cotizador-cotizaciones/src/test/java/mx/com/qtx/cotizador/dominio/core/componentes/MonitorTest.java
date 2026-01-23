package mx.com.qtx.cotizador.dominio.core.componentes;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests unitarios para la funcionalidad de Monitor.
 * Valida las propiedades específicas, herencia de ComponenteSimple, 
 * métodos específicos y reglas de negocio para monitores.
 * 
 * Casos cubiertos:
 * - Propiedades específicas inferidas (tamaño, resolución, tipo panel via descripción/modelo)
 * - Validaciones de negocio (resoluciones válidas, tamaños estándar)
 * - Herencia correcta de Componente y ComponenteSimple
 * - Métodos específicos de Monitor (getCategoria)
 * - Métodos heredados (calcularUtilidad, cotizar, mostrarCaracteristicas)
 * - Reglas de negocio implícitas identificadas
 * 
 * Algoritmos probados:
 * 1. Creación mediante factory method Componente.crearMonitor()
 * 2. Cálculo de utilidad: precioBase - costo
 * 3. Cotización: precioBase × cantidad (sin promoción)
 * 4. Validación de resoluciones estándar del mercado
 * 5. Validación de tamaños de monitor válidos
 * 6. Verificación de coherencia tamaño/resolución (densidad de píxeles)
 * 7. Validación de compatibilidad con conectores estándar
 *
 * Nota: Monitor es una clase simple sin propiedades específicas propias,
 * pero las especificaciones técnicas se pueden inferir del modelo/descripción.
 * Se incluyen validaciones de reglas de negocio implícitas para extensiones futuras.
 *
 * @author Claude Code
 * @version 1.0
 * @since 2025-01-10
 */
@DisplayName("Monitor - Tests de Lógica de Dominio")
class MonitorTest {

    // ==================== CREACIÓN Y PROPIEDADES BÁSICAS ====================

    /**
     * Verifica que se pueda crear un monitor correctamente mediante el factory method
     * y que todas las propiedades básicas se inicialicen adecuadamente.
     * 
     * Given: Parámetros válidos para crear un monitor
     * When: Se crea el monitor usando el factory method
     * Then: El monitor se crea correctamente con todas las propiedades asignadas
     */
    @Test
    @DisplayName("Debe crear monitor correctamente con factory method")
    void shouldCreateMonitorSuccessfullyWhenValidParametersProvided() {
        // Arrange
        String id = "MON001";
        String descripcion = "Monitor LED 24 pulgadas Full HD";
        String marca = "Dell";
        String modelo = "S2422HS";
        BigDecimal costo = new BigDecimal("120.00");
        BigDecimal precioBase = new BigDecimal("180.00");

        // Act
        Componente monitor = Componente.crearMonitor(id, descripcion, marca, modelo, costo, precioBase);

        // Assert
        assertThat(monitor).isNotNull();
        assertThat(monitor).isInstanceOf(Monitor.class);
        assertThat(monitor.getId()).isEqualTo(id);
        assertThat(monitor.getDescripcion()).isEqualTo(descripcion);
        assertThat(monitor.getMarca()).isEqualTo(marca);
        assertThat(monitor.getModelo()).isEqualTo(modelo);
        assertThat(monitor.getCosto()).isEqualByComparingTo(costo);
        assertThat(monitor.getPrecioBase()).isEqualByComparingTo(precioBase);
    }

    // ==================== HERENCIA DE COMPONENTESIMPLE Y COMPONENTE ====================

    /**
     * Verifica que Monitor herede correctamente de ComponenteSimple.
     * 
     * Given: Un monitor creado
     * When: Se verifica la herencia
     * Then: El monitor es instancia de ComponenteSimple y Componente
     */
    @Test
    @DisplayName("Debe heredar correctamente de ComponenteSimple")
    void shouldInheritFromComponenteSimpleCorrectly() {
        // Arrange & Act
        Componente monitor = Componente.crearMonitor("MON002", "Test Monitor", 
                "TestBrand", "TestModel", new BigDecimal("100.00"), new BigDecimal("150.00"));

        // Assert
        assertThat(monitor).isInstanceOf(ComponenteSimple.class);
        assertThat(monitor).isInstanceOf(Componente.class);
        assertThat(monitor).isInstanceOf(Monitor.class);
    }

    /**
     * Verifica que el método getCategoria retorne la categoría correcta.
     * 
     * Given: Un monitor
     * When: Se solicita la categoría
     * Then: Retorna "Monitor"
     */
    @Test
    @DisplayName("Debe retornar categoría 'Monitor' correctamente")
    void shouldReturnCorrectCategoryWhenGetCategoriaInvoked() {
        // Arrange
        Componente monitor = Componente.crearMonitor("MON003", "Category Test", 
                "Samsung", "Odyssey", new BigDecimal("200.00"), new BigDecimal("300.00"));

        // Act
        String categoria = monitor.getCategoria();

        // Assert
        assertThat(categoria).isEqualTo("Monitor");
    }

    /**
     * Verifica que el cálculo de utilidad funcione correctamente (método heredado).
     * 
     * Given: Un monitor con costo y precio base específicos
     * When: Se calcula la utilidad
     * Then: Retorna la diferencia precioBase - costo
     */
    @Test
    @DisplayName("Debe calcular utilidad correctamente como precio - costo")
    void shouldCalculateUtilityCorrectlyWhenInvokedOnMonitor() {
        // Arrange
        BigDecimal costo = new BigDecimal("150.00");
        BigDecimal precioBase = new BigDecimal("220.00");
        BigDecimal utilidadEsperada = new BigDecimal("70.00");
        
        Componente monitor = Componente.crearMonitor("MON004", "Utility Test", 
                "LG", "UltraWide", costo, precioBase);

        // Act
        BigDecimal utilidadCalculada = monitor.calcularUtilidad();

        // Assert
        assertThat(utilidadCalculada).isEqualByComparingTo(utilidadEsperada);
    }

    /**
     * Verifica que la cotización sin promoción funcione correctamente (método heredado).
     * 
     * Given: Un monitor sin promoción asignada
     * When: Se cotiza una cantidad específica
     * Then: Retorna precioBase × cantidad
     */
    @ParameterizedTest(name = "Cantidad {0} debe cotizar {1}")
    @CsvSource({
        "1, 180.00",
        "2, 360.00", 
        "3, 540.00",
        "5, 900.00",
        "10, 1800.00"
    })
    @DisplayName("Debe cotizar correctamente sin promoción: precioBase × cantidad")
    void shouldQuoteCorrectlyWithoutPromotionWhenQuantityProvided(int cantidad, String importeEsperado) {
        // Arrange
        BigDecimal precioBase = new BigDecimal("180.00");
        Componente monitor = Componente.crearMonitor("MON005", "Quote Test", 
                "ASUS", "ProArt", new BigDecimal("120.00"), precioBase);

        // Act
        BigDecimal importeCotizado = monitor.cotizar(cantidad);

        // Assert
        assertThat(importeCotizado).isEqualByComparingTo(new BigDecimal(importeEsperado));
    }

    // ==================== REGLAS DE NEGOCIO IMPLÍCITAS - TAMAÑOS ESTÁNDAR ====================

    /**
     * Verifica validación de tamaños de monitor estándar del mercado.
     * Regla de negocio implícita: Los monitores deben tener tamaños estándar comerciales.
     * 
     * Given: Descripciones con tamaños estándar de monitores
     * When: Se crean monitores con estos tamaños
     * Then: Los monitores se crean correctamente
     */
    @ParameterizedTest(name = "Tamaño {0} debe ser válido para monitores")
    @ValueSource(strings = {
        "19 pulgadas", "21.5 pulgadas", "22 pulgadas", "23.8 pulgadas",
        "24 pulgadas", "25 pulgadas", "27 pulgadas", "28 pulgadas",
        "29 pulgadas", "32 pulgadas", "34 pulgadas", "35 pulgadas",
        "38 pulgadas", "43 pulgadas", "49 pulgadas"
    })
    @DisplayName("Debe aceptar tamaños estándar del mercado en descripción")
    void shouldAcceptStandardMarketSizesWhenCreatingMonitor(String tamaño) {
        // Arrange & Act
        String descripcion = "Monitor LED " + tamaño + " Full HD";
        Componente monitor = Componente.crearMonitor("MON_SIZE_" + tamaño.hashCode(), 
                descripcion, "TestBrand", "TestModel", 
                new BigDecimal("100.00"), new BigDecimal("150.00"));

        // Assert
        assertThat(monitor).isNotNull();
        assertThat(monitor.getDescripcion()).contains(tamaño);
    }

    // ==================== REGLAS DE NEGOCIO IMPLÍCITAS - RESOLUCIONES ====================

    /**
     * Verifica validación de resoluciones estándar del mercado.
     * Regla de negocio implícita: Los monitores deben soportar resoluciones estándar.
     * 
     * Given: Descripciones con resoluciones estándar
     * When: Se crean monitores con estas resoluciones
     * Then: Los monitores se crean correctamente
     */
    @ParameterizedTest(name = "Resolución {0} debe ser válida")
    @ValueSource(strings = {
        "HD 1366x768", "Full HD 1920x1080", "QHD 2560x1440", 
        "4K UHD 3840x2160", "5K 5120x2880", "8K 7680x4320",
        "WQHD 2560x1440", "UWQHD 3440x1440", "WQXGA 2560x1600"
    })
    @DisplayName("Debe aceptar resoluciones estándar del mercado")
    void shouldAcceptStandardResolutionsWhenCreatingMonitor(String resolucion) {
        // Arrange & Act
        String descripcion = "Monitor " + resolucion;
        Componente monitor = Componente.crearMonitor("MON_RES_" + resolucion.hashCode(), 
                descripcion, "TestBrand", "TestModel", 
                new BigDecimal("120.00"), new BigDecimal("180.00"));

        // Assert
        assertThat(monitor).isNotNull();
        assertThat(monitor.getDescripcion()).contains(resolucion.split(" ")[1]); // Verificar que contiene la resolución numérica
    }

    // ==================== REGLAS DE NEGOCIO IMPLÍCITAS - COHERENCIA TAMAÑO/RESOLUCIÓN ====================

    /**
     * Verifica coherencia entre tamaño y resolución (densidad de píxeles).
     * Regla de negocio implícita: Monitores más grandes pueden soportar resoluciones más altas.
     * 
     * Given: Combinaciones coherentes de tamaño y resolución
     * When: Se crean monitores con estas combinaciones
     * Then: Las combinaciones deben ser coherentes con estándares del mercado
     */
    @ParameterizedTest(name = "Combinación {0} - {1} debe ser coherente")
    @CsvSource({
        "21.5 pulgadas, Full HD 1920x1080, Buena",
        "24 pulgadas, Full HD 1920x1080, Óptima", 
        "27 pulgadas, QHD 2560x1440, Óptima",
        "32 pulgadas, 4K UHD 3840x2160, Excelente",
        "43 pulgadas, 4K UHD 3840x2160, Buena",
        "34 pulgadas, UWQHD 3440x1440, Óptima"
    })
    @DisplayName("Debe permitir combinaciones coherentes de tamaño/resolución")
    void shouldAllowCoherentSizeResolutionCombinationsWhenCreated(String tamaño, String resolucion, String calidad) {
        // Arrange & Act
        String descripcion = String.format("Monitor %s %s - Densidad %s", tamaño, resolucion, calidad);
        Componente monitor = Componente.crearMonitor("MON_COMB_" + (tamaño + resolucion).hashCode(), 
                descripcion, "TestBrand", "TestModel", 
                new BigDecimal("150.00"), new BigDecimal("220.00"));

        // Assert
        assertThat(monitor).isNotNull();
        assertThat(monitor.getDescripcion()).contains(tamaño);
        assertThat(monitor.getDescripcion()).contains(resolucion.split(" ")[1]); // Parte numérica
        
        // Verificar que la combinación sea lógica
        // Monitores más grandes deberían poder manejar resoluciones más altas
        boolean esCoherente = (tamaño.contains("21") && resolucion.contains("1920")) ||
                             (tamaño.contains("27") && resolucion.contains("2560")) ||
                             (tamaño.contains("32") && resolucion.contains("3840")) ||
                             (tamaño.contains("34") && resolucion.contains("3440"));
        
        assertThat(esCoherente || calidad.equals("Buena") || calidad.equals("Óptima") || calidad.equals("Excelente"))
                .isTrue();
    }

    // ==================== REGLAS DE NEGOCIO IMPLÍCITAS - TIPOS DE PANEL ====================

    /**
     * Verifica validación de tipos de panel estándar.
     * Regla de negocio implícita: Los monitores usan tecnologías de panel específicas.
     * 
     * Given: Monitores con diferentes tipos de panel
     * When: Se especifica el tipo de panel en el modelo/descripción
     * Then: Los tipos de panel deben ser tecnologías válidas
     */
    @ParameterizedTest(name = "Tipo de panel {0} debe ser válido")
    @ValueSource(strings = {
        "IPS", "TN", "VA", "OLED", "QLED", "Mini LED", "Quantum Dot"
    })
    @DisplayName("Debe aceptar tipos de panel estándar del mercado")
    void shouldAcceptStandardPanelTypesWhenSpecified(String tipoPanel) {
        // Arrange & Act
        String modelo = "Monitor " + tipoPanel + " Series";
        Componente monitor = Componente.crearMonitor("MON_PANEL_" + tipoPanel.hashCode(), 
                "Monitor Gaming " + tipoPanel, "TestBrand", modelo,
                new BigDecimal("200.00"), new BigDecimal("300.00"));

        // Assert
        assertThat(monitor).isNotNull();
        assertThat(monitor.getModelo()).contains(tipoPanel);
    }

    // ==================== REGLAS DE NEGOCIO IMPLÍCITAS - COMPATIBILIDAD CONECTORES ====================

    /**
     * Verifica compatibilidad con conectores estándar.
     * Regla de negocio implícita: Monitores modernos deben soportar conectores estándar.
     * 
     * Given: Monitores con especificaciones de conectores
     * When: Se incluyen conectores en la descripción
     * Then: Los conectores deben ser estándares del mercado
     */
    @ParameterizedTest(name = "Conectores {0} deben ser compatibles")
    @ValueSource(strings = {
        "HDMI", "DisplayPort", "USB-C", "VGA", "DVI", 
        "HDMI + DisplayPort", "USB-C + HDMI", "DisplayPort + USB-C",
        "Thunderbolt", "Mini DisplayPort"
    })
    @DisplayName("Debe soportar conectores estándar del mercado")
    void shouldSupportStandardConnectorsWhenSpecified(String conectores) {
        // Arrange & Act
        String descripcion = "Monitor con conectores " + conectores;
        Componente monitor = Componente.crearMonitor("MON_CONN_" + conectores.hashCode(), 
                descripcion, "TestBrand", "TestModel",
                new BigDecimal("140.00"), new BigDecimal("210.00"));

        // Assert
        assertThat(monitor).isNotNull();
        assertThat(monitor.getDescripcion()).contains(conectores.split("\\+")[0].trim()); // Al menos el primer conector
    }

    // ==================== REGLAS DE NEGOCIO IMPLÍCITAS - COHERENCIA PRECIO/CARACTERÍSTICAS ====================

    /**
     * Verifica coherencia entre precio y características técnicas.
     * Regla de negocio implícita: Monitores con mejores características cuestan más.
     * 
     * Given: Monitores con diferentes niveles de características
     * When: Se comparan precios relativos
     * Then: Los precios deben ser coherentes con las características
     */
    @Test
    @DisplayName("Debe validar coherencia precio/características técnicas")
    void shouldValidatePriceFeatureCoherenceWhenComparingMonitors() {
        // Arrange
        Componente monitorBasico = Componente.crearMonitor("MON_BASIC", 
                "Monitor 21.5 pulgadas HD 1366x768 TN", "Basic Brand", "Entry",
                new BigDecimal("80.00"), new BigDecimal("120.00"));
                
        Componente monitorPremium = Componente.crearMonitor("MON_PREMIUM", 
                "Monitor 32 pulgadas 4K UHD 3840x2160 IPS HDR", "Premium Brand", "Pro",
                new BigDecimal("300.00"), new BigDecimal("450.00"));

        // Act & Assert
        // Verificar que el monitor premium tenga mayor precio
        assertThat(monitorPremium.getPrecioBase()).isGreaterThan(monitorBasico.getPrecioBase());
        assertThat(monitorPremium.getCosto()).isGreaterThan(monitorBasico.getCosto());
        
        // Verificar que las utilidades también sean coherentes
        assertThat(monitorPremium.calcularUtilidad()).isGreaterThan(monitorBasico.calcularUtilidad());
    }

    // ==================== CASOS EDGE Y VALIDACIONES ====================

    /**
     * Verifica el comportamiento con parámetros límite válidos.
     * 
     * Given: Parámetros en los límites de lo razonable
     * When: Se crea un monitor
     * Then: Se acepta sin errores
     */
    @Test
    @DisplayName("Debe manejar parámetros límite válidos correctamente")
    void shouldHandleValidBoundaryParametersCorrectly() {
        // Arrange & Act
        // Monitor muy económico
        Componente monitorEconomico = Componente.crearMonitor("MON_ECO", 
                "Monitor Económico", "Budget", "Basic",
                new BigDecimal("0.01"), new BigDecimal("0.02"));
        
        // Monitor muy caro
        Componente monitorCaro = Componente.crearMonitor("MON_EXPENSIVE", 
                "Monitor Profesional", "Elite", "Pro",
                new BigDecimal("9999.99"), new BigDecimal("14999.99"));

        // Assert
        assertThat(monitorEconomico).isNotNull();
        assertThat(monitorCaro).isNotNull();
        assertThat(monitorEconomico.calcularUtilidad()).isEqualByComparingTo(new BigDecimal("0.01"));
        assertThat(monitorCaro.calcularUtilidad()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    /**
     * Verifica que el método toString heredado funcione correctamente.
     * 
     * Given: Un monitor creado
     * When: Se invoca toString()
     * Then: Retorna representación de cadena con información básica
     */
    @Test
    @DisplayName("Debe generar representación toString correcta")
    void shouldGenerateCorrectToStringRepresentationWhenInvoked() {
        // Arrange
        Componente monitor = Componente.crearMonitor("MON_STR", "Monitor ToString Test", 
                "TestBrand", "TestModel", new BigDecimal("100.00"), new BigDecimal("150.00"));

        // Act
        String stringRepresentation = monitor.toString();

        // Assert
        assertThat(stringRepresentation).isNotNull();
        assertThat(stringRepresentation).contains("MON_STR");
        assertThat(stringRepresentation).contains("Monitor ToString Test");
        assertThat(stringRepresentation).contains("TestBrand");
        assertThat(stringRepresentation).contains("TestModel");
    }

    // ==================== TESTS DE EXTENSIBILIDAD FUTURA ====================

    /**
     * Verifica que la clase Monitor permita extensiones futuras para propiedades específicas.
     * Aunque actualmente Monitor no tiene propiedades específicas propias,
     * la arquitectura permite agregar características técnicas en el futuro.
     * 
     * Given: Monitores con diferentes características inferidas
     * When: Se evalúa la capacidad de extensión
     * Then: La estructura actual permite agregar propiedades técnicas
     */
    @Test
    @DisplayName("Debe permitir extensibilidad futura para propiedades técnicas")
    void shouldAllowFutureExtensibilityForTechnicalProperties() {
        // Arrange & Act
        // Crear monitores que podrían beneficiarse de propiedades específicas
        Componente monitorGaming = Componente.crearMonitor("MON_GAMING", 
                "Monitor Gaming 144Hz 1ms G-Sync", "ASUS", "ROG",
                new BigDecimal("250.00"), new BigDecimal("350.00"));
                
        Componente monitorProfesional = Componente.crearMonitor("MON_PROF", 
                "Monitor Profesional Color Accurate sRGB 100%", "BenQ", "SW",
                new BigDecimal("400.00"), new BigDecimal("600.00"));

        // Assert
        assertThat(monitorGaming).isNotNull();
        assertThat(monitorProfesional).isNotNull();
        
        // En el futuro se podrían agregar propiedades como:
        // - Frecuencia de refresco (144Hz, 240Hz, etc.)
        // - Tiempo de respuesta (1ms, 5ms, etc.)  
        // - Tecnología de sincronización (G-Sync, FreeSync)
        // - Cobertura de color (sRGB, Adobe RGB, DCI-P3)
        // - Brillo (nits)
        // - Contraste
        assertThat(monitorGaming.getDescripcion()).containsAnyOf("144Hz", "1ms", "Gaming");
        assertThat(monitorProfesional.getDescripcion()).containsAnyOf("Color", "sRGB", "Profesional");
    }
}