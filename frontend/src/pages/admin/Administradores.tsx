import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Pencil, Trash2, KeyRound, ToggleLeft, ToggleRight, ShieldPlus } from 'lucide-react'
import axios from 'axios'
import {
  getAdmins, createAdmin, updateAdmin,
  toggleAdminEstado, resetAdminPassword, deleteAdmin,
} from '@/api/administradores'
import type { AdminResponse } from '@/types'
import { useAuthStore } from '@/store/authStore'
import { DataTable } from '@/components/shared/DataTable'
import { FormModal } from '@/components/shared/FormModal'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { PageHeader } from '@/components/shared/PageHeader'
import { StatusBadge } from '@/components/shared/StatusBadge'
import { ErrorAlert } from '@/components/shared/ErrorAlert'

const emptyForm = { nombre: '', email: '', numEmpleado: '' }

export default function Administradores() {
  const qc = useQueryClient()
  const { usuario } = useAuthStore()

  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<AdminResponse | null>(null)
  const [form, setForm] = useState(emptyForm)
  const [formError, setFormError] = useState('')

  const [deleteTarget, setDeleteTarget] = useState<AdminResponse | null>(null)
  const [resetTarget, setResetTarget] = useState<AdminResponse | null>(null)

  const { data: admins = [], isLoading } = useQuery({
    queryKey: ['administradores'],
    queryFn: getAdmins,
  })

  const invalidate = () => qc.invalidateQueries({ queryKey: ['administradores'] })

  const createMut = useMutation({
    mutationFn: createAdmin,
    onSuccess: () => { invalidate(); closeModal() },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al crear administrador.' : 'Error inesperado.'),
  })

  const updateMut = useMutation({
    mutationFn: ({ id, data }: { id: number; data: typeof emptyForm }) => updateAdmin(id, data),
    onSuccess: () => { invalidate(); closeModal() },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al actualizar.' : 'Error inesperado.'),
  })

  const toggleMut = useMutation({
    mutationFn: ({ id, activo }: { id: number; activo: boolean }) => toggleAdminEstado(id, activo),
    onSuccess: invalidate,
  })

  const resetMut = useMutation({
    mutationFn: (id: number) => resetAdminPassword(id),
    onSuccess: () => setResetTarget(null),
  })

  const deleteMut = useMutation({
    mutationFn: (id: number) => deleteAdmin(id),
    onSuccess: () => { invalidate(); setDeleteTarget(null) },
  })

  const openCreate = () => { setEditTarget(null); setForm(emptyForm); setFormError(''); setModalOpen(true) }
  const openEdit = (a: AdminResponse) => {
    setEditTarget(a)
    setForm({ nombre: a.nombre, email: a.email ?? '', numEmpleado: a.numEmpleado })
    setFormError('')
    setModalOpen(true)
  }
  const closeModal = () => { setModalOpen(false); setEditTarget(null); setForm(emptyForm); setFormError('') }

  const handleSubmit = () => {
    if (!form.nombre.trim()) { setFormError('El nombre es requerido.'); return }
    if (!form.email.trim()) { setFormError('El correo es requerido.'); return }
    if (!form.numEmpleado.trim()) { setFormError('El número de empleado es requerido.'); return }
    const data = { nombre: form.nombre.trim(), email: form.email.trim(), numEmpleado: form.numEmpleado.trim() }
    if (editTarget) updateMut.mutate({ id: editTarget.id, data })
    else createMut.mutate(data)
  }

  const isPending = createMut.isPending || updateMut.isPending

  const isSelf = (admin: AdminResponse) => admin.email === usuario?.email

  return (
    <div>
      <PageHeader
        title="Administradores"
        description="Gestión de cuentas de administrador del sistema."
        action={
          <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors">
            <ShieldPlus className="h-4 w-4" />
            Nuevo administrador
          </button>
        }
      />

      <DataTable<AdminResponse>
        data={admins}
        isLoading={isLoading}
        keyExtractor={(a) => a.id}
        searchable
        searchKeys={['nombre', 'numEmpleado', 'email']}
        searchPlaceholder="Buscar por nombre, núm. empleado o correo..."
        emptyMessage="No hay administradores registrados."
        columns={[
          { header: 'Núm. Empleado', accessor: 'numEmpleado' },
          {
            header: 'Nombre',
            accessor: (a) => (
              <span className="flex items-center gap-2">
                {a.nombre}
                {isSelf(a) && (
                  <span className="text-xs text-blue-600 bg-blue-50 border border-blue-200 px-1.5 py-0.5 rounded-full">Tú</span>
                )}
              </span>
            ),
          },
          { header: 'Correo', accessor: 'email' },
          {
            header: 'Estado',
            accessor: (a) => <StatusBadge estado={a.activo ? 'ACTIVO' : 'INACTIVO'} label={a.activo ? 'Activo' : 'Inactivo'} />,
          },
        ]}
        rowActions={(a) => {
          const self = isSelf(a)
          const disabledClass = 'opacity-30 cursor-not-allowed'
          const enabledClass = 'text-slate-400 hover:text-blue-600 hover:bg-blue-50'

          return (
            <div className="flex items-center justify-end gap-1">
              <button
                onClick={() => !self && openEdit(a)}
                disabled={self}
                title={self ? 'No puedes actuar sobre tu propia cuenta' : 'Editar'}
                className={`p-1.5 rounded-lg transition-colors ${self ? disabledClass : enabledClass}`}
              >
                <Pencil className="h-4 w-4" />
              </button>
              <button
                onClick={() => !self && toggleMut.mutate({ id: a.id, activo: !a.activo })}
                disabled={self}
                title={self ? 'No puedes actuar sobre tu propia cuenta' : (a.activo ? 'Desactivar' : 'Activar')}
                className={`p-1.5 rounded-lg transition-colors ${self ? disabledClass : 'text-slate-400 hover:text-amber-600 hover:bg-amber-50'}`}
              >
                {a.activo ? <ToggleRight className="h-4 w-4" /> : <ToggleLeft className="h-4 w-4" />}
              </button>
              <button
                onClick={() => !self && setResetTarget(a)}
                disabled={self}
                title={self ? 'No puedes actuar sobre tu propia cuenta' : 'Restablecer contraseña'}
                className={`p-1.5 rounded-lg transition-colors ${self ? disabledClass : 'text-slate-400 hover:text-violet-600 hover:bg-violet-50'}`}
              >
                <KeyRound className="h-4 w-4" />
              </button>
              <button
                onClick={() => !self && setDeleteTarget(a)}
                disabled={self}
                title={self ? 'No puedes actuar sobre tu propia cuenta' : 'Eliminar'}
                className={`p-1.5 rounded-lg transition-colors ${self ? disabledClass : 'text-slate-400 hover:text-red-600 hover:bg-red-50'}`}
              >
                <Trash2 className="h-4 w-4" />
              </button>
            </div>
          )
        }}
      />

      {/* Create / Edit Modal */}
      <FormModal
        open={modalOpen}
        title={editTarget ? 'Editar administrador' : 'Nuevo administrador'}
        subtitle={editTarget ? `Editando: ${editTarget.nombre}` : 'Completa los datos del nuevo administrador.'}
        onClose={closeModal}
        onSubmit={handleSubmit}
        loading={isPending}
        submitLabel={editTarget ? 'Guardar cambios' : 'Crear administrador'}
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
              placeholder="Ej. Carlos Ramírez Torres"
              className="w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">
              Correo electrónico <span className="text-red-500">*</span>
            </label>
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm((p) => ({ ...p, email: e.target.value }))}
              placeholder="admin@escuela.edu"
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
              placeholder="Ej. ADM001"
              className="w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition"
            />
          </div>
        </div>
      </FormModal>

      {/* Delete Confirm */}
      <ConfirmDialog
        open={!!deleteTarget}
        title="Eliminar administrador"
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
