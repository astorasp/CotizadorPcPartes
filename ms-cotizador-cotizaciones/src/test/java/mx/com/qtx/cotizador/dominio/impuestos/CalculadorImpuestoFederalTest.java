package mx.com.qtx.cotizador.dominio.impuestos;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Pruebas unitarias para la clase {@link CalculadorImpuestoFederal}.
 * <p>
 * Esta suite de pruebas verifica el correcto funcionamiento del calculador de impuestos federales,
 * incluyendo la validación del patrón Bridge, la precisión en los cálculos BigDecimal y la
 * integración con diferentes implementaciones de calculadores por país.
 * </p>
 *
 * <h3>Cobertura de pruebas:</h3>
 * <ul>
 *   <li><strong>Constructor:</strong> Validación de parámetros y inicialización correcta</li>
 *   <li><strong>Cálculo de impuestos:</strong> Fórmula correcta (país + 5% federal)</li>
 *   <li><strong>Precisión decimal:</strong> Manejo correcto de BigDecimal</li>
 *   <li><strong>Casos límite:</strong> Montos cero, grandes y diversos escenarios</li>
 *   <li><strong>Integración:</strong> Funcionamiento con diferentes países mock</li>
 * </ul>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CalculadorImpuestoFederal - Pruebas Unitarias")
class CalculadorImpuestoFederalTest {

    @Mock
    private ICalculadorImpuestoPais calculadorPaisMock;

    private CalculadorImpuestoFederal calculadorFederal;

    /**
     * Configuración inicial para cada prueba.
     */
    @BeforeEach
    void setUp() {
        calculadorFederal = new CalculadorImpuestoFederal(calculadorPaisMock);
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {

        @Test
        @DisplayName("Debería crear instancia con calculador país válido")
        void deberiaCrearInstanciaConCalculadorPaisValido() {
            // Given
            ICalculadorImpuestoPais calculadorPais = mock(ICalculadorImpuestoPais.class);

            // When
            CalculadorImpuestoFederal resultado = new CalculadorImpuestoFederal(calculadorPais);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.calculoImpuestoPais).isSameAs(calculadorPais);
        }

        @Test
        @DisplayName("Debería aceptar calculador país null (sin validación)")
        void deberiaAceptarCalculadorPaisNull() {
            // When
            CalculadorImpuestoFederal resultado = new CalculadorImpuestoFederal(null);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.calculoImpuestoPais).isNull();
        }
    }

    @Nested
    @DisplayName("Cálculo de Impuestos")
    class CalculoImpuestosTest {

        @Test
        @DisplayName("Debería calcular impuesto federal con monto típico")
        void deberiaCalcularImpuestoFederalConMontoTipico() {
            // Given
            BigDecimal monto = new BigDecimal("1000.00");
            BigDecimal impuestoPais = new BigDecimal("160.00"); // 16% México
            when(calculadorPaisMock.calcularImpuestoPais(monto)).thenReturn(impuestoPais);

            // When
            BigDecimal resultado = calculadorFederal.calcularImpuesto(monto);

            // Then
            BigDecimal impuestoFederalEsperado = monto.multiply(new BigDecimal("0.05")); // 5%
            BigDecimal totalEsperado = impuestoPais.add(impuestoFederalEsperado);

            assertThat(resultado).isEqualByComparingTo(totalEsperado);
            assertThat(resultado).isEqualByComparingTo(new BigDecimal("210.00")); // 160 + 50
            verify(calculadorPaisMock).calcularImpuestoPais(monto);
        }

        @Test
        @DisplayName("Debería calcular correctamente con monto cero")
        void deberiaCalcularCorrectamenteConMontoCero() {
            // Given
            BigDecimal monto = BigDecimal.ZERO;
            BigDecimal impuestoPais = BigDecimal.ZERO;
            when(calculadorPaisMock.calcularImpuestoPais(monto)).thenReturn(impuestoPais);

            // When
            BigDecimal resultado = calculadorFederal.calcularImpuesto(monto);

            // Then
            assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
            verify(calculadorPaisMock).calcularImpuestoPais(monto);
        }

        @Test
        @DisplayName("Debería calcular correctamente con monto pequeño")
        void deberiaCalcularCorrectamenteConMontoPequeno() {
            // Given
            BigDecimal monto = new BigDecimal("10.50");
            BigDecimal impuestoPais = new BigDecimal("1.68"); // 16%
            when(calculadorPaisMock.calcularImpuestoPais(monto)).thenReturn(impuestoPais);

            // When
            BigDecimal resultado = calculadorFederal.calcularImpuesto(monto);

            // Then
            BigDecimal impuestoFederal = new BigDecimal("0.525"); // 10.50 * 0.05
            BigDecimal totalEsperado = impuestoPais.add(impuestoFederal);

            assertThat(resultado).isEqualByComparingTo(totalEsperado);
            assertThat(resultado).isEqualByComparingTo(new BigDecimal("2.205")); // 1.68 + 0.525
        }

        @Test
        @DisplayName("Debería calcular correctamente con monto grande")
        void deberiaCalcularCorrectamenteConMontoGrande() {
            // Given
            BigDecimal monto = new BigDecimal("50000.00");
            BigDecimal impuestoPais = new BigDecimal("8000.00"); // 16%
            when(calculadorPaisMock.calcularImpuestoPais(monto)).thenReturn(impuestoPais);

            // When
            BigDecimal resultado = calculadorFederal.calcularImpuesto(monto);

            // Then
            BigDecimal impuestoFederal = new BigDecimal("2500.00"); // 50000 * 0.05
            BigDecimal totalEsperado = impuestoPais.add(impuestoFederal);

            assertThat(resultado).isEqualByComparingTo(totalEsperado);
            assertThat(resultado).isEqualByComparingTo(new BigDecimal("10500.00")); // 8000 + 2500
        }

        @Test
        @DisplayName("Debería mantener precisión decimal en cálculos")
        void deberiaMantenerPrecisionDecimalEnCalculos() {
            // Given
            BigDecimal monto = new BigDecimal("999.99");
            BigDecimal impuestoPais = new BigDecimal("159.9984"); // 16% con 4 decimales
            when(calculadorPaisMock.calcularImpuestoPais(monto)).thenReturn(impuestoPais);

            // When
            BigDecimal resultado = calculadorFederal.calcularImpuesto(monto);

            // Then
            BigDecimal impuestoFederal = new BigDecimal("49.9995"); // 999.99 * 0.05
            BigDecimal totalEsperado = impuestoPais.add(impuestoFederal);

            assertThat(resultado).isEqualByComparingTo(totalEsperado);
            assertThat(resultado).isEqualByComparingTo(new BigDecimal("209.9979"));
        }
    }

    @Nested
    @DisplayName("Integración con Países")
    class IntegracionPaisesTest {

        @Test
        @DisplayName("Debería funcionar con calculador México simulado")
        void deberiaFuncionarConCalculadorMexicoSimulado() {
            // Given - Simular IVA México 16%
            BigDecimal monto = new BigDecimal("1000.00");
            BigDecimal ivaMexico = new BigDecimal("160.00"); // 16%
            when(calculadorPaisMock.calcularImpuestoPais(monto)).thenReturn(ivaMexico);

            // When
            BigDecimal resultado = calculadorFederal.calcularImpuesto(monto);

            // Then - IVA México (160) + Federal (50) = 210
            assertThat(resultado).isEqualByComparingTo(new BigDecimal("210.00"));
            verify(calculadorPaisMock).calcularImpuestoPais(monto);
        }

        @Test
        @DisplayName("Debería funcionar con calculador USA simulado")
        void deberiaFuncionarConCalculadorUsaSimulado() {
            // Given - Simular Sales Tax USA ~8%
            BigDecimal monto = new BigDecimal("1000.00");
            BigDecimal salesTaxUsa = new BigDecimal("80.00"); // 8%
            when(calculadorPaisMock.calcularImpuestoPais(monto)).thenReturn(salesTaxUsa);

            // When
            BigDecimal resultado = calculadorFederal.calcularImpuesto(monto);

            // Then - Sales Tax USA (80) + Federal (50) = 130
            assertThat(resultado).isEqualByComparingTo(new BigDecimal("130.00"));
            verify(calculadorPaisMock).calcularImpuestoPais(monto);
        }

        @Test
        @DisplayName("Debería funcionar con calculador Canadá simulado")
        void deberiaFuncionarConCalculadorCanadaSimulado() {
            // Given - Simular GST + PST Canadá ~13%
            BigDecimal monto = new BigDecimal("1000.00");
            BigDecimal gstPstCanada = new BigDecimal("130.00"); // 13%
            when(calculadorPaisMock.calcularImpuestoPais(monto)).thenReturn(gstPstCanada);

            // When
            BigDecimal resultado = calculadorFederal.calcularImpuesto(monto);

            // Then - GST+PST Canadá (130) + Federal (50) = 180
            assertThat(resultado).isEqualByComparingTo(new BigDecimal("180.00"));
            verify(calculadorPaisMock).calcularImpuestoPais(monto);
        }
    }

    @Nested
    @DisplayName("Fórmula y Validaciones")
    class FormulaValidacionesTest {

        @Test
        @DisplayName("Debería aplicar fórmula correcta: país + 5% federal")
        void deberiaAplicarFormulaCorrectaPaisMas5PorcentoFederal() {
            // Given
            BigDecimal monto = new BigDecimal("2500.00");
            BigDecimal impuestoPais = new BigDecimal("400.00"); // 16%
            when(calculadorPaisMock.calcularImpuestoPais(monto)).thenReturn(impuestoPais);

            // When
            BigDecimal resultado = calculadorFederal.calcularImpuesto(monto);

            // Then - Verificar fórmula paso a paso
            BigDecimal impuestoFederalEsperado = monto.multiply(new BigDecimal("0.05"));
            BigDecimal totalEsperado = impuestoPais.add(impuestoFederalEsperado);

            assertThat(impuestoFederalEsperado).isEqualByComparingTo(new BigDecimal("125.00"));
            assertThat(totalEsperado).isEqualByComparingTo(new BigDecimal("525.00"));
            assertThat(resultado).isEqualByComparingTo(totalEsperado);
        }

        @Test
        @DisplayName("Debería mantener porcentaje federal constante en 5%")
        void deberiaMantenerPorcentajeFederalConstanteEn5Porciento() {
            // Given - Diferentes montos
            BigDecimal[] montos = {
                new BigDecimal("100.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("5000.00")
            };

            for (BigDecimal monto : montos) {
                // Given
                BigDecimal impuestoPais = new BigDecimal("50.00"); // Constante para simplicidad
                when(calculadorPaisMock.calcularImpuestoPais(monto)).thenReturn(impuestoPais);

                // When
                BigDecimal resultado = calculadorFederal.calcularImpuesto(monto);

                // Then - El impuesto federal siempre debe ser 5% del monto
                BigDecimal impuestoFederalEsperado = monto.multiply(new BigDecimal("0.05"));
                BigDecimal totalEsperado = impuestoPais.add(impuestoFederalEsperado);

                assertThat(resultado).isEqualByComparingTo(totalEsperado);
            }
        }

        @Test
        @DisplayName("Debería manejar impuesto país cero correctamente")
        void deberiaManejarImpuestoPaisCeroCorrectamente() {
            // Given - País sin impuestos
            BigDecimal monto = new BigDecimal("1000.00");
            BigDecimal impuestoPais = BigDecimal.ZERO;
            when(calculadorPaisMock.calcularImpuestoPais(monto)).thenReturn(impuestoPais);

            // When
            BigDecimal resultado = calculadorFederal.calcularImpuesto(monto);

            // Then - Solo el 5% federal
            BigDecimal soloFederal = new BigDecimal("50.00");
            assertThat(resultado).isEqualByComparingTo(soloFederal);
            verify(calculadorPaisMock).calcularImpuestoPais(monto);
        }
    }

    @Nested
    @DisplayName("Casos Límite y Excepciones")
    class CasosLimiteExcepcionesTest {

        @Test
        @DisplayName("Debería manejar valores extremadamente pequeños")
        void deberiaManejarValoresExtremadamentePequenos() {
            // Given
            BigDecimal monto = new BigDecimal("0.01"); // 1 centavo
            BigDecimal impuestoPais = new BigDecimal("0.0016"); // 16%
            when(calculadorPaisMock.calcularImpuestoPais(monto)).thenReturn(impuestoPais);

            // When
            BigDecimal resultado = calculadorFederal.calcularImpuesto(monto);

            // Then
            BigDecimal impuestoFederal = new BigDecimal("0.0005"); // 0.01 * 0.05
            BigDecimal totalEsperado = impuestoPais.add(impuestoFederal);

            assertThat(resultado).isEqualByComparingTo(totalEsperado);
            assertThat(resultado).isEqualByComparingTo(new BigDecimal("0.0021"));
        }

        @Test
        @DisplayName("Debería manejar valores extremadamente grandes")
        void deberiaManejarValoresExtremadamenteGrandes() {
            // Given
            BigDecimal monto = new BigDecimal("999999999.99");
            BigDecimal impuestoPais = new BigDecimal("159999999.9984"); // 16%
            when(calculadorPaisMock.calcularImpuestoPais(monto)).thenReturn(impuestoPais);

            // When
            BigDecimal resultado = calculadorFederal.calcularImpuesto(monto);

            // Then
            BigDecimal impuestoFederal = new BigDecimal("49999999.9995"); // 5%
            BigDecimal totalEsperado = impuestoPais.add(impuestoFederal);

            assertThat(resultado).isEqualByComparingTo(totalEsperado);
            assertThat(resultado).isPositive();
        }

        @Test
        @DisplayName("Debería propagar excepción del calculador país")
        void deberiaPropagaExcepcionDelCalculadorPais() {
            // Given
            BigDecimal monto = new BigDecimal("1000.00");
            when(calculadorPaisMock.calcularImpuestoPais(monto))
                .thenThrow(new IllegalArgumentException("Monto inválido"));

            // When & Then
            assertThatThrownBy(() -> calculadorFederal.calcularImpuesto(monto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Monto inválido");
            verify(calculadorPaisMock).calcularImpuestoPais(monto);
        }

        @Test
        @DisplayName("Debería manejar diferentes escalas de BigDecimal")
        void deberiaManejarDiferentesEscalasDeBigDecimal() {
            // Given - Diferentes escalas
            BigDecimal monto1 = new BigDecimal("1000.0");  // Escala 1
            BigDecimal monto2 = new BigDecimal("1000.00"); // Escala 2
            BigDecimal monto3 = new BigDecimal("1000.000");// Escala 3

            BigDecimal impuestoPais = new BigDecimal("160.00");
            when(calculadorPaisMock.calcularImpuestoPais(any(BigDecimal.class)))
                .thenReturn(impuestoPais);

            // When
            BigDecimal resultado1 = calculadorFederal.calcularImpuesto(monto1);
            BigDecimal resultado2 = calculadorFederal.calcularImpuesto(monto2);
            BigDecimal resultado3 = calculadorFederal.calcularImpuesto(monto3);

            // Then - Todos deberían dar el mismo resultado
            assertThat(resultado1).isEqualByComparingTo(resultado2);
            assertThat(resultado2).isEqualByComparingTo(resultado3);
            assertThat(resultado1).isEqualByComparingTo(new BigDecimal("210.00"));
        }
    }
}