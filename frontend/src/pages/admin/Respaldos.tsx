import { useState, useRef } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Download, Upload, AlertTriangle, CheckCircle2, X } from 'lucide-react'
import axios from 'axios'
import { descargarRespaldo, restaurarRespaldo } from '@/api/respaldos'
import { PageHeader } from '@/components/shared/PageHeader'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { LoadingSpinner } from '@/components/shared/LoadingSpinner'
import { ErrorAlert } from '@/components/shared/ErrorAlert'

export default function Respaldos() {
  const [archivoRestaurar, setArchivoRestaurar] = useState<File | null>(null)
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [restoreSuccess, setRestoreSuccess] = useState(false)
  const [restoreError, setRestoreError] = useState('')
  const [downloadError, setDownloadError] = useState('')
  const fileInputRef = useRef<HTMLInputElement>(null)

  const downloadMut = useMutation({
    mutationFn: descargarRespaldo,
    onError: (err) => setDownloadError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al generar el respaldo.' : 'Error inesperado.'),
  })

  const restoreMut = useMutation({
    mutationFn: () => restaurarRespaldo(archivoRestaurar!),
    onSuccess: () => { setRestoreSuccess(true); setRestoreError(''); setArchivoRestaurar(null); setConfirmOpen(false) },
    onError: (err) => {
      setRestoreError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al restaurar el respaldo.' : 'Error inesperado.')
      setConfirmOpen(false)
    },
  })

  const handleFileSelect = (file: File) => {
    setArchivoRestaurar(file)
    setRestoreSuccess(false)
    setRestoreError('')
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <PageHeader
        title="Respaldos de base de datos"
        description="Genera o restaura copias de seguridad de los datos del sistema."
      />

      {/* Download Card */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 bg-slate-50">
          <h3 className="font-semibold text-slate-900 flex items-center gap-2">
            <Download className="h-5 w-5 text-blue-500" />
            Generar respaldo
          </h3>
        </div>
        <div className="px-6 py-6">
          {downloadError && <div className="mb-4"><ErrorAlert message={downloadError} onClose={() => setDownloadError('')} /></div>}
          <p className="text-sm text-slate-600 mb-4">
            Descarga un archivo <code className="bg-slate-100 px-1.5 py-0.5 rounded text-xs font-mono">.sql</code> con
            la copia completa de la base de datos. Guárdala en un lugar seguro.
          </p>
          <button
            onClick={() => { setDownloadError(''); downloadMut.mutate() }}
            disabled={downloadMut.isPending}
            className="flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 transition-colors"
          >
            {downloadMut.isPending ? <LoadingSpinner size="sm" /> : <Download className="h-4 w-4" />}
            {downloadMut.isPending ? 'Generando...' : 'Descargar respaldo (.sql)'}
          </button>
        </div>
      </div>

      {/* Restore Card */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 bg-slate-50">
          <h3 className="font-semibold text-slate-900 flex items-center gap-2">
            <Upload className="h-5 w-5 text-amber-500" />
            Restaurar respaldo
          </h3>
        </div>
        <div className="px-6 py-6 space-y-4">
          {/* Warning */}
          <div className="flex items-start gap-3 rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            <AlertTriangle className="h-4 w-4 mt-0.5 flex-shrink-0 text-red-500" />
            <div>
              <p className="font-semibold mb-1">Advertencia: operación irreversible</p>
              <p>Restaurar un respaldo <strong>reemplazará todos los datos actuales</strong> con los del archivo seleccionado. Esta acción no se puede deshacer. Asegúrate de tener una copia reciente antes de continuar.</p>
            </div>
          </div>

          {restoreSuccess && (
            <div className="flex items-center gap-2 text-sm text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-lg px-4 py-3">
              <CheckCircle2 className="h-4 w-4 flex-shrink-0" />
              Base de datos restaurada correctamente.
            </div>
          )}

          {restoreError && <ErrorAlert message={restoreError} onClose={() => setRestoreError('')} />}

          {/* File Input */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">Archivo de respaldo (.sql)</label>
            <div
              onClick={() => fileInputRef.current?.click()}
              className={`border-2 border-dashed rounded-xl p-6 text-center cursor-pointer transition-colors ${
                archivoRestaurar ? 'border-amber-300 bg-amber-50' : 'border-slate-300 bg-slate-50 hover:border-amber-300 hover:bg-amber-50/40'
              }`}
            >
              <input
                ref={fileInputRef}
                type="file"
                accept=".sql"
                className="hidden"
                onChange={(e) => { const f = e.target.files?.[0]; if (f) handleFileSelect(f) }}
              />
              {archivoRestaurar ? (
                <div className="flex items-center justify-center gap-3">
                  <CheckCircle2 className="h-5 w-5 text-amber-500 flex-shrink-0" />
                  <div className="text-left">
                    <p className="text-sm font-medium text-amber-700">{archivoRestaurar.name}</p>
                    <p className="text-xs text-amber-600">{(archivoRestaurar.size / 1024).toFixed(1)} KB</p>
                  </div>
                  <button
                    type="button"
                    onClick={(e) => { e.stopPropagation(); setArchivoRestaurar(null) }}
                    className="ml-2 text-amber-400 hover:text-amber-600"
                  >
                    <X className="h-4 w-4" />
                  </button>
                </div>
              ) : (
                <div className="flex flex-col items-center gap-1.5 text-slate-400">
                  <Upload className="h-7 w-7" />
                  <p className="text-sm">Haz clic para seleccionar un archivo <code className="font-mono text-xs">.sql</code></p>
                </div>
              )}
            </div>
          </div>

          <div className="flex justify-end">
            <button
              onClick={() => setConfirmOpen(true)}
              disabled={!archivoRestaurar || restoreMut.isPending}
              className="flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-medium text-white bg-red-600 hover:bg-red-700 disabled:opacity-50 transition-colors"
            >
              {restoreMut.isPending ? <LoadingSpinner size="sm" /> : <Upload className="h-4 w-4" />}
              Restaurar base de datos
            </button>
          </div>
        </div>
      </div>

      {/* Confirm Restore */}
      <ConfirmDialog
        open={confirmOpen}
        title="Confirmar restauración"
        description={`¿Estás seguro de que deseas restaurar la base de datos con "${archivoRestaurar?.name}"? Todos los datos actuales serán reemplazados permanentemente.`}
        confirmLabel="Sí, restaurar"
        variant="destructive"
        loading={restoreMut.isPending}
        onConfirm={() => restoreMut.mutate()}
        onCancel={() => setConfirmOpen(false)}
      />
    </div>
  )
}
