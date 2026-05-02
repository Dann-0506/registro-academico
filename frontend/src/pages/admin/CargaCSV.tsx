import { useState, useRef } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Upload, FileUp, AlertCircle, CheckCircle2, X } from 'lucide-react'
import axios from 'axios'
import { importarCSV } from '@/api/carga'
import type { CargaResultadoResponse } from '@/types'
import { PageHeader } from '@/components/shared/PageHeader'
import { LoadingSpinner } from '@/components/shared/LoadingSpinner'
import { ErrorAlert } from '@/components/shared/ErrorAlert'

const TIPOS = [
  { value: 'alumnos', label: 'Alumnos' },
  { value: 'maestros', label: 'Maestros' },
  { value: 'materias', label: 'Materias' },
  { value: 'grupos', label: 'Grupos' },
  { value: 'inscripciones', label: 'Inscripciones' },
]

export default function CargaCSV() {
  const [tipo, setTipo] = useState('alumnos')
  const [archivo, setArchivo] = useState<File | null>(null)
  const [dragging, setDragging] = useState(false)
  const [resultado, setResultado] = useState<CargaResultadoResponse | null>(null)
  const [globalError, setGlobalError] = useState('')
  const fileInputRef = useRef<HTMLInputElement>(null)

  const mutation = useMutation({
    mutationFn: () => importarCSV(archivo!, tipo),
    onSuccess: (data) => { setResultado(data); setGlobalError('') },
    onError: (err) => {
      setGlobalError(axios.isAxiosError(err) ? err.response?.data?.error ?? 'Error al importar el archivo.' : 'Error inesperado.')
      setResultado(null)
    },
  })

  const handleFile = (file: File) => {
    if (!file.name.endsWith('.csv')) { setGlobalError('Solo se admiten archivos .csv'); return }
    setArchivo(file)
    setResultado(null)
    setGlobalError('')
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    setDragging(false)
    const file = e.dataTransfer.files[0]
    if (file) handleFile(file)
  }

  const handleImport = () => {
    if (!archivo) { setGlobalError('Selecciona un archivo CSV primero.'); return }
    mutation.mutate()
  }

  const inputClass = 'w-full px-4 py-2.5 rounded-lg border border-slate-300 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500 transition bg-white'

  return (
    <div className="max-w-2xl mx-auto">
      <PageHeader
        title="Carga masiva por CSV"
        description="Importa registros en bloque subiendo un archivo CSV con el formato requerido."
      />

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 space-y-5">
        {globalError && <ErrorAlert message={globalError} onClose={() => setGlobalError('')} />}

        {/* Tipo selector */}
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1.5">Tipo de datos a importar</label>
          <select value={tipo} onChange={(e) => { setTipo(e.target.value); setResultado(null) }} className={inputClass}>
            {TIPOS.map((t) => (
              <option key={t.value} value={t.value}>{t.label}</option>
            ))}
          </select>
        </div>

        {/* Drop zone */}
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1.5">Archivo CSV</label>
          <div
            onDragOver={(e) => { e.preventDefault(); setDragging(true) }}
            onDragLeave={() => setDragging(false)}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            className={`border-2 border-dashed rounded-xl p-8 text-center cursor-pointer transition-colors ${
              dragging ? 'border-blue-400 bg-blue-50' : archivo ? 'border-emerald-300 bg-emerald-50' : 'border-slate-300 bg-slate-50 hover:border-blue-300 hover:bg-blue-50/40'
            }`}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".csv"
              className="hidden"
              onChange={(e) => { const f = e.target.files?.[0]; if (f) handleFile(f) }}
            />
            {archivo ? (
              <div className="flex items-center justify-center gap-3">
                <CheckCircle2 className="h-6 w-6 text-emerald-500 flex-shrink-0" />
                <div className="text-left">
                  <p className="text-sm font-medium text-emerald-700">{archivo.name}</p>
                  <p className="text-xs text-emerald-600">{(archivo.size / 1024).toFixed(1)} KB</p>
                </div>
                <button
                  type="button"
                  onClick={(e) => { e.stopPropagation(); setArchivo(null); setResultado(null) }}
                  className="ml-2 text-emerald-400 hover:text-emerald-600"
                >
                  <X className="h-4 w-4" />
                </button>
              </div>
            ) : (
              <div className="flex flex-col items-center gap-2 text-slate-400">
                <FileUp className="h-8 w-8" />
                <p className="text-sm font-medium">Arrastra tu archivo CSV aquí</p>
                <p className="text-xs">o haz clic para seleccionarlo</p>
              </div>
            )}
          </div>
        </div>

        {/* Import button */}
        <div className="flex justify-end">
          <button
            onClick={handleImport}
            disabled={mutation.isPending || !archivo}
            className="flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 transition-colors"
          >
            {mutation.isPending ? <LoadingSpinner size="sm" /> : <Upload className="h-4 w-4" />}
            {mutation.isPending ? 'Importando...' : 'Importar'}
          </button>
        </div>
      </div>

      {/* Result */}
      {resultado && (
        <div className="mt-6 bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
          <div className="px-6 py-4 border-b border-slate-100 bg-slate-50">
            <h3 className="font-semibold text-slate-900">Resultado de la importación</h3>
          </div>

          <div className="px-6 py-5 space-y-4">
            {/* Summary */}
            <div className="grid grid-cols-3 gap-4">
              <div className="text-center p-3 rounded-lg bg-slate-50 border border-slate-200">
                <p className="text-2xl font-bold text-slate-800">{resultado.procesados}</p>
                <p className="text-xs text-slate-500 mt-0.5">Procesados</p>
              </div>
              <div className="text-center p-3 rounded-lg bg-emerald-50 border border-emerald-200">
                <p className="text-2xl font-bold text-emerald-700">{resultado.exitosos}</p>
                <p className="text-xs text-emerald-600 mt-0.5">Exitosos</p>
              </div>
              <div className="text-center p-3 rounded-lg bg-red-50 border border-red-200">
                <p className="text-2xl font-bold text-red-700">{resultado.errores.length}</p>
                <p className="text-xs text-red-500 mt-0.5">Con errores</p>
              </div>
            </div>

            {/* Error table */}
            {resultado.errores.length > 0 && (
              <div>
                <div className="flex items-center gap-2 mb-2 text-sm text-red-600 font-medium">
                  <AlertCircle className="h-4 w-4" />
                  Líneas con error
                </div>
                <div className="border border-red-200 rounded-lg overflow-hidden">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="bg-red-50 border-b border-red-200">
                        <th className="px-4 py-2 text-left text-xs font-semibold text-red-600 w-20">Línea</th>
                        <th className="px-4 py-2 text-left text-xs font-semibold text-red-600">Mensaje de error</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-red-100">
                      {resultado.errores.map((e, i) => (
                        <tr key={i} className="hover:bg-red-50/50">
                          <td className="px-4 py-2 text-slate-500 font-mono text-xs">{e.linea}</td>
                          <td className="px-4 py-2 text-slate-700">{e.mensaje}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {resultado.errores.length === 0 && resultado.procesados > 0 && (
              <div className="flex items-center gap-2 text-sm text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-lg px-4 py-3">
                <CheckCircle2 className="h-4 w-4 flex-shrink-0" />
                Todos los registros fueron importados correctamente.
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
