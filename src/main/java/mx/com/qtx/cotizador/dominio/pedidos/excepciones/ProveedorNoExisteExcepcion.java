package mx.com.qtx.cotizador.pedidos.excepciones;

/**
 * Excepción lanzada cuando se intenta utilizar una clave de proveedor que no existe
 * en el repositorio o registro de proveedores conocido.
 */
public class ProveedorNoExisteExcepcion extends Exception {

    /**
     * Construye una nueva ProveedorNoExisteExcepcion indicando la clave del proveedor no encontrado.
     *
     * @param cveProveedor La clave del proveedor que no fue encontrado.
     */
    public ProveedorNoExisteExcepcion(String cveProveedor) {
        super("Proveedor con clave " + cveProveedor + " no encontrado.");    
    }

}
