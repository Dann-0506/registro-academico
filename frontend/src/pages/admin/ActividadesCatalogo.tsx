import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Pencil, Trash2, Plus, ToggleLeft, ToggleRight, Loader2 } from 'lucide-react'
import axios from 'axios'
import {
  getCatalogo, createCatalogo, updateCatalogo, toggleCatalogo, deleteCatalogo,
} from '@/api/actividadesCatalogo'
import type { ActividadCatalogoResponse } from '@/types'
import { DataTable } from '@/components/shared/DataTable'
import { FormModal } from '@/components/shared/FormModal'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { PageHeader } from '@/components/shared/PageHeader'
import { StatusBadge } from '@/components/shared/StatusBadge'
import { ErrorAlert } from '@/components/shared/ErrorAlert'

const emptyForm = { nombre: '', descripcion: '' }

export default function ActividadesCatalogo() {
  const qc = useQueryClient()
  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<ActividadCatalogoResponse | null>(null)
  const [form, setForm] = useState(emptyForm)
  const [formError, setFormError] = useState('')
  const [deleteTarget, setDeleteTarget] = useState<ActividadCatalogoResponse | null>(null)
  const [toggleError, setToggleError] = useState('')
  const [togglingId, setTogglingId] = useState<number | null>(null)

  const { data: actividades = [], isLoading } = useQuery({
    queryKey: ['catalogo-actividades'],
    queryFn: getCatalogo,
  })
  const invalidate = () => qc.invalidateQueries({ queryKey: ['catalogo-actividades'] })

  const createMut = useMutation({
    mutationFn: createCatalogo,
    onSuccess: () => { invalidate(); closeModal() },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al crear.' : 'Error inesperado.'),
  })

  const updateMut = useMutation({
    mutationFn: ({ id, data }: { id: number; data: typeof emptyForm }) => updateCatalogo(id, data),
    onSuccess: () => { invalidate(); closeModal() },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al actualizar.' : 'Error inesperado.'),
  })

  const toggleMut = useMutation({
    mutationFn: ({ id, activo }: { id: number; activo: boolean }) => toggleCatalogo(id, activo),
    onMutate: async ({ id, activo }) => {
      setTogglingId(id); setToggleError('')
      await qc.cancelQueries({ queryKey: ['catalogo-actividades'] })
      const previous = qc.getQueryData<ActividadCatalogoResponse[]>(['catalogo-actividades'])
      qc.setQueryData<ActividadCatalogoResponse[]>(['catalogo-actividades'],
        old => old?.map(a => a.id === id ? { ...a, activo } : a) ?? [])
      return { previous }
    },
    onError: (_e, _v, ctx) => { qc.setQueryData(['catalogo-actividades'], ctx?.previous); setToggleError('No se pudo cambiar el estado.') },
    onSettled: () => { setTogglingId(null); invalidate() },
  })

  const deleteMut = useMutation({
    mutationFn: (id: number) => deleteCatalogo(id),
    onSuccess: () => { invalidate(); setDeleteTarget(null) },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'No se puede eliminar.' : 'Error inesperado.'),
  })

  const openCreate = () => { setEditTarget(null); setForm(emptyForm); setFormError(''); setModalOpen(true) }
  const openEdit = (a: ActividadCatalogoResponse) => {
    setEditTarget(a); setForm({ nombre: a.nombre, descripcion: a.descripcion ?? '' }); setFormError(''); setModalOpen(true)
  }
  const closeModal = () => { setModalOpen(false); setEditTarget(null); setForm(emptyForm); setFormError('') }

  const handleSubmit = () => {
    if (!form.nombre.trim()) { setFormError('El nombre es obligatorio.'); return }
    const nombre = form.nombre.trim()
    const descripcion = form.descripcion.trim()
    if (editTarget) updateMut.mutate({ id: editTarget.id, data: { nombre, descripcion } })
    else createMut.mutate({ nombre, descripcion: descripcion || undefined })
  }

  const inputClass = 'w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition'
  const isPending = createMut.isPending || updateMut.isPending

  return (
    <div>
      <PageHeader
        title="Actividades"
        description="Define las actividades válidas que los maestros pueden asignar a sus grupos."
        action={
          <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors">
            <Plus className="h-4 w-4" /> Nueva actividad
          </button>
        }
      />

      {toggleError && <div className="mb-4"><ErrorAlert message={toggleError} onClose={() => setToggleError('')} /></div>}

      <DataTable<ActividadCatalogoResponse>
        data={actividades}
        isLoading={isLoading}
        keyExtractor={(a) => a.id}
        searchable
        searchKeys={['nombre']}
        searchPlaceholder="Buscar actividad..."
        emptyMessage="No hay actividades en el catálogo."
        columns={[
          { header: 'Nombre', accessor: 'nombre' },
          { header: 'Descripción', accessor: (a) => a.descripcion ?? '—' },
          { header: 'Estado', accessor: (a) => <StatusBadge estado={a.activo ? 'ACTIVO' : 'INACTIVO'} label={a.activo ? 'Activa' : 'Inactiva'} /> },
        ]}
        rowActions={(a) => (
          <div className="flex items-center justify-end gap-1">
            <button onClick={() => openEdit(a)} title="Editar"
              className="p-1.5 rounded-lg text-slate-400 hover:text-blue-600 hover:bg-blue-50 transition-colors">
              <Pencil className="h-4 w-4" />
            </button>
            <button
              onClick={() => toggleMut.mutate({ id: a.id, activo: !a.activo })}
              disabled={togglingId === a.id}
              title={a.activo ? 'Desactivar' : 'Activar'}
              className="p-1.5 rounded-lg text-slate-400 hover:text-amber-600 hover:bg-amber-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {togglingId === a.id
                ? <Loader2 className="h-4 w-4 animate-spin" />
                : a.activo ? <ToggleRight className="h-4 w-4" /> : <ToggleLeft className="h-4 w-4" />}
            </button>
            <button onClick={() => setDeleteTarget(a)} title="Eliminar"
              className="p-1.5 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-colors">
              <Trash2 className="h-4 w-4" />
            </button>
          </div>
        )}
      />

      <FormModal open={modalOpen} title={editTarget ? 'Editar actividad' : 'Nueva actividad'}
        subtitle={editTarget ? `Editando: ${editTarget.nombre}` : 'Agrega una actividad al catálogo del sistema.'}
        onClose={closeModal} onSubmit={handleSubmit} loading={isPending}
        submitLabel={editTarget ? 'Guardar cambios' : 'Crear actividad'}>
        <div className="space-y-4">
          {formError && <ErrorAlert message={formError} onClose={() => setFormError('')} />}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">
              Nombre <span className="text-red-500">*</span>
            </label>
            <input type="text" value={form.nombre}
              onChange={e => setForm(p => ({ ...p, nombre: e.target.value }))}
              placeholder="Ej. Examen parcial, Tarea, Proyecto final"
              className={inputClass} />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">Descripción</label>
            <textarea value={form.descripcion}
              onChange={e => setForm(p => ({ ...p, descripcion: e.target.value }))}
              placeholder="Descripción opcional de la actividad..."
              rows={3}
              className={inputClass + ' resize-none'} />
          </div>
        </div>
      </FormModal>

      <ConfirmDialog open={!!deleteTarget} title="Eliminar actividad del catálogo"
        description={`¿Eliminar "${deleteTarget?.nombre}" del catálogo? Solo es posible si no está en uso en ningún grupo.`}
        confirmLabel="Eliminar" variant="destructive" loading={deleteMut.isPending}
        onConfirm={() => deleteTarget && deleteMut.mutate(deleteTarget.id)}
        onCancel={() => setDeleteTarget(null)} />
    </div>
  )
}
