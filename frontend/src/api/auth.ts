import client from './client'
import type { LoginResponse, PerfilResponse } from '@/types'

export const login = (email: string, password: string) =>
  client.post<LoginResponse>('/auth/login', { email, password }).then(r => r.data)

export const getPerfil = () =>
  client.get<PerfilResponse>('/perfil').then(r => r.data)

export const cambiarPassword = (data: { passwordActual: string; passwordNueva: string; passwordConfirmar: string }) =>
  client.post('/perfil/cambiar-password', data)
