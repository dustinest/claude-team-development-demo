import { Currency } from '@/types/enums'

/**
 * Custom hook for currency formatting utilities
 * @returns Object with formatCurrency function
 */
export const useCurrency = () => {
  /**
   * Format a numeric amount with currency symbol
   * @param amount - Amount as string or number
   * @param currency - Currency enum value
   * @returns Formatted string with currency symbol (e.g., "$100.50")
   */
  const formatCurrency = (amount: string | number, currency: Currency): string => {
    const numAmount = typeof amount === 'string' ? parseFloat(amount) : amount

    // Handle invalid numbers
    if (isNaN(numAmount)) {
      return '—'
    }

    const symbols: Record<Currency, string> = {
      [Currency.USD]: '$',
      [Currency.EUR]: '€',
      [Currency.GBP]: '£',
    }

    return `${symbols[currency]}${numAmount.toFixed(2)}`
  }

  /**
   * Parse currency string to number
   * @param currencyString - Formatted currency string
   * @returns Numeric value
   */
  const parseCurrency = (currencyString: string): number => {
    // Remove currency symbols and parse
    const cleanString = currencyString.replace(/[$€£,]/g, '')
    return parseFloat(cleanString)
  }

  return { formatCurrency, parseCurrency }
}
