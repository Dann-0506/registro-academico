import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import type { Rol } from '@/types'

interface Props { roles?: Rol[] }

export function PrivateRoute({ roles }: Props) {
  const { isAuthenticated, usuario } = useAuthStore()
  const { pathname } = useLocation()

  if (!isAuthenticated()) return <Navigate to="/login" replace />

  // Si el usuario debe cambiar su contraseña, forzar la pantalla correspondiente
  if (usuario?.requiereCambioPassword && pathname !== '/cambiar-password-obligatorio') {
    return <Navigate to="/cambiar-password-obligatorio" replace />
  }

  if (roles && usuario && !roles.includes(usuario.rol)) {
    const redirects: Record<Rol, string> = { admin: '/admin', maestro: '/maestro', alumno: '/alumno' }
    return <Navigate to={redirects[usuario.rol]} replace />
  }
  return <Outlet />
}
