import { useQuery } from '@tanstack/react-query'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { Users, BookOpen, School } from 'lucide-react'
import { getKpis, getRendimiento } from '@/api/analisis'
import { PageHeader } from '@/components/shared/PageHeader'
import { LoadingSpinner } from '@/components/shared/LoadingSpinner'
import { ErrorAlert } from '@/components/shared/ErrorAlert'

interface KpiCardProps {
  label: string
  value: number | undefined
  icon: React.ReactNode
  colorClass: string
  borderClass: string
  bgClass: string
}

function KpiCard({ label, value, icon, colorClass, borderClass, bgClass }: KpiCardProps) {
  return (
    <div className={`bg-white rounded-xl border-l-4 ${borderClass} shadow-sm p-6 flex items-center gap-5`}>
      <div className={`flex h-14 w-14 items-center justify-center rounded-xl ${bgClass} flex-shrink-0`}>
        <span className={colorClass}>{icon}</span>
      </div>
      <div>
        <p className="text-3xl font-bold text-slate-900">{value ?? '—'}</p>
        <p className="text-sm text-slate-500 mt-0.5">{label}</p>
      </div>
    </div>
  )
}

export default function Dashboard() {
  const { data: kpis, isLoading: kpisLoading, error: kpisError } = useQuery({
    queryKey: ['kpis'],
    queryFn: getKpis,
  })

  const { data: rendimiento, isLoading: rendLoading, error: rendError } = useQuery({
    queryKey: ['rendimiento'],
    queryFn: getRendimiento,
  })

  return (
    <div>
      <PageHeader
        title="Dashboard"
        description="Resumen general del sistema institucional de registro académico."
      />

      {kpisError && (
        <div className="mb-6">
          <ErrorAlert message="No se pudieron cargar los indicadores." />
        </div>
      )}

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5 mb-8">
        {kpisLoading ? (
          <div className="sm:col-span-3"><LoadingSpinner className="py-12" size="lg" /></div>
        ) : (
          <>
            <KpiCard
              label="Total Alumnos"
              value={kpis?.totalAlumnos}
              icon={<Users className="h-6 w-6" />}
              colorClass="text-blue-600"
              borderClass="border-blue-500"
              bgClass="bg-blue-50"
            />
            <KpiCard
              label="Total Materias"
              value={kpis?.totalMaterias}
              icon={<BookOpen className="h-6 w-6" />}
              colorClass="text-violet-600"
              borderClass="border-violet-500"
              bgClass="bg-violet-50"
            />
            <KpiCard
              label="Grupos Activos"
              value={kpis?.gruposActivos}
              icon={<School className="h-6 w-6" />}
              colorClass="text-emerald-600"
              borderClass="border-emerald-500"
              bgClass="bg-emerald-50"
            />
          </>
        )}
      </div>

      {/* Rendimiento Chart */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <h2 className="text-base font-semibold text-slate-900 mb-1">Rendimiento por semestre</h2>
        <p className="text-sm text-slate-500 mb-6">Alumnos aprobados y reprobados por período académico.</p>

        {rendError && <ErrorAlert message="No se pudo cargar el gráfico de rendimiento." />}

        {rendLoading ? (
          <LoadingSpinner className="py-20" size="lg" />
        ) : rendimiento && rendimiento.length > 0 ? (
          <ResponsiveContainer width="100%" height={320}>
            <BarChart data={rendimiento} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis
                dataKey="semestre"
                tick={{ fontSize: 12, fill: '#64748b' }}
                axisLine={{ stroke: '#e2e8f0' }}
                tickLine={false}
              />
              <YAxis
                tick={{ fontSize: 12, fill: '#64748b' }}
                axisLine={false}
                tickLine={false}
              />
              <Tooltip
                contentStyle={{ borderRadius: '8px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                labelStyle={{ fontWeight: 600, color: '#0f172a' }}
              />
              <Legend
                iconType="circle"
                iconSize={8}
                wrapperStyle={{ fontSize: '13px', paddingTop: '16px' }}
              />
              <Bar dataKey="aprobados" name="Aprobados" fill="#10b981" radius={[4, 4, 0, 0]} />
              <Bar dataKey="reprobados" name="Reprobados" fill="#ef4444" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        ) : (
          <div className="py-16 text-center text-slate-400 text-sm">Sin datos de rendimiento disponibles.</div>
        )}
      </div>
    </div>
  )
}
