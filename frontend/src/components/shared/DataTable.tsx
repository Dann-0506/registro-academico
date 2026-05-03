import { useState } from 'react'
import { Search, ChevronLeft, ChevronRight } from 'lucide-react'


export interface Column<T> {
  header: string
  accessor: keyof T | ((row: T) => React.ReactNode)
  className?: string
}

const PAGE_SIZE = 20

interface Props<T> {
  columns: Column<T>[]
  data: T[]
  isLoading?: boolean
  searchable?: boolean
  searchPlaceholder?: string
  searchKeys?: (keyof T)[]
  actions?: React.ReactNode
  rowActions?: (row: T) => React.ReactNode
  emptyMessage?: string
  keyExtractor: (row: T) => string | number
}

export function DataTable<T>({
  columns, data, isLoading, searchable = true, searchPlaceholder = 'Buscar...',
  searchKeys, actions, rowActions, emptyMessage = 'Sin resultados.', keyExtractor,
}: Props<T>) {
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(1)

  const colSpan = columns.length + (rowActions ? 1 : 0)
  const ROW_HEIGHT = 'h-[57px]' // py-3.5 + icono de botón (p-1.5 + h-4) + borde = 57 px

  // Filtrado
  const filtered = searchable && searchKeys && search
    ? data.filter(row => searchKeys.some(k => String(row[k] ?? '').toLowerCase().includes(search.toLowerCase())))
    : data

  // Resetear a página 1 al buscar
  const handleSearch = (v: string) => { setSearch(v); setPage(1) }

  // Paginación
  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
  const safePage = Math.min(page, totalPages)
  const start = (safePage - 1) * PAGE_SIZE
  const pageRows = filtered.slice(start, start + PAGE_SIZE)

  // Filas vacías para mantener altura fija de 12 filas
  const emptyRows = PAGE_SIZE - pageRows.length

  // Rango de páginas visibles (máx 5)
  const pageNumbers = (() => {
    const delta = 2
    const range: number[] = []
    for (let i = Math.max(1, safePage - delta); i <= Math.min(totalPages, safePage + delta); i++) {
      range.push(i)
    }
    return range
  })()

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden flex flex-col">
      {/* Toolbar */}
      {(searchable || actions) && (
        <div className="flex items-center justify-between gap-3 px-4 py-3 border-b border-slate-100">
          {searchable && (
            <div className="relative flex-1 max-w-xs">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
              <input
                value={search}
                onChange={e => handleSearch(e.target.value)}
                placeholder={searchPlaceholder}
                className="w-full pl-9 pr-3 py-2 text-sm rounded-lg border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-400 transition"
              />
            </div>
          )}
          {actions && <div className="flex items-center gap-2">{actions}</div>}
        </div>
      )}

      {/* Tabla */}
      <div className="overflow-x-auto flex-1">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-slate-50 border-b border-slate-200">
              {columns.map((col, i) => (
                <th key={i} className={`px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide ${col.className ?? ''}`}>
                  {col.header}
                </th>
              ))}
              {rowActions && (
                <th className="px-4 py-3 text-right text-xs font-semibold text-slate-500 uppercase tracking-wide">
                  Acciones
                </th>
              )}
            </tr>
          </thead>

          <tbody className="divide-y divide-slate-100">
            {isLoading ? (
              // Loading: PAGE_SIZE filas de skeleton
              Array.from({ length: PAGE_SIZE }).map((_, i) => (
                <tr key={i} className={ROW_HEIGHT}>
                  {Array.from({ length: colSpan }).map((_, j) => (
                    <td key={j} className="px-4">
                      <div className="h-3 bg-slate-100 rounded animate-pulse w-3/4" />
                    </td>
                  ))}
                </tr>
              ))
            ) : filtered.length === 0 ? (
              <>
                <tr>
                  <td colSpan={colSpan} className="px-4 py-5 text-center text-sm text-slate-400">
                    {emptyMessage}
                  </td>
                </tr>
                {/* Filas vacías para completar la altura */}
                {Array.from({ length: PAGE_SIZE - 1 }).map((_, i) => (
                  <tr key={i} className={ROW_HEIGHT}>
                    {Array.from({ length: colSpan }).map((_, j) => (
                      <td key={j} className="px-4 py-3.5 select-none text-transparent">&nbsp;</td>
                    ))}
                  </tr>
                ))}
              </>
            ) : (
              <>
                {pageRows.map(row => (
                  <tr key={keyExtractor(row)} className={`${ROW_HEIGHT} hover:bg-slate-50/60 transition-colors`}>
                    {columns.map((col, i) => (
                      <td key={i} className={`px-4 py-3.5 text-slate-700 ${col.className ?? ''}`}>
                        {typeof col.accessor === 'function' ? col.accessor(row) : String(row[col.accessor] ?? '—')}
                      </td>
                    ))}
                    {rowActions && <td className="px-4 py-3.5 text-right">{rowActions(row)}</td>}
                  </tr>
                ))}
                {/* Filas vacías para completar al tamaño de PAGE_SIZE*/}
                {Array.from({ length: emptyRows }).map((_, i) => (
                  <tr key={`empty-${i}`} className={ROW_HEIGHT}>
                    {Array.from({ length: colSpan }).map((_, j) => (
                      <td key={j} className="px-4 py-3.5 select-none text-transparent">&nbsp;</td>
                    ))}
                  </tr>
                ))}
              </>
            )}
          </tbody>
        </table>
      </div>

      {/* Footer: conteo + paginación */}
      <div className="flex items-center justify-between px-4 py-2.5 border-t border-slate-100 bg-slate-50/50">
        {/* Info */}
        <span className="text-xs text-slate-400">
          {isLoading ? '...' : filtered.length === 0
            ? '0 registros'
            : `${start + 1}–${Math.min(start + PAGE_SIZE, filtered.length)} de ${filtered.length} registros`
          }
        </span>

        {/* Controles de paginación */}
        {!isLoading && totalPages > 1 && (
          <div className="flex items-center gap-1">
            <button
              onClick={() => setPage(p => Math.max(1, p - 1))}
              disabled={safePage === 1}
              className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>

            {pageNumbers[0] > 1 && (
              <>
                <PageBtn n={1} current={safePage} onClick={setPage} />
                {pageNumbers[0] > 2 && <span className="px-1 text-slate-300 text-xs">…</span>}
              </>
            )}

            {pageNumbers.map(n => (
              <PageBtn key={n} n={n} current={safePage} onClick={setPage} />
            ))}

            {pageNumbers[pageNumbers.length - 1] < totalPages && (
              <>
                {pageNumbers[pageNumbers.length - 1] < totalPages - 1 && (
                  <span className="px-1 text-slate-300 text-xs">…</span>
                )}
                <PageBtn n={totalPages} current={safePage} onClick={setPage} />
              </>
            )}

            <button
              onClick={() => setPage(p => Math.min(totalPages, p + 1))}
              disabled={safePage === totalPages}
              className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
            >
              <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

function PageBtn({ n, current, onClick }: { n: number; current: number; onClick: (n: number) => void }) {
  const active = n === current
  return (
    <button
      onClick={() => onClick(n)}
      className={`min-w-[28px] h-7 px-1.5 rounded-lg text-xs font-medium transition-colors ${
        active
          ? 'bg-blue-600 text-white'
          : 'text-slate-500 hover:bg-slate-200 hover:text-slate-700'
      }`}
    >
      {n}
    </button>
  )
}
