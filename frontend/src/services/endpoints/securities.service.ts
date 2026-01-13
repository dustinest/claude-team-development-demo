import apiClient from '../api'
import { Security } from '@/types/api.types'
import { SecurityType } from '@/types/enums'

export const securitiesService = {
  /**
   * Get all securities, optionally filtered by type
   * @param type - Optional security type filter
   * @returns Array of securities with current prices
   */
  getAll: async (type?: SecurityType): Promise<Security[]> => {
    const params = type ? { type } : {}
    const response = await apiClient.get<Security[]>('/securities', { params })
    return response.data
  },

  /**
   * Get specific security by symbol
   * @param symbol - Security symbol (e.g., "AAPL", "GOOGL")
   * @returns Security details with current price
   */
  getBySymbol: async (symbol: string): Promise<Security> => {
    const response = await apiClient.get<Security>(`/securities/${symbol}`)
    return response.data
  },
}
