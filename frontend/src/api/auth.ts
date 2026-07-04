import { apiClient, type ApiResponse } from './client'

export type Role = 'DOCTOR' | 'NURSE' | 'ADMIN' | 'PHARMACIST' | 'DIETITIAN'

export interface LoginInitiatedResponse {
  txnId: string
  otpRequired: boolean
  message: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresInSeconds: number
}

export interface MeResponse {
  userId: number
  username: string
  fullName: string
  email: string | null
  role: Role
}

export async function login(username: string, password: string): Promise<LoginInitiatedResponse> {
  const response = await apiClient.post<ApiResponse<LoginInitiatedResponse>>('/auth/login', {
    username,
    password,
  })
  return response.data.data
}

export async function verifyOtp(txnId: string, otp: string): Promise<TokenResponse> {
  const response = await apiClient.post<ApiResponse<TokenResponse>>('/auth/otp/verify', { txnId, otp })
  return response.data.data
}

export async function logout(refreshToken: string): Promise<void> {
  await apiClient.post('/auth/logout', { refreshToken })
}

export async function fetchMe(): Promise<MeResponse> {
  const response = await apiClient.get<ApiResponse<MeResponse>>('/auth/me')
  return response.data.data
}
