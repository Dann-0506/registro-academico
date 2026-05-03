import client from './client'
import type { BonusResponse } from '@/types'

const base = (inscripcionId: number) => `/maestro/inscripciones/${inscripcionId}/bonus`

export const getBonus = (inscripcionId: number) =>
  client.get<BonusResponse[]>(base(inscripcionId)).then(r => r.data)

export const createBonus = (inscripcionId: number, data: {
  inscripcionId: number; unidadId?: number; tipo: string; puntos: number; justificacion: string
}) => client.post<BonusResponse>(base(inscripcionId), data).then(r => r.data)
