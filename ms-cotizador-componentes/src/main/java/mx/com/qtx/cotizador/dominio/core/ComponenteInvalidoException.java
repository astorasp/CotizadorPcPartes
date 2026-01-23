package mx.com.qtx.cotizador.dominio.core;

import mx.com.qtx.cotizador.dominio.core.componentes.Componente;

/**
 * Excepción lanzada cuando un componente no es válido para ser usado en el cotizador.
 * Esta excepción se utiliza para indicar problemas con componentes que no cumplen
 * con los requisitos necesarios para formar parte de una configuración de PC.
 *
 * @author [Nombre del autor]
 * @version 1.0
 */
public class ComponenteInvalidoException extends RuntimeException {
	/**
	 * Identificador único para la serialización de la excepción.
	 */
	private static final long serialVersionUID = 1L;
	
	private Componente comp;

	/**
	 * Constructor que crea una nueva excepción con un mensaje y el componente inválido.
	 *
	 * @param message El mensaje descriptivo del error
	 * @param comp El componente que causó la excepción
	 */
	public ComponenteInvalidoException(String message, Componente comp) {
		super(message);
		this.comp = comp;
	}

	/**
	 * Obtiene el componente que causó la excepción.
	 *
	 * @return El componente inválido
	 */
	public Componente getComp() {
		return comp;
	}

	/**
	 * Establece el componente que causó la excepción.
	 *
	 * @param comp El componente inválido
	 */
	public void setComp(Componente comp) {
		this.comp = comp;
	}
	

}
