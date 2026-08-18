import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { tokenStorage } from './tokenStorage'

interface AuthContextValue {
  accessToken: string | null
  isAuthenticated: boolean
  setAccessToken: (accessToken: string) => void
  clearAccessToken: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setStoredAccessToken] = useState<string | null>(() => tokenStorage.get())

  useEffect(() => tokenStorage.subscribe(setStoredAccessToken), [])

  const value = useMemo<AuthContextValue>(
    () => ({
      accessToken,
      isAuthenticated: accessToken !== null,
      setAccessToken: tokenStorage.set,
      clearAccessToken: tokenStorage.clear,
    }),
    [accessToken],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider.')
  }
  return context
}
