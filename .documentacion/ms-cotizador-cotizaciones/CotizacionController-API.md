# API Documentation: CotizacionController

## Descripción General

El `CotizacionController` es el controlador REST principal del microservicio **ms-cotizador-cotizaciones** que gestiona las operaciones de cotización de PC completas. Implementa la lógica de negocio de cotización utilizando diferentes algoritmos de cálculo y sistemas de impuestos, delegando el procesamiento complejo a la capa de servicio que integra el dominio de cotización.

**Base URL:** `/cotizaciones`

## Características de Seguridad

- **Autenticación:** JWT Bearer Token requerido
- **Autorización:** Role-Based Access Control (RBAC)
- **Configuración CORS:** Permitido desde cualquier origen (`@CrossOrigin(origins = "*")`)
- **Roles del Sistema:**
  - `ADMIN` - Acceso completo a todas las operaciones
  - `GERENTE` - Acceso de gestión (crear, ver, buscar cotizaciones)
  - `VENDEDOR` - Acceso de ventas (crear y ver cotizaciones propias)
  - `INVENTARIO` - Solo lectura para planificación de inventario
  - `CONSULTOR` - Solo lectura para análisis y consultoría

## Mapeo de Códigos de Error

El controlador utiliza un sistema estándar de mapeo de códigos:

- **Código "0"** → HTTP 200 (OK)
- **Código "3"** → HTTP 500 (Internal Server Error)
- **Otros códigos** → HTTP 400 (Bad Request)

---

## Endpoints

### 1. Crear Cotización

**Caso de Uso:** Generar nueva cotización con algoritmos de dominio

```http
POST /cotizaciones
```

**Autorización:** `ADMIN`, `GERENTE`, `VENDEDOR`

**Request Body:**
```json
{
  "tipoCotizador": "string",
  "impuestos": ["string"],
  "detalles": [
    {
      "idComponente": "string",
      "cantidad": "integer",
      "descripcion": "string",
      "precioBase": "number"
    }
  ],
  "observaciones": "string"
}
```

**Validaciones:**
- `tipoCotizador`: Requerido (valores típicos: "A", "B" según algoritmos disponibles)
- `impuestos`: Opcional, lista de tipos de impuestos (IVA, LOCAL, FEDERAL, etc.)
- `detalles`: Requerido, debe tener al menos un elemento
  - `idComponente`: Requerido, máximo 50 caracteres
  - `cantidad`: Requerido, debe ser positivo
  - `descripcion`: Opcional, máximo 200 caracteres
  - `precioBase`: Opcional, debe ser positivo (se puede obtener del repositorio)
- `observaciones`: Opcional

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Cotización creada exitosamente",
  "datos": {
    "folio": "integer",
    "fecha": "string",
    "subtotal": "number",
    "impuestos": "number",
    "total": "number",
    "detalles": [
      {
        "numDetalle": "integer",
        "idComponente": "string",
        "nombreComponente": "string",
        "categoria": "string",
        "cantidad": "integer",
        "descripcion": "string",
        "precioBase": "number",
        "importeTotal": "number"
      }
    ],
    "observaciones": "string"
  }
}
```

---

### 2. Obtener Cotización por ID

**Caso de Uso:** Consultar cotización específica

```http
GET /cotizaciones/{id}
```

**Autorización:** Todos los roles autenticados

**Path Parameters:**
- `id` (integer, required) - Folio de la cotización a consultar

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Cotización encontrada exitosamente",
  "datos": {
    "folio": "integer",
    "fecha": "string",
    "subtotal": "number",
    "impuestos": "number",
    "total": "number",
    "detalles": [
      {
        "numDetalle": "integer",
        "idComponente": "string",
        "nombreComponente": "string",
        "categoria": "string",
        "cantidad": "integer",
        "descripcion": "string",
        "precioBase": "number",
        "importeTotal": "number"
      }
    ],
    "observaciones": "string"
  }
}
```

---

### 3. Listar Todas las Cotizaciones

**Caso de Uso:** Obtener listado completo de cotizaciones

```http
GET /cotizaciones
```

**Autorización:** Todos los roles autenticados

**Nota:** Los datos se filtran según el rol del usuario (implementado en la capa de servicio)

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Cotizaciones obtenidas exitosamente",
  "datos": [
    {
      "folio": "integer",
      "fecha": "string",
      "subtotal": "number",
      "impuestos": "number",
      "total": "number",
      "detalles": [
        {
          "numDetalle": "integer",
          "idComponente": "string",
          "nombreComponente": "string",
          "categoria": "string",
          "cantidad": "integer",
          "descripcion": "string",
          "precioBase": "number",
          "importeTotal": "number"
        }
      ],
      "observaciones": "string"
    }
  ]
}
```

---

### 4. Buscar Cotizaciones por Fecha

**Caso de Uso:** Consultar cotizaciones de una fecha específica

```http
GET /cotizaciones/buscar/fecha?fecha={fecha}
```

**Autorización:** Todos los roles autenticados

**Query Parameters:**
- `fecha` (string, required) - Fecha de búsqueda en formato YYYY-MM-DD

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Cotizaciones encontradas por fecha",
  "datos": [
    {
      "folio": "integer",
      "fecha": "string",
      "subtotal": "number",
      "impuestos": "number",
      "total": "number",
      "detalles": [
        {
          "numDetalle": "integer",
          "idComponente": "string",
          "nombreComponente": "string",
          "categoria": "string",
          "cantidad": "integer",
          "descripcion": "string",
          "precioBase": "number",
          "importeTotal": "number"
        }
      ],
      "observaciones": "string"
    }
  ]
}
```

---

## Sistema de Cotización

### Tipos de Cotizador

El sistema implementa diferentes algoritmos de cotización mediante el parámetro `tipoCotizador`:

- **Tipo "A"**: Algoritmo de cotización estándar
- **Tipo "B"**: Algoritmo de cotización alternativo
- **Otros tipos**: Según la implementación del dominio

Cada tipo de cotizador aplica diferentes estrategias de cálculo, descuentos y márgenes.

### Sistema de Impuestos

El sistema permite aplicar múltiples tipos de impuestos:

- **IVA**: Impuesto al Valor Agregado
- **LOCAL**: Impuestos locales específicos
- **FEDERAL**: Impuestos federales
- **Otros**: Según la configuración del dominio

Los impuestos se calculan automáticamente según:
- El país/región configurado (México, USA, Canadá)
- Los tipos de impuestos especificados en la request
- Las reglas de negocio implementadas en el dominio

### Procesamiento de Detalles

Para cada detalle de cotización:

1. **Resolución de Componente**: Se busca el componente por ID en el repositorio
2. **Cálculo de Precios**: Se aplican los algoritmos de cotización seleccionados
3. **Aplicación de Promociones**: Se evalúan y aplican promociones vigentes
4. **Cálculo de Impuestos**: Se calculan los impuestos según la configuración
5. **Generación de Importe**: Se calcula el importe total del detalle

---

## Reglas de Negocio

### Validaciones de Datos
- Una cotización debe tener al menos un detalle
- Cada detalle debe referenciar un componente válido
- Las cantidades deben ser positivas
- Los precios base, si se proporcionan, deben ser positivos

### Autorización por Rol
- **Creación**: Solo roles con permisos de negocio (ADMIN, GERENTE, VENDEDOR)
- **Consulta**: Todos los roles, pero con filtrado de datos según privilegios
- **Búsqueda por fecha**: Disponible para reportes y análisis (todos los roles)

### Integración con Dominio
- La lógica de cotización se delega completamente al dominio
- El controlador no contiene lógica de negocio, solo validación de DTOs
- Los errores de dominio se mapean automáticamente a códigos HTTP

---

## Ejemplos de Uso

### Crear cotización con algoritmo estándar

```bash
curl -X POST "http://localhost/api/cotizaciones" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "tipoCotizador": "A",
    "impuestos": ["IVA", "LOCAL"],
    "detalles": [
      {
        "idComponente": "GPU001",
        "cantidad": 1,
        "descripcion": "Tarjeta gráfica gaming"
      },
      {
        "idComponente": "MON001",
        "cantidad": 2,
        "descripcion": "Monitor 4K"
      }
    ],
    "observaciones": "Cotización para cliente corporativo"
  }'
```

### Crear cotización con precios base especificados

```bash
curl -X POST "http://localhost/api/cotizaciones" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "tipoCotizador": "B",
    "impuestos": ["IVA"],
    "detalles": [
      {
        "idComponente": "PC001",
        "cantidad": 1,
        "descripcion": "PC Gaming completa",
        "precioBase": 45000.00
      }
    ],
    "observaciones": "Cliente solicita cotización urgente"
  }'
```

### Consultar cotización específica

```bash
curl -X GET "http://localhost/api/cotizaciones/1001" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### Buscar cotizaciones por fecha

```bash
curl -X GET "http://localhost/api/cotizaciones/buscar/fecha?fecha=2025-01-15" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### Listar todas las cotizaciones

```bash
curl -X GET "http://localhost/api/cotizaciones" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

---

## Diferencias con otros Microservicios

### Arquitectura de Dominio
- **ms-cotizador-cotizaciones**: Implementa Domain-Driven Design con rica lógica de negocio
- **ms-cotizador-componentes**: Arquitectura más simple centrada en CRUD de entidades

### Complejidad de Cálculos
- **Algoritmos múltiples**: Diferentes estrategias de cotización (CotizadorA, CotizadorB)
- **Sistema de impuestos**: Cálculos complejos con múltiples tipos y países
- **Integración de promociones**: Aplicación automática de descuentos y ofertas

### Filtrado por Roles
- Los datos se filtran automáticamente según el rol del usuario
- VENDEDOR solo ve sus propias cotizaciones
- INVENTARIO y CONSULTOR tienen acceso de solo lectura
- ADMIN y GERENTE tienen acceso completo

---

## Metadatos de Respuesta

### Información Calculada Automáticamente
- **`folio`**: Número único generado automáticamente
- **`fecha`**: Fecha de creación en formato string
- **`subtotal`**: Suma de importes antes de impuestos
- **`impuestos`**: Total de impuestos calculados
- **`total`**: Importe final incluyendo impuestos

### Detalles Enriquecidos
- **`numDetalle`**: Número secuencial del detalle
- **`nombreComponente`**: Nombre completo obtenido del repositorio
- **`categoria`**: Categoría del componente
- **`importeTotal`**: Importe calculado para el detalle específico

---

*Documentación generada para ms-cotizador-cotizaciones v1.0*
*Estructuras verificadas contra DTOs reales del proyecto*
*Última actualización: 2025-08-20*