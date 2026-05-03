import client from './client'
import type { ConfiguracionResponse, DashboardResponse } from '@/types'

export const getConfiguracion = () => client.get<ConfiguracionResponse>('/admin/configuracion').then(r => r.data)
export const updateConfiguracion = (data: { minimaAprobatoria: number; maxima: number; semestreActivo: string }) =>
  client.put('/admin/configuracion', data)

export const getDashboard = () => client.get<DashboardResponse>('/admin/dashboard').then(r => r.data)
