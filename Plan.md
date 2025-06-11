# Plan de implementación: Uso de la lógica de cotización en la capa de servicio

## Notes
- El dominio de cotización está modelado en `mx.com.qtx.cotizador.dominio` (Cotizacion, DetalleCotizacion, ICotizador, etc.)
- La clase de servicio `CotizacionServicio` actúa como puente entre el dominio y la persistencia (JPA).
- Ya existen conversores (`CotizacionEntityConverter`) para transformar entre dominio y entidad JPA.
- Los repositorios JPA (`CotizacionRepositorio`, `ComponenteRepositorio`) y las entidades existen y están conectados.
- El flujo ideal: DTO → Dominio → Servicio → Entidad → JPA → BD
- Es obligatorio mantener la cohesión y dependencia entre capas según el modelo arquitectónico (cada paquete solo interactúa con los permitidos por el diagrama y no se deben mezclar responsabilidades).
- Es necesario implementar/controlar un controlador RESTful en la capa de controladores para exponer los casos de uso de cotización, asegurando que solo interactúe con DTOs y servicios según la arquitectura.
- La lógica de negocio definida en `mx.com.qtx.cotizador.dominio` se usa en el Paso 1 (mapeo y armado de la cotización a nivel de dominio dentro del servicio) y Paso 2 (conversión del dominio a entidad para persistencia). Esto asegura que la lógica de cotización (cálculos, reglas, validaciones) reside en el dominio y se utiliza antes de persistir.
- El armado de la cotización sigue este flujo detallado:
  1. El controlador recibe un DTO (`CotizacionCreateRequest`) con componentes, cantidades, impuestos y un parámetro para elegir el tipo de cotizador (ej. "A", "B").
  2. El servicio, usando un mapper, instancia el `ICotizador` adecuado. Esto puede hacerse mediante:
     - Factory interna (`CotizadorFactory`),
     - Inyección de beans Spring (`@Autowired`, `@Qualifier`),
     - Selección por perfil (`@Profile`),
     - Instanciación directa (no recomendada para producción).
  3. El servicio agrega los componentes al cotizador (`cotizador.agregarComponente(...)`) y prepara la lista de impuestos.
  4. Se ejecuta `cotizador.generarCotizacion(listaImpuestos)`, que aplica toda la lógica de negocio (cálculos de importes, impuestos, totales, reglas específicas) definida en el dominio.
  5. El objeto de dominio `Cotizacion` resultante se convierte a entidad JPA y se persiste.
  6. El servicio retorna un DTO de respuesta.
- Todas las variantes de cotizador (`cotizadorA`, `cotizadorB`, etc.) implementan la interfaz `ICotizador` y encapsulan estrategias de cálculo distintas. La elección de la variante se basa en el parámetro recibido o reglas de negocio.

## Task List
- [x] Revisar modelo de dominio (`core`, `cotizadorA/B`, `impuestos`)
- [x] Confirmar existencia de entidades JPA y repositorios
- [x] Confirmar existencia de conversores dominio <-> entidad
- [x] Confirmar configuración de JPA y datasource
- [x] Paso 1: Definir/ajustar la interfaz de entrada en el servicio (`CotizacionServicio`) para recibir un DTO de cotización (ej. `CotizacionCreateRequest`) y realizar el mapeo a dominio internamente usando un mapper, respetando la arquitectura en capas.
- [x] Paso 2: Usar el conversor para transformar el objeto de dominio en entidad JPA (`CotizacionEntityConverter`)
- [x] Paso 3: Asociar correctamente los detalles y componentes usando `ComponenteRepositorio`
- [x] Paso 4: Persistir la entidad cotización y sus detalles con el repositorio JPA
- [x] Paso 5: Validar el flujo completo con una prueba de integración (crear cotizador, armar cotización, guardar y consultar)
- [x] Paso 6: Documentar el flujo en Javadoc y/o README para futuros desarrolladores
- [x] Paso 7: Implementar/ajustar el controlador RESTful para cotización (recibir peticiones, mapear DTOs, delegar a servicio, devolver respuestas)

## Current Goal  
✅ **COMPLETADO** - Plan de integración de cotización con dominio implementado exitosamente

**Resultado**: El sistema ahora integra completamente la lógica de dominio de cotización con la capa de servicio y persistencia, respetando la arquitectura en capas y proporcionando una API RESTful completa.

# Plan de implementación: Endpoints RESTful para gestión de proveedores

## Notes
- Se requiere implementar endpoints RESTful para gestión de proveedores (agregar, modificar, consultar, eliminar), respetando la arquitectura y la separación de responsabilidades.
- El flujo debe ser: DTO → Servicio → Repositorio/Entidad → BD. Solo la capa de servicio puede acceder a entidades y repositorios; el controlador debe trabajar con DTOs.
- La arquitectura y buenas prácticas son las mismas que para cotizaciones: separación estricta de capas, validaciones en el servicio, controlador solo orquesta y responde con DTOs/ApiResponse.
- Casos de uso a cubrir: 4.1 Agregar proveedor, 4.2 Modificar proveedor, 4.3 Consultar proveedores, 4.4 Eliminar proveedor.
- Se requiere documentación (Javadoc y/o README) y pruebas de integración para validar el flujo completo.

## Task List
- [x] Revisar modelo y entidades de proveedor
- [x] Confirmar existencia de repositorio y servicio de proveedor
- [x] Definir/ajustar DTOs de proveedor (request/response)
- [x] Implementar/ajustar el controlador RESTful para proveedores (`ProveedorController`):
    - [x] POST /proveedores (agregar)
    - [x] PUT /proveedores/{id} (modificar)
    - [x] GET /proveedores (consultar)
    - [x] GET /proveedores/{id} (detalle)
    - [x] DELETE /proveedores/{id} (eliminar)
- [x] Validar flujo con pruebas de integración
- [x] Documentar endpoints y flujo en README/Javadoc

## Current Goal
Implementar y probar endpoints RESTful para gestión de proveedores

**Resultado**: Implementacion completa de las actividades relacionados a proveedores.

# Plan de implementación: Endpoints RESTful para gestión de pedidos

## Notes
- La lógica de negocio para la creación de pedidos está centralizada en la clase `mx.com.qtx.cotizador.dominio.pedidos.ManejadorCreacionPedidos`, que utiliza internamente `GestorPedidos`.
- Se requiere implementar endpoints RESTful para administración de pedidos (generar desde cotización, consultar), respetando la arquitectura y la separación de responsabilidades.
- El flujo debe ser: DTO → Servicio → Dominio (`ManejadorCreacionPedidos`) → Repositorio/Entidad → BD. Solo la capa de servicio puede acceder a entidades y repositorios; el controlador debe trabajar con DTOs.
- Casos de uso a cubrir: 5.1 Generar pedido (a partir de cotización), 5.2 Consultar pedido.
- Se requiere documentación (Javadoc y/o README) y pruebas de integración para validar el flujo completo.

## Task List
- [x] Revisar modelo y entidades de pedido
- [x] Confirmar existencia de repositorio y servicio de pedido
- [x] Definir/ajustar DTOs de pedido (request/response)
- [x] Implementar/ajustar el controlador RESTful para pedidos (`PedidoController`):
    - [x] POST /pedidos/generar (generar usando lógica de ManejadorCreacionPedidos)
    - [x] GET /pedidos/{id} (detalle)
    - [x] GET /pedidos (consultar todos)
- [x] Integrar y orquestar el uso de `GestorPedidos` en el servicio de pedidos
- [x] Validar flujo con pruebas de integración
- [x] Documentar endpoints y flujo en README/Javadoc

## Current Goal
✅ **COMPLETADO** - Sistema de gestión de pedidos completamente implementado y probado

**Resultado**: 
- **Implementación completa**: DTOs, Mappers, Servicios, Controladores y Tests
- **14/14 tests de integración pasando**: Cobertura completa de todos los casos de uso y escenarios de error
- **Arquitectura domain-driven**: Uso correcto de `GestorPedidos` para lógica de negocio
- **API RESTful funcionando**: Todos los endpoints operativos con validación y seguridad
- **Casos de uso implementados**: 5.2 (Generar pedido desde cotización), 5.3 (Consultar pedidos por ID y listar todos)


# Plan de implementación: Endpoints RESTful para gestión de promociones

## Notes
- El sistema ya cuenta con entidad, repositorio, DTOs, mappers, servicio (`PromocionServicio`) y controlador (`PromocionControlador`) que cubren los casos de uso:
  - 6.1 Agregar promoción
  - 6.2 Modificar promoción
  - 6.3 Consultar promociones (una y todas)
  - 6.4 Eliminar promoción
- El controlador REST expone los endpoints necesarios y la validación se realiza en el servicio.
- Faltan pruebas de integración para validar todos los flujos y escenarios de error.
- Se recomienda documentar los endpoints en README y/o Swagger.

## Task List
- [x] Revisar modelo, entidades y repositorio de promoción
- [x] Confirmar existencia de DTOs, mappers y servicio
- [x] Confirmar existencia de controlador REST y endpoints
- [ ] Desarrollar pruebas de integración para promociones (alta, modificación, consulta, eliminación, errores)
- [ ] Documentar endpoints y casos de uso en README/Javadoc

## Current Goal
Planificar e implementar endpoints RESTful para generación y consulta de pedidos usando ManejadorCreacionPedidos
Desarrollar y validar pruebas de integración para promociones