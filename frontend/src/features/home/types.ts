export type RoutineStatus = 'PENDING' | 'SUCCESS' | 'FAILED'

export interface HomeRoutine {
  id: number
  title: string
  scheduledTime: string
  status: RoutineStatus
}

export interface HomeVlog {
  id: number
  durationLabel: string
}
