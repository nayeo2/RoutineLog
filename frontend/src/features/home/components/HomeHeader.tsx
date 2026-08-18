interface HomeHeaderProps {
  date: Date
}

const fullDateFormatter = new Intl.DateTimeFormat('ko-KR', {
  month: 'long',
  day: 'numeric',
  weekday: 'long',
})

export function HomeHeader({ date }: HomeHeaderProps) {
  return (
    <header className="flex items-start justify-between gap-6 pt-2">
      <div>
        <p className="text-xs font-semibold tracking-[0.18em] text-neutral-400 uppercase">
          Today
        </p>
        <h1 className="mt-2 text-3xl font-semibold tracking-[-0.04em] text-neutral-950">
          {fullDateFormatter.format(date)}
        </h1>
        <p className="mt-2 text-sm text-neutral-500">오늘의 리듬을 차분히 기록해 보세요.</p>
      </div>

      <div
        className="flex size-11 shrink-0 items-center justify-center rounded-full border border-neutral-200 bg-neutral-100 text-xs font-semibold tracking-wide text-neutral-600"
        aria-label="프로필 이미지 자리"
      >
        RL
      </div>
    </header>
  )
}
