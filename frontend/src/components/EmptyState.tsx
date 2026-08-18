interface EmptyStateProps {
  title: string
  description: string
}

export function EmptyState({ title, description }: EmptyStateProps) {
  return (
    <div className="rounded-card border border-dashed border-neutral-200 px-5 py-8 text-center">
      <p className="text-sm font-semibold text-neutral-800">{title}</p>
      <p className="mt-2 text-sm leading-6 text-neutral-500">{description}</p>
    </div>
  )
}
