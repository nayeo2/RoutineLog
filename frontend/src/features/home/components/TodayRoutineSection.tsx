import { EmptyState } from '../../../components/EmptyState'
import { LoadingBlock } from '../../../components/LoadingBlock'
import { SectionHeader } from '../../../components/SectionHeader'
import { SurfaceCard } from '../../../components/SurfaceCard'
import type { HomeRoutine, RoutineStatus } from '../types'

interface TodayRoutineSectionProps {
  routines: HomeRoutine[]
  isLoading?: boolean
}

const statusLabel: Record<RoutineStatus, string> = {
  PENDING: '대기',
  SUCCESS: '완료',
  FAILED: '실패',
}

const statusStyle: Record<RoutineStatus, string> = {
  PENDING: 'border-neutral-200 bg-neutral-50 text-neutral-500',
  SUCCESS: 'border-neutral-950 bg-neutral-950 text-white',
  FAILED: 'border-neutral-300 bg-white text-neutral-700',
}

export function TodayRoutineSection({ routines, isLoading = false }: TodayRoutineSectionProps) {
  return (
    <section className="mt-10">
      <SectionHeader
        title="오늘의 루틴"
        description="시간 순서대로 천천히 완료해 보세요."
        actionLabel={`${routines.length} ROUTINES`}
      />

      <div className="mt-5">
        {isLoading ? (
          <LoadingBlock rows={3} />
        ) : routines.length === 0 ? (
          <EmptyState
            title="오늘 예정된 루틴이 없어요."
            description="새 루틴을 추가하면 이곳에서 하루의 흐름을 확인할 수 있어요."
          />
        ) : (
          <div className="space-y-3">
            {routines.map((routine) => (
              <SurfaceCard className="flex min-h-20 items-center gap-4 px-4 py-4" key={routine.id}>
                <time className="w-12 shrink-0 text-sm font-semibold text-neutral-500">
                  {routine.scheduledTime}
                </time>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-base font-semibold tracking-[-0.015em] text-neutral-950">
                    {routine.title}
                  </p>
                  <p className="mt-1 text-xs text-neutral-400">영상 인증 준비</p>
                </div>
                <span
                  className={`rounded-full border px-2.5 py-1 text-[11px] font-semibold ${statusStyle[routine.status]}`}
                >
                  {statusLabel[routine.status]}
                </span>
              </SurfaceCard>
            ))}
          </div>
        )}
      </div>
    </section>
  )
}
