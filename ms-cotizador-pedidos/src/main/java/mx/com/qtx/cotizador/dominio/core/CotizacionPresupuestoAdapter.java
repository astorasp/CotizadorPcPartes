package mx.com.qtx.cotizador.dominio.core;

import java.util.HashMap;
import java.util.Map;

import mx.com.qtx.cotizador.dominio.pedidos.IPresupuesto;
import mx.com.qtx.cotizador.dominio.pedidos.excepciones.PresupuestoNoCargadoExcepcion;

/**
 * Adaptador que convierte objetos Cotizacion al formato IPresupuesto.
 * <p>
 * Esta clase implementa el patrón de diseño <strong>Adapter</strong> para permitir que las cotizaciones
 * del sistema CotizadorPcPartes sean utilizadas como presupuestos en el módulo de pedidos.
 * El adaptador actúa como puente entre el dominio de cotizaciones y el dominio de pedidos,
 * permitiendo reutilizar la lógica de cotizaciones sin modificar su estructura interna.
 * </p>
 *
 * <h3>Patrón Adapter:</h3>
 * <ul>
 *   <li><strong>Target:</strong> {@link IPresupuesto} - Interfaz que esperan los gestores de pedidos</li>
 *   <li><strong>Adaptee:</strong> {@link Cotizacion} - Clase existente que se adapta</li>
 *   <li><strong>Adapter:</strong> {@link CotizacionPresupuestoAdapter} - Esta clase que realiza la adaptación</li>
 * </ul>
 *
 * <h3>Funcionalidades Adaptadas:</h3>
 * <ul>
 *   <li><strong>getDescripcionArticulo:</strong> Obtiene descripción de componentes desde detalles de cotización</li>
 *   <li><strong>getCantidadesXIdArticulo:</strong> Extrae cantidades de componentes por ID</li>
 *   <li><strong>getDatosArticulo:</strong> Proporciona datos adicionales del componente (precio, cantidad, etc.)</li>
 * </ul>
 *
 * <h3>Uso Principal:</h3>
 * <p>
 * Este adaptador se utiliza principalmente en {@link mx.com.qtx.cotizador.dominio.pedidos.ManejadorCreacionPedidos}
 * cuando se necesita crear un pedido basado en una cotización existente.
 * </p>
 *
 * <h3>Validaciones:</h3>
 * <ul>
 *   <li>La cotización adaptee no puede ser null</li>
 *   <li>Si un componente no se encuentra, se retorna información por defecto</li>
 *   <li>Manejo seguro de datos faltantes o inconsistentes</li>
 * </ul>
 *
 * @see IPresupuesto Interfaz que implementa
 * @see Cotizacion Clase que adapta
 * @see mx.com.qtx.cotizador.dominio.pedidos.ManejadorCreacionPedidos Usuario principal del adaptador
 * @author Sistema CotizadorPcPartes - Patrón Adapter
 * @version 1.0
 */
public class CotizacionPresupuestoAdapter implements IPresupuesto {

    /**
     * Cotización que se está adaptando al formato de presupuesto.
     * <p>
     * Esta es la instancia de {@link Cotizacion} que contiene todos los detalles
     * de componentes, precios y cantidades que serán expuestos a través de la interfaz IPresupuesto.
     * </p>
     */
    private Cotizacion cotizacionAdaptee;

    /**
     * Constructor que inicializa el adaptador con una cotización específica.
     * <p>
     * Crea una nueva instancia del adaptador configurada para trabajar con la cotización
     * proporcionada. Esta cotización será la fuente de datos para todas las consultas
     * realizadas a través de la interfaz IPresupuesto.
     * </p>
     *
     * @param cotizacionAdaptee La cotización que se va a adaptar. No puede ser null.
     * @throws PresupuestoNoCargadoExcepcion Si la cotización proporcionada es null
     */
    public CotizacionPresupuestoAdapter(Cotizacion cotizacionAdaptee) throws PresupuestoNoCargadoExcepcion {
        if(cotizacionAdaptee == null){
            throw new PresupuestoNoCargadoExcepcion();
        }
        this.cotizacionAdaptee = cotizacionAdaptee;
    }

    @Override
    public String getDescripcionArticulo(String idArticulo) {
        DetalleCotizacion detalle = this.getDetallePorId(idArticulo);
        return (detalle != null) ? detalle.getDescripcion() : "Descripción no encontrada";
    }

    @Override
    public Map<String, Integer> getCantidadesXIdArticulo() {
        Map<String, Integer> cantidades = new HashMap<>();
        for (DetalleCotizacion detalle : cotizacionAdaptee.getDetalles()) {
            cantidades.put(detalle.getIdComponente(), detalle.getCantidad());
        }
        return cantidades;
    }

    @Override
    public Map<String, Object> getDatosArticulo(String idArticulo) {
        DetalleCotizacion detalle = this.getDetallePorId(idArticulo);
        if (detalle == null) {
            return new HashMap<>(); // Vacío si no se encuentra
        }
        // Devolvemos un mapa con los datos que podrían ser útiles del detalle
        // Esto es flexible según lo que realmente necesite IPresupuesto
        Map<String, Object> datos = new HashMap<>();
        datos.put("descripcion", detalle.getDescripcion());
        datos.put("cantidad", detalle.getCantidad());
        datos.put("precioBase", detalle.getPrecioBase());
        datos.put("importeTotalLinea", detalle.getImporteCotizado());
        // Se podrían añadir más datos si fueran necesarios
        return datos;
    }

    /**
     * Método auxiliar para buscar un detalle de cotización por el ID del componente.
     * <p>
     * Este método realiza una búsqueda lineal a través de todos los detalles de la cotización
     * para encontrar aquel que corresponda al ID de componente especificado. Es utilizado
     * internamente por los métodos de la interfaz IPresupuesto para acceder a la información
     * específica de cada componente.
     * </p>
     *
     * @param idArticulo El identificador único del componente a buscar
     * @return El detalle de cotización correspondiente al componente, o null si no se encuentra
     */
    private DetalleCotizacion getDetallePorId(String idArticulo) {
        return cotizacionAdaptee
            .getDetalles()
            .stream()
            .filter(x -> x.getIdComponente().equals(idArticulo))
            .findFirst()
            .orElse(null);
    }
} 