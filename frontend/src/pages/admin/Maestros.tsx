import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Pencil, Trash2, KeyRound, ToggleLeft, ToggleRight, UserPlus, Loader2 } from 'lucide-react'
import axios from 'axios'
import {
  getMaestros, createMaestro, updateMaestro,
  toggleMaestroEstado, resetMaestroPassword, deleteMaestro,
} from '@/api/maestros'
import type { MaestroResponse } from '@/types'
import { DataTable } from '@/components/shared/DataTable'
import { FormModal } from '@/components/shared/FormModal'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { PageHeader } from '@/components/shared/PageHeader'
import { StatusBadge } from '@/components/shared/StatusBadge'
import { ErrorAlert } from '@/components/shared/ErrorAlert'

const emptyForm = { nombre: '', email: '', numEmpleado: '' }

export default function Maestros() {
  const qc = useQueryClient()
  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<MaestroResponse | null>(null)
  const [form, setForm] = useState(emptyForm)
  const [formError, setFormError] = useState('')

  const [deleteTarget, setDeleteTarget] = useState<MaestroResponse | null>(null)
  const [resetTarget, setResetTarget] = useState<MaestroResponse | null>(null)
  const [toggleError, setToggleError] = useState('')
  const [togglingId, setTogglingId] = useState<number | null>(null)

  const { data: maestros = [], isLoading } = useQuery({
    queryKey: ['maestros'],
    queryFn: getMaestros,
  })

  const invalidate = () => qc.invalidateQueries({ queryKey: ['maestros'] })

  const createMut = useMutation({
    mutationFn: createMaestro,
    onSuccess: () => { invalidate(); closeModal() },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al crear maestro.' : 'Error inesperado.'),
  })

  const updateMut = useMutation({
    mutationFn: ({ id, data }: { id: number; data: typeof emptyForm }) => updateMaestro(id, data),
    onSuccess: () => { invalidate(); closeModal() },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al actualizar.' : 'Error inesperado.'),
  })

  const toggleMut = useMutation({
    mutationFn: ({ id, activo }: { id: number; activo: boolean }) => toggleMaestroEstado(id, activo),
    onMutate: async ({ id, activo }) => {
      setTogglingId(id)
      setToggleError('')
      await qc.cancelQueries({ queryKey: ['maestros'] })
      const previous = qc.getQueryData<MaestroResponse[]>(['maestros'])
      qc.setQueryData<MaestroResponse[]>(['maestros'], old => old?.map(m => m.id === id ? { ...m, activo } : m) ?? [])
      return { previous }
    },
    onError: (_err, _vars, context) => {
      qc.setQueryData(['maestros'], context?.previous)
      setToggleError('No se pudo cambiar el estado. Intenta de nuevo.')
    },
    onSettled: () => {
      setTogglingId(null)
      qc.invalidateQueries({ queryKey: ['maestros'] })
    },
  })

  const resetMut = useMutation({
    mutationFn: (id: number) => resetMaestroPassword(id),
    onSuccess: () => setResetTarget(null),
  })

  const deleteMut = useMutation({
    mutationFn: (id: number) => deleteMaestro(id),
    onSuccess: () => { invalidate(); setDeleteTarget(null) },
  })

  const openCreate = () => { setEditTarget(null); setForm(emptyForm); setFormError(''); setModalOpen(true) }
  const openEdit = (m: MaestroResponse) => {
    setEditTarget(m)
    setForm({ nombre: m.nombre, email: m.email ?? '', numEmpleado: m.numEmpleado })
    setFormError('')
    setModalOpen(true)
  }
  const closeModal = () => { setModalOpen(false); setEditTarget(null); setForm(emptyForm); setFormError('') }

  const handleSubmit = () => {
    if (!form.nombre.trim()) { setFormError('El nombre es requerido.'); return }
    if (!form.numEmpleado.trim()) { setFormError('El número de empleado es requerido.'); return }
    const data = { nombre: form.nombre.trim(), email: form.email.trim() || undefined, numEmpleado: form.numEmpleado.trim() }
    if (editTarget) updateMut.mutate({ id: editTarget.id, data: data as typeof emptyForm })
    else createMut.mutate(data as typeof emptyForm)
  }

  const isPending = createMut.isPending || updateMut.isPending

  return (
    <div>
      <PageHeader
        title="Maestros"
        description="Gestión de maestros registrados en el sistema."
        action={
          <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors">
            <UserPlus className="h-4 w-4" />
            Nuevo maestro
          </button>
        }
      />

      {toggleError && (
        <div className="mb-4">
          <ErrorAlert message={toggleError} onClose={() => setToggleError('')} />
        </div>
      )}

      <DataTable<MaestroResponse>
        data={maestros}
        isLoading={isLoading}
        keyExtractor={(m) => m.id}
        searchable
        searchKeys={['nombre', 'numEmpleado', 'email']}
        searchPlaceholder="Buscar por nombre, núm. empleado o correo..."
        emptyMessage="No hay maestros registrados."
        columns={[
          { header: 'Núm. Empleado', accessor: 'numEmpleado' },
          { header: 'Nombre', accessor: 'nombre' },
          { header: 'Correo', accessor: (m) => m.email ?? '—' },
          {
            header: 'Estado',
            accessor: (m) => <StatusBadge estado={m.activo ? 'ACTIVO' : 'INACTIVO'} label={m.activo ? 'Activo' : 'Inactivo'} />,
          },
        ]}
        rowActions={(m) => (
          <div className="flex items-center justify-end gap-1">
            <button
              onClick={() => openEdit(m)}
              title="Editar"
              className="p-1.5 rounded-lg text-slate-400 hover:text-blue-600 hover:bg-blue-50 transition-colors"
            >
              <Pencil className="h-4 w-4" />
            </button>
            <button
              onClick={() => toggleMut.mutate({ id: m.id, activo: !m.activo })}
              disabled={togglingId === m.id}
              title={m.activo ? 'Desactivar' : 'Activar'}
              className="p-1.5 rounded-lg text-slate-400 hover:text-amber-600 hover:bg-amber-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {togglingId === m.id
                ? <Loader2 className="h-4 w-4 animate-spin" />
                : m.activo ? <ToggleRight className="h-4 w-4" /> : <ToggleLeft className="h-4 w-4" />}
            </button>
            <button
              onClick={() => setResetTarget(m)}
              title="Restablecer contraseña"
              className="p-1.5 rounded-lg text-slate-400 hover:text-violet-600 hover:bg-violet-50 transition-colors"
            >
              <KeyRound className="h-4 w-4" />
            </button>
            <button
              onClick={() => setDeleteTarget(m)}
              title="Eliminar"
              className="p-1.5 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-colors"
            >
              <Trash2 className="h-4 w-4" />
            </button>
          </div>
        )}
      />

      {/* Create / Edit Modal */}
      <FormModal
        open={modalOpen}
        title={editTarget ? 'Editar maestro' : 'Nuevo maestro'}
        subtitle={editTarget ? `Editando: ${editTarget.nombre}` : 'Completa los datos del nuevo maestro.'}
        onClose={closeModal}
        onSubmit={handleSubmit}
        loading={isPending}
        submitLabel={editTarget ? 'Guardar cambios' : 'Crear maestro'}
      >
        <div className="space-y-4">
          {formError && <ErrorAlert message={formError} onClose={() => setFormError('')} />}

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">
              Nombre completo <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={form.nombre}
              onChange={(e) => setForm((p) => ({ ...p, nombre: e.target.value }))}
              placeholder="Ej. María González López"
              className="w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">Correo electrónico</label>
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm((p) => ({ ...p, email: e.target.value }))}
              placeholder="maestro@escuela.edu (opcional)"
              className="w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">
              Núm. Empleado <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={form.numEmpleado}
              onChange={(e) => setForm((p) => ({ ...p, numEmpleado: e.target.value }))}
              placeholder="Ej. E001234"
              className="w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition"
            />
          </div>
        </div>
      </FormModal>

      {/* Delete Confirm */}
      <ConfirmDialog
        open={!!deleteTarget}
        title="Eliminar maestro"
        description={`¿Estás seguro de que deseas eliminar a "${deleteTarget?.nombre}"? Esta acción no se puede deshacer.`}
        confirmLabel="Eliminar"
        variant="destructive"
        loading={deleteMut.isPending}
        onConfirm={() => deleteTarget && deleteMut.mutate(deleteTarget.id)}
        onCancel={() => setDeleteTarget(null)}
      />

      {/* Reset Password Confirm */}
      <ConfirmDialog
        open={!!resetTarget}
        title="Restablecer contraseña"
        description={`Se restablecerá la contraseña de "${resetTarget?.nombre}" a su número de empleado. ¿Continuar?`}
        confirmLabel="Restablecer"
        variant="warning"
        loading={resetMut.isPending}
        onConfirm={() => resetTarget && resetMut.mutate(resetTarget.id)}
        onCancel={() => setResetTarget(null)}
      />
    </div>
  )
}
