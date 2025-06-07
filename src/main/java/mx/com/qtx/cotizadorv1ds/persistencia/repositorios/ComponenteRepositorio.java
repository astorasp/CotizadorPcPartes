package mx.com.qtx.cotizadorv1ds.persistencia.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizadorv1ds.persistencia.entidades.Componente;
import java.util.List;
import java.math.BigDecimal;

/**
 * Repositorio JPA para la entidad Componente.
 * <p>
 * Proporciona operaciones de persistencia para los componentes, incluyendo consultas
 * personalizadas para buscar componentes por tipo, marca y rango de precio. Extiende
 * JpaRepository para heredar operaciones CRUD estándar.
 * </p>
 */
@Repository
public interface ComponenteRepositorio extends JpaRepository<Componente, String> {
    /**
     * Encuentra componentes por su tipo de componente.
     * <p>
     * Permite buscar todos los componentes que pertenecen a una categoría o tipo específico,
     * identificado por su ID. Útil para filtrar componentes por categoría como monitores,
     * discos duros, tarjetas de video, etc.
     * </p>
     * 
     * @param idTipoComponente ID del tipo de componente a buscar
     * @return Lista de componentes que pertenecen al tipo especificado
     */
    List<Componente> findByTipoComponenteId(Short idTipoComponente);
    
    /**
     * Encuentra componentes cuya marca contiene el texto especificado, ignorando mayúsculas y minúsculas.
     * <p>
     * Permite realizar búsquedas parciales de texto en el campo marca, facilitando
     * la búsqueda de componentes sin necesidad de conocer la marca exacta o completa.
     * La búsqueda es insensible a mayúsculas/minúsculas para mayor flexibilidad.
     * </p>
     * 
     * @param marca Texto a buscar dentro del campo marca de los componentes
     * @return Lista de componentes cuya marca contiene el texto especificado
     */
    List<Componente> findByMarcaContainingIgnoreCase(String marca);
    
    /**
     * Encuentra componentes cuyo precio base está dentro del rango especificado.
     * <p>
     * Permite filtrar componentes por un rango de precios, lo que facilita la búsqueda
     * de componentes dentro de determinados límites presupuestarios.
     * </p>
     * 
     * @param min Límite inferior del rango de precios (inclusive)
     * @param max Límite superior del rango de precios (inclusive)
     * @return Lista de componentes cuyo precio base está dentro del rango especificado
     */
    List<Componente> findByPrecioBaseBetween(BigDecimal min, BigDecimal max);

    /**
     * Elimina un componente por su ID.
     * <p>
     * Sobrescribe el método estándar de JpaRepository para garantizar la correcta
     * eliminación de un componente basado en su identificador único.
     * </p>
     * 
     * @param id Identificador único del componente a eliminar
     */
    void deleteById(String id);
}
