import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { BookOpen, ChevronRight } from 'lucide-react'
import { getMisGrupos } from '@/api/grupos'
import type { GrupoResponse } from '@/types'
import { PageHeader } from '@/components/shared/PageHeader'
import { StatusBadge } from '@/components/shared/StatusBadge'
import { LoadingSpinner } from '@/components/shared/LoadingSpinner'
import { ErrorAlert } from '@/components/shared/ErrorAlert'

export default function MisGrupos() {
  const navigate = useNavigate()

  const { data: grupos = [], isLoading, error } = useQuery({
    queryKey: ['misGrupos'],
    queryFn: getMisGrupos,
  })

  return (
    <div>
      <PageHeader
        title="Mis Grupos"
        description="Grupos académicos asignados a tu cuenta."
      />

      {error && <ErrorAlert message="No se pudieron cargar tus grupos." />}

      {isLoading ? (
        <LoadingSpinner className="py-20" size="lg" />
      ) : grupos.length === 0 ? (
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm py-20 text-center text-slate-400 text-sm">
          No tienes grupos asignados en este momento.
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {grupos.map((g: GrupoResponse) => (
            <button
              key={g.id}
              onClick={() => navigate(`/maestro/grupos/${g.id}`)}
              className="group bg-white rounded-xl border border-slate-200 shadow-sm p-5 text-left hover:border-blue-300 hover:shadow-md transition-all flex flex-col gap-3"
            >
              {/* Header */}
              <div className="flex items-start justify-between gap-2">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 flex-shrink-0">
                  <BookOpen className="h-5 w-5 text-blue-600" />
                </div>
                <StatusBadge estado={g.estadoEvaluacion} />
              </div>

              {/* Info */}
              <div>
                <p className="font-semibold text-slate-900 text-base leading-tight">{g.materiaNombre}</p>
                <p className="text-xs text-slate-500 mt-0.5">{g.materiaClave}</p>
              </div>

              <div className="space-y-1">
                <div className="flex justify-between text-sm">
                  <span className="text-slate-500">Clave del grupo</span>
                  <span className="font-medium text-slate-700">{g.clave}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-slate-500">Semestre</span>
                  <span className="font-medium text-slate-700">{g.semestre}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-slate-500">Estado</span>
                  <span className={`font-medium text-sm ${g.activo ? 'text-emerald-600' : 'text-slate-400'}`}>
                    {g.activo ? 'Activo' : 'Inactivo'}
                  </span>
                </div>
              </div>

              {/* Footer */}
              <div className="flex items-center justify-end text-xs text-blue-500 group-hover:text-blue-700 mt-auto pt-1 gap-1">
                Ver detalle
                <ChevronRight className="h-3.5 w-3.5" />
              </div>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
