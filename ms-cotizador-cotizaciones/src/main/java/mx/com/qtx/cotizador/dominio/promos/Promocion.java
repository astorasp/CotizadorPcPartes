package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;
import java.util.Map;

import mx.com.qtx.cotizador.dominio.core.componentes.IPromocion;

/**
 * Clase abstracta base que representa una promoción en el sistema de cotización.
 * Implementa la interfaz IPromocion y proporciona la estructura básica para
 * diferentes tipos de promociones con descripción y nombre.
 *
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:16:12 p. m.
 */
public abstract class Promocion implements IPromocion{

	/** Descripción detallada de la promoción */
	private String descripcion;
	/** Nombre identificativo de la promoción */
	private String nombre;

 
	/**
	 * Constructor que inicializa una promoción con descripción y nombre.
	 *
	 * @param descripcion Descripción detallada de la promoción
	 * @param nombre Nombre identificativo de la promoción
	 * @throws IllegalArgumentException si la descripción o nombre son inválidos
	 */
	public Promocion(String descripcion, String nombre) {
		super();
		ValidationUtils.validateNotNullOrEmpty(descripcion, "descripcion");
		ValidationUtils.validateNotNullOrEmpty(nombre, "nombre");
		this.descripcion = descripcion;
		this.nombre = nombre;
	}

	/**
	 * Obtiene la descripción de la promoción.
	 *
	 * @return La descripción de la promoción
	 */
	public String getDescripcion() {
		return descripcion;
	}

	/**
	 * Establece la descripción de la promoción.
	 *
	 * @param descripcion La descripción a establecer
	 * @throws IllegalArgumentException si la descripción es inválida
	 */
	public void setDescripcion(String descripcion) {
		ValidationUtils.validateNotNullOrEmpty(descripcion, "descripcion");
		this.descripcion = descripcion;
	}

	/**
	 * Obtiene el nombre de la promoción.
	 *
	 * @return El nombre de la promoción
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Establece el nombre de la promoción.
	 *
	 * @param nombre El nombre a establecer
	 * @throws IllegalArgumentException si el nombre es inválido
	 */
	public void setNombre(String nombre) {
		ValidationUtils.validateNotNullOrEmpty(nombre, "nombre");
		this.nombre = nombre;
	}

	/**
	 * Calcula el importe total de la promoción para una cantidad y precio base dados.
	 * Método abstracto que debe ser implementado por las subclases específicas.
	 *
	 * @param cant Cantidad de unidades del componente
	 * @param precioBase Precio base unitario del componente
	 * @return El importe total calculado aplicando la promoción
	 */
	public abstract BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase);

	/**
	 * Método factory para crear promociones complejas a partir de un builder.
	 * Construye una promoción base y aplica acumulaciones según la configuración del builder.
	 *
	 * @param builder El PromocionBuilder con la configuración de la promoción
	 * @return La promoción construida
	 */
	public static Promocion crearPromocion(PromocionBuilder builder){
	//		System.out.println("crearPromocion(" + builder + ")");
		
			Promocion promoBase = null;
			int tipoPromBase = builder.getTipoPromocionBase();
			switch(tipoPromBase) {
				case PromocionBuilder.PROM_BASE_SIN_DSCTO: 
					promoBase = new PromSinDescto();
					break;
				case PromocionBuilder.PROM_BASE_NXM: 
					promoBase = new PromNXM(builder.getN(), builder.getM());
					break;
				default:
					promoBase = new PromSinDescto();
			}
			
			Promocion promoAcum = promoBase;
			for(Float dsctoPlanoI:builder.getLstDsctosPlanos()) {
				Promocion promDeco = new PromDsctoPlano(promoAcum,dsctoPlanoI);
				promoAcum = promDeco;
			}
			for(Map<Integer,Double> mapDsctosI:builder.getLstMapsCantVsDscto()) {
				Promocion promDeco = new PromDsctoXcantidad(promoAcum, mapDsctosI);
				promoAcum = promDeco;
			}
				
			return promoAcum;
		}

	/**
	 * Obtiene una nueva instancia de PromocionBuilder para construir promociones.
	 *
	 * @return Una nueva instancia de PromocionBuilder
	 */
	public static PromocionBuilder getBuilder() {
		return new PromocionBuilder();
	}

	/**
	 * Muestra en consola la estructura jerárquica de una promoción.
	 * Desglosa las promociones acumulables mostrando su composición.
	 *
	 * @param prom La promoción cuya estructura se va a mostrar
	 */
	public static void mostrarEstructuraPromocion(Promocion prom) {
		System.out.println("\n---------------------------------------------------------------------------------------------");
		mostrarElemEstructuraPromocion(prom);
		System.out.println("---------------------------------------------------------------------------------------------\n");		
	}
	
	/**
	 * Método auxiliar recursivo para mostrar elementos de la estructura de promoción.
	 *
	 * @param prom La promoción a mostrar
	 */
	private static void mostrarElemEstructuraPromocion(Promocion prom) {
		if(prom instanceof PromBase) {
			System.out.println(prom.getClass().getSimpleName() + ": " + prom.getDescripcion());
		}
		else 
		if(prom instanceof PromAcumulable promAcum) {
			mostrarElemEstructuraPromocion(promAcum.promoBase);
			System.out.println(prom.getClass().getSimpleName() + ": " + prom.getDescripcion());
		}
	}

}