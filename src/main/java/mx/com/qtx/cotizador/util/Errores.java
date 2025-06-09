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
    
    // Códigos específicos de cotizaciones
    COTIZACION_NO_ENCONTRADA("20", "Cotización no encontrada"),
    COTIZACION_YA_EXISTE("21", "La cotización ya existe"),
    COTIZACION_SIN_DETALLES("22", "Cotización sin detalles"),
    COTIZACION_INVALIDA("23", "Datos de cotización inválidos"),
    COMPONENTE_NO_ENCONTRADO_EN_COTIZACION("24", "Componente no encontrado en la cotización"),
    RANGO_FECHAS_INVALIDO("25", "Rango de fechas inválido"),
    MONTO_TOTAL_INVALIDO("26", "Monto total inválido"),
    
    // Códigos específicos de proveedores
    PROVEEDOR_NO_ENCONTRADO("30", "Proveedor no encontrado"),
    PROVEEDOR_YA_EXISTE("31", "El proveedor ya existe"),
    PROVEEDOR_CLAVE_INVALIDA("32", "Clave de proveedor inválida"),
    PROVEEDOR_CON_PEDIDOS_ACTIVOS("33", "No se puede eliminar proveedor con pedidos activos"),
    PROVEEDOR_DATOS_INVALIDOS("34", "Datos de proveedor inválidos"),
    
    // Códigos específicos de pedidos
    PEDIDO_NO_ENCONTRADO("40", "Pedido no encontrado"),
    PEDIDO_YA_EXISTE("41", "El pedido ya existe"),
    PEDIDO_SIN_DETALLES("42", "Pedido sin detalles válidos"),
    PROVEEDOR_REQUERIDO_PEDIDO("43", "Proveedor requerido para el pedido"),
    FECHAS_PEDIDO_INVALIDAS("44", "Fechas de pedido inválidas"),
    COTIZACION_NO_ENCONTRADA_PEDIDO("45", "Cotización no encontrada para generar pedido"),
    COTIZACION_INVALIDA_PEDIDO("46", "Cotización inválida para generar pedido"),
    DETALLE_PEDIDO_INVALIDO("47", "Detalle de pedido inválido"),
    
    // Códigos específicos de promociones
    PROMOCION_NO_ENCONTRADA("50", "Promoción no encontrada"),
    PROMOCION_YA_EXISTE("51", "El nombre de promoción ya existe"),
    PROMOCION_SIN_DETALLES("52", "Promoción sin detalles válidos"),
    PROMOCION_FECHAS_INVALIDAS("53", "Fechas de vigencia inválidas"),
    PROMOCION_DETALLE_BASE_REQUERIDO("54", "La promoción debe tener exactamente un detalle base"),
    PROMOCION_PARAMETROS_NXM_INVALIDOS("55", "Parámetros NxM inválidos (llevent debe ser mayor que paguen)"),
    PROMOCION_ESCALAS_DESCUENTO_INVALIDAS("56", "Escalas de descuento por cantidad inválidas"),
    PROMOCION_CON_COMPONENTES_ACTIVOS("57", "No se puede eliminar promoción con componentes asociados"),
    
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
