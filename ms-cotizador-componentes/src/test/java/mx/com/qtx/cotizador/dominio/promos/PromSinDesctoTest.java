package mx.com.qtx.cotizador.dominio.promos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitarios para PromSinDescto
 * 
 * Valida la lógica más básica del sistema de promociones:
 * calcularImportePromocion(cantidad, precioBase) = cantidad * precioBase
 * 
 * Casos cubiertos:
 * - Cálculos básicos correctos
 * - Edge cases (cantidad 0, cantidad 1)
 * - Validación de parámetros null
 * - Precisión decimal
 */
@DisplayName("PromSinDescto - Tests de Lógica de Dominio")
class PromSinDesctoTest {

    // ==================== CASOS BÁSICOS ====================

    @Test
    @DisplayName("Debería calcular correctamente: 5 unidades × $100 = $500")
    void calcular_cantidad5_precio100_deberia_devolver500() {
        // Arrange
        PromSinDescto promocion = new PromSinDescto();
        int cantidad = 5;
        BigDecimal precioBase = BigDecimal.valueOf(100.00);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        assertThat(resultado).isEqualByComparingTo(BigDecimal.valueOf(500.00));
    }

    @Test
    @DisplayName("Debería calcular correctamente: 1 unidad × $250.50 = $250.50")
    void calcular_cantidad1_precioDecimal_deberia_mantenerPrecision() {
        // Arrange
        PromSinDescto promocion = new PromSinDescto();
        int cantidad = 1;
        BigDecimal precioBase = new BigDecimal("250.50");
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        assertThat(resultado).isEqualByComparingTo(new BigDecimal("250.50"));
    }

    @Test
    @DisplayName("Debería calcular correctamente: 10 unidades × $99.99 = $999.90")
    void calcular_cantidad10_precio9999_deberia_conservarDecimales() {
        // Arrange
        PromSinDescto promocion = new PromSinDescto();
        int cantidad = 10;
        BigDecimal precioBase = new BigDecimal("99.99");
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        BigDecimal esperado = new BigDecimal("999.90");
        assertThat(resultado).isEqualByComparingTo(esperado);
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Debería devolver $0 cuando cantidad = 0")
    void calcular_cantidad0_deberia_devolver0() {
        // Arrange
        PromSinDescto promocion = new PromSinDescto();
        int cantidad = 0;
        BigDecimal precioBase = BigDecimal.valueOf(100.00);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Debería devolver $0 cuando precio base = $0")
    void calcular_precioBase0_deberia_devolver0() {
        // Arrange
        PromSinDescto promocion = new PromSinDescto();
        int cantidad = 5;
        BigDecimal precioBase = BigDecimal.ZERO;
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Debería manejar cantidades grandes sin overflow")
    void calcular_cantidadGrande_deberia_funcionar() {
        // Arrange
        PromSinDescto promocion = new PromSinDescto();
        int cantidad = 1000000; // 1 millón
        BigDecimal precioBase = new BigDecimal("0.01"); // 1 centavo
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        BigDecimal esperado = new BigDecimal("10000.00");
        assertThat(resultado).isEqualByComparingTo(esperado);
    }

    // ==================== VALIDACIÓN DE PARÁMETROS ====================

    @Test
    @DisplayName("Debería lanzar excepción cuando precioBase es null")
    void calcular_precioBaseNull_deberia_lanzarExcepcion() {
        // Arrange
        PromSinDescto promocion = new PromSinDescto();
        int cantidad = 5;
        BigDecimal precioBase = null;
        
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            promocion.calcularImportePromocion(cantidad, precioBase);
        });
    }

    @Test
    @DisplayName("Debería manejar cantidad negativa (comportamiento actual)")
    void calcular_cantidadNegativa_comportamientoActual() {
        // Arrange
        PromSinDescto promocion = new PromSinDescto();
        int cantidad = -5;
        BigDecimal precioBase = BigDecimal.valueOf(100.00);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        // El comportamiento actual permite negativos - retorna valor negativo
        assertThat(resultado).isEqualByComparingTo(BigDecimal.valueOf(-500.00));
    }

    // ==================== TESTS DE METADATOS ====================

    @Test
    @DisplayName("Debería tener descripción correcta")
    void deberia_tener_descripcion_correcta() {
        // Arrange & Act
        PromSinDescto promocion = new PromSinDescto();
        
        // Assert
        assertThat(promocion.getDescripcion()).isEqualTo("No se aplica ningun descuento");
        assertThat(promocion.getNombre()).isEqualTo("Precio regular");
    }

    // ==================== TESTS DE INTEGRACIÓN CON BIGDECIMAL ====================

    @Test
    @DisplayName("Debería manejar precisión de BigDecimal correctamente")
    void deberia_manejar_precision_bigdecimal() {
        // Arrange
        PromSinDescto promocion = new PromSinDescto();
        int cantidad = 3;
        // Precio que puede causar problemas de precisión con doubles
        BigDecimal precioBase = new BigDecimal("33.333333333333");
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        BigDecimal esperado = new BigDecimal("99.999999999999");
        assertThat(resultado).isEqualByComparingTo(esperado);
    }

    @Test
    @DisplayName("Debería ser consistente en múltiples cálculos")
    void deberia_ser_consistente_en_multiples_calculos() {
        // Arrange
        PromSinDescto promocion = new PromSinDescto();
        int cantidad = 7;
        BigDecimal precioBase = new BigDecimal("42.50");
        
        // Act - Ejecutar el mismo cálculo varias veces
        BigDecimal resultado1 = promocion.calcularImportePromocion(cantidad, precioBase);
        BigDecimal resultado2 = promocion.calcularImportePromocion(cantidad, precioBase);
        BigDecimal resultado3 = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert - Todos los resultados deben ser idénticos
        assertThat(resultado1).isEqualByComparingTo(resultado2);
        assertThat(resultado2).isEqualByComparingTo(resultado3);
        
        BigDecimal esperado = new BigDecimal("297.50");
        assertThat(resultado1).isEqualByComparingTo(esperado);
    }

    // ==================== PERFORMANCE TEST ====================

    @Test
    @DisplayName("Debería ejecutar rápidamente (test de performance básico)")
    void deberia_ejecutar_rapidamente() {
        // Arrange
        PromSinDescto promocion = new PromSinDescto();
        int cantidad = 100;
        BigDecimal precioBase = new BigDecimal("50.00");
        
        // Act - Medir tiempo de 1000 ejecuciones
        long inicio = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            promocion.calcularImportePromocion(cantidad, precioBase);
        }
        long duracion = System.currentTimeMillis() - inicio;
        
        // Assert - Debería tomar menos de 100ms para 1000 cálculos
        assertThat(duracion).isLessThan(100);
    }
}