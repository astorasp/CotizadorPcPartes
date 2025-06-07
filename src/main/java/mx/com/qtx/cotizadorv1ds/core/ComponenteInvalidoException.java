package mx.com.qtx.cotizadorv1ds.core;

import mx.com.qtx.cotizadorv1ds.core.componentes.Componente;

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
