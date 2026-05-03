import client from './client'
import type { ReportesResponse } from '@/types'

export const getSemestresDisponibles = () =>
  client.get<string[]>('/admin/reportes/semestres').then(r => r.data)

export const getReportes = (semestre?: string) =>
  client.get<ReportesResponse>('/admin/reportes', { params: semestre ? { semestre } : {} }).then(r => r.data)
