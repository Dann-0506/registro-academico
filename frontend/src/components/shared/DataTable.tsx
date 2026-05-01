import { useState } from 'react'
import { Search } from 'lucide-react'
import { LoadingSpinner } from './LoadingSpinner'

export interface Column<T> {
  header: string
  accessor: keyof T | ((row: T) => React.ReactNode)
  className?: string
}

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

  const filtered = searchable && searchKeys && search
    ? data.filter(row => searchKeys.some(k => String(row[k] ?? '').toLowerCase().includes(search.toLowerCase())))
    : data

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      {(searchable || actions) && (
        <div className="flex items-center justify-between gap-3 px-4 py-3 border-b border-slate-100">
          {searchable && (
            <div className="relative flex-1 max-w-xs">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
              <input
                value={search} onChange={e => setSearch(e.target.value)}
                placeholder={searchPlaceholder}
                className="w-full pl-9 pr-3 py-2 text-sm rounded-lg border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-400 transition"
              />
            </div>
          )}
          {actions && <div className="flex items-center gap-2">{actions}</div>}
        </div>
      )}

      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-slate-50 border-b border-slate-200">
              {columns.map((col, i) => (
                <th key={i} className={`px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide ${col.className ?? ''}`}>
                  {col.header}
                </th>
              ))}
              {rowActions && <th className="px-4 py-3 text-right text-xs font-semibold text-slate-500 uppercase tracking-wide">Acciones</th>}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {isLoading ? (
              <tr><td colSpan={columns.length + (rowActions ? 1 : 0)} className="py-16"><LoadingSpinner className="h-full" /></td></tr>
            ) : filtered.length === 0 ? (
              <tr><td colSpan={columns.length + (rowActions ? 1 : 0)} className="py-12 text-center text-slate-400">{emptyMessage}</td></tr>
            ) : filtered.map(row => (
              <tr key={keyExtractor(row)} className="hover:bg-slate-50/60 transition-colors">
                {columns.map((col, i) => (
                  <td key={i} className={`px-4 py-3.5 text-slate-700 ${col.className ?? ''}`}>
                    {typeof col.accessor === 'function' ? col.accessor(row) : String(row[col.accessor] ?? '—')}
                  </td>
                ))}
                {rowActions && <td className="px-4 py-3.5 text-right">{rowActions(row)}</td>}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filtered.length > 0 && (
        <div className="px-4 py-2.5 border-t border-slate-100 text-xs text-slate-400">
          {filtered.length} {filtered.length === 1 ? 'registro' : 'registros'}
        </div>
      )}
    </div>
  )
}
