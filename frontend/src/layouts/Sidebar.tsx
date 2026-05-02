import { NavLink, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import { rolLabel } from '@/lib/utils'
import {
  LayoutDashboard, Users, GraduationCap, BookOpen, School, ClipboardList,
  Settings, Upload, Database, BookCheck, User, LogOut, ChevronRight,
  UserCog, BarChart2, ListChecks,
} from 'lucide-react'
import { cn } from '@/lib/utils'

interface NavItem { label: string; to: string; icon: React.ReactNode }

const adminNav: NavItem[] = [
  { label: 'Dashboard',       to: '/admin',                   icon: <LayoutDashboard className="h-4 w-4" /> },
  { label: 'Administradores', to: '/admin/administradores',   icon: <UserCog className="h-4 w-4" /> },
  { label: 'Maestros',        to: '/admin/maestros',          icon: <GraduationCap className="h-4 w-4" /> },
  { label: 'Alumnos',         to: '/admin/alumnos',           icon: <Users className="h-4 w-4" /> },
  { label: 'Materias',        to: '/admin/materias',          icon: <BookOpen className="h-4 w-4" /> },
  { label: 'Grupos',          to: '/admin/grupos',            icon: <School className="h-4 w-4" /> },
  { label: 'Inscripciones',   to: '/admin/inscripciones',     icon: <ClipboardList className="h-4 w-4" /> },
  { label: 'Cat. Actividades', to: '/admin/actividades-catalogo', icon: <ListChecks className="h-4 w-4" /> },
  { label: 'Reportes',        to: '/admin/reportes',             icon: <BarChart2 className="h-4 w-4" /> },
  { label: 'Configuración',   to: '/admin/configuracion',     icon: <Settings className="h-4 w-4" /> },
  { label: 'Carga CSV',       to: '/admin/carga',             icon: <Upload className="h-4 w-4" /> },
  { label: 'Respaldos',       to: '/admin/respaldos',         icon: <Database className="h-4 w-4" /> },
]

const maestroNav: NavItem[] = [
  { label: 'Mis Grupos', to: '/maestro', icon: <BookCheck className="h-4 w-4" /> },
]

const alumnoNav: NavItem[] = [
  { label: 'Mis Cursos', to: '/alumno', icon: <BookOpen className="h-4 w-4" /> },
]

const navByRol = { admin: adminNav, maestro: maestroNav, alumno: alumnoNav }

export function Sidebar() {
  const { usuario, logout } = useAuthStore()
  const navigate = useNavigate()
  const nav = navByRol[usuario?.rol ?? 'alumno'] ?? []

  const handleLogout = () => { logout(); navigate('/login') }

  return (
    <aside className="flex flex-col w-64 min-h-screen bg-slate-900 border-r border-slate-800">
      {/* Logo */}
      <div className="px-5 py-5 border-b border-slate-800">
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-blue-600 text-white font-bold text-sm">S</div>
          <div>
            <p className="text-sm font-semibold text-white">SIRA</p>
            <p className="text-xs text-slate-400">Registro Académico</p>
          </div>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-0.5">
        {nav.map(item => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/admin' || item.to === '/maestro' || item.to === '/alumno'}
            className={({ isActive }) => cn(
              'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all group',
              isActive
                ? 'bg-blue-600 text-white'
                : 'text-slate-400 hover:text-white hover:bg-slate-800'
            )}
          >
            {item.icon}
            <span className="flex-1">{item.label}</span>
            <ChevronRight className="h-3.5 w-3.5 opacity-0 group-hover:opacity-100 transition-opacity" />
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className="border-t border-slate-800 px-3 py-3 space-y-0.5">
        <NavLink
          to="/perfil"
          className={({ isActive }) => cn(
            'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all',
            isActive ? 'bg-slate-700 text-white' : 'text-slate-400 hover:text-white hover:bg-slate-800'
          )}
        >
          <User className="h-4 w-4" />
          <div className="flex-1 min-w-0">
            <p className="truncate text-white text-xs font-medium">{usuario?.nombre}</p>
            <p className="truncate text-slate-500 text-xs">{rolLabel(usuario?.rol ?? '')}</p>
          </div>
        </NavLink>
        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-slate-400 hover:text-red-400 hover:bg-slate-800 transition-all"
        >
          <LogOut className="h-4 w-4" />
          Cerrar sesión
        </button>
      </div>
    </aside>
  )
}
