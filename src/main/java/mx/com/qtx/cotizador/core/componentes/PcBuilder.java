package mx.com.qtx.cotizadorv1ds.core.componentes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
	
	public Pc build() {
		if(this.pcEsValida() == false) {
			throw new RuntimeException("Estructura Pc Invalida [" + this.toString() + "]");
		}
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