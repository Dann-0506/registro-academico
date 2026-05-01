import client from './client'
import type { KpiResponse, RendimientoResponse } from '@/types'

export const getKpis = () => client.get<KpiResponse>('/admin/analisis/kpis').then(r => r.data)
export const getRendimiento = () => client.get<RendimientoResponse[]>('/admin/analisis/rendimiento').then(r => r.data)
