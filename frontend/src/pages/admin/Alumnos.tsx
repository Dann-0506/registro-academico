import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Pencil, Trash2, KeyRound, ToggleLeft, ToggleRight, UserPlus } from 'lucide-react'
import axios from 'axios'
import {
  getAlumnos, createAlumno, updateAlumno,
  toggleAlumnoEstado, resetAlumnoPassword, deleteAlumno,
} from '@/api/alumnos'
import type { AlumnoResponse } from '@/types'
import { DataTable } from '@/components/shared/DataTable'
import { FormModal } from '@/components/shared/FormModal'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { PageHeader } from '@/components/shared/PageHeader'
import { StatusBadge } from '@/components/shared/StatusBadge'
import { ErrorAlert } from '@/components/shared/ErrorAlert'

const emptyForm = { nombre: '', email: '', matricula: '' }

export default function Alumnos() {
  const qc = useQueryClient()
  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<AlumnoResponse | null>(null)
  const [form, setForm] = useState(emptyForm)
  const [formError, setFormError] = useState('')

  const [deleteTarget, setDeleteTarget] = useState<AlumnoResponse | null>(null)
  const [resetTarget, setResetTarget] = useState<AlumnoResponse | null>(null)

  const { data: alumnos = [], isLoading } = useQuery({
    queryKey: ['alumnos'],
    queryFn: getAlumnos,
  })

  const invalidate = () => qc.invalidateQueries({ queryKey: ['alumnos'] })

  const createMut = useMutation({
    mutationFn: createAlumno,
    onSuccess: () => { invalidate(); closeModal() },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al crear alumno.' : 'Error inesperado.'),
  })

  const updateMut = useMutation({
    mutationFn: ({ id, data }: { id: number; data: typeof emptyForm }) => updateAlumno(id, data),
    onSuccess: () => { invalidate(); closeModal() },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al actualizar.' : 'Error inesperado.'),
  })

  const toggleMut = useMutation({
    mutationFn: ({ id, activo }: { id: number; activo: boolean }) => toggleAlumnoEstado(id, activo),
    onSuccess: invalidate,
  })

  const resetMut = useMutation({
    mutationFn: (id: number) => resetAlumnoPassword(id),
    onSuccess: () => setResetTarget(null),
  })

  const deleteMut = useMutation({
    mutationFn: (id: number) => deleteAlumno(id),
    onSuccess: () => { invalidate(); setDeleteTarget(null) },
  })

  const openCreate = () => { setEditTarget(null); setForm(emptyForm); setFormError(''); setModalOpen(true) }
  const openEdit = (a: AlumnoResponse) => {
    setEditTarget(a)
    setForm({ nombre: a.nombre, email: a.email ?? '', matricula: a.matricula })
    setFormError('')
    setModalOpen(true)
  }
  const closeModal = () => { setModalOpen(false); setEditTarget(null); setForm(emptyForm); setFormError('') }

  const handleSubmit = () => {
    if (!form.nombre.trim()) { setFormError('El nombre es requerido.'); return }
    if (!form.matricula.trim()) { setFormError('La matrícula es requerida.'); return }
    const data = { nombre: form.nombre.trim(), email: form.email.trim() || undefined, matricula: form.matricula.trim() }
    if (editTarget) updateMut.mutate({ id: editTarget.id, data: data as typeof emptyForm })
    else createMut.mutate(data as typeof emptyForm)
  }

  const isPending = createMut.isPending || updateMut.isPending

  return (
    <div>
      <PageHeader
        title="Alumnos"
        description="Gestión de alumnos registrados en el sistema."
        action={
          <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors">
            <UserPlus className="h-4 w-4" />
            Nuevo alumno
          </button>
        }
      />

      <DataTable<AlumnoResponse>
        data={alumnos}
        isLoading={isLoading}
        keyExtractor={(a) => a.id}
        searchable
        searchKeys={['nombre', 'matricula', 'email']}
        searchPlaceholder="Buscar por nombre, matrícula o correo..."
        emptyMessage="No hay alumnos registrados."
        columns={[
          { header: 'Matrícula', accessor: 'matricula' },
          { header: 'Nombre', accessor: 'nombre' },
          { header: 'Correo', accessor: (a) => a.email ?? '—' },
          {
            header: 'Estado',
            accessor: (a) => <StatusBadge estado={a.activo ? 'ACTIVO' : 'INACTIVO'} label={a.activo ? 'Activo' : 'Inactivo'} />,
          },
        ]}
        rowActions={(a) => (
          <div className="flex items-center justify-end gap-1">
            <button
              onClick={() => openEdit(a)}
              title="Editar"
              className="p-1.5 rounded-lg text-slate-400 hover:text-blue-600 hover:bg-blue-50 transition-colors"
            >
              <Pencil className="h-4 w-4" />
            </button>
            <button
              onClick={() => toggleMut.mutate({ id: a.id, activo: !a.activo })}
              title={a.activo ? 'Desactivar' : 'Activar'}
              className="p-1.5 rounded-lg text-slate-400 hover:text-amber-600 hover:bg-amber-50 transition-colors"
            >
              {a.activo ? <ToggleRight className="h-4 w-4" /> : <ToggleLeft className="h-4 w-4" />}
            </button>
            <button
              onClick={() => setResetTarget(a)}
              title="Restablecer contraseña"
              className="p-1.5 rounded-lg text-slate-400 hover:text-violet-600 hover:bg-violet-50 transition-colors"
            >
              <KeyRound className="h-4 w-4" />
            </button>
            <button
              onClick={() => setDeleteTarget(a)}
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
        title={editTarget ? 'Editar alumno' : 'Nuevo alumno'}
        subtitle={editTarget ? `Editando: ${editTarget.nombre}` : 'Completa los datos del nuevo alumno.'}
        onClose={closeModal}
        onSubmit={handleSubmit}
        loading={isPending}
        submitLabel={editTarget ? 'Guardar cambios' : 'Crear alumno'}
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
              placeholder="Ej. Juan Pérez García"
              className="w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">Correo electrónico</label>
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm((p) => ({ ...p, email: e.target.value }))}
              placeholder="alumno@escuela.edu (opcional)"
              className="w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">
              Matrícula <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={form.matricula}
              onChange={(e) => setForm((p) => ({ ...p, matricula: e.target.value }))}
              placeholder="Ej. A12345678"
              className="w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition"
            />
          </div>
        </div>
      </FormModal>

      {/* Delete Confirm */}
      <ConfirmDialog
        open={!!deleteTarget}
        title="Eliminar alumno"
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
        description={`Se restablecerá la contraseña de "${resetTarget?.nombre}" a su matrícula. ¿Continuar?`}
        confirmLabel="Restablecer"
        variant="warning"
        loading={resetMut.isPending}
        onConfirm={() => resetTarget && resetMut.mutate(resetTarget.id)}
        onCancel={() => setResetTarget(null)}
      />
    </div>
  )
}
