import apiClient from './client'
import type { ApiResponse, PagedResponse, Qna, AnswerQnaRequest } from '../types'

export const qnasApi = {
  getList: async (page = 0, size = 20, answered?: string) => {
    const params: Record<string, unknown> = { page, size }
    if (answered !== undefined) {
      params.answered = answered
    }
    const response = await apiClient.get<ApiResponse<PagedResponse<Qna>>>('/admin/qnas', { params })
    return response.data
  },

  getById: async (id: number) => {
    const response = await apiClient.get<ApiResponse<Qna>>(`/admin/qnas/${id}`)
    return response.data
  },

  answer: async (id: number, data: AnswerQnaRequest) => {
    const response = await apiClient.put<ApiResponse<Qna>>(`/admin/qnas/${id}/answer`, data)
    return response.data
  },

  hide: async (id: number, hidden: boolean) => {
    const response = await apiClient.put<ApiResponse<Qna>>(`/admin/qnas/${id}/hide`, null, {
      params: { hidden },
    })
    return response.data
  },

  delete: async (id: number) => {
    const response = await apiClient.delete<ApiResponse<void>>(`/admin/qnas/${id}`)
    return response.data
  },
}
