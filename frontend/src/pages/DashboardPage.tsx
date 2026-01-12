import React from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Container,
  Typography,
  Box,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Paper,
} from '@mui/material'
import {
  AccountBalanceWallet,
  TrendingUp,
  ShoppingCart,
  PieChart,
  Receipt,
} from '@mui/icons-material'
import { useAuth } from '@/hooks/useAuth'
import LoadingSpinner from '@/components/common/LoadingSpinner'

/**
 * User dashboard with welcome message and quick action buttons
 */
const DashboardPage: React.FC = () => {
  const navigate = useNavigate()
  const { user, isLoading } = useAuth()

  if (isLoading) {
    return <LoadingSpinner />
  }

  const quickActions = [
    {
      title: 'Wallet',
      description: 'Manage your funds, deposit, withdraw, and exchange currencies',
      icon: <AccountBalanceWallet sx={{ fontSize: 48, color: 'primary.main' }} />,
      path: '/wallet',
      color: '#1976d2',
    },
    {
      title: 'Market',
      description: 'Browse available securities and view real-time prices',
      icon: <TrendingUp sx={{ fontSize: 48, color: 'success.main' }} />,
      path: '/market',
      color: '#2e7d32',
    },
    {
      title: 'Trade',
      description: 'Buy and sell fractional shares of your favorite stocks',
      icon: <ShoppingCart sx={{ fontSize: 48, color: 'secondary.main' }} />,
      path: '/trade',
      color: '#dc004e',
    },
    {
      title: 'Portfolio',
      description: 'View your holdings and track your investments',
      icon: <PieChart sx={{ fontSize: 48, color: 'warning.main' }} />,
      path: '/portfolio',
      color: '#ed6c02',
    },
    {
      title: 'Transactions',
      description: 'Review your complete transaction history',
      icon: <Receipt sx={{ fontSize: 48, color: 'info.main' }} />,
      path: '/transactions',
      color: '#0288d1',
    },
  ]

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Welcome Section */}
      <Paper sx={{ p: 4, mb: 4, bgcolor: 'primary.main', color: 'white' }}>
        <Typography variant="h3" component="h1" gutterBottom>
          Welcome back, {user?.username || 'Trader'}!
        </Typography>
        <Typography variant="h6" sx={{ opacity: 0.9 }}>
          Ready to trade fractional stocks?
        </Typography>
      </Paper>

      {/* Quick Actions */}
      <Typography variant="h5" gutterBottom sx={{ mb: 3 }}>
        Quick Actions
      </Typography>

      <Grid container spacing={3}>
        {quickActions.map((action) => (
          <Grid key={action.path} size={{ xs: 12, sm: 6, md: 4 }}>
            <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
              <CardContent sx={{ flexGrow: 1, textAlign: 'center' }}>
                <Box sx={{ mb: 2 }}>{action.icon}</Box>
                <Typography variant="h6" gutterBottom>
                  {action.title}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {action.description}
                </Typography>
              </CardContent>
              <CardActions>
                <Button
                  fullWidth
                  variant="contained"
                  onClick={() => navigate(action.path)}
                  sx={{ m: 1 }}
                >
                  Go to {action.title}
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* User Info */}
      <Paper sx={{ p: 3, mt: 4 }}>
        <Typography variant="h6" gutterBottom>
          Account Information
        </Typography>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          <Typography variant="body1">
            <strong>Username:</strong> {user?.username}
          </Typography>
          <Typography variant="body1">
            <strong>Email:</strong> {user?.email}
          </Typography>
          <Typography variant="body1">
            <strong>Phone:</strong> {user?.phoneNumber}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            User ID: {user?.userId}
          </Typography>
        </Box>
      </Paper>
    </Container>
  )
}

export default DashboardPage
