package mx.com.qtx.cotizador.dominio.core.componentes;

import java.math.BigDecimal;

/**
 * Clase abstracta base para todos los componentes del sistema.
 * Versión simplificada para el microservicio de cotizaciones.
 * Incluye soporte para promociones.
 */
public abstract class Componente {
    protected String id;
    protected String descripcion;
    protected String marca;
    protected String modelo;
    protected BigDecimal costo;
    protected BigDecimal precioBase;
    protected IPromocion promocion;
    
    /**
     * Constructor para crear un componente con sus propiedades básicas.
     */
    public Componente(String id, String descripcion, BigDecimal precioBase, String marca, String modelo) {
        this.id = id;
        this.descripcion = descripcion;
        this.marca = marca;
        this.modelo = modelo;
        this.precioBase = precioBase;
        this.costo = precioBase; // Por simplicidad, asumimos que costo = precio
        this.promocion = null; // Sin promoción por defecto
    }
    
    /**
     * Constructor completo que incluye promoción.
     */
    public Componente(String id, String descripcion, BigDecimal precioBase, String marca, String modelo, IPromocion promocion) {
        this(id, descripcion, precioBase, marca, modelo);
        this.promocion = promocion;
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
    
    public IPromocion getPromocion() { return promocion; }
    public void setPromocion(IPromocion promocion) { this.promocion = promocion; }

    /**
     * Calcula la utilidad del componente como la diferencia entre el precio base y el costo.
     */
    public BigDecimal calcularUtilidad() {
        return precioBase.subtract(costo);
    }

    /**
     * Calcula el precio total de una cantidad específica de este componente SIN aplicar promoción.
     */
    public BigDecimal cotizar(int cantidad) {
        return this.precioBase.multiply(new BigDecimal(cantidad));
    }
    
    /**
     * Calcula el precio total de una cantidad específica aplicando la promoción si existe.
     */
    public BigDecimal cotizarConPromocion(int cantidad) {
        if (promocion != null) {
            return promocion.calcularImportePromocion(cantidad, precioBase);
        }
        return cotizar(cantidad);
    }
    
    /**
     * Calcula el descuento total aplicado por la promoción.
     */
    public BigDecimal calcularDescuentoPromocion(int cantidad) {
        BigDecimal precioSinPromocion = cotizar(cantidad);
        BigDecimal precioConPromocion = cotizarConPromocion(cantidad);
        return precioSinPromocion.subtract(precioConPromocion);
    }
    
    /**
     * Verifica si el componente tiene una promoción activa.
     */
    public boolean tienePromocion() {
        return promocion != null;
    }

    /**
     * Obtiene la categoría del componente.
     */
    public abstract String getCategoria();

    @Override
    public String toString() {
        return "Componente [id=" + id + ", descripcion=" + descripcion + ", marca=" + marca + ", modelo=" + modelo
                + ", costo=" + costo + ", precioBase=" + precioBase + "]";
    }
}