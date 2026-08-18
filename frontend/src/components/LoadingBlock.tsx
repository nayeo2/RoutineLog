interface LoadingBlockProps {
  rows?: number
}

export function LoadingBlock({ rows = 2 }: LoadingBlockProps) {
  return (
    <div className="space-y-3" aria-busy="true" aria-label="불러오는 중">
      {Array.from({ length: rows }, (_, index) => (
        <div
          className="h-20 animate-pulse rounded-card border border-neutral-100 bg-neutral-100"
          key={index}
        />
      ))}
    </div>
  )
}
