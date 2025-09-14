package mx.com.qtx.cotizador.dominio.impuestos;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.*;

/**
 * Pruebas unitarias para la clase {@link CalculadorImpuestoMexico}.
 * <p>
 * Esta suite de pruebas verifica el correcto funcionamiento del calculador de impuestos
 * para México, incluyendo el cálculo preciso del IVA (16%), manejo de diferentes
 * montos y casos límite, así como la precisión en los cálculos BigDecimal.
 * </p>
 *
 * <h3>Cobertura de pruebas:</h3>
 * <ul>
 *   <li><strong>Constructor:</strong> Creación correcta de instancias</li>
 *   <li><strong>Cálculo IVA:</strong> Fórmula correcta (monto × 0.16)</li>
 *   <li><strong>Precisión decimal:</strong> Manejo correcto de BigDecimal</li>
 *   <li><strong>Casos límite:</strong> Montos cero, pequeños, grandes y negativos</li>
 *   <li><strong>Casos reales:</strong> Escenarios típicos de cotización</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("CalculadorImpuestoMexico - Pruebas Unitarias")
class CalculadorImpuestoMexicoTest {

    private CalculadorImpuestoMexico calculadorMexico;

    /**
     * Configuración inicial para cada prueba.
     */
    @BeforeEach
    void setUp() {
        calculadorMexico = new CalculadorImpuestoMexico();
    }

    @Nested
    @DisplayName("Constructor e Inicialización")
    class ConstructorInicializacionTest {

        @Test
        @DisplayName("Debería crear instancia correctamente")
        void deberiaCrearInstanciaCorrectamente() {
            // When
            CalculadorImpuestoMexico resultado = new CalculadorImpuestoMexico();

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado).isInstanceOf(ICalculadorImpuestoPais.class);
        }

        @Test
        @DisplayName("Debería implementar interfaz ICalculadorImpuestoPais")
        void deberiaImplementarInterfazICalculadorImpuestoPais() {
            // Then
            assertThat(calculadorMexico).isInstanceOf(ICalculadorImpuestoPais.class);
        }
    }

    @Nested
    @DisplayName("Cálculo de IVA México")
    class CalculoIvaMexicoTest {

        @Test
        @DisplayName("Debería calcular IVA 16% con monto típico")
        void deberiaCalcularIva16PorcentoConMontoTipico() {
            // Given
            BigDecimal monto = new BigDecimal("1000.00");

            // When
            BigDecimal resultado = calculadorMexico.calcularImpuestoPais(monto);

            // Then
            BigDecimal ivaEsperado = new BigDecimal("160.00"); // 1000 × 0.16
            assertThat(resultado).isEqualByComparingTo(ivaEsperado);
        }

        @Test
        @DisplayName("Debería calcular IVA correctamente con monto cero")
        void deberiaCalcularIvaCorrectamenteConMontoCero() {
            // Given
            BigDecimal monto = BigDecimal.ZERO;

            // When
            BigDecimal resultado = calculadorMexico.calcularImpuestoPais(monto);

            // Then
            assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Debería calcular IVA con monto pequeño")
        void deberiaCalcularIvaConMontoPequeno() {
            // Given
            BigDecimal monto = new BigDecimal("10.50");

            // When
            BigDecimal resultado = calculadorMexico.calcularImpuestoPais(monto);

            // Then
            BigDecimal ivaEsperado = new BigDecimal("1.68"); // 10.50 × 0.16
            assertThat(resultado).isEqualByComparingTo(ivaEsperado);
        }

        @Test
        @DisplayName("Debería calcular IVA con monto grande")
        void deberiaCalcularIvaConMontoGrande() {
            // Given
            BigDecimal monto = new BigDecimal("50000.00");

            // When
            BigDecimal resultado = calculadorMexico.calcularImpuestoPais(monto);

            // Then
            BigDecimal ivaEsperado = new BigDecimal("8000.00"); // 50000 × 0.16
            assertThat(resultado).isEqualByComparingTo(ivaEsperado);
        }

        @Test
        @DisplayName("Debería mantener precisión decimal en cálculos")
        void deberiaMantenerPrecisionDecimalEnCalculos() {
            // Given
            BigDecimal monto = new BigDecimal("999.99");

            // When
            BigDecimal resultado = calculadorMexico.calcularImpuestoPais(monto);

            // Then
            BigDecimal ivaEsperado = new BigDecimal("159.9984"); // 999.99 × 0.16
            assertThat(resultado).isEqualByComparingTo(ivaEsperado);
        }
    }

    @Nested
    @DisplayName("Fórmula y Validaciones")
    class FormulaValidacionesTest {

        @Test
        @DisplayName("Debería aplicar fórmula correcta: monto × 0.16")
        void deberiaAplicarFormulaCorrectaMontoMulitplicadoPor016() {
            // Given - Diferentes montos para validar la fórmula
            BigDecimal[] montos = {
                new BigDecimal("100.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("2500.00")
            };

            BigDecimal[] resultadosEsperados = {
                new BigDecimal("16.00"),   // 100 × 0.16
                new BigDecimal("80.00"),   // 500 × 0.16
                new BigDecimal("160.00"),  // 1000 × 0.16
                new BigDecimal("400.00")   // 2500 × 0.16
            };

            // When & Then
            for (int i = 0; i < montos.length; i++) {
                BigDecimal resultado = calculadorMexico.calcularImpuestoPais(montos[i]);
                assertThat(resultado).isEqualByComparingTo(resultadosEsperados[i]);
            }
        }

        @Test
        @DisplayName("Debería mantener tasa constante de 16%")
        void deberiaMantenerTasaConstanteDe16Porciento() {
            // Given - Diferentes montos
            BigDecimal[] montos = {
                new BigDecimal("1.00"),
                new BigDecimal("10.00"),
                new BigDecimal("100.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("10000.00")
            };

            // When & Then - Todos deberían tener exactamente 16% de IVA
            for (BigDecimal monto : montos) {
                BigDecimal resultado = calculadorMexico.calcularImpuestoPais(monto);
                BigDecimal ivaCalculado = monto.multiply(BigDecimal.valueOf(0.16));

                assertThat(resultado).isEqualByComparingTo(ivaCalculado);

                // Verificar que el porcentaje es exactamente 16%
                if (monto.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal porcentaje = resultado.divide(monto, 4, BigDecimal.ROUND_HALF_UP);
                    assertThat(porcentaje).isEqualByComparingTo(new BigDecimal("0.1600"));
                }
            }
        }

        @Test
        @DisplayName("Debería manejar montos negativos correctamente")
        void deberiaManejarMontosNegativosCorrectamente() {
            // Given
            BigDecimal montoNegativo = new BigDecimal("-1000.00");

            // When
            BigDecimal resultado = calculadorMexico.calcularImpuestoPais(montoNegativo);

            // Then - El IVA de un monto negativo es negativo
            BigDecimal ivaEsperado = new BigDecimal("-160.00"); // -1000 × 0.16
            assertThat(resultado).isEqualByComparingTo(ivaEsperado);
            assertThat(resultado).isNegative();
        }
    }

    @Nested
    @DisplayName("Casos Reales de Cotización")
    class CasosRealesCotizacionTest {

        @Test
        @DisplayName("Debería calcular IVA para cotización de PC básica")
        void deberiaCalcularIvaParaCotizacionDePcBasica() {
            // Given - PC básica $15,000
            BigDecimal montoPcBasica = new BigDecimal("15000.00");

            // When
            BigDecimal iva = calculadorMexico.calcularImpuestoPais(montoPcBasica);

            // Then
            BigDecimal ivaEsperado = new BigDecimal("2400.00"); // 15000 × 0.16
            assertThat(iva).isEqualByComparingTo(ivaEsperado);

            // Total con IVA debería ser 17,400
            BigDecimal totalConIva = montoPcBasica.add(iva);
            assertThat(totalConIva).isEqualByComparingTo(new BigDecimal("17400.00"));
        }

        @Test
        @DisplayName("Debería calcular IVA para cotización de PC gamer")
        void deberiaCalcularIvaParaCotizacionDePcGamer() {
            // Given - PC gamer $45,000
            BigDecimal montoPcGamer = new BigDecimal("45000.00");

            // When
            BigDecimal iva = calculadorMexico.calcularImpuestoPais(montoPcGamer);

            // Then
            BigDecimal ivaEsperado = new BigDecimal("7200.00"); // 45000 × 0.16
            assertThat(iva).isEqualByComparingTo(ivaEsperado);

            // Total con IVA debería ser 52,200
            BigDecimal totalConIva = montoPcGamer.add(iva);
            assertThat(totalConIva).isEqualByComparingTo(new BigDecimal("52200.00"));
        }

        @Test
        @DisplayName("Debería calcular IVA para componente individual")
        void deberiaCalcularIvaParaComponenteIndividual() {
            // Given - Monitor $3,500
            BigDecimal montoMonitor = new BigDecimal("3500.00");

            // When
            BigDecimal iva = calculadorMexico.calcularImpuestoPais(montoMonitor);

            // Then
            BigDecimal ivaEsperado = new BigDecimal("560.00"); // 3500 × 0.16
            assertThat(iva).isEqualByComparingTo(ivaEsperado);
        }

        @Test
        @DisplayName("Debería calcular IVA para múltiples componentes")
        void deberiaCalcularIvaParaMultiplesComponentes() {
            // Given - Varios componentes
            BigDecimal procesador = new BigDecimal("5000.00");
            BigDecimal memoria = new BigDecimal("2000.00");
            BigDecimal tarjetaGrafica = new BigDecimal("8000.00");
            BigDecimal subtotal = procesador.add(memoria).add(tarjetaGrafica);

            // When
            BigDecimal iva = calculadorMexico.calcularImpuestoPais(subtotal);

            // Then
            BigDecimal ivaEsperado = new BigDecimal("2400.00"); // 15000 × 0.16
            assertThat(iva).isEqualByComparingTo(ivaEsperado);
            assertThat(subtotal).isEqualByComparingTo(new BigDecimal("15000.00"));
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
            BigDecimal resultado = calculadorMexico.calcularImpuestoPais(montoMinimo);

            // Then
            BigDecimal ivaEsperado = new BigDecimal("0.0016"); // 0.01 × 0.16
            assertThat(resultado).isEqualByComparingTo(ivaEsperado);
        }

        @Test
        @DisplayName("Debería manejar valores extremadamente grandes")
        void deberiaManejarValoresExtremadamenteGrandes() {
            // Given
            BigDecimal montoMaximo = new BigDecimal("999999999.99");

            // When
            BigDecimal resultado = calculadorMexico.calcularImpuestoPais(montoMaximo);

            // Then
            BigDecimal ivaEsperado = new BigDecimal("159999999.9984"); // × 0.16
            assertThat(resultado).isEqualByComparingTo(ivaEsperado);
            assertThat(resultado).isPositive();
        }

        @Test
        @DisplayName("Debería lanzar excepción con monto null")
        void deberiaLanzarExcepcionConMontoNull() {
            // When & Then
            assertThatThrownBy(() -> calculadorMexico.calcularImpuestoPais(null))
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
            BigDecimal resultado1 = calculadorMexico.calcularImpuestoPais(monto1);
            BigDecimal resultado2 = calculadorMexico.calcularImpuestoPais(monto2);
            BigDecimal resultado3 = calculadorMexico.calcularImpuestoPais(monto3);
            BigDecimal resultado4 = calculadorMexico.calcularImpuestoPais(monto4);

            // Then - Todos deberían dar el mismo resultado
            assertThat(resultado1).isEqualByComparingTo(resultado2);
            assertThat(resultado2).isEqualByComparingTo(resultado3);
            assertThat(resultado3).isEqualByComparingTo(resultado4);
            assertThat(resultado1).isEqualByComparingTo(new BigDecimal("160.00"));
        }

        @Test
        @DisplayName("Debería manejar montos con muchos decimales")
        void deberiaManejarMontosConMuchosDecimales() {
            // Given
            BigDecimal montoConMuchosDecimales = new BigDecimal("1234.5678901234");

            // When
            BigDecimal resultado = calculadorMexico.calcularImpuestoPais(montoConMuchosDecimales);

            // Then - Calculamos el esperado de forma precisa
            BigDecimal ivaEsperado = montoConMuchosDecimales.multiply(BigDecimal.valueOf(0.16));
            assertThat(resultado).isEqualByComparingTo(ivaEsperado);

            // Verificar que es aproximadamente 197.53
            assertThat(resultado).isGreaterThan(new BigDecimal("197.50"));
            assertThat(resultado).isLessThan(new BigDecimal("197.55"));
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
                new CalculadorImpuestoFederal(calculadorMexico);
            BigDecimal monto = new BigDecimal("1000.00");

            // When
            BigDecimal impuestoFederal = calculadorFederal.calcularImpuesto(monto);

            // Then - Debería incluir IVA México (160) + Federal (50) = 210
            BigDecimal ivaMexico = calculadorMexico.calcularImpuestoPais(monto); // 160
            BigDecimal federal5Porciento = monto.multiply(new BigDecimal("0.05")); // 50
            BigDecimal totalEsperado = ivaMexico.add(federal5Porciento); // 210

            assertThat(impuestoFederal).isEqualByComparingTo(totalEsperado);
            assertThat(impuestoFederal).isEqualByComparingTo(new BigDecimal("210.00"));
        }

        @Test
        @DisplayName("Debería ser compatible con CalculadorImpuestoLocal")
        void deberiaSerCompatibleConCalculadorImpuestoLocal() {
            // Given
            CalculadorImpuestoLocal calculadorLocal =
                new CalculadorImpuestoLocal(calculadorMexico);
            BigDecimal monto = new BigDecimal("1000.00");

            // When
            BigDecimal impuestoLocal = calculadorLocal.calcularImpuesto(monto);

            // Then - Debería incluir IVA México (160) + Local (30) = 190
            BigDecimal ivaMexico = calculadorMexico.calcularImpuestoPais(monto); // 160
            BigDecimal local3Porciento = monto.multiply(new BigDecimal("0.03")); // 30
            BigDecimal totalEsperado = ivaMexico.add(local3Porciento); // 190

            assertThat(impuestoLocal).isEqualByComparingTo(totalEsperado);
            assertThat(impuestoLocal).isEqualByComparingTo(new BigDecimal("190.00"));
        }
    }
}