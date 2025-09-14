package mx.com.qtx.cotizador.servicio.wrapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias exhaustivas para ProveedorEntityConverter.
 *
 * Esta clase valida todas las conversiones bidireccionales entre:
 * - mx.com.qtx.cotizador.dominio.pedidos.Proveedor (dominio)
 * - mx.com.qtx.cotizador.entidad.Proveedor (persistencia)
 *
 * Cobertura de pruebas:
 * ✅ Conversiones básicas domain ↔ entity
 * ✅ Manejo de valores nulos y campos vacíos
 * ✅ Valores por defecto y casos edge
 * ✅ Actualización de entidades existentes vs creación nuevas
 * ✅ Preservación de datos en conversiones bidireccionales
 *
 * @author Claude Code
 * @version 1.0
 */
@DisplayName("ProveedorEntityConverter - Pruebas de Conversión")
class ProveedorEntityConverterTest {

    // ==================== CONSTANTES PARA PRUEBAS ====================

    private static final String CVE_VALIDA = "PROV-001";
    private static final String NOMBRE_VALIDO = "Proveedor Test";
    private static final String RAZON_SOCIAL_VALIDA = "Proveedor Test SA de CV";

    private static final String CVE_VACIA = "";
    private static final String CVE_ESPACIOS = "   ";

    // ==================== OBJETOS DE PRUEBA ====================

    private mx.com.qtx.cotizador.dominio.pedidos.Proveedor proveedorDomainCompleto;
    private mx.com.qtx.cotizador.entidad.Proveedor proveedorEntityCompleta;

    @BeforeEach
    void setUp() {
        // Proveedor dominio completo
        proveedorDomainCompleto = new mx.com.qtx.cotizador.dominio.pedidos.Proveedor(
            CVE_VALIDA, NOMBRE_VALIDO, RAZON_SOCIAL_VALIDA);

        // Proveedor entidad completa
        proveedorEntityCompleta = new mx.com.qtx.cotizador.entidad.Proveedor();
        proveedorEntityCompleta.setCve(CVE_VALIDA);
        proveedorEntityCompleta.setNombre(NOMBRE_VALIDO);
        proveedorEntityCompleta.setRazonSocial(RAZON_SOCIAL_VALIDA);
    }

    // ==================== PRUEBAS DE CONVERSIÓN DOMAIN → ENTITY ====================

    @Nested
    @DisplayName("Conversión Domain → Entity")
    class ConversionDomainToEntity {

        @Test
        @DisplayName("Debería convertir proveedor dominio completo a entidad")
        void convertToEntity_deberiaConvertirProveedorCompletoCorrectamente() {
            // Act
            mx.com.qtx.cotizador.entidad.Proveedor resultado =
                ProveedorEntityConverter.convertToEntity(proveedorDomainCompleto, null);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCve()).isEqualTo(CVE_VALIDA);
            assertThat(resultado.getNombre()).isEqualTo(NOMBRE_VALIDO);
            assertThat(resultado.getRazonSocial()).isEqualTo(RAZON_SOCIAL_VALIDA);
        }

        @Test
        @DisplayName("Debería manejar proveedor dominio null")
        void convertToEntity_deberiaManejarProveedorDomainNull() {
            // Act
            mx.com.qtx.cotizador.entidad.Proveedor resultado =
                ProveedorEntityConverter.convertToEntity(null, null);

            // Assert
            assertThat(resultado).isNull();
        }

        @Test
        @DisplayName("Debería actualizar entidad existente en lugar de crear nueva")
        void convertToEntity_deberiaActualizarEntidadExistente() {
            // Arrange
            mx.com.qtx.cotizador.entidad.Proveedor entidadExistente =
                new mx.com.qtx.cotizador.entidad.Proveedor();
            entidadExistente.setCve("ORIGINAL");
            entidadExistente.setNombre("Nombre Original");
            entidadExistente.setRazonSocial("Razón Original");

            // Act
            mx.com.qtx.cotizador.entidad.Proveedor resultado =
                ProveedorEntityConverter.convertToEntity(proveedorDomainCompleto, entidadExistente);

            // Assert - Debe ser la misma instancia
            assertThat(resultado).isSameAs(entidadExistente);
            // Assert - Campos actualizados (excepto clave que no se modifica en actualizaciones)
            assertThat(resultado.getCve()).isEqualTo("ORIGINAL"); // No se modifica clave en actualizaciones
            assertThat(resultado.getNombre()).isEqualTo(NOMBRE_VALIDO);
            assertThat(resultado.getRazonSocial()).isEqualTo(RAZON_SOCIAL_VALIDA);
        }

        @Test
        @DisplayName("Debería establecer clave en entidad nueva (no existente)")
        void convertToEntity_deberiaEstablecerClaveEnEntidadNueva() {
            // Act
            mx.com.qtx.cotizador.entidad.Proveedor resultado =
                ProveedorEntityConverter.convertToEntity(proveedorDomainCompleto, null);

            // Assert
            assertThat(resultado.getCve()).isEqualTo(CVE_VALIDA);
        }
    }

    @Nested
    @DisplayName("Conversión New Entity")
    class ConversionNewEntity {

        @Test
        @DisplayName("Debería crear nueva entidad correctamente")
        void convertToNewEntity_deberiaCrearNuevaEntidadCorrectamente() {
            // Act
            mx.com.qtx.cotizador.entidad.Proveedor resultado =
                ProveedorEntityConverter.convertToNewEntity(proveedorDomainCompleto);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCve()).isEqualTo(CVE_VALIDA);
            assertThat(resultado.getNombre()).isEqualTo(NOMBRE_VALIDO);
            assertThat(resultado.getRazonSocial()).isEqualTo(RAZON_SOCIAL_VALIDA);
        }

        @Test
        @DisplayName("Debería manejar proveedor dominio null en convertToNewEntity")
        void convertToNewEntity_deberiaManejarProveedorDomainNull() {
            // Act
            mx.com.qtx.cotizador.entidad.Proveedor resultado =
                ProveedorEntityConverter.convertToNewEntity(null);

            // Assert
            assertThat(resultado).isNull();
        }
    }

    // ==================== PRUEBAS DE CONVERSIÓN ENTITY → DOMAIN ====================

    @Nested
    @DisplayName("Conversión Entity → Domain")
    class ConversionEntityToDomain {

        @Test
        @DisplayName("Debería convertir entidad completa a proveedor dominio")
        void convertToDomain_deberiaConvertirEntidadCompletaCorrectamente() {
            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor resultado =
                ProveedorEntityConverter.convertToDomain(proveedorEntityCompleta);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCve()).isEqualTo(CVE_VALIDA);
            assertThat(resultado.getNombre()).isEqualTo(NOMBRE_VALIDO);
            assertThat(resultado.getRazonSocial()).isEqualTo(RAZON_SOCIAL_VALIDA);
        }

        @Test
        @DisplayName("Debería manejar entidad null")
        void convertToDomain_deberiaManejarEntidadNull() {
            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor resultado =
                ProveedorEntityConverter.convertToDomain(null);

            // Assert
            assertThat(resultado).isNull();
        }

        @Test
        @DisplayName("Debería aplicar valor por defecto 'Sin nombre' cuando nombre es null")
        void convertToDomain_deberiaAplicarValorPorDefectoParaNombre() {
            // Arrange
            proveedorEntityCompleta.setNombre(null);

            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor resultado =
                ProveedorEntityConverter.convertToDomain(proveedorEntityCompleta);

            // Assert
            assertThat(resultado.getNombre()).isEqualTo("Sin nombre");
        }

        @Test
        @DisplayName("Debería usar nombre como razón social por defecto cuando razón social es null")
        void convertToDomain_deberiaUsarNombreComoRazonSocialPorDefecto() {
            // Arrange
            proveedorEntityCompleta.setRazonSocial(null);

            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor resultado =
                ProveedorEntityConverter.convertToDomain(proveedorEntityCompleta);

            // Assert
            assertThat(resultado.getRazonSocial()).isEqualTo(NOMBRE_VALIDO);
        }

        @Test
        @DisplayName("Debería manejar ambos campos null con valores por defecto")
        void convertToDomain_deberiaManejarAmbosNullConDefectos() {
            // Arrange
            proveedorEntityCompleta.setNombre(null);
            proveedorEntityCompleta.setRazonSocial(null);

            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor resultado =
                ProveedorEntityConverter.convertToDomain(proveedorEntityCompleta);

            // Assert
            assertThat(resultado.getNombre()).isEqualTo("Sin nombre");
            assertThat(resultado.getRazonSocial()).isEqualTo("Sin nombre");
        }
    }

    // ==================== PRUEBAS DE CASOS EDGE Y VALORES EXTREMOS ====================

    @Nested
    @DisplayName("Casos Edge y Valores Extremos")
    class CasosEdgeYValoresExtremos {

        @Test
        @DisplayName("Debería manejar strings vacíos correctamente")
        void deberiaManejarStringsVacios() {
            // Arrange
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor proveedorConVacios =
                new mx.com.qtx.cotizador.dominio.pedidos.Proveedor(CVE_VACIA, CVE_VACIA, CVE_VACIA);

            // Act - Domain to Entity
            mx.com.qtx.cotizador.entidad.Proveedor entidadResultado =
                ProveedorEntityConverter.convertToNewEntity(proveedorConVacios);

            // Assert
            assertThat(entidadResultado.getCve()).isEqualTo(CVE_VACIA);
            assertThat(entidadResultado.getNombre()).isEqualTo(CVE_VACIA);
            assertThat(entidadResultado.getRazonSocial()).isEqualTo(CVE_VACIA);

            // Act - Entity to Domain
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor domainResultado =
                ProveedorEntityConverter.convertToDomain(entidadResultado);

            // Assert - String vacío no es null, por lo que se mantiene el valor vacío (no se aplica "Sin nombre")
            assertThat(domainResultado.getCve()).isEqualTo(CVE_VACIA);
            assertThat(domainResultado.getNombre()).isEqualTo(CVE_VACIA); // Se mantiene string vacío
            assertThat(domainResultado.getRazonSocial()).isEqualTo(CVE_VACIA); // Se mantiene string vacío (hereda de nombre)
        }

        @Test
        @DisplayName("Debería manejar strings con solo espacios")
        void deberiaManejarStringsConSoloEspacios() {
            // Arrange
            mx.com.qtx.cotizador.entidad.Proveedor entidadConEspacios =
                new mx.com.qtx.cotizador.entidad.Proveedor();
            entidadConEspacios.setCve(CVE_ESPACIOS);
            entidadConEspacios.setNombre(CVE_ESPACIOS);
            entidadConEspacios.setRazonSocial(CVE_ESPACIOS);

            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor resultado =
                ProveedorEntityConverter.convertToDomain(entidadConEspacios);

            // Assert - Los espacios se mantienen, no se consideran null
            assertThat(resultado.getCve()).isEqualTo(CVE_ESPACIOS);
            assertThat(resultado.getNombre()).isEqualTo(CVE_ESPACIOS);
            assertThat(resultado.getRazonSocial()).isEqualTo(CVE_ESPACIOS);
        }

        @Test
        @DisplayName("Debería manejar strings muy largos")
        void deberiaManejarStringsLargos() {
            // Arrange
            String stringLargo = "A".repeat(1000);
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor proveedorConLargos =
                new mx.com.qtx.cotizador.dominio.pedidos.Proveedor(stringLargo, stringLargo, stringLargo);

            // Act - Domain to Entity and back
            mx.com.qtx.cotizador.entidad.Proveedor entidad =
                ProveedorEntityConverter.convertToNewEntity(proveedorConLargos);
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor resultado =
                ProveedorEntityConverter.convertToDomain(entidad);

            // Assert
            assertThat(resultado.getCve()).isEqualTo(stringLargo);
            assertThat(resultado.getNombre()).isEqualTo(stringLargo);
            assertThat(resultado.getRazonSocial()).isEqualTo(stringLargo);
        }
    }

    // ==================== PRUEBAS DE INTEGRIDAD BIDIRECCIONAL ====================

    @Nested
    @DisplayName("Integridad de Conversiones Bidireccionales")
    class IntegridadConversionesBidireccionales {

        @Test
        @DisplayName("Debería preservar datos en conversión Domain → Entity → Domain")
        void deberiaPreservarDatosEnConversionDomainEntityDomain() {
            // Act
            mx.com.qtx.cotizador.entidad.Proveedor entidad =
                ProveedorEntityConverter.convertToNewEntity(proveedorDomainCompleto);
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor resultado =
                ProveedorEntityConverter.convertToDomain(entidad);

            // Assert - Todos los datos deben preservarse
            assertThat(resultado.getCve()).isEqualTo(proveedorDomainCompleto.getCve());
            assertThat(resultado.getNombre()).isEqualTo(proveedorDomainCompleto.getNombre());
            assertThat(resultado.getRazonSocial()).isEqualTo(proveedorDomainCompleto.getRazonSocial());
        }

        @Test
        @DisplayName("Debería preservar datos en conversión Entity → Domain → Entity")
        void deberiaPreservarDatosEnConversionEntityDomainEntity() {
            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor domain =
                ProveedorEntityConverter.convertToDomain(proveedorEntityCompleta);
            mx.com.qtx.cotizador.entidad.Proveedor resultado =
                ProveedorEntityConverter.convertToNewEntity(domain);

            // Assert - Todos los datos deben preservarse
            assertThat(resultado.getCve()).isEqualTo(proveedorEntityCompleta.getCve());
            assertThat(resultado.getNombre()).isEqualTo(proveedorEntityCompleta.getNombre());
            assertThat(resultado.getRazonSocial()).isEqualTo(proveedorEntityCompleta.getRazonSocial());
        }

        @Test
        @DisplayName("Debería ser idempotente para múltiples conversiones")
        void deberiaSerIdempotentePaMultiplesConversiones() {
            // Act - Múltiples conversiones
            mx.com.qtx.cotizador.entidad.Proveedor entidad1 =
                ProveedorEntityConverter.convertToNewEntity(proveedorDomainCompleto);
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor domain1 =
                ProveedorEntityConverter.convertToDomain(entidad1);
            mx.com.qtx.cotizador.entidad.Proveedor entidad2 =
                ProveedorEntityConverter.convertToNewEntity(domain1);
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor domain2 =
                ProveedorEntityConverter.convertToDomain(entidad2);

            // Assert - Los resultados finales deben ser idénticos
            assertThat(domain2.getCve()).isEqualTo(domain1.getCve());
            assertThat(domain2.getNombre()).isEqualTo(domain1.getNombre());
            assertThat(domain2.getRazonSocial()).isEqualTo(domain1.getRazonSocial());

            assertThat(entidad2.getCve()).isEqualTo(entidad1.getCve());
            assertThat(entidad2.getNombre()).isEqualTo(entidad1.getNombre());
            assertThat(entidad2.getRazonSocial()).isEqualTo(entidad1.getRazonSocial());
        }
    }

    // ==================== PRUEBAS DE COMPORTAMIENTO ESPECÍFICO ====================

    @Nested
    @DisplayName("Comportamiento Específico del Converter")
    class ComportamientoEspecificoConverter {

        @Test
        @DisplayName("Debería crear diferentes instancias en convertToNewEntity")
        void deberiaCrearDiferentesInstanciasEnConvertToNewEntity() {
            // Act
            mx.com.qtx.cotizador.entidad.Proveedor entidad1 =
                ProveedorEntityConverter.convertToNewEntity(proveedorDomainCompleto);
            mx.com.qtx.cotizador.entidad.Proveedor entidad2 =
                ProveedorEntityConverter.convertToNewEntity(proveedorDomainCompleto);

            // Assert - Deben ser diferentes instancias pero con mismo contenido
            assertThat(entidad1).isNotSameAs(entidad2);
            assertThat(entidad1.getCve()).isEqualTo(entidad2.getCve());
            assertThat(entidad1.getNombre()).isEqualTo(entidad2.getNombre());
            assertThat(entidad1.getRazonSocial()).isEqualTo(entidad2.getRazonSocial());
        }

        @Test
        @DisplayName("Debería crear diferentes instancias de dominio en convertToDomain")
        void deberiaCrearDiferentesInstanciasEnConvertToDomain() {
            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor domain1 =
                ProveedorEntityConverter.convertToDomain(proveedorEntityCompleta);
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor domain2 =
                ProveedorEntityConverter.convertToDomain(proveedorEntityCompleta);

            // Assert - Deben ser diferentes instancias pero con mismo contenido
            assertThat(domain1).isNotSameAs(domain2);
            assertThat(domain1.getCve()).isEqualTo(domain2.getCve());
            assertThat(domain1.getNombre()).isEqualTo(domain2.getNombre());
            assertThat(domain1.getRazonSocial()).isEqualTo(domain2.getRazonSocial());
        }
    }
}