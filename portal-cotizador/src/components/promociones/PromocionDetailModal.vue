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
          {{ promocion ? `Detalles de "${promocion.nombre}"` : 'Detalles de la Promoción' }}
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
          <p class="text-gray-600">Cargando detalles de la promoción...</p>
        </div>
      </div>

      <!-- Modal Content -->
      <div v-else-if="promocion" class="py-4">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          <!-- Información básica -->
          <div class="space-y-4">
            <div class="bg-blue-50 p-4 rounded-lg">
              <h4 class="font-medium text-blue-900 mb-3 flex items-center">
                <InformationCircleIcon class="h-5 w-5 mr-2" />
                Información Básica
              </h4>
              <div class="space-y-3 text-sm">
                <div>
                  <span class="text-blue-700 font-medium">ID:</span>
                  <div class="text-gray-900">#{{ promocion.idPromocion }}</div>
                </div>
                <div>
                  <span class="text-blue-700 font-medium">Nombre:</span>
                  <div class="text-gray-900 font-medium">{{ promocion.nombre }}</div>
                </div>
                <div>
                  <span class="text-blue-700 font-medium">Descripción:</span>
                  <div class="text-gray-900">{{ promocion.descripcion }}</div>
                </div>
              </div>
            </div>

            <div class="bg-purple-50 p-4 rounded-lg">
              <h4 class="font-medium text-purple-900 mb-3 flex items-center">
                <TagIcon class="h-5 w-5 mr-2" />
                Tipo de Promoción
              </h4>
              <div class="text-sm">
                <div class="flex items-center">
                  <span class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-purple-100 text-purple-800">
                    {{ getTipoDisplayName(promocion.tipoPromocionPrincipal) }}
                  </span>
                </div>
                <div v-if="promocion.detalles && promocion.detalles.length > 0" class="mt-3">
                  <div v-for="(detalle, index) in promocion.detalles" :key="index" class="mb-2">
                    <div class="text-purple-700 font-medium">{{ detalle.nombre }}</div>
                    <div class="text-gray-600 text-xs">
                      {{ getDetalleDescription(detalle) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Vigencia y estado -->
          <div class="space-y-4">
            <div class="bg-green-50 p-4 rounded-lg">
              <h4 class="font-medium text-green-900 mb-3 flex items-center">
                <CalendarIcon class="h-5 w-5 mr-2" />
                Vigencia
              </h4>
              <div class="grid grid-cols-2 gap-3 text-sm">
                <div>
                  <span class="text-green-700 font-medium">Inicio:</span>
                  <div class="text-gray-900">{{ formatDate(promocion.vigenciaDesde) }}</div>
                </div>
                <div>
                  <span class="text-green-700 font-medium">Fin:</span>
                  <div class="text-gray-900">{{ formatDate(promocion.vigenciaHasta) }}</div>
                </div>
              </div>
              <div v-if="promocion.diasRestantes !== undefined" class="mt-3 text-sm">
                <span class="text-green-700 font-medium">Días restantes:</span>
                <div class="text-gray-900">
                  {{ promocion.diasRestantes > 0 ? `${promocion.diasRestantes} días` : 'Expirada' }}
                </div>
              </div>
            </div>

            <div class="bg-yellow-50 p-4 rounded-lg">
              <h4 class="font-medium text-yellow-900 mb-3 flex items-center">
                <ClockIcon class="h-5 w-5 mr-2" />
                Estado Actual
              </h4>
              <div class="flex items-center justify-between">
                <span 
                  class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium"
                  :class="getEstadoBadgeClass(promocion.estadoVigencia)"
                >
                  {{ getEstadoText(promocion.estadoVigencia) }}
                </span>
                <div v-if="promocion.estadoVigencia === 'VIGENTE'" class="text-green-600 text-sm">
                  <CheckCircleIcon class="h-5 w-5 inline mr-1" />
                  Activa
                </div>
                <div v-else-if="promocion.estadoVigencia === 'EXPIRADA'" class="text-red-600 text-sm">
                  <XCircleIcon class="h-5 w-5 inline mr-1" />
                  Vencida
                </div>
                <div v-else-if="promocion.estadoVigencia === 'FUTURA'" class="text-blue-600 text-sm">
                  <ClockIcon class="h-5 w-5 inline mr-1" />
                  Programada
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Detalles técnicos -->
        <div v-if="promocion.detalles && promocion.detalles.length > 0" class="border-t border-gray-200 pt-4">
          <h4 class="font-medium text-gray-900 mb-4 flex items-center">
            <CogIcon class="h-5 w-5 mr-2" />
            Configuración Técnica
          </h4>
          
          <div class="bg-gray-50 rounded-lg overflow-hidden">
            <div class="max-h-64 overflow-y-auto">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-100">
                  <tr>
                    <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Detalle
                    </th>
                    <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Tipo
                    </th>
                    <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Configuración
                    </th>
                    <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Base
                    </th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr
                    v-for="(detalle, index) in promocion.detalles"
                    :key="index"
                    class="hover:bg-gray-50"
                  >
                    <td class="px-4 py-3">
                      <div class="text-sm font-medium text-gray-900">{{ detalle.nombre }}</div>
                    </td>
                    <td class="px-4 py-3 text-sm text-gray-900">
                      <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800">
                        {{ detalle.tipoBase || detalle.tipoAcumulable || 'N/A' }}
                      </span>
                    </td>
                    <td class="px-4 py-3 text-sm text-gray-900">
                      {{ getDetalleConfiguracion(detalle) }}
                    </td>
                    <td class="px-4 py-3 text-sm text-gray-900">
                      <span v-if="detalle.esBase" class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800">
                        Base
                      </span>
                      <span v-else class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800">
                        Adicional
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Resumen de efectividad (si está disponible) -->
        <div v-if="promocion.totalDetalles" class="border-t border-gray-200 pt-4 mt-4">
          <div class="bg-gradient-to-r from-blue-50 to-purple-50 p-4 rounded-lg">
            <h4 class="font-medium text-gray-900 mb-2 flex items-center">
              <ChartBarIcon class="h-5 w-5 mr-2" />
              Resumen
            </h4>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
              <div class="text-center">
                <div class="text-2xl font-bold text-blue-600">{{ promocion.totalDetalles }}</div>
                <div class="text-gray-600">Configuraciones</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-purple-600">{{ promocion.idPromocion }}</div>
                <div class="text-gray-600">ID Promoción</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Estado sin datos -->
      <div v-else class="py-8 text-center text-gray-500">
        <TagIcon class="h-12 w-12 mx-auto mb-4 text-gray-300" />
        <p>No se pudo cargar la información de la promoción</p>
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
  InformationCircleIcon, 
  TagIcon, 
  CalendarIcon, 
  ClockIcon, 
  CogIcon, 
  ChartBarIcon,
  CheckCircleIcon,
  XCircleIcon
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
  promocion: {
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

// Formateo (duplicado para evitar dependencias circulares)
const formatDate = (dateString) => {
  if (!dateString) return '-'
  try {
    return new Date(dateString).toLocaleDateString('es-MX', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  } catch (error) {
    return dateString
  }
}

const getTipoDisplayName = (tipo) => {
  const tipos = {
    'DESCUENTO_PLANO': 'Descuento Plano',
    'DESCUENTO_PORCENTUAL': 'Descuento Porcentual',
    'POR_CANTIDAD': 'Por Cantidad',
    'NXM': 'N x M',
    'DESCUENTO_ESCALONADO': 'Descuento Escalonado'
  }
  return tipos[tipo] || tipo
}

const getEstadoText = (estado) => {
  const estados = {
    'VIGENTE': 'Vigente',
    'EXPIRADA': 'Expirada', 
    'FUTURA': 'Futura'
  }
  return estados[estado] || 'Sin estado'
}

const getEstadoBadgeClass = (estado) => {
  const badges = {
    'VIGENTE': 'bg-green-100 text-green-800',
    'EXPIRADA': 'bg-red-100 text-red-800',
    'FUTURA': 'bg-blue-100 text-blue-800'
  }
  return badges[estado] || 'bg-gray-100 text-gray-800'
}

const getDetalleDescription = (detalle) => {
  if (detalle.tipoBase === 'NXM' && detalle.parametrosNxM) {
    const { llevent, paguen } = detalle.parametrosNxM
    const descuento = llevent && paguen ? ((llevent - paguen) / llevent * 100).toFixed(1) : 0
    return `Compra ${llevent}, Paga ${paguen} (${descuento}% desc.)`
  }
  
  if (detalle.tipoAcumulable === 'DESCUENTO_PLANO' && detalle.porcentajeDescuentoPlano) {
    if (detalle.porcentajeDescuentoPlano < 1) {
      return `$${detalle.porcentajeDescuentoPlano.toFixed(2)} de descuento`
    } else {
      return `${detalle.porcentajeDescuentoPlano}% de descuento`
    }
  }
  
  if (detalle.tipoBase === 'DESCUENTO_ESCALONADO' && detalle.escalasDescuento) {
    const escala = detalle.escalasDescuento[0]
    return `${escala.descuento}% a partir de ${escala.cantidad} unidades`
  }
  
  return 'Configuración estándar'
}

const getDetalleConfiguracion = (detalle) => {
  if (detalle.tipoBase === 'NXM' && detalle.parametrosNxM) {
    return `${detalle.parametrosNxM.llevent} x ${detalle.parametrosNxM.paguen}`
  }
  
  if (detalle.tipoAcumulable === 'DESCUENTO_PLANO' && detalle.porcentajeDescuentoPlano) {
    if (detalle.porcentajeDescuentoPlano < 1) {
      return `$${detalle.porcentajeDescuentoPlano.toFixed(2)}`
    } else {
      return `${detalle.porcentajeDescuentoPlano}%`
    }
  }
  
  if (detalle.tipoBase === 'DESCUENTO_ESCALONADO' && detalle.escalasDescuento) {
    const escala = detalle.escalasDescuento[0]
    return `${escala.cantidad}+ → ${escala.descuento}%`
  }
  
  return '-'
}
</script>