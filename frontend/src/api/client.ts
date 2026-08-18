import { tokenStorage } from '../auth/tokenStorage'
import type { ApiError, ApiResponse } from '../types/api'

const DEFAULT_API_BASE_URL = '/api/v1'
const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || DEFAULT_API_BASE_URL).replace(/\/$/, '')

export class ApiClientError extends Error {
  constructor(
    public readonly status: number,
    public readonly details: ApiError | null,
  ) {
    super(details?.message ?? 'API 요청을 처리하지 못했습니다.')
    this.name = 'ApiClientError'
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  const accessToken = tokenStorage.get()

  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`)
  }
  if (init.body && !(init.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const response = await fetch(`${apiBaseUrl}${normalizedPath}`, {
    ...init,
    headers,
  })

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  let payload: ApiResponse<T> | null = null
  if (text) {
    try {
      payload = JSON.parse(text) as ApiResponse<T>
    } catch {
      throw new ApiClientError(response.status, null)
    }
  }

  if (!response.ok || !payload?.success || payload.data === null) {
    if (response.status === 401) {
      tokenStorage.clear()
    }
    throw new ApiClientError(response.status, payload?.error ?? null)
  }

  return payload.data
}

export const apiClient = {
  request,
}
