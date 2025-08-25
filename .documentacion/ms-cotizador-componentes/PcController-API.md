# API Documentation: PcController

## Descripción General

El `PcController` es el controlador REST que gestiona las operaciones CRUD de PCs completas con sus sub-componentes en el microservicio **ms-cotizador-componentes**. Implementa una arquitectura donde las PCs pueden contener múltiples componentes individuales (monitores, discos duros, tarjetas de video) y maneja la composición completa como una unidad.

**Base URL:** `/pcs`

## Características de Seguridad

- **Autenticación:** JWT Bearer Token requerido
- **Autorización:** Role-Based Access Control (RBAC)
- **Roles del Sistema:**
  - `ADMIN` - Acceso completo
  - `GERENTE` - Operaciones de gestión y eliminación
  - `VENDEDOR` - Consultas y visualización
  - `INVENTARIO` - Gestión de inventario y modificaciones
  - `CONSULTOR` - Solo consultas

## Mapeo de Códigos de Error

El controlador utiliza un sistema estándar de mapeo de códigos:

- **Código "0"** → HTTP 200 (OK)
- **Código "3"** → HTTP 500 (Internal Server Error)
- **Otros códigos** → HTTP 400 (Bad Request)

---

## Endpoints

### 1. Crear PC Completa

**Caso de Uso:** 2.1 - Crear PC con sub-componentes

```http
POST /pcs
```

**Autorización:** `ADMIN`, `GERENTE`, `INVENTARIO`

**Request Body:**
```json
{
  "id": "string",
  "nombre": "string",
  "precio": "number",
  "descripcion": "string",
  "modelo": "string",
  "marca": "string",
  "cantidad": "integer",
  "subComponentes": [
    {
      "id": "string",
      "descripcion": "string",
      "marca": "string",
      "modelo": "string",
      "costo": "number",
      "precioBase": "number",
      "tipoComponente": "string",
      "capacidadAlm": "string",
      "memoria": "string"
    }
  ]
}
```

**Validaciones:**
- `id`: Requerido, máximo 50 caracteres
- `nombre`: Requerido, máximo 200 caracteres
- `precio`: Requerido, debe ser mayor a 0, máximo 10 dígitos enteros y 2 decimales
- `descripcion`: Opcional, máximo 500 caracteres
- `modelo`: Opcional, máximo 100 caracteres
- `marca`: Opcional, máximo 100 caracteres
- `cantidad`: Rango de 1 a 1000, valor por defecto 1
- `subComponentes`: Requerido, mínimo 1, máximo 10 componentes

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "PC creada exitosamente",
  "datos": {
    "id": "string",
    "nombre": "string",
    "precio": "number",
    "descripcion": "string",
    "modelo": "string",
    "marca": "string",
    "cantidad": "integer",
    "categoria": "PC",
    "subComponentes": [
      {
        "id": "string",
        "descripcion": "string",
        "marca": "string",
        "modelo": "string",
        "costo": "number",
        "precioBase": "number",
        "tipoComponente": "string",
        "capacidadAlm": "string",
        "memoria": "string",
        "promocionId": "string",
        "promocionDescripcion": "string"
      }
    ],
    "precioTotal": "number",
    "totalSubComponentes": "integer",
    "fechaCreacion": "string",
    "fechaActualizacion": "string"
  }
}
```

---

### 2. Actualizar PC Completa

**Caso de Uso:** 2.1 - Modificar PC existente

```http
PUT /pcs/{id}
```

**Autorización:** `ADMIN`, `GERENTE`, `INVENTARIO`

**Path Parameters:**
- `id` (string, required) - ID de la PC a actualizar

**Request Body:**
```json
{
  "nombre": "string",
  "precio": "number",
  "descripcion": "string",
  "modelo": "string",
  "marca": "string",
  "cantidad": "integer",
  "subComponentes": [
    {
      "id": "string",
      "descripcion": "string",
      "marca": "string",
      "modelo": "string",
      "costo": "number",
      "precioBase": "number",
      "tipoComponente": "string",
      "capacidadAlm": "string",
      "memoria": "string"
    }
  ]
}
```

**Validaciones:** (mismas que crear, excepto que no incluye `id`)

**Response:** (misma estructura que crear)

---

### 3. Obtener PC por ID

**Caso de Uso:** 2.4 - Consultar PC específica

```http
GET /pcs/{id}
```

**Autorización:** Todos los roles autenticados

**Path Parameters:**
- `id` (string, required) - ID de la PC a consultar

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "PC encontrada exitosamente",
  "datos": {
    "id": "string",
    "nombre": "string",
    "precio": "number",
    "descripcion": "string",
    "modelo": "string",
    "marca": "string",
    "cantidad": "integer",
    "categoria": "PC",
    "subComponentes": [
      {
        "id": "string",
        "descripcion": "string",
        "marca": "string",
        "modelo": "string",
        "costo": "number",
        "precioBase": "number",
        "tipoComponente": "string",
        "capacidadAlm": "string",
        "memoria": "string",
        "promocionId": "string",
        "promocionDescripcion": "string"
      }
    ],
    "precioTotal": "number",
    "totalSubComponentes": "integer",
    "fechaCreacion": "string",
    "fechaActualizacion": "string"
  }
}
```

---

### 4. Eliminar PC Completa

**Caso de Uso:** 2.1 - Eliminar PC

```http
DELETE /pcs/{id}
```

**Autorización:** `ADMIN`, `GERENTE` únicamente

**Path Parameters:**
- `id` (string, required) - ID de la PC a eliminar

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "PC eliminada exitosamente",
  "datos": null
}
```

---

### 5. Obtener Todas las PCs

**Caso de Uso:** 2.4 - Listar todas las PCs

```http
GET /pcs
```

**Autorización:** Todos los roles autenticados

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "PCs obtenidas exitosamente",
  "datos": [
    {
      "id": "string",
      "descripcion": "string",
      "marca": "string",
      "modelo": "string",
      "costo": "number",
      "precioBase": "number",
      "tipoComponente": "PC",
      "capacidadAlm": "string",
      "memoria": "string",
      "promocionId": "string",
      "promocionDescripcion": "string"
    }
  ]
}
```

**Nota:** Este endpoint devuelve PCs como `ComponenteResponse` para compatibilidad con el sistema de componentes.

---

### 6. Agregar Componente a PC

**Caso de Uso:** 2.2 - Agregar componente individual a PC existente

```http
POST /pcs/{pcId}/componentes
```

**Autorización:** `ADMIN`, `GERENTE`, `INVENTARIO`

**Path Parameters:**
- `pcId` (string, required) - ID de la PC a la que agregar el componente

**Request Body:**
```json
{
  "id": "string",
  "tipoComponente": "string",
  "descripcion": "string",
  "marca": "string",
  "modelo": "string",
  "costo": "number",
  "precioBase": "number",
  "capacidadAlm": "string",
  "memoria": "string"
}
```

**Validaciones:**
- `id`: Requerido, máximo 50 caracteres
- `tipoComponente`: Requerido, debe ser `MONITOR`, `DISCO_DURO`, o `TARJETA_VIDEO`
- `descripcion`: Requerida, máximo 500 caracteres
- `marca`: Requerida, máximo 100 caracteres
- `modelo`: Requerido, máximo 100 caracteres
- `costo`: Requerido, debe ser mayor a 0, máximo 10 dígitos enteros y 2 decimales
- `precioBase`: Requerido, debe ser mayor a 0, máximo 10 dígitos enteros y 2 decimales
- `capacidadAlm`: Opcional, máximo 50 caracteres (específico para `DISCO_DURO`)
- `memoria`: Opcional, máximo 50 caracteres (específico para `TARJETA_VIDEO`)

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Componente agregado exitosamente a la PC",
  "datos": {
    "id": "string",
    "descripcion": "string",
    "marca": "string",
    "modelo": "string",
    "costo": "number",
    "precioBase": "number",
    "tipoComponente": "string",
    "capacidadAlm": "string",
    "memoria": "string",
    "promocionId": "string",
    "promocionDescripcion": "string"
  }
}
```

---

### 7. Quitar Componente de PC

**Caso de Uso:** 2.3 - Quitar componente específico de PC

```http
DELETE /pcs/{pcId}/componentes/{componenteId}
```

**Autorización:** `ADMIN`, `GERENTE`, `INVENTARIO`

**Path Parameters:**
- `pcId` (string, required) - ID de la PC
- `componenteId` (string, required) - ID del componente a quitar

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Componente quitado exitosamente de la PC",
  "datos": null
}
```

---

### 8. Listar Componentes de PC

**Caso de Uso:** Adicional - Ver componentes de una PC específica

```http
GET /pcs/{pcId}/componentes
```

**Autorización:** Todos los roles autenticados

**Path Parameters:**
- `pcId` (string, required) - ID de la PC

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Componentes de PC obtenidos exitosamente",
  "datos": [
    {
      "id": "string",
      "descripcion": "string",
      "marca": "string",
      "modelo": "string",
      "costo": "number",
      "precioBase": "number",
      "tipoComponente": "string",
      "capacidadAlm": "string",
      "memoria": "string",
      "promocionId": "string",
      "promocionDescripcion": "string"
    }
  ]
}
```

---

## Tipos de Componente Permitidos para Agregar

Al agregar componentes individuales a una PC (endpoint 6), solo se permiten estos tipos:

- **`MONITOR`** - Monitores y pantallas
- **`DISCO_DURO`** - Discos duros y SSD (requiere `capacidadAlm`)
- **`TARJETA_VIDEO`** - Tarjetas gráficas (requiere `memoria`)

**Nota:** No se permite agregar componentes de tipo `PC` a otra PC para evitar composiciones circulares.

---

## Reglas de Negocio

### Límites de Petición HTTP
- Una petición de creación/actualización debe incluir al menos 1 sub-componente
- Una petición de creación/actualización puede incluir máximo 10 sub-componentes
- Los sub-componentes pueden ser monitores, discos duros, o tarjetas de video

### Validaciones Específicas por Tipo
- **Disco Duro (`DISCO_DURO`)**: Debe incluir campo `capacidadAlm` (ej: "1TB", "500GB")
- **Tarjeta de Video (`TARJETA_VIDEO`)**: Debe incluir campo `memoria` (ej: "8GB", "16GB")
- **Monitor (`MONITOR`)**: No requiere campos adicionales

---

## Ejemplos de Uso

### Crear una PC completa con sub-componentes

```bash
curl -X POST "http://localhost/api/pcs" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "PC001",
    "nombre": "PC Gaming Ultra",
    "precio": 45000.00,
    "descripcion": "PC de alta gama para gaming profesional",
    "modelo": "Gaming Ultra 2025",
    "marca": "TechBuilder",
    "cantidad": 1,
    "subComponentes": [
      {
        "id": "GPU001",
        "descripcion": "Tarjeta gráfica de alta gama",
        "marca": "NVIDIA",
        "modelo": "RTX 4080",
        "costo": 25000.00,
        "precioBase": 32000.00,
        "tipoComponente": "TARJETA_VIDEO",
        "memoria": "16GB"
      },
      {
        "id": "MON001",
        "descripcion": "Monitor gaming 4K",
        "marca": "Samsung",
        "modelo": "Odyssey G9",
        "costo": 8000.00,
        "precioBase": 12000.00,
        "tipoComponente": "MONITOR"
      }
    ]
  }'
```

### Agregar un disco duro a una PC existente

```bash
curl -X POST "http://localhost/api/pcs/PC001/componentes" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "DD001",
    "tipoComponente": "DISCO_DURO",
    "descripcion": "SSD NVMe de alta velocidad",
    "marca": "Samsung",
    "modelo": "980 PRO",
    "costo": 2500.00,
    "precioBase": 3200.00,
    "capacidadAlm": "1TB"
  }'
```

### Quitar un componente específico de una PC

```bash
curl -X DELETE "http://localhost/api/pcs/PC001/componentes/DD001" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

---

*Documentación generada para ms-cotizador-componentes v1.0*
*Estructuras verificadas contra DTOs reales del proyecto*
*Última actualización: 2025-08-20*