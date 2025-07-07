<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header -->
    <div class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center py-6">
          <div class="flex items-center">
            <router-link 
              to="/dashboard" 
              class="mr-4 p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 transition-colors"
            >
              <ArrowLeftIcon class="h-5 w-5" />
            </router-link>
            <div>
              <h1 class="text-2xl font-bold text-gray-900">Gestión de Usuarios</h1>
              <p class="text-sm text-gray-500">Administrar usuarios y roles del sistema</p>
            </div>
          </div>
          <button
            @click="openCreateModal"
            class="bg-primary-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-primary-700 transition-colors flex items-center"
          >
            <PlusIcon class="h-4 w-4 mr-2" />
            Nuevo Usuario
          </button>
        </div>
      </div>
    </div>

    <!-- Estadísticas -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <UsersIcon class="h-8 w-8 text-blue-600" />
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-500">Total Usuarios</p>
              <p class="text-2xl font-bold text-gray-900">{{ estadisticas.totalUsers || 0 }}</p>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <CheckCircleIcon class="h-8 w-8 text-green-600" />
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-500">Usuarios Activos</p>
              <p class="text-2xl font-bold text-gray-900">{{ estadisticas.activeUsers || 0 }}</p>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <XCircleIcon class="h-8 w-8 text-red-600" />
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-500">Usuarios Inactivos</p>
              <p class="text-2xl font-bold text-gray-900">{{ estadisticas.inactiveUsers || 0 }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Contenido Principal -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-6">
      <div class="bg-white shadow rounded-lg">
        <!-- Filtros y Búsqueda -->
        <div class="border-b border-gray-200 px-6 py-4">
          <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-3 sm:space-y-0">
            <div class="flex items-center space-x-4">
              <div class="relative">
                <MagnifyingGlassIcon class="h-4 w-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                <input
                  v-model="filtros.busqueda"
                  type="text"
                  placeholder="Buscar usuarios..."
                  class="pl-10 pr-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-1 focus:ring-primary-500 focus:border-primary-500"
                  @input="debouncedBuscar"
                />
              </div>
              
              <select
                v-model="filtros.estado"
                @change="aplicarFiltros"
                class="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-1 focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="">Todos los estados</option>
                <option value="true">Activos</option>
                <option value="false">Inactivos</option>
              </select>
            </div>
            
            <div class="flex items-center space-x-2">
              <span class="text-sm text-gray-500">{{ usuarios.length }} de {{ paginacion.totalElements }} usuarios</span>
              <button
                @click="cargarUsuarios"
                class="p-2 text-gray-400 hover:text-gray-600 transition-colors"
                :disabled="loading"
              >
                <ArrowPathIcon class="h-4 w-4" :class="{ 'animate-spin': loading }" />
              </button>
            </div>
          </div>
        </div>

        <!-- Tabla de Usuarios -->
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Usuario
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Roles
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Estado
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Fecha Creación
                </th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-if="loading" class="animate-pulse">
                <td colspan="5" class="px-6 py-4 text-center text-gray-500">
                  Cargando usuarios...
                </td>
              </tr>
              
              <tr v-else-if="usuarios.length === 0">
                <td colspan="5" class="px-6 py-8 text-center text-gray-500">
                  No se encontraron usuarios
                </td>
              </tr>
              
              <tr v-else v-for="usuario in usuarios" :key="usuario.id" class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 h-8 w-8">
                      <div class="h-8 w-8 rounded-full bg-primary-100 flex items-center justify-center">
                        <span class="text-sm font-medium text-primary-600">
                          {{ usuario.usuario.charAt(0).toUpperCase() }}
                        </span>
                      </div>
                    </div>
                    <div class="ml-3">
                      <div class="text-sm font-medium text-gray-900">{{ usuario.usuario }}</div>
                      <div class="text-sm text-gray-500">ID: {{ usuario.id }}</div>
                    </div>
                  </div>
                </td>
                
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex flex-wrap gap-1">
                    <span
                      v-for="rol in usuario.roles || []"
                      :key="rol.id"
                      class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium"
                      :class="getRolColorClass(rol.nombre)"
                    >
                      {{ rol.nombre }}
                    </span>
                    <span v-if="!usuario.roles || usuario.roles.length === 0" class="text-xs text-gray-400">
                      Sin roles
                    </span>
                  </div>
                </td>
                
                <td class="px-6 py-4 whitespace-nowrap">
                  <span
                    class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium"
                    :class="usuario.activo 
                      ? 'bg-green-100 text-green-800' 
                      : 'bg-red-100 text-red-800'"
                  >
                    {{ usuario.activo ? 'Activo' : 'Inactivo' }}
                  </span>
                </td>
                
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ formatDate(usuario.fechaCreacion) }}
                </td>
                
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div class="flex items-center justify-end space-x-2">
                    <button
                      @click="openEditModal(usuario)"
                      class="text-primary-600 hover:text-primary-900 p-1 rounded transition-colors"
                      title="Editar usuario"
                    >
                      <PencilIcon class="h-4 w-4" />
                    </button>
                    
                    <button
                      @click="openRolesModal(usuario)"
                      class="text-blue-600 hover:text-blue-900 p-1 rounded transition-colors"
                      title="Gestionar roles"
                    >
                      <CogIcon class="h-4 w-4" />
                    </button>
                    
                    <button
                      @click="confirmarCambioEstado(usuario)"
                      class="p-1 rounded transition-colors"
                      :class="[
                        puedeDesactivarUsuario(usuario) 
                          ? (usuario.activo 
                              ? 'text-red-600 hover:text-red-900' 
                              : 'text-green-600 hover:text-green-900')
                          : 'text-gray-400 cursor-not-allowed'
                      ]"
                      :title="puedeDesactivarUsuario(usuario) 
                        ? (usuario.activo ? 'Desactivar usuario' : 'Activar usuario')
                        : getMensajeProteccion(usuario)"
                      :disabled="!puedeDesactivarUsuario(usuario)"
                    >
                      <component :is="usuario.activo ? XCircleIcon : CheckCircleIcon" class="h-4 w-4" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Paginación -->
        <div v-if="paginacion.totalPages > 1" class="bg-white px-6 py-3 border-t border-gray-200">
          <div class="flex items-center justify-between">
            <div class="text-sm text-gray-500">
              Mostrando {{ (paginacion.currentPage * paginacion.size) + 1 }} - 
              {{ Math.min((paginacion.currentPage + 1) * paginacion.size, paginacion.totalElements) }} 
              de {{ paginacion.totalElements }} usuarios
            </div>
            
            <div class="flex items-center space-x-2">
              <button
                @click="cambiarPagina(paginacion.currentPage - 1)"
                :disabled="paginacion.currentPage === 0"
                class="px-3 py-1 border border-gray-300 rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
              >
                Anterior
              </button>
              
              <span class="px-3 py-1 text-sm">
                Página {{ paginacion.currentPage + 1 }} de {{ paginacion.totalPages }}
              </span>
              
              <button
                @click="cambiarPagina(paginacion.currentPage + 1)"
                :disabled="paginacion.currentPage >= paginacion.totalPages - 1"
                class="px-3 py-1 border border-gray-300 rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
              >
                Siguiente
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modales -->
    <UsuarioModal
      v-if="showUsuarioModal"
      :show="showUsuarioModal"
      :usuario="usuarioSeleccionado"
      :roles-disponibles="rolesDisponibles"
      @close="closeUsuarioModal"
      @save="handleSaveUsuario"
    />

    <RolesModal
      v-if="showRolesModal"
      :show="showRolesModal"
      :usuario="usuarioSeleccionado"
      :roles-disponibles="rolesDisponibles"
      @close="closeRolesModal"
      @save="handleSaveRoles"
    />

    <ConfirmModal
      v-if="showConfirmModal"
      :show="showConfirmModal"
      :title="confirmModalData.title"
      :message="confirmModalData.message"
      :type="confirmModalData.type"
      @confirm="handleConfirm"
      @cancel="closeConfirmModal"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeftIcon,
  PlusIcon,
  MagnifyingGlassIcon,
  ArrowPathIcon,
  PencilIcon,
  CogIcon,
  XCircleIcon,
  CheckCircleIcon,
  UsersIcon
} from '@heroicons/vue/24/outline'

import usuariosApi from '@/services/usuariosApi'
import { useAuthStore } from '@/stores/useAuthStore'
import { formatDate } from '@/utils/dateUtils'
import { debounce } from '@/utils/helpers'

// Componentes modales (los crearemos después)
import UsuarioModal from '@/components/usuarios/UsuarioModal.vue'
import RolesModal from '@/components/usuarios/RolesModal.vue'
import ConfirmModal from '@/components/ui/ConfirmModal.vue'

// Router y stores
const router = useRouter()
const authStore = useAuthStore()

// Estado reactivo
const loading = ref(false)
const usuarios = ref([])
const rolesDisponibles = ref([])
const estadisticas = ref({})

const paginacion = reactive({
  currentPage: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0
})

const filtros = reactive({
  busqueda: '',
  estado: ''
})

// Modales
const showUsuarioModal = ref(false)
const showRolesModal = ref(false)
const showConfirmModal = ref(false)
const usuarioSeleccionado = ref(null)
const confirmModalData = ref({})

// Computed
const debouncedBuscar = debounce(() => {
  aplicarFiltros()
}, 300)

// Métodos principales
const cargarUsuarios = async () => {
  loading.value = true
  try {
    const params = {
      page: paginacion.currentPage,
      size: paginacion.size,
      sort: 'id',
      direction: 'ASC'
    }

    const result = await usuariosApi.obtenerUsuarios(params)
    if (result.success) {
      usuarios.value = result.data.usuarios || []
      paginacion.totalElements = result.data.totalItems || 0
      paginacion.totalPages = result.data.totalPages || 0
    } else {
      console.error('Error al cargar usuarios:', result.error)
    }
  } catch (error) {
    console.error('Error al cargar usuarios:', error)
  } finally {
    loading.value = false
  }
}

const cargarRoles = async () => {
  try {
    const result = await usuariosApi.obtenerRoles()
    if (result.success) {
      rolesDisponibles.value = result.data || []
    }
  } catch (error) {
    console.error('Error al cargar roles:', error)
  }
}

const cargarEstadisticas = async () => {
  try {
    const result = await usuariosApi.obtenerEstadisticas()
    if (result.success) {
      estadisticas.value = result.data || {}
    }
  } catch (error) {
    console.error('Error al cargar estadísticas:', error)
  }
}

// Gestión de modales
const openCreateModal = () => {
  usuarioSeleccionado.value = null
  showUsuarioModal.value = true
}

const openEditModal = (usuario) => {
  // Verificar si el usuario puede ser editado
  if (usuario.usuario === 'admin' && authStore.user?.usuario !== 'admin') {
    // Solo el propio admin puede editar la cuenta admin
    confirmModalData.value = {
      title: 'Acción no permitida',
      message: 'Solo el usuario administrador puede editar su propia cuenta',
      type: 'info',
      action: 'informacion',
      data: usuario
    }
    showConfirmModal.value = true
    return
  }
  
  usuarioSeleccionado.value = { ...usuario }
  showUsuarioModal.value = true
}

const openRolesModal = (usuario) => {
  // Verificar si se pueden gestionar los roles de este usuario
  if (usuario.usuario === 'admin' && authStore.user?.usuario !== 'admin') {
    // Solo el propio admin puede gestionar roles del admin
    confirmModalData.value = {
      title: 'Acción no permitida',
      message: 'Solo el usuario administrador puede gestionar sus propios roles',
      type: 'info',
      action: 'informacion',
      data: usuario
    }
    showConfirmModal.value = true
    return
  }
  
  usuarioSeleccionado.value = { ...usuario }
  showRolesModal.value = true
}

const closeUsuarioModal = () => {
  showUsuarioModal.value = false
  usuarioSeleccionado.value = null
}

const closeRolesModal = () => {
  showRolesModal.value = false
  usuarioSeleccionado.value = null
}

const closeConfirmModal = () => {
  showConfirmModal.value = false
  confirmModalData.value = {}
}

// Acciones
const handleSaveUsuario = async (usuarioData) => {
  try {
    let result
    if (usuarioData.id) {
      // Actualizar
      result = await usuariosApi.actualizarUsuario(usuarioData.id, usuarioData)
    } else {
      // Crear
      result = await usuariosApi.crearUsuario(usuarioData)
      
      // Si se creó exitosamente y se seleccionaron roles, asignarlos
      if (result.success && usuarioData.roles && usuarioData.roles.length > 0) {
        const usuarioId = result.data.id || result.data.usuario?.id
        if (usuarioId) {
          // Asignar cada rol seleccionado
          for (const rolId of usuarioData.roles) {
            try {
              await usuariosApi.asignarRol(usuarioId, rolId)
            } catch (error) {
              console.error(`Error asignando rol ${rolId}:`, error)
            }
          }
        }
      }
    }

    if (result.success) {
      closeUsuarioModal()
      await cargarUsuarios()
      await cargarEstadisticas()
    } else {
      console.error('Error al guardar usuario:', result.error)
    }
  } catch (error) {
    console.error('Error al guardar usuario:', error)
  }
}

const handleSaveRoles = async (rolesData) => {
  try {
    // Implementar lógica de guardar roles
    closeRolesModal()
    await cargarUsuarios()
  } catch (error) {
    console.error('Error al guardar roles:', error)
  }
}

const confirmarCambioEstado = (usuario) => {
  // Verificar si el usuario puede ser desactivado
  if (!puedeDesactivarUsuario(usuario)) {
    // Mostrar mensaje informativo en lugar de confirmación
    confirmModalData.value = {
      title: 'Acción no permitida',
      message: getMensajeProteccion(usuario),
      type: 'info',
      action: 'informacion',
      data: usuario
    }
    showConfirmModal.value = true
    return
  }
  
  const accion = usuario.activo ? 'desactivar' : 'activar'
  confirmModalData.value = {
    title: `${accion.charAt(0).toUpperCase() + accion.slice(1)} Usuario`,
    message: `¿Está seguro que desea ${accion} al usuario "${usuario.usuario}"?`,
    type: usuario.activo ? 'danger' : 'warning',
    action: 'cambiar-estado',
    data: usuario
  }
  showConfirmModal.value = true
}

const handleConfirm = async () => {
  const { action, data } = confirmModalData.value
  
  if (action === 'informacion') {
    // Solo cerrar el modal para mensajes informativos
    closeConfirmModal()
    return
  }
  
  if (action === 'cambiar-estado') {
    try {
      if (data.activo) {
        // Desactivar
        const result = await usuariosApi.desactivarUsuario(data.id)
        if (result.success) {
          await cargarUsuarios()
          await cargarEstadisticas()
        }
      } else {
        // Activar (implementar endpoint si es necesario)
        console.log('Activar usuario no implementado')
      }
    } catch (error) {
      console.error('Error al cambiar estado:', error)
    }
  }
  
  closeConfirmModal()
}

// Filtros y paginación
const aplicarFiltros = () => {
  paginacion.currentPage = 0
  cargarUsuarios()
}

const cambiarPagina = (nuevaPagina) => {
  if (nuevaPagina >= 0 && nuevaPagina < paginacion.totalPages) {
    paginacion.currentPage = nuevaPagina
    cargarUsuarios()
  }
}

// Utilidades
const getRolColorClass = (rolNombre) => {
  const colorMap = {
    'ADMIN': 'bg-red-100 text-red-800',
    'USER': 'bg-blue-100 text-blue-800',
    'SUPERVISOR': 'bg-green-100 text-green-800',
    'GUEST': 'bg-gray-100 text-gray-800'
  }
  return colorMap[rolNombre] || 'bg-gray-100 text-gray-800'
}

// Funciones de protección
const esUsuarioProtegido = (usuario) => {
  // Proteger usuario admin y usuario actual
  return usuario.usuario === 'admin' || usuario.usuario === authStore.user?.usuario
}

const puedeDesactivarUsuario = (usuario) => {
  if (!usuario.activo) return true // Siempre se puede activar
  return !esUsuarioProtegido(usuario) // Solo se puede desactivar si no está protegido
}

const getMensajeProteccion = (usuario) => {
  if (usuario.usuario === 'admin') {
    return 'No se puede desactivar el usuario administrador del sistema'
  }
  if (usuario.usuario === authStore.user?.usuario) {
    return 'No puedes desactivar tu propia cuenta'
  }
  return ''
}

// Lifecycle
onMounted(() => {
  // Verificar permisos
  if (!authStore.hasRole('ADMIN')) {
    router.push('/dashboard')
    return
  }
  
  cargarUsuarios()
  cargarRoles()
  cargarEstadisticas()
})
</script>