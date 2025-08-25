# API Documentation: ComponenteController

## Descripción General

El `ComponenteController` es el controlador REST principal del microservicio **ms-cotizador-componentes** que gestiona las operaciones CRUD de componentes de PC. Implementa una arquitectura refactorizada donde el controlador solo maneja DTOs y delega la lógica de negocio al servicio.

**Base URL:** `/componentes`

## Características de Seguridad

- **Autenticación:** JWT Bearer Token requerido
- **Autorización:** Role-Based Access Control (RBAC)
- **Roles del Sistema:**
  - `ADMIN` - Acceso completo
  - `GERENTE` - Operaciones de gestión
  - `VENDEDOR` - Consultas y ventas
  - `INVENTARIO` - Gestión de inventario
  - `CONSULTOR` - Solo consultas

## Mapeo de Códigos de Error

El controlador utiliza un sistema estándar de mapeo de códigos:

- **Código "0"** → HTTP 200 (OK)
- **Código "3"** → HTTP 500 (Internal Server Error)
- **Otros códigos** → HTTP 400 (Bad Request)

---

## Endpoints

### 1. Crear Componente

**Caso de Uso:** 1.1 - Agregar componente

```http
POST /componentes
```

**Autorización:** `ADMIN`, `INVENTARIO`

**Request Body:**
```json
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
```

**Validaciones:**
- `id`: Requerido, máximo 10 caracteres
- `descripcion`: Requerida, máximo 200 caracteres
- `marca`: Requerida, máximo 50 caracteres
- `modelo`: Requerido, máximo 50 caracteres
- `costo`: Requerido, debe ser mayor a 0
- `precioBase`: Requerido, debe ser mayor a 0
- `tipoComponente`: Requerido (MONITOR, DISCO_DURO, TARJETA_VIDEO, PC)
- `capacidadAlm`: Opcional, máximo 20 caracteres (específico para discos duros)
- `memoria`: Opcional, máximo 20 caracteres (específico para tarjetas de video)

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Componente creado exitosamente",
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

### 2. Modificar Componente

**Caso de Uso:** 1.2 - Modificar componente

```http
PUT /componentes/{id}
```

**Autorización:** `ADMIN`, `GERENTE`, `INVENTARIO`

**Path Parameters:**
- `id` (string, required) - ID del componente a modificar

**Request Body:**
```json
{
  "descripcion": "string",
  "marca": "string",
  "modelo": "string",
  "costo": "number",
  "precioBase": "number",
  "tipoComponente": "string",
  "capacidadAlm": "string",
  "memoria": "string"
}
```

**Validaciones:** (mismas que crear, excepto que no incluye `id`)

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Componente actualizado exitosamente",
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

### 3. Eliminar Componente

**Caso de Uso:** 1.3 - Eliminar componente

```http
DELETE /componentes/{id}
```

**Autorización:** `ADMIN` únicamente

**Path Parameters:**
- `id` (string, required) - ID del componente a eliminar

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Componente eliminado exitosamente",
  "datos": null
}
```

---

### 4. Obtener Todos los Componentes

**Caso de Uso:** 1.4 - Consultar componentes (listado completo)

```http
GET /componentes
```

**Autorización:** Todos los roles autenticados

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Componentes obtenidos exitosamente",
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

### 5. Obtener Componente por ID

**Caso de Uso:** 1.4 - Consultar componentes (búsqueda específica)

```http
GET /componentes/{id}
```

**Autorización:** Todos los roles autenticados

**Path Parameters:**
- `id` (string, required) - ID del componente a consultar

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Componente encontrado exitosamente",
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

### 6. Obtener Componentes por Tipo

**Caso de Uso:** 1.4 - Consultar componentes (filtrado por tipo)

```http
GET /componentes/tipo/{tipo}
```

**Autorización:** Todos los roles autenticados

**Path Parameters:**
- `tipo` (string, required) - Tipo de componente: `MONITOR`, `DISCO_DURO`, `TARJETA_VIDEO`, `PC`

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Componentes encontrados por tipo",
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

### 7. Verificar Existencia de Componente

**Caso de Uso:** Adicional - Verificar existencia

```http
GET /componentes/{id}/existe
```

**Autorización:** Todos los roles autenticados

**Path Parameters:**
- `id` (string, required) - ID del componente a verificar

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Verificación completada",
  "datos": true
}
```

---

## Tipos de Componente Soportados

- **`MONITOR`** - Monitores y pantallas
- **`DISCO_DURO`** - Discos duros y SSD (requiere `capacidadAlm`)
- **`TARJETA_VIDEO`** - Tarjetas gráficas (requiere `memoria`)
- **`PC`** - Computadoras completas

### Campos Específicos por Tipo

- **Disco Duro (`DISCO_DURO`)**: Requiere campo `capacidadAlm` (ej: "1TB", "500GB")
- **Tarjeta de Video (`TARJETA_VIDEO`)**: Requiere campo `memoria` (ej: "8GB", "16GB")
- **Monitor y PC**: No requieren campos adicionales

---

## Ejemplos de Uso

### Crear un disco duro

```bash
curl -X POST "http://localhost/api/componentes" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "DD001",
    "descripcion": "Disco duro SSD NVMe de alta velocidad",
    "marca": "Samsung",
    "modelo": "980 PRO",
    "costo": 2500.00,
    "precioBase": 3200.00,
    "tipoComponente": "DISCO_DURO",
    "capacidadAlm": "1TB"
  }'
```

### Crear una tarjeta de video

```bash
curl -X POST "http://localhost/api/componentes" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "GPU001",
    "descripcion": "Tarjeta gráfica para gaming de alta gama",
    "marca": "NVIDIA",
    "modelo": "RTX 4080",
    "costo": 25000.00,
    "precioBase": 32000.00,
    "tipoComponente": "TARJETA_VIDEO",
    "memoria": "16GB"
  }'
```

---

*Documentación generada para ms-cotizador-componentes v1.0*
*Estructuras verificadas contra DTOs reales del proyecto*
*Última actualización: 2025-08-20*