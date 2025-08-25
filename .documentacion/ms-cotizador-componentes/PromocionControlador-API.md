# API Documentation: PromocionControlador

## Descripción General

El `PromocionControlador` es el controlador REST que gestiona el sistema complejo de promociones en el microservicio **ms-cotizador-componentes**. Implementa un sistema de promociones jerárquicas con tipos base y acumulables, donde cada promoción puede tener múltiples detalles con diferentes configuraciones de descuento.

**Base URL:** `/promociones`

## Características de Seguridad

- **Autenticación:** JWT Bearer Token requerido
- **Autorización:** Role-Based Access Control (RBAC)
- **Roles del Sistema:**
  - `ADMIN` - Acceso completo (crear, modificar, eliminar, consultar)
  - `GERENTE` - Gestión estratégica (crear, modificar, eliminar, consultar)
  - `VENDEDOR` - Solo lectura para aplicar promociones en ventas
  - `INVENTARIO` - Solo lectura para consultar impacto en inventario
  - `CONSULTOR` - Solo lectura para análisis de efectividad

**Nota:** Las promociones tienen alto impacto financiero, por lo que las operaciones de escritura están restringidas a ADMIN y GERENTE únicamente.

## Mapeo de Códigos de Error

El controlador utiliza un sistema estándar de mapeo de códigos:

- **Código "0"** → HTTP 200 (OK)
- **Código "3"** → HTTP 500 (Internal Server Error)
- **Otros códigos** → HTTP 400 (Bad Request)

---

## Endpoints

### 1. Crear Promoción

**Caso de Uso:** 6.1 - Agregar promoción

```http
POST /promociones
```

**Autorización:** `ADMIN`, `GERENTE` únicamente

**Request Body:**
```json
{
  "nombre": "string",
  "descripcion": "string",
  "vigenciaDesde": "YYYY-MM-DD",
  "vigenciaHasta": "YYYY-MM-DD",
  "detalles": [
    {
      "nombre": "string",
      "esBase": "boolean",
      "tipoBase": "string",
      "parametrosNxM": {
        "llevent": "integer",
        "paguen": "integer"
      },
      "tipoAcumulable": "string",
      "porcentajeDescuentoPlano": "number",
      "escalasDescuento": [
        {
          "cantidadMinima": "integer",
          "cantidadMaxima": "integer",
          "porcentajeDescuento": "number"
        }
      ]
    }
  ]
}
```

**Validaciones:**
- `nombre`: Requerido, entre 2 y 100 caracteres
- `descripcion`: Requerida, entre 5 y 500 caracteres
- `vigenciaDesde`: Requerida, debe ser anterior o igual a `vigenciaHasta`
- `vigenciaHasta`: Requerida
- `detalles`: Requerido, debe tener al menos un elemento, exactamente uno debe ser base (`esBase: true`)

**Tipos de Promoción Base:**
- `SIN_DESCUENTO` - Promoción regular sin descuento
- `NXM` - Compra N unidades, paga M (requiere `parametrosNxM`)

**Tipos de Promoción Acumulable:**
- `DESCUENTO_PLANO` - Descuento fijo (requiere `porcentajeDescuentoPlano`)
- `DESCUENTO_POR_CANTIDAD` - Descuento por escalas (requiere `escalasDescuento`)

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Promoción creada exitosamente",
  "datos": {
    "idPromocion": "integer",
    "nombre": "string",
    "descripcion": "string",
    "vigenciaDesde": "YYYY-MM-DD",
    "vigenciaHasta": "YYYY-MM-DD",
    "detalles": [
      {
        "idDetalle": "integer",
        "nombre": "string",
        "esBase": "boolean",
        "descripcionTipo": "string",
        "tipoBase": "string",
        "llevent": "integer",
        "paguen": "integer",
        "tipoAcumulable": "string",
        "porcentajeDescuentoPlano": "number",
        "escalasDescuento": [
          {
            "cantidadMinima": "integer",
            "cantidadMaxima": "integer",
            "porcentajeDescuento": "number"
          }
        ]
      }
    ],
    "estadoVigencia": "string",
    "diasRestantes": "integer",
    "totalDetalles": "integer",
    "tipoPromocionPrincipal": "string",
    "tieneDescuentosAcumulables": "boolean"
  }
}
```

---

### 2. Actualizar Promoción

**Caso de Uso:** 6.2 - Modificar promoción

```http
PUT /promociones/{id}
```

**Autorización:** `ADMIN`, `GERENTE` únicamente

**Path Parameters:**
- `id` (integer, required) - ID de la promoción a actualizar

**Request Body:** (misma estructura que crear, excepto que no incluye `idPromocion`)

**Response:** (misma estructura que crear)

---

### 3. Buscar Promoción por ID

**Caso de Uso:** 6.3 - Consultar promoción específica

```http
GET /promociones/{id}
```

**Autorización:** Todos los roles autenticados

**Path Parameters:**
- `id` (integer, required) - ID de la promoción a consultar

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Promoción encontrada exitosamente",
  "datos": {
    "idPromocion": "integer",
    "nombre": "string",
    "descripcion": "string",
    "vigenciaDesde": "YYYY-MM-DD",
    "vigenciaHasta": "YYYY-MM-DD",
    "detalles": [
      {
        "idDetalle": "integer",
        "nombre": "string",
        "esBase": "boolean",
        "descripcionTipo": "string",
        "tipoBase": "string",
        "llevent": "integer",
        "paguen": "integer",
        "tipoAcumulable": "string",
        "porcentajeDescuentoPlano": "number",
        "escalasDescuento": [
          {
            "cantidadMinima": "integer",
            "cantidadMaxima": "integer",
            "porcentajeDescuento": "number"
          }
        ]
      }
    ],
    "estadoVigencia": "string",
    "diasRestantes": "integer",
    "totalDetalles": "integer",
    "tipoPromocionPrincipal": "string",
    "tieneDescuentosAcumulables": "boolean"
  }
}
```

---

### 4. Obtener Todas las Promociones

**Caso de Uso:** 6.3 - Consultar todas las promociones

```http
GET /promociones
```

**Autorización:** Todos los roles autenticados

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Promociones obtenidas exitosamente",
  "datos": [
    {
      "idPromocion": "integer",
      "nombre": "string",
      "descripcion": "string",
      "vigenciaDesde": "YYYY-MM-DD",
      "vigenciaHasta": "YYYY-MM-DD",
      "detalles": [...],
      "estadoVigencia": "string",
      "diasRestantes": "integer",
      "totalDetalles": "integer",
      "tipoPromocionPrincipal": "string",
      "tieneDescuentosAcumulables": "boolean"
    }
  ]
}
```

---

### 5. Eliminar Promoción

**Caso de Uso:** 6.4 - Eliminar promoción

```http
DELETE /promociones/{id}
```

**Autorización:** `ADMIN`, `GERENTE` únicamente

**Path Parameters:**
- `id` (integer, required) - ID de la promoción a eliminar

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Promoción eliminada exitosamente",
  "datos": null
}
```

---

## Sistema de Promociones

### Arquitectura Jerárquica

Cada promoción está compuesta por:
- **Datos básicos**: nombre, descripción, vigencia
- **Detalles múltiples**: configuraciones específicas de descuento
- **Detalle base obligatorio**: exactamente uno por promoción (`esBase: true`)
- **Detalles acumulables opcionales**: pueden ser múltiples (`esBase: false`)

### Tipos de Promoción Base

#### 1. SIN_DESCUENTO
- **Propósito**: Promoción regular sin descuento aplicado
- **Parámetros**: Ninguno adicional
- **Uso**: Promociones de visibilidad o marketing sin impacto en precio

#### 2. NXM (Compra N, Paga M)
- **Propósito**: Descuento por cantidad tipo "2x1", "3x2", etc.
- **Parámetros**: 
  - `llevent`: Cantidad que se lleva el cliente
  - `paguen`: Cantidad que paga el cliente
- **Ejemplo**: `llevent: 3, paguen: 2` = "Compra 3, paga 2"

### Tipos de Promoción Acumulable

#### 1. DESCUENTO_PLANO
- **Propósito**: Descuento fijo adicional
- **Parámetros**: `porcentajeDescuentoPlano` (0.0 - 100.0)
- **Uso**: Descuentos adicionales que se suman a la promoción base

#### 2. DESCUENTO_POR_CANTIDAD
- **Propósito**: Descuentos variables según escalas de cantidad
- **Parámetros**: Array de `escalasDescuento`
- **Uso**: "Compra 5+ obtén 5% extra, 10+ obtén 10% extra"

### Estados de Vigencia

Los metadatos calculados automáticamente incluyen:
- **VIGENTE**: Promoción activa actualmente
- **EXPIRADA**: Promoción que ya venció
- **FUTURA**: Promoción que aún no inicia
- **INDEFINIDA**: Fechas no configuradas correctamente

---

## Reglas de Negocio

### Validaciones de Estructura
- Cada promoción debe tener exactamente **un detalle base** (`esBase: true`)
- Una promoción puede tener múltiples **detalles acumulables** (`esBase: false`)
- Las fechas de vigencia deben ser consistentes (`vigenciaDesde <= vigenciaHasta`)

### Validaciones por Tipo
- **NXM**: Requiere `parametrosNxM` con `llevent > paguen > 0`
- **DESCUENTO_PLANO**: Requiere `porcentajeDescuentoPlano` entre 0.0 y 100.0
- **DESCUENTO_POR_CANTIDAD**: Requiere al menos una escala de descuento válida

### Restricciones de Seguridad
- Solo ADMIN y GERENTE pueden crear, modificar o eliminar promociones
- Todos los roles pueden consultar promociones (necesario para aplicarlas en ventas)
- El sistema valida que no haya componentes asociados antes de eliminar

---

## Ejemplos de Uso

### Crear promoción "2x1 en monitores"

```bash
curl -X POST "http://localhost/api/promociones" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "2x1 en Monitores Gaming",
    "descripcion": "Promoción especial: compra 2 monitores gaming y paga solo 1",
    "vigenciaDesde": "2025-01-01",
    "vigenciaHasta": "2025-01-31",
    "detalles": [
      {
        "nombre": "Promoción Base 2x1",
        "esBase": true,
        "tipoBase": "NXM",
        "parametrosNxM": {
          "llevent": 2,
          "paguen": 1
        }
      }
    ]
  }'
```

### Crear promoción con descuento acumulable

```bash
curl -X POST "http://localhost/api/promociones" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "3x2 + Descuento por Volumen",
    "descripcion": "Compra 3 paga 2, más descuentos adicionales por cantidad",
    "vigenciaDesde": "2025-02-01",
    "vigenciaHasta": "2025-02-28",
    "detalles": [
      {
        "nombre": "Base 3x2",
        "esBase": true,
        "tipoBase": "NXM",
        "parametrosNxM": {
          "llevent": 3,
          "paguen": 2
        }
      },
      {
        "nombre": "Descuento Extra por Volumen",
        "esBase": false,
        "tipoAcumulable": "DESCUENTO_POR_CANTIDAD",
        "escalasDescuento": [
          {
            "cantidadMinima": 5,
            "cantidadMaxima": 9,
            "porcentajeDescuento": 5.0
          },
          {
            "cantidadMinima": 10,
            "cantidadMaxima": 999,
            "porcentajeDescuento": 10.0
          }
        ]
      }
    ]
  }'
```

### Consultar promoción específica

```bash
curl -X GET "http://localhost/api/promociones/1" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### Actualizar vigencia de promoción

```bash
curl -X PUT "http://localhost/api/promociones/1" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "2x1 en Monitores Gaming (Extendida)",
    "descripcion": "Promoción especial extendida hasta marzo",
    "vigenciaDesde": "2025-01-01",
    "vigenciaHasta": "2025-03-31",
    "detalles": [
      {
        "nombre": "Promoción Base 2x1",
        "esBase": true,
        "tipoBase": "NXM",
        "parametrosNxM": {
          "llevent": 2,
          "paguen": 1
        }
      }
    ]
  }'
```

---

## Metadatos Calculados

El sistema calcula automáticamente metadatos útiles para el frontend:

- **`estadoVigencia`**: Estado actual basado en fechas
- **`diasRestantes`**: Días hasta vencimiento (si aplica)
- **`totalDetalles`**: Número total de detalles configurados
- **`tipoPromocionPrincipal`**: Descripción del detalle base
- **`tieneDescuentosAcumulables`**: Si tiene detalles adicionales

Estos metadatos se actualizan automáticamente en cada respuesta y facilitan la presentación en interfaces de usuario.

---

*Documentación generada para ms-cotizador-componentes v1.0*
*Estructuras verificadas contra DTOs reales del proyecto*
*Última actualización: 2025-08-20*