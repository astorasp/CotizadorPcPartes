# Sistema de Loading - Ejemplos de Implementación

Este documento muestra cómo implementar el nuevo sistema de loading centralizado en el Portal Cotizador.

## 🏗️ Arquitectura del Sistema

El sistema consta de:

1. **useLoadingStore**: Store global para tracking de operaciones
2. **useAsyncOperation**: Composable para manejo automático de loading
3. **LoadingSpinner**: Componente básico de spinner
4. **LoadingButton**: Botón con estado de loading integrado
5. **LoadingOverlay**: Overlay para operaciones bloqueantes
6. **GlobalLoadingManager**: Manager global que se coloca en App.vue

## 📝 Ejemplos Prácticos

### 1. Usar en Stores (CRUD Operations)

```javascript
// En cualquier store (ej: useComponentesStore.js)
import { useCrudOperations } from '@/composables/useAsyncOperation'

export const useComponentesStore = defineStore('componentes', () => {
  // Usar el sistema de loading centralizado
  const crudOps = useCrudOperations('componente')
  
  // Operación de fetch con loading automático
  const fetchComponentes = async () => {
    const result = await crudOps.fetch(async () => {
      const data = await componentesApi.getAll()
      componentes.value = data || []
      return data
    })
    
    if (!result.success) {
      showAlert('error', result.error)
    }
    
    return result
  }
  
  // Operación de create con loading automático
  const createComponente = async (componenteData) => {
    const result = await crudOps.create(async () => {
      const response = await componentesApi.create(componenteData)
      await fetchComponentes() // Refrescar datos
      return response
    })
    
    if (result.success) {
      showAlert('success', 'Componente creado exitosamente')
    }
    
    return result
  }
  
  // Operación de update con loading automático
  const updateComponente = async (id, data) => {
    const result = await crudOps.update(async () => {
      const response = await componentesApi.update(id, data)
      await fetchComponentes() // Refrescar datos
      return response
    })
    
    return result
  }
  
  // Operación de delete con loading automático (bloquea UI)
  const deleteComponente = async (id) => {
    const result = await crudOps.remove(async () => {
      await componentesApi.delete(id)
      await fetchComponentes() // Refrescar datos
    })
    
    return result
  }
})
```

### 2. Usar LoadingButton en Vistas

```vue
<template>
  <div>
    <!-- Botón con loading automático -->
    <LoadingButton
      :loading="isCreating"
      variant="primary"
      size="md"
      @click="handleCreate"
    >
      <PlusIcon class="w-4 h-4 mr-2" />
      Nuevo Componente
    </LoadingButton>
    
    <!-- Botón de eliminar con confirmación -->
    <LoadingButton
      :loading="isDeleting"
      variant="danger"
      size="sm"
      @click="handleDelete"
    >
      <TrashIcon class="w-4 h-4 mr-2" />
      Eliminar
    </LoadingButton>
    
    <!-- Botón de guardar en formulario -->
    <LoadingButton
      :loading="isSaving"
      variant="primary"
      type="submit"
      full-width
      :disabled="!isFormValid"
    >
      Guardar Cambios
    </LoadingButton>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import LoadingButton from '@/components/ui/LoadingButton.vue'
import { useComponentesStore } from '@/stores/useComponentesStore'

const componentesStore = useComponentesStore()

const isCreating = ref(false)
const isDeleting = ref(false)
const isSaving = ref(false)

const handleCreate = async () => {
  isCreating.value = true
  try {
    const result = await componentesStore.createComponente(formData.value)
    if (result.success) {
      // Lógica de éxito
    }
  } finally {
    isCreating.value = false
  }
}

const handleDelete = async () => {
  if (!confirm('¿Está seguro de eliminar este componente?')) return
  
  isDeleting.value = true
  try {
    await componentesStore.deleteComponente(componenteId.value)
  } finally {
    isDeleting.value = false
  }
}
</script>
```

### 3. Usar el Composable para Operaciones Complejas

```vue
<script setup>
import { useAsyncOperation } from '@/composables/useAsyncOperation'

const asyncOp = useAsyncOperation({
  showNotification: true,
  type: 'modal'
})

// Operación simple con loading automático
const processData = async () => {
  const result = await asyncOp.execute(
    async () => {
      // Tu operación asíncrona aquí
      const response = await api.processComplexData()
      return response
    },
    {
      message: 'Procesando datos...',
      successMessage: 'Datos procesados correctamente',
      errorMessage: 'Error al procesar datos'
    }
  )
  
  if (result.success) {
    console.log('Éxito:', result.data)
  }
}

// Operaciones en paralelo
const loadAllData = async () => {
  const operations = [
    {
      operation: () => api.getComponentes(),
      config: { key: 'load-componentes', message: 'Cargando componentes...' }
    },
    {
      operation: () => api.getProveedores(),
      config: { key: 'load-proveedores', message: 'Cargando proveedores...' }
    },
    {
      operation: () => api.getCotizaciones(),
      config: { key: 'load-cotizaciones', message: 'Cargando cotizaciones...' }
    }
  ]
  
  const results = await asyncOp.executeParallel(operations, {
    blockUI: true,
    message: 'Cargando datos del sistema...'
  })
  
  console.log('Resultados:', results)
}

// Operaciones secuenciales con progreso
const migrateData = async () => {
  const steps = [
    {
      operation: () => api.backupData(),
      config: { message: 'Creando respaldo...' }
    },
    {
      operation: () => api.migrateComponents(),
      config: { message: 'Migrando componentes...' }
    },
    {
      operation: () => api.updateIndexes(),
      config: { message: 'Actualizando índices...' }
    },
    {
      operation: () => api.verifyMigration(),
      config: { message: 'Verificando migración...' }
    }
  ]
  
  const results = await asyncOp.executeSequential(steps, {
    blockUI: true,
    continueOnError: false
  })
  
  console.log('Migración completada:', results)
}
</script>
```

### 4. Usar LoadingSpinner Independiente

```vue
<template>
  <div class="data-container">
    <!-- Loading simple -->
    <LoadingSpinner 
      v-if="loading" 
      size="md" 
      message="Cargando datos..." 
      centered 
    />
    
    <!-- Loading con progreso -->
    <div v-else-if="uploading" class="upload-progress">
      <LoadingSpinner 
        size="lg" 
        color="primary" 
        message="Subiendo archivo..." 
      />
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: uploadProgress + '%' }"></div>
      </div>
      <span class="progress-text">{{ uploadProgress }}%</span>
    </div>
    
    <!-- Contenido normal -->
    <div v-else class="data-content">
      <!-- Tu contenido aquí -->
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

const loading = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
</script>
```

### 5. Integrar con Formularios

```vue
<template>
  <form @submit.prevent="handleSubmit">
    <!-- Campos del formulario -->
    <div class="form-fields">
      <input v-model="form.name" :disabled="submitting" />
      <textarea v-model="form.description" :disabled="submitting" />
    </div>
    
    <!-- Botones de acción -->
    <div class="form-actions">
      <button 
        type="button" 
        @click="cancel" 
        :disabled="submitting"
        class="btn-outline"
      >
        Cancelar
      </button>
      
      <LoadingButton
        type="submit"
        :loading="submitting"
        variant="primary"
        :disabled="!isFormValid"
      >
        {{ isEditMode ? 'Actualizar' : 'Crear' }}
      </LoadingButton>
    </div>
  </form>
</template>

<script setup>
import { ref, computed } from 'vue'
import LoadingButton from '@/components/ui/LoadingButton.vue'
import { useAsyncOperation } from '@/composables/useAsyncOperation'

const asyncOp = useAsyncOperation()
const submitting = ref(false)

const form = ref({
  name: '',
  description: ''
})

const isFormValid = computed(() => {
  return form.value.name.trim() && form.value.description.trim()
})

const handleSubmit = async () => {
  submitting.value = true
  
  try {
    const result = await asyncOp.execute(
      async () => {
        if (isEditMode.value) {
          return await api.updateItem(itemId.value, form.value)
        } else {
          return await api.createItem(form.value)
        }
      },
      {
        message: isEditMode.value ? 'Actualizando...' : 'Creando...',
        successMessage: isEditMode.value ? 'Actualizado correctamente' : 'Creado correctamente',
        blockUI: true
      }
    )
    
    if (result.success) {
      emit('success', result.data)
    }
  } finally {
    submitting.value = false
  }
}
</script>
```

## 🎯 Beneficios del Sistema

1. **Consistencia**: Todos los loaders tienen el mismo aspecto y comportamiento
2. **Centralización**: Un solo lugar para manejar todos los estados de loading
3. **Automatización**: Loading states se manejan automáticamente
4. **Flexibilidad**: Diferentes tipos de loaders para diferentes contextos
5. **UX Mejorada**: Feedback visual inmediato para todas las operaciones
6. **Debugging**: Tracking centralizado de operaciones activas

## 🚀 Implementación Recomendada

1. **Migrar stores uno por uno** usando `useCrudOperations`
2. **Reemplazar botones normales** con `LoadingButton`
3. **Usar LoadingSpinner** para contenido que se carga
4. **Aprovechar el GlobalLoadingManager** para operaciones críticas
5. **Implementar progreso** en operaciones largas como uploads

Este sistema mejora significativamente la experiencia del usuario al proporcionar feedback visual inmediato para todas las operaciones asíncronas del sistema.