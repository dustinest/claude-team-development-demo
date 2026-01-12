import React, { useState } from 'react'
import {
  Box,
  TextField,
  Button,
  MenuItem,
  CircularProgress,
} from '@mui/material'
import { Currency } from '@/types/enums'
import { walletService } from '@/services/endpoints/wallet.service'
import { useAuth } from '@/hooks/useAuth'
import { useNotification } from '@/hooks/useNotification'

interface DepositFormProps {
  onSuccess: () => void
  onCancel?: () => void
}

/**
 * Form for depositing funds into wallet
 */
const DepositForm: React.FC<DepositFormProps> = ({ onSuccess, onCancel }) => {
  const { user } = useAuth()
  const { showNotification } = useNotification()
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState({
    currency: Currency.USD,
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

    const amount = parseFloat(formData.amount)
    if (isNaN(amount) || amount <= 0) {
      showNotification('Please enter a valid amount', 'error')
      return
    }

    setLoading(true)

    try {
      await walletService.deposit(user.userId, {
        currency: formData.currency,
        amount: amount,
      })
      showNotification(`Successfully deposited ${formData.amount} ${formData.currency}`, 'success')
      onSuccess()
    } catch (error: any) {
      console.error('Deposit error:', error)
      const errorMessage = error?.message || 'Deposit failed. Please try again.'
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
        label="Currency"
        name="currency"
        value={formData.currency}
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
        helperText="Minimum deposit: 0.01"
      />

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
          {loading ? <CircularProgress size={24} /> : 'Deposit'}
        </Button>
      </Box>
    </Box>
  )
}

export default DepositForm
