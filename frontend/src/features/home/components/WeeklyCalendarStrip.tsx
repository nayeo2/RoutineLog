interface WeeklyCalendarStripProps {
  selectedDate: Date
}

const weekdayFormatter = new Intl.DateTimeFormat('ko-KR', { weekday: 'short' })

function startOfWeek(date: Date): Date {
  const start = new Date(date)
  const mondayOffset = (date.getDay() + 6) % 7
  start.setDate(date.getDate() - mondayOffset)
  start.setHours(0, 0, 0, 0)
  return start
}

function sameDay(first: Date, second: Date): boolean {
  return (
    first.getFullYear() === second.getFullYear() &&
    first.getMonth() === second.getMonth() &&
    first.getDate() === second.getDate()
  )
}

export function WeeklyCalendarStrip({ selectedDate }: WeeklyCalendarStripProps) {
  const weekStart = startOfWeek(selectedDate)
  const days = Array.from({ length: 7 }, (_, index) => {
    const date = new Date(weekStart)
    date.setDate(weekStart.getDate() + index)
    return date
  })

  return (
    <section className="mt-9" aria-label="이번 주">
      <div className="grid grid-cols-7 gap-1 rounded-card border border-neutral-200 bg-neutral-50 p-2">
        {days.map((date) => {
          const isSelected = sameDay(date, selectedDate)
          return (
            <div
              className={`flex min-h-16 flex-col items-center justify-center rounded-xl transition ${
                isSelected ? 'bg-neutral-950 text-white shadow-sm' : 'text-neutral-500'
              }`}
              key={date.toISOString()}
              aria-current={isSelected ? 'date' : undefined}
            >
              <span className={`text-[11px] ${isSelected ? 'text-neutral-300' : 'text-neutral-400'}`}>
                {weekdayFormatter.format(date)}
              </span>
              <span className="mt-1 text-sm font-semibold">{date.getDate()}</span>
            </div>
          )
        })}
      </div>
    </section>
  )
}
