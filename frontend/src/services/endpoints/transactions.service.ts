import apiClient from '../api'
import { Transaction } from '@/types/api.types'
import { TransactionType } from '@/types/enums'

export const transactionService = {
  /**
   * Get user's transaction history, optionally filtered by type
   * @param userId - UUID of the user
   * @param type - Optional transaction type filter
   * @returns Array of transactions with details and fees
   */
  getTransactions: async (userId: string, type?: TransactionType): Promise<Transaction[]> => {
    const params = type ? { type } : {}
    const response = await apiClient.get<Transaction[]>(`/transactions/${userId}`, { params })
    return response.data
  },
}
