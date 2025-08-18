package mx.com.qtx.cotizador.servicio.wrapper;

import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;

/**
 * Converter para transformar DTOs de respuesta de componentes a objetos de dominio.
 * Respeta la arquitectura en capas: servicio usa DTOs, no entidades directamente.
 */
public class ComponenteResponseConverter {
    
    /**
     * Convierte un ComponenteResponse (DTO) a un objeto de dominio Componente
     */
    public static Componente toDomainObject(ComponenteResponse componenteResponse) {
        if (componenteResponse == null) {
            return null;
        }
        
        // Determinar el tipo de componente y crear la instancia adecuada
        String tipoComponente = componenteResponse.getTipoComponente();
        
        if (tipoComponente == null) {
            tipoComponente = "MONITOR"; // Valor por defecto
        }
        
        switch (tipoComponente.toUpperCase()) {
            case "DISCO_DURO":
                return Componente.crearDiscoDuro(
                    componenteResponse.getId(),
                    componenteResponse.getDescripcion(),
                    componenteResponse.getMarca(),
                    componenteResponse.getModelo(),
                    componenteResponse.getCosto(),
                    componenteResponse.getPrecioBase(),
                    componenteResponse.getCapacidadAlm()
                );
                
            case "TARJETA_VIDEO":
                return Componente.crearTarjetaVideo(
                    componenteResponse.getId(),
                    componenteResponse.getDescripcion(),
                    componenteResponse.getMarca(),
                    componenteResponse.getModelo(),
                    componenteResponse.getCosto(),
                    componenteResponse.getPrecioBase(),
                    componenteResponse.getMemoria()
                );
                
            case "PC":
                // Crear PC usando PcBuilder
                return Componente.getPcBuilder()
                    .definirId(componenteResponse.getId())
                    .definirDescripcion(componenteResponse.getDescripcion())
                    .definirMarcaYmodelo(componenteResponse.getMarca(), componenteResponse.getModelo())
                    .build();
                
            case "MONITOR":
            default:
                return Componente.crearMonitor(
                    componenteResponse.getId(),
                    componenteResponse.getDescripcion(),
                    componenteResponse.getMarca(),
                    componenteResponse.getModelo(),
                    componenteResponse.getCosto(),
                    componenteResponse.getPrecioBase()
                );
        }
    }
    

} 