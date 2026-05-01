import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import type { Rol } from '@/types'

interface Props { roles?: Rol[] }

export function PrivateRoute({ roles }: Props) {
  const { isAuthenticated, usuario } = useAuthStore()

  if (!isAuthenticated()) return <Navigate to="/login" replace />
  if (roles && usuario && !roles.includes(usuario.rol)) {
    const redirects: Record<Rol, string> = { admin: '/admin', maestro: '/maestro', alumno: '/alumno' }
    return <Navigate to={redirects[usuario.rol]} replace />
  }
  return <Outlet />
}
