import client from './client'
import type { CalificacionFinalDto } from '@/types'

export const getReporte = (grupoId: number) =>
  client.get<CalificacionFinalDto[]>(`/maestro/grupos/${grupoId}/reporte`).then(r => r.data)

export const guardarCalificacion = (grupoId: number, unidadId: number, inscripcionId: number, actividadGrupoId: number, calificacion: number | null) =>
  client.post(`/maestro/grupos/${grupoId}/calificaciones/lote`, {
    grupoId, unidadId, resultados: [{ inscripcionId, actividadGrupoId, calificacion }],
  })

export const aplicarOverride = (inscripcionId: number, data: { calificacion: number | null; justificacion: string }) =>
  client.post(`/maestro/inscripciones/${inscripcionId}/override`, data)

// Unidades
export const getEstadosUnidades = (grupoId: number) =>
  client.get<Record<number, string>>(`/maestro/grupos/${grupoId}/unidades/estados`).then(r => r.data)
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

