import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Pencil, Trash2, BookPlus } from 'lucide-react'
import axios from 'axios'
import { getMaterias, createMateria, updateMateria, deleteMateria } from '@/api/materias'
import type { MateriaResponse } from '@/types'
import { DataTable } from '@/components/shared/DataTable'
import { FormModal } from '@/components/shared/FormModal'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { PageHeader } from '@/components/shared/PageHeader'
import { ErrorAlert } from '@/components/shared/ErrorAlert'

const emptyCreateForm = { clave: '', nombre: '', totalUnidades: 1, nombresUnidades: [''] }
const emptyEditForm = { nombre: '' }

export default function Materias() {
  const qc = useQueryClient()

  const [createOpen, setCreateOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<MateriaResponse | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<MateriaResponse | null>(null)

  const [createForm, setCreateForm] = useState(emptyCreateForm)
  const [editForm, setEditForm] = useState(emptyEditForm)
  const [formError, setFormError] = useState('')

  const { data: materias = [], isLoading } = useQuery({
    queryKey: ['materias'],
    queryFn: getMaterias,
  })

  const invalidate = () => qc.invalidateQueries({ queryKey: ['materias'] })

  const createMut = useMutation({
    mutationFn: createMateria,
    onSuccess: () => { invalidate(); setCreateOpen(false); setCreateForm(emptyCreateForm); setFormError('') },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al crear materia.' : 'Error inesperado.'),
  })

  const updateMut = useMutation({
    mutationFn: ({ id, data }: { id: number; data: { nombre: string } }) => updateMateria(id, data),
    onSuccess: () => { invalidate(); setEditTarget(null); setFormError('') },
    onError: (err) => setFormError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al actualizar.' : 'Error inesperado.'),
  })

  const deleteMut = useMutation({
    mutationFn: (id: number) => deleteMateria(id),
    onSuccess: () => { invalidate(); setDeleteTarget(null) },
  })

  // Sync unit name inputs when totalUnidades changes
  useEffect(() => {
    const n = Math.max(1, Math.min(15, createForm.totalUnidades))
    setCreateForm((p) => {
      const current = p.nombresUnidades
      const next = Array.from({ length: n }, (_, i) => current[i] ?? '')
      return { ...p, nombresUnidades: next }
    })
  }, [createForm.totalUnidades])

  const handleCreate = () => {
    if (!createForm.clave.trim()) { setFormError('La clave es requerida.'); return }
    if (!createForm.nombre.trim()) { setFormError('El nombre es requerido.'); return }
    if (createForm.totalUnidades < 1 || createForm.totalUnidades > 15) { setFormError('El total de unidades debe ser entre 1 y 15.'); return }
    createMut.mutate({
      clave: createForm.clave.trim(),
      nombre: createForm.nombre.trim(),
      totalUnidades: createForm.totalUnidades,
      nombresUnidades: createForm.nombresUnidades.map((n) => n.trim()),
    })
  }

  const handleEdit = () => {
    if (!editForm.nombre.trim()) { setFormError('El nombre es requerido.'); return }
    if (editTarget) updateMut.mutate({ id: editTarget.id, data: { nombre: editForm.nombre.trim() } })
  }

  const openEdit = (m: MateriaResponse) => {
    setEditTarget(m)
    setEditForm({ nombre: m.nombre })
    setFormError('')
  }

  const inputClass = 'w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition'
  const readonlyClass = 'w-full px-4 py-2.5 rounded-lg border border-slate-200 bg-slate-50 text-sm text-slate-500 cursor-not-allowed'

  return (
    <div>
      <PageHeader
        title="Materias"
        description="Catálogo de materias con sus unidades temáticas."
        action={
          <button onClick={() => { setCreateForm(emptyCreateForm); setFormError(''); setCreateOpen(true) }} className="flex items-center gap-2 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors">
            <BookPlus className="h-4 w-4" />
            Nueva materia
          </button>
        }
      />

      <DataTable<MateriaResponse>
        data={materias}
        isLoading={isLoading}
        keyExtractor={(m) => m.id}
        searchable
        searchKeys={['clave', 'nombre']}
        searchPlaceholder="Buscar por clave o nombre..."
        emptyMessage="No hay materias registradas."
        columns={[
          { header: 'Clave', accessor: 'clave' },
          { header: 'Nombre', accessor: 'nombre' },
          { header: 'Total Unidades', accessor: 'totalUnidades' },
        ]}
        rowActions={(m) => (
          <div className="flex items-center justify-end gap-1">
            <button
              onClick={() => openEdit(m)}
              title="Editar nombre"
              className="p-1.5 rounded-lg text-slate-400 hover:text-blue-600 hover:bg-blue-50 transition-colors"
            >
              <Pencil className="h-4 w-4" />
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

      {/* Create Modal */}
      <FormModal
        open={createOpen}
        title="Nueva materia"
        subtitle="Define la clave, nombre y unidades temáticas."
        onClose={() => { setCreateOpen(false); setFormError('') }}
        onSubmit={handleCreate}
        loading={createMut.isPending}
        submitLabel="Crear materia"
        size="lg"
      >
        <div className="space-y-4">
          {formError && <ErrorAlert message={formError} onClose={() => setFormError('')} />}

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">
                Clave <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={createForm.clave}
                onChange={(e) => setCreateForm((p) => ({ ...p, clave: e.target.value }))}
                placeholder="Ej. MAT101"
                className={inputClass}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">
                Total de unidades <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                min={1}
                max={15}
                value={createForm.totalUnidades}
                onChange={(e) => setCreateForm((p) => ({ ...p, totalUnidades: parseInt(e.target.value) || 1 }))}
                className={inputClass}
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">
              Nombre <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={createForm.nombre}
              onChange={(e) => setCreateForm((p) => ({ ...p, nombre: e.target.value }))}
              placeholder="Ej. Matemáticas Avanzadas"
              className={inputClass}
            />
          </div>

          {createForm.nombresUnidades.length > 0 && (
            <div>
              <p className="text-sm font-medium text-slate-700 mb-2">Nombres de unidades</p>
              <div className="space-y-2">
                {createForm.nombresUnidades.map((nombre, i) => (
                  <div key={i} className="flex items-center gap-3">
                    <span className="text-xs font-medium text-slate-400 w-16 flex-shrink-0">Unidad {i + 1}</span>
                    <input
                      type="text"
                      value={nombre}
                      onChange={(e) => setCreateForm((p) => {
                        const arr = [...p.nombresUnidades]
                        arr[i] = e.target.value
                        return { ...p, nombresUnidades: arr }
                      })}
                      placeholder={`Nombre de la unidad ${i + 1}`}
                      className={inputClass}
                    />
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </FormModal>

      {/* Edit Modal */}
      <FormModal
        open={!!editTarget}
        title="Editar materia"
        subtitle="Solo se puede modificar el nombre. La clave y unidades son estructurales."
        onClose={() => { setEditTarget(null); setFormError('') }}
        onSubmit={handleEdit}
        loading={updateMut.isPending}
        submitLabel="Guardar cambios"
      >
        <div className="space-y-4">
          {formError && <ErrorAlert message={formError} onClose={() => setFormError('')} />}

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">Clave</label>
            <input type="text" value={editTarget?.clave ?? ''} readOnly className={readonlyClass} />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">
              Nombre <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={editForm.nombre}
              onChange={(e) => setEditForm({ nombre: e.target.value })}
              placeholder="Nombre de la materia"
              className={inputClass}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">Total de unidades</label>
            <input type="text" value={editTarget?.totalUnidades ?? ''} readOnly className={readonlyClass} />
          </div>

          {editTarget && editTarget.unidades.length > 0 && (
            <div>
              <p className="text-sm font-medium text-slate-700 mb-2">Unidades (solo lectura)</p>
              <div className="space-y-1.5">
                {editTarget.unidades.map((u) => (
                  <div key={u.id} className="flex items-center gap-3">
                    <span className="text-xs font-medium text-slate-400 w-16 flex-shrink-0">Unidad {u.numero}</span>
                    <input type="text" value={u.nombre} readOnly className={readonlyClass} />
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </FormModal>

      {/* Delete Confirm */}
      <ConfirmDialog
        open={!!deleteTarget}
        title="Eliminar materia"
        description={`¿Estás seguro de que deseas eliminar la materia "${deleteTarget?.nombre}" (${deleteTarget?.clave})? Esta acción no se puede deshacer.`}
        confirmLabel="Eliminar"
        variant="destructive"
        loading={deleteMut.isPending}
        onConfirm={() => deleteTarget && deleteMut.mutate(deleteTarget.id)}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  )
}
