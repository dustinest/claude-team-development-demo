import apiClient from '../api'
import { TradeRequest, TradeResponse } from '@/types/api.types'

export const tradingService = {
  /**
   * Execute a buy order
   * @param data - Trade request with userId, symbol, currency, order type, and amount/quantity
   * @returns Trade result with execution details
   */
  buy: async (data: TradeRequest): Promise<TradeResponse> => {
    const response = await apiClient.post<TradeResponse>('/trades/buy', data)
    return response.data
  },

  /**
   * Execute a sell order
   * @param data - Trade request with userId, symbol, currency, order type, and amount/quantity
   * @returns Trade result with execution details
   */
  sell: async (data: TradeRequest): Promise<TradeResponse> => {
    const response = await apiClient.post<TradeResponse>('/trades/sell', data)
    return response.data
  },
}
