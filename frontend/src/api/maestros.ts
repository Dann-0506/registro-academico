import client from './client'
import type { MaestroResponse } from '@/types'

const BASE = '/admin/maestros'

export const getMaestros = () => client.get<MaestroResponse[]>(BASE).then(r => r.data)
export const createMaestro = (data: { nombre: string; email?: string; numEmpleado: string }) =>
  client.post<MaestroResponse>(BASE, data).then(r => r.data)
export const updateMaestro = (id: number, data: { nombre: string; email?: string; numEmpleado: string }) =>
  client.put<MaestroResponse>(`${BASE}/${id}`, data).then(r => r.data)
export const toggleMaestroEstado = (id: number, activo: boolean) =>
  client.patch(`${BASE}/${id}/estado`, null, { params: { activo } })
export const resetMaestroPassword = (id: number) => client.post(`${BASE}/${id}/reset-password`)
export const deleteMaestro = (id: number) => client.delete(`${BASE}/${id}`)
