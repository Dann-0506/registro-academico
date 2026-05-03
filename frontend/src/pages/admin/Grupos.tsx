import { useState } from 'react'
import { useInvalidateDashboard } from '@/hooks/useInvalidateDashboard'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Pencil, Trash2, Plus, LockOpen, Lock, LockKeyhole, ChevronDown } from 'lucide-react'
import axios from 'axios'
import {
  getGrupos, createGrupo, updateGrupo,
  cerrarGrupo, reabrirGrupo, cerrarDefinitivamenteGrupo, deleteGrupo,
} from '@/api/grupos'
import { getMaterias } from '@/api/materias'
import { getMaestros } from '@/api/maestros'
import type { GrupoResponse } from '@/types'
import { estadoGrupo, estadoGrupoLabel, estadoGrupoColor } from '@/lib/utils'
import { DataTable } from '@/components/shared/DataTable'
import { FormModal } from '@/components/shared/FormModal'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { PageHeader } from '@/components/shared/PageHeader'
import { ErrorAlert } from '@/components/shared/ErrorAlert'

const emptyForm = {
  clave: '', semestre: '', materiaId: '', maestroId: '',
  calificacionMinimaAprobatoria: '60', calificacionMaxima: '100',
}
type FormState = typeof emptyForm

function EstadoGrupoBadge({ grupo }: { grupo: GrupoResponse }) {
  const estado = estadoGrupo(grupo.activo, grupo.estadoEvaluacion)
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${estadoGrupoColor(estado)}`}>
      {estadoGrupoLabel(estado)}
    </span>
  )
}

function ActionsDropdown({ grupo, onCerrar, onReabrir, onCerrarDef, onEliminar }: {
  grupo: GrupoResponse; onCerrar: () => void; onReabrir: () => void
  onCerrarDef: () => void; onEliminar: () => void
}) {
  const [open, setOpen] = useState(false)
  const estado = estadoGrupo(grupo.activo, grupo.estadoEvaluacion)
  if (estado === 'FINALIZADO') return null
  return (
    <div className="relative">
      <button onClick={() => setOpen(p => !p)} title="Más acciones"
        className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors">
        <ChevronDown className="h-4 w-4" />
      </button>
      {open && (
        <>
          <div className="fixed inset-0 z-10" onClick={() => setOpen(false)} />
          <div className="absolute right-0 z-20 mt-1 w-56 bg-white rounded-xl border border-slate-200 shadow-lg py-1 text-sm">
            {estado === 'EN_CURSO' && (
              <button onClick={() => { setOpen(false); onCerrar() }}
                className="flex items-center gap-2.5 w-full px-4 py-2 text-left text-amber-700 hover:bg-amber-50 transition-colors">
                <Lock className="h-3.5 w-3.5" /> Terminar evaluación
              </button>
            )}
            {estado === 'ACTA_CERRADA' && (
              <button onClick={() => { setOpen(false); onReabrir() }}
                className="flex items-center gap-2.5 w-full px-4 py-2 text-left text-blue-700 hover:bg-blue-50 transition-colors">
                <LockOpen className="h-3.5 w-3.5" /> Reabrir acta
              </button>
            )}
            <button onClick={() => { setOpen(false); onCerrarDef() }}
              className="flex items-center gap-2.5 w-full px-4 py-2 text-left text-red-700 hover:bg-red-50 transition-colors">
              <LockKeyhole className="h-3.5 w-3.5" /> Cerrar definitivamente
            </button>
            {estado === 'EN_CURSO' && (
              <>
                <div className="border-t border-slate-100 my-1" />
                <button onClick={() => { setOpen(false); onEliminar() }}
                  className="flex items-center gap-2.5 w-full px-4 py-2 text-left text-red-600 hover:bg-red-50 transition-colors">
                  <Trash2 className="h-3.5 w-3.5" /> Eliminar
                </button>
              </>
            )}
          </div>
        </>
      )}
    </div>
  )
}

export default function Grupos() {
  const qc = useQueryClient()
  const invalidateDashboard = useInvalidateDashboard()
  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<GrupoResponse | null>(null)
  const [form, setForm] = useState<FormState>(emptyForm)
  const [formError, setFormError] = useState('')
  const [deleteTarget, setDeleteTarget] = useState<GrupoResponse | null>(null)
  const [cerrarTarget, setCerrarTarget] = useState<GrupoResponse | null>(null)
  const [reabrirTarget, setRreabrirTarget] = useState<GrupoResponse | null>(null)
  const [cerrarDefTarget, setCerrarDefTarget] = useState<GrupoResponse | null>(null)

  const { data: grupos = [], isLoading } = useQuery({ queryKey: ['grupos'], queryFn: getGrupos })
  const { data: materias = [] } = useQuery({ queryKey: ['materias'], queryFn: getMaterias })
  const { data: maestros = [] } = useQuery({ queryKey: ['maestros'], queryFn: getMaestros })
  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['grupos'] })
    invalidateDashboard()
  }

  const createMut = useMutation({
    mutationFn: createGrupo, onSuccess: () => { invalidate(); closeModal() },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al crear grupo.' : 'Error inesperado.'),
  })
  const updateMut = useMutation({
    mutationFn: ({ id, data }: { id: number; data: object }) => updateGrupo(id, data),
    onSuccess: () => { invalidate(); closeModal() },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al actualizar.' : 'Error inesperado.'),
  })
  const cerrarMut = useMutation({ mutationFn: cerrarGrupo, onSuccess: () => { invalidate(); setCerrarTarget(null) } })
  const reabrirMut = useMutation({ mutationFn: reabrirGrupo, onSuccess: () => { invalidate(); setRreabrirTarget(null) } })
  const cerrarDefMut = useMutation({ mutationFn: cerrarDefinitivamenteGrupo, onSuccess: () => { invalidate(); setCerrarDefTarget(null) } })
  const deleteMut = useMutation({ mutationFn: deleteGrupo, onSuccess: () => { invalidate(); setDeleteTarget(null) } })

  const openCreate = () => { setEditTarget(null); setForm(emptyForm); setFormError(''); setModalOpen(true) }
  const openEdit = (g: GrupoResponse) => {
    setEditTarget(g)
    setForm({ clave: g.clave, semestre: g.semestre, materiaId: String(g.materiaId), maestroId: String(g.maestroId),
      calificacionMinimaAprobatoria: String(g.calificacionMinimaAprobatoria), calificacionMaxima: String(g.calificacionMaxima) })
    setFormError(''); setModalOpen(true)
  }
  const closeModal = () => { setModalOpen(false); setEditTarget(null); setForm(emptyForm); setFormError('') }

  const handleSubmit = () => {
    if (!form.clave.trim()) { setFormError('La clave es requerida.'); return }
    if (!form.semestre.trim()) { setFormError('El semestre es requerido.'); return }
    if (!form.materiaId) { setFormError('Selecciona una materia.'); return }
    if (!form.maestroId) { setFormError('Selecciona un maestro.'); return }
    const payload = {
      clave: form.clave.trim(), semestre: form.semestre.trim(),
      materiaId: Number(form.materiaId), maestroId: Number(form.maestroId),
      calificacionMinimaAprobatoria: Number(form.calificacionMinimaAprobatoria),
      calificacionMaxima: Number(form.calificacionMaxima),
    }
    if (editTarget) updateMut.mutate({ id: editTarget.id, data: payload })
    else createMut.mutate(payload)
  }

  const inputClass = 'w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition'
  const isPending = createMut.isPending || updateMut.isPending

  return (
    <div>
      <PageHeader title="Grupos" description="Gestión de grupos académicos y su estado de evaluación."
        action={
          <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors">
            <Plus className="h-4 w-4" /> Nuevo grupo
          </button>
        }
      />

      <DataTable<GrupoResponse>
        data={grupos} isLoading={isLoading} keyExtractor={(g) => g.id} searchable
        searchKeys={['clave', 'materiaNombre', 'maestroNombre', 'semestre']}
        searchPlaceholder="Buscar por clave, materia, maestro o semestre..."
        emptyMessage="No hay grupos registrados."
        columns={[
          { header: 'Clave', accessor: 'clave' },
          { header: 'Materia', accessor: 'materiaNombre' },
          { header: 'Maestro', accessor: 'maestroNombre' },
          { header: 'Semestre', accessor: 'semestre' },
          { header: 'Estado', accessor: (g) => <EstadoGrupoBadge grupo={g} /> },
        ]}
        rowActions={(g) => {
          const estado = estadoGrupo(g.activo, g.estadoEvaluacion)
          return (
            <div className="flex items-center justify-end gap-1">
              {estado !== 'FINALIZADO' && (
                <button onClick={() => openEdit(g)} title="Editar"
                  className="p-1.5 rounded-lg text-slate-400 hover:text-blue-600 hover:bg-blue-50 transition-colors">
                  <Pencil className="h-4 w-4" />
                </button>
              )}
              <ActionsDropdown grupo={g}
                onCerrar={() => setCerrarTarget(g)} onReabrir={() => setRreabrirTarget(g)}
                onCerrarDef={() => setCerrarDefTarget(g)} onEliminar={() => setDeleteTarget(g)}
              />
            </div>
          )
        }}
      />

      <FormModal open={modalOpen} title={editTarget ? 'Editar grupo' : 'Nuevo grupo'}
        subtitle={editTarget ? `Editando: ${editTarget.clave}` : 'Configura el nuevo grupo académico.'}
        onClose={closeModal} onSubmit={handleSubmit} loading={isPending}
        submitLabel={editTarget ? 'Guardar cambios' : 'Crear grupo'} size="lg">
        <div className="space-y-4">
          {formError && <ErrorAlert message={formError} onClose={() => setFormError('')} />}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">Clave <span className="text-red-500">*</span></label>
              <input type="text" value={form.clave} onChange={(e) => setForm(p => ({ ...p, clave: e.target.value }))} placeholder="Ej. GRP-A1" className={inputClass} />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">Semestre <span className="text-red-500">*</span></label>
              <input type="text" value={form.semestre} onChange={(e) => setForm(p => ({ ...p, semestre: e.target.value }))} placeholder="Ej. 2025-1" className={inputClass} />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">Materia <span className="text-red-500">*</span></label>
            <select value={form.materiaId} onChange={(e) => setForm(p => ({ ...p, materiaId: e.target.value }))} className={inputClass}>
              <option value="">Seleccionar materia...</option>
              {materias.map(m => <option key={m.id} value={m.id}>{m.clave} — {m.nombre}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">Maestro <span className="text-red-500">*</span></label>
            <select value={form.maestroId} onChange={(e) => setForm(p => ({ ...p, maestroId: e.target.value }))} className={inputClass}>
              <option value="">Seleccionar maestro...</option>
              {maestros.map(m => <option key={m.id} value={m.id}>{m.numEmpleado} — {m.nombre}</option>)}
            </select>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">Cal. mínima aprobatoria</label>
              <input type="number" min={0} max={100} value={form.calificacionMinimaAprobatoria} onChange={(e) => setForm(p => ({ ...p, calificacionMinimaAprobatoria: e.target.value }))} className={inputClass} />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">Calificación máxima</label>
              <input type="number" min={0} max={100} value={form.calificacionMaxima} onChange={(e) => setForm(p => ({ ...p, calificacionMaxima: e.target.value }))} className={inputClass} />
            </div>
          </div>
        </div>
      </FormModal>

      <ConfirmDialog open={!!cerrarTarget} title="Terminar evaluación"
        description={`¿Terminar la evaluación de "${cerrarTarget?.clave}"? Las calificaciones quedarán congeladas. Podrás reabrirlo si es necesario.`}
        confirmLabel="Terminar evaluación" variant="warning" loading={cerrarMut.isPending}
        onConfirm={() => cerrarTarget && cerrarMut.mutate(cerrarTarget.id)} onCancel={() => setCerrarTarget(null)} />

      <ConfirmDialog open={!!reabrirTarget} title="Reabrir acta del grupo"
        description={`¿Reabrir el acta de "${reabrirTarget?.clave}"? Las calificaciones históricas se borrarán y volverá a evaluación activa.`}
        confirmLabel="Reabrir" variant="default" loading={reabrirMut.isPending}
        onConfirm={() => reabrirTarget && reabrirMut.mutate(reabrirTarget.id)} onCancel={() => setRreabrirTarget(null)} />

      <ConfirmDialog open={!!cerrarDefTarget} title="Cerrar definitivamente"
        description={`Esta acción cerrará "${cerrarDefTarget?.clave}" de forma permanente. No se podrá reabrir ni editar. ¿Continuar?`}
        confirmLabel="Cerrar definitivamente" variant="destructive" loading={cerrarDefMut.isPending}
        onConfirm={() => cerrarDefTarget && cerrarDefMut.mutate(cerrarDefTarget.id)} onCancel={() => setCerrarDefTarget(null)} />

      <ConfirmDialog open={!!deleteTarget} title="Eliminar grupo"
        description={`¿Eliminar el grupo "${deleteTarget?.clave}"? Esta acción no se puede deshacer.`}
        confirmLabel="Eliminar" variant="destructive" loading={deleteMut.isPending}
        onConfirm={() => deleteTarget && deleteMut.mutate(deleteTarget.id)} onCancel={() => setDeleteTarget(null)} />
    </div>
  )
}
