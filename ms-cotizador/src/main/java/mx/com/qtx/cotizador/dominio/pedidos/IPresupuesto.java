package mx.com.qtx.cotizador.dominio.pedidos;

import java.util.Map;

/**
 * Define el contrato para objetos que representan un presupuesto.
 * Un presupuesto contiene información sobre artículos, sus cantidades y otros datos relevantes
 * que pueden ser utilizados para generar un pedido.
 */
public interface IPresupuesto {
    /**
     * Obtiene la descripción textual de un artículo específico.
     *
     * @param idArticulo El identificador único del artículo.
     * @return La descripción del artículo, o null si el artículo no se encuentra.
     */
    String getDescripcionArticulo(String idArticulo);

    /**
     * Obtiene un mapa con las cantidades solicitadas para cada artículo en el presupuesto.
     * La clave del mapa es el identificador del artículo y el valor es la cantidad.
     *
     * @return Un mapa que relaciona identificadores de artículo con sus cantidades.
     */
    Map<String, Integer> getCantidadesXIdArticulo();

    /**
     * Obtiene un mapa con datos adicionales sobre un artículo específico.
     * La estructura y contenido de este mapa pueden variar (de ahí el uso de Object como valor),
     * pero típicamente incluiría información como precio base, importe total de línea, etc.
     *
     * @param idArticulo El identificador único del artículo.
     * @return Un mapa con datos adicionales del artículo, o un mapa vacío/null si no hay datos.
     */
    Map<String, Object> getDatosArticulo(String idArticulo); // Object es genérico, podría refinarse
} 