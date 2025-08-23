package mx.com.qtx.cotizador.dominio.promos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests unitarios para PromDsctoXcantidad (Descuento por Escalas)
 * 
 * Valida el algoritmo más complejo del sistema:
 * 1. Buscar todas las escalas aplicables (cantidad >= escala)  
 * 2. Seleccionar la escala más alta aplicable
 * 3. Aplicar el descuento correspondiente
 * 
 * Algoritmo crítico probado:
 * mapCantidadVsDscto.keySet().stream()
 *   .sorted()                           // Ordena ascendente
 *   .filter(k -> k <= cant)             // Solo escalas aplicables
 *   .sorted((n,n2) -> n <= n2 ? 1 : -1) // Reordena descendente  
 *   .findFirst()                        // Toma la más alta aplicable
 *   .get()                              // ⚠️ Puede lanzar excepción
 * 
 * CRÍTICO: Casos edge pueden causar NoSuchElementException
 */
@DisplayName("PromDsctoXcantidad - Tests de Lógica de Dominio (Algoritmo de Escalas)")
class PromDsctoXcantidadTest {

    // ==================== CONFIGURACIÓN DE ESCALAS TÍPICA ====================
    
    private Map<Integer, Double> crearEscalasTipicas() {
        Map<Integer, Double> escalas = new HashMap<>();
        escalas.put(1, 0.0);    // 1-2 unidades: sin descuento
        escalas.put(3, 5.0);    // 3-5 unidades: 5% descuento
        escalas.put(6, 10.0);   // 6-10 unidades: 10% descuento  
        escalas.put(11, 15.0);  // 11+ unidades: 15% descuento
        return escalas;
    }

    // ==================== TESTS DE SELECCIÓN DE ESCALAS ====================

    @Test
    @DisplayName("Cantidad 1 debería seleccionar escala base (0% descuento)")
    void cantidad1_deberia_seleccionar_escala_base() {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, crearEscalasTipicas());
        int cantidad = 1;
        BigDecimal precioBase = BigDecimal.valueOf(100.00);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        // Cantidad 1 → escala 1 (0%) → $100 - $0 = $100
        BigDecimal esperado = BigDecimal.valueOf(100.00);
        assertThat(resultado).isEqualByComparingTo(esperado);
    }

    @Test
    @DisplayName("Cantidad 4 debería seleccionar escala 3-5 (5% descuento)")
    void cantidad4_deberia_seleccionar_escala_3a5() {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, crearEscalasTipicas());
        int cantidad = 4;
        BigDecimal precioBase = BigDecimal.valueOf(100.00);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        // Cantidad 4 → escala 3 (5%) → $400 - ($400 × 0.05) = $400 - $20 = $380
        BigDecimal esperado = BigDecimal.valueOf(380.00);
        assertThat(resultado).isEqualByComparingTo(esperado);
    }

    @Test
    @DisplayName("Cantidad 8 debería seleccionar escala 6-10 (10% descuento)")
    void cantidad8_deberia_seleccionar_escala_6a10() {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, crearEscalasTipicas());
        int cantidad = 8;
        BigDecimal precioBase = BigDecimal.valueOf(50.00);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        // Cantidad 8 → escala 6 (10%) → $400 - ($400 × 0.10) = $400 - $40 = $360
        BigDecimal esperado = BigDecimal.valueOf(360.00);
        assertThat(resultado).isEqualByComparingTo(esperado);
    }

    @Test
    @DisplayName("Cantidad 15 debería seleccionar escala más alta (15% descuento)")
    void cantidad15_deberia_seleccionar_escala_mas_alta() {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, crearEscalasTipicas());
        int cantidad = 15;
        BigDecimal precioBase = BigDecimal.valueOf(20.00);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        // Cantidad 15 → escala 11 (15%) → $300 - ($300 × 0.15) = $300 - $45 = $255
        BigDecimal esperado = BigDecimal.valueOf(255.00);
        assertThat(resultado).isEqualByComparingTo(esperado);
    }

    // ==================== TESTS PARAMETRIZADOS CON ESCALAS TÍPICAS ====================

    @ParameterizedTest
    @CsvSource({
        "1,  100.00", // Escala 1 (0%): $100
        "2,  200.00", // Escala 1 (0%): $200
        "3,  285.00", // Escala 3 (5%): $300 - $15 = $285
        "5,  475.00", // Escala 3 (5%): $500 - $25 = $475
        "6,  540.00", // Escala 6 (10%): $600 - $60 = $540
        "10, 900.00", // Escala 6 (10%): $1000 - $100 = $900
        "11, 935.00", // Escala 11 (15%): $1100 - $165 = $935
        "20, 1700.00" // Escala 11 (15%): $2000 - $300 = $1700
    })
    @DisplayName("Casos parametrizados con escalas típicas")
    void escalas_tipicas_casos_parametrizados(int cantidad, double importeEsperado) {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, crearEscalasTipicas());
        BigDecimal precioBase = BigDecimal.valueOf(100.00);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        BigDecimal esperado = BigDecimal.valueOf(importeEsperado);
        assertThat(resultado)
            .describedAs("Cantidad %d con escalas típicas", cantidad)
            .isEqualByComparingTo(esperado);
    }

    // ==================== TESTS CON ESCALAS PERSONALIZADAS ====================

    @Test
    @DisplayName("Escalas con gaps: cantidad en medio de gap debería usar escala anterior")
    void escalas_con_gaps_deberia_usar_escala_anterior() {
        // Arrange - Escalas con gaps: 1, 5, 20
        Map<Integer, Double> escalasConGaps = new HashMap<>();
        escalasConGaps.put(1, 0.0);   // 1-4: 0%
        escalasConGaps.put(5, 10.0);  // 5-19: 10%
        escalasConGaps.put(20, 25.0); // 20+: 25%
        
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, escalasConGaps);
        
        // Test cantidad 12 (está en el gap entre 5 y 20)
        BigDecimal resultado = promocion.calcularImportePromocion(12, BigDecimal.valueOf(50.00));
        
        // Assert
        // Cantidad 12 → escala 5 (10%) → $600 - $60 = $540
        assertThat(resultado).isEqualByComparingTo(BigDecimal.valueOf(540.00));
    }

    @Test
    @DisplayName("Escalas desordenadas: debería funcionar correctamente")
    void escalas_desordenadas_deberia_funcionar() {
        // Arrange - Insertar escalas en orden no secuencial
        Map<Integer, Double> escalasDesordenadas = new HashMap<>();
        escalasDesordenadas.put(10, 20.0);  // Segundo
        escalasDesordenadas.put(1, 0.0);    // Primero  
        escalasDesordenadas.put(5, 10.0);   // Tercero
        escalasDesordenadas.put(15, 30.0);  // Cuarto
        
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, escalasDesordenadas);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(7, BigDecimal.valueOf(100.00));
        
        // Assert
        // Cantidad 7 → escala 5 (10%) → $700 - $70 = $630
        assertThat(resultado).isEqualByComparingTo(BigDecimal.valueOf(630.00));
    }

    // ==================== DECORATOR SOBRE PROMOCIONES BASE COMPLEJAS ====================

    @Test
    @DisplayName("Descuento por cantidad sobre promoción 3x2")
    void descuento_cantidad_sobre_promocion_3x2() {
        // Arrange
        PromNXM promo3x2 = new PromNXM(3, 2);
        
        Map<Integer, Double> escalas = new HashMap<>();
        escalas.put(1, 0.0);
        escalas.put(5, 20.0); // 20% adicional sobre 5+ unidades
        
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promo3x2, escalas);
        int cantidad = 6; // 2 grupos de 3x2
        BigDecimal precioBase = BigDecimal.valueOf(100.00);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        // Paso 1: Promoción 3x2 con 6 unidades = 2 grupos = paga 4 unidades = $400
        // Paso 2: Descuento por cantidad 6 >= 5 = 20% sobre $400 = $400 - $80 = $320
        BigDecimal esperado = BigDecimal.valueOf(320.00);
        assertThat(resultado).isEqualByComparingTo(esperado);
    }

    // ==================== EDGE CASES CRÍTICOS ====================

    @Test
    @DisplayName("Map vacío debería devolver precio base sin descuento")
    void map_vacio_deberia_devolver_precio_base() {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        Map<Integer, Double> mapVacio = new HashMap<>();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, mapVacio);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(5, BigDecimal.valueOf(100.00));
        
        // Assert
        // Sin escalas disponibles, devuelve precio base: 5 × $100 = $500
        assertThat(resultado).isEqualByComparingTo(BigDecimal.valueOf(500.00));
    }

    @Test
    @DisplayName("Cantidad menor que escala mínima debería devolver precio base")
    void cantidad_menor_que_escala_minima_deberia_devolver_precio_base() {
        // Arrange - Escalas que empiezan en 5
        Map<Integer, Double> escalasDesde5 = new HashMap<>();
        escalasDesde5.put(5, 10.0);
        escalasDesde5.put(10, 20.0);
        
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, escalasDesde5);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(3, BigDecimal.valueOf(100.00));
        
        // Assert - Cantidad 3 < 5, sin descuento aplicable: 3 × $100 = $300
        assertThat(resultado).isEqualByComparingTo(BigDecimal.valueOf(300.00));
    }

    @Test
    @DisplayName("Cantidad 0 debería devolver precio base sin descuento (bug corregido)")
    void cantidad0_deberia_devolver_precioBase_bug_corregido() {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, crearEscalasTipicas());
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(0, BigDecimal.valueOf(100.00));
        
        // Assert
        // BUG CORREGIDO: Cantidad 0 ahora devuelve correctamente 0 × $100 = $0
        // sin lanzar NoSuchElementException
        assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ==================== TESTS DE CONSTRUCCIÓN ====================

    @Test
    @DisplayName("Constructor debería crear descripción con mapa de escalas")
    void constructor_deberia_crear_descripcion_con_escalas() {
        // Arrange & Act
        PromSinDescto promoBase = new PromSinDescto();
        Map<Integer, Double> escalas = crearEscalasTipicas();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, escalas);
        
        // Assert
        assertThat(promocion.getDescripcion()).contains("tabla de cantidades y descuentos");
        assertThat(promocion.getDescripcion()).contains(escalas.toString());
        assertThat(promocion.getNombre()).isEqualTo("Dscto x cantidad");
    }

    // ==================== TESTS DE PRECISIÓN DECIMAL ====================

    @Test
    @DisplayName("Debería manejar descuentos con decimales correctamente")
    void deberia_manejar_descuentos_con_decimales() {
        // Arrange
        Map<Integer, Double> escalasConDecimales = new HashMap<>();
        escalasConDecimales.put(1, 7.25);   // 7.25%
        escalasConDecimales.put(5, 12.75);  // 12.75%
        
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, escalasConDecimales);
        
        // Act
        BigDecimal resultado = promocion.calcularImportePromocion(8, new BigDecimal("200.00"));
        
        // Assert
        // Cantidad 8 → escala 5 (12.75%) → $1600 - ($1600 × 0.1275) = $1600 - $204 = $1396
        BigDecimal esperado = new BigDecimal("1396.00");
        assertThat(resultado).isEqualByComparingTo(esperado);
    }

    // ==================== TESTS DE PERFORMANCE ====================

    @Test
    @DisplayName("Debería ejecutar algoritmo de selección rápidamente")
    void algoritmo_seleccion_deberia_ser_rapido() {
        // Arrange - Map grande con muchas escalas
        Map<Integer, Double> escalasGrandes = new HashMap<>();
        for (int i = 1; i <= 100; i += 5) {
            escalasGrandes.put(i, (double) i / 2);
        }
        
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, escalasGrandes);
        
        // Act - Múltiples ejecuciones con cantidad alta (worst case)
        long inicio = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            promocion.calcularImportePromocion(99, BigDecimal.valueOf(10.00));
        }
        long duracion = System.currentTimeMillis() - inicio;
        
        // Assert
        assertThat(duracion).isLessThan(200); // Tolerante por complejidad del algoritmo
    }

    // ==================== TESTS DE LÓGICA DE NEGOCIO ====================

    @Test
    @DisplayName("Escalas mayores deberían dar mejores descuentos")
    void escalas_mayores_deberian_dar_mejores_descuentos() {
        // Arrange
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, crearEscalasTipicas());
        BigDecimal precioBase = BigDecimal.valueOf(100.00);
        
        // Act - Comparar diferentes cantidades
        BigDecimal resultado3 = promocion.calcularImportePromocion(3, precioBase);  // Escala 3 (5%)
        BigDecimal resultado8 = promocion.calcularImportePromocion(8, precioBase);  // Escala 6 (10%) 
        BigDecimal resultado15 = promocion.calcularImportePromocion(15, precioBase); // Escala 11 (15%)
        
        // Assert - El precio por unidad debe ser decreciente
        BigDecimal precioPorUnidad3 = resultado3.divide(BigDecimal.valueOf(3), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal precioPorUnidad8 = resultado8.divide(BigDecimal.valueOf(8), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal precioPorUnidad15 = resultado15.divide(BigDecimal.valueOf(15), 2, BigDecimal.ROUND_HALF_UP);
        
        assertThat(precioPorUnidad8).isLessThan(precioPorUnidad3);
        assertThat(precioPorUnidad15).isLessThan(precioPorUnidad8);
    }

    @Test
    @DisplayName("Debería ser mejor o igual que promoción base sola")
    void deberia_ser_mejor_que_promocion_base_sola() {
        // Arrange
        PromNXM promo5x4 = new PromNXM(5, 4);
        
        Map<Integer, Double> escalas = new HashMap<>();
        escalas.put(1, 0.0);   // Sin descuento adicional para pocas unidades
        escalas.put(10, 15.0); // 15% adicional para 10+ unidades
        
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promo5x4, escalas);
        int cantidad = 15;
        BigDecimal precioBase = BigDecimal.valueOf(30.00);
        
        // Act
        BigDecimal conDescuentoCantidad = promocion.calcularImportePromocion(cantidad, precioBase);
        BigDecimal soloPromoBase = promo5x4.calcularImportePromocion(cantidad, precioBase);
        
        // Assert
        assertThat(conDescuentoCantidad).isLessThanOrEqualTo(soloPromoBase);
        
        // Verificar cálculo específico
        // Promo 5x4: 15 unidades = 3 grupos × 4 pagadas = 12 × $30 = $360
        assertThat(soloPromoBase).isEqualByComparingTo(BigDecimal.valueOf(360.00));
        // Con descuento cantidad 15%: $360 - ($360 × 0.15) = $360 - $54 = $306
        assertThat(conDescuentoCantidad).isEqualByComparingTo(BigDecimal.valueOf(306.00));
    }

    // ==================== TESTS CASOS SIMPLES CRÍTICOS (FALTANTES) ====================

    @Test
    @DisplayName("Un solo rango: solo descuento para 5+ unidades (caso típico de negocio)")
    void un_solo_rango_descuento_5plus() {
        // Arrange - Caso típico: "Compra 5 o más, obtén 15% descuento"
        Map<Integer, Double> unSoloRango = new HashMap<>();
        unSoloRango.put(5, 15.0); // Solo: 5+ unidades = 15% descuento
        
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, unSoloRango);
        BigDecimal precioBase = BigDecimal.valueOf(50.00);
        
        // Act & Assert - Cantidad menor a 5: sin descuento
        BigDecimal resultado3 = promocion.calcularImportePromocion(3, precioBase);
        // Cantidad 3 < 5 → no hay escala aplicable → precio base: 3 × $50 = $150
        assertThat(resultado3).isEqualByComparingTo(BigDecimal.valueOf(150.00));
        
        // Act & Assert - Cantidad 5: con descuento
        BigDecimal resultado5 = promocion.calcularImportePromocion(5, precioBase);
        // Cantidad 5 >= 5 → escala 5 (15%) → $250 - ($250 × 0.15) = $250 - $37.5 = $212.5
        assertThat(resultado5).isEqualByComparingTo(BigDecimal.valueOf(212.50));
        
        // Act & Assert - Cantidad 10: mismo descuento (solo hay un rango)
        BigDecimal resultado10 = promocion.calcularImportePromocion(10, precioBase);
        // Cantidad 10 >= 5 → escala 5 (15%) → $500 - ($500 × 0.15) = $500 - $75 = $425
        assertThat(resultado10).isEqualByComparingTo(BigDecimal.valueOf(425.00));
    }

    @Test
    @DisplayName("Dos rangos exactos: normal vs mayoreo (patrón binario típico)")
    void dos_rangos_normal_vs_mayoreo() {
        // Arrange - Patrón típico de negocio: precio normal vs. precio mayoreo
        Map<Integer, Double> dosRangos = new HashMap<>();
        dosRangos.put(1, 0.0);   // 1-4 unidades: precio normal (0% descuento)
        dosRangos.put(5, 20.0);  // 5+ unidades: precio mayoreo (20% descuento)
        
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, dosRangos);
        BigDecimal precioBase = BigDecimal.valueOf(100.00);
        
        // Act & Assert - Rango normal (1-4 unidades)
        BigDecimal resultado1 = promocion.calcularImportePromocion(1, precioBase);
        // Cantidad 1 → escala 1 (0%) → $100 - $0 = $100
        assertThat(resultado1).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        
        BigDecimal resultado4 = promocion.calcularImportePromocion(4, precioBase);
        // Cantidad 4 → escala 1 (0%) → $400 - $0 = $400
        assertThat(resultado4).isEqualByComparingTo(BigDecimal.valueOf(400.00));
        
        // Act & Assert - Rango mayoreo (5+ unidades)
        BigDecimal resultado5 = promocion.calcularImportePromocion(5, precioBase);
        // Cantidad 5 → escala 5 (20%) → $500 - ($500 × 0.20) = $500 - $100 = $400
        assertThat(resultado5).isEqualByComparingTo(BigDecimal.valueOf(400.00));
        
        BigDecimal resultado15 = promocion.calcularImportePromocion(15, precioBase);
        // Cantidad 15 → escala 5 (20%) → $1500 - ($1500 × 0.20) = $1500 - $300 = $1200
        assertThat(resultado15).isEqualByComparingTo(BigDecimal.valueOf(1200.00));
    }

    @Test
    @DisplayName("Transición en frontera exacta entre rangos (boundary testing)")
    void transicion_frontera_exacta_entre_rangos() {
        // Arrange - Test crítico de fronteras entre rangos
        Map<Integer, Double> rangosFrontera = new HashMap<>();
        rangosFrontera.put(1, 0.0);    // 1-2 unidades: 0%
        rangosFrontera.put(3, 10.0);   // 3-9 unidades: 10%
        rangosFrontera.put(10, 25.0);  // 10+ unidades: 25%
        
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, rangosFrontera);
        BigDecimal precioBase = BigDecimal.valueOf(50.00);
        
        // Act & Assert - Frontera 2-3 (cambio de 0% a 10%)
        BigDecimal resultado2 = promocion.calcularImportePromocion(2, precioBase);
        // Cantidad 2 → escala 1 (0%) → $100 - $0 = $100
        assertThat(resultado2).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        
        BigDecimal resultado3 = promocion.calcularImportePromocion(3, precioBase);
        // Cantidad 3 → escala 3 (10%) → $150 - $15 = $135
        assertThat(resultado3).isEqualByComparingTo(BigDecimal.valueOf(135.00));
        
        // Act & Assert - Frontera 9-10 (cambio de 10% a 25%)
        BigDecimal resultado9 = promocion.calcularImportePromocion(9, precioBase);
        // Cantidad 9 → escala 3 (10%) → $450 - $45 = $405
        assertThat(resultado9).isEqualByComparingTo(BigDecimal.valueOf(405.00));
        
        BigDecimal resultado10 = promocion.calcularImportePromocion(10, precioBase);
        // Cantidad 10 → escala 10 (25%) → $500 - $125 = $375
        assertThat(resultado10).isEqualByComparingTo(BigDecimal.valueOf(375.00));
        
        // Verificar progresión lógica: precio unitario efectivo debería mejorar con mayor descuento
        BigDecimal precioUnitario2 = resultado2.divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal precioUnitario3 = resultado3.divide(BigDecimal.valueOf(3), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal precioUnitario9 = resultado9.divide(BigDecimal.valueOf(9), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal precioUnitario10 = resultado10.divide(BigDecimal.valueOf(10), 2, BigDecimal.ROUND_HALF_UP);
        
        // Con descuento, precio unitario debería ser menor
        assertThat(precioUnitario3).isLessThan(precioUnitario2); // 10% vs 0%
        assertThat(precioUnitario10).isLessThan(precioUnitario9); // 25% vs 10%
    }

    @Test
    @DisplayName("Caso extremo: escalón único en cantidad muy alta")
    void escalon_unico_cantidad_muy_alta() {
        // Arrange - Caso de negocio: "Descuento VIP solo para compras masivas"
        Map<Integer, Double> escalonAlto = new HashMap<>();
        escalonAlto.put(50, 30.0); // Solo 50+ unidades = 30% descuento VIP
        
        PromSinDescto promoBase = new PromSinDescto();
        PromDsctoXcantidad promocion = new PromDsctoXcantidad(promoBase, escalonAlto);
        BigDecimal precioBase = BigDecimal.valueOf(25.00);
        
        // Act & Assert - Cantidad menor: sin descuento
        BigDecimal resultado30 = promocion.calcularImportePromocion(30, precioBase);
        // Cantidad 30 < 50 → no hay escala aplicable → precio base: 30 × $25 = $750
        assertThat(resultado30).isEqualByComparingTo(BigDecimal.valueOf(750.00));
        
        // Act & Assert - Cantidad en frontera exacta
        BigDecimal resultado50 = promocion.calcularImportePromocion(50, precioBase);
        // Cantidad 50 >= 50 → escala 50 (30%) → $1250 - $375 = $875
        assertThat(resultado50).isEqualByComparingTo(BigDecimal.valueOf(875.00));
        
        // Act & Assert - Cantidad muy alta: mismo descuento
        BigDecimal resultado100 = promocion.calcularImportePromocion(100, precioBase);
        // Cantidad 100 >= 50 → escala 50 (30%) → $2500 - $750 = $1750
        assertThat(resultado100).isEqualByComparingTo(BigDecimal.valueOf(1750.00));
    }
}