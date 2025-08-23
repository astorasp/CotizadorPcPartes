package mx.com.qtx.cotizador.dto.promocion.response;

/**
 * DTO de respuesta para parámetros de promoción NxM
 * 
 * Encapsula la información de configuración NxM (llevent, paguen)
 * para mantener consistencia con la estructura de request.
 */
public class ParametrosNxMResponse {
    
    private Integer llevent; // Número de unidades a llevar
    private Integer paguen;  // Número de unidades a pagar
    
    // Constructores
    public ParametrosNxMResponse() {
        // Constructor vacío para Jackson
    }
    
    public ParametrosNxMResponse(Integer llevent, Integer paguen) {
        this.llevent = llevent;
        this.paguen = paguen;
    }
    
    // Getters y setters
    public Integer getLlevent() {
        return llevent;
    }
    
    public void setLlevent(Integer llevent) {
        this.llevent = llevent;
    }
    
    public Integer getPaguen() {
        return paguen;
    }
    
    public void setPaguen(Integer paguen) {
        this.paguen = paguen;
    }
    
    @Override
    public String toString() {
        return "ParametrosNxMResponse{" +
                "llevent=" + llevent +
                ", paguen=" + paguen +
                '}';
    }
}