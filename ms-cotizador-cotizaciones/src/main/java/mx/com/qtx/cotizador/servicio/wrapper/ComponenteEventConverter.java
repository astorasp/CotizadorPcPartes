package mx.com.qtx.cotizador.servicio.wrapper;

import mx.com.qtx.cotizador.entidad.Componente;
import mx.com.qtx.cotizador.entidad.TipoComponente;
import mx.com.qtx.cotizador.entidad.Promocion;
import mx.com.qtx.cotizador.kafka.dto.ComponenteChangeEvent;
import mx.com.qtx.cotizador.repositorio.PromocionRepositorio;
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
     * @param promocionRepositorio Repositorio para buscar promociones por ID
     * @return Entidad Componente lista para persistir
     */
    public static Componente toEntity(ComponenteChangeEvent event, TipoComponente tipoComponente, PromocionRepositorio promocionRepositorio) {
        if (event == null) {
            return null;
        }
        
        Componente componente = new Componente();
        
        // Campos básicos del evento
        componente.setId(event.getEntityId().toString());
        componente.setDescripcion(event.getDescripcion());
        componente.setMarca(event.getMarca());
        componente.setModelo(event.getModelo());
        
        // Campos específicos del evento
        if (event.getEspecificaciones() != null) {
            componente.setCapacidadAlm(extractEspecificacion(event.getEspecificaciones(), "capacidad"));
            componente.setMemoria(extractEspecificacion(event.getEspecificaciones(), "memoria"));
        }
        
        // Precios - convertir Double a BigDecimal
        if (event.getPrecioBase() != null) {
            BigDecimal precioBase = BigDecimal.valueOf(event.getPrecioBase());
            componente.setPrecioBase(precioBase);
        }
        
        // Costo - usar directamente del evento
        if (event.getCosto() != null) {
            BigDecimal costo = BigDecimal.valueOf(event.getCosto());
            componente.setCosto(costo);
        }
        
        // Relación con TipoComponente
        componente.setTipoComponente(tipoComponente);
        
        // Promoción - buscar por ID si viene en el evento
        if (event.getPromocionId() != null && promocionRepositorio != null) {
            try {
                Promocion promocion = promocionRepositorio.findById(event.getPromocionId().intValue()).orElse(null);
                componente.setPromocion(promocion);
            } catch (Exception e) {
                // Si no se encuentra la promoción, dejar como null
                componente.setPromocion(null);
            }
        } else {
            componente.setPromocion(null);
        }
        
        return componente;
    }
    
    /**
     * Extrae una especificación específica del texto de especificaciones.
     * Formato esperado: "capacidad:1TB;memoria:16GB;velocidad:3200MHz"
     * 
     * @param especificaciones Texto con especificaciones separadas por punto y coma
     * @param clave Clave a buscar (ej: "capacidad", "memoria")
     * @return Valor de la especificación o null si no se encuentra
     */
    private static String extractEspecificacion(String especificaciones, String clave) {
        if (especificaciones == null || especificaciones.isEmpty() || clave == null) {
            return null;
        }
        
        // Buscar patrón "clave:valor"
        String[] partes = especificaciones.split(";");
        for (String parte : partes) {
            if (parte.contains(":")) {
                String[] claveValor = parte.split(":", 2);
                if (claveValor.length == 2 && claveValor[0].trim().equalsIgnoreCase(clave)) {
                    return claveValor[1].trim();
                }
            }
        }
        
        return null;
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