interface SectionHeaderProps {
  title: string
  description?: string
  actionLabel?: string
}

export function SectionHeader({ title, description, actionLabel }: SectionHeaderProps) {
  return (
    <div className="flex items-end justify-between gap-4">
      <div>
        <h2 className="text-xl font-semibold tracking-[-0.025em] text-neutral-950">{title}</h2>
        {description && <p className="mt-1 text-sm text-neutral-500">{description}</p>}
      </div>
      {actionLabel && (
        <span className="shrink-0 text-xs font-medium tracking-wide text-neutral-400">
          {actionLabel}
        </span>
      )}
    </div>
  )
}
