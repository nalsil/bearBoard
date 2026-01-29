import apiClient from './client'
import type { ApiResponse, Board } from '../types'

export const boardsApi = {
  getList: async () => {
    const response = await apiClient.get<ApiResponse<Board[]>>('/admin/boards')
    return response.data
  },

  getById: async (id: number) => {
    const response = await apiClient.get<ApiResponse<Board>>(`/admin/boards/${id}`)
    return response.data
  },
}
