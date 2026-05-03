import { useState, useRef, useEffect } from 'react'
import { NavLink, useNavigate, useLocation } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '@/store/authStore'
import { ChevronDown, LogOut, User, BookCheck, BookOpen } from 'lucide-react'
import { rolLabel, cn } from '@/lib/utils'

// ─── Estructura de navegación admin ──────────────────────────────────────────

const CATALOGOS = [
  { label: 'Administradores', to: '/admin/administradores' },
  { label: 'Maestros',        to: '/admin/maestros' },
  { label: 'Alumnos',         to: '/admin/alumnos' },
  { label: 'Materias',        to: '/admin/materias' },
  { label: 'Actividades',     to: '/admin/actividades-catalogo' },
  { label: 'Grupos',          to: '/admin/grupos' },
  { label: 'Inscripciones',   to: '/admin/inscripciones' },
]

const UTILIDADES = [
  { label: 'Reportes',        to: '/admin/reportes' },
  { label: 'Configuración',   to: '/admin/configuracion' },
  { label: 'Carga CSV',       to: '/admin/carga' },
  { label: 'Respaldos',       to: '/admin/respaldos' },
]

// ─── Dropdown genérico ────────────────────────────────────────────────────────

function NavDropdown({ label, items, isActive }: {
  label: string
  items: { label: string; to: string }[]
  isActive: boolean
}) {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  return (
    <div ref={ref} className="relative">
      <button
        onClick={() => setOpen(p => !p)}
        className={cn(
          'flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors',
          isActive || open
            ? 'text-white bg-white/15'
            : 'text-slate-400 hover:text-white hover:bg-white/10'
        )}
      >
        {label}
        <ChevronDown className={cn('h-3.5 w-3.5 transition-transform duration-200', open && 'rotate-180')} />
      </button>

      {open && (
        <>
          <div className="fixed inset-0 z-40" onClick={() => setOpen(false)} />
          <div className="absolute top-full left-0 mt-1.5 w-52 bg-white rounded-xl border border-slate-200 shadow-xl py-1 z-50">
            {items.map(item => (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={() => setOpen(false)}
                className={({ isActive }) => cn(
                  'block px-4 py-2.5 text-sm transition-colors',
                  isActive
                    ? 'text-blue-600 bg-blue-50 font-medium'
                    : 'text-slate-700 hover:bg-slate-50 hover:text-slate-900'
                )}
              >
                {item.label}
              </NavLink>
            ))}
          </div>
        </>
      )}
    </div>
  )
}

// ─── Menú de usuario ──────────────────────────────────────────────────────────

function UserMenu() {
  const { usuario, logout } = useAuthStore()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const handleLogout = () => { logout(); qc.clear(); navigate('/login') }

  return (
    <div ref={ref} className="relative">
      <button
        onClick={() => setOpen(p => !p)}
        className={cn(
          'flex items-center gap-2 px-2 py-1.5 rounded-lg transition-colors',
          open ? 'bg-white/15' : 'hover:bg-white/10'
        )}
      >
        <div className="h-7 w-7 rounded-full bg-blue-500 flex items-center justify-center text-white text-xs font-bold flex-shrink-0">
          {usuario?.nombre?.[0]?.toUpperCase()}
        </div>
        <div className="text-left hidden sm:block">
          <p className="text-xs font-medium text-white leading-tight">{usuario?.nombre}</p>
          <p className="text-xs text-slate-400 leading-tight">{rolLabel(usuario?.rol ?? '')}</p>
        </div>
        <ChevronDown className={cn('h-3.5 w-3.5 text-slate-400 transition-transform duration-200', open && 'rotate-180')} />
      </button>

      {open && (
        <>
          <div className="fixed inset-0 z-40" onClick={() => setOpen(false)} />
          <div className="absolute top-full right-0 mt-1.5 w-52 bg-white rounded-xl border border-slate-200 shadow-xl py-1 z-50">
            <div className="px-4 py-3 border-b border-slate-100">
              <p className="text-sm font-semibold text-slate-900 truncate">{usuario?.nombre}</p>
              <p className="text-xs text-slate-500 truncate">{usuario?.email}</p>
            </div>
            <NavLink to="/perfil" onClick={() => setOpen(false)}
              className="flex items-center gap-2.5 px-4 py-2.5 text-sm text-slate-700 hover:bg-slate-50 transition-colors">
              <User className="h-4 w-4 text-slate-400" />
              Mi perfil
            </NavLink>
            <button onClick={handleLogout}
              className="w-full flex items-center gap-2.5 px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 transition-colors">
              <LogOut className="h-4 w-4" />
              Cerrar sesión
            </button>
          </div>
        </>
      )}
    </div>
  )
}

// ─── Barra principal ──────────────────────────────────────────────────────────

export function TopNav() {
  const { usuario } = useAuthStore()
  const { pathname } = useLocation()
  const rol = usuario?.rol

  const isCatalogoActive = CATALOGOS.some(i => pathname.startsWith(i.to))
  const isUtilidadesActive = UTILIDADES.some(i => pathname.startsWith(i.to))

  return (
    <header className="sticky top-0 z-30 h-14 bg-slate-900 border-b border-slate-800 flex items-center px-4 gap-2">
      {/* Logo */}
      <NavLink to={rol === 'admin' ? '/admin' : rol === 'maestro' ? '/maestro' : '/alumno'}
        className="flex items-center gap-2.5 mr-4 flex-shrink-0">
        <div className="h-8 w-8 rounded-lg bg-blue-600 flex items-center justify-center text-white font-bold text-sm">
          S
        </div>
        <span className="text-white font-semibold text-sm hidden md:block">SIRA</span>
      </NavLink>

      {/* Nav por rol */}
      <nav className="flex items-center gap-1 flex-1">
        {rol === 'admin' && (
          <>
            <NavLink to="/admin" end
              className={({ isActive }) => cn(
                'px-3 py-2 rounded-lg text-sm font-medium transition-colors',
                isActive ? 'text-white bg-white/15' : 'text-slate-400 hover:text-white hover:bg-white/10'
              )}
            >
              Dashboard
            </NavLink>

            <NavDropdown label="Catálogos" items={CATALOGOS} isActive={isCatalogoActive} />
            <NavDropdown label="Utilidades" items={UTILIDADES} isActive={isUtilidadesActive} />
          </>
        )}

        {rol === 'maestro' && (
          <NavLink to="/maestro"
            className={({ isActive }) => cn(
              'flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-colors',
              isActive ? 'text-white bg-white/15' : 'text-slate-400 hover:text-white hover:bg-white/10'
            )}
          >
            <BookCheck className="h-4 w-4" />
            Mis Grupos
          </NavLink>
        )}

        {rol === 'alumno' && (
          <NavLink to="/alumno"
            className={({ isActive }) => cn(
              'flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-colors',
              isActive ? 'text-white bg-white/15' : 'text-slate-400 hover:text-white hover:bg-white/10'
            )}
          >
            <BookOpen className="h-4 w-4" />
            Mis Cursos
          </NavLink>
        )}
      </nav>

      {/* Usuario */}
      <UserMenu />
    </header>
  )
}
