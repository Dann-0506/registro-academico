import client from './client'
import type { ActividadCatalogoResponse } from '@/types'

const ADMIN = '/admin/actividades-catalogo'
const MAESTRO = '/maestro/actividades-catalogo'

export const getCatalogo = () => client.get<ActividadCatalogoResponse[]>(ADMIN).then(r => r.data)
export const getCatalogoActivo = () => client.get<ActividadCatalogoResponse[]>(MAESTRO).then(r => r.data)

export const createCatalogo = (data: { nombre: string; descripcion?: string }) =>
  client.post<ActividadCatalogoResponse>(ADMIN, data).then(r => r.data)

export const updateCatalogo = (id: number, data: { nombre: string; descripcion?: string }) =>
  client.put<ActividadCatalogoResponse>(`${ADMIN}/${id}`, data).then(r => r.data)

export const toggleCatalogo = (id: number, activo: boolean) =>
  client.patch(`${ADMIN}/${id}/estado`, null, { params: { activo } })

export const deleteCatalogo = (id: number) => client.delete(`${ADMIN}/${id}`)
