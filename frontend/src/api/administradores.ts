import client from './client'
import type { AdminResponse } from '@/types'

const BASE = '/admin/administradores'

export const getAdmins = () => client.get<AdminResponse[]>(BASE).then(r => r.data)
export const createAdmin = (data: { nombre: string; email: string; numEmpleado: string }) =>
  client.post<AdminResponse>(BASE, data).then(r => r.data)
export const updateAdmin = (id: number, data: { nombre: string; email: string; numEmpleado: string }) =>
  client.put<AdminResponse>(`${BASE}/${id}`, data).then(r => r.data)
export const toggleAdminEstado = (id: number, activo: boolean) =>
  client.patch(`${BASE}/${id}/estado`, null, { params: { activo } })
export const resetAdminPassword = (id: number) => client.post(`${BASE}/${id}/reset-password`)
export const deleteAdmin = (id: number) => client.delete(`${BASE}/${id}`)
