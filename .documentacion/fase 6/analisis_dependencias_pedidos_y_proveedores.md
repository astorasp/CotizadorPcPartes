# Análisis Unificado de Dependencias para Módulos de Pedidos y Proveedores (Versión Final Detallada)

Este documento presenta un análisis consolidado y con el máximo nivel de detalle de las clases y tablas de base de datos necesarias para soportar la funcionalidad de `PedidoController` y `ProveedorController`.

## 1. Diagrama de Dependencias de Clases (Visión Unificada y Detallada)

Este diagrama desglosa cada clase involucrada para ofrecer una visión holística y precisa de las interacciones y dependencias entre ambos módulos.

```mermaid
graph TD
    subgraph mx.com.qtx.cotizador.controlador
        PedC[PedidoController]
        ProvC[ProveedorController]
    end

    subgraph mx.com.qtx.cotizador.servicio.pedido
        PedS[PedidoServicio]
        ProvS[ProveedorServicio]
    end
    
    subgraph mx.com.qtx.cotizador.servicio.cotizacion
        CotServ[CotizacionServicio]
    end

    subgraph mx.com.qtx.cotizador.dto
        ReqPed["pedido.request.GenerarPedidoRequest"]
        RespPed["pedido.response.PedidoResponse"]
        ReqProv["proveedor.request.ProveedorCreateRequest"]
        RespProv["proveedor.response.ProveedorResponse"]
    end

    subgraph mx.com.qtx.cotizador.mappers_y_converters
        PedMap[dto.pedido.mapper.PedidoMapper]
        ProvMap[dto.proveedor.mapper.ProveedorMapper]
        PedEntConv[servicio.wrapper.PedidoEntityConverter]
        ProvEntConv[servicio.wrapper.ProveedorEntityConverter]
        CotEntConv[servicio.wrapper.CotizacionEntityConverter]
    end

    subgraph mx.com.qtx.cotizador.dominio
        GestorPed["pedidos.GestorPedidos"]
        Adapter["core.CotizacionPresupuestoAdapter"]
        CotDom["core.Cotizacion (Dominio)"]
        ProvDom["pedidos.Proveedor (Dominio)"]
    end

    subgraph mx.com.qtx.cotizador.repositorio
        PedRepo[PedidoRepositorio]
        ProvRepo[ProveedorRepositorio]
        CompRepo[ComponenteRepositorio]
        CotRepo[CotizacionRepositorio]
    end

    subgraph mx.com.qtx.cotizador.entidad
        PedEnt["Pedido"]
        DetPedEnt["DetallePedido"]
        ProvEnt["Proveedor"]
        CotEnt["Cotizacion"]
        CompEnt["Componente"]
    end

    PedC -- Llama a --> PedS
    ProvC -- Llama a --> ProvS

    PedS -- Depende de --> ProvS
    PedS -- Depende de --> CotServ

    ProvS -- Usa --> ProvMap & ProvEntConv & ProvRepo
    PedS -- Usa --> PedMap & PedEntConv & CotEntConv
    PedS -- Usa --> PedRepo & CompRepo
    PedS -- Usa Lógica de Dominio --> GestorPed & Adapter

    GestorPed -- Usa --> Adapter
    Adapter -- Adapta --> CotDom
    CotServ -- Retorna --> CotEnt
    CotEntConv -- Convierte Entidad a --> CotDom
    
    ProvRepo -- Gestiona --> ProvEnt
    PedRepo -- Gestiona --> PedEnt
    
    ProvEnt -- "tiene (0..*)" --> PedEnt
    PedEnt -- "contiene (1..*)" --> DetPedEnt
    DetPedEnt -- "referencia a (1)" --> CompEnt
```

### Flujo de la Arquitectura (Visión Unificada):

1.  **Vertical de Proveedores (Autocontenida):** El `ProveedorController` activa el `ProveedorServicio`, que realiza operaciones CRUD sobre la entidad `Proveedor` a través de su repositorio. Es un flujo simple y directo.
2.  **Vertical de Pedidos (Orquestadora):** El `PedidoController` activa el `PedidoServicio`. Este servicio es mucho más complejo, ya que para cumplir su función debe:
    *   **Llamar a otros servicios:** Se comunica con `ProveedorServicio` y `CotizacionServicio` para obtener y validar datos.
    *   **Utilizar la capa de Dominio:** Emplea el `GestorPedidos` y `Adapters` para aplicar la lógica de negocio.
    *   **Acceder a múltiples repositorios:** Interactúa con `PedidoRepositorio`, `ProveedorRepositorio` y `ComponenteRepositorio` para persistir la información.

---

## 2. Diagrama de Entidad-Relación (Visión Unificada)

Este diagrama ER consolida todas las tablas necesarias para ambos módulos, mostrando cómo `coproveedor` es una tabla central para esta área del dominio.

```mermaid
erDiagram
    coproveedor {
        varchar cve PK
        varchar nombre
        varchar razon_social
    }

    copedido {
        int num_pedido PK
        date fecha_emision
        date fecha_entrega
        varchar cve_proveedor FK
    }

    codetalle_pedido {
        int num_pedido PK, FK
        int num_detalle PK
        varchar id_componente FK
    }

    cocomponente {
        varchar id_componente PK
        varchar descripcion
    }
    
    cocotizacion {
        int folio PK
        varchar fecha
    }

    coproveedor ||--o{ copedido : "tiene"
    copedido ||--|{ codetalle_pedido : "contiene"
    codetalle_pedido }o--|| cocomponente : "referencia a"
```

### Relaciones de la Base de Datos:

*   La tabla `coproveedor` es el punto de anclaje.
*   La tabla `copedido` depende directamente de `coproveedor`.
*   La tabla `codetalle_pedido` depende de `copedido` y `cocomponente`.
*   La lógica de negocio para crear un pedido depende implícitamente de que exista una `cocotizacion`.

## Conclusión Final

La visión unificada de los diagramas demuestra que, aunque existen dos controladores distintos, no hay dos verticales de negocio verdaderamente independientes. El módulo de **Proveedores** es un **subdominio de soporte**, mientras que el módulo de **Pedidos** es un **subdominio de orquestación** que consume al primero.

*   **Extraer `Proveedores`:** Es factible y relativamente sencillo. El nuevo `ms-proveedores` expondría una API que el `ms-pedidos` consumiría.
*   **Extraer `Pedidos`:** Es complejo. Requeriría que el nuevo `ms-pedidos` reemplazara sus llamadas directas a `CotizacionServicio` y `ProveedorServicio` por llamadas de API REST (o comunicación por eventos) a los otros dos microservicios.

La funcionalidad de Pedidos y Proveedores está intrínsecamente ligada, siendo la gestión de Proveedores una capacidad fundamental requerida por la gestión de Pedidos.
