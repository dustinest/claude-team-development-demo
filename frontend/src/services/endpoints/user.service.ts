import apiClient from '../api'
import { User } from '@/types/api.types'

export const userService = {
  /**
   * Get user details by userId
   * @param userId - UUID of the user
   * @returns User details
   */
  getUser: async (userId: string): Promise<User> => {
    const response = await apiClient.get<User>(`/users/${userId}`)
    return response.data
  },
}
