package mx.com.qtx.cotizador.dominio.impuestos;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.*;

/**
 * Pruebas unitarias para la clase {@link CalculadorImpuestosUsa}.
 * <p>
 * Esta suite de pruebas verifica el correcto funcionamiento del calculador de impuestos
 * para Estados Unidos, incluyendo el cálculo preciso del Sales Tax (5%), manejo de diferentes
 * montos y casos límite, así como la precisión en los cálculos BigDecimal.
 * </p>
 *
 * <h3>Cobertura de pruebas:</h3>
 * <ul>
 *   <li><strong>Constructor:</strong> Creación correcta de instancias</li>
 *   <li><strong>Cálculo Sales Tax:</strong> Fórmula correcta (monto × 0.05)</li>
 *   <li><strong>Precisión decimal:</strong> Manejo correcto de BigDecimal</li>
 *   <li><strong>Casos límite:</strong> Montos cero, pequeños, grandes y negativos</li>
 *   <li><strong>Casos reales:</strong> Escenarios típicos de cotización estadounidense</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("CalculadorImpuestosUsa - Pruebas Unitarias")
class CalculadorImpuestosUsaTest {

    private CalculadorImpuestosUsa calculadorUsa;

    /**
     * Configuración inicial para cada prueba.
     */
    @BeforeEach
    void setUp() {
        calculadorUsa = new CalculadorImpuestosUsa();
    }

    @Nested
    @DisplayName("Constructor e Inicialización")
    class ConstructorInicializacionTest {

        @Test
        @DisplayName("Debería crear instancia correctamente")
        void deberiaCrearInstanciaCorrectamente() {
            // When
            CalculadorImpuestosUsa resultado = new CalculadorImpuestosUsa();

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado).isInstanceOf(ICalculadorImpuestoPais.class);
        }

        @Test
        @DisplayName("Debería implementar interfaz ICalculadorImpuestoPais")
        void deberiaImplementarInterfazICalculadorImpuestoPais() {
            // Then
            assertThat(calculadorUsa).isInstanceOf(ICalculadorImpuestoPais.class);
        }
    }

    @Nested
    @DisplayName("Cálculo de Sales Tax USA")
    class CalculoSalesTaxUsaTest {

        @Test
        @DisplayName("Debería calcular Sales Tax 5% con monto típico")
        void deberiaCalcularSalesTax5PorcentoConMontoTipico() {
            // Given
            BigDecimal monto = new BigDecimal("1000.00");

            // When
            BigDecimal resultado = calculadorUsa.calcularImpuestoPais(monto);

            // Then
            BigDecimal salesTaxEsperado = new BigDecimal("50.00"); // 1000 × 0.05
            assertThat(resultado).isEqualByComparingTo(salesTaxEsperado);
        }

        @Test
        @DisplayName("Debería calcular Sales Tax correctamente con monto cero")
        void deberiaCalcularSalesTaxCorrectamenteConMontoCero() {
            // Given
            BigDecimal monto = BigDecimal.ZERO;

            // When
            BigDecimal resultado = calculadorUsa.calcularImpuestoPais(monto);

            // Then
            assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Debería calcular Sales Tax con monto pequeño")
        void deberiaCalcularSalesTaxConMontoPequeno() {
            // Given
            BigDecimal monto = new BigDecimal("10.50");

            // When
            BigDecimal resultado = calculadorUsa.calcularImpuestoPais(monto);

            // Then
            BigDecimal salesTaxEsperado = new BigDecimal("0.525"); // 10.50 × 0.05
            assertThat(resultado).isEqualByComparingTo(salesTaxEsperado);
        }

        @Test
        @DisplayName("Debería calcular Sales Tax con monto grande")
        void deberiaCalcularSalesTaxConMontoGrande() {
            // Given
            BigDecimal monto = new BigDecimal("50000.00");

            // When
            BigDecimal resultado = calculadorUsa.calcularImpuestoPais(monto);

            // Then
            BigDecimal salesTaxEsperado = new BigDecimal("2500.00"); // 50000 × 0.05
            assertThat(resultado).isEqualByComparingTo(salesTaxEsperado);
        }

        @Test
        @DisplayName("Debería mantener precisión decimal en cálculos")
        void deberiaMantenerPrecisionDecimalEnCalculos() {
            // Given
            BigDecimal monto = new BigDecimal("999.99");

            // When
            BigDecimal resultado = calculadorUsa.calcularImpuestoPais(monto);

            // Then
            BigDecimal salesTaxEsperado = new BigDecimal("49.9995"); // 999.99 × 0.05
            assertThat(resultado).isEqualByComparingTo(salesTaxEsperado);
        }

        @Test
        @DisplayName("Debería diferenciarse de otros países (5% vs 15% vs 16%)")
        void deberiaDiferenciarseDeOtrosPaises5PorcentoVs15PorcentoVs16Porciento() {
            // Given
            BigDecimal monto = new BigDecimal("1000.00");

            // When
            BigDecimal salesTaxUsa = calculadorUsa.calcularImpuestoPais(monto);

            // Then - Sales Tax USA debe ser 5% (50)
            assertThat(salesTaxUsa).isEqualByComparingTo(new BigDecimal("50.00"));
            // Verificar que NO es GST Canadá (150) ni IVA México (160)
            assertThat(salesTaxUsa).isNotEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(salesTaxUsa).isNotEqualByComparingTo(new BigDecimal("160.00"));
        }
    }

    @Nested
    @DisplayName("Fórmula y Validaciones")
    class FormulaValidacionesTest {

        @Test
        @DisplayName("Debería aplicar fórmula correcta: monto × 0.05")
        void deberiaAplicarFormulaCorrectaMontoMultiplicadoPor005() {
            // Given - Diferentes montos para validar la fórmula
            BigDecimal[] montos = {
                new BigDecimal("100.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("2500.00")
            };

            BigDecimal[] resultadosEsperados = {
                new BigDecimal("5.00"),    // 100 × 0.05
                new BigDecimal("25.00"),   // 500 × 0.05
                new BigDecimal("50.00"),   // 1000 × 0.05
                new BigDecimal("125.00")   // 2500 × 0.05
            };

            // When & Then
            for (int i = 0; i < montos.length; i++) {
                BigDecimal resultado = calculadorUsa.calcularImpuestoPais(montos[i]);
                assertThat(resultado).isEqualByComparingTo(resultadosEsperados[i]);
            }
        }

        @Test
        @DisplayName("Debería mantener tasa constante de 5%")
        void deberiaMantenerTasaConstanteDe5Porciento() {
            // Given - Diferentes montos
            BigDecimal[] montos = {
                new BigDecimal("1.00"),
                new BigDecimal("10.00"),
                new BigDecimal("100.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("10000.00")
            };

            // When & Then - Todos deberían tener exactamente 5% de Sales Tax
            for (BigDecimal monto : montos) {
                BigDecimal resultado = calculadorUsa.calcularImpuestoPais(monto);
                BigDecimal salesTaxCalculado = monto.multiply(BigDecimal.valueOf(0.05));

                assertThat(resultado).isEqualByComparingTo(salesTaxCalculado);

                // Verificar que el porcentaje es exactamente 5%
                if (monto.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal porcentaje = resultado.divide(monto, 4, BigDecimal.ROUND_HALF_UP);
                    assertThat(porcentaje).isEqualByComparingTo(new BigDecimal("0.0500"));
                }
            }
        }

        @Test
        @DisplayName("Debería manejar montos negativos correctamente")
        void deberiaManejarMontosNegativosCorrectamente() {
            // Given
            BigDecimal montoNegativo = new BigDecimal("-1000.00");

            // When
            BigDecimal resultado = calculadorUsa.calcularImpuestoPais(montoNegativo);

            // Then - El Sales Tax de un monto negativo es negativo
            BigDecimal salesTaxEsperado = new BigDecimal("-50.00"); // -1000 × 0.05
            assertThat(resultado).isEqualByComparingTo(salesTaxEsperado);
            assertThat(resultado).isNegative();
        }

        @Test
        @DisplayName("Debería ser la tasa más baja de los tres países")
        void deberiaSerLaTasaMasBajaDeLosTreesPaises() {
            // Given
            BigDecimal monto = new BigDecimal("1000.00");
            CalculadorImpuestoMexico mexico = new CalculadorImpuestoMexico();
            CalculadorImpuestosCanada canada = new CalculadorImpuestosCanada();

            // When
            BigDecimal impuestoUsa = calculadorUsa.calcularImpuestoPais(monto);      // 5%
            BigDecimal impuestoCanada = canada.calcularImpuestoPais(monto);          // 15%
            BigDecimal impuestoMexico = mexico.calcularImpuestoPais(monto);          // 16%

            // Then - USA debe ser el más bajo
            assertThat(impuestoUsa).isLessThan(impuestoCanada);
            assertThat(impuestoUsa).isLessThan(impuestoMexico);
            assertThat(impuestoCanada).isLessThan(impuestoMexico);

            // Verificar valores exactos
            assertThat(impuestoUsa).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(impuestoCanada).isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(impuestoMexico).isEqualByComparingTo(new BigDecimal("160.00"));
        }
    }

    @Nested
    @DisplayName("Casos Reales de Cotización USA")
    class CasosRealesCotizacionUsaTest {

        @Test
        @DisplayName("Debería calcular Sales Tax para cotización de PC básica (USD)")
        void deberiaCalcularSalesTaxParaCotizacionDePcBasicaUsd() {
            // Given - PC básica USD $1,500
            BigDecimal montoPcBasica = new BigDecimal("1500.00");

            // When
            BigDecimal salesTax = calculadorUsa.calcularImpuestoPais(montoPcBasica);

            // Then
            BigDecimal salesTaxEsperado = new BigDecimal("75.00"); // 1500 × 0.05
            assertThat(salesTax).isEqualByComparingTo(salesTaxEsperado);

            // Total con Sales Tax debería ser USD $1,575
            BigDecimal totalConSalesTax = montoPcBasica.add(salesTax);
            assertThat(totalConSalesTax).isEqualByComparingTo(new BigDecimal("1575.00"));
        }

        @Test
        @DisplayName("Debería calcular Sales Tax para cotización de PC gamer (USD)")
        void deberiaCalcularSalesTaxParaCotizacionDePcGamerUsd() {
            // Given - PC gamer USD $3,500
            BigDecimal montoPcGamer = new BigDecimal("3500.00");

            // When
            BigDecimal salesTax = calculadorUsa.calcularImpuestoPais(montoPcGamer);

            // Then
            BigDecimal salesTaxEsperado = new BigDecimal("175.00"); // 3500 × 0.05
            assertThat(salesTax).isEqualByComparingTo(salesTaxEsperado);

            // Total con Sales Tax debería ser USD $3,675
            BigDecimal totalConSalesTax = montoPcGamer.add(salesTax);
            assertThat(totalConSalesTax).isEqualByComparingTo(new BigDecimal("3675.00"));
        }

        @Test
        @DisplayName("Debería calcular Sales Tax para componente individual (USD)")
        void deberiaCalcularSalesTaxParaComponenteIndividualUsd() {
            // Given - Graphics Card USD $750
            BigDecimal montoGpu = new BigDecimal("750.00");

            // When
            BigDecimal salesTax = calculadorUsa.calcularImpuestoPais(montoGpu);

            // Then
            BigDecimal salesTaxEsperado = new BigDecimal("37.50"); // 750 × 0.05
            assertThat(salesTax).isEqualByComparingTo(salesTaxEsperado);
        }

        @Test
        @DisplayName("Debería calcular Sales Tax para múltiples componentes (USD)")
        void deberiaCalcularSalesTaxParaMultiplesComponentesUsd() {
            // Given - Varios componentes en USD
            BigDecimal cpu = new BigDecimal("400.00");
            BigDecimal ram = new BigDecimal("150.00");
            BigDecimal gpu = new BigDecimal("600.00");
            BigDecimal subtotal = cpu.add(ram).add(gpu);

            // When
            BigDecimal salesTax = calculadorUsa.calcularImpuestoPais(subtotal);

            // Then
            BigDecimal salesTaxEsperado = new BigDecimal("57.50"); // 1150 × 0.05
            assertThat(salesTax).isEqualByComparingTo(salesTaxEsperado);
            assertThat(subtotal).isEqualByComparingTo(new BigDecimal("1150.00"));
        }

        @Test
        @DisplayName("Debería simular compra en estado sin Sales Tax")
        void deberiaSimularCompraEnEstadoSinSalesTax() {
            // Given - Estados como Delaware, Montana, New Hampshire, Oregon tienen 0% sales tax
            // Pero esta implementación simplificada usa 5%
            BigDecimal monto = new BigDecimal("2000.00");

            // When
            BigDecimal salesTax = calculadorUsa.calcularImpuestoPais(monto);

            // Then - En realidad sería 0%, pero esta implementación usa 5%
            BigDecimal salesTaxSimplificado = new BigDecimal("100.00"); // 2000 × 0.05
            assertThat(salesTax).isEqualByComparingTo(salesTaxSimplificado);

            // Nota: En implementación real se podría usar 0% para ciertos estados
            assertThat(salesTax).isGreaterThan(BigDecimal.ZERO); // Simplificación usa 5%
        }

        @Test
        @DisplayName("Debería calcular Sales Tax más bajo que otros países")
        void deberiaCalcularSalesTaxMasBajoQueOtrosPaises() {
            // Given - Misma cotización en tres países
            BigDecimal monto = new BigDecimal("2500.00");
            CalculadorImpuestoMexico mexico = new CalculadorImpuestoMexico();
            CalculadorImpuestosCanada canada = new CalculadorImpuestosCanada();

            // When
            BigDecimal impuestoUsa = calculadorUsa.calcularImpuestoPais(monto);      // 125.00
            BigDecimal impuestoCanada = canada.calcularImpuestoPais(monto);          // 375.00
            BigDecimal impuestoMexico = mexico.calcularImpuestoPais(monto);          // 400.00

            // Then - Cliente pagará menos impuestos en USA
            assertThat(impuestoUsa).isEqualByComparingTo(new BigDecimal("125.00"));
            assertThat(impuestoCanada).isEqualByComparingTo(new BigDecimal("375.00"));
            assertThat(impuestoMexico).isEqualByComparingTo(new BigDecimal("400.00"));

            // USA es más atractivo fiscalmente (con esta simplificación)
            BigDecimal ahorroVsCanada = impuestoCanada.subtract(impuestoUsa); // 250.00
            BigDecimal ahorroVsMexico = impuestoMexico.subtract(impuestoUsa);  // 275.00

            assertThat(ahorroVsCanada).isEqualByComparingTo(new BigDecimal("250.00"));
            assertThat(ahorroVsMexico).isEqualByComparingTo(new BigDecimal("275.00"));
        }
    }

    @Nested
    @DisplayName("Casos Límite y Excepciones")
    class CasosLimiteExcepcionesTest {

        @Test
        @DisplayName("Debería manejar valores extremadamente pequeños")
        void deberiaManejarValoresExtremadamentePequenos() {
            // Given
            BigDecimal montoMinimo = new BigDecimal("0.01"); // 1 centavo

            // When
            BigDecimal resultado = calculadorUsa.calcularImpuestoPais(montoMinimo);

            // Then
            BigDecimal salesTaxEsperado = new BigDecimal("0.0005"); // 0.01 × 0.05
            assertThat(resultado).isEqualByComparingTo(salesTaxEsperado);
        }

        @Test
        @DisplayName("Debería manejar valores extremadamente grandes")
        void deberiaManejarValoresExtremadamenteGrandes() {
            // Given
            BigDecimal montoMaximo = new BigDecimal("999999999.99");

            // When
            BigDecimal resultado = calculadorUsa.calcularImpuestoPais(montoMaximo);

            // Then
            BigDecimal salesTaxEsperado = new BigDecimal("49999999.9995"); // × 0.05
            assertThat(resultado).isEqualByComparingTo(salesTaxEsperado);
            assertThat(resultado).isPositive();
        }

        @Test
        @DisplayName("Debería lanzar excepción con monto null")
        void deberiaLanzarExcepcionConMontoNull() {
            // When & Then
            assertThatThrownBy(() -> calculadorUsa.calcularImpuestoPais(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Debería manejar diferentes escalas de BigDecimal")
        void deberiaManejarDiferentesEscalasDeBigDecimal() {
            // Given - Diferentes escalas
            BigDecimal monto1 = new BigDecimal("1000.0");   // Escala 1
            BigDecimal monto2 = new BigDecimal("1000.00");  // Escala 2
            BigDecimal monto3 = new BigDecimal("1000.000"); // Escala 3
            BigDecimal monto4 = new BigDecimal("1000");     // Escala 0

            // When
            BigDecimal resultado1 = calculadorUsa.calcularImpuestoPais(monto1);
            BigDecimal resultado2 = calculadorUsa.calcularImpuestoPais(monto2);
            BigDecimal resultado3 = calculadorUsa.calcularImpuestoPais(monto3);
            BigDecimal resultado4 = calculadorUsa.calcularImpuestoPais(monto4);

            // Then - Todos deberían dar el mismo resultado
            assertThat(resultado1).isEqualByComparingTo(resultado2);
            assertThat(resultado2).isEqualByComparingTo(resultado3);
            assertThat(resultado3).isEqualByComparingTo(resultado4);
            assertThat(resultado1).isEqualByComparingTo(new BigDecimal("50.00"));
        }

        @Test
        @DisplayName("Debería manejar montos con muchos decimales")
        void deberiaManejarMontosConMuchosDecimales() {
            // Given
            BigDecimal montoConMuchosDecimales = new BigDecimal("1234.5678901234");

            // When
            BigDecimal resultado = calculadorUsa.calcularImpuestoPais(montoConMuchosDecimales);

            // Then - Calculamos el esperado de forma precisa
            BigDecimal salesTaxEsperado = montoConMuchosDecimales.multiply(BigDecimal.valueOf(0.05));
            assertThat(resultado).isEqualByComparingTo(salesTaxEsperado);

            // Verificar que es aproximadamente 61.73
            assertThat(resultado).isGreaterThan(new BigDecimal("61.70"));
            assertThat(resultado).isLessThan(new BigDecimal("61.75"));
        }
    }

    @Nested
    @DisplayName("Compatibilidad con Patrón Bridge")
    class CompatibilidadPatronBridgeTest {

        @Test
        @DisplayName("Debería ser compatible con CalculadorImpuestoFederal")
        void deberiaSerCompatibleConCalculadorImpuestoFederal() {
            // Given
            CalculadorImpuestoFederal calculadorFederal =
                new CalculadorImpuestoFederal(calculadorUsa);
            BigDecimal monto = new BigDecimal("1000.00");

            // When
            BigDecimal impuestoFederal = calculadorFederal.calcularImpuesto(monto);

            // Then - Debería incluir Sales Tax USA (50) + Federal (50) = 100
            BigDecimal salesTaxUsa = calculadorUsa.calcularImpuestoPais(monto);      // 50
            BigDecimal federal5Porciento = monto.multiply(new BigDecimal("0.05"));   // 50
            BigDecimal totalEsperado = salesTaxUsa.add(federal5Porciento);           // 100

            assertThat(impuestoFederal).isEqualByComparingTo(totalEsperado);
            assertThat(impuestoFederal).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("Debería ser compatible con CalculadorImpuestoLocal")
        void deberiaSerCompatibleConCalculadorImpuestoLocal() {
            // Given
            CalculadorImpuestoLocal calculadorLocal =
                new CalculadorImpuestoLocal(calculadorUsa);
            BigDecimal monto = new BigDecimal("1000.00");

            // When
            BigDecimal impuestoLocal = calculadorLocal.calcularImpuesto(monto);

            // Then - Debería incluir Sales Tax USA (50) + Local (30) = 80
            BigDecimal salesTaxUsa = calculadorUsa.calcularImpuestoPais(monto);      // 50
            BigDecimal local3Porciento = monto.multiply(new BigDecimal("0.03"));     // 30
            BigDecimal totalEsperado = salesTaxUsa.add(local3Porciento);             // 80

            assertThat(impuestoLocal).isEqualByComparingTo(totalEsperado);
            assertThat(impuestoLocal).isEqualByComparingTo(new BigDecimal("80.00"));
        }

        @Test
        @DisplayName("Debería demostrar flexibilidad del patrón Bridge")
        void deberiaDisemostrarFlexibilidadDelPatronBridge() {
            // Given - Misma abstracción con diferentes países
            CalculadorImpuestoMexico mexico = new CalculadorImpuestoMexico();
            CalculadorImpuestosCanada canada = new CalculadorImpuestosCanada();

            CalculadorImpuestoFederal federalUsa = new CalculadorImpuestoFederal(calculadorUsa);
            CalculadorImpuestoFederal federalCanada = new CalculadorImpuestoFederal(canada);
            CalculadorImpuestoFederal federalMexico = new CalculadorImpuestoFederal(mexico);

            BigDecimal monto = new BigDecimal("1000.00");

            // When
            BigDecimal impuestoUsa = federalUsa.calcularImpuesto(monto);      // 50 + 50 = 100
            BigDecimal impuestoCanada = federalCanada.calcularImpuesto(monto); // 150 + 50 = 200
            BigDecimal impuestoMexico = federalMexico.calcularImpuesto(monto); // 160 + 50 = 210

            // Then - Todos usan la misma abstracción federal, diferentes implementaciones país
            assertThat(impuestoUsa).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(impuestoCanada).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(impuestoMexico).isEqualByComparingTo(new BigDecimal("210.00"));

            // USA sigue siendo el más económico
            assertThat(impuestoUsa).isLessThan(impuestoCanada);
            assertThat(impuestoUsa).isLessThan(impuestoMexico);
        }
    }
}