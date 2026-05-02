import client from './client'

// Endpoints reservados para la futura vista de reportes estratégicos
export const getKpis = () => client.get('/admin/analisis/kpis').then(r => r.data)
export const getRendimiento = () => client.get('/admin/analisis/rendimiento').then(r => r.data)
