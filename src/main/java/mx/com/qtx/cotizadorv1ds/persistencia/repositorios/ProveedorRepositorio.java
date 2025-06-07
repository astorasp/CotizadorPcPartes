package mx.com.qtx.cotizadorv1ds.persistencia.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizadorv1ds.persistencia.entidades.Proveedor;

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
