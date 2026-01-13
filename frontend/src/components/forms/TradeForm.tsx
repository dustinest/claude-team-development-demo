import React, { useState, useEffect } from 'react'
import {
  Box,
  TextField,
  Button,
  MenuItem,
  CircularProgress,
  Typography,
  RadioGroup,
  FormControlLabel,
  Radio,
  FormLabel,
  Divider,
  Alert,
} from '@mui/material'
import { TradeType, OrderType, Currency } from '@/types/enums'
import { tradingService } from '@/services/endpoints/trading.service'
import { securitiesService } from '@/services/endpoints/securities.service'
import { useAuth } from '@/hooks/useAuth'
import { useNotification } from '@/hooks/useNotification'
import { Security } from '@/types/api.types'

interface TradeFormProps {
  tradeType: TradeType
  initialSymbol?: string
  onSuccess: () => void
  onCancel?: () => void
}

/**
 * Form for buying or selling securities
 */
const TradeForm: React.FC<TradeFormProps> = ({ tradeType, initialSymbol, onSuccess, onCancel }) => {
  const { user } = useAuth()
  const { showNotification } = useNotification()
  const [loading, setLoading] = useState(false)
  const [securities, setSecurities] = useState<Security[]>([])
  const [selectedSecurity, setSelectedSecurity] = useState<Security | null>(null)
  const [formData, setFormData] = useState({
    symbol: initialSymbol || '',
    orderType: OrderType.BY_AMOUNT,
    amount: '',
    quantity: '',
    currency: Currency.USD,
  })

  // Fetch securities list
  useEffect(() => {
    const fetchSecurities = async () => {
      try {
        const data = await securitiesService.getAll()
        setSecurities(data)

        // If initial symbol provided, find and set the security
        if (initialSymbol) {
          const security = data.find(s => s.symbol === initialSymbol)
          if (security) {
            setSelectedSecurity(security)
          }
        }
      } catch (error) {
        console.error('Failed to fetch securities:', error)
        showNotification('Failed to load securities', 'error')
      }
    }
    fetchSecurities()
  }, [initialSymbol, showNotification])

  // Update selected security when symbol changes
  useEffect(() => {
    const security = securities.find(s => s.symbol === formData.symbol)
    setSelectedSecurity(security || null)
  }, [formData.symbol, securities])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!user?.userId) {
      showNotification('User not found', 'error')
      return
    }

    if (!formData.symbol) {
      showNotification('Please select a security', 'error')
      return
    }

    const amount = formData.orderType === OrderType.BY_AMOUNT ? parseFloat(formData.amount) : undefined
    const quantity = formData.orderType === OrderType.BY_QUANTITY ? parseFloat(formData.quantity) : undefined

    if (formData.orderType === OrderType.BY_AMOUNT && (isNaN(amount!) || amount! <= 0)) {
      showNotification('Please enter a valid amount', 'error')
      return
    }

    if (formData.orderType === OrderType.BY_QUANTITY && (isNaN(quantity!) || quantity! <= 0)) {
      showNotification('Please enter a valid quantity', 'error')
      return
    }

    setLoading(true)

    try {
      const tradeRequest = {
        userId: user.userId,
        symbol: formData.symbol,
        orderType: formData.orderType,
        ...(formData.orderType === OrderType.BY_AMOUNT && { amount: amount! }),
        ...(formData.orderType === OrderType.BY_QUANTITY && { quantity: quantity! }),
        currency: formData.currency,
      }

      if (tradeType === TradeType.BUY) {
        await tradingService.buy(tradeRequest)
        showNotification(`Successfully bought ${formData.symbol}`, 'success')
      } else {
        await tradingService.sell(tradeRequest)
        showNotification(`Successfully sold ${formData.symbol}`, 'success')
      }

      onSuccess()
    } catch (error: any) {
      console.error('Trade error:', error)
      const errorMessage = error?.message || 'Trade failed. Please try again.'
      showNotification(errorMessage, 'error')
    } finally {
      setLoading(false)
    }
  }

  const getEstimatedValue = (): string => {
    if (!selectedSecurity) return '0.00'

    const price = parseFloat(selectedSecurity.currentPrice)

    if (formData.orderType === OrderType.BY_AMOUNT) {
      return formData.amount || '0.00'
    } else {
      const quantity = parseFloat(formData.quantity)
      if (isNaN(quantity)) return '0.00'
      return (price * quantity).toFixed(2)
    }
  }

  return (
    <Box component="form" onSubmit={handleSubmit}>
      {/* Security Selection */}
      <TextField
        select
        fullWidth
        label="Security"
        name="symbol"
        value={formData.symbol}
        onChange={handleChange}
        required
        margin="normal"
      >
        {securities.map((security) => (
          <MenuItem key={security.symbol} value={security.symbol}>
            {security.symbol} - {security.name}
          </MenuItem>
        ))}
      </TextField>

      {/* Current Price Display */}
      {selectedSecurity && (
        <Alert severity="info" sx={{ mt: 2 }}>
          <Typography variant="body2">
            <strong>Current Price:</strong> ${parseFloat(selectedSecurity.currentPrice).toFixed(2)}
          </Typography>
        </Alert>
      )}

      <Divider sx={{ my: 2 }} />

      {/* Order Type Selection */}
      <FormLabel component="legend" sx={{ mt: 2 }}>Order Type</FormLabel>
      <RadioGroup
        row
        name="orderType"
        value={formData.orderType}
        onChange={handleChange}
      >
        <FormControlLabel
          value={OrderType.BY_AMOUNT}
          control={<Radio />}
          label="By Amount (USD)"
        />
        <FormControlLabel
          value={OrderType.BY_QUANTITY}
          control={<Radio />}
          label="By Quantity (Shares)"
        />
      </RadioGroup>

      {/* Currency Selection */}
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

      {/* Amount or Quantity Input */}
      {formData.orderType === OrderType.BY_AMOUNT ? (
        <TextField
          fullWidth
          label="Amount (USD)"
          name="amount"
          type="number"
          value={formData.amount}
          onChange={handleChange}
          required
          margin="normal"
          inputProps={{ min: '0.01', step: '0.01' }}
          placeholder="0.00"
          helperText="Enter the dollar amount you want to trade"
        />
      ) : (
        <TextField
          fullWidth
          label="Quantity (Shares)"
          name="quantity"
          type="number"
          value={formData.quantity}
          onChange={handleChange}
          required
          margin="normal"
          inputProps={{ min: '0.01', step: '0.01' }}
          placeholder="0.00"
          helperText="Enter the number of shares (min 0.01)"
        />
      )}

      {/* Estimated Value */}
      {selectedSecurity && (
        <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
          <Typography variant="body2" color="text.secondary">
            Estimated {tradeType === TradeType.BUY ? 'Cost' : 'Proceeds'}:
          </Typography>
          <Typography variant="h6" color="primary.main">
            ${getEstimatedValue()}
          </Typography>
        </Box>
      )}

      {/* Action Buttons */}
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
          color={tradeType === TradeType.BUY ? 'primary' : 'secondary'}
          disabled={loading}
        >
          {loading ? <CircularProgress size={24} /> : `${tradeType} ${formData.symbol || 'Security'}`}
        </Button>
      </Box>
    </Box>
  )
}

export default TradeForm
