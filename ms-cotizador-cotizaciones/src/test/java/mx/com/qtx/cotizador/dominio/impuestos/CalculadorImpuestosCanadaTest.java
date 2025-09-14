package mx.com.qtx.cotizador.dominio.impuestos;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.*;

/**
 * Pruebas unitarias para la clase {@link CalculadorImpuestosCanada}.
 * <p>
 * Esta suite de pruebas verifica el correcto funcionamiento del calculador de impuestos
 * para Canadá, incluyendo el cálculo preciso del GST (15%), manejo de diferentes
 * montos y casos límite, así como la precisión en los cálculos BigDecimal.
 * </p>
 *
 * <h3>Cobertura de pruebas:</h3>
 * <ul>
 *   <li><strong>Constructor:</strong> Creación correcta de instancias</li>
 *   <li><strong>Cálculo GST:</strong> Fórmula correcta (monto × 0.15)</li>
 *   <li><strong>Precisión decimal:</strong> Manejo correcto de BigDecimal</li>
 *   <li><strong>Casos límite:</strong> Montos cero, pequeños, grandes y negativos</li>
 *   <li><strong>Casos reales:</strong> Escenarios típicos de cotización canadiense</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("CalculadorImpuestosCanada - Pruebas Unitarias")
class CalculadorImpuestosCanadaTest {

    private CalculadorImpuestosCanada calculadorCanada;

    /**
     * Configuración inicial para cada prueba.
     */
    @BeforeEach
    void setUp() {
        calculadorCanada = new CalculadorImpuestosCanada();
    }

    @Nested
    @DisplayName("Constructor e Inicialización")
    class ConstructorInicializacionTest {

        @Test
        @DisplayName("Debería crear instancia correctamente")
        void deberiaCrearInstanciaCorrectamente() {
            // When
            CalculadorImpuestosCanada resultado = new CalculadorImpuestosCanada();

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado).isInstanceOf(ICalculadorImpuestoPais.class);
        }

        @Test
        @DisplayName("Debería implementar interfaz ICalculadorImpuestoPais")
        void deberiaImplementarInterfazICalculadorImpuestoPais() {
            // Then
            assertThat(calculadorCanada).isInstanceOf(ICalculadorImpuestoPais.class);
        }
    }

    @Nested
    @DisplayName("Cálculo de GST Canadá")
    class CalculoGstCanadaTest {

        @Test
        @DisplayName("Debería calcular GST 15% con monto típico")
        void deberiaCalcularGst15PorcentoConMontoTipico() {
            // Given
            BigDecimal monto = new BigDecimal("1000.00");

            // When
            BigDecimal resultado = calculadorCanada.calcularImpuestoPais(monto);

            // Then
            BigDecimal gstEsperado = new BigDecimal("150.00"); // 1000 × 0.15
            assertThat(resultado).isEqualByComparingTo(gstEsperado);
        }

        @Test
        @DisplayName("Debería calcular GST correctamente con monto cero")
        void deberiaCalcularGstCorrectamenteConMontoCero() {
            // Given
            BigDecimal monto = BigDecimal.ZERO;

            // When
            BigDecimal resultado = calculadorCanada.calcularImpuestoPais(monto);

            // Then
            assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Debería calcular GST con monto pequeño")
        void deberiaCalcularGstConMontoPequeno() {
            // Given
            BigDecimal monto = new BigDecimal("10.50");

            // When
            BigDecimal resultado = calculadorCanada.calcularImpuestoPais(monto);

            // Then
            BigDecimal gstEsperado = new BigDecimal("1.575"); // 10.50 × 0.15
            assertThat(resultado).isEqualByComparingTo(gstEsperado);
        }

        @Test
        @DisplayName("Debería calcular GST con monto grande")
        void deberiaCalcularGstConMontoGrande() {
            // Given
            BigDecimal monto = new BigDecimal("50000.00");

            // When
            BigDecimal resultado = calculadorCanada.calcularImpuestoPais(monto);

            // Then
            BigDecimal gstEsperado = new BigDecimal("7500.00"); // 50000 × 0.15
            assertThat(resultado).isEqualByComparingTo(gstEsperado);
        }

        @Test
        @DisplayName("Debería mantener precisión decimal en cálculos")
        void deberiaMantenerPrecisionDecimalEnCalculos() {
            // Given
            BigDecimal monto = new BigDecimal("999.99");

            // When
            BigDecimal resultado = calculadorCanada.calcularImpuestoPais(monto);

            // Then
            BigDecimal gstEsperado = new BigDecimal("149.9985"); // 999.99 × 0.15
            assertThat(resultado).isEqualByComparingTo(gstEsperado);
        }

        @Test
        @DisplayName("Debería diferenciarse del IVA mexicano (15% vs 16%)")
        void deberiaDiferenciarseDelIvaMexicano15PorcentoVs16Porciento() {
            // Given
            BigDecimal monto = new BigDecimal("1000.00");

            // When
            BigDecimal gstCanada = calculadorCanada.calcularImpuestoPais(monto);

            // Then - GST Canadá debe ser 15% (150) vs IVA México sería 16% (160)
            assertThat(gstCanada).isEqualByComparingTo(new BigDecimal("150.00"));
            // Verificar que NO es el IVA mexicano (160)
            assertThat(gstCanada).isNotEqualByComparingTo(new BigDecimal("160.00"));
        }
    }

    @Nested
    @DisplayName("Fórmula y Validaciones")
    class FormulaValidacionesTest {

        @Test
        @DisplayName("Debería aplicar fórmula correcta: monto × 0.15")
        void deberiaAplicarFormulaCorrectaMontoMultiplicadoPor015() {
            // Given - Diferentes montos para validar la fórmula
            BigDecimal[] montos = {
                new BigDecimal("100.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("2500.00")
            };

            BigDecimal[] resultadosEsperados = {
                new BigDecimal("15.00"),   // 100 × 0.15
                new BigDecimal("75.00"),   // 500 × 0.15
                new BigDecimal("150.00"),  // 1000 × 0.15
                new BigDecimal("375.00")   // 2500 × 0.15
            };

            // When & Then
            for (int i = 0; i < montos.length; i++) {
                BigDecimal resultado = calculadorCanada.calcularImpuestoPais(montos[i]);
                assertThat(resultado).isEqualByComparingTo(resultadosEsperados[i]);
            }
        }

        @Test
        @DisplayName("Debería mantener tasa constante de 15%")
        void deberiaMantenerTasaConstanteDe15Porciento() {
            // Given - Diferentes montos
            BigDecimal[] montos = {
                new BigDecimal("1.00"),
                new BigDecimal("10.00"),
                new BigDecimal("100.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("10000.00")
            };

            // When & Then - Todos deberían tener exactamente 15% de GST
            for (BigDecimal monto : montos) {
                BigDecimal resultado = calculadorCanada.calcularImpuestoPais(monto);
                BigDecimal gstCalculado = monto.multiply(BigDecimal.valueOf(0.15));

                assertThat(resultado).isEqualByComparingTo(gstCalculado);

                // Verificar que el porcentaje es exactamente 15%
                if (monto.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal porcentaje = resultado.divide(monto, 4, BigDecimal.ROUND_HALF_UP);
                    assertThat(porcentaje).isEqualByComparingTo(new BigDecimal("0.1500"));
                }
            }
        }

        @Test
        @DisplayName("Debería manejar montos negativos correctamente")
        void deberiaManejarMontosNegativosCorrectamente() {
            // Given
            BigDecimal montoNegativo = new BigDecimal("-1000.00");

            // When
            BigDecimal resultado = calculadorCanada.calcularImpuestoPais(montoNegativo);

            // Then - El GST de un monto negativo es negativo
            BigDecimal gstEsperado = new BigDecimal("-150.00"); // -1000 × 0.15
            assertThat(resultado).isEqualByComparingTo(gstEsperado);
            assertThat(resultado).isNegative();
        }
    }

    @Nested
    @DisplayName("Casos Reales de Cotización Canadá")
    class CasosRealesCotizacionCanadaTest {

        @Test
        @DisplayName("Debería calcular GST para cotización de PC básica (CAD)")
        void deberiaCalcularGstParaCotizacionDePcBasicaCad() {
            // Given - PC básica CAD $1,800
            BigDecimal montoPcBasica = new BigDecimal("1800.00");

            // When
            BigDecimal gst = calculadorCanada.calcularImpuestoPais(montoPcBasica);

            // Then
            BigDecimal gstEsperado = new BigDecimal("270.00"); // 1800 × 0.15
            assertThat(gst).isEqualByComparingTo(gstEsperado);

            // Total con GST debería ser CAD $2,070
            BigDecimal totalConGst = montoPcBasica.add(gst);
            assertThat(totalConGst).isEqualByComparingTo(new BigDecimal("2070.00"));
        }

        @Test
        @DisplayName("Debería calcular GST para cotización de PC gamer (CAD)")
        void deberiaCalcularGstParaCotizacionDePcGamerCad() {
            // Given - PC gamer CAD $4,500
            BigDecimal montoPcGamer = new BigDecimal("4500.00");

            // When
            BigDecimal gst = calculadorCanada.calcularImpuestoPais(montoPcGamer);

            // Then
            BigDecimal gstEsperado = new BigDecimal("675.00"); // 4500 × 0.15
            assertThat(gst).isEqualByComparingTo(gstEsperado);

            // Total con GST debería ser CAD $5,175
            BigDecimal totalConGst = montoPcGamer.add(gst);
            assertThat(totalConGst).isEqualByComparingTo(new BigDecimal("5175.00"));
        }

        @Test
        @DisplayName("Debería calcular GST para componente individual (CAD)")
        void deberiaCalcularGstParaComponenteIndividualCad() {
            // Given - Monitor CAD $350
            BigDecimal montoMonitor = new BigDecimal("350.00");

            // When
            BigDecimal gst = calculadorCanada.calcularImpuestoPais(montoMonitor);

            // Then
            BigDecimal gstEsperado = new BigDecimal("52.50"); // 350 × 0.15
            assertThat(gst).isEqualByComparingTo(gstEsperado);
        }

        @Test
        @DisplayName("Debería calcular GST para múltiples componentes (CAD)")
        void deberiaCalcularGstParaMultiplesComponentesCad() {
            // Given - Varios componentes en CAD
            BigDecimal procesador = new BigDecimal("600.00");
            BigDecimal memoria = new BigDecimal("200.00");
            BigDecimal tarjetaGrafica = new BigDecimal("900.00");
            BigDecimal subtotal = procesador.add(memoria).add(tarjetaGrafica);

            // When
            BigDecimal gst = calculadorCanada.calcularImpuestoPais(subtotal);

            // Then
            BigDecimal gstEsperado = new BigDecimal("255.00"); // 1700 × 0.15
            assertThat(gst).isEqualByComparingTo(gstEsperado);
            assertThat(subtotal).isEqualByComparingTo(new BigDecimal("1700.00"));
        }

        @Test
        @DisplayName("Debería simular sistema fiscal canadiense simplificado")
        void deberiaSimularSistemaFiscalCanadienseSimplificado() {
            // Given - Compra típica en Canadá
            BigDecimal monto = new BigDecimal("2000.00");

            // When
            BigDecimal gst = calculadorCanada.calcularImpuestoPais(monto);

            // Then - Nota: En realidad sería GST 5% + PST provincial variable
            // Pero esta implementación usa 15% simplificado
            BigDecimal gstEsperado = new BigDecimal("300.00"); // 2000 × 0.15
            assertThat(gst).isEqualByComparingTo(gstEsperado);

            // Total con impuestos
            BigDecimal totalConImpuestos = monto.add(gst);
            assertThat(totalConImpuestos).isEqualByComparingTo(new BigDecimal("2300.00"));
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
            BigDecimal resultado = calculadorCanada.calcularImpuestoPais(montoMinimo);

            // Then
            BigDecimal gstEsperado = new BigDecimal("0.0015"); // 0.01 × 0.15
            assertThat(resultado).isEqualByComparingTo(gstEsperado);
        }

        @Test
        @DisplayName("Debería manejar valores extremadamente grandes")
        void deberiaManejarValoresExtremadamenteGrandes() {
            // Given
            BigDecimal montoMaximo = new BigDecimal("999999999.99");

            // When
            BigDecimal resultado = calculadorCanada.calcularImpuestoPais(montoMaximo);

            // Then
            BigDecimal gstEsperado = new BigDecimal("149999999.9985"); // × 0.15
            assertThat(resultado).isEqualByComparingTo(gstEsperado);
            assertThat(resultado).isPositive();
        }

        @Test
        @DisplayName("Debería lanzar excepción con monto null")
        void deberiaLanzarExcepcionConMontoNull() {
            // When & Then
            assertThatThrownBy(() -> calculadorCanada.calcularImpuestoPais(null))
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
            BigDecimal resultado1 = calculadorCanada.calcularImpuestoPais(monto1);
            BigDecimal resultado2 = calculadorCanada.calcularImpuestoPais(monto2);
            BigDecimal resultado3 = calculadorCanada.calcularImpuestoPais(monto3);
            BigDecimal resultado4 = calculadorCanada.calcularImpuestoPais(monto4);

            // Then - Todos deberían dar el mismo resultado
            assertThat(resultado1).isEqualByComparingTo(resultado2);
            assertThat(resultado2).isEqualByComparingTo(resultado3);
            assertThat(resultado3).isEqualByComparingTo(resultado4);
            assertThat(resultado1).isEqualByComparingTo(new BigDecimal("150.00"));
        }

        @Test
        @DisplayName("Debería manejar montos con muchos decimales")
        void deberiaManejarMontosConMuchosDecimales() {
            // Given
            BigDecimal montoConMuchosDecimales = new BigDecimal("1234.5678901234");

            // When
            BigDecimal resultado = calculadorCanada.calcularImpuestoPais(montoConMuchosDecimales);

            // Then - Calculamos el esperado de forma precisa
            BigDecimal gstEsperado = montoConMuchosDecimales.multiply(BigDecimal.valueOf(0.15));
            assertThat(resultado).isEqualByComparingTo(gstEsperado);

            // Verificar que es aproximadamente 185.19
            assertThat(resultado).isGreaterThan(new BigDecimal("185.15"));
            assertThat(resultado).isLessThan(new BigDecimal("185.25"));
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
                new CalculadorImpuestoFederal(calculadorCanada);
            BigDecimal monto = new BigDecimal("1000.00");

            // When
            BigDecimal impuestoFederal = calculadorFederal.calcularImpuesto(monto);

            // Then - Debería incluir GST Canadá (150) + Federal (50) = 200
            BigDecimal gstCanada = calculadorCanada.calcularImpuestoPais(monto); // 150
            BigDecimal federal5Porciento = monto.multiply(new BigDecimal("0.05")); // 50
            BigDecimal totalEsperado = gstCanada.add(federal5Porciento); // 200

            assertThat(impuestoFederal).isEqualByComparingTo(totalEsperado);
            assertThat(impuestoFederal).isEqualByComparingTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("Debería ser compatible con CalculadorImpuestoLocal")
        void deberiaSerCompatibleConCalculadorImpuestoLocal() {
            // Given
            CalculadorImpuestoLocal calculadorLocal =
                new CalculadorImpuestoLocal(calculadorCanada);
            BigDecimal monto = new BigDecimal("1000.00");

            // When
            BigDecimal impuestoLocal = calculadorLocal.calcularImpuesto(monto);

            // Then - Debería incluir GST Canadá (150) + Local (30) = 180
            BigDecimal gstCanada = calculadorCanada.calcularImpuestoPais(monto); // 150
            BigDecimal local3Porciento = monto.multiply(new BigDecimal("0.03")); // 30
            BigDecimal totalEsperado = gstCanada.add(local3Porciento); // 180

            assertThat(impuestoLocal).isEqualByComparingTo(totalEsperado);
            assertThat(impuestoLocal).isEqualByComparingTo(new BigDecimal("180.00"));
        }

        @Test
        @DisplayName("Debería diferenciarse de otros países en Bridge pattern")
        void deberiaDiferenciarseDeOtrosPaisesEnBridgePattern() {
            // Given - Comparar con México usando misma abstracción
            CalculadorImpuestoMexico mexico = new CalculadorImpuestoMexico();
            CalculadorImpuestoFederal federalCanada = new CalculadorImpuestoFederal(calculadorCanada);
            CalculadorImpuestoFederal federalMexico = new CalculadorImpuestoFederal(mexico);

            BigDecimal monto = new BigDecimal("1000.00");

            // When
            BigDecimal impuestoCanada = federalCanada.calcularImpuesto(monto); // 150 + 50 = 200
            BigDecimal impuestoMexico = federalMexico.calcularImpuesto(monto);  // 160 + 50 = 210

            // Then - Los resultados deberían ser diferentes
            assertThat(impuestoCanada).isNotEqualByComparingTo(impuestoMexico);
            assertThat(impuestoCanada).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(impuestoMexico).isEqualByComparingTo(new BigDecimal("210.00"));
        }
    }
}