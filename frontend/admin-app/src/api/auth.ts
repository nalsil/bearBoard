import apiClient from './client'
import type { ApiResponse, AuthResponse, LoginRequest } from '../types'

export const authApi = {
  login: async (data: LoginRequest) => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', data)
    return response.data
  },

  logout: async () => {
    const response = await apiClient.post<ApiResponse<void>>('/auth/logout')
    return response.data
  },

  me: async () => {
    const response = await apiClient.get<ApiResponse<AuthResponse>>('/auth/me')
    return response.data
  },

  switchCompany: async (companyId: number) => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/switch-company', { companyId })
    return response.data
  },
}
