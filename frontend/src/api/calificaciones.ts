import client from './client'
import type { CalificacionFinalDto } from '@/types'

export const getReporte = (grupoId: number) =>
  client.get<CalificacionFinalDto[]>(`/maestro/grupos/${grupoId}/reporte`).then(r => r.data)

export const guardarLote = (grupoId: number, data: {
  grupoId: number; unidadId: number;
  resultados: { inscripcionId: number; actividadGrupoId: number; calificacion: number | null }[]
}) => client.post(`/maestro/grupos/${grupoId}/calificaciones/lote`, data)

export const aplicarOverride = (inscripcionId: number, data: { calificacion: number | null; justificacion: string }) =>
  client.post(`/maestro/inscripciones/${inscripcionId}/override`, data)

// Alumno
export const getMisCalificaciones = (grupoId: number) =>
  client.get<CalificacionFinalDto>(`/alumno/cursos/${grupoId}/calificaciones`).then(r => r.data)

// PDF
export const descargarActaPdf = async (grupoId: number) => {
  const res = await client.get(`/maestro/grupos/${grupoId}/reporte/pdf`, { responseType: 'blob' })
  const url = URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = url; a.download = `acta_grupo_${grupoId}.pdf`; a.click()
  URL.revokeObjectURL(url)
}

export const descargarBoletaAlumno = async (grupoId: number) => {
  const res = await client.get(`/alumno/cursos/${grupoId}/boleta/pdf`, { responseType: 'blob' })
  const url = URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = url; a.download = `boleta_${grupoId}.pdf`; a.click()
  URL.revokeObjectURL(url)
}

