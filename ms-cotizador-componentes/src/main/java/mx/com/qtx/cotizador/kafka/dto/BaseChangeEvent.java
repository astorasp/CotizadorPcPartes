package mx.com.qtx.cotizador.kafka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Clase base para todos los eventos de cambio en Kafka.
 * 
 * Proporciona estructura común para eventos de creación, actualización y eliminación
 * de entidades del microservicio de componentes.
 * 
 * @author Subagente2E - [2025-01-17 16:35:00 MST] - DTOs para eventos Kafka
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "eventType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ComponenteChangeEvent.class, name = "COMPONENTE_CHANGE"),
    @JsonSubTypes.Type(value = PromocionChangeEvent.class, name = "PROMOCION_CHANGE"),
    @JsonSubTypes.Type(value = PcChangeEvent.class, name = "PC_CHANGE")
})
public abstract class BaseChangeEvent {
    
    /**
     * Tipos de operaciones que pueden generar eventos
     */
    public enum OperationType {
        CREATE, UPDATE, DELETE, ADD_COMPONENT, REMOVE_COMPONENT
    }
    
    /**
     * Tipos de eventos disponibles
     */
    public enum EventType {
        COMPONENTE_CHANGE, PROMOCION_CHANGE, PC_CHANGE
    }
    
    private String eventId;
    private EventType eventType;
    private OperationType operationType;
    private String entityId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String source;
    private String version;
    
    /**
     * Constructor base para eventos de cambio
     */
    public BaseChangeEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.source = "ms-cotizador-componentes";
        this.version = "1.0";
    }
    
    public BaseChangeEvent(EventType eventType, OperationType operationType, String entityId) {
        this();
        this.eventType = eventType;
        this.operationType = operationType;
        this.entityId = entityId;
    }
    
    // Getters y Setters
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
    
    public OperationType getOperationType() {
        return operationType;
    }
    
    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }
    
    public String getEntityId() {
        return entityId;
    }
    
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
}