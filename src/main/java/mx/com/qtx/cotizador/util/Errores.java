package mx.com.qtx.cotizador.util;

public enum Errores {
    OK("0", "OK"),
    ACCESO_DENEGADO("1", "Acceso denegado"),
    ERROR_DE_VALIDACION("2", "Error de validación"),
    ERROR_INTERNO_DEL_SERVICIO("3", "Servicio no disponible");    

    private final String codigo;
    private final String mensaje;
    
    Errores(String codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public String getCodigo() {
        return codigo;
    }  

    public String getMensaje() {
        return mensaje;
    }
}
