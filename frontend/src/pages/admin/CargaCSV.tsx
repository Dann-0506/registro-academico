import { useState, useRef } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Upload, FileUp, AlertCircle, CheckCircle2, X, ChevronDown, ChevronUp, Info } from 'lucide-react'
import axios from 'axios'
import { importarCSV } from '@/api/carga'
import type { CargaResultadoResponse } from '@/types'
import { PageHeader } from '@/components/shared/PageHeader'
import { LoadingSpinner } from '@/components/shared/LoadingSpinner'
import { ErrorAlert } from '@/components/shared/ErrorAlert'

// ─── Definición de formatos ───────────────────────────────────────────────────

interface Columna { nombre: string; requerida: boolean; descripcion: string }
interface FormatoTipo {
  label: string
  columnas: Columna[]
  ejemplo: string
  notas?: string
}

const FORMATOS: Record<string, FormatoTipo> = {
  alumnos: {
    label: 'Alumnos',
    columnas: [
      { nombre: 'num_control', requerida: true,  descripcion: 'Número de control único del alumno (ej. 21310001)' },
      { nombre: 'nombre',     requerida: true,  descripcion: 'Nombre completo' },
      { nombre: 'correo',     requerida: true, descripcion: 'Correo electrónico (se usa para iniciar sesión)' },
    ],
    ejemplo: '21310001,Juan Pérez García,juan@escuela.edu',
    notas: 'La contraseña temporal será el número de control del alumno.',
  },
  maestros: {
    label: 'Maestros',
    columnas: [
      { nombre: 'num_empleado', requerida: true,  descripcion: 'Número de empleado único (ej. EMP-001)' },
      { nombre: 'nombre',       requerida: true,  descripcion: 'Nombre completo' },
      { nombre: 'correo',       requerida: true,  descripcion: 'Correo electrónico (se usa para iniciar sesión)' },
    ],
    ejemplo: 'EMP-001,María López Ruiz,maria@escuela.edu',
    notas: 'La contraseña temporal será el número de empleado.',
  },
  administradores: {
    label: 'Administradores',
    columnas: [
      { nombre: 'num_empleado', requerida: true, descripcion: 'Número de empleado único (ej. ADMIN-002)' },
      { nombre: 'nombre',       requerida: true, descripcion: 'Nombre completo' },
      { nombre: 'correo',       requerida: true, descripcion: 'Correo electrónico (requerido para login)' },
    ],
    ejemplo: 'ADMIN-002,Carlos Ramírez,carlos@escuela.edu',
    notas: 'La contraseña temporal será el número de empleado.',
  },
  materias: {
    label: 'Materias',
    columnas: [
      { nombre: 'clave',           requerida: true,  descripcion: 'Clave única de la materia (ej. MAT101)' },
      { nombre: 'nombre',          requerida: true,  descripcion: 'Nombre de la materia' },
      { nombre: 'total_unidades',  requerida: true,  descripcion: 'Número de unidades (1–15)' },
      { nombre: 'nombres_unidades', requerida: false, descripcion: 'Nombres de unidades separados por | (ej. Álgebra|Cálculo|Geometría)' },
    ],
    ejemplo: 'MAT101,Matemáticas I,3,Álgebra Básica|Cálculo Diferencial|Geometría Analítica',
    notas: 'Si no se especifican nombres de unidades, se generan automáticamente como "Unidad 1", "Unidad 2", etc.',
  },
  grupos: {
    label: 'Grupos',
    columnas: [
      { nombre: 'clave_materia', requerida: true, descripcion: 'Clave de materia existente (ej. MAT101)' },
      { nombre: 'num_empleado',  requerida: true, descripcion: 'Número de empleado del maestro asignado' },
      { nombre: 'clave_grupo',   requerida: true, descripcion: 'Clave del grupo (ej. G-01)' },
      { nombre: 'semestre',      requerida: true, descripcion: 'Período académico (ej. 2026-1)' },
    ],
    ejemplo: 'MAT101,EMP-001,G-01,2026-1',
    notas: 'La materia y el maestro deben existir previamente en el sistema.',
  },
  inscripciones: {
    label: 'Inscripciones',
    columnas: [
      { nombre: 'num_control', requerida: true, descripcion: 'Número de control del alumno existente' },
      { nombre: 'clave_grupo', requerida: true, descripcion: 'Clave del grupo existente' },
      { nombre: 'semestre',    requerida: true, descripcion: 'Semestre del grupo (ej. 2026-1)' },
    ],
    ejemplo: 'A12345678,G-01,2026-1',
    notas: 'El alumno y el grupo deben existir previamente. No se permiten inscripciones duplicadas.',
  },
  actividades: {
    label: 'Actividades',
    columnas: [
      { nombre: 'nombre',      requerida: true,  descripcion: 'Nombre de la actividad, debe ser único en el catálogo (ej. Examen parcial)' },
      { nombre: 'descripcion', requerida: false, descripcion: 'Descripción opcional de la actividad' },
    ],
    ejemplo: 'Examen parcial,Evaluación escrita individual sin consulta',
    notas: 'Si ya existe una actividad con el mismo nombre se reportará como error en esa línea. Las actividades se crean como activas por defecto.',
  },
}

const TIPOS = Object.entries(FORMATOS).map(([value, f]) => ({ value, label: f.label }))

// ─── Componente de formato ────────────────────────────────────────────────────

function FormatoEsperado({ tipo }: { tipo: string }) {
  const [open, setOpen] = useState(false)
  const fmt = FORMATOS[tipo]
  if (!fmt) return null

  return (
    <div className="rounded-lg border border-slate-200 overflow-hidden">
      <button
        type="button"
        onClick={() => setOpen(p => !p)}
        className="w-full flex items-center justify-between px-4 py-3 bg-slate-50 hover:bg-slate-100 transition-colors text-sm font-medium text-slate-700"
      >
        <div className="flex items-center gap-2">
          <Info className="h-4 w-4 text-blue-500" />
          Ver formato esperado para <strong>{fmt.label}</strong>
        </div>
        {open ? <ChevronUp className="h-4 w-4 text-slate-400" /> : <ChevronDown className="h-4 w-4 text-slate-400" />}
      </button>

      {open && (
        <div className="px-4 py-4 space-y-4 bg-white">
          {/* Columnas */}
          <div>
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-2">Columnas</p>
            <div className="space-y-1.5">
              {fmt.columnas.map((col, i) => (
                <div key={i} className="flex items-start gap-3">
                  <span className="text-xs font-mono bg-slate-100 text-slate-700 px-2 py-0.5 rounded mt-0.5 flex-shrink-0 min-w-[120px]">
                    {col.nombre}
                  </span>
                  <div className="flex items-center gap-1.5 flex-1 flex-wrap">
                    <span className={`text-xs px-1.5 py-0.5 rounded font-medium ${col.requerida ? 'bg-red-100 text-red-600' : 'bg-slate-100 text-slate-500'}`}>
                      {col.requerida ? 'Requerida' : 'Opcional'}
                    </span>
                    <span className="text-xs text-slate-500">{col.descripcion}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Ejemplo */}
          <div>
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-2">Ejemplo de fila</p>
            <code className="block text-xs bg-slate-900 text-emerald-400 px-4 py-3 rounded-lg font-mono break-all">
              {fmt.ejemplo}
            </code>
          </div>

          {/* Notas */}
          {fmt.notas && (
            <div className="flex items-start gap-2 text-xs text-amber-700 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2.5">
              <AlertCircle className="h-3.5 w-3.5 mt-0.5 flex-shrink-0" />
              {fmt.notas}
            </div>
          )}

          {/* Hint encabezado */}
          <p className="text-xs text-slate-400">
            La primera fila puede ser un encabezado — el sistema la detecta y la omite automáticamente.
          </p>
        </div>
      )}
    </div>
  )
}

// ─── Página principal ─────────────────────────────────────────────────────────

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

  const handleTipoChange = (nuevoTipo: string) => {
    setTipo(nuevoTipo)
    setResultado(null)
    setGlobalError('')
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
          <select value={tipo} onChange={(e) => handleTipoChange(e.target.value)} className={inputClass}>
            {TIPOS.map((t) => (
              <option key={t.value} value={t.value}>{t.label}</option>
            ))}
          </select>
        </div>

        {/* Formato esperado */}
        <FormatoEsperado tipo={tipo} />

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
