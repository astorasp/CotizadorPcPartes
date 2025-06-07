package mx.com.qtx.cotizador.persistencia.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mx.com.qtx.cotizador.persistencia.entidades.Cotizacion;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio JPA para la entidad Cotizacion.
 * <p>
 * Proporciona operaciones de persistencia para las cotizaciones, incluyendo consultas
 * personalizadas para buscar cotizaciones por diferentes criterios como fecha, monto total
 * y componentes incluidos. Extiende JpaRepository para heredar operaciones CRUD estándar.
 * </p>
 */
@Repository
public interface CotizacionRepositorio extends JpaRepository<Cotizacion, Integer> {
    /**
     * Encuentra cotizaciones que contienen la cadena de fecha especificada.
     * <p>
     * Permite buscar cotizaciones cuyo campo fecha incluya el patrón de texto proporcionado,
     * lo que facilita búsquedas parciales o por periodos específicos.
     * </p>
     * 
     * @param fecha Patrón de texto a buscar en el campo fecha de las cotizaciones
     * @return Lista de cotizaciones que coinciden con el criterio de búsqueda
     */
    List<Cotizacion> findByFechaContaining(String fecha);
    
    /**
     * Encuentra cotizaciones cuyo monto total está dentro del rango especificado.
     * <p>
     * Permite filtrar cotizaciones por un rango de valores monetarios, facilitando
     * la identificación de cotizaciones dentro de determinados límites de precios.
     * </p>
     * 
     * @param min Límite inferior del rango de búsqueda (inclusive)
     * @param max Límite superior del rango de búsqueda (inclusive)
     * @return Lista de cotizaciones cuyo monto total está dentro del rango especificado
     */
    List<Cotizacion> findByTotalBetween(BigDecimal min, BigDecimal max);
    
    /**
     * Encuentra cotizaciones cuyo monto total es mayor que el valor especificado.
     * <p>
     * Útil para identificar cotizaciones de alto valor o que superan un umbral monetario determinado.
     * </p>
     * 
     * @param minTotal Valor mínimo que deben superar las cotizaciones a buscar
     * @return Lista de cotizaciones con monto total mayor al valor especificado
     */
    List<Cotizacion> findByTotalGreaterThan(BigDecimal minTotal);
    
    /**
     * Busca cotizaciones que incluyen un componente específico identificado por su ID.
     * <p>
     * Utiliza una consulta JPQL personalizada para conectar las cotizaciones con sus detalles
     * y filtrar aquellas que contienen el componente especificado. Esta consulta implica
     * un join entre las tablas de cotizaciones y detalles.
     * </p>
     * 
     * @param idComponente Identificador único del componente a buscar en las cotizaciones
     * @return Lista de cotizaciones que incluyen el componente especificado
     */
    @Query("SELECT c FROM Cotizacion c JOIN c.detalles d WHERE d.componente.id = :idComponente")
    List<Cotizacion> findCotizacionesByComponente(@Param("idComponente") String idComponente);
}
