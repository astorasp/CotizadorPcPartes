package mx.com.qtx.cotizadorv1ds.promos;

import java.math.BigDecimal;
import java.util.Map;

import mx.com.qtx.cotizadorv1ds.core.componentes.IPromocion;

/**
 * @author hp835
 * @version 1.0
 * @created 24-mar.-2025 11:16:12 p. m.
 */
public abstract class Promocion implements IPromocion{

	private String descripcion;
	private String nombre;

 
	public Promocion(String descripcion, String nombre) {
		super();
		this.descripcion = descripcion;
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * 
	 * @param cant
	 * @param precioBase
	 */
	public abstract BigDecimal calcularImportePromocion(int cant, BigDecimal precioBase);

	/**
	 * 
	 * @param builder
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

	public static PromocionBuilder getBuilder() {
		return new PromocionBuilder();
	}
	public static void mostrarEstructuraPromocion(Promocion prom) {
		System.out.println("\n---------------------------------------------------------------------------------------------");
		mostrarElemEstructuraPromocion(prom);
		System.out.println("---------------------------------------------------------------------------------------------\n");		
	}
	
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