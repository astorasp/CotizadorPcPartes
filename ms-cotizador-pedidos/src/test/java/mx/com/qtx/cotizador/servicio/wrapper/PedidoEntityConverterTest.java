package mx.com.qtx.cotizador.servicio.wrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mx.com.qtx.cotizador.entidad.Componente;
import mx.com.qtx.cotizador.entidad.DetallePedido;
import mx.com.qtx.cotizador.repositorio.ComponenteRepositorio;
import mx.com.qtx.cotizador.repositorio.ProveedorRepositorio;

/**
 * Pruebas unitarias exhaustivas para PedidoEntityConverter.
 *
 * Esta clase valida todas las conversiones bidireccionales entre:
 * - mx.com.qtx.cotizador.dominio.pedidos.Pedido (dominio)
 * - mx.com.qtx.cotizador.entidad.Pedido (persistencia)
 *
 * El PedidoEntityConverter es el más complejo de los converters debido a:
 * ✅ Manejo de fechas LocalDate
 * ✅ Relaciones con Proveedor (requiere repositorio)
 * ✅ Relaciones con DetallePedido (IDs compuestos complejos)
 * ✅ Cálculos de totales y agregaciones
 * ✅ Múltiples métodos de conversión con diferentes propósitos
 * ✅ Manejo de excepciones y validaciones
 *
 * @author Claude Code
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoEntityConverter - Pruebas de Conversión Complejas")
class PedidoEntityConverterTest {

    // ==================== MOCKS ====================

    @Mock
    private ProveedorRepositorio proveedorRepositorio;

    @Mock
    private ComponenteRepositorio componenteRepositorio;

    // ==================== CONSTANTES PARA PRUEBAS ====================

    private static final String CVE_PROVEEDOR = "PROV-001";
    private static final String NOMBRE_PROVEEDOR = "Proveedor Test";
    private static final String RAZON_SOCIAL_PROVEEDOR = "Proveedor Test SA de CV";

    private static final String ID_COMPONENTE_1 = "COMP-001";
    private static final String DESC_COMPONENTE_1 = "Procesador Intel";
    private static final String ID_COMPONENTE_2 = "COMP-002";
    private static final String DESC_COMPONENTE_2 = "Memoria RAM 16GB";

    private static final LocalDate FECHA_EMISION = LocalDate.of(2024, 1, 15);
    private static final LocalDate FECHA_ENTREGA = LocalDate.of(2024, 1, 22);
    private static final int NIVEL_SURTIDO = 100;
    private static final BigDecimal TOTAL_PEDIDO = new BigDecimal("25000.00");

    private static final Integer NUM_PEDIDO = 12345;

    // ==================== OBJETOS DE PRUEBA ====================

    private mx.com.qtx.cotizador.dominio.pedidos.Proveedor proveedorDominio;
    private mx.com.qtx.cotizador.entidad.Proveedor proveedorEntity;
    private mx.com.qtx.cotizador.dominio.pedidos.Pedido pedidoDominioCompleto;
    private mx.com.qtx.cotizador.entidad.Pedido pedidoEntityCompleta;
    private Componente componenteEntity1;
    private Componente componenteEntity2;

    @BeforeEach
    void setUp() {
        // Crear proveedor dominio
        proveedorDominio = new mx.com.qtx.cotizador.dominio.pedidos.Proveedor(
            CVE_PROVEEDOR, NOMBRE_PROVEEDOR, RAZON_SOCIAL_PROVEEDOR);

        // Crear proveedor entity
        proveedorEntity = new mx.com.qtx.cotizador.entidad.Proveedor();
        proveedorEntity.setCve(CVE_PROVEEDOR);
        proveedorEntity.setNombre(NOMBRE_PROVEEDOR);
        proveedorEntity.setRazonSocial(RAZON_SOCIAL_PROVEEDOR);

        // Crear pedido dominio completo
        pedidoDominioCompleto = new mx.com.qtx.cotizador.dominio.pedidos.Pedido(
            NUM_PEDIDO.longValue(), FECHA_EMISION, FECHA_ENTREGA, NIVEL_SURTIDO, proveedorDominio);

        // Agregar detalles al pedido dominio
        pedidoDominioCompleto.agregarDetallePedido(
            ID_COMPONENTE_1, DESC_COMPONENTE_1, 2, new BigDecimal("8000.00"), new BigDecimal("16000.00"));
        pedidoDominioCompleto.agregarDetallePedido(
            ID_COMPONENTE_2, DESC_COMPONENTE_2, 1, new BigDecimal("9000.00"), new BigDecimal("9000.00"));

        // Crear componentes entity
        componenteEntity1 = new Componente();
        componenteEntity1.setId(ID_COMPONENTE_1);
        componenteEntity1.setDescripcion(DESC_COMPONENTE_1);

        componenteEntity2 = new Componente();
        componenteEntity2.setId(ID_COMPONENTE_2);
        componenteEntity2.setDescripcion(DESC_COMPONENTE_2);

        // Crear pedido entity completa
        pedidoEntityCompleta = new mx.com.qtx.cotizador.entidad.Pedido();
        pedidoEntityCompleta.setNumPedido(NUM_PEDIDO);
        pedidoEntityCompleta.setFechaEmision(FECHA_EMISION);
        pedidoEntityCompleta.setFechaEntrega(FECHA_ENTREGA);
        pedidoEntityCompleta.setNivelSurtido(NIVEL_SURTIDO);
        pedidoEntityCompleta.setProveedor(proveedorEntity);
        pedidoEntityCompleta.setTotal(TOTAL_PEDIDO);

        // Agregar detalles a pedido entity
        DetallePedido detalle1 = new DetallePedido();
        DetallePedido.DetallePedidoId id1 = new DetallePedido.DetallePedidoId();
        id1.setIdPedido(NUM_PEDIDO);
        id1.setNumDetalle(1);
        detalle1.setId(id1);
        detalle1.setCantidad(2);
        detalle1.setPrecioUnitario(new BigDecimal("8000.00"));
        detalle1.setTotalCotizado(new BigDecimal("16000.00"));
        detalle1.setComponente(componenteEntity1);

        DetallePedido detalle2 = new DetallePedido();
        DetallePedido.DetallePedidoId id2 = new DetallePedido.DetallePedidoId();
        id2.setIdPedido(NUM_PEDIDO);
        id2.setNumDetalle(2);
        detalle2.setId(id2);
        detalle2.setCantidad(1);
        detalle2.setPrecioUnitario(new BigDecimal("9000.00"));
        detalle2.setTotalCotizado(new BigDecimal("9000.00"));
        detalle2.setComponente(componenteEntity2);

        pedidoEntityCompleta.addDetalle(detalle1);
        pedidoEntityCompleta.addDetalle(detalle2);
    }

    // ==================== PRUEBAS DE CONVERSIÓN DOMAIN → ENTITY ====================

    @Nested
    @DisplayName("Conversión Domain → Entity")
    class ConversionDomainToEntity {

        @Test
        @DisplayName("Debería convertir pedido dominio completo a entidad")
        void convertToEntity_deberiaConvertirPedidoCompletoCorrectamente() {
            // Arrange
            when(proveedorRepositorio.findByCve(CVE_PROVEEDOR)).thenReturn(proveedorEntity);

            // Act
            mx.com.qtx.cotizador.entidad.Pedido resultado =
                PedidoEntityConverter.convertToEntity(pedidoDominioCompleto, proveedorRepositorio);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getFechaEmision()).isEqualTo(FECHA_EMISION);
            assertThat(resultado.getFechaEntrega()).isEqualTo(FECHA_ENTREGA);
            assertThat(resultado.getNivelSurtido()).isEqualTo(NIVEL_SURTIDO);
            assertThat(resultado.getProveedor()).isSameAs(proveedorEntity);
            assertThat(resultado.getTotal()).isEqualTo(TOTAL_PEDIDO);

            // Verificar interacción con repositorio
            verify(proveedorRepositorio).findByCve(CVE_PROVEEDOR);
        }

        @Test
        @DisplayName("Debería manejar pedido dominio null")
        void convertToEntity_deberiaManejarPedidoDomainNull() {
            // Act
            mx.com.qtx.cotizador.entidad.Pedido resultado =
                PedidoEntityConverter.convertToEntity(null, proveedorRepositorio);

            // Assert
            assertThat(resultado).isNull();
            verify(proveedorRepositorio, never()).findByCve(any());
        }

        @Test
        @DisplayName("Debería manejar fechas null con valores por defecto")
        void convertToEntity_deberiaManejarFechasNull() {
            // Arrange
            mx.com.qtx.cotizador.dominio.pedidos.Pedido pedidoSinFechas =
                new mx.com.qtx.cotizador.dominio.pedidos.Pedido(
                    NUM_PEDIDO.longValue(), null, null, NIVEL_SURTIDO, proveedorDominio);

            // Act
            mx.com.qtx.cotizador.entidad.Pedido resultado =
                PedidoEntityConverter.convertToEntity(pedidoSinFechas, null);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getFechaEmision()).isNull();
            assertThat(resultado.getFechaEntrega()).isNull();
        }

        @Test
        @DisplayName("Debería manejar proveedor null en pedido dominio")
        void convertToEntity_deberiaManejarProveedorNull() {
            // Arrange
            mx.com.qtx.cotizador.dominio.pedidos.Pedido pedidoSinProveedor =
                new mx.com.qtx.cotizador.dominio.pedidos.Pedido(
                    NUM_PEDIDO.longValue(), FECHA_EMISION, FECHA_ENTREGA, NIVEL_SURTIDO, null);

            // Act
            mx.com.qtx.cotizador.entidad.Pedido resultado =
                PedidoEntityConverter.convertToEntity(pedidoSinProveedor, proveedorRepositorio);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getProveedor()).isNull();
            verify(proveedorRepositorio, never()).findByCve(any());
        }

        @Test
        @DisplayName("Debería manejar repositorio proveedor null")
        void convertToEntity_deberiaManejarRepositorioProveedorNull() {
            // Act
            mx.com.qtx.cotizador.entidad.Pedido resultado =
                PedidoEntityConverter.convertToEntity(pedidoDominioCompleto, null);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getProveedor()).isNull();
        }

        @Test
        @DisplayName("Debería manejar proveedor no encontrado en repositorio")
        void convertToEntity_deberiaManejarProveedorNoEncontrado() {
            // Arrange
            when(proveedorRepositorio.findByCve(CVE_PROVEEDOR)).thenReturn(null);

            // Act
            mx.com.qtx.cotizador.entidad.Pedido resultado =
                PedidoEntityConverter.convertToEntity(pedidoDominioCompleto, proveedorRepositorio);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getProveedor()).isNull();
            verify(proveedorRepositorio).findByCve(CVE_PROVEEDOR);
        }
    }

    // ==================== PRUEBAS DE ADICIÓN DE DETALLES ====================

    @Nested
    @DisplayName("Adición de Detalles a Entidad")
    class AdicionDetallesAEntidad {

        private mx.com.qtx.cotizador.entidad.Pedido pedidoEntityVacia;

        @BeforeEach
        void setUpDetalles() {
            pedidoEntityVacia = new mx.com.qtx.cotizador.entidad.Pedido();
            pedidoEntityVacia.setNumPedido(NUM_PEDIDO);
        }

        @Test
        @DisplayName("Debería agregar detalles correctamente a entidad existente")
        void addDetallesTo_deberiaAgregarDetallesCorrectamente() {
            // Arrange
            when(componenteRepositorio.findById(ID_COMPONENTE_1)).thenReturn(Optional.of(componenteEntity1));
            when(componenteRepositorio.findById(ID_COMPONENTE_2)).thenReturn(Optional.of(componenteEntity2));

            // Act
            PedidoEntityConverter.addDetallesTo(pedidoDominioCompleto, pedidoEntityVacia, componenteRepositorio);

            // Assert
            assertThat(pedidoEntityVacia.getDetalles()).hasSize(2);

            DetallePedido detalle1 = pedidoEntityVacia.getDetalles().get(0);
            assertThat(detalle1.getId().getIdPedido()).isEqualTo(NUM_PEDIDO);
            assertThat(detalle1.getId().getNumDetalle()).isEqualTo(1);
            assertThat(detalle1.getCantidad()).isEqualTo(2);
            assertThat(detalle1.getPrecioUnitario()).isEqualByComparingTo(new BigDecimal("8000.00"));
            assertThat(detalle1.getTotalCotizado()).isEqualByComparingTo(new BigDecimal("16000.00"));
            assertThat(detalle1.getComponente()).isSameAs(componenteEntity1);

            DetallePedido detalle2 = pedidoEntityVacia.getDetalles().get(1);
            assertThat(detalle2.getId().getIdPedido()).isEqualTo(NUM_PEDIDO);
            assertThat(detalle2.getId().getNumDetalle()).isEqualTo(2);
            assertThat(detalle2.getCantidad()).isEqualTo(1);
            assertThat(detalle2.getPrecioUnitario()).isEqualByComparingTo(new BigDecimal("9000.00"));
            assertThat(detalle2.getTotalCotizado()).isEqualByComparingTo(new BigDecimal("9000.00"));
            assertThat(detalle2.getComponente()).isSameAs(componenteEntity2);

            // Verificar interacciones con repositorio
            verify(componenteRepositorio).findById(ID_COMPONENTE_1);
            verify(componenteRepositorio).findById(ID_COMPONENTE_2);
        }

        @Test
        @DisplayName("Debería limpiar detalles existentes antes de agregar nuevos")
        void addDetallesTo_deberiaLimpiarDetallesExistentes() {
            // Arrange - Agregar detalle existente
            DetallePedido detalleExistente = new DetallePedido();
            pedidoEntityVacia.addDetalle(detalleExistente);
            assertThat(pedidoEntityVacia.getDetalles()).hasSize(1);

            when(componenteRepositorio.findById(any())).thenReturn(Optional.of(componenteEntity1));

            // Act
            PedidoEntityConverter.addDetallesTo(pedidoDominioCompleto, pedidoEntityVacia, componenteRepositorio);

            // Assert - Debe tener solo los nuevos detalles
            assertThat(pedidoEntityVacia.getDetalles()).hasSize(2);
            assertThat(pedidoEntityVacia.getDetalles()).doesNotContain(detalleExistente);
        }

        @Test
        @DisplayName("Debería manejar componente no encontrado en repositorio")
        void addDetallesTo_deberiaManejarComponenteNoEncontrado() {
            // Arrange
            when(componenteRepositorio.findById(ID_COMPONENTE_1)).thenReturn(Optional.empty());
            when(componenteRepositorio.findById(ID_COMPONENTE_2)).thenReturn(Optional.of(componenteEntity2));

            // Act
            PedidoEntityConverter.addDetallesTo(pedidoDominioCompleto, pedidoEntityVacia, componenteRepositorio);

            // Assert
            assertThat(pedidoEntityVacia.getDetalles()).hasSize(2);

            DetallePedido detalle1 = pedidoEntityVacia.getDetalles().get(0);
            assertThat(detalle1.getComponente()).isNull(); // Componente no encontrado

            DetallePedido detalle2 = pedidoEntityVacia.getDetalles().get(1);
            assertThat(detalle2.getComponente()).isSameAs(componenteEntity2);
        }

        @Test
        @DisplayName("Debería manejar repositorio componentes null")
        void addDetallesTo_deberiaManejarRepositorioNull() {
            // Act
            PedidoEntityConverter.addDetallesTo(pedidoDominioCompleto, pedidoEntityVacia, null);

            // Assert - Detalles se crean pero sin componentes
            assertThat(pedidoEntityVacia.getDetalles()).hasSize(2);

            for (DetallePedido detalle : pedidoEntityVacia.getDetalles()) {
                assertThat(detalle.getComponente()).isNull();
            }
        }

        @Test
        @DisplayName("Debería manejar pedido dominio null")
        void addDetallesTo_deberiaManejarPedidoDomainNull() {
            // Act
            PedidoEntityConverter.addDetallesTo(null, pedidoEntityVacia, componenteRepositorio);

            // Assert
            assertThat(pedidoEntityVacia.getDetalles()).isEmpty();
            verify(componenteRepositorio, never()).findById(any());
        }

        @Test
        @DisplayName("Debería manejar pedido entity null")
        void addDetallesTo_deberiaManejarPedidoEntityNull() {
            // Act - No debería lanzar excepción
            PedidoEntityConverter.addDetallesTo(pedidoDominioCompleto, null, componenteRepositorio);

            // Verify - No debería interactuar con repositorio
            verify(componenteRepositorio, never()).findById(any());
        }
    }

    // ==================== PRUEBAS DE CONVERSIÓN NEW ENTITY ====================

    @Nested
    @DisplayName("Conversión a Nueva Entidad")
    class ConversionNuevaEntidad {

        @Test
        @DisplayName("Debería crear nueva entidad correctamente")
        void convertToNewEntity_deberiaCrearNuevaEntidadCorrectamente() {
            // Arrange
            when(proveedorRepositorio.findByCve(CVE_PROVEEDOR)).thenReturn(proveedorEntity);

            // Act
            mx.com.qtx.cotizador.entidad.Pedido resultado =
                PedidoEntityConverter.convertToNewEntity(pedidoDominioCompleto, proveedorRepositorio, componenteRepositorio);

            // Assert - Debe tener los datos básicos pero no los detalles (comentario en método dice que no se pueden agregar sin ID)
            assertThat(resultado).isNotNull();
            assertThat(resultado.getFechaEmision()).isEqualTo(FECHA_EMISION);
            assertThat(resultado.getFechaEntrega()).isEqualTo(FECHA_ENTREGA);
            assertThat(resultado.getNivelSurtido()).isEqualTo(NIVEL_SURTIDO);
            assertThat(resultado.getProveedor()).isSameAs(proveedorEntity);
            assertThat(resultado.getTotal()).isEqualTo(TOTAL_PEDIDO);
        }

        @Test
        @DisplayName("Debería manejar pedido dominio null en convertToNewEntity")
        void convertToNewEntity_deberiaManejarPedidoDomainNull() {
            // Act
            mx.com.qtx.cotizador.entidad.Pedido resultado =
                PedidoEntityConverter.convertToNewEntity(null, proveedorRepositorio, componenteRepositorio);

            // Assert
            assertThat(resultado).isNull();
        }
    }

    // ==================== PRUEBAS DE CONVERSIÓN ENTITY → DOMAIN ====================

    @Nested
    @DisplayName("Conversión Entity → Domain")
    class ConversionEntityToDomain {

        @Test
        @DisplayName("Debería convertir entidad completa a pedido dominio")
        void convertToDomain_deberiaConvertirEntidadCompletaCorrectamente() {
            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Pedido resultado =
                PedidoEntityConverter.convertToDomain(pedidoEntityCompleta);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getNumPedido()).isEqualTo(NUM_PEDIDO.longValue());
            assertThat(resultado.getFechaEmision()).isEqualTo(FECHA_EMISION);
            assertThat(resultado.getFechaEntrega()).isEqualTo(FECHA_ENTREGA);
            assertThat(resultado.getNivelSurtido()).isEqualTo(NIVEL_SURTIDO);

            // Verificar proveedor
            assertThat(resultado.getProveedor()).isNotNull();
            assertThat(resultado.getProveedor().getCve()).isEqualTo(CVE_PROVEEDOR);
            assertThat(resultado.getProveedor().getNombre()).isEqualTo(NOMBRE_PROVEEDOR);
            assertThat(resultado.getProveedor().getRazonSocial()).isEqualTo(RAZON_SOCIAL_PROVEEDOR);

            // Verificar detalles
            assertThat(resultado.getDetallesPedido()).hasSize(2);
        }

        @Test
        @DisplayName("Debería manejar entidad null")
        void convertToDomain_deberiaManejarEntidadNull() {
            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Pedido resultado =
                PedidoEntityConverter.convertToDomain(null);

            // Assert
            assertThat(resultado).isNull();
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando entidad no tiene número de pedido")
        void convertToDomain_deberiaLanzarExcepcionSinNumeroPedido() {
            // Arrange
            pedidoEntityCompleta.setNumPedido(null);

            // Act & Assert
            assertThatThrownBy(() -> PedidoEntityConverter.convertToDomain(pedidoEntityCompleta))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("número de pedido asignado");
        }

        @Test
        @DisplayName("Debería aplicar valores por defecto para fechas null")
        void convertToDomain_deberiaAplicarValoresPorDefectoFechas() {
            // Arrange
            pedidoEntityCompleta.setFechaEmision(null);
            pedidoEntityCompleta.setFechaEntrega(null);

            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Pedido resultado =
                PedidoEntityConverter.convertToDomain(pedidoEntityCompleta);

            // Assert
            assertThat(resultado.getFechaEmision()).isEqualTo(LocalDate.now());
            assertThat(resultado.getFechaEntrega()).isEqualTo(LocalDate.now().plusDays(7));
        }

        @Test
        @DisplayName("Debería aplicar valor por defecto para nivel surtido null")
        void convertToDomain_deberiaAplicarValorPorDefectoNivelSurtido() {
            // Arrange
            pedidoEntityCompleta.setNivelSurtido(null);

            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Pedido resultado =
                PedidoEntityConverter.convertToDomain(pedidoEntityCompleta);

            // Assert
            assertThat(resultado.getNivelSurtido()).isEqualTo(0);
        }

        @Test
        @DisplayName("Debería manejar proveedor null en entidad")
        void convertToDomain_deberiaManejarProveedorNullEnEntidad() {
            // Arrange
            pedidoEntityCompleta.setProveedor(null);

            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Pedido resultado =
                PedidoEntityConverter.convertToDomain(pedidoEntityCompleta);

            // Assert
            assertThat(resultado.getProveedor()).isNull();
        }

        @Test
        @DisplayName("Debería aplicar valores por defecto en proveedor con campos null")
        void convertToDomain_deberiaAplicarDefectosProveedorCamposNull() {
            // Arrange
            proveedorEntity.setNombre(null);
            proveedorEntity.setRazonSocial(null);

            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Pedido resultado =
                PedidoEntityConverter.convertToDomain(pedidoEntityCompleta);

            // Assert
            assertThat(resultado.getProveedor()).isNotNull();
            assertThat(resultado.getProveedor().getNombre()).isEqualTo("Sin nombre");
            assertThat(resultado.getProveedor().getRazonSocial()).isEqualTo("Sin nombre");
        }

        @Test
        @DisplayName("Debería convertir detalles correctamente")
        void convertToDomain_deberiaConvertirDetallesCorrectamente() {
            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Pedido resultado =
                PedidoEntityConverter.convertToDomain(pedidoEntityCompleta);

            // Assert
            assertThat(resultado.getDetallesPedido()).hasSize(2);

            mx.com.qtx.cotizador.dominio.pedidos.DetallePedido detalle1 = resultado.getDetallesPedido().get(0);
            assertThat(detalle1.getIdArticulo()).isEqualTo(ID_COMPONENTE_1);
            assertThat(detalle1.getDescripcion()).isEqualTo(DESC_COMPONENTE_1);
            assertThat(detalle1.getCantidad()).isEqualTo(2);
            assertThat(detalle1.getPrecioUnitario()).isEqualByComparingTo(new BigDecimal("8000.00"));
            assertThat(detalle1.getTotalCotizado()).isEqualByComparingTo(new BigDecimal("16000.00"));
        }

        @Test
        @DisplayName("Debería manejar detalles con componente null")
        void convertToDomain_deberiaManejarDetallesComponenteNull() {
            // Arrange
            pedidoEntityCompleta.getDetalles().get(0).setComponente(null);

            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Pedido resultado =
                PedidoEntityConverter.convertToDomain(pedidoEntityCompleta);

            // Assert
            mx.com.qtx.cotizador.dominio.pedidos.DetallePedido detalle1 = resultado.getDetallesPedido().get(0);
            assertThat(detalle1.getIdArticulo()).isEqualTo("N/A");
            assertThat(detalle1.getDescripcion()).isEqualTo("Sin descripción");
        }

        @Test
        @DisplayName("Debería aplicar valores por defecto para campos null en detalles")
        void convertToDomain_deberiaAplicarDefectosEnDetalles() {
            // Arrange
            DetallePedido detalle = pedidoEntityCompleta.getDetalles().get(0);
            detalle.setCantidad(null);
            detalle.setPrecioUnitario(null);
            detalle.setTotalCotizado(null);

            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Pedido resultado =
                PedidoEntityConverter.convertToDomain(pedidoEntityCompleta);

            // Assert
            mx.com.qtx.cotizador.dominio.pedidos.DetallePedido detalleDomain = resultado.getDetallesPedido().get(0);
            assertThat(detalleDomain.getCantidad()).isEqualTo(0);
            assertThat(detalleDomain.getPrecioUnitario()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(detalleDomain.getTotalCotizado()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ==================== PRUEBAS DE INTEGRIDAD BIDIRECCIONAL ====================

    @Nested
    @DisplayName("Integridad de Conversiones Bidireccionales")
    class IntegridadConversionesBidireccionales {

        @Test
        @DisplayName("Debería preservar datos básicos en conversión bidireccional (sin detalles)")
        void deberiaPreservarDatosBasicosEnConversionBidireccional() {
            // Arrange - Pedido dominio simple sin detalles
            mx.com.qtx.cotizador.dominio.pedidos.Pedido pedidoSimple =
                new mx.com.qtx.cotizador.dominio.pedidos.Pedido(
                    999L, FECHA_EMISION, FECHA_ENTREGA, NIVEL_SURTIDO, proveedorDominio);

            when(proveedorRepositorio.findByCve(CVE_PROVEEDOR)).thenReturn(proveedorEntity);

            // Act - Domain → Entity → Domain
            mx.com.qtx.cotizador.entidad.Pedido entidad =
                PedidoEntityConverter.convertToEntity(pedidoSimple, proveedorRepositorio);
            entidad.setNumPedido(999); // Simular asignación de ID por BD

            mx.com.qtx.cotizador.dominio.pedidos.Pedido resultado =
                PedidoEntityConverter.convertToDomain(entidad);

            // Assert - Datos básicos preservados
            assertThat(resultado.getNumPedido()).isEqualTo(999L);
            assertThat(resultado.getFechaEmision()).isEqualTo(FECHA_EMISION);
            assertThat(resultado.getFechaEntrega()).isEqualTo(FECHA_ENTREGA);
            assertThat(resultado.getNivelSurtido()).isEqualTo(NIVEL_SURTIDO);
            assertThat(resultado.getProveedor().getCve()).isEqualTo(CVE_PROVEEDOR);
        }

        @Test
        @DisplayName("Debería manejar conversión con valores por defecto")
        void deberiaManejarConversionConValoresPorDefecto() {
            // Arrange - Entidad con algunos campos null
            mx.com.qtx.cotizador.entidad.Pedido entidadIncompleta = new mx.com.qtx.cotizador.entidad.Pedido();
            entidadIncompleta.setNumPedido(123);
            entidadIncompleta.setFechaEmision(null); // Aplicará valor por defecto
            entidadIncompleta.setFechaEntrega(null); // Aplicará valor por defecto
            entidadIncompleta.setNivelSurtido(null); // Aplicará valor por defecto
            entidadIncompleta.setProveedor(null);   // Permanecerá null

            // Act
            mx.com.qtx.cotizador.dominio.pedidos.Pedido resultado =
                PedidoEntityConverter.convertToDomain(entidadIncompleta);

            // Assert
            assertThat(resultado.getFechaEmision()).isEqualTo(LocalDate.now());
            assertThat(resultado.getFechaEntrega()).isEqualTo(LocalDate.now().plusDays(7));
            assertThat(resultado.getNivelSurtido()).isEqualTo(0);
            assertThat(resultado.getProveedor()).isNull();
        }
    }
}