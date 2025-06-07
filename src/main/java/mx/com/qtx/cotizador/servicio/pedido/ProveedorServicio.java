package mx.com.qtx.cotizador.servicio.pedido;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.com.qtx.cotizador.dominio.pedidos.Proveedor;
import mx.com.qtx.cotizador.repositorio.ProveedorRepositorio;
import mx.com.qtx.cotizador.servicio.wrapper.ProveedorEntityConverter;

/**
 * Servicio que gestiona las operaciones relacionadas con los proveedores.
 * Utiliza un conversor de entidades para transformar entre las clases del dominio
 * y las entidades de persistencia.
 */
@Service
public class ProveedorServicio {
    private final ProveedorRepositorio proveedorRepositorio;
    
    public ProveedorServicio(ProveedorRepositorio proveedorRepositorio) {
        this.proveedorRepositorio = proveedorRepositorio;
    }
    
    /**
     * Busca un proveedor por su clave y lo convierte a un objeto del dominio.
     * 
     * @param cve La clave del proveedor a buscar
     * @return El objeto Proveedor del dominio o null si no existe
     */
    public Proveedor buscarPorClave(String cve) {
        // Primero recuperamos la entidad de la base de datos
        var proveedorEntity = proveedorRepositorio.findByCve(cve);
        
        // Si no existe, retornamos null
        if (proveedorEntity == null) {
            return null;
        }
        
        // Convertimos la entidad a un objeto del dominio
        return ProveedorEntityConverter.convertToDomain(proveedorEntity);
    }
    
    /**
     * Guarda un proveedor en la base de datos.
     * Si el proveedor ya existe (tiene un ID válido), se actualiza;
     * de lo contrario, se crea uno nuevo.
     * 
     * @param proveedor El proveedor del dominio a guardar
     * @return El proveedor guardado, convertido de nuevo al dominio
     */
    @Transactional
    public Proveedor guardarProveedor(Proveedor proveedor) {
        if (proveedor == null) {
            return null;
        }
        
        // Convertir a entidad para persistir
        var proveedorEntity = ProveedorEntityConverter.convertToNewEntity(proveedor);
        
        // Persistir la entidad
        proveedorEntity = proveedorRepositorio.save(proveedorEntity);
        
        // Convertir de vuelta a objeto del dominio y retornar
        return ProveedorEntityConverter.convertToDomain(proveedorEntity);
    }
    
    /**
     * Actualiza un proveedor existente en la base de datos.
     * 
     * @param cve La clave del proveedor a actualizar
     * @param proveedor El proveedor con los datos actualizados
     * @return El proveedor actualizado, o null si no existe
     */
    @Transactional
    public Proveedor actualizarProveedor(String cve, Proveedor proveedor) {
        // Verificar que el proveedor exista
        var proveedorEntity = proveedorRepositorio.findByCve(cve);
        if (proveedorEntity == null) {
            return null;
        }
        
        // Actualizar la entidad con los datos del dominio
        proveedorEntity = ProveedorEntityConverter.convertToEntity(proveedor, proveedorEntity);
        
        // Persistir los cambios
        proveedorEntity = proveedorRepositorio.save(proveedorEntity);
        
        // Convertir de vuelta a objeto del dominio
        return ProveedorEntityConverter.convertToDomain(proveedorEntity);
    }
    
    /**
     * Elimina un proveedor de la base de datos.
     * 
     * @param cve La clave del proveedor a eliminar
     * @return true si el proveedor fue eliminado, false si no existía
     */
    @Transactional
    public boolean eliminarProveedor(String cve) {
        if (!proveedorRepositorio.existsById(cve)) {
            return false;
        }
        
        proveedorRepositorio.deleteById(cve);
        return true;
    }
    
    /**
     * Obtiene todos los proveedores de la base de datos, convertidos a objetos del dominio.
     * 
     * @return Lista de todos los proveedores como objetos del dominio
     */
    public List<Proveedor> obtenerTodos() {
        List<Proveedor> proveedoresDominio = new ArrayList<>();
        
        // Recuperar todas las entidades
        var proveedoresEntidad = proveedorRepositorio.findAll();
        
        // Convertir cada entidad a objeto del dominio
        for (var proveedorEntity : proveedoresEntidad) {
            proveedoresDominio.add(ProveedorEntityConverter.convertToDomain(proveedorEntity));
        }
        
        return proveedoresDominio;
    }
}