import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatCalificacion(value: number | null | undefined): string {
  if (value == null) return '—'
  return Number(value).toFixed(2)
}

export function estadoColor(estado: string): string {
  switch (estado) {
    case 'APROBADO': return 'text-emerald-700 bg-emerald-50 border-emerald-200'
    case 'REPROBADO': return 'text-red-700 bg-red-50 border-red-200'
    case 'PENDIENTE': return 'text-slate-600 bg-slate-100 border-slate-200'
    case 'ABIERTO': case 'ABIERTA': return 'text-blue-700 bg-blue-50 border-blue-200'
    case 'CERRADO': case 'CERRADA': return 'text-amber-700 bg-amber-50 border-amber-200'
    case 'ACTIVO': return 'text-emerald-700 bg-emerald-50 border-emerald-200'
    case 'INACTIVO': return 'text-slate-500 bg-slate-100 border-slate-200'
    default: return 'text-slate-600 bg-slate-100 border-slate-200'
  }
}

export function rolLabel(rol: string): string {
  const map: Record<string, string> = { admin: 'Administrador', maestro: 'Maestro', alumno: 'Alumno' }
  return map[rol] ?? rol
}

// Convierte los dos campos independientes (activo + estadoEvaluacion) en un
// único estado semántico para la UI de grupos.
export type EstadoGrupo = 'EN_CURSO' | 'ACTA_CERRADA' | 'FINALIZADO'

export function estadoGrupo(activo: boolean, estadoEvaluacion: string): EstadoGrupo {
  if (activo && estadoEvaluacion === 'ABIERTO') return 'EN_CURSO'
  if (activo && estadoEvaluacion === 'CERRADO') return 'ACTA_CERRADA'
  return 'FINALIZADO'
}

export function estadoGrupoLabel(estado: EstadoGrupo): string {
  const map: Record<EstadoGrupo, string> = {
    EN_CURSO:     'En curso',
    ACTA_CERRADA: 'Acta cerrada',
    FINALIZADO:   'Finalizado',
  }
  return map[estado]
}

export function estadoGrupoColor(estado: EstadoGrupo): string {
  switch (estado) {
    case 'EN_CURSO':     return 'text-emerald-700 bg-emerald-50 border-emerald-200'
    case 'ACTA_CERRADA': return 'text-amber-700 bg-amber-50 border-amber-200'
    case 'FINALIZADO':   return 'text-slate-500 bg-slate-100 border-slate-300'
  }
}
