package mx.com.qtx.cotizador.dominio.core.componentes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests unitarios para validaciones de la clase abstracta Componente.
 * Valida las validaciones requeridas por PRD para campos del constructor,
 * métodos básicos heredados y comportamientos de la clase base.
 *
 * Casos cubiertos:
 * - Validaciones de constructor: campos no nulos, no vacíos, valores no negativos
 * - Métodos básicos: getters, setters, calcularUtilidad, cotizar
 * - Factory methods para creación de componentes específicos
 * - Comportamiento con promociones aplicadas
 * - Validaciones de PRD: IllegalArgumentException para parámetros inválidos
 *
 * Algoritmos probados:
 * 1. Validación de parámetros en constructor según PRD
 * 2. Cálculo de utilidad: precioBase - costo
 * 3. Cotización sin promoción: precioBase × cantidad
 * 4. Cotización con promoción: delegación a IPromocion
 *
 * @author Claude Code
 * @version 1.0
 * @see Componente
 * @see DiscoDuro
 * @see TarjetaVideo
 * @see Monitor
 */
@DisplayName("Componente - Tests Unitarios")
public class ComponenteTest {

    // =====================================================================
    // CONSTANTES PARA TESTS
    // =====================================================================

    private static final String ID_VALIDO = "COMP-001";
    private static final String DESCRIPCION_VALIDA = "Componente de prueba";
    private static final String MARCA_VALIDA = "MarcaPrueba";
    private static final String MODELO_VALIDO = "ModeloPrueba";
    private static final BigDecimal COSTO_VALIDO = new BigDecimal("100.00");
    private static final BigDecimal PRECIO_BASE_VALIDO = new BigDecimal("150.00");

    // =====================================================================
    // TESTS DE VALIDACIONES DE CONSTRUCTOR - PRD REQUERIDAS
    // =====================================================================

    @Test
    @DisplayName("Dado constructor con parámetros válidos entonces creación exitosa")
    public void dadoConstructorConParametrosValidosEntoncesCreacionExitosa() {
        // Given & When
        Componente componente = Componente.crearDiscoDuro(
            ID_VALIDO, DESCRIPCION_VALIDA, MARCA_VALIDA, MODELO_VALIDO,
            COSTO_VALIDO, PRECIO_BASE_VALIDO, "1TB"
        );

        // Then
        assertThat(componente).isNotNull();
        assertThat(componente.getId()).isEqualTo(ID_VALIDO);
        assertThat(componente.getDescripcion()).isEqualTo(DESCRIPCION_VALIDA);
        assertThat(componente.getMarca()).isEqualTo(MARCA_VALIDA);
        assertThat(componente.getModelo()).isEqualTo(MODELO_VALIDO);
        assertThat(componente.getCosto()).isEqualTo(COSTO_VALIDO);
        assertThat(componente.getPrecioBase()).isEqualTo(PRECIO_BASE_VALIDO);
    }

    @Test
    @DisplayName("Dado id nulo en constructor entonces lanza IllegalArgumentException")
    public void dadoIdNuloEnConstructorEntoncesLanzaIllegalArgumentException() {
        // Given & When & Then
        assertThatThrownBy(() ->
            Componente.crearDiscoDuro(
                null, DESCRIPCION_VALIDA, MARCA_VALIDA, MODELO_VALIDO,
                COSTO_VALIDO, PRECIO_BASE_VALIDO, "1TB"
            )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("id")
         .hasMessageContaining("nulo");
    }

    @Test
    @DisplayName("Dado descripción nula en constructor entonces lanza IllegalArgumentException")
    public void dadoDescripcionNulaEnConstructorEntoncesLanzaIllegalArgumentException() {
        // Given & When & Then
        assertThatThrownBy(() ->
            Componente.crearDiscoDuro(
                ID_VALIDO, null, MARCA_VALIDA, MODELO_VALIDO,
                COSTO_VALIDO, PRECIO_BASE_VALIDO, "1TB"
            )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("descripcion")
         .hasMessageContaining("nula");
    }

    @Test
    @DisplayName("Dado marca nula en constructor entonces lanza IllegalArgumentException")
    public void dadoMarcaNulaEnConstructorEntoncesLanzaIllegalArgumentException() {
        // Given & When & Then
        assertThatThrownBy(() ->
            Componente.crearDiscoDuro(
                ID_VALIDO, DESCRIPCION_VALIDA, null, MODELO_VALIDO,
                COSTO_VALIDO, PRECIO_BASE_VALIDO, "1TB"
            )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("marca")
         .hasMessageContaining("nula");
    }

    @Test
    @DisplayName("Dado modelo nulo en constructor entonces lanza IllegalArgumentException")
    public void dadoModeloNuloEnConstructorEntoncesLanzaIllegalArgumentException() {
        // Given & When & Then
        assertThatThrownBy(() ->
            Componente.crearDiscoDuro(
                ID_VALIDO, DESCRIPCION_VALIDA, MARCA_VALIDA, null,
                COSTO_VALIDO, PRECIO_BASE_VALIDO, "1TB"
            )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("modelo")
         .hasMessageContaining("nulo");
    }

    @Test
    @DisplayName("Dado costo nulo en constructor entonces lanza IllegalArgumentException")
    public void dadoCostoNuloEnConstructorEntoncesLanzaIllegalArgumentException() {
        // Given & When & Then
        assertThatThrownBy(() ->
            Componente.crearDiscoDuro(
                ID_VALIDO, DESCRIPCION_VALIDA, MARCA_VALIDA, MODELO_VALIDO,
                null, PRECIO_BASE_VALIDO, "1TB"
            )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("costo")
         .hasMessageContaining("nulo");
    }

    @Test
    @DisplayName("Dado precio base nulo en constructor entonces lanza IllegalArgumentException")
    public void dadoPrecioBaseNuloEnConstructorEntoncesLanzaIllegalArgumentException() {
        // Given & When & Then
        assertThatThrownBy(() ->
            Componente.crearDiscoDuro(
                ID_VALIDO, DESCRIPCION_VALIDA, MARCA_VALIDA, MODELO_VALIDO,
                COSTO_VALIDO, null, "1TB"
            )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("precioBase")
         .hasMessageContaining("nulo");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("Dado string vacío en constructor entonces lanza IllegalArgumentException")
    public void dadoStringVacioEnConstructorEntoncesLanzaIllegalArgumentException(String valorVacio) {
        // Given & When & Then
        assertThatThrownBy(() ->
            Componente.crearDiscoDuro(
                valorVacio, DESCRIPCION_VALIDA, MARCA_VALIDA, MODELO_VALIDO,
                COSTO_VALIDO, PRECIO_BASE_VALIDO, "1TB"
            )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("vacío");

        assertThatThrownBy(() ->
            Componente.crearDiscoDuro(
                ID_VALIDO, valorVacio, MARCA_VALIDA, MODELO_VALIDO,
                COSTO_VALIDO, PRECIO_BASE_VALIDO, "1TB"
            )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("vacía");
    }

    @Test
    @DisplayName("Dado costo negativo en constructor entonces lanza IllegalArgumentException")
    public void dadoCostoNegativoEnConstructorEntoncesLanzaIllegalArgumentException() {
        // Given
        BigDecimal costoNegativo = new BigDecimal("-50.00");

        // When & Then
        assertThatThrownBy(() ->
            Componente.crearDiscoDuro(
                ID_VALIDO, DESCRIPCION_VALIDA, MARCA_VALIDA, MODELO_VALIDO,
                costoNegativo, PRECIO_BASE_VALIDO, "1TB"
            )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("costo")
         .hasMessageContaining("negativo");
    }

    @Test
    @DisplayName("Dado precio base negativo en constructor entonces lanza IllegalArgumentException")
    public void dadoPrecioBaseNegativoEnConstructorEntoncesLanzaIllegalArgumentException() {
        // Given
        BigDecimal precioNegativo = new BigDecimal("-100.00");

        // When & Then
        assertThatThrownBy(() ->
            Componente.crearDiscoDuro(
                ID_VALIDO, DESCRIPCION_VALIDA, MARCA_VALIDA, MODELO_VALIDO,
                COSTO_VALIDO, precioNegativo, "1TB"
            )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("precioBase")
         .hasMessageContaining("negativo");
    }

    // =====================================================================
    // TESTS DE MÉTODOS BÁSICOS HEREDADOS
    // =====================================================================

    @Test
    @DisplayName("Dado componente válido cuando calcular utilidad entonces retorna diferencia precio-costo")
    public void dadoComponenteValidoCuandoCalcularUtilidadEntoncesRetornaDiferenciaPrecioCosto() {
        // Given
        Componente componente = Componente.crearMonitor(
            ID_VALIDO, DESCRIPCION_VALIDA, MARCA_VALIDA, MODELO_VALIDO,
            new BigDecimal("200.00"), new BigDecimal("300.00")
        );

        // When
        BigDecimal utilidad = componente.calcularUtilidad();

        // Then
        assertThat(utilidad).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Dado componente sin promoción cuando cotizar entonces retorna precio base por cantidad")
    public void dadoComponenteSinPromocionCuandoCotizarEntoncesRetornaPrecioBasePorCantidad() {
        // Given
        Componente componente = Componente.crearTarjetaVideo(
            ID_VALIDO, DESCRIPCION_VALIDA, MARCA_VALIDA, MODELO_VALIDO,
            COSTO_VALIDO, new BigDecimal("500.00"), "8GB"
        );
        int cantidad = 3;

        // When
        BigDecimal cotizacion = componente.cotizar(cantidad);

        // Then
        assertThat(cotizacion).isEqualTo(new BigDecimal("1500.00"));
        assertThat(componente.getPromo()).isNull();
    }

    @Test
    @DisplayName("Dado componente con promoción cuando cotizar entonces delega a promoción")
    public void dadoComponenteConPromocionCuandoCotizarEntoncesDelegaAPromocion() {
        // Given
        Componente componente = Componente.crearDiscoDuro(
            ID_VALIDO, DESCRIPCION_VALIDA, MARCA_VALIDA, MODELO_VALIDO,
            COSTO_VALIDO, new BigDecimal("400.00"), "2TB"
        );

        // Mock de promoción que aplica 10% descuento
        IPromocion promocionMock = (cantidad, precioBase) ->
            precioBase.multiply(new BigDecimal(cantidad))
                     .multiply(new BigDecimal("0.9"));

        componente.setPromo(promocionMock);

        // When
        BigDecimal cotizacion = componente.cotizar(2);

        // Then
        assertThat(cotizacion).isEqualByComparingTo(new BigDecimal("720.00")); // 400 * 2 * 0.9
        assertThat(componente.getPromo()).isEqualTo(promocionMock);
    }

    // =====================================================================
    // TESTS DE GETTERS Y SETTERS
    // =====================================================================

    @Test
    @DisplayName("Dado componente válido cuando usar getters y setters entonces funcionan correctamente")
    public void dadoComponenteValidoCuandoUsarGettersYSettersEntoncesFuncionanCorrectamente() {
        // Given
        Componente componente = Componente.crearMonitor(
            ID_VALIDO, DESCRIPCION_VALIDA, MARCA_VALIDA, MODELO_VALIDO,
            COSTO_VALIDO, PRECIO_BASE_VALIDO
        );

        // When & Then - Test getters iniciales
        assertThat(componente.getId()).isEqualTo(ID_VALIDO);
        assertThat(componente.getDescripcion()).isEqualTo(DESCRIPCION_VALIDA);
        assertThat(componente.getMarca()).isEqualTo(MARCA_VALIDA);
        assertThat(componente.getModelo()).isEqualTo(MODELO_VALIDO);
        assertThat(componente.getCosto()).isEqualTo(COSTO_VALIDO);
        assertThat(componente.getPrecioBase()).isEqualTo(PRECIO_BASE_VALIDO);

        // When & Then - Test setters
        componente.setId("NUEVO-ID");
        componente.setDescripcion("Nueva descripción");
        componente.setMarca("Nueva marca");
        componente.setModelo("Nuevo modelo");
        componente.setCosto(new BigDecimal("80.00"));
        componente.setPrecioBase(new BigDecimal("120.00"));

        assertThat(componente.getId()).isEqualTo("NUEVO-ID");
        assertThat(componente.getDescripcion()).isEqualTo("Nueva descripción");
        assertThat(componente.getMarca()).isEqualTo("Nueva marca");
        assertThat(componente.getModelo()).isEqualTo("Nuevo modelo");
        assertThat(componente.getCosto()).isEqualTo(new BigDecimal("80.00"));
        assertThat(componente.getPrecioBase()).isEqualTo(new BigDecimal("120.00"));
    }

    // =====================================================================
    // TESTS DE FACTORY METHODS
    // =====================================================================

    @Test
    @DisplayName("Dado factory method crearDiscoDuro cuando usar entonces crea DiscoDuro válido")
    public void dadoFactoryMethodCrearDiscoDuroCuandoUsarEntoncesCreaDiscoDuroValido() {
        // Given & When
        Componente disco = Componente.crearDiscoDuro(
            ID_VALIDO, DESCRIPCION_VALIDA, MARCA_VALIDA, MODELO_VALIDO,
            COSTO_VALIDO, PRECIO_BASE_VALIDO, "500GB"
        );

        // Then
        assertThat(disco).isInstanceOf(DiscoDuro.class);
        assertThat(disco.getCategoria()).isEqualTo("Disco Duro");
        assertThat(((DiscoDuro) disco).getCapacidadAlm()).isEqualTo("500GB");
    }

    @Test
    @DisplayName("Dado factory method crearTarjetaVideo cuando usar entonces crea TarjetaVideo válida")
    public void dadoFactoryMethodCrearTarjetaVideoCuandoUsarEntoncesCreaTarjetaVideoValida() {
        // Given & When
        Componente tarjeta = Componente.crearTarjetaVideo(
            ID_VALIDO, DESCRIPCION_VALIDA, MARCA_VALIDA, MODELO_VALIDO,
            COSTO_VALIDO, PRECIO_BASE_VALIDO, "16GB"
        );

        // Then
        assertThat(tarjeta).isInstanceOf(TarjetaVideo.class);
        assertThat(tarjeta.getCategoria()).isEqualTo("Tarjeta de Video");
        assertThat(((TarjetaVideo) tarjeta).getMemoria()).isEqualTo("16GB");
    }

    @Test
    @DisplayName("Dado factory method crearMonitor cuando usar entonces crea Monitor válido")
    public void dadoFactoryMethodCrearMonitorCuandoUsarEntoncesCreaMonitorValido() {
        // Given & When
        Componente monitor = Componente.crearMonitor(
            ID_VALIDO, DESCRIPCION_VALIDA, MARCA_VALIDA, MODELO_VALIDO,
            COSTO_VALIDO, PRECIO_BASE_VALIDO
        );

        // Then
        assertThat(monitor).isInstanceOf(Monitor.class);
        assertThat(monitor.getCategoria()).isEqualTo("Monitor");
    }

    @Test
    @DisplayName("Dado getPcBuilder cuando usar entonces retorna PcBuilder válido")
    public void dadoGetPcBuilderCuandoUsarEntoncesRetornaPcBuilderValido() {
        // Given & When
        PcBuilder builder = Componente.getPcBuilder();

        // Then
        assertThat(builder).isNotNull();
        assertThat(builder).isInstanceOf(PcBuilder.class);
    }

    // =====================================================================
    // TESTS DE MÉTODOS ADICIONALES
    // =====================================================================

    @Test
    @DisplayName("Dado componente válido cuando toString entonces retorna representación correcta")
    public void dadoComponenteValidoCuandoToStringEntoncesRetornaRepresentacionCorrecta() {
        // Given
        Componente componente = Componente.crearDiscoDuro(
            "TEST-001", "Disco de prueba", "TestBrand", "TestModel",
            new BigDecimal("75.50"), new BigDecimal("125.75"), "1TB"
        );

        // When
        String resultado = componente.toString();

        // Then
        assertThat(resultado)
            .contains("TEST-001")
            .contains("Disco de prueba")
            .contains("TestBrand")
            .contains("TestModel")
            .contains("75.50")
            .contains("125.75");
    }
}