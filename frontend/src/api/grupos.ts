import client from './client'
import type { GrupoResponse } from '@/types'

const BASE = '/admin/grupos'
const MAESTRO_BASE = '/maestro/grupos'

export const getGrupos = () => client.get<GrupoResponse[]>(BASE).then(r => r.data)
export const getGrupo = (id: number) => client.get<GrupoResponse>(`${BASE}/${id}`).then(r => r.data)
export const createGrupo = (data: object) => client.post<GrupoResponse>(BASE, data).then(r => r.data)
export const updateGrupo = (id: number, data: object) => client.put<GrupoResponse>(`${BASE}/${id}`, data).then(r => r.data)
export const toggleGrupoEstado = (id: number, activo: boolean) =>
  client.patch(`${BASE}/${id}/estado`, null, { params: { activo } })
export const cerrarGrupo = (id: number) => client.post(`${BASE}/${id}/cerrar`)
export const reabrirGrupo = (id: number) => client.post(`${BASE}/${id}/reabrir`)
export const cerrarDefinitivamenteGrupo = (id: number) => client.post(`${BASE}/${id}/cerrar-definitivo`)
export const deleteGrupo = (id: number) => client.delete(`${BASE}/${id}`)

// Maestro
export const getMisGrupos = () => client.get<GrupoResponse[]>(`${MAESTRO_BASE}`).then(r => r.data)
export const getMiGrupo = (id: number) => client.get<GrupoResponse>(`${MAESTRO_BASE}/${id}`).then(r => r.data)
export const cerrarGrupoMaestro = (id: number) => client.post(`${MAESTRO_BASE}/${id}/cerrar`)
export const reabrirGrupoMaestro = (id: number) => client.post(`${MAESTRO_BASE}/${id}/reabrir`)
export const cerrarDefinitivamenteMaestro = (id: number) => client.post(`${MAESTRO_BASE}/${id}/cerrar-definitivo`)
