import apiClient from '../api'
import { SignupRequest, SignupResponse } from '@/types/api.types'

export const authService = {
  /**
   * Register a new user
   * @param data - User signup information
   * @returns User data with generated userId
   */
  signup: async (data: SignupRequest): Promise<SignupResponse> => {
    const response = await apiClient.post<SignupResponse>('/signup', data)
    return response.data
  },
}
