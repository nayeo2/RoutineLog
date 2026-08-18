import type { ReactNode } from 'react'

interface SurfaceCardProps {
  children: ReactNode
  className?: string
}

export function SurfaceCard({ children, className = '' }: SurfaceCardProps) {
  return (
    <div className={`rounded-card border border-neutral-200 bg-white shadow-card ${className}`}>
      {children}
    </div>
  )
}
