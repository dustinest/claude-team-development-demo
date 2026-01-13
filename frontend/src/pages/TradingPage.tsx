import React, { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import {
  Container,
  Typography,
  Box,
  Paper,
  Tabs,
  Tab,
} from '@mui/material'
import { ShoppingCart, TrendingUp, TrendingDown } from '@mui/icons-material'
import { TradeType } from '@/types/enums'
import TradeForm from '@/components/forms/TradeForm'

interface TabPanelProps {
  children?: React.ReactNode
  index: number
  value: number
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (
    <div hidden={value !== index} role="tabpanel">
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  )
}

/**
 * Trading page with tabs for buying and selling
 */
const TradingPage: React.FC = () => {
  const location = useLocation()
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState(0)

  // Get initial symbol from navigation state (if coming from Market page)
  const initialSymbol = location.state?.symbol as string | undefined

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue)
  }

  const handleTradeSuccess = () => {
    // Navigate to portfolio after successful trade
    navigate('/portfolio')
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4, textAlign: 'center' }}>
        <Typography variant="h4" component="h1" gutterBottom>
          <ShoppingCart sx={{ mr: 1, verticalAlign: 'middle' }} />
          Trade Securities
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Buy and sell fractional shares with as little as 0.01 shares
        </Typography>
      </Box>

      {/* Tabs */}
      <Paper>
        <Tabs
          value={activeTab}
          onChange={handleTabChange}
          variant="fullWidth"
          indicatorColor="primary"
        >
          <Tab
            icon={<TrendingUp />}
            label="Buy"
            iconPosition="start"
          />
          <Tab
            icon={<TrendingDown />}
            label="Sell"
            iconPosition="start"
          />
        </Tabs>

        <Box sx={{ p: 3 }}>
          {/* Buy Tab */}
          <TabPanel value={activeTab} index={0}>
            <TradeForm
              tradeType={TradeType.BUY}
              initialSymbol={initialSymbol}
              onSuccess={handleTradeSuccess}
            />
          </TabPanel>

          {/* Sell Tab */}
          <TabPanel value={activeTab} index={1}>
            <TradeForm
              tradeType={TradeType.SELL}
              initialSymbol={initialSymbol}
              onSuccess={handleTradeSuccess}
            />
          </TabPanel>
        </Box>
      </Paper>

      {/* Info Box */}
      <Paper sx={{ p: 3, mt: 3, bgcolor: 'grey.50' }}>
        <Typography variant="body2" color="text.secondary">
          <strong>Trading Information:</strong>
        </Typography>
        <Typography variant="body2" color="text.secondary" component="ul" sx={{ mt: 1, pl: 2 }}>
          <li>Minimum trade quantity: 0.01 shares</li>
          <li>Fractional shares supported (2 decimal precision)</li>
          <li>Choose to trade by amount (USD) or by quantity (shares)</li>
          <li>Transactions are processed in real-time</li>
          <li>View your holdings in the Portfolio page after trading</li>
        </Typography>
      </Paper>
    </Container>
  )
}

export default TradingPage
