# API Documentation: ProveedorController

## Descripción General

El `ProveedorController` es el controlador REST del microservicio **ms-cotizador-pedidos** que gestiona la administración completa de proveedores del sistema. Implementa las operaciones CRUD completas para la gestión de la cadena de suministro, incluyendo funcionalidades avanzadas de búsqueda y filtrado para facilitar las operaciones comerciales e inventario.

**Base URL:** `/proveedores`

## Características de Seguridad

- **Autenticación:** JWT Bearer Token requerido
- **Autorización:** Role-Based Access Control (RBAC)
- **Configuración CORS:** Permitido desde cualquier origen (`@CrossOrigin(origins = "*")`)
- **Logging Extensivo:** Auditoría completa con logging crítico para debug
- **Roles del Sistema:**
  - `ADMIN` - Acceso completo incluyendo eliminación de proveedores
  - `GERENTE` - Gestión comercial (crear, ver, modificar, buscar - sin eliminar)
  - `VENDEDOR` - Solo lectura para proceso de ventas (ver, buscar)
  - `INVENTARIO` - Gestión de inventario (crear, ver, modificar, buscar - sin eliminar)
  - `CONSULTOR` - Solo lectura para consultoría (ver, buscar)

## Mapeo de Códigos de Error

El controlador utiliza un sistema estándar de mapeo de códigos:

- **Código "0"** → HTTP 200 (OK)
- **Código "3"** → HTTP 500 (Internal Server Error)
- **Otros códigos** → HTTP 400 (Bad Request)

---

## Endpoints

### 1. Crear Proveedor

**Caso de Uso:** 4.1 - Agregar proveedor

```http
POST /proveedores
```

**Autorización:** `ADMIN`, `GERENTE`, `INVENTARIO`

**Request Body:**
```json
{
  "cve": "string",
  "nombre": "string",
  "razonSocial": "string"
}
```

**Validaciones:**
- `cve`: Requerida, máximo 10 caracteres, debe ser única en el sistema
- `nombre`: Requerido, máximo 100 caracteres
- `razonSocial`: Requerida, máximo 200 caracteres

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Proveedor creado exitosamente",
  "datos": {
    "cve": "string",
    "nombre": "string",
    "razonSocial": "string",
    "numeroPedidos": "integer"
  }
}
```

---

### 2. Actualizar Proveedor

**Caso de Uso:** 4.2 - Modificar proveedor

```http
PUT /proveedores/{cve}
```

**Autorización:** `ADMIN`, `GERENTE`, `INVENTARIO`

**Path Parameters:**
- `cve` (string, required) - Clave del proveedor a actualizar

**Request Body:**
```json
{
  "nombre": "string",
  "razonSocial": "string"
}
```

**Validaciones:**
- `nombre`: Requerido, máximo 100 caracteres
- `razonSocial`: Requerida, máximo 200 caracteres

**Nota:** La clave (`cve`) no se puede modificar, se toma del path parameter

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Proveedor actualizado exitosamente",
  "datos": {
    "cve": "string",
    "nombre": "string",
    "razonSocial": "string",
    "numeroPedidos": "integer"
  }
}
```

---

### 3. Consultar Proveedor por Clave

**Caso de Uso:** 4.3 - Consultar proveedores (búsqueda específica)

```http
GET /proveedores/{cve}
```

**Autorización:** Todos los roles autenticados (datos filtrados según el rol)

**Path Parameters:**
- `cve` (string, required) - Clave del proveedor a consultar

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Proveedor encontrado exitosamente",
  "datos": {
    "cve": "string",
    "nombre": "string",
    "razonSocial": "string",
    "numeroPedidos": "integer"
  }
}
```

---

### 4. Consultar Todos los Proveedores

**Caso de Uso:** 4.3 - Consultar proveedores (listado completo)

```http
GET /proveedores
```

**Autorización:** Todos los roles autenticados (datos filtrados según el rol)

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Lista de proveedores obtenida exitosamente",
  "datos": [
    {
      "cve": "string",
      "nombre": "string",
      "razonSocial": "string",
      "numeroPedidos": "integer"
    }
  ]
}
```

---

### 5. Eliminar Proveedor

**Caso de Uso:** 4.4 - Eliminar proveedor

```http
DELETE /proveedores/{cve}
```

**Autorización:** `ADMIN` únicamente (operación crítica para integridad del sistema)

**Path Parameters:**
- `cve` (string, required) - Clave del proveedor a eliminar

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Proveedor eliminado exitosamente",
  "datos": null
}
```

---

### 6. Buscar Proveedores por Nombre

**Caso de Uso:** Búsqueda avanzada - Filtro por nombre

```http
GET /proveedores/buscar/nombre?nombre={nombre}
```

**Autorización:** Todos los roles autenticados

**Query Parameters:**
- `nombre` (string, required) - Nombre o parte del nombre a buscar (búsqueda parcial)

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Proveedores encontrados por nombre",
  "datos": [
    {
      "cve": "string",
      "nombre": "string",
      "razonSocial": "string",
      "numeroPedidos": "integer"
    }
  ]
}
```

---

### 7. Buscar Proveedores por Razón Social

**Caso de Uso:** Búsqueda avanzada - Filtro por razón social

```http
GET /proveedores/buscar/razon-social?razonSocial={razonSocial}
```

**Autorización:** Todos los roles autenticados

**Query Parameters:**
- `razonSocial` (string, required) - Razón social o parte de la razón social a buscar (búsqueda parcial)

**Response:**
```json
{
  "codigo": "0",
  "mensaje": "Proveedores encontrados por razón social",
  "datos": [
    {
      "cve": "string",
      "nombre": "string",
      "razonSocial": "string",
      "numeroPedidos": "integer"
    }
  ]
}
```

---

## Sistema de Gestión de Proveedores

### Identificación por Clave
- **Clave única (`cve`)**: Identificador principal máximo 10 caracteres
- **Inmutable**: La clave no se puede modificar una vez creado el proveedor
- **Validación de unicidad**: El sistema previene duplicados

### Información Comercial
- **Nombre comercial**: Nombre bajo el cual opera el proveedor
- **Razón social**: Denominación legal completa de la empresa
- **Diferenciación**: Permite distinguir entre nombre comercial y legal

### Estadísticas Integradas
- **Número de pedidos**: Contador automático de pedidos asociados
- **Metadatos**: Información calculada para reportes y análisis

---

## Reglas de Negocio

### Validaciones de Datos
- **Clave única**: No se permiten claves duplicadas en el sistema
- **Longitudes máximas**: Límites estrictos para garantizar integridad de datos
- **Campos obligatorios**: Toda la información básica es requerida

### Autorización por Rol
- **Creación/Modificación**: Solo roles operativos (ADMIN, GERENTE, INVENTARIO)
- **Consulta**: Todos los roles con filtrado automático según privilegios
- **Eliminación**: Exclusivamente ADMIN para proteger integridad del sistema
- **Búsquedas**: Disponible para todos los roles para facilitar operaciones

### Filtrado de Datos por Rol
- **VENDEDOR**: Ve proveedores relevantes para ventas
- **INVENTARIO**: Acceso enfocado en gestión de suministros
- **CONSULTOR**: Vista de solo lectura para análisis
- **GERENTE/ADMIN**: Acceso completo sin restricciones

### Integridad Referencial
- **Validación de eliminación**: Prevención de eliminar proveedores con pedidos activos
- **Actualización de estadísticas**: Recalculo automático de contadores
- **Auditoría**: Logging extensivo para trazabilidad completa

---

## Funcionalidades de Búsqueda

### Búsqueda por Nombre
- **Tipo**: Búsqueda parcial (LIKE)
- **Sensibilidad**: Insensible a mayúsculas/minúsculas
- **Uso típico**: Búsqueda rápida durante generación de pedidos

### Búsqueda por Razón Social
- **Tipo**: Búsqueda parcial (LIKE)
- **Sensibilidad**: Insensible a mayúsculas/minúsculas
- **Uso típico**: Búsquedas legales y contractuales

### Optimización de Consultas
- **Índices**: Optimización en campos de búsqueda frecuente
- **Filtrado**: Aplicación automática de filtros por rol
- **Performance**: Diseñado para manejo de grandes volúmenes

---

## Ejemplos de Uso

### Crear nuevo proveedor

```bash
curl -X POST "http://localhost/api/proveedores" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "cve": "TECH001",
    "nombre": "TechSupply México",
    "razonSocial": "Tecnología y Suministros de México S.A. de C.V."
  }'
```

### Actualizar información de proveedor

```bash
curl -X PUT "http://localhost/api/proveedores/TECH001" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "TechSupply México Plus",
    "razonSocial": "Tecnología y Suministros de México Plus S.A. de C.V."
  }'
```

### Consultar proveedor específico

```bash
curl -X GET "http://localhost/api/proveedores/TECH001" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### Listar todos los proveedores

```bash
curl -X GET "http://localhost/api/proveedores" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### Buscar proveedores por nombre

```bash
curl -X GET "http://localhost/api/proveedores/buscar/nombre?nombre=Tech" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### Buscar proveedores por razón social

```bash
curl -X GET "http://localhost/api/proveedores/buscar/razon-social?razonSocial=Tecnología" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

### Eliminar proveedor (solo ADMIN)

```bash
curl -X DELETE "http://localhost/api/proveedores/TECH001" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

---

## Logging y Auditoría

### Logging Crítico para Debug
El controlador implementa logging extensivo especialmente en la creación de proveedores:

```java
// Logging detallado en creación
logger.debug("=== CONTROLLER DEBUG ===");
logger.debug("Respuesta servicio código: {}", respuestaServicio.getCodigo());
logger.debug("Data.getCve(): {}", data.getCve());
logger.debug("Data.getNombre(): {}", data.getNombre());
```

### Niveles de Logging
- **INFO**: Operaciones principales y resultados
- **DEBUG**: Información detallada para troubleshooting
- **ERROR**: Errores y excepciones del sistema

### Trazabilidad
- **Operaciones CRUD**: Logging completo de todas las operaciones
- **Búsquedas**: Registro de criterios de búsqueda
- **Cambios de estado**: Auditoría de modificaciones

---

## Diferencias con otros Microservicios

### Enfoque de Gestión B2B
- **ms-cotizador-pedidos (Proveedores)**: Gestión de cadena de suministro
- **ms-cotizador-componentes**: Catálogo de productos internos
- **ms-cotizador-cotizaciones**: Cálculos y estimaciones

### Búsquedas Avanzadas
- **Múltiples criterios**: Nombre y razón social
- **Búsqueda parcial**: Flexibilidad para encontrar proveedores
- **Optimización**: Diseñado para consultas frecuentes

### Seguridad Estricta
- **Eliminación restringida**: Solo ADMIN puede eliminar
- **Validación de integridad**: Prevención de eliminación con pedidos activos
- **Auditoría completa**: Logging extensivo para compliance

### Integración Empresarial
- **Información legal**: Razón social completa
- **Estadísticas automáticas**: Conteo de pedidos por proveedor
- **Filtrado por rol**: Datos relevantes según función empresarial

---

## Metadatos de Response

### Información Calculada Automáticamente
- **`numeroPedidos`**: Contador automático de pedidos asociados al proveedor
- **Estadísticas**: Metadatos útiles para reportes y análisis

### Campos de Identificación
- **`cve`**: Clave única inmutable del proveedor
- **`nombre`**: Nombre comercial para uso operativo
- **`razonSocial`**: Denominación legal para documentos oficiales

---

*Documentación generada para ms-cotizador-pedidos v1.0*
*Estructuras verificadas contra DTOs reales del proyecto*
*Logging crítico y auditoría completa implementada*
*Última actualización: 2025-08-20*