import client from './client'
import type { ConfiguracionResponse } from '@/types'

export const getConfiguracion = () => client.get<ConfiguracionResponse>('/admin/configuracion').then(r => r.data)
export const updateConfiguracion = (data: { minimaAprobatoria: number; maxima: number }) =>
  client.put('/admin/configuracion', data)
