import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Pencil, Trash2, Save, Lock, LockOpen, LockKeyhole, FileDown, Gift } from 'lucide-react'
import axios from 'axios'
import { getMiGrupo, cerrarGrupoMaestro, reabrirGrupoMaestro, cerrarDefinitivamenteMaestro } from '@/api/grupos'
import { getUnidadesByMateria } from '@/api/materias'
import { getActividades, createActividad, updateActividad, deleteActividad } from '@/api/actividades'
import { getReporte, guardarLote, aplicarOverride, descargarActaPdf } from '@/api/calificaciones'
import { getBonus, createBonus } from '@/api/bonus'
import type {
  GrupoResponse, ActividadGrupoResponse, CalificacionFinalDto,
  ResultadoUnidadDto, BonusResponse,
} from '@/types'
import { StatusBadge } from '@/components/shared/StatusBadge'
import { FormModal } from '@/components/shared/FormModal'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { LoadingSpinner } from '@/components/shared/LoadingSpinner'
import { ErrorAlert } from '@/components/shared/ErrorAlert'
import { formatCalificacion } from '@/lib/utils'

// ─── Types ───────────────────────────────────────────────────────────────────

type Tab = 'actividades' | 'calificaciones' | 'bonus' | 'reporte'

// ─── Actividades Tab ─────────────────────────────────────────────────────────

function ActividadesTab({ grupo }: { grupo: GrupoResponse }) {
  const qc = useQueryClient()
  const grupoId = grupo.id
  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<ActividadGrupoResponse | null>(null)
  const [form, setForm] = useState({ unidadId: '', nombre: '', ponderacion: '' })
  const [formError, setFormError] = useState('')
  const [deleteTarget, setDeleteTarget] = useState<ActividadGrupoResponse | null>(null)

  const { data: actividades = [], isLoading } = useQuery({
    queryKey: ['actividades', grupoId],
    queryFn: () => getActividades(grupoId),
  })

  const { data: unidades = [] } = useQuery({
    queryKey: ['unidades', grupo.materiaId],
    queryFn: () => getUnidadesByMateria(grupo.materiaId),
  })

  const inv = () => qc.invalidateQueries({ queryKey: ['actividades', grupoId] })

  const createMut = useMutation({
    mutationFn: (d: { unidadId: number; nombre: string; ponderacion: number }) => createActividad(grupoId, d),
    onSuccess: () => { inv(); closeModal() },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al crear actividad.' : 'Error inesperado.'),
  })

  const updateMut = useMutation({
    mutationFn: ({ id, data }: { id: number; data: { nombre: string; ponderacion: number } }) => updateActividad(grupoId, id, data),
    onSuccess: () => { inv(); closeModal() },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al actualizar.' : 'Error inesperado.'),
  })

  const deleteMut = useMutation({
    mutationFn: (id: number) => deleteActividad(grupoId, id),
    onSuccess: () => { inv(); setDeleteTarget(null) },
  })

  const openCreate = () => {
    setEditTarget(null)
    setForm({ unidadId: String(grupo.materiaId ? '' : ''), nombre: '', ponderacion: '' })
    setFormError('')
    setModalOpen(true)
  }
  const openEdit = (a: ActividadGrupoResponse) => {
    setEditTarget(a)
    setForm({ unidadId: String(a.unidadId), nombre: a.nombre, ponderacion: String(a.ponderacion) })
    setFormError('')
    setModalOpen(true)
  }
  const closeModal = () => { setModalOpen(false); setEditTarget(null); setFormError('') }

  const handleSubmit = () => {
    if (!form.nombre.trim()) { setFormError('El nombre es requerido.'); return }
    if (!form.ponderacion || isNaN(Number(form.ponderacion))) { setFormError('La ponderación es requerida.'); return }
    if (editTarget) {
      updateMut.mutate({ id: editTarget.id, data: { nombre: form.nombre.trim(), ponderacion: Number(form.ponderacion) } })
    } else {
      if (!form.unidadId) { setFormError('Selecciona una unidad.'); return }
      createMut.mutate({ unidadId: Number(form.unidadId), nombre: form.nombre.trim(), ponderacion: Number(form.ponderacion) })
    }
  }

  // Group by unit
  const byUnidad = actividades.reduce<Record<number, ActividadGrupoResponse[]>>((acc, a) => {
    if (!acc[a.unidadId]) acc[a.unidadId] = []
    acc[a.unidadId].push(a)
    return acc
  }, {})

  const inputClass = 'w-full px-3 py-2 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition'

  // Get unique units from activities for grouping labels
  const unitLabels = actividades.reduce<Record<number, { numero: number; nombre: string }>>((acc, a) => {
    if (!acc[a.unidadId]) acc[a.unidadId] = { numero: a.unidadNumero, nombre: a.unidadNombre }
    return acc
  }, {})

  return (
    <div className="space-y-5">
      <div className="flex justify-end">
        <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors">
          <Plus className="h-4 w-4" /> Agregar actividad
        </button>
      </div>

      {isLoading ? <LoadingSpinner className="py-10" /> : actividades.length === 0 ? (
        <div className="py-12 text-center text-slate-400 text-sm">No hay actividades registradas en este grupo.</div>
      ) : (
        Object.entries(byUnidad).map(([unidadId, acts]) => {
          const uid = Number(unidadId)
          const totalPonderacion = acts.reduce((s, a) => s + a.ponderacion, 0)
          const info = unitLabels[uid]
          return (
            <div key={uid} className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
              <div className="flex items-center justify-between px-5 py-3 bg-slate-50 border-b border-slate-200">
                <h4 className="font-semibold text-slate-800">
                  Unidad {info?.numero}: {info?.nombre}
                </h4>
                <span className="text-xs text-slate-500">
                  Ponderación: <span className={`font-semibold ${totalPonderacion > 100 ? 'text-red-600' : totalPonderacion === 100 ? 'text-emerald-600' : 'text-amber-600'}`}>{totalPonderacion}%</span>
                </span>
              </div>

              {/* Progress bar */}
              <div className="px-5 pt-3">
                <div className="h-1.5 w-full bg-slate-100 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all ${totalPonderacion > 100 ? 'bg-red-500' : totalPonderacion === 100 ? 'bg-emerald-500' : 'bg-blue-500'}`}
                    style={{ width: `${Math.min(totalPonderacion, 100)}%` }}
                  />
                </div>
              </div>

              <div className="divide-y divide-slate-100">
                {acts.map((a) => (
                  <div key={a.id} className="flex items-center justify-between px-5 py-3">
                    <div>
                      <p className="text-sm font-medium text-slate-800">{a.nombre}</p>
                      <p className="text-xs text-slate-400">{a.ponderacion}% de la unidad</p>
                    </div>
                    <div className="flex items-center gap-1">
                      <button onClick={() => openEdit(a)} className="p-1.5 rounded-lg text-slate-400 hover:text-blue-600 hover:bg-blue-50 transition-colors">
                        <Pencil className="h-3.5 w-3.5" />
                      </button>
                      <button onClick={() => setDeleteTarget(a)} className="p-1.5 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-colors">
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )
        })
      )}

      <FormModal
        open={modalOpen}
        title={editTarget ? 'Editar actividad' : 'Nueva actividad'}
        onClose={closeModal}
        onSubmit={handleSubmit}
        loading={createMut.isPending || updateMut.isPending}
        submitLabel={editTarget ? 'Guardar cambios' : 'Crear actividad'}
      >
        <div className="space-y-4">
          {formError && <ErrorAlert message={formError} onClose={() => setFormError('')} />}
          {!editTarget && (
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">Unidad <span className="text-red-500">*</span></label>
              <select value={form.unidadId} onChange={(e) => setForm((p) => ({ ...p, unidadId: e.target.value }))} className={inputClass}>
                <option value="">Seleccionar unidad...</option>
                {unidades.map((u) => (
                  <option key={u.id} value={u.id}>Unidad {u.numero}: {u.nombre}</option>
                ))}
              </select>
            </div>
          )}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">Nombre <span className="text-red-500">*</span></label>
            <input type="text" value={form.nombre} onChange={(e) => setForm((p) => ({ ...p, nombre: e.target.value }))} placeholder="Ej. Examen parcial 1" className={inputClass} />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">Ponderación (%) <span className="text-red-500">*</span></label>
            <input type="number" min={1} max={100} value={form.ponderacion} onChange={(e) => setForm((p) => ({ ...p, ponderacion: e.target.value }))} placeholder="Ej. 30" className={inputClass} />
          </div>
        </div>
      </FormModal>

      <ConfirmDialog
        open={!!deleteTarget}
        title="Eliminar actividad"
        description={`¿Eliminar la actividad "${deleteTarget?.nombre}"? Se perderán todas sus calificaciones.`}
        confirmLabel="Eliminar"
        variant="destructive"
        loading={deleteMut.isPending}
        onConfirm={() => deleteTarget && deleteMut.mutate(deleteTarget.id)}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  )
}

// ─── Calificaciones Tab ───────────────────────────────────────────────────────

function CalificacionesTab({ grupo }: { grupo: GrupoResponse }) {
  const qc = useQueryClient()
  const grupoId = grupo.id

  const { data: reporte = [], isLoading } = useQuery({
    queryKey: ['reporte', grupoId],
    queryFn: () => getReporte(grupoId),
  })

  const { data: actividades = [] } = useQuery({
    queryKey: ['actividades', grupoId],
    queryFn: () => getActividades(grupoId),
  })

  // Local grades state: [inscripcionId][actividadGrupoId] = value
  const [grades, setGrades] = useState<Record<number, Record<number, string>>>({})
  const [saving, setSaving] = useState<Record<number, boolean>>({})
  const [saveErrors, setSaveErrors] = useState<Record<number, string>>({})

  const getGrade = (inscripcionId: number, actividadId: number): string => {
    const existing = reporte
      .find((r) => r.inscripcionId === inscripcionId)
      ?.unidades.flatMap((u) => u.desglose)
      .find((d) => d.actividadGrupoId === actividadId)
    const local = grades[inscripcionId]?.[actividadId]
    if (local !== undefined) return local
    return existing?.calificacion != null ? String(existing.calificacion) : ''
  }

  const setGrade = (inscripcionId: number, actividadId: number, value: string) => {
    setGrades((p) => ({ ...p, [inscripcionId]: { ...p[inscripcionId], [actividadId]: value } }))
  }

  const handleSaveUnidad = async (unidadId: number, _nombre: string) => {
    setSaving((p) => ({ ...p, [unidadId]: true }))
    setSaveErrors((p) => ({ ...p, [unidadId]: '' }))
    const actsByUnidad = actividades.filter((a: ActividadGrupoResponse) => a.unidadId === unidadId)
    const resultados = reporte.flatMap((r) =>
      actsByUnidad.map((a: ActividadGrupoResponse) => ({
        inscripcionId: r.inscripcionId,
        actividadGrupoId: a.id,
        calificacion: grades[r.inscripcionId]?.[a.id] !== undefined
          ? (grades[r.inscripcionId][a.id] === '' ? null : Number(grades[r.inscripcionId][a.id]))
          : (r.unidades.flatMap((u) => u.desglose).find((d) => d.actividadGrupoId === a.id)?.calificacion ?? null),
      }))
    )
    try {
      await guardarLote(grupoId, { grupoId, unidadId, resultados })
      qc.invalidateQueries({ queryKey: ['reporte', grupoId] })
    } catch (err) {
      const msg = axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al guardar.' : 'Error inesperado.'
      setSaveErrors((p) => ({ ...p, [unidadId]: msg }))
    } finally {
      setSaving((p) => ({ ...p, [unidadId]: false }))
    }
  }

  const unidades = [...new Map(
    actividades.map((a: ActividadGrupoResponse) => [a.unidadId, { id: a.unidadId, numero: a.unidadNumero, nombre: a.unidadNombre }])
  ).values()].sort((a, b) => a.numero - b.numero)

  if (isLoading) return <LoadingSpinner className="py-10" />
  if (reporte.length === 0) return <div className="py-12 text-center text-slate-400 text-sm">No hay alumnos inscritos en este grupo.</div>

  return (
    <div className="space-y-6">
      {unidades.map((u) => {
        const actsU = actividades.filter((a: ActividadGrupoResponse) => a.unidadId === u.id)
        if (actsU.length === 0) return null
        return (
          <div key={u.id} className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
            <div className="flex items-center justify-between px-5 py-3 bg-slate-50 border-b border-slate-200">
              <h4 className="font-semibold text-slate-800">Unidad {u.numero}: {u.nombre}</h4>
              <div className="flex items-center gap-3">
                {saveErrors[u.id] && <span className="text-xs text-red-600">{saveErrors[u.id]}</span>}
                <button
                  onClick={() => handleSaveUnidad(u.id, u.nombre)}
                  disabled={saving[u.id]}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 transition-colors"
                >
                  {saving[u.id] ? <LoadingSpinner size="sm" /> : <Save className="h-3 w-3" />}
                  Guardar
                </button>
              </div>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-slate-50/60 border-b border-slate-100">
                    <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-500">Alumno</th>
                    {actsU.map((a: ActividadGrupoResponse) => (
                      <th key={a.id} className="px-3 py-2.5 text-center text-xs font-semibold text-slate-500 min-w-[90px]">
                        <div>{a.nombre}</div>
                        <div className="font-normal text-slate-400">{a.ponderacion}%</div>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {reporte.map((r) => (
                    <tr key={r.inscripcionId} className="hover:bg-slate-50/60">
                      <td className="px-4 py-2.5 text-slate-700 font-medium">{r.alumnoNombre}</td>
                      {actsU.map((a: ActividadGrupoResponse) => (
                        <td key={a.id} className="px-3 py-2 text-center">
                          <input
                            type="number"
                            min={0}
                            max={100}
                            step={0.1}
                            value={getGrade(r.inscripcionId, a.id)}
                            onChange={(e) => setGrade(r.inscripcionId, a.id, e.target.value)}
                            className="w-20 text-center px-2 py-1 rounded border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-400 transition"
                            placeholder="—"
                          />
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )
      })}
    </div>
  )
}

// ─── Bonus Tab ────────────────────────────────────────────────────────────────

function BonusTab({ grupo }: { grupo: GrupoResponse }) {
  const qc = useQueryClient()
  const grupoId = grupo.id
  const [selectedInscripcionId, setSelectedInscripcionId] = useState<number | null>(null)
  const [bonusForm, setBonusForm] = useState({ tipo: 'unidad', unidadId: '', puntos: '', justificacion: '' })
  const [bonusError, setBonusError] = useState('')

  const { data: reporte = [], isLoading: reporteLoading } = useQuery({
    queryKey: ['reporte', grupoId],
    queryFn: () => getReporte(grupoId),
  })

  const { data: bonusList = [], isLoading: bonusLoading } = useQuery({
    queryKey: ['bonus', selectedInscripcionId],
    queryFn: () => getBonus(selectedInscripcionId!),
    enabled: !!selectedInscripcionId,
  })

  const invBonus = () => qc.invalidateQueries({ queryKey: ['bonus', selectedInscripcionId] })

  const createMut = useMutation({
    mutationFn: (data: Parameters<typeof createBonus>[1]) => createBonus(selectedInscripcionId!, data),
    onSuccess: () => { invBonus(); setBonusForm({ tipo: 'unidad', unidadId: '', puntos: '', justificacion: '' }); setBonusError('') },
    onError: (err) => setBonusError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al aplicar bonus.' : 'Error inesperado.'),
  })

  const handleBonus = () => {
    if (!bonusForm.puntos || isNaN(Number(bonusForm.puntos))) { setBonusError('Ingresa los puntos de bonus.'); return }
    if (!bonusForm.justificacion.trim()) { setBonusError('La justificación es requerida.'); return }
    createMut.mutate({
      inscripcionId: selectedInscripcionId!,
      unidadId: bonusForm.tipo === 'unidad' && bonusForm.unidadId ? Number(bonusForm.unidadId) : undefined,
      tipo: bonusForm.tipo,
      puntos: Number(bonusForm.puntos),
      justificacion: bonusForm.justificacion.trim(),
    })
  }

  // Get unique units
  const unidades = [...new Map(
    reporte.flatMap((r) => r.unidades).map((u) => [u.unidadId, { id: u.unidadId, numero: u.unidadNumero, nombre: u.unidadNombre }])
  ).values()]

  const inputClass = 'w-full px-3 py-2 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition'

  if (reporteLoading) return <LoadingSpinner className="py-10" />

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
      {/* Left: alumno list */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="px-5 py-3 border-b border-slate-100 bg-slate-50">
          <h4 className="font-semibold text-slate-800">Alumnos</h4>
        </div>
        <div className="divide-y divide-slate-100">
          {reporte.map((r) => (
            <button
              key={r.inscripcionId}
              onClick={() => setSelectedInscripcionId(r.inscripcionId)}
              className={`w-full text-left px-5 py-3 transition-colors ${selectedInscripcionId === r.inscripcionId ? 'bg-blue-50 border-l-2 border-blue-500' : 'hover:bg-slate-50'}`}
            >
              <p className="text-sm font-medium text-slate-800">{r.alumnoNombre}</p>
              <p className="text-xs text-slate-400">{r.alumnoMatricula}</p>
            </button>
          ))}
        </div>
      </div>

      {/* Right: bonus list + form */}
      <div className="space-y-4">
        {selectedInscripcionId ? (
          <>
            {/* Existing bonus */}
            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
              <div className="px-5 py-3 border-b border-slate-100 bg-slate-50">
                <h4 className="font-semibold text-slate-800 flex items-center gap-2">
                  <Gift className="h-4 w-4 text-violet-500" /> Bonus existentes
                </h4>
              </div>
              {bonusLoading ? <LoadingSpinner className="py-6" /> : bonusList.length === 0 ? (
                <div className="py-6 text-center text-slate-400 text-sm">Sin bonus registrados.</div>
              ) : (
                <div className="divide-y divide-slate-100">
                  {bonusList.map((b: BonusResponse) => (
                    <div key={b.id} className="px-5 py-3">
                      <div className="flex items-center justify-between mb-0.5">
                        <span className="text-sm font-medium text-violet-700">+{b.puntos} pts</span>
                        <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${b.tipo === 'materia' ? 'bg-violet-100 text-violet-600' : 'bg-blue-100 text-blue-600'}`}>
                          {b.tipo === 'materia' ? 'Materia' : 'Unidad'}
                        </span>
                      </div>
                      <p className="text-xs text-slate-500">{b.justificacion}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Add bonus form */}
            <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-5 space-y-3">
              <h4 className="font-semibold text-slate-800 text-sm">Agregar bonus</h4>
              {bonusError && <ErrorAlert message={bonusError} onClose={() => setBonusError('')} />}

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">Tipo</label>
                  <select value={bonusForm.tipo} onChange={(e) => setBonusForm((p) => ({ ...p, tipo: e.target.value, unidadId: '' }))} className={inputClass}>
                    <option value="unidad">Por unidad</option>
                    <option value="materia">Por materia</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">Puntos</label>
                  <input type="number" step={0.1} value={bonusForm.puntos} onChange={(e) => setBonusForm((p) => ({ ...p, puntos: e.target.value }))} placeholder="Ej. 5" className={inputClass} />
                </div>
              </div>

              {bonusForm.tipo === 'unidad' && (
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">Unidad</label>
                  <select value={bonusForm.unidadId} onChange={(e) => setBonusForm((p) => ({ ...p, unidadId: e.target.value }))} className={inputClass}>
                    <option value="">Seleccionar...</option>
                    {unidades.map((u) => (
                      <option key={u.id} value={u.id}>Unidad {u.numero}: {u.nombre}</option>
                    ))}
                  </select>
                </div>
              )}

              <div>
                <label className="block text-xs font-medium text-slate-600 mb-1">Justificación</label>
                <textarea value={bonusForm.justificacion} onChange={(e) => setBonusForm((p) => ({ ...p, justificacion: e.target.value }))} rows={2} placeholder="Motivo del bonus..." className={inputClass} />
              </div>

              <div className="flex justify-end">
                <button onClick={handleBonus} disabled={createMut.isPending} className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium text-white bg-violet-600 hover:bg-violet-700 disabled:opacity-50 transition-colors">
                  {createMut.isPending ? <LoadingSpinner size="sm" /> : <Plus className="h-4 w-4" />}
                  Aplicar bonus
                </button>
              </div>
            </div>
          </>
        ) : (
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm py-16 text-center text-slate-400 text-sm">
            Selecciona un alumno para gestionar sus bonus.
          </div>
        )}
      </div>
    </div>
  )
}

// ─── Reporte Tab ──────────────────────────────────────────────────────────────

function ReporteTab({ grupo }: { grupo: GrupoResponse }) {
  const qc = useQueryClient()
  const grupoId = grupo.id
  const [overrideTarget, setOverrideTarget] = useState<CalificacionFinalDto | null>(null)
  const [overrideForm, setOverrideForm] = useState({ calificacion: '', justificacion: '' })
  const [overrideError, setOverrideError] = useState('')
  const [cerrarOpen, setCerrarOpen] = useState(false)
  const [reabrirOpen, setRreabrirOpen] = useState(false)
  const [definitiveOpen, setDefinitiveOpen] = useState(false)
  const [downloadLoading, setDownloadLoading] = useState(false)

  const { data: reporte = [], isLoading } = useQuery({
    queryKey: ['reporte', grupoId],
    queryFn: () => getReporte(grupoId),
  })

  const invReporte = () => qc.invalidateQueries({ queryKey: ['reporte', grupoId] })
  const invGrupo = () => qc.invalidateQueries({ queryKey: ['miGrupo', grupoId] })

  const overrideMut = useMutation({
    mutationFn: ({ inscripcionId, data }: { inscripcionId: number; data: { calificacion: number | null; justificacion: string } }) =>
      aplicarOverride(inscripcionId, data),
    onSuccess: () => { invReporte(); setOverrideTarget(null); setOverrideError('') },
    onError: (err) => setOverrideError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al aplicar override.' : 'Error inesperado.'),
  })

  const cerrarMut = useMutation({ mutationFn: () => cerrarGrupoMaestro(grupoId), onSuccess: () => { invGrupo(); setCerrarOpen(false) } })
  const reabrirMut = useMutation({ mutationFn: () => reabrirGrupoMaestro(grupoId), onSuccess: () => { invGrupo(); setRreabrirOpen(false) } })
  const defMut = useMutation({ mutationFn: () => cerrarDefinitivamenteMaestro(grupoId), onSuccess: () => { invGrupo(); setDefinitiveOpen(false) } })

  const handleDownloadActa = async () => {
    setDownloadLoading(true)
    try { await descargarActaPdf(grupoId) } finally { setDownloadLoading(false) }
  }

  const handleOverride = () => {
    if (!overrideForm.justificacion.trim()) { setOverrideError('La justificación es requerida.'); return }
    overrideMut.mutate({
      inscripcionId: overrideTarget!.inscripcionId,
      data: {
        calificacion: overrideForm.calificacion !== '' ? Number(overrideForm.calificacion) : null,
        justificacion: overrideForm.justificacion.trim(),
      },
    })
  }

  if (isLoading) return <LoadingSpinner className="py-10" />

  return (
    <div className="space-y-5">
      {/* Action buttons */}
      <div className="flex items-center gap-3 flex-wrap">
        {grupo.estadoEvaluacion === 'ABIERTO' && (
          <button onClick={() => setCerrarOpen(true)} className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium text-amber-700 bg-amber-50 hover:bg-amber-100 border border-amber-200 transition-colors">
            <Lock className="h-4 w-4" /> Cerrar acta
          </button>
        )}
        {grupo.estadoEvaluacion === 'CERRADO' && (
          <button onClick={() => setRreabrirOpen(true)} className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium text-blue-700 bg-blue-50 hover:bg-blue-100 border border-blue-200 transition-colors">
            <LockOpen className="h-4 w-4" /> Reabrir acta
          </button>
        )}
        <button onClick={() => setDefinitiveOpen(true)} className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium text-red-700 bg-red-50 hover:bg-red-100 border border-red-200 transition-colors">
          <LockKeyhole className="h-4 w-4" /> Cerrar definitivamente
        </button>
        <button
          onClick={handleDownloadActa}
          disabled={downloadLoading}
          className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 border border-slate-200 transition-colors ml-auto disabled:opacity-50"
        >
          {downloadLoading ? <LoadingSpinner size="sm" /> : <FileDown className="h-4 w-4" />}
          Descargar acta PDF
        </button>
      </div>

      {/* Table */}
      {reporte.length === 0 ? (
        <div className="bg-white rounded-xl border border-slate-200 py-12 text-center text-slate-400 text-sm">Sin datos de reporte.</div>
      ) : (
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200">
                  <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500">Alumno</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500">Matrícula</th>
                  {reporte[0]?.unidades.map((u: ResultadoUnidadDto) => (
                    <th key={u.unidadId} className="px-4 py-3 text-center text-xs font-semibold text-slate-500">U{u.unidadNumero}</th>
                  ))}
                  <th className="px-4 py-3 text-center text-xs font-semibold text-slate-500">Cal. Final</th>
                  <th className="px-4 py-3 text-center text-xs font-semibold text-slate-500">Estado</th>
                  <th className="px-4 py-3 text-right text-xs font-semibold text-slate-500">Override</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {reporte.map((r) => (
                  <tr key={r.inscripcionId} className="hover:bg-slate-50/60">
                    <td className="px-4 py-3 font-medium text-slate-800">{r.alumnoNombre}</td>
                    <td className="px-4 py-3 text-slate-500">{r.alumnoMatricula}</td>
                    {r.unidades.map((u: ResultadoUnidadDto) => (
                      <td key={u.unidadId} className="px-4 py-3 text-center text-slate-700">{formatCalificacion(u.resultadoFinal)}</td>
                    ))}
                    <td className="px-4 py-3 text-center font-semibold text-slate-900">{formatCalificacion(r.calificacionFinal)}</td>
                    <td className="px-4 py-3 text-center"><StatusBadge estado={r.estado} /></td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => {
                          setOverrideTarget(r)
                          setOverrideForm({ calificacion: r.calificacionFinal != null ? String(r.calificacionFinal) : '', justificacion: r.overrideJustificacion ?? '' })
                          setOverrideError('')
                        }}
                        className="p-1.5 rounded-lg text-slate-400 hover:text-violet-600 hover:bg-violet-50 transition-colors"
                        title="Aplicar override"
                      >
                        <Pencil className="h-3.5 w-3.5" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Override modal */}
      <FormModal
        open={!!overrideTarget}
        title="Override de calificación"
        subtitle={`Alumno: ${overrideTarget?.alumnoNombre}`}
        onClose={() => setOverrideTarget(null)}
        onSubmit={handleOverride}
        loading={overrideMut.isPending}
        submitLabel="Aplicar override"
      >
        <div className="space-y-4">
          {overrideError && <ErrorAlert message={overrideError} onClose={() => setOverrideError('')} />}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">Calificación override</label>
            <input type="number" min={0} max={100} step={0.1} value={overrideForm.calificacion} onChange={(e) => setOverrideForm((p) => ({ ...p, calificacion: e.target.value }))} placeholder="Dejar vacío para quitar override" className="w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition" />
            <p className="text-xs text-slate-400 mt-1">Calificación calculada: {formatCalificacion(overrideTarget?.calificacionFinal)}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">Justificación <span className="text-red-500">*</span></label>
            <textarea value={overrideForm.justificacion} onChange={(e) => setOverrideForm((p) => ({ ...p, justificacion: e.target.value }))} rows={3} placeholder="Motivo del override..." className="w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition" />
          </div>
        </div>
      </FormModal>

      <ConfirmDialog
        open={cerrarOpen}
        title="Cerrar acta"
        description="¿Cerrar el acta de este grupo? No podrás editar calificaciones hasta que se reabra."
        confirmLabel="Cerrar acta"
        variant="warning"
        loading={cerrarMut.isPending}
        onConfirm={() => cerrarMut.mutate()}
        onCancel={() => setCerrarOpen(false)}
      />
      <ConfirmDialog
        open={reabrirOpen}
        title="Reabrir acta"
        description="¿Reabrir el acta de este grupo?"
        confirmLabel="Reabrir"
        loading={reabrirMut.isPending}
        onConfirm={() => reabrirMut.mutate()}
        onCancel={() => setRreabrirOpen(false)}
      />
      <ConfirmDialog
        open={definitiveOpen}
        title="Cerrar definitivamente"
        description="Esta acción cerrará el grupo de forma permanente e irreversible. ¿Continuar?"
        confirmLabel="Cerrar definitivamente"
        variant="destructive"
        loading={defMut.isPending}
        onConfirm={() => defMut.mutate()}
        onCancel={() => setDefinitiveOpen(false)}
      />
    </div>
  )
}

// ─── Main Component ───────────────────────────────────────────────────────────

const TABS: { key: Tab; label: string }[] = [
  { key: 'actividades', label: 'Actividades' },
  { key: 'calificaciones', label: 'Calificaciones' },
  { key: 'bonus', label: 'Bonus y Overrides' },
  { key: 'reporte', label: 'Reporte y Acta' },
]

export default function GrupoDetalle() {
  const { id } = useParams<{ id: string }>()
  const grupoId = Number(id)
  const [activeTab, setActiveTab] = useState<Tab>('actividades')

  const { data: grupo, isLoading, error } = useQuery({
    queryKey: ['miGrupo', grupoId],
    queryFn: () => getMiGrupo(grupoId),
    enabled: !!grupoId,
  })

  if (isLoading) return <LoadingSpinner className="py-20" size="lg" />
  if (error || !grupo) return <ErrorAlert message="No se pudo cargar el grupo." />

  return (
    <div>
      {/* Header */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 mb-6">
        <div className="flex items-start justify-between gap-4 flex-wrap">
          <div>
            <h1 className="text-xl font-bold text-slate-900">{grupo.materiaNombre}</h1>
            <p className="text-sm text-slate-500 mt-0.5">{grupo.materiaClave}</p>
            <div className="flex items-center gap-4 mt-3 text-sm text-slate-600">
              <span>Clave: <strong className="text-slate-800">{grupo.clave}</strong></span>
              <span>Semestre: <strong className="text-slate-800">{grupo.semestre}</strong></span>
            </div>
          </div>
          <StatusBadge estado={grupo.estadoEvaluacion} />
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 bg-slate-100 p-1 rounded-xl mb-6 overflow-x-auto">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setActiveTab(t.key)}
            className={`flex-1 min-w-max px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              activeTab === t.key ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-700'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {activeTab === 'actividades' && <ActividadesTab grupo={grupo} />}
      {activeTab === 'calificaciones' && <CalificacionesTab grupo={grupo} />}
      {activeTab === 'bonus' && <BonusTab grupo={grupo} />}
      {activeTab === 'reporte' && <ReporteTab grupo={grupo} />}
    </div>
  )
}
