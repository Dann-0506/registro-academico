import client from './client'
import type { MateriaResponse } from '@/types'

const BASE = '/admin/materias'

export const getMaterias = () => client.get<MateriaResponse[]>(BASE).then(r => r.data)
export const getMateria = (id: number) => client.get<MateriaResponse>(`${BASE}/${id}`).then(r => r.data)
export const createMateria = (data: { clave: string; nombre: string; totalUnidades: number; nombresUnidades?: string[] }) =>
  client.post<MateriaResponse>(BASE, data).then(r => r.data)
export const updateMateria = (id: number, data: { nombre: string }) =>
  client.put<MateriaResponse>(`${BASE}/${id}`, data).then(r => r.data)
export const deleteMateria = (id: number) => client.delete(`${BASE}/${id}`)
