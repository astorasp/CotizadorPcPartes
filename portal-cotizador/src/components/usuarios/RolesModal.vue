<template>
  <Teleport to="body">
    <Transition name="modal" appear>
      <div
        v-if="show"
        class="fixed inset-0 z-50 overflow-y-auto"
        aria-labelledby="modal-title"
        role="dialog"
        aria-modal="true"
      >
        <!-- Overlay -->
        <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
          <div
            class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"
            @click="$emit('close')"
          ></div>

          <!-- Modal Content -->
          <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
          
          <div class="inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-md sm:w-full sm:p-6">
            <!-- Header -->
            <div class="mb-4">
              <h3 class="text-lg leading-6 font-medium text-gray-900" id="modal-title">
                Gestionar Roles
              </h3>
              <p class="mt-1 text-sm text-gray-500">
                Usuario: <span class="font-medium">{{ usuario?.usuario }}</span>
              </p>
            </div>

            <!-- Lista de Roles -->
            <div class="space-y-3">
              <div
                v-for="rol in rolesDisponibles"
                :key="rol.id"
                class="flex items-center justify-between p-3 border border-gray-200 rounded-lg"
                :class="{ 'bg-blue-50 border-blue-200': isRolAsignado(rol.id) }"
              >
                <div class="flex items-center space-x-3">
                  <div
                    class="flex-shrink-0 h-3 w-3 rounded-full"
                    :class="getRolColorClass(rol.nombre)"
                  ></div>
                  <div class="flex-1">
                    <div class="flex items-center space-x-2">
                      <p class="text-sm font-medium text-gray-900">{{ rol.nombre }}</p>
                      <LockClosedIcon 
                        v-if="esRolProtegido(rol)" 
                        class="h-3 w-3 text-amber-500" 
                        title="Rol protegido"
                      />
                    </div>
                    <p class="text-xs text-gray-500">
                      {{ getRolDescription(rol.nombre) }}
                      <span v-if="esRolProtegido(rol)" class="text-amber-600 font-medium"> (Protegido)</span>
                    </p>
                  </div>
                </div>
                
                <button
                  @click="puedeModificarRol(rol) ? toggleRol(rol) : mostrarMensajeProteccion(rol)"
                  :disabled="loading || !puedeModificarRol(rol)"
                  class="flex-shrink-0 relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2"
                  :class="[
                    isRolAsignado(rol.id) 
                      ? (puedeModificarRol(rol) ? 'bg-primary-600' : 'bg-gray-400') 
                      : 'bg-gray-200',
                    !puedeModificarRol(rol) ? 'opacity-60 cursor-not-allowed' : 'disabled:opacity-50'
                  ]"
                  :title="!puedeModificarRol(rol) ? getMensajeProteccionRol(rol) : ''"
                >
                  <span
                    class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform"
                    :class="isRolAsignado(rol.id) ? 'translate-x-6' : 'translate-x-1'"
                  ></span>
                </button>
              </div>
            </div>

            <!-- Estado de cambios -->
            <div v-if="cambiosPendientes.length > 0" class="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded-md">
              <div class="flex">
                <ExclamationTriangleIcon class="h-5 w-5 text-yellow-400" />
                <div class="ml-3">
                  <p class="text-sm text-yellow-800 font-medium">Cambios pendientes:</p>
                  <ul class="mt-1 text-sm text-yellow-700">
                    <li v-for="cambio in cambiosPendientes" :key="cambio.rolId">
                      {{ cambio.accion }} rol {{ cambio.rolNombre }}
                    </li>
                  </ul>
                </div>
              </div>
            </div>

            <!-- Error -->
            <div v-if="error" class="mt-4 p-3 bg-red-50 border border-red-200 rounded-md">
              <div class="flex">
                <XCircleIcon class="h-5 w-5 text-red-400" />
                <div class="ml-3">
                  <p class="text-sm text-red-800">{{ error }}</p>
                </div>
              </div>
            </div>

            <!-- Botones -->
            <div class="flex justify-end space-x-3 pt-6">
              <button
                type="button"
                class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                @click="handleCancel"
                :disabled="loading"
              >
                Cancelar
              </button>
              
              <button
                type="button"
                class="px-4 py-2 bg-primary-600 border border-transparent rounded-md text-sm font-medium text-white hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
                @click="handleSave"
                :disabled="loading || cambiosPendientes.length === 0"
              >
                <span v-if="loading" class="flex items-center">
                  <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Guardando...
                </span>
                <span v-else>
                  Aplicar Cambios ({{ cambiosPendientes.length }})
                </span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import {
  ExclamationTriangleIcon,
  XCircleIcon,
  LockClosedIcon
} from '@heroicons/vue/24/outline'

import usuariosApi from '@/services/usuariosApi'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  usuario: {
    type: Object,
    default: null
  },
  rolesDisponibles: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['close', 'save'])

// Estado reactivo
const loading = ref(false)
const error = ref('')
const rolesUsuario = ref([])
const cambiosPendientes = ref([])

// Computed properties
const getRolDescription = (rolNombre) => {
  const descriptions = {
    'ADMIN': 'Acceso completo al sistema',
    'USER': 'Acceso básico de usuario',
    'SUPERVISOR': 'Supervisión y reportes',
    'GUEST': 'Acceso limitado de invitado'
  }
  return descriptions[rolNombre] || 'Rol del sistema'
}

const getRolColorClass = (rolNombre) => {
  const colorMap = {
    'ADMIN': 'bg-red-500',
    'USER': 'bg-blue-500',
    'SUPERVISOR': 'bg-green-500',
    'GUEST': 'bg-gray-500'
  }
  return colorMap[rolNombre] || 'bg-gray-500'
}

// Methods
const isRolAsignado = (rolId) => {
  return Array.isArray(rolesUsuario.value) && rolesUsuario.value.some(rol => {
    // Check both 'id' and 'rolId' fields to handle API response structure
    const roleIdToCheck = rol.id || rol.rolId
    return roleIdToCheck === rolId || roleIdToCheck == rolId || String(roleIdToCheck) === String(rolId)
  })
}

// Funciones de protección para roles críticos
const esRolProtegido = (rol) => {
  // Proteger el rol ADMIN del usuario admin
  return props.usuario?.usuario === 'admin' && rol.nombre === 'ADMIN'
}

const puedeModificarRol = (rol) => {
  const estaAsignado = isRolAsignado(rol.id)
  
  // Si el rol está asignado y es protegido, no se puede quitar
  if (estaAsignado && esRolProtegido(rol)) {
    return false
  }
  
  return true
}

const getMensajeProteccionRol = (rol) => {
  if (esRolProtegido(rol)) {
    return 'No se puede quitar el rol ADMIN del usuario administrador del sistema'
  }
  return ''
}

const mostrarMensajeProteccion = (rol) => {
  // Mostrar mensaje de error cuando se intenta modificar un rol protegido
  error.value = getMensajeProteccionRol(rol)
  
  // Limpiar el mensaje después de unos segundos
  setTimeout(() => {
    error.value = ''
  }, 4000)
}

const cargarRolesUsuario = async () => {
  if (!props.usuario?.id) return

  try {
    const result = await usuariosApi.obtenerRolesUsuario(props.usuario.id)
    if (result.success) {
      const roles = result.data?.roles || result.data || []
      rolesUsuario.value = Array.isArray(roles) ? roles : []
    } else {
      error.value = 'Error al cargar roles del usuario'
      rolesUsuario.value = []
    }
  } catch (err) {
    error.value = 'Error al cargar roles del usuario'
    rolesUsuario.value = []
    console.error('Error al cargar roles:', err)
  }
}

const toggleRol = (rol) => {
  // Verificar si el rol puede ser modificado
  if (!puedeModificarRol(rol)) {
    mostrarMensajeProteccion(rol)
    return
  }
  
  // Ensure rolesUsuario.value is an array
  if (!Array.isArray(rolesUsuario.value)) {
    rolesUsuario.value = []
  }
  
  const estaAsignado = isRolAsignado(rol.id)
  
  if (estaAsignado) {
    // Quitar rol - check both id and rolId
    rolesUsuario.value = rolesUsuario.value.filter(r => {
      const roleIdToCheck = r.id || r.rolId
      return roleIdToCheck !== rol.id
    })
    agregarCambio('Quitar', rol.id, rol.nombre)
  } else {
    // Agregar rol
    rolesUsuario.value.push(rol)
    agregarCambio('Asignar', rol.id, rol.nombre)
  }
}

const agregarCambio = (accion, rolId, rolNombre) => {
  // Remover cambio previo para este rol si existe
  cambiosPendientes.value = cambiosPendientes.value.filter(c => c.rolId !== rolId)
  
  // Agregar nuevo cambio
  cambiosPendientes.value.push({
    accion,
    rolId,
    rolNombre
  })
}

const handleSave = async () => {
  if (cambiosPendientes.value.length === 0) return

  loading.value = true
  error.value = ''

  try {
    // Procesar cada cambio
    for (const cambio of cambiosPendientes.value) {
      if (cambio.accion === 'Asignar') {
        const result = await usuariosApi.asignarRol(props.usuario.id, cambio.rolId)
        if (!result.success) {
          throw new Error(`Error al asignar rol ${cambio.rolNombre}: ${result.error}`)
        }
      } else if (cambio.accion === 'Quitar') {
        const result = await usuariosApi.revocarRol(props.usuario.id, cambio.rolId)
        if (!result.success) {
          throw new Error(`Error al revocar rol ${cambio.rolNombre}: ${result.error}`)
        }
      }
    }

    // Limpiar cambios pendientes
    cambiosPendientes.value = []
    
    // Emitir evento de guardado exitoso
    emit('save', {
      usuarioId: props.usuario.id,
      roles: rolesUsuario.value
    })

    // Cerrar modal
    emit('close')

  } catch (err) {
    error.value = err.message || 'Error al guardar cambios de roles'
    console.error('Error al guardar roles:', err)
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  // Recargar roles originales
  cambiosPendientes.value = []
  cargarRolesUsuario()
  emit('close')
}

const resetState = () => {
  rolesUsuario.value = []
  cambiosPendientes.value = []
  error.value = ''
  loading.value = false
}

// Watchers
watch(() => props.show, (newValue) => {
  if (newValue && props.usuario) {
    resetState()
    cargarRolesUsuario()
  }
})

// Lifecycle
onMounted(() => {
  if (props.show && props.usuario) {
    cargarRolesUsuario()
  }
})
</script>

<style scoped>
/* Transiciones del modal */
.modal-enter-active, .modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from, .modal-leave-to {
  opacity: 0;
}

/* Animación del toggle */
.toggle-transition {
  transition: transform 0.2s ease-in-out;
}
</style>