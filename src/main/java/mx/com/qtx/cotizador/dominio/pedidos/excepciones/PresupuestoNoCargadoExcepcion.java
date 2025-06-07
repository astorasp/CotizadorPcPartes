package mx.com.qtx.cotizador.pedidos.excepciones;

/**
 * Excepción lanzada cuando se intenta realizar una operación que requiere un presupuesto
 * (como generar un pedido) pero no se ha cargado previamente ningún presupuesto
 * en el componente correspondiente (ej. GestorPedidos).
 */
public class PresupuestoNoCargadoExcepcion extends Exception {
    
    /**
     * Construye una nueva PresupuestoNoCargadoExcepcion con un mensaje de detalle predeterminado.
     */
    public PresupuestoNoCargadoExcepcion() {
        super("No hay presupuesto cargado.");
    }
}
