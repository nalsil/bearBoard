import axios, { AxiosError, AxiosResponse } from 'axios'
import type { ApiResponse } from '../types'

const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    if (error.response?.status === 401) {
      // Redirect to login on unauthorized
      window.location.href = '/admin-app/login'
    }
    return Promise.reject(error)
  }
)

export default apiClient
