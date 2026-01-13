import { useContext } from 'react'
import { NotificationContext } from '@/context/NotificationContext'

/**
 * Custom hook to access notification context
 * @throws Error if used outside of NotificationProvider
 * @returns Notification context with showNotification function
 */
export const useNotification = () => {
  const context = useContext(NotificationContext)
  if (!context) {
    throw new Error('useNotification must be used within NotificationProvider')
  }
  return context
}
