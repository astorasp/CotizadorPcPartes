package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Constructor de PCs que implementa el patrón Builder.
 * Permite construir una PC de manera fluida agregando componentes individuales
 * y configurando sus propiedades básicas, validando restricciones mínimas y máximas.
 *
 * @author [Nombre del autor]
 * @version 1.0
 */
public class PcBuilder {
	/** Número mínimo de monitores requeridos */
	public static final int MIN_MONITORES = 1;
	/** Número máximo de monitores permitidos */
	public static final int MAX_MONITORES = 2;
	/** Lista de monitores agregados a la PC */
	private List<Monitor> monitores;
	/** Número mínimo de tarjetas de video requeridas */
	public static final int MIN_TARJETAS = 1;
	/** Número máximo de tarjetas de video permitidas */
	public static final int MAX_TARJETAS = 2;
	/** Lista de tarjetas de video agregadas a la PC */
	private List<TarjetaVideo> tarjetas;
	/** Número mínimo de discos duros requeridos */
	public static final int MIN_DISCOS = 1;
	/** Número máximo de discos duros permitidos */
	public static final int MAX_DISCOS = 3;
	/** Lista de discos duros agregados a la PC */
	private List<DiscoDuro> discos;
	
	/** Identificador único de la PC */
	private String idPc;
	/** Descripción de la PC */
	private String descripcionPc;
	/** Marca de la PC */
	private String marcaPc;
	/** Modelo de la PC */
	private String modeloPc;
	
	/**
	 * Obtiene el número mínimo de monitores.
	 *
	 * @return El mínimo de monitores
	 */
	static int getMinMonitores() {
		return MIN_MONITORES;
	}

	/**
	 * Obtiene el número máximo de monitores.
	 *
	 * @return El máximo de monitores
	 */
	static int getMaxMonitores() {
		return MAX_MONITORES;
	}

	/**
	 * Obtiene la lista de monitores.
	 *
	 * @return Lista de monitores
	 */
	List<Monitor> getMonitores() {
		return monitores;
	}

	/**
	 * Obtiene el número mínimo de tarjetas de video.
	 *
	 * @return El mínimo de tarjetas
	 */
	static int getMinTarjetas() {
		return MIN_TARJETAS;
	}

	/**
	 * Obtiene el número máximo de tarjetas de video.
	 *
	 * @return El máximo de tarjetas
	 */
	static int getMaxTarjetas() {
		return MAX_TARJETAS;
	}

	/**
	 * Obtiene la lista de tarjetas de video.
	 *
	 * @return Lista de tarjetas de video
	 */
	List<TarjetaVideo> getTarjetas() {
		return tarjetas;
	}

	/**
	 * Obtiene el número mínimo de discos duros.
	 *
	 * @return El mínimo de discos
	 */
	static int getMinDiscos() {
		return MIN_DISCOS;
	}

	/**
	 * Obtiene el número máximo de discos duros.
	 *
	 * @return El máximo de discos
	 */
	static int getMaxDiscos() {
		return MAX_DISCOS;
	}

	/**
	 * Obtiene la lista de discos duros.
	 *
	 * @return Lista de discos duros
	 */
	List<DiscoDuro> getDiscos() {
		return discos;
	}

	/**
	 * Obtiene el identificador de la PC.
	 *
	 * @return El ID de la PC
	 */
	String getIdPc() {
		return idPc;
	}

	/**
	 * Obtiene la descripción de la PC.
	 *
	 * @return La descripción de la PC
	 */
	String getDescripcionPc() {
		return descripcionPc;
	}

	/**
	 * Obtiene la marca de la PC.
	 *
	 * @return La marca de la PC
	 */
	String getMarcaPc() {
		return marcaPc;
	}

	/**
	 * Obtiene el modelo de la PC.
	 *
	 * @return El modelo de la PC
	 */
	String getModeloPc() {
		return modeloPc;
	}

	/**
	 * Constructor por defecto que inicializa las listas de componentes.
	 */
	PcBuilder() {
		super();
		this.discos = new ArrayList<>();
		this.tarjetas = new ArrayList<>();
		this.monitores = new ArrayList<>();
	}
	
	/**
	 * Agrega un disco duro a la PC si no excede el máximo permitido.
	 *
	 * @param id Identificador del disco duro
	 * @param descripcion Descripción del disco duro
	 * @param marca Marca del disco duro
	 * @param modelo Modelo del disco duro
	 * @param costo Costo del disco duro
	 * @param precioBase Precio base del disco duro
	 * @param capacidadAlm Capacidad de almacenamiento del disco duro
	 * @return Esta instancia del builder para encadenamiento
	 */
	public PcBuilder agregarDisco(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase, String capacidadAlm) {
		if(this.discos.size() == PcBuilder.MAX_DISCOS) //Si excede el max, lo ignora
			return this;
		this.discos.add(new DiscoDuro(id, descripcion, marca, modelo, costo,
				precioBase, capacidadAlm));
		return this;
	}
	
	/**
	 * Agrega un monitor a la PC si no excede el máximo permitido.
	 *
	 * @param id Identificador del monitor
	 * @param descripcion Descripción del monitor
	 * @param marca Marca del monitor
	 * @param modelo Modelo del monitor
	 * @param costo Costo del monitor
	 * @param precioBase Precio base del monitor
	 * @return Esta instancia del builder para encadenamiento
	 */
	public PcBuilder agregarMonitor(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase) {
		if(this.monitores.size() == PcBuilder.MAX_MONITORES) //
			return this;
		this.monitores.add(new Monitor(id, descripcion, marca, modelo, costo,precioBase));
		return this;
	}

	/**
	 * Agrega una tarjeta de video a la PC si no excede el máximo permitido.
	 *
	 * @param id Identificador de la tarjeta de video
	 * @param descripcion Descripción de la tarjeta de video
	 * @param marca Marca de la tarjeta de video
	 * @param modelo Modelo de la tarjeta de video
	 * @param costo Costo de la tarjeta de video
	 * @param precioBase Precio base de la tarjeta de video
	 * @param memoria Memoria de la tarjeta de video
	 * @return Esta instancia del builder para encadenamiento
	 */
	public PcBuilder agregarTarjetaVideo(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase, String memoria) {
		if(this.tarjetas.size() == PcBuilder.MAX_TARJETAS) //
			return this;
		this.tarjetas.add(new TarjetaVideo(id, descripcion, marca, modelo, costo,
				precioBase, memoria));
		return this;
	}
	
	/**
	 * Valida si la configuración actual de la PC es válida.
	 * Verifica que se hayan definido ID, descripción, marca, modelo y
	 * que se cumplan los mínimos de componentes.
	 *
	 * @return true si la PC es válida, false en caso contrario
	 */
	private boolean pcEsValida() {
		if(this.idPc == null)
			return false;
		if(this.descripcionPc == null)
			return false;
		if(this.marcaPc == null)
			return false;
		if(this.modeloPc == null)
			return false;
		if(this.discos.size() < PcBuilder.MIN_DISCOS)
			return false;
		if(this.monitores.size() < PcBuilder.MIN_MONITORES)
			return false;
		if(this.tarjetas.size() < PcBuilder.MIN_TARJETAS)
			return false;
		
		return true;
	}
	
	/**
	 * Define la marca y modelo de la PC.
	 *
	 * @param marca La marca de la PC
	 * @param modelo El modelo de la PC
	 * @return Esta instancia del builder para encadenamiento
	 */
	public PcBuilder definirMarcaYmodelo(String marca, String modelo) {
		this.marcaPc = marca;
		this.modeloPc = modelo;
		return this;
	}
	
	/**
	 * Define el identificador de la PC.
	 *
	 * @param id El ID de la PC
	 * @return Esta instancia del builder para encadenamiento
	 */
	public PcBuilder definirId(String id) {
		this.idPc = id;
		return this;
	}
		
	/**
	 * Define la descripción de la PC.
	 *
	 * @param descripcion La descripción de la PC
	 * @return Esta instancia del builder para encadenamiento
	 */
	public PcBuilder definirDescripcion(String descripcion) {
		this.descripcionPc = descripcion;
		return this;
	}
	
	/**
	 * Construye y devuelve la PC con la configuración actual.
	 *
	 * @return La PC construida
	 */
	public Pc build() {
		//if(this.pcEsValida() == false) {
			//throw new RuntimeException("Estructura Pc Invalida [" + this.toString() + "]");
		//}
		Pc pc = new Pc(this);
		return pc;
	}

	/**
	 * Representación en cadena del estado actual del builder.
	 *
	 * @return Cadena con la información del builder
	 */
	@Override
	public String toString() {
		return "\nPcBuilder["
				+ "\n   monitores(" + monitores.size()
				+ ") =" + monitores 
				+ "\n   tarjetas(" + tarjetas.size()
				+ ") =" + tarjetas 
				+ "\n   discos(" + discos.size()
				+ ") =" + discos 
				+ "\n   idPc=" + idPc
				+ ", descripcionPc=" + descripcionPc 
				+ ", marcaPc=" + marcaPc 
				+ ", modeloPc=" + modeloPc + "]\n";
	}
	
}