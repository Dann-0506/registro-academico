import { useState } from 'react'
import { useInvalidateDashboard } from '@/hooks/useInvalidateDashboard'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Trash2, UserPlus, ChevronDown } from 'lucide-react'
import axios from 'axios'
import { getGrupos } from '@/api/grupos'
import { getAlumnos } from '@/api/alumnos'
import { getInscripcionesByGrupo, createInscripcion, deleteInscripcion } from '@/api/inscripciones'
import type { InscripcionResponse, GrupoResponse, AlumnoResponse } from '@/types'
import { DataTable } from '@/components/shared/DataTable'
import { FormModal } from '@/components/shared/FormModal'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { PageHeader } from '@/components/shared/PageHeader'
import { StatusBadge } from '@/components/shared/StatusBadge'
import { ErrorAlert } from '@/components/shared/ErrorAlert'
import { LoadingSpinner } from '@/components/shared/LoadingSpinner'
import { formatCalificacion } from '@/lib/utils'

export default function Inscripciones() {
  const qc = useQueryClient()
  const invalidateDashboard = useInvalidateDashboard()
  const [selectedGrupoId, setSelectedGrupoId] = useState<number | null>(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [selectedAlumnoId, setSelectedAlumnoId] = useState('')
  const [alumnoSearch, setAlumnoSearch] = useState('')
  const [formError, setFormError] = useState('')
  const [deleteTarget, setDeleteTarget] = useState<InscripcionResponse | null>(null)

  const { data: grupos = [], isLoading: gruposLoading } = useQuery({
    queryKey: ['grupos'],
    queryFn: getGrupos,
  })

  const { data: alumnos = [] } = useQuery({
    queryKey: ['alumnos'],
    queryFn: getAlumnos,
  })

  const { data: inscripciones = [], isLoading: inscLoading } = useQuery({
    queryKey: ['inscripciones', selectedGrupoId],
    queryFn: () => getInscripcionesByGrupo(selectedGrupoId!),
    enabled: !!selectedGrupoId,
  })

  const invalidateInsc = () => {
    qc.invalidateQueries({ queryKey: ['inscripciones', selectedGrupoId] })
    invalidateDashboard()
  }

  const createMut = useMutation({
    mutationFn: createInscripcion,
    onSuccess: () => { invalidateInsc(); setModalOpen(false); setSelectedAlumnoId(''); setAlumnoSearch(''); setFormError('') },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al inscribir alumno.' : 'Error inesperado.'),
  })

  const deleteMut = useMutation({
    mutationFn: deleteInscripcion,
    onSuccess: () => { invalidateInsc(); setDeleteTarget(null) },
  })

  const grupoSeleccionado = grupos.find((g: GrupoResponse) => g.id === selectedGrupoId)
  const grupoCerrado = grupoSeleccionado?.estadoEvaluacion === 'CERRADO'

  const alumnosFiltrados = alumnos.filter((a: AlumnoResponse) =>
    alumnoSearch
      ? a.nombre.toLowerCase().includes(alumnoSearch.toLowerCase()) ||
        a.numControl.toLowerCase().includes(alumnoSearch.toLowerCase())
      : true
  )

  const handleInscribir = () => {
    if (!selectedAlumnoId) { setFormError('Selecciona un alumno.'); return }
    if (!selectedGrupoId) return
    createMut.mutate({ alumnoId: Number(selectedAlumnoId), grupoId: selectedGrupoId })
  }

  return (
    <div>
      <PageHeader
        title="Inscripciones"
        description="Gestión de inscripciones de alumnos en grupos."
      />

      {/* Group Selector */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-4 mb-6">
        <label className="block text-sm font-medium text-slate-700 mb-2">Seleccionar grupo</label>
        {gruposLoading ? (
          <LoadingSpinner size="sm" className="py-2" />
        ) : (
          <div className="relative max-w-md">
            <select
              value={selectedGrupoId ?? ''}
              onChange={(e) => setSelectedGrupoId(e.target.value ? Number(e.target.value) : null)}
              className="w-full px-4 py-2.5 pr-10 rounded-lg border border-slate-300 text-sm text-slate-800 bg-white appearance-none focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition"
            >
              <option value="">-- Elige un grupo --</option>
              {grupos.map((g: GrupoResponse) => (
                <option key={g.id} value={g.id}>
                  {g.clave} — {g.materiaNombre} ({g.semestre})
                </option>
              ))}
            </select>
            <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 pointer-events-none" />
          </div>
        )}
      </div>

      {/* Inscriptions Table */}
      {selectedGrupoId ? (
        <>
          <DataTable<InscripcionResponse>
            data={inscripciones}
            isLoading={inscLoading}
            keyExtractor={(i) => i.id}
            searchable
            searchKeys={['alumnoNombre', 'alumnoNumControl']}
            searchPlaceholder="Buscar por nombre o núm. de control..."
            emptyMessage="No hay alumnos inscritos en este grupo."
            actions={
              <button
                onClick={() => { setFormError(''); setSelectedAlumnoId(''); setAlumnoSearch(''); setModalOpen(true) }}
                disabled={grupoCerrado}
                title={grupoCerrado ? 'El grupo tiene acta cerrada' : undefined}
                className="flex items-center gap-2 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <UserPlus className="h-4 w-4" />
                Inscribir alumno
              </button>
            }
            columns={[
              { header: 'Núm. de control', accessor: 'alumnoNumControl' },
              { header: 'Nombre alumno', accessor: 'alumnoNombre' },
              {
                header: 'Fecha',
                accessor: (i) => i.fecha ? new Date(i.fecha).toLocaleDateString('es-MX') : '—',
              },
              {
                header: 'Estado académico',
                accessor: (i) => <StatusBadge estado={i.estadoAcademico} />,
              },
              {
                header: 'Cal. final',
                accessor: (i) => formatCalificacion(i.calificacionFinalCalculada ?? i.calificacionFinalOverride),
              },
            ]}
            rowActions={(i) => (
              <button
                onClick={() => setDeleteTarget(i)}
                disabled={grupoCerrado}
                title={grupoCerrado ? 'El grupo tiene acta cerrada' : 'Eliminar inscripción'}
                className="p-1.5 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-colors disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:text-slate-400 disabled:hover:bg-transparent"
              >
                <Trash2 className="h-4 w-4" />
              </button>
            )}
          />

          {grupoSeleccionado && (
            <p className="text-xs text-slate-400 mt-2">
              Grupo: {grupoSeleccionado.clave} · {grupoSeleccionado.materiaNombre} · {grupoSeleccionado.semestre} · {grupoSeleccionado.maestroNombre}
            </p>
          )}
        </>
      ) : (
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm py-20 text-center text-slate-400 text-sm">
          Selecciona un grupo para ver sus inscripciones.
        </div>
      )}

      {/* Inscribir Modal */}
      <FormModal
        open={modalOpen}
        title="Inscribir alumno"
        subtitle={`Inscribir en el grupo: ${grupoSeleccionado?.clave ?? ''}`}
        onClose={() => setModalOpen(false)}
        onSubmit={handleInscribir}
        loading={createMut.isPending}
        submitLabel="Inscribir"
      >
        <div className="space-y-4">
          {formError && <ErrorAlert message={formError} onClose={() => setFormError('')} />}

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">
              Buscar alumno <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={alumnoSearch}
              onChange={(e) => { setAlumnoSearch(e.target.value); setSelectedAlumnoId('') }}
              placeholder="Nombre o núm. de control..."
              className="w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition mb-2"
            />
            <div className="max-h-48 overflow-y-auto border border-slate-200 rounded-lg divide-y divide-slate-100">
              {alumnosFiltrados.length === 0 ? (
                <div className="py-4 text-center text-slate-400 text-sm">Sin resultados.</div>
              ) : alumnosFiltrados.map((a: AlumnoResponse) => (
                <button
                  key={a.id}
                  type="button"
                  onClick={() => { setSelectedAlumnoId(String(a.id)); setAlumnoSearch(`${a.numControl} — ${a.nombre}`) }}
                  className={`w-full text-left px-4 py-2.5 text-sm transition-colors ${selectedAlumnoId === String(a.id) ? 'bg-blue-50 text-blue-700 font-medium' : 'text-slate-700 hover:bg-slate-50'}`}
                >
                  <span className="font-medium">{a.numControl}</span>
                  <span className="text-slate-500 ml-2">{a.nombre}</span>
                </button>
              ))}
            </div>
          </div>
        </div>
      </FormModal>

      {/* Delete Confirm */}
      <ConfirmDialog
        open={!!deleteTarget}
        title="Eliminar inscripción"
        description={`¿Eliminar la inscripción de "${deleteTarget?.alumnoNombre}" del grupo? Esta acción no se puede deshacer.`}
        confirmLabel="Eliminar"
        variant="destructive"
        loading={deleteMut.isPending}
        onConfirm={() => deleteTarget && deleteMut.mutate(deleteTarget.id)}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  )
}
