package mx.com.qtx.cotizador.servicio.wrapper;

import mx.com.qtx.cotizador.entidad.Proveedor;

/**
 * Conversor para transformar objetos Proveedor del dominio de negocio a entidades Proveedor 
 * para persistencia y viceversa.
 */
public class ProveedorEntityConverter {

    /**
     * Convierte una entidad Proveedor en un objeto del dominio Proveedor.
     * 
     * @param proveedorEntity La entidad de persistencia Proveedor
     * @return Un objeto del dominio Proveedor con la información del proveedor
     */
    public static mx.com.qtx.cotizador.dominio.pedidos.Proveedor convertToDomain(
            Proveedor proveedorEntity) {
        
        if (proveedorEntity == null) {
            return null;
        }
        
        // Extraer datos de la entidad
        String cve = proveedorEntity.getCve();
        String nombre = proveedorEntity.getNombre() != null ? 
                proveedorEntity.getNombre() : "Sin nombre";
        
        // Obtener la razón social directamente del campo
        String razonSocial = proveedorEntity.getRazonSocial() != null ?
                proveedorEntity.getRazonSocial() : nombre;
        
        // Crear y devolver el objeto de dominio
        return new mx.com.qtx.cotizador.dominio.pedidos.Proveedor(cve, nombre, razonSocial);
    }
    
    /**
     * Convierte un objeto del dominio Proveedor en una entidad Proveedor.
     * Si el proveedor ya existe en la base de datos (tiene una clave válida), se debe cargar
     * primero y luego actualizar sus propiedades.
     * 
     * @param proveedorDomain El objeto del dominio Proveedor
     * @param existingEntity Entidad existente (opcional, puede ser null para nuevos proveedores)
     * @return Una entidad Proveedor lista para ser persistida
     */
    public static Proveedor convertToEntity(
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor proveedorDomain,
            Proveedor existingEntity) {
        
        if (proveedorDomain == null) {
            return null;
        }
        
        // Usar la entidad existente o crear una nueva
        Proveedor proveedorEntity =
                existingEntity != null ? existingEntity : 
                new Proveedor();
        
        // Establecer las propiedades básicas
        proveedorEntity.setNombre(proveedorDomain.getNombre());
        proveedorEntity.setRazonSocial(proveedorDomain.getRazonSocial());
        
        // Si es un nuevo proveedor, establecemos la clave
        if (existingEntity == null) {
            // Establecer la clave del proveedor
            proveedorEntity.setCve(proveedorDomain.getCve());
        }
        
        return proveedorEntity;
    }
    
    /**
     * Convierte un objeto del dominio Proveedor en una nueva entidad Proveedor.
     * 
     * @param proveedorDomain El objeto del dominio Proveedor
     * @return Una nueva entidad Proveedor lista para ser persistida
     */
    public static Proveedor convertToNewEntity(
            mx.com.qtx.cotizador.dominio.pedidos.Proveedor proveedorDomain) {
        return convertToEntity(proveedorDomain, null);
    }
}