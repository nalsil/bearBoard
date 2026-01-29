import apiClient from './client'
import type { ApiResponse, PagedResponse, YoutubeVideo, CreateYoutubeVideoRequest } from '../types'

export const youtubeApi = {
  getList: async (page = 0, size = 20) => {
    const response = await apiClient.get<ApiResponse<PagedResponse<YoutubeVideo>>>('/admin/youtube', {
      params: { page, size },
    })
    return response.data
  },

  getById: async (id: number) => {
    const response = await apiClient.get<ApiResponse<YoutubeVideo>>(`/admin/youtube/${id}`)
    return response.data
  },

  create: async (data: CreateYoutubeVideoRequest) => {
    const response = await apiClient.post<ApiResponse<YoutubeVideo>>('/admin/youtube', data)
    return response.data
  },

  update: async (id: number, data: CreateYoutubeVideoRequest) => {
    const response = await apiClient.put<ApiResponse<YoutubeVideo>>(`/admin/youtube/${id}`, data)
    return response.data
  },

  delete: async (id: number) => {
    const response = await apiClient.delete<ApiResponse<void>>(`/admin/youtube/${id}`)
    return response.data
  },
}
