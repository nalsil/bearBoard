import apiClient from './client'
import type { ApiResponse, DashboardStats } from '../types'

export const dashboardApi = {
  getStats: async () => {
    const response = await apiClient.get<ApiResponse<DashboardStats>>('/admin/dashboard')
    return response.data
  },
}
