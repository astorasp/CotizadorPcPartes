<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header/Navbar -->
    <nav class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <h1 class="text-xl font-bold text-primary-600">Portal Cotizador Vue</h1>
            </div>
          </div>
          
          <div class="flex items-center space-x-4">
            <span class="text-sm text-gray-700">
              Bienvenido, <span class="font-medium">{{ authStore.userName }}</span>
            </span>
            <button
              @click="handleLogout"
              class="bg-red-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-red-700 transition-colors"
            >
              Cerrar Sesión
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <!-- Page Header -->
      <div class="px-4 py-6 sm:px-0">
        <h1 class="text-3xl font-bold text-gray-900 mb-2">Dashboard</h1>
        <p class="text-gray-600">Gestión y administración del sistema de cotizaciones</p>
      </div>

      <!-- Dashboard Cards -->
      <div class="px-4 sm:px-0">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-7 gap-6 mb-8 pr-8">
          <!-- Componentes Card -->
          <div class="bg-white overflow-hidden shadow-sm rounded-lg hover:shadow-md transition-shadow cursor-pointer" @click="navigateTo('/componentes')">
            <div class="p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <CpuChipIcon class="h-8 w-8 text-indigo-600" />
                </div>
                <div class="ml-3 min-w-0 flex-1">
                  <h3 class="text-base font-medium text-gray-900 truncate">Componentes</h3>
                  <p class="text-xs text-gray-500 truncate">Gestión de componentes</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Armado PCs Card -->
          <div class="bg-white overflow-hidden shadow-sm rounded-lg hover:shadow-md transition-shadow cursor-pointer" @click="navigateTo('/pcs')">
            <div class="p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <ComputerDesktopIcon class="h-8 w-8 text-gray-600" />
                </div>
                <div class="ml-3 min-w-0 flex-1">
                  <h3 class="text-base font-medium text-gray-900 truncate">Armado PCs</h3>
                  <p class="text-xs text-gray-500 truncate">Armado de PCs</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Cotizaciones Card -->
          <div class="bg-white overflow-hidden shadow-sm rounded-lg hover:shadow-md transition-shadow cursor-pointer" @click="navigateTo('/cotizaciones')">
            <div class="p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <CalculatorIcon class="h-8 w-8 text-orange-600" />
                </div>
                <div class="ml-3 min-w-0 flex-1">
                  <h3 class="text-base font-medium text-gray-900 truncate">Cotizaciones</h3>
                  <p class="text-xs text-gray-500 truncate">Gestión de cotizaciones</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Proveedores Card -->
          <div class="bg-white overflow-hidden shadow-sm rounded-lg hover:shadow-md transition-shadow cursor-pointer" @click="navigateTo('/proveedores')">
            <div class="p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <BuildingOfficeIcon class="h-8 w-8 text-blue-600" />
                </div>
                <div class="ml-3 min-w-0 flex-1">
                  <h3 class="text-base font-medium text-gray-900 truncate">Proveedores</h3>
                  <p class="text-xs text-gray-500 truncate">Gestión de proveedores</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Pedidos Card -->
          <div class="bg-white overflow-hidden shadow-sm rounded-lg hover:shadow-md transition-shadow cursor-pointer" @click="navigateTo('/pedidos')">
            <div class="p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <DocumentTextIcon class="h-8 w-8 text-green-600" />
                </div>
                <div class="ml-3 min-w-0 flex-1">
                  <h3 class="text-base font-medium text-gray-900 truncate">Pedidos</h3>
                  <p class="text-xs text-gray-500 truncate">Gestión de pedidos</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Promociones Card -->
          <div class="bg-white overflow-hidden shadow-sm rounded-lg hover:shadow-md transition-shadow cursor-pointer" @click="navigateTo('/promociones')">
            <div class="p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <TagIcon class="h-8 w-8 text-purple-600" />
                </div>
                <div class="ml-3 min-w-0 flex-1">
                  <h3 class="text-base font-medium text-gray-900 truncate">Promociones</h3>
                  <p class="text-xs text-gray-500 truncate">Gestión de promociones</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Usuarios Card - NUEVA SECCIÓN -->
          <div 
            v-if="authStore.hasRole('ADMIN')"
            class="bg-white overflow-hidden shadow-sm rounded-lg hover:shadow-md transition-shadow cursor-pointer" 
            @click="navigateTo('/usuarios')"
          >
            <div class="p-4">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <UsersIcon class="h-8 w-8 text-teal-600" />
                </div>
                <div class="ml-3 min-w-0 flex-1">
                  <h3 class="text-base font-medium text-gray-900 truncate">Usuarios</h3>
                  <p class="text-xs text-gray-500 truncate">Gestión de usuarios</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Quick Actions -->
        <div class="bg-white shadow-sm rounded-lg">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">Acciones Rápidas</h3>
          </div>
          <div class="p-6">
            <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
              <button
                @click="navigateTo('/proveedores')"
                class="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
              >
                <PlusIcon class="h-5 w-5 text-blue-600 mr-3" />
                <span class="text-sm font-medium text-gray-900">Nuevo Proveedor</span>
              </button>
              
              <button
                @click="navigateTo('/pedidos')"
                class="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
              >
                <PlusIcon class="h-5 w-5 text-green-600 mr-3" />
                <span class="text-sm font-medium text-gray-900">Nuevo Pedido</span>
              </button>
              
              <button
                @click="navigateTo('/promociones')"
                class="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
              >
                <PlusIcon class="h-5 w-5 text-purple-600 mr-3" />
                <span class="text-sm font-medium text-gray-900">Nueva Promoción</span>
              </button>

              <!-- Nueva acción para usuarios - Solo visible para ADMIN -->
              <button
                v-if="authStore.hasRole('ADMIN')"
                @click="navigateTo('/usuarios')"
                class="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
              >
                <PlusIcon class="h-5 w-5 text-teal-600 mr-3" />
                <span class="text-sm font-medium text-gray-900">Nuevo Usuario</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Welcome Message -->
        <div class="mt-8 bg-primary-50 border border-primary-200 rounded-lg p-6">
          <div class="flex items-start">
            <div class="flex-shrink-0">
              <InformationCircleIcon class="h-6 w-6 text-primary-600" />
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-primary-800">
                ¡Bienvenido al Portal Cotizador Vue!
              </h3>
              <div class="mt-2 text-sm text-primary-700">
                <p>
                  Sistema integral para la gestión de cotizaciones de componentes de PC. 
                  Utilice el menú de navegación o las tarjetas de acceso rápido para comenzar.
                </p>
                <p v-if="authStore.hasRole('ADMIN')" class="mt-2 font-medium">
                  Como administrador, tienes acceso completo a la gestión de usuarios y configuración del sistema.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'
import {
  BuildingOfficeIcon,
  DocumentTextIcon,
  TagIcon,
  CalculatorIcon,
  PlusIcon,
  InformationCircleIcon,
  CpuChipIcon,
  ComputerDesktopIcon,
  UsersIcon
} from '@heroicons/vue/24/outline'

// Router and stores
const router = useRouter()
const authStore = useAuthStore()

// Methods
const navigateTo = (path) => {
  router.push(path)
}

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>