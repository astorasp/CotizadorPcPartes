package mx.com.qtx.cotizador.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mx.com.qtx.cotizador.dominio.core.Cotizacion;
import mx.com.qtx.cotizador.dominio.core.DetalleCotizacion;
import mx.com.qtx.cotizador.dominio.pedidos.DetallePedido;
import mx.com.qtx.cotizador.dominio.pedidos.Pedido;
import mx.com.qtx.cotizador.dominio.pedidos.Proveedor;
import mx.com.qtx.cotizador.dto.pedido.request.GenerarPedidoRequest;
import mx.com.qtx.cotizador.dto.proveedor.response.ProveedorResponse;

/**
 * Utilidades comunes para pruebas unitarias del microservicio ms-cotizador-pedidos.
 *
 * <p>Esta clase proporciona métodos helper para crear objetos de test válidos y consistentes,
 * facilitando la escritura de pruebas unitarias y garantizando la reutilización de código.
 * Todos los métodos son estáticos para facilitar su uso en las clases de test.</p>
 *
 * <h3>Categorías de Utilidades:</h3>
 * <ul>
 *   <li><strong>Dominio Core:</strong> Creación de Cotizacion y DetalleCotizacion</li>
 *   <li><strong>Dominio Pedidos:</strong> Creación de Pedido, DetallePedido y Proveedor</li>
 *   <li><strong>DTOs:</strong> Creación de requests y responses</li>
 *   <li><strong>Constantes:</strong> Valores comunes para tests</li>
 *   <li><strong>Validaciones:</strong> Métodos de verificación de objetos de test</li>
 * </ul>
 *
 * <h3>Uso Típico:</h3>
 * <pre>{@code
 * // En una clase de test
 * @Test
 * void deberiaCrearPedidoValido() {
 *     // Arrange
 *     Cotizacion cotizacion = TestUtils.crearCotizacionValida();
 *     Proveedor proveedor = TestUtils.crearProveedorValido();
 *
 *     // Act & Assert
 *     // ... lógica de test
 * }
 * }</pre>
 *
 * @author Sistema de Testing ms-cotizador-pedidos
 * @version 1.0.0
 * @since 2.0.0
 * @see mx.com.qtx.cotizador.dominio.core.Cotizacion
 * @see mx.com.qtx.cotizador.dominio.pedidos.Pedido
 * @see mx.com.qtx.cotizador.dominio.pedidos.Proveedor
 */
public final class TestUtils {

    // ==================== CONSTANTES DE TEST ====================

    /** ID de cotización por defecto para tests */
    public static final Integer DEFAULT_COTIZACION_ID = 1;

    /** Clave de proveedor por defecto para tests */
    public static final String DEFAULT_PROVEEDOR_CVE = "PROV-001";

    /** Nombre de proveedor por defecto para tests */
    public static final String DEFAULT_PROVEEDOR_NOMBRE = "Proveedor Test SA de CV";

    /** ID de componente por defecto para tests */
    public static final String DEFAULT_COMPONENTE_ID = "COMP-001";

    /** Descripción de componente por defecto para tests */
    public static final String DEFAULT_COMPONENTE_DESC = "Monitor LED 24 pulgadas";

    /** Precio base por defecto para tests */
    public static final BigDecimal DEFAULT_PRECIO_BASE = new BigDecimal("500.00");

    /** Cantidad por defecto para tests */
    public static final int DEFAULT_CANTIDAD = 1;

    /** Total cotizado por defecto para tests */
    public static final BigDecimal DEFAULT_TOTAL_COTIZADO = new BigDecimal("500.00");

    /**
     * Constructor privado para evitar instanciación.
     * Esta clase solo contiene métodos estáticos de utilidad.
     */
    private TestUtils() {
        throw new UnsupportedOperationException("TestUtils es una clase de utilidades y no debe ser instanciada");
    }

    // ==================== CREADORES DE OBJETOS DE DOMINIO CORE ====================

    /**
     * Crea una cotización válida con datos por defecto.
     *
     * <p>La cotización incluye:</p>
     * <ul>
     *   <li>Fecha actual</li>
     *   <li>Total de $1,160.00</li>
     *   <li>Impuestos de $160.00</li>
     *   <li>Un detalle de cotización válido</li>
     * </ul>
     *
     * @return Cotización válida para usar en tests
     */
    public static Cotizacion crearCotizacionValida() {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setFecha(LocalDate.now());
        cotizacion.setTotal(new BigDecimal("1160.00"));
        cotizacion.setTotalImpuestos(new BigDecimal("160.00"));

        DetalleCotizacion detalle = crearDetalleCotizacionValido();
        cotizacion.agregarDetalle(detalle);

        return cotizacion;
    }

    /**
     * Crea una cotización con múltiples detalles.
     *
     * @return Cotización con 3 detalles diferentes
     */
    public static Cotizacion crearCotizacionConMultiplesDetalles() {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setFecha(LocalDate.now());
        cotizacion.setTotal(new BigDecimal("1750.00"));
        cotizacion.setTotalImpuestos(new BigDecimal("250.00"));

        cotizacion.agregarDetalle(crearDetalleCotizacion(1, "COMP-001", "Monitor", 1, "500.00", "500.00"));
        cotizacion.agregarDetalle(crearDetalleCotizacion(2, "COMP-002", "Teclado", 2, "100.00", "200.00"));
        cotizacion.agregarDetalle(crearDetalleCotizacion(3, "COMP-003", "Mouse", 1, "50.00", "50.00"));

        return cotizacion;
    }

    /**
     * Crea un detalle de cotización válido con datos por defecto.
     *
     * @return DetalleCotizacion válido para usar en tests
     */
    public static DetalleCotizacion crearDetalleCotizacionValido() {
        return crearDetalleCotizacion(1, DEFAULT_COMPONENTE_ID, DEFAULT_COMPONENTE_DESC,
                                    DEFAULT_CANTIDAD, DEFAULT_PRECIO_BASE.toString(),
                                    DEFAULT_TOTAL_COTIZADO.toString());
    }

    /**
     * Crea un detalle de cotización con parámetros específicos.
     *
     * @param numDetalle Número de detalle
     * @param idComponente ID del componente
     * @param descripcion Descripción del componente
     * @param cantidad Cantidad solicitada
     * @param precioBase Precio base del componente
     * @param importeCotizado Importe total cotizado
     * @return DetalleCotizacion con los parámetros especificados
     */
    public static DetalleCotizacion crearDetalleCotizacion(int numDetalle, String idComponente,
                                                         String descripcion, int cantidad,
                                                         String precioBase, String importeCotizado) {
        return new DetalleCotizacion(
            numDetalle,
            idComponente,
            descripcion,
            cantidad,
            new BigDecimal(precioBase),
            new BigDecimal(importeCotizado),
            "MONITOR"
        );
    }

    // ==================== CREADORES DE OBJETOS DE DOMINIO PEDIDOS ====================

    /**
     * Crea un pedido válido con datos por defecto.
     *
     * @return Pedido válido para usar en tests
     */
    public static Pedido crearPedidoValido() {
        Proveedor proveedor = crearProveedorValido();
        return new Pedido(1L, LocalDate.now(), LocalDate.now().plusDays(15), 0, proveedor);
    }

    /**
     * Crea un pedido con parámetros específicos.
     *
     * @param numeroPedido Número del pedido
     * @param fechaEmision Fecha de emisión
     * @param fechaEntrega Fecha de entrega
     * @param nivelSurtido Nivel de surtido
     * @param proveedor Proveedor del pedido
     * @return Pedido con los parámetros especificados
     */
    public static Pedido crearPedido(Long numeroPedido, LocalDate fechaEmision, LocalDate fechaEntrega, int nivelSurtido, Proveedor proveedor) {
        return new Pedido(numeroPedido, fechaEmision, fechaEntrega, nivelSurtido, proveedor);
    }

    /**
     * Crea un detalle de pedido válido con datos por defecto.
     *
     * @return DetallePedido válido para usar en tests
     */
    public static DetallePedido crearDetallePedidoValido() {
        return new DetallePedido(
            DEFAULT_COMPONENTE_ID,
            DEFAULT_COMPONENTE_DESC,
            DEFAULT_CANTIDAD,
            DEFAULT_PRECIO_BASE,
            DEFAULT_TOTAL_COTIZADO
        );
    }

    /**
     * Crea un proveedor válido con datos por defecto.
     *
     * @return Proveedor válido para usar en tests
     */
    public static Proveedor crearProveedorValido() {
        return new Proveedor(DEFAULT_PROVEEDOR_CVE, DEFAULT_PROVEEDOR_NOMBRE,
                           DEFAULT_PROVEEDOR_NOMBRE + " SA de CV");
    }

    /**
     * Crea un proveedor con parámetros específicos.
     *
     * @param cve Clave del proveedor
     * @param nombre Nombre del proveedor
     * @param razonSocial Razón social del proveedor
     * @return Proveedor con los parámetros especificados
     */
    public static Proveedor crearProveedor(String cve, String nombre, String razonSocial) {
        return new Proveedor(cve, nombre, razonSocial);
    }

    /**
     * Crea una lista de proveedores válidos para usar en tests.
     *
     * @return Lista con varios proveedores configurados
     */
    public static List<Proveedor> crearListaProveedoresValidos() {
        List<Proveedor> proveedores = new ArrayList<>();
        proveedores.add(crearProveedorValido());
        proveedores.add(crearProveedor("PROV-002", "Proveedor Secundario", "Proveedor Secundario SA de CV"));
        proveedores.add(crearProveedor("PROV-003", "Proveedor Alternativo", "Proveedor Alternativo SA de CV"));
        return proveedores;
    }

    // ==================== CREADORES DE DTOs ====================

    /**
     * Crea un GenerarPedidoRequest válido con datos por defecto.
     *
     * @return GenerarPedidoRequest válido para usar en tests
     */
    public static GenerarPedidoRequest crearGenerarPedidoRequestValido() {
        GenerarPedidoRequest request = new GenerarPedidoRequest();
        request.setCotizacionId(DEFAULT_COTIZACION_ID);
        request.setCveProveedor(DEFAULT_PROVEEDOR_CVE);
        return request;
    }

    /**
     * Crea un ProveedorResponse válido con datos por defecto.
     *
     * @return ProveedorResponse válido para usar en tests de mocking
     */
    public static ProveedorResponse crearProveedorResponseValido() {
        ProveedorResponse response = new ProveedorResponse();
        response.setCve(DEFAULT_PROVEEDOR_CVE);
        response.setNombre(DEFAULT_PROVEEDOR_NOMBRE);
        response.setRazonSocial(DEFAULT_PROVEEDOR_NOMBRE + " SA de CV");
        response.setNumeroPedidos(0);
        return response;
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Verifica que una cotización tenga datos válidos básicos.
     *
     * @param cotizacion Cotización a validar
     * @return true si la cotización es válida
     */
    public static boolean esCotizacionValida(Cotizacion cotizacion) {
        return cotizacion != null
            && cotizacion.getFecha() != null
            && cotizacion.getTotal() != null
            && cotizacion.getTotal().compareTo(BigDecimal.ZERO) > 0
            && !cotizacion.getDetalles().isEmpty();
    }

    /**
     * Verifica que un pedido tenga datos válidos básicos.
     *
     * @param pedido Pedido a validar
     * @return true si el pedido es válido
     */
    public static boolean esPedidoValido(Pedido pedido) {
        return pedido != null
            && pedido.getNumPedido() > 0
            && pedido.getProveedor() != null
            && pedido.getFechaEmision() != null
            && pedido.getFechaEntrega() != null
            && pedido.getTotalPedido() != null
            && pedido.getTotalPedido().compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Verifica que un proveedor tenga datos válidos básicos.
     *
     * @param proveedor Proveedor a validar
     * @return true si el proveedor es válido
     */
    public static boolean esProveedorValido(Proveedor proveedor) {
        return proveedor != null
            && proveedor.getCve() != null
            && !proveedor.getCve().trim().isEmpty()
            && proveedor.getNombre() != null
            && !proveedor.getNombre().trim().isEmpty()
            && proveedor.getRazonSocial() != null
            && !proveedor.getRazonSocial().trim().isEmpty();
    }

    // ==================== DATOS MOCK PARA CONVERSORES ====================

    /**
     * Crea un mapa de datos de artículo para usar en tests de IPresupuesto.
     *
     * @return Map con datos simulados de un artículo
     */
    public static Map<String, Object> crearDatosArticuloMock() {
        Map<String, Object> datos = new HashMap<>();
        datos.put("descripcion", DEFAULT_COMPONENTE_DESC);
        datos.put("cantidad", DEFAULT_CANTIDAD);
        datos.put("precioBase", DEFAULT_PRECIO_BASE);
        datos.put("importeTotalLinea", DEFAULT_TOTAL_COTIZADO);
        return datos;
    }

    /**
     * Crea un mapa de cantidades por ID de artículo para tests.
     *
     * @return Map con cantidades simuladas
     */
    public static Map<String, Integer> crearCantidadesPorIdMock() {
        Map<String, Integer> cantidades = new HashMap<>();
        cantidades.put(DEFAULT_COMPONENTE_ID, DEFAULT_CANTIDAD);
        cantidades.put("COMP-002", 2);
        cantidades.put("COMP-003", 1);
        return cantidades;
    }

    // ==================== CASOS DE EDGE PARA TESTS ====================

    /**
     * Crea una cotización con valores límite para tests de edge cases.
     *
     * @return Cotización con valores de edge case
     */
    public static Cotizacion crearCotizacionConValoresLimite() {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setFecha(LocalDate.now());
        cotizacion.setTotal(new BigDecimal("999999999.99"));
        cotizacion.setTotalImpuestos(new BigDecimal("159999999.99"));

        DetalleCotizacion detalle = crearDetalleCotizacion(
            Integer.MAX_VALUE,
            "COMP-EDGE-CASE-12345",
            "Descripción muy larga para probar límites de caracteres en el sistema de cotizaciones",
            1000,
            "999999.99",
            "999999999.00"
        );
        cotizacion.agregarDetalle(detalle);

        return cotizacion;
    }

    /**
     * Crea un proveedor con datos de edge case.
     *
     * @return Proveedor con valores límite
     */
    public static Proveedor crearProveedorConDatosLimite() {
        return crearProveedor(
            "PROV-EDGE-CASE-123456789",
            "Proveedor con Nombre Muy Largo para Probar Límites de Caracteres en el Sistema",
            "Proveedor con Razón Social Muy Larga para Probar Límites de Caracteres en el Sistema de Gestión de Proveedores de Componentes de Computadoras y Tecnología SA de CV"
        );
    }
}