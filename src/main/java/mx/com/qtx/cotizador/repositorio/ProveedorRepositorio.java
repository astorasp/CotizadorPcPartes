package mx.com.qtx.cotizador.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizador.entidad.Proveedor;

import java.util.List;

@Repository
public interface ProveedorRepositorio extends JpaRepository<Proveedor, String> {
    // Encontrar proveedores por nombre
    List<Proveedor> findByNombreContainingIgnoreCase(String nombre);
    
    // Encontrar proveedores por razón social
    List<Proveedor> findByRazonSocialContainingIgnoreCase(String razonSocial);
    
    // Encontrar proveedores por clave
    Proveedor findByCve(String cve);
}
