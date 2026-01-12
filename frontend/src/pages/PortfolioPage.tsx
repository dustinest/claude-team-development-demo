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
} from '@mui/material'
import {
  PieChart,
  Refresh,
  ShoppingCart,
} from '@mui/icons-material'
import { useAuth } from '@/hooks/useAuth'
import { portfolioService } from '@/services/endpoints/portfolio.service'
import { Holding } from '@/types/api.types'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import ErrorAlert from '@/components/common/ErrorAlert'

/**
 * Portfolio page for viewing holdings
 */
const PortfolioPage: React.FC = () => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [holdings, setHoldings] = useState<Holding[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchHoldings = async () => {
    if (!user?.userId) return

    setLoading(true)
    setError(null)

    try {
      const data = await portfolioService.getPortfolio(user.userId)
      setHoldings(data)
    } catch (err) {
      console.error('Failed to fetch holdings:', err)
      setError('Failed to load portfolio')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchHoldings()
  }, [user?.userId])

  const getTotalValue = (): string => {
    const total = holdings.reduce((sum, holding) => {
      const quantity = parseFloat(holding.quantity)
      const avgPrice = parseFloat(holding.averagePrice)
      return sum + (quantity * avgPrice)
    }, 0)
    return total.toFixed(2)
  }

  if (loading && holdings.length === 0) {
    return <LoadingSpinner />
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4" component="h1">
          <PieChart sx={{ mr: 1, verticalAlign: 'middle' }} />
          My Portfolio
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<Refresh />}
            onClick={fetchHoldings}
            disabled={loading}
          >
            Refresh
          </Button>
          <Button
            variant="contained"
            startIcon={<ShoppingCart />}
            onClick={() => navigate('/trade')}
          >
            Trade
          </Button>
        </Box>
      </Box>

      {error && <ErrorAlert message={error} />}

      {/* Total Value Card */}
      {holdings.length > 0 && (
        <Paper sx={{ p: 3, mb: 3, bgcolor: 'primary.main', color: 'white' }}>
          <Typography variant="h6" gutterBottom>
            Total Portfolio Value
          </Typography>
          <Typography variant="h3" fontWeight="bold">
            ${getTotalValue()}
          </Typography>
          <Typography variant="body2" sx={{ mt: 1, opacity: 0.9 }}>
            Based on average purchase prices
          </Typography>
        </Paper>
      )}

      {/* Holdings Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell><strong>Symbol</strong></TableCell>
              <TableCell align="right"><strong>Quantity</strong></TableCell>
              <TableCell align="right"><strong>Avg. Price</strong></TableCell>
              <TableCell align="right"><strong>Total Value</strong></TableCell>
              <TableCell align="center"><strong>Action</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {holdings.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center">
                  <Box sx={{ py: 4 }}>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      No holdings found
                    </Typography>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      Start trading to build your portfolio
                    </Typography>
                    <Button
                      variant="contained"
                      startIcon={<ShoppingCart />}
                      onClick={() => navigate('/trade')}
                      sx={{ mt: 2 }}
                    >
                      Start Trading
                    </Button>
                  </Box>
                </TableCell>
              </TableRow>
            ) : (
              holdings.map((holding) => {
                const quantity = parseFloat(holding.quantity)
                const avgPrice = parseFloat(holding.averagePrice)
                const totalValue = quantity * avgPrice

                return (
                  <TableRow key={holding.symbol} hover>
                    <TableCell>
                      <Typography variant="body1" fontWeight="bold">
                        {holding.symbol}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Chip
                        label={quantity.toFixed(2)}
                        size="small"
                        color="primary"
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="body2">
                        ${avgPrice.toFixed(2)}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="body1" fontWeight="medium">
                        ${totalValue.toFixed(2)}
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Button
                        variant="outlined"
                        size="small"
                        onClick={() => navigate('/trade', { state: { symbol: holding.symbol } })}
                      >
                        Trade
                      </Button>
                    </TableCell>
                  </TableRow>
                )
              })
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Info Box */}
      {holdings.length > 0 && (
        <Paper sx={{ p: 3, mt: 3, bgcolor: 'grey.50' }}>
          <Typography variant="body2" color="text.secondary">
            <strong>Note:</strong> Portfolio values are calculated based on your average purchase price.
            For real-time profit/loss calculations, current market prices would need to be fetched.
            You can sell any holding by clicking the "Trade" button.
          </Typography>
        </Paper>
      )}
    </Container>
  )
}

export default PortfolioPage
