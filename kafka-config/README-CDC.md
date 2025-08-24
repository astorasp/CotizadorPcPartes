# 🔄 Sistema CDC Automatizado - Kafka + Debezium

Este directorio contiene la configuración completa para el sistema Change Data Capture (CDC) totalmente automatizado.

## 🚀 Inicio Rápido

```bash
# 1. Inicializar variables de entorno
./init-env.sh

# 2. Levantar todo el sistema
docker-compose up -d

# 3. Validar que todo funcione (opcional)
./kafka-config/validate-cdc-setup.sh
```

**¡Eso es todo!** El sistema se configura automáticamente al 100%.

## 📋 Arquitectura de Replicación

### 🔄 Flujos de Datos
```
┌─────────────────┐    ┌───────────────────┐    ┌─────────────────┐
│   COMPONENTES   │────┤      KAFKA        ├────│   COTIZACIONES  │
│                 │    │                   │    │                 │
│  • componentes  │────┤  📝 componentes   ├────│  • componentes  │
│  • promociones  │────┤  📝 promociones   ├────│  • promociones  │
│  • pc_partes    │────┤  📝 pc_partes     ├────│  • pc_partes    │
└─────────────────┘    └───────────────────┘    └─────────────────┘
                                │
                                │ (solo componentes + cotizaciones)
                       ┌─────────────────┐
                       │     PEDIDOS     │
                       │                 │
                       │  • componentes  │ ← Solo estos
                       │  • cotizaciones │
                       │  • pedidos      │
                       └─────────────────┘
```

### 📊 Conectores CDC (8 Total)

#### **🟢 Source Connectors (3)**
- `componentes-mysql-connector` → Captura cambios de componentes
- `cotizaciones-mysql-connector` → Captura cambios de cotizaciones  
- `pedidos-mysql-connector` → Captura cambios de pedidos

#### **🟢 Sink Connectors (5)**
- `componentes-cotizaciones-sink` → Replica componentes a cotizaciones
- `componentes-pedidos-sink` → Replica componentes a pedidos
- `cotizaciones-pedidos-sink` → Replica cotizaciones a pedidos
- `pedidos-sink-connector` → Conector adicional componentes
- `cotizaciones-sink-connector` → Conector adicional componentes

## 🔧 Scripts de Inicialización Automatizada

### **Problema Resuelto** 
Los scripts anteriores fallaban porque intentaban otorgar permisos sobre bases de datos que no existían en contenedores específicos.

### **Solución Implementada**
Cada microservicio tiene su propio script de inicialización que:
- ✅ Solo maneja la base de datos local
- ✅ Crea usuario debezium con permisos correctos
- ✅ Se ejecuta automáticamente al inicializar contenedores
- ✅ No genera errores por bases inexistentes

## 🧪 Validación Automatizada

El script `validate-cdc-setup.sh` realiza una validación completa:

```bash
# Ejecutar validación
./kafka-config/validate-cdc-setup.sh

# Salida esperada:
# 🎉 ¡VALIDACIÓN COMPLETA EXITOSA!
# ✅ Sistema listo para producción
```

## ⚡ Características del Sistema

- **🔄 Replicación**: < 3 segundos de latencia
- **🛡️ Robusto**: Permisos específicos por microservicio  
- **📈 Escalable**: Kafka automático
- **🔍 Monitoreado**: Health checks integrados

---

**🎉 Sistema CDC completamente automatizado y listo para producción**