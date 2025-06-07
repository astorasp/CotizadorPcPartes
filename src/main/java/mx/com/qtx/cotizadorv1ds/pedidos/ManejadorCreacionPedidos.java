package mx.com.qtx.cotizadorv1ds.pedidos;

import java.time.LocalDate;

import mx.com.qtx.cotizadorv1ds.core.Cotizacion;
import mx.com.qtx.cotizadorv1ds.core.CotizacionPresupuestoAdapter;
import mx.com.qtx.cotizadorv1ds.pedidos.excepciones.PresupuestoNoCargadoExcepcion;

/**
 * Orquesta el proceso de creación de pedidos a partir de objetos Cotizacion.
 * Utiliza un GestorPedidos interno y un adaptador para convertir la información
 * de una cotización al formato esperado por el gestor (IPresupuesto).
 */
public class ManejadorCreacionPedidos {
    private GestorPedidos gestorPedidos;

    /**
     * Construye una nueva instancia de ManejadorCreacionPedidos.
     * Inicializa el GestorPedidos interno.
     */
    public ManejadorCreacionPedidos() {        
        this.gestorPedidos = new GestorPedidos();
    }

    /**
     * Crea y genera un pedido utilizando la información de una Cotizacion existente.
     * El proceso implica adaptar la cotización a IPresupuesto, pasarla al GestorPedidos
     * y finalmente invocar la generación del pedido en el gestor.
     * Maneja excepciones genéricas durante el proceso e imprime mensajes en la consola.
     *
     * @param cotizacion La cotización original de la cual se generará el pedido.
     * @param cveProveedor Clave del proveedor para el pedido.
     * @param numPedido Número deseado para el nuevo pedido.
     * @param nivelSurtido Nivel inicial de surtido para el pedido.
     * @param fechaEmision Fecha de emisión del pedido.
     * @param fechaEntrega Fecha de entrega programada para el pedido.
     */
    public void crearPedidoDesdeCotizacion(Cotizacion cotizacion, String cveProveedor, 
                                            int numPedido, int nivelSurtido, 
                                            LocalDate fechaEmision, LocalDate fechaEntrega) {
        try {            
            // 1. Adaptar la cotización a un presupuesto
            IPresupuesto presupuestoAdaptado = new CotizacionPresupuestoAdapter(cotizacion);            
            // 2. Agregar el presupuesto al gestor
            gestorPedidos.agregarPresupuesto(presupuestoAdaptado);
            System.out.println("Iniciando proceso para generar pedido desde cotización: " + cotizacion.getNum());
            // 3. Generar el pedido
            gestorPedidos.generarPedido(cveProveedor, numPedido, nivelSurtido, 
                fechaEmision, fechaEntrega);
            System.out.println("Pedido generado exitosamente para cotización: " + cotizacion.getNum());            
        } 
        catch (PresupuestoNoCargadoExcepcion e) {
            System.err.println("Error al cargar el presupuesto: " + e.getMessage());            
        }
        catch (Exception e) {
             System.err.println("Error inesperado al generar pedido: " + e.getMessage());
             e.printStackTrace();
             // Lanzar excepción, loggear, etc.
        }
    }

    /**
     * Solicita al GestorPedidos interno que imprima el último pedido generado.
     * La salida se dirige a la consola estándar.
     */
    public void imprimirPedido() {
        gestorPedidos.imprimirPedidoActual();
    }
}
