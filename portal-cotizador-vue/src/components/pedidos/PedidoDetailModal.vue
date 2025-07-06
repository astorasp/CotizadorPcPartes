<template>
  <div
    v-if="show"
    class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50"
    @click="handleBackdropClick"
  >
    <div class="relative top-8 mx-auto p-5 border w-full max-w-4xl bg-white rounded-lg shadow-lg">
      <!-- Modal Header -->
      <div class="flex items-center justify-between pb-4 border-b border-gray-200">
        <h3 class="text-lg font-semibold text-gray-900">
          {{ pedido ? `Detalles del Pedido #${pedido.numPedido}` : 'Detalles del Pedido' }}
        </h3>
        <button
          @click="$emit('close')"
          class="text-gray-400 hover:text-gray-600 transition-colors"
          :disabled="loading"
        >
          <XMarkIcon class="h-6 w-6" />
        </button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="py-8">
        <div class="text-center">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto mb-4"></div>
          <p class="text-gray-600">Cargando detalles del pedido...</p>
        </div>
      </div>

      <!-- Modal Content -->
      <div v-else-if="pedido" class="py-4">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          <!-- Información del pedido -->
          <div class="space-y-4">
            <div class="bg-blue-50 p-4 rounded-lg">
              <h4 class="font-medium text-blue-900 mb-3 flex items-center">
                <DocumentTextIcon class="h-5 w-5 mr-2" />
                Información del Pedido
              </h4>
              <div class="grid grid-cols-2 gap-3 text-sm">
                <div>
                  <span class="text-blue-700">Número:</span>
                  <div class="font-medium">#{{ pedido.numPedido }}</div>
                </div>
                <div>
                  <span class="text-blue-700">Proveedor:</span>
                  <div class="font-medium">{{ pedido.nombreProveedor }}</div>
                </div>
                <div>
                  <span class="text-blue-700">Clave Proveedor:</span>
                  <div class="font-medium">{{ pedido.cveProveedor }}</div>
                </div>
                <div>
                  <span class="text-blue-700">Nivel de Surtido:</span>
                  <div class="font-medium">{{ pedido.nivelSurtido }}%</div>
                </div>
              </div>
            </div>

            <div class="bg-green-50 p-4 rounded-lg">
              <h4 class="font-medium text-green-900 mb-3 flex items-center">
                <CalendarIcon class="h-5 w-5 mr-2" />
                Fechas
              </h4>
              <div class="grid grid-cols-2 gap-3 text-sm">
                <div>
                  <span class="text-green-700">Emisión:</span>
                  <div class="font-medium">{{ formatDate(pedido.fechaEmision) }}</div>
                </div>
                <div>
                  <span class="text-green-700">Entrega:</span>
                  <div class="font-medium">{{ formatDate(pedido.fechaEntrega) }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Resumen financiero -->
          <div class="space-y-4">
            <div class="bg-yellow-50 p-4 rounded-lg">
              <h4 class="font-medium text-yellow-900 mb-3 flex items-center">
                <CurrencyDollarIcon class="h-5 w-5 mr-2" />
                Resumen Financiero
              </h4>
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-yellow-700">Total de líneas:</span>
                  <span class="font-medium">{{ pedido.totalDetalles || 0 }}</span>
                </div>
                <div class="flex justify-between border-t border-yellow-300 pt-2">
                  <span class="text-yellow-700 font-medium">Total del pedido:</span>
                  <span class="font-bold text-lg text-green-600">{{ formatCurrency(pedido.total) }}</span>
                </div>
              </div>
            </div>

            <!-- Estado del pedido -->
            <div class="bg-gray-50 p-4 rounded-lg">
              <h4 class="font-medium text-gray-900 mb-2">Estado</h4>
              <div class="flex items-center">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                  Generado
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Detalles del pedido -->
        <div class="border-t border-gray-200 pt-4">
          <h4 class="font-medium text-gray-900 mb-4 flex items-center">
            <ListBulletIcon class="h-5 w-5 mr-2" />
            Detalles del Pedido ({{ pedido.totalDetalles || 0 }} líneas)
          </h4>
          
          <div v-if="pedido.detalles && pedido.detalles.length > 0" class="bg-white border border-gray-200 rounded-lg overflow-hidden">
            <div class="max-h-64 overflow-y-auto">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Artículo
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Cantidad
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Precio Unit.
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Total
                    </th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr
                    v-for="(detalle, index) in pedido.detalles"
                    :key="index"
                    class="hover:bg-gray-50"
                  >
                    <td class="px-6 py-4">
                      <div>
                        <div class="text-sm font-medium text-gray-900">{{ detalle.descripcion }}</div>
                        <div class="text-sm text-gray-500">ID: {{ detalle.idArticulo }}</div>
                      </div>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {{ detalle.cantidad }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {{ formatCurrency(detalle.precioUnitario) }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {{ formatCurrency(detalle.totalCotizado) }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          
          <div v-else class="text-center py-8 text-gray-500">
            <ListBulletIcon class="h-12 w-12 mx-auto mb-4 text-gray-300" />
            <p>No hay detalles disponibles para este pedido</p>
          </div>
        </div>
      </div>

      <!-- Estado sin datos -->
      <div v-else class="py-8 text-center text-gray-500">
        <DocumentTextIcon class="h-12 w-12 mx-auto mb-4 text-gray-300" />
        <p>No se pudo cargar la información del pedido</p>
      </div>

      <!-- Modal Footer -->
      <div class="flex items-center justify-end pt-4 border-t border-gray-200">
        <button
          @click="$emit('close')"
          type="button"
          class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
          :disabled="loading"
        >
          Cerrar
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { 
  XMarkIcon, 
  DocumentTextIcon, 
  CalendarIcon, 
  CurrencyDollarIcon, 
  ListBulletIcon 
} from '@heroicons/vue/24/outline'

// Props
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  pedido: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['close'])

// Methods
const handleBackdropClick = (event) => {
  if (event.target === event.currentTarget) {
    emit('close')
  }
}

// Formateo (simple para el modal)
const formatDate = (dateString) => {
  if (!dateString) return '-'
  try {
    return new Date(dateString).toLocaleDateString('es-MX', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  } catch (error) {
    return dateString
  }
}

const formatCurrency = (amount) => {
  if (amount == null || isNaN(amount)) return '$0.00'
  return parseFloat(amount).toLocaleString('es-MX', {
    style: 'currency',
    currency: 'MXN',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}
</script>