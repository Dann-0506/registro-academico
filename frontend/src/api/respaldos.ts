import client from './client'

export const descargarRespaldo = async () => {
  const res = await client.get('/admin/respaldos/descargar', { responseType: 'blob' })
  const url = URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = url
  a.download = `sira_respaldo_${new Date().toISOString().slice(0,19).replace(/[:T]/g, '-')}.sql`
  a.click()
  URL.revokeObjectURL(url)
}

export const restaurarRespaldo = (archivo: File) => {
  const form = new FormData()
  form.append('archivo', archivo)
  return client.post('/admin/respaldos/restaurar', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
