import { useState, useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Cell,
} from 'recharts'
import { ChevronDown, AlertTriangle, TrendingDown, GraduationCap } from 'lucide-react'
import { getSemestresDisponibles, getReportes } from '@/api/reportes'
import type {
  MateriaReprobacionDto, AlumnoRiesgoDto,
  MaestroAprovechamientoDto,
} from '@/types'
import { PageHeader } from '@/components/shared/PageHeader'
import { LoadingSpinner } from '@/components/shared/LoadingSpinner'

// ─── Selector de semestre ─────────────────────────────────────────────────────

function SemestreSelector({ value, semestres, onChange }: {
  value: string; semestres: string[]; onChange: (s: string) => void
}) {
  return (
    <div className="flex items-center gap-3">
      <span className="text-sm font-medium text-slate-600">Semestre:</span>
      <div className="relative">
        <select
          value={value}
          onChange={e => onChange(e.target.value)}
          className="appearance-none w-36 pl-3 pr-8 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition"
        >
          {semestres.length === 0
            ? <option value="">Sin datos</option>
            : semestres.map(s => <option key={s} value={s}>{s}</option>)
          }
        </select>
        <ChevronDown className="absolute right-2 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-slate-400 pointer-events-none" />
      </div>
    </div>
  )
}

// ─── Reporte 1: Materias con mayor reprobación ────────────────────────────────

function MateriasReprobacion({ data }: { data: MateriaReprobacionDto[] }) {
  const top = data.slice(0, 10)
  const chartData = top.map(m => ({
    nombre: m.nombre.length > 22 ? m.nombre.slice(0, 22) + '…' : m.nombre,
    reprobados: m.reprobados,
    aprobados: m.aprobados,
    pct: m.porcentajeReprobacion,
  }))

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div className="flex items-center gap-3 px-5 py-4 border-b border-slate-100 bg-slate-50">
        <TrendingDown className="h-5 w-5 text-red-500" />
        <div>
          <h3 className="font-semibold text-slate-900 text-sm">Materias con mayor reprobación</h3>
          <p className="text-xs text-slate-500 mt-0.5">Grupos con acta cerrada — top 10</p>
        </div>
      </div>

      {data.length === 0 ? (
        <p className="px-5 py-8 text-center text-sm text-slate-400">Sin datos para este semestre.</p>
      ) : (
        <div className="p-5">
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={chartData} layout="vertical" margin={{ left: 0, right: 40, top: 4, bottom: 4 }}>
              <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#f1f5f9" />
              <XAxis type="number" tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={false} tickLine={false} />
              <YAxis type="category" dataKey="nombre" width={160} tick={{ fontSize: 11, fill: '#475569' }} axisLine={false} tickLine={false} />
              <Tooltip
                formatter={(v, name) => [v, name === 'reprobados' ? 'Reprobados' : 'Aprobados']}
                contentStyle={{ borderRadius: 8, border: '1px solid #e2e8f0', fontSize: 12 }}
              />
              <Bar dataKey="aprobados" name="aprobados" fill="#10b981" radius={[0, 3, 3, 0]} />
              <Bar dataKey="reprobados" name="reprobados" fill="#ef4444" radius={[0, 3, 3, 0]}>
                {chartData.map((entry, i) => (
                  <Cell key={i} fill={entry.pct >= 50 ? '#dc2626' : entry.pct >= 30 ? '#f97316' : '#ef4444'} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>

          {/* Tabla resumen */}
          <div className="mt-4 overflow-x-auto">
            <table className="w-full text-xs">
              <thead>
                <tr className="border-b border-slate-100">
                  {['Materia', 'Evaluados', 'Aprobados', 'Reprobados', '% Reprobación'].map(h => (
                    <th key={h} className="px-3 py-2 text-left font-semibold text-slate-500 uppercase tracking-wide">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {top.map(m => (
                  <tr key={m.materiaId} className="hover:bg-slate-50">
                    <td className="px-3 py-2 font-medium text-slate-800">{m.nombre}</td>
                    <td className="px-3 py-2 text-slate-600">{m.totalAlumnos}</td>
                    <td className="px-3 py-2 text-emerald-600">{m.aprobados}</td>
                    <td className="px-3 py-2 text-red-600">{m.reprobados}</td>
                    <td className="px-3 py-2">
                      <span className={`font-semibold ${m.porcentajeReprobacion >= 50 ? 'text-red-600' : m.porcentajeReprobacion >= 30 ? 'text-orange-600' : 'text-slate-700'}`}>
                        {m.porcentajeReprobacion}%
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}

// ─── Reporte 2: Alumnos en riesgo ─────────────────────────────────────────────

function AlumnosRiesgo({ data }: { data: AlumnoRiesgoDto[] }) {
  const [expandido, setExpandido] = useState<number | null>(null)

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div className="flex items-center gap-3 px-5 py-4 border-b border-slate-100 bg-slate-50">
        <AlertTriangle className="h-5 w-5 text-amber-500" />
        <div>
          <h3 className="font-semibold text-slate-900 text-sm">Alumnos en riesgo académico</h3>
          <p className="text-xs text-slate-500 mt-0.5">2 o más materias reprobadas en el semestre</p>
        </div>
        <span className={`ml-auto text-xs font-bold px-2.5 py-0.5 rounded-full ${data.length > 0 ? 'bg-amber-100 text-amber-700' : 'bg-emerald-100 text-emerald-700'}`}>
          {data.length}
        </span>
      </div>

      {data.length === 0 ? (
        <p className="px-5 py-8 text-center text-sm text-slate-400">Ningún alumno con 2 o más reprobadas.</p>
      ) : (
        <div className="overflow-y-auto max-h-[420px] divide-y divide-slate-100">
          {data.map(a => (
            <div key={a.alumnoId}>
              <button
                onClick={() => setExpandido(expandido === a.alumnoId ? null : a.alumnoId)}
                className="w-full flex items-center justify-between px-5 py-3 hover:bg-slate-50 transition-colors text-left"
              >
                <div>
                  <p className="text-sm font-medium text-slate-800">{a.nombre}</p>
                  <p className="text-xs text-slate-400 font-mono mt-0.5">{a.numControl}</p>
                </div>
                <div className="flex items-center gap-2.5">
                  <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-red-100 text-red-700">
                    {a.materiasReprobadas} reprobadas
                  </span>
                  <ChevronDown className={`h-4 w-4 text-slate-400 transition-transform ${expandido === a.alumnoId ? 'rotate-180' : ''}`} />
                </div>
              </button>
              {expandido === a.alumnoId && (
                <div className="px-5 pb-3 bg-slate-50/60">
                  <ul className="space-y-1">
                    {a.grupos.map((g, i) => (
                      <li key={i} className="text-xs text-slate-600 flex items-center gap-2">
                        <span className="h-1.5 w-1.5 rounded-full bg-red-400 flex-shrink-0" />
                        {g}
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

// ─── Reporte 3: Índice por maestro ────────────────────────────────────────────

function MaestrosAprovechamiento({ data }: { data: MaestroAprovechamientoDto[] }) {
  const [sortBy, setSortBy] = useState<keyof MaestroAprovechamientoDto>('porcentajeAprobacion')
  const [asc, setAsc] = useState(false)

  const handleSort = (col: keyof MaestroAprovechamientoDto) => {
    if (sortBy === col) setAsc(p => !p)
    else { setSortBy(col); setAsc(false) }
  }

  const sorted = [...data].sort((a, b) => {
    const va = a[sortBy] as number, vb = b[sortBy] as number
    return asc ? (va > vb ? 1 : -1) : (va < vb ? 1 : -1)
  })

  const Th = ({ col, label }: { col: keyof MaestroAprovechamientoDto; label: string }) => (
    <th
      onClick={() => handleSort(col)}
      className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide cursor-pointer hover:text-blue-600 select-none"
    >
      {label}{sortBy === col ? (asc ? ' ↑' : ' ↓') : ''}
    </th>
  )

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div className="flex items-center gap-3 px-5 py-4 border-b border-slate-100 bg-slate-50">
        <GraduationCap className="h-5 w-5 text-violet-500" />
        <div>
          <h3 className="font-semibold text-slate-900 text-sm">Índice de aprovechamiento por maestro</h3>
          <p className="text-xs text-slate-500 mt-0.5">Haz clic en los encabezados para ordenar</p>
        </div>
      </div>

      {data.length === 0 ? (
        <p className="px-5 py-8 text-center text-sm text-slate-400">Sin datos para este semestre.</p>
      ) : (
        <div className="overflow-x-auto overflow-y-auto max-h-[420px]">
          <table className="w-full text-sm">
            <thead className="sticky top-0 z-10">
              <tr className="bg-slate-50 border-b border-slate-200">
                <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide">Maestro</th>
                <Th col="grupos" label="Grupos" />
                <Th col="alumnosEvaluados" label="Evaluados" />
                <Th col="aprobados" label="Aprobados" />
                <Th col="reprobados" label="Reprobados" />
                <Th col="porcentajeAprobacion" label="% Aprobación" />
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {sorted.map(m => (
                <tr key={m.maestroId} className="hover:bg-slate-50">
                  <td className="px-4 py-3">
                    <p className="font-medium text-slate-800">{m.nombre}</p>
                    <p className="text-xs text-slate-400 font-mono">{m.numEmpleado}</p>
                  </td>
                  <td className="px-4 py-3 text-slate-600">{m.grupos}</td>
                  <td className="px-4 py-3 text-slate-600">{m.alumnosEvaluados}</td>
                  <td className="px-4 py-3 text-emerald-600 font-medium">{m.aprobados}</td>
                  <td className="px-4 py-3 text-red-600 font-medium">{m.reprobados}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <div className="flex-1 h-1.5 bg-slate-100 rounded-full overflow-hidden">
                        <div
                          className={`h-full rounded-full ${m.porcentajeAprobacion >= 70 ? 'bg-emerald-500' : m.porcentajeAprobacion >= 50 ? 'bg-amber-400' : 'bg-red-400'}`}
                          style={{ width: `${m.porcentajeAprobacion}%` }}
                        />
                      </div>
                      <span className={`text-xs font-semibold w-10 text-right ${m.porcentajeAprobacion >= 70 ? 'text-emerald-700' : m.porcentajeAprobacion >= 50 ? 'text-amber-700' : 'text-red-700'}`}>
                        {m.porcentajeAprobacion}%
                      </span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

// ─── Página principal ─────────────────────────────────────────────────────────

export default function Reportes() {
  const [semestre, setSemestre] = useState('')

  const { data: semestres = [], isLoading: loadingSem } = useQuery({
    queryKey: ['semestresDisponibles'],
    queryFn: getSemestresDisponibles,
  })

  // Inicializa el semestre con el más reciente al cargar
  useEffect(() => {
    if (semestres.length > 0 && !semestre) setSemestre(semestres[0])
  }, [semestres, semestre])

  const { data, isLoading } = useQuery({
    queryKey: ['reportes', semestre],
    queryFn: () => getReportes(semestre),
    enabled: !!semestre,
  })

  return (
    <div>
      <PageHeader
        title="Reportes"
        description="Análisis estratégico del desempeño académico por semestre."
        action={
          loadingSem ? <LoadingSpinner size="sm" /> :
            <SemestreSelector value={semestre} semestres={semestres} onChange={setSemestre} />
        }
      />

      {!semestre ? (
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm py-16 text-center text-slate-400 text-sm">
          No hay semestres con actas cerradas disponibles.
        </div>
      ) : isLoading ? (
        <LoadingSpinner className="py-20" size="lg" />
      ) : (
        <div className="space-y-6">
          {/* Fila 1: Materias + Alumnos en riesgo */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <MateriasReprobacion data={data?.materiasReprobacion ?? []} />
            <AlumnosRiesgo data={data?.alumnosRiesgo ?? []} />
          </div>

          {/* Fila 2: Índice por maestro */}
          <MaestrosAprovechamiento data={data?.maestrosAprovechamiento ?? []} />
        </div>
      )}
    </div>
  )
}
