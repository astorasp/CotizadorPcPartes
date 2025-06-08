package mx.com.qtx.cotizador.util;

public enum Errores {
    // Códigos de éxito
    OK("0", "OK"),
    
    // Códigos de negocio/validación (4xx)
    ACCESO_DENEGADO("1", "Acceso denegado"),
    ERROR_DE_VALIDACION("2", "Error de validación"),
    RECURSO_NO_ENCONTRADO("4", "Recurso no encontrado"),
    RECURSO_YA_EXISTE("5", "El recurso ya existe"),
    VALOR_INVALIDO("6", "Valor inválido proporcionado"),
    OPERACION_INVALIDA("7", "Operación no válida"),
    CAMPO_REQUERIDO("8", "Campo requerido faltante"),
    FORMATO_INVALIDO("9", "Formato de datos inválido"),
    REGLA_NEGOCIO_VIOLADA("10", "Regla de negocio violada"),
    
    // Códigos de sistema (5xx)
    ERROR_INTERNO_DEL_SERVICIO("3", "Servicio no disponible"),
    ERROR_BASE_DATOS("11", "Error en base de datos"),
    ERROR_SERVICIO_EXTERNO("12", "Error en servicio externo"),
    ERROR_CONFIGURACION("13", "Error de configuración del sistema");    

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
