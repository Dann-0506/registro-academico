import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import {
  Users, GraduationCap, School, ClipboardList,
  AlertTriangle, Clock, UserX, CheckCircle2, Settings,
} from 'lucide-react'
import { getDashboard } from '@/api/configuracion'
import type { GrupoAlertaDto, AlumnoAlertaDto } from '@/types'
import { LoadingSpinner } from '@/components/shared/LoadingSpinner'

// ─── KPI Card ────────────────────────────────────────────────────────────────

function KpiCard({ label, value, icon, color }: {
  label: string; value: number | undefined; icon: React.ReactNode; color: string
}) {
  return (
    <div className={`bg-white rounded-xl border-l-4 ${color} shadow-sm p-5 flex items-center gap-4`}>
      <div className="flex-shrink-0">{icon}</div>
      <div>
        <p className="text-2xl font-bold text-slate-900">{value ?? '—'}</p>
        <p className="text-sm text-slate-500 mt-0.5">{label}</p>
      </div>
    </div>
  )
}

// ─── Alerta de grupos ────────────────────────────────────────────────────────

function AlertaGrupos({ titulo, icono, colorHeader, colorBadge, grupos, emptyMsg }: {
  titulo: string
  icono: React.ReactNode
  colorHeader: string
  colorBadge: string
  grupos: GrupoAlertaDto[]
  emptyMsg: string
}) {
  const MAX = 5
  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden flex flex-col">
      {/* Header */}
      <div className={`flex items-center justify-between px-5 py-3.5 border-b border-slate-100 ${colorHeader}`}>
        <div className="flex items-center gap-2.5 text-sm font-semibold">
          {icono}
          {titulo}
        </div>
        <span className={`text-xs font-bold px-2.5 py-0.5 rounded-full ${colorBadge}`}>
          {grupos.length}
        </span>
      </div>

      {/* Body */}
      <div className="flex-1 divide-y divide-slate-100">
        {grupos.length === 0 ? (
          <div className="flex items-center gap-2.5 px-5 py-4 text-sm text-emerald-600">
            <CheckCircle2 className="h-4 w-4 flex-shrink-0" />
            {emptyMsg}
          </div>
        ) : (
          grupos.slice(0, MAX).map(g => (
            <div key={g.id} className="px-5 py-3">
              <div className="flex items-start justify-between gap-2">
                <p className="text-sm font-medium text-slate-800 truncate">{g.materiaNombre}</p>
                <span className="text-xs text-slate-400 flex-shrink-0 font-mono">{g.clave}</span>
              </div>
              <p className="text-xs text-slate-500 mt-0.5">{g.maestroNombre}</p>
            </div>
          ))
        )}
        {grupos.length > MAX && (
          <div className="px-5 py-2.5 text-xs text-slate-400">
            y {grupos.length - MAX} grupo{grupos.length - MAX > 1 ? 's' : ''} más...
          </div>
        )}
      </div>
    </div>
  )
}

// ─── Alerta de alumnos ────────────────────────────────────────────────────────

function AlertaAlumnos({ alumnos }: { alumnos: AlumnoAlertaDto[] }) {
  const MAX = 6
  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      {/* Header */}
      <div className="flex items-center justify-between px-5 py-3.5 border-b border-slate-100 bg-slate-50">
        <div className="flex items-center gap-2.5 text-sm font-semibold text-slate-700">
          <UserX className="h-4 w-4 text-slate-500" />
          Alumnos activos sin inscripciones en el semestre
        </div>
        <span className={`text-xs font-bold px-2.5 py-0.5 rounded-full ${alumnos.length > 0 ? 'bg-red-100 text-red-700' : 'bg-emerald-100 text-emerald-700'}`}>
          {alumnos.length}
        </span>
      </div>

      {/* Body */}
      {alumnos.length === 0 ? (
        <div className="flex items-center gap-2.5 px-5 py-4 text-sm text-emerald-600">
          <CheckCircle2 className="h-4 w-4 flex-shrink-0" />
          Todos los alumnos activos tienen al menos una inscripción este semestre.
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 divide-y sm:divide-y-0 sm:divide-x divide-slate-100">
          {alumnos.slice(0, MAX).map(a => (
            <div key={a.id} className="px-5 py-3">
              <p className="text-sm font-medium text-slate-800">{a.nombre}</p>
              <p className="text-xs text-slate-400 font-mono mt-0.5">{a.numControl}</p>
            </div>
          ))}
        </div>
      )}
      {alumnos.length > MAX && (
        <div className="px-5 py-2.5 border-t border-slate-100 text-xs text-slate-400">
          y {alumnos.length - MAX} alumno{alumnos.length - MAX > 1 ? 's' : ''} más sin inscribir...
        </div>
      )}
    </div>
  )
}

// ─── Página principal ─────────────────────────────────────────────────────────

export default function Dashboard() {
  const navigate = useNavigate()
  const { data, isLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: getDashboard,
    staleTime: 0, // siempre obsoleto: refetch garantizado al montar el componente
  })

  if (isLoading) return <LoadingSpinner className="py-20" size="lg" />

  return (
    <div className="space-y-6">
      {/* Header con semestre activo */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Dashboard</h1>
          <p className="mt-1 text-sm text-slate-500">Estado operativo del sistema.</p>
        </div>
        <button
          onClick={() => navigate('/admin/configuracion')}
          className="flex items-center gap-1.5 text-xs text-slate-500 hover:text-blue-600 bg-slate-100 hover:bg-blue-50 border border-slate-200 hover:border-blue-200 px-3 py-1.5 rounded-lg transition-colors"
        >
          <Settings className="h-3.5 w-3.5" />
          Semestre activo: <strong>{data?.semestreActivo || '—'}</strong>
        </button>
      </div>

      {/* KPIs */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <KpiCard label="Alumnos activos" value={data?.alumnosActivos}
          color="border-blue-500"
          icon={<div className="h-10 w-10 rounded-xl bg-blue-50 flex items-center justify-center"><Users className="h-5 w-5 text-blue-600" /></div>} />
        <KpiCard label="Maestros activos" value={data?.maestrosActivos}
          color="border-violet-500"
          icon={<div className="h-10 w-10 rounded-xl bg-violet-50 flex items-center justify-center"><GraduationCap className="h-5 w-5 text-violet-600" /></div>} />
        <KpiCard label="Grupos en curso" value={data?.gruposEnCurso}
          color="border-emerald-500"
          icon={<div className="h-10 w-10 rounded-xl bg-emerald-50 flex items-center justify-center"><School className="h-5 w-5 text-emerald-600" /></div>} />
        <KpiCard label="Inscripciones activas" value={data?.inscripcionesActivas}
          color="border-amber-500"
          icon={<div className="h-10 w-10 rounded-xl bg-amber-50 flex items-center justify-center"><ClipboardList className="h-5 w-5 text-amber-600" /></div>} />
      </div>

      {/* Alertas en dos columnas */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        <AlertaGrupos
          titulo="Grupos sin actividades definidas"
          icono={<AlertTriangle className="h-4 w-4 text-amber-500" />}
          colorHeader="bg-amber-50"
          colorBadge={data && data.gruposSinActividades.length > 0 ? 'bg-amber-200 text-amber-800' : 'bg-emerald-100 text-emerald-700'}
          grupos={data?.gruposSinActividades ?? []}
          emptyMsg="Todos los grupos tienen actividades definidas."
        />
        <AlertaGrupos
          titulo="Grupos pendientes de cerrar acta"
          icono={<Clock className="h-4 w-4 text-blue-500" />}
          colorHeader="bg-blue-50"
          colorBadge={data && data.gruposPendientesCierre.length > 0 ? 'bg-blue-200 text-blue-800' : 'bg-emerald-100 text-emerald-700'}
          grupos={data?.gruposPendientesCierre ?? []}
          emptyMsg="No hay actas pendientes de cierre."
        />
      </div>

      {/* Alumnos sin inscripciones */}
      <AlertaAlumnos alumnos={data?.alumnosSinInscripciones ?? []} />
    </div>
  )
}
