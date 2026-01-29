import apiClient from './client'
import type { ApiResponse, PagedResponse, Product, CreateProductRequest } from '../types'

export const productsApi = {
  getList: async (page = 0, size = 20) => {
    const response = await apiClient.get<ApiResponse<PagedResponse<Product>>>('/admin/products', {
      params: { page, size },
    })
    return response.data
  },

  getById: async (id: number) => {
    const response = await apiClient.get<ApiResponse<Product>>(`/admin/products/${id}`)
    return response.data
  },

  create: async (data: CreateProductRequest) => {
    const response = await apiClient.post<ApiResponse<Product>>('/admin/products', data)
    return response.data
  },

  update: async (id: number, data: CreateProductRequest) => {
    const response = await apiClient.put<ApiResponse<Product>>(`/admin/products/${id}`, data)
    return response.data
  },

  delete: async (id: number) => {
    const response = await apiClient.delete<ApiResponse<void>>(`/admin/products/${id}`)
    return response.data
  },

  getCategories: async () => {
    const response = await apiClient.get<ApiResponse<string[]>>('/admin/products/categories')
    return response.data
  },
}
