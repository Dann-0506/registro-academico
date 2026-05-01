import client from './client'
import type { InscripcionResponse } from '@/types'

const BASE = '/admin/inscripciones'

export const getInscripciones = () => client.get<InscripcionResponse[]>(BASE).then(r => r.data)
export const getInscripcionesByGrupo = (grupoId: number) =>
  client.get<InscripcionResponse[]>(`${BASE}/grupo/${grupoId}`).then(r => r.data)
export const createInscripcion = (data: { alumnoId: number; grupoId: number; fecha?: string }) =>
  client.post<InscripcionResponse>(BASE, data).then(r => r.data)
export const deleteInscripcion = (id: number) => client.delete(`${BASE}/${id}`)

// Alumno
export const getMisInscripciones = () => client.get<InscripcionResponse[]>('/alumno/mis-inscripciones').then(r => r.data)
