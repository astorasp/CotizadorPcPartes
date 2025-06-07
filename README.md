# Cotizador y Generador de Pedidos Modulo 3

Este proyecto implementa un sistema completo para cotizar componentes (como hardware de computadora), generar pedidos basados en esas cotizaciones y persistir la información en una base de datos.

## Requisitos

* Java Development Kit (JDK) 17 o superior.
* MySQL 8.0 o superior como motor de base de datos.
* Maven para gestión de dependencias (opcional).

## Funcionalidades Principales

1.  **Cotización de Componentes:**
    *   Permite definir y agregar diferentes tipos de componentes (Monitores, Discos Duros, Tarjetas de Video, PCs ensambladas) a una cotización.
    *   Calcula costos y precios de venta para cada componente y para la cotización total.
    *   Puede manejar componentes simples y componentes compuestos (como una PC que incluye otros componentes).
    *   La clase principal para interactuar con esta funcionalidad es `mx.com.qtx.cotizador.core.ICotizador` (obtenida a través de `mx.com.qtx.cotizador.config.Config`).

2.  **Generación de Pedidos desde Cotizaciones:**
    *   Convierte una `Cotizacion` generada en un formato de `IPresupuesto` utilizando un patrón Adapter (`mx.com.qtx.cotizador.core.CotizacionPresupuestoAdapter`).
    *   Utiliza un `GestorPedidos` (`mx.com.qtx.cotizador.pedidos.GestorPedidos`) para procesar el presupuesto adaptado y generar un `Pedido` (`mx.com.qtx.cotizador.pedidos.Pedido`).
    *   Asocia el pedido a un `Proveedor` (`mx.com.qtx.cotizador.pedidos.Proveedor`) específico.
    *   Maneja excepciones básicas como `ProveedorNoExisteExcepcion` y `PresupuestoNoCargadoExcepcion`.

3.  **Cálculo Flexible de Impuestos (Patrón Bridge):**
    *   Implementa un patrón Bridge para desacoplar la *abstracción* del tipo de impuesto (p.ej., local, federal - `CalculoImpuestoLocal`, `CalculoImpuestoFederal`) de la *implementación* específica por país o región (p.ej., `CalculoImpuestoMexico`, `CalculoImpuestosUsa`).
    *   Esto permite configurar y aplicar diferentes combinaciones de impuestos (ej. IVA + ISLR en México, Sales Tax en USA) de forma dinámica al generar la cotización.
    *   La interfaz base es `mx.com.qtx.cotizador.impuestos.CalculoImpuesto`.

4.  **Persistencia de Datos (JPA/Hibernate):**
    *   Permite guardar cotizaciones, componentes y pedidos en una base de datos MySQL.
    *   Implementa el patrón Repository para separar la lógica de acceso a datos de la lógica de negocio.
    *   Utiliza JPA y Hibernate como ORM para mapear objetos a tablas de la base de datos.
    *   Configuración flexible de propiedades de persistencia a través de `application.properties`.

## Pruebas Cruciales: `CotizacionAdapterBridgeTest.java`

La clase `src/mx/com/qtx/cotizadorv1ds/casosDeUso/CotizacionAdapterBridgeTest.java` sirve como punto de entrada principal para probar y demostrar el flujo completo del sistema:

*   **`obtenerCotizacionMock()`:** Crea una cotización de ejemplo (`Cotizacion`) agregando diversos componentes simples y compuestos.
*   **`testGenerarCotizacion()`:**
    1.  Obtiene la cotización de ejemplo.
    2.  Crea una instancia de `ManejadorCreacionPedidos`.
    3.  Llama al método `crearPedidoDesdeCotizacion` para generar un pedido basado en la cotización, utilizando el adaptador internamente.
    4.  Repite el paso 3 para otro proveedor.
    5.  Imprime los detalles de los pedidos generados en la consola, mostrando la información formateada.

Para ejecutar las pruebas y ver el sistema en acción:

1. Asegúrate de tener configurada correctamente tu base de datos MySQL, usando la información en `application.properties`.
2. Ejecuta alguna de las clases de prueba:
   * `CotizacionAdapterBridgeTest.java` para probar la integración entre cotizaciones y pedidos.
   * `CotizadorTest.java` para probar la funcionalidad completa del cotizador con persistencia.

## Estructura del Proyecto (Paquetes Principales)

*   **`mx.com.qtx.cotizador.config`:** Configuración del sistema y proveedor de contexto Spring.
    * `SpringContextProvider`: Inicializa y proporciona acceso al contexto de Spring.
*   **`mx.com.qtx.cotizador.core`:** Clases centrales del módulo de cotización.
    * `Cotizacion`: Representa una cotización con sus detalles y métodos para calcular totales y guardar.
    * `DetalleCotizacion`: Representa un ítem individual dentro de una cotización.
    * `ICotizador`: Interface para los diferentes implementaciones del cotizador.
*   **`mx.com.qtx.cotizador.core.componentes`:** Componentes cotizables.
    * `Componente`: Clase base abstracta para todos los componentes.
    * Clases específicas como `Monitor`, `DiscoDuro`, `TarjetaVideo` y `Pc`.
*   **`mx.com.qtx.cotizador.impuestos`:** Clases relacionadas con el cálculo de impuestos.
*   **`mx.com.qtx.cotizador.pedidos`:** Clases relacionadas con la gestión y creación de pedidos.
*   **`mx.com.qtx.cotizador.persistencia`:** Capa de persistencia para acceso a datos.
    * **`.config`:** Configuración JPA y Hibernate.
      * `JpaConfig`: Configura DataSource, EntityManagerFactory y TransactionManager.
    * **`.entidades`:** Entidades JPA mapeadas a tablas de la base de datos.
      * Entidades como `Componente`, `Cotizacion`, `DetalleCotizacion`, etc.
    * **`.repositorios`:** Interfaces Repository para operaciones CRUD.
      * `ComponenteRepositorio`: Operaciones para componentes.
      * `CotizacionRepositorio`: Operaciones para cotizaciones.
      * Otros repositorios específicos.
*   **`mx.com.qtx.cotizador.servicios`:** Servicios de negocio que actúan como intermediarios.
    * `ComponenteServicio`: Servicios para gestionar componentes.
    * `CotizacionServicio`: Servicios para gestionar cotizaciones.
    * **`.wrapper`:** Convertidores entre objetos de dominio y entidades.
      * `ComponenteEntityConverter`: Convierte entre componentes del dominio y entidades.
*   **`mx.com.qtx.cotizador.casosDeUso`:** Clases que demuestran casos de uso específicos.
    * `CotizacionAdapterBridgeTest`: Prueba la integración de cotizaciones y pedidos.
    * `CotizadorTest`: Prueba funcionalidades del cotizador con persistencia.