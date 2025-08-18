package mx.com.qtx.cotizador.servicio.wrapper;

import mx.com.qtx.cotizador.entidad.Componente;
import mx.com.qtx.cotizador.entidad.TipoComponente;
import mx.com.qtx.cotizador.entidad.Promocion;
import mx.com.qtx.cotizador.kafka.dto.ComponenteChangeEvent;
import java.math.BigDecimal;

/**
 * Convertidor para transformar eventos Kafka de componentes a entidades JPA.
 * Facilita la sincronización de datos desde ms-cotizador-componentes.
 */
public class ComponenteEventConverter {
    
    /**
     * Convierte un ComponenteChangeEvent de Kafka a una entidad Componente para persistir localmente.
     * 
     * @param event Evento de cambio de componente desde Kafka
     * @param tipoComponente Entidad TipoComponente ya cargada desde la BD local
     * @param promocion Entidad Promocion ya cargada desde la BD local (puede ser null)
     * @return Entidad Componente lista para persistir
     */
    public static Componente toEntity(ComponenteChangeEvent event, TipoComponente tipoComponente, Promocion promocion) {
        if (event == null) {
            return null;
        }
        
        Componente componente = new Componente();
        
        // Campos básicos del evento (ahora usando los campos completos)
        componente.setId(event.getEntityId().toString());
        componente.setDescripcion(event.getDescripcion());
        componente.setMarca(event.getMarca());
        componente.setModelo(event.getModelo());
        componente.setCapacidadAlm(event.getCapacidadAlm());
        componente.setMemoria(event.getMemoria());
        
        // Precios - convertir Double a BigDecimal
        if (event.getPrecioBase() != null) {
            BigDecimal precioBase = BigDecimal.valueOf(event.getPrecioBase());
            componente.setPrecioBase(precioBase);
            
            // Calcular costo como 70% del precio base (regla de negocio típica)
            BigDecimal costo = precioBase.multiply(BigDecimal.valueOf(0.7));
            componente.setCosto(costo);
        }
        
        // Relaciones
        componente.setTipoComponente(tipoComponente);
        componente.setPromocion(promocion);
        
        return componente;
    }
    
    /**
     * Mapea el nombre del tipo de componente desde el evento al nombre usado en la BD local.
     * 
     * @param tipoComponenteEvent Tipo de componente desde el evento
     * @return Nombre normalizado para buscar en la BD local
     */
    public static String mapearTipoComponente(String tipoComponenteEvent) {
        if (tipoComponenteEvent == null) {
            return "MONITOR"; // Tipo por defecto
        }
        
        switch (tipoComponenteEvent.toUpperCase().trim()) {
            case "DISCO_DURO":
            case "HDD":
            case "SSD":
                return "DISCO_DURO";
            case "TARJETA_VIDEO":
            case "TARJETA_GRAFICA": 
            case "GPU":
                return "TARJETA_VIDEO";
            case "MONITOR":
            case "PANTALLA":
                return "MONITOR";
            case "PC":
                return "PC";
            case "RAM":
            case "MEMORIA":
                return "RAM";
            case "CPU":
            case "PROCESADOR":
                return "CPU";
            default:
                return "MONITOR"; // Tipo por defecto para tipos no reconocidos
        }
    }
}