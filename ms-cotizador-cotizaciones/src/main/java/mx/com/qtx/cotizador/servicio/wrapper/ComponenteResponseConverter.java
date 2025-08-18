package mx.com.qtx.cotizador.servicio.wrapper;

import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.core.componentes.ComponenteCpu;
import mx.com.qtx.cotizador.dominio.core.componentes.ComponenteGpu;
import mx.com.qtx.cotizador.dominio.core.componentes.ComponenteRam;
import mx.com.qtx.cotizador.dominio.core.componentes.ComponenteHdd;
import mx.com.qtx.cotizador.dominio.core.componentes.ComponenteSsd;
import mx.com.qtx.cotizador.dominio.core.componentes.ComponenteMonitor;
import mx.com.qtx.cotizador.dto.componente.response.ComponenteResponse;
import mx.com.qtx.cotizador.util.TipoComponenteEnum;

/**
 * Convertidor para transformar ComponenteResponse a objetos de dominio.
 * Maneja la conversión de DTOs de respuesta a entidades de dominio.
 */
public class ComponenteResponseConverter {
    
    /**
     * Convierte un ComponenteResponse a objeto de dominio Componente
     * 
     * @param response DTO de respuesta del componente
     * @return Objeto de dominio correspondiente al tipo de componente
     */
    public static Componente toDomainObject(ComponenteResponse response) {
        if (response == null) {
            return null;
        }
        
        // Determinar el tipo de componente y crear la instancia correspondiente
        String tipoComponente = response.getTipoComponente();
        if (tipoComponente == null) {
            tipoComponente = response.getCategoria(); // Fallback
        }
        
        TipoComponenteEnum tipo = mapearTipoComponente(tipoComponente);
        
        switch (tipo) {
            case CPU:
                return new ComponenteCpu(
                    response.getId(),
                    response.getDescripcion(),
                    response.getPrecio(),
                    response.getMarca(),
                    response.getModelo(),
                    obtenerEspecificacion(response, "arquitectura", "x64"),
                    obtenerEspecificacionInt(response, "nucleos", 4),
                    obtenerEspecificacion(response, "socket", "LGA1151")
                );
                
            case GPU:
                return new ComponenteGpu(
                    response.getId(),
                    response.getDescripcion(),
                    response.getPrecio(),
                    response.getMarca(),
                    response.getModelo(),
                    obtenerEspecificacionInt(response, "memoria", 8),
                    obtenerEspecificacion(response, "tipo_memoria", "GDDR6")
                );
                
            case RAM:
                return new ComponenteRam(
                    response.getId(),
                    response.getDescripcion(),
                    response.getPrecio(),
                    response.getMarca(),
                    response.getModelo(),
                    obtenerEspecificacionInt(response, "capacidad", 16),
                    obtenerEspecificacion(response, "tipo", "DDR4"),
                    obtenerEspecificacionInt(response, "frecuencia", 3200)
                );
                
            case HDD:
                return new ComponenteHdd(
                    response.getId(),
                    response.getDescripcion(),
                    response.getPrecio(),
                    response.getMarca(),
                    response.getModelo(),
                    obtenerEspecificacionInt(response, "capacidad", 1000),
                    obtenerEspecificacionInt(response, "rpm", 7200),
                    obtenerEspecificacion(response, "interfaz", "SATA")
                );
                
            case SSD:
                return new ComponenteSsd(
                    response.getId(),
                    response.getDescripcion(),
                    response.getPrecio(),
                    response.getMarca(),
                    response.getModelo(),
                    obtenerEspecificacionInt(response, "capacidad", 500),
                    obtenerEspecificacion(response, "interfaz", "NVMe"),
                    obtenerEspecificacionInt(response, "velocidad_lectura", 3500)
                );
                
            case MONITOR:
                return new ComponenteMonitor(
                    response.getId(),
                    response.getDescripcion(),
                    response.getPrecio(),
                    response.getMarca(),
                    response.getModelo(),
                    obtenerEspecificacionInt(response, "tamaño", 24),
                    obtenerEspecificacion(response, "resolucion", "1920x1080"),
                    obtenerEspecificacion(response, "tecnologia", "IPS")
                );
                
            default:
                // Crear componente genérico
                return new ComponenteCpu( // Usar CPU como tipo por defecto
                    response.getId(),
                    response.getDescripcion(),
                    response.getPrecio(),
                    response.getMarca(),
                    response.getModelo(),
                    "generic",
                    1,
                    "generic"
                );
        }
    }
    
    /**
     * Mapea el string del tipo de componente a enum
     */
    private static TipoComponenteEnum mapearTipoComponente(String tipo) {
        if (tipo == null) {
            return TipoComponenteEnum.CPU; // Por defecto
        }
        
        switch (tipo.toUpperCase()) {
            case "CPU":
            case "PROCESADOR":
                return TipoComponenteEnum.CPU;
            case "GPU":
            case "TARJETA_GRAFICA":
            case "VGA":
                return TipoComponenteEnum.GPU;
            case "RAM":
            case "MEMORIA":
                return TipoComponenteEnum.RAM;
            case "HDD":
            case "DISCO_DURO":
                return TipoComponenteEnum.HDD;
            case "SSD":
            case "DISCO_SOLIDO":
                return TipoComponenteEnum.SSD;
            case "MONITOR":
            case "PANTALLA":
                return TipoComponenteEnum.MONITOR;
            default:
                return TipoComponenteEnum.CPU; // Por defecto
        }
    }
    
    /**
     * Obtiene una especificación de las especificaciones del componente
     */
    private static String obtenerEspecificacion(ComponenteResponse response, String clave, String valorPorDefecto) {
        String especificaciones = response.getEspecificaciones();
        if (especificaciones == null || especificaciones.isEmpty()) {
            return valorPorDefecto;
        }
        
        // Buscar la clave en las especificaciones (formato simple clave:valor)
        String[] lineas = especificaciones.split("\\n|;|,");
        for (String linea : lineas) {
            if (linea.contains(clave + ":")) {
                String[] partes = linea.split(":");
                if (partes.length > 1) {
                    return partes[1].trim();
                }
            }
        }
        
        return valorPorDefecto;
    }
    
    /**
     * Obtiene una especificación numérica de las especificaciones del componente
     */
    private static Integer obtenerEspecificacionInt(ComponenteResponse response, String clave, Integer valorPorDefecto) {
        String valor = obtenerEspecificacion(response, clave, valorPorDefecto.toString());
        try {
            return Integer.parseInt(valor.replaceAll("[^0-9]", "")); // Extraer solo números
        } catch (NumberFormatException e) {
            return valorPorDefecto;
        }
    }
}