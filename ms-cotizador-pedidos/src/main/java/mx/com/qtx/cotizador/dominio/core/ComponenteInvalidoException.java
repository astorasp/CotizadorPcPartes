package mx.com.qtx.cotizador.dominio.core;

// Importamos la clase de dominio local

public class ComponenteInvalidoException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Componente comp;

	public ComponenteInvalidoException(String message, Componente comp) {
		super(message);
		this.comp = comp;
	}

	public Componente getComp() {
		return comp;
	}

	public void setComp(Componente comp) {
		this.comp = comp;
	}
	

}
