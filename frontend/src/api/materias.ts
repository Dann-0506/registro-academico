import client from './client'
import type { MateriaResponse, UnidadDto } from '@/types'

const BASE = '/admin/materias'

export const getMaterias = () => client.get<MateriaResponse[]>(BASE).then(r => r.data)
export const createMateria = (data: { clave: string; nombre: string; totalUnidades: number; nombresUnidades?: string[] }) =>
  client.post<MateriaResponse>(BASE, data).then(r => r.data)
export const updateMateria = (id: number, data: { nombre: string }) =>
  client.put<MateriaResponse>(`${BASE}/${id}`, data).then(r => r.data)
export const deleteMateria = (id: number) => client.delete(`${BASE}/${id}`)

// Endpoint accesible por MAESTRO (no requiere rol admin)
export const getUnidadesByGrupo = (grupoId: number) =>
  client.get<UnidadDto[]>(`/maestro/grupos/${grupoId}/unidades`).then(r => r.data)
