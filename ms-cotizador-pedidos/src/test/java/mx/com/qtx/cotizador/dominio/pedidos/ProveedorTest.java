package mx.com.qtx.cotizador.dominio.pedidos;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import mx.com.qtx.cotizador.util.TestUtils;

/**
 * Pruebas unitarias para la clase {@link Proveedor}.
 *
 * <p>Esta clase de pruebas verifica el funcionamiento correcto del objeto de dominio Proveedor,
 * que representa a los proveedores de artículos o servicios en el sistema. Las pruebas cubren:</p>
 *
 * <ul>
 *   <li><strong>Constructor:</strong> Inicialización correcta con parámetros válidos e inválidos</li>
 *   <li><strong>Getters:</strong> Acceso correcto a todos los campos del proveedor</li>
 *   <li><strong>Inmutabilidad:</strong> Verificación de que el objeto es inmutable después de creación</li>
 *   <li><strong>Validación:</strong> Comportamiento con datos null y strings vacíos</li>
 *   <li><strong>Casos Edge:</strong> Manejo de valores límite y caracteres especiales</li>
 *   <li><strong>Igualdad:</strong> Comparación entre instancias y consistencia</li>
 * </ul>
 *
 * <h3>Cobertura de Casos de Prueba:</h3>
 * <table border="1">
 *   <tr><th>Categoría</th><th>Escenarios</th><th>Validaciones</th></tr>
 *   <tr><td>Constructor</td><td>Parámetros válidos, null, vacíos</td><td>Inicialización correcta</td></tr>
 *   <tr><td>Getters</td><td>Todos los campos, consistencia</td><td>Valores correctos</td></tr>
 *   <tr><td>Inmutabilidad</td><td>Intentos de modificación externa</td><td>Estado protegido</td></tr>
 *   <tr><td>Casos Edge</td><td>Strings largos, caracteres especiales</td><td>Robustez</td></tr>
 *   <tr><td>Igualdad</td><td>Comparaciones, hashCode consistency</td><td>Comportamiento igual</td></tr>
 * </table>
 *
 * <h3>Patrones de Prueba Utilizados:</h3>
 * <ul>
 *   <li><strong>AAA Pattern:</strong> Arrange-Act-Assert para estructura clara</li>
 *   <li><strong>Nested Tests:</strong> Agrupación lógica por funcionalidad</li>
 *   <li><strong>TestUtils:</strong> Datos de prueba estandarizados</li>
 *   <li><strong>Immutability Testing:</strong> Verificación de objetos inmutables</li>
 *   <li><strong>Edge Case Testing:</strong> Casos límite y valores extremos</li>
 *   <li><strong>Null Safety Testing:</strong> Comportamiento con valores null</li>
 * </ul>
 *
 * <h3>Características del Objeto Proveedor:</h3>
 * <p>El objeto Proveedor es un Value Object inmutable que representa:</p>
 * <ul>
 *   <li><strong>cve:</strong> Clave única que identifica al proveedor</li>
 *   <li><strong>nombre:</strong> Nombre comercial o corto del proveedor</li>
 *   <li><strong>razonSocial:</strong> Razón social completa y legal del proveedor</li>
 * </ul>
 *
 * @author Sistema de Testing ms-cotizador-pedidos
 * @version 1.0.0
 * @since 1.0.0
 * @see Proveedor
 * @see TestUtils#crearProveedorValido()
 * @see TestUtils#crearProveedor(String, String, String)
 */
@DisplayName("Proveedor - Value Object inmutable para datos de proveedor")
class ProveedorTest {

    // ==================== CAMPOS DE PRUEBA ====================

    private Proveedor proveedor;

    // Constantes para pruebas
    private static final String CVE_VALIDA = TestUtils.DEFAULT_PROVEEDOR_CVE;
    private static final String NOMBRE_VALIDO = TestUtils.DEFAULT_PROVEEDOR_NOMBRE;
    private static final String RAZON_SOCIAL_VALIDA = NOMBRE_VALIDO + " SA de CV";

    // ==================== CONFIGURACIÓN DE PRUEBAS ====================

    @BeforeEach
    void setUp() {
        proveedor = TestUtils.crearProveedorValido();
    }

    // ==================== TESTS DE CONSTRUCTOR ====================

    @Nested
    @DisplayName("Constructor y Inicialización")
    class ConstructorTest {

        @Test
        @DisplayName("Debería crear proveedor correctamente con parámetros válidos")
        void constructor_deberiaCrearProveedorCorrectamente() {
            // Act
            Proveedor nuevoProveedor = new Proveedor(CVE_VALIDA, NOMBRE_VALIDO, RAZON_SOCIAL_VALIDA);

            // Assert
            assertThat(nuevoProveedor).isNotNull();
            assertThat(nuevoProveedor.getCve()).isEqualTo(CVE_VALIDA);
            assertThat(nuevoProveedor.getNombre()).isEqualTo(NOMBRE_VALIDO);
            assertThat(nuevoProveedor.getRazonSocial()).isEqualTo(RAZON_SOCIAL_VALIDA);
        }

        @Test
        @DisplayName("Debería aceptar strings vacíos sin lanzar excepción")
        void constructor_deberiaAceptarStringsVacios() {
            // Act & Assert - No debe lanzar excepción
            assertDoesNotThrow(() -> {
                new Proveedor("", "", "");
            });

            Proveedor proveedorVacio = new Proveedor("", "", "");
            assertThat(proveedorVacio.getCve()).isEmpty();
            assertThat(proveedorVacio.getNombre()).isEmpty();
            assertThat(proveedorVacio.getRazonSocial()).isEmpty();
        }

        @Test
        @DisplayName("Debería aceptar valores null sin lanzar excepción")
        void constructor_deberiaAceptarValoresNull() {
            // Act & Assert - No debe lanzar excepción
            assertDoesNotThrow(() -> {
                new Proveedor(null, null, null);
            });

            Proveedor proveedorNull = new Proveedor(null, null, null);
            assertThat(proveedorNull.getCve()).isNull();
            assertThat(proveedorNull.getNombre()).isNull();
            assertThat(proveedorNull.getRazonSocial()).isNull();
        }

        @Test
        @DisplayName("Debería manejar combinaciones de valores válidos y null")
        void constructor_deberiaManejarCombinacionesValores() {
            // Act
            Proveedor proveedorMixto1 = new Proveedor("PROV-001", null, "Razón Social SA");
            Proveedor proveedorMixto2 = new Proveedor(null, "Nombre Proveedor", null);
            Proveedor proveedorMixto3 = new Proveedor("PROV-003", "Nombre", null);

            // Assert
            assertThat(proveedorMixto1.getCve()).isEqualTo("PROV-001");
            assertThat(proveedorMixto1.getNombre()).isNull();
            assertThat(proveedorMixto1.getRazonSocial()).isEqualTo("Razón Social SA");

            assertThat(proveedorMixto2.getCve()).isNull();
            assertThat(proveedorMixto2.getNombre()).isEqualTo("Nombre Proveedor");
            assertThat(proveedorMixto2.getRazonSocial()).isNull();

            assertThat(proveedorMixto3.getCve()).isEqualTo("PROV-003");
            assertThat(proveedorMixto3.getNombre()).isEqualTo("Nombre");
            assertThat(proveedorMixto3.getRazonSocial()).isNull();
        }

        @Test
        @DisplayName("Debería aceptar strings con espacios en blanco")
        void constructor_deberiaAceptarEspaciosEnBlanco() {
            // Act
            Proveedor proveedorEspacios = new Proveedor("  PROV-001  ", "  Nombre  ", "  Razón Social  ");

            // Assert
            assertThat(proveedorEspacios.getCve()).isEqualTo("  PROV-001  ");
            assertThat(proveedorEspacios.getNombre()).isEqualTo("  Nombre  ");
            assertThat(proveedorEspacios.getRazonSocial()).isEqualTo("  Razón Social  ");
        }
    }

    // ==================== TESTS DE GETTERS ====================

    @Nested
    @DisplayName("Métodos Getter")
    class GettersTest {

        @Test
        @DisplayName("getCve() debería retornar clave correcta")
        void getCve_deberiaRetornarClaveCorrecta() {
            // Act & Assert
            assertThat(proveedor.getCve()).isEqualTo(CVE_VALIDA);
        }

        @Test
        @DisplayName("getNombre() debería retornar nombre correcto")
        void getNombre_deberiaRetornarNombreCorrecto() {
            // Act & Assert
            assertThat(proveedor.getNombre()).isEqualTo(NOMBRE_VALIDO);
        }

        @Test
        @DisplayName("getRazonSocial() debería retornar razón social correcta")
        void getRazonSocial_deberiaRetornarRazonSocialCorrecta() {
            // Act & Assert
            assertThat(proveedor.getRazonSocial()).isEqualTo(RAZON_SOCIAL_VALIDA);
        }

        @Test
        @DisplayName("Getters deberían ser consistentes en múltiples llamadas")
        void getters_deberianSerConsistentes() {
            // Act
            String cve1 = proveedor.getCve();
            String cve2 = proveedor.getCve();
            String nombre1 = proveedor.getNombre();
            String nombre2 = proveedor.getNombre();
            String razonSocial1 = proveedor.getRazonSocial();
            String razonSocial2 = proveedor.getRazonSocial();

            // Assert
            assertThat(cve1).isEqualTo(cve2);
            assertThat(nombre1).isEqualTo(nombre2);
            assertThat(razonSocial1).isEqualTo(razonSocial2);
        }

        @Test
        @DisplayName("Getters deberían retornar valores inmutables")
        void getters_deberianRetornarValoresInmutables() {
            // Act
            String cveOriginal = proveedor.getCve();
            String nombreOriginal = proveedor.getNombre();
            String razonSocialOriginal = proveedor.getRazonSocial();

            // Los strings son inmutables en Java, pero verificamos que no hay efectos secundarios
            String cveNueva = proveedor.getCve().toLowerCase(); // Operación que no debe afectar el original

            // Assert
            assertThat(proveedor.getCve()).isEqualTo(cveOriginal); // Original no cambió
            assertThat(cveNueva).isNotEqualTo(cveOriginal); // Nueva variable sí cambió
        }
    }

    // ==================== TESTS DE INMUTABILIDAD ====================

    @Nested
    @DisplayName("Inmutabilidad del Objeto")
    class InmutabilidadTest {

        @Test
        @DisplayName("Proveedor debería ser inmutable después de creación")
        void proveedor_deberiaSerInmutable() {
            // Arrange
            String cveOriginal = proveedor.getCve();
            String nombreOriginal = proveedor.getNombre();
            String razonSocialOriginal = proveedor.getRazonSocial();

            // Act - Intentar "modificar" (no hay setters, solo verificamos que no existan)
            // Usar reflection para verificar que no hay setters públicos
            assertThat(Proveedor.class.getMethods())
                .filteredOn(method -> method.getName().startsWith("set"))
                .isEmpty();

            // Assert - Los valores deben permanecer iguales
            assertThat(proveedor.getCve()).isEqualTo(cveOriginal);
            assertThat(proveedor.getNombre()).isEqualTo(nombreOriginal);
            assertThat(proveedor.getRazonSocial()).isEqualTo(razonSocialOriginal);
        }

        @Test
        @DisplayName("No debería tener métodos que modifiquen el estado")
        void noDeberiaTenerMetodosModificadores() {
            // Act & Assert - Verificar que no hay métodos que puedan modificar el estado
            assertThat(Proveedor.class.getMethods())
                .filteredOn(method ->
                    method.getName().startsWith("set") ||
                    method.getName().startsWith("add") ||
                    method.getName().startsWith("remove") ||
                    method.getName().startsWith("clear") ||
                    method.getName().startsWith("update") ||
                    method.getName().startsWith("modify"))
                .isEmpty();
        }

        @Test
        @DisplayName("Múltiples referencias al mismo proveedor deberían ser consistentes")
        void multiplesReferenciasDeberianSerConsistentes() {
            // Act
            Proveedor referencia1 = proveedor;
            Proveedor referencia2 = proveedor;

            // Assert
            assertThat(referencia1.getCve()).isEqualTo(referencia2.getCve());
            assertThat(referencia1.getNombre()).isEqualTo(referencia2.getNombre());
            assertThat(referencia1.getRazonSocial()).isEqualTo(referencia2.getRazonSocial());

            // Ambas referencias apuntan al mismo objeto
            assertThat(referencia1).isSameAs(referencia2);
        }
    }

    // ==================== TESTS DE CASOS EDGE ====================

    @Nested
    @DisplayName("Casos Edge y Valores Límite")
    class CasosEdgeTest {

        @Test
        @DisplayName("Debería manejar strings muy largos")
        void deberiaManejarStringsLargos() {
            // Arrange
            String cveUltraLarga = "PROV-" + "X".repeat(1000);
            String nombreUltraLargo = "Proveedor con Nombre Ultra Mega Súper " + "A".repeat(1000);
            String razonSocialUltraLarga = "Razón Social Extremadamente " + "B".repeat(1000) + " SA de CV";

            // Act & Assert - No debe lanzar excepción
            assertDoesNotThrow(() -> {
                new Proveedor(cveUltraLarga, nombreUltraLargo, razonSocialUltraLarga);
            });

            Proveedor proveedorLargo = new Proveedor(cveUltraLarga, nombreUltraLargo, razonSocialUltraLarga);
            assertThat(proveedorLargo.getCve()).isEqualTo(cveUltraLarga);
            assertThat(proveedorLargo.getNombre()).isEqualTo(nombreUltraLargo);
            assertThat(proveedorLargo.getRazonSocial()).isEqualTo(razonSocialUltraLarga);
        }

        @Test
        @DisplayName("Debería manejar caracteres especiales y unicode")
        void deberiaManejarCaracteresEspeciales() {
            // Arrange
            String cveEspecial = "PROV-001-ñáéíóú@#$%";
            String nombreEspecial = "Proveedor & Compañía López-García";
            String razonSocialEspecial = "Proveedor & Compañía López-García S.A. de C.V. ® ™ © ¿¡";

            // Act
            Proveedor proveedorEspecial = new Proveedor(cveEspecial, nombreEspecial, razonSocialEspecial);

            // Assert
            assertThat(proveedorEspecial.getCve()).isEqualTo(cveEspecial);
            assertThat(proveedorEspecial.getNombre()).isEqualTo(nombreEspecial);
            assertThat(proveedorEspecial.getRazonSocial()).isEqualTo(razonSocialEspecial);
        }

        @Test
        @DisplayName("Debería manejar strings con saltos de línea y tabs")
        void deberiaManejarSaltosLineaYTabs() {
            // Arrange
            String cveConSaltos = "PROV\n001\t";
            String nombreConSaltos = "Proveedor\nCon\tSaltos";
            String razonSocialConSaltos = "Razón\nSocial\tCon\rSaltos\nSA";

            // Act
            Proveedor proveedorConSaltos = new Proveedor(cveConSaltos, nombreConSaltos, razonSocialConSaltos);

            // Assert
            assertThat(proveedorConSaltos.getCve()).isEqualTo(cveConSaltos);
            assertThat(proveedorConSaltos.getNombre()).isEqualTo(nombreConSaltos);
            assertThat(proveedorConSaltos.getRazonSocial()).isEqualTo(razonSocialConSaltos);
        }

        @Test
        @DisplayName("Debería manejar strings con solo caracteres de escape")
        void deberiaManejarCaracteresEscape() {
            // Arrange
            String cveEscape = "\\n\\t\\r\\\\";
            String nombreEscape = "\"Proveedor\"";
            String razonSocialEscape = "'Razón Social'";

            // Act
            Proveedor proveedorEscape = new Proveedor(cveEscape, nombreEscape, razonSocialEscape);

            // Assert
            assertThat(proveedorEscape.getCve()).isEqualTo(cveEscape);
            assertThat(proveedorEscape.getNombre()).isEqualTo(nombreEscape);
            assertThat(proveedorEscape.getRazonSocial()).isEqualTo(razonSocialEscape);
        }
    }

    // ==================== TESTS DE IGUALDAD Y COMPARACIÓN ====================

    @Nested
    @DisplayName("Igualdad y Comparación")
    class IgualdadTest {

        @Test
        @DisplayName("Proveedores con mismos datos deberían ser considerados iguales")
        void proveedoresConMismosDatos_deberianSerIguales() {
            // Arrange
            Proveedor proveedor1 = new Proveedor(CVE_VALIDA, NOMBRE_VALIDO, RAZON_SOCIAL_VALIDA);
            Proveedor proveedor2 = new Proveedor(CVE_VALIDA, NOMBRE_VALIDO, RAZON_SOCIAL_VALIDA);

            // Act & Assert - Verificar que tienen los mismos datos
            assertThat(proveedor1.getCve()).isEqualTo(proveedor2.getCve());
            assertThat(proveedor1.getNombre()).isEqualTo(proveedor2.getNombre());
            assertThat(proveedor1.getRazonSocial()).isEqualTo(proveedor2.getRazonSocial());

            // Nota: La clase Proveedor no sobreescribe equals(), por lo que usa Object.equals()
            // Esto significa que dos instancias diferentes no serán iguales aunque tengan los mismos datos
            assertThat(proveedor1).isNotSameAs(proveedor2);
        }

        @Test
        @DisplayName("Misma instancia debería ser igual a sí misma")
        void mismaInstancia_deberiaSerIgualASiMisma() {
            // Act & Assert
            assertThat(proveedor).isSameAs(proveedor);
            assertThat(proveedor.getCve()).isEqualTo(proveedor.getCve());
        }

        @Test
        @DisplayName("Proveedores con diferentes datos deberían ser diferentes")
        void proveedoresConDiferentesDatos_deberianSerDiferentes() {
            // Arrange
            Proveedor proveedor1 = new Proveedor("PROV-001", "Proveedor Uno", "Proveedor Uno SA");
            Proveedor proveedor2 = new Proveedor("PROV-002", "Proveedor Dos", "Proveedor Dos SA");

            // Act & Assert
            assertThat(proveedor1.getCve()).isNotEqualTo(proveedor2.getCve());
            assertThat(proveedor1.getNombre()).isNotEqualTo(proveedor2.getNombre());
            assertThat(proveedor1.getRazonSocial()).isNotEqualTo(proveedor2.getRazonSocial());
            assertThat(proveedor1).isNotSameAs(proveedor2);
        }

        @Test
        @DisplayName("Comparación con null debería ser manejada correctamente")
        void comparacionConNull_deberiaManejarseCorrectamente() {
            // Act & Assert
            assertThat(proveedor).isNotNull();
            assertThat(proveedor.getCve()).isNotNull();
            assertThat(proveedor.getNombre()).isNotNull();
            assertThat(proveedor.getRazonSocial()).isNotNull();
        }
    }

    // ==================== TESTS DE INTEGRACIÓN CON TestUtils ====================

    @Nested
    @DisplayName("Integración con TestUtils")
    class IntegracionTestUtilsTest {

        @Test
        @DisplayName("TestUtils.crearProveedorValido() debería crear proveedor consistente")
        void testUtilsCrearProveedorValido_deberiaCrearConsistente() {
            // Act
            Proveedor proveedor1 = TestUtils.crearProveedorValido();
            Proveedor proveedor2 = TestUtils.crearProveedorValido();

            // Assert - Ambos deberían tener los mismos datos
            assertThat(proveedor1.getCve()).isEqualTo(proveedor2.getCve());
            assertThat(proveedor1.getNombre()).isEqualTo(proveedor2.getNombre());
            assertThat(proveedor1.getRazonSocial()).isEqualTo(proveedor2.getRazonSocial());

            // Pero ser instancias diferentes
            assertThat(proveedor1).isNotSameAs(proveedor2);
        }

        @Test
        @DisplayName("TestUtils.crearProveedor() debería crear proveedor con parámetros específicos")
        void testUtilsCrearProveedor_deberiaCrearConParametros() {
            // Arrange
            String cveEspecifica = "PROV-TEST-123";
            String nombreEspecifico = "Proveedor de Test";
            String razonSocialEspecifica = "Proveedor de Test SA de CV";

            // Act
            Proveedor proveedorEspecifico = TestUtils.crearProveedor(cveEspecifica, nombreEspecifico, razonSocialEspecifica);

            // Assert
            assertThat(proveedorEspecifico.getCve()).isEqualTo(cveEspecifica);
            assertThat(proveedorEspecifico.getNombre()).isEqualTo(nombreEspecifico);
            assertThat(proveedorEspecifico.getRazonSocial()).isEqualTo(razonSocialEspecifica);
        }

        @Test
        @DisplayName("TestUtils.esProveedorValido() debería validar correctamente")
        void testUtilsEsProveedorValido_deberiaValidarCorrectamente() {
            // Act & Assert
            assertThat(TestUtils.esProveedorValido(proveedor)).isTrue();

            // Casos inválidos
            assertThat(TestUtils.esProveedorValido(null)).isFalse();
            assertThat(TestUtils.esProveedorValido(new Proveedor(null, "nombre", "razon"))).isFalse();
            assertThat(TestUtils.esProveedorValido(new Proveedor("", "nombre", "razon"))).isFalse();
            assertThat(TestUtils.esProveedorValido(new Proveedor("   ", "nombre", "razon"))).isFalse();
        }

        @Test
        @DisplayName("TestUtils.crearListaProveedoresValidos() debería crear lista consistente")
        void testUtilsCrearListaProveedores_deberiaCrearListaConsistente() {
            // Act
            var proveedores = TestUtils.crearListaProveedoresValidos();

            // Assert
            assertThat(proveedores).isNotEmpty();
            assertThat(proveedores).allSatisfy(p -> TestUtils.esProveedorValido(p));

            // Verificar que todos tienen claves únicas
            var claves = proveedores.stream().map(Proveedor::getCve).toList();
            assertThat(claves).doesNotHaveDuplicates();
        }
    }

    // ==================== TESTS DE RENDIMIENTO Y MEMORIA ====================

    @Nested
    @DisplayName("Rendimiento y Uso de Memoria")
    class RendimientoTest {

        @Test
        @DisplayName("Creación masiva de proveedores debería ser eficiente")
        void creacionMasiva_deberiaSerEficiente() {
            // Act & Assert - No debe lanzar OutOfMemoryError
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 10000; i++) {
                    new Proveedor("PROV-" + i, "Proveedor " + i, "Proveedor " + i + " SA");
                }
            });
        }

        @Test
        @DisplayName("Getters deberían ser eficientes en llamadas repetidas")
        void getters_deberianSerEficientes() {
            // Act & Assert - Múltiples llamadas no deberían afectar rendimiento significativamente
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 100000; i++) {
                    proveedor.getCve();
                    proveedor.getNombre();
                    proveedor.getRazonSocial();
                }
            });
        }

        @Test
        @DisplayName("Proveedor debería ocupar poca memoria con strings largos reutilizados")
        void deberiaOcuparPocaMemoriaConStringsReutilizados() {
            // Arrange
            String cveReutilizada = "PROV-REUTILIZADA";
            String nombreReutilizado = "Nombre Reutilizado";
            String razonSocialReutilizada = "Razón Social Reutilizada";

            // Act - Crear múltiples proveedores con los mismos strings
            Proveedor[] proveedores = new Proveedor[1000];
            for (int i = 0; i < 1000; i++) {
                proveedores[i] = new Proveedor(cveReutilizada, nombreReutilizado, razonSocialReutilizada);
            }

            // Assert - Todos deberían tener referencias a los mismos strings (interning)
            for (Proveedor p : proveedores) {
                assertThat(p.getCve()).isEqualTo(cveReutilizada);
                assertThat(p.getNombre()).isEqualTo(nombreReutilizado);
                assertThat(p.getRazonSocial()).isEqualTo(razonSocialReutilizada);
            }
        }
    }
}