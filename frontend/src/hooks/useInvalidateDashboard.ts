import { useQueryClient } from '@tanstack/react-query'

export function useInvalidateDashboard() {
  const qc = useQueryClient()
  return () => qc.invalidateQueries({ queryKey: ['dashboard'] })
}
