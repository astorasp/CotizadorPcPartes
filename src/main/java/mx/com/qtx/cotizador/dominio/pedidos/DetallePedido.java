package mx.com.qtx.cotizador.pedidos;

import java.math.BigDecimal;

/**
 * Representa una línea de detalle dentro de un pedido.
 * Almacena la información de un artículo específico incluido en el pedido,
 * como su identificador, descripción, cantidad, precio unitario y el total cotizado para esa línea.
 * Esta clase es utilizada para transferir datos desde un presupuesto hacia un pedido formal.
 */
public class DetallePedido {
    private String idArticulo;
    private String descripcion;
    private int cantidad;
    private BigDecimal precioUnitario; // O importe total línea, según necesidad
    private BigDecimal totalCotizado;

    /**
     * Construye una nueva instancia de DetallePedido.
     *
     * @param idArticulo Identificador único del artículo.
     * @param descripcion Descripción textual del artículo.
     * @param cantidad Número de unidades del artículo solicitadas.
     * @param precioUnitario Precio por unidad del artículo.
     * @param totalCotizado Costo total para la cantidad de artículos solicitados en esta línea de detalle.
     */
    public DetallePedido(String idArticulo, String descripcion, int cantidad
    , BigDecimal precioUnitario, BigDecimal totalCotizado) {
        this.idArticulo = idArticulo;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.totalCotizado = totalCotizado;
    }

    // Getters
    /**
     * Obtiene el identificador del artículo.
     * @return El identificador del artículo.
     */
    public String getIdArticulo() {
        return idArticulo;
    }

    /**
     * Obtiene la descripción del artículo.
     * @return La descripción del artículo.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Obtiene la cantidad de unidades solicitadas del artículo.
     * @return La cantidad del artículo.
     */
    public int getCantidad() {
        return cantidad;
    }

    /**
     * Obtiene el precio unitario del artículo.
     * @return El precio unitario del artículo.
     */
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    /**
     * Obtiene el costo total cotizado para esta línea de detalle.
     * @return El total cotizado para la línea.
     */
    public BigDecimal getTotalCotizado() {
        return totalCotizado;
    }
    
    /**
     * Establece el costo total cotizado para esta línea de detalle.
     * @param totalCotizado El nuevo total cotizado.
     */
    public void setTotalCotizado(BigDecimal totalCotizado) {
        this.totalCotizado = totalCotizado;
    }

    /**
     * Devuelve una representación en cadena de este detalle de pedido, formateada como una fila de tabla.
     * Incluye idArticulo, descripción, cantidad, precio unitario y total cotizado.
     * @return Una cadena formateada representando el detalle del pedido.
     */
    @Override
    public String toString() {
        // Formato: | idArticulo | descripcion                      | cant. | precio Unitario |  total Cotizado |
        return String.format("| %-10s | %-30s | %5d | %,15.2f | %,15.2f |", 
                             this.idArticulo, 
                             this.descripcion, 
                             this.cantidad, 
                             this.precioUnitario,
                             this.totalCotizado);
    }

    /**
     * Devuelve la cadena de encabezado formateada para la representación tabular de los detalles del pedido.
     * Corresponde al formato generado por el método {@link #toString()}.
     * @return La cadena de encabezado formateada.
     */
    public static String getHeader() {
        // Devuelve la cadena de encabezado formateada
        return String.format("| %-10s | %-30s | %5s | %15s | %15s |", 
                             "idArticulo", "descripcion", "cant.", "precio Unitario", "total Cotizado");
    }
}