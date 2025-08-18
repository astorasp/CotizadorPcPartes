package mx.com.qtx.cotizador.dominio.core;

/**
 * Excepción lanzada cuando se intenta usar un componente inválido.
 */
public class ComponenteInvalidoException extends Exception {
    
    public ComponenteInvalidoException(String mensaje) {
        super(mensaje);
    }
    
    public ComponenteInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}