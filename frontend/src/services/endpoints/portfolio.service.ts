import apiClient from '../api'
import { Holding } from '@/types/api.types'

export const portfolioService = {
  /**
   * Get user's portfolio holdings
   * @param userId - UUID of the user
   * @returns Array of holdings with quantity and average price
   */
  getPortfolio: async (userId: string): Promise<Holding[]> => {
    const response = await apiClient.get<Holding[]>(`/portfolios/${userId}`)
    return response.data
  },
}
