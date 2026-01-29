import apiClient from './client'
import type { ApiResponse, PagedResponse, Post, CreatePostRequest } from '../types'

export const postsApi = {
  getList: async (boardId: number, page = 0, size = 20) => {
    const response = await apiClient.get<ApiResponse<PagedResponse<Post>>>('/admin/posts', {
      params: { boardId, page, size },
    })
    return response.data
  },

  getById: async (id: number) => {
    const response = await apiClient.get<ApiResponse<Post>>(`/admin/posts/${id}`)
    return response.data
  },

  create: async (data: CreatePostRequest) => {
    const response = await apiClient.post<ApiResponse<Post>>('/admin/posts', data)
    return response.data
  },

  update: async (id: number, data: CreatePostRequest) => {
    const response = await apiClient.put<ApiResponse<Post>>(`/admin/posts/${id}`, data)
    return response.data
  },

  delete: async (id: number) => {
    const response = await apiClient.delete<ApiResponse<void>>(`/admin/posts/${id}`)
    return response.data
  },
}
