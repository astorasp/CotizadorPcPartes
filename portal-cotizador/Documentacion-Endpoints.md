# 📋 Documentación de Endpoints - API Cotizador

## Información General

**Base URL**: `http://localhost:8080/api`  
**Formato de Respuesta**: JSON  
**Content-Type**: `application/json`

Todos los endpoints retornan un formato estándar `ApiResponse<T>`:

```json
{
  "codigo": "0",        // "0" = éxito, otros = error
  "mensaje": "string",  // Descripción del resultado
  "datos": {}          // Datos de respuesta (tipo T)
}
```

**Mapeo de Códigos HTTP**:
- Código `"0"` → HTTP 200 (OK)
- Código `"3"` → HTTP 500 (Error interno)
- Otros códigos → HTTP 400 (Bad Request)

---

## 🔧 1. Gestión de Componentes

### Base URL: `/componentes`

#### 1.1 Crear Componente
**POST** `/componentes`

**Request Body (ComponenteCreateRequest)**:
```json
{
  "id": "string",                    // ID único del componente
  "descripcion": "string",           // Descripción del componente
  "marca": "string",                 // Marca del componente
  "modelo": "string",                // Modelo del componente
  "costo": 0.00,                     // Costo del componente
  "precioBase": 0.00,                // Precio base
  "tipoComponente": "string",        // Tipo: "MONITOR", "DISCO_DURO", "TARJETA_VIDEO", "PC"
  "capacidadAlm": "string",          // Solo para discos duros
  "memoria": "string"                // Solo para tarjetas de video
}
```

**Response**: `ApiResponse<ComponenteResponse>`

---

#### 1.2 Actualizar Componente
**PUT** `/componentes/{id}`

**Request Body (ComponenteUpdateRequest)**: Misma estructura que Create

**Response**: `ApiResponse<ComponenteResponse>`

---

#### 1.3 Eliminar Componente
**DELETE** `/componentes/{id}`

**Response**: `ApiResponse<Void>`

---

#### 1.4 Obtener Todos los Componentes
**GET** `/componentes`

**Response**: `ApiResponse<List<ComponenteResponse>>`

**ComponenteResponse**:
```json
{
  "id": "string",
  "descripcion": "string",
  "marca": "string",
  "modelo": "string",
  "costo": 0.00,
  "precioBase": 0.00,
  "tipoComponente": "string",
  "capacidadAlm": "string",
  "memoria": "string",
  "promocionId": "string",           // Si tiene promoción asociada
  "promocionDescripcion": "string"   // Descripción de la promoción
}
```

---

#### 1.5 Obtener Componente por ID
**GET** `/componentes/{id}`

**Response**: `ApiResponse<ComponenteResponse>`

---

#### 1.6 Obtener Componentes por Tipo
**GET** `/componentes/tipo/{tipo}`

**Tipos válidos**: `MONITOR`, `DISCO_DURO`, `TARJETA_VIDEO`, `PC`

**Response**: `ApiResponse<List<ComponenteResponse>>`

---

#### 1.7 Verificar Existencia de Componente
**GET** `/componentes/{id}/existe`

**Response**: `ApiResponse<Boolean>`

---

## 🖥️ 2. Gestión de PCs (Armado)

### Base URL: `/pcs`

#### 2.1 Crear PC Completa
**POST** `/pcs`

**Request Body (PcCreateRequest)**:
```json
{
  "id": "string",
  "descripcion": "string",
  "marca": "string",
  "modelo": "string",
  "costo": 0.00,
  "precioBase": 0.00,
  "subComponentes": [               // Lista de componentes que forman la PC
    {
      "id": "string",               // ID del componente
      "cantidad": 1                 // Cantidad del componente
    }
  ]
}
```

**Response**: `ApiResponse<PcResponse>`

---

#### 2.2 Actualizar PC Completa
**PUT** `/pcs/{id}`

**Request Body (PcUpdateRequest)**: Misma estructura que Create

**Response**: `ApiResponse<PcResponse>`

---

#### 2.3 Obtener PC por ID
**GET** `/pcs/{id}`

**Response**: `ApiResponse<PcResponse>`

**PcResponse**:
```json
{
  "id": "string",
  "descripcion": "string",
  "marca": "string",
  "modelo": "string",
  "costo": 0.00,
  "precioBase": 0.00,
  "tipoComponente": "PC",
  "subComponentes": [               // Lista de componentes de la PC
    {
      "id": "string",
      "descripcion": "string",
      "marca": "string",
      "modelo": "string",
      "costo": 0.00,
      "precioBase": 0.00,
      "tipoComponente": "string"
    }
  ]
}
```

---

#### 2.4 Eliminar PC
**DELETE** `/pcs/{id}`

**Response**: `ApiResponse<Void>`

---

#### 2.5 Listar Todas las PCs
**GET** `/pcs`

**Response**: `ApiResponse<List<ComponenteResponse>>`

---

#### 2.6 Agregar Componente a PC
**POST** `/pcs/{pcId}/componentes`

**Request Body (AgregarComponenteRequest)**:
```json
{
  "id": "string",                   // ID del componente a agregar
  "cantidad": 1                     // Cantidad del componente
}
```

**Response**: `ApiResponse<ComponenteResponse>`

---

#### 2.7 Quitar Componente de PC
**DELETE** `/pcs/{pcId}/componentes/{componenteId}`

**Response**: `ApiResponse<Void>`

---

#### 2.8 Listar Componentes de una PC
**GET** `/pcs/{pcId}/componentes`

**Response**: `ApiResponse<List<ComponenteResponse>>`

---

## 💰 3. Gestión de Cotizaciones

### Base URL: `/cotizaciones`

#### 3.1 Crear Cotización
**POST** `/cotizaciones`

**Request Body (CotizacionCreateRequest)**:
```json
{
  "tipoCotizador": "string",        // Tipo de cotizador utilizado
  "impuestos": [                    // Lista de configuraciones de impuestos
    {
      "tipo": "string",             // Tipo de impuesto
      "pais": "string",             // País aplicable (MX, US, CA)
      "tasa": 0.00                  // Tasa del impuesto
    }
  ],
  "detalles": [                     // Lista de componentes cotizados
    {
      "componenteId": "string",     // ID del componente
      "cantidad": 1,                // Cantidad solicitada
      "precioUnitario": 0.00        // Precio unitario aplicado
    }
  ]
}
```

**Response**: `ApiResponse<CotizacionResponse>`

---

#### 3.2 Obtener Cotización por ID
**GET** `/cotizaciones/{id}`

**Response**: `ApiResponse<CotizacionResponse>`

**CotizacionResponse**:
```json
{
  "id": 1,
  "tipoCotizador": "string",
  "fechaCreacion": "2024-01-01T10:00:00",
  "subtotal": 0.00,
  "totalImpuestos": 0.00,
  "total": 0.00,
  "detalles": [                     // Líneas de detalle de la cotización
    {
      "componenteId": "string",
      "descripcion": "string",
      "cantidad": 1,
      "precioUnitario": 0.00,
      "subtotal": 0.00
    }
  ],
  "impuestosAplicados": [           // Impuestos calculados
    {
      "tipo": "string",
      "pais": "string",
      "base": 0.00,
      "tasa": 0.00,
      "monto": 0.00
    }
  ]
}
```

---

#### 3.3 Listar Todas las Cotizaciones
**GET** `/cotizaciones`

**Response**: `ApiResponse<List<CotizacionResponse>>`

---

#### 3.4 Buscar Cotizaciones por Fecha
**GET** `/cotizaciones/buscar/fecha?fecha={fecha}`

**Parámetros**:
- `fecha`: Formato `YYYY-MM-DD`

**Response**: `ApiResponse<List<CotizacionResponse>>`

---

## 🏪 4. Gestión de Proveedores

### Base URL: `/proveedores`

#### 4.1 Crear Proveedor
**POST** `/proveedores`

**Request Body (ProveedorCreateRequest)**:
```json
{
  "cve": "string",                  // Clave única del proveedor
  "nombre": "string",               // Nombre del proveedor
  "razonSocial": "string",          // Razón social
  "contacto": "string",             // Información de contacto
  "telefono": "string",             // Teléfono
  "email": "string",                // Email
  "direccion": "string"             // Dirección
}
```

**Response**: `ApiResponse<ProveedorResponse>`

---

#### 4.2 Actualizar Proveedor
**PUT** `/proveedores/{cve}`

**Request Body (ProveedorUpdateRequest)**: Misma estructura que Create (sin cve)

**Response**: `ApiResponse<ProveedorResponse>`

---

#### 4.3 Obtener Proveedor por Clave
**GET** `/proveedores/{cve}`

**Response**: `ApiResponse<ProveedorResponse>`

**ProveedorResponse**:
```json
{
  "cve": "string",
  "nombre": "string",
  "razonSocial": "string",
  "contacto": "string",
  "telefono": "string",
  "email": "string",
  "direccion": "string",
  "numeroPedidos": 0,               // Número de pedidos asociados
  "fechaCreacion": "2024-01-01T10:00:00",
  "fechaActualizacion": "2024-01-01T10:00:00"
}
```

---

#### 4.4 Listar Todos los Proveedores
**GET** `/proveedores`

**Response**: `ApiResponse<List<ProveedorResponse>>`

---

#### 4.5 Eliminar Proveedor
**DELETE** `/proveedores/{cve}`

**Response**: `ApiResponse<Void>`

---

#### 4.6 Buscar Proveedores por Nombre
**GET** `/proveedores/buscar/nombre?nombre={nombre}`

**Response**: `ApiResponse<List<ProveedorResponse>>`

---

#### 4.7 Buscar Proveedores por Razón Social
**GET** `/proveedores/buscar/razon-social?razonSocial={razonSocial}`

**Response**: `ApiResponse<List<ProveedorResponse>>`

---

## 📦 5. Gestión de Pedidos

### Base URL: `/pedidos`

#### 5.1 Generar Pedido desde Cotización
**POST** `/pedidos/generar`

**Request Body (GenerarPedidoRequest)**:
```json
{
  "cotizacionId": 1,                // ID de la cotización base
  "cveProveedor": "string",         // Clave del proveedor asignado
  "numeroPedido": 1,                // Número de pedido
  "nivelSurtido": 1,                // Nivel de surtido
  "fechaEmision": "2024-01-01",     // Fecha de emisión
  "fechaEntrega": "2024-01-08"      // Fecha de entrega estimada
}
```

**Response**: `ApiResponse<PedidoResponse>`

---

#### 5.2 Obtener Pedido por ID
**GET** `/pedidos/{id}`

**Response**: `ApiResponse<PedidoResponse>`

**PedidoResponse**:
```json
{
  "id": 1,
  "numeroPedido": 1,
  "cotizacionId": 1,
  "cveProveedor": "string",
  "nombreProveedor": "string",
  "nivelSurtido": 1,
  "fechaEmision": "2024-01-01",
  "fechaEntrega": "2024-01-08",
  "total": 0.00,
  "estado": "string",               // Estado del pedido
  "detalles": [                     // Líneas de detalle del pedido
    {
      "componenteId": "string",
      "descripcion": "string",
      "cantidad": 1,
      "precioUnitario": 0.00,
      "subtotal": 0.00
    }
  ]
}
```

---

#### 5.3 Listar Todos los Pedidos
**GET** `/pedidos`

**Response**: `ApiResponse<List<PedidoResponse>>`

---

## 🎉 6. Gestión de Promociones

### Base URL: `/promociones`

#### 6.1 Crear Promoción
**POST** `/promociones`

**Request Body (PromocionCreateRequest)**:
```json
{
  "nombre": "string",               // Nombre de la promoción
  "descripcion": "string",          // Descripción detallada
  "tipoPromocion": "string",        // Tipo: "DESCUENTO_PLANO", "POR_CANTIDAD", "NXM", etc.
  "fechaInicio": "2024-01-01",      // Fecha de inicio
  "fechaFin": "2024-01-31",         // Fecha de fin
  "activa": true,                   // Si está activa
  "configuracion": {                // Configuración específica del tipo
    "descuento": 0.00,              // Para descuentos
    "cantidadMinima": 1,            // Para promociones por cantidad
    "cantidadPagar": 2,             // Para promociones N×M
    "cantidadGratis": 1             // Para promociones N×M
  },
  "componentesAplicables": ["string"] // IDs de componentes aplicables
}
```

**Response**: `ApiResponse<PromocionResponse>`

---

#### 6.2 Actualizar Promoción
**PUT** `/promociones/{id}`

**Request Body (PromocionUpdateRequest)**: Misma estructura que Create

**Response**: `ApiResponse<PromocionResponse>`

---

#### 6.3 Obtener Promoción por ID
**GET** `/promociones/{id}`

**Response**: `ApiResponse<PromocionResponse>`

**PromocionResponse**:
```json
{
  "id": 1,
  "nombre": "string",
  "descripcion": "string",
  "tipoPromocion": "string",
  "fechaInicio": "2024-01-01",
  "fechaFin": "2024-01-31",
  "activa": true,
  "configuracion": {},
  "componentesAplicables": ["string"],
  "fechaCreacion": "2024-01-01T10:00:00",
  "fechaActualizacion": "2024-01-01T10:00:00",
  "vigente": true,                  // Calculado: si está en periodo vigente
  "diasRestantes": 10               // Calculado: días restantes de vigencia
}
```

---

#### 6.4 Listar Todas las Promociones
**GET** `/promociones`

**Response**: `ApiResponse<List<PromocionResponse>>`

---

#### 6.5 Eliminar Promoción
**DELETE** `/promociones/{id}`

**Response**: `ApiResponse<Void>`

---

## 🛠️ Información Técnica Adicional

### Tipos de Componentes Válidos
- `MONITOR`: Monitores
- `DISCO_DURO`: Discos duros (requiere `capacidadAlm`)
- `TARJETA_VIDEO`: Tarjetas de video (requiere `memoria`)
- `PC`: PCs completas (contiene `subComponentes`)

### Tipos de Promociones
- `DESCUENTO_PLANO`: Descuento fijo en pesos
- `DESCUENTO_PORCENTAJE`: Descuento porcentual
- `POR_CANTIDAD`: Descuento por comprar cierta cantidad
- `NXM`: Promociones tipo "compra N, paga M"

### Países Soportados para Impuestos
- `MX`: México
- `US`: Estados Unidos  
- `CA`: Canadá

### Códigos de Error Comunes
- `"0"`: Operación exitosa
- `"1"`: Error de validación de datos
- `"2"`: Recurso no encontrado
- `"3"`: Error interno del servidor
- `"4"`: Error de reglas de negocio

### URLs Completas de Ejemplo
```
GET http://localhost:8080/api/componentes
POST http://localhost:8080/api/cotizaciones
GET http://localhost:8080/api/proveedores/PROV001
DELETE http://localhost:8080/api/promociones/1
```

---

## 📝 Notas para el Frontend

1. **Validaciones**: Todos los campos requeridos deben validarse en el frontend antes del envío
2. **Fechas**: Usar formato ISO 8601 (`YYYY-MM-DD` para fechas, `YYYY-MM-DDTHH:mm:ss` para timestamps)
3. **Decimales**: Los precios y costos admiten hasta 2 decimales
4. **IDs**: Los IDs de componentes son strings, los IDs de cotizaciones/pedidos/promociones son números
5. **CORS**: La API está configurada para aceptar peticiones desde cualquier origen (`*`)