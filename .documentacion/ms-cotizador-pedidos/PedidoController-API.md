# API Documentation: PedidoController

## Descripción General

El `PedidoController` es el controlador REST principal del microservicio **ms-cotizador-pedidos** que gestiona la transformación de cotizaciones en pedidos operativos con proveedores. Implementa la lógica de negocio para generar, consultar y gestionar pedidos con integración completa con proveedores externos y gestión de inventario.

**Base URL:** `/pedidos`

## Características de Seguridad

- **Autenticación:** JWT Bearer Token requerido
- **Autorización:** Role-Based Access Control (RBAC)
- **Configuración CORS:** Permitido desde cualquier origen (`@CrossOrigin`)
- **Documentación OpenAPI:** Integración completa con Swagger UI
- **Roles del Sistema:**
  - `ADMIN` - Acceso completo a todas las operaciones
  - `GERENTE` - Gestión y aprobación de pedidos, cancelaciones
  - `VENDEDOR` - Generación, consulta y cancelación de pedidos
  - `INVENTARIO` - Gestión de cumplimiento, surtido e inventario
  - `CONSULTOR` - Solo lectura para análisis y reportes

## Mapeo de Códigos de Error

El controlador utiliza un sistema estándar de mapeo de códigos:

- **Código "0"** → HTTP 200 (OK)
- **Código "3"** → HTTP 500 (Internal Server Error)
- **Otros códigos** → HTTP 400 (Bad Request)

---

## Endpoints

### 1. Generar Pedido desde Cotización

**Caso de Uso:** 5.2 - Generar pedido

```http
POST /pedidos/generar
```

**Autorización:** `ADMIN`, `GERENTE`, `VENDEDOR`, `INVENTARIO`

**Request Body:**
```json
{
  "cotizacionId": "integer",
  "cveProveedor": "string",
  "fechaEmision": "YYYY-MM-DD",
  "fechaEntrega": "YYYY-MM-DD",
  "nivelSurtido": "integer"
}
```

**Validaciones:**
- `cotizacionId`: Requerido, debe existir en el sistema
- `cveProveedor`: Requerido, máximo 10 caracteres, debe ser proveedor válido
- `fechaEmision`: Requerida, fecha en formato ISO
- `fechaEntrega`: Requerida, fecha en formato ISO
- `nivelSurtido`: Requerido, valor entre 0 y 100

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Pedido generado exitosamente",
  "datos": {
    "numPedido": "number",
    "fechaEmision": "YYYY-MM-DD",
    "fechaEntrega": "YYYY-MM-DD",
    "nivelSurtido": "integer",
    "cveProveedor": "string",
    "nombreProveedor": "string",
    "total": "number",
    "detalles": [
      {
        "idArticulo": "string",
        "descripcion": "string",
        "cantidad": "integer",
        "precioUnitario": "number",
        "totalCotizado": "number"
      }
    ],
    "totalDetalles": "integer"
  }
}
```

---

### 2. Consultar Pedido por ID

**Caso de Uso:** 5.3 - Consultar pedidos

```http
GET /pedidos/{id}
```

**Autorización:** Todos los roles autenticados (datos filtrados según el rol)

**Path Parameters:**
- `id` (integer, required) - ID del pedido a consultar

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Pedido encontrado exitosamente",
  "datos": {
    "numPedido": "number",
    "fechaEmision": "YYYY-MM-DD",
    "fechaEntrega": "YYYY-MM-DD",
    "nivelSurtido": "integer",
    "cveProveedor": "string",
    "nombreProveedor": "string",
    "total": "number",
    "detalles": [
      {
        "idArticulo": "string",
        "descripcion": "string",
        "cantidad": "integer",
        "precioUnitario": "number",
        "totalCotizado": "number"
      }
    ],
    "totalDetalles": "integer"
  }
}
```

---

### 3. Consultar Todos los Pedidos

**Caso de Uso:** 5.3 - Consultar pedidos (listado completo)

```http
GET /pedidos
```

**Autorización:** Todos los roles autenticados (datos filtrados según el rol)

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Lista de pedidos obtenida exitosamente",
  "datos": [
    {
      "numPedido": "number",
      "fechaEmision": "YYYY-MM-DD",
      "fechaEntrega": "YYYY-MM-DD",
      "nivelSurtido": "integer",
      "cveProveedor": "string",
      "nombreProveedor": "string",
      "total": "number",
      "detalles": [
        {
          "idArticulo": "string",
          "descripcion": "string",
          "cantidad": "integer",
          "precioUnitario": "number",
          "totalCotizado": "number"
        }
      ],
      "totalDetalles": "integer"
    }
  ]
}
```

---

### 4. Cancelar Pedido

**Caso de Uso:** Gestión de pedidos - Cancelación

```http
POST /pedidos/{pedidoId}/cancelar?reason={reason}
```

**Autorización:** `ADMIN`, `GERENTE`, `VENDEDOR`

**Path Parameters:**
- `pedidoId` (integer, required) - ID del pedido a cancelar

**Query Parameters:**
- `reason` (string, optional) - Razón de la cancelación (por defecto: "Cancelación manual")

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Pedido cancelado exitosamente",
  "datos": "Pedido {pedidoId} cancelado por razón: {reason}"
}
```

---

## Sistema de Pedidos

### Flujo de Generación de Pedidos

El sistema transforma cotizaciones aprobadas en pedidos operativos siguiendo este flujo:

1. **Validación de Cotización**: Verifica que la cotización existe y está en estado válido
2. **Validación de Proveedor**: Confirma que el proveedor está activo y puede cumplir el pedido
3. **Procesamiento de Detalles**: Convierte los detalles de cotización en líneas de pedido
4. **Cálculo de Fechas**: Valida fechas de emisión y entrega
5. **Asignación de Nivel de Surtido**: Configura el porcentaje de cumplimiento esperado
6. **Generación de Número de Pedido**: Asigna identificador único al pedido

### Nivel de Surtido

El **Nivel de Surtido** (0-100) indica el porcentaje de cumplimiento esperado del pedido:

- **100%**: Pedido completo, todos los artículos deben entregarse
- **75-99%**: Pedido prioritario, alta expectativa de cumplimiento
- **50-74%**: Pedido estándar, cumplimiento moderado aceptable
- **25-49%**: Pedido flexible, cumplimiento parcial aceptable
- **0-24%**: Pedido de respaldo, bajo cumplimiento esperado

### Integración con Proveedores

El sistema integra con proveedores externos mediante:

- **Validación de Clave**: Verificación de `cveProveedor` en directorio de proveedores
- **Resolución de Nombres**: Obtención automática del nombre del proveedor
- **Gestión de Fechas**: Coordinación de fechas de entrega según capacidad del proveedor

---

## Reglas de Negocio

### Validaciones de Datos
- La cotización referenciada debe existir y estar en estado válido
- El proveedor debe estar activo en el sistema
- La fecha de entrega debe ser posterior a la fecha de emisión
- El nivel de surtido debe estar en el rango válido (0-100)

### Autorización por Rol
- **Generación**: Solo roles con permisos operativos (ADMIN, GERENTE, VENDEDOR, INVENTARIO)
- **Consulta**: Todos los roles, con filtrado automático de datos según privilegios
- **Cancelación**: Solo roles de gestión (ADMIN, GERENTE, VENDEDOR)

### Filtrado de Datos por Rol
- **VENDEDOR**: Solo ve pedidos que ha generado
- **INVENTARIO**: Ve todos los pedidos pero enfocado en cumplimiento
- **CONSULTOR**: Acceso de solo lectura para análisis
- **GERENTE/ADMIN**: Acceso completo sin restricciones

### Estados del Pedido
Aunque no se muestran explícitamente en los DTOs, el sistema maneja estados internos:
- **GENERADO**: Pedido recién creado
- **ENVIADO**: Pedido enviado al proveedor
- **EN_PROCESO**: Proveedor está preparando el pedido
- **ENTREGADO**: Pedido completado
- **CANCELADO**: Pedido cancelado por alguna razón

---

## Ejemplos de Uso

### Generar pedido desde cotización aprobada

```bash
curl -X POST "http://localhost/api/pedidos/generar" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "cotizacionId": 1001,
    "cveProveedor": "PROV001",
    "fechaEmision": "2025-01-20",
    "fechaEntrega": "2025-02-15",
    "nivelSurtido": 95
  }'
```

### Generar pedido con surtido parcial

```bash
curl -X POST "http://localhost/api/pedidos/generar" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "cotizacionId": 1002,
    "cveProveedor": "PROV002",
    "fechaEmision": "2025-01-20",
    "fechaEntrega": "2025-03-01",
    "nivelSurtido": 75
  }'
```

### Consultar pedido específico

```bash
curl -X GET "http://localhost/api/pedidos/1001" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### Listar todos los pedidos

```bash
curl -X GET "http://localhost/api/pedidos" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### Cancelar pedido con razón específica

```bash
curl -X POST "http://localhost/api/pedidos/1001/cancelar?reason=Cliente%20canceló%20orden" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### Cancelar pedido con razón por defecto

```bash
curl -X POST "http://localhost/api/pedidos/1001/cancelar" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

---

## Integración con Swagger UI

El controlador está completamente documentado con OpenAPI 3.0:

- **Tags**: Agrupado bajo "Pedidos"
- **Operaciones**: Cada endpoint tiene summary y description detallada
- **Parámetros**: Documentación completa de path y query parameters
- **Respuestas**: Códigos de estado HTTP documentados con descripciones
- **Esquemas**: DTOs automáticamente incluidos en la documentación

### Acceso a Swagger UI
- **URL**: `http://localhost/swagger-ui.html`
- **Sección**: "Pedidos - Gestión de pedidos del sistema"

---

## Diferencias con otros Microservicios

### Enfoque Empresarial
- **ms-cotizador-pedidos**: Orientado a operaciones B2B con proveedores
- **ms-cotizador-cotizaciones**: Enfocado en cálculos y estimaciones
- **ms-cotizador-componentes**: CRUD básico de catálogo

### Gestión de Fechas y Cronogramas
- Manejo explícito de fechas de emisión y entrega
- Coordinación con calendarios de proveedores
- Planificación de surtido y cumplimiento

### Integración Externa
- Conexión directa con sistemas de proveedores
- Validación de disponibilidad de artículos
- Gestión de niveles de inventario y surtido

### Auditabilidad Completa
- Logging extensivo para auditoría
- Trazabilidad completa desde cotización hasta pedido
- Razones documentadas para cancelaciones

---

## Metadatos de Response

### Información Calculada Automáticamente
- **`numPedido`**: Número único generado automáticamente
- **`nombreProveedor`**: Resuelto automáticamente desde `cveProveedor`
- **`total`**: Suma calculada de todos los detalles del pedido
- **`totalDetalles`**: Contador de líneas de detalle

### Detalles Enriquecidos por Línea
- **`idArticulo`**: Identificador del componente/artículo
- **`descripcion`**: Descripción completa del artículo
- **`totalCotizado`**: Cálculo automático (cantidad × precio unitario)

---

*Documentación generada para ms-cotizador-pedidos v1.0*
*Estructuras verificadas contra DTOs reales del proyecto*
*Integración completa con OpenAPI/Swagger*
*Última actualización: 2025-08-20*