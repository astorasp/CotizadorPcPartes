package mx.com.qtx.cotizador.servicio.wrapper;

import mx.com.qtx.cotizador.dominio.core.componentes.Componente;
import mx.com.qtx.cotizador.dominio.core.componentes.IPromocion;
import mx.com.qtx.cotizador.dominio.core.componentes.ComponenteCpu;
import mx.com.qtx.cotizador.dominio.core.componentes.ComponenteGpu;
import mx.com.qtx.cotizador.dominio.core.componentes.ComponenteHdd;
import mx.com.qtx.cotizador.dominio.core.componentes.ComponenteMonitor;
import mx.com.qtx.cotizador.dominio.core.componentes.ComponenteRam;
import mx.com.qtx.cotizador.dominio.core.componentes.ComponenteSsd;

/**
 * Converter para transformar entidades JPA de componentes a objetos de dominio.
 * Se encarga de crear el tipo específico de componente e incluir su promoción.
 */
public class ComponenteEntityConverter {
    
    /**
     * Convierte una entidad JPA Componente a un objeto de dominio.
     * Determina el tipo específico de componente basado en el tipo y crea la instancia correcta.
     * 
     * @param entidadComponente Entidad JPA de componente
     * @return Objeto de dominio del tipo específico de componente
     */
    public static Componente convertirADominio(mx.com.qtx.cotizador.entidad.Componente entidadComponente) {
        if (entidadComponente == null) {
            throw new IllegalArgumentException("La entidad componente no puede ser null");
        }
        
        // Convertir promoción si existe
        IPromocion promocion = null;
        if (entidadComponente.getPromocion() != null) {
            promocion = PromocionEntityConverter.convertirADominio(entidadComponente.getPromocion());
        }
        
        // Determinar el tipo de componente y crear la instancia específica
        String tipoComponente = obtenerTipoComponente(entidadComponente);
        
        return crearComponenteEspecifico(
            tipoComponente,
            entidadComponente.getId(),
            entidadComponente.getDescripcion(),
            entidadComponente.getPrecioBase(),
            entidadComponente.getMarca(),
            entidadComponente.getModelo(),
            promocion
        );
    }
    
    /**
     * Obtiene el tipo de componente desde la entidad.
     * Si no tiene tipo específico, asume CPU por defecto.
     */
    private static String obtenerTipoComponente(mx.com.qtx.cotizador.entidad.Componente entidad) {
        if (entidad.getTipoComponente() != null && entidad.getTipoComponente().getNombre() != null) {
            return entidad.getTipoComponente().getNombre().toUpperCase();
        }
        
        // Si no hay tipo específico, inferir del ID o descripción
        String id = entidad.getId().toLowerCase();
        String descripcion = entidad.getDescripcion().toLowerCase();
        
        if (id.contains("cpu") || descripcion.contains("procesador") || descripcion.contains("cpu")) {
            return "CPU";
        } else if (id.contains("gpu") || id.contains("vga") || descripcion.contains("grafica") || descripcion.contains("video")) {
            return "GPU";
        } else if (id.contains("ram") || id.contains("memoria") || descripcion.contains("memoria")) {
            return "RAM";
        } else if (id.contains("ssd") || descripcion.contains("ssd")) {
            return "SSD";
        } else if (id.contains("hdd") || descripcion.contains("disco duro") || descripcion.contains("hdd")) {
            return "HDD";
        } else if (id.contains("monitor") || descripcion.contains("monitor") || descripcion.contains("pantalla")) {
            return "MONITOR";
        }
        
        // Por defecto, asumir CPU
        return "CPU";
    }
    
    /**
     * Crea la instancia específica del tipo de componente.
     */
    private static Componente crearComponenteEspecifico(String tipo, String id, String descripcion, 
            java.math.BigDecimal precio, String marca, String modelo, IPromocion promocion) {
        
        switch (tipo) {
            case "CPU":
            case "PROCESADOR":
                return new ComponenteCpu(id, descripcion, precio, marca, modelo, promocion);
                
            case "GPU":
            case "VGA":
            case "TARJETA_GRAFICA":
                return new ComponenteGpu(id, descripcion, precio, marca, modelo, promocion);
                
            case "RAM":
            case "MEMORIA":
                return new ComponenteRam(id, descripcion, precio, marca, modelo, promocion);
                
            case "SSD":
                return new ComponenteSsd(id, descripcion, precio, marca, modelo, promocion);
                
            case "HDD":
            case "DISCO_DURO":
                return new ComponenteHdd(id, descripcion, precio, marca, modelo, promocion);
                
            case "MONITOR":
            case "PANTALLA":
                return new ComponenteMonitor(id, descripcion, precio, marca, modelo, promocion);
                
            default:
                // Para tipos no reconocidos, crear un CPU genérico
                return new ComponenteCpu(id, descripcion, precio, marca, modelo, promocion);
        }
    }
    
    /**
     * Método de conveniencia para conversión con logging de debugging.
     */
    public static Componente convertirADominioConDebug(mx.com.qtx.cotizador.entidad.Componente entidadComponente, boolean debug) {
        if (debug && entidadComponente != null) {
            String tipo = obtenerTipoComponente(entidadComponente);
            String promocionInfo = entidadComponente.getPromocion() != null ? 
                PromocionEntityConverter.obtenerInfoTipoPromocion(entidadComponente.getPromocion()) : 
                "Sin promoción";
            System.out.println(String.format("DEBUG ComponenteEntityConverter: %s [%s] -> %s, Promoción: %s", 
                entidadComponente.getId(), entidadComponente.getDescripcion(), tipo, promocionInfo));
        }
        
        return convertirADominio(entidadComponente);
    }
    
    /**
     * Verifica si una entidad de componente puede ser convertida exitosamente.
     */
    public static boolean puedeConvertir(mx.com.qtx.cotizador.entidad.Componente entidadComponente) {
        if (entidadComponente == null) {
            return false;
        }
        
        try {
            convertirADominio(entidadComponente);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}