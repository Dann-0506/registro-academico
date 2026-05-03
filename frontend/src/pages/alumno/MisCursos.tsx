import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { BookOpen, ChevronRight } from 'lucide-react'
import { getMisInscripciones } from '@/api/inscripciones'
import type { InscripcionResponse } from '@/types'
import { PageHeader } from '@/components/shared/PageHeader'
import { StatusBadge } from '@/components/shared/StatusBadge'
import { LoadingSpinner } from '@/components/shared/LoadingSpinner'
import { ErrorAlert } from '@/components/shared/ErrorAlert'
import { formatCalificacion } from '@/lib/utils'

export default function MisCursos() {
  const navigate = useNavigate()

  const { data: inscripciones = [], isLoading, error } = useQuery({
    queryKey: ['misInscripciones'],
    queryFn: getMisInscripciones,
  })

  return (
    <div>
      <PageHeader
        title="Mis Cursos"
        description="Cursos en los que estás inscrito este período."
      />

      {error && <ErrorAlert message="No se pudieron cargar tus cursos." />}

      {isLoading ? (
        <LoadingSpinner className="py-20" size="lg" />
      ) : inscripciones.length === 0 ? (
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm py-20 text-center text-slate-400 text-sm">
          No tienes cursos inscritos en este momento.
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {inscripciones.map((i: InscripcionResponse) => {
            const calFinal = i.calificacionFinalOverride ?? i.calificacionFinalCalculada

            return (
              <button
                key={i.id}
                onClick={() => navigate(`/alumno/cursos/${i.grupoId}`)}
                className="group bg-white rounded-xl border border-slate-200 shadow-sm p-5 text-left hover:border-blue-300 hover:shadow-md transition-all flex flex-col gap-3"
              >
                {/* Header */}
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 flex-shrink-0">
                  <BookOpen className="h-5 w-5 text-blue-600" />
                </div>

                {/* Info */}
                <div>
                  <p className="font-semibold text-slate-900 text-base leading-tight">{i.materiaNombre}</p>
                  <p className="text-xs text-slate-500 mt-0.5">Grupo: {i.grupoClave}</p>
                </div>

                <div className="space-y-1.5">
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-500">Semestre</span>
                    <span className="font-medium text-slate-700">{i.semestre}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-500">Fecha inscripción</span>
                    <span className="font-medium text-slate-700">
                      {i.fecha ? new Date(i.fecha).toLocaleDateString('es-MX', { day: '2-digit', month: 'short' }) : '—'}
                    </span>
                  </div>
                  {calFinal != null && (
                    <div className="flex justify-between text-sm">
                      <span className="text-slate-500">Cal. final</span>
                      <span className={`font-bold ${i.estadoAcademico === 'APROBADO' ? 'text-emerald-600' : i.estadoAcademico === 'REPROBADO' ? 'text-red-600' : 'text-slate-700'}`}>
                        {formatCalificacion(calFinal)}
                      </span>
                    </div>
                  )}
                </div>

                {/* Estado evaluación */}
                <div className="flex items-center justify-between mt-auto pt-2 border-t border-slate-100">
                  <StatusBadge estado={i.estadoAcademico === 'APROBADO' ? 'APROBADO' : i.estadoAcademico === 'REPROBADO' ? 'REPROBADO' : 'PENDIENTE'} />
                  <span className="flex items-center gap-1 text-xs text-blue-500 group-hover:text-blue-700">
                    Ver detalle <ChevronRight className="h-3.5 w-3.5" />
                  </span>
                </div>
              </button>
            )
          })}
        </div>
      )}
    </div>
  )
}
