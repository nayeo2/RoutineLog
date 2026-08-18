import { EmptyState } from '../../../components/EmptyState'
import { LoadingBlock } from '../../../components/LoadingBlock'
import { SectionHeader } from '../../../components/SectionHeader'
import { SurfaceCard } from '../../../components/SurfaceCard'
import type { HomeVlog } from '../types'

interface TodayVlogSectionProps {
  vlog: HomeVlog | null
  isLoading?: boolean
}

export function TodayVlogSection({ vlog, isLoading = false }: TodayVlogSectionProps) {
  return (
    <section className="mt-10 pb-12">
      <SectionHeader title="오늘의 Vlog" description="완료한 순간들이 하루의 영상이 됩니다." />
      <div className="mt-5">
        {isLoading ? (
          <LoadingBlock rows={1} />
        ) : vlog ? (
          <SurfaceCard className="overflow-hidden border-neutral-900 bg-neutral-950 text-white">
            <div className="flex min-h-44 flex-col justify-between p-5">
              <div className="flex size-11 items-center justify-center rounded-full border border-white/25 bg-white/10">
                <span
                  className="ml-0.5 block h-0 w-0 border-y-[6px] border-l-[9px] border-y-transparent border-l-white"
                  aria-hidden="true"
                />
              </div>
              <div>
                <p className="text-xs tracking-[0.16em] text-neutral-400 uppercase">Daily Film</p>
                <p className="mt-2 text-lg font-semibold">오늘의 기록</p>
                <p className="mt-1 text-sm text-neutral-400">{vlog.durationLabel}</p>
              </div>
            </div>
          </SurfaceCard>
        ) : (
          <EmptyState
            title="아직 오늘의 Vlog가 없어요."
            description="영상으로 인증한 루틴이 모이면 하루의 기록을 만들 수 있어요."
          />
        )}
      </div>
    </section>
  )
}
