import { useState } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import { getPerfil, cambiarPassword } from '@/api/auth'
import { PageHeader } from '@/components/shared/PageHeader'
import { ErrorAlert } from '@/components/shared/ErrorAlert'
import { LoadingSpinner } from '@/components/shared/LoadingSpinner'
import { rolLabel } from '@/lib/utils'
import { User, Lock, ShieldCheck, Hash } from 'lucide-react'
import axios from 'axios'

export default function Perfil() {
  const { data: perfil, isLoading } = useQuery({ queryKey: ['perfil'], queryFn: getPerfil })
  const [form, setForm] = useState({ passwordActual: '', passwordNueva: '', passwordConfirmar: '' })
  const [success, setSuccess] = useState(false)
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: cambiarPassword,
    onSuccess: () => { setSuccess(true); setForm({ passwordActual: '', passwordNueva: '', passwordConfirmar: '' }); setError('') },
    onError: (err) => {
      setError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al cambiar la contraseña.' : 'Error inesperado.')
      setSuccess(false)
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    mutation.mutate(form)
  }

  if (isLoading) return <LoadingSpinner className="py-20" size="lg" />

  const labelFor: Record<string, string> = { admin: 'Núm. empleado', maestro: 'Núm. empleado', alumno: 'Matrícula' }

  return (
    <div className="max-w-2xl">
      <PageHeader title="Mi perfil" description="Información de tu cuenta y ajustes de seguridad." />

      {/* Info card */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden mb-6">
        <div className="bg-gradient-to-r from-slate-800 to-slate-900 px-6 py-5 flex items-center gap-4">
          <div className="flex h-14 w-14 items-center justify-center rounded-full bg-blue-500 text-white text-xl font-bold">
            {perfil?.nombre?.[0]?.toUpperCase()}
          </div>
          <div>
            <p className="text-white font-semibold text-lg">{perfil?.nombre}</p>
            <p className="text-slate-400 text-sm">{rolLabel(perfil?.rol ?? '')}</p>
          </div>
        </div>

        <div className="divide-y divide-slate-100">
          {[
            { icon: <User className="h-4 w-4" />, label: 'Nombre', value: perfil?.nombre },
            { icon: <span className="h-4 w-4 text-xs font-bold flex items-center">@</span>, label: 'Correo electrónico', value: perfil?.email },
            ...(perfil?.identificador ? [{ icon: <Hash className="h-4 w-4" />, label: labelFor[perfil.rol] ?? 'Identificador', value: perfil.identificador }] : []),
            { icon: <ShieldCheck className="h-4 w-4" />, label: 'Rol en el sistema', value: rolLabel(perfil?.rol ?? '') },
          ].map((item, i) => (
            <div key={i} className="flex items-center gap-4 px-6 py-4">
              <span className="text-slate-400">{item.icon}</span>
              <span className="text-sm text-slate-500 w-40">{item.label}</span>
              <span className="text-sm font-medium text-slate-800">{item.value}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Change password */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="flex items-center gap-3 px-6 py-4 border-b border-slate-100">
          <Lock className="h-5 w-5 text-slate-400" />
          <h3 className="font-semibold text-slate-900">Cambiar contraseña</h3>
        </div>

        <form onSubmit={handleSubmit} className="px-6 py-5 space-y-4">
          {success && (
            <div className="rounded-lg bg-emerald-50 border border-emerald-200 px-4 py-3 text-sm text-emerald-700">
              Contraseña actualizada correctamente.
            </div>
          )}
          {error && <ErrorAlert message={error} onClose={() => setError('')} />}

          {[
            { id: 'passwordActual', label: 'Contraseña actual' },
            { id: 'passwordNueva', label: 'Nueva contraseña' },
            { id: 'passwordConfirmar', label: 'Confirmar nueva contraseña' },
          ].map(({ id, label }) => (
            <div key={id}>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">{label}</label>
              <input
                type="password" required minLength={6}
                value={form[id as keyof typeof form]}
                onChange={e => setForm(p => ({ ...p, [id]: e.target.value }))}
                className="w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition"
              />
            </div>
          ))}

          <div className="flex justify-end pt-2">
            <button type="submit" disabled={mutation.isPending} className="px-5 py-2 rounded-lg text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2 transition-colors">
              {mutation.isPending && <LoadingSpinner size="sm" />}
              Actualizar contraseña
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
