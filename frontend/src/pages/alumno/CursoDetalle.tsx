import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { FileDown, BookOpen } from 'lucide-react'
import { getMisCalificaciones, descargarBoletaAlumno } from '@/api/calificaciones'
import type { ResultadoUnidadDto, ResultadoDto } from '@/types'
import { PageHeader } from '@/components/shared/PageHeader'
import { StatusBadge } from '@/components/shared/StatusBadge'
import { LoadingSpinner } from '@/components/shared/LoadingSpinner'
import { ErrorAlert } from '@/components/shared/ErrorAlert'
import { formatCalificacion } from '@/lib/utils'

export default function CursoDetalle() {
  const { grupoId } = useParams<{ grupoId: string }>()
  const id = Number(grupoId)
  const [downloadLoading, setDownloadLoading] = useState(false)

  const { data: reporte, isLoading, error } = useQuery({
    queryKey: ['misCalificaciones', id],
    queryFn: () => getMisCalificaciones(id),
    enabled: !!id,
  })

  const handleDescargar = async () => {
    setDownloadLoading(true)
    try { await descargarBoletaAlumno(id) } finally { setDownloadLoading(false) }
  }

  if (isLoading) return <LoadingSpinner className="py-20" size="lg" />
  if (error || !reporte) return <ErrorAlert message="No se pudo cargar la información del curso." />

  const calFinal = reporte.calificacionFinal ?? reporte.calificacionConBonus ?? reporte.calificacionCalculada

  return (
    <div>
      <PageHeader
        title="Detalle del curso"
        description="Consulta tus calificaciones por unidad y tu resultado final."
        action={
          <button
            onClick={handleDescargar}
            disabled={downloadLoading}
            className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 border border-slate-200 shadow-sm disabled:opacity-50 transition-colors"
          >
            {downloadLoading ? <LoadingSpinner size="sm" /> : <FileDown className="h-4 w-4" />}
            Descargar boleta (PDF)
          </button>
        }
      />

      {/* Unidades */}
      <div className="space-y-5 mb-6">
        {reporte.unidades.map((u: ResultadoUnidadDto) => {
          const pct = u.actividadesTotales > 0 ? (u.actividadesCalificadas / u.actividadesTotales) * 100 : 0
          return (
            <div key={u.unidadId} className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
              {/* Unit header */}
              <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100 bg-slate-50">
                <div className="flex items-center gap-3">
                  <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-100 text-blue-700 text-sm font-bold">
                    {u.unidadNumero}
                  </div>
                  <div>
                    <p className="font-semibold text-slate-800">{u.unidadNombre}</p>
                    <p className="text-xs text-slate-400">
                      {u.actividadesCalificadas} de {u.actividadesTotales} actividades calificadas
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-lg font-bold text-slate-900">{formatCalificacion(u.resultadoFinal)}</p>
                  {u.bonusPuntos > 0 && (
                    <p className="text-xs text-violet-600">+{u.bonusPuntos} bonus</p>
                  )}
                </div>
              </div>

              {/* Progress */}
              <div className="px-5 pt-3 pb-2">
                <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
                  <span>Progreso de evaluación</span>
                  <span>{Math.round(pct)}%</span>
                </div>
                <div className="h-1.5 w-full bg-slate-100 rounded-full overflow-hidden">
                  <div className="h-full bg-blue-500 rounded-full transition-all" style={{ width: `${pct}%` }} />
                </div>
              </div>

              {/* Activities */}
              {u.desglose.length > 0 && (
                <div className="divide-y divide-slate-100">
                  {u.desglose.map((d: ResultadoDto) => (
                    <div key={d.id} className="flex items-center justify-between px-5 py-3">
                      <div>
                        <p className="text-sm text-slate-700">{d.actividadNombre}</p>
                        <p className="text-xs text-slate-400">{d.ponderacion}% de la unidad</p>
                      </div>
                      <div className="text-right">
                        <p className="text-sm font-semibold text-slate-800">{formatCalificacion(d.calificacion)}</p>
                        {d.aportacion > 0 && (
                          <p className="text-xs text-slate-400">Aporta: {d.aportacion.toFixed(2)}</p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )
        })}
      </div>

      {/* Final grade card */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="flex items-center gap-3 px-6 py-4 border-b border-slate-100 bg-slate-50">
          <BookOpen className="h-5 w-5 text-slate-400" />
          <h3 className="font-semibold text-slate-900">Calificación final</h3>
        </div>
        <div className="px-6 py-6">
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <div className="text-center p-4 rounded-xl bg-slate-50 border border-slate-200">
              <p className="text-2xl font-bold text-slate-900">{formatCalificacion(reporte.calificacionCalculada)}</p>
              <p className="text-xs text-slate-500 mt-1">Calculada</p>
            </div>
            {reporte.bonusMateria > 0 && (
              <div className="text-center p-4 rounded-xl bg-violet-50 border border-violet-200">
                <p className="text-2xl font-bold text-violet-700">+{reporte.bonusMateria.toFixed(2)}</p>
                <p className="text-xs text-violet-500 mt-1">Bonus materia</p>
              </div>
            )}
            {reporte.calificacionConBonus != null && (
              <div className="text-center p-4 rounded-xl bg-blue-50 border border-blue-200">
                <p className="text-2xl font-bold text-blue-700">{formatCalificacion(reporte.calificacionConBonus)}</p>
                <p className="text-xs text-blue-500 mt-1">Con bonus</p>
              </div>
            )}
            <div className="text-center p-4 rounded-xl bg-slate-800 border border-slate-700">
              <p className={`text-2xl font-bold ${reporte.estado === 'APROBADO' ? 'text-emerald-400' : reporte.estado === 'REPROBADO' ? 'text-red-400' : 'text-white'}`}>
                {formatCalificacion(calFinal)}
              </p>
              <p className="text-xs text-slate-400 mt-1">Final</p>
            </div>
          </div>

          <div className="flex items-center justify-between mt-5 pt-4 border-t border-slate-100">
            <div>
              {reporte.esOverride && (
                <p className="text-xs text-amber-600 font-medium">
                  Override aplicado{reporte.overrideJustificacion ? `: ${reporte.overrideJustificacion}` : ''}
                </p>
              )}
            </div>
            <StatusBadge estado={reporte.estado} />
          </div>
        </div>
      </div>
    </div>
  )
}
