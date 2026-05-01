import { estadoColor } from '@/lib/utils'

interface Props { estado: string; label?: string }

export function StatusBadge({ estado, label }: Props) {
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${estadoColor(estado)}`}>
      {label ?? estado}
    </span>
  )
}
