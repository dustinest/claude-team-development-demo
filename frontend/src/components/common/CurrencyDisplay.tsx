import React from 'react'
import { Typography, TypographyProps } from '@mui/material'
import { Currency } from '@/types/enums'
import { useCurrency } from '@/hooks/useCurrency'

interface CurrencyDisplayProps extends Omit<TypographyProps, 'children'> {
  amount: string | number
  currency: Currency
}

/**
 * Reusable component to display formatted currency values
 * @param amount - Amount as string or number
 * @param currency - Currency enum value
 * @param props - Additional Typography props
 */
const CurrencyDisplay: React.FC<CurrencyDisplayProps> = ({ amount, currency, ...props }) => {
  const { formatCurrency } = useCurrency()

  return (
    <Typography {...props}>
      {formatCurrency(amount, currency)}
    </Typography>
  )
}

export default CurrencyDisplay
