package mx.com.qtx.cotizador.dominio.core;

/**
 * Excepción lanzada cuando se intenta usar un componente inválido en operaciones de cotización.
 * <p>
 * Esta excepción se utiliza en el dominio de cotizaciones para indicar situaciones donde
 * un componente no puede ser procesado correctamente. Las causas comunes incluyen:
 * componentes inexistentes, componentes inactivos, componentes sin stock disponible,
 * o componentes con datos inconsistentes.
 * </p>
 *
 * <h3>Casos de uso:</h3>
 * <ul>
 *   <li>Intento de eliminar un componente que no existe en la cotización actual</li>
 *   <li>Referencia a un componente que ya no está disponible en el catálogo</li>
 *   <li>Componente con datos corruptos o inválidos</li>
 *   <li>Violación de reglas de negocio relacionadas con componentes</li>
 * </ul>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * try {
 *     cotizador.eliminarComponente("COMP001");
 * } catch (ComponenteInvalidoException e) {
 *     logger.error("Error al eliminar componente: {}", e.getMessage());
 *     throw new BusinessException("No se puede eliminar el componente especificado", e);
 * }
 * }</pre>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see mx.com.qtx.cotizador.dominio.core.ICotizador#eliminarComponente(String)
 */
public class ComponenteInvalidoException extends Exception {
    
    public ComponenteInvalidoException(String mensaje) {
        super(mensaje);
    }
    
    public ComponenteInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}