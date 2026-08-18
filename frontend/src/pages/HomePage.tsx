import { HomeHeader } from '../features/home/components/HomeHeader'
import { RoutineSummary } from '../features/home/components/RoutineSummary'
import { TodayRoutineSection } from '../features/home/components/TodayRoutineSection'
import { TodayVlogSection } from '../features/home/components/TodayVlogSection'
import { WeeklyCalendarStrip } from '../features/home/components/WeeklyCalendarStrip'
import type { HomeRoutine } from '../features/home/types'

const previewRoutines: HomeRoutine[] = [
  { id: 1, title: '가벼운 스트레칭', scheduledTime: '07:00', status: 'SUCCESS' },
  { id: 2, title: '책 20분 읽기', scheduledTime: '12:30', status: 'PENDING' },
  { id: 3, title: '저녁 산책', scheduledTime: '20:00', status: 'PENDING' },
]

export function HomePage() {
  const today = new Date()

  return (
    <main className="min-h-dvh px-5 pt-6 sm:px-6 sm:pt-8">
      <HomeHeader date={today} />
      <WeeklyCalendarStrip selectedDate={today} />
      <TodayRoutineSection routines={previewRoutines} />
      <RoutineSummary routines={previewRoutines} />
      <TodayVlogSection vlog={null} />
    </main>
  )
}
