package mx.com.qtx.cotizador.dominio.promos;

import java.math.BigDecimal;
import java.time.LocalDate;
import mx.com.qtx.cotizador.dominio.core.componentes.IPromocion;

/**
 * Clase abstracta base para todas las promociones del sistema.
 * Implementa el patrón Template Method para el cálculo de promociones.
 */
public abstract class Promocion implements IPromocion {
    
    protected Integer idPromocion;
    protected String nombre;
    protected String descripcion;
    protected LocalDate vigenciaDesde;
    protected LocalDate vigenciaHasta;
    
    /**
     * Constructor base para promociones
     */
    protected Promocion(Integer idPromocion, String nombre, String descripcion, 
                       LocalDate vigenciaDesde, LocalDate vigenciaHasta) {
        this.idPromocion = idPromocion;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.vigenciaDesde = vigenciaDesde;
        this.vigenciaHasta = vigenciaHasta;
    }
    
    /**
     * Template method que implementa el algoritmo base de cálculo de promociones.
     * Valida vigencia antes de calcular el descuento específico.
     */
    @Override
    public final BigDecimal calcularImportePromocion(int cantidad, BigDecimal precioBase) {
        if (!esVigente()) {
            return precioBase.multiply(BigDecimal.valueOf(cantidad));
        }
        
        if (cantidad <= 0 || precioBase == null || precioBase.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        
        return calcularDescuentoEspecifico(cantidad, precioBase);
    }
    
    /**
     * Método abstracto que debe implementar cada tipo de promoción específica.
     * Define el algoritmo particular de cálculo de descuento.
     */
    protected abstract BigDecimal calcularDescuentoEspecifico(int cantidad, BigDecimal precioBase);
    
    /**
     * Verifica si la promoción está vigente en la fecha actual.
     */
    protected boolean esVigente() {
        LocalDate hoy = LocalDate.now();
        return (vigenciaDesde == null || !hoy.isBefore(vigenciaDesde)) &&
               (vigenciaHasta == null || !hoy.isAfter(vigenciaHasta));
    }
    
    // Getters para subclases
    public Integer getIdPromocion() {
        return idPromocion;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public LocalDate getVigenciaDesde() {
        return vigenciaDesde;
    }
    
    public LocalDate getVigenciaHasta() {
        return vigenciaHasta;
    }
}