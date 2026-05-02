import client from './client'
import type { ActividadGrupoResponse } from '@/types'

const base = (grupoId: number) => `/maestro/grupos/${grupoId}/actividades`

export const getActividades = (grupoId: number) =>
  client.get<ActividadGrupoResponse[]>(base(grupoId)).then(r => r.data)

export const createActividad = (grupoId: number, data: {
  unidadId: number
  actividadCatalogoId: number
  etiqueta?: string
  ponderacion: number
}) => client.post<ActividadGrupoResponse>(base(grupoId), data).then(r => r.data)

export const updateActividad = (grupoId: number, id: number, data: {
  etiqueta?: string
  ponderacion: number
}) => client.put<ActividadGrupoResponse>(`${base(grupoId)}/${id}`, data).then(r => r.data)

export const deleteActividad = (grupoId: number, id: number) => client.delete(`${base(grupoId)}/${id}`)
