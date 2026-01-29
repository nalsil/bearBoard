import apiClient from './client'
import type { ApiResponse, Company } from '../types'

export const companiesApi = {
  getList: async () => {
    const response = await apiClient.get<ApiResponse<Company[]>>('/admin/companies')
    return response.data
  },

  getActiveList: async () => {
    const response = await apiClient.get<ApiResponse<Company[]>>('/admin/companies/active')
    return response.data
  },

  getById: async (id: number) => {
    const response = await apiClient.get<ApiResponse<Company>>(`/admin/companies/${id}`)
    return response.data
  },

  getCurrent: async () => {
    const response = await apiClient.get<ApiResponse<Company>>('/admin/companies/current')
    return response.data
  },
}
