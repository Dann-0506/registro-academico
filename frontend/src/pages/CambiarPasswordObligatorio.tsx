import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { cambiarPassword } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'
import { LoadingSpinner } from '@/components/shared/LoadingSpinner'
import { ErrorAlert } from '@/components/shared/ErrorAlert'
import { ShieldAlert, Eye, EyeOff } from 'lucide-react'
import axios from 'axios'

export default function CambiarPasswordObligatorio() {
  const navigate = useNavigate()
  const { usuario, clearPasswordFlag } = useAuthStore()
  const [form, setForm] = useState({ passwordActual: '', passwordNueva: '', passwordConfirmar: '' })
  const [showPass, setShowPass] = useState({ actual: false, nueva: false, confirmar: false })
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: cambiarPassword,
    onSuccess: () => {
      clearPasswordFlag()
      const redirects = { admin: '/admin', maestro: '/maestro', alumno: '/alumno' }
      navigate(redirects[usuario!.rol], { replace: true })
    },
    onError: (err) => {
      setError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al cambiar la contraseña.' : 'Error inesperado.')
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    mutation.mutate(form)
  }

  const toggleEye = (field: keyof typeof showPass) =>
    setShowPass(p => ({ ...p, [field]: !p[field] }))

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-blue-950 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-2xl shadow-2xl overflow-hidden">
          {/* Header */}
          <div className="bg-gradient-to-r from-amber-500 to-orange-500 px-8 py-7 text-white text-center">
            <div className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-white/20 backdrop-blur mb-3">
              <ShieldAlert className="h-7 w-7" />
            </div>
            <h1 className="text-xl font-bold">Cambio de contraseña requerido</h1>
            <p className="text-amber-100 text-sm mt-1">
              Por seguridad, debes establecer una nueva contraseña antes de continuar.
            </p>
          </div>

          {/* Info */}
          <div className="mx-6 mt-5 rounded-lg bg-amber-50 border border-amber-200 px-4 py-3 text-sm text-amber-800">
            Tu contraseña temporal es tu{' '}
            <strong>{usuario?.rol === 'alumno' ? 'número de control' : 'número de empleado'}</strong>.
            Introdúcela como contraseña actual.
          </div>

          {/* Form */}
          <div className="px-8 py-6">
            {error && <div className="mb-4"><ErrorAlert message={error} onClose={() => setError('')} /></div>}

            <form onSubmit={handleSubmit} className="space-y-4">
              {[
                { id: 'passwordActual', label: 'Contraseña actual (temporal)', key: 'actual' as const },
                { id: 'passwordNueva', label: 'Nueva contraseña', key: 'nueva' as const },
                { id: 'passwordConfirmar', label: 'Confirmar nueva contraseña', key: 'confirmar' as const },
              ].map(({ id, label, key }) => (
                <div key={id}>
                  <label className="block text-sm font-medium text-slate-700 mb-1.5">{label}</label>
                  <div className="relative">
                    <input
                      type={showPass[key] ? 'text' : 'password'}
                      required
                      minLength={id === 'passwordActual' ? 1 : 6}
                      value={form[id as keyof typeof form]}
                      onChange={e => setForm(p => ({ ...p, [id]: e.target.value }))}
                      className="w-full px-4 py-2.5 pr-10 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-amber-500/30 focus:border-amber-500 transition"
                    />
                    <button
                      type="button"
                      onClick={() => toggleEye(key)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                    >
                      {showPass[key] ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </button>
                  </div>
                </div>
              ))}

              <button
                type="submit"
                disabled={mutation.isPending}
                className="w-full mt-2 py-2.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-white text-sm font-semibold flex items-center justify-center gap-2 disabled:opacity-60 transition-colors"
              >
                {mutation.isPending && <LoadingSpinner size="sm" />}
                {mutation.isPending ? 'Guardando...' : 'Establecer nueva contraseña'}
              </button>
            </form>
          </div>
        </div>

        <p className="text-center text-slate-500 text-xs mt-4">
          SIRA · Sistema Institucional de Registro Académico
        </p>
      </div>
    </div>
  )
}
