import client from './client'
import type { CalificacionFinalDto, InscripcionResponse } from '@/types'

export const getReporte = (grupoId: number) =>
  client.get<CalificacionFinalDto[]>(`/maestro/grupos/${grupoId}/reporte`).then(r => r.data)

export const guardarLote = (grupoId: number, data: {
  grupoId: number; unidadId: number;
  resultados: { inscripcionId: number; actividadGrupoId: number; calificacion: number | null }[]
}) => client.post(`/maestro/grupos/${grupoId}/calificaciones/lote`, data)

export const aplicarOverride = (inscripcionId: number, data: { calificacion: number | null; justificacion: string }) =>
  client.post(`/maestro/inscripciones/${inscripcionId}/override`, data)

// Unidades
export const getEstadoUnidad = (grupoId: number, unidadId: number) =>
  client.get<{ estado: string }>(`/maestro/grupos/${grupoId}/unidades/${unidadId}/estado`).then(r => r.data)
export const cerrarUnidad = (grupoId: number, unidadId: number) =>
  client.post(`/maestro/grupos/${grupoId}/unidades/${unidadId}/cerrar`)
export const abrirUnidad = (grupoId: number, unidadId: number) =>
  client.post(`/maestro/grupos/${grupoId}/unidades/${unidadId}/abrir`)

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

export const descargarBoletaMaestro = async (inscripcionId: number) => {
  const res = await client.get(`/maestro/inscripciones/${inscripcionId}/boleta/pdf`, { responseType: 'blob' })
  const url = URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = url; a.download = `boleta_${inscripcionId}.pdf`; a.click()
  URL.revokeObjectURL(url)
}

export const getMisCursos = () =>
  client.get<InscripcionResponse[]>('/alumno/mis-cursos').then(r => r.data)
