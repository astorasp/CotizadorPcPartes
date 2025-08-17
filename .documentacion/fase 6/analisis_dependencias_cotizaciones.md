# Análisis de Dependencias para el Módulo de Cotizaciones (Versión Final con Máximo Detalle)

Este documento detalla **todas** las clases y tablas de base de datos, incluyendo dependencias directas, transitivas y las implementaciones concretas de la capa de dominio, necesarias para soportar la funcionalidad expuesta en `CotizacionController`.

## 1. Diagrama de Dependencias de Clases (Nivel Máximo de Detalle)

Este diagrama desglosa todos los componentes, incluyendo las implementaciones específicas de los patrones de diseño en la capa de dominio, y ubica cada clase en su paquete real para ofrecer una visión completa y sin abstracciones.

```mermaid
graph TD
    subgraph mx.com.qtx.cotizador.controlador
        CC[CotizacionController]
    end

    subgraph mx.com.qtx.cotizador.servicio.cotizacion
        CS[CotizacionServicio]
    end
    
    subgraph mx.com.qtx.cotizador.servicio.componente
        CompServ[ComponenteServicio]
    end

    subgraph mx.com.qtx.cotizador.dto
        ReqCot["cotizacion.request.CotizacionCreateRequest"]
        RespCot["cotizacion.response.CotizacionResponse"]
        RespComp["componente.response.ComponenteResponse"]
    end

    subgraph mx.com.qtx.cotizador.dto.cotizacion.mapper
        CotMap[CotizacionMapper]
    end

    subgraph mx.com.qtx.cotizador.servicio.wrapper
        CotEntConv[CotizacionEntityConverter]
        CompRespConv[ComponenteResponseConverter]
    end

    subgraph mx.com.qtx.cotizador.dominio
        direction LR
        subgraph dominio.core
            ICotizador["core.ICotizador (Interfaz)"]
            CotizadorA["cotizadorA.Cotizador"]
            CotizadorB["cotizadorB.CotizadorConMap"]
            CompDom["core.componentes.Componente (Abstracto)"]
        end
        subgraph dominio.impuestos
            CalcImp["impuestos.CalculadorImpuesto (Abstracto)"]
            IVA["impuestos.IVA"]
            CalcImpLocal["impuestos.CalculadorImpuestoLocal"]
            CalcImpFed["impuestos.CalculadorImpuestoFederal"]
            CalcImpMex["impuestos.CalculadorImpuestoMexico"]
        end
    end

    subgraph mx.com.qtx.cotizador.repositorio
        CotRepo[CotizacionRepositorio]
        CompRepo[ComponenteRepositorio]
        PromoRepo[PromocionRepositorio]
        TipoCompRepo[TipoComponenteRepositorio]
        PcPartesRepo[PcPartesRepositorio]
    end

    subgraph mx.com.qtx.cotizador.entidad
        CotEnt["Cotizacion"]
        DetCotEnt["DetalleCotizacion"]
        CompEnt["Componente"]
    end

    CC -- Llama a --> CS
    CS -- DEPENDE DE --> CompServ
    CS -- Usa --> CotRepo & CompRepo
    CS -- Usa --> CotMap
    CS -- Usa --> CotEntConv
    CS -- Usa --> CompRespConv
    CS -- Instancia e implementa --> ICotizador
    ICotizador -- Implementado por --> CotizadorA & CotizadorB
    CotizadorA -- Usa --> CalcImp
    CalcImp -- Implementado por --> IVA & CalcImpLocal & CalcImpFed
    CalcImpLocal -- Usa --> CalcImpMex
    CalcImpFed -- Usa --> CalcImpMex
    CompRespConv -- Convierte DTO a --> CompDom
    CompServ -- Usa --> CompRepo & PromoRepo & TipoCompRepo & PcPartesRepo
    CotRepo -- Gestiona --> CotEnt
    CotEnt -- "1..*" --> DetCotEnt
    DetCotEnt -- "1" --> CompEnt
```

### Flujo de la Arquitectura (Análisis Detallado):

El `CotizacionServicio` es el núcleo de la orquestación y su complejidad radica en cómo construye y utiliza la capa de dominio:
1.  **Selección de Estrategia de Cotizador:** El servicio actúa como una *Factory*. Basado en un parámetro del DTO, instancia una implementación concreta de `ICotizador` (sea `cotizadorA.Cotizador` o `cotizadorB.CotizadorConMap`).
2.  **Selección de Estrategia de Impuestos:** De manera similar, instancia las implementaciones concretas de `CalculadorImpuesto` (`IVA`, `CalculadorImpuestoLocal`, etc.) que serán inyectadas en la estrategia de cotizador.
3.  **Conversión de Datos:** Llama al `ComponenteServicio` para obtener DTOs, que luego convierte a objetos de dominio `Componente` para poder pasarlos a la estrategia de cotizador.

---

## 2. Diagrama de Entidad-Relación (Esquema Completo Requerido)

Este diagrama no cambia, ya que el análisis de la capa de datos ya era exhaustivo.

```mermaid
erDiagram
    cocotizacion {
        int folio PK
        varchar fecha
        decimal total
    }

    codetalle_cotizacion {
        int folio PK, FK
        int num_detalle PK
        varchar id_componente FK
    }

    cocomponente {
        varchar id_componente PK
        short id_tipo_componente FK
        int id_promocion FK
    }

    cotipo_componente {
        short id_tipo_componente PK
        varchar nombre
    }

    copromocion {
        int id_promocion PK
        varchar nombre
    }

    codetalle_promocion {
        int id_detalle_promocion PK
        int id_promocion FK
    }

    codetalle_prom_dscto_x_cant {
        int num_dscto PK
        int num_det_promocion PK, FK
    }

    copc_parte {
        varchar id_pc PK, FK
        varchar id_componente PK, FK
    }

    cocotizacion ||--|{ codetalle_cotizacion : "contiene"
    codetalle_cotizacion }o--|| cocomponente : "referencia a"
    cocomponente }o--|| cotipo_componente : "es de tipo"
    cocomponente }o--o| copromocion : "puede tener"
    copromocion ||--|{ codetalle_promocion : "define"
    codetalle_promocion ||--|{ codetalle_prom_dscto_x_cant : "especifica"
    copc_parte }o--|| cocomponente : "es un"
    copc_parte }o--|| cocomponente : "contiene un"
```

## Conclusión Definitiva

La funcionalidad del `CotizacionController` está **fuertemente acoplada** a la funcionalidad del `ComponenteController`. Para que las cotizaciones funcionen, se necesita prácticamente todo el subdominio de Catálogo. La lista completa de clases y repositorios necesarios es la siguiente:

*   **Servicios:** `CotizacionServicio`, `ComponenteServicio`.
*   **Repositorios:** `CotizacionRepositorio`, `ComponenteRepositorio`, `PromocionRepositorio`, `TipoComponenteRepositorio`, `PcPartesRepositorio`.
*   **Entidades:** `Cotizacion`, `DetalleCotizacion`, `Componente`, `Promocion` (con sus detalles), `TipoComponente`, `PcParte`.
*   **Toda la lógica de Dominio, DTOs, Mappers y Converters** asociados a ambos módulos.