import { useState, useEffect } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import { Settings, Save } from 'lucide-react'
import axios from 'axios'
import { getConfiguracion, updateConfiguracion } from '@/api/configuracion'
import { PageHeader } from '@/components/shared/PageHeader'
import { LoadingSpinner } from '@/components/shared/LoadingSpinner'
import { ErrorAlert } from '@/components/shared/ErrorAlert'

export default function Configuracion() {
  const [form, setForm] = useState({ minimaAprobatoria: 60, maxima: 100 })
  const [success, setSuccess] = useState(false)
  const [error, setError] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['configuracion'],
    queryFn: getConfiguracion,
  })

  useEffect(() => {
    if (data) setForm({ minimaAprobatoria: data.minimaAprobatoria, maxima: data.maxima })
  }, [data])

  const mutation = useMutation({
    mutationFn: updateConfiguracion,
    onSuccess: () => { setSuccess(true); setError('') },
    onError: (err) => {
      setError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al guardar la configuración.' : 'Error inesperado.')
      setSuccess(false)
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (form.minimaAprobatoria < 0 || form.minimaAprobatoria > form.maxima) {
      setError('La calificación mínima debe ser mayor a 0 y menor o igual a la máxima.')
      return
    }
    mutation.mutate({ minimaAprobatoria: form.minimaAprobatoria, maxima: form.maxima })
  }

  const inputClass = 'w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition'

  return (
    <div className="max-w-xl">
      <PageHeader
        title="Configuración del sistema"
        description="Parámetros globales que aplican a todos los grupos y evaluaciones."
      />

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="flex items-center gap-3 px-6 py-4 border-b border-slate-100 bg-slate-50">
          <Settings className="h-5 w-5 text-slate-400" />
          <h3 className="font-semibold text-slate-900">Parámetros de calificación</h3>
        </div>

        {isLoading ? (
          <LoadingSpinner className="py-16" size="lg" />
        ) : (
          <form onSubmit={handleSubmit} className="px-6 py-6 space-y-5">
            {success && (
              <div className="rounded-lg bg-emerald-50 border border-emerald-200 px-4 py-3 text-sm text-emerald-700">
                Configuración guardada correctamente.
              </div>
            )}
            {error && <ErrorAlert message={error} onClose={() => setError('')} />}

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">
                Calificación mínima aprobatoria
              </label>
              <input
                type="number"
                min={0}
                max={form.maxima}
                step={0.5}
                value={form.minimaAprobatoria}
                onChange={(e) => { setForm((p) => ({ ...p, minimaAprobatoria: parseFloat(e.target.value) || 0 })); setSuccess(false) }}
                className={inputClass}
              />
              <p className="text-xs text-slate-400 mt-1">
                Los alumnos con calificación final igual o mayor a este valor serán considerados aprobados.
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">
                Calificación máxima
              </label>
              <input
                type="number"
                min={1}
                max={1000}
                step={0.5}
                value={form.maxima}
                onChange={(e) => { setForm((p) => ({ ...p, maxima: parseFloat(e.target.value) || 100 })); setSuccess(false) }}
                className={inputClass}
              />
              <p className="text-xs text-slate-400 mt-1">
                Valor máximo alcanzable en la escala de calificaciones del sistema.
              </p>
            </div>

            <div className="flex justify-end pt-2">
              <button
                type="submit"
                disabled={mutation.isPending}
                className="flex items-center gap-2 px-5 py-2 rounded-lg text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 transition-colors"
              >
                {mutation.isPending ? <LoadingSpinner size="sm" /> : <Save className="h-4 w-4" />}
                Guardar configuración
              </button>
            </div>
          </form>
        )}
      </div>

      {data && (
        <div className="mt-4 px-4 py-3 bg-slate-50 rounded-lg border border-slate-200 text-xs text-slate-500">
          Valores actuales en el sistema: mínima aprobatoria = <strong>{data.minimaAprobatoria}</strong>, máxima = <strong>{data.maxima}</strong>
        </div>
      )}
    </div>
  )
}
