package mx.com.qtx.cotizadorv1ds.pedidos;

/**
 * Representa a un proveedor de artículos o servicios.
 * Contiene información básica como su clave única, nombre comercial y razón social.
 */
public class Proveedor {
    private String cve;
    private String nombre;
    private String razonSocial;

    /**
     * Construye una nueva instancia de Proveedor.
     *
     * @param cve Clave única que identifica al proveedor.
     * @param nombre Nombre comercial o corto del proveedor.
     * @param razonSocial Razón social completa y legal del proveedor.
     */
    public Proveedor(String cve, String nombre, String razonSocial) {
        this.cve = cve;
        this.nombre = nombre;
        this.razonSocial = razonSocial;
    }

    // Getters
    /**
     * Obtiene la clave única del proveedor.
     * @return La clave del proveedor.
     */
    public String getCve() {
        return cve;
    }

    /**
     * Obtiene el nombre comercial del proveedor.
     * @return El nombre del proveedor.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene la razón social del proveedor.
     * @return La razón social del proveedor.
     */
    public String getRazonSocial() {
        return razonSocial;
    }
} 