import apiClient from './client'
import type { ApiResponse, PagedResponse, Faq, CreateFaqRequest } from '../types'

export const faqsApi = {
  getList: async (page = 0, size = 20) => {
    const response = await apiClient.get<ApiResponse<PagedResponse<Faq>>>('/admin/faqs', {
      params: { page, size },
    })
    return response.data
  },

  getById: async (id: number) => {
    const response = await apiClient.get<ApiResponse<Faq>>(`/admin/faqs/${id}`)
    return response.data
  },

  create: async (data: CreateFaqRequest) => {
    const response = await apiClient.post<ApiResponse<Faq>>('/admin/faqs', data)
    return response.data
  },

  update: async (id: number, data: CreateFaqRequest) => {
    const response = await apiClient.put<ApiResponse<Faq>>(`/admin/faqs/${id}`, data)
    return response.data
  },

  delete: async (id: number) => {
    const response = await apiClient.delete<ApiResponse<void>>(`/admin/faqs/${id}`)
    return response.data
  },

  getCategories: async () => {
    const response = await apiClient.get<ApiResponse<string[]>>('/admin/faqs/categories')
    return response.data
  },
}
