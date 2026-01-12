import React, { useState } from 'react'
import {
  Box,
  TextField,
  Button,
  MenuItem,
  CircularProgress,
  Typography,
} from '@mui/material'
import { SwapHoriz } from '@mui/icons-material'
import { Currency } from '@/types/enums'
import { walletService } from '@/services/endpoints/wallet.service'
import { useAuth } from '@/hooks/useAuth'
import { useNotification } from '@/hooks/useNotification'

interface ExchangeFormProps {
  onSuccess: () => void
  onCancel?: () => void
}

/**
 * Form for exchanging currencies
 */
const ExchangeForm: React.FC<ExchangeFormProps> = ({ onSuccess, onCancel }) => {
  const { user } = useAuth()
  const { showNotification } = useNotification()
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState({
    fromCurrency: Currency.USD,
    toCurrency: Currency.EUR,
    amount: '',
  })

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!user?.userId) {
      showNotification('User not found', 'error')
      return
    }

    if (formData.fromCurrency === formData.toCurrency) {
      showNotification('Please select different currencies', 'error')
      return
    }

    const amount = parseFloat(formData.amount)
    if (isNaN(amount) || amount <= 0) {
      showNotification('Please enter a valid amount', 'error')
      return
    }

    setLoading(true)

    try {
      await walletService.exchange(user.userId, {
        fromCurrency: formData.fromCurrency,
        toCurrency: formData.toCurrency,
        amount: amount,
      })
      showNotification(
        `Successfully exchanged ${formData.amount} ${formData.fromCurrency} to ${formData.toCurrency}`,
        'success'
      )
      onSuccess()
    } catch (error: any) {
      console.error('Exchange error:', error)
      const errorMessage = error?.message || 'Exchange failed. Please check your balance and try again.'
      showNotification(errorMessage, 'error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box component="form" onSubmit={handleSubmit}>
      <TextField
        select
        fullWidth
        label="From Currency"
        name="fromCurrency"
        value={formData.fromCurrency}
        onChange={handleChange}
        required
        margin="normal"
      >
        {Object.values(Currency).map((currency) => (
          <MenuItem key={currency} value={currency}>
            {currency}
          </MenuItem>
        ))}
      </TextField>

      <Box sx={{ display: 'flex', justifyContent: 'center', my: 1 }}>
        <SwapHoriz sx={{ fontSize: 32, color: 'primary.main' }} />
      </Box>

      <TextField
        select
        fullWidth
        label="To Currency"
        name="toCurrency"
        value={formData.toCurrency}
        onChange={handleChange}
        required
        margin="normal"
      >
        {Object.values(Currency).map((currency) => (
          <MenuItem
            key={currency}
            value={currency}
            disabled={currency === formData.fromCurrency}
          >
            {currency}
          </MenuItem>
        ))}
      </TextField>

      <TextField
        fullWidth
        label="Amount"
        name="amount"
        type="number"
        value={formData.amount}
        onChange={handleChange}
        required
        margin="normal"
        inputProps={{ min: '0.01', step: '0.01' }}
        placeholder="0.00"
        helperText={`Amount in ${formData.fromCurrency} to exchange`}
      />

      <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
        Exchange rates are applied at the current market rate
      </Typography>

      <Box sx={{ display: 'flex', gap: 2, mt: 3 }}>
        {onCancel && (
          <Button
            fullWidth
            variant="outlined"
            onClick={onCancel}
            disabled={loading}
          >
            Cancel
          </Button>
        )}
        <Button
          fullWidth
          type="submit"
          variant="contained"
          disabled={loading}
        >
          {loading ? <CircularProgress size={24} /> : 'Exchange'}
        </Button>
      </Box>
    </Box>
  )
}

export default ExchangeForm
