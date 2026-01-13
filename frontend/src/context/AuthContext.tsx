import React, { createContext, useState, useEffect, ReactNode } from 'react'
import { User } from '@/types/api.types'
import { userService } from '@/services/endpoints/user.service'

interface AuthContextType {
  user: User | null
  userId: string | null
  isLoading: boolean
  login: (userId: string) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined)

const USER_ID_KEY = 'trading_platform_user_id'

interface AuthProviderProps {
  children: ReactNode
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null)
  const [userId, setUserId] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    // Check localStorage for existing session on mount
    const storedUserId = localStorage.getItem(USER_ID_KEY)
    if (storedUserId) {
      login(storedUserId).catch(() => {
        // If login fails, clear the invalid stored userId
        logout()
      })
    } else {
      setIsLoading(false)
    }
  }, [])

  const login = async (id: string) => {
    setIsLoading(true)
    try {
      const userData = await userService.getUser(id)
      setUser(userData)
      setUserId(id)
      localStorage.setItem(USER_ID_KEY, id)
      console.log('User logged in:', userData.username)
    } catch (error) {
      console.error('Failed to load user:', error)
      logout()
      throw error
    } finally {
      setIsLoading(false)
    }
  }

  const logout = () => {
    setUser(null)
    setUserId(null)
    localStorage.removeItem(USER_ID_KEY)
    setIsLoading(false)
    console.log('User logged out')
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        userId,
        isLoading,
        login,
        logout,
        isAuthenticated: !!userId,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}
