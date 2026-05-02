export type Rol = 'admin' | 'maestro' | 'alumno'

export interface AuthUsuario {
  id: number
  nombre: string
  email: string
  rol: Rol
  identificador: string | null
  requiereCambioPassword: boolean
}

export interface LoginResponse {
  token: string
  tipo: string
  id: number
  nombre: string
  email: string
  rol: Rol
  requiereCambioPassword: boolean
}

export interface PerfilResponse {
  id: number
  nombre: string
  email: string
  rol: Rol
  identificador: string | null
}

// ─── Admin ────────────────────────────────────────────────────────────────────
export interface AdminResponse {
  id: number
  numEmpleado: string
  nombre: string
  email: string
  activo: boolean
}

export interface AlumnoResponse {
  id: number
  usuarioId: number
  numControl: string
  nombre: string
  email: string
  activo: boolean
}

export interface MaestroResponse {
  id: number
  usuarioId: number
  numEmpleado: string
  nombre: string
  email: string
  activo: boolean
}

export interface MateriaResponse {
  id: number
  clave: string
  nombre: string
  totalUnidades: number
  unidades: UnidadDto[]
}

export interface UnidadDto {
  id: number
  numero: number
  nombre: string
}

export interface GrupoResponse {
  id: number
  clave: string
  semestre: string
  materiaId: number
  materiaNombre: string
  materiaClave: string
  maestroId: number
  maestroNombre: string
  activo: boolean
  estadoEvaluacion: 'ABIERTO' | 'CERRADO'
  calificacionMinimaAprobatoria: number
  calificacionMaxima: number
}

export interface InscripcionResponse {
  id: number
  alumnoId: number
  alumnoNombre: string
  alumnoNumControl: string
  grupoId: number
  grupoClave: string
  materiaNombre: string
  semestre: string
  fecha: string
  estadoAcademico: 'APROBADO' | 'REPROBADO' | 'PENDIENTE'
  calificacionFinalCalculada: number | null
  calificacionFinalOverride: number | null
}

export interface ConfiguracionResponse {
  minimaAprobatoria: number
  maxima: number
}

export interface KpiResponse {
  totalAlumnos: number
  totalMaterias: number
  gruposActivos: number
}

export interface RendimientoResponse {
  semestre: string
  aprobados: number
  reprobados: number
}

export interface CargaResultadoResponse {
  procesados: number
  exitosos: number
  errores: { linea: number; mensaje: string }[]
}

// ─── Maestro ──────────────────────────────────────────────────────────────────
export interface ActividadGrupoResponse {
  id: number
  grupoId: number
  unidadId: number
  unidadNumero: number
  unidadNombre: string
  nombre: string
  ponderacion: number
}

export interface BonusResponse {
  id: number
  inscripcionId: number
  unidadId: number | null
  tipo: 'unidad' | 'materia'
  puntos: number
  justificacion: string
  otorgadoEn: string
}

export interface ResultadoDto {
  id: number
  inscripcionId: number
  actividadGrupoId: number
  actividadNombre: string
  ponderacion: number
  calificacion: number | null
  aportacion: number
}

export interface ResultadoUnidadDto {
  inscripcionId: number
  unidadId: number
  unidadNumero: number
  unidadNombre: string
  desglose: ResultadoDto[]
  resultadoBase: number | null
  bonusPuntos: number
  resultadoFinal: number | null
  actividadesCalificadas: number
  actividadesTotales: number
  estado: string
}

export interface CalificacionFinalDto {
  inscripcionId: number
  alumnoId: number
  alumnoNombre: string
  alumnoNumControl: string
  unidades: ResultadoUnidadDto[]
  calificacionCalculada: number | null
  bonusMateria: number
  calificacionConBonus: number | null
  calificacionFinal: number | null
  esOverride: boolean
  overrideJustificacion: string | null
  estado: string
}

export interface ErrorResponse {
  error: string
}
