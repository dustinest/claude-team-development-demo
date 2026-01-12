import React, { useState, useEffect } from 'react'
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
  Receipt,
  Refresh,
} from '@mui/icons-material'
import { format } from 'date-fns'
import { useAuth } from '@/hooks/useAuth'
import { transactionService } from '@/services/endpoints/transactions.service'
import { Transaction } from '@/types/api.types'
import { TransactionType } from '@/types/enums'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import ErrorAlert from '@/components/common/ErrorAlert'

/**
 * Transactions page for viewing transaction history
 */
const TransactionsPage: React.FC = () => {
  const { user } = useAuth()
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [filter, setFilter] = useState<string>('all')

  const fetchTransactions = async () => {
    if (!user?.userId) return

    setLoading(true)
    setError(null)

    try {
      const filterValue = filter === 'all' ? undefined : (filter as TransactionType)
      const data = await transactionService.getTransactions(user.userId, filterValue)
      // Sort by date descending (most recent first)
      const sorted = data.sort((a, b) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      )
      setTransactions(sorted)
    } catch (err) {
      console.error('Failed to fetch transactions:', err)
      setError('Failed to load transactions')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchTransactions()
  }, [user?.userId, filter])

  const handleFilterChange = (_event: React.MouseEvent<HTMLElement>, newFilter: string) => {
    if (newFilter !== null) {
      setFilter(newFilter)
    }
  }

  const getTransactionTypeColor = (type: TransactionType): 'success' | 'error' | 'primary' | 'info' | 'warning' => {
    switch (type) {
      case TransactionType.DEPOSIT:
        return 'success'
      case TransactionType.WITHDRAWAL:
        return 'error'
      case TransactionType.BUY:
        return 'primary'
      case TransactionType.SELL:
        return 'warning'
      case TransactionType.CURRENCY_EXCHANGE:
        return 'info'
      default:
        return 'primary'
    }
  }

  const formatDate = (dateString: string): string => {
    try {
      return format(new Date(dateString), 'MMM dd, yyyy HH:mm')
    } catch {
      return dateString
    }
  }

  if (loading && transactions.length === 0) {
    return <LoadingSpinner />
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4" component="h1">
          <Receipt sx={{ mr: 1, verticalAlign: 'middle' }} />
          Transaction History
        </Typography>
        <Button
          variant="outlined"
          startIcon={<Refresh />}
          onClick={fetchTransactions}
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
          aria-label="transaction type filter"
        >
          <ToggleButton value="all">
            All
          </ToggleButton>
          {Object.values(TransactionType).map((type) => (
            <ToggleButton key={type} value={type}>
              {type}
            </ToggleButton>
          ))}
        </ToggleButtonGroup>
      </Box>

      {/* Transactions Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell><strong>Date</strong></TableCell>
              <TableCell><strong>Type</strong></TableCell>
              <TableCell align="right"><strong>Amount</strong></TableCell>
              <TableCell align="right"><strong>Fees</strong></TableCell>
              <TableCell><strong>Currency</strong></TableCell>
              <TableCell><strong>ID</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {transactions.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  <Typography variant="body2" color="text.secondary" sx={{ py: 3 }}>
                    No transactions found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              transactions.map((transaction) => (
                <TableRow key={transaction.id} hover>
                  <TableCell>
                    <Typography variant="body2">
                      {formatDate(transaction.createdAt)}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={transaction.type}
                      size="small"
                      color={getTransactionTypeColor(transaction.type)}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Typography variant="body1" fontWeight="medium">
                      ${parseFloat(transaction.amount).toFixed(2)}
                    </Typography>
                  </TableCell>
                  <TableCell align="right">
                    <Typography variant="body2" color="text.secondary">
                      ${parseFloat(transaction.fees).toFixed(2)}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={transaction.currency}
                      size="small"
                      variant="outlined"
                    />
                  </TableCell>
                  <TableCell>
                    <Typography variant="caption" color="text.secondary" sx={{ fontFamily: 'monospace' }}>
                      {transaction.id.substring(0, 8)}...
                    </Typography>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Summary */}
      {transactions.length > 0 && (
        <Paper sx={{ p: 3, mt: 3, bgcolor: 'grey.50' }}>
          <Typography variant="body2" color="text.secondary">
            <strong>Total Transactions:</strong> {transactions.length}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Transactions are sorted by date (most recent first). Use the filter to view specific transaction types.
          </Typography>
        </Paper>
      )}
    </Container>
  )
}

export default TransactionsPage
