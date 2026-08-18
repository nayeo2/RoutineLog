import { apiClient } from '../../api/client'

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: 'Bearer'
}

export interface SignupRequest {
  email: string
  password: string
  name: string
}

export interface SignupResponse {
  id: number
  email: string
  name: string
}

export function login(request: LoginRequest): Promise<LoginResponse> {
  return apiClient.request<LoginResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function signup(request: SignupRequest): Promise<SignupResponse> {
  return apiClient.request<SignupResponse>('/auth/signup', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}
