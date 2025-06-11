package mx.com.qtx.cotizador.dominio.core.componentes;
import java.math.BigDecimal;
import java.util.List;

/**
 * Clase abstracta base para todos los componentes del sistema.
 * Representa un componente genérico con propiedades básicas como identificador,
 * descripción, marca, modelo, costo y precio base.
 * Clases específicas como Monitor, DiscoDuro, TarjetaVideo y Pc heredan de esta clase.
 */
public abstract class Componente {
    protected String id;
    protected String descripcion;
    protected String marca;
    protected String modelo;
    protected BigDecimal costo;
    protected BigDecimal precioBase;
    
    protected IPromocion promo;
    
    /**
     * Constructor para crear un componente con sus propiedades básicas.
     * 
     * @param id Identificador único del componente
     * @param descripcion Descripción detallada del componente
     * @param marca Marca del componente
     * @param modelo Modelo específico del componente
     * @param costo Costo de adquisición del componente
     * @param precioBase Precio base de venta del componente
     */
    public Componente(String id, String descripcion, String marca, String modelo, 
                     BigDecimal costo, BigDecimal precioBase) {
        this.id = id;
        this.descripcion = descripcion;
        this.marca = marca;
        this.modelo = modelo;
        this.costo = costo;
        this.precioBase = precioBase;
    }
 
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }

    public BigDecimal getPrecioBase() { return precioBase; }
    public void setPrecioBase(BigDecimal precioBase) { this.precioBase = precioBase; }


    /**
     * Obtiene la promoción aplicada al componente.
     * 
     * @return La promoción actual del componente o null si no tiene ninguna
     */
	public IPromocion getPromo() {
		return promo;
	}

    /**
     * Establece una promoción para este componente.
     * 
     * @param promo La promoción a aplicar al componente
     */
	public void setPromo(IPromocion promo) {
		this.promo = promo;
	}

	// Métodos
    /**
     * Muestra en consola las características principales del componente.
     * Incluye ID, categoría, descripción, marca, modelo, costo, precio base y utilidad.
     * Este método puede ser sobrescrito por clases hijas para mostrar información específica.
     */
    public void mostrarCaracteristicas() {
        System.out.println("ID: " + id);
        System.out.println("Categoría: " + this.getCategoria());
        System.out.println("Descripción: " + descripcion);
        System.out.println("Marca: " + marca);
        System.out.println("Modelo: " + modelo);
        System.out.println("Costo: $" + costo);
        System.out.println("Precio Base: $" + precioBase);
        System.out.println("Utilidad: " + this.calcularUtilidad());
               
    }

    /**
     * Calcula la utilidad del componente como la diferencia entre el precio base y el costo.
     * 
     * @return La utilidad calculada del componente
     */
    public BigDecimal calcularUtilidad() {
        return precioBase.subtract(costo);
    }

    /**
     * Calcula el precio total de una cantidad específica de este componente.
     * Si el componente tiene una promoción asignada, utiliza el cálculo de la promoción.
     * Si no tiene promoción, multiplica el precio base por la cantidad.
     * Este método es final para que no pueda ser sobrescrito por las clases hijas.
     * 
     * @param cantidadI Cantidad de unidades a cotizar
     * @return El importe total de la cotización para la cantidad especificada
     */
	final public BigDecimal cotizar(int cantidadI) {
		if(this.promo == null)
			return this.precioBase.multiply(new BigDecimal(cantidadI));
		else
			return this.promo.calcularImportePromocion(cantidadI, this.precioBase);
	}

	/**
	 * Obtiene la categoría del componente.
	 * Cada tipo específico de componente debe implementar este método para
	 * proporcionar su categoría particular.
	 * 
	 * @return La categoría del componente como cadena de texto
	 */
	public abstract String getCategoria();
	
	/**
	 * Método factory para crear un disco duro.
	 * Este método estático facilita la creación de discos duros con los parámetros especificados.
	 * 
	 * @param id Identificador único del disco duro
	 * @param descripcion Descripción detallada del disco duro
	 * @param marca Marca del disco duro
	 * @param modelo Modelo específico del disco duro
	 * @param costo Costo de adquisición del disco duro
	 * @param precioBase Precio base de venta del disco duro
	 * @param capacidadAlm Capacidad de almacenamiento del disco duro
	 * @return Un nuevo componente de tipo DiscoDuro
	 */
	public static Componente crearDiscoDuro(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase, String capacidadAlm) {
		Componente disco = new DiscoDuro(id,descripcion,marca,modelo,costo,precioBase,capacidadAlm);
		return disco;
	}
	
	/**
	 * Método factory para crear un monitor.
	 * Este método estático facilita la creación de monitores con los parámetros especificados.
	 * 
	 * @param id Identificador único del monitor
	 * @param descripcion Descripción detallada del monitor
	 * @param marca Marca del monitor
	 * @param modelo Modelo específico del monitor
	 * @param costo Costo de adquisición del monitor
	 * @param precioBase Precio base de venta del monitor
	 * @return Un nuevo componente de tipo Monitor
	 */
	public static Componente crearMonitor(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase) {
		Componente monitor = new Monitor(id,descripcion, marca, modelo, costo,precioBase);	
		return monitor;
	}

	/**
	 * Método factory para crear una tarjeta de video.
	 * Este método estático facilita la creación de tarjetas de video con los parámetros especificados.
	 * 
	 * @param id Identificador único de la tarjeta de video
	 * @param descripcion Descripción detallada de la tarjeta de video
	 * @param marca Marca de la tarjeta de video
	 * @param modelo Modelo específico de la tarjeta de video
	 * @param costo Costo de adquisición de la tarjeta de video
	 * @param precioBase Precio base de venta de la tarjeta de video
	 * @param memoria Cantidad de memoria de la tarjeta de video
	 * @return Un nuevo componente de tipo TarjetaVideo
	 */
	public static Componente crearTarjetaVideo(String id, String descripcion, String marca, String modelo, BigDecimal costo,
			BigDecimal precioBase, String memoria) {
		Componente tarjeta = new TarjetaVideo(id, descripcion, marca, modelo, costo,
				precioBase, memoria);
		return tarjeta;
	}

	/**
	 * Método factory para crear una PC compuesta por varios subcomponentes.
	 * Este método estático facilita la creación de PCs con los parámetros especificados.
	 * Filtra los subcomponentes para asegurar que solo los componentes simples sean incluidos.
	 * 
	 * @param id Identificador único de la PC
	 * @param descripcion Descripción detallada de la PC
	 * @param marca Marca de la PC
	 * @param modelo Modelo específico de la PC
	 * @param subComponentes Lista de componentes que forman parte de la PC
	 * @return Un nuevo componente de tipo Pc
	 */
	public static Componente crearPc(String id, String descripcion, String marca, String modelo, 
			List<Componente> subComponentes) {
		List<ComponenteSimple> lstDispositivos = subComponentes.stream()
				                                          .filter(cmpI->(cmpI instanceof ComponenteSimple))
		                                                  .map(dispI -> (ComponenteSimple) dispI)
		                                                  .toList();
		Componente pc = new Pc(id, descripcion, marca, modelo, lstDispositivos);
		return pc;
	}
	
	/**
	 * Obtiene un nuevo constructor de PCs (PcBuilder).
	 * Este método implementa el patrón Builder para facilitar la construcción de PCs
	 * de manera más fluida y con una sintaxis más clara.
	 * 
	 * @return Una nueva instancia de PcBuilder para construir PCs
	 */
	public static PcBuilder getPcBuilder() {
		return new PcBuilder();
	}

	@Override
	public String toString() {
		return "Componente [id=" + id + ", descripcion=" + descripcion + ", marca=" + marca + ", modelo=" + modelo
				+ ", costo=" + costo + ", precioBase=" + precioBase + "]";
	}
	
	
}