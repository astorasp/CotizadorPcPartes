package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Constructor de PCs (Builder Pattern) para ensamblar computadoras con componentes validados.
 * <p>
 * Esta clase implementa el patrón Builder para la construcción fluida y segura de objetos
 * {@link Pc}. Permite agregar componentes de manera incremental aplicando reglas de negocio
 * automáticas para garantizar la compatibilidad y configuración adecuada de las PCs.
 * </p>
 *
 * <h3>Reglas de negocio aplicadas:</h3>
 * <ul>
 *   <li><strong>Mínimo 1 monitor, máximo 2 monitores</strong></li>
 *   <li><strong>Mínimo 1 tarjeta de video, máximo 2 tarjetas de video</strong></li>
 *   <li><strong>Mínimo 1 disco duro, máximo 3 discos duros</strong></li>
 *   <li><strong>Requerido:</strong> ID, descripción, marca y modelo de la PC</li>
 *   <li><strong>Validación automática:</strong> Se ignoran componentes que excedan los límites</li>
 * </ul>
 *
 * <h3>Patrón Builder:</h3>
 * <p>
 * Esta implementación utiliza el patrón Builder para:
 * <ul>
 *   <li>Construcción paso a paso de objetos complejos</li>
 *   <li>Interfaz fluida con métodos encadenables</li>
 *   <li>Validación automática de restricciones</li>
 *   <li>Construcción inmutable del objeto final</li>
 * </ul>
 * </p>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>{@code
 * Pc pcGaming = Componente.getPcBuilder()
 *     .definirId("PC001")
 *     .definirDescripcion("PC Gaming Profesional")
 *     .definirMarcaYmodelo("Custom Build", "Gaming Pro X")
 *     .agregarMonitor("MON001", "Monitor 27\"", "LG", "27UK650", 2500.00, 3500.00)
 *     .agregarDisco("SSD001", "SSD NVMe 1TB", "Samsung", "980 Pro", 1800.00, 2400.00, "1TB")
 *     .agregarTarjetaVideo("GPU001", "RTX 3080", "NVIDIA", "GeForce RTX 3080", 8000.00, 11000.00, "10GB")
 *     .build();
 * }</pre>
 *
 * @author Subagente3F - [2025-01-17 19:30:00 MST]
 * @version 1.0.0
 * @since 1.0.0
 * @see mx.com.qtx.cotizador.dominio.core.componentes.Pc
 * @see mx.com.qtx.cotizador.dominio.core.componentes.Componente#getPcBuilder()
 */
public class PcBuilder {
	public static final int MIN_MONITORES = 1;
	public static final int MAX_MONITORES = 2;
	private List<Monitor> monitores;
	public static final int MIN_TARJETAS = 1;
	public static final int MAX_TARJETAS = 2;
	private List<TarjetaVideo> tarjetas;
	public static final int MIN_DISCOS = 1;
	public static final int MAX_DISCOS = 3;
	private List<DiscoDuro> discos;
	
	private String idPc;
	private String descripcionPc;
	private String marcaPc;
	private String modeloPc;
	
	static int getMinMonitores() {
		return MIN_MONITORES;
	}

	static int getMaxMonitores() {
		return MAX_MONITORES;
	}

	List<Monitor> getMonitores() {
		return monitores;
	}

	static int getMinTarjetas() {
		return MIN_TARJETAS;
	}

	static int getMaxTarjetas() {
		return MAX_TARJETAS;
	}

	List<TarjetaVideo> getTarjetas() {
		return tarjetas;
	}

	static int getMinDiscos() {
		return MIN_DISCOS;
	}

	static int getMaxDiscos() {
		return MAX_DISCOS;
	}

	List<DiscoDuro> getDiscos() {
		return discos;
	}

	String getIdPc() {
		return idPc;
	}

	String getDescripcionPc() {
		return descripcionPc;
	}

	String getMarcaPc() {
		return marcaPc;
	}

	String getModeloPc() {
		return modeloPc;
	}

	PcBuilder() {
		super();
		this.discos = new ArrayList<>();
		this.tarjetas = new ArrayList<>();
		this.monitores = new ArrayList<>();
	}
	
	/**
	 * Agrega un disco duro a la configuración de la PC.
	 * <p>
	 * Este método permite agregar discos duros a la PC en construcción, respetando
	 * el límite máximo de {@value #MAX_DISCOS} discos. Si se intenta agregar más
	 * discos que el límite permitido, el método se ignora silenciosamente y retorna
	 * el builder sin cambios.
	 * </p>
	 *
	 * @param id Identificador único del disco duro
	 * @param descripcion Descripción del disco duro
	 * @param marca Marca del fabricante del disco duro
	 * @param modelo Modelo específico del disco duro
	 * @param costo Costo de adquisición del disco duro
	 * @param precioBase Precio base de venta del disco duro
	 * @param capacidadAlm Capacidad de almacenamiento (ej: "1TB", "500GB")
	 * @return Esta instancia de PcBuilder para encadenamiento de métodos
	 */
	public PcBuilder agregarDisco(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase, String capacidadAlm) {
		if(this.discos.size() == PcBuilder.MAX_DISCOS) //Si excede el max, lo ignora
			return this;
		this.discos.add(new DiscoDuro(id, descripcion, marca, modelo, costo,
				precioBase, capacidadAlm));
		return this;
	}
	
	public PcBuilder agregarMonitor(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase) {
		if(this.monitores.size() == PcBuilder.MAX_MONITORES) //
			return this;
		this.monitores.add(new Monitor(id, descripcion, marca, modelo, costo,precioBase));
		return this;
	}

	public PcBuilder agregarTarjetaVideo(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase, String memoria) {
		if(this.tarjetas.size() == PcBuilder.MAX_TARJETAS) //
			return this;
		this.tarjetas.add(new TarjetaVideo(id, descripcion, marca, modelo, costo,
				precioBase, memoria));
		return this;
	}
	
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
	
	public PcBuilder definirMarcaYmodelo(String marca, String modelo) {
		this.marcaPc = marca;
		this.modeloPc = modelo;
		return this;
	}
	
	public PcBuilder definirId(String id) {
		this.idPc = id;
		return this;
	}
		
	public PcBuilder definirDescripcion(String descripcion) {
		this.descripcionPc = descripcion;
		return this;
	}
	
	/**
	 * Construye y retorna la PC configurada con todos los componentes agregados.
	 * <p>
	 * Este método finaliza el proceso de construcción y crea una instancia de {@link Pc}
	 * con todos los componentes que han sido agregados al builder. La PC resultante
	 * tendrá automáticamente calculados su precio base y costo total basándose en
	 * los componentes incluidos.
	 * </p>
	 *
	 * <h4>Proceso de construcción:</h4>
	 * <ol>
	 *   <li>Validación de componentes mínimos requeridos</li>
	 *   <li>Cálculo automático de precio total de componentes</li>
	 *   <li>Aplicación de descuento por ensamble (20%)</li>
	 *   <li>Creación de la instancia Pc inmutable</li>
	 * </ol>
	 *
	 * <h4>Nota sobre validación:</h4>
	 * <p>
	 * Actualmente la validación está deshabilitada, pero el código incluye
	 * lógica para validar que se cumplan los requisitos mínimos de componentes.
	 * Si se habilita la validación, se lanzará una excepción si faltan componentes
	 * obligatorios o si la configuración es inválida.
	 * </p>
	 *
	 * @return Una nueva instancia de {@link Pc} completamente configurada
	 * @throws RuntimeException si la configuración de la PC es inválida (cuando validación esté habilitada)
	 * @see #pcEsValida()
	 * @see mx.com.qtx.cotizador.dominio.core.componentes.Pc
	 */
	public Pc build() {
		//if(this.pcEsValida() == false) {
			//throw new RuntimeException("Estructura Pc Invalida [" + this.toString() + "]");
		//}
		Pc pc = new Pc(this);
		return pc;
	}

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