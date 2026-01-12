import React, { useState, useEffect } from 'react'
import {
  Container,
  Typography,
  Box,
  Grid,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material'
import {
  AccountBalanceWallet,
  Add,
  Remove,
  SwapHoriz,
  Refresh,
} from '@mui/icons-material'
import { useAuth } from '@/hooks/useAuth'
import { walletService } from '@/services/endpoints/wallet.service'
import { WalletBalance } from '@/types/api.types'
import { Currency } from '@/types/enums'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import ErrorAlert from '@/components/common/ErrorAlert'
import CurrencyDisplay from '@/components/common/CurrencyDisplay'
import DepositForm from '@/components/forms/DepositForm'
import WithdrawForm from '@/components/forms/WithdrawForm'
import ExchangeForm from '@/components/forms/ExchangeForm'

/**
 * Wallet page for managing funds
 */
const WalletPage: React.FC = () => {
  const { user } = useAuth()
  const [balances, setBalances] = useState<WalletBalance[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [depositDialogOpen, setDepositDialogOpen] = useState(false)
  const [withdrawDialogOpen, setWithdrawDialogOpen] = useState(false)
  const [exchangeDialogOpen, setExchangeDialogOpen] = useState(false)

  const fetchBalances = async () => {
    if (!user?.userId) return

    setLoading(true)
    setError(null)

    try {
      const data = await walletService.getBalances(user.userId)
      setBalances(data)
    } catch (err) {
      console.error('Failed to fetch balances:', err)
      setError('Failed to load wallet balances')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchBalances()
  }, [user?.userId])

  const handleOperationSuccess = () => {
    setDepositDialogOpen(false)
    setWithdrawDialogOpen(false)
    setExchangeDialogOpen(false)
    fetchBalances()
  }

  if (loading && balances.length === 0) {
    return <LoadingSpinner />
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4" component="h1">
          <AccountBalanceWallet sx={{ mr: 1, verticalAlign: 'middle' }} />
          My Wallet
        </Typography>
        <Button
          variant="outlined"
          startIcon={<Refresh />}
          onClick={fetchBalances}
          disabled={loading}
        >
          Refresh
        </Button>
      </Box>

      {error && <ErrorAlert message={error} />}

      {/* Quick Actions */}
      <Grid container spacing={2} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Button
            fullWidth
            variant="contained"
            color="primary"
            startIcon={<Add />}
            onClick={() => setDepositDialogOpen(true)}
            sx={{ py: 1.5 }}
          >
            Deposit
          </Button>
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Button
            fullWidth
            variant="contained"
            color="secondary"
            startIcon={<Remove />}
            onClick={() => setWithdrawDialogOpen(true)}
            sx={{ py: 1.5 }}
          >
            Withdraw
          </Button>
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Button
            fullWidth
            variant="contained"
            color="info"
            startIcon={<SwapHoriz />}
            onClick={() => setExchangeDialogOpen(true)}
            sx={{ py: 1.5 }}
          >
            Exchange
          </Button>
        </Grid>
      </Grid>

      {/* Balances Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell><strong>Currency</strong></TableCell>
              <TableCell align="right"><strong>Balance</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {balances.length === 0 ? (
              <TableRow>
                <TableCell colSpan={2} align="center">
                  <Typography variant="body2" color="text.secondary" sx={{ py: 3 }}>
                    No balances found. Deposit funds to get started.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              balances.map((balance) => (
                <TableRow key={balance.currency}>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Typography variant="h6">{balance.currency}</Typography>
                    </Box>
                  </TableCell>
                  <TableCell align="right">
                    <CurrencyDisplay
                      amount={balance.balance}
                      currency={balance.currency as Currency}
                    />
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Deposit Dialog */}
      <Dialog
        open={depositDialogOpen}
        onClose={() => setDepositDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Deposit Funds</DialogTitle>
        <DialogContent>
          <DepositForm
            onSuccess={handleOperationSuccess}
            onCancel={() => setDepositDialogOpen(false)}
          />
        </DialogContent>
      </Dialog>

      {/* Withdraw Dialog */}
      <Dialog
        open={withdrawDialogOpen}
        onClose={() => setWithdrawDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Withdraw Funds</DialogTitle>
        <DialogContent>
          <WithdrawForm
            onSuccess={handleOperationSuccess}
            onCancel={() => setWithdrawDialogOpen(false)}
          />
        </DialogContent>
      </Dialog>

      {/* Exchange Dialog */}
      <Dialog
        open={exchangeDialogOpen}
        onClose={() => setExchangeDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Exchange Currency</DialogTitle>
        <DialogContent>
          <ExchangeForm
            onSuccess={handleOperationSuccess}
            onCancel={() => setExchangeDialogOpen(false)}
          />
        </DialogContent>
      </Dialog>
    </Container>
  )
}

export default WalletPage
