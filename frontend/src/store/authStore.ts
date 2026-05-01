import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { AuthUsuario, Rol } from '@/types'

interface AuthState {
  token: string | null
  usuario: AuthUsuario | null
  login: (token: string, usuario: AuthUsuario) => void
  logout: () => void
  isAuthenticated: () => boolean
  hasRole: (rol: Rol) => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      usuario: null,
      login: (token, usuario) => set({ token, usuario }),
      logout: () => set({ token: null, usuario: null }),
      isAuthenticated: () => !!get().token,
      hasRole: (rol) => get().usuario?.rol === rol,
    }),
    { name: 'sira-auth' }
  )
)
