import { LoadingBlock } from '../../../components/LoadingBlock'
import { SectionHeader } from '../../../components/SectionHeader'
import { SurfaceCard } from '../../../components/SurfaceCard'
import type { HomeRoutine } from '../types'

interface RoutineSummaryProps {
  routines: HomeRoutine[]
  isLoading?: boolean
}

export function RoutineSummary({ routines, isLoading = false }: RoutineSummaryProps) {
  const completed = routines.filter((routine) => routine.status === 'SUCCESS').length
  const pending = routines.filter((routine) => routine.status === 'PENDING').length

  return (
    <section className="mt-10">
      <SectionHeader title="오늘의 요약" />
      <div className="mt-5">
        {isLoading ? (
          <LoadingBlock rows={1} />
        ) : (
          <SurfaceCard className="grid grid-cols-3 divide-x divide-neutral-100 px-2 py-5">
            <SummaryValue label="전체" value={routines.length} />
            <SummaryValue label="완료" value={completed} />
            <SummaryValue label="남음" value={pending} />
          </SurfaceCard>
        )}
      </div>
    </section>
  )
}

function SummaryValue({ label, value }: { label: string; value: number }) {
  return (
    <div className="text-center">
      <strong className="text-2xl font-semibold tracking-[-0.03em] text-neutral-950">{value}</strong>
      <p className="mt-1 text-xs text-neutral-400">{label}</p>
    </div>
  )
}
