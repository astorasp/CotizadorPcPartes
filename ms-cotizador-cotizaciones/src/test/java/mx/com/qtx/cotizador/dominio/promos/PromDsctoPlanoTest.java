package mx.com.qtx.cotizador.dominio.promos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests unitarios para PromDsctoPlano (Patrón Decorator)
 * 
 * Valida la lógica de descuentos porcentuales que se aplican
 * sobre una promoción base usando el patrón Decorator.
 * 
 * Algoritmo probado:
 * 1. Calcular importe base con la promoción envuelta
 * 2. Aplicar descuento porcentual sobre el resultado
 * 3. importeFinal = importeBase - (importeBase × porcentaje/100)
 * 
 * CRÍTICO: Patrón Decorator permite apilar múltiples descuentos
 */
@DisplayName("PromDsctoPlano - Tests de Lógica de Dominio (Decorator)")
class PromDsctoPlanoTest {

    // ==================== DECORATOR SOBRE PROMOCIÓN SIN DESCUENTO ====================

    @Test
    @DisplayName("Descuento 10% sobre promoción sin descuento: $1000 → $900")
    void descuento10_sobrePromSinDescto_base1000_deberia_devolver900() {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoPlano promocion = new PromDsctoPlano(promoBase, 10.0f);
        int cantidad = 10;
        BigDecimal precioBase = BigDecimal.valueOf(100.00);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        // Base: 10 × $100 = $1000, Descuento 10%: $1000 - $100 = $900
        BigDecimal esperado = BigDecimal.valueOf(900.00);
        assertThat(resultado).isEqualByComparingTo(esperado);
    }

    @Test
    @DisplayName("Descuento 15.5% sobre promoción sin descuento con decimales")
    void descuento155_sobrePromSinDescto_conDecimales_deberia_conservarPrecision() {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoPlano promocion = new PromDsctoPlano(promoBase, 15.5f);
        int cantidad = 4;
        BigDecimal precioBase = new BigDecimal("125.75");
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        // Base: 4 × $125.75 = $503.00
        // Descuento 15.5%: $503.00 × 0.155 = $77.965
        // Final: $503.00 - $77.965 = $425.035
        BigDecimal esperado = new BigDecimal("425.035");
        assertThat(resultado).isCloseTo(esperado, within(new BigDecimal("0.001")));
    }

    // ==================== DECORATOR SOBRE PROMOCIÓN NXM ====================

    @Test
    @DisplayName("Descuento 20% sobre promoción 3x2: aplicar descuento sobre precio ya reducido")
    void descuento20_sobrePromNXM_deberia_aplicarSobrePrecioReducido() {
        // Arrange
        PromNXM promoBase = new PromNXM(3, 2); // 3x2
        PromDsctoPlano promocion = new PromDsctoPlano(promoBase, 20.0f);
        int cantidad = 6; // 2 grupos completos
        BigDecimal precioBase = BigDecimal.valueOf(100.00);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        // Base NXM: 6 unidades = 2 grupos × 2 pagadas = 4 × $100 = $400
        // Descuento 20%: $400 × 0.20 = $80
        // Final: $400 - $80 = $320
        BigDecimal esperado = BigDecimal.valueOf(320.00);
        assertThat(resultado).isEqualByComparingTo(esperado);
    }

    // ==================== CASOS PARAMETRIZADOS ====================

    @ParameterizedTest
    @CsvSource({
        "0.0,   1000.00, 1000.00",  // Sin descuento
        "5.0,   1000.00,  950.00",  // 5% descuento
        "10.0,  1000.00,  900.00",  // 10% descuento  
        "25.0,  1000.00,  750.00",  // 25% descuento
        "50.0,  1000.00,  500.00",  // 50% descuento
        "75.0,  1000.00,  250.00",  // 75% descuento
        "100.0, 1000.00,    0.00"   // 100% descuento
    })
    @DisplayName("Diferentes porcentajes de descuento sobre base fija")
    void diferentes_porcentajes_descuento_sobre_base_fija(
            float porcentaje, double importeBase, double importeEsperado) {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoPlano promocion = new PromDsctoPlano(promoBase, porcentaje);
        int cantidad = 10;
        BigDecimal precioBase = BigDecimal.valueOf(100.00); // Base será $1000
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        BigDecimal esperado = BigDecimal.valueOf(importeEsperado);
        assertThat(resultado)
            .describedAs("Descuento %.1f%% sobre $%.2f", porcentaje, importeBase)
            .isEqualByComparingTo(esperado);
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Descuento 0% debería mantener precio base")
    void descuento0_deberia_mantenerPrecioBase() {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoPlano promocion = new PromDsctoPlano(promoBase, 0.0f);
        int cantidad = 5;
        BigDecimal precioBase = BigDecimal.valueOf(200.00);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        BigDecimal esperado = BigDecimal.valueOf(1000.00); // 5 × $200, sin descuento
        assertThat(resultado).isEqualByComparingTo(esperado);
    }

    @Test
    @DisplayName("Descuento 100% debería devolver $0")
    void descuento100_deberia_devolver0() {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoPlano promocion = new PromDsctoPlano(promoBase, 100.0f);
        int cantidad = 3;
        BigDecimal precioBase = BigDecimal.valueOf(150.00);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Descuento sobre cantidad 0 debería devolver $0")
    void descuento_sobre_cantidad0_deberia_devolver0() {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoPlano promocion = new PromDsctoPlano(promoBase, 25.0f);
        int cantidad = 0;
        BigDecimal precioBase = BigDecimal.valueOf(100.00);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ==================== TESTS DE CONSTRUCCIÓN ====================

    @Test
    @DisplayName("Constructor debería crear descripción correcta")
    void constructor_deberia_crear_descripcion_correcta() {
        // Arrange & Act
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoPlano promocion = new PromDsctoPlano(promoBase, 15.75f);
        
        // Assert
        assertThat(promocion.getDescripcion()).contains("15.75");
        assertThat(promocion.getDescripcion()).contains("%");
        assertThat(promocion.getDescripcion()).isEqualTo("Descuento Plano del 15.75 %");
        assertThat(promocion.getNombre()).isEqualTo("Dscto Plano");
    }

    @Test
    @DisplayName("Constructor con diferentes porcentajes")
    void constructor_diferentes_porcentajes_descripcion_correcta() {
        // Test múltiples casos
        PromSinDescto promoBase = new PromSinDescto();
        
        PromDsctoPlano promo5 = new PromDsctoPlano(promoBase, 5.0f);
        assertThat(promo5.getDescripcion()).isEqualTo("Descuento Plano del 5.00 %");
        
        PromDsctoPlano promo25 = new PromDsctoPlano(promoBase, 25.5f);
        assertThat(promo25.getDescripcion()).isEqualTo("Descuento Plano del 25.50 %");
        
        PromDsctoPlano promo100 = new PromDsctoPlano(promoBase, 100.0f);
        assertThat(promo100.getDescripcion()).isEqualTo("Descuento Plano del 100.00 %");
    }

    // ==================== TESTS DE PATRÓN DECORATOR ====================

    @Test
    @DisplayName("Debería mantener referencia a promoción base")
    void deberia_mantener_referencia_promocion_base() {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoPlano promocion = new PromDsctoPlano(promoBase, 10.0f);
        
        // Act & Assert
        // Verificar que mantiene la referencia (testing del patrón)
        assertThat(promocion).hasFieldOrProperty("promoBase");
        assertThat(promocion).hasFieldOrProperty("porcDescto");
    }

    @Test
    @DisplayName("Debería funcionar con cualquier promoción base (polimorfismo)")
    void deberia_funcionar_con_cualquier_promocion_base() {
        // Arrange - Diferentes tipos de promoción base
        PromSinDescto sinDescuento = new PromSinDescto();
        PromNXM promocion3x2 = new PromNXM(3, 2);
        
        PromDsctoPlano sobre_sinDescuento = new PromDsctoPlano(sinDescuento, 20.0f);
        PromDsctoPlano sobre_3x2 = new PromDsctoPlano(promocion3x2, 20.0f);
        
        int cantidad = 6;
        BigDecimal precioBase = BigDecimal.valueOf(100.00);
        
        // Act
        BigDecimal resultado_sinDesc = sobre_sinDescuento.calcularImportePromocion(cantidad, precioBase);
        BigDecimal resultado_3x2 = sobre_3x2.calcularImportePromocion(cantidad, precioBase);
        
        // Assert - Ambos deben funcionar pero con resultados diferentes
        assertThat(resultado_sinDesc).isEqualByComparingTo(BigDecimal.valueOf(480.00)); // (6×$100) - 20% = $480
        assertThat(resultado_3x2).isEqualByComparingTo(BigDecimal.valueOf(320.00));     // (4×$100) - 20% = $320
        
        // El descuento sobre 3x2 debe ser menor (mejor base para aplicar descuento)
        assertThat(resultado_3x2).isLessThan(resultado_sinDesc);
    }

    // ==================== TESTS DE PRECISIÓN DECIMAL ====================

    @Test
    @DisplayName("Debería manejar precisión decimal correctamente")
    void deberia_manejar_precision_decimal_correctamente() {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoPlano promocion = new PromDsctoPlano(promoBase, 33.333f); // Porcentaje que causa problemas
        int cantidad = 3;
        BigDecimal precioBase = new BigDecimal("100.00");
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        // Base: $300.00, Descuento 33.333%: $99.999, Final: $200.001
        BigDecimal esperado = new BigDecimal("200.001");
        assertThat(resultado).isCloseTo(esperado, within(new BigDecimal("0.001")));
    }

    // ==================== TESTS DE PERFORMANCE ====================

    @Test
    @DisplayName("Debería ejecutar rápidamente (incluyendo cálculo de promoción base)")
    void deberia_ejecutar_rapidamente() {
        // Arrange
        PromNXM promoBase = new PromNXM(5, 4); // Promoción base más compleja
        PromDsctoPlano promocion = new PromDsctoPlano(promoBase, 15.0f);
        int cantidad = 100;
        BigDecimal precioBase = BigDecimal.valueOf(50.00);
        
        // Act - Múltiples ejecuciones
        long inicio = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            promocion.calcularImportePromocion(cantidad, precioBase);
        }
        long duracion = System.currentTimeMillis() - inicio;
        
        // Assert - Debería ser rápido incluso con promoción base compleja
        assertThat(duracion).isLessThan(100);
    }

    // ==================== TESTS DE LÓGICA DE NEGOCIO ====================

    @Test
    @DisplayName("Descuento debería ser siempre mejor o igual que la promoción base")
    void descuento_deberia_ser_mejor_o_igual_que_promocion_base() {
        // Arrange
        PromNXM promoBase = new PromNXM(4, 3);
        PromDsctoPlano promocion = new PromDsctoPlano(promoBase, 10.0f); // 10% adicional
        int cantidad = 12;
        BigDecimal precioBase = BigDecimal.valueOf(25.00);
        
        // Act
        BigDecimal conDescuentoAdicional = promocion.calcularImportePromocion(cantidad, precioBase);
        BigDecimal soloPromoBase = promoBase.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        assertThat(conDescuentoAdicional).isLessThanOrEqualTo(soloPromoBase);
        
        // Verificar valores específicos
        // Base 4x3: 12 unidades = 3 grupos × 3 pagadas = 9 × $25 = $225
        assertThat(soloPromoBase).isEqualByComparingTo(BigDecimal.valueOf(225.00));
        // Con 10% adicional: $225 - ($225 × 0.10) = $225 - $22.50 = $202.50
        assertThat(conDescuentoAdicional).isEqualByComparingTo(BigDecimal.valueOf(202.50));
    }

    @Test
    @DisplayName("Múltiples descuentos planos deberían ser acumulativos (composición)")
    void multiples_descuentos_deberian_ser_acumulativos() {
        // Arrange - Composición de múltiples descuentos
        PromSinDescto base = new PromSinDescto();
        PromDsctoPlano desc1 = new PromDsctoPlano(base, 10.0f);          // Primer descuento 10%
        PromDsctoPlano desc2 = new PromDsctoPlano(desc1, 20.0f);         // Segundo descuento 20%
        
        int cantidad = 10;
        BigDecimal precioBase = BigDecimal.valueOf(100.00);
        
        // Act
        BigDecimal resultado = desc2.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        // Base: $1000
        // Primer descuento 10%: $1000 - $100 = $900  
        // Segundo descuento 20% sobre $900: $900 - $180 = $720
        BigDecimal esperado = BigDecimal.valueOf(720.00);
        assertThat(resultado).isEqualByComparingTo(esperado);
        
        // NO debería ser descuento simple 30% ($700)
        BigDecimal descuentoSimple30 = BigDecimal.valueOf(700.00);
        assertThat(resultado).isNotEqualTo(descuentoSimple30);
    }
}