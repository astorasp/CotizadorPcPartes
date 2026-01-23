package mx.com.qtx.cotizador.dominio.promos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Clase de tests unitarios para PromocionBuilder.
 * 
 * Esta clase valida el comportamiento del patrón Builder aplicado a la construcción
 * de promociones complejas mediante acumulación de descuentos. Incluye tests para
 * promociones base (sin descuento y N×M) y acumulación de descuentos planos y por cantidad.
 * 
 * Casos de prueba cubiertos:
 * - Inicialización del builder
 * - Promociones base: sin descuento y N×M
 * - Acumulación de descuentos planos
 * - Acumulación de descuentos por cantidad
 * - Construcción de promociones complejas
 * - Validación de encadenamiento fluido
 * - Casos límite y edge cases
 * 
 * @author Sistema de Cotización PC
 * @version 1.0
 * @see PromocionBuilder
 * @see Promocion
 */
@DisplayName("PromocionBuilder - Tests del Constructor de Promociones")
class PromocionBuilderTest {

    private PromocionBuilder builder;

    /**
     * Configuración inicial para cada test.
     * Crea una nueva instancia del builder antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        builder = new PromocionBuilder();
    }

    // ==================== TESTS DE INICIALIZACIÓN ====================

    /**
     * Verifica que el PromocionBuilder se inicialice correctamente.
     * 
     * Given: Constructor por defecto del PromocionBuilder
     * When: Se crea una nueva instancia
     * Then: Se inicializan las listas vacías de descuentos
     */
    @Test
    @DisplayName("Constructor debe inicializar listas de descuentos vacías")
    void constructor_debeInicializarListasVacias() {
        // Given & When: Constructor ejecutado en @BeforeEach
        
        // Then: Listas inicializadas y vacías
        assertAll("Inicialización del builder",
            () -> assertNotNull(builder.getLstDsctosPlanos(), 
                "Lista de descuentos planos debe estar inicializada"),
            () -> assertThat(builder.getLstDsctosPlanos()).isEmpty(),
            () -> assertNotNull(builder.getLstMapsCantVsDscto(), 
                "Lista de mapas de descuentos por cantidad debe estar inicializada"),
            () -> assertThat(builder.getLstMapsCantVsDscto()).isEmpty(),
            () -> assertEquals(0, builder.getTipoPromocionBase(), 
                "Tipo de promoción base debe ser 0 (no definido)")
        );
    }

    /**
     * Verifica que el builder tenga las constantes de tipos de promoción correctas.
     * 
     * Given: Constantes estáticas del PromocionBuilder
     * When: Se acceden a las constantes
     * Then: Tienen los valores esperados
     */
    @Test
    @DisplayName("Constantes de tipos de promoción deben tener valores correctos")
    void constantes_debenTenerValoresCorrectos() {
        // Given & When & Then: Verificación de constantes
        assertAll("Constantes de tipos de promoción",
            () -> assertEquals(1, PromocionBuilder.PROM_BASE_SIN_DSCTO,
                "PROM_BASE_SIN_DSCTO debe ser 1"),
            () -> assertEquals(2, PromocionBuilder.PROM_BASE_NXM,
                "PROM_BASE_NXM debe ser 2")
        );
    }

    // ==================== TESTS DE PROMOCIÓN BASE SIN DESCUENTO ====================

    /**
     * Verifica la configuración de promoción base sin descuento.
     * 
     * Given: Un PromocionBuilder inicializado
     * When: Se configura promoción base sin descuento
     * Then: Se establece el tipo correcto y retorna el builder
     */
    @Test
    @DisplayName("conPromocionBaseSinDscto() debe configurar tipo y permitir encadenamiento")
    void conPromocionBaseSinDscto_debeConfigurarTipoYPermitirEncadenamiento() {
        // Given: Builder inicializado
        
        // When: Se configura promoción base sin descuento
        PromocionBuilder resultado = builder.conPromocionBaseSinDscto();
        
        // Then: Tipo configurado correctamente y permite encadenamiento
        assertAll("Promoción base sin descuento",
            () -> assertThat(resultado).isSameAs(builder)
                .as("Debe retornar la misma instancia para encadenamiento"),
            () -> assertEquals(PromocionBuilder.PROM_BASE_SIN_DSCTO, builder.getTipoPromocionBase(),
                "Tipo de promoción base debe ser SIN_DSCTO")
        );
    }

    /**
     * Verifica la construcción de promoción sin descuento.
     * 
     * Given: PromocionBuilder configurado con promoción base sin descuento
     * When: Se construye la promoción
     * Then: Se crea una PromSinDescto funcional
     */
    @Test
    @DisplayName("build() con promoción sin descuento debe crear PromSinDescto")
    void build_conPromocionSinDescuento_debeCrearPromSinDescto() {
        // Given: Builder con promoción base sin descuento
        builder.conPromocionBaseSinDscto();
        
        // When: Se construye la promoción
        Promocion promocion = builder.build();
        
        // Then: Se crea instancia correcta
        assertAll("Promoción sin descuento creada",
            () -> assertNotNull(promocion, "Promoción debe ser creada"),
            () -> assertThat(promocion).isInstanceOf(PromSinDescto.class)
                .as("Debe ser instancia de PromSinDescto"),
            () -> {
                // Verificar comportamiento: sin descuento debe retornar precio×cantidad
                BigDecimal precio = new BigDecimal("100.00");
                int cantidad = 3;
                BigDecimal esperado = precio.multiply(new BigDecimal(cantidad));
                BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precio);
                assertEquals(0, esperado.compareTo(resultado), "Sin descuento debe retornar precio×cantidad");
            }
        );
    }

    // ==================== TESTS DE PROMOCIÓN BASE N×M ====================

    /**
     * Verifica la configuración de promoción base N×M.
     * 
     * Given: Un PromocionBuilder inicializado
     * When: Se configura promoción base N×M
     * Then: Se establecen los parámetros N y M correctamente
     */
    @Test
    @DisplayName("conPromocionBaseNXM() debe configurar parámetros N y M")
    void conPromocionBaseNXM_debeConfigurarParametrosNyM() {
        // Given: Builder inicializado
        int n = 3;
        int m = 2;
        
        // When: Se configura promoción N×M
        PromocionBuilder resultado = builder.conPromocionBaseNXM(n, m);
        
        // Then: Parámetros configurados correctamente
        assertAll("Promoción base N×M",
            () -> assertThat(resultado).isSameAs(builder)
                .as("Debe retornar la misma instancia para encadenamiento"),
            () -> assertEquals(PromocionBuilder.PROM_BASE_NXM, builder.getTipoPromocionBase(),
                "Tipo de promoción base debe ser NXM"),
            () -> assertEquals(n, builder.getN(), "Parámetro N debe estar configurado"),
            () -> assertEquals(m, builder.getM(), "Parámetro M debe estar configurado")
        );
    }

    /**
     * Verifica la construcción de promoción N×M con parámetros válidos.
     * 
     * Given: PromocionBuilder configurado con promoción N×M (3×2)
     * When: Se construye la promoción
     * Then: Se crea una PromNXM con comportamiento correcto
     */
    @Test
    @DisplayName("build() con promoción 3×2 debe crear PromNXM funcional")
    void build_conPromocion3x2_debeCrearPromNXMFuncional() {
        // Given: Builder con promoción 3×2 (lleva 3, paga 2)
        int n = 3;
        int m = 2;
        builder.conPromocionBaseNXM(n, m);
        
        // When: Se construye la promoción
        Promocion promocion = builder.build();
        
        // Then: Se crea instancia correcta con comportamiento esperado
        assertAll("Promoción 3×2 creada",
            () -> assertNotNull(promocion, "Promoción debe ser creada"),
            () -> assertThat(promocion).isInstanceOf(PromNXM.class)
                .as("Debe ser instancia de PromNXM"),
            () -> {
                // Verificar comportamiento: 6 unidades deben costar como 4 (2 grupos de 3×2)
                BigDecimal precio = new BigDecimal("50.00");
                int cantidad = 6;
                BigDecimal esperado = precio.multiply(new BigDecimal(4)); // 6 lleva, 4 paga
                BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precio);
                assertEquals(0, esperado.compareTo(resultado), "6 unidades con 3×2 deben costar como 4");
            }
        );
    }

    /**
     * Verifica comportamiento de promoción N×M con cantidad no múltiplo.
     * 
     * Given: PromocionBuilder configurado con promoción 3×2
     * When: Se aplica a cantidad no múltiplo de N (5 unidades)
     * Then: Se aplica promoción solo a grupos completos
     */
    @Test
    @DisplayName("Promoción 3×2 con 5 unidades debe aplicar solo a grupos completos")
    void promocion3x2_con5Unidades_debeAplicarSoloGruposCompletos() {
        // Given: Builder con promoción 3×2
        builder.conPromocionBaseNXM(3, 2);
        Promocion promocion = builder.build();
        
        // When: Se aplica a 5 unidades
        BigDecimal precio = new BigDecimal("30.00");
        int cantidad = 5;
        
        // Then: 1 grupo de 3×2 (paga 2) + 2 unidades normales = 4 unidades totales
        BigDecimal esperado = precio.multiply(new BigDecimal(4)); // 2 + 2 = 4
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precio);
        
        assertEquals(0, esperado.compareTo(resultado), 
            "5 unidades con 3×2: 1 grupo (paga 2) + 2 sueltas = 4 unidades");
    }

    // ==================== TESTS DE DESCUENTOS PLANOS ====================

    /**
     * Verifica la adición de descuentos planos individuales.
     * 
     * Given: Un PromocionBuilder inicializado
     * When: Se agrega un descuento plano
     * Then: Se añade a la lista y permite encadenamiento
     */
    @Test
    @DisplayName("agregarDsctoPlano() debe añadir descuento y permitir encadenamiento")
    void agregarDsctoPlano_debeAnadirDescuentoYPermitirEncadenamiento() {
        // Given: Builder inicializado
        float descuento = 15.0f;
        
        // When: Se agrega descuento plano
        PromocionBuilder resultado = builder.agregarDsctoPlano(descuento);
        
        // Then: Descuento agregado y encadenamiento posible
        assertAll("Descuento plano agregado",
            () -> assertThat(resultado).isSameAs(builder)
                .as("Debe retornar la misma instancia para encadenamiento"),
            () -> assertThat(builder.getLstDsctosPlanos()).hasSize(1)
                .as("Lista debe tener un descuento"),
            () -> assertThat(builder.getLstDsctosPlanos()).containsExactly(descuento)
                .as("Lista debe contener el descuento agregado")
        );
    }

    /**
     * Verifica la acumulación de múltiples descuentos planos.
     * 
     * Given: Un PromocionBuilder inicializado
     * When: Se agregan múltiples descuentos planos
     * Then: Se acumulan en el orden correcto
     */
    @Test
    @DisplayName("Múltiples descuentos planos deben acumularse en orden")
    void multiplesDescuentosPlanos_debenAcumularseEnOrden() {
        // Given: Builder inicializado
        float desc1 = 10.0f;
        float desc2 = 5.0f;
        float desc3 = 8.5f;
        
        // When: Se agregan múltiples descuentos
        builder.agregarDsctoPlano(desc1)
               .agregarDsctoPlano(desc2)
               .agregarDsctoPlano(desc3);
        
        // Then: Descuentos en orden correcto
        assertThat(builder.getLstDsctosPlanos())
            .hasSize(3)
            .containsExactly(desc1, desc2, desc3)
            .as("Descuentos deben estar en orden de inserción");
    }

    /**
     * Verifica la construcción de promoción con descuento plano.
     * 
     * Given: PromocionBuilder con promoción base sin descuento y descuento plano 20%
     * When: Se construye la promoción
     * Then: Se crea promoción acumulable con descuento aplicado
     */
    @Test
    @DisplayName("build() con descuento plano 20% debe aplicar descuento correctamente")
    void build_conDescuentoPlano20_debeAplicarDescuentoCorrectamente() {
        // Given: Builder con promoción base y descuento plano 20%
        builder.conPromocionBaseSinDscto()
               .agregarDsctoPlano(20.0f);
        
        // When: Se construye la promoción
        Promocion promocion = builder.build();
        
        // Then: Descuento aplicado correctamente
        assertAll("Promoción con descuento plano 20%",
            () -> assertNotNull(promocion, "Promoción debe ser creada"),
            () -> assertThat(promocion).isInstanceOf(PromDsctoPlano.class)
                .as("Debe ser instancia de PromDsctoPlano"),
            () -> {
                // Verificar comportamiento: 20% de descuento
                BigDecimal precio = new BigDecimal("100.00");
                int cantidad = 2;
                // Sin descuento: 200.00, con 20%: 160.00
                BigDecimal esperado = new BigDecimal("160.00");
                BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precio);
                assertEquals(0, esperado.compareTo(resultado), "20% descuento: $200 debe quedar en $160");
            }
        );
    }

    // ==================== TESTS DE DESCUENTOS POR CANTIDAD ====================

    /**
     * Verifica la adición de descuentos por cantidad.
     * 
     * Given: Un PromocionBuilder inicializado
     * When: Se agrega un mapa de descuentos por cantidad
     * Then: Se añade a la lista y permite encadenamiento
     */
    @Test
    @DisplayName("agregarDsctoXcantidad() debe añadir mapa y permitir encadenamiento")
    void agregarDsctoXcantidad_debeAnadirMapaYPermitirEncadenamiento() {
        // Given: Builder inicializado y mapa de descuentos
        Map<Integer, Double> mapaDescuentos = new HashMap<>();
        mapaDescuentos.put(5, 10.0);  // 5+ unidades: 10% descuento
        mapaDescuentos.put(10, 20.0); // 10+ unidades: 20% descuento
        
        // When: Se agrega descuento por cantidad
        PromocionBuilder resultado = builder.agregarDsctoXcantidad(mapaDescuentos);
        
        // Then: Mapa agregado y encadenamiento posible
        assertAll("Descuento por cantidad agregado",
            () -> assertThat(resultado).isSameAs(builder)
                .as("Debe retornar la misma instancia para encadenamiento"),
            () -> assertThat(builder.getLstMapsCantVsDscto()).hasSize(1)
                .as("Lista debe tener un mapa"),
            () -> assertThat(builder.getLstMapsCantVsDscto().get(0))
                .containsExactlyInAnyOrderEntriesOf(mapaDescuentos)
                .as("Mapa debe contener los descuentos agregados")
        );
    }

    /**
     * Verifica la construcción de promoción con descuento por cantidad.
     * 
     * Given: PromocionBuilder con promoción base y descuento por cantidad
     * When: Se construye la promoción para diferentes cantidades
     * Then: Se aplican los descuentos según las cantidades
     */
    @Test
    @DisplayName("build() con descuento por cantidad debe aplicar según rangos")
    void build_conDescuentoPorCantidad_debeAplicarSegunRangos() {
        // Given: Builder con promoción base y descuentos por cantidad
        Map<Integer, Double> mapaDescuentos = new HashMap<>();
        mapaDescuentos.put(3, 10.0);  // 3+ unidades: 10% descuento
        mapaDescuentos.put(6, 25.0);  // 6+ unidades: 25% descuento
        
        builder.conPromocionBaseSinDscto()
               .agregarDsctoXcantidad(mapaDescuentos);
        
        // When: Se construye la promoción
        Promocion promocion = builder.build();
        BigDecimal precio = new BigDecimal("100.00");
        
        // Then: Descuentos aplicados según cantidad
        assertAll("Promoción con descuento por cantidad",
            () -> assertNotNull(promocion, "Promoción debe ser creada"),
            () -> assertThat(promocion).isInstanceOf(PromDsctoXcantidad.class)
                .as("Debe ser instancia de PromDsctoXcantidad"),
            () -> {
                // 2 unidades: sin descuento = $200
                BigDecimal resultado2 = promocion.calcularImportePromocion(2, precio);
                assertEquals(0, new BigDecimal("200.00").compareTo(resultado2), 
                    "2 unidades sin descuento: $200");
            },
            () -> {
                // 4 unidades: 10% descuento = $360
                BigDecimal resultado4 = promocion.calcularImportePromocion(4, precio);
                assertEquals(0, new BigDecimal("360.00").compareTo(resultado4), 
                    "4 unidades con 10% descuento: $360");
            },
            () -> {
                // 8 unidades: 25% descuento = $600
                BigDecimal resultado8 = promocion.calcularImportePromocion(8, precio);
                assertEquals(0, new BigDecimal("600.00").compareTo(resultado8), 
                    "8 unidades con 25% descuento: $600");
            }
        );
    }

    // ==================== TESTS DE PROMOCIONES COMPLEJAS ====================

    /**
     * Verifica la construcción de promoción compleja con N×M + descuentos acumulables.
     * 
     * Given: PromocionBuilder con promoción 2×1 + descuento plano 15% + descuento por cantidad
     * When: Se construye la promoción
     * Then: Se aplican todos los descuentos en secuencia correcta
     */
    @Test
    @DisplayName("Promoción compleja: 2×1 + 15% plano + descuento por cantidad")
    void promocionCompleja_2x1MasDescuentos_debeAplicarTodosEnSecuencia() {
        // Given: Builder con promoción compleja
        Map<Integer, Double> mapaDescuentos = new HashMap<>();
        mapaDescuentos.put(4, 10.0);  // 4+ unidades: 10% adicional
        
        builder.conPromocionBaseNXM(2, 1)    // Lleva 2, paga 1
               .agregarDsctoPlano(15.0f)     // 15% descuento plano
               .agregarDsctoXcantidad(mapaDescuentos); // 10% adicional en 4+
        
        // When: Se construye y aplica a 6 unidades
        Promocion promocion = builder.build();
        BigDecimal precio = new BigDecimal("100.00");
        int cantidad = 6;
        
        // Then: Aplicación en secuencia:
        // 1. Base 2×1: 6 unidades → paga 3 → $300
        // 2. Descuento plano 15%: $300 → $255  
        // 3. Descuento por cantidad 10% (6≥4): $255 → $229.50
        BigDecimal esperado = new BigDecimal("229.50");
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precio);
        
        assertEquals(0, esperado.compareTo(resultado), 
            "Promoción compleja: 6 unidades con 2×1 + 15% + 10% = $229.50");
    }

    /**
     * Verifica promoción compleja con múltiples descuentos planos acumulables.
     * 
     * Given: PromocionBuilder con promoción base + múltiples descuentos planos
     * When: Se construye la promoción
     * Then: Se aplican todos los descuentos planos en secuencia
     */
    @Test
    @DisplayName("Múltiples descuentos planos se deben acumular secuencialmente")
    void multiplesDescuentosPlanos_debenAcumularseSecuencialmente() {
        // Given: Builder con múltiples descuentos planos
        builder.conPromocionBaseSinDscto()
               .agregarDsctoPlano(20.0f)  // 20% descuento
               .agregarDsctoPlano(10.0f); // 10% adicional sobre el resultado anterior
        
        // When: Se construye y aplica
        Promocion promocion = builder.build();
        BigDecimal precio = new BigDecimal("100.00");
        int cantidad = 1;
        
        // Then: Aplicación secuencial:
        // 1. Sin descuento: $100
        // 2. Primer descuento 20%: $100 → $80
        // 3. Segundo descuento 10%: $80 → $72
        BigDecimal esperado = new BigDecimal("72.00");
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precio);
        
        assertEquals(0, esperado.compareTo(resultado), 
            "Múltiples descuentos planos: 20% + 10% secuencial = $72");
    }

    // ==================== TESTS DE CASOS LÍMITE ====================

    /**
     * Verifica comportamiento con promoción N×M donde N = M.
     * 
     * Given: PromocionBuilder con promoción 1×1 (sin beneficio real)
     * When: Se construye la promoción
     * Then: Funciona pero no ofrece descuento
     */
    @Test
    @DisplayName("Promoción 1×1 debe funcionar sin ofrecer descuento")
    void promocion1x1_debeFuncionarSinDescuento() {
        // Given: Builder con promoción 1×1
        builder.conPromocionBaseNXM(1, 1);
        
        // When: Se construye y aplica
        Promocion promocion = builder.build();
        BigDecimal precio = new BigDecimal("50.00");
        int cantidad = 5;
        
        // Then: No hay descuento real (lleva 1, paga 1)
        BigDecimal esperado = precio.multiply(new BigDecimal(cantidad));
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precio);
        
        assertEquals(0, esperado.compareTo(resultado), "Promoción 1×1 no debe ofrecer descuento");
    }

    /**
     * Verifica comportamiento con descuento plano del 0%.
     * 
     * Given: PromocionBuilder con descuento plano 0%
     * When: Se construye la promoción
     * Then: No modifica el precio
     */
    @Test
    @DisplayName("Descuento plano 0% no debe modificar el precio")
    void descuentoPlano0Porciento_noDebeModificarPrecio() {
        // Given: Builder con descuento plano 0%
        builder.conPromocionBaseSinDscto()
               .agregarDsctoPlano(0.0f);
        
        // When: Se construye y aplica
        Promocion promocion = builder.build();
        BigDecimal precio = new BigDecimal("75.00");
        int cantidad = 3;
        
        // Then: Precio sin modificar
        BigDecimal esperado = precio.multiply(new BigDecimal(cantidad));
        BigDecimal resultado = promocion.calcularImportePromocion(cantidad, precio);
        
        assertEquals(0, esperado.compareTo(resultado), "Descuento 0% no debe modificar el precio");
    }

    /**
     * Verifica que mapa de descuentos por cantidad vacío lanza excepción de validación.
     *
     * Given: PromocionBuilder intentando agregar mapa de descuentos vacío
     * When: Se intenta agregar el mapa vacío
     * Then: Lanza IllegalArgumentException
     */
    @Test
    @DisplayName("Mapa de descuentos vacío debería lanzar excepción de validación")
    void mapaDescuentosVacio_deberiaLanzarExcepcion() {
        // Given: Builder con mapa vacío
        Map<Integer, Double> mapaVacio = new HashMap<>();

        // When & Then: Intentar agregar mapa vacío debe fallar
        assertThrows(IllegalArgumentException.class, () -> {
            builder.conPromocionBaseSinDscto()
                   .agregarDsctoXcantidad(mapaVacio);
        });
    }

    // ==================== TESTS DE VALIDACIÓN Y GETTERS ====================

    /**
     * Verifica el comportamiento del método toString().
     * 
     * Given: PromocionBuilder con configuración específica
     * When: Se llama toString()
     * Then: Retorna representación legible del estado
     */
    @Test
    @DisplayName("toString() debe mostrar estado actual del builder")
    void toString_debeMostrarEstadoActual() {
        // Given: Builder con configuración específica
        Map<Integer, Double> mapa = Map.of(5, 15.0);
        builder.conPromocionBaseNXM(3, 2)
               .agregarDsctoPlano(10.0f)
               .agregarDsctoXcantidad(mapa);
        
        // When: Se obtiene representación en string
        String resultado = builder.toString();
        
        // Then: Contiene información relevante
        assertAll("toString() del builder",
            () -> assertThat(resultado).contains("PromocionBuilder")
                .as("Debe incluir nombre de la clase"),
            () -> assertThat(resultado).contains("tipoPromocionBase=2")
                .as("Debe mostrar tipo de promoción base"),
            () -> assertThat(resultado).contains("n=3", "m=2")
                .as("Debe mostrar parámetros N y M"),
            () -> assertThat(resultado).contains("10.0")
                .as("Debe mostrar descuentos planos"),
            () -> assertThat(resultado).contains("15.0")
                .as("Debe mostrar descuentos por cantidad")
        );
    }

    // ==================== TESTS DE INTEGRACIÓN CON PROMOCION ====================

    /**
     * Verifica integración con método factory de Promocion.
     * 
     * Given: PromocionBuilder configurado
     * When: Se usa Promocion.getBuilder()
     * Then: Funciona igual que constructor directo
     */
    @Test
    @DisplayName("Integración con Promocion.getBuilder() debe funcionar correctamente")
    void integracionConPromocionGetBuilder_debeFuncionarCorrectamente() {
        // Given: Builder obtenido via factory method
        PromocionBuilder builderFactory = Promocion.getBuilder();
        
        // When: Se configura y construye
        Promocion promocion = builderFactory
            .conPromocionBaseSinDscto()
            .agregarDsctoPlano(25.0f)
            .build();
        
        // Then: Funciona correctamente
        assertAll("Integración con factory method",
            () -> assertNotNull(builderFactory, "Factory debe retornar builder"),
            () -> assertNotNull(promocion, "Builder factory debe construir promoción"),
            () -> {
                BigDecimal precio = new BigDecimal("80.00");
                BigDecimal esperado = new BigDecimal("60.00"); // 25% descuento
                BigDecimal resultado = promocion.calcularImportePromocion(1, precio);
                assertEquals(0, esperado.compareTo(resultado), "Promoción factory debe funcionar igual");
            }
        );
    }
}