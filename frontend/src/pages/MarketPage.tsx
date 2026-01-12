import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Container,
  Typography,
  Box,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  ToggleButton,
  ToggleButtonGroup,
} from '@mui/material'
import {
  TrendingUp,
  Refresh,
  ShoppingCart,
} from '@mui/icons-material'
import { securitiesService } from '@/services/endpoints/securities.service'
import { Security } from '@/types/api.types'
import { SecurityType } from '@/types/enums'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import ErrorAlert from '@/components/common/ErrorAlert'

/**
 * Market page for browsing available securities
 */
const MarketPage: React.FC = () => {
  const navigate = useNavigate()
  const [securities, setSecurities] = useState<Security[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [filter, setFilter] = useState<string>('all')

  const fetchSecurities = async () => {
    setLoading(true)
    setError(null)

    try {
      const filterValue = filter === 'all' ? undefined : (filter as SecurityType)
      const data = await securitiesService.getAll(filterValue)
      setSecurities(data)
    } catch (err) {
      console.error('Failed to fetch securities:', err)
      setError('Failed to load securities')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchSecurities()
  }, [filter])

  const handleFilterChange = (_event: React.MouseEvent<HTMLElement>, newFilter: string) => {
    if (newFilter !== null) {
      setFilter(newFilter)
    }
  }

  const handleTrade = (symbol: string) => {
    navigate('/trade', { state: { symbol } })
  }

  if (loading && securities.length === 0) {
    return <LoadingSpinner />
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4" component="h1">
          <TrendingUp sx={{ mr: 1, verticalAlign: 'middle' }} />
          Market
        </Typography>
        <Button
          variant="outlined"
          startIcon={<Refresh />}
          onClick={fetchSecurities}
          disabled={loading}
        >
          Refresh
        </Button>
      </Box>

      {error && <ErrorAlert message={error} />}

      {/* Filter */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="subtitle1" gutterBottom>
          Filter by Type:
        </Typography>
        <ToggleButtonGroup
          value={filter}
          exclusive
          onChange={handleFilterChange}
          aria-label="security type filter"
        >
          <ToggleButton value="all">
            All
          </ToggleButton>
          {Object.values(SecurityType).map((type) => (
            <ToggleButton key={type} value={type}>
              {type}
            </ToggleButton>
          ))}
        </ToggleButtonGroup>
      </Box>

      {/* Securities Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell><strong>Symbol</strong></TableCell>
              <TableCell><strong>Name</strong></TableCell>
              <TableCell><strong>Type</strong></TableCell>
              <TableCell align="right"><strong>Current Price</strong></TableCell>
              <TableCell align="center"><strong>Action</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {securities.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center">
                  <Typography variant="body2" color="text.secondary" sx={{ py: 3 }}>
                    No securities found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              securities.map((security) => (
                <TableRow key={security.symbol} hover>
                  <TableCell>
                    <Typography variant="body1" fontWeight="bold">
                      {security.symbol}
                    </Typography>
                  </TableCell>
                  <TableCell>{security.name}</TableCell>
                  <TableCell>
                    <Chip
                      label={security.type}
                      size="small"
                      color={security.type === SecurityType.STOCK ? 'primary' : 'default'}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Typography variant="body1" fontWeight="medium">
                      ${parseFloat(security.currentPrice).toFixed(2)}
                    </Typography>
                  </TableCell>
                  <TableCell align="center">
                    <Button
                      variant="contained"
                      size="small"
                      startIcon={<ShoppingCart />}
                      onClick={() => handleTrade(security.symbol)}
                    >
                      Trade
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Info Box */}
      <Paper sx={{ p: 3, mt: 3, bgcolor: 'grey.50' }}>
        <Typography variant="body2" color="text.secondary">
          <strong>Note:</strong> Prices shown are current market prices. You can buy fractional shares
          with as little as 0.01 shares (2 decimal precision). Click "Trade" to start buying or selling.
        </Typography>
      </Paper>
    </Container>
  )
}

export default MarketPage
