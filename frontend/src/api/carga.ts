import client from './client'
import type { CargaResultadoResponse } from '@/types'

export const importarCSV = (archivo: File, tipo: string) => {
  const form = new FormData()
  form.append('archivo', archivo)
  return client.post<CargaResultadoResponse>(`/admin/carga/csv?tipo=${tipo}`, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }).then(r => r.data)
}
