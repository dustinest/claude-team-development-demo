import apiClient from '../api'
import { WalletBalance, DepositRequest, WithdrawRequest, ExchangeRequest, ExchangeResponse } from '@/types/api.types'

export const walletService = {
  /**
   * Get all wallet balances for a user
   * @param userId - UUID of the user
   * @returns Array of wallet balances for each currency
   */
  getBalances: async (userId: string): Promise<WalletBalance[]> => {
    const response = await apiClient.get<WalletBalance[]>(`/wallets/${userId}/balances`)
    return response.data
  },

  /**
   * Deposit funds into wallet
   * @param userId - UUID of the user
   * @param data - Deposit details (currency and amount)
   */
  deposit: async (userId: string, data: DepositRequest): Promise<void> => {
    await apiClient.post(`/wallets/${userId}/deposit`, data)
  },

  /**
   * Withdraw funds from wallet
   * @param userId - UUID of the user
   * @param data - Withdrawal details (currency and amount)
   */
  withdraw: async (userId: string, data: WithdrawRequest): Promise<void> => {
    await apiClient.post(`/wallets/${userId}/withdraw`, data)
  },

  /**
   * Exchange currency
   * @param userId - UUID of the user
   * @param data - Exchange details (from/to currency and amount)
   * @returns Exchange result with rates and fees
   */
  exchange: async (userId: string, data: ExchangeRequest): Promise<ExchangeResponse> => {
    const response = await apiClient.post<ExchangeResponse>(`/wallets/${userId}/exchange`, data)
    return response.data
  },
}
