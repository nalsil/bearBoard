import { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { message } from 'antd'
import { authApi } from '../api'
import type { AuthResponse } from '../types'

interface AuthContextType {
  user: AuthResponse | null
  loading: boolean
  isAuthenticated: boolean
  isSuperAdmin: boolean
  login: (username: string, password: string) => Promise<boolean>
  logout: () => Promise<void>
  switchCompany: (companyId: number) => Promise<boolean>
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  const refreshUser = useCallback(async () => {
    try {
      const response = await authApi.me()
      if (response.success && response.data) {
        setUser(response.data)
      } else {
        setUser(null)
      }
    } catch {
      setUser(null)
    }
  }, [])

  useEffect(() => {
    const initAuth = async () => {
      try {
        await refreshUser()
      } finally {
        setLoading(false)
      }
    }
    initAuth()
  }, [refreshUser])

  const login = async (username: string, password: string): Promise<boolean> => {
    try {
      const response = await authApi.login({ username, password })
      if (response.success && response.data) {
        setUser(response.data)
        message.success(response.message)
        return true
      } else {
        message.error(response.message || '로그인에 실패했습니다.')
        return false
      }
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } }
      message.error(err.response?.data?.message || '로그인에 실패했습니다.')
      return false
    }
  }

  const logout = async () => {
    try {
      await authApi.logout()
      setUser(null)
      navigate('/login')
      message.success('로그아웃되었습니다.')
    } catch {
      // Even if API fails, clear local state
      setUser(null)
      navigate('/login')
    }
  }

  const switchCompany = async (companyId: number): Promise<boolean> => {
    try {
      const response = await authApi.switchCompany(companyId)
      if (response.success && response.data) {
        setUser(response.data)
        message.success(response.message)
        return true
      } else {
        message.error(response.message || '회사 전환에 실패했습니다.')
        return false
      }
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } }
      message.error(err.response?.data?.message || '회사 전환에 실패했습니다.')
      return false
    }
  }

  const value: AuthContextType = {
    user,
    loading,
    isAuthenticated: !!user,
    isSuperAdmin: user?.role === 'SUPER_ADMIN',
    login,
    logout,
    switchCompany,
    refreshUser,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
