package mx.com.qtx.cotizadorv1ds.pedidos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un pedido realizado a un proveedor.
 * Contiene información general del pedido como número, fechas, proveedor
 * y una lista de detalles de los artículos solicitados.
 */
public class Pedido {
    private long numPedido;
    private LocalDate fechaEmision;
    private LocalDate fechaEntrega;
    private int nivelSurtido; 
    private Proveedor proveedor; // Relación con el proveedor
    private List<DetallePedido> detallesPedido; // Almacena los datos copiados

    /**
     * Constructor privado para inicializar la lista de detalles.
     * Utilizado internamente por otros constructores.
     */
    private Pedido() {
        this.detallesPedido = new ArrayList<>();
    }

    /**
     * Construye una nueva instancia de Pedido.
     *
     * @param numPedido Número único identificador del pedido.
     * @param fechaEmision Fecha en que se emitió el pedido.
     * @param fechaEntrega Fecha estimada o acordada para la entrega del pedido.
     * @param nivelSurtido Nivel inicial de surtido del pedido (ej. porcentaje o estado).
     * @param proveedor El proveedor al que se realiza el pedido.
     */
    public Pedido(long numPedido, LocalDate fechaEmision, LocalDate fechaEntrega, 
        int nivelSurtido, Proveedor proveedor) {
        this();
        this.numPedido = numPedido;
        this.fechaEmision = fechaEmision;
        this.fechaEntrega = fechaEntrega;
        this.proveedor = proveedor;
        this.nivelSurtido = nivelSurtido; // Inicial
    }

    /**
     * Obtiene el número de pedido.
     * @return El número único del pedido.
     */
    public long getNumPedido() {
        return numPedido;
    }

    /**
     * Obtiene la fecha de emisión del pedido.
     * @return La fecha de emisión.
     */
    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    /**
     * Obtiene la fecha de entrega programada del pedido.
     * @return La fecha de entrega.
     */
    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    /**
     * Obtiene el nivel de surtido actual del pedido.
     * Representa qué tan completo está el proceso de surtido.
     * @return El nivel de surtido.
     */
    public int getNivelSurtido() {
        return nivelSurtido;
    }

    /**
     * Establece el nivel de surtido del pedido.
     * @param nivelSurtido El nuevo nivel de surtido.
     */
    public void setNivelSurtido(int nivelSurtido) {
        this.nivelSurtido = nivelSurtido;
    }

    /**
     * Obtiene el proveedor asociado a este pedido.
     * @return El objeto Proveedor.
     */
    public Proveedor getProveedor() {
        return proveedor;
    }

    /**
     * Obtiene una copia de la lista de detalles del pedido.
     * Devuelve una copia defensiva para evitar modificaciones externas.
     * @return Una lista con los detalles del pedido.
     */
    public List<DetallePedido> getDetallesPedido() {
        return new ArrayList<>(detallesPedido); // Devolver copia defensiva
    }

    /**
     * Agrega una nueva línea de detalle al pedido.
     * Crea un objeto DetallePedido con la información proporcionada y lo añade a la lista interna.
     *
     * @param idArticulo Identificador del artículo.
     * @param descripcion Descripción del artículo.
     * @param cantidad Cantidad solicitada del artículo.
     * @param precioUnitario Precio unitario del artículo.
     * @param totalCotizado Costo total para esta línea de detalle.
     */
    public void agregarDetallePedido(String idArticulo, String descripcion, 
            int cantidad, BigDecimal precioUnitario, BigDecimal totalCotizado) {
        DetallePedido detalle = new DetallePedido(idArticulo, descripcion, cantidad, precioUnitario
            , totalCotizado);
        this.detallesPedido.add(detalle);
    }

    /**
     * Devuelve una representación en cadena del Pedido, incluyendo sus datos generales
     * y la lista de detalles formateada como tabla.
     * @return Una cadena con la información completa del pedido.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Datos del Pedido: \n");
        sb.append("=".repeat(30));
        sb.append("\n");
        sb.append("numPedido=").append(numPedido)
            .append("\nfechaEmision=").append(fechaEmision)
            .append("\nfechaEntrega=").append(fechaEntrega)
            .append("\nnivelSurtido=").append(nivelSurtido)
            .append("\nproveedor=").append(proveedor != null ? proveedor.getCve() : "N/A")
            .append("\n").append("=".repeat(30))
            .append("\nDetalle Pedido: \n")
            .append(DetallePedido.getHeader());
        for (DetallePedido detalle : detallesPedido) {
            sb.append("\n").append(detalle.toString());
        }
        
        
        return sb.toString();
    }
}
