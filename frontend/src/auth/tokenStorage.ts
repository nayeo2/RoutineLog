const ACCESS_TOKEN_KEY = 'routine-log.access-token'

type TokenListener = (accessToken: string | null) => void

const listeners = new Set<TokenListener>()

function readToken(): string | null {
  return typeof window === 'undefined' ? null : window.localStorage.getItem(ACCESS_TOKEN_KEY)
}

function notify(accessToken: string | null) {
  listeners.forEach((listener) => listener(accessToken))
}

export const tokenStorage = {
  get(): string | null {
    return readToken()
  },

  set(accessToken: string): void {
    window.localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    notify(accessToken)
  },

  clear(): void {
    window.localStorage.removeItem(ACCESS_TOKEN_KEY)
    notify(null)
  },

  subscribe(listener: TokenListener): () => void {
    listeners.add(listener)
    return () => {
      listeners.delete(listener)
    }
  },
}
