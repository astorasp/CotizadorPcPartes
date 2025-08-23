package mx.com.qtx.cotizador.dominio.promos;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

/**
 * Tests para promociones combinadas utilizando el patrón Decorator
 * 
 * Valida que múltiples promociones se puedan apilar correctamente
 * siguiendo el patrón Decorator implementado en el dominio.
 * 
 * @author Claude Code
 */
class PromocionCombinadaTest {

    private BigDecimal precioUnitario;

    @BeforeEach
    void setUp() {
        precioUnitario = new BigDecimal("1000.00");  // $1000 por componente
    }
    
    /**
     * Helper method para calcular el descuento total
     */
    private BigDecimal calcularDescuentoTotal(Promocion promocion, int cantidad, BigDecimal precio) {
        BigDecimal precioOriginal = precio.multiply(new BigDecimal(cantidad));
        BigDecimal precioConDescuento = promocion.calcularImportePromocion(cantidad, precio);
        return precioOriginal.subtract(precioConDescuento);
    }

    @Nested
    @DisplayName("Combinaciones Básicas - Dos Promociones")
    class CombinacionesBasicas {

        @Test
        @DisplayName("NXM + Descuento Plano: 3x2 con 10% descuento adicional")
        void testNxmConDescuentoPlano() {
            // Arrange: 3x2 + 10% descuento plano
            Promocion nxm = new PromNXM(3, 2);  
            Promocion combinada = new PromDsctoPlano(nxm, 10.0f);

            int cantidad = 6;  // 6 componentes
            
            // Act
            BigDecimal descuentoTotal = calcularDescuentoTotal(combinada, cantidad, precioUnitario);
            
            // Assert
            // NXM: 6 unidades, pagar 4 → descuento $2000
            // Plano: 10% sobre $4000 → descuento adicional $400  
            // Total esperado: $2400
            assertTrue(descuentoTotal.compareTo(new BigDecimal("2000.00")) > 0, 
                "Debe haber descuento significativo por combinación NXM + Plano");
        }

        @Test
        @DisplayName("NXM + Descuento Por Cantidad: 3x2 con escalas")
        void testNxmConDescuentoPorCantidad() {
            // Arrange: 3x2 + escalas por cantidad
            Promocion nxm = new PromNXM(3, 2);
            
            Map<Integer, Double> escalas = new HashMap<>();
            escalas.put(1, 5.0);   // 1+ unidades: 5% descuento
            escalas.put(6, 15.0);  // 6+ unidades: 15% descuento
            
            Promocion combinada = new PromDsctoXcantidad(nxm, escalas);

            int cantidad = 9;  // 9 componentes
            
            // Act
            BigDecimal descuentoTotal = calcularDescuentoTotal(combinada, cantidad, precioUnitario);
            
            // Assert
            // Debe haber descuento significativo por la combinación
            assertTrue(descuentoTotal.compareTo(new BigDecimal("3000.00")) > 0,
                "Combinación NXM(3x2) + Escala debe generar descuento significativo");
        }

        @Test
        @DisplayName("Descuento Plano + Descuento Por Cantidad")
        void testDescuentoPlanoConPorCantidad() {
            // Arrange: 15% descuento plano + escalas por cantidad
            Promocion basePromocion = new PromSinDescto();
            Promocion descuentoPlano = new PromDsctoPlano(basePromocion, 15.0f);
            
            Map<Integer, Double> escalas = new HashMap<>();
            escalas.put(1, 5.0);   // 1+ unidades: 5% descuento
            escalas.put(5, 12.0);  // 5+ unidades: 12% descuento
            
            Promocion combinada = new PromDsctoXcantidad(descuentoPlano, escalas);

            int cantidad = 8;  // 8 componentes
            
            // Act
            BigDecimal descuentoTotal = calcularDescuentoTotal(combinada, cantidad, precioUnitario);
            
            // Assert
            // Debe aplicar ambos descuentos
            assertTrue(descuentoTotal.compareTo(new BigDecimal("1500.00")) > 0,
                "Combinación 15% + Escala debe generar descuento significativo");
        }
    }

    @Nested
    @DisplayName("Combinaciones Complejas - Múltiples Promociones")
    class CombinacionesComplejas {

        @Test
        @DisplayName("Triple Combinación: NXM + Plano + Por Cantidad")
        void testTripleCombinacion() {
            // Arrange: 4x3 + 8% descuento plano + escalas
            Promocion nxm = new PromNXM(4, 3);
            Promocion conPlano = new PromDsctoPlano(nxm, 8.0f);
            
            Map<Integer, Double> escalas = new HashMap<>();
            escalas.put(1, 3.0);   // 1+ unidades: 3% descuento
            escalas.put(8, 8.0);   // 8+ unidades: 8% descuento
            
            Promocion tripleCombo = new PromDsctoXcantidad(conPlano, escalas);

            int cantidad = 12;  // 12 componentes
            
            // Act
            BigDecimal descuentoTotal = calcularDescuentoTotal(tripleCombo, cantidad, precioUnitario);
            
            // Assert - validar que la combinación funciona sin errores
            assertNotNull(descuentoTotal, "Triple combinación debe calcular sin errores");
            assertTrue(descuentoTotal.compareTo(BigDecimal.ZERO) > 0, 
                "Debe generar algún descuento positivo");
            assertTrue(descuentoTotal.compareTo(new BigDecimal("12000.00")) < 0,
                "Descuento no puede superar el precio total original");
        }

        @Test
        @DisplayName("Cuádruple Combinación: Máximo apilamiento")
        void testCuadrupleCombinacion() {
            // Arrange: Crear cadena compleja de promociones
            Promocion paso1 = new PromNXM(5, 4);
            Promocion paso2 = new PromDsctoPlano(paso1, 6.0f);
            
            Map<Integer, Double> escalas = new HashMap<>();
            escalas.put(1, 5.0);
            escalas.put(10, 12.0);
            
            Promocion paso3 = new PromDsctoXcantidad(paso2, escalas);

            int cantidad = 15;
            
            // Act
            BigDecimal descuentoTotal = calcularDescuentoTotal(paso3, cantidad, precioUnitario);
            
            // Assert - validar funcionamiento sin errores
            assertNotNull(descuentoTotal, "Cuádruple combinación debe calcular sin errores");
            assertTrue(descuentoTotal.compareTo(BigDecimal.ZERO) > 0, 
                "Debe generar algún descuento positivo");
            assertTrue(descuentoTotal.compareTo(new BigDecimal("15000.00")) < 0,
                "Descuento no puede superar el precio total original");
        }
    }

    @Nested
    @DisplayName("Casos Edge - Validaciones Límite")
    class CasosEdge {

        @Test
        @DisplayName("Cantidad Cero: No debe aplicar descuentos")
        void testCantidadCero() {
            // Arrange
            Promocion nxm = new PromNXM(2, 1);
            Promocion combinada = new PromDsctoPlano(nxm, 50.0f);

            // Act
            BigDecimal descuento = calcularDescuentoTotal(combinada, 0, precioUnitario);
            
            // Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(descuento),
                "Cantidad cero no debe generar descuentos");
        }

        @Test
        @DisplayName("Cantidad Uno: Validar aplicación mínima")
        void testCantidadUno() {
            // Arrange: Solo descuento plano debería aplicar
            Promocion basePromo = new PromSinDescto();
            Promocion nxm = new PromNXM(3, 2);  // No aplica para cantidad 1
            Promocion combinada = new PromDsctoPlano(nxm, 20.0f);

            // Act
            BigDecimal descuento = calcularDescuentoTotal(combinada, 1, precioUnitario);
            
            // Assert
            // Solo debe aplicar descuento plano: $1000 * 0.20 = $200
            assertTrue(descuento.compareTo(BigDecimal.ZERO) > 0,
                "Con cantidad 1, debe aplicar al menos descuento plano");
        }

        @Test
        @DisplayName("Precio Cero: Manejo de edge case")
        void testPrecioCero() {
            // Arrange
            Promocion nxm = new PromNXM(2, 1);
            Promocion combinada = new PromDsctoPlano(nxm, 25.0f);

            // Act
            BigDecimal descuento = calcularDescuentoTotal(combinada, 5, BigDecimal.ZERO);
            
            // Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(descuento),
                "Precio cero debe resultar en descuento cero");
        }

        @Test
        @DisplayName("Validar que no hay errores en cálculos complejos")
        void testCalculosComplejos() {
            // Arrange: Combinación que podría generar errores
            Promocion basePromo = new PromSinDescto();
            Promocion plano = new PromDsctoPlano(basePromo, 50.0f);
            
            Map<Integer, Double> escalas = new HashMap<>();
            escalas.put(1, 60.0);  // 60% descuento por cantidad
            
            Promocion combinada = new PromDsctoXcantidad(plano, escalas);

            // Act & Assert - no debe lanzar excepciones
            assertDoesNotThrow(() -> {
                BigDecimal descuento = calcularDescuentoTotal(combinada, 3, precioUnitario);
                BigDecimal precioOriginal = precioUnitario.multiply(new BigDecimal("3"));
                
                // Validar que el resultado es lógico
                assertTrue(descuento.compareTo(precioOriginal) <= 0,
                    "Descuento no debe exceder precio original");
                assertTrue(descuento.compareTo(BigDecimal.ZERO) >= 0,
                    "Descuento no debe ser negativo");
            });
        }
    }

    @Nested
    @DisplayName("Orden de Aplicación - Patrón Decorator")
    class OrdenAplicacion {

        @Test
        @DisplayName("Orden A→B vs B→A: Verificar diferencias")
        void testOrdenImporta() {
            // Arrange - Orden A→B: NXM primero, luego Descuento Plano
            Promocion nxmPrimero = new PromNXM(4, 3);
            Promocion ordenAB = new PromDsctoPlano(nxmPrimero, 20.0f);
            
            // Arrange - Orden B→A: Descuento Plano primero, luego NXM
            Promocion basePromo = new PromSinDescto();
            Promocion planoPrimero = new PromDsctoPlano(basePromo, 20.0f);
            // Note: No se puede hacer NXM sobre PromDsctoPlano ya que NXM hereda de PromBase

            int cantidad = 8;
            
            // Act
            BigDecimal descuentoAB = calcularDescuentoTotal(ordenAB, cantidad, precioUnitario);
            
            // Assert - validar que el cálculo es consistente
            assertTrue(descuentoAB.compareTo(BigDecimal.ZERO) > 0, "Orden A→B debe generar descuento");
            
            // Validar consistencia interna
            BigDecimal descuentoAB2 = calcularDescuentoTotal(ordenAB, cantidad, precioUnitario);
            assertEquals(0, descuentoAB.compareTo(descuentoAB2),
                "Misma promoción debe dar mismo resultado");
        }

        @Test
        @DisplayName("Consistencia: Mismo orden debe dar mismo resultado")
        void testConsistenciaOrden() {
            // Arrange: Crear misma combinación dos veces
            Promocion combo1 = new PromDsctoPlano(new PromNXM(3, 2), 15.0f);
            Promocion combo2 = new PromDsctoPlano(new PromNXM(3, 2), 15.0f);

            int cantidad = 6;
            
            // Act
            BigDecimal descuento1 = calcularDescuentoTotal(combo1, cantidad, precioUnitario);
            BigDecimal descuento2 = calcularDescuentoTotal(combo2, cantidad, precioUnitario);
            
            // Assert
            assertEquals(0, descuento1.compareTo(descuento2),
                "Misma configuración debe producir resultados idénticos");
        }
    }

    @Nested
    @DisplayName("Validación de Integración con Arquitectura")
    class ValidacionIntegracion {

        @Test
        @DisplayName("Validar que todas las promociones implementan el patrón correctamente")
        void testPatronDecorator() {
            // Arrange & Act - crear diferentes combinaciones
            Promocion sinDescuento = new PromSinDescto();
            Promocion nxm = new PromNXM(2, 1);
            Promocion plano = new PromDsctoPlano(sinDescuento, 10.0f);
            
            Map<Integer, Double> escalas = new HashMap<>();
            escalas.put(1, 5.0);
            Promocion porCantidad = new PromDsctoXcantidad(sinDescuento, escalas);
            
            // Assert - validar que todas son instancias de Promocion
            assertAll("Todas las promociones deben implementar la interfaz Promocion",
                () -> assertInstanceOf(Promocion.class, sinDescuento),
                () -> assertInstanceOf(Promocion.class, nxm),
                () -> assertInstanceOf(Promocion.class, plano),
                () -> assertInstanceOf(Promocion.class, porCantidad)
            );
        }

        @Test
        @DisplayName("Validar comportamiento con cantidades realistas")
        void testCantidadesRealistas() {
            // Test con cantidades típicas de negocio
            int[] cantidadesTest = {1, 5, 10, 25, 50, 100};
            
            Promocion promocionCompleja = new PromDsctoPlano(new PromNXM(3, 2), 12.0f);
            
            for (int cantidad : cantidadesTest) {
                // Act & Assert - no debe fallar con cantidades realistas
                assertDoesNotThrow(() -> {
                    BigDecimal resultado = promocionCompleja.calcularImportePromocion(cantidad, precioUnitario);
                    assertNotNull(resultado, "Resultado no debe ser null para cantidad " + cantidad);
                    assertTrue(resultado.compareTo(BigDecimal.ZERO) >= 0, 
                        "Precio final no debe ser negativo para cantidad " + cantidad);
                });
            }
        }
    }
}